package cyder.file;

import com.google.common.base.Preconditions;
import cyder.exceptions.FatalException;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An observer and notifier to subscribers of events which happen in a directory.
 * Instances of this class are immutable not thread safe. To achieve thread-safety,
 * clients should surrounding object method invocations with external synchronization
 * techniques.
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

    /**
     * Whether this directory watcher is/should be watching the watch directory.
     */
    private final AtomicBoolean isWatching = new AtomicBoolean();

    /**
     * Stops watching the watch directory if this water is currently watching.
     */
    public void stopWatching() {
        isWatching.set(false);
    }

    /**
     * Returns whether this directory watcher is currently watching the watch directory.
     *
     * @return whether this directory watcher is currently watching the watch directory
     */
    public boolean isWatching() {
        return isWatching.get();
    }

    /**
     * Starts watching the current directory for current and future watch requests.
     *
     * @throws IllegalStateException if the watch directory DNE
     * @throws IllegalStateException if the directory is already being watched
     * @throws FatalException        if the watch directory is deleted while the watch subroutine is active
     */
    public void startWatching() {
        Preconditions.checkState(watchDirectory.exists());
        Preconditions.checkState(!isWatching.get());

        isWatching.set(true);

        String threadName = "Directory Watcher, directory: " + watchDirectory.getAbsolutePath();
        CyderThreadRunner.submit(() -> {
            while (isWatching.get()) {
                if (!watchDirectory.exists()) {
                    throw new FatalException("Watch directory no longer exists: " + watchDirectory.getAbsolutePath());
                }

                // todo actions

                ThreadUtil.sleep(pollTimeout);
            }

            isWatching.set(false);
        }, threadName);
    }

    // todo don't take runnable, take a thing that checks if it's a specific
    //  file, extension, name, etc. and then invokes the runnable if true

    private final ArrayList<WatchDirectoryListener> onFileAddedListeners = new ArrayList<>();
    private final ArrayList<WatchDirectoryListener> onFileRemovedListeners = new ArrayList<>();
    private final ArrayList<WatchDirectoryListener> onFileModifiedListeners = new ArrayList<>();

    private final ArrayList<WatchDirectoryListener> onDirectoryAddedListeners = new ArrayList<>();
    private final ArrayList<WatchDirectoryListener> onDirectoryRemovedListeners = new ArrayList<>();
    private final ArrayList<WatchDirectoryListener> onDirectoryModifiedListeners = new ArrayList<>();

    // todo file added, file removed, file updated, specific file(s) added,
    //  specific file(s) removed, specific file(s) updated

    // todo use this in photo viewer and notes
}
