package cyder.handlers.external;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// todo this should actually be inside of audio player.java

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

    @CanIgnoreReturnValue
    public boolean requestProgressPercentage(float percent) {
        // mouse pressed means during a drag event or in between a click event
        if (!mousePressed) {
            Preconditions.checkArgument(progressBarRange.contains(percent));
            currentProgressPercent = percent;

            // todo update bar (label will automatically follow)
        }

        return false;
    }

    private Color firstAnimationColor = new Color(236, 64, 122);
    private Color secondAnimationColor = new Color(85, 85, 255);

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

    private boolean mousePressed;

    public MouseListener componentMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            // todo update progress bar loc and audio loc
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mousePressed = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (mousePressed) {
                // todo update progress bar location
                mousePressed = false;
            }
        }
    };

    // todo label should always follow the progress bar percentage

    // todo when mousePressed, stop updating progress bar based on audio's location

    public MouseMotionListener componentMouseMotionListener = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (mousePressed) {
                // todo update progress bar location but not audio position
            }
        }
    };
}
