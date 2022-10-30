package cyder.file;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class to for {@link DirectoryWatcher} subscribers.
 */
public abstract class WatchDirectorySubscriber {
    /**
     * The list of events this subscriber is subscribed to.
     */
    private final ArrayList<WatchDirectoryEvent> subscriptions = new ArrayList<>();

    /**
     * The logic to perform when an event this subscriber subscribed to occurs.
     *
     * @param broker    the directory watcher which pushed the event to this subscriber
     * @param event     the event which occurred
     * @param eventFile the file which caused the event
     */
    public abstract void onEvent(DirectoryWatcher broker, WatchDirectoryEvent event, File eventFile);

    /**
     * Subscribes to the provided event.
     *
     * @param watchDirectoryEvent the event to subscribe to
     */
    public void subscribeTo(WatchDirectoryEvent watchDirectoryEvent) {
        Preconditions.checkNotNull(watchDirectoryEvent);
        Preconditions.checkState(!subscriptions.contains(watchDirectoryEvent));

        subscriptions.add(watchDirectoryEvent);
    }

    /**
     * Subscribes to the provided events.
     *
     * @param watchDirectoryEvents the events to subscribe to
     */
    public void subscribeTo(WatchDirectoryEvent... watchDirectoryEvents) {
        Preconditions.checkNotNull(watchDirectoryEvents);
        Arrays.stream(watchDirectoryEvents).forEach(this::subscribeTo);
    }

    /**
     * Returns the subscriptions this subscriber is subscribed to.
     *
     * @return the subscriptions this subscriber is subscribed to
     */
    public ImmutableList<WatchDirectoryEvent> getSubscriptions() {
        return ImmutableList.copyOf(subscriptions);
    }
}
