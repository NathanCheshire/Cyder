package cyder.console;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import cyder.audio.AudioIcons;
import cyder.audio.AudioPlayer;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.Direction;
import cyder.enums.Dynamic;
import cyder.enums.ExitCondition;
import cyder.enums.IgnoreThread;
import cyder.exceptions.FatalException;
import cyder.genesis.CyderSplash;
import cyder.genesis.PropLoader;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.records.ConsoleBackground;
import cyder.test.ManualTests;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.*;
import cyder.user.*;
import cyder.utils.*;
import cyder.youtube.YoutubeUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyder.console.ConsoleConstants.*;

/**
 * Singleton of components that represent the GUI way a user
 * interacts with Cyder and its functions.
 */
public enum Console {
    /**
     * The Console singleton.
     */
    INSTANCE;

    /**
     * Log when the console singleton is constructed
     * (enums are constructed when they are first referenced).
     */
    Console() {
        Logger.log(Logger.Tag.OBJECT_CREATION, "Console singleton constructed");
    }

    /**
     * The font used for the clock label.
     */
    public static final Font CONSOLE_CLOCK_FONT = new Font("Segoe UI Black", Font.BOLD, 21);

    /**
     * An immutable list of the frames to ignore when placing a frame in the console taskbar menu.
     */
    private ImmutableList<CyderFrame> frameTaskbarExceptions;

    /**
     * The UUID of the user currently associated with the Console.
     */
    private String uuid;

    /**
     * The previous uuid used for tracking purposes.
     */
    private String previousUuid;

    /**
     * The Console's CyderFrame instance.
     */
    private CyderFrame consoleCyderFrame;

    /**
     * The input handler linked to the Console's IO.
     */
    private BaseInputHandler baseInputHandler;

    /**
     * The Console output scroll pane.
     */
    private CyderScrollPane outputScroll;

    /**
     * The Console output TextPane controlled by the scroll pane.
     */
    private JTextPane outputArea;

    /**
     * The JTextPane used for the console menu.
     */
    private JTextPane menuPane;

    /**
     * The input field for the Console. This is a password field
     * in case we ever want to obfuscate the text in the future.
     */
    private JPasswordField inputField;

    /**
     * The label added to the top drag label to show the time.
     */
    private JLabel consoleClockLabel;

    /**
     * The label used for the Cyder taskbar.
     */
    private JLabel menuLabel;

    /**
     * The scroll pane for the active frames.
     */
    private CyderScrollPane menuScroll;

    /**
     * The top drag label menu toggle button.
     */
    private JButton menuButton;

