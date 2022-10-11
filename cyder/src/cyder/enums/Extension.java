package cyder.enums;

/**
 * Common extensions used and checked for throughout Cyder.
 */
// todo find and use throughout cyder
public enum Extension {
    EXE(".exe"),
    MP3(".mp3"),
    MP4(".mp4"),
    BIN(".bin"),
    TXT(".txt"),
    ZIP(".zip"),
    WAV(".wav"),
    JPG(".jpg"),
    JPEG(".jpeg"),
    PNG(".png"),
    JAVA(".java");

    /**
     * The extension text with the leading period.
     */
    private final String extension;

    Extension(String extension) {
        this.extension = extension;
    }

    /**
     * Returns the extension with the leading period.
     *
     * @return the extension with the leading period
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Returns the extension without the leading period.
     *
     * @return the extension without the leading period
     */
    public String getExtensionWithoutPeriod() {
        return extension.substring(1);
    }
}
