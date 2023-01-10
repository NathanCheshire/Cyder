package cyder.youtube.parsers;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * Holds the thumbnails linking to a YouTube video.
 * The thumbnails object for a {@link Snippet}.
 */
public class Thumbnails {
    /**
     * The default resolution thumbnail object.
     */
    @SerializedName("default")
    private Thumbnail _default;

    /**
     * The medium resolution thumbnail object.
     */
    private Thumbnail medium;

    /**
     * The highest resolution thumbnail object.
     */
    private Thumbnail high;

    /**
     * Constructs a new thumbnails object.
     */
    public Thumbnails() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the default resolution thumbnail object.
     *
     * @return the default resolution thumbnail object
     */
    public Thumbnail get_default() {
        return _default;
    }

    /**
     * Sets the default resolution thumbnail object.
     *
     * @param _default the default resolution thumbnail object
     */
    public void set_default(Thumbnail _default) {
        this._default = _default;
    }

    /**
     * Returns the medium resolution thumbnail object.
     *
     * @return the medium resolution thumbnail object
     */
    public Thumbnail getMedium() {
        return medium;
    }

    /**
     * Sets the medium resolution thumbnail object.
     *
     * @param medium the medium resolution thumbnail object
     */
    public void setMedium(Thumbnail medium) {
        this.medium = medium;
    }

    /**
     * Returns the high resolution thumbnail object.
     *
     * @return the high resolution thumbnail object
     */
    public Thumbnail getHigh() {
        return high;
    }

    /**
     * Sets the high resolution thumbnail object.
     *
     * @param high the high resolution thumbnail object
     */
    public void setHigh(Thumbnail high) {
        this.high = high;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Thumbnails)) {
            return false;
        }

        Thumbnails other = (Thumbnails) o;
        return other._default.equals(_default)
                && other.medium.equals(medium)
                && other.high.equals(high);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = _default.hashCode();
        ret = 31 * ret + medium.hashCode();
        ret = 31 * ret + high.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Thumbnails{"
                + "default=" + _default
                + ", medium=" + medium
                + ", high=" + high
                + "}";
    }
}
