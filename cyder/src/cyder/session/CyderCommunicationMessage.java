package cyder.session;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import cyder.strings.CyderStrings;
import cyder.utils.SerializationUtil;

/**
 * A serialization class for serializing messages between instances of Cyder.
 */
public class CyderCommunicationMessage {
    /**
     * The message json identifier.
     */
    private static final String MESSAGE = "message";

    /**
     * The content json identifier.
     */
    private static final String CONTENT = "content";

    /**
     * The session id json identifier.
     */
    private static final String SESSION_ID = "session_id";

    /**
     * The type of message.
     */
    private String message;

    /**
     * The content of the message
     */
    private String content;

    /**
     * The Cyder instance session ID.
     */
    @SerializedName("session_id")
    private String sessionId;

    /**
     * Constructs a new Cyder communication message.
     * Method exposed for usage by {@link SerializationUtil}.
     */
    private CyderCommunicationMessage() {}

    /**
     * Constructs a new Cyder communication message.
     *
     * @param message   the message type
     * @param content   the content of the message
     * @param sessionId the Cyder session ID
     */
    public CyderCommunicationMessage(String message, String content, String sessionId) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(content);
        Preconditions.checkNotNull(sessionId);
        Preconditions.checkArgument(!message.isEmpty());
        Preconditions.checkArgument(!content.isEmpty());
        Preconditions.checkArgument(!sessionId.isEmpty());

        this.message = message;
        this.content = content;
        this.sessionId = sessionId;
    }

    /**
     * Returns the message type.
     *
     * @return the message type
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the content of the message.
     *
     * @return the content of the message
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the session ID of the instance of Cyder this message originated from.
     *
     * @return the session ID of the instance of Cyder this message originated from
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the message type of this message.
     *
     * @param message the message type of this message
     */
    public void setMessage(String message) {
        Preconditions.checkNotNull(message);
        Preconditions.checkArgument(!message.isEmpty());

        this.message = message;
    }

    /**
     * Sets the content of this message.
     *
     * @param content the content of this message
     */
    public void setContent(String content) {
        Preconditions.checkNotNull(content);
        Preconditions.checkArgument(!content.isEmpty());

        this.content = content;
    }

    /**
     * Sets the session ID of the instance of Cyder this message originated from.
     *
     * @param sessionId the session ID of the instance of Cyder this message originated from
     */
    public void setSessionId(String sessionId) {
        Preconditions.checkNotNull(sessionId);
        Preconditions.checkArgument(!sessionId.isEmpty());

        this.sessionId = sessionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CyderCommunicationMessage)) {
            return false;
        }

        CyderCommunicationMessage other = (CyderCommunicationMessage) o;
        return getMessage().equals(other.getMessage())
                && getContent().equals(other.getContent())
                && getSessionId().equals(other.getSessionId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = message.hashCode();
        ret = 31 * ret + content.hashCode();
        ret = 31 * ret + sessionId.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "{"
                + CyderStrings.quote + MESSAGE + CyderStrings.quote
                + CyderStrings.colon
                + CyderStrings.quote + getMessage() + CyderStrings.quote
                + CyderStrings.comma
                + CyderStrings.quote + CONTENT + CyderStrings.quote
                + CyderStrings.colon
                + CyderStrings.quote + getContent() + CyderStrings.quote
                + CyderStrings.comma
                + CyderStrings.quote + SESSION_ID + CyderStrings.quote
                + CyderStrings.colon
                + CyderStrings.quote + getSessionId() + CyderStrings.quote
                + "}";
    }

    /**
     * Returns a new Cyder communication message using the provided json string.
     *
     * @param json the json to serialize the object from
     * @return a new Cyder communication message
     */
    public static CyderCommunicationMessage fromJson(String json) {
        Preconditions.checkNotNull(json);
        Preconditions.checkArgument(!json.isEmpty());

        return SerializationUtil.fromJson(json, CyderCommunicationMessage.class);
    }
}
