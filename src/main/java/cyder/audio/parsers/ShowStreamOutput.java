package cyder.audio.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.ArrayList;

/**
 * A parser class for a show_stream ffprobe command.
 */
public class ShowStreamOutput {
    /**
     * The list of streams found by the show stream output command.
     */
    private ArrayList<Stream> streams;

    /**
     * Constructs a new show stream output object.
     */
    public ShowStreamOutput() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the list of streams found by the show stream output command.
     *
     * @return the list of streams found by the show stream output command
     */
    public ArrayList<Stream> getStreams() {
        return streams;
    }

    /**
     * Sets the list of streams found by the show stream output command.
     *
     * @param streams the list of streams found by the show stream output command
     */
    public void setStreams(ArrayList<Stream> streams) {
        this.streams = streams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ShowStreamOutput{"
                + "streams=" + streams
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return streams.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ShowStreamOutput)) {
            return false;
        }

        ShowStreamOutput other = (ShowStreamOutput) o;
        return streams.equals(other.streams);
    }
}
