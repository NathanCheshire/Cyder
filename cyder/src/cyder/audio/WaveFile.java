package cyder.audio;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.handlers.internal.ExceptionHandler;
import cyder.utilities.FileUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Wrapper class for a wave (.wav) file.
 * See <a href="http://soundfile.sapp.org/doc/WaveFormat">this link</a>
 * to reference the wav file data structure.
 *
 * Instances of this class are immutable.
 */
@Immutable
public class WaveFile {
    /**
     * The number of bits per sample of a wav file.
     */
    public static final int BITS_PER_SAMPLE = 8;

    /**
     * The number of bytes for an integer primitive.
     */
    public static final int INT_SIZE = 4;

    /**
     * The number of channels of the wav.
     */
    private int numChannels;

    /**
     * The wav byte data.
     */
    private byte[] data;

    /**
     * Whether the file could be decoded and is playable.
     */
    private boolean isPlayable;

    /**
     * The stream for the wav.
     */
    private AudioInputStream audioInputStream;

    /**
     * The audio format of the wav.
     */
    private AudioFormat audioFormat;

    /**
     * The clip object for the wav.
     */
    private Clip clip;

    /**
     * The sample size of the wav.
     */
    private int sampleSize = AudioSystem.NOT_SPECIFIED;

    /**
     * The number of frames of the wav.
     */
    private long numFrames = AudioSystem.NOT_SPECIFIED;

    /**
     * The sample rate of the wav.
     */
    private int sampleRate = AudioSystem.NOT_SPECIFIED;

    /**
     * The wrapped wav file.
     */
    private final File wavFile;

    /**
     * Constructs a new WaveFile object.
     *
     * @param file the wave file
     */
    public WaveFile(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists(), "File does not exist");
        Preconditions.checkArgument(FileUtil.validateExtension(file, ".wav"));

        wavFile = file;
        setup();
    }

    /**
     * Performs setup after the wav file has been validated and set.
     */
    private void setup() {
        try {
            audioInputStream = AudioSystem.getAudioInputStream(wavFile);
            audioFormat = audioInputStream.getFormat();
            numFrames = audioInputStream.getFrameLength();

            sampleRate = (int) audioFormat.getSampleRate();
            sampleSize = audioFormat.getSampleSizeInBits() / BITS_PER_SAMPLE;
            numChannels = audioFormat.getChannels();

            long dataLength = numFrames * audioFormat.getSampleSizeInBits() * audioFormat.getChannels() / 8;

            data = new byte[(int) dataLength];
            audioInputStream.read(data);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        try {
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(wavFile));
            clip.setFramePosition(0);
            isPlayable = true;
        } catch (Exception e) {
            // non 16-bit or 8-bit audio file
            isPlayable = false;
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the amplitude of the wav at the provided sample point.
     * Note that in case of stereos, samples go one after another meaning
     * 0 is the first of the left channel, 1 the first of the right, 2
     * the second of the left, 3 the second of the right, etc.
     *
     * @param samplePoint the point to sample the wav at
     * @return the amplitude at the sample point.
     */
    public int getSampleInt(int samplePoint) {
        Preconditions.checkArgument(samplePoint < 0,
                "Invalid sample point: " + samplePoint);
        Preconditions.checkArgument(samplePoint >= data.length / sampleSize,
                "Invalid sample point: " + samplePoint);

        byte[] sampleBytes = new byte[INT_SIZE];

        for (int i = 0; i < sampleSize; i++) {
            sampleBytes[i] = data[samplePoint * sampleSize * numChannels + i];
        }

        return ByteBuffer.wrap(sampleBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public boolean isPlayable() {
        return isPlayable;
    }

    public void play() {
        clip.start();
    }

    public void stop() {
        clip.stop();
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public double getDurationTime() {
        return getNumFrames() / getAudioFormat().getFrameRate();
    }

    public long getNumFrames() {
        return numFrames;
    }

    /**
     * Returns the sample rate of this wav file.
     *
     * @return the sample rate of this wav file
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Returns the clip for this wav file.
     *
     * @return the clip for this wav file
     */
    public Clip getClip() {
        return clip;
    }
}
