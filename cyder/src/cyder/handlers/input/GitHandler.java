package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.GitHubUtil;
import cyder.utilities.NetworkUtil;
import cyder.utilities.OSUtil;

/**
 * A handler for commands and inputs related to git/github/gitlab.
 */
public class GitHandler extends InputHandlerBase {
    /**
     * Suppress default constructor.
     */
    private GitHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle({"gitme", "github", "issues"})
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
            default -> throw new IllegalArgumentException("Illegal command for git handler");
        }
    }

    /**
     * Performs the following git commands:
     * 1. git add .
     * 2. git commit -m getArg(0)
     * 3. git push -u origin main
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
            GitHubUtil.Issue[] issues = GitHubUtil.getIssues();
            getInputHandler().println(issues.length + " issue" + (issues.length == 1 ? "" : "s") + " found:\n");
            getInputHandler().println("----------------------------------------");

            for (GitHubUtil.Issue issue : issues) {
                getInputHandler().println("Issue #" + issue.number);
                getInputHandler().println(issue.title);
                getInputHandler().println(issue.body);
                getInputHandler().println("----------------------------------------");
            }
        }, "GitHub Issue Getter");
    }
}
