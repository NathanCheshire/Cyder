package cyder.handlers.input;

import com.google.common.collect.ImmutableList;
import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.github.Issue;
import cyder.threads.CyderThreadRunner;
import cyder.user.UserFile;
import cyder.utils.GitHubUtil;
import cyder.utils.NetworkUtil;
import cyder.utils.OSUtil;
import cyder.utils.UserUtil;

import java.util.Optional;
import java.util.concurrent.Future;

/**
 * A handler for commands and inputs related to git/github/gitlab.
 */
public class GitHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private GitHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"gitme", "github", "issues", "git"})
    public static boolean handle() {
        switch (getInputHandler().getCommand()) {
            case "gitme" -> {
                gitme();
                return true;
            }
            case "github" -> {
                NetworkUtil.openUrl(CyderUrls.CYDER_SOURCE);
                return true;
            }
            case "issues" -> {
                printIssues();
                return true;
            }
            case "git" -> {
                if (!getInputHandler().checkArgsLength(2)) {
                    getInputHandler().println("Supported git commands: clone");
                } else {
                    if (getInputHandler().getArg(0).equalsIgnoreCase("clone")) {
                        CyderThreadRunner.submit(() -> {
                            try {
                                Future<Optional<Boolean>> cloned = GitHubUtil.cloneRepoToDirectory(
                                        getInputHandler().getArg(1),
                                        UserUtil.getUserFile(UserFile.FILES.getName()));

                                while (!cloned.isDone()) {
                                    Thread.onSpinWait();
                                }

                                if (cloned.get().isPresent()) {
                                    if (cloned.get().get() == Boolean.TRUE) {
                                        getInputHandler().println("Clone finished");
                                    } else {
                                        getInputHandler().print("Clone failed");
                                    }
                                } else {
                                    getInputHandler().println("Clone failed");
                                }
                            } catch (Exception e) {
                                ExceptionHandler.handle(e);
                            }
                        }, "Git Cloner, repo: " + getInputHandler().getArg(1));
                    } else {
                        getInputHandler().println("Supported git commands: clone");
                    }
                }

                return true;
            }
            default -> throw new IllegalArgumentException("Illegal command for git handler");
        }
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
        if (!getInputHandler().checkArgsLength(1)) {
            ProcessBuilder processBuilderAdd = new ProcessBuilder("git", "add", ".");
            ProcessBuilder processBuilderCommit = new ProcessBuilder(
                    "git", "commit", "-m", "\"" + getInputHandler().argsToString() + "\"");
            ProcessBuilder processBuilderPush = new ProcessBuilder("git", "push", "-u", "origin", "main");

            OSUtil.runAndPrintProcessesSuccessive(getInputHandler(), processBuilderAdd,
                    processBuilderCommit, processBuilderPush);
        } else {
            getInputHandler().println("gitme usage: gitme [commit message without quotes]");
        }
    }

    /**
     * Prints all the issues found for the official Cyder github repo.
     */
    private static void printIssues() {
        CyderThreadRunner.submit(() -> {
            ImmutableList<Issue> issues = GitHubUtil.getIssues();
            getInputHandler().println(issues.size() + " issue" + (issues.size() == 1 ? "" : "s") + " found:\n");
            getInputHandler().println("----------------------------------------");

            for (Issue issue : issues) {
                getInputHandler().println("Issue #" + issue.number);
                getInputHandler().println(issue.title);
                getInputHandler().println(issue.body);
                getInputHandler().println("----------------------------------------");
            }
        }, "Cyder GitHub issue getter");
    }
}
