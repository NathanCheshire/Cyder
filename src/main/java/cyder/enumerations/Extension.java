package cyder.enumerations;

import com.google.common.base.Preconditions;
import cyder.files.FileUtil;

import java.io.File;

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
     * Returns whether the provided file is using this extension.
     *
     * @param file this file
     * @return whether the provided file is using this extension
     */
    public boolean validateExtension(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        return FileUtil.getExtension(file).equals(extension);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return extension;
    }
}
