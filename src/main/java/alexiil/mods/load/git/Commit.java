package alexiil.mods.load.git;

public class Commit {
    public final CommitInfo commit;
    public final String sha;
    public final String url;
    public final GitHubUser author;

    public Commit(String url, CommitInfo message, String id, GitHubUser user) {
        this.commit = message;
        this.sha = id;
        this.url = url;
        author = user;
    }
}
