package cyder.audio.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.ArrayList;

/**
 * A parser class for a show_stream ffprobe command.
 */
public class ShowStreamOutput {
    private ArrayList<Stream> streams;

    public ShowStreamOutput() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    public ArrayList<Stream> getStreams() {
        return streams;
    }

    public void setStreams(ArrayList<Stream> streams) {
        this.streams = streams;
    }
}
