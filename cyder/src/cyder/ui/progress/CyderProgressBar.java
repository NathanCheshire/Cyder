package cyder.ui.progress;

import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.StringUtil;
import cyder.utils.UiUtil;

import javax.swing.*;

/**
 * A progress bar styled and configured for Cyder.
 */
public class CyderProgressBar extends JProgressBar {
    /**
     * Constructs a new CyderProgressBar.
     *
     * @param orientation the orientation of the progressbar
     */
    public CyderProgressBar(int orientation) {
        super(orientation);
        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Constructs a new CyderProgressBar.
     *
     * @param min the minimum progress bar value
     * @param max the maximum progress bar value
     */
    public CyderProgressBar(int min, int max) {
        super(min, max);
        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Constructs a new CyderProgressBar.
     *
     * @param orientation the orientation of the progressbar
     * @param min         the minimum progress bar value
     * @param max         the maximum progress bar value
     */
    public CyderProgressBar(int orientation, int min, int max) {
        super(orientation, min, max);
        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return StringUtil.commonCyderUiToString(this);
    }
}