    /**
     * The top drag label pin button.
     */
    private final JButton pinButton = new CyderIconButton("Pin",
            CyderIcons.pinIcon, CyderIcons.pinIconHover, new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            if (consoleCyderFrame.isAlwaysOnTop()) {
                pinButton.setIcon(CyderIcons.pinIcon);
            } else {
                pinButton.setIcon(CyderIcons.pinIconHover);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (consoleCyderFrame.isAlwaysOnTop()) {
                pinButton.setIcon(CyderIcons.pinIconHover);
            } else {
                pinButton.setIcon(CyderIcons.pinIcon);
            }
        }
    }, new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            pinButton.setIcon(CyderIcons.pinIconHover);
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (consoleCyderFrame.isAlwaysOnTop()) {
                pinButton.setIcon(CyderIcons.pinIconHover);
            } else {
                pinButton.setIcon(CyderIcons.pinIcon);
            }
        }
    });

    /**
     * The top drag label help button.
     */
    private final JButton helpButton = new CyderIconButton(
            "Help", CyderIcons.helpIcon, CyderIcons.helpIconHover);

    /**
     * The top drag label audio menu toggle button.
     */
    private final JButton toggleAudioControls = new CyderIconButton(
            "Audio Controls", CyderIcons.menuIcon, CyderIcons.menuIconHover);

    /**
     * The top drag label minimize button.
     */
    private final CyderIconButton minimizeButton = new CyderIconButton(
            "Minimize", CyderIcons.minimizeIcon, CyderIcons.minimizeIconHover);

    /**
     * The top drag label alternate background button.
     */
    private final CyderIconButton alternateBackgroundButton = new CyderIconButton("Alternate Background",
            CyderIcons.changeSizeIcon, CyderIcons.changeSizeIconHover);

    /**
     * The top drag label close button.
     */
    private final CyderIconButton closeButton = new CyderIconButton("Close",
            CyderIcons.closeIcon, CyderIcons.closeIconHover, new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            closeButton.setIcon(CyderIcons.closeIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            closeButton.setIcon(CyderIcons.closeIcon);
        }
    }, new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            closeButton.setIcon(CyderIcons.closeIconHover);
        }

        @Override
        public void focusLost(FocusEvent e) {
            closeButton.setIcon(CyderIcons.closeIcon);
            outputArea.requestFocus();
        }
    });

    /**
     * The audio menu parent label
     */
    private JLabel audioControlsLabel;

    /**
     * The button label used to indicate if audio is playing or not
     */
    private JLabel playPauseAudioLabel;

    /**
     * Whether the console is closed.
     */
    private boolean consoleClosed = true;

    /**
     * The current bash string to use for the start of the input field.
     */
    private String consoleBashString;

    /**
     * The command list used for scrolling.
     */
    private final ArrayList<String> commandList = new ArrayList<>();

    /**
     * The index of the command in the command history list we are at.
     */
    private int commandIndex;

    /**
     * The last direction performed upon the most recent switch background call.
     */
    private Direction lastSlideDirection = Direction.LEFT;

    /**
     * The current orientation of the Console.
     */
    private Direction consoleDir = Direction.TOP;

    /**
     * The last direction the console was oriented in.
     */
    private Direction lastConsoleDir = consoleDir;

    /**
     * The list of recognized backgrounds that the Console may switch to.
     */
    private final ArrayList<ConsoleBackground> backgrounds = new ArrayList<>();

    /**
     * The index of the background we are currently at in the backgrounds list.
     */
    private int backgroundIndex;

    /**
     * Whether dancing is currently active.
     */
    private boolean currentlyDancing;

    /**
     * Performs Console setup routines before constructing
     * the frame and setting its visibility, location, and size.
     *
     * @throws FatalException if the Console was left open
     */
    public void launch() {
        ExceptionHandler.checkFatalCondition(isClosed(), previousUuid);

        loadBackgrounds();
        resizeBackgrounds();

        resetMembers();

        CyderColors.refreshGuiThemeColor();

        ConsoleIcon consoleIcon = determineConsoleIconAndDimensions();

        setupConsoleCyderFrame(consoleIcon);

        refreshConsoleTitle();

        installConsoleResizing();

        installOutputArea();
        installInputField();

        baseInputHandler = new BaseInputHandler(outputArea);
        baseInputHandler.startConsolePrintingAnimation();

        setupButtonEnterInputMap();

        installDragLabelButtons();

        generateAudioMenu();

        installConsoleClock();

        installConsolePinnedWindowListeners();

        startExecutors();

        FrameUtil.closeAllFrames(true, consoleCyderFrame);
        CyderSplash.INSTANCE.fastDispose();

        if (!isFullscreen()) {
            restorePreviousFrameBounds(consoleIcon);
        } else {
            consoleCyderFrame.setVisible(true);
        }

        revalidateInputAndOutputBounds(true);

        TimeUtil.setConsoleStartTime(System.currentTimeMillis());
        baseInputHandler.println("Console loaded in " + (TimeUtil.getConsoleStartTime()
                - TimeUtil.getAbsoluteStartTime()) + "ms");
    }

    /**
     * Resets private variables to their default state.
     */
    private void resetMembers() {
        consoleBashString = UserUtil.getCyderUser().getName() + BASH_STRING_PREFIX;

        lastSlideDirection = DEFAULT_CONSOLE_DIRECTION;
        consoleDir = DEFAULT_CONSOLE_DIRECTION;

        commandIndex = 0;

        consoleClosed = false;
        menuLabel = null;

        commandList.clear();
        currentActiveFrames.clear();
    }

    /**
     * Sets up the console cyder frame and performs all subsequent calls on the object.
     *
     * @param consoleIcon the console icon record to use for the direct props
     */
    private void setupConsoleCyderFrame(ConsoleIcon consoleIcon) {
        consoleCyderFrame = new CyderFrame(consoleIcon.dimension().width,
                consoleIcon.dimension.height, consoleIcon.background) {
            @Override
            public void setBounds(int x, int y, int w, int h) {
                super.setBounds(x, y, w, h);

                revalidateInputAndOutputBounds();

                if (menuLabel != null && menuLabel.isVisible()) {
                    menuLabel.setBounds(3, CyderDragLabel.DEFAULT_HEIGHT - 2,
                            menuLabel.getWidth(),
                            consoleCyderFrame.getHeight() - CyderDragLabel.DEFAULT_HEIGHT - 5);
                }

                if (audioControlsLabel != null && audioControlsLabel.isVisible()) {
                    audioControlsLabel.setBounds(w - audioControlsLabel.getWidth() - 6,
                            CyderDragLabel.DEFAULT_HEIGHT - 2,
                            audioControlsLabel.getWidth(), audioControlsLabel.getHeight());
                }

                revalidateMenu();
                refreshClockText();
                revalidateTitleNotify();
            }

            /**
             * Disposes the console and ensures focus borders do not appear during
             * the possible close animation.
             */
            @Override
            public void dispose() {
                outputArea.setFocusable(false);
                outputScroll.setFocusable(false);

                super.dispose(isFullscreen());
            }

            private final int DEGREE_LIMIT = 360;
            private final int DEGREE_INCREMENT = 2;
            private final int DEGREE_DELAY = 2;
            private boolean consoleBarrelRollLocked = false;

            @Override
            public void barrelRoll() {
                if (consoleBarrelRollLocked)
                    return;

                consoleBarrelRollLocked = true;

                CyderThreadRunner.submit(() -> {
                    BufferedImage masterImage = getCurrentBackground().generateBufferedImage();
                    for (int i = 0 ; i <= DEGREE_LIMIT ; i += DEGREE_INCREMENT) {
                        BufferedImage rotated = ImageUtil.rotateImage(masterImage, i);
                        getConsoleCyderFrameContentPane().setIcon(new ImageIcon(rotated));
                        ThreadUtil.sleep(DEGREE_DELAY);
                    }

                    getConsoleCyderFrameContentPane().setIcon(getCurrentBackground().generateImageIcon());
                    consoleBarrelRollLocked = false;
                }, "Console Barrel roll");
            }
        };

        frameTaskbarExceptions = ImmutableList.of(consoleCyderFrame, CyderSplash.INSTANCE.getSplashFrame());

        consoleCyderFrame.setBackground(Color.black);

        consoleCyderFrame.addPostCloseAction(() -> OSUtil.exit(ExitCondition.GenesisControlledExit));

        consoleCyderFrame.setDraggingEnabled(!UserUtil.getCyderUser().getFullscreen().equals("1"));

        consoleCyderFrame.addWindowListener(consoleWindowAdapter);

        getConsoleCyderFrameContentPane().setToolTipText(
                FileUtil.getFilename(getCurrentBackground().referenceFile().getName()));

        consoleCyderFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        consoleCyderFrame.setPaintCyderFrameTitle(false);
        consoleCyderFrame.setPaintSuperTitle(true);

        consoleCyderFrame.setShouldAnimateOpacity(!isFullscreen());
    }

    /**
     * The record used for determining the console background icon and the corresponding width and height.
     */
    private record ConsoleIcon(ImageIcon background, Dimension dimension) {}

    /**
     * Determines the initial console background icon.
     *
     * @return a record containing the initial console background icon and the dimensions of the icon
     */
    private ConsoleIcon determineConsoleIconAndDimensions() {
        int width;
        int height;
        ImageIcon icon;

        if (UserUtil.getCyderUser().getRandombackground().equals("1")) {
            if (getBackgrounds().size() <= 1) {
                consoleCyderFrame.notify("Sorry, " + UserUtil.getCyderUser().getName()
                        + ", but you only have one background file "
                        + "so there's no random element to be chosen.");
            } else {
                backgroundIndex = NumberUtil.randInt(0, backgrounds.size() - 1);
            }
        }

        if (UserUtil.getCyderUser().getFullscreen().equals("1")) {
            width = ScreenUtil.getScreenWidth();
            height = ScreenUtil.getScreenHeight();
            icon = new ImageIcon(ImageUtil.resizeImage(width,
                    height, getCurrentBackground().referenceFile()));
        } else {
            BufferedImage bi = getCurrentBackground().generateBufferedImage();

            if (bi == null) {
                throw new FatalException("Generated buffered image is null");
            }

            width = bi.getWidth();
            height = bi.getHeight();
            icon = new ImageIcon(ImageUtil.getRotatedImage(
                    getCurrentBackground().referenceFile().toString(), getConsoleDirection()));
        }

        if (width == 0 || height == 0)
            throw new FatalException("Could not construct background dimension");

        return new ConsoleIcon(icon, new Dimension(width, height));
    }

    /**
     * Refreshes the console title.
     */
    private void refreshConsoleTitle() {
        consoleCyderFrame.setTitle(PropLoader.getString("version") +
                " Cyder [" + UserUtil.getCyderUser().getName() + "]");
    }

    /**
     * The mouse motion adapter to add to the console used for pinned window logic.
     */
    private final MouseMotionAdapter consolePinnedWindowMouseMotionAdapter = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (consoleCyderFrame != null
                    && consoleCyderFrame.isFocused()
                    && consoleCyderFrame.isDraggingEnabled()) {

                for (Frame f : Frame.getFrames()) {
                    if (f instanceof CyderFrame
                            && ((CyderFrame) f).isConsolePinned()
                            && !f.getTitle().equals(consoleCyderFrame.getTitle())
                            && ((CyderFrame) f).getRelativeX() != Integer.MIN_VALUE
                            && ((CyderFrame) f).getRelativeY() != Integer.MIN_VALUE) {

                        f.setLocation(
                                consoleCyderFrame.getX() + ((CyderFrame) f).getRelativeX(),
                                consoleCyderFrame.getY() + ((CyderFrame) f).getRelativeY());
                    }
                }
            }
        }
    };

    /**
     * The mouse adapter to add to all the console drag labels.
     */
    private final MouseAdapter consolePinnedWindowMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (consoleCyderFrame != null
                    && consoleCyderFrame.isFocused()
                    && consoleCyderFrame.isDraggingEnabled()) {

                Rectangle consoleRect = new Rectangle(
                        consoleCyderFrame.getX(),
                        consoleCyderFrame.getY(),
                        consoleCyderFrame.getWidth(),
                        consoleCyderFrame.getHeight());

                for (Frame f : Frame.getFrames()) {
                    if (f instanceof CyderFrame
                            && ((CyderFrame) f).isConsolePinned()
                            && !f.getTitle().equals(consoleCyderFrame.getTitle())) {
                        Rectangle frameRect = new Rectangle(f.getX(), f.getY(), f.getWidth(), f.getHeight());

                        if (MathUtil.rectanglesOverlap(consoleRect, frameRect)) {
                            ((CyderFrame) f).setRelativeX(-consoleCyderFrame.getX() + f.getX());
                            ((CyderFrame) f).setRelativeY(-consoleCyderFrame.getY() + f.getY());
                        } else {
                            ((CyderFrame) f).setRelativeX(Integer.MIN_VALUE);
                            ((CyderFrame) f).setRelativeY(Integer.MIN_VALUE);
                        }
                    }
                }
            }
        }
    };

    /**
     * Adds the pinned window logic listeners to the console.
     */
    private void installConsolePinnedWindowListeners() {
        consoleCyderFrame.addDragListener(consolePinnedWindowMouseMotionAdapter);
        consoleCyderFrame.addDragLabelMouseListener(consolePinnedWindowMouseAdapter);
    }

    /**
     * Revalidates the bounds of the input field and output area based off
     * of the current console size and the menu state.
     */
    private void revalidateInputAndOutputBounds() {
        revalidateInputAndOutputBounds(false);
    }

    /**
     * Revalidates the bounds of the input field and output area based off
     * of the current console size and the menu state.
     *
     * @param ignoreMenuLabel whether to ignore the menu label and treat it as invisible
     */
    private void revalidateInputAndOutputBounds(boolean ignoreMenuLabel) {
        if (outputScroll != null && inputField != null) {
            int w = consoleCyderFrame.getWidth();
            int h = consoleCyderFrame.getHeight();

            int addX = 0;

            if (menuLabel != null && menuLabel.isVisible() && !ignoreMenuLabel) {
                addX = 2 + menuLabel.getWidth();
            }

            outputScroll.setBounds(addX + FIELD_X_PADDING,
                    CyderDragLabel.DEFAULT_HEIGHT + FIELD_Y_PADDING,
                    w - addX - 2 * FIELD_X_PADDING,
                    h - INPUT_FIELD_HEIGHT - FIELD_Y_PADDING * 3 - CyderDragLabel.DEFAULT_HEIGHT);

            inputField.setBounds(addX + FIELD_X_PADDING,
                    outputScroll.getY() + FIELD_Y_PADDING + outputScroll.getHeight(),
                    w - 2 * FIELD_X_PADDING - addX, INPUT_FIELD_HEIGHT);
        }
    }

    /**
     * Sets up the output area and output scroll and adds it to the console.
     */
    private void installOutputArea() {
        outputArea = new JTextPane() {
            @Override
            public void setBounds(int x, int y, int w, int h) {
                StyledDocument sd = outputArea.getStyledDocument();
                int pos = outputArea.getCaretPosition();
                super.setBounds(x, y, w, h);
                outputArea.setStyledDocument(sd);
                outputArea.setCaretPosition(pos);
            }
        };

        outputArea.setEditable(false);
        outputArea.setCaretColor(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
        outputArea.setCaret(new CyderCaret(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground())));
        outputArea.setAutoscrolls(true);

        outputArea.setFocusable(true);
        outputArea.setSelectionColor(CyderColors.selectionColor);
        outputArea.setOpaque(false);
        outputArea.setBackground(CyderColors.empty);
        outputArea.setForeground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
        outputArea.setFont(INSTANCE.generateUserFont());

        installOutputAreaListeners();

        outputScroll = new CyderScrollPane(outputArea,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED) {
            @Override
            public void setBounds(int x, int y, int w, int h) {
                super.setBounds(x, y, w, h);

                if (outputArea != null) {
                    int pos = outputArea.getCaretPosition();
                    outputArea.setStyledDocument(outputArea.getStyledDocument());
                    outputArea.setCaretPosition(pos);
                }
            }
        };
        outputScroll.setThumbColor(CyderColors.regularPink);
        outputScroll.getViewport().setOpaque(false);
        outputScroll.setOpaque(false);
        outputScroll.setFocusable(true);
        outputScroll.setBorder(UserUtil.getCyderUser().getOutputborder().equals("1")
                ? new LineBorder(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()),
                FIELD_BORDER_THICKNESS, false)
                : BorderFactory.createEmptyBorder());

        if (UserUtil.getCyderUser().getOutputfill().equals("1")) {
            outputArea.setOpaque(true);
            outputArea.setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
            outputArea.repaint();
            outputArea.revalidate();
        }

        consoleCyderFrame.getContentPane().add(outputScroll);
    }

    /**
     * Sets up the input field and adds it to the console.
     */
    private void installInputField() {
        inputField = new JPasswordField(40);

        inputField.setEchoChar((char) 0);
        inputField.setText(consoleBashString);
        inputField.setBorder(UserUtil.getCyderUser().getInputborder().equals("1")
                ? new LineBorder(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()),
                FIELD_BORDER_THICKNESS, false)
                : BorderFactory.createEmptyBorder());
        inputField.setSelectionColor(CyderColors.selectionColor);
        inputField.setCaretPosition(inputField.getPassword().length);

        inputField.setOpaque(false);
        inputField.setCaretColor(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
        inputField.setCaret(new CyderCaret(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground())));
        inputField.setForeground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
        inputField.setFont(INSTANCE.generateUserFont());

        installInputFieldListeners();

        if (UserUtil.getCyderUser().getInputfill().equals("1")) {
            inputField.setOpaque(true);
            inputField.setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
            inputField.repaint();
            inputField.revalidate();
        }

        consoleCyderFrame.getContentPane().add(inputField);
    }

    /**
     * Sets up the input map to allow the drag label buttons to be triggered via the enter key.
     */
    private void setupButtonEnterInputMap() {
        InputMap inputMap = (InputMap) UIManager.get(BUTTON_INPUT_FOCUS_MAP_KEY);
        inputMap.put(KeyStroke.getKeyStroke(ENTER), PRESSED);
        inputMap.put(KeyStroke.getKeyStroke(RELEASED_ENTER), RELEASED);
    }

    /**
     * Sets up the console position based on the saved stats from the previous session.
     *
     * @param consoleIcon the console icon record to get the size from
     */
    private void restorePreviousFrameBounds(ConsoleIcon consoleIcon) {
        ScreenStat requestedConsoleStats = UserUtil.getCyderUser().getScreenStat();

        consoleCyderFrame.setAlwaysOnTop(requestedConsoleStats.isConsoleOnTop());

        int requestedConsoleWidth = requestedConsoleStats.getConsoleWidth();
        int requestedConsoleHeight = requestedConsoleStats.getConsoleHeight();

        if (requestedConsoleWidth <= consoleIcon.dimension().width
                && requestedConsoleHeight <= consoleIcon.dimension().height
                && requestedConsoleWidth >= MINIMUM_SIZE.width
                && requestedConsoleHeight >= MINIMUM_SIZE.height) {
            consoleCyderFrame.setSize(requestedConsoleWidth, requestedConsoleHeight);
            consoleCyderFrame.refreshBackground();
        }

        consoleDir = requestedConsoleStats.getConsoleDirection();

        revalidate(true, false, true);

        FrameUtil.requestFramePosition(
                requestedConsoleStats.getMonitor(),
                requestedConsoleStats.getConsoleX(),
                requestedConsoleStats.getConsoleY(),
                consoleCyderFrame);

        consoleCyderFrame.setVisible(true);
    }

    /**
     * Sets up the drag label button lists for all the console's drag labels.
     */
    private void installDragLabelButtons() {
        menuButton = new CyderIconButton(
                "Menu", CyderIcons.menuIcon, CyderIcons.menuIconHover,
                menuButtonMouseListener, menuButtonFocusAdapter);
        menuButton.addActionListener(menuButtonActionListener);
        menuButton.setBounds(4, 4, 22, 22);
        consoleCyderFrame.getTopDragLabel().add(menuButton);
        menuButton.addKeyListener(menuButtonKeyAdapter);

        helpButton.addActionListener(helpButtonActionListener);
        helpButton.setBounds(32, 4, 22, 22);
        consoleCyderFrame.getTopDragLabel().add(helpButton);

        LinkedList<JButton> consoleDragButtonList = new LinkedList<>();

        toggleAudioControls.addActionListener(e -> {
            if (audioControlsLabel.isVisible()) {
                animateOutAudioControls();
            } else {
                animateInAudioControls();
            }
        });
        toggleAudioControls.setVisible(false);
        consoleDragButtonList.add(toggleAudioControls);

        minimizeButton.addActionListener(e -> {
            consoleCyderFrame.setRestoreX(consoleCyderFrame.getX());
            consoleCyderFrame.setRestoreY(consoleCyderFrame.getY());
            consoleCyderFrame.minimizeAnimation();
        });
        consoleDragButtonList.add(minimizeButton);

        pinButton.addActionListener(e -> {
            consoleCyderFrame.setAlwaysOnTop(!consoleCyderFrame.isAlwaysOnTop());
            pinButton.setIcon(consoleCyderFrame.isAlwaysOnTop() ? CyderIcons.pinIconHover : CyderIcons.pinIcon);
            pinButton.setToolTipText(consoleCyderFrame.isAlwaysOnTop() ? "Unpin" : "Pin");
            saveScreenStat();
        });
        pinButton.setIcon(UserUtil.getCyderUser().getScreenStat().isConsoleOnTop()
                ? CyderIcons.pinIconHover : CyderIcons.pinIcon);
        consoleDragButtonList.add(pinButton);

        alternateBackgroundButton.addActionListener(e -> {
            loadBackgrounds();

            try {
                if (canSwitchBackground()) {
                    switchBackground();
                } else if (getBackgrounds().size() == 1) {
                    consoleCyderFrame.notify(new CyderFrame.NotificationBuilder(
                            "You only have one background image. Try adding more via the user editor")
                            .setViewDuration(5000)
                            .setOnKillAction(() -> UserEditor.showGui(0)));
                }
            } catch (Exception ex) {
                consoleCyderFrame.notify("Error in parsing background; perhaps it was deleted.");
                Logger.log(Logger.Tag.EXCEPTION, "Background DNE");
            }
        });
        consoleDragButtonList.add(alternateBackgroundButton);

        closeButton.addActionListener(e -> {
            if (UserUtil.getCyderUser().getMinimizeonclose().equals("1")) {
                FrameUtil.minimizeAllFrames();
            } else {
                closeFrame(true, false);
            }
        });
        consoleDragButtonList.add(closeButton);

        consoleCyderFrame.getTopDragLabel().setButtonList(consoleDragButtonList);
        consoleCyderFrame.getBottomDragLabel().setButtonList(null);
        consoleCyderFrame.getLeftDragLabel().setButtonList(null);
        consoleCyderFrame.getRightDragLabel().setButtonList(null);
    }

    /**
     * The action listener for the drag label help button.
     */
    private final ActionListener helpButtonActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            CyderThreadRunner.submit(() -> {
                String space = StringUtil.generateNSpaces(4);
                CyderButton suggestionButton = new CyderButton(space + "Make a Suggestion" + space);
                suggestionButton.setColors(CyderColors.regularPink);
                suggestionButton.addActionListener(ex -> CyderThreadRunner.submit(() -> {
                    String suggestion = GetterUtil.getInstance().getString(new GetterUtil.Builder("Suggestion")
                            .setRelativeTo(consoleCyderFrame)
                            .setFieldTooltip("Suggestion")
                            .setSubmitButtonColor(CyderColors.regularPink));

                    if (!StringUtil.isNull(suggestion)) {
                        Logger.log(Logger.Tag.SUGGESTION, suggestion.trim());
                        baseInputHandler.println("Suggestion logged");
                    }
                }, "Suggestion Getter Waiter Thread"));

                baseInputHandler.println(suggestionButton);
            }, "Suggestion Getter Waiter Thread");
        }
    };

    /**
     * Sets up and adds the console clock to the top drag label.
     */
    private void installConsoleClock() {
        consoleClockLabel = new JLabel(TimeUtil.userFormattedTime(), SwingConstants.CENTER);
        consoleClockLabel.setSize(0, StringUtil.getAbsoluteMinHeight("143", CONSOLE_CLOCK_FONT));
        consoleClockLabel.setFont(CONSOLE_CLOCK_FONT);
        consoleClockLabel.setForeground(CyderColors.vanilla);
        consoleCyderFrame.getTopDragLabel().add(consoleClockLabel);
        consoleClockLabel.setFocusable(false);
        consoleClockLabel.setVisible(true);
    }

    /**
     * The key used for the debug lines abstract action.
     */
    private final String DEBUG_LINES = "debug_lines";

    /**
     * The key used for the forced exit abstract action.
     */
    private final String FORCED_EXIT = "forced_exit";

    /**
     * Installs all the input field listeners.
     */
    private void installInputFieldListeners() {
        inputField.addKeyListener(inputFieldKeyAdapter);
        inputField.addKeyListener(commandScrolling);

        inputField.addMouseWheelListener(fontSizerListener);
        inputField.addActionListener(inputFieldActionListener);

        inputField.addFocusListener(inputFieldFocusAdapter);

        inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), DEBUG_LINES);
        inputField.getActionMap().put(DEBUG_LINES, debugLinesAbstractAction);

        inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK), FORCED_EXIT);
        inputField.getActionMap().put(FORCED_EXIT, forcedExitAbstractAction);
    }

    /**
     * Removes all the input field listeners
     */
    private void uninstallInputFieldListeners() {
        inputField.removeKeyListener(inputFieldKeyAdapter);
        inputField.removeKeyListener(commandScrolling);

        inputField.removeFocusListener(inputFieldFocusAdapter);

        inputField.removeMouseWheelListener(fontSizerListener);
        inputField.removeActionListener(inputFieldActionListener);

        inputField.getActionMap().remove(DEBUG_LINES);
        inputField.getActionMap().remove(FORCED_EXIT);
    }

    /**
     * Installs all the output area listeners.
     */
    private void installOutputAreaListeners() {
        outputArea.addFocusListener(outputAreaFocusAdapter);
        outputArea.addMouseWheelListener(fontSizerListener);
    }

    /**
     * Removes all the output area listeners.
     */
    private void uninstallOutputAreaListeners() {
        outputArea.removeFocusListener(outputAreaFocusAdapter);
        outputArea.removeMouseWheelListener(fontSizerListener);
    }

    /**
     * Sets up resizing for the console.
     */
    private void installConsoleResizing() {
        consoleCyderFrame.initializeResizing();
        consoleCyderFrame.setResizable(true);
        consoleCyderFrame.setBackgroundResizing(true);
        consoleCyderFrame.setMinimumSize(MINIMUM_SIZE);

        refreshConsoleMaxSize();
    }

    /**
     * Refreshes the maximum size of the console.
     */
    private void refreshConsoleMaxSize() {
        if (getCurrentBackground().referenceFile() != null) {
            ImageIcon tmpImageIcon = getCurrentBackground().generateImageIcon();
            int w = tmpImageIcon.getIconWidth();
            int h = tmpImageIcon.getIconHeight();

            if (getConsoleDirection() == Direction.RIGHT || getConsoleDirection() == Direction.LEFT) {
                consoleCyderFrame.setMaximumSize(new Dimension(h, w));
            } else {
                consoleCyderFrame.setMaximumSize(new Dimension(w, h));
            }
        } else {
            consoleCyderFrame.setMaximumSize(consoleCyderFrame.getSize());
        }
    }

    /**
     * The window adapter for window iconification/de-iconification actions.
     */
    private final WindowAdapter consoleWindowAdapter = new WindowAdapter() {
        @Override
        public void windowDeiconified(WindowEvent e) {
            inputField.requestFocus();
            inputField.setCaretPosition(inputField.getPassword().length);
        }

        @Override
        public void windowOpened(WindowEvent e) {
            inputField.requestFocus();
            inputField.setCaretPosition(inputField.getPassword().length);
            onLaunch();
        }
    };

    /**
     * Begins the console checker executors/threads.
     */
    private void startExecutors() {
        CyderThreadRunner.submit(() -> {
            try {
                int lastChimeHour = -1;

                while (true) {
                    if (!isClosed()) {
                        int min = LocalDateTime.now().getMinute();
                        int sec = LocalDateTime.now().getSecond();

                        // if at hh:00:00 and we haven't chimed for this hour yet
                        if (min == 0 && sec == 0 && lastChimeHour != LocalDateTime.now().getHour()) {
                            if (UserUtil.getCyderUser().getHourlychimes().equals("1")) {
                                IOUtil.playSystemAudio("static/audio/chime.mp3");
                                lastChimeHour = LocalDateTime.now().getHour();
                            }
                        }

                        ThreadUtil.sleep(50);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.HourlyChimeChecker.getName());

        CyderThreadRunner.submit(() -> {
            OUTER:
            while (true) {
                if (!isClosed()) {
                    try {
                        refreshClockText();

                        // sleep 200 ms
                        int i = 0;
                        while (i < 200) {
                            ThreadUtil.sleep(50);
                            if (consoleClosed) {
                                break OUTER;
                            }
                            i += 50;
                        }
                    } catch (Exception e) {
                        // sometimes this throws for no reason trying to get times or something so log quietly
                        ExceptionHandler.silentHandle(e);
                    }
                }
            }
        }, IgnoreThread.ConsoleClockUpdater.getName());

        CyderThreadRunner.submit(() -> {
            try {
                OUTER:
                while (true) {
                    if (!isClosed() && UserUtil.getCyderUser().getShowbusyicon().equals("1")) {
                        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                        int num = threadGroup.activeCount();
                        Thread[] printThreads = new Thread[num];
                        threadGroup.enumerate(printThreads);

                        int busyThreads = 0;

                        for (int i = 0 ; i < num ; i++) {
                            boolean contains = false;

                            for (IgnoreThread ignoreThread : IgnoreThread.values()) {
                                if (ignoreThread.getName().equalsIgnoreCase(printThreads[i].getName())) {
                                    contains = true;
                                    break;
                                }
                            }

                            if (!printThreads[i].isDaemon() && !contains) {
                                busyThreads++;
                            }
                        }

                        if (busyThreads == 0 && CyderIcons.getCurrentCyderIcon() != CyderIcons.X_ICON) {
                            CyderIcons.setCurrentCyderIcon(CyderIcons.CYDER_ICON);
                        } else if (CyderIcons.getCurrentCyderIcon() != CyderIcons.X_ICON) {
                            CyderIcons.setCurrentCyderIcon(CyderIcons.CYDER_BUSY_ICON);
                        }
                    } else if (CyderIcons.getCurrentCyderIcon() != CyderIcons.X_ICON) {
                        CyderIcons.setCurrentCyderIcon(CyderIcons.CYDER_ICON);
                    }

                    consoleCyderFrame.setIconImage(CyderIcons.getCurrentCyderIcon().getImage());

                    //sleep 3 seconds
                    int i = 0;
                    while (i < 3000) {
                        ThreadUtil.sleep(50);
                        if (consoleClosed) {
                            break OUTER;
                        }
                        i += 50;
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                CyderIcons.setCurrentCyderIcon(CyderIcons.CYDER_ICON);
                consoleCyderFrame.setIconImage(CyderIcons.getCurrentCyderIcon().getImage());
            }
        }, IgnoreThread.CyderBusyChecker.getName());

        CyderThreadRunner.submit(() -> {
            int setDelay = 3000;

            try {
                // initial delay
                ThreadUtil.sleep(setDelay);

                OUTER:
                while (true) {
                    saveScreenStat();

                    int i = 0;
                    while (i < setDelay) {
                        ThreadUtil.sleep(50);
                        if (consoleClosed) {
                            break OUTER;
                        }
                        i += 50;
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.ConsoleDataSaver.getName());
    }

    /**
     * Performs special actions on the console start such as special day events,
     * debug properties, determining the user's last start time, auto testing, etc.
     */
    private void onLaunch() {
        if (TimeUtil.isChristmas()) {
            consoleCyderFrame.notify("Merry Christmas!");
            ReflectionUtil.invokeCardWidget("Christmas", TimeUtil.getYear());
        }

        if (TimeUtil.isHalloween()) {
            consoleCyderFrame.notify("Happy Halloween!");
            ReflectionUtil.invokeCardWidget("Halloween", TimeUtil.getYear());
        }

        if (TimeUtil.isIndependenceDay()) {
            consoleCyderFrame.notify("Happy 4th of July!");
            ReflectionUtil.invokeCardWidget("Independence", TimeUtil.getYear());
        }

        if (TimeUtil.isThanksgiving()) {
            consoleCyderFrame.notify("Happy Thanksgiving!");
            ReflectionUtil.invokeCardWidget("Thanksgiving", TimeUtil.getYear());
        }

        if (TimeUtil.isAprilFoolsDay()) {
            consoleCyderFrame.notify("Happy April Fools Day!");
            ReflectionUtil.invokeCardWidget("AprilFools", TimeUtil.getYear());
        }

        if (TimeUtil.isValentinesDay()) {
            consoleCyderFrame.notify("Happy Valentines Day!");
        }

        if (TimeUtil.isPiDay()) {
            consoleCyderFrame.notify("Happy Pi day!");
        }

        if (TimeUtil.isEaster()) {
            consoleCyderFrame.notify("Happy Easter!");
        }

        if (TimeUtil.isDeveloperBirthday()) {
            Console.INSTANCE.getInputHandler().println("Thanks for creating me Nate :,)");
        }

        if (UserUtil.getCyderUser().getDebugwindows().equals("1")) {
            CyderThreadRunner.submit(() -> {
                try {
                    for (String prop : StatUtil.getSystemProperties()) {
                        getInputHandler().println(prop);
                    }

                    for (String prop : StatUtil.getComputerMemorySpaces()) {
                        getInputHandler().println(prop);
                    }

                    for (String prop : StatUtil.getJavaProperties()) {
                        getInputHandler().println(prop);
                    }

                    Future<StatUtil.DebugStats> futureStats = StatUtil.getDebugProps();

                    while (!futureStats.isDone()) {
                        Thread.onSpinWait();
                    }

                    StatUtil.DebugStats stats = futureStats.get();

                    for (String line : stats.lines()) {
                        getInputHandler().println(line);
                    }

                    getInputHandler().println(stats.countryFlag());
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Debug Stat Finder");
        }

        if (PropLoader.getBoolean("testing_mode")) {
            Logger.log(Logger.Tag.CONSOLE_LOAD, "[" + OSUtil.getOsUsername() + "] [TESTING MODE]");
            ManualTests.launchTests();
        }

        if (TimeUtil.millisToDays(System.currentTimeMillis() -
                Long.parseLong(UserUtil.getCyderUser().getLaststart())) > 1) {
            consoleCyderFrame.notify("Welcome back, " + UserUtil.getCyderUser().getName() + "!");
        }

        UserUtil.getCyderUser().setLaststart(String.valueOf(System.currentTimeMillis()));

        introMusicCheck();
    }

    /**
     * Determines what audio to play at the beginning of the Console startup.
     */
    private void introMusicCheck() {
        //if the user wants some custom intro music
        if (UserUtil.getCyderUser().getIntromusic().equalsIgnoreCase("1")) {
            ArrayList<File> musicList = new ArrayList<>();

            File userMusicDir = new File(OSUtil.buildPath(
                    Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                    INSTANCE.getUuid(), UserFile.MUSIC.getName()));

            File[] files = userMusicDir.listFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (FileUtil.isSupportedAudioExtension(file)) {
                        musicList.add(file);
                    }
                }
            }

            //if they have music then play their own
            if (!musicList.isEmpty()) {
                IOUtil.playGeneralAudio(files[NumberUtil.randInt(0, files.length - 1)].getAbsolutePath());
            }
            // otherwise, play our own
            else {
                IOUtil.playGeneralAudio(OSUtil.buildPath("static", "audio", "ride.mp3"));
            }
        }
        // intro music not on, check for grayscale image
        else if (PropLoader.getBoolean("released")) {
            try {
                CyderThreadRunner.submit(() -> {
                    try {
                        Image icon = new ImageIcon(ImageIO.read(getCurrentBackground().referenceFile())).getImage();

                        int w = icon.getWidth(null);
                        int h = icon.getHeight(null);

                        int[] pixels = new int[w * h];
                        PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);
                        pg.grabPixels();

                        boolean grayscale = true;
                        for (int pixel : pixels) {
                            Color color = new Color(pixel);
                            if (color.getRed() != color.getGreen() || color.getRed() != color.getBlue()) {
                                grayscale = false;
                                break;
                            }
                        }

                        if (grayscale) {
                            IOUtil.playGeneralAudio(GRAYSCALE_AUDIO_PATHS.get(
                                    NumberUtil.randInt(0, GRAYSCALE_AUDIO_PATHS.size() - 1)));
                        } else {
                            IOUtil.playGeneralAudio(OSUtil.buildPath("static","audio","introtheme.mp3"));
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }, "Intro Music Checker");
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    /**
     * Whether debug lines should be drawn.
     */
    private boolean debugLines = false;

    /**
     * The action to allow debug lines to be drawn across all frames via the console.
     */
    private final AbstractAction debugLinesAbstractAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            debugLines = !debugLines;

            for (CyderFrame frame : FrameUtil.getCyderFrames()) {
                frame.drawDebugLines(debugLines);
            }
        }
    };

    /**
     * The action to allow Cyder to close when alt + F4 are pressed in combination.
     */
    private final AbstractAction forcedExitAbstractAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            OSUtil.exit(ExitCondition.ForcedImmediateExit);
        }
    };

    /**
     * The focus adapter for the output area field.
     */
    private final FocusAdapter outputAreaFocusAdapter = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            outputScroll.setBorder(new LineBorder(ColorUtil.hexStringToColor(
                    UserUtil.getCyderUser().getBackground()), 3));
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (UserUtil.getCyderUser().getOutputborder().equals("0"))
                outputScroll.setBorder(BorderFactory.createEmptyBorder());

            inputField.requestFocusInWindow();
            inputField.setCaretPosition(inputField.getPassword().length);
        }
    };

    /**
     * The focus adapter for the input field.
     */
    private final FocusAdapter inputFieldFocusAdapter = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            super.focusLost(e);
            currentFocusedMenuItemIndex = 0;
        }
    };

    /**
     * The index of the current focused menu item index.
     */
    private int currentFocusedMenuItemIndex = -1;

    /**
     * Focuses the next menu item.
     */
    private void focusNextTaskbarMenuItem() {
        focusTaskbarMenuItem(currentFocusedMenuItemIndex + 1);
    }

    /**
     * Focuses the last last menu item
     */
    private void focusPreviousTaskbarMenuItem() {
        focusTaskbarMenuItem(currentFocusedMenuItemIndex - 1);
    }

    /**
     * Removes focus from the previous focused taskbar menu item and focuses the one at the requested index if valid.
     *
     * @param index the index of the taskbar menu item to focus
     */
    private void focusTaskbarMenuItem(int index) {
        ImmutableList<TaskbarIcon> state = ImmutableList.copyOf(
                Stream.of(currentFrameMenuItems, currentMappedExeItems, currentDefaultMenuItems)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));

        if (state.size() == 0) {
            return;
        }

        // remove focus from previous item if possible
        if (currentFocusedMenuItemIndex != -1) {
            state.get(currentFocusedMenuItemIndex).getBuilder().setFocused(false);
        }

        // wrap around logic
        if (index < 0) {
            index = state.size() - 1;
        } else if (index > state.size() - 1) {
            index = 0;
        }

        // give focus to new item
        currentFocusedMenuItemIndex = index;
        state.get(currentFocusedMenuItemIndex).getBuilder().setFocused(true);
        reinstallCurrentTaskbarIcons();
    }

    /**
     * Removes focus from any and task menu taskbar items
     */
    private void removeFocusFromTaskbarMenuIcons() {
        currentFocusedMenuItemIndex = -1;

        if (menuLabel == null) {
            return;
        }

        ImmutableList<TaskbarIcon> state = ImmutableList.copyOf(
                Stream.of(currentFrameMenuItems, currentMappedExeItems, currentDefaultMenuItems)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));

        for (TaskbarIcon icon : state) {
            icon.getBuilder().setFocused(false);
        }

        if (menuLabel.isVisible()) {
            reinstallCurrentTaskbarIcons();
        }
    }

    /**
     * The action listener for the menu button.
     */
    private final ActionListener menuButtonActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Point menuPoint = menuButton.getLocationOnScreen();
            boolean mouseTriggered = MathUtil.pointInOrOnRectangle(MouseInfo.getPointerInfo().getLocation(),
                    new Rectangle((int) menuPoint.getX(), (int) menuPoint.getY(), menuButton.getWidth(),
                            menuButton.getHeight()));

            // if there's a focused item and it wasn't a mouse click
            if (currentFocusedMenuItemIndex != -1 && !mouseTriggered) {
                ImmutableList.copyOf(Stream.of(currentFrameMenuItems,
                                        currentMappedExeItems, currentDefaultMenuItems)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()))
                        .get(currentFocusedMenuItemIndex).runRunnable();
                return;
            }

            if (menuLabel == null) {
                generateConsoleMenu();
            }

            if (!menuLabel.isVisible()) {
                CyderThreadRunner.submit(() -> {
                    menuLabel.setLocation(-150, CyderDragLabel.DEFAULT_HEIGHT - 2);
                    int y = menuLabel.getY();

                    for (int i = -150 ; i < 2 ; i += 8) {
                        menuLabel.setLocation(i, y);
                        ThreadUtil.sleep(10);
                    }

                    menuLabel.setLocation(2, y);

                    revalidateInputAndOutputBounds();
                }, "minimize menu thread");

                CyderThreadRunner.submit(() -> {
                    if (menuLabel == null) {
                        generateConsoleMenu();
                    }

                    menuLabel.setLocation(-150, CyderDragLabel.DEFAULT_HEIGHT - 2);
                    menuLabel.setVisible(true);

                    int addX = 0;

                    if (menuLabel.isVisible())
                        addX = 2 + menuLabel.getWidth();

                    int finalAddX = addX;

                    for (int i = inputField.getX() ; i < finalAddX + 15 ; i += 8) {
                        outputScroll.setBounds(i, outputScroll.getY(), outputScroll.getWidth() + 1,
                                outputScroll.getHeight());
                        inputField.setBounds(i, inputField.getY(), inputField.getWidth() + 1, inputField.getHeight());

                        ThreadUtil.sleep(10);
                    }

                    revalidateInputAndOutputBounds();
                }, "Console menu animator");
            } else {
                minimizeMenu();
            }
        }
    };

    /**
     * The mouse listener for the menu button.
     */
    private final MouseListener menuButtonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            menuButton.setIcon(CyderIcons.menuIconHover);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            menuButton.setIcon(CyderIcons.menuIcon);
        }
    };

    /**
     * The focus adapter for the menu button.
     */
    private final FocusAdapter menuButtonFocusAdapter = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            menuButton.setIcon(CyderIcons.menuIcon);
            removeFocusFromTaskbarMenuIcons();
        }

        @Override
        public void focusGained(FocusEvent e) {
            menuButton.setIcon(CyderIcons.menuIconHover);
            removeFocusFromTaskbarMenuIcons();
        }
    };

    /**
     * The key adapter for the menu button to allow "focusing" taskbar items.
     */
    private final KeyAdapter menuButtonKeyAdapter = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();

            if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_RIGHT) {
                focusNextTaskbarMenuItem();
            } else if (code == KeyEvent.VK_UP || code == KeyEvent.VK_LEFT) {
                focusPreviousTaskbarMenuItem();
            }
        }
    };

    /**
     * The current active frames to generate TaskbarIcons for the console's menu.
     */
    private final LinkedList<CyderFrame> currentActiveFrames = new LinkedList<>();

    /**
     * The alignment object used for menu alignment.
     */
    private final SimpleAttributeSet alignment = new SimpleAttributeSet();

    /**
     * The current taskbar menu frame items.
     */
    private ImmutableList<TaskbarIcon> currentFrameMenuItems = ImmutableList.of();

    /**
     * The current taskbar menu mapped exe items.
     */
    private ImmutableList<TaskbarIcon> currentMappedExeItems = ImmutableList.of();

    /**
     * The current taskbar default menu items.
     */
    private ImmutableList<TaskbarIcon> currentDefaultMenuItems = ImmutableList.of();

    /**
     * Refreshes the taskbar icons based on the frames currently in the frame list.
     */
    private synchronized void installMenuTaskbarIcons() {
        boolean compactMode = UserUtil.getCyderUser().getCompactTextMode().equals("1");

        StyleConstants.setAlignment(alignment, compactMode
                ? StyleConstants.ALIGN_LEFT : StyleConstants.ALIGN_CENTER);

        StyledDocument doc = menuPane.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), alignment, false);

        ImmutableList<TaskbarIcon> frameMenuItems = getCurrentFrameTaskbarIcons(compactMode);
        ImmutableList<TaskbarIcon> mappedExeItems = getMappedExeTaskbarIcons(compactMode);
        ImmutableList<TaskbarIcon> defaultMenuItems = getDefaultTaskbarIcons(compactMode);

        if (!differentMenuState(frameMenuItems, mappedExeItems, defaultMenuItems)) {
            return;
        }

        currentFrameMenuItems = frameMenuItems;
        currentMappedExeItems = mappedExeItems;
        currentDefaultMenuItems = defaultMenuItems;

        reinstallCurrentTaskbarIcons();
    }

    /**
     * Returns the current frame taskbar icon items.
     *
     * @param compactMode whether the menu should be laid out in compact mode
     * @return the current frame taskbar icon items
     */
    private synchronized ImmutableList<TaskbarIcon> getCurrentFrameTaskbarIcons(boolean compactMode) {
        LinkedList<TaskbarIcon> ret = new LinkedList<>();

        if (!currentActiveFrames.isEmpty()) {
            for (CyderFrame currentFrame : Lists.reverse(currentActiveFrames)) {
                ret.add(new TaskbarIcon.Builder()
                        .setName(currentFrame.getTitle())
                        .setCompact(compactMode)
                        .setFocused(false)
                        .setBorderColor(currentFrame.getTaskbarIconBorderColor())
                        .setCustomIcon(currentFrame.getCustomTaskbarIcon())
                        .setRunnable(FrameUtil.generateCommonFrameTaskbarIconRunnable(currentFrame))
                        .build());
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the mapped exe taskbar icon items.
     *
     * @param compactMode whether the menu should be laid out in compact mode
     * @return the current mapped exe taskbar icon items
     */
    private ImmutableList<TaskbarIcon> getMappedExeTaskbarIcons(boolean compactMode) {
        LinkedList<MappedExecutable> exes = UserUtil.getCyderUser().getExecutables();
        LinkedList<TaskbarIcon> ret = new LinkedList<>();

        if (!exes.isEmpty()) {
            for (MappedExecutable exe : exes) {
                Runnable runnable = () -> {
                    IOUtil.openOutsideProgram(exe.getFilepath());
                    exe.displayInvokedNotification();
                };

                ret.add(new TaskbarIcon.Builder()
                        .setName(exe.getName())
                        .setFocused(false)
                        .setCompact(compactMode)
                        .setRunnable(runnable)
                        .setBorderColor(CyderColors.vanilla)
                        .build());
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * The default compact taskbar icons.
     */
    private final ImmutableList<TaskbarIcon> compactDefaultTaskbarIcons = ImmutableList.of(
            new TaskbarIcon.Builder()
                    .setName("Prefs")
                    .setFocused(false)
                    .setCompact(true)
                    .setRunnable(() -> UserEditor.showGui(0))
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build(),
            new TaskbarIcon.Builder()
                    .setName("Logout")
                    .setFocused(false)
                    .setCompact(true)
                    .setRunnable(this::logout)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build());

    /**
     * The default non compact taskbar icons.
     */
    private final ImmutableList<TaskbarIcon> nonCompactDefaultTaskbarIcons = ImmutableList.of(
            new TaskbarIcon.Builder()
                    .setName("Prefs")
                    .setFocused(false)
                    .setCompact(false)
                    .setRunnable(() -> UserEditor.showGui(0))
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build(),
            new TaskbarIcon.Builder()
                    .setName("Logout")
                    .setFocused(false)
                    .setCompact(false)
                    .setRunnable(this::logout)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build());

    /**
     * Returns the default taskbar icon items.
     *
     * @param compactMode whether the menu should be laid out in compact mode
     * @return the default taskbar icon items
     */
    private ImmutableList<TaskbarIcon> getDefaultTaskbarIcons(boolean compactMode) {
        return compactMode ? compactDefaultTaskbarIcons : nonCompactDefaultTaskbarIcons;
    }

    /**
     * Returns whether the provided new taskbar icons comprehensive list is different than the previous menu state.
     *
     * @param frameMenuItems   the new proposed taskbar frame items
     * @param mappedExeItems   the new proposed taskbar mapped exe items
     * @param defaultMenuItems the new proposed taskbar default menu items
     * @return whether the provided new taskbar icons comprehensive list is different than the previous menu state
     */
    private synchronized boolean differentMenuState(
            ImmutableList<TaskbarIcon> frameMenuItems, ImmutableList<TaskbarIcon> mappedExeItems,
            ImmutableList<TaskbarIcon> defaultMenuItems) {

        ImmutableList<TaskbarIcon> newState = ImmutableList.copyOf(
                Stream.of(frameMenuItems, mappedExeItems, defaultMenuItems)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));

        ImmutableList<TaskbarIcon> previousState = ImmutableList.copyOf(
                Stream.of(currentFrameMenuItems, currentMappedExeItems, currentDefaultMenuItems)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));

        if (previousState.size() == 0) {
            return true;
        }

        if (newState.size() != previousState.size()) {
            return true;
        }

        for (int i = 0 ; i < newState.size() ; i++) {
            if (!newState.get(i).equals(previousState.get(i))) {
                return true;
            }
        }

        return false;
    }

    /**
     * The listener used when input is first handled.
     */
    private final ActionListener inputFieldActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String op = String.valueOf(inputField.getPassword())
                    .substring(consoleBashString.length())
                    .trim().replace(consoleBashString, "");

            if (!StringUtil.isNull(op)) {
                // add op unless last thing
                if (commandList.isEmpty() || !commandList.get(commandList.size() - 1).equals(op)) {
                    commandList.add(op);
                }

                commandIndex = commandList.size();
                baseInputHandler.handle(op, true);
            }

            inputField.setText(consoleBashString);
            inputField.setCaretPosition(consoleBashString.length());
        }
    };

    /**
     * Revalidates the taskbar menu bounds and re-installs the icons.
     */
    private void generateConsoleMenu() {
        int menuHeight = consoleCyderFrame.getHeight() - CyderDragLabel.DEFAULT_HEIGHT - 5;

        if (menuLabel != null) {
            menuLabel.setVisible(false);
        }

        menuLabel = new JLabel();
        menuLabel.setBounds(-TASKBAR_MENU_WIDTH, CyderDragLabel.DEFAULT_HEIGHT - 2, TASKBAR_MENU_WIDTH, menuHeight);
        menuLabel.setOpaque(true);
        menuLabel.setBackground(CyderColors.getGuiThemeColor());
        menuLabel.setFocusable(false);
        menuLabel.setVisible(false);
        menuLabel.setBorder(new LineBorder(Color.black, 5));
        consoleCyderFrame.getIconPane().add(menuLabel, JLayeredPane.MODAL_LAYER);

        menuPane = new JTextPane();
        menuPane.setEditable(false);
        menuPane.setAutoscrolls(false);
        menuPane.setFocusable(false);
        menuPane.setOpaque(false);
        menuPane.setBackground(CyderColors.getGuiThemeColor());

        menuScroll = new CyderScrollPane(menuPane);
        menuScroll.setThumbSize(5);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setFocusable(false);
        menuScroll.setOpaque(false);
        menuScroll.setThumbColor(CyderColors.regularPink);
        menuScroll.setBackground(CyderColors.getGuiThemeColor());
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        menuScroll.setBounds(7, 10, menuLabel.getWidth() - 10, menuHeight - 20);
        menuLabel.add(menuScroll);

        installMenuTaskbarIcons();
    }

    /**
     * Clears the taskbar menu pane and re-prints the current taskbar icons.
     */
    private void reinstallCurrentTaskbarIcons() {
        boolean compactMode = UserUtil.getCyderUser().getCompactTextMode().equals("1");
        StringUtil printingUtil = new StringUtil(new CyderOutputPane(menuPane));

        menuPane.setText("");

        printingUtil.newline(!compactMode);

        for (TaskbarIcon frameItem : currentFrameMenuItems) {
            frameItem.generateTaskbarIcon();
            printingUtil.printlnComponent(frameItem.getTaskbarIcon());
            printingUtil.newline(!compactMode);
        }

        if (currentFrameMenuItems.size() > 0 && !compactMode) {
            printingUtil.printSeparator();
        }

        for (TaskbarIcon mappedExe : currentMappedExeItems) {
            mappedExe.generateTaskbarIcon();
            printingUtil.printlnComponent(mappedExe.getTaskbarIcon());
            printingUtil.newline(!compactMode);
        }

        if (currentMappedExeItems.size() > 0 && currentFrameMenuItems.size() > 0 && !compactMode) {
            printingUtil.printSeparator();
        }

        for (TaskbarIcon taskbarIcon : currentDefaultMenuItems) {
            taskbarIcon.generateTaskbarIcon();
            printingUtil.printlnComponent(taskbarIcon.getTaskbarIcon());
            printingUtil.newline(!compactMode);
        }

        menuPane.setCaretPosition(0);
    }

    /**
     * Removes the provided frame reference from the taskbar frame list.
     *
     * @param associatedFrame the frame reference to remove from the taskbar frame list
     */
    public void removeTaskbarIcon(CyderFrame associatedFrame) {
        if (currentActiveFrames.contains(associatedFrame)) {
            currentActiveFrames.remove(associatedFrame);
            revalidateMenu();
        }
    }

    /**
     * Adds the provided frame reference to the taskbar frame list and revalidates the taskbar.
     *
     * @param associatedFrame the frame reference to add to the taskbar list
     */
    public void addTaskbarIcon(CyderFrame associatedFrame) {
        if (isClosed() || frameTaskbarExceptions.contains(associatedFrame)) {
            return;
        }

        if (!currentActiveFrames.contains(associatedFrame)) {
            currentActiveFrames.add(associatedFrame);
            revalidateMenu();
        }
    }

    /**
     * Slowly animates the taskbar away.
     */
    private void minimizeMenu() {
        if (menuLabel.isVisible()) {
            CyderThreadRunner.submit(() -> {
                for (int i = inputField.getX() ; i > 15 ; i -= 8) {
                    outputScroll.setBounds(i, outputScroll.getY(), outputScroll.getWidth() + 1,
                            outputScroll.getHeight());
                    inputField.setBounds(i, inputField.getY(), inputField.getWidth() + 1, inputField.getHeight());

                    ThreadUtil.sleep(10);
                }

                revalidateInputAndOutputBounds(true);
            }, "Console menu animator");

            CyderThreadRunner.submit(() -> {
                menuLabel.setLocation(2, CyderDragLabel.DEFAULT_HEIGHT - 2);
                int y = menuLabel.getY();

                for (int i = 0 ; i > -150 ; i -= 8) {
                    menuLabel.setLocation(i, y);
                    ThreadUtil.sleep(10);
                }

                menuLabel.setLocation(-150, y);

                menuLabel.setVisible(false);

                revalidateInputAndOutputBounds();
            }, "minimize menu thread");
        }
    }

    /**
     * The input field key adapter used for thread escaping and console rotation.
     */
    private final KeyAdapter inputFieldKeyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            //escaping
            if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
                baseInputHandler.escapeThreads();
            }

            int caretPosition = outputArea.getCaretPosition();

            // direction switching
            if ((e.getKeyCode() == KeyEvent.VK_DOWN)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0)) {
                setConsoleDirection(Direction.BOTTOM);
                outputArea.setCaretPosition(caretPosition);
            } else if ((e.getKeyCode() == KeyEvent.VK_RIGHT)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0)) {
                setConsoleDirection(Direction.RIGHT);
                outputArea.setCaretPosition(caretPosition);
            } else if ((e.getKeyCode() == KeyEvent.VK_UP)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0)) {
                setConsoleDirection(Direction.TOP);
                outputArea.setCaretPosition(caretPosition);
            } else if ((e.getKeyCode() == KeyEvent.VK_LEFT)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0)) {
                setConsoleDirection(Direction.LEFT);
                outputArea.setCaretPosition(caretPosition);
            }
        }

        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {
            // suppress backspaces that take away from bash string
            if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                String currentText = String.valueOf(inputField.getPassword());
                if (currentText.startsWith(consoleBashString.trim())
                        && currentText.trim().equals(consoleBashString.trim())) {
                    e.consume();
                    inputField.setText(consoleBashString);
                    return;
                }
            }

            // ensure starts with bash string
            if (inputField.getPassword().length < consoleBashString.toCharArray().length) {
                inputField.setText(consoleBashString + String.valueOf(inputField.getPassword()));
                inputField.setCaretPosition(inputField.getPassword().length);
            }

            // if the caret position is before start
            if (inputField.getCaretPosition() < consoleBashString.toCharArray().length) {
                // if missing content, set to bash string and place cursor at end
                if (inputField.getPassword().length < consoleBashString.toCharArray().length) {
                    inputField.setText(consoleBashString);
                    inputField.setCaretPosition(consoleBashString.length());
                }

                // place cursor at start of user-entered string
                inputField.setCaretPosition(consoleBashString.toCharArray().length);
            } else {
                // content is long enough but check for starting with bash string
                String text = new String(inputField.getPassword());

                if (!text.startsWith(consoleBashString)) {
                    inputField.setText(consoleBashString + text.replace(consoleBashString, ""));
                    inputField.setCaretPosition(consoleBashString.length());
                }
            }

            super.keyTyped(e);

            // handles if bash string content was removed
            if (inputField.getPassword().length < consoleBashString.toCharArray().length) {
                inputField.setText(consoleBashString);
                inputField.setCaretPosition(consoleBashString.length());
            }
        }
    };

    /**
     * The key listener for input field to control command scrolling.
     */
    private final KeyListener commandScrolling = new KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent event) {
            int code = event.getKeyCode();

            try {
                // make sure ctrl + alt are not pressed
                if ((event.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0
                        && ((event.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == 0)) {

                    // command scrolling

                    if (code == KeyEvent.VK_UP) {
                        if (commandIndex - 1 >= 0) {
                            commandIndex -= 1;
                            inputField.setText(consoleBashString
                                    + commandList.get(commandIndex).replace(consoleBashString, ""));
                        }
                    } else if (code == KeyEvent.VK_DOWN) {
                        if (commandIndex + 1 < commandList.size()) {
                            commandIndex += 1;
                            inputField.setText(consoleBashString + commandList.get(commandIndex)
                                    .replace(consoleBashString, ""));
                        } else if (commandIndex + 1 == commandList.size()) {
                            commandIndex += 1;
                            inputField.setText(consoleBashString);
                        }
                    }

                    for (int specialCode : SPECIAL_FUNCTION_KEY_CODES) {
                        if (code == specialCode) {
                            int functionKey = (code - KeyEvent.VK_F13 + SPECIAL_FUNCTION_KEY_CODE_OFFSET);
                            baseInputHandler.println("Interesting F" + functionKey + " key");

                            if (functionKey == F_17_KEY_CODE) {
                                IOUtil.playGeneralAudio(OSUtil.buildPath("static", "audio", "f17.mp3"));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    };

    /**
     * The MouseWheelListener used for increasing/decreasing the
     * font size for input field and output area.
     */
    @SuppressWarnings("MagicConstant") // check font metric before using
    private final MouseWheelListener fontSizerListener = e -> {
        if (e.isControlDown()) {
            int size = Integer.parseInt(UserUtil.getCyderUser().getFontsize());
            size += e.getWheelRotation() == -1 ? 1 : -1;

            if (size > Preferences.MAX_FONT_SIZE || size < Preferences.MIN_FONT_SIZE) {
                return;
            }

            try {
                String fontName = UserUtil.getCyderUser().getFont();
                int fontMetric = Integer.parseInt(PropLoader.getString("font_metric"));
                Font newFont = new Font(fontName, fontMetric, size);

                if (NumberUtil.isValidFontMetric(fontMetric)) {
                    inputField.setFont(newFont);
                    outputArea.setFont(newFont);

                    UserUtil.getCyderUser().setFontsize(String.valueOf(size));

                    YoutubeUtil.refreshAllDownloadLabels();
                }
            } catch (Exception ignored) {}
        } else {
            // don't disrupt original function
            outputArea.getParent().dispatchEvent(e);
            inputField.getParent().dispatchEvent(e);
        }
    };

    /**
     * Sets the UUID for this Cyder session. Everything else relies on this being set and not null.
     * Once set, a one time check is performed to fix any possibly corrupted userdata.
     *
     * @param uuid the user uuid that we will use to determine our output dir and other
     *             information specific to this instance of the console
     */
    public void setUuid(String uuid) {
        Preconditions.checkNotNull(uuid);

        previousUuid = this.uuid;
        this.uuid = uuid;

        UserUtil.setCyderUser(uuid);
        UserUtil.logoutAllUsers();
        UserUtil.getCyderUser().setLoggedin("1");
        UserUtil.deleteInvalidBackgrounds(uuid);
    }

    /**
     * Returns the uuid of the current user.
     *
     * @return the uuid of the current user
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Get the desired user font in combination with the set font metric and font size.
     *
     * @return the font to use for the input and output areas
     */
    @SuppressWarnings("MagicConstant") // check font metric before use
    public Font generateUserFont() {
        int metric = Integer.parseInt(PropLoader.getString("font_metric"));

        if (NumberUtil.isValidFontMetric(metric)) {
            return new Font(UserUtil.getCyderUser().getFont(), metric,
                    Integer.parseInt(UserUtil.getCyderUser().getFontsize()));
        } else {
            return new Font(UserUtil.getCyderUser().getFont(), Font.BOLD,
                    Integer.parseInt(UserUtil.getCyderUser().getFontsize()));
        }
    }

    // -----------------
    // background logic
    // -----------------

    public final int MINIMUM_BACKGROUND_WIDTH = 400;
    public final int MINIMUM_BACKGROUND_HEIGHT= 400;

    /**
     * Takes into account the dpi scaling value and checks all the backgrounds in the user's
     * directory against the current monitor's resolution. If any width or height of a background file
     * exceeds the monitor's width or height. We resize until it doesn't. We also check to make sure the background
     * meets our minimum pixel dimension parameters. The old images are automatically resized and replaced with the
     * properly resized and cropped images.
     */
    public void resizeBackgrounds() {
        try {
            int minWidth = MINIMUM_BACKGROUND_WIDTH;
            int minHeight = MINIMUM_BACKGROUND_HEIGHT;
            int maxWidth = ScreenUtil.getScreenWidth();
            int maxHeight = ScreenUtil.getScreenHeight();

            for (ConsoleBackground currentBackground : backgrounds) {
                File currentFile = currentBackground.referenceFile();

                if (!currentBackground.referenceFile().exists()) {
                    backgrounds.remove(currentBackground);
                }

                BufferedImage currentImage = ImageIO.read(currentFile);

                int backgroundWidth = currentImage.getWidth();
                int backgroundHeight = currentImage.getHeight();
                int imageType = currentImage.getType();

                // Inform the user we are changing the size of the image
                boolean resizeNeeded = backgroundWidth > maxWidth
                        || backgroundHeight > maxHeight
                        || backgroundWidth < minWidth
                        || backgroundHeight < minHeight;

                if (resizeNeeded) {
                    InformHandler.inform(new InformHandler.Builder(
                            "Resizing the background image \"" + currentFile.getName() + "\"")
                            .setTitle("System Action"));
                }

                Dimension resizeDimensions = ImageUtil.getImageResizeDimensions(minWidth, minHeight,
                        maxWidth, maxHeight, currentImage);

                int deltaWidth = (int) resizeDimensions.getWidth();
                int deltaHeight = (int) resizeDimensions.getHeight();

                //if the image doesn't need a resize then continue to the next image
                if (deltaWidth == 0 || deltaHeight == 0) {
                    continue;
                }

                ImageIO.write(ImageUtil.resizeImage(currentImage, imageType, deltaWidth, deltaHeight),
                        FileUtil.getExtension(currentFile), currentFile);
            }

            loadBackgrounds();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Initializes the backgrounds associated with the current user.
     * Also attempts to find the background index of the Console current background if it exists.
     */
    public void loadBackgrounds() {
        try {
            ArrayList<File> backgroundFiles = new ArrayList<>();

            File[] backgroundFilesArr = OSUtil.buildFile(
                    Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(),
                    getUuid(),
                    UserFile.BACKGROUNDS.getName()).listFiles();
            if (backgroundFilesArr != null && backgroundFilesArr.length > 0) {
                for (File file : backgroundFilesArr) {
                    if (StringUtil.in(FileUtil.getExtension(file),
                            true, FileUtil.SUPPORTED_IMAGE_EXTENSIONS)) {
                        backgroundFiles.add(file);
                    }
                }
            }

            if (backgroundFiles.isEmpty()) {
                backgroundFiles.add(UserUtil.createDefaultBackground(uuid));
            }

            backgrounds.clear();

            for (File file : backgroundFiles) {
                if (ImageUtil.isValidImage(file)) {
                    backgrounds.add(new ConsoleBackground(file));
                }
            }

            // now we have our wrapped files list

            // find the index we are it if console has a content pane
            revalidateBackgroundIndex();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Reloads the background files and returns the resulting list of found backgrounds.
     *
     * @return list of found backgrounds
     */
    public ArrayList<ConsoleBackground> getBackgrounds() {
        loadBackgrounds();
        return backgrounds;
    }

    /**
     * Revalidates the index that the current background is at after
     * refreshing due to a possible background list change.
     */
    private void revalidateBackgroundIndex() {
        if (consoleCyderFrame != null) {
            JLabel contentLabel = getConsoleCyderFrameContentPane();

            if (contentLabel != null) {
                String filename = contentLabel.getToolTipText();

                if (StringUtil.isNull(filename)) {
                    backgroundIndex = 0;
                    return;
                }

                for (int i = 0 ; i < backgrounds.size() ; i++) {
                    if (FileUtil.getFilename(backgrounds.get(i).referenceFile()).equals(filename)) {
                        backgroundIndex = i;
                        return;
                    }
                }
            }
        }

        // failsafe
        backgroundIndex = 0;
    }

    /**
     * Sets the background to the provided file in the user's backgrounds directory provided it exists.
     *
     * @param backgroundFile the background file to set the console to
     */
    public void setBackgroundFile(File backgroundFile) {
        loadBackgrounds();

        int index = -1;

        for (int i = 0 ; i < backgrounds.size() ; i++) {
            if (backgrounds.get(i).referenceFile().getAbsolutePath().equals(backgroundFile.getAbsolutePath())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            setBackgroundIndex(index);
        } else {
            throw new IllegalArgumentException("Provided file not found in user's backgrounds directory: "
                    + backgroundFile.getAbsolutePath());
        }
    }

    /**
     * Simply sets the background to the provided icon without having a reference file.
     * Please ensure the icon size is the same as the current background's.
     *
     * @param icon the icon to set to the background of the console
     */
    public void setBackground(ImageIcon icon) {
        Preconditions.checkNotNull(icon);
        Preconditions.checkArgument(icon.getIconWidth() == consoleCyderFrame.getWidth());
        Preconditions.checkArgument(icon.getIconHeight() == consoleCyderFrame.getHeight());

        consoleCyderFrame.setBackground(icon);
    }

    /**
     * The degree amount used for console directions requiring one rotation.
     */
    private final int NINETY_DEGREES = 90;

    /**
     * The degree amount used for console directions requiring two rotations.
     */
    private final int ONE_EIGHTY_DEGREES = 180;

    /**
     * Sets the background index to the provided index
     * if valid and switches to that background.
     *
     * @param index the index to switch the console background to
     */
    private void setBackgroundIndex(int index) {
        loadBackgrounds();

        if (index < 0 || index > backgrounds.size() - 1) {
            return;
        }

        Point center = consoleCyderFrame.getCenterPointOnScreen();

        revalidate(true, false);

        backgroundIndex = index;

        ImageIcon imageIcon = switch (consoleDir) {
            case LEFT -> new ImageIcon(ImageUtil.rotateImage(
                    backgrounds.get(backgroundIndex).generateBufferedImage(), -NINETY_DEGREES));
            case RIGHT -> new ImageIcon(ImageUtil.rotateImage(
                    backgrounds.get(backgroundIndex).generateBufferedImage(), NINETY_DEGREES));
            case TOP -> getCurrentBackground().generateImageIcon();
            case BOTTOM -> new ImageIcon(ImageUtil.rotateImage(
                    backgrounds.get(backgroundIndex).generateBufferedImage(), ONE_EIGHTY_DEGREES));
        };

        consoleCyderFrame.setBackground(imageIcon);
        consoleCyderFrame.setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());

        consoleCyderFrame.setLocation((int) (center.getX() - (imageIcon.getIconWidth()) / 2),
                (int) (center.getY() - (imageIcon.getIconHeight()) / 2));

        //tooltip based on image name
        getConsoleCyderFrameContentPane().setToolTipText(
                FileUtil.getFilename(getCurrentBackground().referenceFile().getName()));

        revalidateInputAndOutputBounds();

        //focus default component
        inputField.requestFocus();

        //fix menu
        revalidateMenu();
    }

    /**
     * Returns the current background.
     *
     * @return the current background
     */
    public ConsoleBackground getCurrentBackground() {
        return backgrounds.get(backgroundIndex);
    }

    /**
     * Whether the background switching is locked meaning an animation is currently underway.
     */
    private final AtomicBoolean backgroundSwitchingLocked = new AtomicBoolean(false);

    /**
     * Switches backgrounds to the next background in the list via a sliding animation.
     * The Console will remain in fullscreen mode if in fullscreen mode as well as maintain
     * whatever size it was at before a background switch was requested.
     */
    @SuppressWarnings("UnnecessaryDefault")
    private void switchBackground() {

        if (backgroundSwitchingLocked.get()) {
            return;
        }

        backgroundSwitchingLocked.set(true);

        ImageIcon nextBackground = (backgroundIndex + 1 == backgrounds.size()
                ? backgrounds.get(0).generateImageIcon()
                : backgrounds.get(backgroundIndex + 1).generateImageIcon());

        backgroundIndex = backgroundIndex + 1 == backgrounds.size() ? 0 : backgroundIndex + 1;

        int width = nextBackground.getIconWidth();
        int height = nextBackground.getIconHeight();

        if (isFullscreen()) {
            width = (int) consoleCyderFrame.getMonitorBounds().getWidth();
            height = (int) consoleCyderFrame.getMonitorBounds().getHeight();
            nextBackground = ImageUtil.resizeImage(nextBackground, width, height);
        } else if (consoleDir == Direction.LEFT) {
            width = nextBackground.getIconHeight();
            height = nextBackground.getIconWidth();
            nextBackground = ImageUtil.rotateImage(nextBackground, -NINETY_DEGREES);
        } else if (consoleDir == Direction.RIGHT) {
            width = nextBackground.getIconHeight();
            height = nextBackground.getIconWidth();
            nextBackground = ImageUtil.rotateImage(nextBackground, NINETY_DEGREES);
        } else if (consoleDir == Direction.BOTTOM) {
            width = nextBackground.getIconWidth();
            height = nextBackground.getIconHeight();
            nextBackground = ImageUtil.rotateImage(nextBackground, ONE_EIGHTY_DEGREES);
        }

        // get console's content pane
        JLabel contentPane = getConsoleCyderFrameContentPane();

        // tooltip based on image name
        contentPane.setToolTipText(FileUtil.getFilename(getCurrentBackground().referenceFile().getName()));

        // create final background that won't change
        ImageIcon nextBackFinal = nextBackground;

        // Get the original background and resize it as needed
        ImageIcon oldBack = ImageUtil.resizeImage((ImageIcon) contentPane.getIcon(), width, height);

        // Change frame size and put the center in the same spot
        Point originalCenter = consoleCyderFrame.getCenterPointOnScreen();
        consoleCyderFrame.setSize(width, height);

        // Bump frame into bounds if new size pushed part out of bounds
        FrameUtil.requestFramePosition(consoleCyderFrame.getMonitor(),
                (int) originalCenter.getX() - width / 2,
                (int) originalCenter.getY() - height / 2, consoleCyderFrame);

        ImageIcon combinedIcon = switch (lastSlideDirection) {
            case LEFT -> ImageUtil.combineImages(oldBack, nextBackground, Direction.BOTTOM);
            case RIGHT -> ImageUtil.combineImages(oldBack, nextBackground, Direction.TOP);
            case TOP -> ImageUtil.combineImages(oldBack, nextBackground, Direction.LEFT);
            case BOTTOM -> ImageUtil.combineImages(oldBack, nextBackground, Direction.RIGHT);
            default -> throw new IllegalStateException("Invalid last slide direction: " + lastSlideDirection);
        };

        // Revalidate bounds for icon label and icon pane
        consoleCyderFrame.refreshBackground();

        // Set dimensions
        switch (lastSlideDirection) {
            case LEFT ->
                    // will be sliding up
                    contentPane.setBounds(CyderFrame.FRAME_RESIZING_LEN, CyderFrame.FRAME_RESIZING_LEN,
                            combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
            case RIGHT ->
                    // will be sliding down
                    contentPane.setBounds(CyderFrame.FRAME_RESIZING_LEN, -combinedIcon.getIconHeight() / 2,
                            combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
            case TOP ->
                    // will be sliding right
                    contentPane.setBounds(-combinedIcon.getIconWidth() / 2, CyderFrame.FRAME_RESIZING_LEN,
                            combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
            case BOTTOM ->
                    // will be sliding left
                    contentPane.setBounds(combinedIcon.getIconWidth() / 2, CyderFrame.FRAME_RESIZING_LEN,
                            combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
            default -> throw new IllegalStateException("Invalid last slide direction: " + lastSlideDirection);
        }

        // set to combined icon
        contentPane.setIcon(combinedIcon);

        boolean wasDraggable = consoleCyderFrame.isDraggingEnabled();
        consoleCyderFrame.disableDragging();

        boolean outputAreaWasFocusable = outputArea.isFocusable();
        outputArea.setFocusable(false);

        CyderThreadRunner.submit(() -> {
            int timeout = isFullscreen() ? FULLSCREEN_TIMEOUT : DEFAULT_TIMEOUT;
            int increment = isFullscreen() ? FULLSCREEN_INCREMENT : DEFAULT_INCREMENT;

            switch (lastSlideDirection) {
                case LEFT -> {
                    // Sliding up
                    for (int i = 0 ; i >= -consoleCyderFrame.getHeight() ; i -= increment) {
                        ThreadUtil.sleep(timeout);
                        contentPane.setLocation(consoleCyderFrame.getContentPane().getX(), i);
                    }
                    lastSlideDirection = Direction.TOP;
                }
                case RIGHT -> {
                    // Sliding down
                    for (int i = -consoleCyderFrame.getHeight() ; i <= 0 ; i += increment) {
                        ThreadUtil.sleep(timeout);
                        contentPane.setLocation(consoleCyderFrame.getContentPane().getX(), i);
                    }
                    lastSlideDirection = Direction.BOTTOM;
                }
                case TOP -> {
                    // Sliding right
                    for (int i = -consoleCyderFrame.getWidth() ; i <= 0 ; i += increment) {
                        ThreadUtil.sleep(timeout);
                        contentPane.setLocation(i, consoleCyderFrame.getContentPane().getY());
                    }
                    lastSlideDirection = Direction.RIGHT;
                }
                case BOTTOM -> {
                    // Sliding left
                    for (int i = 0 ; i >= -consoleCyderFrame.getWidth() ; i -= increment) {
                        ThreadUtil.sleep(timeout);
                        contentPane.setLocation(i, consoleCyderFrame.getContentPane().getY());
                    }
                    lastSlideDirection = Direction.LEFT;
                }
                default -> throw new IllegalStateException("Invalid last slide direction: " + lastSlideDirection);
            }

            consoleCyderFrame.setBackground(nextBackFinal);
            contentPane.setIcon(nextBackFinal);

            consoleCyderFrame.refreshBackground();
            consoleCyderFrame.getContentPane().revalidate();

            refreshConsoleMaxSize();

            consoleCyderFrame.setDraggingEnabled(wasDraggable);
            outputArea.setFocusable(outputAreaWasFocusable);

            // Revalidate bounds to be safe
            boolean fullscreen = isFullscreen();
            revalidate(!fullscreen, fullscreen);

            // Default focus owner
            inputField.requestFocus();

            backgroundSwitchingLocked.set(false);
        }, "Background Switcher");
    }

    /**
     * Sets the console orientation and refreshes the frame.
     * This action exits fullscreen mode if active.
     *
     * @param consoleDirection the direction the background is to face
     */
    private void setConsoleDirection(Direction consoleDirection) {
        // only reset console size if setting in the current direction
        boolean revalidateConsoleSize = (consoleDirection != consoleDir);

        lastConsoleDir = consoleDir;
        consoleDir = consoleDirection;
        UserUtil.getCyderUser().setFullscreen("0");
        revalidate(true, false, revalidateConsoleSize);
    }

    /**
     * Returns the current console direction.
     *
     * @return the current console direction
     */
    private Direction getConsoleDirection() {
        return consoleDir;
    }

    /**
     * Refreshes the console, bounds, orientation, and fullscreen mode.
     *
     * @param fullscreen whether to set the frame to fullscreen mode.
     */
    public void setFullscreen(boolean fullscreen) {
        try {
            UserUtil.getCyderUser().setFullscreen(fullscreen ? "1" : "0");

            if (fullscreen) {
                consoleDir = Direction.TOP;
                consoleCyderFrame.setShouldAnimateOpacity(false);
                revalidate(false, true);
            } else {
                consoleCyderFrame.setShouldAnimateOpacity(true);
                revalidate(true, false);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns whether fullscreen is on.
     *
     * @return whether fullscreen is on
     */
    private boolean isFullscreen() {
        return UserUtil.getCyderUser().getFullscreen().equals("1");
    }

    /**
     * Returns the index in the command history we are currently at.
     *
     * @return the index in the command history we are currently at
     */
    public int getCommandIndex() {
        return commandIndex;
    }

    /**
     * Sets the index in the command history we are at.
     *
     * @param downs the index in the command history we are at
     */
    public void setCommandIndex(int downs) {
        commandIndex = downs;
    }

    /**
     * Increments the command index by 1.
     */
    public void incrementCommandIndex() {
        commandIndex += 1;
    }

    /**
     * Decreases the command index by 1.
     */
    public void decrementCommandIndex() {
        commandIndex -= 1;
    }

    /**
     * Returns whether the current background index is the maximum index.
     *
     * @return whether the current background index is the maximum index
     */
    public boolean onLastBackground() {
        loadBackgrounds();
        return backgrounds.size() == backgroundIndex + 1;
    }

    /**
     * Returns whether background switch is allowable and possible.
     *
     * @return whether a background switch is allowable and possible
     */
    private boolean canSwitchBackground() {
        return backgrounds.size() > 1;
    }

    /**
     * Returns the input handler associated with the Console.
     *
     * @return the input handler associated with the Console
     */
    public BaseInputHandler getInputHandler() {
        return baseInputHandler;
    }

    /**
     * Returns the x value of the Console.
     *
     * @return the x value of the Console
     */
    public int getX() {
        return consoleCyderFrame.getX();
    }

    /**
     * Returns the y value of the Console.
     *
     * @return the y value of the Console
     */
    public int getY() {
        return consoleCyderFrame.getY();
    }

    /**
     * Returns the width of the Console.
     *
     * @return the width of the Console
     */
    public int getWidth() {
        return consoleCyderFrame.getWidth();
    }

    /**
     * Returns the height of the Console.
     *
     * @return the height of the Console
     */
    public int getHeight() {
        return consoleCyderFrame.getHeight();
    }

    // --------------------
    // command history mods
    // --------------------

    /**
     * Wipes all command history and sets the command index back to 0.
     */
    public void clearCommandHistory() {
        commandList.clear();
        commandIndex = 0;
    }

    /**
     * Returns the command history.
     *
     * @return the command history
     */
    public ArrayList<String> getCommandHistory() {
        return commandList;
    }

    // ----------
    // ui getters
    // ----------

    /**
     * Returns the JTextPane associated with the Console.
     *
     * @return the JTextPane associated with the Console
     */
    public JTextPane getOutputArea() {
        return outputArea;
    }

    /**
     * Returns the JScrollPane associated with the Console.
     *
     * @return the JScrollPane associated with the Console
     */
    public CyderScrollPane getOutputScroll() {
        return outputScroll;
    }

    /**
     * Returns the input JTextField associated with the Console.
     *
     * @return the input JTextField associated with the Console
     */
    public JTextField getInputField() {
        return inputField;
    }

    /**
     * Revalidates the Console size, bounds, background, menu, clock, audio menu, draggable property, etc.
     * based on the current background. Note that maintainDirection trumps maintainFullscreen.
     *
     * @param maintainDirection  whether to maintain the console direction
     * @param maintainFullscreen whether to maintain fullscreen mode
     */
    public void revalidate(boolean maintainDirection, boolean maintainFullscreen) {
        revalidate(maintainDirection, maintainFullscreen, false);
    }

    /**
     * Revalidates the Console size, bounds, background, menu, clock, audio menu, draggable property, etc.
     * based on the current background. Note that maintainDirection trumps maintainFullscreen.
     *
     * @param maintainDirection   whether to maintain the console direction
     * @param maintainFullscreen  whether to maintain fullscreen mode
     * @param maintainConsoleSize whether to maintain the currently set size of the console
     */
    public void revalidate(boolean maintainDirection, boolean maintainFullscreen, boolean maintainConsoleSize) {
        Point originalCenter = consoleCyderFrame.getCenterPointOnScreen();

        ImageIcon background;

        if (maintainDirection) {
            // have full size of image and maintain currently set direction
            background = switch (consoleDir) {
                case TOP -> getCurrentBackground().generateImageIcon();
                case LEFT -> new ImageIcon(ImageUtil.getRotatedImage(
                        getCurrentBackground().referenceFile().getAbsolutePath(), Direction.LEFT));
                case RIGHT -> new ImageIcon(ImageUtil.getRotatedImage(
                        getCurrentBackground().referenceFile().getAbsolutePath(), Direction.RIGHT));
                case BOTTOM -> new ImageIcon(ImageUtil.getRotatedImage(
                        getCurrentBackground().referenceFile().getAbsolutePath(), Direction.BOTTOM));
            };

            UserUtil.getCyderUser().setFullscreen("0");
            consoleCyderFrame.setShouldAnimateOpacity(true);
        } else if (maintainFullscreen && UserUtil.getCyderUser().getFullscreen().equals("1")) {
            // have fullscreen on current monitor
            background = ImageUtil.resizeImage(getCurrentBackground().generateImageIcon(),
                    (int) consoleCyderFrame.getMonitorBounds().getWidth(),
                    (int) consoleCyderFrame.getMonitorBounds().getHeight());
            consoleCyderFrame.setShouldAnimateOpacity(false);
        } else {
            background = getCurrentBackground().generateImageIcon();
        }

        if (maintainConsoleSize) {
            switch (consoleDir) {
                case TOP:
                case BOTTOM:
                    if (lastConsoleDir == Direction.LEFT || lastConsoleDir == Direction.RIGHT) {
                        background = ImageUtil.resizeImage(background, getHeight(), getWidth());
                    } else {
                        background = ImageUtil.resizeImage(background, getWidth(), getHeight());
                    }
                    break;
                case LEFT:
                case RIGHT:
                    if (lastConsoleDir == Direction.LEFT || lastConsoleDir == Direction.RIGHT) {
                        background = ImageUtil.resizeImage(background, getWidth(), getHeight());
                    } else {
                        background = ImageUtil.resizeImage(background, getHeight(), getWidth());
                    }
                    break;
            }
        }

        int w = background.getIconWidth();
        int h = background.getIconHeight();

        // this shouldn't ever happen
        if (w == -1 || h == -1) {
            throw new FatalException("Resulting width or height was found to " +
                    "not have been set in Console refresh method. " + CyderStrings.EUROPEAN_TOY_MAKER);
        }

        consoleCyderFrame.setSize(w, h);
        consoleCyderFrame.setBackground(background);

        FrameUtil.requestFramePosition(consoleCyderFrame.getMonitor(),
                (int) originalCenter.getX() - w / 2,
                (int) originalCenter.getY() - h / 2, consoleCyderFrame);

        revalidateInputAndOutputBounds();

        refreshConsoleMaxSize();

        // this takes care of offset of input/output due to menu
        revalidateMenu();

        consoleCyderFrame.refreshBackground();

        if (isFullscreen()) {
            consoleCyderFrame.disableDragging();
        } else {
            consoleCyderFrame.enableDragging();
        }

        //audio menu bounds
        if (audioControlsLabel != null && audioControlsLabel.isVisible()) {
            audioControlsLabel.setBounds(w - audioControlsLabel.getWidth(), CyderDragLabel.DEFAULT_HEIGHT - 2,
                    audioControlsLabel.getWidth(), audioControlsLabel.getHeight());
        }

        // clock text
        refreshClockText();
    }

    /**
     * Returns the CyderFrame used for the Console.
     *
     * @return the CyderFrame used for the Console
     */
    public CyderFrame getConsoleCyderFrame() {
        return consoleCyderFrame;
    }

    // --------------------------------
    // menu generation and revalidation
    // --------------------------------

    /**
     * Revalidates the console menu bounds and height and places
     * it where it in the proper spot depending on if it is shown.
     */
    public void revalidateMenu() {
        if (consoleClosed || menuLabel == null)
            return;

        // revalidate bounds if needed and change icon
        if (menuLabel.isVisible()) {
            menuButton.setIcon(CyderIcons.menuIconHover);
            installMenuTaskbarIcons();
            menuLabel.setBounds(3, CyderDragLabel.DEFAULT_HEIGHT - 2,
                    menuLabel.getWidth(), consoleCyderFrame.getHeight()
                            - CyderDragLabel.DEFAULT_HEIGHT - 5);
            menuScroll.setBounds(7, 10, menuLabel.getWidth() - 10, menuLabel.getHeight() - 20);
        } else {
            menuButton.setIcon(CyderIcons.menuIcon);
            //no other actions needed
        }

        revalidateInputAndOutputBounds();
    }

    /**
     * Smoothly animates out the console audio controls.
     */
    private void animateOutAudioControls() {
        CyderThreadRunner.submit(() -> {
            for (int i = audioControlsLabel.getY() ; i > -40 ; i -= 8) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                        - audioControlsLabel.getWidth() - 6, i);
                ThreadUtil.sleep(10);
            }
            audioControlsLabel.setVisible(false);
        }, "Console Audio Menu Minimizer");
    }

    /**
     * Smooth animates out and removes the audio controls button.
     */
    public void animateOutAndRemoveAudioControls() {
        CyderThreadRunner.submit(() -> {
            for (int i = audioControlsLabel.getY() ; i > -40 ; i -= 8) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                        - audioControlsLabel.getWidth() - 6, i);
                ThreadUtil.sleep(10);
            }
            audioControlsLabel.setVisible(false);
            removeAudioControls();
        }, "Console Audio Menu Minimizer");
    }

    /**
     * Smoothly animates in the audio controls.
     */
    private void animateInAudioControls() {
        CyderThreadRunner.submit(() -> {
            generateAudioMenu();
            audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                    - audioControlsLabel.getWidth() - 6, -40);
            audioControlsLabel.setVisible(true);
            for (int i = -40 ; i < CyderDragLabel.DEFAULT_HEIGHT - 2 ; i += 8) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                        - audioControlsLabel.getWidth() - 6, i);
                ThreadUtil.sleep(10);
            }
            audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                    - audioControlsLabel.getWidth() - 6, CyderDragLabel.DEFAULT_HEIGHT - 2);
        }, "Console Audio Menu Minimizer");
    }

    /**
     * Revalidates the visibility audio menu and the play/pause button based on if audio is playing.
     */
    public void revalidateAudioMenu() {
        if (!AudioPlayer.isWidgetOpen() && !IOUtil.generalAudioPlaying()) {
            if (audioControlsLabel.isVisible()) {
                animateOutAndRemoveAudioControls();
            } else {
                removeAudioControls();
            }
        } else {
            if (!audioControlsLabel.isVisible()) {
                audioControlsLabel.setLocation(audioControlsLabel.getX(), -40);
                toggleAudioControls.setVisible(true);
            }

            if (IOUtil.generalAudioPlaying() || AudioPlayer.isAudioPlaying()) {
                playPauseAudioLabel.setIcon(AudioIcons.pauseIcon);
            } else {
                playPauseAudioLabel.setIcon(AudioIcons.playIcon);
            }
        }
    }

    /**
     * Hides the audio controls panel and toggle button.
     */
    private void removeAudioControls() {
        audioControlsLabel.setVisible(false);
        toggleAudioControls.setVisible(false);
        consoleCyderFrame.getTopDragLabel().refreshButtons();
    }

    /**
     * The number of audio menu buttons.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int AUDIO_MENU_BUTTONS = 3;

    /**
     * The size of an audio menu button.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int AUDIO_MENU_BUTTON_SIZE = 30;

    /**
     * The height of the audio menu label.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int AUDIO_MENU_LABEL_HEIGHT = 40;

    /**
     * Generates the audio menu label and the button components.
     */
    private void generateAudioMenu() {
        int xPadding = 10;

        int labelWidth = AUDIO_MENU_BUTTON_SIZE * AUDIO_MENU_BUTTONS + xPadding * (AUDIO_MENU_BUTTONS + 1);

        int yPadding = (AUDIO_MENU_LABEL_HEIGHT - AUDIO_MENU_BUTTON_SIZE) / 2;

        audioControlsLabel = new JLabel();
        audioControlsLabel.setBounds(consoleCyderFrame.getWidth() - labelWidth - 6,
                -AUDIO_MENU_LABEL_HEIGHT, labelWidth, AUDIO_MENU_LABEL_HEIGHT);
        audioControlsLabel.setOpaque(true);
        audioControlsLabel.setBackground(CyderColors.getGuiThemeColor());
        audioControlsLabel.setBorder(new LineBorder(Color.black, 5));
        audioControlsLabel.setVisible(false);
        consoleCyderFrame.getIconPane().add(audioControlsLabel, JLayeredPane.MODAL_LAYER);

        int currentX = xPadding;

        JLabel lastMusicLabel = new JLabel();
        lastMusicLabel.setBounds(currentX, yPadding, AUDIO_MENU_BUTTON_SIZE, AUDIO_MENU_BUTTON_SIZE);
        lastMusicLabel.setIcon(AudioIcons.lastIcon);
        lastMusicLabel.setToolTipText("Previous");
        lastMusicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (AudioPlayer.isWidgetOpen()) {
                    AudioPlayer.handleLastAudioButtonClick();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lastMusicLabel.setIcon(AudioIcons.lastIconHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastMusicLabel.setIcon(AudioIcons.lastIcon);
            }
        });
        lastMusicLabel.setVisible(true);
        lastMusicLabel.setOpaque(false);
        audioControlsLabel.add(lastMusicLabel);

        currentX += xPadding + AUDIO_MENU_BUTTON_SIZE;

        playPauseAudioLabel = new JLabel();
        playPauseAudioLabel.setBounds(currentX, yPadding, AUDIO_MENU_BUTTON_SIZE, AUDIO_MENU_BUTTON_SIZE);
        playPauseAudioLabel.setToolTipText("Play/Pause");
        playPauseAudioLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (AudioPlayer.isWidgetOpen()) {
                    AudioPlayer.handlePlayPauseButtonClick();
                }

                if (IOUtil.generalAudioPlaying()) {
                    IOUtil.stopGeneralAudio();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (IOUtil.generalAudioPlaying() || AudioPlayer.isAudioPlaying()) {
                    playPauseAudioLabel.setIcon(AudioIcons.pauseIconHover);
                } else {
                    playPauseAudioLabel.setIcon(AudioIcons.playIconHover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (IOUtil.generalAudioPlaying() || AudioPlayer.isAudioPlaying()) {
                    playPauseAudioLabel.setIcon(AudioIcons.pauseIcon);
                } else {
                    playPauseAudioLabel.setIcon(AudioIcons.playIcon);
                }
            }
        });
        playPauseAudioLabel.setVisible(true);
        playPauseAudioLabel.setOpaque(false);
        audioControlsLabel.add(playPauseAudioLabel);

        if (IOUtil.generalAudioPlaying() || AudioPlayer.isAudioPlaying()) {
            playPauseAudioLabel.setIcon(AudioIcons.pauseIcon);
        } else {
            playPauseAudioLabel.setIcon(AudioIcons.playIcon);
        }

        audioControlsLabel.add(playPauseAudioLabel);

        currentX += xPadding + AUDIO_MENU_BUTTON_SIZE;

        JLabel nextMusicLabel = new JLabel();
        nextMusicLabel.setBounds(currentX, yPadding, AUDIO_MENU_BUTTON_SIZE, AUDIO_MENU_BUTTON_SIZE);
        nextMusicLabel.setIcon(AudioIcons.nextIcon);
        audioControlsLabel.add(nextMusicLabel);
        nextMusicLabel.setToolTipText("Skip");
        nextMusicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (AudioPlayer.isWidgetOpen()) {
                    AudioPlayer.handleNextAudioButtonClick();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                nextMusicLabel.setIcon(AudioIcons.nextIconHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextMusicLabel.setIcon(AudioIcons.nextIcon);
            }
        });
        nextMusicLabel.setVisible(true);
        nextMusicLabel.setOpaque(false);

        audioControlsLabel.add(nextMusicLabel);
    }

    /**
     * Revalidates the background colors of the console menus (audio or taskbar) that are active.
     */
    public void revalidateMenuBackgrounds() {
        if (menuLabel != null && menuLabel.isVisible()) {
            generateConsoleMenu();
            menuLabel.setLocation(2, CyderDragLabel.DEFAULT_HEIGHT - 2);
        }

        if (audioControlsLabel != null && audioControlsLabel.isVisible()) {
            audioControlsLabel.setBackground(CyderColors.getGuiThemeColor());
            audioControlsLabel.repaint();
        }
    }

    /**
     * Sets the console to a provided ScreenPosition and moves any pinned CyderFrame windows with it.
     *
     * @param screenPos the screen position to move the Console to
     */
    public void setLocationOnScreen(CyderFrame.ScreenPosition screenPos) {
        ArrayList<RelativeFrame> frames = getPinnedFrames();

        consoleCyderFrame.setLocationOnScreen(screenPos);

        for (RelativeFrame rf : frames) {
            rf.frame().setLocation(
                    rf.xOffset() + consoleCyderFrame.getX(),
                    rf.yOffset() + consoleCyderFrame.getY());
        }
    }

    /**
     * The record used for frames pinned to the console.
     */
    private record RelativeFrame(CyderFrame frame, int xOffset, int yOffset) {
    }

    /**
     * Returns a list of all frames that are pinned to the Console.
     *
     * @return a list of all frames that are pinned to the Console
     */
    private ArrayList<RelativeFrame> getPinnedFrames() {
        ArrayList<RelativeFrame> frames = new ArrayList<>();

        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame) {
                if (((CyderFrame) f).isConsolePinned() &&
                        !f.getTitle().equals(consoleCyderFrame.getTitle())) {
                    if (MathUtil.rectanglesOverlap(consoleCyderFrame.getBounds(), f.getBounds())) {
                        frames.add(new RelativeFrame((CyderFrame) f,
                                f.getX() - consoleCyderFrame.getX(), f.getY() - consoleCyderFrame.getY()));
                    }
                }
            }
        }

        return frames;
    }

    /**
     * Refreshes the text on the ConsoleClock based off of showSeconds and the possibly set
     * custom date pattern. The bounds of ConsoleClock are also updated.
     */
    public void refreshClockText() {
        try {
            if (consoleClockLabel == null)
                return;

            if (UserUtil.getCyderUser().getClockonconsole().equals("1")) {
                consoleClockLabel.setVisible(true);
            } else {
                consoleClockLabel.setVisible(false);
                return;
            }

            // get pattern
            String pattern = UserUtil.getCyderUser().getConsoleclockformat();

            //get time according to the pattern
            String time = TimeUtil.getTime(pattern);

            String regularSecondTime = TimeUtil.consoleSecondTime();
            String regularNoSecondTime = TimeUtil.consoleNoSecondTime();

            // no custom pattern so take into account showSeconds
            if (time.equalsIgnoreCase(regularSecondTime) || time.equalsIgnoreCase(regularNoSecondTime)) {
                if (UserUtil.getCyderUser().getShowseconds().equalsIgnoreCase("1")) {
                    time = regularSecondTime;
                } else {
                    time = regularNoSecondTime;
                }
            }

            consoleClockLabel.setText(time);

            int w = StringUtil.getMinWidth(time, consoleClockLabel.getFont());
            consoleClockLabel.setBounds(consoleCyderFrame.getWidth() / 2 - w / 2,
                    0, w, consoleClockLabel.getHeight());
        } catch (Exception ignored) {
        }
        //sometimes extracting user throws, so we will ignore exceptions thrown from this method
    }

    /**
     * Simply closes the console due to a user logout.
     *
     * @param exit       whether to exit Cyder upon closing the Console
     * @param logoutUser whether to log out the currently logged-in user.
     */
    public void closeFrame(boolean exit, boolean logoutUser) {
        consoleClosed = true;
        saveScreenStat();

        //stop any audio
        IOUtil.stopGeneralAudio();

        //close the input handler
        if (baseInputHandler != null) {
            baseInputHandler.killThreads();
            baseInputHandler = null;
        }

        if (logoutUser) {
            Logger.log(Logger.Tag.LOGOUT, "[CyderUser = " + UserUtil.getCyderUser().getName() + "]");
            UserUtil.getCyderUser().setLoggedin("0");
        }

        //remove closing actions
        consoleCyderFrame.removePostCloseActions();

        //dispose and set closed var as true
        if (exit) {
            consoleCyderFrame.addPostCloseAction(() -> OSUtil.exit(ExitCondition.GenesisControlledExit));
        }

        consoleCyderFrame.dispose();
    }

    /**
     * Returns whether the Console is closed.
     *
     * @return whether the Console is closed
     */
    public boolean isClosed() {
        return consoleClosed;
    }

    /**
     * Saves the console's position and window stats to the currently logged-in user's json file.
     */
    public void saveScreenStat() {
        if (getUuid() == null)
            return;

        if (consoleCyderFrame != null) {
            // create new screen stat object
            ScreenStat screenStat = UserUtil.getCyderUser().getScreenStat();
            screenStat.setConsoleWidth(consoleCyderFrame.getWidth());
            screenStat.setConsoleHeight(consoleCyderFrame.getHeight());
            screenStat.setConsoleOnTop(consoleCyderFrame.isAlwaysOnTop());
            screenStat.setMonitor(Integer.parseInt(consoleCyderFrame.getGraphicsConfiguration()
                    .getDevice().getIDstring().replaceAll("[^0-9]", "")));

            if (consoleCyderFrame.getState() == JFrame.ICONIFIED) {
                screenStat.setConsoleX(consoleCyderFrame.getRestoreX());
                screenStat.setConsoleY(consoleCyderFrame.getRelativeY());
            } else {
                screenStat.setConsoleX(consoleCyderFrame.getX());
                screenStat.setConsoleY(consoleCyderFrame.getY());
            }

            screenStat.setConsoleDirection(consoleDir);

            if (!isClosed()) {
                UserUtil.getCyderUser().setScreenStat(screenStat);
                UserUtil.writeUser();
            }
        }
    }

    /**
     * Closes the CyderFrame and shows the LoginFrame
     * relative to where Console was closed.
     */
    public void logout() {
        closeFrame(false, true);
        FrameUtil.closeAllFrames(true);

        IOUtil.stopAllAudio();

        LoginHandler.showGui();
    }

    // -------
    // dancing
    // -------

    /**
     * Invokes dance in a synchronous way on all CyderFrame instances.
     */
    public void dance() {
        class RestoreFrame {
            private final int restoreX;
            private final int restoreY;
            private final CyderFrame frame;
            private final boolean draggingWasEnabled;

            private RestoreFrame(CyderFrame frame, int restoreX, int restoreY, boolean draggingWasEnabled) {
                this.restoreX = restoreX;
                this.restoreY = restoreY;
                this.frame = frame;
                this.draggingWasEnabled = draggingWasEnabled;
            }
        }

        //list of frames for restoration purposes
        LinkedList<RestoreFrame> restoreFrames = new LinkedList<>();

        //add frame's to list for restoration coords and dragging restoration
        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame) {
                restoreFrames.add(new RestoreFrame((CyderFrame) f,
                        f.getX(), f.getY(), ((CyderFrame) f).isDraggingEnabled()));
                ((CyderFrame) f).disableDragging();
            }
        }

        //set var to true so we can terminate dancing
        currentlyDancing = true;

        //invoke dance step on all threads which currently dancing is true and all frames are not in the finished state
        while (currentlyDancing && !allFramesFinishedDancing()) {
            for (Frame f : Frame.getFrames()) {
                if (f instanceof CyderFrame) {
                    ((CyderFrame) f).danceStep();
                }
            }
        }

        stopDancing();

        //reset frame's locations and dragging vars
        for (RestoreFrame f : restoreFrames) {
            f.frame.setLocation(f.restoreX, f.restoreY);

            if (f.draggingWasEnabled) {
                f.frame.enableDragging();
            }
        }
    }

    /**
     * Ends the dancing sequence if ongoing.
     */
    public void stopDancing() {
        //end dancing sequence
        currentlyDancing = false;

        //reset all frames to dance again
        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame) {
                ((CyderFrame) f).setDancingDirection(CyderFrame.DancingDirection.INITIAL_UP);
                ((CyderFrame) f).setDancingFinished(false);
            }
        }
    }

    /**
     * Returns whether all frames have completed a dance iteration.
     *
     * @return whether all frames have completed a dance iteration
     */
    private boolean allFramesFinishedDancing() {
        boolean ret = true;

        for (Frame f : Frame.getFrames()) {
            if (!((CyderFrame) f).isDancingFinished()) {
                ret = false;
                break;
            }
        }

        return ret;
    }

    /**
     * Sets the background of the console to whatever is behind it.
     * This was the original implementation of frame chams functionality before
     * the windows were actually set to be transparent.
     */
    public void originalChams() {
        try {
            CyderFrame ref = INSTANCE.getConsoleCyderFrame();
            Rectangle monitorBounds = ref.getMonitorBounds();

            INSTANCE.getConsoleCyderFrame().setVisible(false);
            BufferedImage capture = Console.INSTANCE
                    .getInputHandler().getRobot().createScreenCapture(monitorBounds);
            INSTANCE.getConsoleCyderFrame().setVisible(true);

            capture = ImageUtil.cropImage(capture, (int) (Math.abs(monitorBounds.getX()) + ref.getX()),
                    (int) (Math.abs(monitorBounds.getY()) + ref.getY()), ref.getWidth(), ref.getHeight());

            INSTANCE.setBackground(ImageUtil.toImageIcon(capture));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the console's content pane.
     *
     * @return the console's content pane
     */
    public JLabel getConsoleCyderFrameContentPane() {
        return ((JLabel) (consoleCyderFrame.getContentPane()));
    }

    /**
     * An semaphore to ensure only one title notification is ever visible
     */
    private final Semaphore titleNotifySemaphore = new Semaphore(1);

    /**
     * The label used for title notifications.
     */
    private final CyderLabel titleNotifyLabel = new CyderLabel();

    /**
     * Paints a label with the provided possibly html-formatted string over the
     * Console for the provided number of milliseconds
     *
     * @param htmlString      the string to display, may or may not be formatted using html
     * @param labelFont       the font to use for the label
     * @param visibleDuration the duration in ms the notification should be visible for
     */
    public void titleNotify(String htmlString, Font labelFont, int visibleDuration) {
        Preconditions.checkNotNull(htmlString);
        Preconditions.checkArgument(visibleDuration > 500,
                "A user probably won't see a message with that short of a duration");

        CyderThreadRunner.submit(() -> {
            try {
                titleNotifySemaphore.acquire();

                BufferedImage bi = getCurrentBackground().generateBufferedImage();

                titleNotifyLabel.setFont(labelFont);
                titleNotifyLabel.setOpaque(true);
                titleNotifyLabel.setVisible(true);
                titleNotifyLabel.setBackground(ColorUtil.getDominantGrayscaleColor(bi));
                titleNotifyLabel.setForeground(ColorUtil.getSuitableOverlayTextColor(bi));

                BoundsUtil.BoundsString boundsString = BoundsUtil.widthHeightCalculation(htmlString,
                        labelFont, consoleCyderFrame.getWidth());

                int containerWidth = boundsString.width();
                int containerHeight = boundsString.height();

                if (containerHeight + 2 * NOTIFICATION_PADDING > consoleCyderFrame.getHeight()
                        || containerWidth + 2 * NOTIFICATION_PADDING > consoleCyderFrame.getWidth()) {
                    consoleCyderFrame.inform(htmlString, "Console Notification");
                    return;
                }

                Point center = consoleCyderFrame.getCenterPointOnFrame();

                titleNotifyLabel.setText(BoundsUtil.addCenteringToHtml(boundsString.text()));
                titleNotifyLabel.setBounds(
                        (int) (center.getX() - NOTIFICATION_PADDING - containerWidth / 2),
                        (int) (center.getY() - NOTIFICATION_PADDING - containerHeight / 2),
                        containerWidth, containerHeight);
                consoleCyderFrame.getContentPane().add(titleNotifyLabel, JLayeredPane.POPUP_LAYER);
                consoleCyderFrame.repaint();

                ThreadUtil.sleep(visibleDuration);
                titleNotifyLabel.setVisible(false);
                consoleCyderFrame.remove(titleNotifyLabel);
                titleNotifyLabel.setText("");

                titleNotifySemaphore.release();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Console Title Notify: " + htmlString);
    }

    /**
     * Revalidates the bounds of the title label notify if one is underway.
     */
    public void revalidateTitleNotify() {
        if (consoleCyderFrame == null || titleNotifyLabel.getText().isEmpty()) {
            return;
        }

        int w = titleNotifyLabel.getWidth();
        int h = titleNotifyLabel.getHeight();

        Point center = consoleCyderFrame.getCenterPointOnFrame();

        titleNotifyLabel.setLocation(
                (int) (center.getX() - NOTIFICATION_PADDING - w / 2),
                (int) (center.getY() - NOTIFICATION_PADDING - h / 2));

        consoleCyderFrame.repaint();
    }
}
