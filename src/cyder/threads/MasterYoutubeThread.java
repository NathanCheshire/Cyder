package cyder.threads;

import cyder.ui.ConsoleFrame;

import javax.swing.*;
import java.util.LinkedList;

public class MasterYoutubeThread {
    private JTextPane outputArea;
    private static boolean active = false;

    //this class still exists in the event we want to allow multithreading once again with random youtube
    // that will be hard to figure out due to the nature of threads, context switching, and just the general way
    // processors and operating systems work D:

    //should be associated with an input handler
    public MasterYoutubeThread(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    private static LinkedList<YoutubeThread> youtubeThreads = new LinkedList<>();

    //should be in MasterYoutube class
    public void killAllYoutube() {
        for (YoutubeThread ytt : youtubeThreads)
            ytt.kill();

        active = false;
    }

    //this will always be 1 as of right now
    public void start(int number) {
        if (ConsoleFrame.getConsoleFrame().getInputHandler().getBletchyThread().isActive() ||
            ConsoleFrame.getConsoleFrame().getInputHandler().getMasterYoutube().isActive()) {
            ConsoleFrame.getConsoleFrame().notify("Cannot start bletchy/youtube thread" +
                    " at the same time as another instance.");
            return;
        }

        for (int i = 0; i < number; i++) {
            YoutubeThread current = new YoutubeThread(outputArea);
            youtubeThreads.add(current);
        }

        ConsoleFrame.getConsoleFrame().notify("Type \"stop scripts\" or press ctrl + c to stop the YouTube thread.");
        active = true;
    }

    public static boolean isActive() {
        return active;
    }
}
