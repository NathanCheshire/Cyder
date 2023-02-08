package cyder.audio.parsers;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.Objects;

/**
 * A stream parser class for the stream objects contained within {@link ShowStreamOutput#getStreams()}.
 */
public class Stream {
    /**
     * The index of the stream.
     */
    private int index;

    /**
     * The name of the codec device.
     */
    @SerializedName("codec_name")
    private String codecName;

    /**
     * The full name of the codec device.
     */
    @SerializedName("codec_long_name")
    private String codecLongName;

    /**
     * The type of the codec device.
     */
    @SerializedName("codec_type")
    private String codecType;

    /**
     * The tag string of the codec device.
     */
    @SerializedName("codec_tag_string")
    private String codecTagString;

    /**
     * The tag of the codec device.
     */
    @SerializedName("codec_tag")
    private String codecTag;

    /**
     * The sample rate of this stream.
     */
    @SerializedName("sample_rate")
    private String sampleRate;

    /**
     * The number of channels of this stream.
     */
    private int channels;

    /**
     * The channel layout of this stream.
     */
    @SerializedName("channel_layout")
    private String channelLayout;

    /**
     * The bits per sample of this stream.
     */
    @SerializedName("bits_per_sample")
    private int bitsPerSample;

    /**
     * The "r" frame rate of this stream.
     */
    @SerializedName("r_frame_rate")
    private String rFrameRate;

    /**
     * The average frame rate of this stream.
     */
    @SerializedName("avg_frame_rate")
    private String avgFrameRate;

    /**
     * The time base of this stream.
     */
    @SerializedName("time_base")
    private String timeBase;

    /**
     * The pts of the first package of this stream.
     */
    @SerializedName("start_pts")
    private int startPts;

    /**
     * The start time of this stream.
     */
    @SerializedName("start_time")
    private String startTime;

    /**
     * The duration time base of this stream.
     */
    @SerializedName("duration_ts")
    private long durationTs;

    /**
     * The duration of this stream.
     */
    private String duration;

    /**
     * The bit rate of this stream.
     */
    @SerializedName("bit_rate")
    private int bitRate;

    /**
     * The disposition of this stream
     */
    private Disposition disposition;

    /**
     * The tags object of this stream.
     */
    private Tags tags;

    /**
     * Constructs a new stream object.
     */
    public Stream() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the index of the stream.
     *
     * @return the index of the stream
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of the stream.
     *
     * @param index the index of the stream
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the name of the codec device.
     *
     * @return the name of the codec device
     */
    public String getCodecName() {
        return codecName;
    }

    /**
     * Sets the name of the codec device.
     *
     * @param codecName the name of the codec device
     */
    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    /**
     * Returns the full name of the codec device.
     *
     * @return the full name of the codec device
     */
    public String getCodecLongName() {
        return codecLongName;
    }

    /**
     * Sets the full name of the codec device.
     *
     * @param codecLongName full name of the codec device
     */
    public void setCodecLongName(String codecLongName) {
        this.codecLongName = codecLongName;
    }

    /**
     * Returns the type of the codec device.
     *
     * @return the type of the codec device
     */
    public String getCodecType() {
        return codecType;
    }

    /**
     * Sets the type of the codec device.
     *
     * @param codecType the type of the codec device
     */
    public void setCodecType(String codecType) {
        this.codecType = codecType;
    }

    /**
     * Returns the tag string of the codec device
     *
     * @return the tag string of the codec device
     */
    public String getCodecTagString() {
        return codecTagString;
    }

    /**
     * Sets the tag string of the codec device
     *
     * @param codecTagString the tag string of the codec device
     */
    public void setCodecTagString(String codecTagString) {
        this.codecTagString = codecTagString;
    }

    /**
     * Returns the tag of the codec device
     *
     * @return the tag of the codec device
     */
    public String getCodecTag() {
        return codecTag;
    }

    /**
     * Sets the tag of the codec device
     *
     * @param codecTag tag of the codec device
     */
    public void setCodecTag(String codecTag) {
        this.codecTag = codecTag;
    }

    /**
     * Returns the sample rate of this stream.
     *
     * @return the sample rate of this stream
     */
    public String getSampleRate() {
        return sampleRate;
    }

    /**
     * Sets the sample rate of this stream.
     *
     * @param sampleRate the sample rate of this stream
     */
    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * Returns the number of channels of this stream.
     *
     * @return the number of channels of this stream
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Sets the number of channels of this stream.
     *
     * @param channels the number of channels of this stream
     */
    public void setChannels(int channels) {
        this.channels = channels;
    }

    /**
     * Returns the channel layout of this stream.
     *
     * @return the channel layout of this stream
     */
    public String getChannelLayout() {
        return channelLayout;
    }

    /**
     * Sets the channel layout of this stream.
     *
     * @param channelLayout the channel layout of this stream
     */
    public void setChannelLayout(String channelLayout) {
        this.channelLayout = channelLayout;
    }

