package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.enums.Direction;
import cyder.exception.FatalException;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.handler.InputHandler;
import cyder.handler.SessionLogger;
import cyder.utilities.*;
import cyder.widgets.Calculator;
import cyder.widgets.GenericInform;
import cyder.widgets.TempConverter;
import cyder.widgets.Weather;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public final class ConsoleFrame {
    //the one and only console frame method
    private static ConsoleFrame consoleFrameInstance = new ConsoleFrame();

    public static ConsoleFrame getConsoleFrame() {
        return consoleFrameInstance;
    }

    private ConsoleFrame() {} //no instantiation this way

    //program driver
    private String UUID;
    private CyderFrame consoleCyderFrame;
    private InputHandler inputHandler;

    //ui elements
    private CyderScrollPane outputScroll;
    public static JTextPane outputArea;
    private JPasswordField inputField;
    private JLabel consoleClockLabel;
    private JLabel menuLabel;
    private JButton suggestionButton;
    private JButton menuButton;
    private JButton alternateBackground;

    //boolean vars
    private boolean menuGenerated;
    private boolean drawConsoleLines;
    private boolean consoleLinesDrawn;
    private boolean fullscreen;
    private boolean doThreads;
    private boolean closed = true;

    //background vars
    private LinkedList<File> backgroundFiles;
    private int backgroundIndex;
    private File backgroundFile;
    private ImageIcon backgroundImageIcon;

    //string and font vars
    private String consoleBashString;
    private int fontMetric = Font.BOLD;
    private int fontSize = 30;

    //debug vars
    private Color lineColor = Color.white;

    //command scrolling
    public static ArrayList<String> operationList = new ArrayList<>();
    private static int scrollingIndex;

    //directional enums
    private Direction lastSlideDirection = Direction.LEFT;
    private Direction consoleDir = Direction.TOP;

    public void start() {
        if (consoleCyderFrame != null)
            consoleCyderFrame.closeAnimation();

        resizeBackgrounds();
        initBackgrounds();

        try {
            //set variables
            consoleBashString = getUsername() + "@Cyder:~$ ";
            lineColor = ImageUtil.getDominantColorOpposite(ImageIO.read(getCurrentBackgroundFile()));
            lastSlideDirection = Direction.LEFT;
            consoleDir = Direction.TOP;
            operationList.clear();
            scrollingIndex = 0;
            fullscreen = false;
            closed = false;

            //handle random background by setting a random background index
            if (IOUtil.getUserData("RandomBackground").equals("1")) {
                if (getBackgrounds().size() <= 1) {
                    consoleCyderFrame.notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", " +
                            "but you only have one background file so there's no random element to be chosen.");
                } else {
                    backgroundIndex = NumberUtil.randInt(0,backgroundFiles.size() - 1);
                }
            }

            //get proper width, height, and background image icon,
            // we take into account console rotation and fullscreen here
            int w = 0;
            int h = 0;
            ImageIcon usage = null;

            if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                w = (int) SystemUtil.getScreenSize().getWidth();
                h = (int) SystemUtil.getScreenSize().getHeight();
                usage = new ImageIcon(ImageUtil.resizeImage(w,h,getCurrentBackgroundFile()));
                fullscreen = true;
            } else {
                w = getCurrentBackgroundImageIcon().getIconWidth();
                h = getCurrentBackgroundImageIcon().getIconHeight();
                usage = new ImageIcon(ImageUtil.getRotatedImage(
                        getCurrentBackgroundFile().toString(),getConsoleDirection()));
            }

            //override the CyderFrame we use for ConsoleFrame to add in the debug lines
            consoleCyderFrame = new CyderFrame(w, h, usage) {
                @Override
                public void paint(Graphics g) {
                    super.paint(g);

                    if (drawConsoleLines && !consoleLinesDrawn) {
                        Graphics2D g2d = (Graphics2D) g;

                        BufferedImage img = null;
                        int w = 0;
                        int h = 0;

                        try {
                            img = ImageUtil.resizeImage(25,25,ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile());
                            w = img.getWidth(null);
                            h = img.getHeight(null);

                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }

                        g2d.setPaint(lineColor);
                        int strokeThickness = 4;
                        g2d.setStroke(new BasicStroke(strokeThickness));

                        g2d.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
                        g2d.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);

                        if (img != null)
                            g2d.drawImage(img, getWidth() / 2 - w / 2, getHeight() / 2 - h / 2, null);

                        consoleLinesDrawn = true;
                    }
                }

                @Override
                public void setBounds(int x, int y, int w, int h) {
                    super.setBounds(x,y,w,h);

                    consoleLinesDrawn = false;
                    drawConsoleLines = false;

                    if (outputScroll != null && inputField != null) {
                        outputScroll.setBounds(10, 62, w - 20, h - 204);
                        inputField.setBounds(10, 62 + outputScroll.getHeight() + 20,w - 20,
                                h - (62 + outputScroll.getHeight() + 20 + 20));
                    }
                }
            };

            //on minimize / reopen end/start threads for optimization
            //on launch, request input field focus and run onLaunch() method
            consoleCyderFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowIconified(WindowEvent e) {
                    inputField.requestFocus();
                    endExecutors();
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                    inputField.requestFocus();
                    startExecutors();
                }

                @Override
                public void windowOpened(WindowEvent e) {
                    inputField.requestFocus();
                    onLaunch();
                }

                @Override
                public void windowClosing(WindowEvent e) {
                   GenesisShare.exit(25);
                }
            });

            //we should always be using controlled exits so this is why we use DO_NOTHING_ON_CLOSE
            consoleCyderFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            consoleCyderFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);

            consoleCyderFrame.paintWindowTitle(false);
            consoleCyderFrame.paintSuperTitle(true);
            consoleCyderFrame.setTitle(IOUtil.getSystemData("Version") +
                    " Cyder [" + ConsoleFrame.getConsoleFrame().getUsername() + "]");

            if (IOUtil.getSystemData("ConsoleResizable").equals("1")) {
                consoleCyderFrame.initializeResizing();
                consoleCyderFrame.setResizable(true);
                consoleCyderFrame.setMinimumSize(new Dimension(600,600));
                consoleCyderFrame.setMaximumSize(new Dimension(w, h));
            }

            ((JLabel) (consoleCyderFrame.getContentPane()))
                    .setToolTipText(StringUtil.getFilename(getCurrentBackgroundFile().getName()));

            outputArea = new JTextPane() {
                @Override
                public String toString() {
                    return "JTextPane outputArea used for ConsoleFrame instance: " + consoleCyderFrame;
                }
            };
            outputArea.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                }

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
            outputArea.setSelectionColor(CyderColors.selectionColor);
            outputArea.setOpaque(false);
            outputArea.setBackground(CyderColors.nul);
            outputArea.setForeground(ConsoleFrame.getConsoleFrame().getUserForegroundColor());
            outputArea.setFont(ConsoleFrame.getConsoleFrame().getUserFont());

            //init input handler
            inputHandler = new InputHandler(outputArea);

            //start printing queue for input handler
            inputHandler.startConsolePrintingAnimation();

            outputScroll = new CyderScrollPane(outputArea,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            outputScroll.setThumbColor(CyderColors.intellijPink);
            outputScroll.getViewport().setOpaque(false);
            outputScroll.setOpaque(false);
            outputScroll.setFocusable(true);

            if (IOUtil.getUserData("OutputBorder").equalsIgnoreCase("1")) {
                outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")),
                        3, false));
            } else {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            outputScroll.setBounds(10, 62, getBackgroundWidth() - 20, getBackgroundHeight() - 204);
            consoleCyderFrame.getContentPane().add(outputScroll);

            //output area settings complete; starting input field
            inputField = new JPasswordField(40);
            inputField.setEchoChar((char)0);
            inputField.setText(consoleBashString);

            if (IOUtil.getUserData("InputBorder").equalsIgnoreCase("1")) {
                inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor
                        (IOUtil.getUserData("Background")), 3, false));
            } else {
                inputField.setBorder(null);
            }

            //input field key listeners such as auto-capitalization, escaping, and console rotations
            inputField.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    //capitalization
                    if (inputField.getPassword().length == consoleBashString.length() + 1) {
                        inputField.setText(consoleBashString + String.valueOf(
                                inputField.getPassword()).substring(consoleBashString.length()).toUpperCase());
                    }

                    //escaping
                    if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
                        try {
                            inputHandler.handle("controlc");
                        } catch (Exception exception) {
                            ErrorHandler.handle(exception);
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
                public void keyReleased(java.awt.event.KeyEvent e) {
                    //capitalization
                    if (inputField.getPassword().length == consoleBashString.length() + 1) {
                        inputField.setText(consoleBashString +
                                String.valueOf(inputField.getPassword()).substring(consoleBashString.length()).toUpperCase());
                    }
                    //debug lines
                    if ((KeyEvent.SHIFT_DOWN_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        if (!consoleLinesDrawn) {
                            drawConsoleLines = true;
                        } else {
                            drawConsoleLines = false;
                            consoleLinesDrawn = false;
                        }
                        consoleCyderFrame.repaint();
                    }
                }

                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    //capitalization
                    if (inputField.getPassword().length == consoleBashString.length() + 1) {
                        inputField.setText(consoleBashString + String.valueOf(
                                inputField.getPassword()).substring(consoleBashString.length()).toUpperCase());
                    }
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
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK), "forcedexit");

            inputField.getActionMap().put("forcedexit", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GenesisShare.exit(-404);
                }
            });

            //a bodge to update the caret position if it goes before an allowed index for console bash string
            new Thread(() -> {
                try {
                    while (consoleCyderFrame != null) {
                        if (inputField.getCaretPosition() < consoleBashString.length()) {
                            inputField.setCaretPosition(inputField.getPassword().length);
                        }

                        //if it doesn't start with bash string, reset it to it
                        if (!String.valueOf(inputField.getPassword()).startsWith(consoleBashString)) {
                            inputField.setText(consoleBashString);
                        }

                        Thread.sleep(50);
                    }
                }

                catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            },"Console Input Caret Position Updater").start();

            inputField.setToolTipText("Input Field");
            inputField.setSelectionColor(CyderColors.selectionColor);
            inputField.addKeyListener(commandScrolling);
            inputField.setCaretPosition(consoleBashString.length());

            inputField.setBounds(10, 62 + outputArea.getHeight() + 20,w - 20,
                    h - (62 + outputArea.getHeight() + 20 + 20));
            inputField.setOpaque(false);
            consoleCyderFrame.getContentPane().add(inputField);
            inputField.addActionListener(e -> {
                try {
                    String op = String.valueOf(inputField.getPassword()).substring(consoleBashString.length()).trim();

                    if (!StringUtil.empytStr(op)) {
                        if (!(operationList.size() > 0 && operationList.get(operationList.size() - 1).equals(op))) {
                            operationList.add(op);
                        }

                        scrollingIndex = operationList.size();

                        //calls to linked inputhandler
                        if (!inputHandler.getUserInputMode()) {
                            inputHandler.handle(op);
                        } else if (inputHandler.getUserInputMode()) {
                            inputHandler.setUserInputMode(false);
                            inputHandler.handleSecond(op);
                        }
                    }

                    inputField.setText(consoleBashString);
                    inputField.setCaretPosition(consoleBashString.length());
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            });
            inputField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                }
            });

            inputField.setCaretColor(ConsoleFrame.getConsoleFrame().getUserForegroundColor());
            inputField.setCaret(new CyderCaret(ConsoleFrame.getConsoleFrame().getUserForegroundColor()));
            inputField.setForeground(ConsoleFrame.getConsoleFrame().getUserForegroundColor());
            inputField.setFont(ConsoleFrame.getConsoleFrame().getUserFont());

            if (IOUtil.getUserData("OutputFill").equals("1")) {
                outputArea.setOpaque(true);
                outputArea.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
                outputArea.repaint();
                outputArea.revalidate();
            }

            if (IOUtil.getUserData("InputFill").equals("1")) {
                inputField.setOpaque(true);
                inputField.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
                inputField.repaint();
                inputField.revalidate();
            }

            suggestionButton = new JButton("");
            suggestionButton.setToolTipText("Suggestions");
            suggestionButton.addActionListener(e -> {
                consoleCyderFrame.notify("What feature would you like to suggest? " +
                        "(Please include as much detail as possible such as " +
                        "how the feature should be triggered and how the program should responded; be detailed)");
                inputHandler.setUserInputDesc("suggestion");
                inputHandler.setUserInputMode(true);
                inputField.requestFocus();
            });
            suggestionButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("sys/pictures/icons/suggestion2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("sys/pictures/icons/suggestion1.png"));
                }
            });
            suggestionButton.setBounds(32, 4, 22, 22);
            suggestionButton.setIcon(new ImageIcon("sys/pictures/icons/suggestion1.png"));
            consoleCyderFrame.getTopDragLabel().add(suggestionButton);
            suggestionButton.setFocusPainted(false);
            suggestionButton.setOpaque(false);
            suggestionButton.setContentAreaFilled(false);
            suggestionButton.setBorderPainted(false);

            menuButton = new JButton("");
            menuLabel = new JLabel();
            menuLabel.setVisible(false);
            menuButton.setToolTipText("Menu");
            menuButton.addMouseListener(consoleMenu);
            menuButton.setBounds(4, 4, 22, 22);
            menuButton.setIcon(new ImageIcon("sys/pictures/icons/menuSide1.png"));
            consoleCyderFrame.getTopDragLabel().add(menuButton);
            menuButton.setVisible(true);
            menuButton.setFocusPainted(false);
            menuButton.setOpaque(false);
            menuButton.setContentAreaFilled(false);
            menuButton.setBorderPainted(false);

            consoleCyderFrame.getTopDragLabel().addMinimizeListener(e -> minimizeMenu());

            alternateBackground = new JButton("");
            alternateBackground.setToolTipText("Alternate Background");
            alternateBackground.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("sys/pictures/icons/ChangeSize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("sys/pictures/icons/ChangeSize1.png"));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    ConsoleFrame.getConsoleFrame().initBackgrounds();

                    try {
                        if (canSwitchBackground()) {
                            switchBackground();
                        }  else if (getBackgrounds().size() == 1) {
                            consoleCyderFrame.notify("You only have one background image. " +
                                    "Would you like to add more? (Enter yes/no)");
                            inputField.requestFocus();
                            inputHandler.setUserInputMode(true);
                            inputHandler.setUserInputDesc("addbackgrounds");
                            inputField.requestFocus();
                        }
                    } catch (Exception ex) {
                        ErrorHandler.handle(new FatalException("Background DNE"));
                        consoleCyderFrame.notify("Error in parsing background; perhaps it was deleted.");
                    }
                }
            });

            alternateBackground.setIcon(new ImageIcon("sys/pictures/icons/ChangeSize1.png"));
            alternateBackground.setFocusPainted(false);
            alternateBackground.setOpaque(false);
            alternateBackground.setContentAreaFilled(false);
            alternateBackground.setBorderPainted(false);
            consoleCyderFrame.getTopDragLabel().addButton(alternateBackground,1);

            consoleCyderFrame.getTopDragLabel().addCloseListener(e -> {
                endExecutors();
                GenesisShare.exit(25);
            });

            //this turns into setting a center title
            consoleClockLabel = new JLabel(TimeUtil.consoleTime(), SwingConstants.CENTER);
            consoleClockLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(20f));
            consoleClockLabel.setForeground(CyderColors.vanila);
            //bounds not needed to be set since the executor service handles that
            consoleCyderFrame.getTopDragLabel().add(consoleClockLabel);
            consoleClockLabel.setVisible(true);

            //spin off console executors
            startExecutors();

            //show frame
            consoleCyderFrame.setVisible(true);
            consoleCyderFrame.setLocationRelativeTo(null);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void startExecutors() {
        //set control boolean
        doThreads = true;

        //internet connection checker
        new Thread(() -> {
            try {
                while (doThreads) {
                    if (!NetworkUtil.internetReachable()) {
                        consoleCyderFrame.notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() +
                                ", but I had trouble connecting to the internet.\n" +
                                "As a result, some features may not work properly.");
                    }

                    //sleep 5 minutes
                    Thread.sleep(5 * 60 * 1000);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Stable Network Connection Checker").start();

        //hourly Chime Checker
        new Thread(() -> {
            try {
                Thread.sleep(3600 * 1000 //1 hour
                        - LocalDateTime.now().getMinute() * 60 * 1000 //minus minutes in hour to milis
                        - LocalDateTime.now().getSecond() * 1000); //minus seconds in hour to milis

                while (doThreads) {
                    if (IOUtil.getUserData("HourlyChimes").equalsIgnoreCase("1")) {
                        IOUtil.playSystemAudio("sys/audio/chime.mp3");
                    }

                    //sleep 60 minutes
                    Thread.sleep(60 * 60 * 1000);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Hourly Chime Checker").start();

        //Console Clock Updater
        new Thread(() -> {
            try {
                while (doThreads) {
                    if (IOUtil.getUserData("ClockOnConsole").equalsIgnoreCase("1")) {
                        if (IOUtil.getUserData("ShowSeconds").equalsIgnoreCase("1")) {
                            String time = TimeUtil.consoleSecondTime();
                            int clockWidth = CyderFrame.getMinWidth(time, consoleClockLabel.getFont()) + 10;
                            int clockHeight = CyderFrame.getMinHeight(time, consoleClockLabel.getFont());

                            consoleClockLabel.setBounds(consoleCyderFrame.getWidth() / 2 - clockWidth / 2,
                                    -5, clockWidth, clockHeight);
                            consoleClockLabel.setText(time);
                        } else {
                            String time = TimeUtil.consoleTime();
                            int clockWidth = CyderFrame.getMinWidth(time, consoleClockLabel.getFont()) + 10;
                            int clockHeight = CyderFrame.getMinHeight(time, consoleClockLabel.getFont());

                            consoleClockLabel.setBounds(consoleCyderFrame.getWidth() / 2 - clockWidth / 2,
                                    -5, clockWidth, clockHeight);
                            consoleClockLabel.setText(time);
                        }
                    }

                    //sleep 1 second
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Console Clock Updater").start();

        //Console Clock Updater
        new Thread(() -> {
            try {
                while (doThreads) {
                    ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                    int num = threadGroup.activeCount();
                    Thread[] printThreads = new Thread[num];
                    threadGroup.enumerate(printThreads);

                    LinkedList<String> ignoreNames = new LinkedList<>();
                    ignoreNames.add("Cyder Busy Checker");
                    ignoreNames.add("AWT-EventQueue-0");
                    ignoreNames.add("Console Clock Updater");
                    ignoreNames.add("Hourly Chime Checker");
                    ignoreNames.add("Stable Network Connection Checker");
                    ignoreNames.add("Final Frame Disposed Checker");
                    ignoreNames.add("DestroyJavaVM");
                    ignoreNames.add("JavaFX Application Thread");
                    ignoreNames.add("Console Input Caret Position Updater");
                    ignoreNames.add("Console Printing Animation");

                    int busyThreads = 0;

                    for (int i = 0; i < num; i++) {
                        if (!printThreads[i].isDaemon() && !ignoreNames.contains(printThreads[i].getName())) {
                            busyThreads++;
                        }
                    }

                    if (busyThreads == 0) {
                        consoleCyderFrame.setIconImage(SystemUtil.getCyderIcon().getImage());
                    } else {
                        consoleCyderFrame.setIconImage(SystemUtil.getCyderIconBlink().getImage());
                    }

                    //sleep 3 seconds
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            } finally {
                consoleCyderFrame.setIconImage(SystemUtil.getCyderIcon().getImage());
            }
        }, "Cyder Busy Checker").start();
    }

    private void endExecutors() {
        doThreads = false;
    }

    //one time run things such as notifying due to special days, debug properties,
    // last start time, and auto testing
    private void onLaunch() {
        //special day events
        if (TimeUtil.isChristmas())
            consoleCyderFrame.notify("Merry Christmas!");

        if (TimeUtil.isHalloween())
            consoleCyderFrame.notify("Happy Halloween!");

        if (TimeUtil.isIndependenceDay())
            consoleCyderFrame.notify("Happy 4th of July!");

        if (TimeUtil.isThanksgiving())
            consoleCyderFrame.notify("Happy Thanksgiving!");

        if (TimeUtil.isAprilFoolsDay())
            consoleCyderFrame.notify("Happy April Fool Day!");

        //preference handlers here
        if (IOUtil.getUserData("DebugWindows").equals("1")) {
            StatUtil.systemProperties();
            StatUtil.computerProperties();
            StatUtil.javaProperties();
            StatUtil.debugMenu();
        }

        //Auto test in upon start debug mode
        if (SecurityUtil.nathanLenovo()) {
            try {
                inputHandler.handle("test");
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }

        //last start time operations
        if (TimeUtil.milisToDays(System.currentTimeMillis() -
                Long.parseLong(IOUtil.getUserData("laststart"))) > 1) {
            consoleCyderFrame.notify("Welcome back, " + ConsoleFrame.getConsoleFrame().getUsername() + "!");
        }

        IOUtil.setUserData("laststart",System.currentTimeMillis() + "");

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
                            IOUtil.playAudio("sys/audio/BadApple.mp3", inputHandler);
                        } else if (rand == 1){
                            IOUtil.playAudio("sys/audio/BeetleJuice.mp3", inputHandler);
                        } else {
                            IOUtil.playAudio("sys/audio/BlackOrWhite.mp3", inputHandler);
                        }
                    }
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            },"Black or White Checker").start();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void generateConsoleMenu() {
        Font menuFont = CyderFonts.defaultFontSmall;
        int menuHeight = 250;
        int fontHeight = CyderFrame.getMinHeight("TURNED MYSELF INTO A PICKLE MORTY!", menuFont);

        menuButton.setIcon(new ImageIcon("sys/pictures/icons/menu2.png"));

        menuLabel = new JLabel("");
        menuLabel.setBounds(-150, DragLabel.getDefaultHeight(),
                CyderFrame.getMinWidth("TEMP CONV", menuFont) + 10, menuHeight);
        menuLabel.setOpaque(true);
        menuLabel.setBackground(CyderColors.navy);
        menuLabel.setVisible(true);
        consoleCyderFrame.getIconPane().add(menuLabel, JLayeredPane.POPUP_LAYER);

        Dimension menuSize = new Dimension(menuLabel.getWidth(), menuLabel.getHeight());

        JTextPane menuPane = new JTextPane();
        menuPane.setEditable(false);
        menuPane.setAutoscrolls(false);
        menuPane.setBounds(7, 10, (int) (menuSize.getWidth() - 10), menuHeight);
        menuPane.setFocusable(true);
        menuPane.setOpaque(false);
        menuPane.setBackground(CyderColors.navy);

        //adding components
        StringUtil printingUtil = new StringUtil(menuPane);

        JLabel calculatorLabel = new JLabel("Calculator");
        calculatorLabel.setFont(menuFont);
        calculatorLabel.setForeground(CyderColors.vanila);
        calculatorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Calculator c = new Calculator();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                calculatorLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                calculatorLabel.setForeground(CyderColors.vanila);
            }
        });
        printingUtil.printlnComponent(calculatorLabel);

        JLabel musicLabel = new JLabel("Music");
        musicLabel.setFont(menuFont);
        musicLabel.setForeground(CyderColors.vanila);
        printingUtil.printlnComponent(musicLabel);
        musicLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                IOUtil.mp3("");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                musicLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                musicLabel.setForeground(CyderColors.vanila);
            }
        });

        JLabel weatherLabel = new JLabel("Weather");
        weatherLabel.setFont(menuFont);
        weatherLabel.setForeground(CyderColors.vanila);
        printingUtil.printlnComponent(weatherLabel);
        weatherLabel.setOpaque(false);
        weatherLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Weather ww = new Weather();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                weatherLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                weatherLabel.setForeground(CyderColors.vanila);
            }
        });

        JLabel noteLabel = new JLabel("Notes");
        noteLabel.setFont(menuFont);
        noteLabel.setForeground(CyderColors.vanila);
        printingUtil.printlnComponent(noteLabel);
        noteLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                IOUtil.startNoteEditor();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                noteLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                noteLabel.setForeground(CyderColors.vanila);
            }
        });

        JLabel editUserLabel = new JLabel("Edit user");
        editUserLabel.setFont(menuFont);
        editUserLabel.setForeground(CyderColors.vanila);
        printingUtil.printlnComponent(editUserLabel);
        editUserLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    inputHandler.handle("prefs");
                } catch (Exception exception) {
                    ErrorHandler.handle(exception);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                editUserLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                editUserLabel.setForeground(CyderColors.vanila);
            }
        });

        JLabel temperatureLabel = new JLabel("Temp conv");
        temperatureLabel.setFont(menuFont);
        temperatureLabel.setForeground(CyderColors.vanila);
        printingUtil.printlnComponent(temperatureLabel);
        temperatureLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TempConverter tc = new TempConverter();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                temperatureLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                temperatureLabel.setForeground(CyderColors.vanila);
            }
        });

        JLabel youtubeLabel = new JLabel("YouTube");
        youtubeLabel.setFont(menuFont);
        youtubeLabel.setForeground(CyderColors.vanila);
        printingUtil.printlnComponent(youtubeLabel);
        youtubeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkUtil.internetConnect("https://youtube.com");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                youtubeLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                youtubeLabel.setForeground(CyderColors.vanila);
            }
        });

        JLabel twitterLabel = new JLabel("Twitter");
        twitterLabel.setFont(menuFont);
        twitterLabel.setForeground(CyderColors.vanila);
        printingUtil.printlnComponent(twitterLabel);
        twitterLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkUtil.internetConnect("https://twitter.com");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                twitterLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                twitterLabel.setForeground(CyderColors.vanila);
            }
        });

        JLabel sepLabel = new JLabel("SEPARATOR") {
            @Override
            public void paintComponent(Graphics g) {
                //draw 5 high line 150 width across
                g.setColor(getForeground());
                g.fillRect(0, 7, 150, 5);
                g.dispose();
            }
        };
        sepLabel.setForeground(CyderColors.vanila);
        sepLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Thread(() -> {
                    try {
                        for (int i = 0; i < 10; i++) {
                            sepLabel.setForeground(CyderColors.regularRed);
                            Thread.sleep(100);
                            sepLabel.setForeground(CyderColors.vanila);
                            Thread.sleep(100);
                        }
                    } catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                }, "Menu Line Disco Easter Egg").start();
            }
        });
        printingUtil.printlnComponent(sepLabel);

        JLabel logoutLabel = new JLabel("Logout");
        logoutLabel.setFont(menuFont);
        logoutLabel.setForeground(CyderColors.vanila);
        printingUtil.printlnComponent(logoutLabel);
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    inputHandler.handle("logout");
                } catch (Exception exception) {
                    ErrorHandler.handle(exception);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                logoutLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutLabel.setForeground(CyderColors.vanila);
            }
        });

        JLabel exitLabel = new JLabel("Exit");
        exitLabel.setFont(menuFont);
        exitLabel.setForeground(CyderColors.vanila);
        printingUtil.printlnComponent(exitLabel);
        exitLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    inputHandler.handle("quit");
                } catch (Exception exception) {
                    ErrorHandler.handle(exception);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                exitLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                exitLabel.setForeground(CyderColors.vanila);
            }
        });

        /*
        add sep here if mapped labels
        add mapped ones here: if title len greater than 9 items and cut off label text at > 9 chars
        you'll need to redo how user data is stored and retreived since you'll need more than key/value pairs
        probably should use JSON format. For example:

        Mapped menu links: {
            [
                name: Banner
                link: http://
            ],
            [
                name: Super Long title that will be trimmed
                link: something.io
            ]
        }
        Username:nathan
        Password;jdkljf230948kwejkflqj0
         */

        CyderScrollPane menuScroll = new CyderScrollPane(menuPane);
        menuScroll.setThumbSize(5);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setFocusable(true);
        menuScroll.setOpaque(false);
        menuScroll.setThumbColor(CyderColors.intellijPink);
        menuScroll.setBackground(CyderColors.navy);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        menuScroll.setBounds(7, 10, (int) (menuSize.getWidth() - 10), menuHeight);
        menuLabel.add(menuScroll);

        //set menu location to top
        menuPane.setCaretPosition(0);

        menuGenerated = true;
    }

    private MouseAdapter consoleMenu = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!menuLabel.isVisible()) {
                if (!menuGenerated)
                    generateConsoleMenu();

                menuLabel.setLocation(-150,DragLabel.getDefaultHeight() - 5);
                menuLabel.setVisible(true);

                if (IOUtil.getUserData("menudirection").equals("1")) {
                    AnimationUtil.componentRight(-150, 0, 10, 8, menuLabel);
                } else {
                    menuLabel.setLocation(0, -250);
                    AnimationUtil.componentDown(-250, DragLabel.getDefaultHeight() - 5, 10, 8, menuLabel);
                }
            } else {
                minimizeMenu();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("sys/pictures/icons/menu2.png"));
            } else {
                menuButton.setIcon(new ImageIcon("sys/pictures/icons/menuSide2.png"));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("sys/pictures/icons/menu1.png"));
            } else {
                menuButton.setIcon(new ImageIcon("sys/pictures/icons/menuSide1.png"));
            }
        }
    };

    private void minimizeMenu() {
        if (menuLabel.isVisible()) {
            if (IOUtil.getUserData("menudirection").equals("1")) {
                menuLabel.setLocation(0, menuLabel.getY());

                new Thread(() -> {
                    int y = menuLabel.getY();

                    for (int i = 0 ; i > -150 ; i-= 8) {
                        menuLabel.setLocation(i, y);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }

                    menuLabel.setLocation(-150, y);

                    menuLabel.setVisible(false);
                    menuButton.setIcon(new ImageIcon("sys/pictures/icons/menuSide1.png"));
                },"minimize menu thread").start();
            } else {
                menuLabel.setLocation(0, DragLabel.getDefaultHeight() - 5);

                new Thread(() -> {
                    int x = menuLabel.getX();

                    for (int i = 30 ; i > -250 ; i-= 8) {
                        menuLabel.setLocation(x, i);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }

                    menuLabel.setLocation(x, -250);

                    menuLabel.setVisible(false);
                    menuButton.setIcon(new ImageIcon("sys/pictures/icons/menuSide1.png"));
                },"minimize menu thread").start();
            }
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
                    if (code == KeyEvent.VK_DOWN) {
                        if (scrollingIndex - 1 >= 0) {
                            scrollingIndex -= 1;
                            inputField.setText(consoleBashString +  operationList.get(scrollingIndex));
                        }
                    }
                    //scroll to subsequent command if exist
                    else if (code == KeyEvent.VK_UP) {
                        if (scrollingIndex + 1 < operationList.size()) {
                            scrollingIndex += 1;
                            inputField.setText(consoleBashString + operationList.get(scrollingIndex));
                        } else if (scrollingIndex + 1 == operationList.size()) {
                            scrollingIndex += 1;
                            inputField.setText(consoleBashString);
                        }
                    }

                    //f17 easter egg and other acknowlegement of other function keys
                    for (int i = 61440; i < 61452; i++) {
                        if (code == i) {
                            if (i - 61427 == 17) {
                                IOUtil.playAudio("sys/audio/f17.mp3", inputHandler);
                            } else {
                                inputHandler.println("Interesting F" + (i - 61427) + " key");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    };

    /**
     * Set the UUID for this Cyder session. Everything else relies on this being set and not null.
     * Once set, a one time check is performed to fix any possibly corrupted userdata.
     * @param uuid - the user uuid that we will use to determine our output dir and other
     *             information specific to this instance of the console frame
     */
    public void setUUID(String uuid) {
        UUID = uuid;
        IOUtil.fixUserData();
    }

    public String getUUID() {
        return UUID;
    }

    public String getUsername() {
        String name = IOUtil.getUserData("Name");
        if (name == null || name.trim().length() < 1)
            return "Name Not Found";
        else
            return name;
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
     * @param size - the size of the font
     */
    public void setFontSize(int size) {
        fontSize = size;
    }

    /**
     * Get the desired user font in combination with the set font metric and font size.
     * @return - the font to use for the input and output areas
     */
    public Font getUserFont() {
        return new Font(IOUtil.getUserData("Font"), fontMetric, fontSize);
    }

    /**
     * Get the user's foreground color from Userdata
     * @return - a Color object representing the chosen foreground
     */
    public Color getUserForegroundColor() {
        return ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground"));
    }

    /**
     * Get the user's background color from Userdata
     * @return - a Color object representing the chosen background
     */
    public Color getUserBackgroundColor() {
        return ColorUtil.hextorgbColor(IOUtil.getUserData("Background"));
    }

    /**
     * Takes into account the dpi scaling value and checks all the backgrounds in the user's
     * directory against the current monitors resolution. If any width or height of a background file
     * exceeds the monitor's width or height. We resize until it doesn't. We also check to make sure the background
     * meets our minimum pixel dimension parameters.
     */
    public void resizeBackgrounds() {
        try {
            LinkedList<File> backgrounds = getBackgrounds();

            for (int i = 0; i < backgrounds.size(); i++) {
                File currentFile = backgrounds.get(i);
                BufferedImage currentImage = ImageIO.read(currentFile);
                int backgroundWidth = currentImage.getWidth();
                int backgroundHeight = currentImage.getHeight();
                int screenWidth = SystemUtil.getScreenWidth();
                int screenHeight = SystemUtil.getScreenHeight();
                double aspectRatio = ImageUtil.getAspectRatio(currentImage);
                int imageType = currentImage.getType();

                //inform the user we are changing the size of the image.
                if (backgroundWidth > SystemUtil.getScreenWidth() || backgroundHeight > SystemUtil.getScreenHeight())
                    GenericInform.inform("Resizing the background image \"" + currentFile.getName() +
                                    "\" since it's too big.", "System Action");

                //while the image dimensions are greater than the screen dimensions,
                // divide the image dimensions by the the aspect ratio if it will result in a smaller number
                // if it won't then we divide by 1/aspectRatio which will result in a smaller number if the first did not
                while (backgroundWidth > screenWidth * 0.70 || backgroundHeight > screenHeight * 0.70) {
                    backgroundWidth = (int) (backgroundWidth / ((aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio)));
                    backgroundHeight = (int) (backgroundHeight / ((aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio)));
                }

                //inform the user we are changing the size of the image
                if (backgroundWidth < 600 || backgroundHeight < 600)
                    GenericInform.inform("Resizing the background image \"" + getBackgrounds().get(i).getName()
                            + "\" since it's too small.", "System Action");

                //while the image dimensions are less than 800x800, multiply the image dimensions by the
                // aspect ratio if it will result in a bigger number, if it won't, multiply it by 1.0 / aspectRatio
                // which will result in a number greater than 1.0 if the first option failed.
                while (backgroundWidth < 600 || backgroundHeight < 600) {
                    backgroundWidth = (int) (backgroundWidth * (aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio));
                    backgroundHeight = (int) (backgroundHeight * (aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio));
                }

                //save the modified image
                BufferedImage saveImage = ImageUtil.resizeImage(currentImage, imageType, backgroundWidth, backgroundHeight);
                ImageIO.write(saveImage, "png", currentFile);
            }

            //reinit backgrounds after resizing all backgrounds that needed fixing
            initBackgrounds();
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public void initBackgrounds() {
        try {
            File dir = new File("users/" + getUUID() + "/Backgrounds");
            FilenameFilter PNGFilter = (dir1, filename) -> filename.endsWith(".png");

            backgroundFiles = new LinkedList<>(Arrays.asList(dir.listFiles(PNGFilter)));

            //if no backgrounds, copy the default image icon over and recall initBackgrounds()
            if (backgroundFiles.size() == 0) {
                Image img = CyderImages.defaultBackground.getImage();

                BufferedImage bi = new BufferedImage(img.getWidth(null),
                        img.getHeight(null),BufferedImage.TYPE_INT_RGB);

                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                File backgroundFile = new File("users/" + UUID + "/Backgrounds/Default.png");
                backgroundFile.mkdirs();
                ImageIO.write(bi, "png", backgroundFile);

                initBackgrounds();
            }
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public LinkedList<File> getBackgrounds() {
        initBackgrounds();
        return backgroundFiles;
    }

    public int getBackgroundIndex() {
        return backgroundIndex;
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
            ErrorHandler.handle(e);
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
            ErrorHandler.handle(e);
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
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }

    //if this returns false then we didn't switch so we should tell the user they should add more backgrounds
    public boolean switchBackground() {
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
                width = SystemUtil.getScreenWidth();
                height = SystemUtil.getScreenHeight();

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
                                ErrorHandler.handle(e);
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
                                ErrorHandler.handle(e);
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
                                ErrorHandler.handle(e);
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
                                ErrorHandler.handle(e);
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
                    },"ConsoleFrame Background Switch Animation").start();

                    break;
            }

            //increment background index
            incBackgroundIndex();

            //change tooltip to new image name
            ((JLabel) (consoleCyderFrame.getContentPane()))
                    .setToolTipText(StringUtil.getFilename(getCurrentBackgroundFile().getName()));

            //update line color
            lineColor = ImageUtil.getDominantColorOpposite(ImageIO.read(getCurrentBackgroundFile()));

            outputScroll.setBounds(10, 62, width - 20, height - 204);
            inputField.setBounds(10, 62 + outputScroll.getHeight() + 20,width - 20,
                    height - (62 + outputScroll.getHeight() + 20 + 20));

            //request focus
            inputField.requestFocus();

        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return true;
        }
    }

    /**
     * @return returns the current background with using the current background ImageIcon and whether or not full screen is active
     */
    public int getBackgroundWidth() {
        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return (int) SystemUtil.getScreenSize().getWidth();
        else
            return getCurrentBackgroundImageIcon().getIconWidth();
    }

    /**
     * @return returns the current background height using the current background ImageIcon and whether or not full screen is active
     */
    public int getBackgroundHeight() {
        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
            return (int) SystemUtil.getScreenSize().getHeight();
        else
            return getCurrentBackgroundImageIcon().getIconHeight();
    }

    //this is an example of how the new method should look within here, remove all get user data from IOUtil
    public String getUserData(String dataName) {
        return (dataName instanceof String ? dataName : dataName.equals("1") ? "true" : "false");
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
     * @param deg - the degree by which to smoothly rotate
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

    public void setFullscreen(Boolean enable) {
        try {
            fullscreen = enable;

            int w = 0;
            int h = 0;
            ImageIcon rotatedIcon = null;

            if (fullscreen) {
                w = SystemUtil.getScreenWidth();
                h = SystemUtil.getScreenHeight();
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

            int relativeX = consoleCyderFrame.getX() + consoleCyderFrame.getWidth() / 2;
            int relativeY = consoleCyderFrame.getY() + consoleCyderFrame.getHeight() / 2;

            consoleCyderFrame.setSize(w, h);
            consoleCyderFrame.setBackground(rotatedIcon);

            consoleCyderFrame.setLocation(relativeX - w / 2, relativeY - h / 2);

            outputScroll.setBounds(10, 62, w - 20, h - 204);
            inputField.setBounds(10, 62 + outputScroll.getHeight() + 20,w - 20,
                    h - (62 + outputScroll.getHeight() + 20 + 20));

            if (fullscreen)
                consoleCyderFrame.setLocationRelativeTo(null);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void refreshBasedOnPrefs() {
        //output border
        if (IOUtil.getUserData("OutputBorder").equals("0")) {
            outputScroll.setBorder(BorderFactory.createEmptyBorder());
        } else {
            outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")), 3, true));
        }

        //input border
        if (IOUtil.getUserData("InputBorder").equals("0")) {
            inputField.setBorder(null);
        } else {
            inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")), 3, true));
        }

        //full screen
        if (IOUtil.getUserData("FullScreen").equals("0") && isFullscreen()) {
            setFullscreen(false);
        } else if (IOUtil.getUserData("FullScreen").equals("1") && !isFullscreen()) {
            setFullscreen(true);
        }

        //console clock
        consoleClockLabel.setVisible(IOUtil.getUserData("ClockOnConsole").equals("1"));

        //output color fill
        if (IOUtil.getUserData("OutputFill").equals("0")) {
            outputArea.setBackground(null);
            outputArea.setOpaque(false);
        } else {
            outputArea.setOpaque(true);
            outputArea.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
            outputArea.repaint();
            outputArea.revalidate();
        }

        //input color fill
        if (IOUtil.getUserData("InputFill").equals("0")) {
            inputField.setBackground(null);
            inputField.setOpaque(false);
        } else {
            inputField.setOpaque(true);
            inputField.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
            inputField.repaint();
            inputField.revalidate();
        }

        //round corners fixer
        for (Frame f : Frame.getFrames()) {
            f.repaint();
        }

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

    public void close() {
        inputHandler.close();
        inputHandler = null;
        consoleCyderFrame.closeAnimation();
        closed = true;
        SessionLogger.log(SessionLogger.Tag.LOGOUT, " [" + getUsername() + "]");
    }

    public boolean isClosed() {
        return closed;
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

    public void clearOperationList() {
        operationList.clear();
        scrollingIndex = 0;
    }

    public JTextPane getOutputArea() {
        return outputArea;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public ArrayList<String> getOperationList() {
        return operationList;
    }

    public void minimize() {
        consoleCyderFrame.minimizeAnimation();
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

    public void setFrameRelativeTo(Frame f) {
        f.setLocationRelativeTo(consoleCyderFrame);
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
                ImageIcon blinkIcon = new ImageIcon("sys/pictures/icons/suggestion2.png");
                ImageIcon regularIcon = new ImageIcon("sys/pictures/icons/suggestion1.png");

                for (int i = 0 ; i < 4 ; i++) {
                    suggestionButton.setIcon(blinkIcon);
                    Thread.sleep(300);
                    suggestionButton.setIcon(regularIcon);
                    Thread.sleep(300);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Suggestion Button Flash").start();
    }

    public void repaint() {
        setFullscreen(fullscreen);
    }
}
