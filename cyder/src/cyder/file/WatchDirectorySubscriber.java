package cyder.file;

import com.google.common.collect.ImmutableList;

import java.io.File;

/**
 * An interface for classes to implement that wish to be used as a {@link DirectoryWatcher} file subscriber.
 */
public interface WatchDirectorySubscriber {
    /**
     * The logic to perform when an event this subscriber subscribed to occurs.
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
     * Returns the subscriptions this subscriber is subscribed to.
     *
     * @return the subscriptions this subscriber is subscribed to
     */
    ImmutableList<WatchDirectoryEvent> getSubscriptions();
}
