package cyder.test;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.enums.Direction;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;
import cyder.utilities.ImageUtil;

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

    public static void testNotifications() {
        CyderFrame testFrame = new CyderFrame(350,350,CyderImages.defaultBackground);
        testFrame.setTitle("Notification Test");

        int miliDelay = 3000;

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(100,50,150,40);
        testFrame.getContentPane().add(ctf);

        CyderButton topNotifiy = new CyderButton("Top");
        topNotifiy.setBounds(100,110,150,40);
        topNotifiy.addActionListener(e -> testFrame.notify(ctf.getText(), miliDelay, Direction.TOP));
        testFrame.getContentPane().add(topNotifiy);

        CyderButton rightNotify = new CyderButton("Right");
        rightNotify.setBounds(100,170,150,40);
        rightNotify.addActionListener(e -> testFrame.notify(ctf.getText(), miliDelay, Direction.RIGHT));
        testFrame.getContentPane().add(rightNotify);

        CyderButton bottomNotify = new CyderButton("Bottom");
        bottomNotify.setBounds(100,230,150,40);
        bottomNotify.addActionListener(e -> testFrame.notify(ctf.getText(), miliDelay, Direction.BOTTOM));
        testFrame.getContentPane().add(bottomNotify);

        CyderButton leftNotify = new CyderButton("Left");
        leftNotify.setBounds(100,290,150,40);
        leftNotify.addActionListener(e -> testFrame.notify(ctf.getText(), miliDelay, Direction.LEFT));
        testFrame.getContentPane().add(leftNotify);

        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(null);
    }

    public static void testAskew() {
        CyderFrame testFrame = new CyderFrame(350,300, CyderImages.defaultBackground);
        testFrame.setTitle("Askew Test");

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(100,100,150,40);
        testFrame.getContentPane().add(ctf);

        CyderButton cb = new CyderButton("Askew");
        cb.setBounds(100,200,150,40);
        testFrame.getContentPane().add(cb);
        cb.addActionListener(e -> testFrame.rotateBackground(Integer.parseInt(ctf.getText())));

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(null);
    }

    public static void testIconLabelSliding() {
        CyderFrame testFrame = new CyderFrame(600,600,
                new ImageIcon(ImageUtil.getImageGradient(600,1200,
                        CyderColors.selectionColor, CyderColors.intellijPink, CyderColors.regularBlue)));
        testFrame.setTitle("Sliding test");
        testFrame.initializeResizing();
        testFrame.setResizable(true);

        CyderButton slideUp = new CyderButton("UP");
        slideUp.setBounds(225,150,150,40);
        slideUp.addActionListener(e -> new Thread(() -> {
            //this up action is proving we can slide up a w, h*2 image to give an animation effect
            testFrame.getContentPane().setSize(testFrame.getContentPane().getWidth(),
                    testFrame.getContentPane().getHeight() * 2);
            try {
                int x = testFrame.getContentPane().getX();
                for (int i = 0 ; i > -testFrame.getHeight() ; i--) {
                    testFrame.getContentPane().setLocation(x,i);
                    Thread.sleep(1);
                }
                testFrame.getContentPane().setLocation(0,0);
                testFrame.refreshBackground();
                testFrame.getContentPane().revalidate();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }).start());
        testFrame.getContentPane().add(slideUp);

        CyderButton slideLeft = new CyderButton("LEFT");
        slideLeft.setBounds(225,200,150,40);
        slideLeft.addActionListener(e -> new Thread(() -> {
            try {
                int y = testFrame.getContentPane().getY();
                for (int i = 0 ; i > -testFrame.getWidth() ; i--) {
                    testFrame.getContentPane().setLocation(i,y);
                    Thread.sleep(1);
                }
                testFrame.getContentPane().setLocation(0,0);
                testFrame.refreshBackground();
                testFrame.getContentPane().revalidate();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }).start());
        testFrame.getContentPane().add(slideLeft);

        CyderButton slideDown = new CyderButton("DOWN");
        slideDown.setBounds(225,250,150,40);
        slideDown.addActionListener(e -> new Thread(() -> {
            try {
                int x = testFrame.getContentPane().getX();
                for (int i = 0 ; i < testFrame.getHeight() ; i++) {
                    testFrame.getContentPane().setLocation(x,i);
                    Thread.sleep(1);
                }
                testFrame.getContentPane().setLocation(0,0);
                testFrame.refreshBackground();
                testFrame.getContentPane().revalidate();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }).start());
        testFrame.getContentPane().add(slideDown);

        CyderButton slideRight = new CyderButton("RIGHT");
        slideRight.setBounds(225,300,150,40);
        slideRight.addActionListener(e -> new Thread(() -> {
            try {
                int y = testFrame.getContentPane().getY();
                for (int i = 0 ; i < testFrame.getWidth() ; i++) {
                    testFrame.getContentPane().setLocation(i,y);
                    Thread.sleep(1);
                }
                testFrame.getContentPane().setLocation(0,0);
                testFrame.refreshBackground();
                testFrame.getContentPane().revalidate();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }).start());
        testFrame.getContentPane().add(slideRight);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(null);
    }
}
