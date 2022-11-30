package cyder.session;

/** A serialization class for messages which pertain to the remote shutdown API. */
public class CyderRemoteShutdownMessage extends CyderCommunicationMessage {
    /** The message type for this Cyder communication message. */
    public static final String MESSAGE = "Remote shutdown";

    /**
     * Constructs a new Cyder remote shutdown message.
     *
     * @param content   the content of the remote shutdown message.
     * @param sessionId the session ID of the instance of Cyder this message originated from
     */
    public CyderRemoteShutdownMessage(String content, String sessionId) {
        this(MESSAGE, content, sessionId);
    }

    /**
     * Constructs a new Cyder remote shutdown message.
     *
     * @param message   the message type
     * @param content   the content of the message
     * @param sessionId the session ID of the instance of Cyder this message originated from
     */
    private CyderRemoteShutdownMessage(String message, String content, String sessionId) {
        super(message, content, sessionId);
    }
}
