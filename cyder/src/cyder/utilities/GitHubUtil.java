package cyder.utilities;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

public class GitHubUtil {
    private GitHubUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static Issue[] getIssues() {
        Issue[] ret = null;

        try {
            String urlString = "https://api.github.com/repos/nathancheshire/cyder/issues";
            URL url = new URL(urlString);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            String rawJSON = sb.toString();

            ret = new Gson().fromJson(rawJSON, Issue[].class);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    public static class Issue {
        public String url;
        public String repository_url;
        public String labels_url;
        public String comments_url;
        public String events_url;
        public String html_url;
        public int id;
        public String node_id;
        public int number;
        public String title;
        public Issue.User user;
        public LinkedList<String> labels;
        public String state;
        public boolean locked;
        public String assignee;
        public LinkedList<String> assignees;
        public boolean milestone;
        public int comments;
        public String created_at;
        public String updated_at;
        public String closed_at;
        public String author_association;
        public boolean active_lock_reason;
        public String body;
        public Reaction reactions;
        public String timeline_url;
        public boolean performed_via_github_app;

        public static class User {
            public String login;
            public int id;
            public String node_id;
            public String avatar_url;
            public String gravatar_id;
            public String url;
            public String html_url;
            public String following_url;
            public String followers_url;
            public String gists_url;
            public String starred_url;
            public String subscriptions_url;
            public String organizations_url;
            public String repos_url;
            public String events_url;
            public String received_events_url;
            public String type;
            public boolean site_admin;
        }

        public static class Reaction {
            public String url;
            public int total_count;

            @SerializedName("+1")
            public int plusOne;

            @SerializedName("-1")
            public int minusOne;

            public int laugh;
            public int hooray;
            public int confused;
            public int heart;
            public int rocket;
            public int eyes;
        }
    }

    /**
     * Determines if the provided url is a valid and public github clonable repository.
     * Example: https://github.com/NathanCheshire/Cyder.git returns true
     *
     * @param url the url to clone locally
     * @return whether or not the url is a valid, public, and cloneable repository
     */
    public static boolean validateGitHubURL(String url) {
        if (!url.contains("://"))
            return false;

        String parts[] = url.split("://");

        if (parts.length != 2)
            return false;

        if (!parts[1].startsWith("github.com") && !parts[1].startsWith("www.github.com"))
            return false;

        if (!parts[1].endsWith(".git"))
            return false;

        return NetworkUtil.isURL(url);
    }

    /**
     * Clones the provided github repo to the provided directory.
     *
     * @param githubRepo the URL of the github repository to clone
     * @param directory the directory to save the repo to
     * @return whether or not the repo was successfully cloned and saved
     */
    public static boolean cloneRepoToDirectory(String githubRepo, File directory) {
        if (!validateGitHubURL(githubRepo))
            throw new IllegalArgumentException("provided repo link is invalid");
        else if (directory.exists())
            directory.mkdir();

        System.out.println("Cloning: " + NetworkUtil.getURLTitle(githubRepo)
                + " to " + directory.getName() + OSUtil.FILE_SEP);

        

        return false;
    }
}
