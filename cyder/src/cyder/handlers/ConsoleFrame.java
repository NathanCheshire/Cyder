package cyder.handlers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.builders.GetterBuilder;
import cyder.builders.InformBuilder;
import cyder.builders.NotificationBuilder;
import cyder.common.CyderBackground;
import cyder.common.CyderEntry;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.Direction;
import cyder.enums.DynamicDirectory;
import cyder.enums.ExitCondition;
import cyder.enums.IgnoreThread;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderSplash;
import cyder.genesis.PropLoader;
import cyder.handlers.external.AudioPlayer;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.test.ManualTests;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.user.*;
import cyder.utilities.*;

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
import java.util.LinkedList;
import java.util.concurrent.Future;

/**
 * Singleton of components that represent the GUI way a user
 * interacts with Cyder and its functions.
 */
public enum ConsoleFrame {
    /**
     * The ConsoleFrame singleton.
     */
    INSTANCE;

    /**
     * Log when the console frame singleton is constructed
     * (enums are constructed when they are first referenced).
     */
    ConsoleFrame() {
        Logger.log(Logger.Tag.OBJECT_CREATION, "ConsoleFrame singleton constructed");
    }

    /**
     * The UUID of the user currently associated with the ConsoleFrame.
     */
    private String uuid;

    /**
     * The previous uuid used for tracking purposes.
     */
    private String previousUuid;

    /**
     * The ConsoleFrame's CyderFrame instance.
     */
    private CyderFrame consoleCyderFrame;

    /**
     * The input handler linked to the ConsoleFrame's IO.
     */
    private BaseInputHandler baseInputHandler;

    /**
     * The ConsoleFrame output scroll pane.
     */
    private CyderScrollPane outputScroll;

    /**
     * The ConsoleFrame output TextPane controlled by the scroll pane.
     */
    private JTextPane outputArea;

    /**
     * The JTextPane used for the console menu.
     */
    private JTextPane menuPane;

    /**
     * The input field for the ConsoleFrame. This is a password field
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
    private final JButton menuButton = new CyderIconButton(
            "Menu", CyderIcons.menuIcon, CyderIcons.menuIconHover);

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
     * Whether the console frame is closed.
     */
    private boolean consoleFrameClosed = true;

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
     * The current orientation of the ConsoleFrame.
     */
    private Direction consoleDir = Direction.TOP;

    /**
     * The last direction the console frame was oriented in.
     */
    private Direction lastConsoleDir = consoleDir;

    /**
     * The list of recognized backgrounds that the ConsoleFrame may switch to.
     */
    private final ArrayList<CyderBackground> backgrounds = new ArrayList<>();

    /**
     * The index of the background we are currently at in the backgrounds list.
     */
    private int backgroundIndex;

    /**
     * The clickable taskbar icons.
     */
    private final LinkedList<CyderFrame> menuTaskbarFrames = new LinkedList<>();

    /**
     * The absolute minimum size allowable for the ConsoleFrame.
     */
    private final Dimension MINIMUM_SIZE = new Dimension(600, 600);

    /**
     * The possible audio files to play if the starting user background is grayscale.
     */
    private final ImmutableList<String> grayscaleAudioPaths = ImmutableList.of(
            OSUtil.buildPath("static", "audio", "badapple.mp3"),
            OSUtil.buildPath("static", "audio", "beetlejuice.mp3"),
            OSUtil.buildPath("static", "audio", "blackorwhite.mp3"));

    /**
     * Whether dancing is currently active.
     */
    private boolean currentlyDancing;

    /**
     * The thickness of the border around the input field and output area when enabled.
     */
    private final int fieldBorderThickness = 3;

    /**
     * Performs ConsoleFrame setup routines before constructing
     * the frame and setting its visibility, location, and size.
     *
     * @param entryPoint where the launch call originated from
     * @throws FatalException if the ConsoleFrame was left open
     */
    public void launch(CyderEntry entryPoint) {
        ExceptionHandler.checkFatalCondition(isClosed(),
                "ConsoleFrame launch() invoked when not closed. Old uuid = " + previousUuid);

        Logger.log(Logger.Tag.DEBUG, "Cyder Entry = " + entryPoint);

        UserUtil.ensureUserFilesExist(uuid);
        UserUtil.getCyderUser().setFullscreen("0");

        loadBackgrounds();
        resizeBackgrounds();

        consoleBashString = UserUtil.getCyderUser().getName() + "@Cyder:~$ ";
        lastSlideDirection = Direction.LEFT;
        consoleDir = Direction.TOP;
        commandIndex = 0;
        consoleFrameClosed = false;

        commandList.clear();
        menuTaskbarFrames.clear();

        CyderColors.refreshGuiThemeColor();

        ConsoleIcon consoleIcon = determineConsoleIconAndDimensions();

        setupConsoleCyderFrame(consoleIcon);

        refreshConsoleFrameTitle();

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

        FrameUtil.closeAllFrames(true, consoleCyderFrame, CyderSplash.getSplashFrame());

        restorePreviousFrameBounds(consoleIcon);

        CyderSplash.fastDispose();

        consoleCyderFrame.setVisible(true);

        TimeUtil.setConsoleStartTime(System.currentTimeMillis());
        baseInputHandler.println("Console loaded in " + (TimeUtil.getConsoleStartTime()
                - TimeUtil.getAbsoluteStartTime()) + "ms");
    }

    /**
     * Sets up the console cyder frame and performs all subsequent calls on the object.
     *
     * @param consoleIcon the console icon record to use for the direct props
     */
    private void setupConsoleCyderFrame(ConsoleIcon consoleIcon) {
        consoleCyderFrame =
                new CyderFrame(consoleIcon.dimension().width, consoleIcon.dimension.height, consoleIcon.background) {
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
                    }

                    /**
                     * Disposes the console frame and ensures focus borders do not appear during
                     * the possible close animation.
                     */
                    @Override
                    public void dispose() {
                        outputArea.setFocusable(false);
                        outputScroll.setFocusable(false);
                        super.dispose();
                    }

                    /**
                     * Barrel roll not allowed for ConsoleFrame yet.
                     */
                    @Override
                    public void barrelRoll() {
                        throw new IllegalMethodException("Method is broken for ConsoleFrame; implementation pending");
                    }
                };

        consoleCyderFrame.setBackground(Color.black);

        consoleCyderFrame.addPostCloseAction(() -> OSUtil.exit(ExitCondition.GenesisControlledExit));

        consoleCyderFrame.setDraggingEnabled(!UserUtil.getCyderUser().getFullscreen().equals("1"));

        consoleCyderFrame.addWindowListener(consoleFrameWindowAdapter);

        getContentPane().setToolTipText(FileUtil.getFilename(getCurrentBackground().getReferenceFile().getName()));

        consoleCyderFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        consoleCyderFrame.setPaintCyderFrameTitle(false);
        consoleCyderFrame.setPaintSuperTitle(true);
    }

    /**
     * The record used for determining the console background icon and the corresponding width and height.
     */
    private record ConsoleIcon(ImageIcon background, Dimension dimension) {
    }

    /**
     * Determines the initial console frame background icon.
     *
     * @return a record containing the initial console frame background icon and the dimensions of the icon
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
                    height, getCurrentBackground().getReferenceFile()));
        } else {
            width = getCurrentBackground().generateBufferedImage().getWidth();
            height = getCurrentBackground().generateBufferedImage().getHeight();
            icon = new ImageIcon(ImageUtil.getRotatedImage(
                    getCurrentBackground().getReferenceFile().toString(), getConsoleDirection()));
        }

        if (width == 0 || height == 0)
            throw new RuntimeException("Could not construct background dimension");

        return new ConsoleIcon(icon, new Dimension(width, height));
    }

    /**
     * Refreshes the console frame title.
     */
    private void refreshConsoleFrameTitle() {
        consoleCyderFrame.setTitle(PropLoader.getString("version") +
                " Cyder [" + UserUtil.getCyderUser().getName() + "]");
    }

    /**
     * The mouse motion adapter to add to the console frame used for pinned window logic.
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
     * The mouse adapter to add to all the console frame drag labels.
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

                        if (MathUtil.overlaps(consoleRect, frameRect)) {
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
     * Adds the pinned window logic listeners to the console frame.
     */
    private void installConsolePinnedWindowListeners() {
        consoleCyderFrame.addDragListener(consolePinnedWindowMouseMotionAdapter);
        consoleCyderFrame.addDragLabelMouseListener(consolePinnedWindowMouseAdapter);
    }

    /**
     * Revalidates the bounds of the input field and output area based off
     * of the current console frame size and the menu state.
     */
    private void revalidateInputAndOutputBounds() {
        int xPadding = 15;
        int yPadding = 15;
        int inputFieldHeight = 100;

        if (outputScroll != null && inputField != null) {
            int w = consoleCyderFrame.getWidth();
            int h = consoleCyderFrame.getHeight();

            int addX = 0;

            if (menuLabel != null && menuLabel.isVisible()) {
                addX = 2 + menuLabel.getWidth();
            }

            outputScroll.setBounds(addX + xPadding, CyderDragLabel.DEFAULT_HEIGHT + yPadding,
                    w - addX - 2 * xPadding,
                    h - inputFieldHeight - yPadding * 3 - CyderDragLabel.DEFAULT_HEIGHT);

            inputField.setBounds(addX + xPadding, outputScroll.getY() + yPadding + outputScroll.getHeight(),
                    w - 2 * xPadding - addX, inputFieldHeight);
        }
    }

    /**
     * Sets up the output area and output scroll and adds it to the console frame.
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
        outputArea.setCaretColor(ColorUtil.hexToRgb(UserUtil.getCyderUser().getForeground()));
        outputArea.setCaret(new CyderCaret(ColorUtil.hexToRgb(UserUtil.getCyderUser().getForeground())));
        outputArea.setAutoscrolls(true);

        outputArea.setFocusable(true);
        outputArea.setSelectionColor(CyderColors.selectionColor);
        outputArea.setOpaque(false);
        outputArea.setBackground(CyderColors.nullus);
        outputArea.setForeground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getForeground()));
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
                ? new LineBorder(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()),
                fieldBorderThickness, false)
                : BorderFactory.createEmptyBorder());

        if (UserUtil.getCyderUser().getOutputfill().equals("1")) {
            outputArea.setOpaque(true);
            outputArea.setBackground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()));
            outputArea.repaint();
            outputArea.revalidate();
        }

        consoleCyderFrame.getContentPane().add(outputScroll);
    }

    /**
     * Sets up the input field and adds it to the console frame.
     */
    private void installInputField() {
        inputField = new JPasswordField(40);

        inputField.setEchoChar((char) 0);
        inputField.setText(consoleBashString);
        inputField.setBorder(UserUtil.getCyderUser().getInputborder().equals("1")
                ? new LineBorder(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()),
                fieldBorderThickness, false)
                : BorderFactory.createEmptyBorder());
        inputField.setSelectionColor(CyderColors.selectionColor);
        inputField.setCaretPosition(inputField.getPassword().length);

        inputField.setOpaque(false);
        inputField.setCaretColor(ColorUtil.hexToRgb(UserUtil.getCyderUser().getForeground()));
        inputField.setCaret(new CyderCaret(ColorUtil.hexToRgb(UserUtil.getCyderUser().getForeground())));
        inputField.setForeground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getForeground()));
        inputField.setFont(INSTANCE.generateUserFont());

        installInputFieldListeners();

        if (UserUtil.getCyderUser().getInputfill().equals("1")) {
            inputField.setOpaque(true);
            inputField.setBackground(ColorUtil.hexToRgb(UserUtil.getCyderUser().getBackground()));
            inputField.repaint();
            inputField.revalidate();
        }

        consoleCyderFrame.getContentPane().add(inputField);
    }

    /**
     * Sets up the input map to allow the drag label buttons to be triggered via the enter key.
     */
    private void setupButtonEnterInputMap() {
        String pressed = "pressed";
        String released = "released";
        String enter = "ENTER";
        String releasedEnter = "released ENTER";
        InputMap im = (InputMap) UIManager.get("Button.focusInputMap");
        im.put(KeyStroke.getKeyStroke(enter), pressed);
        im.put(KeyStroke.getKeyStroke(releasedEnter), released);
    }

    /**
     * Sets up the console frame position based on the saved stats from the previous session.
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

        consoleDir = requestedConsoleStats.getConsoleFrameDirection();

        revalidate(true, false, true);

        FrameUtil.requestFramePosition(
                requestedConsoleStats.getMonitor(),
                requestedConsoleStats.getConsoleX(),
                requestedConsoleStats.getConsoleY(),
                consoleCyderFrame);
    }

    /**
     * Sets up the drag label button lists for all the console frame's drag labels.
     */
    private void installDragLabelButtons() {
        menuButton.addActionListener(menuButtonListener);
        menuButton.setBounds(4, 4, 22, 22);
        consoleCyderFrame.getTopDragLabel().add(menuButton);

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
            saveScreenStat();
        });
        pinButton.setIcon(UserUtil.getCyderUser().getScreenStat().isConsoleOnTop() ?
                CyderIcons.pinIconHover : CyderIcons.pinIcon);
        consoleDragButtonList.add(pinButton);

        alternateBackgroundButton.addActionListener(e -> {
            loadBackgrounds();

            try {
                if (canSwitchBackground()) {
                    switchBackground();
                } else if (getBackgrounds().size() == 1) {
                    NotificationBuilder builder = new NotificationBuilder(
                            "You only have one background image. "
                                    + "Try adding more via the user editor");
                    builder.setViewDuration(5000);
                    builder.setOnKillAction(() -> UserEditor.showGui(0));
                    consoleCyderFrame.notify(builder);
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
                closeConsoleFrame(true, false);
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
                baseInputHandler.printlns(ReflectionUtil.getManualTests());

                CyderButton suggestionButton = new CyderButton("    Make a Suggestion   ");
                suggestionButton.setColors(CyderColors.regularPink);
                suggestionButton.addActionListener(ex -> CyderThreadRunner.submit(() -> {
                    GetterBuilder builder = new GetterBuilder("Suggestion");
                    builder.setRelativeTo(consoleCyderFrame);
                    builder.setFieldTooltip("Suggestion");
                    builder.setSubmitButtonColor(CyderColors.regularPink);
                    String suggestion = GetterUtil.getInstance().getString(builder);

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
        consoleClockLabel.setSize(0, StringUtil.getAbsoluteMinHeight("143 ;)",
                CyderFonts.consoleClockLabelFont));
        consoleClockLabel.setFont(CyderFonts.consoleClockLabelFont);
        consoleClockLabel.setForeground(CyderColors.vanila);
        consoleCyderFrame.getTopDragLabel().add(consoleClockLabel);
        consoleClockLabel.setFocusable(false);
        consoleClockLabel.setVisible(true);
    }

    /**
     * The key used for the debug lines abstract action.
     */
    private final String debugLines = "debuglines";

    /**
     * The key used for the forced exit abstract action.
     */
    private final String forcedExit = "forcedexit";

    /**
     * Installs all the input field listeners.
     */
    private void installInputFieldListeners() {
        inputField.addKeyListener(inputFieldKeyAdapter);
        inputField.addKeyListener(commandScrolling);

        inputField.addMouseWheelListener(fontSizerListener);
        inputField.addActionListener(inputFieldActionListener);

        inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), debugLines);
        inputField.getActionMap().put(debugLines, debugLinesAbstractAction);

        inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK), forcedExit);
        inputField.getActionMap().put(forcedExit, forcedExitAbstractAction);
    }

    /**
     * Removes all the input field listeners
     */
    @SuppressWarnings("unused")
    private void uninstallInputFieldListeners() {
        inputField.removeKeyListener(inputFieldKeyAdapter);
        inputField.removeKeyListener(commandScrolling);

        inputField.removeMouseWheelListener(fontSizerListener);
        inputField.removeActionListener(inputFieldActionListener);

        inputField.getActionMap().remove(debugLines);
        inputField.getActionMap().remove(forcedExit);
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
    @SuppressWarnings("unused")
    private void uninstallOutputAreaListeners() {
        outputArea.removeFocusListener(outputAreaFocusAdapter);
        outputArea.removeMouseWheelListener(fontSizerListener);
    }

    /**
     * Sets up resizing for the console frame.
     */
    private void installConsoleResizing() {
        consoleCyderFrame.initializeResizing();
        consoleCyderFrame.setResizable(true);
        consoleCyderFrame.setBackgroundResizing(true);
        consoleCyderFrame.setMinimumSize(MINIMUM_SIZE);
        consoleCyderFrame.setMaximumSize(consoleCyderFrame.getSize());
    }

    private final WindowAdapter consoleFrameWindowAdapter = new WindowAdapter() {
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
     * Begins the console frame checker executors/threads.
     */
    private void startExecutors() {
        CyderThreadRunner.submit(() -> {
            try {
                int lastChimeHour = -1;

                while (true) {
                    int min = LocalDateTime.now().getMinute();
                    int sec = LocalDateTime.now().getSecond();

                    // if at hh:00:00 and we haven't chimed for this hour yet
                    if (min == 0 && sec == 0 && lastChimeHour != LocalDateTime.now().getHour()) {
                        if (!isClosed() && UserUtil.getCyderUser().getHourlychimes().equals("1")) {
                            IOUtil.playSystemAudio("static/audio/chime.mp3");
                            lastChimeHour = LocalDateTime.now().getHour();
                        }
                    }

                    Thread.sleep(50);
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
                            Thread.sleep(50);
                            if (consoleFrameClosed) {
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

                        if (busyThreads == 0 && CyderIcons.getCurrentCyderIcon() != CyderIcons.xxxIcon) {
                            CyderIcons.setCurrentCyderIcon(CyderIcons.CYDER_ICON);
                        } else if (CyderIcons.getCurrentCyderIcon() != CyderIcons.xxxIcon) {
                            CyderIcons.setCurrentCyderIcon(CyderIcons.CYDER_BUSY_ICON);
                        }
                    } else if (CyderIcons.getCurrentCyderIcon() != CyderIcons.xxxIcon) {
                        CyderIcons.setCurrentCyderIcon(CyderIcons.CYDER_ICON);
                    }

                    consoleCyderFrame.setIconImage(CyderIcons.getCurrentCyderIcon().getImage());

                    //sleep 3 seconds
                    int i = 0;
                    while (i < 3000) {
                        Thread.sleep(50);
                        if (consoleFrameClosed) {
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
                Thread.sleep(setDelay);

                OUTER:
                while (true) {
                    saveScreenStat();

                    int i = 0;
                    while (i < setDelay) {
                        Thread.sleep(50);
                        if (consoleFrameClosed) {
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
            ReflectionUtil.cardInvoker("Christmas", TimeUtil.getYear());
        }

        if (TimeUtil.isHalloween()) {
            consoleCyderFrame.notify("Happy Halloween!");
            ReflectionUtil.cardInvoker("Halloween", TimeUtil.getYear());
        }

        if (TimeUtil.isIndependenceDay()) {
            consoleCyderFrame.notify("Happy 4th of July!");
            ReflectionUtil.cardInvoker("Independence", TimeUtil.getYear());
        }

        if (TimeUtil.isThanksgiving()) {
            consoleCyderFrame.notify("Happy Thanksgiving!");
            ReflectionUtil.cardInvoker("Thanksgiving", TimeUtil.getYear());
        }

        if (TimeUtil.isAprilFoolsDay()) {
            consoleCyderFrame.notify("Happy April Fools Day!");
            ReflectionUtil.cardInvoker("AprilFools", TimeUtil.getYear());
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
            ConsoleFrame.INSTANCE.getInputHandler().println("Thanks for creating me Nate :,)");
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
            Logger.log(Logger.Tag.CONSOLE_LOAD, "[" + OSUtil.getSystemUsername() + "] [TESTING MODE]");
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
     * Determines what audio to play at the beginning of the ConsoleFrame startup.
     */
    private void introMusicCheck() {
        //if the user wants some custom intro music
        if (UserUtil.getCyderUser().getIntromusic().equalsIgnoreCase("1")) {
            ArrayList<File> musicList = new ArrayList<>();

            File userMusicDir = new File(OSUtil.buildPath(
                    DynamicDirectory.DYNAMIC_PATH, "users",
                    INSTANCE.getUUID(), UserFile.MUSIC.getName()));

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
                IOUtil.playAudio(files[NumberUtil.randInt(0, files.length - 1)].getAbsolutePath());
            }
            // otherwise, play our own
            else {
                IOUtil.playAudio(OSUtil.buildPath("static", "audio", "ride.mp3"));
            }
        }
        // intro music not on, check for grayscale image
        else if (PropLoader.getBoolean("released")) {
            try {
                CyderThreadRunner.submit(() -> {
                    try {
                        Image icon = new ImageIcon(ImageIO.read(getCurrentBackground().getReferenceFile())).getImage();

                        int w = icon.getWidth(null);
                        int h = icon.getHeight(null);

                        int[] pixels = new int[w * h];
                        PixelGrabber pg = new PixelGrabber(icon, 0, 0, w, h, pixels, 0, w);
                        pg.grabPixels();

                        boolean correct = true;
                        for (int pixel : pixels) {
                            Color color = new Color(pixel);
                            if (color.getRed() != color.getGreen() || color.getRed() != color.getBlue()) {
                                correct = false;
                                break;
                            }
                        }

                        //Bad Apple / Beetlejuice / Michael Jackson reference for a grayscale image
                        if (correct) {
                            IOUtil.playAudio(grayscaleAudioPaths.get(
                                    NumberUtil.randInt(0, grayscaleAudioPaths.size() - 1)));
                        } else if (PropLoader.getBoolean("released")) {
                            IOUtil.playAudio("static/audio/introtheme.mp3");
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

    private final AbstractAction debugLinesAbstractAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean drawLines = !consoleCyderFrame.isDrawDebugLines();

            for (CyderFrame frame : FrameUtil.getCyderFrames()) {
                frame.drawDebugLines(drawLines);
            }
        }
    };

    private final AbstractAction forcedExitAbstractAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            OSUtil.exit(ExitCondition.ForcedImmediateExit);
        }
    };

    private final FocusAdapter outputAreaFocusAdapter = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            outputScroll.setBorder(new LineBorder(ColorUtil.hexToRgb(
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

    private final ActionListener menuButtonListener = e -> {
        if (menuLabel == null) {
            generateConsoleMenu();
        }

        if (!menuLabel.isVisible()) {
            CyderThreadRunner.submit(() -> {
                menuLabel.setLocation(-150, CyderDragLabel.DEFAULT_HEIGHT - 2);
                int y = menuLabel.getY();

                for (int i = -150 ; i < 2 ; i += 8) {
                    menuLabel.setLocation(i, y);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        ExceptionHandler.handle(ex);
                    }
                }

                menuLabel.setLocation(2, y);

                menuButton.setIcon(CyderIcons.menuIcon);

                revalidateInputAndOutputBounds();
            }, "minimize menu thread");

            CyderThreadRunner.submit(() -> {
                generateConsoleMenu();
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
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                }

                revalidateInputAndOutputBounds();
            }, "Console menu animator");
        } else {
            minimizeMenu();
        }
    };

    /**
     * Refreshes the taskbar icons based on the frames currently in the frame list.
     */
    private synchronized void installMenuTaskbarIcons() {
        boolean compactMode = UserUtil.getCyderUser().getCompactTextMode().equals("1");

        StyledDocument doc = menuPane.getStyledDocument();
        SimpleAttributeSet alignment = new SimpleAttributeSet();

        if (compactMode) {
            StyleConstants.setAlignment(alignment, StyleConstants.ALIGN_LEFT);
        } else {
            StyleConstants.setAlignment(alignment, StyleConstants.ALIGN_CENTER);
        }

        doc.setParagraphAttributes(0, doc.getLength(), alignment, false);

        //adding components
        StringUtil printingUtil = new StringUtil(new CyderOutputPane(menuPane));
        menuPane.setText("");

        if (!menuTaskbarFrames.isEmpty()) {
            for (int i = menuTaskbarFrames.size() - 1 ; i > -1 ; i--) {
                CyderFrame currentFrame = menuTaskbarFrames.get(i);

                if (compactMode) {
                    printingUtil.printlnComponent(currentFrame.getCompactTaskbarButton());
                } else {
                    if (currentFrame.isUseCustomTaskbarIcon()) {
                        printingUtil.printlnComponent(currentFrame.getCustomTaskbarIcon());
                    } else {
                        printingUtil.printlnComponent(currentFrame.getTaskbarButton());
                    }

                    printingUtil.println("");
                }
            }
        }

        LinkedList<MappedExecutable> exes = null;

        //mapped executables
        try {
            exes = UserUtil.getCyderUser().getExecutables();
        } catch (Exception e) {
            installMenuTaskbarIcons();
        }

        if (exes != null && !exes.isEmpty()) {
            if (!menuTaskbarFrames.isEmpty()) {
                printingUtil.printlnComponent(generateMenuSep());

                if (!compactMode)
                    printingUtil.println("");
            }

            for (MappedExecutable exe : exes) {
                if (compactMode) {
                    printingUtil.printlnComponent(
                            CyderFrame.generateDefaultCompactTaskbarComponent(exe.getName(), () -> {
                                IOUtil.openOutsideProgram(exe.getFilepath());
                                consoleCyderFrame.notify("Opening: " + exe.getName());
                            }));
                } else {
                    printingUtil.printlnComponent(
                            CyderFrame.generateDefaultTaskbarComponent(exe.getName(), () -> {
                                IOUtil.openOutsideProgram(exe.getFilepath());
                                consoleCyderFrame.notify("Opening: " + exe.getName());
                            }, CyderColors.vanila));

                    printingUtil.println("");
                }
            }

            printingUtil.printlnComponent(generateMenuSep());

            if (!compactMode) {
                printingUtil.println("");
            }
        }

        if (exes != null && exes.isEmpty() && !menuTaskbarFrames.isEmpty() && !compactMode) {
            printingUtil.printlnComponent(generateMenuSep());
            printingUtil.println("");
        }

        //default menu items
        if (compactMode) {
            printingUtil.printlnComponent(
                    CyderFrame.generateDefaultCompactTaskbarComponent("Prefs", () -> UserEditor.showGui(0)));

            printingUtil.printlnComponent(
                    CyderFrame.generateDefaultCompactTaskbarComponent("Logout", this::logout));
        } else {
            printingUtil.printlnComponent(
                    CyderFrame.generateDefaultTaskbarComponent("Prefs", () -> UserEditor.showGui(0)));
            printingUtil.println("");

            printingUtil.printlnComponent(
                    CyderFrame.generateDefaultTaskbarComponent("Logout", this::logout));
        }

        //extracted common part from above if statement
        printingUtil.println("");

        //set menu location to top
        menuPane.setCaretPosition(0);

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
     * Revalidates the taskbar bounds and revalidates the icons.
     */
    private void generateConsoleMenu() {
        int menuHeight = consoleCyderFrame.getHeight() - CyderDragLabel.DEFAULT_HEIGHT - 5;

        menuButton.setIcon(CyderIcons.menuIcon);

        if (menuLabel != null) {
            menuLabel.setVisible(false);
        }

        int menuWidth = 110;

        menuLabel = new JLabel();

        menuLabel = new JLabel("");
        menuLabel.setBounds(-menuWidth, CyderDragLabel.DEFAULT_HEIGHT - 2,
                menuWidth, menuHeight);
        menuLabel.setOpaque(true);
        menuLabel.setBackground(CyderColors.getGuiThemeColor());
        menuLabel.setFocusable(false);
        menuLabel.setVisible(false);
        menuLabel.setBorder(new LineBorder(Color.black, 5));
        consoleCyderFrame.getIconPane().add(menuLabel, JLayeredPane.MODAL_LAYER);

        Dimension menuSize = new Dimension(menuLabel.getWidth(), menuLabel.getHeight());

        menuPane = new JTextPane();
        menuPane.setEditable(false);
        menuPane.setAutoscrolls(false);
        menuPane.setFocusable(false);
        menuPane.setOpaque(false);
        menuPane.setBackground(CyderColors.getGuiThemeColor());

        menuScroll = new CyderScrollPane(menuPane);
        menuScroll.setThumbSize(5);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setFocusable(true);
        menuScroll.setOpaque(false);
        menuScroll.setThumbColor(CyderColors.regularPink);
        menuScroll.setBackground(CyderColors.getGuiThemeColor());
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        menuScroll.setBounds(7, 10, (int) (menuSize.getWidth() - 10), menuHeight - 20);
        menuLabel.add(menuScroll);

        installMenuTaskbarIcons();
    }

    /**
     * Removes the provided frame reference from the taskbar frame list.
     *
     * @param associatedFrame the frame reference to remove from the taskbar frame list
     */
    public void removeTaskbarIcon(CyderFrame associatedFrame) {
        if (menuTaskbarFrames.contains(associatedFrame)) {
            menuTaskbarFrames.remove(associatedFrame);
            revalidateMenu();
        }
    }

    /**
     * Adds the provided frame reference to the taskbar frame list and revalidates the taskbar.
     *
     * @param associatedFrame the frame reference to add to the taskbar list
     */
    public void addTaskbarIcon(CyderFrame associatedFrame) {
        if (!menuTaskbarFrames.contains(associatedFrame)) {
            menuTaskbarFrames.add(associatedFrame);
            revalidateMenu();
        }
    }

    /**
     * Returns a menu separation label.
     *
     * @return a menu separation label
     */
    private JLabel generateMenuSep() {
        JLabel sepLabel = new JLabel("90210  90210") {
            @Override
            public void paintComponent(Graphics g) {
                //draw 5 high line 150 width across
                g.setColor(getForeground());
                g.fillRect(0, 7, 175, 5);
                g.dispose();
            }
        };
        sepLabel.setForeground(CyderColors.vanila);
        return sepLabel;
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

                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                }

                revalidateInputAndOutputBounds();
            }, "Console menu animator");

            CyderThreadRunner.submit(() -> {
                menuLabel.setLocation(2, CyderDragLabel.DEFAULT_HEIGHT - 2);
                int y = menuLabel.getY();

                for (int i = 0 ; i > -150 ; i -= 8) {
                    menuLabel.setLocation(i, y);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }

                menuLabel.setLocation(-150, y);

                menuLabel.setVisible(false);
                menuButton.setIcon(CyderIcons.menuIcon);

                revalidateInputAndOutputBounds();
            }, "minimize menu thread");
        }
    }

    private final KeyAdapter inputFieldKeyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            //escaping
            if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
                try {
                    baseInputHandler.escapeThreads();
                } catch (Exception exception) {
                    ExceptionHandler.handle(exception);
                }
            }

            // direction switching
            if ((e.getKeyCode() == KeyEvent.VK_DOWN) && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0)) {
                int pos = outputArea.getCaretPosition();
                setConsoleDirection(Direction.BOTTOM);
                outputArea.setCaretPosition(pos);
            } else if ((e.getKeyCode() == KeyEvent.VK_RIGHT)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0)) {
                int pos = outputArea.getCaretPosition();
                setConsoleDirection(Direction.RIGHT);
                outputArea.setCaretPosition(pos);
            } else if ((e.getKeyCode() == KeyEvent.VK_UP)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0)) {
                int pos = outputArea.getCaretPosition();
                setConsoleDirection(Direction.TOP);
                outputArea.setCaretPosition(pos);
            } else if ((e.getKeyCode() == KeyEvent.VK_LEFT)
                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    && ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0)) {
                int pos = outputArea.getCaretPosition();
                setConsoleDirection(Direction.LEFT);
                outputArea.setCaretPosition(pos);
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
     * The list function key codes above the default 12 function keys which Windows is capable of handling.
     */
    private final ImmutableList<Integer> specialFunctionKeyCodes = ImmutableList.of(
            KeyEvent.VK_F13,
            KeyEvent.VK_F14,
            KeyEvent.VK_F15,
            KeyEvent.VK_F16,
            KeyEvent.VK_F17,
            KeyEvent.VK_F18,
            KeyEvent.VK_F19,
            KeyEvent.VK_F20,
            KeyEvent.VK_F21,
            KeyEvent.VK_F22,
            KeyEvent.VK_F23,
            KeyEvent.VK_F24);

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

                    // function key easter egg

                    for (int specialCode : specialFunctionKeyCodes) {
                        if (code == specialCode) {
                            int functionKey = (code - KeyEvent.VK_F13 + 13);
                            baseInputHandler.println("Interesting F" + functionKey + " key");

                            if (functionKey == 17) {
                                IOUtil.playAudio("static/audio/f17.mp3");
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

            if (e.getWheelRotation() == -1) {
                size++;
            } else {
                size--;
            }

            if (size > Preferences.FONT_MAX_SIZE || size < Preferences.FONT_MIN_SIZE)
                return;
            try {
                String fontName = UserUtil.getCyderUser().getFont();
                int fontMetric = Integer.parseInt(PropLoader.getString("font_metric"));

                if (NumberUtil.numberInFontMetricRange(fontMetric)) {
                    inputField.setFont(new Font(fontName, fontMetric, size));
                    outputArea.setFont(new Font(fontName, fontMetric, size));
                }

                UserUtil.getCyderUser().setFontsize(String.valueOf(size));
            } catch (Exception ignored) {
            }
            //sometimes this throws so ignore it
        } else {
            // don't disrupt original function
            outputArea.getParent().dispatchEvent(e);
            inputField.getParent().dispatchEvent(e);
        }
    };

    /**
     * Set the UUID for this Cyder session. Everything else relies on this being set and not null.
     * Once set, a one time check is performed to fix any possibly corrupted userdata.
     *
     * @param uuid the user uuid that we will use to determine our output dir and other
     *             information specific to this instance of the console frame
     */
    public void setUUID(String uuid) {
        Preconditions.checkNotNull(uuid, "Provided UUID is null");

        // set previous ID to the current one
        previousUuid = this.uuid;

        // set the current uuid
        this.uuid = uuid;

        // build file and pass to user util to set user and user file
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
    public String getUUID() {
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

        if (NumberUtil.numberInFontMetricRange(metric)) {
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

    /**
     * Takes into account the dpi scaling value and checks all the backgrounds in the user's
     * directory against the current monitor's resolution. If any width or height of a background file
     * exceeds the monitor's width or height. We resize until it doesn't. We also check to make sure the background
     * meets our minimum pixel dimension parameters. The old images are automatically resized and replaced with the
     * properly resized and cropped images.
     */
    public void resizeBackgrounds() {
        try {
            int minWidth = 400;
            int minHeight = 400;
            int maxWidth = ScreenUtil.getScreenWidth();
            int maxHeight = ScreenUtil.getScreenHeight();

            for (CyderBackground currentBackground : backgrounds) {
                File currentFile = currentBackground.getReferenceFile();

                if (!currentBackground.getReferenceFile().exists()) {
                    backgrounds.remove(currentBackground);
                }

                BufferedImage currentImage = ImageIO.read(currentFile);

                int backgroundWidth = currentImage.getWidth();
                int backgroundHeight = currentImage.getHeight();

                int imageType = currentImage.getType();

                //inform the user we are changing the size of the image
                boolean resizeNeeded = backgroundWidth > maxWidth || backgroundHeight > maxHeight ||
                        backgroundWidth < minWidth || backgroundHeight < minHeight;
                if (resizeNeeded) {
                    InformBuilder builder = new InformBuilder(
                            "Resizing the background image \"" + currentFile.getName() + "\"");
                    builder.setTitle("System Action");
                    InformHandler.inform(builder);
                }

                Dimension resizeDimensions =
                        ImageUtil.getImageResizeDimensions(minWidth, minHeight, maxWidth, maxHeight, currentImage);
                int deltaWidth = (int) resizeDimensions.getWidth();
                int deltaHeight = (int) resizeDimensions.getHeight();

                //if the image doesn't need a resize then continue to the next image
                if (deltaWidth == 0 || deltaHeight == 0)
                    continue;

                //save the modified image
                BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, deltaWidth, deltaHeight);
                ImageIO.write(saveImage, FileUtil.getExtension(currentFile), currentFile);
            }

            //reload backgrounds
            loadBackgrounds();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Initializes the backgrounds associated with the current user.
     * Also attempts to find the background index of the ConsoleFrame current background if it exists.
     */
    public void loadBackgrounds() {
        try {
            File backgroundsDir = OSUtil.buildFile(DynamicDirectory.DYNAMIC_PATH,
                    DynamicDirectory.USERS.getDirectoryName(), uuid, "Backgrounds");

            ArrayList<File> backgroundFiles = new ArrayList<>();

            File[] backgroundFilesArr = backgroundsDir.listFiles();
            if (backgroundFilesArr != null && backgroundFilesArr.length > 0) {
                for (File file : backgroundFilesArr) {
                    if (StringUtil.in(FileUtil.getExtension(file),
                            true, FileUtil.SUPPORTED_IMAGE_EXTENSIONS)) {
                        backgroundFiles.add(file);
                    }
                }
            }

            if (backgroundFiles.isEmpty()) {
                // create and reload backgrounds since this shouldn't be empty now
                UserUtil.createDefaultBackground(uuid);
                loadBackgrounds();
            }

            backgrounds.clear();

            for (File file : backgroundFiles) {
                if (ImageUtil.isValidImage(file)) {
                    backgrounds.add(new CyderBackground(file));
                }
            }


            //now we have our wrapped files list

            //find the index we are it if console frame has a content pane
            revalidateBackgroundIndex();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Reinitializes the background files and returns the resulting list of found backgrounds.
     *
     * @return list of found backgrounds
     */
    public ArrayList<CyderBackground> getBackgrounds() {
        loadBackgrounds();
        return backgrounds;
    }

    /**
     * Revalidates the index that the current background is at after
     * refreshing due to a possible background list change.
     */
    private void revalidateBackgroundIndex() {
        if (consoleCyderFrame != null) {
            JLabel contentLabel = getContentPane();

            if (contentLabel != null) {
                String filename = contentLabel.getToolTipText();

                if (StringUtil.isNull(filename)) {
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

        // failsafe
        backgroundIndex = 0;
    }

    /**
     * Sets the background to the provided file in the user's backgrounds directory provided it exists.
     *
     * @param backgroundFile the background file to set the console frame to
     */
    public void setBackgroundFile(File backgroundFile) {
        loadBackgrounds();

        int index = -1;

        for (int i = 0 ; i < backgrounds.size() ; i++) {
            if (backgrounds.get(i).getReferenceFile().getAbsolutePath()
                    .equals(backgroundFile.getAbsolutePath())) {
                index = i;
                break;
            }
        }

        if (index != -1)
            setBackgroundIndex(index);
        else
            throw new IllegalArgumentException("Provided file not found in user's backgrounds directory: "
                    + backgroundFile.getAbsolutePath());
    }

    /**
     * Simply sets the background to the provided icon without having a reference file.
     * Please ensure the icon size is the same as the current background's.
     *
     * @param icon the icon to set to the background of the console frame
     */
    public void setBackground(ImageIcon icon) {
        if (icon.getIconWidth() != consoleCyderFrame.getWidth()
                || icon.getIconHeight() != consoleCyderFrame.getHeight())
            throw new IllegalArgumentException("Provided icon is not the same size as the current frame dimensions");

        consoleCyderFrame.setBackground(icon);
    }

    /**
     * Sets the background index to the provided index
     * if valid and switches to that background.
     *
     * @param index the index to switch the console frame background to
     */
    private void setBackgroundIndex(int index) {
        loadBackgrounds();

        //don't do anything
        if (index < 0 || index > backgrounds.size() - 1) {
            return;
        }

        Point center = consoleCyderFrame.getCenterPoint();

        revalidate(true, false);

        backgroundIndex = index;

        ImageIcon imageIcon = switch (consoleDir) {
            case LEFT -> new ImageIcon(ImageUtil.rotateImageByDegrees(
                    backgrounds.get(backgroundIndex).generateBufferedImage(), -90));
            case RIGHT -> new ImageIcon(ImageUtil.rotateImageByDegrees(
                    backgrounds.get(backgroundIndex).generateBufferedImage(), 90));
            case TOP -> getCurrentBackground().generateImageIcon();
            case BOTTOM -> new ImageIcon(ImageUtil.rotateImageByDegrees(
                    backgrounds.get(backgroundIndex).generateBufferedImage(), 180));
        };

        consoleCyderFrame.setBackground(imageIcon);
        consoleCyderFrame.setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight());

        consoleCyderFrame.setLocation((int) (center.getX() - (imageIcon.getIconWidth()) / 2),
                (int) (center.getY() - (imageIcon.getIconHeight()) / 2));

        //tooltip based on image name
        getContentPane().setToolTipText(FileUtil.getFilename(getCurrentBackground().getReferenceFile().getName()));

        revalidateInputAndOutputBounds();

        //focus default component
        inputField.requestFocus();

        //fix menu
        revalidateMenu();
    }

    /**
     * Increments the background index.
     * Wraps back to 0 if it exceeds the background size.
     */
    private void incBackgroundIndex() {
        if (backgroundIndex + 1 == backgrounds.size()) {
            backgroundIndex = 0;
        } else {
            backgroundIndex += 1;
        }
    }

    /**
     * Returns the current background.
     *
     * @return the current background
     */
    public CyderBackground getCurrentBackground() {
        return backgrounds.get(backgroundIndex);
    }

    /**
     * Switches backgrounds to the next background in the list via a sliding animation.
     * The ConsoleFrame will remain in fullscreen mode if in fullscreen mode as well as maintain
     * whatever size it was at before a background switch was requested.
     */
    @SuppressWarnings("UnnecessaryDefault")
    private void switchBackground() {
        // always load first to ensure we're up-to-date with the valid backgrounds
        loadBackgrounds();

        try {
            // find the next background to use and rotate current background accordingly
            ImageIcon nextBack;

            if (backgroundIndex + 1 == backgrounds.size()) {
                nextBack = backgrounds.get(0).generateImageIcon();
            } else {
                nextBack = backgrounds.get(backgroundIndex + 1).generateImageIcon();
            }

            // increment background index accordingly
            incBackgroundIndex();

            // find the dimensions of the image after transforming it as needed
            int width;
            int height;

            // full screen trumps all else
            if (isFullscreen()) {
                width = (int) consoleCyderFrame.getMonitorBounds().getWidth();
                height = (int) consoleCyderFrame.getMonitorBounds().getHeight();

                nextBack = ImageUtil.resizeImage(nextBack, width, height);
            } else if (consoleDir == Direction.LEFT) {
                // not a typo
                width = nextBack.getIconHeight();
                height = nextBack.getIconWidth();

                nextBack = ImageUtil.rotateImageByDegrees(nextBack, -90);
            } else if (consoleDir == Direction.RIGHT) {
                // not a typo
                width = nextBack.getIconHeight();
                height = nextBack.getIconWidth();

                nextBack = ImageUtil.rotateImageByDegrees(nextBack, 90);
            } else if (consoleDir == Direction.BOTTOM) {
                width = nextBack.getIconWidth();
                height = nextBack.getIconHeight();

                nextBack = ImageUtil.rotateImageByDegrees(nextBack, 180);
            } else {
                // orientation is UP so dimensions
                width = nextBack.getIconWidth();
                height = nextBack.getIconHeight();
            }

            // get console frame's content pane
            JLabel contentPane = ((JLabel) (consoleCyderFrame.getContentPane()));

            // tooltip based on image name
            contentPane.setToolTipText(FileUtil.getFilename(getCurrentBackground().getReferenceFile().getName()));

            // create final background that won't change
            ImageIcon nextBackFinal = nextBack;

            // get the original background and resize it as needed
            ImageIcon oldBack = (ImageIcon) contentPane.getIcon();
            oldBack = ImageUtil.resizeImage(oldBack, width, height);

            // change frame size and put the center in the same spot
            Point originalCenter = consoleCyderFrame.getCenterPoint();
            consoleCyderFrame.setSize(width, height);

            // bump frame into bounds if resize pushes it out
            FrameUtil.requestFramePosition(consoleCyderFrame.getMonitor(),
                    (int) originalCenter.getX() - width / 2,
                    (int) originalCenter.getY() - height / 2, consoleCyderFrame);

            // stitch images
            ImageIcon combinedIcon = switch (lastSlideDirection) {
                case LEFT -> ImageUtil.combineImages(oldBack, nextBack, Direction.BOTTOM);
                case RIGHT -> ImageUtil.combineImages(oldBack, nextBack, Direction.TOP);
                case TOP -> ImageUtil.combineImages(oldBack, nextBack, Direction.LEFT);
                case BOTTOM -> ImageUtil.combineImages(oldBack, nextBack, Direction.RIGHT);
                default -> throw new IllegalStateException("Invalid last slide direction: " + lastSlideDirection);
            };

            // revalidate bounds for icon label and icon pane
            consoleCyderFrame.refreshBackground();

            // set dimensions
            switch (lastSlideDirection) {
                case LEFT ->
                        // will be sliding up
                        contentPane.setBounds(2, 2,
                                combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
                case RIGHT ->
                        // will be sliding down
                        contentPane.setBounds(2, -combinedIcon.getIconHeight() / 2,
                                combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
                case TOP ->
                        // will be sliding right
                        contentPane.setBounds(-combinedIcon.getIconWidth() / 2, 2,
                                combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
                case BOTTOM ->
                        // will be sliding left
                        contentPane.setBounds(combinedIcon.getIconWidth() / 2, 2,
                                combinedIcon.getIconWidth(), combinedIcon.getIconHeight());
                default -> throw new IllegalStateException("Invalid last slide direction: " + lastSlideDirection);
            }

            // set to combined icon
            contentPane.setIcon(combinedIcon);

            // disable dragging
            consoleCyderFrame.disableDragging();

            // restrict focus
            outputArea.setFocusable(false);

            // create and submit job for animation
            Runnable backgroundSwitcher = () -> {
                // set delay and increment for the animation
                int delay = isFullscreen() ? 1 : 5;
                int increment = isFullscreen() ? 20 : 8;

                // animate the old image away and set last slide direction
                switch (lastSlideDirection) {
                    case LEFT -> {
                        // sliding up
                        for (int i = 0 ; i >= -consoleCyderFrame.getHeight() ; i -= increment) {
                            try {
                                Thread.sleep(delay);
                                consoleCyderFrame.getContentPane().setLocation(
                                        consoleCyderFrame.getContentPane().getX(), i);
                            } catch (InterruptedException e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                        lastSlideDirection = Direction.TOP;
                    }
                    case RIGHT -> {
                        // sliding down
                        for (int i = -consoleCyderFrame.getHeight() ; i <= 0 ; i += increment) {
                            try {
                                Thread.sleep(delay);
                                consoleCyderFrame.getContentPane().setLocation(
                                        consoleCyderFrame.getContentPane().getX(), i);
                            } catch (InterruptedException e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                        lastSlideDirection = Direction.BOTTOM;
                    }
                    case TOP -> {
                        // sliding right
                        for (int i = -consoleCyderFrame.getWidth() ; i <= 0 ; i += increment) {
                            try {
                                Thread.sleep(delay);
                                consoleCyderFrame.getContentPane().setLocation(i,
                                        consoleCyderFrame.getContentPane().getY());
                            } catch (InterruptedException e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                        lastSlideDirection = Direction.RIGHT;
                    }
                    case BOTTOM -> {
                        // sliding left
                        for (int i = 0 ; i >= -consoleCyderFrame.getWidth() ; i -= increment) {
                            try {
                                Thread.sleep(delay);
                                consoleCyderFrame.getContentPane().setLocation(i,
                                        consoleCyderFrame.getContentPane().getY());
                            } catch (InterruptedException e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                        lastSlideDirection = Direction.LEFT;
                    }
                    default -> throw new IllegalStateException("Invalid last slide direction: " + lastSlideDirection);
                }

                // set the new image since the animation has concluded
                consoleCyderFrame.setBackground(nextBackFinal);
                contentPane.setIcon(nextBackFinal);

                // call refresh background on the CyderFrame object
                consoleCyderFrame.refreshBackground();
                consoleCyderFrame.getContentPane().revalidate();

                // set new max resizing size
                consoleCyderFrame.setMaximumSize(new Dimension(
                        nextBackFinal.getIconWidth(), nextBackFinal.getIconHeight()));

                // enable dragging
                consoleCyderFrame.enableDragging();

                // allow focus
                outputArea.setFocusable(false);

                // revalidate bounds to be safe
                if (isFullscreen()) {
                    revalidate(false, true);
                } else {
                    revalidate(true, false);
                }

                // give focus back to original owner
                inputField.requestFocus();
            };

            CyderThreadRunner.submit(backgroundSwitcher, "Background Switcher");
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
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
     * Refreshes the console frame, bounds, orientation, and fullscreen mode.
     *
     * @param fullscreen whether to set the frame to fullscreen mode.
     */
    public void setFullscreen(boolean fullscreen) {
        try {
            UserUtil.getCyderUser().setFullscreen(fullscreen ? "1" : "0");

            if (fullscreen) {
                consoleDir = Direction.TOP;
                revalidate(false, true);
            } else {
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
    @SuppressWarnings("unused")
    public int getCommandIndex() {
        return commandIndex;
    }

    /**
     * Sets the index in the command history we are at.
     *
     * @param downs the index in the command history we are at
     */
    @SuppressWarnings("unused")
    public void setCommandIndex(int downs) {
        commandIndex = downs;
    }

    /**
     * Increments the command index by 1.
     */
    @SuppressWarnings("unused")
    public void incrementCommandIndex() {
        commandIndex += 1;
    }

    /**
     * Decreases the command index by 1.
     */
    @SuppressWarnings("unused")
    public void decrementCommandIndex() {
        commandIndex -= 1;
    }

    /**
     * Returns whether the current background index is the maximum index.
     *
     * @return whether the current background index is the maximum index
     */
    @SuppressWarnings("unused")
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
     * Returns the input handler associated with the ConsoleFrame.
     *
     * @return the input handler associated with the ConsoleFrame
     */
    public BaseInputHandler getInputHandler() {
        return baseInputHandler;
    }

    /**
     * Returns the x value of the ConsoleFrame.
     *
     * @return the x value of the ConsoleFrame
     */
    public int getX() {
        return consoleCyderFrame.getX();
    }

    /**
     * Returns the y value of the ConsoleFrame.
     *
     * @return the y value of the ConsoleFrame
     */
    public int getY() {
        return consoleCyderFrame.getY();
    }

    /**
     * Returns the width of the ConsoleFrame.
     *
     * @return the width of the ConsoleFrame
     */
    public int getWidth() {
        return consoleCyderFrame.getWidth();
    }

    /**
     * Returns the height of the ConsoleFrame.
     *
     * @return the height of the ConsoleFrame
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
    @SuppressWarnings("unused")
    public ArrayList<String> getCommandHistory() {
        return commandList;
    }

    // -----------------
    // ui getters
    // -----------------

    /**
     * Returns the JTextPane associated with the ConsoleFrame.
     *
     * @return the JTextPane associated with the ConsoleFrame
     */
    public JTextPane getOutputArea() {
        return outputArea;
    }

    /**
     * Returns the JScrollPane associated with the ConsoleFrame.
     *
     * @return the JScrollPane associated with the ConsoleFrame
     */
    public CyderScrollPane getOutputScroll() {
        return outputScroll;
    }

    /**
     * Returns the input JTextField associated with the ConsoleFrame.
     *
     * @return the input JTextField associated with the ConsoleFrame
     */
    public JTextField getInputField() {
        return inputField;
    }

    /**
     * Revalidates the ConsoleFrame size, bounds, background, menu, clock, audio menu, draggable property, etc.
     * based on the current background. Note that maintainDirection trumps maintainFullscreen.
     *
     * @param maintainDirection  whether to maintain the console direction
     * @param maintainFullscreen whether to maintain fullscreen mode
     */
    public void revalidate(boolean maintainDirection, boolean maintainFullscreen) {
        revalidate(maintainDirection, maintainFullscreen, false);
    }

    /**
     * Revalidates the ConsoleFrame size, bounds, background, menu, clock, audio menu, draggable property, etc.
     * based on the current background. Note that maintainDirection trumps maintainFullscreen.
     *
     * @param maintainDirection   whether to maintain the console direction
     * @param maintainFullscreen  whether to maintain fullscreen mode
     * @param maintainConsoleSize whether to maintain the currently set size of the console
     */
    public void revalidate(boolean maintainDirection, boolean maintainFullscreen, boolean maintainConsoleSize) {
        Point originalCenter = consoleCyderFrame.getCenterPoint();

        ImageIcon background;

        if (maintainDirection) {
            // have full size of image and maintain currently set direction
            background = switch (consoleDir) {
                case TOP -> getCurrentBackground().generateImageIcon();
                case LEFT -> new ImageIcon(ImageUtil.getRotatedImage(
                        getCurrentBackground().getReferenceFile().getAbsolutePath(), Direction.LEFT));
                case RIGHT -> new ImageIcon(ImageUtil.getRotatedImage(
                        getCurrentBackground().getReferenceFile().getAbsolutePath(), Direction.RIGHT));
                case BOTTOM -> new ImageIcon(ImageUtil.getRotatedImage(
                        getCurrentBackground().getReferenceFile().getAbsolutePath(), Direction.BOTTOM));
            };

            UserUtil.getCyderUser().setFullscreen("0");
        } else if (maintainFullscreen && UserUtil.getCyderUser().getFullscreen().equals("1")) {
            // have fullscreen on current monitor
            background = ImageUtil.resizeImage(getCurrentBackground().generateImageIcon(),
                    (int) consoleCyderFrame.getMonitorBounds().getWidth(),
                    (int) consoleCyderFrame.getMonitorBounds().getHeight());
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

        // no background somehow so create the default one in user space
        if (background == null) {
            File newlyCreatedBackground = UserUtil.createDefaultBackground(uuid);

            try {
                background = ImageUtil.toImageIcon(ImageIO.read(newlyCreatedBackground));
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        if (background == null) {
            throw new FatalException("Could not create a background");
        }

        int w = background.getIconWidth();
        int h = background.getIconHeight();

        // this shouldn't ever happen
        if (w == -1 || h == -1) {
            throw new IllegalStateException("Resulting width or height was found to " +
                    "not have been set in ConsoleFrame refresh method. " + CyderStrings.europeanToymaker);
        }

        consoleCyderFrame.setSize(w, h);
        consoleCyderFrame.setBackground(background);

        FrameUtil.requestFramePosition(consoleCyderFrame.getMonitor(),
                (int) originalCenter.getX() - w / 2,
                (int) originalCenter.getY() - h / 2, consoleCyderFrame);

        revalidateInputAndOutputBounds();

        consoleCyderFrame.setMaximumSize(new Dimension(w, h));

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
     * Returns the CyderFrame used for the ConsoleFrame.
     *
     * @return the CyderFrame used for the ConsoleFrame
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
        if (consoleFrameClosed || menuLabel == null)
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
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {
                }
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
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {
                }
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
            audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                    - audioControlsLabel.getWidth() - 6, -40);
            audioControlsLabel.setVisible(true);
            for (int i = -40 ; i < CyderDragLabel.DEFAULT_HEIGHT - 2 ; i += 8) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                        - audioControlsLabel.getWidth() - 6, i);
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {
                }
            }
            audioControlsLabel.setLocation(consoleCyderFrame.getWidth()
                    - audioControlsLabel.getWidth() - 6, CyderDragLabel.DEFAULT_HEIGHT - 2);
        }, "Console Audio Menu Minimizer");
    }

    /**
     * Revalidates the visibility audio menu based on if audio is playing.
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
                audioControlsLabel.setLocation(audioControlsLabel.getX(), -40);
                toggleAudioControls.setVisible(true);
            }

            if (IOUtil.generalAudioPlaying() || AudioPlayer.isAudioPlaying()) {
                playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
            } else {
                playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Play.png"));
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
     * Generates the audio menu label and the button components.
     */
    private void generateAudioMenu() {
        int numButtons = 3;
        int buttonSize = 30;

        int xPadding = 10;

        int labelWidth = buttonSize * numButtons + xPadding * (numButtons + 1);
        int labelHeight = 40;

        int yPadding = (labelHeight - buttonSize) / 2;

        audioControlsLabel = new JLabel();
        audioControlsLabel.setBounds(consoleCyderFrame.getWidth() - labelWidth - 6,
                -labelHeight, labelWidth, labelHeight);
        audioControlsLabel.setOpaque(true);
        audioControlsLabel.setBackground(CyderColors.getGuiThemeColor());
        audioControlsLabel.setBorder(new LineBorder(Color.black, 5));
        audioControlsLabel.setVisible(false);
        consoleCyderFrame.getIconPane().add(audioControlsLabel, JLayeredPane.MODAL_LAYER);

        int currentX = xPadding;

        JLabel lastMusicLabel = new JLabel();
        lastMusicLabel.setBounds(currentX, yPadding, buttonSize, buttonSize);
        lastMusicLabel.setIcon(new ImageIcon("static/pictures/music/SkipBack.png"));
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
                lastMusicLabel.setIcon(new ImageIcon("static/pictures/music/SkipBackHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastMusicLabel.setIcon(new ImageIcon("static/pictures/music/SkipBack.png"));
            }
        });
        lastMusicLabel.setVisible(true);
        lastMusicLabel.setOpaque(false);
        audioControlsLabel.add(lastMusicLabel);

        currentX += xPadding + buttonSize;

        playPauseAudioLabel = new JLabel();
        playPauseAudioLabel.setBounds(currentX, yPadding, buttonSize, buttonSize);
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
                if (!IOUtil.generalAudioPlaying() && !AudioPlayer.isAudioPlaying()) {
                    playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/PlayHover.png"));
                } else {
                    playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/PauseHover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!IOUtil.generalAudioPlaying() && !AudioPlayer.isAudioPlaying()) {
                    playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Play.png"));
                } else {
                    playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
                }
            }
        });
        playPauseAudioLabel.setVisible(true);
        playPauseAudioLabel.setOpaque(false);
        audioControlsLabel.add(playPauseAudioLabel);

        if (!IOUtil.generalAudioPlaying() && !AudioPlayer.isAudioPlaying()) {
            playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Play.png"));
        } else {
            playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
        }

        audioControlsLabel.add(playPauseAudioLabel);

        currentX += xPadding + buttonSize;

        JLabel nextMusicLabel = new JLabel();
        nextMusicLabel.setBounds(currentX, yPadding, buttonSize, buttonSize);
        nextMusicLabel.setIcon(new ImageIcon("static/pictures/music/Skip.png"));
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
                nextMusicLabel.setIcon(new ImageIcon("static/pictures/music/SkipHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextMusicLabel.setIcon(new ImageIcon("static/pictures/music/Skip.png"));
            }
        });
        nextMusicLabel.setVisible(true);
        nextMusicLabel.setOpaque(false);

        audioControlsLabel.add(nextMusicLabel);
    }

    /**
     * Revalidates the background colors of the console menus that are active.
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
     * Sets the console frame to a provided ScreenPosition and moves any pinned CyderFrame windows with it.
     *
     * @param screenPos the screen position to move the ConsoleFrame to
     */
    public void setLocationOnScreen(CyderFrame.ScreenPosition screenPos) {
        consoleCyderFrame.setLocationOnScreen(screenPos);

        ArrayList<RelativeFrame> frames = getPinnedFrames();

        for (RelativeFrame rf : frames) {
            rf.frame().setLocation(
                    rf.xOffset() + consoleCyderFrame.getX(),
                    rf.yOffset() + consoleCyderFrame.getY());
        }
    }

    /**
     * The record used for pinned frame logic.
     */
    private record RelativeFrame(CyderFrame frame, int xOffset, int yOffset) {
    }

    /**
     * Returns a list of all frames that are pinned to the ConsoleFrame.
     *
     * @return a list of all frames that are pinned to the ConsoleFrame
     */
    private ArrayList<RelativeFrame> getPinnedFrames() {
        ArrayList<RelativeFrame> frames = new ArrayList<>();

        Rectangle consoleRect = new Rectangle(consoleCyderFrame.getX(), consoleCyderFrame.getY(),
                consoleCyderFrame.getWidth(), consoleCyderFrame.getHeight());

        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame) {
                if (((CyderFrame) f).isConsolePinned() &&
                        !f.getTitle().equals(consoleCyderFrame.getTitle())) {
                    Rectangle frameRect = new Rectangle(f.getX(), f.getY(), f.getWidth(), f.getHeight());

                    if (MathUtil.overlaps(consoleRect, frameRect)) {
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
     * Simply closes the console frame due to a user logout.
     *
     * @param exit       whether to exit Cyder upon closing the ConsoleFrame
     * @param logoutUser whether to log out the currently logged-in user.
     */
    public void closeConsoleFrame(boolean exit, boolean logoutUser) {
        consoleFrameClosed = true;
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
     * Returns whether the ConsoleFrame is closed.
     *
     * @return whether the ConsoleFrame is closed
     */
    public boolean isClosed() {
        return consoleFrameClosed;
    }

    /**
     * Saves the console frame's position and window stats to the currently logged-in user's json file.
     */
    public void saveScreenStat() {
        if (getUUID() == null)
            return;

        if (consoleCyderFrame != null) {
            // create new screen stat object
            ScreenStat screenStat = UserUtil.getCyderUser().getScreenStat();
            screenStat.setConsoleWidth(consoleCyderFrame.getWidth());
            screenStat.setConsoleHeight(consoleCyderFrame.getHeight());
            screenStat.setConsoleOnTop(consoleCyderFrame.isAlwaysOnTop());
            screenStat.setMonitor(Integer.parseInt(consoleCyderFrame.getGraphicsConfiguration()
                    .getDevice().getIDstring().replaceAll("[^0-9]", "")));
            screenStat.setConsoleX(consoleCyderFrame.getX());
            screenStat.setConsoleY(consoleCyderFrame.getY());
            screenStat.setConsoleFrameDirection(consoleDir);

            // just to be safe
            if (!isClosed()) {
                // set new screen stat
                UserUtil.getCyderUser().setScreenStat(screenStat);

                // this also saves the user every time the screen stats are saved
                UserUtil.writeUser();
            }
        }
    }

    /**
     * Closes the CyderFrame and shows the LoginFrame
     * relative to where ConsoleFrame was closed.
     */
    public void logout() {
        closeConsoleFrame(false, true);
        FrameUtil.closeAllFrames(true);

        IOUtil.stopAllAudio();

        LoginHandler.showGui();
    }

    // ---------------------------
    // dancing stuff
    // ---------------------------

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
     * Sets the background of the console frame to whatever is behind it.
     */
    public void originalChams() {
        try {
            CyderFrame ref = INSTANCE.getConsoleCyderFrame();
            Rectangle monitorBounds = ref.getMonitorBounds();

            INSTANCE.getConsoleCyderFrame().setVisible(false);
            BufferedImage capture = ConsoleFrame.INSTANCE
                    .getInputHandler().getRobot().createScreenCapture(monitorBounds);
            INSTANCE.getConsoleCyderFrame().setVisible(true);

            capture = ImageUtil.getCroppedImage(capture, (int) (Math.abs(monitorBounds.getX()) + ref.getX()),
                    (int) (Math.abs(monitorBounds.getY()) + ref.getY()), ref.getWidth(), ref.getHeight());

            INSTANCE.setBackground(ImageUtil.toImageIcon(capture));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the console frame's content pane.
     *
     * @return the console frame's content pane
     */
    public JLabel getContentPane() {
        return ((JLabel) (consoleCyderFrame.getContentPane()));
    }

}
