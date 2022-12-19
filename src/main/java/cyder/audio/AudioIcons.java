package main.java.cyder.audio;

import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.utils.StaticUtil;

import javax.swing.*;

/**
 * All icons from static/pictures/music in the form of {@link javax.swing.ImageIcon}s.
 */
public final class AudioIcons {
    /**
     * The play icon.
     */
    public static final ImageIcon playIcon = new ImageIcon(StaticUtil.getStaticPath("Play.png"));

    /**
     * The play icon for hover events.
     */
    public static final ImageIcon playIconHover = new ImageIcon(StaticUtil.getStaticPath("PlayHover.png"));

    /**
     * The pause icon.
     */
    public static final ImageIcon pauseIcon = new ImageIcon(StaticUtil.getStaticPath("Pause.png"));

    /**
     * The pause icon for hover events.
     */
    public static final ImageIcon pauseIconHover = new ImageIcon(StaticUtil.getStaticPath("PauseHover.png"));

    /**
     * The next icon.
     */
    public static final ImageIcon nextIcon = new ImageIcon(StaticUtil.getStaticPath("Skip.png"));

    /**
     * The next icon for hover events.
     */
    public static final ImageIcon nextIconHover = new ImageIcon(StaticUtil.getStaticPath("SkipHover.png"));

    /**
     * The last icon.
     */
    public static final ImageIcon lastIcon = new ImageIcon(StaticUtil.getStaticPath("SkipBack.png"));

    /**
     * The last icon for hover events.
     */
    public static final ImageIcon lastIconHover = new ImageIcon(StaticUtil.getStaticPath("SkipBackHover.png"));

    /**
     * The repeat icon.
     */
    public static final ImageIcon repeatIcon = new ImageIcon(StaticUtil.getStaticPath("Repeat.png"));

    /**
     * The repeat icon for hover events.
     */
    public static final ImageIcon repeatIconHover = new ImageIcon(StaticUtil.getStaticPath("RepeatHover.png"));

    /**
     * The shuffle icon.
     */
    public static final ImageIcon shuffleIcon = new ImageIcon(StaticUtil.getStaticPath("Shuffle.png"));

    /**
     * The shuffle icon for hover events.
     */
    public static final ImageIcon shuffleIconHover = new ImageIcon(
            StaticUtil.getStaticPath("ShuffleHover.png"));

    /**
     * Suppress default constructor.
     */
    private AudioIcons() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
