package cyder.file;

import com.google.common.collect.ImmutableList;

import java.io.File;

/**
 * An interface for classes to implement that wish to be used as a {@link DirectoryWatcher} file listener.
 */
public interface WatchDirectoryListener {
    /**
     * The logic to perform when an event this listener subscribed to occurs.
     *
     * @param event     the event which occurred
     * @param eventFile the file which caused the event
     */
    void onEvent(WatchDirectoryEvent event, File eventFile);

    /**
     * Subscribes to the provided event.
     *
     * @param watchDirectoryEvent the event to subscribe to
     */
    void subscribeTo(WatchDirectoryEvent watchDirectoryEvent);

    /**
     * Returns the subscriptions this listener is subscribed to.
     *
     * @return the subscriptions this listener is subscribed to
     */
    ImmutableList<WatchDirectoryEvent> getSubscriptions();
}
