package alexiil.mods.load.git;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;

public class SiteRequester {
    private static final String LOGIN = "\"login\":\"";// "login":"
    private static final String AVATAR = "\"avatar_url\":\"";// "avatar_url":"
    private static final String COMMITS = "\"contributions\":";// "contributions":
    private static final String URL = "\"url\":\"";// "url":"
    public static String accessToken = null;

    private static final Map<String, GitHubUser> usersCache = new HashMap<String, GitHubUser>();

    public static List<GitHubUser> getContributors(String site) {
        String response = getResponse(site);
        if (response == null)
            return Collections.emptyList();
        List<GitHubUser> users = parseContributors(response);
        Collections.sort(users, new Comparator<GitHubUser>() {
            @Override
            public int compare(GitHubUser o1, GitHubUser o2) {
                return o2.commits - o1.commits;
            }
        });
        for (GitHubUser usr : users)
            usersCache.put(usr.login, usr);
        return users;
    }

    public static List<Commit> getCommits(String site) {
        String response = getResponse(site);
        if (response == null)
            return Collections.emptyList();
        Commit[] commits = new GsonBuilder().create().fromJson(response, Commit[].class);
        for (int i = 0; i < commits.length; i++) {
            commits[i] = populateUser(commits[i]);
        }
        return Arrays.asList(commits);
    }

    public static List<Release> getReleases(String site) {
        String response = getResponse(site);
        if (response == null)
            return Collections.emptyList();
        Release[] releases = new GsonBuilder().create().fromJson(response, Release[].class);
        return Arrays.asList(releases);
    }

    private static Commit populateUser(Commit c) {
        if (!usersCache.containsKey(c.author.login))
            return c;
        CommitInfo ci = c.commit;
        String sha = c.sha;
        String url = c.url;
        String author = c.author.login;
        return new Commit(url, ci, sha, usersCache.get(author));
    }

    private static List<GitHubUser> parseContributors(String s) {
        String[] strings = s.split("\\},\\{");
        List<GitHubUser> lst = new ArrayList<GitHubUser>();
        for (String string : strings) {
            if (string.startsWith("[{"))
                string = string.substring(2);
            if (string.endsWith("}]"))
                string = string.substring(0, string.length() - 2);

            String name = "";
            String avatarUrl = "";
            String url = "";
            int contributions = 0;

            for (String tag : string.split(",")) {
                if (tag.startsWith(LOGIN))
                    name = tag.substring(LOGIN.length(), tag.length() - 1);
                if (tag.startsWith(AVATAR))
                    avatarUrl = tag.substring(AVATAR.length(), tag.length() - 1);
                if (tag.startsWith(COMMITS))
                    contributions = Integer.parseInt(tag.substring(COMMITS.length(), tag.length()));
                if (tag.startsWith(URL))
                    url = tag.substring(URL.length(), tag.length() - 1);
            }

            lst.add(new GitHubUser(name, avatarUrl, url, contributions));
        }
        return lst;
    }

    public static String getResponse(String site) {
        try {
            URLConnection url = new URL(site).openConnection();
            InputStream response = url.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(response, Charset.forName("UTF-8")));
            String s = "";
            String temp;
            while (true) {
                temp = br.readLine();
                if (temp == null)
                    break;
                s += temp;
            }
            return s;
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
