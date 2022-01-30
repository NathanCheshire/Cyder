package cyder.ui;

import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CyderProgressBar extends JProgressBar {
    public CyderProgressBar(int orientation) {
        super(orientation);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.ACTION, e.getComponent());
            }
        });
    }

    public CyderProgressBar(int min, int max) {
        super(min, max);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.ACTION, e.getComponent());
            }
        });
    }

    public CyderProgressBar(int orientation, int min, int max) {
        super(orientation, min, max);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.ACTION, e.getComponent());
            }
        });
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}
