package cyder.files;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A class to for {@link DirectoryWatcher} subscribers.
 */
public abstract class WatchDirectorySubscriber {
    /**
     * The list of events this subscriber is subscribed to.
     */
    private final ArrayList<WatchDirectoryEvent> subscriptions = new ArrayList<>();

    /**
     * The regex matcher to determine whether this subscriber should be notified
     * of a file event based on the name of the file excluding the extension.
     */
    private Pattern fileNameRegex;

    /**
     * The regex matcher to determine whether this subscriber should be notified
     * of a file event based on the extension of the file excluding the filename.
     */
    private Pattern fileExtensionRegex;

    /**
     * The regex matcher to determine whether this subscriber should be notified
     * of a file event based on the file name and extension.
     */
    private Pattern fileRegex;

    /**
     * The regex matcher to determine whether this subscriber should be notified
     * of a directory event based on the name of the directory.
     */
    private Pattern directoryRegex;

    /**
     * The logic to perform when an event this subscriber is subscribed to occurs.
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
     * @param watchDirectoryEvent  the first even to subscribe to
     * @param watchDirectoryEvents the additional events to subscribe to if present
     */
    public void subscribeTo(WatchDirectoryEvent watchDirectoryEvent, WatchDirectoryEvent... watchDirectoryEvents) {
        Preconditions.checkNotNull(watchDirectoryEvents);
        subscribeTo(watchDirectoryEvent);
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

    /**
     * Sets the filename regex to use when determining whether this subscriber should be notified
     * of a file event based on the name of the file excluding the extension.
     *
     * @param fileNameRegex the filename regex
     */
    public void setFileNameRegex(String fileNameRegex) {
        Preconditions.checkNotNull(fileNameRegex);
        Preconditions.checkArgument(!fileNameRegex.isEmpty());

        this.fileNameRegex = Pattern.compile(fileNameRegex);
    }

    /**
     * Returns the filename regex to use when determining whether this subscriber should be notified
     * of a file event based on the name of the file excluding the extension.
     *
     * @return the filename regex
     */
    public Pattern getFileNameRegex() {
        return fileNameRegex;
    }

    /**
     * Sets the regex matcher to determine whether this subscriber should be notified
     * of a file event based on the extension of the file excluding the filename.
     *
     * @param fileExtensionRegex the extension regex
     */
    public void setFileExtensionRegex(String fileExtensionRegex) {
        Preconditions.checkNotNull(fileExtensionRegex);
        Preconditions.checkArgument(!fileExtensionRegex.isEmpty());

        this.fileExtensionRegex = Pattern.compile(fileExtensionRegex);
    }

    /**
     * Returns the regex matcher to determine whether this subscriber should be notified
     * of a file event based on the extension of the file excluding the filename.
     *
     * @return the extension regex
     */
    public Pattern getFileExtensionRegex() {
        return fileExtensionRegex;
    }

    /**
     * Sets the regex matcher to determine whether this subscriber should be notified
     * of a file event based on the file name and extension.
     *
     * @param fileRegex the file regex
     */
    public void setFileRegex(String fileRegex) {
        Preconditions.checkNotNull(fileRegex);
        Preconditions.checkArgument(!fileRegex.isEmpty());

        this.fileRegex = Pattern.compile(fileRegex);
    }

    /**
     * Returns the regex matcher to determine whether this subscriber should be notified
     * of a file event based on the file name and extension.
     *
     * @return the file regex
     */
    public Pattern getFileRegex() {
        return fileRegex;
    }

    /**
     * Sets the regex matcher to determine whether this subscriber should be notified
     * of a directory event based on the name of the directory.
     *
     * @param directoryRegex the directory regex
     */
    public void setDirectoryRegex(String directoryRegex) {
        Preconditions.checkNotNull(directoryRegex);
        Preconditions.checkArgument(!directoryRegex.isEmpty());

        this.directoryRegex = Pattern.compile(directoryRegex);
    }

    /**
     * Returns the regex matcher to determine whether this subscriber should be notified
     * of a directory event based on the name of the directory.
     *
     * @return the directory regex
     */
    public Pattern getDirectoryRegex() {
        return directoryRegex;
    }

    /**
     * Returns whether the filename of the provided file matches the filename pattern matcher.
     *
     * @param file the file
     * @return whether the filename of the provided file matches the filename pattern matcher
     */
    private boolean filenameMatches(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkState(fileNameRegex != null);

        return fileNameRegex.matcher(FileUtil.getFilename(file)).matches();
    }

    /**
     * Returns whether the file extension of the provided file matches the file extension pattern matcher.
     *
     * @param file the file
     * @return whether the file extension of the provided file matches the file extension pattern matcher
     */
    private boolean fileExtensionMatches(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkState(fileExtensionRegex != null);

        return fileExtensionRegex.matcher(FileUtil.getExtension(file)).matches();
    }

    /**
     * Returns whether the file name and extension of the provided file matches the file pattern matcher.
     *
     * @param file the file
     * @return whether the file name and extension of the provided file matches the file pattern matcher
     */
    private boolean fileMatches(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkState(fileRegex != null);

        return fileRegex.matcher(file.getName()).matches();
    }

    /**
     * Returns whether the file name of the provided file matches the directory pattern matcher.
     *
     * @param file the file
     * @return whether the file name of the provided file matches the directory pattern matcher
     */
    private boolean directoryMatches(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkState(directoryRegex != null);

        return directoryRegex.matcher(file.getName()).matches();
    }

    /**
     * Returns whether the set patterns match the provided file.
     * If no patterns are set then {@link Boolean#TRUE} is returned.
     *
     * @param file the file
     * @return whether the set patterns match the filename/extension
     */
    @SuppressWarnings("RedundantIfStatement") /* Readability */
    public boolean patternsMatch(File file) {
        Preconditions.checkNotNull(file);

        boolean isDirectory = file.isDirectory();
        if (isDirectory) {
            return directoryRegex == null || directoryMatches(file);
        }

        boolean isFile = file.isFile();
        if (isFile) {
            if (fileNameRegex != null && !filenameMatches(file)) return false;
            if (fileExtensionRegex != null && !fileExtensionMatches(file)) return false;
            if (fileRegex != null && !fileMatches(file)) return false;
        }

        return true;
    }
}
