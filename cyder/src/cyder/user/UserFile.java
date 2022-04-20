package cyder.user;

/**
 * Enum representing the different directories/files which should always exist within a user's directory.
 */
public enum UserFile {
    /**
     * The Music directory.
     */
    MUSIC("Music", false),

    /**
     * The Backgrounds directory
     */
    BACKGROUNDS("Backgrounds", false),

    /**
     * The Notes directory.
     */
    NOTES("Notes", false),

    /**
     * The Userdata json file.
     */
    USERDATA("Userdata.json", true),

    /**
     * The Files directory.
     */
    FILES("Files", false);

    /**
     * The name of the file/directory.
     */
    private final String name;

    /**
     * Whether the path represents a file or directory.
     */
    private final boolean isFile;

    /**
     * Returns the name of this file.
     *
     * @return the name of this file
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether or not the object represented by this enum is a File type or directory.
     *
     * @return whether or not the object represented by this enum is a File type or directory
     */
    public boolean isFile() {
        return isFile;
    }

    /**
     * Enum type representing a file that should exist within the user's directory
     *
     * @param name the name of the file/directory
     * @param isFile whether or not it is a file
     */
    UserFile(String name, boolean isFile) {
        this.name = name;
        this.isFile = isFile;
    }
}
