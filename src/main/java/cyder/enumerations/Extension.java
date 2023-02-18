package cyder.enumerations;

/**
 * Common extensions used and checked for throughout Cyder.
 */
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
    JAVA(".java"),
    JSON(".json"),
    GIT(".git"),
    TTF(".ttf"),
    LOG(".log"),
    INI(".ini");

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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return extension;
    }
}
