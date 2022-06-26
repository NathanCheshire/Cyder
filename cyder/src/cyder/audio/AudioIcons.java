package cyder.audio;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import javax.swing.*;

/**
 * All icons from static/pictures/music in the form of {@link javax.swing.ImageIcon}s.
 */
public final class AudioIcons {
    /**
     * The play icon.
     */
    public static final ImageIcon playIcon = new ImageIcon("static/pictures/music/Play.png");

    /**
     * The play icon for hover events.
     */
    public static final ImageIcon playIconHover = new ImageIcon("static/pictures/music/PlayHover.png");

    /**
     * The pause icon.
     */
    public static final ImageIcon pauseIcon = new ImageIcon("static/pictures/music/Pause.png");

    /**
     * The pause icon for hover events.
     */
    public static final ImageIcon pauseIconHover = new ImageIcon("static/pictures/music/PauseHover.png");

    /**
     * The next icon.
     */
    public static final ImageIcon nextIcon = new ImageIcon("static/pictures/music/Skip.png");

    /**
     * The next icon for hover events.
     */
    public static final ImageIcon nextIconHover = new ImageIcon("static/pictures/music/SkipHover.png");

    /**
     * The last icon.
     */
    public static final ImageIcon lastIcon = new ImageIcon("static/pictures/music/SkipBack.png");

    /**
     * The last icon for hover events.
     */
    public static final ImageIcon lastIconHover = new ImageIcon("static/pictures/music/SkipBackHover.png");

    /**
     * The repeat icon.
     */
    public static final ImageIcon repeatIcon = new ImageIcon("static/pictures/music/Repeat.png");

    /**
     * The repeat icon for hover events.
     */
    public static final ImageIcon repeatIconHover = new ImageIcon("static/pictures/music/RepeatHover.png");

    /**
     * The shuffle icon.
     */
    public static final ImageIcon shuffleIcon = new ImageIcon("static/pictures/music/Shuffle.png");

    /**
     * The shuffle icon for hover events.
     */
    public static final ImageIcon shuffleIconHover = new ImageIcon("static/pictures/music/ShuffleHover.png");

    /**
     * The alternate view icon.
     */
    public static final ImageIcon alternateView = new ImageIcon("static/pictures/icons/ChangeSize1.png");

    /**
     * The alternate view hover icon.
     */
    public static final ImageIcon alternateViewHover = new ImageIcon("static/pictures/icons/ChangeSize2.png");

    /**
     * Suppress default constructor.
     */
    private AudioIcons() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
