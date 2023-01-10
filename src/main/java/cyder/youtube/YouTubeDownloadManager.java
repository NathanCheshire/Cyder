package cyder.youtube;

import com.google.common.base.Preconditions;

import java.util.ArrayList;

/**
 * A manager for active and past YouTube downloads.
 */
public enum YouTubeDownloadManager {
    /**
     * The YoutubeDownloadManager instance.
     */
    INSTANCE;

    /**
     * A list of YouTube videos currently being downloaded.
     */
    private final ArrayList<YouTubeAudioDownload> activeDownloads = new ArrayList<>();

    /**
     * Removes the provided YouTube download from the active downloads list.
     *
     * @param youtubeDownload the YouTube download to remove from the active downloads list
     */
    void removeActiveDownload(YouTubeAudioDownload youtubeDownload) {
        Preconditions.checkNotNull(youtubeDownload);

        activeDownloads.remove(youtubeDownload);
    }

    /**
     * Adds the provided YouTube download to the downloads list.
     *
     * @param youtubeDownload the youtube download to add to the list
     */
    void addActiveDownload(YouTubeAudioDownload youtubeDownload) {
        Preconditions.checkNotNull(youtubeDownload);

        activeDownloads.add(youtubeDownload);
    }

    /**
     * Cancels all active youtube downloads.
     */
    public void cancelAllActiveDownloads() {
        activeDownloads.forEach(YouTubeAudioDownload::cancel);
    }
}
