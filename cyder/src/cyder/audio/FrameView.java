package cyder.audio;

/**
 * The available frame views for both the audio player and YouTube downloader.
 */
enum FrameView {
    /**
     * All ui elements visible.
     */
    FULL,
    /**
     * Album art hidden.
     */
    HIDDEN_ART,
    /**
     * Mini audio player mode.
     */
    MINI,
    /**
     * Searching YouTube for a video's audio to download.
     */
    SEARCH,
}
