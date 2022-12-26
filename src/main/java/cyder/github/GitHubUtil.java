package cyder.github;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import cyder.console.Console;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.remote.github.Issue;
import cyder.process.ProcessUtil;
import cyder.threads.CyderThreadFactory;
import cyder.utils.OsUtil;
import cyder.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static cyder.strings.CyderStrings.*;

/**
 * Utilities involving REST APIs for GitHub.
 */
public final class GitHubUtil {
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
     * The link for the github api to return a json of currently open issues for Cyder.
     */
    private static final String CYDER_ISSUES = "https://api.github.com/repos/nathancheshire/cyder/issues";

    /**
     * The link to download git from.
     */
    private static final String GIT_DOWNLOAD = "https://git-scm.com/downloads";

    /**
     * The url to get the languages used throughout Cyder from.
     */
    private static final String LANGUAGES_URL = "https://api.github.com/repos/nathancheshire/cyder/languages";

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
     * Clones the provided github repo to the provided directory.
     *
     * @param cloneLink the URL of the github repository to clone
     * @param directory the directory to save the repo to
     *                  Note this directory must exist prior to method invocation
     * @return whether the repo was successfully cloned and saved
     */
    public static Future<Boolean> cloneRepoToDirectory(String cloneLink, File directory) {
        Preconditions.checkNotNull(cloneLink);
        Preconditions.checkArgument(!cloneLink.isEmpty());
        Preconditions.checkNotNull(directory);
        Preconditions.checkArgument(directory.exists());
        Preconditions.checkArgument(directory.isDirectory());

        return cloningExecutor.submit(() -> {
            Console.INSTANCE.getInputHandler().println("Validating github link: " + cloneLink);

            GithubCloneRepoLink githubCloneRepoLink = new GithubCloneRepoLink(cloneLink);

            String repoName = githubCloneRepoLink.getRepository();
            File saveDirectory = OsUtil.buildFile(directory.getAbsolutePath(), repoName);

            if (!saveDirectory.exists()) {
                if (!saveDirectory.mkdirs()) {
                    throw new IOException("Failed to create save directory"
                            + colon
                            + space
                            + saveDirectory.getAbsolutePath());
                }
            }

            Console.INSTANCE.getInputHandler().println("Checking for git");

            if (!OsUtil.isBinaryInstalled("git")) {
                Console.INSTANCE.getInputHandler().println("Git not installed."
                        + " Please install it at: " + GIT_DOWNLOAD);
                return false;
            }

            Console.INSTANCE.getInputHandler().println("Cloning"
                    + colon
                    + space
                    + quote
                    + repoName
                    + quote
                    + space
                    + "to"
                    + space
                    + quote
                    + saveDirectory.getName()
                    + OsUtil.FILE_SEP
                    + quote);

            try {
                String command = "git"
                        + space
                        + "clone"
                        + space
                        + cloneLink
                        + space
                        + saveDirectory.getAbsolutePath();
                ProcessUtil.runAndWaitForProcess(command);
            } catch (Exception ignored) {
                Console.INSTANCE.getInputHandler().println("Failed to clone repo " + repoName);

                return false;
            }

            return true;
        });
    }
}
