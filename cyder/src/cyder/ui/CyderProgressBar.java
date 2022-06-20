package cyder.ui;

import cyder.handlers.internal.Logger;
import cyder.utils.ReflectionUtil;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A wrapper for JProgressBar to Cyder-fy it.
 */
public class CyderProgressBar extends JProgressBar {
    /**
     * Constructs a new CyderProgressBar.
     *
     * @param orientation the orientation of the progressbar
     */
    public CyderProgressBar(int orientation) {
        super(orientation);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Constructs a new CyderProgressBar.
     *
     * @param min the minimum progress bar value
     * @param max the maximum progress bar value
     */
    public CyderProgressBar(int min, int max) {
        super(min, max);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
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
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}
