package cyder.threads;

import cyder.ui.ConsoleFrame;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class MasterYoutubeThread {
    private JTextPane outputArea;
    private static boolean isActive = false;

    //this class still exists in the event we want to allow multithreading once again with random youtube
    // that will be hard to figure out due to the nature of threads, context switching, and just the general way
    // processors and operating systems work D:

    //should be associated with an input handler
    public MasterYoutubeThread(JTextPane outputArea, Semaphore semaphore) {
        this.outputArea = outputArea;
    }

    private static LinkedList<YoutubeThread> youtubeThreads = new LinkedList<>();

    //should be in MasterYoutube class
    public void killAllYoutube() {
        for (YoutubeThread ytt : youtubeThreads)
            ytt.kill();

        isActive = false;
    }

    //this will always be 1 as of right now
    public void start(int number) {
        if (ConsoleFrame.getConsoleFrame().getInputHandler().getBletchyThread().isActive() ||
            ConsoleFrame.getConsoleFrame().getInputHandler().getMasterYoutube().isIsActive()) {
            ConsoleFrame.getConsoleFrame().notify("Cannot start bletchy/youtube thread" +
                    " at the same time as another instance.");
            return;
        }

        for (int i = 0; i < number; i++) {
            YoutubeThread current = new YoutubeThread(outputArea);
            youtubeThreads.add(current);
        }

        ConsoleFrame.getConsoleFrame().notify("Type \"stopscript\" or press ctrl + c to stop the YouTube thread.");
        isActive = true;
    }

    public static boolean isIsActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
