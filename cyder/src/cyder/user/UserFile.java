package cyder.user;

/**
 * Enum representing the different directories/files which should always exist within a user's directory.
 */
public enum UserFile {
    /**
     * The Music directory.
     */
    MUSIC("Music"),

    /**
     * The Backgrounds directory
     */
    BACKGROUNDS("Backgrounds"),

    /**
     * The Notes directory.
     */
    NOTES("Notes"),

    /**
     * The Userdata json file.
     */
    USERDATA("Userdata.json", true),

    /**
     * The Files directory.
     */
    FILES("Files");

    /**
     * The album art directory within {@link #MUSIC}.
     */
    public static final String ALBUM_ART = "AlbumArt";

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
     * Returns whether the object represented by this enum is a File type or directory.
     *
     * @return whether the object represented by this enum is a File type or directory
     */
    public boolean isFile() {
        return isFile;
    }

    /**
     * Enum type representing a file that should exist within the user's directory
     *
     * @param name   the name of the file/directory
     * @param isFile whether it is a file
     */
    UserFile(String name, boolean isFile) {
        this.name = name;
        this.isFile = isFile;
    }

    /**
     * Enum type representing a file that should exist within the user's directory
     *
     * @param name the name of the file/directory
     */
    UserFile(String name) {
        this.name = name;
        this.isFile = false;
    }
}
