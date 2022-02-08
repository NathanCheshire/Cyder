package test.java;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.AnimationDirection;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.enums.SliderShape;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.layouts.CyderFlowLayout;
import cyder.layouts.CyderGridLayout;
import cyder.structs.CyderQueue;
import cyder.structs.CyderStack;
import cyder.ui.*;
import cyder.ui.objects.NotificationBuilder;
import cyder.utilities.ImageUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Tests which must be performed manually and cannot be unit tested.
 */
public class ManualTests {
    /**
     * Runs the tests within the method.
     * This method is used purely for testing purposes.
     */
    public static void launchTests() {
        try {

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    //this was used on 7-1-21 to verify adding/removing buttons to/from drag labels
    public static void dragLabelButtonTest() {
        CyderFrame testFrame = new CyderFrame(600,600, CyderIcons.defaultBackground);
        testFrame.setTitle("Test Frame");
        testFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

        JButton pinButton = new JButton("");
        pinButton.setToolTipText("Random button");
        pinButton.setIcon(CyderIcons.changeSizeIcon);
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
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void buttonAndTitlePosTest() {
        CyderFrame testFrame = new CyderFrame(600, 400, CyderIcons.defaultBackground);
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
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void notificationTest() {
        CyderFrame testFrame = new CyderFrame(350,600, CyderIcons.defaultBackground);
        testFrame.setTitle("Notification Test");

        int miliDelay = 3000;

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(100,50,150,40);
        testFrame.getContentPane().add(ctf);

        CyderButton topNotifiy = new CyderButton("Top");
        topNotifiy.setBounds(100,110,150,40);
        topNotifiy.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(miliDelay);
            notificationBuilder.setArrowDir(Direction.TOP);
            notificationBuilder.setNotificationDirection(NotificationDirection.TOP);
            testFrame.notify(notificationBuilder);
        });
        testFrame.getContentPane().add(topNotifiy);

        CyderButton rightNotify = new CyderButton("Top Right");
        rightNotify.setBounds(100,170,150,40);
        rightNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(miliDelay);
            notificationBuilder.setArrowDir(Direction.RIGHT);
            notificationBuilder.setNotificationDirection(NotificationDirection.TOP_RIGHT);
            testFrame.notify(notificationBuilder);
        });
        testFrame.getContentPane().add(rightNotify);

        CyderButton bottomNotify = new CyderButton("Bottom");
        bottomNotify.setBounds(100,230,150,40);
        bottomNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(miliDelay);
            notificationBuilder.setArrowDir(Direction.BOTTOM);
            notificationBuilder.setNotificationDirection(NotificationDirection.BOTTOM);
            testFrame.notify(notificationBuilder);
        });
        testFrame.getContentPane().add(bottomNotify);

        CyderButton leftNotify = new CyderButton("Top Left");
        leftNotify.setBounds(100,290,150,40);
        leftNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(miliDelay);
            notificationBuilder.setArrowDir(Direction.LEFT);
            notificationBuilder.setNotificationDirection(NotificationDirection.TOP_LEFT);
            testFrame.notify(notificationBuilder);
        });
        testFrame.getContentPane().add(leftNotify);

        CyderButton centerLeftNotify = new CyderButton("Center Left");
        centerLeftNotify.setBounds(100,350,150,40);
        centerLeftNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(miliDelay);
            notificationBuilder.setArrowDir(Direction.LEFT);
            notificationBuilder.setNotificationDirection(NotificationDirection.LEFT);
            testFrame.notify(notificationBuilder);
        });
        testFrame.getContentPane().add(centerLeftNotify);

        CyderButton centerRightNotify = new CyderButton("Center Right");
        centerRightNotify.setBounds(100,410,150,40);
        centerRightNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(miliDelay);
            notificationBuilder.setArrowDir(Direction.RIGHT);
            notificationBuilder.setNotificationDirection(NotificationDirection.RIGHT);
            testFrame.notify(notificationBuilder);
        });
        testFrame.getContentPane().add(centerRightNotify);

        CyderButton bottomLeftNotify = new CyderButton("Bottom Left");
        bottomLeftNotify.setBounds(100,470,150,40);
        bottomLeftNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(miliDelay);
            notificationBuilder.setArrowDir(Direction.LEFT);
            notificationBuilder.setNotificationDirection(NotificationDirection.BOTTOM_LEFT);
            testFrame.notify(notificationBuilder);
        });
        testFrame.getContentPane().add(bottomLeftNotify);

        CyderButton bottomRightNotify = new CyderButton("Bottom Right");
        bottomRightNotify.setBounds(100,530,170,40);
        bottomRightNotify.addActionListener(e -> {
            NotificationBuilder notificationBuilder = new NotificationBuilder(ctf.getText());
            notificationBuilder.setViewDuration(miliDelay);
            notificationBuilder.setArrowDir(Direction.RIGHT);
            notificationBuilder.setNotificationDirection(NotificationDirection.BOTTOM_RIGHT);
            testFrame.notify(notificationBuilder);
        });
        testFrame.getContentPane().add(bottomRightNotify);

        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void askewTest() {
        CyderFrame testFrame = new CyderFrame(350,300, CyderIcons.defaultBackground);
        testFrame.setTitle("Askew Test");

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(100,100,150,40);
        testFrame.getContentPane().add(ctf);

        CyderButton cb = new CyderButton("Askew");
        cb.setBounds(100,200,150,40);
        testFrame.getContentPane().add(cb);
        cb.addActionListener(e -> testFrame.rotateBackground(Integer.parseInt(ctf.getText())));

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void iconLabelSlidingTest() {
        ImageIcon theImage =  new ImageIcon(ImageUtil.getImageGradient(600,1200,
                CyderColors.regularPink, CyderColors.regularBlue, CyderColors.regularBlue));

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
                ExceptionHandler.handle(interruptedException);
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
                ExceptionHandler.handle(interruptedException);
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
                ExceptionHandler.handle(interruptedException);
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
                ExceptionHandler.handle(interruptedException);
            }
        }).start());
        testFrame.getContentPane().add(slideRight);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void checkboxTest() {
        CyderFrame testFrame = new CyderFrame(400,400, CyderIcons.defaultBackground);
        testFrame.setTitle("Checkbox Test");

        CyderCheckbox cb = new CyderCheckbox();
        cb.setBounds(175,150,50, 50);
        cb.setRoundedCorners(true);
        testFrame.getContentPane().add(cb);

        CyderCheckbox cb1 = new CyderCheckbox();
        cb1.setBounds(175,225,50, 50);
        cb1.setRoundedCorners(false);
        testFrame.getContentPane().add(cb1);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void informTest() {
        CyderFrame testFrame = new CyderFrame(300,200, CyderIcons.defaultBackground);
        testFrame.setTitle("Inform test");

        CyderTextField textField = new CyderTextField(0);
        textField.setBounds(50,50,200,40);
        testFrame.getContentPane().add(textField);

        CyderButton informButton = new CyderButton("Inform");
        informButton.setBounds(50,120, 200, 40);
        testFrame.getContentPane().add(informButton);
        informButton.addActionListener(e -> testFrame.inform(textField.getText(),"Inform"));

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void progressBarTest() {
        CyderFrame cf = new CyderFrame(400,100);
        cf.setTitle("ProgressBar Test");

        JProgressBar jpb = new JProgressBar(0,500);
        jpb.setBounds(40,40,320,20);
        jpb.setOrientation(JProgressBar.HORIZONTAL);
        CyderProgressUI ui = new CyderProgressUI();
        ui.setAnimationDirection(AnimationDirection.LEFT_TO_RIGHT);
        ui.setColors(new Color[]{CyderColors.regularBlue, CyderColors.regularPink});
        ui.setShape(CyderProgressUI.Shape.SQUARE);
        jpb.setUI(ui);
        jpb.setValue(50);
        cf.getContentPane().add(jpb);
        cf.setVisible(true);
        cf.setLocationRelativeTo(CyderCommon.getDominantFrame());

        new Thread( () -> {
            for (int i = 0 ; i <= jpb.getMaximum() / 2; i++) {
                jpb.setValue(i);
                try {
                    Thread.sleep(2000 / jpb.getMaximum());
                } catch (InterruptedException e) {
                    ExceptionHandler.handle(e);
                }
            }

            for (int i = jpb.getMaximum() / 2 ; i <= jpb.getMaximum(); i++) {
                jpb.setValue(i);
                try {
                    Thread.sleep(500 / jpb.getMaximum());
                } catch (InterruptedException e) {
                    ExceptionHandler.handle(e);
                }
            }
        }, "ProgressBar Animator").start();
    }

    public static void cyderSliderTest() {
        CyderFrame testFrame = new CyderFrame(400,400);
        testFrame.setTitle("Cyder Slider Test");

        JSlider audioVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        CyderSliderUI UI = new CyderSliderUI(audioVolumeSlider);
        UI.setThumbDiameter(25);
        UI.setSliderShape(SliderShape.CIRCLE);
        UI.setFillColor(CyderColors.regularPink);
        UI.setOutlineColor(CyderColors.regularPink);
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
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
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
        cf.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void stackTest() {
        CyderStack<String> stringStack = new CyderStack<>();

        CyderFrame cf = new CyderFrame( 280, 350);
        cf.setTitle("Stack Test");
        cf.setTitlePosition(CyderFrame.TitlePosition.RIGHT);

        CyderButton peekButton = new CyderButton("Peek");
        peekButton.setBounds(40,40,200,40);
        peekButton.addActionListener(e -> cf.notify(new NotificationBuilder(stringStack.peek().toString())));
        cf.getContentPane().add(peekButton);

        //pop data notify
        CyderButton popButton = new CyderButton("Pop");
        popButton.setBounds(40,100,200,40);
        popButton.addActionListener(e -> cf.notify(new NotificationBuilder(stringStack.pop().toString())));
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
        isEmptyButton.addActionListener(e -> cf.notify(new NotificationBuilder(String.valueOf(stringStack.isEmpty()))));
        cf.getContentPane().add(isEmptyButton);

        //stack size
        CyderButton printButton = new CyderButton("Print");
        printButton.setBounds(40,280,200,40);
        printButton.addActionListener(e -> cf.notify(new NotificationBuilder(String.valueOf(stringStack.toString()))));
        cf.getContentPane().add(printButton);

        cf.setVisible(true);
        cf.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void switchTest() {
        CyderFrame testFrame = new CyderFrame(500,500);
        testFrame.setTitle("CyderSwitch test");

        CyderSwitch cs = new CyderSwitch(300,100);
        cs.setBounds(100,100,300,100);
        cs.setState(CyderSwitch.State.OFF);
        testFrame.getContentPane().add(cs);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
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
        dequeueButton.addActionListener(e -> cf.notify(new NotificationBuilder(String.valueOf(queue.dequeue()))));
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
        removeLast.addActionListener(e -> cf.notify(new NotificationBuilder(String.valueOf(queue.dequeue()))));
        removeLast.setBounds(40,220, 220, 40);
        cf.getContentPane().add(removeLast);

        //forward traversal button
        CyderButton forwardTraversalButton = new CyderButton("Forward Traversal");
        forwardTraversalButton.addActionListener(e -> cf.notify(new NotificationBuilder(String.valueOf(queue.forwardTraversal()))));
        forwardTraversalButton.setBounds(40,280, 220, 40);
        cf.getContentPane().add(forwardTraversalButton);

        //reverse traversal button
        CyderButton reverseTraversalButton = new CyderButton("Reverse Traversal");
        reverseTraversalButton.addActionListener(e -> cf.notify(new NotificationBuilder(String.valueOf(queue.reverseTraversal()))));
        reverseTraversalButton.setBounds(40,340, 220, 40);
        cf.getContentPane().add(reverseTraversalButton);

        CyderButton sizeButton = new CyderButton("Size");
        sizeButton.addActionListener(e -> cf.notify(new NotificationBuilder((String.valueOf(queue.size())))));
        sizeButton.setBounds(40,400,220,40);
        cf.getContentPane().add(sizeButton);

        CyderTextField containsField = new CyderTextField(0);
        containsField.setToolTipText("Contains");
        containsField.addActionListener(e -> {
            cf.notify(new NotificationBuilder(String.valueOf(queue.contains(containsField.getText()))));
            containsField.setText("");
        });
        containsField.setBounds(40,460, 220, 40);
        cf.getContentPane().add(containsField);

        cf.setVisible(true);
        cf.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void comboBoxTest() {
        CyderFrame testFrame = new CyderFrame(400,400);
        testFrame.setTitle("ComboBox Test");

        CyderComboBox ccb = new CyderComboBox(200,40, new String[]{"one","two","three"});
        ccb.setBounds(40,40,200,40);
        testFrame.getContentPane().add(ccb);

        CyderButton printbutton = new CyderButton("Print choice");
        printbutton.setBounds(40,150,200,40);
        testFrame.getContentPane().add(printbutton);
        printbutton.addActionListener(e -> testFrame.notify(new NotificationBuilder(ccb.getValue())));

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }

    public static void rippleLabelTest() {
        CyderFrame rippleTestFrame = new CyderFrame(600,600);
        rippleTestFrame.setTitle("Ripple Test");

        CyderLabel ripplingLabel = new CyderLabel("<html>" + CyderStrings.QUICK_BROWN_FOX + "<br/>" +
                CyderStrings.QUICK_BROWN_FOX + "<br/><br/>" + CyderStrings.QUICK_BROWN_FOX + "<br/>" +
                CyderStrings.QUICK_BROWN_FOX + "<br/>" + CyderStrings.QUICK_BROWN_FOX + "<br/>" +
                CyderStrings.QUICK_BROWN_FOX + "<br/><br/>Love,<br/>Nathan Cheshire" + "</html>");
        ripplingLabel.setFont(CyderFonts.segoe20);

        //fill content area with label
        ripplingLabel.setBounds(40,40,
                rippleTestFrame.getWidth() - 40 * 2, rippleTestFrame.getHeight() - 40 * 2);
        rippleTestFrame.getContentPane().add(ripplingLabel);

        //fast timeout and relatively high char count
        ripplingLabel.setRippleMsTimeout(10);
        ripplingLabel.setRippleChars(15);

        //enable rippling
        ripplingLabel.setRippling(true);

        rippleTestFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
        rippleTestFrame.setVisible(true);
    }

    public static void checkboxGroupTest() {
        CyderFrame testFrame = new CyderFrame(400,110);
        testFrame.setTitle("Checkbox group test");

        CyderCheckboxGroup cbg = new CyderCheckboxGroup();

        int startX = 50;

        for (int i = 0 ; i < 5 ; i++) {
            CyderCheckbox cb = new CyderCheckbox();
            cb.setBounds(startX + (60) * i,40,50,50);
            testFrame.getContentPane().add(cb);

            if (i != 4)
                cbg.addCheckbox(cb);
        }

        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
        testFrame.setVisible(true);
    }

    public static void cyderGridTest() {
        CyderFrame testFrame = new CyderFrame();
        testFrame.setTitle("Zoomable Grid Test");

        CyderGrid cg = new CyderGrid(37,620);
        cg.setBounds(50,50,620,620);
        cg.setBackgroundColor(CyderColors.vanila);
        testFrame.getContentPane().add(cg);
        cg.setResizable(true);
        cg.setDrawGridLines(false);
        cg.setDrawExtendedBorder(true);
        cg.addNode(new CyderGrid.GridNode(CyderColors.regularPink, 20,20));
        cg.addNode(new CyderGrid.GridNode(CyderColors.regularBlue, 21,21));
        cg.addNode(new CyderGrid.GridNode(CyderColors.regularPink, 20,21));
        cg.addNode(new CyderGrid.GridNode(CyderColors.regularBlue, 21,20));

        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
        testFrame.setVisible(true);
    }

    public static void cyderGridLayoutTest() {
        //regular frame calls
        CyderFrame gridTestFrame = new CyderFrame(800,800);
        gridTestFrame.setTitle("Grid Layout Test");

        //init the main panel layout
        CyderGridLayout layout = new CyderGridLayout(2,2);

        //add components to the layout at specified position
        CyderButton testButton = new CyderButton("This");
        testButton.setSize(100,100);
        testButton.addActionListener(e -> gridTestFrame.notify(new NotificationBuilder("Notified button clicked")));
        layout.addComponent(testButton, 0, 0, CyderGridLayout.Position.MIDDLE_RIGHT);

        CyderLabel testLabel2 = new CyderLabel("A");
        testLabel2.setSize(50,50);
        layout.addComponent(testLabel2, 0, 1);

        CyderLabel testLabel3 = new CyderLabel("IS");
        testLabel3.setSize(50,50);
        layout.addComponent(testLabel3, 1, 0);

        CyderLabel testLabel4 = new CyderLabel("Test");
        testLabel4.setSize(50,50);
        CyderButton testButton1 = new CyderButton("Click");
        testButton1.setSize(150,40);

        //sub grid
        CyderGridLayout cyderGridLayout2 = new CyderGridLayout(2,1);
        cyderGridLayout2.addComponent(testLabel4,0,0);
        cyderGridLayout2.addComponent(testButton1,1,0);

        //make sub panel and set layout as sub grid
        CyderPanel subPanel = new CyderPanel(cyderGridLayout2);
        layout.addComponent(subPanel, 1, 1);

        //create master panel with the layout we have added components to
        CyderPanel panel = new CyderPanel(layout);
        //set the frame's content panel
        gridTestFrame.setContentPanel(panel);

        //resizing on
        gridTestFrame.initializeResizing();
        gridTestFrame.setResizable(true);
        gridTestFrame.setMaximumSize(new Dimension(1200,1200));
        gridTestFrame.setBackgroundResizing(true);

        //regular final frame calls
        gridTestFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
        gridTestFrame.setVisible(true);
    }

    public static void flowLayoutTest() {
        CyderFrame testFrame = new CyderFrame(600,600);
        testFrame.setTitle("Flow Layout Test");

        //make layout
        CyderFlowLayout layout = new CyderFlowLayout(CyderFlowLayout.Alignment.CENTER,25,15);

        //add 10 buttons to layout
        for (int i = 1 ; i < 11 ; i++) {
            CyderButton cb = new CyderButton("Test Button " + i);
            cb.setSize(200, 50);
            int finalI = i;
            cb.addActionListener(e -> testFrame.notify(new NotificationBuilder(finalI + "button: " + cb)));
            layout.addComponent(cb);
        }

        //make panel and set as frame's content panel
        CyderPanel panel = new CyderPanel(layout);
        testFrame.setContentPanel(panel);

        //resizing on
        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.setMaximumSize(new Dimension(2000,2000));
        testFrame.setMinimumSize(new Dimension(300,300));
        testFrame.setBackgroundResizing(true);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }
}
