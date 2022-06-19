package cyder.handlers.external.audio;

/**
 * A serialization class for GSON to serialize a response resulting from an audio length post.
 */
public class AudioLengthResponse {
    /**
     * The length of the audio file in seconds.
     */
    private float length;

    /**
     * Constructs a new audio length response.
     *
     * @param length the length of the audio file in seconds.
     */
    public AudioLengthResponse(float length) {
        this.length = length;
    }

    /**
     * Returns the length of the audio file in seconds.
     *
     * @return the length of the audio file in seconds
     */
    public float getLength() {
        return length;
    }

    /**
     * Sets the length of the audio file in seconds.
     *
     * @param length the length of the audio file in seconds
     */
    public void setLength(float length) {
        this.length = length;
    }
}
