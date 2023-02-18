package cyder.user;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.enumerations.Dynamic;

import java.io.File;

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
     * Returns a pointer to this user file using the Console's currently set UUID.
     *
     * @return a pointer to this user file using the Console's currently set UUID
     */
    public File getFilePointer() {
        Preconditions.checkNotNull(Console.INSTANCE.getUuid());

        return Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(), name);
    }

    /**
     * Returns whether the file referenced by this user file exists.
     *
     * @return whether the file referenced by this user file exists
     */
    @CanIgnoreReturnValue /* Can be used as a Precondition */
    public boolean exists() {
        return getFilePointer().exists();
    }
}
