package cyder.utilities;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;

public class GitHubUtil {
    public static LinkedList<Issue> getIssues() {
        LinkedList<Issue> ret = new LinkedList<>();

        //todo https://api.github.com/repos/nathancheshire/cyder/issues

        return ret;
    }

    private static class Issue {
        private String url;
        private String repository_url;
        private String labels_url;
        private String comments_url;
        private String events_url;
        private String html_url;
        private int id;
        private String node_id;
        private int number;
        private String title;
        private Issue.User user;
        private LinkedList<String> labels;
        private boolean milestone;
        private int comments;
        private String created_at;
        private String updated_at;
        private String closed_at;
        private String author_association;
        private boolean active_lock_reason;
        private String body;
        private Reaction reactions;
        private String timeline_url;
        private boolean performed_via_github_app;

        private static class User {
            private String login;
            private int id;
            private String node_id;
            private String avatar_url;
            private String gravatar_id;
            private String url;
            private String html_url;
            private String following_url;
            private String followers_url;
            private String gists_url;
            private String starred_url;
            private String subscriptions_url;
            private String organizations_url;
            private String repos_url;
            private String events_url;
            private String received_events_url;
            private String type;
            private boolean site_admin;
        }

        private static class Reaction {
            private String url;
            private int total_count;

            @SerializedName("+1")
            private int plusOne;

            @SerializedName("-1")
            private int minusOne;

            private int laugh;
            private int hooray;
            private int confused;
            private int heart;
            private int rocket;
            private int eyes;
        }
    }
}
