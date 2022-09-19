package cyder.console;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import cyder.annotations.ForReadability;
import cyder.audio.AudioIcons;
import cyder.audio.AudioPlayer;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderRegexPatterns;
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
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.test.ManualTests;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.button.*;
import cyder.ui.field.CyderCaret;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollPane;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyder.console.ConsoleConstants.*;

/**
 * Singleton of components that represent the GUI way a user
 * interacts with Cyder and its functions.
 */
@SuppressWarnings({"FieldCanBeLocal", "ImmutableEnumChecker"}) /* member clarity, enum used as singleton */
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
     * An list of the frames to ignore when placing a frame in the console taskbar menu.
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
     * The menu button for the console frame.
     */
    private MenuButton menuButton;

    /**
     * The menu button for the audio controls.
     */
    private MenuButton toggleAudioControls;

    /**
     * The default focus owner for focus to default to when no focused components can be found.
     */
    private Component defaultFocusOwner;

    /**
     * The label used for the Cyder taskbar.
     */
    private JLabel menuLabel;

    /**
     * The scroll pane for the active frames.
     */
    private CyderScrollPane menuScroll;

    /**
     * The audio menu parent label
     */
    private JLabel audioControlsLabel;

    /**
     * The button label used to indicate if audio is playing
     */
    private JLabel playPauseAudioLabel;

    /**
     * Whether the console is closed.
     */
    private final AtomicBoolean consoleClosed = new AtomicBoolean(true);

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

        NetworkUtil.startHighPingChecker();

        reloadBackgrounds();

        resetMembers();

        CyderColors.refreshGuiThemeColor();
        ConsoleIcon consoleIcon = determineConsoleIconAndDimensions();

        setupConsoleCyderFrame(consoleIcon);
        refreshConsoleSuperTitle();

        installConsoleResizing();

        installOutputArea();
        installInputField();

        baseInputHandler = new BaseInputHandler(outputArea);

        setupButtonEnterInputMap();

        installRightDragLabelButtons();
        installLeftDragLabelButtons();

        generateAudioMenu();
        installConsoleClock();
        installConsolePinnedWindowListeners();
        startExecutors();

        /*
        Note to maintainers: we only close splash here, all other frames are disposed when we logout
        which is the only way this launch method is invoked more than once for an instance of Cyder.

        The login frame is disposed elsewhere as well. Thus any frames left open are warnings or
        popups from validation subroutines which the user (hopefully developer) should read and dismiss themselves.
         */
        CyderSplash.INSTANCE.fastDispose();

        if (!isFullscreen()) {
            restorePreviousFrameBounds(consoleIcon);
        }

        setConsoleVisibleAndPerformFadeInAnimation();

        revalidateInputAndOutputBounds(true);

        TimeUtil.setConsoleStartTime(System.currentTimeMillis());
        long loadTime = TimeUtil.getConsoleStartTime() - TimeUtil.getAbsoluteStartTime();
        baseInputHandler.println("Console loaded in " + TimeUtil.formatMillis(loadTime));
    }

    /**
     * Resets private variables to their default state.
     */
    @ForReadability
    private void resetMembers() {
        consoleBashString = UserUtil.getCyderUser().getName() + BASH_STRING_PREFIX;

        lastSlideDirection = DEFAULT_CONSOLE_DIRECTION;
        consoleDir = DEFAULT_CONSOLE_DIRECTION;

        commandIndex = 0;

        consoleClosed.set(false);
        menuLabel = null;

        commandList.clear();
        currentActiveFrames.clear();
    }

    /**
     * Sets up the console cyder frame and performs all subsequent calls on the object.
     *
     * @param consoleIcon the console icon record to use for the direct props
     */
    @ForReadability
    private void setupConsoleCyderFrame(ConsoleIcon consoleIcon) {
        int w = (int) consoleIcon.dimension().getWidth();
        int h = (int) consoleIcon.dimension().getHeight();

        consoleCyderFrame = new CyderFrame(w, h, consoleIcon.background()) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y, width, height);

                revalidateInputAndOutputBounds();
                revalidateCustomMenuBounds();
                revalidateAudioMenuBounds();
                revalidateMenu();
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

            /**
             * A full barrel roll's rotation degrees.
             */
            private static final int FULL_ROTATION_DEGREES = 360;

            /**
             * The increment in degrees for a barrel roll.
             */
            private static final int DEGREE_INCREMENT = 2;

            /**
             * The delay between barrel roll increments.
             */
            private static final int BARREL_ROLL_DELAY = 2;

            /**
             * Whether a barrel roll is currently underway.
             */
            private static boolean consoleBarrelRollLocked = false;

            /**
             * The thread name for the barrel roll animator.
             */
            private static final String BARREL_ROLL_THREAD_NAME = "Console Barrel Roll Thread";

            /**
             * {@inheritDoc}
             */
            @Override
            public void barrelRoll() {
                if (consoleBarrelRollLocked) return;
                consoleBarrelRollLocked = true;

                CyderThreadRunner.submit(() -> {
                    BufferedImage masterImage = getCurrentBackground().generateBufferedImage();
                    for (int i = 0 ; i <= FULL_ROTATION_DEGREES ; i += DEGREE_INCREMENT) {
                        BufferedImage rotated = ImageUtil.rotateImage(masterImage, i);
                        getConsoleCyderFrameContentPane().setIcon(new ImageIcon(rotated));
                        ThreadUtil.sleep(BARREL_ROLL_DELAY);
                    }

                    getConsoleCyderFrameContentPane().setIcon(getCurrentBackground().generateImageIcon());
                    consoleBarrelRollLocked = false;
                }, BARREL_ROLL_THREAD_NAME);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void minimizeAndIconify() {
                saveScreenStat();
                super.minimizeAndIconify();
            }
        };

        frameTaskbarExceptions = ImmutableList.of(consoleCyderFrame, CyderSplash.INSTANCE.getSplashFrame());

        consoleCyderFrame.setBackground(Color.black);
        consoleCyderFrame.addEndDragEventCallback(this::saveScreenStat);
        consoleCyderFrame.setDraggingEnabled(!UserUtil.getCyderUser().getFullscreen().equals("1"));
        consoleCyderFrame.addWindowListener(consoleWindowAdapter);

        getConsoleCyderFrameContentPane().setToolTipText(
                FileUtil.getFilename(getCurrentBackground().getReferenceFile().getName()));

        consoleCyderFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        consoleCyderFrame.setPaintCyderFrameTitleOnSuperCall(false);
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
    @ForReadability
    private ConsoleIcon determineConsoleIconAndDimensions() {
        int width;
        int height;
        ImageIcon icon;

        boolean randombackground = UserUtil.getCyderUser().getRandombackground().equals("1");
        if (randombackground && reloadAndGetBackgrounds().size() > 1) {
            backgroundIndex = NumberUtil.randInt(backgrounds.size() - 1);
        }

        boolean fullscreen = UserUtil.getCyderUser().getFullscreen().equals("1");
        if (fullscreen) {
            int monitorId = UserUtil.getCyderUser().getScreenStat().getMonitor();
            Rectangle monitorBounds = UiUtil.getGraphicsDevice(monitorId).getDefaultConfiguration().getBounds();

            width = (int) monitorBounds.getWidth();
            height = (int) monitorBounds.getHeight();

            icon = new ImageIcon(ImageUtil.resizeImage(width, height, getCurrentBackground().getReferenceFile()));
        } else {
            BufferedImage bi = getCurrentBackground().generateBufferedImage();

            if (bi == null) {
                throw new FatalException("Generated buffered image is null");
            }

            width = bi.getWidth();
            height = bi.getHeight();
            icon = new ImageIcon(ImageUtil.getRotatedImage(getCurrentBackground()
                    .getReferenceFile().toString(), getConsoleDirection()));
        }

        if (width == 0 || height == 0) {
            throw new FatalException("Could not construct background dimension");
        }

        return new ConsoleIcon(icon, new Dimension(width, height));
    }

    /**
     * The key for obtaining the Cyder version prop from the props.
     */
    private static final String VERSION = "version";

    /**
     * Refreshes the console super title, that of displaying "Version Cyder [Nathan]".
     */
    public void refreshConsoleSuperTitle() {
        consoleCyderFrame.setTitle(PropLoader.getString(VERSION) +
                " Cyder [" + UserUtil.getCyderUser().getName() + "]");
    }

    /**
     * The value to indicate a frame is not pinned to the console.
     */
    private static final int FRAME_NOT_PINNED = Integer.MIN_VALUE;

    /**
     * The mouse motion adapter for frame pinned window logic.
     */
    private final MouseMotionAdapter consolePinnedMouseMotionAdapter = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (consoleCyderFrame == null
                    || !consoleCyderFrame.isFocusable()
                    || !consoleCyderFrame.isDraggingEnabled()) return;

            UiUtil.getCyderFrames().stream().filter(frame -> frame.isConsolePinned() && !frame.isConsole()
                            && frame.getRelativeX() != FRAME_NOT_PINNED
                            && frame.getRelativeY() != FRAME_NOT_PINNED)
                    .forEach(frame -> frame.setLocation(consoleCyderFrame.getX() + frame.getRelativeX(),
                            consoleCyderFrame.getY() + frame.getRelativeY()));
        }
    };

    /**
     * The mouse adapter for frame pinned window logic.
     */
    private final MouseAdapter consolePinnedMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (consoleCyderFrame == null
                    || !consoleCyderFrame.isFocusable()
                    || !consoleCyderFrame.isDraggingEnabled()) return;


            UiUtil.getCyderFrames().stream()
                    .filter(frame -> frame.isConsolePinned() && !frame.isConsole())
                    .forEach(frame -> {
                        boolean overlap = MathUtil.rectanglesOverlap(consoleCyderFrame.getBounds(), frame.getBounds());
                        if (overlap) {
                            frame.setRelativeX(-consoleCyderFrame.getX() + frame.getX());
                            frame.setRelativeY(-consoleCyderFrame.getY() + frame.getY());
                        } else {
                            frame.setRelativeX(FRAME_NOT_PINNED);
                            frame.setRelativeY(FRAME_NOT_PINNED);
                        }
                    });
        }
    };

    /**
     * Adds the pinned window logic listeners to the console.
     */
    @ForReadability
    private void installConsolePinnedWindowListeners() {
        consoleCyderFrame.addDragListener(consolePinnedMouseMotionAdapter);
        consoleCyderFrame.addDragLabelMouseListener(consolePinnedMouseAdapter);
    }

    /**
     * The x value for the menu label when open.
     */
    private static final int menuLabelShowingX = 3;

    /**
     * The y value for the menu label when open.
     */
    private static final int menuLabelShowingY = CyderDragLabel.DEFAULT_HEIGHT - 2;

    /**
     * Revalidates the bounds of the custom console menu and the audio controls menu.
     */
    private void revalidateCustomMenuBounds() {
        if (UiUtil.notNullAndVisible(menuLabel)) {
            menuLabel.setBounds(menuLabelShowingX, menuLabelShowingY, TASKBAR_MENU_WIDTH, calculateMenuHeight());
        }
    }

    /**
     * The y value for the audio menu after animated on.
     */
    private final int audioMenuLabelShowingY = CyderDragLabel.DEFAULT_HEIGHT - 2;

    /**
     * Revalidates the audio menu bounds.
     */
    public void revalidateAudioMenuBounds() {
        if (UiUtil.notNullAndVisible(audioControlsLabel)) {
            audioControlsLabel.setBounds(calculateAudioMenuX(), audioMenuLabelShowingY,
                    AUDIO_MENU_LABEL_WIDTH, AUDIO_MENU_LABEL_HEIGHT);
        }
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

            int menuLabelEndX = (UiUtil.notNullAndVisible(menuLabel) && !ignoreMenuLabel)
                    ? 2 + menuLabel.getWidth() : 0;

            outputScroll.setBounds(menuLabelEndX + FIELD_X_PADDING,
                    CyderDragLabel.DEFAULT_HEIGHT + FIELD_Y_PADDING,
                    w - menuLabelEndX - 2 * FIELD_X_PADDING,
                    h - INPUT_FIELD_HEIGHT - FIELD_Y_PADDING * 3 - CyderDragLabel.DEFAULT_HEIGHT);

            inputField.setBounds(menuLabelEndX + FIELD_X_PADDING,
                    outputScroll.getY() + FIELD_Y_PADDING + outputScroll.getHeight(),
                    w - 2 * FIELD_X_PADDING - menuLabelEndX, INPUT_FIELD_HEIGHT);
        }
    }

    /**
     * Sets up the output area and output scroll and adds it to the console.
     */
    @ForReadability
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
        outputArea.setFont(generateUserFont());

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
        boolean outputBorder = UserUtil.getCyderUser().getOutputborder().equals("1");
        Color color = ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground());
        outputScroll.setBorder(outputBorder
                ? new LineBorder(color, FIELD_BORDER_THICKNESS, false)
                : BorderFactory.createEmptyBorder());

        boolean outputFill = UserUtil.getCyderUser().getOutputfill().equals("1");
        if (outputFill) {
            outputArea.setOpaque(true);
            outputArea.setBackground(color);
            outputArea.repaint();
            outputArea.revalidate();
        }

        consoleCyderFrame.getContentPane().add(outputScroll);
    }

    /**
     * Sets up the input field and adds it to the console.
     */
    @ForReadability
    private void installInputField() {
        inputField = new JPasswordField();

        inputField.setEchoChar((char) 0);
        inputField.setText(consoleBashString);
        boolean inputBorder = UserUtil.getCyderUser().getInputborder().equals("1");
        Color backgroundColor = ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground());
        inputField.setBorder(inputBorder
                ? new LineBorder(backgroundColor, FIELD_BORDER_THICKNESS, false)
                : BorderFactory.createEmptyBorder());
        inputField.setSelectionColor(CyderColors.selectionColor);
        inputField.setCaretPosition(inputField.getPassword().length);

        inputField.setOpaque(false);
        inputField.setCaretColor(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
        inputField.setCaret(new CyderCaret(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground())));
        inputField.setForeground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getForeground()));
        inputField.setFont(generateUserFont());

        installInputFieldListeners();

        boolean inputFill = UserUtil.getCyderUser().getInputfill().equals("1");
        if (inputFill) {
            inputField.setOpaque(true);
            inputField.setBackground(ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground()));
            inputField.repaint();
            inputField.revalidate();
        }

        consoleCyderFrame.getContentPane().add(inputField);

        defaultFocusOwner = inputField;
    }

    /**
     * Sets up the input map to allow the drag label buttons to be triggered via the enter key.
     */
    @ForReadability
    private void setupButtonEnterInputMap() {
        InputMap inputMap = (InputMap) UIManager.get(BUTTON_INPUT_FOCUS_MAP_KEY);
        inputMap.put(KeyStroke.getKeyStroke(ENTER), PRESSED);
        inputMap.put(KeyStroke.getKeyStroke(RELEASED_ENTER), RELEASED);
    }

    /**
     * The increment of the console fade in animation.
     */
    private static final float FADE_IN_ANIMATION_INCREMENT = 0.02f;

    /**
     * The starting minimum opacity of the console for the fade in animation.
     */
    private static final float FADE_IN_STARTING_OPACITY = 0f;

    /**
     * The ending maximum opacity of the console for the fade in animation.
     */
    private static final float FADE_IN_ENDING_OPACITY = 1f;

    /**
     * The delay between fade in increments for the fade in animation.
     */
    private static final int FADE_IN_ANIMATION_DELAY = 2;

    /**
     * The thread name for the console fade in animation.
     */
    private static final String FADE_IN_ANIMATION_THREAD_NAME = "Console Fade-in Animation";

    /**
     * Sets the console to visible and performs the fade in animation.
     */
    @ForReadability
    private void setConsoleVisibleAndPerformFadeInAnimation() {
        consoleCyderFrame.setOpacity(FADE_IN_STARTING_OPACITY);
        consoleCyderFrame.setVisible(true);

        CyderThreadRunner.submit(() -> {
            for (float i = FADE_IN_STARTING_OPACITY ; i < FADE_IN_ENDING_OPACITY ; i += FADE_IN_ANIMATION_INCREMENT) {
                consoleCyderFrame.setOpacity(i);
                ThreadUtil.sleep(FADE_IN_ANIMATION_DELAY);
            }
        }, FADE_IN_ANIMATION_THREAD_NAME);
    }

    /**
     * Sets up the console position based on the saved stats from the previous session.
     *
     * @param consoleIcon the console icon record to get the size from
     */
    @ForReadability
    private void restorePreviousFrameBounds(ConsoleIcon consoleIcon) {
        ScreenStat requestedConsoleStats = UserUtil.getCyderUser().getScreenStat();

        boolean onTop = requestedConsoleStats.isConsoleOnTop();
        PinButton.PinState state = onTop ? PinButton.PinState.CONSOLE_PINNED : PinButton.PinState.DEFAULT;
        consoleCyderFrame.getTopDragLabel().getPinButton().setState(state);

        int requestedConsoleWidth = requestedConsoleStats.getConsoleWidth();
        int requestedConsoleHeight = requestedConsoleStats.getConsoleHeight();

        if (requestedConsoleWidth < consoleIcon.dimension().getWidth()
                && requestedConsoleHeight < consoleIcon.dimension().getHeight()
                && requestedConsoleWidth > MINIMUM_SIZE.width
                && requestedConsoleHeight > MINIMUM_SIZE.height) {
            consoleCyderFrame.setSize(requestedConsoleWidth, requestedConsoleHeight);
        }

        consoleDir = requestedConsoleStats.getConsoleDirection();
        ImageIcon rotated = getCurrentRotatedConsoleBackground();
        ImageIcon resized = ImageUtil.resizeImage(rotated, requestedConsoleWidth, requestedConsoleHeight);
        consoleCyderFrame.setBackground(resized);

        int requestedConsoleX = requestedConsoleStats.getConsoleX();
        int requestedConsoleY = requestedConsoleStats.getConsoleY();
        Point relocatedSplashCenter = CyderSplash.INSTANCE.getRelocatedCenterPoint();
        if (relocatedSplashCenter != null) {
            requestedConsoleX = (int) (relocatedSplashCenter.getX() - consoleCyderFrame.getWidth() / 2);
            requestedConsoleY = (int) (relocatedSplashCenter.getY() - consoleCyderFrame.getHeight() / 2);
        }
        // This is more so to push the frame into bounds if any part was out of bounds on the requested monitor.
        UiUtil.requestFramePosition(requestedConsoleX, requestedConsoleY, consoleCyderFrame);
    }

    /**
     * The tooltip of the alternate background button.
     */
    private static final String ALTERNATE_BACKGROUND = "Alternate Background";

    /**
     * The tooltip of the audio menu button.
     */
    private static final String AUDIO_MENU = "Audio Menu";

    /**
     * The text for the only one background notification builder.
     */
    private static final String onlyOneBackgroundNotificationText = "You only have one background image. "
            + "Try adding more via the user editor";

    /**
     * The builder for when the alternate background buttons is pressed when only one background is present.
     */
    private static final CyderFrame.NotificationBuilder onlyOneBackgroundNotificationBuilder
            = new CyderFrame.NotificationBuilder(onlyOneBackgroundNotificationText)
            .setViewDuration(5000)
            .setOnKillAction(() -> UserEditor.showGui(UserEditor.Page.FILES));

    /**
     * Installs the right drag label buttons for the console frame.
     */
    @ForReadability
    private void installRightDragLabelButtons() {
        // Remove default close button
        consoleCyderFrame.getTopDragLabel().removeRightButton(2);
        // Add custom close button
        CloseButton closeButton = new CloseButton();
        closeButton.setForConsole(true);
        closeButton.setClickAction(() -> {
            boolean shouldMinimize = UserUtil.getCyderUser().getMinimizeonclose().equals("1");
            if (shouldMinimize) {
                UiUtil.minimizeAllFrames();
            } else {
                closeFrame(true, false);
            }
        });
        closeButton.addFocusLostAction(() -> outputArea.requestFocus());
        consoleCyderFrame.getTopDragLabel().addRightButton(closeButton, 2);

        // Remove default minimize button
        consoleCyderFrame.getTopDragLabel().removeRightButton(0);
        // Add custom minimize button
        MinimizeButton minimizeButton = new MinimizeButton(consoleCyderFrame);
        minimizeButton.setForConsole(true);
        consoleCyderFrame.getTopDragLabel().addRightButton(minimizeButton, 0);

        // Remove default pin button
        consoleCyderFrame.getTopDragLabel().removeRightButton(1);
        //  Add custom pin button
        PinButton pinButton = new PinButton(consoleCyderFrame);
        pinButton.setForConsole(true);
        consoleCyderFrame.getTopDragLabel().setPinButton(pinButton);
        consoleCyderFrame.getTopDragLabel().addRightButton(pinButton, 1);

        ChangeSizeButton changeSizeButton = new ChangeSizeButton();
        changeSizeButton.setForConsole(true);
        changeSizeButton.setToolTipText(ALTERNATE_BACKGROUND);
        changeSizeButton.setClickAction(() -> {
            reloadBackgrounds();

            if (canSwitchBackground()) {
                switchBackground();
            } else if (reloadAndGetBackgrounds().size() == 1) {
                consoleCyderFrame.notify(onlyOneBackgroundNotificationBuilder);
            }
        });
        consoleCyderFrame.getTopDragLabel().addRightButton(changeSizeButton, 2);

        toggleAudioControls = new MenuButton();
        toggleAudioControls.setForConsole(true);
        toggleAudioControls.setToolTipText(AUDIO_MENU);
        toggleAudioControls.setClickAction(() -> {
            if (audioControlsLabel.isVisible()) {
                animateOutAudioControls();
            } else {
                animateInAudioControls();
            }
        });
        toggleAudioControls.setVisible(false);
        consoleCyderFrame.getTopDragLabel().addRightButton(toggleAudioControls, 0);
    }

    /**
     * Installs the left drag label buttons for the console frame.
     */
    @ForReadability
    private void installLeftDragLabelButtons() {
        menuButton = new MenuButton();
        menuButton.setForConsole(true);
        menuButton.setClickAction(this::menuButtonAction);
        menuButton.addKeyListener(menuButtonKeyAdapter);
        menuButton.addFocusGainedAction(this::removeFocusFromTaskbarMenuIcons);
        menuButton.addFocusLostAction(this::removeFocusFromTaskbarMenuIcons);
        consoleCyderFrame.getTopDragLabel().addLeftButton(menuButton, 0);
    }

    /**
     * Sets up and adds the console clock to the top drag label.
     */
    @ForReadability
    private void installConsoleClock() {
        consoleCyderFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        consoleCyderFrame.setCyderFrameTitle("");
        consoleCyderFrame.setTitleLabelFont(CONSOLE_CLOCK_FONT);
    }

    /**
     * The key used for the debug lines abstract action.
     */
    private static final String DEBUG_LINES = "debuglines";

    /**
     * The key used for the forced exit abstract action.
     */
    private static final String FORCED_EXIT = "forcedexit";

    /**
     * Installs all the input field listeners.
     */
    private void installInputFieldListeners() {
        inputField.addKeyListener(inputFieldKeyAdapter);
        inputField.addKeyListener(commandScrolling);

        inputField.addMouseWheelListener(fontSizerListener);
        inputField.addActionListener(inputFieldActionListener);

        inputField.addFocusListener(inputFieldFocusAdapter);

        KeyStroke debugKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(debugKeystroke, DEBUG_LINES);
        inputField.getActionMap().put(DEBUG_LINES, debugLinesAbstractAction);

        KeyStroke exitKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK);
        inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(exitKeystroke, FORCED_EXIT);
        inputField.getActionMap().put(FORCED_EXIT, forcedExitAbstractAction);
    }

    /**
     * Installs all the output area listeners.
     */
    private void installOutputAreaListeners() {
        outputArea.addFocusListener(outputAreaFocusAdapter);
        outputArea.addMouseWheelListener(fontSizerListener);
    }

    /**
     * Sets up resizing for the console.
     */
    @ForReadability
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
        if (getCurrentBackground().getReferenceFile() != null) {
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
     * The name of the chime file.
     */
    private final String CHIME = "chime.mp3";

    /**
     * The path to the chime mp3 file.
     */
    private final String CHIME_PATH = StaticUtil.getStaticPath(CHIME);

    /**
     * The last hour a chime sound was played at.
     */
    private final AtomicInteger lastChimeHour = new AtomicInteger(-1);

    /**
     * The frequency to check the clock for a chime.
     */
    private static final int CHIME_CHECKER_FREQUENCY = 50;

    /**
     * The clock refresh frequency.
     */
    private static final int CLOCK_REFRESH_SLEEP_TIME = 200;

    /**
     * The frequency to check for console disposal in the clock refresh thread.
     */
    private static final int CLOCK_CHECK_FREQUENCY = 50;

    /**
     * Begins the console checker executors/threads.
     */
    @ForReadability
    private void startExecutors() {
        CyderThreadRunner.submit(() -> {
            try {
                while (true) {
                    if (!isClosed()) {
                        int min = LocalDateTime.now().getMinute();
                        int sec = LocalDateTime.now().getSecond();

                        if (min == 0 && sec == 0 && lastChimeHour.get() != LocalDateTime.now().getHour()) {
                            boolean chime = UserUtil.getCyderUser().getHourlychimes().equals("1");
                            if (chime) {
                                IOUtil.playSystemAudio(CHIME_PATH);
                                lastChimeHour.set(LocalDateTime.now().getHour());
                            }
                        }

                        ThreadUtil.sleep(CHIME_CHECKER_FREQUENCY);
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.HourlyChimeChecker.getName());

        CyderThreadRunner.submit(() -> {
            while (true) {
                if (!isClosed()) {
                    try {
                        refreshClockText();
                        TimeUtil.sleepWithChecks(CLOCK_REFRESH_SLEEP_TIME, CLOCK_CHECK_FREQUENCY, consoleClosed);
                    } catch (Exception e) {
                        ExceptionHandler.silentHandle(e);
                    }
                }
            }
        }, IgnoreThread.ConsoleClockUpdater.getName());

        CyderThreadRunner.submit(() -> {
            try {
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
                    TimeUtil.sleepWithChecks(3000, 50, consoleClosed);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            } finally {
                CyderIcons.setCurrentCyderIcon(CyderIcons.CYDER_ICON);
                consoleCyderFrame.setIconImage(CyderIcons.getCurrentCyderIcon().getImage());
            }
        }, IgnoreThread.CyderBusyChecker.getName());
    }

    /**
     * The thread name of the debug stat finder.
     */
    private final String DEBUG_STAT_FINDER_THREAD_NAME = "Debug Stat Finder";

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
            getInputHandler().println("Thanks for creating me Nate :,)");
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
            }, DEBUG_STAT_FINDER_THREAD_NAME);
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
     * The default intro music to play if enabled an no user music is present.
     */
    private static final File DEFAULT_INTRO_MUSIC = StaticUtil.getStaticResource("ride.mp3");

    /**
     * The key for getting whether Cyder is released from the props.
     */
    private static final String RELEASED_KEY = "released";

    /**
     * The name of the Cyder intro theme file
     */
    private static final String INTRO_THEME = "introtheme.mp3";

    /**
     * The thread name of the intro music grayscale checker.
     */
    private static final String INTRO_MUSIC_CHECKER_THREAD_NAME = "Intro Music Checker";

    /**
     * Determines what audio to play at the beginning of the Console startup.
     */
    private void introMusicCheck() {
        boolean introMusic = UserUtil.getCyderUser().getIntromusic().equalsIgnoreCase("1");
        boolean released = PropLoader.getBoolean(RELEASED_KEY);
        if (introMusic) {
            performIntroMusic();
        } else if (released) {
            grayscaleImageCheck();
        }
    }

    /**
     * Plays intro music, from the user's music folder if file are present. Otherwise the default intro music.
     */
    @ForReadability
    private void performIntroMusic() {
        ArrayList<File> musicList = new ArrayList<>();

        File userMusicDir = new File(OSUtil.buildPath(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), getUuid(), UserFile.MUSIC.getName()));

        File[] files = userMusicDir.listFiles();
        if (files != null && files.length > 0) {
            Arrays.stream(files).forEach(file -> {
                if (FileUtil.isSupportedAudioExtension(file)) {
                    musicList.add(file);
                }
            });
        }

        if (!musicList.isEmpty()) {
            int randomFileIndex = NumberUtil.randInt(files.length - 1);
            IOUtil.playGeneralAudio(files[randomFileIndex].getAbsolutePath());
        } else {
            IOUtil.playGeneralAudio(DEFAULT_INTRO_MUSIC);
        }
    }

    /**
     * Checks for a grayscale image and plays a grayscale song if true.
     */
    @ForReadability
    private void grayscaleImageCheck() {
        CyderThreadRunner.submit(() -> {
            Image icon = null;

            try {
                icon = new ImageIcon(ImageIO.read(getCurrentBackground().getReferenceFile())).getImage();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            if (icon == null) return;

            int w = icon.getWidth(null);
            int h = icon.getHeight(null);

            int[] pixels = new int[w * h];
            PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);

            try {
                pg.grabPixels();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            boolean grayscale = true;
            for (int pixel : pixels) {
                Color color = new Color(pixel);
                if (color.getRed() != color.getGreen() || color.getRed() != color.getBlue()) {
                    grayscale = false;
                    break;
                }
            }

            if (grayscale) {
                int upperBound = GRAYSCALE_AUDIO_PATHS.size() - 1;
                int grayscaleAudioRandomIndex = NumberUtil.randInt(upperBound);
                IOUtil.playGeneralAudio(GRAYSCALE_AUDIO_PATHS.get(grayscaleAudioRandomIndex));
            } else {
                IOUtil.playGeneralAudio(StaticUtil.getStaticResource(INTRO_THEME));
            }
        }, INTRO_MUSIC_CHECKER_THREAD_NAME);
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

            for (CyderFrame frame : UiUtil.getCyderFrames()) {
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
            Color color = ColorUtil.hexStringToColor(UserUtil.getCyderUser().getBackground());
            outputScroll.setBorder(new LineBorder(color, 3));
        }

        @Override
        public void focusLost(FocusEvent e) {
            boolean outputBorder = UserUtil.getCyderUser().getOutputborder().equals("1");
            if (!outputBorder) {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

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

        if (state.size() == 0) return;

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

        if (menuLabel == null) return;

        Stream.of(currentFrameMenuItems, currentMappedExeItems,
                        currentDefaultMenuItems)
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .forEach(icon -> icon.getBuilder().setFocused(false));

        if (menuLabel.isVisible()) {
            reinstallCurrentTaskbarIcons();
        }
    }

    /**
     * The action for when the menu button is pressed.
     */
    private void menuButtonAction() {
        Point menuPoint = menuButton.getLocationOnScreen();
        Rectangle rect = new Rectangle((int) menuPoint.getX(), (int) menuPoint.getY(),
                menuButton.getWidth(), menuButton.getHeight());
        boolean mouseTriggered = MathUtil.pointInOrOnRectangle(MouseInfo.getPointerInfo().getLocation(), rect);

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
            String MINIMIZE_MEN_THREAD_NAME = "Minimize Console Menu Thread";
            int menuHiddenX = -150;

            CyderThreadRunner.submit(() -> {
                menuLabel.setLocation(menuHiddenX, CyderDragLabel.DEFAULT_HEIGHT - 2);
                int y = menuLabel.getY();

                int increment = 8;
                int delay = 10;
                for (int i = menuHiddenX ; i < 2 ; i += increment) {
                    menuLabel.setLocation(i, y);
                    ThreadUtil.sleep(delay);
                }

                menuLabel.setLocation(2, y);

                revalidateInputAndOutputBounds();
            }, MINIMIZE_MEN_THREAD_NAME);

            String ENTER_CONSOLE_MENU_THREAD_NAME = "Enter Console Menu Thread";
            CyderThreadRunner.submit(() -> {
                if (menuLabel == null) {
                    generateConsoleMenu();
                }

                menuLabel.setLocation(menuHiddenX, CyderDragLabel.DEFAULT_HEIGHT - 2);
                menuLabel.setVisible(true);

                int addX = 0;

                if (menuLabel.isVisible()) {
                    addX = 2 + menuLabel.getWidth();
                }

                int finalAddX = addX;

                int increment = 8;
                int delay = 10;
                for (int x = inputField.getX() ; x < finalAddX + 15 ; x += increment) {
                    outputScroll.setBounds(x, outputScroll.getY(),
                            outputScroll.getWidth(), outputScroll.getHeight());
                    inputField.setBounds(x, inputField.getY(),
                            inputField.getWidth(), inputField.getHeight());

                    ThreadUtil.sleep(delay);
                }

                revalidateInputAndOutputBounds();
            }, ENTER_CONSOLE_MENU_THREAD_NAME);
        } else {
            minimizeMenu();
        }
    }

    /**
     * The key adapter for the menu button to allow "focusing" taskbar items.
     */
    private final KeyAdapter menuButtonKeyAdapter = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();

            if (downOrRight(code)) {
                focusNextTaskbarMenuItem();
            } else if (upOrLeft(code)) {
                focusPreviousTaskbarMenuItem();
            }
        }
    };

    @ForReadability
    private boolean downOrRight(int code) {
        return code == KeyEvent.VK_DOWN || code == KeyEvent.VK_RIGHT;
    }

    @ForReadability
    private boolean upOrLeft(int code) {
        return code == KeyEvent.VK_UP || code == KeyEvent.VK_LEFT;
    }

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
        lockMenuTaskbarInstallation();

        boolean compactMode = UserUtil.getCyderUser().getCompactTextMode().equals("1");

        StyleConstants.setAlignment(alignment, compactMode
                ? StyleConstants.ALIGN_LEFT : StyleConstants.ALIGN_CENTER);

        StyledDocument doc = menuPane.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), alignment, false);

        ImmutableList<TaskbarIcon> frameMenuItems = getCurrentFrameTaskbarIcons(compactMode);
        ImmutableList<TaskbarIcon> mappedExeItems = getMappedExeTaskbarIcons(compactMode);
        ImmutableList<TaskbarIcon> defaultMenuItems = getDefaultTaskbarIcons(compactMode);

        if (!differentMenuState(frameMenuItems, mappedExeItems, defaultMenuItems)) {
            unlockMenuTaskbarInstallation();
            return;
        }

        currentFrameMenuItems = frameMenuItems;
        currentMappedExeItems = mappedExeItems;
        currentDefaultMenuItems = defaultMenuItems;

        reinstallCurrentTaskbarIcons();
        unlockMenuTaskbarInstallation();
    }

    /**
     * The semaphore used to lock invocation of the {@link #installMenuTaskbarIcons()} method.
     */
    private final Semaphore menuTaskbarLockingSemaphore = new Semaphore(1);

    /**
     * Locks invocation of the {@link #installMenuTaskbarIcons()} method.
     */
    private void lockMenuTaskbarInstallation() {
        try {
            menuTaskbarLockingSemaphore.acquire();
        } catch (InterruptedException e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Unlocks invocation of the {@link #installMenuTaskbarIcons()} method.
     */
    private void unlockMenuTaskbarInstallation() {
        menuTaskbarLockingSemaphore.release();
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
            Lists.reverse(currentActiveFrames).forEach(currentFrame ->
                    ret.add(new TaskbarIcon.Builder()
                            .setName(currentFrame.getTitle())
                            .setCompact(compactMode)
                            .setFocused(false)
                            .setBorderColor(currentFrame.getTaskbarIconBorderColor())
                            .setCustomIcon(currentFrame.getCustomTaskbarIcon())
                            .setRunnable(UiUtil.generateCommonFrameTaskbarIconRunnable(currentFrame))
                            .build()));
        }

        return ImmutableList.copyOf(ret);
    }

    // todo remove icon button class, need an alternative for generating
    //  a JLabel with an icon for normal, hover, and focused states

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
            exes.forEach(exe -> {
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
            });
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Whether the last send action for the user editor frame was {@link CyderFrame#toFront()}.
     */
    private final AtomicBoolean sentToFront = new AtomicBoolean();

    /**
     * The runnable for when the preferences default taskbar icon is clicked.
     */
    private final Runnable prefsRunnable = () -> {
        if (UserEditor.isOpen()) {
            if (UserEditor.isMinimized()) {
                UserEditor.restore();
            } else {
                if (sentToFront.get()) {
                    UserEditor.getEditUserFrame().toBack();
                    sentToFront.set(false);
                } else {
                    UserEditor.getEditUserFrame().toFront();
                    sentToFront.set(true);
                }
            }
        } else {
            UserEditor.showGui();
        }

        revalidateMenu();
    };

    /**
     * The tooltip and label text for the preferences default taskbar icon.
     */
    private static final String PREFS = "Prefs";

    /**
     * The tooltip and label text for the logout default taskbar icon.
     */
    private static final String LOGOUT = "Logout";

    /**
     * The default compact taskbar icons.
     */
    private final ImmutableList<TaskbarIcon> compactDefaultTaskbarIcons = ImmutableList.of(
            new TaskbarIcon.Builder()
                    .setName(PREFS)
                    .setFocused(false)
                    .setCompact(true)
                    .setRunnable(prefsRunnable)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build(),
            new TaskbarIcon.Builder()
                    .setName(LOGOUT)
                    .setFocused(false)
                    .setCompact(true)
                    .setRunnable(this::logout)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build()
    );

    /**
     * The default non compact taskbar icons.
     */
    private final ImmutableList<TaskbarIcon> nonCompactDefaultTaskbarIcons = ImmutableList.of(
            new TaskbarIcon.Builder()
                    .setName(PREFS)
                    .setFocused(false)
                    .setCompact(false)
                    .setRunnable(prefsRunnable)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build(),
            new TaskbarIcon.Builder()
                    .setName(LOGOUT)
                    .setFocused(false)
                    .setCompact(false)
                    .setRunnable(this::logout)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build()
    );

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

        if (previousState.size() == 0) return true;

        if (newState.size() != previousState.size()) return true;

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
            String input = String.valueOf(inputField.getPassword())
                    .substring(consoleBashString.length())
                    .trim().replace(consoleBashString, "");

            if (StringUtil.isNullOrEmpty(input)) {
                resetInputField();
                return;
            }

            if (shouldAddToCommandList(input)) commandList.add(input);
            commandIndex = commandList.size();
            baseInputHandler.handle(input, true);

            resetInputField();
        }
    };

    /**
     * Returns whether the provided user input should be added to the command list.
     *
     * @param input the user input to add to the command list if the proper conditions are met
     * @return whether the provided user input should be added to the command list
     */
    @ForReadability
    private boolean shouldAddToCommandList(String input) {
        return commandList.isEmpty() || !commandList.get(commandList.size() - 1).equals(input);
    }

    /**
     * Sets the input field text to the console bash string and the caret to the end.
     */
    @ForReadability
    private void resetInputField() {
        inputField.setText(consoleBashString);
        inputField.setCaretPosition(consoleBashString.length());
    }

    /**
     * Returns the height of the console menu based on the current frame height.
     *
     * @return the height of the console menu based on the current frame height
     */
    private int calculateMenuHeight() {
        return consoleCyderFrame.getHeight() - CyderDragLabel.DEFAULT_HEIGHT - CyderFrame.BORDER_LEN;
    }

    /**
     * The point the console menu is set at and animated to when visible.
     */
    private static final Point consoleMenuShowingPoint = new Point(7, 10);

    /**
     * Revalidates the taskbar menu bounds and re-installs the icons.
     */
    private void generateConsoleMenu() {
        if (menuLabel != null) menuLabel.setVisible(false);

        menuLabel = new JLabel();
        menuLabel.setBounds(-TASKBAR_MENU_WIDTH, CyderDragLabel.DEFAULT_HEIGHT - 2,
                TASKBAR_MENU_WIDTH, calculateMenuHeight());
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

        menuScroll.setBounds(
                (int) consoleMenuShowingPoint.getX(),
                (int) consoleMenuShowingPoint.getY(),
                (int) (menuLabel.getWidth() - consoleMenuShowingPoint.getX()),
                (int) (calculateMenuHeight() - 2 * consoleMenuShowingPoint.getY()));
        menuLabel.add(menuScroll);

        installMenuTaskbarIcons();
    }

    /**
     * Clears the taskbar menu pane and re-prints the current taskbar icons from the three lists.
     */
    private void reinstallCurrentTaskbarIcons() {
        boolean compactMode = UserUtil.getCyderUser().getCompactTextMode().equals("1");
        StringUtil printingUtil = new StringUtil(new CyderOutputPane(menuPane));

        menuPane.setText("");

        printingUtil.newline(!compactMode);

        currentFrameMenuItems.forEach(frameItem -> {
            frameItem.generateTaskbarIcon();
            printingUtil.printlnComponent(frameItem.getTaskbarIcon());
            printingUtil.newline(!compactMode);
        });

        if (currentFrameMenuItems.size() > 0 && !compactMode) {
            printingUtil.printSeparator();
        }

        currentMappedExeItems.forEach(mappedExe -> {
            mappedExe.generateTaskbarIcon();
            printingUtil.printlnComponent(mappedExe.getTaskbarIcon());
            printingUtil.newline(!compactMode);
        });

        if (currentMappedExeItems.size() > 0 && currentFrameMenuItems.size() > 0 && !compactMode) {
            printingUtil.printSeparator();
        }

        currentDefaultMenuItems.forEach(taskbarIcon -> {
            taskbarIcon.generateTaskbarIcon();
            printingUtil.printlnComponent(taskbarIcon.getTaskbarIcon());
            printingUtil.newline(!compactMode);
        });

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
        if (isClosed() || frameTaskbarExceptions.contains(associatedFrame)) return;

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
            int increment = 8;
            int delay = 10;

            // todo pretty sure this is duplicated above

            CyderThreadRunner.submit(() -> {
                for (int i = inputField.getX() ; i > 15 ; i -= increment) {
                    outputScroll.setBounds(i, outputScroll.getY(),
                            outputScroll.getWidth() + 1, outputScroll.getHeight());
                    inputField.setBounds(i, inputField.getY(),
                            inputField.getWidth() + 1, inputField.getHeight());

                    ThreadUtil.sleep(delay);
                }

                revalidateInputAndOutputBounds(true);
            }, "Console menu animator");

            CyderThreadRunner.submit(() -> {
                menuLabel.setLocation(2, CyderDragLabel.DEFAULT_HEIGHT - 2);
                int y = menuLabel.getY();
                int hiddenX = -150;

                for (int i = 0 ; i > hiddenX ; i -= increment) {
                    menuLabel.setLocation(i, y);
                    ThreadUtil.sleep(delay);
                }

                menuLabel.setLocation(hiddenX, y);
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
        public void keyPressed(KeyEvent e) {
            if (isControlC(e)) baseInputHandler.escapeThreads();
            int caretPosition = outputArea.getCaretPosition();

            if (isConsoleAltDown(e)) {
                setConsoleDirection(Direction.BOTTOM);
                outputArea.setCaretPosition(caretPosition);
            } else if (isConsoleAltRight(e)) {
                setConsoleDirection(Direction.RIGHT);
                outputArea.setCaretPosition(caretPosition);
            } else if (isConsoleAltUp(e)) {
                setConsoleDirection(Direction.TOP);
                outputArea.setCaretPosition(caretPosition);
            } else if (isConsoleAltLeft(e)) {
                setConsoleDirection(Direction.LEFT);
                outputArea.setCaretPosition(caretPosition);
            }
        }

        @ForReadability
        private boolean isControlC(KeyEvent e) {
            return (e.getKeyCode() == KeyEvent.VK_C)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0);
        }

        @ForReadability
        private boolean isConsoleAltDown(KeyEvent e) {
            return (e.getKeyCode() == KeyEvent.VK_DOWN)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0);
        }

        @ForReadability
        private boolean isConsoleAltRight(KeyEvent e) {
            return (e.getKeyCode() == KeyEvent.VK_RIGHT)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0);
        }

        @ForReadability
        private boolean isConsoleAltUp(KeyEvent e) {
            return (e.getKeyCode() == KeyEvent.VK_UP)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0);
        }

        @ForReadability
        private boolean isConsoleAltLeft(KeyEvent e) {
            return (e.getKeyCode() == KeyEvent.VK_LEFT)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if (isBackspace(e) && wouldRemoveBashStringContents()) {
                e.consume();
                inputField.setText(consoleBashString);
                return;
            }

            if (inputField.getCaretPosition() < consoleBashString.toCharArray().length) {
                ensureFullBashStringPresent();
                setCaretPositionAtEnd();
            } else {
                ensureStartsWithBashString();
            }

            super.keyTyped(e);
            ensureFullBashStringPresent();
        }

        @ForReadability
        private boolean wouldRemoveBashStringContents() {
            return String.valueOf(inputField.getPassword()).trim().equals(consoleBashString.trim());
        }

        @ForReadability
        private boolean isBackspace(KeyEvent e) {
            return e.getKeyChar() == KeyEvent.VK_BACK_SPACE;
        }

        @ForReadability
        private void ensureStartsWithBashString() {
            String text = new String(inputField.getPassword());
            if (!text.startsWith(consoleBashString)) {
                inputField.setText(consoleBashString + text.replace(consoleBashString, ""));
                setCaretPositionAtBashStringLength();
            }
        }

        @ForReadability
        private void ensureFullBashStringPresent() {
            if (inputField.getPassword().length < consoleBashString.length()) {
                inputField.setText(consoleBashString);
                setCaretPositionAtBashStringLength();
            }
        }

        @ForReadability
        private void setCaretPositionAtBashStringLength() {
            inputField.setCaretPosition(consoleBashString.length());
        }

        @ForReadability
        private void setCaretPositionAtEnd() {
            inputField.setCaretPosition(inputField.getPassword().length);
        }
    };

    /**
     * The key listener for input field to control command scrolling.
     */
    private final KeyListener commandScrolling = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!controlAltNotPressed(e)) return;

            if (upEvent(e)) {
                attemptScrollUp();
            } else if (downEvent(e)) {
                attemptScrollDown();
            }

            checkForSpecialFunctionKeys(e);
        }

        @ForReadability
        private void attemptScrollUp() {
            if (commandIndex - 1 >= 0) {
                commandIndex -= 1;
                inputField.setText(consoleBashString + commandList.get(commandIndex)
                        .replace(consoleBashString, ""));
            }
        }

        @ForReadability
        private void attemptScrollDown() {
            if (commandIndex + 1 < commandList.size()) {
                commandIndex += 1;
                inputField.setText(consoleBashString + commandList.get(commandIndex)
                        .replace(consoleBashString, ""));
            } else if (commandIndex + 1 == commandList.size()) {
                commandIndex += 1;
                inputField.setText(consoleBashString);
            }
        }

        @ForReadability
        private void checkForSpecialFunctionKeys(KeyEvent e) {
            int code = e.getKeyCode();

            for (int specialCode : SPECIAL_FUNCTION_KEY_CODES) {
                if (code == specialCode) {
                    int functionKey = (code - KeyEvent.VK_F13 + SPECIAL_FUNCTION_KEY_CODE_OFFSET);
                    baseInputHandler.println("Interesting F" + functionKey + " key");

                    if (functionKey == F_17_KEY_CODE) {
                        IOUtil.playGeneralAudio(StaticUtil.getStaticResource("f17.mp3"));
                    }
                }
            }
        }

        @ForReadability
        private boolean upEvent(KeyEvent e) {
            return e.getKeyCode() == KeyEvent.VK_UP;
        }

        @ForReadability
        private boolean downEvent(KeyEvent e) {
            return e.getKeyCode() == KeyEvent.VK_DOWN;
        }

        @ForReadability
        private boolean controlAltNotPressed(KeyEvent e) {
            return (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == 0);
        }
    };

    /**
     * The key to use for the max font size prop.
     */
    private final String MAX_FONT_SIZE = "max_font_size";

    /**
     * The key to use for the min font size prop.
     */
    private final String MIN_FONT_SIZE = "min_font_size";

    /**
     * The key to use for the font metric prop.
     */
    private final String FONT_METRIC = "font_metric";

    /**
     * Some kind of a magic number that denotes the mouse wheel is being scrolled up.
     */
    private final int WHEEL_UP = -1;

    /**
     * The MouseWheelListener used for increasing/decreasing the
     * font size for input field and output area.
     */
    @SuppressWarnings("MagicConstant") // Font metric is always checked
    private final MouseWheelListener fontSizerListener = e -> {
        if (e.isControlDown()) {
            int size = Integer.parseInt(UserUtil.getCyderUser().getFontsize());
            size += e.getWheelRotation() == WHEEL_UP ? 1 : -1;

            if (size > PropLoader.getInteger(MAX_FONT_SIZE)
                    || size < PropLoader.getInteger(MIN_FONT_SIZE)) {
                return;
            }

            try {
                String fontName = UserUtil.getCyderUser().getFont();
                int fontMetric = Integer.parseInt(PropLoader.getString(FONT_METRIC));
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
    @SuppressWarnings("MagicConstant") // Font metric is always checked before use
    public Font generateUserFont() {
        int metric = Integer.parseInt(PropLoader.getString(FONT_METRIC));

        if (NumberUtil.isValidFontMetric(metric)) {
            return new Font(UserUtil.getCyderUser().getFont(), metric,
                    Integer.parseInt(UserUtil.getCyderUser().getFontsize()));
        } else {
            return new Font(UserUtil.getCyderUser().getFont(), Font.BOLD,
                    Integer.parseInt(UserUtil.getCyderUser().getFontsize()));
        }
    }

    // ----------------
    // Background logic
    // ----------------

    /**
     * Initializes the backgrounds associated with the current user.
     * Also attempts to find the background index of the Console current background if it exists.
     */
    public void reloadBackgrounds() {
        try {
            ArrayList<File> backgroundFiles = new ArrayList<>();

            File[] backgroundFilesArr = OSUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                    getUuid(), UserFile.BACKGROUNDS.getName()).listFiles();
            if (backgroundFilesArr != null && backgroundFilesArr.length > 0) {
                Arrays.stream(backgroundFilesArr).forEach(file -> {
                    if (StringUtil.in(FileUtil.getExtension(file),
                            true, FileUtil.SUPPORTED_IMAGE_EXTENSIONS)) {
                        backgroundFiles.add(file);
                    }
                });
            }

            if (backgroundFiles.isEmpty()) {
                backgroundFiles.add(UserUtil.createDefaultBackground(uuid));
            }

            backgrounds.clear();

            backgroundFiles.forEach(backgroundFile -> {
                if (ImageUtil.isValidImage(backgroundFile)) {
                    backgrounds.add(new ConsoleBackground(backgroundFile));
                }
            });

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
    public ArrayList<ConsoleBackground> reloadAndGetBackgrounds() {
        reloadBackgrounds();
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

                if (StringUtil.isNullOrEmpty(filename)) {
                    backgroundIndex = 0;
                    return;
                }

                for (int i = 0 ; i < backgrounds.size() ; i++) {
                    if (FileUtil.getFilename(backgrounds.get(i).getReferenceFile()).equals(filename)) {
                        backgroundIndex = i;
                        return;
                    }
                }
            }
        }

        backgroundIndex = 0;
    }

    /**
     * Sets the background to the provided file in the user's backgrounds directory provided it exists.
     *
     * @param backgroundFile the background file to set the console to
     */
    public void setBackgroundFile(File backgroundFile) {
        Preconditions.checkNotNull(backgroundFile);
        Preconditions.checkArgument(backgroundFile.exists());

        setBackgroundFile(backgroundFile, false);
    }

    /**
     * Sets the background to the provided file in the user's backgrounds directory provided it exists.
     *
     * @param backgroundFile        the background file to set the console to
     * @param maintainSizeAndCenter whether to maintain the current console frame size and center
     */
    public void setBackgroundFile(File backgroundFile, boolean maintainSizeAndCenter) {
        Preconditions.checkNotNull(backgroundFile);
        Preconditions.checkArgument(backgroundFile.exists());

        reloadBackgrounds();

        int index = -1;

        for (int i = 0 ; i < backgrounds.size() ; i++) {
            if (backgrounds.get(i).getReferenceFile().getAbsolutePath().equals(backgroundFile.getAbsolutePath())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            setBackgroundIndex(index, maintainSizeAndCenter);
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
    @SuppressWarnings("UnusedMethod")
    private void setBackgroundIndex(int index) {
        setBackgroundIndex(index, false);
    }

    /**
     * Sets the background index to the provided index
     * if valid and switches to that background.
     *
     * @param index                 the index to switch the console background to
     * @param maintainSizeAndCenter whether to maintain the current console frame size and center
     */
    private void setBackgroundIndex(int index, boolean maintainSizeAndCenter) {
        reloadBackgrounds();

        if (index < 0 || index > backgrounds.size() - 1) return;

        Dimension originalSize = consoleCyderFrame.getSize();
        Point center = consoleCyderFrame.getCenterPointOnScreen();

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

        if (maintainSizeAndCenter) {
            consoleCyderFrame.setSize(originalSize);
            consoleCyderFrame.setCenterPoint(center);
        } else {
            consoleCyderFrame.setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());
            consoleCyderFrame.setLocation((int) (center.getX() - (imageIcon.getIconWidth()) / 2),
                    (int) (center.getY() - (imageIcon.getIconHeight()) / 2));
        }

        // Tooltip based on image name
        getConsoleCyderFrameContentPane().setToolTipText(
                FileUtil.getFilename(getCurrentBackground().getReferenceFile().getName()));

        revalidateInputAndOutputBounds();
        inputField.requestFocus();
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
        if (backgroundSwitchingLocked.get()) return;
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

        JLabel contentPane = getConsoleCyderFrameContentPane();
        contentPane.setToolTipText(FileUtil.getFilename(getCurrentBackground().getReferenceFile().getName()));

        ImageIcon nextBackFinal = nextBackground;
        ImageIcon oldBack = ImageUtil.resizeImage((ImageIcon) contentPane.getIcon(), width, height);

        Point originalCenter = consoleCyderFrame.getCenterPointOnScreen();
        consoleCyderFrame.setSize(width, height);

        // Bump frame into bounds if new size pushed part out of bounds
        UiUtil.requestFramePosition((int) originalCenter.getX() - width / 2,
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

            // Revalidate bounds to be safe
            boolean fullscreen = isFullscreen();
            revalidate(!fullscreen, fullscreen);

            defaultFocusOwner.requestFocus();

            outputArea.setFocusable(outputAreaWasFocusable);

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
     * @param fullscreen whether to set the frame to fullscreen mode
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
        reloadBackgrounds();
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
    // Command history mods
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
    // Ui getters
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
     * Returns the current console background accounting for the console direction.
     *
     * @return the current console background accounting for the console direction
     */
    public ImageIcon getCurrentRotatedConsoleBackground() {
        return switch (consoleDir) {
            case TOP -> getCurrentBackground().generateImageIcon();
            case LEFT -> new ImageIcon(ImageUtil.getRotatedImage(
                    getCurrentBackground().getReferenceFile().getAbsolutePath(), Direction.LEFT));
            case RIGHT -> new ImageIcon(ImageUtil.getRotatedImage(
                    getCurrentBackground().getReferenceFile().getAbsolutePath(), Direction.RIGHT));
            case BOTTOM -> new ImageIcon(ImageUtil.getRotatedImage(
                    getCurrentBackground().getReferenceFile().getAbsolutePath(), Direction.BOTTOM));
        };
    }

    /**
     * Revalidates the Console size, bounds, background, menu, clock, audio menu, draggable property, etc.
     * based on the current background. Note that maintainDirection trumps maintainFullscreen.
     * <p>
     * Order of priority is as follows: maintainDirection > maintainFullscreen.
     * Neither of these affect maintainConsoleSize.
     *
     * @param maintainDirection   whether to maintain the console direction
     * @param maintainFullscreen  whether to maintain fullscreen mode
     * @param maintainConsoleSize whether to maintain the currently set size of the console
     */
    public void revalidate(boolean maintainDirection, boolean maintainFullscreen, boolean maintainConsoleSize) {
        Point originalCenter = consoleCyderFrame.getCenterPointOnScreen();

        ImageIcon background;

        if (maintainDirection) {
            background = getCurrentRotatedConsoleBackground();

            UserUtil.getCyderUser().setFullscreen("0");
            consoleCyderFrame.setShouldAnimateOpacity(true);
        } else if (maintainFullscreen && UserUtil.getCyderUser().getFullscreen().equals("1")) {
            // Setup fullscreen on current monitor
            background = ImageUtil.resizeImage(getCurrentBackground().generateImageIcon(),
                    (int) consoleCyderFrame.getMonitorBounds().getWidth(),
                    (int) consoleCyderFrame.getMonitorBounds().getHeight());
            consoleCyderFrame.setShouldAnimateOpacity(false);
        } else {
            background = getCurrentBackground().generateImageIcon();
        }

        if (maintainConsoleSize) {
            switch (consoleDir) {
                case TOP: // Fallthrough
                case BOTTOM:
                    if (lastConsoleDir == Direction.LEFT || lastConsoleDir == Direction.RIGHT) {
                        background = ImageUtil.resizeImage(background, getHeight(), getWidth());
                    } else {
                        background = ImageUtil.resizeImage(background, getWidth(), getHeight());
                    }
                    break;
                case LEFT: // Fallthrough
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

        UiUtil.requestFramePosition((int) originalCenter.getX() - w / 2,
                (int) originalCenter.getY() - h / 2, consoleCyderFrame);

        revalidateInputAndOutputBounds();

        refreshConsoleMaxSize();

        // This takes care of offset of input field and output area too
        revalidateMenu();

        consoleCyderFrame.refreshBackground();
        consoleCyderFrame.setDraggingEnabled(!isFullscreen());

        revalidateCustomMenuBounds();
        revalidateAudioMenuBounds();
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
     * Sets the visibility of the audio controls button to true.
     */
    public void showAudioButton() {
        toggleAudioControls.setVisible(true);
    }

    /**
     * Revalidates the console menu bounds and places
     * it where it in the proper spot depending on if it is shown.
     * The taskbar icons are also regenerated and shown.
     */
    public void revalidateMenu() {
        if (consoleClosed.get() || menuLabel == null) return;

        installMenuTaskbarIcons();

        // revalidate bounds if needed and change icon
        if (menuLabel.isVisible()) {
            menuLabel.setBounds(menuLabelShowingX, menuLabelShowingY,
                    menuLabel.getWidth(), consoleCyderFrame.getHeight()
                            - CyderDragLabel.DEFAULT_HEIGHT - 5);
            menuScroll.setBounds(10, 10,
                    menuLabel.getWidth() - 10, menuLabel.getHeight() - 20);
        }

        revalidateInputAndOutputBounds();
    }

    /**
     * Smoothly animates out the console audio controls.
     */
    private void animateOutAudioControls() {
        String CONSOLE_AUDIO_MENU_MINIMIZER_THREAD_NAME = "Console Audio Menu Minimizer";
        int decrement = 8;
        int sleepTime = 10;

        CyderThreadRunner.submit(() -> {
            for (int i = audioControlsLabel.getY() ; i > -AUDIO_MENU_LABEL_HEIGHT ; i -= decrement) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                        - audioControlsLabel.getWidth() - AUDIO_MENU_X_OFFSET, i);
                ThreadUtil.sleep(sleepTime);
            }
            audioControlsLabel.setVisible(false);
        }, CONSOLE_AUDIO_MENU_MINIMIZER_THREAD_NAME);
    }

    /**
     * Smooth animates out and removes the audio controls button.
     */
    public void animateOutAndRemoveAudioControls() {
        String CONSOLE_AUDIO_MENU_MINIMIZER_THREAD_NAME = "Console Audio Menu Minimizer";
        int decrement = 8;
        int delay = 10;

        CyderThreadRunner.submit(() -> {
            for (int i = audioControlsLabel.getY() ; i > -AUDIO_MENU_LABEL_HEIGHT ; i -= decrement) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                        - audioControlsLabel.getWidth() - AUDIO_MENU_X_OFFSET, i);
                ThreadUtil.sleep(delay);
            }
            audioControlsLabel.setVisible(false);
            removeAudioControls();
        }, CONSOLE_AUDIO_MENU_MINIMIZER_THREAD_NAME);
    }

    /**
     * Smoothly animates in the audio controls.
     */
    private void animateInAudioControls() {
        CyderThreadRunner.submit(() -> {
            generateAudioMenu();
            audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                    - audioControlsLabel.getWidth() - AUDIO_MENU_X_OFFSET, -AUDIO_MENU_LABEL_HEIGHT);
            audioControlsLabel.setVisible(true);
            for (int i = -AUDIO_MENU_LABEL_HEIGHT ; i < CyderDragLabel.DEFAULT_HEIGHT - 2 ; i += 8) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                        - audioControlsLabel.getWidth() - AUDIO_MENU_X_OFFSET, i);
                ThreadUtil.sleep(10);
            }
            audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                    - audioControlsLabel.getWidth() - AUDIO_MENU_X_OFFSET, CyderDragLabel.DEFAULT_HEIGHT - 2);
        }, "Console Audio Menu Minimizer");
    }

    /**
     * Revalidates the visibility audio menu and the play/pause button based on if audio is playing.
     */
    public void revalidateAudioMenuVisibility() {
        if (!AudioPlayer.isWidgetOpen() && !IOUtil.generalAudioPlaying()) {
            if (audioControlsLabel.isVisible()) {
                animateOutAndRemoveAudioControls();
            } else {
                removeAudioControls();
            }
        } else {
            if (!audioControlsLabel.isVisible()) {
                audioControlsLabel.setLocation(audioControlsLabel.getX(), -AUDIO_MENU_LABEL_HEIGHT);
                toggleAudioControls.setVisible(true);
            }

            revalidateAudioMenuPlayPauseButton();
        }
    }

    /**
     * Revalidates the play pause audio label button icon.
     */
    @ForReadability
    private void revalidateAudioMenuPlayPauseButton() {
        if (IOUtil.generalAudioPlaying() || AudioPlayer.isAudioPlaying()) {
            playPauseAudioLabel.setIcon(AudioIcons.pauseIcon);
        } else {
            playPauseAudioLabel.setIcon(AudioIcons.playIcon);
        }
    }

    /**
     * Hides the audio controls menu and toggle button.
     */
    private void removeAudioControls() {
        audioControlsLabel.setVisible(false);
        toggleAudioControls.setVisible(false);
        consoleCyderFrame.getTopDragLabel().refreshRightButtons();
    }

    /**
     * The number of audio menu buttons.
     */
    private static final int AUDIO_MENU_BUTTONS = 3;

    /**
     * The size of each audio menu button.
     */
    private static final int AUDIO_MENU_BUTTON_SIZE = 30;

    /**
     * The height of the audio menu.
     */
    private static final int AUDIO_MENU_LABEL_HEIGHT = 40;

    /**
     * The padding between buttons for the audio menu.
     */
    private static final int AUDIO_MENU_X_PADDING = 10;

    /**
     * The audio menu button y padding.
     */
    private static final int AUDIO_MENU_BUTTON_Y_PADDING = (AUDIO_MENU_LABEL_HEIGHT - AUDIO_MENU_BUTTON_SIZE) / 2;

    /**
     * The width of the audio menu.
     */
    private static final int AUDIO_MENU_LABEL_WIDTH = AUDIO_MENU_BUTTON_SIZE * AUDIO_MENU_BUTTONS
            + AUDIO_MENU_X_PADDING * (AUDIO_MENU_BUTTONS + 1);

    /**
     * The offset between the end of the audio menu label and the end of the console frame.
     */
    private static final int AUDIO_MENU_X_OFFSET = 6;

    /**
     * The tooltip for the previous audio menu button.
     */
    private static final String PREVIOUS = "Previous";

    /**
     * The tooltip for the play pause audio menu button.
     */
    private static final String PLAY_PAUSE = "Play/Pause";

    /**
     * The tooltip for the skip audio menu button.
     */
    private static final String SKIP = "Skip";

    /**
     * Returns the x value to place the audio menu at.
     *
     * @return the x value to place the audio menu at
     */
    private int calculateAudioMenuX() {
        return consoleCyderFrame.getWidth() - AUDIO_MENU_LABEL_WIDTH - AUDIO_MENU_X_OFFSET;
    }

    /**
     * Generates the audio menu label and the button components.
     */
    private void generateAudioMenu() {
        audioControlsLabel = new JLabel();
        audioControlsLabel.setBounds(
                calculateAudioMenuX(),
                -AUDIO_MENU_LABEL_HEIGHT,
                AUDIO_MENU_LABEL_WIDTH,
                AUDIO_MENU_LABEL_HEIGHT);
        audioControlsLabel.setOpaque(true);
        audioControlsLabel.setBackground(CyderColors.getGuiThemeColor());
        audioControlsLabel.setBorder(new LineBorder(Color.black, 5));
        audioControlsLabel.setVisible(false);
        consoleCyderFrame.getIconPane().add(audioControlsLabel, JLayeredPane.MODAL_LAYER);

        int currentX = AUDIO_MENU_X_PADDING;

        JLabel lastMusicLabel = new JLabel();
        lastMusicLabel.setBounds(currentX, AUDIO_MENU_BUTTON_Y_PADDING,
                AUDIO_MENU_BUTTON_SIZE, AUDIO_MENU_BUTTON_SIZE);
        lastMusicLabel.setIcon(AudioIcons.lastIcon);
        lastMusicLabel.setToolTipText(PREVIOUS);
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

        currentX += AUDIO_MENU_X_PADDING + AUDIO_MENU_BUTTON_SIZE;

        playPauseAudioLabel = new JLabel();
        playPauseAudioLabel.setBounds(currentX, AUDIO_MENU_BUTTON_Y_PADDING,
                AUDIO_MENU_BUTTON_SIZE, AUDIO_MENU_BUTTON_SIZE);
        playPauseAudioLabel.setToolTipText(PLAY_PAUSE);
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

        revalidateAudioMenuPlayPauseButton();

        audioControlsLabel.add(playPauseAudioLabel);

        currentX += AUDIO_MENU_X_PADDING + AUDIO_MENU_BUTTON_SIZE;

        JLabel nextMusicLabel = new JLabel();
        nextMusicLabel.setBounds(currentX, AUDIO_MENU_BUTTON_Y_PADDING, AUDIO_MENU_BUTTON_SIZE, AUDIO_MENU_BUTTON_SIZE);
        nextMusicLabel.setIcon(AudioIcons.nextIcon);
        audioControlsLabel.add(nextMusicLabel);
        nextMusicLabel.setToolTipText(SKIP);
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
        if (UiUtil.notNullAndVisible(menuLabel)) {
            menuLabel.setBackground(CyderColors.getGuiThemeColor());
            audioControlsLabel.repaint();
            menuScroll.setBackground(CyderColors.getGuiThemeColor());
            menuScroll.repaint();
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
        consoleCyderFrame.setLocationOnScreen(screenPos);

        getPinnedFrames().forEach(relativeFrame ->
                relativeFrame.frame().setLocation(
                        relativeFrame.xOffset() + consoleCyderFrame.getX(),
                        relativeFrame.yOffset() + consoleCyderFrame.getY())
        );
    }

    /**
     * The record used for frames pinned to the console.
     */
    private record RelativeFrame(CyderFrame frame, int xOffset, int yOffset) {}

    /**
     * Returns a list of all frames that are pinned to the Console.
     *
     * @return a list of all frames that are pinned to the Console
     */
    private ArrayList<RelativeFrame> getPinnedFrames() {
        ArrayList<RelativeFrame> ret = new ArrayList<>();

        ImmutableList.copyOf(UiUtil.getCyderFrames().stream()
                .filter(frame -> frame.isConsole() && !frame.isConsole()
                        && MathUtil.rectanglesOverlap(consoleCyderFrame.getBounds(), frame.getBounds()))
                .collect(Collectors.toList())).forEach(frame -> {
            int xOffset = frame.getX() - consoleCyderFrame.getX();
            int yOffset = frame.getY() - consoleCyderFrame.getY();
            ret.add(new RelativeFrame(frame, xOffset, yOffset));
        });

        return ret;
    }

    /**
     * Refreshes the consoleCyderFrame painted title to display the console clock in the specified pattern if enabled.
     */
    public void refreshClockText() {
        boolean showClock = UserUtil.getCyderUser().getClockonconsole().equals("1");
        if (!showClock) {
            consoleCyderFrame.setCyderFrameTitle("");
            return;
        }

        String regularSecondTime = TimeUtil.consoleSecondTime();
        String regularNoSecondTime = TimeUtil.consoleNoSecondTime();
        String userConfiguredTime = TimeUtil.userFormattedTime();

        // No custom pattern so take into account showSeconds
        if (userConfiguredTime.equalsIgnoreCase(regularSecondTime)
                || userConfiguredTime.equalsIgnoreCase(regularNoSecondTime)) {
            boolean showSeconds = UserUtil.getCyderUser().getShowseconds().equalsIgnoreCase("1");
            userConfiguredTime = showSeconds ? regularSecondTime : regularNoSecondTime;
        }

        consoleCyderFrame.setCyderFrameTitle(userConfiguredTime);
    }

    /**
     * Simply closes the console frame.
     *
     * @param exit       whether to exit Cyder upon closing the Console
     * @param logoutUser whether to log out the currently logged-in user
     */
    public void closeFrame(boolean exit, boolean logoutUser) {
        consoleClosed.set(true);

        saveScreenStat();
        IOUtil.stopGeneralAudio();

        if (baseInputHandler != null) {
            baseInputHandler.killThreads();
            baseInputHandler = null;
        }

        if (logoutUser) {
            Logger.log(Logger.Tag.LOGOUT, UserUtil.getCyderUser().getName());
            UserUtil.getCyderUser().setLoggedin("0");
        }

        if (exit) consoleCyderFrame.addPostCloseAction(() -> OSUtil.exit(ExitCondition.GenesisControlledExit));
        consoleCyderFrame.dispose();
    }

    /**
     * Returns whether the Console is closed.
     *
     * @return whether the Console is closed
     */
    public boolean isClosed() {
        return consoleClosed.get();
    }

    /**
     * Saves the console's position and window stats to the currently logged-in user's json file.
     */
    public void saveScreenStat() {
        if (consoleCyderFrame == null) return;
        if (consoleCyderFrame.getState() == FRAME_ICONIFIED) return;
        if (getUuid() == null) return;

        ScreenStat screenStat = UserUtil.getCyderUser().getScreenStat();
        screenStat.setConsoleWidth(consoleCyderFrame.getWidth());
        screenStat.setConsoleHeight(consoleCyderFrame.getHeight());
        screenStat.setConsoleOnTop(consoleCyderFrame.isAlwaysOnTop());
        screenStat.setMonitor(Integer.parseInt(consoleCyderFrame.getGraphicsConfiguration()
                .getDevice().getIDstring().replaceAll(CyderRegexPatterns.nonNumberRegex, "")));

        screenStat.setConsoleX(consoleCyderFrame.getX());
        screenStat.setConsoleY(consoleCyderFrame.getY());

        screenStat.setConsoleDirection(consoleDir);

        if (!isClosed()) {
            UserUtil.getCyderUser().setScreenStat(screenStat);
            UserUtil.writeUser();
        }
    }

    /**
     * Closes the CyderFrame and shows the LoginFrame
     * relative to where Console was closed.
     */
    public void logout() {
        closeFrame(false, true);
        UiUtil.closeAllFrames(true);

        IOUtil.stopAllAudio();
        NetworkUtil.endDecentPingChecker();
        LoginHandler.showGui();
    }

    // -----------------------------------------------------
    // Dancing (Get up on teh floor, dancing all night long)
    // -----------------------------------------------------

    /**
     * A record for a frame to dance.
     */
    private record RestoreFrame(CyderFrame frame, int restoreX, int restoreY, boolean draggingWasEnabled) {
        /**
         * Restores the encapsulated frame's original location and re-enables
         * dragging if it was enabled prior to dancing.
         */
        public void restore() {
            frame.setLocation(restoreX, restoreY);

            if (draggingWasEnabled) {
                frame.enableDragging();
            }
        }
    }

    /**
     * Invokes dance in a synchronous way on all CyderFrame instances.
     */
    public void dance() {
        LinkedList<RestoreFrame> restoreFrames = new LinkedList<>();
        UiUtil.getCyderFrames().forEach(frame -> {
            restoreFrames.add(new RestoreFrame(frame, frame.getX(), frame.getY(), frame.isDraggingEnabled()));
            frame.disableDragging();
        });

        currentlyDancing = true;

        while (currentlyDancing && !allFramesFinishedDancing()) {
            UiUtil.getCyderFrames().forEach(CyderFrame::danceStep);
        }

        stopDancing();
        restoreFrames.forEach(RestoreFrame::restore);
    }

    /**
     * Ends the dancing sequence if ongoing.
     */
    public void stopDancing() {
        currentlyDancing = false;
        UiUtil.getCyderFrames().forEach(CyderFrame::resetDancing);
    }

    /**
     * Returns whether all frames have completed a dance iteration.
     *
     * @return whether all frames have completed a dance iteration
     */
    private boolean allFramesFinishedDancing() {
        return UiUtil.getCyderFrames().stream().anyMatch(frame -> !frame.isDancingFinished());
    }

    /**
     * Sets the background of the console to whatever is behind it.
     * This was the original implementation of frame chams functionality before
     * the windows were actually set to be transparent.
     */
    public void originalChams() {
        try {
            CyderFrame ref = getConsoleCyderFrame();
            Rectangle monitorBounds = ref.getMonitorBounds();

            consoleCyderFrame.setVisible(false);
            BufferedImage capture = getInputHandler().getRobot().createScreenCapture(monitorBounds);
            consoleCyderFrame.setVisible(true);

            capture = ImageUtil.cropImage(capture, (int) (Math.abs(monitorBounds.getX()) + ref.getX()),
                    (int) (Math.abs(monitorBounds.getY()) + ref.getY()), ref.getWidth(), ref.getHeight());

            setBackground(ImageUtil.toImageIcon(capture));
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
     * The exception text for a title notify call with too short if a visible duration.
     */
    private static final String TITLE_NOTIFY_LENGTH_TOO_SHORT = "A user probably won't"
            + " see a message with that short of a duration";

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
        Preconditions.checkArgument(visibleDuration > 500, TITLE_NOTIFY_LENGTH_TOO_SHORT);

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
        if (consoleCyderFrame == null || titleNotifyLabel.getText().isEmpty()) return;

        int w = titleNotifyLabel.getWidth();
        int h = titleNotifyLabel.getHeight();

        Point center = consoleCyderFrame.getCenterPointOnFrame();

        titleNotifyLabel.setLocation(
                (int) (center.getX() - NOTIFICATION_PADDING - w / 2),
                (int) (center.getY() - NOTIFICATION_PADDING - h / 2));

        consoleCyderFrame.repaint();
    }

    /**
     * Adds the provided frame to {@link #frameTaskbarExceptions}.
     *
     * @param frame the frame to add as an exception
     */
    public void addToFrameTaskbarExceptions(CyderFrame frame) {
        Preconditions.checkNotNull(frame);
        frameTaskbarExceptions = ImmutableList.<CyderFrame>builder().addAll(frameTaskbarExceptions).add(frame).build();
    }

    /**
     * Removes the provided frame from {@link #frameTaskbarExceptions} if it is contained.
     *
     * @param frame the frame to remove
     */
    public void removeFrameTaskbarException(CyderFrame frame) {
        Preconditions.checkNotNull(frame);

        frameTaskbarExceptions = ImmutableList.copyOf(frameTaskbarExceptions.stream()
                .filter(streamingFrame -> !streamingFrame.equals(frame)).collect(Collectors.toList()));
    }
}
