package cyder.testing;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.enums.AnimationDirection;
import cyder.enums.NotificationDirection;
import cyder.enums.SliderShape;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.structobjects.CyderQueue;
import cyder.structobjects.CyderStack;
import cyder.ui.*;
import cyder.utilities.ImageUtil;

import javax.swing.*;
import java.awt.*;

public class ManualTestingWidgets {
    //this was used on 7-1-21 to verify adding/removing buttons to/from drag labels
    public static void AddingAndRemovingDragLabelButtonsTest() {
        CyderFrame testFrame = new CyderFrame(600,600, CyderImages.defaultBackground);
        testFrame.setTitle("Test Frame");
        testFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JButton pinButton = new JButton("");
        pinButton.setToolTipText("Random button");
        pinButton.setIcon(new ImageIcon("sys/pictures/icons/changesize1.png"));
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
        cb1.addActionListener(e -> testFrame.getTopDragLabel().removeButton(testFrame.getTopDragLabel().getButtonsList().size() - 1));
        testFrame.getContentPane().add(cb1);

        CyderButton addPinFirst = new CyderButton("Add Random Butter first");
        addPinFirst.setBounds(100, 250, 150, 40);
        addPinFirst.addActionListener(e -> testFrame.getTopDragLabel().addButton(pinButton, 0));
        testFrame.getContentPane().add(addPinFirst);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void ButtonAndTitlePositionsTest() {
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
        testFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void NotificationsTest() {
        CyderFrame testFrame = new CyderFrame(350,350,CyderImages.defaultBackground);
        testFrame.setTitle("Notification Test");

        int miliDelay = 3000;

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(100,50,150,40);
        testFrame.getContentPane().add(ctf);

        CyderButton topNotifiy = new CyderButton("Top");
        topNotifiy.setBounds(100,110,150,40);
        topNotifiy.addActionListener(e -> testFrame.notify(ctf.getText(), miliDelay, NotificationDirection.TOP.TOP));
        testFrame.getContentPane().add(topNotifiy);

        CyderButton rightNotify = new CyderButton("Right");
        rightNotify.setBounds(100,170,150,40);
        rightNotify.addActionListener(e -> testFrame.notify(ctf.getText(), miliDelay, NotificationDirection.TOP_RIGHT));
        testFrame.getContentPane().add(rightNotify);

        CyderButton bottomNotify = new CyderButton("Bottom");
        bottomNotify.setBounds(100,230,150,40);
        bottomNotify.addActionListener(e -> testFrame.notify(ctf.getText(), miliDelay, NotificationDirection.BOTTOM));
        testFrame.getContentPane().add(bottomNotify);

        CyderButton leftNotify = new CyderButton("Left");
        leftNotify.setBounds(100,290,150,40);
        leftNotify.addActionListener(e -> testFrame.notify(ctf.getText(), miliDelay, NotificationDirection.TOP_LEFT));
        testFrame.getContentPane().add(leftNotify);

        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void AskewTest() {
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
        testFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void IconLabelSlidingTest() {
        ImageIcon theImage =  new ImageIcon(ImageUtil.getImageGradient(600,1200,
                CyderColors.intellijPink, CyderColors.regularBlue, CyderColors.regularBlue));

        CyderFrame testFrame = new CyderFrame(600,600, theImage);
        testFrame.setTitle("Sliding test");
        testFrame.initializeResizing();
        testFrame.setResizable(true);

        CyderButton slideUp = new CyderButton("UP");
        slideUp.setBounds(225,150,150,40);
        slideUp.addActionListener(e -> new Thread(() -> {
            testFrame.getContentPane().setSize(600, 1200);
            ((JLabel) testFrame.getContentPane()).setIcon(theImage);

            try {
                int x = testFrame.getContentPane().getX();
                for (int i = testFrame.getContentPane().getY() ; i > -testFrame.getHeight() ; i--) {
                    testFrame.getContentPane().setLocation(x,i);
                    Thread.sleep(1);
                }
                testFrame.getContentPane().setLocation(0,0);
                testFrame.refreshBackground();
                testFrame.getContentPane().revalidate();
            } catch (InterruptedException interruptedException) {
                ErrorHandler.handle(interruptedException);
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
                ErrorHandler.handle(interruptedException);
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
                ErrorHandler.handle(interruptedException);
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
                ErrorHandler.handle(interruptedException);
            }
        }).start());
        testFrame.getContentPane().add(slideRight);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void CheckboxTest() {
        CyderFrame testFrame = new CyderFrame(400,400, CyderImages.defaultBackground);
        testFrame.setTitle("Checkbox Test");

        CyderCheckBox cb = new CyderCheckBox();
        cb.setBounds(175,150,50, 50);
        cb.setRoundedCorners(true);
        testFrame.getContentPane().add(cb);

        CyderCheckBox cb1 = new CyderCheckBox();
        cb1.setBounds(175,225,50, 50);
        cb1.setRoundedCorners(false);
        testFrame.getContentPane().add(cb1);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void InformTest() {
        CyderFrame testFrame = new CyderFrame(300,200, CyderImages.defaultBackground);
        testFrame.setTitle("Inform test");

        CyderTextField textField = new CyderTextField(0);
        textField.setBounds(50,50,200,40);
        testFrame.getContentPane().add(textField);

        CyderButton informButton = new CyderButton("Inform");
        informButton.setBounds(50,120, 200, 40);
        testFrame.getContentPane().add(informButton);
        informButton.addActionListener(e -> testFrame.inform(textField.getText(),"Inform"));

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void ProgressBarTest() {
        CyderFrame cf = new CyderFrame(400,100);
        cf.setTitle("ProgressBar Test");

        JProgressBar jpb = new JProgressBar(0,500);
        jpb.setBounds(40,40,320,20);
        jpb.setOrientation(JProgressBar.HORIZONTAL);
        CyderProgressUI ui = new CyderProgressUI();
        ui.setAnimationDirection(AnimationDirection.LEFT_TO_RIGHT);
        ui.setColors(new Color[]{CyderColors.regularBlue, CyderColors.intellijPink});
        ui.setShape(CyderProgressUI.Shape.SQUARE);
        jpb.setUI(ui);
        jpb.setValue(50);
        cf.getContentPane().add(jpb);
        cf.setVisible(true);
        cf.setLocationRelativeTo(GenesisShare.getDominantFrame());

        new Thread( () -> {
            for (int i = 0 ; i <= jpb.getMaximum() / 2; i++) {
                jpb.setValue(i);
                try {
                    Thread.sleep(2000 / jpb.getMaximum());
                } catch (InterruptedException e) {
                    ErrorHandler.handle(e);
                }
            }

            for (int i = jpb.getMaximum() / 2 ; i <= jpb.getMaximum(); i++) {
                jpb.setValue(i);
                try {
                    Thread.sleep(500 / jpb.getMaximum());
                } catch (InterruptedException e) {
                    ErrorHandler.handle(e);
                }
            }
        }, "ProgressBar Animator").start();
    }

    public static void CyderSliderTest() {
        CyderFrame testFrame = new CyderFrame(400,400);
        testFrame.setTitle("Cyder Slider Test");

        JSlider audioVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        CyderSliderUI UI = new CyderSliderUI(audioVolumeSlider);
        UI.setThumbDiameter(25);
        UI.setSliderShape(SliderShape.CIRCLE);
        UI.setFillColor(CyderColors.intellijPink);
        UI.setOutlineColor(CyderColors.intellijPink);
        UI.setNewValColor(CyderColors.navy);
        UI.setOldValColor(CyderColors.regularBlue);
        UI.setTrackStroke(new BasicStroke(4.0f));
        audioVolumeSlider.setUI(UI);
        audioVolumeSlider.setBounds(50, 150, 300, 40);
        audioVolumeSlider.setMinimum(0);
        audioVolumeSlider.setMaximum(100);
        audioVolumeSlider.setPaintTicks(false);
        audioVolumeSlider.setPaintLabels(false);
        audioVolumeSlider.setVisible(true);
        audioVolumeSlider.setValue(50);
        audioVolumeSlider.setOpaque(false);
        audioVolumeSlider.setToolTipText("Volume");
        audioVolumeSlider.setFocusable(false);
        audioVolumeSlider.repaint();
        testFrame.getContentPane().add(audioVolumeSlider);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void frameTitleLengthTest() {
        CyderFrame cf = new CyderFrame( 600, 200);
        cf.setTitle("Title Length Test");
        cf.setTitlePosition(CyderFrame.TitlePosition.LEFT);

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(40,40, 600 - 80, 40);
        cf.getContentPane().add(ctf);
        ctf.addActionListener(e -> cf.setTitle(ctf.getText().trim()));

        CyderButton cb = new CyderButton("Set Title");
        cb.setBounds(40,100, 600 - 80, 40);
        cf.getContentPane().add(cb);
        cb.addActionListener(e -> cf.setTitle(ctf.getText().trim()));

        cf.setVisible(true);
        cf.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void stackTest() {
        CyderStack<String> stringStack = new CyderStack<>();

        CyderFrame cf = new CyderFrame( 280, 350);
        cf.setTitle("Stack Test");
        cf.setTitlePosition(CyderFrame.TitlePosition.RIGHT);

        CyderButton peekButton = new CyderButton("Peek");
        peekButton.setBounds(40,40,200,40);
        peekButton.addActionListener(e -> cf.notify(stringStack.peek().toString()));
        cf.getContentPane().add(peekButton);

        //pop data notify
        CyderButton popButton = new CyderButton("Pop");
        popButton.setBounds(40,100,200,40);
        popButton.addActionListener(e -> cf.notify(stringStack.pop().toString()));
        cf.getContentPane().add(popButton);

        //push data
        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(40,160,200,40);
        ctf.addActionListener(e -> {
            stringStack.push(ctf.getText());
            ctf.setText("");
        });
        cf.getContentPane().add(ctf);

        //isEmpty notify
        CyderButton isEmptyButton = new CyderButton("isEmpty");
        isEmptyButton.setBounds(40,220,200,40);
        isEmptyButton.addActionListener(e -> cf.notify(String.valueOf(stringStack.isEmpty())));
        cf.getContentPane().add(isEmptyButton);

        //stack size
        CyderButton printButton = new CyderButton("Print");
        printButton.setBounds(40,280,200,40);
        printButton.addActionListener(e -> cf.notify(String.valueOf(stringStack.toString())));
        cf.getContentPane().add(printButton);

        cf.setVisible(true);
        cf.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    public static void queueTest() {
        CyderQueue<String> queue = new CyderQueue<>();

        CyderFrame cf = new CyderFrame(300,520);
        cf.setTitle("Queue test");

        //enqueue
        CyderTextField enqueueField = new CyderTextField(0);
        enqueueField.setToolTipText("Enqueue");
        enqueueField.addActionListener(e -> {
            queue.enqueue(enqueueField.getText());
            enqueueField.setText("");
        });
        enqueueField.setBounds(40,40, 220, 40);
        cf.getContentPane().add(enqueueField);

        //dequeue
        CyderButton dequeueButton = new CyderButton("Dequeue");
        dequeueButton.addActionListener(e -> cf.notify(String.valueOf(queue.dequeue())));
        dequeueButton.setBounds(40,100, 220, 40);
        cf.getContentPane().add(dequeueButton);

        //add first field
        CyderTextField addFirstField = new CyderTextField(0);
        addFirstField.setToolTipText("Add first");
        addFirstField.addActionListener(e -> {
            queue.addFirst(addFirstField.getText());
            addFirstField.setText("");
        });
        addFirstField.setBounds(40,160, 220, 40);
        cf.getContentPane().add(addFirstField);

        //remove last button
        CyderButton removeLast = new CyderButton("Remove Last");
        removeLast.addActionListener(e -> cf.notify(String.valueOf(queue.dequeue())));
        removeLast.setBounds(40,220, 220, 40);
        cf.getContentPane().add(removeLast);

        //forward traversal button
        CyderButton forwardTraversalButton = new CyderButton("Forward Traversal");
        forwardTraversalButton.addActionListener(e -> cf.notify(String.valueOf(queue.forwardTraversal())));
        forwardTraversalButton.setBounds(40,280, 220, 40);
        cf.getContentPane().add(forwardTraversalButton);

        //reverse traversal button
        CyderButton reverseTraversalButton = new CyderButton("Reverse Traversal");
        reverseTraversalButton.addActionListener(e -> cf.notify(String.valueOf(queue.reverseTraversal())));
        reverseTraversalButton.setBounds(40,340, 220, 40);
        cf.getContentPane().add(reverseTraversalButton);

        CyderButton sizeButton = new CyderButton("Size");
        sizeButton.addActionListener(e -> cf.notify(String.valueOf(queue.size())));
        sizeButton.setBounds(40,400,220,40);
        cf.getContentPane().add(sizeButton);

        CyderTextField containsField = new CyderTextField(0);
        containsField.setToolTipText("Contains");
        containsField.addActionListener(e -> {
            cf.notify(String.valueOf(queue.contains(containsField.getText())));
            containsField.setText("");
        });
        containsField.setBounds(40,460, 220, 40);
        cf.getContentPane().add(containsField);

        cf.setVisible(true);
        cf.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }
}
