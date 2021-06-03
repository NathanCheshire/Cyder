package cyder.ui;

import cyder.consts.CyderStrings;

import javax.swing.*;
import java.awt.*;

/**
 * A simple extension of {@link CyderFrame} which performs common setup steps to speed up UIUX testing.
 */
public class TestFrame extends CyderFrame {

    private CyderFrame testFrame;

    public TestFrame() {
        testFrame = new CyderFrame(600,600, new ImageIcon(CyderStrings.DEFAULT_BACKGROUND_PATH));
        testFrame.setTitle("Test Frame");
        testFrame.setTitlePosition(TitlePosition.CENTER);
        testFrame.stealConsoleBackground();
        testFrame.initResizing();
        testFrame.setResizable(true);
        testFrame.setMinimumSize(new Dimension(200,200));
        testFrame.setMaximumSize(new Dimension(1000, 1000));
        testFrame.setSnapSize(new Dimension(1,1));
        testFrame.setLocationRelativeTo(null);
        testFrame.setVisible(true);
    }
}
