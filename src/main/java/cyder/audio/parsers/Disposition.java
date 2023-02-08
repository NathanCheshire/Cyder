package cyder.audio.parsers;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A disposition object serializer for a {@link Stream} object.
 */
public class Disposition {
    /**
     * The default prop value.
     */
    @SerializedName("default")
    private int defaultProp;

    /**
     * The dub value.
     */
    private int dub;

    /**
     * The ordinal value.
     */
    private int ordinal;

    /**
     * The comment value.
     */
    private int comment;

    /**
     * the lyrics value.
     */
    private int lyrics;

    /**
     * The karaoke value.
     */
    private int karaoke;

    /**
     * The forced value.
     */
    private int forced;

    /**
     * The hearing impaired value.
     */
    @SerializedName("hearing_impaired")
    private int hearingImpaired;

    /**
     * The visual impaired value.
     */
    @SerializedName("visual_impaired")
    private int visualImpaired;

    /**
     * The clean effects value.
     */
    @SerializedName("clean_effects")
    private int cleanEffects;

    /**
     * The attached pic value.
     */
    @SerializedName("attached_pic")
    private int attachedPic;

    /**
     * The timed thumbnails value.
     */
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

    /**
     * Returns the dub value.
     *
     * @return the dub value
     */
    public int getDub() {
        return dub;
    }

    /**
     * Sets the dub value
     *
     * @param dub the dub value
     */
    public void setDub(int dub) {
        this.dub = dub;
    }

    /**
     * Returns the ordinal value.
     *
     * @return the ordinal value
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * Sets the ordinal value.
     *
     * @param ordinal the ordinal value
     */
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    /**
     * Returns the comment value.
     *
     * @return the comment value
     */
    public int getComment() {
        return comment;
    }

    /**
     * Sets the comment value.
     *
     * @param comment the comment value
     */
    public void setComment(int comment) {
        this.comment = comment;
    }

    /**
     * Returns the lyrics value.
     *
     * @return the lyrics value
     */
    public int getLyrics() {
        return lyrics;
    }

    /**
     * Sets the lyrics value.
     *
     * @param lyrics the lyrics value
     */
    public void setLyrics(int lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * Returns the karaoke value.
     *
     * @return the karaoke value
     */
    public int getKaraoke() {
        return karaoke;
    }

    /**
     * Sets the karaoke value.
     *
     * @param karaoke the karaoke value
     */
    public void setKaraoke(int karaoke) {
        this.karaoke = karaoke;
    }

    /**
     * Returns the forced value.
     *
     * @return the forced value
     */
    public int getForced() {
        return forced;
    }

    /**
     * Sets the forced value.
     *
     * @param forced the forced value
     */
    public void setForced(int forced) {
        this.forced = forced;
    }

    /**
     * Returns the hearing impaired value.
     *
     * @return the hearing impaired value
     */
    public int getHearingImpaired() {
        return hearingImpaired;
    }

    /**
     * Sets the hearing impaired value.
     *
     * @param hearingImpaired the hearing impaired value
     */
    public void setHearingImpaired(int hearingImpaired) {
        this.hearingImpaired = hearingImpaired;
    }

    /**
     * Returns the visual impaired value.
     *
     * @return the visual impaired value
     */
    public int getVisualImpaired() {
        return visualImpaired;
    }

    /**
     * Sets the visual impaired value.
     *
     * @param visualImpaired the visual impaired value
     */
    public void setVisualImpaired(int visualImpaired) {
        this.visualImpaired = visualImpaired;
    }

    /**
     * Returns the clean effects value.
     *
     * @return the clean effects value
     */
    public int getCleanEffects() {
        return cleanEffects;
    }

    /**
     * Sets the clean effects value.
     *
     * @param cleanEffects the clean effects value
     */
    public void setCleanEffects(int cleanEffects) {
        this.cleanEffects = cleanEffects;
    }

    /**
     * Returns the attached pic value.
     *
     * @return the attached pic value
     */
    public int getAttachedPic() {
        return attachedPic;
    }

    /**
     * Sets the attached pic value.
     *
     * @param attachedPic the attached pic value
     */
    public void setAttachedPic(int attachedPic) {
        this.attachedPic = attachedPic;
    }

    /**
     * Returns the timed thumbnails value.
     *
     * @return the timed thumbnails value
     */
    public int getTimedThumbnails() {
        return timedThumbnails;
    }

    /**
     * Sets the timed thumbnails value.
     *
     * @param timedThumbnails the timed thumbnails value
     */
    public void setTimedThumbnails(int timedThumbnails) {
        this.timedThumbnails = timedThumbnails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Disposition{"
                + "defaultProp=" + defaultProp
                + ", dub=" + dub
                + ", ordinal=" + ordinal
                + ", comment=" + comment
                + ", lyrics=" + lyrics
                + ", karaoke=" + karaoke
                + ", forced=" + forced
                + ", hearingImpaired=" + hearingImpaired
                + ", visualImpaired=" + visualImpaired
                + ", cleanEffects=" + cleanEffects
                + ", attachedPic=" + attachedPic
                + ", timedThumbnails=" + timedThumbnails
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(defaultProp);
        ret = 31 * ret + Integer.hashCode(dub);
        ret = 31 * ret + Integer.hashCode(ordinal);
        ret = 31 * ret + Integer.hashCode(comment);
        ret = 31 * ret + Integer.hashCode(lyrics);
        ret = 31 * ret + Integer.hashCode(karaoke);
        ret = 31 * ret + Integer.hashCode(forced);
        ret = 31 * ret + Integer.hashCode(hearingImpaired);
        ret = 31 * ret + Integer.hashCode(visualImpaired);
        ret = 31 * ret + Integer.hashCode(cleanEffects);
        ret = 31 * ret + Integer.hashCode(attachedPic);
        ret = 31 * ret + Integer.hashCode(timedThumbnails);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Disposition)) {
            return false;
        }

        Disposition other = (Disposition) o;
        return defaultProp == other.defaultProp
                && dub == other.dub
                && ordinal == other.ordinal
                && comment == other.comment
                && lyrics == other.lyrics
                && karaoke == other.karaoke
                && forced == other.forced
                && hearingImpaired == other.hearingImpaired
                && visualImpaired == other.visualImpaired
                && cleanEffects == other.cleanEffects
                && attachedPic == other.attachedPic
                && timedThumbnails == other.timedThumbnails;
    }
}
