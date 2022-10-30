package cyder.threads;

import com.google.common.base.Preconditions;

import java.io.File;

/**
 * An observer and notifier to subscribers of events which happen in a directory.
 */
public class DirectoryWatcher {
    /**
     * The directory this watcher watches.
     */
    private final File watchDirectory;

    /**
     * The default poll timeout.
     */
    private static final int DEFAULT_POLL_TIMEOUT = 100;

    /**
     * The timeout between checking the watch directory.
     */
    private int pollTimeout;

    /**
     * Constructs a new directory watcher.
     *
     * @param watchDirectory the directory to watch
     */
    public DirectoryWatcher(File watchDirectory) {
        this(watchDirectory, DEFAULT_POLL_TIMEOUT);
    }

    /**
     * Constructs a new directory watcher.
     *
     * @param watchDirectory the directory to watch
     * @param pollTimeout    the timeout between checking the directory
     */
    public DirectoryWatcher(File watchDirectory, int pollTimeout) {
        Preconditions.checkNotNull(watchDirectory);
        Preconditions.checkArgument(watchDirectory.exists());
        Preconditions.checkArgument(watchDirectory.isDirectory());
        Preconditions.checkArgument(pollTimeout > 0);

        this.watchDirectory = watchDirectory;
        this.pollTimeout = pollTimeout;
    }

    /**
     * Returns the directory this watcher watches.
     *
     * @return the directory this watcher watches
     */
    public File getWatchDirectory() {
        return watchDirectory;
    }

    /**
     * Returns the timeout between directory content polls.
     *
     * @return the timeout between directory content polls
     */
    public int getPollTimeout() {
        return pollTimeout;
    }

    /**
     * Sets the timeout between directory content polls.
     *
     * @param pollTimeout the timeout between directory content polls
     */
    public void setPollTimeout(int pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    // todo file added, file removed, file updated, specific file(s) added,
    //  specific file(s) removed, specific file(s) updated
}
