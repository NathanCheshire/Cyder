package cyder.handlers.external.audio.youtube;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the thumbnails linking to a youtube video.
 */
@SuppressWarnings("unused")
public class Thumbnails {
    @SerializedName("default")
    private Thumbnail _default;
    private Thumbnail medium;
    private Thumbnail high;

    public Thumbnail get_default() {
        return _default;
    }

    public void set_default(Thumbnail _default) {
        this._default = _default;
    }

    public Thumbnail getMedium() {
        return medium;
    }

    public void setMedium(Thumbnail medium) {
        this.medium = medium;
    }

    public Thumbnail getHigh() {
        return high;
    }

    public void setHigh(Thumbnail high) {
        this.high = high;
    }
}
