package cyder.ui;

import cyder.algorithoms.GeometryAlgorithms;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.enums.ScreenPosition;
import cyder.genesis.CyderCommon;
import cyder.handlers.external.AudioPlayer;
import cyder.handlers.internal.*;
import cyder.user.User;
import cyder.user.UserEditor;
import cyder.user.UserFile;
import cyder.utilities.*;
import cyder.widgets.CardWidget;
import test.java.Debug;

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
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public final class ConsoleFrame {
    /**
     * The ConsoleFrame singleton.
     */
    private static ConsoleFrame consoleFrameInstance = new ConsoleFrame();

    /**
     * Whether the ConsoleFrame singleton has been initialized.
     */
    private static boolean singletonCreated = false;

    /**
     * Returns the ConsoleFrame singleton object.
     *
     * @return the ConsoleFrame singleton object
     */
    public static ConsoleFrame getConsoleFrame() {
        return consoleFrameInstance;
    }

    /**
     * Constructor necessary once, after that it should never be invoked again.
     */
    private ConsoleFrame() {
        if (singletonCreated)
            throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);

        singletonCreated = true;
    }

    /**
     * The UUID of the user currently associated with the ConsoleFrame.
     */
    private String uuid = null;

    /**
     * The ConsoleFrame's cyderframe instance.
     */
    private CyderFrame consoleCyderFrame;

    /**
     * The input handler linked to the ConsoleFrame's IO.
     */
    private InputHandler inputHandler;

    /**
     * The ConsoleFrame output scroll pane.
     */
    private CyderScrollPane outputScroll;

    /**
     * The ConsoleFrame output textpane controlled by the scroll pane.
     */
    public static JTextPane outputArea;

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
     * The top drag label help button.
     */
    private JButton helpButton;

    /**
     * The top drag label menu toggle button.
     */
    private JButton menuButton;

    /**
     * The top drag label minimize button.
     */
    private JButton minimize;

    /**
     * The top drag label pin button.
     */
    private JButton pin;

    /**
     * The top drag label background switch button.
     */
    private JButton alternateBackground;

    /**
     * The top drag label close button.
     */
    private JButton close;

    /**
     * The top drag label audio menu toggle button.
     */
    private JButton toggleAudioControls;

    /**
     * The audio menu parent label
     */
    private JLabel audioControlsLabel;

    /**
     * The button label used to indicate if audio is playing or not
     */
    private JLabel playPauseAudioLabel;

    /**
     * Whether the console menu has been generated.
     */
    private boolean consoleMenuGenerated;

    /**
     * Whether the console frame is in fullscreen mode.
     */
    private boolean fullscreen;

    /**
     * Whether the console frame is closed.
     */
    private boolean consoleFrameClosed = true;

    /**
     * The list of valid backgrounds for the current user.
     */
    private ArrayList<File> backgroundFiles;

    /**
     * The index in the background files list we are currently at.
     */
    private int backgroundIndex;

    /**
     * The file associated with the current background.
     */
    private File backgroundFile;

    /**
     * The image icon associated with the current background.
     */
    private ImageIcon backgroundImageIcon;

    /**
     * The current bash string to use for the start of the input field.
     */
    private String consoleBashString;

    /**
     * The font metric to use for the ConsoleFrame IO fields.
     */
    private int fontMetric = Font.BOLD;

    /**
     * The size of the font for the ConsoleFrame IO fields.
     */
    private int fontSize = 30;

    /**
     * The command list used for scrolling.
     */
    public static ArrayList<String> commandList = new ArrayList<>();

    /**
     * The index of the command list we are at.
     */
    private static int scrollingIndex;

    /**
     * The last direction performed upon the most recent switch background call.
     */
    private Direction lastSlideDirection = Direction.LEFT;

    /**
     * The current orientation of the ConsoleFrame.
     */
    private Direction consoleDir = Direction.TOP;

    /**
     * The thread that checks for threads to indicate if Cyder is busy.
     */
    private Thread busyCheckerThread;

    /**
     * The thread that updates the console clock.
     */
    private Thread consoleClockUpdaterThread;

    /**
     * The thread that determines if the internet is slow or not.
     */
    private Thread highPingChecker;

    /**
     * The thread that chimes every hour on the dot if the user preference for it is set to true.
     */
    private Thread hourlyChimerThread;

    /**
     * The clickable taskbar icons.
     */
    private LinkedList<CyderFrame> menuTaskbarFrames = new LinkedList<>();

    /**
     * The absolute minimum size allowable for the ConsoleFrame.
     */
    public static final Dimension MINIMUM_SIZE = new Dimension(600,600);

    /**
     * Performs ConsoleFrame setup routines before constructing
     * the frame and setting its visibility, location, and size.
     */
    public void launch() {
        //the ConsoleFrame should always be closed properly before start is invoked again
        if (consoleCyderFrame != null)
            throw new RuntimeException("ConsoleFrame left open");

        resizeBackgrounds();
        initBackgrounds();

        try {
            //set bashstring based on cyder username
            consoleBashString = getUsername() + "@Cyder:~$ ";

            //init slide and dir directions
            lastSlideDirection = Direction.LEFT;
            consoleDir = Direction.TOP;

            //new op list and scrolling index
            commandList.clear();
            scrollingIndex = 0;

            //special boolean vars
            fullscreen = false;
            consoleFrameClosed = false;

            //menu items
            consoleMenuGenerated = false;
            menuLabel = null;
            menuTaskbarFrames.clear();

            //handle random background by setting a random background index
            if (UserUtil.getUserData("RandomBackground").equals("1")) {
                if (getBackgrounds().size() <= 1) {
                    consoleCyderFrame.notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", " +
                            "but you only have one background file so there's no random element to be chosen.");
                } else {
                    backgroundIndex = NumberUtil.randInt(0,backgroundFiles.size() - 1);
                }
            }

            //figure out the theme color
            CyderColors.setGuiThemeColor(ColorUtil.hextorgbColor(UserUtil.getUserData("windowcolor")));

            //get proper width, height, and background image icon,
            // we take into account console rotation and fullscreen here
            int consoleFrameBackgroundWidth = 0;
            int consoleFrameBackgroundHeight = 0;
            ImageIcon usage = null;

            if (UserUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                consoleFrameBackgroundWidth = ScreenUtil.getScreenWidth();
                consoleFrameBackgroundHeight = ScreenUtil.getScreenHeight();
                usage = new ImageIcon(ImageUtil.resizeImage(consoleFrameBackgroundWidth,
                        consoleFrameBackgroundHeight,getCurrentBackgroundFile()));
                fullscreen = true;
            } else {
                consoleFrameBackgroundWidth = getCurrentBackgroundImageIcon().getIconWidth();
                consoleFrameBackgroundHeight = getCurrentBackgroundImageIcon().getIconHeight();
                usage = new ImageIcon(ImageUtil.getRotatedImage(
                        getCurrentBackgroundFile().toString(),getConsoleDirection()));
            }

            //anonymous class
            consoleCyderFrame = new CyderFrame(consoleFrameBackgroundWidth, consoleFrameBackgroundHeight, usage) {
                @Override
                public void setBounds(int x, int y, int w, int h) {
                    super.setBounds(x,y,w,h);

                    //set pane component bounds
                    if (outputScroll != null && inputField != null) {
                        int addX = 0;

                        if (menuLabel != null && menuLabel.isVisible())
                            addX = 2 + menuLabel.getWidth();

                        outputScroll.setBounds(addX + 15, 62, w - 40 - addX, h - 204);
                        inputField.setBounds(addX + 15, 62 + outputScroll.getHeight() + 20,w - 40 - addX,
                                h - (62 + outputScroll.getHeight() + 20 + 20));
                    }

                    //menu label bounds
                    if (menuLabel != null && menuLabel.isVisible()) {
                        menuLabel.setBounds(3, DragLabel.getDefaultHeight() - 2,
                                menuLabel.getWidth(), menuLabel.getHeight());
                    }

                    //audio menu bounds
                    if (audioControlsLabel != null && audioControlsLabel.isVisible()) {
                        audioControlsLabel.setBounds(w - 156, DragLabel.getDefaultHeight() - 2,
                                audioControlsLabel.getWidth(), audioControlsLabel.getHeight());
                    }

                    revalidateMenu();
                    refreshClockText();
                }

                @Override
                public void dispose() {
                    outputArea.setFocusable(false);
                    outputScroll.setFocusable(false);
                    super.dispose();
                }
            };

            //after closing the frame exit the program is typical function
            consoleCyderFrame.addPostCloseAction(() -> CyderCommon.exit(25));

            //set background to non-navy color
            consoleCyderFrame.setBackground(Color.black);

            //this has to be here since we need consoleCyderFrame to not be null
            if (fullscreen) {
                consoleCyderFrame.disableDragging();
            }

            //on minimize / reopen end/start threads for optimization
            //on launch, request input field focus and run onLaunch() method
            consoleCyderFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowIconified(WindowEvent e) {
                    inputField.requestFocus();
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                    inputField.requestFocus();
                }

                @Override
                public void windowOpened(WindowEvent e) {
                    inputField.requestFocus();
                    onLaunch();
                }
            });

            //we should always be using controlled exits so this is why we use DO_NOTHING_ON_CLOSE
            consoleCyderFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            consoleCyderFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

            consoleCyderFrame.paintWindowTitle(false);
            consoleCyderFrame.paintSuperTitle(true);
            consoleCyderFrame.setTitle(IOUtil.getSystemData().getVersion()+
                    " Cyder [" + ConsoleFrame.getConsoleFrame().getUsername() + "]");

            if (IOUtil.getSystemData().isConsoleresizable()) {
                consoleCyderFrame.initializeResizing();
                consoleCyderFrame.setResizable(true);

                consoleCyderFrame.setBackgroundResizing(true);

                consoleCyderFrame.setMinimumSize(MINIMUM_SIZE);
                consoleCyderFrame.setMaximumSize(new Dimension(consoleFrameBackgroundWidth,
                        consoleFrameBackgroundHeight));
            }

            //set contentpane tooltip
            ((JLabel) (consoleCyderFrame.getContentPane())).setToolTipText(
                    StringUtil.getFilename(getCurrentBackgroundFile().getName()));

            outputArea = new JTextPane() {
                @Override
                public String toString() {
                    return "JTextPane outputArea used for ConsoleFrame instance: " + consoleCyderFrame;
                }

                @Override
                public void setBounds(int x, int y, int w, int h) {
                    StyledDocument sd = outputArea.getStyledDocument();
                    int pos = outputArea.getCaretPosition();
                    super.setBounds(x,y,w,h);
                    outputArea.setStyledDocument(sd);
                    outputArea.setCaretPosition(pos);
                }
            };
            outputArea.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    inputField.requestFocusInWindow();
                    inputField.setCaretPosition(inputField.getDocument().getLength());
                }
            });

            outputArea.setEditable(false);
            outputArea.setCaretColor(ConsoleFrame.getConsoleFrame().getUserForegroundColor());
            outputArea.setCaret(new CyderCaret(ConsoleFrame.getConsoleFrame().getUserForegroundColor()));
            outputArea.setAutoscrolls(true);
            outputArea.setBounds(10, 62, ConsoleFrame.getConsoleFrame().getBackgroundWidth() - 20, ConsoleFrame.getConsoleFrame().getBackgroundHeight() - 204);
            outputArea.setFocusable(true);
            outputArea.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")),3));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (UserUtil.extractUser().getOutputborder().equals("0"))
                        outputScroll.setBorder(BorderFactory.createEmptyBorder());
                }
            });
            outputArea.setSelectionColor(CyderColors.selectionColor);
            outputArea.setOpaque(false);
            outputArea.setBackground(CyderColors.nullus);
            outputArea.setForeground(ConsoleFrame.getConsoleFrame().getUserForegroundColor());
            outputArea.setFont(ConsoleFrame.getConsoleFrame().getUserFont());

            //init input handler
            inputHandler = new InputHandler(outputArea);

            //start printing queue for input handler
            inputHandler.startConsolePrintingAnimation();

            outputScroll = new CyderScrollPane(outputArea,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED) {
                @Override
                public void setBounds(int x, int y, int w, int h) {
                    super.setBounds(x,y,w,h);

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
            outputScroll.setFocusable(false);

            if (UserUtil.getUserData("OutputBorder").equalsIgnoreCase("1")) {
                outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")),
                        3, false));
            } else {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            outputScroll.setBounds(15, 62, consoleCyderFrame.getWidth() - 40, getBackgroundHeight() - 204);
            consoleCyderFrame.getContentPane().add(outputScroll);

            //output area settings complete; starting input field
            inputField = new JPasswordField(40);
            inputField.setEchoChar((char)0);
            inputField.setText(consoleBashString);

            if (UserUtil.getUserData("InputBorder").equalsIgnoreCase("1")) {
                inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor
                        (UserUtil.getUserData("Background")), 3, false));
            } else {
                inputField.setBorder(null);
            }

            //input field key listeners such as escaping and console rotations
            inputField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    //escaping
                    if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
                        try {
                            inputHandler.escapeThreads();
                        } catch (Exception exception) {
                            ExceptionHandler.handle(exception);
                        }
                    }

                    //direction switching
                    if ((e.getKeyCode() == KeyEvent.VK_DOWN) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        int pos = outputArea.getCaretPosition();
                        setConsoleDirection(Direction.BOTTOM);
                        outputArea.setCaretPosition(pos);
                    } else if ((e.getKeyCode() == KeyEvent.VK_RIGHT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        int pos = outputArea.getCaretPosition();
                        setConsoleDirection(Direction.RIGHT);
                        outputArea.setCaretPosition(pos);
                    } else if ((e.getKeyCode() == KeyEvent.VK_UP) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        int pos = outputArea.getCaretPosition();
                       setConsoleDirection(Direction.TOP);
                        outputArea.setCaretPosition(pos);
                    } else if ((e.getKeyCode() == KeyEvent.VK_LEFT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        int pos = outputArea.getCaretPosition();
                        setConsoleDirection(Direction.LEFT);
                        outputArea.setCaretPosition(pos);
                    }
                }

                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    //bashstring checker
                    if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                        if (inputField.getPassword().length < consoleBashString.toCharArray().length) {
                            e.consume();
                            inputField.setText(consoleBashString);
                        }
                    }
                }
            });

            inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "debuglines");

            inputField.getActionMap().put("debuglines", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    consoleCyderFrame.drawDebugLines(!consoleCyderFrame.isDrawDebugLines());
                }
            });

            inputField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK), "forcedexit");

            inputField.getActionMap().put("forcedexit", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    CyderCommon.exit(-404);
                }
            });

            //a bodge to update the caret position if it goes before an allowed index for console bash string
            new Thread(() -> {
                try {
                    while (!isClosed()) {
                        //if caret position is before the bash string
                        if (inputField.getCaretPosition() < consoleBashString.length()) {
                            inputField.setCaretPosition(inputField.getPassword().length);
                        }

                        //if it doesn't start with bash string, reset it to start with it
                        if (!String.valueOf(inputField.getPassword()).startsWith(consoleBashString)) {
                            inputField.setText(consoleBashString +
                                    String.valueOf(inputField.getPassword()).replace(consoleBashString, "").trim());
                            inputField.setCaretPosition(inputField.getPassword().length);
                        }

                        Thread.sleep(50);
                    }
                }

                catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            },"Console Input Caret Position Updater").start();

            inputField.setToolTipText("Input Field");
            inputField.setSelectionColor(CyderColors.selectionColor);
            inputField.addKeyListener(commandScrolling);
            inputField.setCaretPosition(inputField.getPassword().length);

            inputField.setBounds(15, 62 + outputArea.getHeight() + 20,consoleFrameBackgroundWidth - 40,
                    consoleFrameBackgroundHeight - (62 + outputArea.getHeight() + 20 + 20));
            inputField.setOpaque(false);
            consoleCyderFrame.getContentPane().add(inputField);
            inputField.addActionListener(e -> {
                try {
                    String op = String.valueOf(inputField.getPassword()).substring(consoleBashString.length())
                            .trim().replace(consoleBashString, "");

                    if (!StringUtil.empytStr(op)) {
                        if (!(commandList.size() > 0 && commandList.get(commandList.size() - 1).equals(op))) {
                            commandList.add(op);
                        }

                        scrollingIndex = commandList.size();

                        //calls to linked inputhandler
                        if (!inputHandler.getUserInputMode()) {
                            inputHandler.handle(op, true);
                        }
                        //send the operation to handle second if it is awaiting a secondary input
                        else if (inputHandler.getUserInputMode()) {
                            inputHandler.setUserInputMode(false);
                            inputHandler.handleSecond(op);
                        }
                    }

                    inputField.setText(consoleBashString);
                    inputField.setCaretPosition(consoleBashString.length());
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            });

            inputField.setCaretColor(ConsoleFrame.getConsoleFrame().getUserForegroundColor());
            inputField.setCaret(new CyderCaret(ConsoleFrame.getConsoleFrame().getUserForegroundColor()));
            inputField.setForeground(ConsoleFrame.getConsoleFrame().getUserForegroundColor());
            inputField.setFont(ConsoleFrame.getConsoleFrame().getUserFont());

            if (UserUtil.getUserData("OutputFill").equals("1")) {
                outputArea.setOpaque(true);
                outputArea.setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")));
                outputArea.repaint();
                outputArea.revalidate();
            }

            if (UserUtil.getUserData("InputFill").equals("1")) {
                inputField.setOpaque(true);
                inputField.setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")));
                inputField.repaint();
                inputField.revalidate();
            }

            helpButton = new JButton("");
            helpButton.setToolTipText("Help");

            //instantiate enter listener for all buttons
            InputMap im = (InputMap)UIManager.get("Button.focusInputMap");
            im.put(KeyStroke.getKeyStroke("ENTER"), "pressed");
            im.put(KeyStroke.getKeyStroke("released ENTER"), "released");

            helpButton.addActionListener(e -> new Thread(() -> {
                //print tests in case the user was trying to invoke one
                inputHandler.printManualTests();
                inputHandler.printUnitTests();

                CyderButton suggestionButton = new CyderButton("Make a Suggestion");
                suggestionButton.setColors(CyderColors.regularPink);
                suggestionButton.addActionListener(ex -> new Thread(() -> {
                    String suggestion = new GetterUtil().getString("Suggestion",
                            "Cyder Suggestion", "Submit", CyderColors.regularPink);

                    if (!StringUtil.isNull(suggestion)) {
                        Logger.log(Logger.Tag.SUGGESTION, suggestion.trim());
                        inputHandler.println("Suggestion logged");
                    }
                }, "Suggestion Getter Waiter Thread").start());

                inputHandler.printlnComponent(suggestionButton);
            },"Suggestion Getter Waiter Thread").start());
            helpButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    helpButton.setIcon(CyderIcons.helpIconHover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    helpButton.setIcon(CyderIcons.helpIcon);
                }
            });
            helpButton.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    helpButton.setIcon(CyderIcons.helpIconHover);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    helpButton.setIcon(CyderIcons.helpIcon);
                }
            });
            helpButton.setBounds(32, 4, 22, 22);
            helpButton.setIcon(CyderIcons.helpIcon);
            consoleCyderFrame.getTopDragLabel().add(helpButton);
            helpButton.setFocusPainted(false);
            helpButton.setOpaque(false);
            helpButton.setContentAreaFilled(false);
            helpButton.setBorderPainted(false);

            menuButton = new JButton("");
            menuLabel = new JLabel();
            menuLabel.setFocusable(false);
            menuLabel.setVisible(false);
            menuButton.setToolTipText("Menu");
            menuButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (menuLabel.isVisible()) {
                        menuButton.setIcon(new ImageIcon("static/pictures/icons/menu2.png"));
                    } else {
                        menuButton.setIcon(new ImageIcon("static/pictures/icons/menuSide2.png"));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (menuLabel.isVisible()) {
                        menuButton.setIcon(new ImageIcon("static/pictures/icons/menu1.png"));
                    } else {
                        menuButton.setIcon(new ImageIcon("static/pictures/icons/menuSide1.png"));
                    }
                }
            });
            menuButton.addActionListener(e -> {
                if (!menuLabel.isVisible()) {
                    new Thread(() -> {
                        menuLabel.setLocation(-150, DragLabel.getDefaultHeight() - 2);
                        int y = menuLabel.getY();

                        for (int i = -150 ; i < 2 ; i+= 8) {
                            menuLabel.setLocation(i, y);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ex) {
                                ExceptionHandler.handle(ex);
                            }
                        }

                        menuLabel.setLocation(2, y);

                        menuButton.setIcon(new ImageIcon("static/pictures/icons/menu2.png"));
                    },"minimize menu thread").start();

                    new Thread(() -> {
                        generateConsoleMenu();
                        menuLabel.setLocation(-150,DragLabel.getDefaultHeight() - 2);
                        menuLabel.setVisible(true);

                        int addX = 0;
                        int width = consoleCyderFrame.getWidth();
                        int height = consoleCyderFrame.getHeight();

                        if (menuLabel.isVisible())
                            addX = 2 + menuLabel.getWidth();

                        int finalAddX = addX;

                        for (int i = inputField.getX(); i < finalAddX + 15 ; i += 8) {
                            outputScroll.setBounds(i, outputScroll.getY(), outputScroll.getWidth() + 1, outputScroll.getHeight());
                            inputField.setBounds(i, inputField.getY(), inputField.getWidth() + 1, inputField.getHeight());
                            try {
                                Thread.sleep(10);
                            } catch (Exception ex) {
                                ExceptionHandler.handle(ex);
                            }
                        }

                        outputScroll.setBounds(finalAddX + 15, 62, width - 40 - finalAddX, height - 204);
                        inputField.setBounds(finalAddX + 15, 62 + outputScroll.getHeight() + 20,width - 40 - finalAddX,
                                height - (62 + outputScroll.getHeight() + 20 + 20));
                    },"Console field animator").start();
                } else {
                    minimizeMenu();
                }
            });
            menuButton.setBounds(4, 4, 22, 22);
            menuButton.setIcon(new ImageIcon("static/pictures/icons/menuSide1.png"));
            consoleCyderFrame.getTopDragLabel().add(menuButton);
            menuButton.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (menuLabel.isVisible()) {
                        menuButton.setIcon(new ImageIcon("static/pictures/icons/menu2.png"));
                    } else {
                        menuButton.setIcon(new ImageIcon("static/pictures/icons/menuSide2.png"));
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (menuLabel.isVisible()) {
                        menuButton.setIcon(new ImageIcon("static/pictures/icons/menu1.png"));
                    } else {
                        menuButton.setIcon(new ImageIcon("static/pictures/icons/menuSide1.png"));
                    }
                }
            });
            menuButton.setVisible(true);
            menuButton.setFocusPainted(false);
            menuButton.setOpaque(false);
            menuButton.setContentAreaFilled(false);
            menuButton.setBorderPainted(false);

            //custom list of buttons even for mini and close so that we can focus traverse them
            LinkedList<JButton> consoleDragButtonList = new LinkedList<>();

            toggleAudioControls = new JButton("");
            toggleAudioControls.setToolTipText("Audio Controls");
            toggleAudioControls.addActionListener(e -> {
                if (audioControlsLabel.isVisible()) {
                    animateOutAudioControls();
                } else {
                    animateInAudioControls();
                }
            });

            toggleAudioControls.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    toggleAudioControls.setIcon(new ImageIcon("static/pictures/icons/menu2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    toggleAudioControls.setIcon(new ImageIcon("static/pictures/icons/menu1.png"));
                }
            });
            toggleAudioControls.setIcon(new ImageIcon("static/pictures/icons/menu1.png"));
            toggleAudioControls.setContentAreaFilled(false);
            toggleAudioControls.setBorderPainted(false);
            toggleAudioControls.setFocusPainted(false);
            toggleAudioControls.setFocusable(false);
            toggleAudioControls.setVisible(false);
            consoleDragButtonList.add(toggleAudioControls);

            minimize = new JButton("");
            minimize.setToolTipText("Minimize");
            minimize.addActionListener(e -> {
                consoleCyderFrame.setRestoreX(consoleCyderFrame.getX());
                consoleCyderFrame.setRestoreY(consoleCyderFrame.getY());
                consoleCyderFrame.minimizeAnimation();
            });
            minimize.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    minimize.setIcon(CyderIcons.minimizeIconHover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    minimize.setIcon(CyderIcons.minimizeIcon);
                }
            });
            minimize.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimize.setIcon(CyderIcons.minimizeIconHover);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    minimize.setIcon(CyderIcons.minimizeIcon);
                }
            });
            minimize.setIcon(CyderIcons.minimizeIcon);
            minimize.setContentAreaFilled(false);
            minimize.setBorderPainted(false);
            minimize.setFocusPainted(false);
            minimize.setFocusable(true);
            consoleDragButtonList.add(minimize);

            pin = new JButton("");
            pin.setToolTipText("Pin");
            pin.addActionListener(e -> {
                if (consoleCyderFrame.isAlwaysOnTop()) {
                    consoleCyderFrame.setAlwaysOnTop(false);

                    User user = UserUtil.extractUser();
                    User.ScreenStat screenStat = user.getScreenStat();
                    screenStat.setConsoleOnTop(false);
                    user.setScreenStat(screenStat);
                    UserUtil.setUserData(user);

                    pin.setIcon(CyderIcons.pinIcon);
                } else {
                    consoleCyderFrame.setAlwaysOnTop(true);

                    User user = UserUtil.extractUser();
                    User.ScreenStat screenStat = user.getScreenStat();
                    screenStat.setConsoleOnTop(true);
                    user.setScreenStat(screenStat);
                    UserUtil.setUserData(user);

                    pin.setIcon(CyderIcons.pinIconHover);
                }
            });
            pin.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (consoleCyderFrame.isAlwaysOnTop()) {
                        pin.setIcon(CyderIcons.pinIcon);
                    } else {
                        pin.setIcon(CyderIcons.pinIconHover);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (consoleCyderFrame.isAlwaysOnTop()) {
                        pin.setIcon(CyderIcons.pinIconHover);
                    } else {
                        pin.setIcon(CyderIcons.pinIcon);
                    }
                }
            });
            pin.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    pin.setIcon(CyderIcons.pinIconHover);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (consoleCyderFrame.isAlwaysOnTop()) {
                        pin.setIcon(CyderIcons.pinIconHover);
                    } else {
                        pin.setIcon(CyderIcons.pinIcon);
                    }
                }
            });

            pin.setIcon(UserUtil.extractUser().getScreenStat().isConsoleOnTop() ?
                    CyderIcons.pinIconHover : CyderIcons.pinIcon);
            pin.setContentAreaFilled(false);
            pin.setBorderPainted(false);
            pin.setFocusPainted(false);
            pin.setFocusable(true);
            consoleDragButtonList.add(pin);

            alternateBackground = new JButton("");
            alternateBackground.setToolTipText("Alternate Background");
            alternateBackground.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    alternateBackground.setIcon(CyderIcons.changeSizeIconHover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    alternateBackground.setIcon(CyderIcons.changeSizeIcon);
                }
            });
            alternateBackground.addActionListener(e -> {
                ConsoleFrame.getConsoleFrame().initBackgrounds();

                try {
                    if (canSwitchBackground()) {
                        switchBackground();
                    }  else if (getBackgrounds().size() == 1) {
                        consoleCyderFrame.notify("You only have one background image. " +
                                "Try adding more via the user editor", 5000, NotificationDirection.TOP, () -> UserEditor.showGUI(0));
                    }
                } catch (Exception ex) {
                    consoleCyderFrame.notify("Error in parsing background; perhaps it was deleted.");
                    throw new IllegalArgumentException("Background DNE");
                }
            });
            alternateBackground.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    alternateBackground.setIcon(CyderIcons.changeSizeIconHover);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    alternateBackground.setIcon(CyderIcons.changeSizeIcon);
                }
            });
            alternateBackground.setIcon(CyderIcons.changeSizeIcon);
            alternateBackground.setFocusPainted(false);
            alternateBackground.setOpaque(false);
            alternateBackground.setContentAreaFilled(false);
            alternateBackground.setBorderPainted(false);
            consoleDragButtonList.add(alternateBackground);

            close = new JButton("");
            close.setToolTipText("Close");
            close.addActionListener(e -> {
                saveConsoleFramePosition();

                if (UserUtil.getUserData("minimizeonclose").equals("1")) {
                    ConsoleFrame.getConsoleFrame().minimizeAll();
                } else {
                    closeConsoleFrame(true);
                }
            });
            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    close.setIcon(CyderIcons.closeIconHover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(CyderIcons.closeIcon);
                }
            });
            close.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    close.setIcon(CyderIcons.closeIconHover);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    close.setIcon(CyderIcons.closeIcon);
                }
            });
            close.setIcon(CyderIcons.closeIcon);
            close.setContentAreaFilled(false);
            close.setBorderPainted(false);
            close.setFocusPainted(false);
            close.setFocusable(true);
            consoleDragButtonList.add(close);

            //set top drag's button list and others to none
            consoleCyderFrame.getTopDragLabel().setButtonsList(consoleDragButtonList);
            consoleCyderFrame.getBottomDragLabel().setButtonsList(null);
            consoleCyderFrame.getLeftDragLabel().setButtonsList(null);
            consoleCyderFrame.getRightDragLabel().setButtonsList(null);

            //audio controls
            generateAudioMenu();

            //this turns into setting a center title
            consoleClockLabel = new JLabel(TimeUtil.consoleTime(), SwingConstants.CENTER);
            consoleClockLabel.setFont(CyderFonts.segoe20.deriveFont(20f));
            consoleClockLabel.setForeground(CyderColors.vanila);

            //bounds not needed to be set since the executor service handles that
            consoleCyderFrame.getTopDragLabel().add(consoleClockLabel);
            consoleClockLabel.setFocusable(false);
            consoleClockLabel.setVisible(true);

            //add listeners for pinned window functon
            consoleCyderFrame.addDragListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (consoleCyderFrame != null && consoleCyderFrame.isFocused()
                            && consoleCyderFrame.draggingEnabled()) {

                        Rectangle consoleRect = new Rectangle(consoleCyderFrame.getX(), consoleCyderFrame.getY(),
                                consoleCyderFrame.getWidth(), consoleCyderFrame.getHeight());

                        for (Frame f : Frame.getFrames()) {
                            if (f instanceof CyderFrame && ((CyderFrame) f).isConsolePinned() &&
                                    !f.getTitle().equals(consoleCyderFrame.getTitle()) &&
                                    ((CyderFrame) f).getRelativeX() != Integer.MIN_VALUE &&
                                    ((CyderFrame) f).getRelativeY() != Integer.MIN_VALUE) {

                                    f.setLocation(consoleCyderFrame.getX() + ((CyderFrame) f).getRelativeX(),
                                            consoleCyderFrame.getY() + ((CyderFrame) f).getRelativeY());
                            }
                        }
                    }
                }
            });

            consoleCyderFrame.addDragMouseListener(new MouseAdapter() {
                //this should figure out what frames we need to move during hte current drag event
                @Override
                public void mousePressed(MouseEvent e) {
                    if (consoleCyderFrame != null && consoleCyderFrame.isFocused()
                            && consoleCyderFrame.draggingEnabled()) {

                        Rectangle consoleRect = new Rectangle(consoleCyderFrame.getX(), consoleCyderFrame.getY(),
                                consoleCyderFrame.getWidth(), consoleCyderFrame.getHeight());

                        for (Frame f : Frame.getFrames()) {
                            if (f instanceof CyderFrame && ((CyderFrame) f).isConsolePinned() &&
                                    !f.getTitle().equals(consoleCyderFrame.getTitle())) {
                                Rectangle frameRect = new Rectangle(f.getX(), f.getY(), f.getWidth(), f.getHeight());

                                if (GeometryAlgorithms.overlaps(consoleRect,frameRect)) {
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
            });

            //spin off console executors
            startExecutors();

            //close all non-CyderFrames
            for (Frame f : FrameUtil.getNonCyderFrames()) {
                if (UserUtil.getUserData("closeanimation").equals("1")) {
                    AnimationUtil.closeAnimation(f);
                } else {
                    f.dispose();
                }
            }

            //ensure console frame is on top if left on top last session
            consoleCyderFrame.setAlwaysOnTop(UserUtil.extractUser().getScreenStat().isConsoleOnTop());

            //close all frames just before showing console
            for (CyderFrame f : FrameUtil.getCyderFrames()) {
                if (f == consoleCyderFrame) {
                    //we're trying to open this right now so don't close it
                } else {
                    f.dispose(true);
                }
            }

            User.ScreenStat requestedConsoleStats = UserUtil.extractUser().getScreenStat();

            //requested console bounds
            int requestedConsoleWidth = requestedConsoleStats.getConsoleWidth();
            int requestedConsoleHeight = requestedConsoleStats.getConsoleHeight();
            int requestedConsoleX = requestedConsoleStats.getConsoleX();
            int requestedConsoleY = requestedConsoleStats.getConsoleY();

            //if requested width x height is valid for the background
            // Cyder started on
            if (requestedConsoleWidth <= consoleFrameBackgroundWidth &&
                    requestedConsoleHeight <= consoleFrameBackgroundHeight &&
                        requestedConsoleWidth > MINIMUM_SIZE.width &&
                        requestedConsoleHeight > MINIMUM_SIZE.height) {
                consoleCyderFrame.setSize(requestedConsoleWidth, requestedConsoleHeight);
                consoleCyderFrame.refreshBackground();
            }

            //done with console frame size logic -------------------------------------------

            //show on correct monitor if it exists
            int requestedMonitor = requestedConsoleStats.getMonitor();

            GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screenDevices = graphicsEnvironment.getScreenDevices();

            //if the monitor is valid, then we start on it
            if (requestedMonitor > -1 && requestedMonitor < screenDevices.length) {
                Rectangle requestedScreenBounds =
                        screenDevices[requestedMonitor].getDefaultConfiguration().getBounds();

                int minX = requestedScreenBounds.x;
                int minY = requestedScreenBounds.y;
                int maxX = requestedScreenBounds.x + requestedScreenBounds.width;
                int maxY = requestedScreenBounds.y + requestedScreenBounds.height;

                //if too far right, set to max x for this monitor
                if (requestedConsoleX + consoleFrameBackgroundWidth > maxX) {
                    requestedConsoleX = maxX - consoleFrameBackgroundWidth;
                }

                //if too far left, set to min x for this monitor
                else if (requestedConsoleX < minX) {
                    requestedConsoleX = minX;
                }

                //if too far down, set to max y for this monitor
                if (requestedConsoleY + consoleFrameBackgroundHeight > maxY) {
                    requestedConsoleY = maxY - consoleFrameBackgroundHeight;
                }

                //if too far up, set to min y
                else if (requestedConsoleY < minY) {
                    requestedConsoleY = minY;
                }
            }
            //otherwise display on the primary monitor
            else {
                //primary monitor bounds
                int minX = 0;
                int minY = 0;
                int maxX = ScreenUtil.getScreenWidth();
                int maxY = ScreenUtil.getScreenHeight();

                //if too far left, set to min x
                if (requestedConsoleX < minX) {
                    requestedConsoleX = 0;
                }

                //if too far right, set to max x minus console width
                if (requestedConsoleX + consoleFrameBackgroundWidth > maxX) {
                    requestedConsoleX = maxX - consoleFrameBackgroundWidth;
                }

                //if too far up, set to min y
                if (requestedConsoleY < minY) {
                    requestedConsoleY = 0;
                }

                //if too far down, set to max y minus console height
                if (requestedConsoleY + consoleFrameBackgroundHeight > maxY) {
                    requestedConsoleY = maxY - consoleFrameBackgroundHeight;
                }

            }

            //set the location to the calculated location
            consoleCyderFrame.setLocation(requestedConsoleX, requestedConsoleY);

            //show frame
            consoleCyderFrame.setVisible(true);

            //inform and log how long it took to load Console from program start
            CyderCommon.setConsoleStartTime(System.currentTimeMillis());

            String logString = "Console loaded in " +
                    (CyderCommon.getConsoleStartTime() - CyderCommon.getAbsoluteStartTime()) + "ms";
            Logger.log(Logger.Tag.ACTION, logString);
            notify(logString);

            //resume frame checker
            CyderCommon.resumeFrameChecker();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private void startExecutors() {
        //internet connection checker
        highPingChecker = new Thread(() -> {
            try {
                OUTER:
                    while (true) {
                        if (!NetworkUtil.decentPing()) {
                            consoleCyderFrame.notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() +
                                    ", but I had trouble connecting to the internet.\n" +
                                    "As a result, some features have been restricted until a " +
                                    "stable connection can be established.");
                            CyderCommon.setHighLatency(true);
                        } else {
                            CyderCommon.setHighLatency(false);
                        }

                        //sleep 5 minutes
                        int i = 0;
                        while (i < 5 * 60 * 1000) {
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
        }, "Stable Network Connection Checker");
        highPingChecker.start();

        //hourly Chime Checker
        hourlyChimerThread = new Thread(() -> {
            try {
                long initSleep = (3600 * 1000 //1 hour
                        - LocalDateTime.now().getMinute() * 60 * 1000 //minus minutes in hour to milis
                        - LocalDateTime.now().getSecond() * 1000); //minus seconds in hour to milis
                int j = 0;
                while (j < initSleep) {
                    Thread.sleep(50);
                    if (consoleFrameClosed) {
                        return;
                    }
                    j += 50;
                }

                OUTER:
                    while (true) {
                        if (UserUtil.getUserData("HourlyChimes").equalsIgnoreCase("1")) {
                            IOUtil.playSystemAudio("static/audio/chime.mp3");
                        }

                        //sleep 60 minutes
                        int i = 0;
                        while (i < 60 * 60 * 1000) {
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
        }, "Hourly Chime Checker");
        hourlyChimerThread.start();

        //Console Clock Updater
        consoleClockUpdaterThread = new Thread(() -> {
            OUTER:
                while (true) {
                    try {
                        refreshClockText();

                        //sleep 500 ms
                        int i = 0;
                        while (i < 500) {
                            Thread.sleep(50);
                            if (consoleFrameClosed) {
                                break OUTER;
                            }
                            i += 50;
                        }
                    }  catch (Exception e) {
                        //sometimes this throws for no reason trying to get times or something so log quietly
                        ExceptionHandler.silentHandle(e);
                    }
                }
        }, "Console Clock Updater");
        consoleClockUpdaterThread.start();

        //Cyder Busy Checker
        busyCheckerThread = new Thread(() -> {
            try {
                OUTER:
                    while (true) {
                        if (UserUtil.getUserData("showbusyicon").equals("1")) {
                            ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                            int num = threadGroup.activeCount();
                            Thread[] printThreads = new Thread[num];
                            threadGroup.enumerate(printThreads);

                            ArrayList<String> ignoreThreads = IOUtil.getIgnoreThreads().getIgnorethreads();

                            int busyThreads = 0;

                            for (int i = 0; i < num; i++) {
                                boolean contains = false;

                                for (String ignoreThread : ignoreThreads) {
                                    if (ignoreThread.equalsIgnoreCase(printThreads[i].getName())) {
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
                            } else if (CyderIcons.getCurrentCyderIcon() != CyderIcons.xxxIcon){
                                CyderIcons.setCurrentCyderIcon(CyderIcons.CYDER_ICON_BLINK);
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
        }, "Cyder Busy Checker");
        busyCheckerThread.start();

        Thread consolePosSaverThread = new Thread(() -> {
            try {
                //initial delay
                Thread.sleep(5000);

                OUTER:
                    while (true) {
                        User.ScreenStat screenStat = UserUtil.extractUser().getScreenStat();

                        //just to be safe
                        if (consoleCyderFrame != null) {
                            screenStat.setConsoleWidth(consoleCyderFrame.getWidth());
                            screenStat.setConsoleHeight(consoleCyderFrame.getHeight());
                            screenStat.setConsoleOnTop(consoleCyderFrame.isAlwaysOnTop());

                            int monitor = Integer.parseInt(consoleCyderFrame.getGraphicsConfiguration().getDevice()
                                    .getIDstring().replaceAll("[^0-9]", ""));
                            screenStat.setMonitor(monitor);

                            //remove this errr replace with monitor specific
                            screenStat.setConsoleX(consoleCyderFrame.getX());
                            screenStat.setConsoleY(consoleCyderFrame.getY());
                        }

                        User user = UserUtil.extractUser();
                        user.setScreenStat(screenStat);
                        UserUtil.setUserData(user);

                        //sleep 5000 ms
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
            }
        },"ConsoleFrame Position Saver");
        consolePosSaverThread.start();
    }

    //one time run things such as notifying due to special days, debug properties,
    // last start time, and auto testing
    private void onLaunch() {
        //special day events
        if (TimeUtil.isChristmas()) {
            consoleCyderFrame.notify("Merry Christmas!");
            cardReflector("Christmas", TimeUtil.getYear());
        }

        if (TimeUtil.isHalloween()) {
            consoleCyderFrame.notify("Happy Halloween!");
            cardReflector("Halloween", TimeUtil.getYear());
        }

        if (TimeUtil.isIndependenceDay()) {
            consoleCyderFrame.notify("Happy 4th of July!");
            cardReflector("Independence", TimeUtil.getYear());
        }

        if (TimeUtil.isThanksgiving()) {
            consoleCyderFrame.notify("Happy Thanksgiving!");
            cardReflector("Thanksgiving", TimeUtil.getYear());
        }

        if (TimeUtil.isAprilFoolsDay()) {
            consoleCyderFrame.notify("Happy April Fools Day!");
            cardReflector("AprilFools", TimeUtil.getYear());
        }

        if (TimeUtil.isValentinesDay()) {
            consoleCyderFrame.notify("Happy Valentines Day!");
        }

        //preference handlers here
        if (UserUtil.getUserData("DebugWindows").equals("1")) {
            StatUtil.systemProperties();
            StatUtil.computerProperties();
            StatUtil.javaProperties();
            StatUtil.debugMenu();
        }

        //testing mode
        if (IOUtil.getSystemData().isTestingmode()) {
            Logger.log(Logger.Tag.ENTRY, "TESTING MODE");
            Debug.launchTests();
        }

        //last start time operations
        if (TimeUtil.milisToDays(System.currentTimeMillis() -
                Long.parseLong(UserUtil.getUserData("laststart"))) > 1) {
            consoleCyderFrame.notify("Welcome back, " + ConsoleFrame.getConsoleFrame().getUsername() + "!");
        }

        UserUtil.setUserData("laststart", String.valueOf(System.currentTimeMillis()));

        introMusicCheck();
    }

    /**
     * Invokes the method with the name holiday + year from the CardsWidget
     * @param holiday the holiday name such as Christmas
     * @param year the year of the holiday such as 2021
     */
    private void cardReflector(String holiday, int year) {
        //don't reflect if in testing mode
        if (IOUtil.getSystemData().isTestingmode())
            return;

        try {
            CardWidget cardWidget = new CardWidget();

            for (Method m : cardWidget.getClass().getMethods()) {
                if (m.getName().toLowerCase().contains(holiday.toLowerCase())
                        && m.getName().toLowerCase().contains(String.valueOf(year))) {
                    m.invoke(cardWidget);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        }
    }

    /**
     * Determines what audio to play at the beginning of the ConsoleFrame startup.
     */
    private void introMusicCheck() {
        //if the user wants some custom intro music
        if (UserUtil.extractUser().getIntromusic().equalsIgnoreCase("1")) {
            ArrayList<String> musicList = new ArrayList<>();

            File userMusicDir = new File(OSUtil.buildPath("dynamic","users",
                    ConsoleFrame.getConsoleFrame().getUUID(), UserFile.MUSIC.getName()));

            String[] fileNames = userMusicDir.list();

            if (fileNames != null) {
                for (String fileName : fileNames) {
                    if (fileName.endsWith(".mp3")) {
                        musicList.add(fileName);
                    }
                }
            }

            //if they have music then play their own
            if (!musicList.isEmpty()) {
                String audioName = fileNames[NumberUtil.randInt(0, fileNames.length - 1)];

                IOUtil.playAudio(OSUtil.buildPath("dynamic","users",
                        ConsoleFrame.getConsoleFrame().getUUID(), UserFile.MUSIC.getName(), audioName));
            }
            //otherwise play our own
            else {
                IOUtil.playAudio(OSUtil.buildPath("static","audio","Ride.mp3"));
            }
        }
        //otherwise no intro music so check for gray scale image/play startup sound if released
        else if (IOUtil.getSystemData().isReleased()) {
            //Bad Apple / Beetlejuice / Michael Jackson reference for a grayscale image
            try {
                new Thread(() -> {
                    try {
                        Image icon = new ImageIcon(ImageIO.read(getCurrentBackgroundFile())).getImage();
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

                        if (correct) {
                            int rand = NumberUtil.randInt(0,2);
                            if (rand == 0) {
                                IOUtil.playAudio("static/audio/BadApple.mp3");
                            } else if (rand == 1){
                                IOUtil.playAudio("static/audio/BeetleJuice.mp3");
                            } else {
                                IOUtil.playAudio("static/audio/BlackOrWhite.mp3");
                            }
                        }
                        //not grayscale so play the default startup sound
                        else {
                            IOUtil.playSystemAudio("static/audio/startup.mp3");
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                },"Black or White Checker").start();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    private void installMenuTaskbarIcons() {
        boolean compactMode = UserUtil.extractUser().getCompactTextMode().equals("1");

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

        if (menuTaskbarFrames != null && menuTaskbarFrames.size() > 0) {
            for (int i = menuTaskbarFrames.size() - 1 ; i > -1 ; i--) {
                CyderFrame currentFrame = menuTaskbarFrames.get(i);

                if (compactMode) {
                    printingUtil.printlnComponent(currentFrame.getComapctTaskbarButton());
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

        LinkedList<User.MappedExecutable> exes = null;

        //mapped executables
        try {
           exes = UserUtil.extractUser().getExecutables();
        } catch (Exception e) {
            installMenuTaskbarIcons();
        }

        if (exes != null && !exes.isEmpty()) {
            if (!menuTaskbarFrames.isEmpty()) {
                printingUtil.printlnComponent(generateMenuSep());

                if (!compactMode)
                    printingUtil.println("");
            }

            for (User.MappedExecutable exe : exes) {
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
                    CyderFrame.generateDefaultCompactTaskbarComponent("Prefs", () -> UserEditor.showGUI(0)));

            printingUtil.printlnComponent(
                    CyderFrame.generateDefaultCompactTaskbarComponent("Logout", this::logout));
        } else {
            printingUtil.printlnComponent(
                    CyderFrame.generateDefaultTaskbarComponent("Prefs", () -> UserEditor.showGUI(0)));
            printingUtil.println("");

            printingUtil.printlnComponent(
                    CyderFrame.generateDefaultTaskbarComponent("Logout", this::logout));
        }

        //extracted common part from above if statement
        printingUtil.println("");

        //set menu location to top
        menuPane.setCaretPosition(0);

        consoleMenuGenerated = true;
    }

    /**
     * The JTextPane used for the console menu.
     */
    private JTextPane menuPane;

    private void generateConsoleMenu() {
        Font menuFont = CyderFonts.defaultFontSmall;
        int menuHeight = consoleCyderFrame.getHeight() - DragLabel.getDefaultHeight() - 5;

        menuButton.setIcon(new ImageIcon("static/pictures/icons/menu2.png"));

        if (menuLabel != null) {
            menuLabel.setVisible(false);
        }

        int MENUWIDTH = 110;

        menuLabel = new JLabel("");
        menuLabel.setBounds(-MENUWIDTH, DragLabel.getDefaultHeight() - 2,
                MENUWIDTH, menuHeight);
        menuLabel.setOpaque(true);
        menuLabel.setBackground(CyderColors.guiThemeColor);
        menuLabel.setVisible(true);
        menuLabel.setBorder(new LineBorder(Color.black, 5));
        consoleCyderFrame.getIconPane().add(menuLabel, JLayeredPane.MODAL_LAYER);

        Dimension menuSize = new Dimension(menuLabel.getWidth(), menuLabel.getHeight());

        menuPane = new JTextPane();
        menuPane.setEditable(false);
        menuPane.setAutoscrolls(false);
        menuPane.setFocusable(true);
        menuPane.setOpaque(false);
        menuPane.setBackground(CyderColors.guiThemeColor);

        CyderScrollPane menuScroll = new CyderScrollPane(menuPane);
        menuScroll.setThumbSize(5);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setFocusable(true);
        menuScroll.setOpaque(false);
        menuScroll.setThumbColor(CyderColors.regularPink);
        menuScroll.setBackground(CyderColors.guiThemeColor);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        menuScroll.setBounds(7, 10, (int) (menuSize.getWidth() - 10), menuHeight - 20);
        menuLabel.add(menuScroll);

        installMenuTaskbarIcons();
    }

    public void removeTaskbarIcon(CyderFrame associatedFrame) {
        if (menuTaskbarFrames.contains(associatedFrame)) {
            menuTaskbarFrames.remove(associatedFrame);
            consoleMenuGenerated = false;
            revalidateMenu();
        }
    }

    public void addTaskbarIcon(CyderFrame associatedFrame) {
        if (!menuTaskbarFrames.contains(associatedFrame)) {
            menuTaskbarFrames.add(associatedFrame);
            consoleMenuGenerated = false;
            revalidateMenu();
        }
    }

    public JLabel generateMenuSep() {
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

    private void minimizeMenu() {
        if (menuLabel.isVisible()) {
            new Thread(() -> {
                int width = consoleCyderFrame.getWidth();
                int height = consoleCyderFrame.getHeight();

                for (int i = inputField.getX() ; i > 15 ; i -= 8) {
                    outputScroll.setBounds(i, outputScroll.getY(), outputScroll.getWidth() + 1, outputScroll.getHeight());
                    inputField.setBounds(i, inputField.getY(), inputField.getWidth() + 1, inputField.getHeight());

                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                }

                outputScroll.setBounds(15, 62, width - 40, height - 204);
                inputField.setBounds(15, 62 + outputScroll.getHeight() + 20,width - 40,
                        height - (62 + outputScroll.getHeight() + 20 + 20));
            },"Console field animator").start();

            new Thread(() -> {
                menuLabel.setLocation(2, DragLabel.getDefaultHeight() - 2);
                int y = menuLabel.getY();

                for (int i = 0 ; i > -150 ; i-= 8) {
                    menuLabel.setLocation(i, y);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }

                menuLabel.setLocation(-150, y);

                menuLabel.setVisible(false);
                menuButton.setIcon(new ImageIcon("static/pictures/icons/menuSide1.png"));
            },"minimize menu thread").start();
        }
    }

    private KeyListener commandScrolling = new KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent event) {
            int code = event.getKeyCode();
            try {
                //command scrolling
                if ((event.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == 0 && ((event.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == 0)) {
                    //scroll to previous commands
                    if (code == KeyEvent.VK_UP) {
                        if (scrollingIndex - 1 >= 0) {
                            scrollingIndex -= 1;
                            inputField.setText(consoleBashString +  commandList.get(scrollingIndex).replace(consoleBashString, ""));
                        }
                    }
                    //scroll to subsequent command if exist
                    else if (code == KeyEvent.VK_DOWN) {
                        if (scrollingIndex + 1 < commandList.size()) {
                            scrollingIndex += 1;
                            inputField.setText(consoleBashString + commandList.get(scrollingIndex).replace(consoleBashString, ""));
                        } else if (scrollingIndex + 1 == commandList.size()) {
                            scrollingIndex += 1;
                            inputField.setText(consoleBashString);
                        }
                    }

                    //f17 Easter egg and other acknowlegement of other function keys
                    for (int i = 61440; i < 61452; i++) {
                        if (code == i) {
                            if (i - 61427 == 17) {
                                IOUtil.playAudio("static/audio/f17.mp3");
                            } else {
                                inputHandler.println("Interesting F" + (i - 61427) + " key");
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
     * Set the UUID for this Cyder session. Everything else relies on this being set and not null.
     * Once set, a one time check is performed to fix any possibly corrupted userdata.
     * @param uuid the user uuid that we will use to determine our output dir and other
     *             information specific to this instance of the console frame
     */
    public void setUUID(String uuid) {
        if (uuid == null)
            throw new IllegalArgumentException("Provided uuid is null");

        this.uuid = uuid;

        //perform preference injection
        UserUtil.fixUser();

        //log out all users that may have been left as logged in
        LoginHandler.logoutAllUsers();

        //log the current user in
        UserUtil.setUserData("loggedin","1");

        //resize backgrounds
        UserUtil.deleteInvalidBackgrounds();
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
     * Returns the username associated with the user currently logged into Cyder.
     *
     * @return the username associated with the user currently logged into Cyder
     */
    public String getUsername() {
        String name = UserUtil.getUserData("Name");

        if (name == null || name.trim().length() < 1)
            throw new RuntimeException("Username not found");

        return name;
    }

    public File getUserJson() {
        if (uuid == null)
            throw new RuntimeException("UUID not set");

        File userJson = new File(OSUtil.buildPath("dynamic","users", uuid, UserFile.USERDATA.getName()));

        if (!userJson.exists())
            throw new RuntimeException("User json does not exist");

        return userJson;
    }

    public void setFontBold() {
        fontMetric = Font.BOLD;
    }

    public void setFontItalic() {
        fontMetric = Font.ITALIC;
    }

    public void setFontPlain() {
        fontMetric = Font.PLAIN;
    }

    /**
     * Sets the OutputArea and InputField font style for the current user
     * @param combStyle use Font.BOLD and Font.Italic to set the user
     *                  font style. You may pass combinations of font
     *                  styling using the addition operator
     */
    public void setFontMetric(int combStyle) {
        fontMetric = combStyle;
    }

    /**
     * Sets the font size for the user to be used when {@link ConsoleFrame#getUserFont()} is called.
     * @param size the size of the font
     */
    public void setFontSize(int size) {
        fontSize = size;
    }

    /**
     * Get the desired user font in combination with the set font metric and font size.
     * @return the font to use for the input and output areas
     */
    public Font getUserFont() {
        return new Font(UserUtil.getUserData("Font"), fontMetric, fontSize);
    }

    /**
     * Get the user's foreground color from Userdata
     * @return a Color object representing the chosen foreground
     */
    public Color getUserForegroundColor() {
        return ColorUtil.hextorgbColor(UserUtil.getUserData("Foreground"));
    }

    /**
     * Get the user's background color from Userdata
     * @return a Color object representing the chosen background
     */
    public Color getUserBackgroundColor() {
        return ColorUtil.hextorgbColor(UserUtil.getUserData("Background"));
    }

    /**
     * Takes into account the dpi scaling value and checks all the backgrounds in the user's
     * directory against the current monitor's resolution. If any width or height of a background file
     * exceeds the monitor's width or height. We resize until it doesn't. We also check to make sure the background
     * meets our minimum pixel dimension parameters. The old images are automatically resized and replaced with the
     * properly resized and cropped images.
     */
    public void resizeBackgrounds() {
        try {
            ArrayList<File> backgrounds = getBackgrounds();

            int minWidth = 400;
            int minHeight = 400;
            int maxWidth = ScreenUtil.getScreenWidth();
            int maxHeight = ScreenUtil.getScreenHeight();

            for (File currentFile : backgrounds) {
                BufferedImage currentImage = ImageIO.read(currentFile);

                int backgroundWidth = currentImage.getWidth();
                int backgroundHeight = currentImage.getHeight();

                int imageType = currentImage.getType();

                //inform the user we are changing the size of the image
                boolean resizeNeeded = backgroundWidth > maxWidth || backgroundHeight > maxHeight ||
                        backgroundWidth < minWidth || backgroundHeight < minHeight;
                if (resizeNeeded)
                    PopupHandler.inform("Resizing the background image \"" + currentFile.getName() +
                            "\"", "System Action");

                Dimension resizeDimensions = ImageUtil.getImageResizeDimensions(minWidth,minHeight,maxWidth,maxHeight,currentImage);
                int deltaWidth = (int) resizeDimensions.getWidth();
                int deltaHeight = (int) resizeDimensions.getHeight();

                //if the image doesn't need a resize then continue to the next image
                if (deltaWidth == 0 || deltaHeight == 0)
                    continue;

                //save the modified image
                BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, deltaWidth, deltaHeight);
                ImageIO.write(saveImage, "png", currentFile);
            }

            //reinit backgrounds after resizing all backgrounds that needed fixing
            initBackgrounds();
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    public void initBackgrounds() {
        try {
            File dir = new File("dynamic/users/" + getUUID() + "/Backgrounds");
            FilenameFilter PNGFilter = (dir1, filename) -> filename.endsWith(".png");

            backgroundFiles = new ArrayList<>(Arrays.asList(dir.listFiles(PNGFilter)));

            //if no backgrounds, copy the default image icon over and recall initBackgrounds()
            if (backgroundFiles.size() == 0) {
                Image img = CyderIcons.defaultBackground.getImage();

                BufferedImage bi = new BufferedImage(img.getWidth(null),
                        img.getHeight(null),BufferedImage.TYPE_INT_RGB);

                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                File backgroundFile = new File("dynamic/users/" + uuid + "/Backgrounds/Default.png");
                backgroundFile.mkdirs();
                ImageIO.write(bi, "png", backgroundFile);

                initBackgrounds();
            }
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    public ArrayList<File> getBackgrounds() {
        initBackgrounds();
        return backgroundFiles;
    }

    public int getBackgroundIndex() {
        return backgroundIndex;
    }

    public void revalidateBackgroundIndex() {
        try {
            ImageIcon currentBackground = ((ImageIcon) ((JLabel) consoleCyderFrame.getContentPane()).getIcon());
            initBackgrounds();

            for (int i = 0 ; i < backgroundFiles.size() ; i++) {
                ImageIcon possibleMatch = new ImageIcon(ImageIO.read(backgroundFiles.get(i)));

                if (ImageUtil.imageIconsEqual(possibleMatch, currentBackground)) {
                    setBackgroundIndex(i);
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public void setBackgroundIndex(int i) {
        backgroundIndex = i;
    }

    public void incBackgroundIndex() {
        if (backgroundIndex + 1 == backgroundFiles.size()) {
            backgroundIndex = 0;
        } else {
            backgroundIndex += 1;
        }
    }

    public void decBackgroundIndex() {
        if (backgroundIndex - 1 < 0) {
            backgroundIndex = backgroundFiles.size() - 1;
        } else {
            backgroundIndex -= 1;
        }
    }

    public File getCurrentBackgroundFile() {
        backgroundFile = backgroundFiles.get(backgroundIndex);
        return backgroundFile;
    }

    public ImageIcon getCurrentBackgroundImageIcon() {
        try {
            File f = getCurrentBackgroundFile();
            backgroundImageIcon = new ImageIcon(ImageIO.read(f));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            return backgroundImageIcon;
        }
    }

    public ImageIcon getNextBackgroundImageIcon() {
        ImageIcon ret = null;

        try {
            if (backgroundIndex + 1 == backgroundFiles.size()) {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(0)));
            } else {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(backgroundIndex + 1)));
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            return ret;
        }
    }

    public ImageIcon getLastBackgroundImageIcon() {
        ImageIcon ret = null;

        try {
            if (backgroundIndex - 1 >= 0) {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(backgroundIndex - 1)));
            } else {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(backgroundFiles.size() - 1)));
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            return ret;
        }
    }

    public void switchBackground() {
        revalidateBackgroundIndex();

        try {
            ImageIcon oldBack = getCurrentBackgroundImageIcon();
            ImageIcon newBack = getNextBackgroundImageIcon();

            //get the dimensions which we will flip to, the next image
            int width = newBack.getIconWidth();
            int height = newBack.getIconHeight();

            //are we full screened and are we rotated?
            boolean fullscreen = isFullscreen();
            Direction direction = getConsoleDirection();

            //if full screen then get full screen images
            if (fullscreen) {
                width = ScreenUtil.getScreenWidth();
                height = ScreenUtil.getScreenHeight();

                oldBack = ImageUtil.resizeImage(oldBack, width, height);
                newBack = ImageUtil.resizeImage(newBack, width, height);
            }

            //when switching backgrounds, we ignore rotation if in full screen because it is impossible
            else {
                //not full screen and oriented left
                if (direction == Direction.LEFT) {
                    width = width + height;
                    height = width - height;
                    width = width - height;

                    oldBack = new ImageIcon(ImageUtil.rotateImageByDegrees(
                            ImageUtil.ImageIcon2BufferedImage(oldBack), -90));
                    newBack = new ImageIcon(ImageUtil.rotateImageByDegrees(
                            ImageUtil.ImageIcon2BufferedImage(newBack), -90));
                }

                //not full screen and oriented right
                else if (direction == Direction.RIGHT) {
                    width = width + height;
                    height = width - height;
                    width = width - height;

                    oldBack = new ImageIcon(ImageUtil.rotateImageByDegrees(
                            ImageUtil.ImageIcon2BufferedImage(oldBack), 90));
                    newBack = new ImageIcon(ImageUtil.rotateImageByDegrees(
                            ImageUtil.ImageIcon2BufferedImage(newBack), 90));
                } else if (direction == Direction.BOTTOM) {
                    oldBack = new ImageIcon(ImageUtil.rotateImageByDegrees(
                            ImageUtil.ImageIcon2BufferedImage(oldBack), 180));
                    newBack = new ImageIcon(ImageUtil.rotateImageByDegrees(
                            ImageUtil.ImageIcon2BufferedImage(newBack), 180));
                }
            }

            //make master image to set to background and slide
            ImageIcon combinedIcon;

            //before combining images, we need to resize to the new width and height
            oldBack = ImageUtil.resizeImage(oldBack, width, height);

            //we only need to resize our new image in the event of a full screen or rotation event
            if (newBack.getIconWidth() != width && newBack.getIconHeight() != height) {
                newBack = ImageUtil.resizeImage(newBack, width, height);
            }

            //update frame bounds and set location relative to old center
            int oldCenterX = consoleCyderFrame.getX() + consoleCyderFrame.getWidth() / 2;
            int oldCenterY = consoleCyderFrame.getY() + consoleCyderFrame.getHeight() / 2;

            consoleCyderFrame.setSize(width,height);

            consoleCyderFrame.setLocation(oldCenterX - width / 2, oldCenterY - height / 2);

            //icon to set as the background after sliding animation completes
            ImageIcon finalNewBack = newBack;

            switch (lastSlideDirection) {
                case LEFT:
                    //get combined icon
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.BOTTOM);
                    //set content pane bounds to hold combined image
                    consoleCyderFrame.getContentPane().setSize(
                            consoleCyderFrame.getContentPane().getWidth(),
                            consoleCyderFrame.getContentPane().getHeight() * 2);
                    //set content pane image
                    ((JLabel)consoleCyderFrame.getContentPane()).setIcon(combinedIcon);
                    //animate the image up
                    new Thread(() -> {
                        int delay = 5;
                        int increment = 8;

                        if (fullscreen) {
                            delay = 1;
                            increment = 20;
                        }

                        //disable dragging to avoid random repaints
                        consoleCyderFrame.disableDragging();

                        for (int i = 0; i >= -consoleCyderFrame.getHeight(); i -= increment) {
                            try {
                                Thread.sleep(delay);
                                consoleCyderFrame.getContentPane().setLocation(consoleCyderFrame.getContentPane().getX(), i);
                            } catch (InterruptedException e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                        //set proper location for complete animation
                        consoleCyderFrame.getContentPane().setLocation(consoleCyderFrame.getContentPane().getX(),
                                -consoleCyderFrame.getHeight());

                        //reanble dragging
                        consoleCyderFrame.enableDragging();

                        //reset content pane bounds
                        consoleCyderFrame.getContentPane().setLocation(0,0);

                        //reset the icon to the new one without combined icon
                        consoleCyderFrame.setBackground(finalNewBack);
                        ((JLabel)consoleCyderFrame.getContentPane()).setIcon(finalNewBack);

                        //refresh the background
                        consoleCyderFrame.refreshBackground();
                        consoleCyderFrame.getContentPane().revalidate();

                        //set our last slide direction
                        lastSlideDirection = Direction.TOP;
                        consoleCyderFrame.setMaximumSize(new Dimension(finalNewBack.getIconWidth(),
                                finalNewBack.getIconHeight()));
                    },"ConsoleFrame Background Switch Animation").start();

                    break;
                case TOP:
                    //get combined icon
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.LEFT);
                    //set content pane bounds to hold combined image
                    consoleCyderFrame.getContentPane().setBounds(-consoleCyderFrame.getContentPane().getWidth(),0,
                            consoleCyderFrame.getContentPane().getWidth() * 2,
                            consoleCyderFrame.getContentPane().getHeight());
                    //set content pane image
                    ((JLabel)consoleCyderFrame.getContentPane()).setIcon(combinedIcon);
                    //animate the image up
                    new Thread(() -> {
                        int delay = 5;
                        int increment = 8;

                        if (fullscreen) {
                            delay = 1;
                            increment = 20;
                        }

                        //disable dragging to avoid random repaints
                        consoleCyderFrame.disableDragging();

                        for (int i = -consoleCyderFrame.getWidth() ; i <= 0; i += increment) {
                            try {
                                Thread.sleep(delay);
                                consoleCyderFrame.getContentPane().setLocation(i, consoleCyderFrame.getContentPane().getY());
                            } catch (InterruptedException e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                        //set proper location for complete animation
                        consoleCyderFrame.getContentPane().setLocation(0, consoleCyderFrame.getContentPane().getY());

                        //reanble dragging
                        consoleCyderFrame.enableDragging();

                        //reset content pane bounds
                        consoleCyderFrame.getContentPane().setLocation(0,0);

                        //reset the icon to the new one without combined icon
                        consoleCyderFrame.setBackground(finalNewBack);
                        ((JLabel)consoleCyderFrame.getContentPane()).setIcon(finalNewBack);

                        //refresh the background
                        consoleCyderFrame.refreshBackground();
                        consoleCyderFrame.getContentPane().revalidate();

                        //set our last slide direction
                        lastSlideDirection = Direction.RIGHT;
                        consoleCyderFrame.setMaximumSize(new Dimension(finalNewBack.getIconWidth(),
                                finalNewBack.getIconHeight()));
                    },"ConsoleFrame Background Switch Animation").start();

                    break;
                case RIGHT:
                    //get combined icon
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.TOP);
                    //set content pane bounds to hold combined image
                    consoleCyderFrame.getContentPane().setBounds(0,-consoleCyderFrame.getHeight(),
                            consoleCyderFrame.getContentPane().getWidth(),
                            consoleCyderFrame.getContentPane().getHeight() * 2);
                    //set content pane image
                    ((JLabel)consoleCyderFrame.getContentPane()).setIcon(combinedIcon);
                    //animate the image up
                    new Thread(() -> {
                        int delay = 5;
                        int increment = 8;

                        if (fullscreen) {
                            delay = 1;
                            increment = 20;
                        }

                        //disable dragging to avoid random repaints
                        consoleCyderFrame.disableDragging();

                        for (int i = -consoleCyderFrame.getHeight() ; i <= 0; i += increment) {
                            try {
                                Thread.sleep(delay);
                                consoleCyderFrame.getContentPane().setLocation(consoleCyderFrame.getContentPane().getX(), i);
                            } catch (InterruptedException e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                        //set proper location for complete animation
                        consoleCyderFrame.getContentPane().setLocation(consoleCyderFrame.getContentPane().getX(), 0);

                        //reanble dragging
                        consoleCyderFrame.enableDragging();

                        //reset content pane bounds
                        consoleCyderFrame.getContentPane().setLocation(0,0);

                        //reset the icon to the new one without combined icon
                        consoleCyderFrame.setBackground(finalNewBack);
                        ((JLabel)consoleCyderFrame.getContentPane()).setIcon(finalNewBack);

                        //refresh the background
                        consoleCyderFrame.refreshBackground();
                        consoleCyderFrame.getContentPane().revalidate();

                        //set our last slide direction
                        lastSlideDirection = Direction.BOTTOM;
                        consoleCyderFrame.setMaximumSize(new Dimension(finalNewBack.getIconWidth(),
                                finalNewBack.getIconHeight()));
                    },"ConsoleFrame Background Switch Animation").start();

                    break;
                case BOTTOM:
                    //get combined icon
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.RIGHT);
                    //set content pane bounds to hold combined image
                    consoleCyderFrame.getContentPane().setBounds(0,0,
                            consoleCyderFrame.getContentPane().getWidth() * 2,
                            consoleCyderFrame.getContentPane().getHeight());
                    //set content pane image
                    ((JLabel)consoleCyderFrame.getContentPane()).setIcon(combinedIcon);
                    //animate the image up
                    new Thread(() -> {
                        int delay = 5;
                        int increment = 8;

                        if (fullscreen) {
                            delay = 1;
                            increment = 20;
                        }

                        //disable dragging to avoid random repaints
                        consoleCyderFrame.disableDragging();

                        for (int i = 0; i >= -consoleCyderFrame.getWidth() ; i -= increment) {
                            try {
                                Thread.sleep(delay);
                                consoleCyderFrame.getContentPane().setLocation(i, consoleCyderFrame.getContentPane().getY());
                            } catch (InterruptedException e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                        //set proper location for complete animation
                        consoleCyderFrame.getContentPane().setLocation(-consoleCyderFrame.getWidth() / 2,
                                consoleCyderFrame.getContentPane().getY());

                        //reanble dragging
                        consoleCyderFrame.enableDragging();

                        //reset content pane bounds
                        consoleCyderFrame.getContentPane().setLocation(0,0);

                        //reset the icon to the new one without combined icon
                        consoleCyderFrame.setBackground(finalNewBack);
                        ((JLabel)consoleCyderFrame.getContentPane()).setIcon(finalNewBack);

                        //refresh the background
                        consoleCyderFrame.refreshBackground();
                        consoleCyderFrame.getContentPane().revalidate();

                        //set our last slide direction
                        lastSlideDirection = Direction.LEFT;
                        consoleCyderFrame.setMaximumSize(new Dimension(finalNewBack.getIconWidth(),
                                finalNewBack.getIconHeight()));
                    },"ConsoleFrame Background Switch Animation").start();

                    break;
            }


            //increment background index
            incBackgroundIndex();

            //change tooltip to new image name
            ((JLabel) (consoleCyderFrame.getContentPane()))
                    .setToolTipText(StringUtil.getFilename(getCurrentBackgroundFile().getName()));

            //set input and output bounds
            outputScroll.setBounds(15, 62, width - 40, height - 204);
            inputField.setBounds(15, 62 + outputScroll.getHeight() + 20,width - 40,
                    height - (62 + outputScroll.getHeight() + 20 + 20));

            //request focus
            inputField.requestFocus();

            //fix menu
            revalidateMenu();

            //fix foreground if needed
            if (ImageUtil.solidColor(getCurrentBackgroundFile())) {
                getInputHandler().handle("fix foreground", false);
            }

            //fix frame out of bounds if needed
            if (consoleCyderFrame.getX() + consoleCyderFrame.getWidth() > ScreenUtil.getScreenWidth()) {
                consoleCyderFrame.setLocation(ScreenUtil.getScreenWidth() - consoleCyderFrame.getWidth(),
                        consoleCyderFrame.getY());
            }
            if (consoleCyderFrame.getX() < 0) {
                consoleCyderFrame.setLocation(0, consoleCyderFrame.getY());
            }
            if (consoleCyderFrame.getY() + consoleCyderFrame.getHeight() > ScreenUtil.getScreenHeight()) {
                consoleCyderFrame.setLocation(consoleCyderFrame.getX(),
                        ScreenUtil.getScreenHeight() - consoleCyderFrame.getHeight());
            }
            if (consoleCyderFrame.getY() < 0) {
                consoleCyderFrame.setLocation(consoleCyderFrame.getX(), 0);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * @return returns the current background with using the current background ImageIcon and regardless of whether full screen is active
     */
    public int getBackgroundWidth() {
        if (UserUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return ScreenUtil.getScreenHeight();
        else
            return getCurrentBackgroundImageIcon().getIconWidth();
    }

    /**
     * @return returns the current background height using the current background ImageIcon and regardless of whether full screen is active
     */
    public int getBackgroundHeight() {
        if (UserUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return ScreenUtil.getScreenHeight();
        else
            return getCurrentBackgroundImageIcon().getIconHeight();
    }

    public void setConsoleDirection(Direction conDir) {
        consoleDir = conDir;
        setFullscreen(false);
    }

    public Direction getConsoleDirection() {
        return consoleDir;
    }

    /**
     * Smoothly transitions the background icon to the specified degrees.
     * Use set console direction for console flipping and not this.
     * @param deg the degree by which to smoothly rotate
     */
    private void rotateConsole(int deg) {
        ImageIcon masterIcon = (ImageIcon) ((JLabel) consoleCyderFrame.getContentPane()).getIcon();
        BufferedImage master = ImageUtil.getBi(masterIcon);

        Timer timer = null;
        Timer finalTimer = timer;
        timer = new Timer(10, new ActionListener() {
            private double angle = 0;
            private double delta = 2.0;

            BufferedImage rotated;

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += delta;
                if (angle > deg) {
                    rotated = ImageUtil.rotateImageByDegrees(master, deg);
                    ((JLabel) consoleCyderFrame.getContentPane()).setIcon(new ImageIcon(rotated));
                    return;
                }
                rotated = ImageUtil.rotateImageByDegrees(master, angle);
                ((JLabel) consoleCyderFrame.getContentPane()).setIcon(new ImageIcon(rotated));
            }
        });
        timer.start();
    }

    /**
     * Repaints the ConsoleFrame based on the console flip diretion and regardless of whether fullscreen is turned on.
     * Use this method as a repaint essentially.
     * @param enable the fullscreen value of the frame
     */
    public void setFullscreen(Boolean enable) {
        try {
            fullscreen = enable;

            UserUtil.setUserData("fullscreen", enable ? "1" : "0");

            int w = 0;
            int h = 0;
            ImageIcon rotatedIcon = null;

            //determine the background
            if (fullscreen) {
                w = (int) ScreenUtil.getMonitorWidth(consoleCyderFrame);
                h = (int) ScreenUtil.getMonitorHeight(consoleCyderFrame);

                consoleDir = Direction.TOP;
                rotatedIcon = new ImageIcon(ImageUtil.resizeImage(w,h,getCurrentBackgroundFile()));
            } else {
                if (consoleDir == Direction.LEFT || consoleDir == Direction.RIGHT) {
                    w = getCurrentBackgroundImageIcon().getIconHeight();
                    h = getCurrentBackgroundImageIcon().getIconWidth();
                } else {
                    w = getCurrentBackgroundImageIcon().getIconWidth();
                    h = getCurrentBackgroundImageIcon().getIconHeight();
                }

                switch (consoleDir) {
                    case TOP:
                        rotatedIcon = getCurrentBackgroundImageIcon();
                        break;
                    case LEFT:
                        rotatedIcon = new ImageIcon(ImageUtil.getRotatedImage(
                                getCurrentBackgroundFile().getAbsolutePath(), Direction.LEFT));
                        break;
                    case RIGHT:
                        rotatedIcon = new ImageIcon(ImageUtil.getRotatedImage(
                                getCurrentBackgroundFile().getAbsolutePath(), Direction.RIGHT));
                        break;
                    case BOTTOM:
                        rotatedIcon = new ImageIcon(ImageUtil.getRotatedImage(
                                getCurrentBackgroundFile().getAbsolutePath(), Direction.BOTTOM));
                        break;
                }
            }

            consoleCyderFrame.setSize(w, h);
            consoleCyderFrame.setBackground(rotatedIcon);

            int addX = (int) ScreenUtil.getMonitorXOffset(consoleCyderFrame);
            int addY = (int) ScreenUtil.getMonitorYOffset(consoleCyderFrame);

            int width = (int) ScreenUtil.getMonitorWidth(consoleCyderFrame);
            int height = (int) ScreenUtil.getMonitorHeight(consoleCyderFrame);

            consoleCyderFrame.setLocation(
                    addX - w / 2 + width / 2,
                    addY - h / 2 + height / 2);

            outputScroll.setBounds(15, 62, w - 40, h - 204);
            inputField.setBounds(15, 62 + outputScroll.getHeight() + 20,w - 40,
                    h - (62 + outputScroll.getHeight() + 20 + 20));

            consoleCyderFrame.setMaximumSize(new Dimension(w, h));

            if (!menuLabel.isVisible()) {
                consoleMenuGenerated = false;
            }

            revalidateMenu();

            if (fullscreen) {
                consoleCyderFrame.disableDragging();
            } else {
                consoleCyderFrame.enableDragging();
            }

            consoleCyderFrame.refreshBackground();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public void refreshBasedOnPrefs() {
        //output border
        if (UserUtil.getUserData("OutputBorder").equals("0")) {
            outputScroll.setBorder(BorderFactory.createEmptyBorder());
        } else {
            outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")), 3, true));
        }

        //input border
        if (UserUtil.getUserData("InputBorder").equals("0")) {
            inputField.setBorder(null);
        } else {
            inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")), 3, true));
        }

        //full screen
        if (UserUtil.getUserData("FullScreen").equals("0") && isFullscreen()) {
            setFullscreen(false);
        } else if (UserUtil.getUserData("FullScreen").equals("1") && !isFullscreen()) {
            setFullscreen(true);
        }

        //console clock
        consoleClockLabel.setVisible(UserUtil.getUserData("ClockOnConsole").equals("1"));

        //output color fill
        if (UserUtil.getUserData("OutputFill").equals("0")) {
            outputArea.setBackground(null);
            outputArea.setOpaque(false);
        } else {
            outputArea.setOpaque(true);
            outputArea.setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")));
            outputArea.repaint();
            outputArea.revalidate();
        }

        //input color fill
        if (UserUtil.getUserData("InputFill").equals("0")) {
            inputField.setBackground(null);
            inputField.setOpaque(false);
        } else {
            inputField.setOpaque(true);
            inputField.setBackground(ColorUtil.hextorgbColor(UserUtil.getUserData("Background")));
            inputField.repaint();
            inputField.revalidate();
        }

        //round corners fixer
        for (Frame f : Frame.getFrames()) {
            f.repaint();
        }

        //always revalidate menu
        revalidateMenu();

        consoleCyderFrame.repaint();
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public int getScrollingIndex() {
        return scrollingIndex;
    }

    public void setScrollingIndex(int downs) {
        scrollingIndex = downs;
    }

    public void incScrollingIndex() {
        scrollingIndex += 1;
    }

    public void decScrollingIndex() {
        scrollingIndex -= 1;
    }

    public boolean onLastBackground() {
        initBackgrounds();
        return backgroundFiles.size() == backgroundIndex + 1;
    }

    public boolean canSwitchBackground() {
        return backgroundFiles.size() > 1;
    }

    public boolean isClosed() {
        return consoleFrameClosed;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public int getX() {
        return consoleCyderFrame.getX();
    }

    public int getY() {
        return consoleCyderFrame.getY();
    }

    public int getWidth() {
        return consoleCyderFrame.getWidth();
    }

    public int getHeight() {
        return consoleCyderFrame.getHeight();
    }

    public void rotateBackground(int degrees) {
        consoleCyderFrame.rotateBackground(degrees);
    }

    public void clearCommandHistory() {
        commandList.clear();
        scrollingIndex = 0;
    }

    public JTextPane getOutputArea() {
        return outputArea;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public ArrayList<String> getCommandHistory() {
        return commandList;
    }

    public void minimize() {
        consoleCyderFrame.minimizeAnimation();
    }

    public void minimizeAll() {
        User.ScreenStat screenStat = UserUtil.extractUser().getScreenStat();
        screenStat.setConsoleX(consoleCyderFrame.getX());
        screenStat.setConsoleY(consoleCyderFrame.getY());

        User user = UserUtil.extractUser();
        user.setScreenStat(screenStat);
        UserUtil.setUserData(user);

        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame) {
                ((CyderFrame) f).minimizeAnimation();
            } else {
                f.setState(Frame.ICONIFIED);
            }
        }
    }

    public void minimizeAllCyderFrames() {
        User.ScreenStat screenStat = UserUtil.extractUser().getScreenStat();
        screenStat.setConsoleX(consoleCyderFrame.getX());
        screenStat.setConsoleY(consoleCyderFrame.getY());

        User user = UserUtil.extractUser();
        user.setScreenStat(screenStat);
        UserUtil.setUserData(user);

        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame) {
                ((CyderFrame) f).minimizeAnimation();
            }
        }
    }

    public void barrelRoll() {
        consoleCyderFrame.barrelRoll();
    }

    public void setLocation(int x, int y) {
        consoleCyderFrame.setLocation(x, y);
    }

    public void setLocationRelativeTo(Component c) {
        consoleCyderFrame.setLocationRelativeTo(c);
    }

    public void setTitle(String title) {
        consoleCyderFrame.setTitle(title);
    }

    public void notify(String text) {
        consoleCyderFrame.notify(text);
    }

    public void flashSuggestionButton() {
        new Thread(() -> {
            try {
                for (int i = 0 ; i < 4 ; i++) {
                    helpButton.setIcon(CyderIcons.helpIconHover);
                    Thread.sleep(300);
                    helpButton.setIcon(CyderIcons.helpIcon);
                    Thread.sleep(300);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Suggestion Button Flash").start();
    }

    public void repaint() {
        setFullscreen(fullscreen);
    }

    /**
     * Figures out the index of where the current background is in case some files in that
     * directory were deleted/added. We cannot rely on the current background file or index
     */
    public void refreshBackgroundIndex() {
        ArrayList<File> backgroundFiles = ConsoleFrame.getConsoleFrame().getBackgrounds();
        String currentBackground = ((JLabel) consoleCyderFrame.getContentPane()).getToolTipText();

        for (int i = 0; i < backgroundFiles.size(); i++) {
            String thisname = StringUtil.getFilename(backgroundFiles.get(i));
            if (currentBackground.equalsIgnoreCase(thisname)) {
                ConsoleFrame.getConsoleFrame().setBackgroundIndex(i);
                break;
            }
        }
    }

    public CyderFrame getConsoleCyderFrame() {
        return consoleCyderFrame;
    }

    /**
     * Revalidates the console menu and places it where it was depending on if it was visible or not
     */
    public void revalidateMenu() {
        //if the frame is closed or the label simply doesn't exis
        if (consoleFrameClosed || menuLabel == null)
            return;

        consoleMenuGenerated = false;

        if (menuLabel.isVisible()) {
            menuButton.setIcon(new ImageIcon("static/pictures/icons/menu1.png"));
            installMenuTaskbarIcons();
        } else {
            menuButton.setIcon(new ImageIcon("static/pictures/icons/menuSide1.png"));
            //no other actions needed
        }

        //based on console menu, set bounds of fields
        int addX = 0;
        int w = consoleCyderFrame.getWidth();
        int h = consoleCyderFrame.getHeight();

        //offset for setting field bounds
        if (menuLabel.isVisible())
            addX = 2 + menuLabel.getWidth();

        //set bounds
        outputScroll.setBounds(addX + 15, 62, w - 40 - addX, h - 204);
        inputField.setBounds(addX + 15, 62 + outputScroll.getHeight() + 20,w - 40 - addX,
                h - (62 + outputScroll.getHeight() + 20 + 20));
    }

    public void animateOutAudioControls() {
        new Thread(() -> {
            for (int i = audioControlsLabel.getY() ; i > -40 ; i -= 8) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth() - 156, i);
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {}
            }
            audioControlsLabel.setVisible(false);
        }, "Console Audio Menu Minimizer").start();
    }

    public void animateOutAndRemoveAudioControls() {
        new Thread(() -> {
            for (int i = audioControlsLabel.getY() ; i > -40 ; i -= 8) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth() - 156, i);
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {}
            }
            audioControlsLabel.setVisible(false);
            removeAudioControls();
        }, "Console Audio Menu Minimizer").start();
    }

    public void animateInAudioControls() {
        new Thread(() -> {
            audioControlsLabel.setLocation(consoleCyderFrame.getWidth() - 156, -40);
            audioControlsLabel.setVisible(true);
            for (int i = -40 ; i < DragLabel.getDefaultHeight() - 2 ; i += 8) {
                audioControlsLabel.setLocation(consoleCyderFrame.getWidth() - 156, i);
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {}
            }
            audioControlsLabel.setLocation(consoleCyderFrame.getWidth() - 156, DragLabel.getDefaultHeight() - 2);
        }, "Console Audio Menu Minimizer").start();
    }

    public void revalidateAudioMenu() {
        if (!AudioPlayer.windowOpen() && !IOUtil.generalAudioPlaying()) {
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

            if (IOUtil.generalAudioPlaying() || AudioPlayer.audioPlaying()) {
                playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
            } else {
                playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Play.png"));
            }
        }
    }

    /**
     * Simply removes the audio controls, no questions asked
     */
    public void removeAudioControls() {
        audioControlsLabel.setVisible(false);
        toggleAudioControls.setVisible(false);
        consoleCyderFrame.getTopDragLabel().refreshButtons();
    }

    private void generateAudioMenu() {
        audioControlsLabel = new JLabel("");
        audioControlsLabel.setBounds(consoleCyderFrame.getWidth() - 156,
                -40, //negative height
                150,40);
        audioControlsLabel.setOpaque(true);
        audioControlsLabel.setBackground(CyderColors.guiThemeColor);
        audioControlsLabel.setBorder(new LineBorder(Color.black, 5));
        audioControlsLabel.setVisible(false);
        consoleCyderFrame.getIconPane().add(audioControlsLabel, JLayeredPane.MODAL_LAYER);

        JLabel stopMusicLabel = new JLabel("");
        stopMusicLabel.setBounds(45,5,30, 30);
        stopMusicLabel.setIcon(new ImageIcon("static/pictures/music/Stop.png"));
        audioControlsLabel.add(stopMusicLabel);
        stopMusicLabel.setToolTipText("Stop");
        stopMusicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                IOUtil.stopAllAudio();
                playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Play.png"));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                stopMusicLabel.setIcon(new ImageIcon("static/pictures/music/StopHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stopMusicLabel.setIcon(new ImageIcon("static/pictures/music/Stop.png"));
            }
        });
        stopMusicLabel.setVisible(true);
        stopMusicLabel.setOpaque(false);
        audioControlsLabel.add(stopMusicLabel);

        playPauseAudioLabel = new JLabel("");
        playPauseAudioLabel.setBounds(80,5,30, 30);

        audioControlsLabel.add(playPauseAudioLabel);
        playPauseAudioLabel.setToolTipText("Play/Pause");
        playPauseAudioLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (IOUtil.generalAudioPlaying()) {
                    IOUtil.stopAudio();
                } else if (AudioPlayer.audioPlaying()) {
                    IOUtil.pauseAudio();
                } else if (AudioPlayer.isPaused()) {
                    AudioPlayer.resumeAudio();
                } else if (AudioPlayer.windowOpen()) {
                    AudioPlayer.startAudio();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!IOUtil.generalAudioPlaying() && !AudioPlayer.audioPlaying()) {
                    playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/PlayHover.png"));
                } else {
                    playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/PauseHover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!IOUtil.generalAudioPlaying() && !AudioPlayer.audioPlaying()) {
                    playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Play.png"));
                } else {
                    playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
                }
            }
        });
        playPauseAudioLabel.setVisible(true);
        playPauseAudioLabel.setOpaque(false);
        audioControlsLabel.add(playPauseAudioLabel);
        if (!IOUtil.generalAudioPlaying() && !AudioPlayer.audioPlaying()) {
            playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Play.png"));
        } else {
            playPauseAudioLabel.setIcon(new ImageIcon("static/pictures/music/Pause.png"));
        }

        JLabel nextMusicLabel = new JLabel("");
        nextMusicLabel.setBounds(110,5,30, 30);
        nextMusicLabel.setIcon(new ImageIcon("static/pictures/music/Skip.png"));
        audioControlsLabel.add(nextMusicLabel);
        nextMusicLabel.setToolTipText("Skip");
        nextMusicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (AudioPlayer.windowOpen()) {
                    AudioPlayer.nextAudio();
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

        JLabel lastMusicLabel = new JLabel("");
        lastMusicLabel.setBounds(10,5,30, 30);
        lastMusicLabel.setIcon(new ImageIcon("static/pictures/music/SkipBack.png"));
        audioControlsLabel.add(nextMusicLabel);
        lastMusicLabel.setToolTipText("Previous");
        lastMusicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (AudioPlayer.windowOpen()) {
                    AudioPlayer.previousAudio();
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
    }

    /**
     * Sets the console frame to a provided ScreenPosition and moves any pinned CyderFrame windows with it
     * @param screenPos the screen position to move the ConsoleFrame to
     */
    public void setLocationOnScreen(ScreenPosition screenPos) {
        LinkedList<RelativeFrame> frames = getPinnedFrames();

        switch(screenPos) {
            case CENTER:
                consoleCyderFrame.setLocationRelativeTo(null);
                break;
            case TOP_LEFT:
                consoleCyderFrame.setLocation(0, 0);
                break;
            case TOP_RIGHT:
                consoleCyderFrame.setLocation(ScreenUtil.getScreenWidth() - ConsoleFrame.getConsoleFrame().getWidth(), 0);
                break;
            case BOTTOM_LEFT:
                consoleCyderFrame.setLocation(0, ScreenUtil.getScreenHeight() - ConsoleFrame.getConsoleFrame().getHeight());
                break;
            case BOTTOM_RIGHT:
                consoleCyderFrame.setLocation(ScreenUtil.getScreenWidth() - ConsoleFrame.getConsoleFrame().getWidth(),
                        ScreenUtil.getScreenHeight() - ConsoleFrame.getConsoleFrame().getHeight());
                break;
        }

        for (RelativeFrame rf : frames) {
            rf.getFrame().setLocation(rf.getxRelative() + consoleCyderFrame.getX(), rf.getyRelative() + consoleCyderFrame.getY());
        }
    }

    private static class RelativeFrame {
        private CyderFrame frame;
        private int xRelative;
        private int yRelative;

        public RelativeFrame(CyderFrame frame, int xRelative, int yRelative) {
            this.frame = frame;
            this.xRelative = xRelative;
            this.yRelative = yRelative;
        }

        public CyderFrame getFrame() {
            return frame;
        }

        public void setFrame(CyderFrame frame) {
            this.frame = frame;
        }

        public int getxRelative() {
            return xRelative;
        }

        public void setxRelative(int xRelative) {
            this.xRelative = xRelative;
        }

        public int getyRelative() {
            return yRelative;
        }

        public void setyRelative(int yRelative) {
            this.yRelative = yRelative;
        }
    }

    //returns all frames pinned to the ConsoleFrame
    private LinkedList<RelativeFrame> getPinnedFrames() {
        LinkedList<RelativeFrame> frames = new LinkedList<>();

        Rectangle consoleRect = new Rectangle(consoleCyderFrame.getX(), consoleCyderFrame.getY(),
                consoleCyderFrame.getWidth(), consoleCyderFrame.getHeight());

        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame) {
                if (((CyderFrame) f).isConsolePinned() &&
                        !f.getTitle().equals(consoleCyderFrame.getTitle())) {
                    Rectangle frameRect = new Rectangle(f.getX(), f.getY(), f.getWidth(), f.getHeight());

                    if (GeometryAlgorithms.overlaps(consoleRect,frameRect)) {
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
           if (consoleClockLabel == null || !consoleClockLabel.isVisible())
               return;

           //the user set time
           String pattern = UserUtil.extractUser().getConsoleclockformat();
           String time = TimeUtil.getTime(pattern);

           String regularSecondTime = TimeUtil.consoleSecondTime();
           String regularNoSecondTime = TimeUtil.consoleNoSecondTime();

           //no custom pattern so take into account showSeconds
           if (time.equalsIgnoreCase(regularSecondTime) || time.equalsIgnoreCase(regularNoSecondTime)) {
               if (UserUtil.extractUser().getShowseconds().equalsIgnoreCase("1")) {
                   time = regularSecondTime;
               } else {
                   time = regularNoSecondTime;
               }
           }

           int w = CyderFrame.getMinWidth(time, consoleClockLabel.getFont());
           int h = CyderFrame.getAbsoluteMinHeight(time, consoleClockLabel.getFont());
           consoleClockLabel.setBounds(consoleCyderFrame.getWidth() / 2 - w / 2,
                   0, w, h);
           consoleClockLabel.setText(time);
       } catch (Exception ignored) {}
       //sometimes extracting user throws so we will ignore exceptions thrown from this method
    }

    /**
     * Simply closes the console frame due to a user logout.
     *
     * @param exit whether or not to exit Cyder upon closing the ConsoleFrame
     */
    public void closeConsoleFrame(boolean exit) {
        //stop any audio
        IOUtil.stopAudio();

        //close the input handler
        inputHandler.killThreads();
        inputHandler = null;

        //logs
        Logger.log(Logger.Tag.LOGOUT, "[" + getUsername() + "]");
        UserUtil.setUserData("loggedin","0");

        //remove closing actions
        consoleCyderFrame.removePostCloseActions();

        //dispose and set closed var as true
        if (exit) {
            consoleCyderFrame.addPostCloseAction(() -> CyderCommon.exit(25));
        }

        consoleFrameClosed = true;
        consoleCyderFrame.dispose();
    }

    /**
     * Saves the console frame position to the currently logged in user's json file.
     */
    public void saveConsoleFramePosition() {
        if (this.getUUID() == null)
            return;

        //save window location
        User.ScreenStat screenStat = UserUtil.extractUser().getScreenStat();

        if (consoleCyderFrame != null) {
            screenStat.setConsoleWidth(consoleCyderFrame.getWidth());
            screenStat.setConsoleHeight(consoleCyderFrame.getHeight());
            screenStat.setConsoleX(consoleCyderFrame.getX());
            screenStat.setConsoleY(consoleCyderFrame.getY());
            screenStat.setConsoleOnTop(consoleCyderFrame.isAlwaysOnTop());
            int monitor = Integer.parseInt(consoleCyderFrame.getGraphicsConfiguration().getDevice()
                    .getIDstring().replaceAll("[^0-9]", ""));
            screenStat.setMonitor(monitor);
        }

        User user = UserUtil.extractUser();
        user.setScreenStat(screenStat);
        UserUtil.setUserData(user);
    }

    public void logout() {
        saveConsoleFramePosition();

        CyderCommon.suspendFrameChecker();

        //close the consoleframe if it's still open
        closeConsoleFrame(false);

        //close all residual cyderframes
        for (Frame f : Frame.getFrames()) {
            f.dispose();
        }

        LoginHandler.showGUI();
    }

    //dancing stuff -----------------------------------------------------------------------

    private boolean currentlyDancing = false;

    /**
     * Invokes dance in a synchronous way on all CyderFrame instances
     */
    public void dance() {
        //anonymous inner class
        class RestoreFrame {
            int restoreX;
            int restoreY;
            CyderFrame frame;
            boolean draggingWasEnabled;

            public RestoreFrame(CyderFrame frame, int restoreX, int restoreY, boolean draggingWasEnabled) {
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
                        f.getX(), f.getY(), ((CyderFrame) f).draggingEnabled()));
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
            f.frame.setDancingFinished(false);
            if (f.draggingWasEnabled) {
                f.frame.enableDragging();
            }
        }
    }

    /**
     * Ends the dancing sequence if on-going
     */
    public void stopDancing() {
        //end dancing sequence
        currentlyDancing = false;

        //reset all frames to dance again
        for (Frame f : Frame.getFrames()) {
            if (f instanceof CyderFrame) {
                ((CyderFrame) f).setDancingDirection(CyderFrame.DancingDirection.INITIAL_UP);
            }
        }
    }

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

    //end dancing stuff -------------------------------------------------------------------
}
