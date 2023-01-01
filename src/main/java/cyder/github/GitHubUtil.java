package cyder.github;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import cyder.console.Console;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.github.Issue;
import cyder.process.ProcessUtil;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.utils.OsUtil;
import cyder.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static cyder.strings.CyderStrings.*;

/**
 * Utilities for working with REST APIs provided by <a href="https://www.github.com">GitHub.com</a>.
 */
public final class GitHubUtil {
    /**
     * The nathan cheshire user string.
     */
    private static final String nathanCheshireUserName = "nathancheshire";

    /**
     * The repo name for cyder.
     */
    private static final String cyderRepoName = "cyder";

    /**
     * The name of the thread for cloning repos from GitHub.
     */
    private static final String GIT_REPO_CLONER_THREAD_NAME = "Git Repo Cloner";

    /**
     * Executor service used to clone github repos.
     */
    private static final ExecutorService cloningExecutor = Executors.newSingleThreadScheduledExecutor(
            new CyderThreadFactory(GIT_REPO_CLONER_THREAD_NAME));

    /**
     * The link to download git from.
     */
    private static final String DOWNLOAD_GIT = "https://git-scm.com/downloads";

    /**
     * The GitHub repos API header.
     */
    private static final String GITHUB_REPOS_API_HEADER = "https://api.github.com/repos/";

    /**
     * The url to get the languages used throughout Cyder from.
     */
    private static final String cyderLanguagesUrl = GITHUB_REPOS_API_HEADER + nathanCheshireUserName
            + "/" + cyderRepoName + "/languages";

    /**
     * Suppress default constructor.
     */
    private GitHubUtil() {
        throw new IllegalMethodException(ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns a list of issues for Cyder.
     *
     * @return the list of currently open issues for Cyder
     */
    public static ImmutableList<Issue> getCyderIssues() {
        return getIssues(nathanCheshireUserName, cyderRepoName);
    }

    /**
     * Returns a list of issues for the repo under the provided user.
     *
     * @param user       the github user
     * @param githubRepo the github repo
     * @return a list of issues
     */
    public static ImmutableList<Issue> getIssues(String user, String githubRepo) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(githubRepo);
        Preconditions.checkArgument(!user.isEmpty());
        Preconditions.checkArgument(!githubRepo.isEmpty());

        Issue[] ret = new Issue[0];

        try {
            String urlString = GITHUB_REPOS_API_HEADER + user + "/" + githubRepo + "/issues";
            URL url = new URL(urlString);

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

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the languages used to code Cyder along with the raw number of bytes of the respective language.
     *
     * @return the languages used to code Cyder along with the raw number of bytes of the respective language
     */
    public static HashMap<String, Integer> getLanguages() {
        HashMap<String, Integer> ret = new HashMap<>();

        try {
            URL url = new URL(cyderLanguagesUrl);

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
     * Clones the provided github repo to the provided directory.
     * Updates are printed to the {@link Console}s {@link BaseInputHandler}.
     *
     * @param cloneLink the URL of the github repository to clone
     * @param directory the directory to save the repo to
     *                  Note this directory must exist prior to method invocation
     * @return whether the repo was successfully cloned and saved
     */
    public static Future<Boolean> cloneRepoToDirectory(String cloneLink, File directory) {
        return cloneRepoToDirectory(cloneLink, directory, Console.INSTANCE.getInputHandler());
    }

    /**
     * Clones the provided github repo to the provided directory.
     *
     * @param cloneLink    the URL of the github repository to clone
     * @param directory    the directory to save the repo to
     *                     Note this directory must exist prior to method invocation
     * @param inputHandler the input handler to print operation updates to. Provide {@code null} to avoid
     * @return whether the repo was successfully cloned and saved
     */
    public static Future<Boolean> cloneRepoToDirectory(String cloneLink, File directory,
                                                       BaseInputHandler inputHandler) {
        Preconditions.checkNotNull(cloneLink);
        Preconditions.checkArgument(!cloneLink.isEmpty());
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        boolean print = inputHandler != null;

        return cloningExecutor.submit(() -> {
            if (print) {
                inputHandler.println("Validating GitHub clone link: " + cloneLink);
            }

            GithubCloneRepoLink githubCloneRepoLink = null;

            try {
                githubCloneRepoLink = new GithubCloneRepoLink(cloneLink);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                if (print) {
                    inputHandler.println("Failed to create github clone repo link wrapper object: " + e.getMessage());
                }
            }

            if (githubCloneRepoLink == null) {
                return false;
            }

            String repoName = githubCloneRepoLink.getRepository();
            File saveDirectory = OsUtil.buildFile(directory.getAbsolutePath(), repoName);

            if (!saveDirectory.exists()) {
                if (!saveDirectory.mkdirs()) {
                    if (print) {
                        inputHandler.println("Failed to create save directory"
                                + colon + space + saveDirectory.getAbsolutePath());
                    }
                    return false;
                }
            }

            if (print) {
                inputHandler.println("Checking for git");
            }

            if (!OsUtil.isBinaryInstalled("git")) {
                if (print) {
                    inputHandler.println("Git not installed. Please install it at: " + DOWNLOAD_GIT);
                }
                return false;
            }

            if (print) {
                String printString = "Cloning" + space + StringUtil.getPlural(
                        StringUtil.capsFirst(githubCloneRepoLink.getUser()))
                        + space + quote + StringUtil.capsFirst(repoName) + quote + space + "to" + space
                        + quote + saveDirectory.getName() + OsUtil.FILE_SEP + quote;
                inputHandler.println(printString);
            }

            try {
                String command = "git" + space
                        + "clone" + space + githubCloneRepoLink.getLink()
                        + space + saveDirectory.getAbsolutePath();
                ProcessUtil.runAndWaitForProcess(command);
            } catch (Exception ignored) {
                if (print) {
                    inputHandler.println("Failed to clone repo " + repoName);
                }
                return false;
            }

            return true;
        });
    }
}
