package cyder.utilities;

import cyder.consts.CyderStrings;
import cyder.ui.ConsoleFrame;

public class ThreadUtil {
    private ThreadUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static int getDaemonThreadCount() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        return threadGroup.activeCount();
    }

    public static int getThreadCount() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        return threadGroup.activeCount();
    }

    public static void printDaemonThreads() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int num = threadGroup.activeCount();
        Thread[] printThreads = new Thread[num];
        threadGroup.enumerate(printThreads);
        for (int i = 0; i < num; i++)
            ConsoleFrame.getConsoleFrame().getInputHandler().println(printThreads[i].getName());
    }

    public static void printThreads() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int num = threadGroup.activeCount();
        Thread[] printThreads = new Thread[num];
        threadGroup.enumerate(printThreads);

        for (int i = 0; i < num; i++)
            if (!printThreads[i].isDaemon())
                ConsoleFrame.getConsoleFrame().getInputHandler().println(printThreads[i].getName());
    }
}
