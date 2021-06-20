package cyder.threads;

import javax.swing.*;
import java.util.LinkedList;

public class MasterYoutube {
    private JTextPane outputArea;

    //todo this class still exists in the event we want to allow multithreading once again with random youtube
    // that will be hard to figure out due to the nature of threads, context switching, and just the general way
    // processors and operating systems work D:

    //should be associated with an input handler
    public MasterYoutube(JTextPane outputArea) {
        this.outputArea = outputArea;
    }

    private static LinkedList<YoutubeThread> youtubeThreads = new LinkedList<>();

    //should be in MasterYoutube class
    public void killAllYoutube() {
        for (YoutubeThread ytt : youtubeThreads)
            ytt.kill();
    }

    //this will always be 1 as of right now
    public void start(int number) {
        for (int i = 0; i < number; i++) {
            YoutubeThread current = new YoutubeThread(outputArea);
            youtubeThreads.add(current);
        }
    }
}
