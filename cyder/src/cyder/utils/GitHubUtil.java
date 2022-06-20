package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities involving Rest APIs for GitHub.
 */
public class GitHubUtil {
    /**
     * Suppress default constructor.
     */
    private GitHubUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The gson object used to serialize issue github responses.
     */
    private static final Gson gson = new Gson();

    /**
     * Returns a list of issues for Cyder.
     *
     * @return the list of currently open issues for Cyder.
     */
    public static Issue[] getIssues() {
        Issue[] ret = null;

        try {
            String urlString = CyderUrls.CYDER_ISSUES;
            URL url = new URL(urlString);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            String rawJSON = sb.toString();

            ret = gson.fromJson(rawJSON, Issue[].class);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * A json object class for a GitHub Issue object.
     */
    @SuppressWarnings("unused")
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
     * Determines if the provided url is a valid and public github cloneable repository.
     * Example: https://github.com/NathanCheshire/Cyder.git returns true
     *
     * @param url the url to clone locally
     * @return whether the url is a valid, public, and cloneable repository
     */
    public static boolean validateGitHubRepoCloneUrl(String url) {
        Preconditions.checkNotNull(url, "Url is null");
        Preconditions.checkArgument(!url.isEmpty(), "Url is empty");

        // first test if it will even work as a url
        if (!NetworkUtil.isValidUrl(url))
            return false;

        // make sure it ends with .git
        if (!url.endsWith(".git"))
            return false;

        // http://url or https://
        if (url.contains("://")) {
            String[] parts = url.split("://");

            // malformed Url so this shouldn't even be possible
            if (parts.length != 2)
                return false;

            // remove protocol from url
            url = parts[1];
        }

        // at this point it should be one of the following
        // github.com/user/repo.git or www.github.com/user/repo.git
        return url.startsWith(CyderUrls.GITHUB_BASE)
                || url.startsWith(CyderUrls.GITHUB_BASE.substring(4)); // valid url and starts with one of the above
    }

    /**
     * Executor service used to clone github repos.
     */
    private static final ExecutorService cloningExecutor =
            Executors.newSingleThreadScheduledExecutor(new CyderThreadFactory("Git Repo Cloner"));

    /**
     * Clones the provided github repo to the provided directory.
     *
     * @param githubRepo the URL of the github repository to clone
     * @param directory  the directory to save the repo to.
     *                   Note this directory must exist prior to method invocation
     * @return whether the repo was successfully cloned and saved
     */
    public static Future<Optional<Boolean>> cloneRepoToDirectory(String githubRepo, File directory) {
        Preconditions.checkNotNull(githubRepo);
        Preconditions.checkArgument(!githubRepo.isEmpty());
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        return cloningExecutor.submit(() -> {
            ConsoleFrame.INSTANCE.getInputHandler().println("Validating github link: " + githubRepo);

            if (!validateGitHubRepoCloneUrl(githubRepo)) {
                ConsoleFrame.INSTANCE.getInputHandler().println("Provided repo link is invalid");
                return Optional.of(Boolean.FALSE);
            }

            //this shouldn't be possible
            if (!githubRepo.contains("/"))
                return Optional.of(Boolean.FALSE);

            String[] parts = githubRepo.split("/");

            String repoName = parts[parts.length - 1];

            //shouldn't be possible
            if (!repoName.endsWith(".git"))
                return Optional.of(Boolean.FALSE);

            repoName = repoName.replace(".git", "");

            //folder name is index of last / to the .git
            File saveDir = new File(directory.getAbsolutePath()
                    + OSUtil.FILE_SEP + repoName);

            if (!saveDir.exists()) {
                if (!saveDir.mkdirs()) {
                    throw new IOException("Failed to create save directory: " + saveDir.getAbsolutePath());
                }
            }

            ConsoleFrame.INSTANCE.getInputHandler().println("Checking for git");

            if (!OSUtil.isBinaryInstalled("git")) {
                ConsoleFrame.INSTANCE.getInputHandler()
                        .println("Git not installed. Please install it at: " + CyderUrls.GIT_DOWNLOAD);
                return Optional.of(Boolean.FALSE);
            }

            ConsoleFrame.INSTANCE.getInputHandler().println("Cloning: \"" + NetworkUtil.getUrlTitle(githubRepo)
                    + "\" to \"" + saveDir.getName() + OSUtil.FILE_SEP + "\"");

            try {
                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec("git clone " + githubRepo + " " + saveDir.getAbsolutePath());

                proc.waitFor();

            } catch (Exception e) {
                ExceptionHandler.handle(e);
                return Optional.of(Boolean.FALSE);
            }

            return Optional.of(Boolean.TRUE);
        });
    }
}
