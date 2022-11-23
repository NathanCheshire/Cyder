package cyder.comms;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
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
     * The type of message.
     */
    private String message;

    /**
     * The content of the message
     */
    private String content;

    /**
     * Constructs a new Cyder communication message.
     */
    private CyderCommunicationMessage() {
        // Exposed for usage by GSON
    }

    /**
     * Constructs a new Cyder communication message.
     *
     * @param message the message type
     * @param content the content of the message
     */
    public CyderCommunicationMessage(String message, String content) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(content);
        Preconditions.checkArgument(!message.isEmpty());
        Preconditions.checkArgument(!content.isEmpty());

        this.message = message;
        this.content = content;
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
        return getMessage().equals(other.getMessage()) && getContent().equals(other.getContent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = message.hashCode();
        ret = 31 * ret + content.hashCode();
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
