package cyder.comms;

/**
 * A serialization class for messages which pertain to the remote shutdown API.
 */
public class CyderRemoteShutdownMessage extends CyderCommunicationMessage {
    /**
     * The message type for this Cyder communication message.
     */
    private static final String message = "Remote shutdown";

    /**
     * Constructs a new Cyder remote shutdown message.
     *
     * @param content the content of the remote shutdown message.
     */
    public CyderRemoteShutdownMessage(String content) {
        this(message, content);
    }

    /**
     * Constructs a new Cyder remote shutdown message.
     *
     * @param message the message type
     * @param content the content of the message
     */
    private CyderRemoteShutdownMessage(String message, String content) {
        super(message, content);
    }
}