    /**
     * Returns the bits per sample of this.
     *
     * @return the bits per sample of this
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /**
     * Sets the bits per sample of this .
     *
     * @param bitsPerSample the bits per sample of this
     */
    public void setBitsPerSample(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    /**
     * Returns the "r" frame rate of this stream.
     *
     * @return the "r" frame rate of this stream
     */
    public String getrFrameRate() {
        return rFrameRate;
    }

    /**
     * Sets the "r" frame rate of this stream.
     *
     * @param rFrameRate the "r" frame rate of this stream
     */
    public void setrFrameRate(String rFrameRate) {
        this.rFrameRate = rFrameRate;
    }

    /**
     * Returns the average frame rate of this stream.
     *
     * @return the average frame rate of this stream
     */
    public String getAvgFrameRate() {
        return avgFrameRate;
    }

    /**
     * Sets the average frame rate of this stream
     *
     * @param avgFrameRate average frame rate of this stream
     */
    public void setAvgFrameRate(String avgFrameRate) {
        this.avgFrameRate = avgFrameRate;
    }

    /**
     * Returns the time base of this stream.
     *
     * @return the time base of this stream
     */
    public String getTimeBase() {
        return timeBase;
    }

    /**
     * Sets the time base of this stream.
     *
     * @param timeBase the time base of this stream
     */
    public void setTimeBase(String timeBase) {
        this.timeBase = timeBase;
    }

    /**
     * Returns the pts of the first package of this stream.
     *
     * @return the pts of the first package of this stream
     */
    public int getStartPts() {
        return startPts;
    }

    /**
     * Sets the pts of the first package of this stream.
     *
     * @param startPts the pts of the first package of this stream
     */
    public void setStartPts(int startPts) {
        this.startPts = startPts;
    }

    /**
     * Returns the start time of this stream.
     *
     * @return the start time of this stream
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of this stream.
     *
     * @param startTime the start time of this stream
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the duration time base of this stream.
     *
     * @return the duration time base of this stream
     */
    public long getDurationTs() {
        return durationTs;
    }

    /**
     * Sets the duration time base of this stream.
     *
     * @param durationTs the duration time base of this stream
     */
    public void setDurationTs(long durationTs) {
        this.durationTs = durationTs;
    }

    /**
     * Returns the duration of this stream.
     *
     * @return the duration of this stream
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Sets the duration of this stream.
     *
     * @param duration the duration of this stream
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * Returns the bit rate of this stream.
     *
     * @return the bit rate of this stream
     */
    public int getBitRate() {
        return bitRate;
    }

    /**
     * Sets the bit rate of this stream.
     *
     * @param bitRate the bit rate of this stream
     */
    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    /**
     * Returns the disposition of this stream.
     *
     * @return the disposition of this stream
     */
    public Disposition getDisposition() {
        return disposition;
    }

    /**
     * Sets the the disposition of this stream.
     *
     * @param disposition the disposition of this stream
     */
    public void setDisposition(Disposition disposition) {
        this.disposition = disposition;
    }

    /**
     * Returns the tags object of this stream.
     *
     * @return the tags object of this stream
     */
    public Tags getTags() {
        return tags;
    }

    /**
     * Sets the tags object of this stream.
     *
     * @param tags the tags object of this stream
     */
    public void setTags(Tags tags) {
        this.tags = tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Stream{"
                + "index=" + index
                + ", codecName=\"" + codecName + "\""
                + ", codecLongName=\"" + codecLongName + "\""
                + ", codecType=\"" + codecType + "\""
                + ", codecTagString=\"" + codecTagString + "\""
                + ", codecTag=\"" + codecTag + "\""
                + ", sampleRate=\"" + sampleRate + "\""
                + ", channels=" + channels
                + ", channelLayout=\"" + channelLayout + "\""
                + ", bitsPerSample=" + bitsPerSample
                + ", rFrameRate=\"" + rFrameRate + "\""
                + ", avgFrameRate=\"" + avgFrameRate + "\""
                + ", timeBase=\"" + timeBase + "\""
                + ", startPts=" + startPts
                + ", startTime=\"" + startTime + "\""
                + ", durationTs=" + durationTs
                + ", duration=\"" + duration + "\""
                + ", bitRate=" + bitRate
                + ", disposition=" + disposition
                + ", tags=" + tags
                + "}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(index);
        ret = 31 * ret + codecName.hashCode();
        ret = 31 * ret + codecLongName.hashCode();
        ret = 31 * ret + codecType.hashCode();
        ret = 31 * ret + codecTagString.hashCode();
        ret = 31 * ret + codecTag.hashCode();
        ret = 31 * ret + sampleRate.hashCode();
        ret = 31 * ret + Integer.hashCode(channels);
        ret = 31 * ret + channelLayout.hashCode();
        ret = 31 * ret + Integer.hashCode(bitsPerSample);
        ret = 31 * ret + rFrameRate.hashCode();
        ret = 31 * ret + avgFrameRate.hashCode();
        ret = 31 * ret + timeBase.hashCode();
        ret = 31 * ret + Integer.hashCode(startPts);
        ret = 31 * ret + startTime.hashCode();
        ret = 31 * ret + Long.hashCode(durationTs);
        ret = 31 * ret + duration.hashCode();
        ret = 31 * ret + Integer.hashCode(bitRate);
        ret = 31 * ret + disposition.hashCode();
        ret = 31 * ret + tags.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Stream)) {
            return false;
        }

        Stream other = (Stream) o;
        return index == other.index
                && Objects.equals(codecName, other.codecName)
                && Objects.equals(codecLongName, other.codecLongName)
                && Objects.equals(codecType, other.codecType)
                && Objects.equals(codecTagString, other.codecTagString)
                && Objects.equals(codecTag, other.codecTag)
                && Objects.equals(sampleRate, other.sampleRate)
                && channels == other.channels
                && Objects.equals(channelLayout, other.channelLayout)
                && bitsPerSample == other.bitsPerSample
                && Objects.equals(rFrameRate, other.rFrameRate)
                && Objects.equals(avgFrameRate, other.avgFrameRate)
                && Objects.equals(timeBase, other.timeBase)
                && startPts == other.startPts
                && Objects.equals(startTime, other.startTime)
                && durationTs == other.durationTs
                && Objects.equals(duration, other.duration)
                && bitRate == other.bitRate
                && disposition == other.disposition
                && tags == other.tags;
    }
}
