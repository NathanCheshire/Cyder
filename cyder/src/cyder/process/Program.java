package cyder.process;

import cyder.utils.OsUtil;

/**
 * Common external programs/binaries utilized by Cyder.
 */
public enum Program {
    FFMPEG("ffmpeg"),
    FFPROBE("ffprobe"),
    FFPLAY("ffplay"),
    YOUTUBE_DL("youtube-dl"),
    PIP("pip"),
    PYTHON("python");

    /**
     * The name of this program
     */
    private final String programName;

    Program(String programName) {
        this.programName = programName;
    }

    /**
     * Returns the name of this program.
     *
     * @return the name of this program
     */
    public String getProgramName() {
        return programName;
    }

    /**
     * Returns whether this program is installed.
     *
     * @return whether this program is installed
     */
    public boolean isInstalled() {
        return OsUtil.isBinaryInstalled(programName);
    }
}
