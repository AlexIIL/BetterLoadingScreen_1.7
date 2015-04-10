package alexiil.mods.load.git;

public class CommitInfo {
    public static class Commiter {
        public final String name, email, date;

        public Commiter(String name, String email, String date) {
            this.name = name;
            this.email = email;
            this.date = date;
        }
    }

    public static class Tree {
        public final String sha, url;

        public Tree(String sha, String url) {
            this.sha = sha;
            this.url = url;
        }
    }

    public final String message, url;
    public final int comment_count;
    public final Commiter committer, author;
    public final Tree tree;

    public CommitInfo(String message, String url, int comment_count, Commiter commiter, Commiter author, Tree tree) {
        this.message = message;
        this.url = url;
        this.comment_count = comment_count;
        this.committer = commiter;
        this.author = author;
        this.tree = tree;
    }
}
