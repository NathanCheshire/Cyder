package cyder.handlers.input;

import com.google.common.collect.ImmutableList;
import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.remote.github.Issue;
import cyder.threads.CyderThreadRunner;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.utils.GitHubUtil;
import cyder.utils.NetworkUtil;
import cyder.utils.OsUtil;

import java.util.Map;
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

    @Handle({"gitme", "github", "issues", "git", "languages"})
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
                                        UserUtil.getUserFile(UserFile.FILES));

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
            case "languages" -> {
                Map<String, Integer> map = GitHubUtil.getLanguages();

                getInputHandler().println("Cyder uses the following languages:");
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    getInputHandler().println(entry.getKey() + " takes up " + OsUtil.formatBytes(entry.getValue()));
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

            OsUtil.runAndPrintProcessesSequential(getInputHandler(), processBuilderAdd,
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
