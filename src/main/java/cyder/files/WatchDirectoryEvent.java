package cyder.files;

/**
 * File events that may occur inside of a directory.
 */
public enum WatchDirectoryEvent {
    /**
     * A file was added within the directory.
     */
    FILE_ADDED,

    /**
     * A file was deleted within the directory.
     */
    FILE_DELETED,

    /**
     * A file was modified within the directory.
     */
    FILE_MODIFIED,

    /**
     * A directory was added within the directory.
     */
    DIRECTORY_ADDED,

    /**
     * A directory was deleted within the directory.
     */
    DIRECTORY_DELETED,

    /**
     * A directory was modified within the directory.
     */
    DIRECTORY_MODIFIED,
}
