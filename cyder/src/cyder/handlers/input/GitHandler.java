package cyder.handlers.input;

import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.parsers.remote.github.Issue;
import cyder.process.ProcessUtil;
import cyder.threads.CyderThreadRunner;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.utils.GitHubUtil;
import cyder.utils.OsUtil;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * A handler for commands and inputs related to git/github/gitlab.
 */
public class GitHandler extends InputHandler {
    /**
     * The git command.
     */
    private static final String GIT = "git";

    /**
     * Suppress default constructor.
     */
    private GitHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    // todo test all
    @Handle({"gitme", "github", "issues", "git clone", "languages"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("gitme")) {
            gitme();
        } else if (getInputHandler().commandIs("github")) {
            NetworkUtil.openUrl(CyderUrls.CYDER_SOURCE);
        } else if (getInputHandler().commandIs("issues")) {
            printIssues();
        } else if (getInputHandler().inputIgnoringSpacesMatches("git clone")) {
            cloneRepo();
        } else if (getInputHandler().commandIs("languages")) {
            printLanguagesUsedByCyder();
        } else {
            ret = false;
        }

        return ret;
    }

    @ForReadability
    private static void printLanguagesUsedByCyder() {
        Map<String, Integer> map = GitHubUtil.getLanguages();

        getInputHandler().println("Cyder uses the following languages:");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            getInputHandler().println(entry.getKey() + " takes up " + OsUtil.formatBytes(entry.getValue()));
        }
    }

    @ForReadability
    private static void cloneRepo() {
        String threadName = "Git Cloner, repo: " + getInputHandler().getArg(1);
        CyderThreadRunner.submit(() -> {
            try {
                Future<Optional<Boolean>> futureCloned = GitHubUtil.cloneRepoToDirectory(
                        getInputHandler().getArg(1), UserUtil.getUserFile(UserFile.FILES));

                while (!futureCloned.isDone()) Thread.onSpinWait();
                Optional<Boolean> cloned = futureCloned.get();
                if (cloned.isPresent() && cloned.get()) {
                    getInputHandler().println("Clone successfully finished");
                } else {
                    getInputHandler().println("Clone failed");
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, threadName);
    }

    /**
     * An escaped quote.
     */
    private static final String QUOTE = "\"";

    /**
     * Generates and returns the commands to send to a process
     * builder to perform a git add for all files in the current directory.
     *
     * @return the commands for a git add
     */
    private static String[] generateGitAddCommand() {
        return new String[]{GIT, "add", "."};
    }

    /**
     * Generates and returns the command to send a process builder to perform
     * a git push for all local yet not pushed commits.
     * Note this assumes the remote branch currently being tracked is named "main".
     *
     * @return the commands for a git push
     */
    private static String[] generateGitPushCommand() {
        return new String[]{GIT, "push", "-u", "origin", "main"};
    }

    /**
     * Performs the following git commands at the repo level:
     * <ul>
     *     <li>git add .</li>
     *     <li>git commit -m getArg(0)</li>
     *     <li>git push -u origin main</li>
     * </ul>
     */
    private static void gitme() {
        int argsLength = getInputHandler().getArgsSize();
        if (argsLength < 1) {
            getInputHandler().println("gitme usage: gitme [commit message, quotes not needed]");
            return;
        }

        ProcessBuilder gitAddProcessBuilder = new ProcessBuilder(generateGitAddCommand());

        String commitMessage = QUOTE + getInputHandler().argsToString() + QUOTE;
        String[] GIT_COMMIT_COMMAND = {GIT, "commit", "-m", commitMessage};
        ProcessBuilder gitCommitProcessBuilder = new ProcessBuilder(GIT_COMMIT_COMMAND);

        ProcessBuilder gitPushProcessBuilder = new ProcessBuilder(generateGitPushCommand());

        ImmutableList<ProcessBuilder> builders = ImmutableList.of(
                gitAddProcessBuilder, gitCommitProcessBuilder, gitPushProcessBuilder);
        ProcessUtil.runAndPrintProcessesSequential(getInputHandler(), builders);
    }

    /**
     * Prints all the issues found for the official Cyder github repo.
     */
    private static void printIssues() {
        CyderThreadRunner.submit(() -> {
            StringBuilder builder = new StringBuilder();
            ImmutableList<Issue> issues = GitHubUtil.getIssues();
            builder.append(issues.size()).append(" issue")
                    .append(issues.size() == 1 ? "" : "s")
                    .append(" found:").append("\n");
            builder.append("----------------------------------------").append("\n");

            for (Issue issue : issues) {
                builder.append("Issue #").append(issue.number).append("\n");
                builder.append(issue.title).append("\n");
                builder.append(issue.body).append("\n");
                builder.append("----------------------------------------").append("\n");
            }

            getInputHandler().println(builder);
        }, "Cyder GitHub issue getter");
    }
}
