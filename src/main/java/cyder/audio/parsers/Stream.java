package cyder.audio.parsers;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

public class Stream {
    private int index;

    @SerializedName("codec_name")
    private String codecName;

    @SerializedName("codec_long_name")
    private String codecLongName;

    @SerializedName("codec_type")
    private String codecType;

    @SerializedName("codec_tag_string")
    private String codecTagString;

    @SerializedName("codec_tag")
    private String codecTag;

    @SerializedName("sample_rate")
    private String sampleRate;

    private int channels;

    @SerializedName("channel_layout")
    private String channelLayout;

    @SerializedName("bits_per_sample")
    private int bitsPerSample;

    @SerializedName("r_frame_rate")
    private String rFrameRate;

    @SerializedName("avg_frame_rate")
    private String avgFrameRate;

    @SerializedName("time_base")
    private String timeBase;

    @SerializedName("start_pts")
    private int startPts;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("duration_ts")
    private long durationTs;

    private String duration;

    @SerializedName("bit_rate")
    private int bitRate;

    private Disposition disposition;

    private Tags tags;

    public Stream() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getCodecLongName() {
        return codecLongName;
    }

    public void setCodecLongName(String codecLongName) {
        this.codecLongName = codecLongName;
    }

    public String getCodecType() {
        return codecType;
    }

    public void setCodecType(String codecType) {
        this.codecType = codecType;
    }

    public String getCodecTagString() {
        return codecTagString;
    }

    public void setCodecTagString(String codecTagString) {
        this.codecTagString = codecTagString;
    }

    public String getCodecTag() {
        return codecTag;
    }

    public void setCodecTag(String codecTag) {
        this.codecTag = codecTag;
    }

    public String getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public String getChannelLayout() {
        return channelLayout;
    }

    public void setChannelLayout(String channelLayout) {
        this.channelLayout = channelLayout;
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public void setBitsPerSample(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    public String getrFrameRate() {
        return rFrameRate;
    }

    public void setrFrameRate(String rFrameRate) {
        this.rFrameRate = rFrameRate;
    }

    public String getAvgFrameRate() {
        return avgFrameRate;
    }

    public void setAvgFrameRate(String avgFrameRate) {
        this.avgFrameRate = avgFrameRate;
    }

    public String getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(String timeBase) {
        this.timeBase = timeBase;
    }

    public int getStartPts() {
        return startPts;
    }

    public void setStartPts(int startPts) {
        this.startPts = startPts;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public long getDurationTs() {
        return durationTs;
    }

    public void setDurationTs(long durationTs) {
        this.durationTs = durationTs;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    public void setDisposition(Disposition disposition) {
        this.disposition = disposition;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }
}
