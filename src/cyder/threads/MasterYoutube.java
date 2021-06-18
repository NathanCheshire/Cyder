package cyder.threads;

import javax.swing.*;
import java.util.LinkedList;

public class MasterYoutube {
    private JTextPane outputArea;

    //todo random youtube should start from 0 and go through all permutations and remember where it's been
    // so when killed save where we are and pickup from there after, in this sense, we need further math,
    // to figure out how to split up threads such as start, start + 1, start + 2, and inc of 3 for each in the case
    // of 3 threads for example

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

    public void start(int number) {
        for (int i = 0; i < number; i++) {
            YoutubeThread current = new YoutubeThread(outputArea);
            youtubeThreads.add(current);
        }
    }
}
