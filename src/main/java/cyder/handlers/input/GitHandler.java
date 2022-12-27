package cyder.handlers.input;

import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.annotations.Handle;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.github.GitHubUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.parsers.remote.github.Issue;
import cyder.process.ProcessUtil;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.utils.OsUtil;

import java.util.Map;
import java.util.concurrent.Future;

import static cyder.strings.CyderStrings.quote;
import static cyder.strings.CyderStrings.space;

/**
 * A handler for commands and inputs related to git/github/gitlab.
 */
public class GitHandler extends InputHandler {
    /**
     * The git command.
     */
    private static final String GIT = "git";

    /**
     * The git clone command.
     */
    private static final String GIT_CLONE = "git clone";

    /**
     * The issue string separator.
     */
    private static final String issueSeparator = "----------------------------------------";

    /**
     * The name of the github issue printer thread.
     */
    private static final String GITHUB_ISSUE_PRINTER_THREAD_NAME = "Cyder GitHub Issue Printer";

    /**
     * Suppress default constructor.
     */
    private GitHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"gitme", "github", "issues", "git clone", "languages"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("gitme")) {
            gitme();
        } else if (getInputHandler().commandIs("github")) {
            NetworkUtil.openUrl(CyderUrls.CYDER_SOURCE);
        } else if (getInputHandler().commandIs("issues")) {
            printIssues();
        } else if (getInputHandler().inputIgnoringSpacesAndCaseStartsWith(GIT_CLONE)) {
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
        String repo = getInputHandler().commandAndArgsToString().substring(GIT_CLONE.length()).trim();
        if (repo.isEmpty()) {
            getInputHandler().println("Git clone usage: git clone [repository remote link]");
            return;
        }

        String threadName = "Git Cloner, repo: " + repo;
        CyderThreadRunner.submit(() -> {
            try {
                Future<Boolean> futureCloned = GitHubUtil.cloneRepoToDirectory(repo,
                        UserUtil.getUserFile(UserFile.FILES));

                while (!futureCloned.isDone()) Thread.onSpinWait();
                boolean cloned = futureCloned.get();
                if (cloned) {
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
        if (getInputHandler().noArgs()) {
            getInputHandler().println("gitme usage: gitme [commit message, quotes not needed]");
            return;
        }

        ProcessBuilder gitAddProcessBuilder = new ProcessBuilder(generateGitAddCommand());

        String commitMessage = quote + getInputHandler().argsToString() + quote;
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
            ImmutableList<Issue> issues = GitHubUtil.getCyderIssues();

            StringBuilder builder = new StringBuilder();
            builder.append(issues.size()).append(space)
                    .append(StringUtil.getPlural(issues.size(), "issue"))
                    .append(space).append("found:").append(CyderStrings.newline);
            builder.append(issueSeparator).append(CyderStrings.newline);

            issues.forEach(issue -> {
                builder.append("Issue #").append(issue.number).append(CyderStrings.newline);
                builder.append(issue.title).append(CyderStrings.newline);
                builder.append(issue.body).append(CyderStrings.newline);
                builder.append(issueSeparator).append(CyderStrings.newline);
            });

            getInputHandler().println(builder);
        }, GITHUB_ISSUE_PRINTER_THREAD_NAME);
    }
}
