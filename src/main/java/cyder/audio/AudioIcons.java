package cyder.audio;

import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.utils.StaticUtil;

import javax.swing.*;

/**
 * All icons from the static audio directory loaded as {@link javax.swing.ImageIcon}s.
 */
public final class AudioIcons {
    /**
     * The play icon.
     */
    public static final ImageIcon playIcon = StaticUtil.getImageIcon("Play.png");

    /**
     * The play icon for hover events.
     */
    public static final ImageIcon playIconHover = StaticUtil.getImageIcon("PlayHover.png");

    /**
     * The pause icon.
     */
    public static final ImageIcon pauseIcon = StaticUtil.getImageIcon("Pause.png");

    /**
     * The pause icon for hover events.
     */
    public static final ImageIcon pauseIconHover = StaticUtil.getImageIcon("PauseHover.png");

    /**
     * The next icon.
     */
    public static final ImageIcon nextIcon = StaticUtil.getImageIcon("Skip.png");

    /**
     * The next icon for hover events.
     */
    public static final ImageIcon nextIconHover = StaticUtil.getImageIcon("SkipHover.png");

    /**
     * The last icon.
     */
    public static final ImageIcon lastIcon = StaticUtil.getImageIcon("SkipBack.png");

    /**
     * The last icon for hover events.
     */
    public static final ImageIcon lastIconHover = StaticUtil.getImageIcon("SkipBackHover.png");

    /**
     * The repeat icon.
     */
    public static final ImageIcon repeatIcon = StaticUtil.getImageIcon("Repeat.png");

    /**
     * The repeat icon for hover events.
     */
    public static final ImageIcon repeatIconHover = StaticUtil.getImageIcon("RepeatHover.png");

    /**
     * The shuffle icon.
     */
    public static final ImageIcon shuffleIcon = StaticUtil.getImageIcon("Shuffle.png");

    /**
     * The shuffle icon for hover events.
     */
    public static final ImageIcon shuffleIconHover = StaticUtil.getImageIcon("ShuffleHover.png");

    /**
     * Suppress default constructor.
     */
    private AudioIcons() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
