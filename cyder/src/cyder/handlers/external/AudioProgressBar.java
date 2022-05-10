package cyder.handlers.external;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.constants.CyderColors;

import javax.swing.*;
import java.awt.*;

/**
 * The audio progress bar used for the audio player.
 */
public class AudioProgressBar {
    private final int width;
    private final int height;

    public AudioProgressBar(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public JLabel generateProgressBar() {
        return null;
    }

    public void startAnimaiton() {

    }

    public void stopAnimation() {

    }

    public static final Range<Float> progressBarRange = Range.closed(0.0f, 100.0f);

    private float currentProgressPercent;

    public float getCurrentProgressPercent() {
        return currentProgressPercent;
    }

    public void setCurrentProgressPercent(float percent) {
        Preconditions.checkArgument(progressBarRange.contains(percent));

        currentProgressPercent = percent;

        // todo update bar and label
    }

    private Color firstAnimationColor = CyderColors.regularPink;
    private Color secondAnimationColor = CyderColors.notificationForegroundColor;

    public Color getFirstAnimationColor() {
        return firstAnimationColor;
    }

    public void setFirstAnimationColor(Color firstAnimationColor) {
        this.firstAnimationColor = firstAnimationColor;
    }

    public Color getSecondAnimationColor() {
        return secondAnimationColor;
    }

    public void setSecondAnimationColor(Color secondAnimationColor) {
        this.secondAnimationColor = secondAnimationColor;
    }
}
