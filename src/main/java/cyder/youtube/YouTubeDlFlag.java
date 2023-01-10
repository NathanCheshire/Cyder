package cyder.youtube;

/**
 * Supported YouTube-DL flags.
 */
public enum YouTubeDlFlag {
    /**
     * The extract audio YouTube-dl flag.
     */
    EXTRACT_AUDIO("--extract-audio"),

    /**
     * The audio format YouTube-dl flag.
     */
    AUDIO_FORMAT("--audio-format"),

    /**
     * The output YouTube-dl flag.
     */
    OUTPUT("--output"),

    /**
     * The flag to keep the video file after post-processing.
     */
    KEEP_VIDEO("--keep-video"),

    /**
     * The flag to not use .part files and to write directly into output files.
     */
    NO_PART("--no-part");

    /**
     * The flag for this YouTube-dl flag
     */
    private final String flag;

    YouTubeDlFlag(String flag) {
        this.flag = flag;
    }

    /**
     * Returns the flag for this YouTube-dl flag.
     *
     * @return the flag for this YouTube-dl flag
     */
    public String getFlag() {
        return flag;
    }
}
