package cyder.audio.parsers;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A disposition object serializer for a {@link Stream} object.
 */
public class Disposition {
    /**
     * The default prop.
     */
    @SerializedName("default")
    private int defaultProp;

    /**
     * The dub.
     */
    private int dub;
    private int ordinal;
    private int comment;
    private int lyrics;
    private int karaoke;
    private int forced;

    @SerializedName("hearing_impaired")
    private int hearingImpaired;

    @SerializedName("visual_impaired")
    private int visualImpaired;

    @SerializedName("clean_effects")
    private int cleanEffects;

    @SerializedName("attached_pic")
    private int attachedPic;

    @SerializedName("timed_thumbnails")
    private int timedThumbnails;

    /**
     * Constructs a new disposition object.
     */
    public Disposition() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the default prop.
     *
     * @return the default prop
     */
    public int getDefaultProp() {
        return defaultProp;
    }

    /**
     * Sets the default prop.
     *
     * @param defaultProp the default prop
     */
    public void setDefaultProp(int defaultProp) {
        this.defaultProp = defaultProp;
    }

    public int getDub() {
        return dub;
    }

    public void setDub(int dub) {
        this.dub = dub;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public int getLyrics() {
        return lyrics;
    }

    public void setLyrics(int lyrics) {
        this.lyrics = lyrics;
    }

    public int getKaraoke() {
        return karaoke;
    }

    public void setKaraoke(int karaoke) {
        this.karaoke = karaoke;
    }

    public int getForced() {
        return forced;
    }

    public void setForced(int forced) {
        this.forced = forced;
    }

    public int getHearingImpaired() {
        return hearingImpaired;
    }

    public void setHearingImpaired(int hearingImpaired) {
        this.hearingImpaired = hearingImpaired;
    }

    public int getVisualImpaired() {
        return visualImpaired;
    }

    public void setVisualImpaired(int visualImpaired) {
        this.visualImpaired = visualImpaired;
    }

    public int getCleanEffects() {
        return cleanEffects;
    }

    public void setCleanEffects(int cleanEffects) {
        this.cleanEffects = cleanEffects;
    }

    public int getAttachedPic() {
        return attachedPic;
    }

    public void setAttachedPic(int attachedPic) {
        this.attachedPic = attachedPic;
    }

    public int getTimedThumbnails() {
        return timedThumbnails;
    }

    public void setTimedThumbnails(int timedThumbnails) {
        this.timedThumbnails = timedThumbnails;
    }
}
