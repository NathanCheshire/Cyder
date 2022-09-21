package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.remote.github.Issue;
import cyder.threads.CyderThreadFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utilities involving Rest APIs for GitHub.
 */
public final class GitHubUtil {
    /**
     * Suppress default constructor.
     */
    private GitHubUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The link for the github api to return a json of currently open issues for Cyder.
     */
    private static final String CYDER_ISSUES = "https://api.github.com/repos/nathancheshire/cyder/issues";

    /**
     * The link to download git from.
     */
    private static final String GIT_DOWNLOAD = "https://git-scm.com/downloads";

    /**
     * Returns a list of issues for Cyder.
     *
     * @return the list of currently open issues for Cyder
     */
    public static ImmutableList<Issue> getIssues() {
        Issue[] ret = null;

        try {
            URL url = new URL(CYDER_ISSUES);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            String rawJson = sb.toString();

            ret = SerializationUtil.fromJson(rawJson, Issue[].class);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret == null ? ImmutableList.of() : ImmutableList.copyOf(ret);
    }

    /**
     * The url to get the languages used throughout Cyder from.
     */
    private static final String LANGUAGES_URL = "https://api.github.com/repos/nathancheshire/cyder/languages";

    /**
     * Returns the languages used to code Cyder along with the raw number of bytes of the respective language.
     *
     * @return the languages used to code Cyder along with the raw number of bytes of the respective language
     */
    public static HashMap<String, Integer> getLanguages() {
        HashMap<String, Integer> ret = new HashMap<>();

        try {
            URL url = new URL(LANGUAGES_URL);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            String rawJson = sb.toString();

            Type type = new TypeToken<HashMap<String, Integer>>() {}.getType();

            ret = SerializationUtil.fromJson(rawJson, type);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * The default github url.
     */
    private static final String GITHUB_BASE = "www.github.com";

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
        if (!NetworkUtil.isValidUrl(url)) {
            return false;
        }

        // make sure it ends with .git
        if (!url.endsWith(".git")) {
            return false;
        }

        // http://url or https://
        if (url.contains("://")) {
            String[] parts = url.split("://");

            // malformed url so this shouldn't even be possible
            if (parts.length != 2) {
                return false;
            }

            // remove protocol from url
            url = parts[1];
        }

        // at this point it should be one of the following
        // github.com/user/repo.git or www.github.com/user/repo.git
        return url.startsWith(GITHUB_BASE)
                || url.startsWith(GITHUB_BASE.substring(4)); // valid url and starts with one of the above
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
     * @param directory  the directory to save the repo to
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
            Console.INSTANCE.getInputHandler().println("Validating github link: " + githubRepo);

            if (!validateGitHubRepoCloneUrl(githubRepo)) {
                Console.INSTANCE.getInputHandler().println("Provided repo link is invalid");
                return Optional.of(Boolean.FALSE);
            }

            //this shouldn't be possible
            if (!githubRepo.contains("/")) {
                return Optional.of(Boolean.FALSE);
            }

            String[] parts = githubRepo.split("/");

            String repoName = parts[parts.length - 1];

            //shouldn't be possible
            if (!repoName.endsWith(".git")) {
                return Optional.of(Boolean.FALSE);
            }

            repoName = repoName.replace(".git", "");

            //folder name is index of last / to the .git
            File saveDir = new File(directory.getAbsolutePath()
                    + OsUtil.FILE_SEP + repoName);

            if (!saveDir.exists()) {
                if (!saveDir.mkdirs()) {
                    throw new IOException("Failed to create save directory: " + saveDir.getAbsolutePath());
                }
            }

            Console.INSTANCE.getInputHandler().println("Checking for git");

            if (!OsUtil.isBinaryInstalled("git")) {
                Console.INSTANCE.getInputHandler()
                        .println("Git not installed. Please install it at: " + GIT_DOWNLOAD);
                return Optional.of(Boolean.FALSE);
            }

            Console.INSTANCE.getInputHandler().println("Cloning: \"" + NetworkUtil.getUrlTitle(githubRepo)
                    + "\" to \"" + saveDir.getName() + OsUtil.FILE_SEP + "\"");

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
