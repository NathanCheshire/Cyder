package cyder.ui;

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
}
