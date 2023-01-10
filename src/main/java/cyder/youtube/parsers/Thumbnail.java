package cyder.youtube.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * The thumbnail object for a {@link Thumbnails} object.
 */
public class Thumbnail {
    /**
     * The url of the image.
     */
    private String url;

    /**
     * The width of the image.
     */
    private int width;

    /**
     * The height of the image.
     */
    private int height;

    /**
     * Constructs a new thumbnail.
     */
    public Thumbnail() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the url of the image.
     *
     * @return the url of the image
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url of the image.
     *
     * @param url the url of the image
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the width of the image.
     *
     * @return the width of the image
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the image.
     *
     * @param width the width of the image
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the height of the image.
     *
     * @return the height of the image
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the image.
     *
     * @param height the height of the image
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Thumbnail)) {
            return false;
        }

        Thumbnail other = (Thumbnail) o;
        return other.url.equals(url)
                && other.width == width
                && other.height == height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = url.hashCode();
        ret = 31 * ret + Integer.hashCode(width);
        ret = 31 * ret + Integer.hashCode(height);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Thumbnail{"
                + "url=\"" + url + "\""
                + ", width=" + width
                + ", height=" + height
                + "}";
    }
}
