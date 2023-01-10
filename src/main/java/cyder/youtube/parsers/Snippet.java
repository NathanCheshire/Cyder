package cyder.youtube.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A snippet for a {@link YouTubeVideo} object.
 */
public class Snippet {
    /**
     * The formatted time string the YouTube video was published at.
     */
    private String publishedAt;

    /**
     * The channel ID of the owner of the video.
     */
    private String channelId;

    /**
     * The title of the video.
     */
    private String title;

    /**
     * The video description.
     */
    private String description;

    /**
     * The thumbnails object.
     */
    private Thumbnails thumbnails;

    /**
     * The title of the video's publishing channel.
     */
    private String channelTitle;

    /**
     * The live broadcast content.
     */
    private String liveBroadcastContent;

    /**
     * The time at which the video was published.
     */
    private String publishTime;

    /**
     * Constructs a new Snippet.
     */
    public Snippet() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the formatted time string the YouTube video was published at.
     *
     * @return the formatted time string the YouTube video was published at
     */
    public String getPublishedAt() {
        return publishedAt;
    }

    /**
     * Sets the formatted time string the YouTube video was published at.
     *
     * @param publishedAt the formatted time string the YouTube video was published at
     */
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    /**
     * Returns the channel ID of the owner of the video.
     *
     * @return the channel ID of the owner of the video
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Sets the channel ID of the owner of the video.
     *
     * @param channelId the channel ID of the owner of the video
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Returns the title of the video.
     *
     * @return the title of the video
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the video.
     *
     * @param title the title of the video
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the video description.
     *
     * @return the video description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the video description.
     *
     * @param description the video description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the title of the video's publishing channel.
     *
     * @return the title of the video's publishing channel
     */
    public String getChannelTitle() {
        return channelTitle;
    }

    /**
     * Sets the title of the video's publishing channel.
     *
     * @param channelTitle the title of the video's publishing channel
     */
    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    /**
     * Returns the live broadcast content.
     *
     * @return the live broadcast content
     */
    public String getLiveBroadcastContent() {
        return liveBroadcastContent;
    }

    /**
     * Sets the live broadcast content.
     *
     * @param liveBroadcastContent the live broadcast content
     */
    public void setLiveBroadcastContent(String liveBroadcastContent) {
        this.liveBroadcastContent = liveBroadcastContent;
    }

    /**
     * Returns the time at which the video was published.
     *
     * @return the time at which the video was published
     */
    public String getPublishTime() {
        return publishTime;
    }

    /**
     * Sets the time at which the video was published.
     *
     * @param publishTime the time at which the video was published
     */
    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    /**
     * Returns the thumbnails object.
     *
     * @return the thumbnails object
     */
    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    /**
     * Sets the thumbnails object.
     *
     * @param thumbnails the thumbnails object
     */
    public void setThumbnails(Thumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Snippet)) {
            return false;
        }

        Snippet other = (Snippet) o;
        return other.publishedAt.equals(publishedAt)
                && other.channelId.equals(channelId)
                && other.title.equals(title)
                && other.description.equals(description)
                && other.thumbnails.equals(thumbnails)
                && other.channelTitle.equals(channelTitle)
                && other.liveBroadcastContent.equals(liveBroadcastContent)
                && other.publishTime.equals(publishTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = publishedAt.hashCode();
        ret = 31 * ret + channelId.hashCode();
        ret = 31 * ret + title.hashCode();
        ret = 31 * ret + description.hashCode();
        ret = 31 * ret + thumbnails.hashCode();
        ret = 31 * ret + channelTitle.hashCode();
        ret = 31 * ret + liveBroadcastContent.hashCode();
        ret = 31 * ret + publishTime.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Snippet{"
                + "publishedAt=\"" + publishedAt + "\""
                + ", channelId=\"" + channelId + "\""
                + ", title=\"" + title + "\""
                + ", description=\"" + description + "\""
                + ", thumbnails=" + thumbnails
                + ", channelTitle=\"" + channelTitle + "\""
                + ", liveBroadcastContent=\"" + liveBroadcastContent + "\""
                + ", publishTime=\"" + publishTime + "\""
                + "}";
    }
}
