package cyder.test;

import cyder.annotations.ManualTest;
import cyder.annotations.SuppressCyderInspections;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.layouts.*;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.CyderGrid;
import cyder.ui.CyderPanel;
import cyder.ui.button.CyderButton;
import cyder.ui.button.CyderModernButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.button.MenuButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.progress.CyderProgressUI;
import cyder.ui.selection.CyderCheckbox;
import cyder.ui.selection.CyderCheckboxGroup;
import cyder.ui.selection.CyderComboBox;
import cyder.ui.selection.CyderSwitch;
import cyder.ui.slider.CyderSliderUi;
import cyder.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Manual widgets used to test certain aspects of Cyder.
 */
public final class ManualTests {
    /**
     * Restricts default instantiation.
     */
    private ManualTests() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Runs the tests within the method.
     * This method is used purely for testing purposes.
     */
    @ManualTest("test")
    @SuppressCyderInspections(CyderInspection.TestInspection) /* not ending in test */
    @SuppressWarnings({"EmptyTryBlock", "RedundantSuppression"}) /* for when try is empty and not empty */
    public static void launchTests() {
        CyderThreadRunner.submit(() -> {
            try {

            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Manual Tests Thread");
    }

    /**
     * Tests for the switcher.
     */
    @ManualTest("switcher test")
    public static void cyderSwitcherTest() {
        CyderFrame testFrame = new CyderFrame(280, 120);
        testFrame.setTitle("Switcher test");

        ArrayList<CyderComboBox.ComboItem> states = new ArrayList<>();
        states.add(new CyderComboBox.ComboItem("Uno", "uno long"));
        states.add(new CyderComboBox.ComboItem("Dos", "dos long"));
        states.add(new CyderComboBox.ComboItem("Tres", "tres long"));
        states.add(new CyderComboBox.ComboItem("Cuatro", "cuatro long"));

        CyderComboBox.ComboItem startingState = states.get(0);

        CyderComboBox switcher = new CyderComboBox(200, 40, states, startingState);
        switcher.setBounds(40, 40, 200, 40);
        testFrame.getContentPane().add(switcher);
        switcher.addOnChangeListener((param) -> {
            testFrame.notify(switcher.getNextState().mappedValue());

            return param;
        });

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the CyderGrid.
     */
    @ManualTest("grid test")
    public static void cyderGridTest() {
        CyderFrame cf = new CyderFrame(1000, 1000);
        cf.setTitle("Cyder Grid");

        CyderGrid cg = new CyderGrid(200, 800);
        cg.setBounds(100, 100, 800, 800);
        cf.getContentPane().add(cg);
        cg.setResizable(true);
        cg.setDrawGridLines(false);
        cg.installClickListener();
        cg.installDragListener();

        cf.finalizeAndShow();
    }

    /**
     * Tests for drag label buttons.
     */
    @ManualTest("drag label button test")
    public static void dragLabelButtonTest() {
        CyderFrame testFrame = new CyderFrame(600, 600, CyderIcons.defaultBackground);
        testFrame.setTitle("Drag label test");

        CyderButton addLeftButton = new CyderButton("Add left");
        addLeftButton.addActionListener(e -> testFrame.getTopDragLabel().addLeftButton(new MenuButton(), 0));
        addLeftButton.setSize(200, 40);

        CyderButton removeLeftButton = new CyderButton("Remove left");
        removeLeftButton.addActionListener(e -> testFrame.getTopDragLabel().removeLeftButton(0));
        removeLeftButton.setSize(200, 40);

        CyderButton addRightButton = new CyderButton("Add right");
        addRightButton.addActionListener(e -> testFrame.getTopDragLabel().addRightButton(new MenuButton(), 0));
        addRightButton.setSize(200, 40);

        CyderButton removeRightButton = new CyderButton("Remove right");
        removeRightButton.addActionListener(e -> testFrame.getTopDragLabel().removeRightButton(0));
        removeRightButton.setSize(200, 40);

        CyderButton leftTitle = new CyderButton("Left title");
        leftTitle.addActionListener(e -> testFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT));
        leftTitle.setSize(200, 40);

        CyderButton centerTitle = new CyderButton("Center title");
        centerTitle.addActionListener(e -> testFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER));
        centerTitle.setSize(200, 40);

        CyderButton rightTitle = new CyderButton("Right title");
        rightTitle.addActionListener(e -> testFrame.setTitlePosition(CyderFrame.TitlePosition.RIGHT));
        rightTitle.setSize(200, 40);

        CyderTextField titleField = new CyderTextField();
        titleField.setSize(200, 40);
        titleField.addActionListener(e -> {
            if (!titleField.getText().isEmpty()) {
                testFrame.setTitle(titleField.getText().trim());
            }
        });

        testFrame.initializeResizing();
        testFrame.setMaximumSize(1200, 1200);
        testFrame.setMinimumSize(200, 200);

        CyderGridLayout cyderGridLayout = new CyderGridLayout(1, 8);

        cyderGridLayout.addComponent(addLeftButton);
        cyderGridLayout.addComponent(removeLeftButton);
        cyderGridLayout.addComponent(addRightButton);
        cyderGridLayout.addComponent(removeRightButton);

        cyderGridLayout.addComponent(leftTitle);
        cyderGridLayout.addComponent(centerTitle);
        cyderGridLayout.addComponent(rightTitle);

        cyderGridLayout.addComponent(titleField);

        testFrame.setCyderLayout(cyderGridLayout);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for notifications.
     */
    @ManualTest("notification test")
    public static void notificationTest() {
        CyderFrame testFrame = new CyderFrame(600, 600, CyderIcons.defaultBackground);
        testFrame.setTitle("Notification Test");

        int milliDelay = 3000;

        CyderGridLayout layout = new CyderGridLayout(3, 3);

        CyderTextField ctf = new CyderTextField(0);
        ctf.setSize(150, 40);

        CyderButton topNotify = new CyderButton("Top");
        topNotify.setSize(150, 40);
        topNotify.addActionListener(e -> testFrame.notify(new CyderFrame.NotificationBuilder(ctf.getText())
                .setViewDuration(milliDelay)
                .setArrowDir(Direction.TOP)
                .setNotificationDirection(NotificationDirection.TOP)));

        CyderButton rightNotify = new CyderButton("Top Right");
        rightNotify.setSize(150, 40);
        rightNotify.addActionListener(e -> testFrame.notify(new CyderFrame.NotificationBuilder(ctf.getText())
                .setViewDuration(milliDelay)
                .setArrowDir(Direction.RIGHT)
                .setNotificationDirection(NotificationDirection.TOP_RIGHT)));

        CyderButton bottomNotify = new CyderButton("Bottom");
        bottomNotify.setSize(150, 40);
        bottomNotify.addActionListener(e -> testFrame.notify(new CyderFrame.NotificationBuilder(ctf.getText())
                .setViewDuration(milliDelay)
                .setArrowDir(Direction.BOTTOM)
                .setNotificationDirection(NotificationDirection.BOTTOM)));

        CyderButton leftNotify = new CyderButton("Top Left");
        leftNotify.setSize(150, 40);
        leftNotify.addActionListener(e -> testFrame.notify(new CyderFrame.NotificationBuilder(ctf.getText())
                .setViewDuration(milliDelay)
                .setArrowDir(Direction.LEFT)
                .setNotificationDirection(NotificationDirection.TOP_LEFT)));

        CyderButton centerLeftNotify = new CyderButton("Center Left");
        centerLeftNotify.setSize(150, 40);
        centerLeftNotify.addActionListener(e -> testFrame.notify(new CyderFrame.NotificationBuilder(ctf.getText())
                .setViewDuration(milliDelay)
                .setArrowDir(Direction.LEFT)
                .setNotificationDirection(NotificationDirection.LEFT)));

        CyderButton centerRightNotify = new CyderButton("Center Right");
        centerRightNotify.setSize(150, 40);
        centerRightNotify.addActionListener(e -> testFrame.notify(new CyderFrame.NotificationBuilder(ctf.getText())
                .setViewDuration(milliDelay)
                .setArrowDir(Direction.RIGHT)
                .setNotificationDirection(NotificationDirection.RIGHT)));

        CyderButton bottomLeftNotify = new CyderButton("Bottom Left");
        bottomLeftNotify.setSize(150, 40);
        bottomLeftNotify.addActionListener(e -> testFrame.notify(new CyderFrame.NotificationBuilder(ctf.getText())
                .setViewDuration(milliDelay)
                .setArrowDir(Direction.LEFT)
                .setNotificationDirection(NotificationDirection.BOTTOM_LEFT)));
        CyderButton bottomRightNotify = new CyderButton("Bottom Right");
        bottomRightNotify.setSize(170, 40);
        bottomRightNotify.addActionListener(e -> testFrame.notify(new CyderFrame.NotificationBuilder(ctf.getText())
                .setViewDuration(milliDelay)
                .setArrowDir(Direction.RIGHT)
                .setNotificationDirection(NotificationDirection.BOTTOM_RIGHT)));

        layout.addComponent(leftNotify, 0, 0);
        layout.addComponent(topNotify, 1, 0);
        layout.addComponent(rightNotify, 2, 0);

        layout.addComponent(centerLeftNotify, 0, 1);
        layout.addComponent(ctf, 1, 1);
        layout.addComponent(centerRightNotify, 2, 1);

        layout.addComponent(bottomLeftNotify, 0, 2);
        layout.addComponent(bottomNotify, 1, 2);
        layout.addComponent(bottomRightNotify, 2, 2);

        CyderPanel panel = new CyderPanel(layout);
        testFrame.setCyderLayoutPanel(panel);

        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.finalizeAndShow();
    }

    @ManualTest("askew test")
    public static void askewTest() {
        CyderFrame testFrame = new CyderFrame(350, 300, CyderIcons.defaultBackground);
        testFrame.setTitle("Askew Test");

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(100, 100, 150, 40);
        testFrame.getContentPane().add(ctf);

        CyderButton cb = new CyderButton("Askew");
        cb.setBounds(100, 200, 150, 40);
        testFrame.getContentPane().add(cb);
        cb.addActionListener(e -> testFrame.rotateBackground(Integer.parseInt(ctf.getText())));

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the sliding icon label.
     */
    @ManualTest("sliding icon label test")
    public static void iconLabelSlidingTest() {
        ImageIcon theImage = new ImageIcon(ImageUtil.getImageGradient(600, 1200,
                CyderColors.regularPink, CyderColors.regularBlue, CyderColors.regularBlue));

        CyderFrame testFrame = new CyderFrame(600, 600, theImage);
        testFrame.setTitle("Sliding test");
        testFrame.initializeResizing();
        testFrame.setResizable(true);

        CyderButton slideUp = new CyderButton("UP");
        slideUp.setBounds(225, 150, 150, 40);
        slideUp.addActionListener(e -> CyderThreadRunner.submit(() -> {
            testFrame.getContentPane().setSize(600, 1200);
            ((JLabel) testFrame.getContentPane()).setIcon(theImage);

            int x = testFrame.getContentPane().getX();
            for (int i = testFrame.getContentPane().getY() ; i > -testFrame.getHeight() ; i--) {
                testFrame.getContentPane().setLocation(x, i);
                ThreadUtil.sleep(1);
            }
            testFrame.getContentPane().setLocation(0, 0);
            testFrame.refreshBackground();
            testFrame.getContentPane().revalidate();
        }, ""));
        testFrame.getContentPane().add(slideUp);

        CyderButton slideLeft = new CyderButton("LEFT");
        slideLeft.setBounds(225, 200, 150, 40);
        slideLeft.addActionListener(e -> CyderThreadRunner.submit(() -> {
            int y = testFrame.getContentPane().getY();
            for (int i = 0 ; i > -testFrame.getWidth() ; i--) {
                testFrame.getContentPane().setLocation(i, y);
                ThreadUtil.sleep(1);
            }
            testFrame.getContentPane().setLocation(0, 0);
            testFrame.refreshBackground();
            testFrame.getContentPane().revalidate();
        }, ""));
        testFrame.getContentPane().add(slideLeft);

        CyderButton slideDown = new CyderButton("DOWN");
        slideDown.setBounds(225, 250, 150, 40);
        slideDown.addActionListener(e -> CyderThreadRunner.submit(() -> {
            int x = testFrame.getContentPane().getX();
            for (int i = 0 ; i < testFrame.getHeight() ; i++) {
                testFrame.getContentPane().setLocation(x, i);
                ThreadUtil.sleep(1);
            }
            testFrame.getContentPane().setLocation(0, 0);
            testFrame.refreshBackground();
            testFrame.getContentPane().revalidate();
        }, ""));
        testFrame.getContentPane().add(slideDown);

        CyderButton slideRight = new CyderButton("RIGHT");
        slideRight.setBounds(225, 300, 150, 40);
        slideRight.addActionListener(e -> CyderThreadRunner.submit(() -> {
            int y = testFrame.getContentPane().getY();
            for (int i = 0 ; i < testFrame.getWidth() ; i++) {
                testFrame.getContentPane().setLocation(i, y);
                ThreadUtil.sleep(1);
            }
            testFrame.getContentPane().setLocation(0, 0);
            testFrame.refreshBackground();
            testFrame.getContentPane().revalidate();
        }, ""));
        testFrame.getContentPane().add(slideRight);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for checkboxes.
     */
    @ManualTest("checkbox test")
    public static void checkboxTest() {
        CyderFrame testFrame = new CyderFrame(400, 400, CyderIcons.defaultBackground);
        testFrame.setTitle("Checkbox Test");

        CyderCheckbox cb = new CyderCheckbox();
        cb.setBounds(175, 150, 50, 50);
        cb.setRoundedCorners(true);
        testFrame.getContentPane().add(cb);

        CyderCheckbox cb1 = new CyderCheckbox();
        cb1.setBounds(175, 225, 50, 50);
        cb1.setRoundedCorners(false);
        testFrame.getContentPane().add(cb1);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the progress bar ui.
     */
    @ManualTest("progress bar test")
    public static void progressBarTest() {
        CyderFrame cf = new CyderFrame(400, 100);
        cf.setTitle("ProgressBar Test");

        JProgressBar jpb = new JProgressBar(0, 500);
        jpb.setBounds(40, 40, 320, 20);
        jpb.setOrientation(SwingConstants.HORIZONTAL);
        CyderProgressUI ui = new CyderProgressUI();
        ui.setAnimationDirection(CyderProgressUI.AnimationDirection.LEFT_TO_RIGHT);
        ui.setAnimationColors(CyderColors.regularBlue, CyderColors.regularPink);
        jpb.setUI(ui);
        jpb.setValue(50);
        cf.getContentPane().add(jpb);
        cf.finalizeAndShow();

        CyderThreadRunner.submit(() -> {
            for (int i = 0 ; i <= jpb.getMaximum() / 2 ; i++) {
                jpb.setValue(i);
                ThreadUtil.sleep(2000 / jpb.getMaximum());
            }

            for (int i = jpb.getMaximum() / 2 ; i <= jpb.getMaximum() ; i++) {
                jpb.setValue(i);
                ThreadUtil.sleep(500 / jpb.getMaximum());
            }
        }, "ProgressBar Animator");
    }

    /**
     * Tests for the slider ui.
     */
    @ManualTest("slider test")
    public static void cyderSliderTest() {
        CyderFrame testFrame = new CyderFrame(400, 400);
        testFrame.setTitle("Cyder Slider Test");

        JSlider audioVolumeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
        CyderSliderUi UI = new CyderSliderUi(audioVolumeSlider);
        UI.setThumbRadius(25);
        UI.setThumbShape(CyderSliderUi.ThumbShape.CIRCLE);
        UI.setThumbFillColor(CyderColors.regularPink);
        UI.setThumbOutlineColor(CyderColors.regularPink);
        UI.setRightThumbColor(CyderColors.navy);
        UI.setLeftThumbColor(CyderColors.regularBlue);
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

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the frame title length.
     */
    @ManualTest("frame length test")
    public static void frameTitleLengthTest() {
        CyderFrame cf = new CyderFrame(600, 200);
        cf.setTitle("Title Length Test");
        cf.setTitlePosition(CyderFrame.TitlePosition.LEFT);

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(40, 40, 600 - 80, 40);
        cf.getContentPane().add(ctf);
        ctf.addActionListener(e -> cf.setTitle(ctf.getText().trim()));

        CyderButton cb = new CyderButton("Set Title");
        cb.setBounds(40, 100, 600 - 80, 40);
        cf.getContentPane().add(cb);
        cb.addActionListener(e -> cf.setTitle(ctf.getText().trim()));

        cf.finalizeAndShow();
    }

    /**
     * Tests for the switch.
     */
    @ManualTest("switch test")
    public static void switchTest() {
        CyderFrame testFrame = new CyderFrame(500, 500);
        testFrame.setTitle("CyderSwitch test");

        CyderSwitch cs = new CyderSwitch(300, 100);
        cs.setBounds(100, 100, 300, 100);
        cs.setState(CyderSwitch.State.OFF);
        testFrame.getContentPane().add(cs);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the ripple label.
     */
    @ManualTest("ripple label test")
    public static void rippleLabelTest() {
        CyderFrame rippleTestFrame = new CyderFrame(600, 600);
        rippleTestFrame.setTitle("Ripple Test");

        CyderLabel ripplingLabel = new CyderLabel("<html>" + CyderStrings.QUICK_BROWN_FOX + "<br/>" +
                CyderStrings.QUICK_BROWN_FOX + "<br/><br/>" + CyderStrings.QUICK_BROWN_FOX + "<br/>" +
                CyderStrings.QUICK_BROWN_FOX + "<br/>" + CyderStrings.QUICK_BROWN_FOX + "<br/>" +
                CyderStrings.QUICK_BROWN_FOX + "<br/><br/>Love,<br/>Nathan Cheshire" + "</html>");
        ripplingLabel.setFont(CyderFonts.SEGOE_20);

        //fill content area with label
        ripplingLabel.setBounds(40, 40,
                rippleTestFrame.getWidth() - 40 * 2, rippleTestFrame.getHeight() - 40 * 2);
        rippleTestFrame.getContentPane().add(ripplingLabel);

        //fast timeout and relatively high char count
        ripplingLabel.setRippleMsTimeout(10);
        ripplingLabel.setRippleChars(15);

        //enable rippling
        ripplingLabel.setRippling(true);

        rippleTestFrame.finalizeAndShow();
    }

    /**
     * Tests for the checkbox group.
     */
    @ManualTest("checkbox group test")
    public static void checkboxGroupTest() {
        CyderFrame testFrame = new CyderFrame(400, 110);
        testFrame.setTitle("Checkbox group test");

        CyderCheckboxGroup cbg = new CyderCheckboxGroup();

        int startX = 50;

        for (int i = 0 ; i < 5 ; i++) {
            CyderCheckbox cb = new CyderCheckbox();
            cb.setBounds(startX + (60) * i, 40, 50, 50);
            testFrame.getContentPane().add(cb);

            if (i != 4)
                cbg.addCheckbox(cb);
        }

        testFrame.finalizeAndShow();
    }

    /**
     * Test for the grid layout.
     */
    @ManualTest("grid layout test")
    public static void cyderGridLayoutTest() {
        //regular frame calls
        CyderFrame gridTestFrame = new CyderFrame(800, 800);
        gridTestFrame.setTitle("Grid Layout Test");

        //init the main panel layout
        CyderGridLayout layout = new CyderGridLayout(2, 2);

        //add components to the layout at specified position
        CyderButton testButton = new CyderButton("This");
        testButton.setSize(100, 100);
        testButton.addActionListener(e -> gridTestFrame.notify(
                new CyderFrame.NotificationBuilder("Notified button clicked")));
        layout.addComponent(testButton, 0, 0, GridPosition.RIGHT);

        CyderLabel testLabel2 = new CyderLabel("A");
        testLabel2.setSize(50, 50);
        layout.addComponent(testLabel2, 0, 1);

        CyderLabel testLabel3 = new CyderLabel("IS");
        testLabel3.setSize(50, 50);
        layout.addComponent(testLabel3, 1, 0);

        CyderLabel testLabel4 = new CyderLabel("Test");
        testLabel4.setSize(50, 50);
        CyderButton testButton1 = new CyderButton("Click");
        testButton1.setSize(150, 40);

        //sub grid
        CyderGridLayout cyderGridLayout2 = new CyderGridLayout(2, 1);
        cyderGridLayout2.addComponent(testLabel4, 0, 0);
        cyderGridLayout2.addComponent(testButton1, 1, 0);

        //make sub panel and set layout as sub grid
        CyderPanel subPanel = new CyderPanel(cyderGridLayout2);
        layout.addComponent(subPanel, 1, 1);

        //create master panel with the layout we have added components to
        CyderPanel panel = new CyderPanel(layout);
        //set the frame's content panel
        gridTestFrame.setCyderLayoutPanel(panel);

        //resizing on
        gridTestFrame.initializeResizing();
        gridTestFrame.setResizable(true);
        gridTestFrame.setMaximumSize(new Dimension(1200, 1200));
        gridTestFrame.setBackgroundResizing(true);

        //regular final frame calls
        gridTestFrame.finalizeAndShow();
    }

    /**
     * Test for the flow layout.
     */
    @ManualTest("flow layout test")
    public static void flowLayoutTest() {
        CyderFrame testFrame = new CyderFrame(600, 600);
        testFrame.setTitle("Flow Layout Test");

        // make layout
        CyderFlowLayout layout = new CyderFlowLayout(HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER, 25, 15);

        //add 10 buttons to layout
        for (int i = 1 ; i < 11 ; i++) {
            CyderButton cb = new CyderButton("Test Button " + i);
            cb.setSize(200, 50);
            int finalI = i;
            cb.addActionListener(e -> testFrame.notify(
                    new CyderFrame.NotificationBuilder(finalI + " button: " + cb)));
            layout.addComponent(cb);
        }

        //make panel and set as frame's content panel
        CyderPanel panel = new CyderPanel(layout);
        testFrame.setCyderLayoutPanel(panel);

        //resizing on
        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.setMaximumSize(new Dimension(2000, 2000));
        testFrame.setMinimumSize(new Dimension(300, 300));
        testFrame.setBackgroundResizing(true);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for popups switcher.
     */
    @ManualTest("inform test")
    public static void popupTest() {
        CyderFrame testFrame = new CyderFrame(400, 120);
        testFrame.setTitle("Inform Test");

        CyderTextField ctf = new CyderTextField(0);
        ctf.setBounds(40, 40, 320, 40);
        ctf.addActionListener(e -> {
            String text = ctf.getText();

            if (!text.isEmpty()) {
                InformHandler.inform(text);
            }
        });
        testFrame.getContentPane().add(ctf);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for CyderFrame menu.
     */
    @ManualTest("menu test")
    public static void frameMenuTest() {
        CyderFrame testFrame = new CyderFrame(400, 400);
        testFrame.setTitle("Menu Test");

        testFrame.setMenuEnabled(true);
        testFrame.setMenuType(CyderFrame.MenuType.RIBBON);

        testFrame.initializeResizing();
        testFrame.setResizable(true);
        testFrame.setMaximumSize(new Dimension(1000, 1000));

        testFrame.addMenuItem("hello", () -> testFrame.notify("hello"));
        testFrame.addMenuItem("darkness", () -> testFrame.notify("darkness"));
        testFrame.addMenuItem("my old", () -> testFrame.notify("my old"));
        testFrame.addMenuItem("friend", () -> testFrame.notify("friend"));
        testFrame.addMenuItem("I've come to talk", () -> testFrame.notify("I've come to talk"));
        testFrame.addMenuItem("with you again", () -> testFrame.notify("with you again"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("something", () -> testFrame.notify("something"));
        testFrame.addMenuItem("done", () -> testFrame.notify("done"));

        CyderButton switchMenuType = new CyderButton("Switch Menu");
        switchMenuType.setSize(200, 40);
        switchMenuType.addActionListener(e -> {
            if (testFrame.getMenuType() == CyderFrame.MenuType.PANEL) {
                testFrame.setMenuType(CyderFrame.MenuType.RIBBON);
            } else {
                testFrame.setMenuType(CyderFrame.MenuType.PANEL);
            }
        });

        CyderTextField addMenuItem = new CyderTextField(0);
        addMenuItem.setSize(200, 40);
        addMenuItem.addActionListener(e -> {
            if (addMenuItem.getText().trim().length() < 3)
                return;

            testFrame.addMenuItem(addMenuItem.getText(), () -> testFrame.notify(addMenuItem.getText()));
        });

        CyderGridLayout gridLayout = new CyderGridLayout(1, 2);
        gridLayout.addComponent(switchMenuType);
        gridLayout.addComponent(addMenuItem);

        CyderPanel panel = new CyderPanel(gridLayout);
        testFrame.setCyderLayoutPanel(panel);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the notify and possibly overflow
     * onto an inform pane custom container test.
     */
    @ManualTest("notify container test")
    public static void notifyAndInformCustomContainerTest() {
        CyderFrame testFrame = new CyderFrame(400, 400);
        testFrame.setTitle("Notify Container Test");

        JLabel container = new JLabel("<html><div>Creatine water weight, yeah boi</div></html>",
                SwingConstants.CENTER);
        container.setSize(500, 500);
        container.setFont(CyderFonts.DEFAULT_FONT);

        // needs to be opaque to fill background
        container.setOpaque(true);
        container.setBackground(CyderColors.notificationBackgroundColor);
        container.setForeground(CyderColors.vanilla);

        CyderTextField ctf = new CyderTextField(0);
        ctf.setSize(200, 40);
        ctf.addActionListener(
                e -> testFrame.notify(new CyderFrame.NotificationBuilder("NULL").setContainer(container)));

        CyderFlowLayout cyderFlow = new CyderFlowLayout(
                HorizontalAlignment.CENTER_STATIC,
                VerticalAlignment.CENTER_STATIC);
        cyderFlow.addComponent(ctf);

        CyderPanel panel = new CyderPanel(cyderFlow);
        testFrame.setCyderLayoutPanel(panel);

        testFrame.finalizeAndShow();
    }

    /**
     * Tests for the notify and possibly overflow
     * onto an inform pane custom container test.
     */
    @ManualTest("disable relative to test")
    public static void informDisableRelativeToTest() {
        CyderFrame testFrame = new CyderFrame(400, 400);
        testFrame.setTitle("Disable RelativeTo test");

        testFrame.finalizeAndShow();

        InformHandler.inform(new InformHandler.Builder("Hello")
                .setDisableRelativeTo(true)
                .setRelativeTo(testFrame));
    }

    /**
     * Tests for the shape of the checkbox check.
     */
    @ManualTest("checkbox check")
    public static void checkboxCheckTest() {
        CyderFrame checkboxFrame = new CyderFrame(220, 350);
        checkboxFrame.setTitle("Checkbox Test");

        CyderCheckbox squareRegular = new CyderCheckbox();
        squareRegular.setLocation(50, 50);
        squareRegular.setRoundedCorners(false);
        checkboxFrame.getContentPane().add(squareRegular);

        CyderCheckbox roundedRegular = new CyderCheckbox();
        roundedRegular.setLocation(110, 50);
        roundedRegular.setRoundedCorners(true);
        checkboxFrame.getContentPane().add(roundedRegular);

        CyderCheckbox squareCheckColor = new CyderCheckbox();
        squareCheckColor.setLocation(50, 110);
        squareCheckColor.setRoundedCorners(false);
        squareCheckColor.setCheckColor(CyderColors.regularBlue);
        checkboxFrame.getContentPane().add(squareCheckColor);

        CyderCheckbox roundedCheckColor = new CyderCheckbox();
        roundedCheckColor.setLocation(110, 110);
        roundedCheckColor.setRoundedCorners(true);
        roundedCheckColor.setCheckColor(CyderColors.regularBlue);
        checkboxFrame.getContentPane().add(roundedCheckColor);

        CyderCheckbox squareDifferentShape = new CyderCheckbox();
        squareDifferentShape.setLocation(50, 170);
        squareDifferentShape.setCheckShape(CyderCheckbox.CheckShape.FILLED_CIRCLE);
        squareDifferentShape.setRoundedCorners(false);
        checkboxFrame.getContentPane().add(squareDifferentShape);

        CyderCheckbox roundedDifferentShape = new CyderCheckbox();
        roundedDifferentShape.setLocation(110, 170);
        roundedDifferentShape.setCheckShape(CyderCheckbox.CheckShape.FILLED_CIRCLE);
        roundedDifferentShape.setRoundedCorners(true);
        checkboxFrame.getContentPane().add(roundedDifferentShape);

        CyderCheckbox squareHollowCheck = new CyderCheckbox();
        squareHollowCheck.setLocation(50, 230);
        squareHollowCheck.setCheckShape(CyderCheckbox.CheckShape.HOLLOW_CIRCLE);
        squareHollowCheck.setRoundedCorners(false);
        checkboxFrame.getContentPane().add(squareHollowCheck);

        CyderCheckbox roundedHollowCheck = new CyderCheckbox();
        roundedHollowCheck.setLocation(110, 230);
        roundedHollowCheck.setCheckShape(CyderCheckbox.CheckShape.HOLLOW_CIRCLE);
        roundedHollowCheck.setRoundedCorners(true);
        checkboxFrame.getContentPane().add(roundedHollowCheck);

        checkboxFrame.finalizeAndShow();
    }

    @ManualTest("partitioned layout test")
    public static void partitionedLayoutTest() {
        CyderFrame horizontalFrame = new CyderFrame(200, 500);
        horizontalFrame.setTitle("Partitioned layout");

        CyderPartitionedLayout layout = new CyderPartitionedLayout();
        layout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.COLUMN);
        layout.setNewComponentPartitionAlignment(CyderPartitionedLayout.PartitionAlignment.CENTER);

        int components = 5;
        Dimension buttonSize = new Dimension(80, 80);
        layout.setNewComponentPartitionSpace(CyderPartitionedLayout.MAX_PARTITION / components);

        for (int i = 0 ; i < components ; i++) {
            CyderButton button = new CyderButton(String.valueOf(i));
            button.setSize(buttonSize);
            int finalI = i;
            button.addActionListener(e -> horizontalFrame.notify("Clicked: " + finalI));

            if (i == 2) {
                layout.addComponent(new JLabel());
                continue;
            }

            layout.addComponent(button);
        }

        CyderPanel contentPanel = new CyderPanel(layout);

        horizontalFrame.initializeResizing();
        horizontalFrame.setMaximumSize(200, 1000);
        horizontalFrame.setMinimumSize(200, 400);

        horizontalFrame.setCyderLayoutPanel(contentPanel);
        horizontalFrame.finalizeAndShow();
    }

    @ManualTest("drag label text button test")
    public static void dragLabelTextButtonTest() {
        CyderFrame testFrame = new CyderFrame(800, 800);
        testFrame.setTitle("Drag Label Text Button Test");

        CyderGridLayout cyderGridLayout = new CyderGridLayout(1, 4);

        CyderLabel leftLabel = new CyderLabel("Add left text button");
        leftLabel.setSize(400, 40);
        cyderGridLayout.addComponent(leftLabel);

        CyderTextField leftField = new CyderTextField();
        leftField.setSize(200, 40);
        leftField.addActionListener(e -> {
            String text = leftField.getTrimmedText();

            if (!text.isEmpty()) {
                JLabel textButton = CyderDragLabel.generateTextButton(text, text, () -> testFrame.notify(text));
                testFrame.getTopDragLabel().addLeftButton(textButton, 0);
            }
        });
        cyderGridLayout.addComponent(leftField);

        CyderLabel rightLabel = new CyderLabel("Add right text button");
        rightLabel.setSize(400, 40);
        cyderGridLayout.addComponent(rightLabel);

        CyderTextField rightField = new CyderTextField();
        rightField.setSize(200, 40);
        rightField.addActionListener(e -> {
            String text = rightField.getTrimmedText();

            if (!text.isEmpty()) {
                JLabel textButton = CyderDragLabel.generateTextButton(text, text, () -> testFrame.notify(text));
                testFrame.getTopDragLabel().addRightButton(textButton, 0);
            }
        });
        cyderGridLayout.addComponent(rightField);

        testFrame.initializeResizing();
        testFrame.setMaximumSize(1000, 1000);
        testFrame.setMinimumSize(400, 400);

        testFrame.setCyderLayout(cyderGridLayout);
        testFrame.finalizeAndShow();
    }

    @ManualTest("modern button test")
    private static void modernButtonTest() {
        CyderFrame testFrame = new CyderFrame();
        testFrame.setTitle("Modern button test");
        testFrame.setSize(500, 200);

        CyderGridLayout layout = new CyderGridLayout(4, 1);

        CyderModernButton regularButton = new CyderModernButton("Regular");
        regularButton.pack();
        regularButton.setYPadding(2);
        regularButton.setXPadding(10);
        layout.addComponent(regularButton);

        CyderModernButton differentButton = new CyderModernButton("Different");
        differentButton.setForegroundColor(CyderColors.vanilla);
        differentButton.setBorderLength(0);
        differentButton.setColors(CyderColors.regularBlue);
        differentButton.pack();
        differentButton.setYPadding(0);
        differentButton.setXPadding(10);
        layout.addComponent(differentButton);

        CyderModernButton greenButton = new CyderModernButton("Green");
        greenButton.setForegroundColor(CyderColors.vanilla);
        greenButton.setFont(CyderFonts.SEGOE_20);
        greenButton.setBorderLength(4);
        greenButton.setColors(CyderColors.regularGreen);
        greenButton.pack();
        greenButton.setYPadding(0);
        greenButton.setXPadding(10);
        layout.addComponent(greenButton);

        CyderModernButton cyderButtonCopy = new CyderModernButton("Button");
        cyderButtonCopy.setRoundedCorners(false);
        cyderButtonCopy.setFont(CyderFonts.DEFAULT_FONT);
        cyderButtonCopy.setForegroundColor(CyderColors.navy);
        cyderButtonCopy.setBorderLength(5);
        cyderButtonCopy.setColors(CyderColors.regularRed);
        cyderButtonCopy.pack();
        cyderButtonCopy.setYPadding(0);
        cyderButtonCopy.setXPadding(10);
        layout.addComponent(cyderButtonCopy);

        testFrame.setCyderLayout(layout);
        testFrame.finalizeAndShow();
    }
}
