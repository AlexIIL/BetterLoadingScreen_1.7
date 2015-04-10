package alexiil.mods.load.git;

public class GitHubUser {
    public final String login;
    public final int commits;// May use this, not sure
    public final String avatar_url;// Might use this in a new Gui
    public final String url;// Useful if anyone wants to get more info on a person

    public GitHubUser(String name, String avatarUrl, String githubUrl, int commits) {
        this.login = name;
        this.avatar_url = avatarUrl;
        this.url = githubUrl;
        this.commits = commits;
    }
}
