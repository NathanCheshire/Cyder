package cyder.ui;

import cyder.utilities.ReflectionUtil;

import javax.swing.*;

public class CyderProgressBar extends JProgressBar {
    public CyderProgressBar(int orientation) {
        super(orientation);
    }

    public CyderProgressBar(int min, int max) {
        super(min, max);
    }

    public CyderProgressBar(int orientation, int min, int max) {
        super(orientation, min, max);
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}
