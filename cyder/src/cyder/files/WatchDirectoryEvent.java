package cyder.files;

/**
 * Events that may occur inside of a directory.
 */
public enum WatchDirectoryEvent {
    FILE_ADDED,
    FILE_DELETED,
    FILE_MODIFIED,
    DIRECTORY_ADDED,
    DIRECTORY_DELETED,
    DIRECTORY_MODIFIED,
}
