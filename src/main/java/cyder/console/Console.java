package cyder.console;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.audio.AudioIcons;
import cyder.audio.GeneralAndSystemAudioPlayer;
import cyder.audio.player.AudioPlayer;
import cyder.bounds.BoundsString;
import cyder.bounds.BoundsUtil;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderRegexPatterns;
import cyder.enumerations.Direction;
import cyder.enumerations.Dynamic;
import cyder.enumerations.ExitCondition;
import cyder.enumerations.SystemPropertyKey;
import cyder.exceptions.FatalException;
import cyder.files.FileUtil;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.input.TestHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.login.LoginHandler;
import cyder.managers.CyderVersionManager;
import cyder.managers.ProgramModeManager;
import cyder.managers.RobotManager;
import cyder.math.AngleUtil;
import cyder.math.GeometryUtil;
import cyder.math.NumberUtil;
import cyder.meta.CyderSplash;
import cyder.meta.ProgramState;
import cyder.meta.ProgramStateManager;
import cyder.network.NetworkUtil;
import cyder.props.Props;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.time.SpecialDay;
import cyder.time.TimeUtil;
import cyder.ui.UiConstants;
import cyder.ui.UiUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.button.*;
import cyder.ui.field.CyderCaret;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.enumerations.ScreenPosition;
import cyder.ui.frame.enumerations.TitlePosition;
import cyder.ui.frame.notification.NotificationBuilder;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollPane;
import cyder.user.UserDataManager;
import cyder.user.UserEditor;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.user.data.MappedExecutable;
import cyder.user.data.ScreenStat;
import cyder.utils.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static cyder.console.ConsoleConstants.*;
import static cyder.strings.CyderStrings.*;

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
        Logger.log(LogTag.OBJECT_CREATION, "Console singleton constructed");
    }

    /**
     * The hashmap of frame secret ids to the frame object. Any caller passing a valid hash which maps
     * to a frame taskbar exception has the power to remove it.
     */
    private final ConcurrentHashMap<String, CyderFrame> frameTaskbarExceptions = new ConcurrentHashMap<>();

    /**
     * The UUID of the user currently associated with the Console.
     */
    private String uuid;

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
     * The close button for the drag label right button list.
     */
    private CloseButton closeButton;

    /**
     * The change size button for the drag label right button list.
     */
    private ChangeSizeButton changeSizeButton;

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
     * Performs Console setup routines before constructing
     * the frame and setting its visibility, location, and size.
     *
     * @param uuid the uuid of the user to be linked to this instance of the console
     * @throws IllegalStateException if the Console was left open
     */
    public void initializeAndLaunch(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(!uuid.isEmpty());
        Preconditions.checkState(isClosed());
        consoleClosed.set(false);

        this.uuid = uuid;

        UserDataManager.INSTANCE.initialize(uuid);
        UserUtil.logoutAllUsers();
        UserDataManager.INSTANCE.setLoggedIn(true);
        UserUtil.deleteInvalidBackgrounds(uuid);

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

        initializeBusyIcon();

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

        if (!isFullscreen()) restoreFromPreviousScreenStat(consoleIcon);
        finalizeFrameAndInputOutputBounds();

        performSpecialDayChecks();

        if (UserDataManager.INSTANCE.shouldShowDebugStats()) showDebugStats();

        checkForTestingMode();

        performTimingChecks();

        introMusicCheck();
    }

    /**
     * Finalizes the frame's location and bounds and the bounds/focus
     * ownership of the input and output fields.
     */
    private void finalizeFrameAndInputOutputBounds() {
        consoleCyderFrame.finalizeAndShowCurrentLocation();
        consoleCyderFrame.toFront();

        revalidateInputAndOutputBounds(true);

        setInputFieldCaretPositionToEnd();
    }

    /**
     * Checks for testing mode from the props which will invoke all
     * public static void methods found annotated with {@link cyder.annotations.CyderTest}.
     */
    private void checkForTestingMode() {
        String currentProgramMode = ProgramModeManager.INSTANCE.getProgramMode().getName();
        Logger.log(LogTag.CONSOLE_LOAD, openingBracket + OsUtil.getOsUsername()
                + closingBracket + space + openingBracket + currentProgramMode + closingBracket);
        if (Props.testingMode.getValue()) TestHandler.invokeDefaultTests();
    }

    /**
     * Performs timing checks when the console loads such as figuring out how long it has been
     * since the user last started Cyder and informing them with a welcome back notification if it
     * has been a while. The console load time is also printed to the output area.
     */
    private void performTimingChecks() {
        // Session start logic
        long lastStart = UserDataManager.INSTANCE.getLastSessionStart();
        long millisSinceLastStart = System.currentTimeMillis() - lastStart;
        if (TimeUtil.millisToDays(millisSinceLastStart) > ACCEPTABLE_DAYS_WITHOUT_USE) {
            String username = UserDataManager.INSTANCE.getUsername();
            consoleCyderFrame.notify("Welcome back, " + username);
            Logger.log(LogTag.DEBUG, "Last start by" + space + username
                    + space + TimeUtil.formatMillis(millisSinceLastStart) + space + "ago");
        }
        UserDataManager.INSTANCE.setLastSessionStart(System.currentTimeMillis());

        // Welcome message logic
        if (!UserDataManager.INSTANCE.hasShownWelcomeMessage()) {
            String boldUsername = HtmlUtil.applyBold(UserDataManager.INSTANCE.getUsername());
            String notifyText = "Welcome to Cyder, " + boldUsername + "! Type \"help\" for command assists";
            titleNotify(HtmlUtil.surroundWithHtmlTags(notifyText),
                    CyderFonts.DEFAULT_FONT_LARGE, Duration.ofMillis(6000));
            UserDataManager.INSTANCE.setShownWelcomeMessage(true);
        }

        // Year anniversary logic
        long accountActiveTime = System.currentTimeMillis() - UserDataManager.INSTANCE.getAccountCreationTime();
        int days = (int) Math.floor(TimeUtil.millisToDays(accountActiveTime));
        if (days % (int) TimeUtil.daysInYear == 0) {
            int years = (int) (days / TimeUtil.daysInYear);
            titleNotify("You've been using Cyder for " + years + " years! That's a long time, thank you.",
                    CyderFonts.DEFAULT_FONT_LARGE, Duration.ofMillis(4000));
        }

        // Load time logic
        long loadTime = Instant.now().minusMillis(consoleLoadStartTime.toEpochMilli()).toEpochMilli();
        if (initialConsoleLoad) {
            loadTime = JvmUtil.getRuntime();
            initialConsoleLoad = false;
        }
        baseInputHandler.println("Console loaded in " + TimeUtil.formatMillis(loadTime));
    }

    private boolean initialConsoleLoad = true;

    /**
     * The time at which the console load was started.
     */
    private Instant consoleLoadStartTime;

    /**
     * Sets the time at which teh console load was started to now.
     */
    public void setConsoleLoadStartTime() {
        consoleLoadStartTime = Instant.now();
    }

    /**
     * The ratio of the busy width length to the console width.
     */
    private final int busyIconToConsoleWidthRatio = 8;

    /**
     * The height of the busy icon.
     */
    private final int busyIconHeight = 3;

    /**
     * The busy icon animation increment.
     */
    private final int busyIconAnimationIncrement = 2;

    /**
     * The busy icon animation delay.
     */
    private final int busyIconAnimationDelay = 4;

    /**
     * The delay the busy icon stops between sliding from the right to left and the left to the right.
     */
    private final Duration busyIconAnimationTransitionDelay = Duration.ofMillis(500);

    /**
     * The busy icon for the console.
     */
    private JLabel busyIcon;

    /**
     * Whether the busy icon has been initialized.
     */
    private boolean busyIconInitialized = false;

    /**
     * Whether the busy icon should be showed currently.
     */
    private final AtomicBoolean shouldShowBusyAnimation = new AtomicBoolean();

    /**
     * The starting point of the busy icon.
     */
    private final Point busyIconStartingPoint = new Point(0, 0);

    /**
     * Initializes the console busy icon.
     */
    private void initializeBusyIcon() {
        if (busyIconInitialized) return;
        busyIconInitialized = true;

        int busyIconWidth = consoleCyderFrame.getWidth() / busyIconToConsoleWidthRatio;
        busyIcon = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(CyderColors.vanilla);
                g.fillRect(0, 0, busyIconWidth, busyIconHeight);
            }
        };
        busyIcon.setBounds(0, 0, busyIconWidth, busyIconHeight);
        consoleCyderFrame.getTopDragLabel().add(busyIcon);

        busyIcon.repaint();
        busyIcon.setVisible(false);
    }

    /**
     * Shows the busy animation starting from the beginning
     */
    private void showBusyAnimation() {
        Preconditions.checkState(!shouldShowBusyAnimation.get());

        shouldShowBusyAnimation.set(true);
        busyIcon.setLocation(busyIconStartingPoint);
        busyIcon.repaint();
        busyIcon.setVisible(true);

        CyderThreadRunner.submit(() -> {
            OUTER:
            while (shouldShowBusyAnimation.get()) {
                while (busyIcon.getX() + busyIcon.getWidth() < consoleCyderFrame.getWidth()) {
                    busyIcon.setSize(consoleCyderFrame.getWidth() / busyIconToConsoleWidthRatio, busyIconHeight);
                    busyIcon.setLocation(busyIcon.getX() + busyIconAnimationIncrement, 0);
                    busyIcon.repaint();
                    ThreadUtil.sleep(busyIconAnimationDelay);
                    if (!shouldShowBusyAnimation.get()) break OUTER;
                }

                ThreadUtil.sleep(busyIconAnimationTransitionDelay.toMillis());

                while (busyIcon.getX() > 0) {
                    if (busyIcon.getX() + busyIcon.getWidth() > consoleCyderFrame.getWidth()) {
                        busyIcon.setLocation(consoleCyderFrame.getWidth() - busyIcon.getWidth(), 0);
                    }

                    busyIcon.setSize(consoleCyderFrame.getWidth() / busyIconToConsoleWidthRatio, busyIconHeight);
                    busyIcon.setLocation(busyIcon.getX() - busyIconAnimationIncrement, 0);
                    busyIcon.repaint();
                    ThreadUtil.sleep(busyIconAnimationDelay);
                    if (!shouldShowBusyAnimation.get()) break OUTER;
                }

                ThreadUtil.sleep(busyIconAnimationTransitionDelay.toMillis());
            }

            busyIcon.setVisible(false);
        }, IgnoreThread.ConsoleBusyAnimation.getName());
    }

    /**
     * Hides the busy animation if currently visible.
     */
    public void hideBusyAnimation() {
        shouldShowBusyAnimation.set(false);
    }

    /**
     * Resets private variables to their default state.
     */
    @ForReadability
    private void resetMembers() {
        consoleBashString = UserDataManager.INSTANCE.getUsername() + BASH_STRING_PREFIX;

        lastSlideDirection = DEFAULT_CONSOLE_DIRECTION;
        consoleDir = DEFAULT_CONSOLE_DIRECTION;

        commandIndex = 0;

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
        consoleCyderFrame = new CyderFrame.Builder()
                .setWidth((int) consoleIcon.dimension().getWidth())
                .setHeight((int) consoleIcon.dimension().getHeight())
                .setBackgroundIcon(consoleIcon.background())
                .build();
        consoleCyderFrame.addPostSetBoundsRunnable(() -> {
            revalidateInputAndOutputBounds();
            revalidateConsoleMenuBounds();
            revalidateAudioMenuBounds();
            consoleCyderFrame.revalidateMenu();
            revalidateTitleNotify();
        });
        consoleCyderFrame.addPreMinimizeAndIconifyAction(this::saveScreenStat);
        consoleCyderFrame.addPreCloseAction(() -> {
            outputArea.setFocusable(false);
            outputScroll.setFocusable(false);
            UiUtil.disposeAllFrames(true, consoleCyderFrame);
        });

        // It is intended that the console and splash never show up in the taskbar
        String consoleKey = SecurityUtil.generateUuid();
        String splashKey = SecurityUtil.generateUuid();
        frameTaskbarExceptions.put(consoleKey, consoleCyderFrame);
        frameTaskbarExceptions.put(splashKey, CyderSplash.INSTANCE.getSplashFrame());

        consoleCyderFrame.setBackground(Color.black);
        consoleCyderFrame.addEndDragEventCallback(this::saveScreenStat);
        consoleCyderFrame.setDraggingEnabled(!UserDataManager.INSTANCE.isFullscreen());
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

        boolean randombackground = UserDataManager.INSTANCE.shouldChooseRandomBackground();
        if (randombackground && reloadAndGetBackgrounds().size() > 1) {
            backgroundIndex = NumberUtil.generateRandomInt(backgrounds.size() - 1);
        }

        boolean fullscreen = UserDataManager.INSTANCE.isFullscreen();
        if (fullscreen) {
            int monitorId = UserDataManager.INSTANCE.getScreenStat().getMonitor();
            Rectangle monitorBounds = UiUtil.getGraphicsDevice(monitorId).getDefaultConfiguration().getBounds();

            width = (int) monitorBounds.getWidth();
            height = (int) monitorBounds.getHeight();

            icon = new ImageIcon(ImageUtil.resizeImage(width, height, getCurrentBackground().getReferenceFile()));
        } else {
            BufferedImage bi = getCurrentBackground().generateBufferedImage();
            if (bi == null) throw new FatalException("Generated buffered image is null");

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
     * Refreshes the console super title, that of displaying "Version Cyder [Nathan]".
     */
    public void refreshConsoleSuperTitle() {
        consoleCyderFrame.setTitle(CyderVersionManager.INSTANCE.getVersion()
                + space
                + CyderVersionManager.INSTANCE.getProgramName()
                + space + dash + space
                + UserDataManager.INSTANCE.getUsername());
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

            UiUtil.getCyderFrames().stream().filter(frame -> frame.isPinnedToConsole()
                            && !frame.isConsole()
                            && frame.getRelativeX() != FRAME_NOT_PINNED
                            && frame.getRelativeY() != FRAME_NOT_PINNED)
                    .forEach(frame -> UiUtil.requestFramePosition(
                            new Point(consoleCyderFrame.getX() + frame.getRelativeX(),
                                    consoleCyderFrame.getY() + frame.getRelativeY()), frame
                    ));
        }
    };

    /**
     * The mouse adapter for frame pinned window logic.
     */
    private final MouseAdapter consolePinnedMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (consoleCyderFrame == null || !consoleCyderFrame.isFocusable()
                    || !consoleCyderFrame.isDraggingEnabled()) return;

            UiUtil.getCyderFrames().stream()
                    .filter(frame -> frame.isPinnedToConsole() && !frame.isConsole())
                    .forEach(frame -> {
                        if (GeometryUtil.rectanglesOverlap(
                                consoleCyderFrame.getBounds(), frame.getBounds())) {
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
     * Revalidates the bounds of the custom console menu and the audio controls menu.
     */
    private void revalidateConsoleMenuBounds() {
        if (UiUtil.notNullAndVisible(menuLabel)) {
            menuLabel.setBounds((int) consoleMenuShowingPoint.getX(), (int) consoleMenuShowingPoint.getY(),
                    TASKBAR_MENU_WIDTH, calculateMenuHeight());
        }
    }

    /**
     * The y value for the audio menu after animated on.
     */
    private static final int audioMenuLabelShowingY = CyderDragLabel.DEFAULT_HEIGHT - 2;

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
        outputArea.setCaretColor(UserDataManager.INSTANCE.getForegroundColor());
        outputArea.setCaret(new CyderCaret(UserDataManager.INSTANCE.getForegroundColor()));
        outputArea.setAutoscrolls(true);

        outputArea.setFocusable(true);
        outputArea.setSelectionColor(CyderColors.selectionColor);
        outputArea.setOpaque(false);
        outputArea.setBackground(CyderColors.empty);
        outputArea.setForeground(UserDataManager.INSTANCE.getForegroundColor());
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
        boolean outputBorder = UserDataManager.INSTANCE.shouldDrawOutputBorder();
        Color color = UserDataManager.INSTANCE.getBackgroundColor();
        outputScroll.setBorder(outputBorder
                ? new LineBorder(color, FIELD_BORDER_THICKNESS, false)
                : BorderFactory.createEmptyBorder());

        boolean outputFill = UserDataManager.INSTANCE.shouldDrawOutputFill();
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
        boolean inputBorder = UserDataManager.INSTANCE.shouldDrawInputBorder();
        Color backgroundColor = UserDataManager.INSTANCE.getBackgroundColor();
        inputField.setBorder(inputBorder
                ? new LineBorder(backgroundColor, FIELD_BORDER_THICKNESS, false)
                : BorderFactory.createEmptyBorder());
        inputField.setSelectionColor(CyderColors.selectionColor);
        setInputFieldCaretPositionToEnd();

        inputField.setOpaque(false);
        inputField.setCaretColor(UserDataManager.INSTANCE.getForegroundColor());
        inputField.setCaret(new CyderCaret(UserDataManager.INSTANCE.getForegroundColor()));
        inputField.setForeground(UserDataManager.INSTANCE.getForegroundColor());
        inputField.setFont(generateUserFont());

        installInputFieldListeners();

        boolean inputFill = UserDataManager.INSTANCE.shouldDrawInputFill();
        if (inputFill) {
            inputField.setOpaque(true);
            inputField.setBackground(UserDataManager.INSTANCE.getBackgroundColor());
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
     * Sets up the console location, size, background direction, and pin button state
     * based on the saved stats from the previous session.
     *
     * @param consoleIcon the console icon to use as the background
     */
    @SuppressWarnings("SuspiciousNameCombination") /* Switching with and height vars */
    private void restoreFromPreviousScreenStat(ConsoleIcon consoleIcon) {
        ScreenStat requestedConsoleStats = UserDataManager.INSTANCE.getScreenStat();
        Direction consoleDirection = requestedConsoleStats.getConsoleDirection();

        boolean onTop = requestedConsoleStats.isConsoleOnTop();
        PinButton.PinState state = onTop ? PinButton.PinState.CONSOLE_PINNED : PinButton.PinState.DEFAULT;
        consoleCyderFrame.getTopDragLabel().getPinButton().setState(state);

        double consoleIconWidth = consoleIcon.dimension().getWidth();
        double consoleIconHeight = consoleIcon.dimension().getHeight();
        if (Direction.isHorizontal(consoleDirection)) {
            double tmp = consoleIconHeight;
            consoleIconHeight = consoleIconWidth;
            consoleIconWidth = tmp;
        }

        int requestedConsoleWidth = requestedConsoleStats.getConsoleWidth();
        int requestedConsoleHeight = requestedConsoleStats.getConsoleHeight();

        if (requestedConsoleWidth <= consoleIconWidth
                && requestedConsoleHeight <= consoleIconHeight
                && requestedConsoleWidth >= MINIMUM_SIZE.width
                && requestedConsoleHeight >= MINIMUM_SIZE.height) {
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
        UiUtil.requestFramePosition(new Point(requestedConsoleX, requestedConsoleY), consoleCyderFrame);
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
    private static final NotificationBuilder onlyOneBackgroundNotificationBuilder
            = new NotificationBuilder(onlyOneBackgroundNotificationText)
            .setViewDuration(5000)
            .setOnKillAction(() -> UserEditor.showGui(UserEditor.Page.FILES));

    /**
     * Installs the right drag label buttons for the console frame.
     */
    private void installRightDragLabelButtons() {
        // Remove default close button
        consoleCyderFrame.getTopDragLabel().removeRightButton(2);
        // Add custom close button
        closeButton = new CloseButton();
        closeButton.setFocusPaintable(true);
        closeButton.setClickAction(() -> {
            if (UserDataManager.INSTANCE.shouldMinimizeOnClose()) {
                UiUtil.minimizeAllFrames();
            } else {
                releaseResourcesAndCloseFrame(true);
            }
        });
        closeButton.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.getCause() == FocusEvent.Cause.TRAVERSAL_BACKWARD) {
                    changeSizeButton.requestFocus();
                } else {
                    outputArea.requestFocus();
                }
            }
        });
        consoleCyderFrame.getTopDragLabel().addRightButton(closeButton, 2);

        // Remove default minimize button
        consoleCyderFrame.getTopDragLabel().removeRightButton(0);
        // Add custom minimize button
        MinimizeButton minimizeButton = new MinimizeButton(consoleCyderFrame);
        minimizeButton.setFocusPaintable(true);
        consoleCyderFrame.getTopDragLabel().addRightButton(minimizeButton, 0);

        // Remove default pin button
        consoleCyderFrame.getTopDragLabel().removeRightButton(1);
        //  Add custom pin button
        PinButton pinButton = new PinButton(consoleCyderFrame);
        pinButton.setFocusPaintable(true);
        pinButton.addClickAction(this::saveScreenStat);
        consoleCyderFrame.getTopDragLabel().setPinButton(pinButton);
        consoleCyderFrame.getTopDragLabel().addRightButton(pinButton, 1);

        changeSizeButton = new ChangeSizeButton();
        changeSizeButton.setFocusPaintable(true);
        changeSizeButton.setToolTipText(ALTERNATE_BACKGROUND);
        changeSizeButton.setClickAction(this::attemptToSwitchBackground);
        consoleCyderFrame.getTopDragLabel().addRightButton(changeSizeButton, 2);

        toggleAudioControls = new MenuButton();
        toggleAudioControls.setFocusPaintable(true);
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
        menuButton.setFocusPaintable(true);
        menuButton.setClickAction(this::onMenuButtonClicked);
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
        consoleCyderFrame.setTitlePosition(TitlePosition.CENTER);
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

        KeyStroke debugKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK);
        inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(debugKeystroke, DEBUG_LINES);
        inputField.getActionMap().put(DEBUG_LINES, UiUtil.generateAbstractAction(() -> {
            debugLinesShown.set(!debugLinesShown.get());
            refreshDebugLines();
        }));

        KeyStroke exitKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK);
        inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(exitKeystroke, FORCED_EXIT);
        inputField.getActionMap().put(FORCED_EXIT, UiUtil.generateAbstractAction(
                () -> OsUtil.exit(ExitCondition.ForcedImmediateExit)));
    }

    /**
     * The state of debug lines.
     */
    private final AtomicBoolean debugLinesShown = new AtomicBoolean(false);

    /**
     * Refreshes the state of the debug lines depending on {@link #debugLinesShown}.
     */
    private void refreshDebugLines() {
        getInputHandler().println((debugLinesShown.get() ? "Drawing" : "Erasing") + " debug lines");
        UiUtil.getCyderFrames().forEach(frame -> frame.toggleDebugLines(debugLinesShown.get()));
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
    private void installConsoleResizing() {
        consoleCyderFrame.initializeResizing();
        consoleCyderFrame.setResizable(true);
        consoleCyderFrame.setBackgroundResizing(true);
        consoleCyderFrame.setMinimumSize(MINIMUM_SIZE);
        consoleCyderFrame.setSnapSize(SNAP_SIZE);

        refreshConsoleMaxSize();
    }

    /**
     * Refreshes the maximum size of the console.
     */
    private void refreshConsoleMaxSize() {
        if (getCurrentBackground().getReferenceFile() != null) {
            ImageIcon currentIcon = getCurrentBackground().generateImageIcon();
            int w = currentIcon.getIconWidth();
            int h = currentIcon.getIconHeight();

            if (Direction.isHorizontal(getConsoleDirection())) {
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
        /**
         * {@inheritDoc}
         */
        @Override
        public void windowDeiconified(WindowEvent e) {
            inputField.requestFocus();
            setInputFieldCaretPositionToEnd();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void windowClosed(WindowEvent e) {
            if (consoleClosed.get()) return;

            if (UserDataManager.INSTANCE.shouldMinimizeOnClose()) {
                UiUtil.minimizeAllFrames();
            } else {
                Console.INSTANCE.releaseResourcesAndCloseFrame(true);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void windowOpened(WindowEvent e) {
            inputField.requestFocus();
        }
    };

    /**
     * The chime audio file.
     */
    private final File chimeFile = StaticUtil.getStaticResource("chime.mp3");

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
     * The frequency to check for whether the busy animation should be shown.
     */
    private static final int busyAnimationSleepTime = 3000;

    /**
     * The frequency to check for console disposal in the busy animation checker thread.
     */
    private static final int busyAnimationCheckFrequency = 50;

    /**
     * Begins the console checker executors/threads.
     */
    @ForReadability
    private void startExecutors() {
        CyderThreadRunner.submit(() -> {
            try {
                while (true) {
                    if (!isClosed()) {
                        LocalDateTime now = LocalDateTime.now();
                        int min = now.getMinute();
                        int sec = now.getSecond();
                        int hour = now.getHour();

                        if (min == 0 && sec == 0 && lastChimeHour.get() != hour) {
                            if (UserDataManager.INSTANCE.shouldPlayHourlyChimes()) {
                                GeneralAndSystemAudioPlayer.playSystemAudio(chimeFile);
                                lastChimeHour.set(hour);
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
                        ThreadUtil.sleepWithChecks(CLOCK_REFRESH_SLEEP_TIME, CLOCK_CHECK_FREQUENCY, consoleClosed);
                    } catch (Exception ignored) {
                        // Don't care
                    }
                }
            }
        }, IgnoreThread.ConsoleClockUpdater.getName());

        CyderThreadRunner.submit(() -> {
            try {
                while (true) {
                    boolean busyIcon = UserDataManager.INSTANCE.shouldShowBusyAnimation();
                    if (!isClosed() && busyIcon) {
                        if (ThreadUtil.threadsIndicateCyderBusy()) {
                            shouldShowBusyAnimation.set(false);
                        } else if (!shouldShowBusyAnimation.get()) {
                            showBusyAnimation();
                        }
                    }

                    ThreadUtil.sleepWithChecks(busyAnimationSleepTime, busyAnimationCheckFrequency, consoleClosed);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.CyderBusyChecker.getName());
    }

    /**
     * The thread name of the debug stat finder.
     */
    private static final String DEBUG_STAT_FINDER_THREAD_NAME = "Debug Stat Finder";

    /**
     * The number of days without Cyder use which can pass without a welcome back notification.
     */
    private static final int ACCEPTABLE_DAYS_WITHOUT_USE = 2;

    /**
     * Checks today against all the values of {@link SpecialDay}.
     * If any are today, the notification message is shown to the user.
     */
    private void performSpecialDayChecks() {
        SpecialDay.getSpecialDaysOfToday().forEach(
                specialDay -> consoleCyderFrame.notify(specialDay.getNotificationMessage()));
    }

    /**
     * Shows the debug stats, that being the following:
     * <ul>
     *     <li>{@link StatUtil#getSystemProperties()}</li>
     *     <li>{@link StatUtil#getComputerMemorySpaces()}</li>
     *     <li>{@link SystemPropertyKey#values()}</li>
     *     <li>{@link StatUtil#getDebugProps()}</li>
     * </ul>
     */
    private void showDebugStats() {
        CyderThreadRunner.submit(() -> {
            try {
                StatUtil.getSystemProperties().forEach(property -> getInputHandler().println(property));
                StatUtil.getComputerMemorySpaces().forEach(property -> getInputHandler().println(property));
                Arrays.stream(SystemPropertyKey.values()).forEach(property ->
                        getInputHandler().println(property.getProperty()));

                Future<StatUtil.DebugStats> futureStats = StatUtil.getDebugProps();
                while (!futureStats.isDone()) Thread.onSpinWait();
                StatUtil.DebugStats stats = futureStats.get();

                stats.lines().forEach(line -> getInputHandler().println(line));
                getInputHandler().println(stats.countryFlag());
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, DEBUG_STAT_FINDER_THREAD_NAME);
    }

    /**
     * The default intro music to play if enabled an no user music is present.
     */
    private static final File DEFAULT_INTRO_MUSIC = StaticUtil.getStaticResource("ride.mp3");

    /**
     * The Cyder intro theme file.
     */
    private static final File introTheme = StaticUtil.getStaticResource("introtheme.mp3");

    /**
     * The thread name of the intro music grayscale checker.
     */
    private static final String INTRO_MUSIC_CHECKER_THREAD_NAME = "Intro Music Checker";

    /**
     * Determines what audio to play at the beginning of the Console startup.
     */
    private void introMusicCheck() {
        if (UserDataManager.INSTANCE.shouldPlayIntroMusic()) {
            performIntroMusic();
        } else if (CyderVersionManager.INSTANCE.isReleased()) {
            grayscaleImageCheck();
        }
    }

    /**
     * Plays music from the user's music folder if a file is present. Otherwise the default intro music is played.
     */
    @ForReadability
    private void performIntroMusic() {
        ArrayList<File> musicList = new ArrayList<>();

        File userMusicDir = Dynamic.buildDynamic(
                Dynamic.USERS.getFileName(), uuid, UserFile.MUSIC.getName());

        File[] files = userMusicDir.listFiles();
        if (files != null && files.length > 0) {
            Arrays.stream(files).forEach(file -> {
                if (FileUtil.isSupportedAudioExtension(file)) {
                    musicList.add(file);
                }
            });
        }

        if (!musicList.isEmpty()) {
            int randomFileIndex = NumberUtil.generateRandomInt(files.length - 1);
            GeneralAndSystemAudioPlayer.playGeneralAudio(files[randomFileIndex]);
        } else {
            GeneralAndSystemAudioPlayer.playGeneralAudio(DEFAULT_INTRO_MUSIC);
        }
    }

    /**
     * Checks for a grayscale image and plays a grayscale song if true.
     */
    @ForReadability
    private void grayscaleImageCheck() {
        CyderThreadRunner.submit(() -> {
            try {
                if (ImageUtil.isGrayscale(ImageUtil.read(getCurrentBackground().getReferenceFile()))) {
                    int grayscaleAudioRandomIndex = NumberUtil.generateRandomInt(GRAYSCALE_AUDIO_PATHS.size() - 1);
                    GeneralAndSystemAudioPlayer.playGeneralAudio(GRAYSCALE_AUDIO_PATHS.get(grayscaleAudioRandomIndex));
                } else {
                    GeneralAndSystemAudioPlayer.playGeneralAudio(introTheme);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, INTRO_MUSIC_CHECKER_THREAD_NAME);
    }

    /**
     * The focus adapter for the output area field.
     */
    private final FocusAdapter outputAreaFocusAdapter = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            Color color = UserDataManager.INSTANCE.getBackgroundColor();
            outputScroll.setBorder(new LineBorder(color, 3));
        }

        @Override
        public void focusLost(FocusEvent e) {
            boolean outputBorder = UserDataManager.INSTANCE.shouldDrawOutputBorder();
            if (!outputBorder) {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            if (e.getCause() == FocusEvent.Cause.TRAVERSAL_BACKWARD) {
                closeButton.requestFocus();
            } else {
                inputField.requestFocusInWindow();
                setInputFieldCaretPositionToEnd();
            }
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

        // Remove focus from previous item if possible
        if (currentFocusedMenuItemIndex != -1) {
            state.get(currentFocusedMenuItemIndex).setFocused(false);
        }

        // Wrap around if out of bounds
        if (index < 0) {
            index = state.size() - 1;
        } else if (index > state.size() - 1) {
            index = 0;
        }

        // Give and paint focus on new item
        currentFocusedMenuItemIndex = index;
        state.get(currentFocusedMenuItemIndex).setFocused(true);
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
                .forEach(icon -> icon.setFocused(false));

        if (menuLabel.isVisible()) {
            reinstallCurrentTaskbarIcons();
        }
    }

    /**
     * The logic for when the menu button is pressed.
     */
    private void onMenuButtonClicked() {
        Point menuButtonPointOnScreen = menuButton.getLocationOnScreen();
        Rectangle menuButtonBoundsOnScreen = new Rectangle(
                (int) menuButtonPointOnScreen.getX(),
                (int) menuButtonPointOnScreen.getY(),
                menuButton.getWidth(),
                menuButton.getHeight());

        boolean mouseTriggered = GeometryUtil.pointInOrOnRectangle(
                MouseInfo.getPointerInfo().getLocation(), menuButtonBoundsOnScreen);

        // if there's a focused item and it wasn't a mouse click
        if (currentFocusedMenuItemIndex != -1 && !mouseTriggered) {
            ImmutableList.copyOf(Stream.of(currentFrameMenuItems, currentMappedExeItems, currentDefaultMenuItems)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()))
                    .get(currentFocusedMenuItemIndex).runRunnable();
            return;
        }

        if (menuLabel == null) {
            generateConsoleMenu();
        }

        if (menuLabel.isVisible()) {
            minimizeMenu();
            return;
        }

        animateInMenuLabel();
    }

    /**
     * The name of the animating thread for the console input and output fields.
     */
    private static final String CONSOLE_FIELDS_ANIMATOR_THREAD_NAME = "Console Fields Animator";

    /**
     * The name of the thread for animating in the menu label.
     */
    private static final String MINIMIZE_MENU_THREAD_NAME = "Minimize Console Menu Thread";

    /**
     * The increment in pixels for the menu label and fields for animations.
     */
    private static final int menuAnimationIncrement = 8;

    /**
     * The delay in ms for the menu and fields animations.
     */
    private static final int menuAnimationDelayMs = 5;

    /**
     * The x value the fields should be animated to when the menu label is animating in.
     */
    private static final int fieldsEnterAnimateToX = TASKBAR_MENU_WIDTH + 2 + 15;

    /**
     * Animates the menu label into the frame and animates the output area and input fields
     * to smaller sizes to account for the space taken by the menu.
     */
    private void animateInMenuLabel() {
        CyderThreadRunner.submit(() -> {
            menuLabel.setLocation(consoleMenuHiddenPoint);
            menuLabel.setVisible(true);
            int y = menuLabel.getY();

            for (int i = (int) consoleMenuHiddenPoint.getX()
                 ; i < consoleMenuShowingPoint.getX() ; i += menuAnimationIncrement) {
                menuLabel.setLocation(i, y);
                ThreadUtil.sleep(menuAnimationDelayMs);
            }

            menuLabel.setLocation(consoleMenuShowingPoint);

            revalidateInputAndOutputBounds();
        }, MINIMIZE_MENU_THREAD_NAME);

        CyderThreadRunner.submit(() -> {
            int outputScrollY = outputScroll.getY();
            int outputScrollWidth = outputScroll.getWidth();
            int outputScrollHeight = outputScroll.getHeight();

            int inputFieldY = inputField.getY();
            int inputFieldWidth = inputField.getWidth();
            int inputFieldHeight = inputField.getHeight();

            for (int x = inputField.getX() ; x < fieldsEnterAnimateToX ; x += menuAnimationIncrement) {
                outputScroll.setBounds(x, outputScrollY, outputScrollWidth, outputScrollHeight);
                inputField.setBounds(x, inputFieldY, inputFieldWidth, inputFieldHeight);

                ThreadUtil.sleep(menuAnimationDelayMs);
            }

            revalidateInputAndOutputBounds();
        }, CONSOLE_FIELDS_ANIMATOR_THREAD_NAME);
    }

    /**
     * The key adapter for the menu button to allow "focusing" taskbar items.
     */
    private final KeyAdapter menuButtonKeyAdapter = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();

            if (KeyCodeUtil.downOrRight(code)) {
                focusNextTaskbarMenuItem();
            } else if (KeyCodeUtil.upOrLeft(code)) {
                focusPreviousTaskbarMenuItem();
            }
        }
    };

    /**
     * The current active frames to generate TaskbarIcons for the console's menu.
     */
    private final ArrayList<CyderFrame> currentActiveFrames = new ArrayList<>();

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

        boolean compactMode = UserDataManager.INSTANCE.compactTextMode();

        UiUtil.setJTextPaneDocumentAlignment(menuPane, compactMode
                ? UiUtil.JTextPaneAlignment.LEFT : UiUtil.JTextPaneAlignment.CENTER);

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
        ArrayList<TaskbarIcon> ret = new ArrayList<>();

        if (!currentActiveFrames.isEmpty()) {
            Lists.reverse(currentActiveFrames).forEach(currentFrame -> {
                TaskbarIcon.Builder builder = new TaskbarIcon.Builder(currentFrame.getTitle())
                        .setCompact(compactMode)
                        .setFocused(false)
                        .setBorderColor(currentFrame.getTaskbarIconBorderColor())
                        .setRunnable(UiUtil.generateCommonFrameTaskbarIconRunnable(currentFrame));

                currentFrame.getCustomTaskbarIcon().ifPresent(builder::setCustomIcon);
                ret.add(builder.build());
            });
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
        Collection<MappedExecutable> exes = UserDataManager.INSTANCE.getMappedExecutables().getExecutables();
        ArrayList<TaskbarIcon> ret = new ArrayList<>();

        if (!exes.isEmpty()) {
            exes.forEach(exe -> {
                Runnable runnable = () -> {
                    FileUtil.openResource(exe.getFilepath(), true);
                    exe.displayInvokedNotification();
                };

                ret.add(new TaskbarIcon.Builder(exe.getName())
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
     * The action for when the preferences default taskbar icon is clicked.
     */
    private void onPrefsMenuItemClicked() {
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

        revalidateConsoleTaskbarMenu();
    }

    /**
     * The tooltip and label text for the preferences default taskbar icon.
     */
    private static final String PREFERENCES = "Preferences";

    /**
     * The tooltip and label text for the logout default taskbar icon.
     */
    private static final String LOGOUT = "Logout";

    /**
     * The default compact taskbar icons.
     */
    private final ImmutableList<TaskbarIcon> compactDefaultTaskbarIcons = ImmutableList.of(
            new TaskbarIcon.Builder(PREFERENCES)
                    .setFocused(false)
                    .setCompact(true)
                    .setRunnable(this::onPrefsMenuItemClicked)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build(),
            new TaskbarIcon.Builder(LOGOUT)
                    .setFocused(false)
                    .setCompact(true)
                    .setRunnable(this::logoutCurrentUserAndShowLoginFrame)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build()
    );

    /**
     * The default non compact taskbar icons.
     */
    private final ImmutableList<TaskbarIcon> nonCompactDefaultTaskbarIcons = constructNonCompactDefaultTaskbarIcons();

    /**
     * Constructs and returns the non-compact, default taskbar icons.
     *
     * @return the non-compact, default taskbar icons
     */
    private ImmutableList<TaskbarIcon> constructNonCompactDefaultTaskbarIcons() {
        try {
            TaskbarIcon prefsTaskbarIcon = new TaskbarIcon.Builder(PREFERENCES)
                    .setFocused(false)
                    .setCompact(false)
                    .setRunnable(this::onPrefsMenuItemClicked)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build();

            TaskbarIcon logoutTaskbarIcon = new TaskbarIcon.Builder(LOGOUT)
                    .setFocused(false)
                    .setCompact(false)
                    .setRunnable(this::logoutCurrentUserAndShowLoginFrame)
                    .setBorderColor(CyderColors.taskbarDefaultColor)
                    .build();

            return ImmutableList.of(prefsTaskbarIcon, logoutTaskbarIcon);
        } catch (Exception e) {
            throw new FatalException(e.getMessage());
        }
    }

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
            baseInputHandler.handle(input);

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
     * The increment in degrees for a barrel roll.
     */
    private static final int DEGREE_INCREMENT = 2;

    /**
     * The delay between barrel roll increments.
     */
    private static final int BARREL_ROLL_DELAY = 2;

    /**
     * The thread name for the barrel roll animator.
     */
    private static final String BARREL_ROLL_THREAD_NAME = "Console Barrel Roll Thread";

    /**
     * The object to synchronize on when performing a console barrel roll.
     */
    private static final Object barrelRollLockingObject = new Object();

    /**
     * Performs a barrel roll on the console frame.
     */
    public void barrelRoll() {
        synchronized (barrelRollLockingObject) {
            CyderThreadRunner.submit(() -> {
                BufferedImage currentBi = getCurrentBackground().generateBufferedImage();
                BufferedImage masterImage = ImageUtil.resizeImage(currentBi, currentBi.getType(),
                        consoleCyderFrame.getWidth(), consoleCyderFrame.getHeight());
                for (int i = 0 ; i <= AngleUtil.THREE_SIXTY_DEGREES ; i += DEGREE_INCREMENT) {
                    BufferedImage rotated = ImageUtil.rotateImage(masterImage, i);
                    getConsoleCyderFrameContentPane().setIcon(new ImageIcon(rotated));
                    ThreadUtil.sleep(BARREL_ROLL_DELAY);
                }

                getConsoleCyderFrameContentPane().setIcon(ImageUtil.toImageIcon(masterImage));
            }, BARREL_ROLL_THREAD_NAME);
        }
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
    private static final Point consoleMenuShowingPoint = new Point(2, CyderDragLabel.DEFAULT_HEIGHT - 2);

    /**
     * The point the console menu is set at before animating to the visible point.
     */
    private static final Point consoleMenuHiddenPoint = new Point(-150, CyderDragLabel.DEFAULT_HEIGHT - 2);

    /**
     * The padding between the menu label and the menu scroll panel on the horizontal axis.
     */
    private static final int menuScrollHorizontalPadding = 5;

    /**
     * The padding between the menu label and the menu scroll panel on the vertical axis.
     */
    private static final int menuScrollVerticalPadding = 5;

    /**
     * The output pane for the console taskbar menu.
     */
    private CyderOutputPane menuPaneOutputPane;

    /**
     * Revalidates the taskbar menu bounds and re-installs the icons.
     */
    private void generateConsoleMenu() {
        if (menuLabel != null) {
            menuLabel.setVisible(false);
        }

        menuLabel = new JLabel();
        revalidateConsoleMenuSize();
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

        menuPaneOutputPane = new CyderOutputPane(menuPane);

        menuScroll = new CyderScrollPane(menuPane);
        menuScroll.setThumbSize(5);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setFocusable(false);
        menuScroll.setOpaque(false);
        menuScroll.setThumbColor(CyderColors.regularPink);
        menuScroll.setBackground(CyderColors.getGuiThemeColor());
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        int width = menuLabel.getWidth() - 2 * menuScrollHorizontalPadding;
        int height = menuLabel.getHeight() - 2 * menuScrollVerticalPadding;
        menuScroll.setBounds(menuScrollHorizontalPadding, menuScrollVerticalPadding, width, height);
        menuLabel.add(menuScroll);

        installMenuTaskbarIcons();
    }

    /**
     * Revalidates the console menu size.
     */
    private void revalidateConsoleMenuSize() {
        if (menuLabel != null) menuLabel.setSize(TASKBAR_MENU_WIDTH, calculateMenuHeight());
    }

    /**
     * Clears the taskbar menu pane and re-prints the current taskbar icons from the three lists.
     */
    private void reinstallCurrentTaskbarIcons() {
        boolean compactMode = UserDataManager.INSTANCE.compactTextMode();

        menuPaneOutputPane.printlnMenuSeparator();

        menuPane.setText("");

        menuPaneOutputPane.getStringUtil().newline(!compactMode);

        currentFrameMenuItems.forEach(frameItem -> {
            frameItem.generateTaskbarIcon();
            menuPaneOutputPane.getStringUtil().printlnComponent(frameItem.getTaskbarIcon());
            menuPaneOutputPane.getStringUtil().newline(!compactMode);
        });

        if (currentFrameMenuItems.size() > 0 && !compactMode) {
            menuPaneOutputPane.printlnMenuSeparator();
        }

        currentMappedExeItems.forEach(mappedExe -> {
            mappedExe.generateTaskbarIcon();
            menuPaneOutputPane.getStringUtil().printlnComponent(mappedExe.getTaskbarIcon());
            menuPaneOutputPane.getStringUtil().newline(!compactMode);
        });

        if (currentMappedExeItems.size() > 0 && !compactMode) {
            menuPaneOutputPane.printlnMenuSeparator();
        }

        currentDefaultMenuItems.forEach(taskbarIcon -> {
            taskbarIcon.generateTaskbarIcon();
            menuPaneOutputPane.getStringUtil().printlnComponent(taskbarIcon.getTaskbarIcon());
            menuPaneOutputPane.getStringUtil().newline(!compactMode);
        });

        menuPane.setCaretPosition(0);
    }

    /**
     * Removes the provided frame reference from the taskbar frame list.
     *
     * @param frame the frame reference to remove from the taskbar frame list
     */
    public void removeTaskbarIcon(CyderFrame frame) {
        Preconditions.checkNotNull(frame);

        if (currentActiveFrames.contains(frame)) {
            currentActiveFrames.remove(frame);
            revalidateConsoleTaskbarMenu();
        }
    }

    /**
     * Adds the provided frame reference to the taskbar frame list and revalidates the taskbar.
     *
     * @param associatedFrame the frame reference to add to the taskbar list
     */
    public void addTaskbarIcon(CyderFrame associatedFrame) {
        Preconditions.checkNotNull(associatedFrame);

        if (isClosed() || frameTaskbarExceptions.containsValue(associatedFrame)) return;

        if (!currentActiveFrames.contains(associatedFrame)) {
            currentActiveFrames.add(associatedFrame);
            revalidateConsoleTaskbarMenu();
        }
    }

    /**
     * The thread name for the input and output fields out animator thread.
     */
    private static final String CONSOLE_FIELDS_OUT_ANIMATOR_THREAD_NAME = "Console Field Out Animator";

    /**
     * The animate to x value when animating the fields left with the menu minimize animation.
     */
    private static final int minFieldAnimateToX = 15;

    /**
     * Animates the taskbar menu away.
     */
    private void minimizeMenu() {
        Preconditions.checkState(menuLabel.isVisible());

        CyderThreadRunner.submit(() -> {
            int outputScrollY = outputScroll.getY();
            int outputScrollWidth = outputScroll.getWidth();
            int outputScrollHeight = outputScroll.getHeight();

            int inputFieldY = inputField.getY();
            int inputFieldWidth = inputField.getWidth();
            int inputFieldHeight = inputField.getHeight();

            for (int i = inputField.getX() ; i > minFieldAnimateToX ; i -= menuAnimationIncrement) {
                outputScroll.setBounds(i, outputScrollY, outputScrollWidth + 1, outputScrollHeight);
                inputField.setBounds(i, inputFieldY, inputFieldWidth + 1, inputFieldHeight);

                ThreadUtil.sleep(menuAnimationDelayMs);
            }

            revalidateInputAndOutputBounds(true);
        }, CONSOLE_FIELDS_OUT_ANIMATOR_THREAD_NAME);

        CyderThreadRunner.submit(() -> {
            menuLabel.setLocation(consoleMenuShowingPoint);
            int y = menuLabel.getY();

            for (int i = 0 ; i > consoleMenuHiddenPoint.getX() ; i -= menuAnimationIncrement) {
                menuLabel.setLocation(i, y);
                ThreadUtil.sleep(menuAnimationDelayMs);
            }

            menuLabel.setLocation(consoleMenuHiddenPoint);
            menuLabel.setVisible(false);

            revalidateInputAndOutputBounds();
        }, MINIMIZE_MENU_THREAD_NAME);
    }

    /**
     * The input field key adapter used for thread escaping and console rotation.
     */
    private final KeyAdapter inputFieldKeyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (KeyCodeUtil.isControlC(e)) baseInputHandler.escapeThreads();

            int caretPosition = outputArea.getCaretPosition();

            if (KeyCodeUtil.isControlAltDown(e)) {
                setConsoleDirection(Direction.BOTTOM);
                outputArea.setCaretPosition(caretPosition);
            } else if (KeyCodeUtil.isControlAltRight(e)) {
                setConsoleDirection(Direction.RIGHT);
                outputArea.setCaretPosition(caretPosition);
            } else if (KeyCodeUtil.isControlAltUp(e)) {
                setConsoleDirection(Direction.TOP);
                outputArea.setCaretPosition(caretPosition);
            } else if (KeyCodeUtil.isControlAltLeft(e)) {
                setConsoleDirection(Direction.LEFT);
                outputArea.setCaretPosition(caretPosition);
            }
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
                setInputFieldCaretPositionToEnd();
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
    };

    /**
     * Sets the caret position of the input field to the end.
     */
    private void setInputFieldCaretPositionToEnd() {
        inputField.setCaretPosition(inputField.getPassword().length);
    }

    /**
     * The key listener for input field to control command scrolling.
     */
    private final KeyListener commandScrolling = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!controlAltNotPressed(e)) return;

            if (KeyCodeUtil.up(e.getKeyCode())) {
                attemptScrollUp();
            } else if (KeyCodeUtil.down(e.getKeyCode())) {
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

            IntStream.range(KeyEvent.VK_F13, KeyEvent.VK_F24 + 1).forEach(specialCode -> {
                if (code == specialCode) {
                    int functionKey = (code - KeyEvent.VK_F13 + SPECIAL_FUNCTION_KEY_CODE_OFFSET);
                    baseInputHandler.println("Interesting F" + functionKey + " key");

                    if (functionKey == F_17_KEY_CODE) {
                        GeneralAndSystemAudioPlayer.playGeneralAudio(F_17_MUSIC_FILE);
                    }
                }
            });
        }

        @ForReadability
        private boolean controlAltNotPressed(KeyEvent e) {
            return (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == 0);
        }
    };

    /**
     * Some kind of a magic number that denotes the mouse wheel is being scrolled up.
     */
    private static final int WHEEL_UP = -1;

    /**
     * The MouseWheelListener used for increasing/decreasing the
     * font size for input field and output area.
     */
    @SuppressWarnings("MagicConstant") /* Font metric is always checked */
    private final MouseWheelListener fontSizerListener = e -> {
        if (!e.isControlDown()) {
            outputArea.getParent().dispatchEvent(e);
            inputField.getParent().dispatchEvent(e);
            return;
        }

        int fontSize = UserDataManager.INSTANCE.getFontSize();
        if (e.getWheelRotation() == WHEEL_UP) {
            fontSize++;
        } else {
            fontSize--;
        }

        int maxFontSize = Props.maxFontSize.getValue();
        int minFontSize = Props.minFontSize.getValue();
        if (fontSize > maxFontSize || fontSize < minFontSize) return;

        String fontName = UserDataManager.INSTANCE.getFontName();
        int fontMetric = FontUtil.getFontMetricFromProps();

        Font newFont = new Font(fontName, fontMetric, fontSize);
        if (FontUtil.isValidFontMetric(fontMetric)) {
            inputField.setFont(newFont);
            outputArea.setFont(newFont);

            UserDataManager.INSTANCE.setFontSize(fontSize);
            baseInputHandler.refreshPrintedLabels();
        }
    };

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
    @SuppressWarnings("MagicConstant") /* Font metric is always checked before use */
    public Font generateUserFont() {
        String fontName = UserDataManager.INSTANCE.getFontName();
        int fontMetric = FontUtil.getFontMetricFromProps();
        int fontSize = UserDataManager.INSTANCE.getFontSize();

        if (!FontUtil.isValidFontMetric(fontMetric)) fontMetric = Font.BOLD;

        return new Font(fontName, fontMetric, fontSize);
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

            File[] backgroundFilesArr = Dynamic.buildDynamic(
                    Dynamic.USERS.getFileName(), uuid,
                    UserFile.BACKGROUNDS.getName()).listFiles();
            if (backgroundFilesArr != null && backgroundFilesArr.length > 0) {
                Arrays.stream(backgroundFilesArr)
                        .filter(FileUtil::isSupportedImageExtension)
                        .forEach(backgroundFiles::add);
            }

            if (backgroundFiles.isEmpty()) {
                Logger.log(LogTag.SYSTEM_IO, "No backgrounds found for user "
                        + uuid + ", creating default background");
                backgroundFiles.add(UserUtil.createDefaultBackground(uuid));
            }

            backgrounds.clear();

            backgroundFiles.forEach(backgroundFile -> backgrounds.add(new ConsoleBackground(backgroundFile)));
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
        if (consoleCyderFrame == null) {
            backgroundIndex = 0;
            return;
        }

        JLabel contentLabel = getConsoleCyderFrameContentPane();
        if (contentLabel == null) {
            backgroundIndex = 0;
            return;
        }

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

        for (int i = 0 ; i < backgrounds.size() ; i++) {
            if (backgrounds.get(i).getReferenceFile().getAbsolutePath()
                    .equals(backgroundFile.getAbsolutePath())) {
                setBackgroundIndex(i, maintainSizeAndCenter);
                return;
            }
        }

        throw new IllegalArgumentException("Provided file not found in user's backgrounds directory: "
                + backgroundFile.getAbsolutePath());
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
                    backgrounds.get(backgroundIndex).generateBufferedImage(), -AngleUtil.NINETY_DEGREES));
            case RIGHT -> new ImageIcon(ImageUtil.rotateImage(
                    backgrounds.get(backgroundIndex).generateBufferedImage(), AngleUtil.NINETY_DEGREES));
            case TOP -> getCurrentBackground().generateImageIcon();
            case BOTTOM -> new ImageIcon(ImageUtil.rotateImage(
                    backgrounds.get(backgroundIndex).generateBufferedImage(), AngleUtil.ONE_EIGHTY_DEGREES));
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
        revalidateConsoleTaskbarMenu();
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
     * The name of the thread which animates the background switch.
     */
    private static final String CONSOLE_BACKGROUND_SWITCHER_THREAD_NAME = "Console Background Switcher";

    /**
     * Switches backgrounds to the next background in the list via a sliding animation.
     * The Console will remain in fullscreen mode if in fullscreen mode as well as maintain
     * whatever size it was at before a background switch was requested.
     */
    @SuppressWarnings("UnnecessaryDefault")
    private void attemptToSwitchBackground() {
        if (backgroundSwitchingLocked.get()) return;
        if (backgrounds.size() == 1) {
            consoleCyderFrame.notify(onlyOneBackgroundNotificationBuilder);
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
            nextBackground = ImageUtil.rotateImage(nextBackground, -AngleUtil.NINETY_DEGREES);
        } else if (consoleDir == Direction.RIGHT) {
            width = nextBackground.getIconHeight();
            height = nextBackground.getIconWidth();
            nextBackground = ImageUtil.rotateImage(nextBackground, AngleUtil.NINETY_DEGREES);
        } else if (consoleDir == Direction.BOTTOM) {
            width = nextBackground.getIconWidth();
            height = nextBackground.getIconHeight();
            nextBackground = ImageUtil.rotateImage(nextBackground, AngleUtil.ONE_EIGHTY_DEGREES);
        }

        JLabel contentPane = getConsoleCyderFrameContentPane();
        contentPane.setToolTipText(FileUtil.getFilename(getCurrentBackground().getReferenceFile().getName()));

        ImageIcon nextBackFinal = nextBackground;
        ImageIcon oldBack = ImageUtil.resizeImage((ImageIcon) contentPane.getIcon(), width, height);

        Point originalCenter = consoleCyderFrame.getCenterPointOnScreen();
        consoleCyderFrame.setSize(width, height);

        // Bump frame into bounds if new size pushed part out of bounds
        UiUtil.requestFramePosition(new Point((int) originalCenter.getX() - width / 2,
                (int) originalCenter.getY() - height / 2), consoleCyderFrame);

        ImageIcon combinedIcon = switch (lastSlideDirection) {
            case LEFT -> ImageUtil.combineImages(oldBack, nextBackground, Direction.BOTTOM);
            case RIGHT -> ImageUtil.combineImages(oldBack, nextBackground, Direction.TOP);
            case TOP -> ImageUtil.combineImages(oldBack, nextBackground, Direction.LEFT);
            case BOTTOM -> ImageUtil.combineImages(oldBack, nextBackground, Direction.RIGHT);
            default -> throw new IllegalStateException("Invalid last slide direction: " + lastSlideDirection);
        };

        // Revalidate bounds for icon label and icon pane
        consoleCyderFrame.refreshBackground();

        // Determine this slide direction
        Direction nextSlideDirection;
        switch (lastSlideDirection) {
            case LEFT -> nextSlideDirection = Direction.TOP;
            case RIGHT -> nextSlideDirection = Direction.BOTTOM;
            case TOP -> nextSlideDirection = Direction.RIGHT;
            case BOTTOM -> nextSlideDirection = Direction.LEFT;
            default -> throw new IllegalStateException("Invalid last slide direction: " + lastSlideDirection);
        }

        // Set dimensions
        switch (nextSlideDirection) {
            case TOP -> contentPane.setBounds(CyderFrame.FRAME_RESIZING_LEN, CyderFrame.FRAME_RESIZING_LEN,
                    combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
            case BOTTOM -> contentPane.setBounds(CyderFrame.FRAME_RESIZING_LEN, -combinedIcon.getIconHeight() / 2,
                    combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
            case RIGHT -> contentPane.setBounds(-combinedIcon.getIconWidth() / 2, CyderFrame.FRAME_RESIZING_LEN,
                    combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
            case LEFT -> contentPane.setBounds(combinedIcon.getIconWidth() / 2, CyderFrame.FRAME_RESIZING_LEN,
                    combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
        }

        // set to combined icon
        contentPane.setIcon(combinedIcon);

        boolean wasDraggable = consoleCyderFrame.isDraggingEnabled();
        consoleCyderFrame.disableDragging();

        boolean outputAreaWasFocusable = outputArea.isFocusable();
        outputArea.setFocusable(false);

        Executors.newSingleThreadExecutor(new CyderThreadFactory(CONSOLE_BACKGROUND_SWITCHER_THREAD_NAME))
                .submit(() -> {
                    int timeout =
                            isFullscreen() ? fullscreenBackgroundAnimationTimeout : defaultBackgroundAnimationTimeout;
                    int increment = isFullscreen() ? fullscreenBackgroundAnimationIncrement :
                            defaultBackgroundAnimationIncrement;

                    switch (nextSlideDirection) {
                        case TOP -> {
                            for (int i = 0 ; i >= -consoleCyderFrame.getHeight() ; i -= increment) {
                                ThreadUtil.sleep(timeout);
                                contentPane.setLocation(consoleCyderFrame.getContentPane().getX(), i);
                            }
                            lastSlideDirection = nextSlideDirection;
                        }
                        case BOTTOM -> {
                            for (int i = -consoleCyderFrame.getHeight() ; i <= 0 ; i += increment) {
                                ThreadUtil.sleep(timeout);
                                contentPane.setLocation(consoleCyderFrame.getContentPane().getX(), i);
                            }
                            lastSlideDirection = nextSlideDirection;
                        }
                        case RIGHT -> {
                            for (int i = -consoleCyderFrame.getWidth() ; i <= 0 ; i += increment) {
                                ThreadUtil.sleep(timeout);
                                contentPane.setLocation(i, consoleCyderFrame.getContentPane().getY());
                            }
                            lastSlideDirection = nextSlideDirection;
                        }
                        case LEFT -> {
                            for (int i = 0 ; i >= -consoleCyderFrame.getWidth() ; i -= increment) {
                                ThreadUtil.sleep(timeout);
                                contentPane.setLocation(i, consoleCyderFrame.getContentPane().getY());
                            }
                            lastSlideDirection = nextSlideDirection;
                        }
                    }

                    consoleCyderFrame.setBackground(nextBackFinal);
                    contentPane.setIcon(nextBackFinal);

                    consoleCyderFrame.refreshBackground();
                    consoleCyderFrame.getContentPane().revalidate();

                    refreshConsoleMaxSize();

                    consoleCyderFrame.setDraggingEnabled(wasDraggable);

                    revalidateMaintainFullscreenOrDirection();

                    defaultFocusOwner.requestFocus();

                    outputArea.setFocusable(outputAreaWasFocusable);

                    backgroundSwitchingLocked.set(false);
                });
    }

    /**
     * Whether chams (chameleon mode) is currently active.
     */
    private final AtomicBoolean chamsActive = new AtomicBoolean();

    /**
     * Sets the console orientation and refreshes the frame.
     * This action exits fullscreen mode if active.
     *
     * @param consoleDirection the direction the background is to face
     */
    private void setConsoleDirection(Direction consoleDirection) {
        boolean maintainConsoleSize = consoleDirection != consoleDir;
        lastConsoleDir = consoleDir;
        consoleDir = consoleDirection;

        // If chams, reset to what background should be first
        if (chamsActive.get()) {
            revalidate(true, false, true);
            chamsActive.set(false);
            return;
        }

        UserDataManager.INSTANCE.setFullscreen(false);
        revalidate(true, false, maintainConsoleSize);
        revalidateConsoleMenuSize();
        saveScreenStat();
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
            UserDataManager.INSTANCE.setFullscreen(fullscreen);

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

    @ForReadability
    private boolean isFullscreen() {
        return UserDataManager.INSTANCE.isFullscreen();
    }

    /**
     * Returns the input handler associated with the Console.
     *
     * @return the input handler associated with the Console
     */
    public BaseInputHandler getInputHandler() {
        return baseInputHandler;
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
     * Invokes the {@link #revalidate(boolean, boolean)} method, maintaining either full screen or direction,
     * not both. Maintaining fullscreen takes precedent over maintaining the console direction.
     */
    public void revalidateMaintainFullscreenOrDirection() {
        boolean fullscreen = isFullscreen();
        revalidate(!fullscreen, fullscreen);
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
     * Revalidates the following console properties:
     * <ul>
     *     <li>Whether the frame should animate opacity on click events</li>
     *     <li>Background</li>
     *     <li>Background size</li>
     *     <li>Ensuring the frame position is completely in bounds of the monitor</li>
     *     <li>Input and output bounds</li>
     *     <li>Console max size</li>
     *     <li>Console menu</li>
     *     <li>Whether dragging is enabled</li>
     *     <li>Menu bounds, including the audio menu</li>
     * </ul>
     * <p>
     * Order of priority is as follows: maintainDirection, maintainFullscreen.
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

            UserDataManager.INSTANCE.setFullscreen(false);
            consoleCyderFrame.setShouldAnimateOpacity(true);
        } else if (maintainFullscreen && UserDataManager.INSTANCE.isFullscreen()) {
            // Setup fullscreen on current monitor
            background = ImageUtil.resizeImage(getCurrentBackground().generateImageIcon(),
                    (int) consoleCyderFrame.getMonitorBounds().getWidth(),
                    (int) consoleCyderFrame.getMonitorBounds().getHeight());
            consoleCyderFrame.setShouldAnimateOpacity(false);
        } else {
            background = getCurrentBackground().generateImageIcon();
        }

        int width = consoleCyderFrame.getWidth();
        int height = consoleCyderFrame.getHeight();

        if (maintainConsoleSize) {
            switch (consoleDir) {
                case TOP: // Fallthrough
                case BOTTOM:
                    if (Direction.isHorizontal(lastConsoleDir)) {
                        background = ImageUtil.resizeImage(background, height, width);
                    } else {
                        background = ImageUtil.resizeImage(background, width, height);
                    }
                    break;
                case LEFT: // Fallthrough
                case RIGHT:
                    if (Direction.isHorizontal(lastConsoleDir)) {
                        background = ImageUtil.resizeImage(background, width, height);
                    } else {
                        background = ImageUtil.resizeImage(background, height, width);
                    }
                    break;
            }
        }

        int w = background.getIconWidth();
        int h = background.getIconHeight();

        consoleCyderFrame.setSize(w, h);
        consoleCyderFrame.setBackground(background);

        int topLeftX = (int) originalCenter.getX() - w / 2;
        int topLeftY = (int) originalCenter.getY() - h / 2;
        UiUtil.requestFramePosition(new Point(topLeftX, topLeftY), consoleCyderFrame);

        revalidateInputAndOutputBounds();

        refreshConsoleMaxSize();

        // This takes care of offset of input field and output area too
        revalidateConsoleTaskbarMenu();

        consoleCyderFrame.refreshBackground();
        consoleCyderFrame.setDraggingEnabled(!isFullscreen());

        revalidateConsoleTaskbarMenu();
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
    public void revalidateConsoleTaskbarMenu() {
        if (consoleClosed.get() || menuLabel == null) return;

        installMenuTaskbarIcons();

        // revalidate bounds if needed and change icon
        if (menuLabel.isVisible()) {
            menuLabel.setBounds(
                    (int) consoleMenuShowingPoint.getX(),
                    (int) consoleMenuShowingPoint.getY(),
                    TASKBAR_MENU_WIDTH,
                    calculateMenuHeight());

            int width = menuLabel.getWidth() - 2 * menuScrollHorizontalPadding;
            int height = menuLabel.getHeight() - 2 * menuScrollVerticalPadding;
            menuScroll.setBounds(menuScrollHorizontalPadding, menuScrollVerticalPadding, width, height);
        }

        revalidateInputAndOutputBounds();
    }

    /**
     * The name of the console audio menu minimizer thread.
     */
    private static final String CONSOLE_AUDIO_MENU_MINIMIZER_THREAD_NAME = "Console Audio Menu Minimizer";

    /**
     * The increment for audio menu animations.
     */
    private static final int audioMenuAnimationIncrement = 8;

    /**
     * The delay for audio menu animations.
     */
    private static final int audioMenuAnimationDelayMs = 10;

    /**
     * Smoothly animates out the console audio controls.
     */
    private void animateOutAudioControls() {
        CyderThreadRunner.submit(() -> {
            for (int i = audioControlsLabel.getY() ; i > -AUDIO_MENU_LABEL_HEIGHT ; i -= audioMenuAnimationIncrement) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                        - audioControlsLabel.getWidth() - AUDIO_MENU_X_OFFSET, i);
                ThreadUtil.sleep(audioMenuAnimationDelayMs);
            }
            audioControlsLabel.setVisible(false);
        }, CONSOLE_AUDIO_MENU_MINIMIZER_THREAD_NAME);
    }

    /**
     * Smooth animates out and removes the audio controls button.
     */
    public void animateOutAndRemoveAudioControls() {
        CyderThreadRunner.submit(() -> {
            for (int i = audioControlsLabel.getY() ; i > -AUDIO_MENU_LABEL_HEIGHT ; i -= audioMenuAnimationIncrement) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                        - audioControlsLabel.getWidth() - AUDIO_MENU_X_OFFSET, i);
                ThreadUtil.sleep(audioMenuAnimationDelayMs);
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

            int y = CyderDragLabel.DEFAULT_HEIGHT - AUDIO_MENU_LABEL_HEIGHT;
            audioControlsLabel.setLocation(calculateAudioMenuX(), y);

            audioControlsLabel.setVisible(true);

            for (int i = y ; i < CyderDragLabel.DEFAULT_HEIGHT - 2 ; i += audioMenuAnimationIncrement) {
                audioControlsLabel.setLocation(calculateAudioMenuX(), i);
                ThreadUtil.sleep(audioMenuAnimationDelayMs);
            }

            audioControlsLabel.setLocation(calculateAudioMenuX(), CyderDragLabel.DEFAULT_HEIGHT - 2);
        }, CONSOLE_AUDIO_MENU_MINIMIZER_THREAD_NAME);
    }

    /**
     * Revalidates the visibility audio menu and the play/pause button based on if audio is playing.
     */
    public void revalidateAudioMenuVisibility() {
        if (!AudioPlayer.isWidgetOpen() && !GeneralAndSystemAudioPlayer.isGeneralAudioPlaying()) {
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
        if (GeneralAndSystemAudioPlayer.isGeneralAudioPlaying() || AudioPlayer.isAudioPlaying()) {
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
    private static final int AUDIO_MENU_X_OFFSET = CyderFrame.BORDER_LEN + 1;

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

                if (GeneralAndSystemAudioPlayer.isGeneralAudioPlaying()) {
                    GeneralAndSystemAudioPlayer.stopGeneralAudio();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (GeneralAndSystemAudioPlayer.isGeneralAudioPlaying() || AudioPlayer.isAudioPlaying()) {
                    playPauseAudioLabel.setIcon(AudioIcons.pauseIconHover);
                } else {
                    playPauseAudioLabel.setIcon(AudioIcons.playIconHover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (GeneralAndSystemAudioPlayer.isGeneralAudioPlaying() || AudioPlayer.isAudioPlaying()) {
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
     * @param screenPosition the screen position to move the Console to
     */
    public void setLocationOnScreen(ScreenPosition screenPosition) {
        Preconditions.checkNotNull(screenPosition);

        consoleCyderFrame.setLocationOnScreen(screenPosition);

        getPinnedFrames().forEach(relativeFrame -> UiUtil.requestFramePosition(
                new Point(relativeFrame.xOffset() + consoleCyderFrame.getX(),
                        relativeFrame.yOffset() + consoleCyderFrame.getY()),
                relativeFrame.frame()));
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
                        && GeometryUtil.rectanglesOverlap(consoleCyderFrame.getBounds(), frame.getBounds()))
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
        boolean showClock = UserDataManager.INSTANCE.shouldDrawConsoleClock();
        if (!showClock) {
            consoleCyderFrame.setCyderFrameTitle("");
            return;
        }

        String regularSecondTime = TimeUtil.consoleSecondTime();
        String regularNoSecondTime = TimeUtil.consoleNoSecondTime();
        String userConfiguredTime = TimeUtil.getFormattedTime(
                new SimpleDateFormat(UserDataManager.INSTANCE.getConsoleClockFormat()));

        // No custom pattern so take into account showSeconds
        if (userConfiguredTime.equalsIgnoreCase(regularSecondTime)
                || userConfiguredTime.equalsIgnoreCase(regularNoSecondTime)) {
            boolean showSeconds = UserDataManager.INSTANCE.shouldShowConsoleClockSeconds();
            userConfiguredTime = showSeconds ? regularSecondTime : regularNoSecondTime;
        }

        consoleCyderFrame.setCyderFrameTitle(userConfiguredTime);
    }

    /**
     * Logs out the current user and revokes user management from the {@link UserDataManager}.
     */
    public void logoutCurrentUser() {
        Logger.log(LogTag.LOGOUT, UserDataManager.INSTANCE.getUsername());
        UserDataManager.INSTANCE.setLoggedIn(false);
        UserDataManager.INSTANCE.removeManagement();
        NetworkUtil.terminateHighPingChecker();
        uuid = null;
    }

    /**
     * Logs out the current user and shows the login frame
     * relative to the Console's location before it was closed.
     */
    public void logoutCurrentUserAndShowLoginFrame() {
        Point centerPoint = consoleCyderFrame.getCenterPointOnScreen();
        UiUtil.disposeAllFrames(true);
        releaseResourcesAndCloseFrame(false);
        logoutCurrentUser();
        LoginHandler.showGui(centerPoint);
    }

    /**
     * Performs the following actions and then exits the program if instructed to:
     * <ul>
     *     <li>Saves the user's screen stat</li>
     *     <li>Stops all audio</li>
     *     <li>Deactivates the base input handler</li>
     *     <li>Closes the console frame if open</li>
     *     <li>Exits the program if invokeExit is true.
     *     If the ConsoleFrame is currently open, this occurs after the closing animation completes</li>
     * </ul>
     *
     * @param invokeExit whether to invoke a program exit after the above actions
     */
    public void releaseResourcesAndCloseFrame(boolean invokeExit) {
        if (consoleClosed.get()) return;
        consoleClosed.set(true);

        saveScreenStat();
        GeneralAndSystemAudioPlayer.stopAllAudio();

        if (baseInputHandler != null) {
            baseInputHandler.deactivate();
            baseInputHandler = null;
        }

        if (invokeExit) {
            if (consoleCyderFrame.isDisposed()) {
                OsUtil.exit(ExitCondition.StandardControlledExit);
            } else {
                consoleCyderFrame.addPostCloseAction(() -> OsUtil.exit(ExitCondition.StandardControlledExit));
                consoleCyderFrame.dispose();
            }
        }
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
        if (consoleCyderFrame == null || consoleCyderFrame.isDisposed()) return;
        if (consoleCyderFrame.getState() == UiConstants.FRAME_ICONIFIED) return;
        if (uuid == null) return;

        ScreenStat screenStat = UserDataManager.INSTANCE.getScreenStat();
        screenStat.setConsoleWidth(consoleCyderFrame.getWidth());
        screenStat.setConsoleHeight(consoleCyderFrame.getHeight());
        screenStat.setConsoleOnTop(consoleCyderFrame.isAlwaysOnTop());
        screenStat.setMonitor(Integer.parseInt(consoleCyderFrame.getGraphicsConfiguration()
                .getDevice().getIDstring().replaceAll(CyderRegexPatterns.nonNumberRegex, "")));

        screenStat.setConsoleX(consoleCyderFrame.getX());
        screenStat.setConsoleY(consoleCyderFrame.getY());

        screenStat.setConsoleDirection(consoleDir);

        UserDataManager.INSTANCE.setScreenStat(screenStat);
        UserDataManager.INSTANCE.writeUser();
    }

    // -------
    // Dancing
    // -------

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
        ArrayList<RestoreFrame> restoreFrames = new ArrayList<>();
        UiUtil.getCyderFrames().forEach(frame -> {
            restoreFrames.add(new RestoreFrame(frame, frame.getX(), frame.getY(), frame.isDraggingEnabled()));
            frame.disableDragging();
        });

        ProgramStateManager.INSTANCE.setCurrentProgramState(ProgramState.DANCING);

        while (ProgramStateManager.INSTANCE.getCurrentProgramState() == ProgramState.DANCING) {
            if (allFramesFinishedDancing()) break;

            UiUtil.getCyderFrames().forEach(CyderFrame::danceStep);
        }

        stopDancing();
        restoreFrames.forEach(RestoreFrame::restore);
    }

    /**
     * Ends the dancing sequence if ongoing.
     */
    public void stopDancing() {
        ProgramStateManager.INSTANCE.setCurrentProgramState(ProgramState.NORMAL);
        UiUtil.getCyderFrames().forEach(CyderFrame::resetDancing);
    }

    /**
     * Returns whether all frames have completed a dance iteration.
     *
     * @return whether all frames have completed a dance iteration
     */
    private boolean allFramesFinishedDancing() {
        for (CyderFrame frame : UiUtil.getCyderFrames()) {
            if (!frame.isDancingFinished()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets the background of the console to whatever is behind it.
     * This was the original implementation of frame chams functionality before
     * the windows were actually set to be transparent.
     */
    public void originalChams() {
        try {
            CyderFrame frame = getConsoleCyderFrame();
            Rectangle monitorBounds = frame.getMonitorBounds();

            consoleCyderFrame.setVisible(false);
            BufferedImage capture = RobotManager.INSTANCE.getRobot().createScreenCapture(monitorBounds);
            consoleCyderFrame.setVisible(true);

            int x = (int) (Math.abs(monitorBounds.getX()) + frame.getX());
            int y = (int) (Math.abs(monitorBounds.getY()) + frame.getY());
            capture = ImageUtil.cropImage(capture, x, y, frame.getWidth(), frame.getHeight());

            setBackground(ImageUtil.toImageIcon(capture));
            chamsActive.set(true);
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
    private final Semaphore titleNotifyLock = new Semaphore(1);

    /**
     * The label used for title notifications.
     */
    private final CyderLabel titleNotifyLabel = new CyderLabel();

    /**
     * The minimum acceptable time for a title notify invocation.
     */
    private static final int minimumTitleNotifyVisibleTime = 1000;

    /**
     * Paints a label with the provided possibly html-formatted string over the
     * Console for the provided number of milliseconds
     *
     * @param htmlString      the string to display, may or may not be formatted using html
     * @param labelFont       the font to use for the label
     * @param visibleDuration the duration in ms the notification should be visible for
     */
    public void titleNotify(String htmlString, Font labelFont, Duration visibleDuration) {
        Preconditions.checkNotNull(htmlString);
        Preconditions.checkNotNull(labelFont);
        Preconditions.checkNotNull(visibleDuration);
        Preconditions.checkArgument(visibleDuration.toMillis() > minimumTitleNotifyVisibleTime);

        CyderThreadRunner.submit(() -> {
            try {
                titleNotifyLock.acquire();

                titleNotifyLabel.setFont(labelFont);
                titleNotifyLabel.setOpaque(true);
                titleNotifyLabel.setVisible(true);
                titleNotifyLabel.setForeground(CyderColors.defaultDarkModeTextColor);
                titleNotifyLabel.setBackground(CyderColors.darkModeBackgroundColor);

                BoundsString boundsString = BoundsUtil.widthHeightCalculation(htmlString,
                        labelFont, consoleCyderFrame.getWidth());

                int containerWidth = boundsString.getWidth();
                int containerHeight = boundsString.getHeight();

                if (containerHeight + 2 * titleNotificationPadding > consoleCyderFrame.getHeight()
                        || containerWidth + 2 * titleNotificationPadding > consoleCyderFrame.getWidth()) {
                    consoleCyderFrame.inform(htmlString, "Console Notification");
                    return;
                }

                Point center = consoleCyderFrame.getCenterPointOnFrame();

                titleNotifyLabel.setText(HtmlUtil.addCenteringToHtml(boundsString.getText()));
                titleNotifyLabel.setBounds(
                        (int) (center.getX() - titleNotificationPadding - containerWidth / 2),
                        (int) (center.getY() - titleNotificationPadding - containerHeight / 2),
                        containerWidth, containerHeight);
                consoleCyderFrame.getContentPane().add(titleNotifyLabel, JLayeredPane.POPUP_LAYER);
                consoleCyderFrame.repaint();

                ThreadUtil.sleep(visibleDuration.toMillis());

                titleNotifyLabel.setVisible(false);
                consoleCyderFrame.remove(titleNotifyLabel);
                titleNotifyLabel.setText("");

                titleNotifyLock.release();
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

        w = (int) (center.getX() - titleNotificationPadding - w / 2);
        h = (int) (center.getY() - titleNotificationPadding - h / 2);
        titleNotifyLabel.setLocation(w, h);

        consoleCyderFrame.repaint();
    }

    /**
     * Adds the provided frame to {@link #frameTaskbarExceptions}.
     *
     * @param frame the frame to add as an exception
     * @return the hash needed to remove the provided frame from the taskbar exceptions map
     */
    @CanIgnoreReturnValue
    public String addToFrameTaskbarExceptions(CyderFrame frame) {
        Preconditions.checkNotNull(frame);
        Preconditions.checkArgument(!frameTaskbarExceptions.contains(frame));

        String hash = SecurityUtil.generateUuid();
        frameTaskbarExceptions.put(hash, frame);
        return hash;
    }

    /**
     * Removes the provided frame from {@link #frameTaskbarExceptions} if it is contained.
     *
     * @param securityHash the hash returned when {@link #addToFrameTaskbarExceptions(CyderFrame)} was called
     */
    public void removeFrameTaskbarException(String securityHash) {
        Preconditions.checkNotNull(securityHash);
        Preconditions.checkArgument(!securityHash.isEmpty());

        frameTaskbarExceptions.remove(securityHash);
    }
}
