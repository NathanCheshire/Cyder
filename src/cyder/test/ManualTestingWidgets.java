package cyder.test;

import cyder.consts.CyderImages;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;

import javax.swing.*;

public class ManualTestingWidgets {
    //this was used on 7-1-21 to verify adding/removing buttons to/from drag labels
    public static void addingAndRemovingDragLabelButtonsTest() {
        CyderFrame testFrame = new CyderFrame(600,600, CyderImages.defaultBackground);
        testFrame.setTitle("Test Frame");
        testFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JButton pinButton = new JButton("");
        pinButton.setToolTipText("Pin window");
        pinButton.setIcon(new ImageIcon("sys/pictures/icons/pin.png"));
        pinButton.setContentAreaFilled(false);
        pinButton.setBorderPainted(false);
        pinButton.setFocusPainted(false);
        testFrame.getTopDragLabel().addButton(pinButton, 1);

        CyderButton cb = new CyderButton("Remove first");
        cb.setBounds(100, 100, 150, 40);
        cb.addActionListener(e -> testFrame.getTopDragLabel().removeButton(0));
        testFrame.getContentPane().add(cb);

        CyderButton cb1 = new CyderButton("Remove last");
        cb1.setBounds(100, 180, 150, 40);
        cb1.addActionListener(e -> testFrame.getTopDragLabel().removeButton(testFrame.getTopDragLabel().getButtonListSize() - 1));
        testFrame.getContentPane().add(cb1);

        CyderButton addPinFirst = new CyderButton("Add pin first");
        addPinFirst.setBounds(100, 250, 150, 40);
        addPinFirst.addActionListener(e -> testFrame.getTopDragLabel().addButton(pinButton, 0));
        testFrame.getContentPane().add(addPinFirst);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(null);
    }

    public static void testButtonAndTitlePositions() {
        CyderFrame testFrame = new CyderFrame(600, 400, CyderImages.defaultBackground);
        testFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        testFrame.setTitle("Testing Title");

        CyderButton setLeftTitle = new CyderButton("Left title");
        setLeftTitle.setBounds(100,100,140,40);
        setLeftTitle.addActionListener(e -> testFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT));
        testFrame.getContentPane().add(setLeftTitle);

        CyderButton setCenterTitle = new CyderButton("Center title");
        setCenterTitle.setBounds(100,160,140,40);
        setCenterTitle.addActionListener(e -> testFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER));
        testFrame.getContentPane().add(setCenterTitle);

        CyderButton setRightTitle = new CyderButton("Right title");
        setRightTitle.setBounds(100,220,140,40);
        setRightTitle.addActionListener(e -> testFrame.setTitlePosition(CyderFrame.TitlePosition.RIGHT));
        testFrame.getContentPane().add(setRightTitle);

        CyderButton setLeftButton = new CyderButton("Left button");
        setLeftButton.setBounds(300,100,150,40);
        setLeftButton.addActionListener(e -> testFrame.setButtonPosition(CyderFrame.ButtonPosition.LEFT));
        testFrame.getContentPane().add(setLeftButton);

        CyderButton setRightButton = new CyderButton("Right button");
        setRightButton.setBounds(300,160,150,40);
        setRightButton.addActionListener(e -> testFrame.setButtonPosition(CyderFrame.ButtonPosition.RIGHT));
        testFrame.getContentPane().add(setRightButton);

        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(null);
    }
}
