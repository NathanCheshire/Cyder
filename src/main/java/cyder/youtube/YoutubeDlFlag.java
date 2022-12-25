package cyder.youtube;

/**
 * Supported YouTube-DL flags.
 */
public enum YoutubeDlFlag {
    /**
     * The extract audio youtube-dl flag.
     */
    EXTRACT_AUDIO("--extract-audio"),

    /**
     * The audio format youtube-dl flag.
     */
    AUDIO_FORMAT("--audio-format"),

    /**
     * The output youtube-dl flag.
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
     * The flag for this youtube-dl flag
     */
    private final String flag;

    YoutubeDlFlag(String flag) {
        this.flag = flag;
    }

    /**
     * Returns the flag for this youtube-dl flag.
     *
     * @return the flag for this youtube-dl flag
     */
    public String getFlag() {
        return flag;
    }
}
