package cyder.genesis;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.enums.Direction;

import cyder.exception.CyderException;
import cyder.exception.FatalException;
import cyder.games.Hangman;
import cyder.games.TicTacToe;
import cyder.handler.ErrorHandler;
import cyder.handler.PhotoViewer;
import cyder.threads.YoutubeThread;
import cyder.ui.*;
import cyder.utilities.*;
import cyder.widgets.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static cyder.consts.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class CyderMain {
    //todo shared package
    public static Semaphore exitingSem;

    //specific to an instance of a handler method
    private LinkedList<YoutubeThread> youtubeThreads = new LinkedList<>();

    //todo login spins off of main of autocypher fails
    private CyderFrame loginFrame;
    private JPasswordField loginField;
    private boolean doLoginAnimations;
    private int loginMode;
    private String username;
    private final String bashString = SystemUtil.getWindowsUsername() + "@Cyder:~$ ";

    //todo handler which each consoleframe uses
    private StringUtil stringUtil;
    private String operation;
    private String anagram;

    //todo go away
    private int restoreX;
    private int restoreY;
    private int xMouse;
    private int yMouse;
    private boolean slidLeft;
    private JLabel consoleDragLabel;

    /**
     * create user widget
     */
    private CyderFrame createUserFrame;
    private JPasswordField newUserPasswordconf;
    private JPasswordField newUserPassword;
    private JTextField newUserName;
    private CyderButton createNewUser;
    private CyderButton chooseBackground;
    private File createUserBackground;

    /**
     * pixelate widget
     */
    private File pixelateFile;

    /**
     * start the best program ever made
     * @param CA - the arguments passed in
     */
    public static void main(String[] CA)  {
        new CyderMain(CA);
    }

    /**
     * Shouldn't be entered but once
     * @param CA - Arguments that we are going to log
     */
    private CyderMain(String[] CA) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "exit-hook"));

        initObjects();
        initSystemProperties();
        initUIManager();

        IOUtil.cleanUpUsers();
        IOUtil.deleteTempDir();
        IOUtil.logArgs(CA);

        backgroundProcessChecker();

        if (SecurityUtil.nathanLenovo())
            autoCypher();

        else if (IOUtil.getSystemData("Released").equals("1"))
            login();
        else {
            try {
                CyderMain.exitingSem.acquire();
                CyderMain.exitingSem.release();
                System.exit(-600);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    }

    //TODO track and return lists, ALL console frames should be spun off from main
    //TODO all login windows should be spawned in from main
    //todo if not released, there should only ever be one instance
    // current structure will only allow for one frame to be active at any time
    public static ConsoleFrame[] getConsoleFrameInstances() {
        return null;
    }

    /**
     * init objects needed for main's use, most will go away and sem should become const in shared package
     */
    private void initObjects() {
        stringUtil = new StringUtil(outputArea);
        exitingSem = new Semaphore(1);
    }

    /**
     * Initializes System.getProperty key/value pairs
     */
    private void initSystemProperties() {
        //Fix scaling issue for high DPI displays like nathanLenovo which is 2560x1440
        //todo be able to change this if in debug loginMode in sys.ini?
        System.setProperty("sun.java2d.uiScale", "1.0");
    }

    /**
     * Initializes UIManager.put key/value pairs
     */
    private void initUIManager() {
        UIManager.put("ToolTip.background", CyderColors.tooltipBackgroundColor);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createLineBorder(CyderColors.tooltipBorderColor, 2, true)));
        UIManager.put("ToolTip.font", CyderFonts.tahoma.deriveFont(22f));
        UIManager.put("ToolTip.foreground", CyderColors.tooltipForegroundColor);
    }

    /**
     * Used for debugging, automatically logs me in if my account exists,
     * otherwise the program continues as normal
     */
    private void autoCypher() {
        try {
            File autoCypher = new File("../autocypher.txt");
            File Users = new File("users/");

            if (autoCypher.exists() && Users.listFiles().length != 0) {
                BufferedReader ac = new BufferedReader(new FileReader(autoCypher));

                String line = ac.readLine();
                String[] parts = line.split(":");

                if (parts.length == 2 && !parts[0].equals("") && !parts[1].equals("")) {
                    ac.close();
                    recognize(parts[0], parts[1].toCharArray());
                }
            } else
                login();
        } catch (Exception e) {
            ErrorHandler.handle(e);
            login();
        }
    }

    private static JTextPane outputArea;

    //TODO make a consoleframe text area so that it can be like DOS, no need for sep input and output
    // also make an output area link to an InputHandler which links to a ContextEngine as well
    private JTextField inputField;
    public static JFrame consoleFrame;
    private JButton minimize;
    private JButton close;
    private JLabel consoleClockLabel;
    private boolean updateConsoleClock;
    private JLabel parentLabel;
    private static ArrayList<String> operationList = new ArrayList<>();
    private static int scrollingIndex;
    private JList fontList;
    private SpecialDay specialDayNotifier;
    private JLabel menuLabel;
    private JLayeredPane parentPane;
    private JButton suggestionButton;
    private JButton menuButton;
    private CyderScrollPane outputScroll;
    private JButton alternateBackground;

    /**
     * move to consoleFrame, instead of calling console, we will just call userFrame = new ConsoleFrame();
     * that's all! possibly add some other methods to change things about the console frame like close operations. etc.
     */
    //anything that has ConsoleFrame.* can be simplifiied to * after we move this
    //todo this will become consoleFrame.show(), the main setup

    //this extends CyderFrame so we need to override the settitle method since we'll be painting the time
    // as the center title

    //disable setting title position to left

    //add the menu and suggestion button to the drag label

    //override the action of the close button
    public void console() {
        try {
            ConsoleFrame.resizeBackgrounds();
            ConsoleFrame.initBackgrounds();

            consoleFrame = new JFrame();
            consoleFrame.setUndecorated(true);
            //this doesn't really do much since we don't call consoleFrame.dispose typicallyf
            consoleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            consoleFrame.setBounds(0, 0, ConsoleFrame.getBackgroundWidth(), ConsoleFrame.getBackgroundHeight());
            consoleFrame.setTitle(IOUtil.getSystemData("Version") + " Cyder [" + ConsoleFrame.getUsername() + "]");

            parentPane = new JLayeredPane();
            parentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

            consoleFrame.setContentPane(parentPane);

            parentPane.setLayout(null);

            parentLabel = new JLabel();
            parentLabel.setOpaque(false);

            if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1"))
                parentLabel.setIcon(new ImageIcon(ImageUtil.resizeImage((int) SystemUtil.getScreenSize().getWidth(),
                        (int) SystemUtil.getScreenSize().getHeight(), ConsoleFrame.getCurrentBackgroundFile())));
            else
                parentLabel.setIcon(new ImageIcon(ImageUtil.getRotatedImage(ConsoleFrame.getCurrentBackgroundFile().toString(),
                        ConsoleFrame.getConsoleDirection())));

            parentLabel.setBounds(0, 0, ConsoleFrame.getBackgroundWidth(), ConsoleFrame.getBackgroundHeight());

            parentLabel.setBorder(new LineBorder(CyderColors.navy, 8, false));
            parentLabel.setToolTipText(ConsoleFrame.getCurrentBackgroundFile().getName().replace(".png", ""));

            parentPane.add(parentLabel, 1, 0);

            consoleFrame.setIconImage(SystemUtil.getCyderIcon().getImage());

            outputArea = new JTextPane();
            outputArea.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                    inputField.requestFocus();
                }
            });

            outputArea.setEditable(false);
            outputArea.setAutoscrolls(true);
            outputArea.setBounds(10, 62, ConsoleFrame.getBackgroundWidth() - 20, ConsoleFrame.getBackgroundHeight() - 204);
            outputArea.setFocusable(true);
            outputArea.setSelectionColor(new Color(204, 153, 0));
            outputArea.setOpaque(false);
            outputArea.setBackground(new Color(0, 0, 0, 0));

            outputScroll = new CyderScrollPane(outputArea,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            outputScroll.setThumbColor(CyderColors.intellijPink);
            outputScroll.getViewport().setOpaque(false);
            outputScroll.setOpaque(false);
            outputScroll.setFocusable(true);

            if (IOUtil.getUserData("OutputBorder").equalsIgnoreCase("1"))
                outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")), 3, true));

            else
                outputScroll.setBorder(BorderFactory.createEmptyBorder());

            outputScroll.setBounds(10, 62, ConsoleFrame.getBackgroundWidth() - 20, ConsoleFrame.getBackgroundHeight() - 204);

            parentLabel.add(outputScroll);

            inputField = new JTextField(40);

            if (IOUtil.getUserData("InputBorder").equalsIgnoreCase("1"))
                inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")), 3, true));

            else
                inputField.setBorder(BorderFactory.createEmptyBorder());

            inputField.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1)
                        inputField.setText(inputField.getText().toUpperCase());

                    if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0))
                        handle("controlc");

                    if ((e.getKeyCode() == KeyEvent.VK_DOWN) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.setConsoleDirection(Direction.BOTTOM);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_RIGHT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.setConsoleDirection(Direction.RIGHT);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_UP) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.setConsoleDirection(Direction.TOP);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_LEFT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.setConsoleDirection(Direction.LEFT);
                        exitFullscreen();
                    }

                    if ((KeyEvent.SHIFT_DOWN_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        //these booleans were already moved
//                        if (!consoleLinesDrawn) {
//                            drawConsoleLines = true;
//                            consoleFrame.repaint();
//                        }
                    }
                }

                @Override
                public void keyReleased(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1)
                        inputField.setText(inputField.getText().toUpperCase());

                    if ((KeyEvent.SHIFT_DOWN_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        //these booleans were already moved

//                        drawConsoleLines = false;
//                        consoleLinesDrawn = false;
//                        consoleFrame.repaint();
                    }
                }

                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1)
                        inputField.setText(inputField.getText().toUpperCase());
                }
            });

            inputField.setToolTipText("Input Field");
            inputField.setSelectionColor(CyderColors.selectionColor);
            inputField.addKeyListener(commandScrolling);

            consoleFrame.addWindowListener(consoleEcho);

            inputField.setBounds(10, 82 + outputArea.getHeight(),
                    ConsoleFrame.getBackgroundWidth() - 20, ConsoleFrame.getBackgroundHeight() -
                            (outputArea.getHeight() + 62 + 40));
            inputField.setOpaque(false);

            parentLabel.add(inputField);

            inputField.addActionListener(inputFieldAction);
            inputField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                }
            });

            inputField.setCaretColor(ConsoleFrame.getUserForegroundColor());
            inputField.setCaret(new CyderCaret(ConsoleFrame.getUserForegroundColor()));

            IOUtil.readUserData();

            inputField.setForeground(ConsoleFrame.getUserForegroundColor());
            outputArea.setForeground(ConsoleFrame.getUserForegroundColor());

            inputField.setFont(ConsoleFrame.getUserFont());
            outputArea.setFont(ConsoleFrame.getUserFont());

            Color fillColor = ColorUtil.hextorgbColor(IOUtil.getUserData("Background"));

            if (IOUtil.getUserData("OutputFill").equals("1")) {
                outputArea.setOpaque(true);
                outputArea.setBackground(fillColor);
                outputArea.repaint();
                outputArea.revalidate();
                consoleFrame.revalidate();
            }

            if (IOUtil.getUserData("InputFill").equals("1"))
                inputField.setBackground(fillColor);

            suggestionButton = new JButton("");
            suggestionButton.setToolTipText("Suggestions");
            suggestionButton.addActionListener(e -> {
                println("What feature would you like to suggest? (Please include as much detail as possible such as how " +
                        "the feature should be triggered and how the program should responded; be detailed)");
                stringUtil.setUserInputDesc("suggestion");
                stringUtil.setUserInputMode(true);
                inputField.requestFocus();
            });

            suggestionButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("sys/pictures/suggestion2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("sys/pictures/suggestion1.png"));
                }
            });

            suggestionButton.setBounds(32, 4, 22, 22);

            ImageIcon DebugIcon = new ImageIcon("sys/pictures/suggestion1.png");

            suggestionButton.setIcon(DebugIcon);

            parentLabel.add(suggestionButton);

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

            ImageIcon MenuIcon = new ImageIcon("sys/pictures/menuSide1.png");

            menuButton.setIcon(MenuIcon);

            parentLabel.add(menuButton);

            menuButton.setVisible(true);
            menuButton.setFocusPainted(false);
            menuButton.setOpaque(false);
            menuButton.setContentAreaFilled(false);
            menuButton.setBorderPainted(false);

            minimize = new JButton("");
            minimize.setToolTipText("Minimize");
            minimize.addActionListener(e -> {
                restoreX = consoleFrame.getX();
                restoreY = consoleFrame.getY();
                AnimationUtil.minimizeAnimation(consoleFrame);
                updateConsoleClock = false;
                consoleFrame.setState(Frame.ICONIFIED);
                minimizeMenu();
            });

            minimize.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    minimize.setIcon(CyderImages.minimizeIconHover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    minimize.setIcon(CyderImages.minimizeIcon);
                }
            });

            minimize.setBounds(ConsoleFrame.getBackgroundWidth() - 81, 4, 22, 20);

            ImageIcon minimizeIcon = CyderImages.minimizeIcon;
            minimize.setIcon(minimizeIcon);
            parentLabel.add(minimize);
            minimize.setFocusPainted(false);
            minimize.setOpaque(false);
            minimize.setContentAreaFilled(false);
            minimize.setBorderPainted(false);

            alternateBackground = new JButton("");
            alternateBackground.setToolTipText("Alternate Background");
            alternateBackground.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("sys/pictures/ChangeSize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("sys/pictures/ChangeSize1.png"));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    ConsoleFrame.initBackgrounds();

                    try {
                        //todo uncomment
                        //lineColor = new ImageUtil().getDominantColorOpposite(ImageIO.read(ConsoleFrame.getCurrentBackgroundFile()));

                        if (ConsoleFrame.canSwitchBackground() && ConsoleFrame.getBackgrounds().size() > 1) {
                            ConsoleFrame.incBackgroundIndex();
                            switchBackground();
                        } else if (ConsoleFrame.onLastBackground() && ConsoleFrame.getBackgrounds().size() > 1) {
                            ConsoleFrame.setBackgroundIndex(0);
                            switchBackground();
                        } else if (ConsoleFrame.getBackgrounds().size() == 1) {
                            println("You only have one background image. Would you like to add more? (Enter yes/no)");
                            inputField.requestFocus();
                            stringUtil.setUserInputMode(true);
                            stringUtil.setUserInputDesc("addbackgrounds");
                            inputField.requestFocus();
                        }
                    } catch (Exception ex) {
                        ErrorHandler.handle(new FatalException("Background DNE"));
                        println("Error in parsing background; perhaps it was deleted.");
                    }
                }
            });

            alternateBackground.setBounds(ConsoleFrame.getBackgroundWidth() - 54, 4, 22, 20);
            alternateBackground.setIcon(new ImageIcon("sys/pictures/ChangeSize1.png"));
            parentLabel.add(alternateBackground);
            alternateBackground.setFocusPainted(false);
            alternateBackground.setOpaque(false);
            alternateBackground.setContentAreaFilled(false);
            alternateBackground.setBorderPainted(false);

            close = new JButton("");
            close.setToolTipText("Close");
            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    close.setIcon(CyderImages.closeIconHover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(CyderImages.closeIcon);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    exit();
                }
            });

            close.setBounds(ConsoleFrame.getBackgroundWidth() - 27, 4, 22, 20);
            close.setIcon(CyderImages.closeIcon);
            parentLabel.add(close);
            close.setFocusPainted(false);
            close.setOpaque(false);
            close.setContentAreaFilled(false);
            close.setBorderPainted(false);

            consoleDragLabel = new JLabel();
            consoleDragLabel.setBounds(0, 0, ConsoleFrame.getBackgroundWidth(), 30);
            consoleDragLabel.setOpaque(true);
            consoleDragLabel.setBackground(CyderColors.navy);
            consoleDragLabel.addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getXOnScreen();
                    int y = e.getYOnScreen();

                    if (consoleFrame != null && consoleFrame.isFocused())
                        consoleFrame.setLocation(x - xMouse, y - yMouse);
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    xMouse = e.getX();
                    yMouse = e.getY();
                }
            });

            consoleClockLabel = new JLabel(TimeUtil.consoleTime(), SwingConstants.CENTER);
            consoleClockLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(20f));
            consoleClockLabel.setForeground(CyderColors.vanila);
            consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 -
                            CyderFrame.getMinWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) / 2 - 13,
                    2, CyderFrame.getMinWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) + 26, 25);

            consoleDragLabel.add(consoleClockLabel, SwingConstants.CENTER);

            updateConsoleClock = IOUtil.getUserData("ClockOnConsole").equalsIgnoreCase("1");

            //make a method to spin off executors
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (consoleClockLabel.isVisible())
                    if (IOUtil.getUserData("ShowSeconds").equalsIgnoreCase("1"))
                        consoleClockLabel.setText(TimeUtil.consoleSecondTime());
                    else
                        consoleClockLabel.setText(TimeUtil.consoleTime());

            }, 0, 500, TimeUnit.MILLISECONDS);

            consoleClockLabel.setVisible(updateConsoleClock);

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (IOUtil.getUserData("HourlyChimes").equalsIgnoreCase("1"))
                    IOUtil.playAudio("sys/audio/chime.mp3");

            }, 3600 - LocalDateTime.now().getSecond() - LocalDateTime.now().getMinute() * 60, 3600, TimeUnit.SECONDS);

            parentLabel.add(consoleDragLabel);

            consoleFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeiconified(WindowEvent e) {
                    updateConsoleClock = true;
                    consoleFrame.setLocation(restoreX, restoreY);
                }
            });
            //these will go away since cyderframe has them
            consoleFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeiconified(WindowEvent e) {
                    updateConsoleClock = false;
                    restoreX = consoleFrame.getX();
                    restoreY = consoleFrame.getY();
                }
            });

            if (IOUtil.getUserData("RandomBackground").equals("1")) {
                int len = ConsoleFrame.getBackgrounds().size();

                if (len <= 1)
                    println("Sorry, " + ConsoleFrame.getUsername() + ", but you only have one background file so there's no random element to be chosen.");

                else if (len > 1) {
                    try {
                        LinkedList<File> backgrounds = ConsoleFrame.getBackgrounds();

                        ConsoleFrame.setBackgroundIndex(NumberUtil.randInt(0, (backgrounds.size()) - 1));

                        String newBackFile = ConsoleFrame.getCurrentBackgroundFile().toString();

                        ImageIcon newBack;
                        int tempW = 0;
                        int tempH = 0;

                        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                            newBack = new ImageIcon(ImageUtil.resizeImage((int) SystemUtil.getScreenSize().getWidth(),
                                    (int) SystemUtil.getScreenSize().getHeight(), new File(newBackFile)));
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        } else {
                            newBack = new ImageIcon(newBackFile);
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        }

                        parentLabel.setIcon(newBack);

                        consoleFrame.setBounds(0, 0, tempW, tempH);
                        parentPane.setBounds(0, 0, tempW, tempH);
                        parentLabel.setBounds(0, 0, tempW, tempH);

                        outputArea.setBounds(0, 0, tempW - 20, tempH - 204);
                        outputScroll.setBounds(10, 62, tempW - 20, tempH - 204);
                        inputField.setBounds(10, 82 + outputArea.getHeight(), tempW - 20, tempH - (outputArea.getHeight() + 62 + 40));
                        consoleDragLabel.setBounds(0, 0, tempW, 30);
                        minimize.setBounds(tempW - 81, 4, 22, 20);
                        alternateBackground.setBounds(tempW - 54, 4, 22, 20);
                        close.setBounds(tempW - 27, 4, 22, 20);

                        inputField.requestFocus();

                        parentLabel.setIcon(newBack);

                        parentLabel.setToolTipText(ConsoleFrame.getCurrentBackgroundFile().getName().replace(".png", ""));
                        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 -
                                        CyderFrame.getMinWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) / 2 - 13,
                                2, CyderFrame.getMinWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) + 26, 25);
                    } catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                } else
                    throw new FatalException("Only one but also more than one background.");
            }

            //will be removed
            AnimationUtil.enterAnimation(consoleFrame);

            //internet checker every 5 minutes
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                //network unreachable notification
            }, 0, 5, TimeUnit.MINUTES);

            consoleClockLabel.setVisible(updateConsoleClock);

            //lineColor = new ImageUtil().getDominantColorOpposite(ImageIO.read(ConsoleFrame.getCurrentBackgroundFile()));

            if (IOUtil.getUserData("DebugWindows").equals("1")) {
                StatUtil.systemProperties();
                StatUtil.computerProperties();
                StatUtil.javaProperties();
                StatUtil.debugMenu(outputArea);
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private MouseAdapter consoleMenu = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("sys/pictures/menu2.png"));

                menuLabel = new JLabel("");
                menuLabel.setOpaque(true);
                menuLabel.setBackground(new Color(26, 32, 51));

                parentPane.add(menuLabel, 1, 0);

                Font menuFont = CyderFonts.defaultFontSmall;
                int fontHeight = CyderFrame.getMinHeight("TURNED MYSELF INTO A PICKLE MORTY!",menuFont);
                menuLabel.setVisible(true);

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

                menuLabel.add(calculatorLabel);
                calculatorLabel.setBounds(5, 20, 150, fontHeight);

                JLabel musicLabel = new JLabel("Music");
                musicLabel.setFont(menuFont);
                musicLabel.setForeground(CyderColors.vanila);
                musicLabel.setBounds(5, 50, 150, fontHeight);
                menuLabel.add(musicLabel);
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
                menuLabel.add(weatherLabel);
                weatherLabel.setBounds(5, 80, 150, fontHeight);
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
                menuLabel.add(noteLabel);
                noteLabel.setBounds(5, 110, 150, fontHeight);
                noteLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        new Notes();
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
                menuLabel.add(editUserLabel);
                editUserLabel.setBounds(5, 140, 150, fontHeight);
                editUserLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        editUser();
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
                menuLabel.add(temperatureLabel);
                temperatureLabel.setBounds(5, 170, 150, fontHeight);
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
                menuLabel.add(youtubeLabel);
                youtubeLabel.setBounds(5, 200, 150, fontHeight);
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
                menuLabel.add(twitterLabel);
                twitterLabel.setBounds(5, 230, 150, fontHeight);
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

                JLabel logoutLabel = new JLabel("Logout");
                logoutLabel.setFont(menuFont);
                logoutLabel.setForeground(CyderColors.vanila);
                menuLabel.add(logoutLabel);
                logoutLabel.setBounds(5, 255, 150, fontHeight);
                logoutLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handle("logout");
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

                //add more after this such as user mapped actions
                //todo use a loop to make this easy to add new ones too

                menuLabel.setBounds(-150, 30, CyderFrame.getMinWidth("TEMP CONV",menuFont),
                        fontHeight * (menuLabel.getComponentCount() - 1));

                AnimationUtil.jLabelXRight(-150, 0, 10, 8, menuLabel);
            } else if (menuLabel.isVisible()) {
                minimizeMenu();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("sys/pictures/menu2.png"));
            } else {
                menuButton.setIcon(new ImageIcon("sys/pictures/menuSide2.png"));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("sys/pictures/menu1.png"));
            } else {
                menuButton.setIcon(new ImageIcon("sys/pictures/menuSide1.png"));
            }
        }
    };

    private KeyListener commandScrolling = new KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent event) {
            int code = event.getKeyCode();

            try {
                if ((event.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == 0 && ((event.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == 0)) {
                    if (code == KeyEvent.VK_DOWN) {
                        if (scrollingIndex + 1 < operationList.size()) {
                            scrollingIndex = scrollingIndex + 1;
                            inputField.setText(operationList.get(scrollingIndex));
                        }
                    } else if (code == KeyEvent.VK_UP) {
                        boolean Found = false;

                        for (int i = 0; i < operationList.size(); i++) {
                            if (operationList.get(i).equals(inputField.getText())) {
                                Found = true;
                                break;
                            } else if (!operationList.get(i).equals(inputField.getText()) && i == operationList.size() - 1) {
                                Found = false;
                                break;
                            }
                        }

                        if (inputField.getText() == null || inputField.getText().equals("")) {
                            ConsoleFrame.setScrollingDowns(0);
                        } else if (!Found) {
                            ConsoleFrame.setScrollingDowns(0);
                        }

                        if (scrollingIndex - 1 >= 0) {
                            if (ConsoleFrame.getScrollingDowns() != 0) {
                                scrollingIndex = scrollingIndex - 1;
                            }

                            inputField.setText(operationList.get(scrollingIndex));
                            ConsoleFrame.incScrollingDowns();
                        }

                        if (operationList.size() == 1) {
                            inputField.setText(operationList.get(0));
                        }
                    }

                    for (int i = 61440; i < 61452; i++) {
                        if (code == i) {
                            int seventeen = (i - 61427);

                            if (seventeen == 17)
                                IOUtil.playAudio("sys/audio/f17.mp3");
                            else
                                println("Interesting F" + (i - 61427) + " key");
                        }
                    }
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    };

    //when we first launch this will check for any special days in the special days class
    //will this work for multiple things on the same day?
    //consolidate with console frame in one time run method
    private WindowAdapter consoleEcho = new WindowAdapter() {
        public void windowOpened(WindowEvent e) {
        inputField.requestFocus();
        specialDayNotifier = new SpecialDay(parentPane);
        }
    };

    //sets program icon if background threads are running
    private void backgroundProcessChecker() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (consoleFrame != null) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                int threadCount = 0;

                //todo name all threads and executors so if anything contains "pool" or "thread" then we knkow it's busy
                for (int i = 0; i < num; i++)
                    if (!printThreads[i].isDaemon() &&
                            !printThreads[i].getName().contains("pool") &&
                            !printThreads[i].getName().contains("AWT-EventQueue-0") &&
                            !printThreads[i].getName().contains("DestroyJavaVM"))

                        threadCount++;

                if (threadCount > 0)
                    consoleFrame.setIconImage(SystemUtil.getCyderIconBlink().getImage());

                else
                    consoleFrame.setIconImage(SystemUtil.getCyderIcon().getImage());
            }

        }, 0, 3, TimeUnit.SECONDS);
    }

    //console frame
    private Action inputFieldAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
        try {
            String originalOp = inputField.getText().trim();
            String op = originalOp;

            if (!stringUtil.empytStr(op)) {
                if (!(operationList.size() > 0 && operationList.get(operationList.size() - 1).equals(op))) {
                    operationList.add(op);
                }

                scrollingIndex = operationList.size() - 1;
                ConsoleFrame.setScrollingDowns(0);

                if (!stringUtil.getUserInputMode()) {
                    handle(op);
                } else if (stringUtil.getUserInputMode()) {
                    stringUtil.setUserInputMode(false);
                    handleSecond(op);
                }
            }

            inputField.setText("");
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
        }
    };

    private void typingPrint(String print, JTextPane refArea) {
        try {
            StyledDocument document = (StyledDocument) refArea.getDocument();
            document.insertString(document.getLength(), print, null);
            refArea.setCaretPosition(refArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private LinkedList<String> printingList = new LinkedList<>();
    private LinkedList<String> priorityPrintingList = new LinkedList<>();

    private void loginTypingAnimation(JTextPane refArea) {
        printingList.clear();
        SimpleDateFormat versionFormatter = new SimpleDateFormat("MM.dd.yy");
        printingList.add("Cyder version: " + versionFormatter.format(new Date()) + "\n");
        printingList.add("Type \"h\" for a list of valid commands\n");
        printingList.add("Build: Soultree\n");
        printingList.add("Author: Nathan Cheshire\n");
        printingList.add("Design OS: Windows 10+\n");
        printingList.add("Design JVM: 8+\n");
        printingList.add("Description: A programmer's swiss army knife\n");

        int charTimeout = 40;
        int lineTimeout = 500;

        new Thread(() -> {
            try {
                while (doLoginAnimations && loginFrame != null)  {
                    if (priorityPrintingList.size() > 0) {
                        String line = priorityPrintingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            typingPrint(String.valueOf(c), refArea);
                            Thread.sleep(charTimeout);
                        }
                    } else if (printingList.size() > 0) {
                        String line = printingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            typingPrint(String.valueOf(c), refArea);
                            Thread.sleep(charTimeout);
                        }
                    }

                    Thread.sleep(lineTimeout);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"login animation").start();
        new Thread(() -> {
            try {
                while (doLoginAnimations && loginFrame != null) {
                    if (loginField.getCaretPosition() < bashString.length()) {
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    Thread.sleep(100);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"login caret position updater").start();
    }

    protected final void login() {
        doLoginAnimations = true;
        loginMode = 0;

        if (loginFrame != null)
            loginFrame.closeAnimation();

        IOUtil.cleanUpUsers();

        loginFrame = new CyderFrame(600, 400, new ImageIcon("sys/pictures/login.png"));
        loginFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        loginFrame.setTitle(IOUtil.getSystemData("Version") + " login");
        loginFrame.setBackground(new Color(21,23,24));

        if (consoleFrame == null || !consoleFrame.isActive() || !consoleFrame.isVisible())
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        else
            loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextPane loginArea = new JTextPane();
        loginArea.setBounds(20, 40, 560, 280);
        loginArea.setBackground(new Color(21,23,24));
        loginArea.setBorder(null);
        loginArea.setFocusable(false);
        loginArea.setEditable(false);
        loginArea.setFont(new Font("Agency FB",Font.BOLD, 26));
        loginArea.setForeground(new Color(85,181,219));
        loginArea.setCaretColor(loginArea.getForeground());

        CyderScrollPane loginScroll = new CyderScrollPane(loginArea,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        loginScroll.setThumbColor(CyderColors.intellijPink);
        loginScroll.setBounds(20, 40, 560, 280);
        loginScroll.getViewport().setOpaque(false);
        loginScroll.setOpaque(false);
        loginScroll.setBorder(null);
        loginArea.setAutoscrolls(true);

        loginFrame.getContentPane().add(loginScroll);

        loginField = new JPasswordField(20);
        loginField.setEchoChar((char)0);
        loginField.setText(bashString);
        loginField.setBounds(20, 340, 560, 40);
        loginField.setBackground(new Color(21,23,24));
        loginField.setBorder(null);
        loginField.setCaret(new CyderCaret(loginArea.getForeground()));
        loginField.setSelectionColor(CyderColors.selectionColor);
        loginField.setFont(new Font("Agency FB",Font.BOLD, 26));
        loginField.setForeground(new Color(85,181,219));
        loginField.setCaretColor(new Color(85,181,219));
        loginField.addActionListener(e -> loginField.requestFocusInWindow());
        loginField.addKeyListener(new KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE && loginMode != 2) {
                    if (loginField.getPassword().length < bashString.toCharArray().length) {
                        evt.consume();
                        loginField.setText(bashString);
                    }
                }

                else if (evt.getKeyChar() == '\n') {
                    char[] input = loginField.getPassword();

                    if (loginMode != 2) {
                        char[] newInput = new char[input.length - bashString.toCharArray().length];

                        //copy input to new input with offset
                        if (input.length - bashString.length() >= 0) {
                            System.arraycopy(input, bashString.length(), newInput, 0,
                                    input.length - bashString.length());
                        }

                        input = newInput.clone();
                    }

                    switch (loginMode) {
                        case 0:
                            try {
                                if (Arrays.equals(input,"create".toCharArray())) {
                                    createUser();
                                    loginField.setText(bashString);
                                    loginMode = 0;
                                } else if (Arrays.equals(input,"login".toCharArray())) {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Awaiting Username...\n");
                                    loginMode = 1;
                                } else if (Arrays.equals(input,"login admin".toCharArray())) {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Feature not yet implemented\n");
                                    loginMode = 0;
                                } else if (Arrays.equals(input,"quit".toCharArray())) {
                                    loginFrame.closeAnimation();
                                } else if (Arrays.equals(input,"h".toCharArray())) {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Valid commands: create, login, login admin, quit, h\n");
                                } else {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Unknown command; See \"h\" for help\n");
                                }
                            } catch (Exception e) {
                                ErrorHandler.handle(e);
                            }

                            break;
                        case 1:
                            username = new String(input);
                            loginMode = 2;
                            loginField.setEchoChar('*');
                            loginField.setText("");
                            priorityPrintingList.add("Awaiting Password...\n");

                            break;

                        case 2:
                            loginField.setEchoChar((char)0);

                            try {
                                Robot rob = new Robot();
                                rob.keyPress(KeyEvent.VK_BACK_SPACE);
                                rob.keyRelease(KeyEvent.VK_BACK_SPACE);
                            } catch (Exception e) {
                                ErrorHandler.handle(e);
                            }

                            recognize(username,input);
                            priorityPrintingList.add("Could not recognize user\n");

                            if (input != null)
                                for (char c: input)
                                    c = '\0';

                            loginMode = 0;
                            break;

                        default:
                            loginField.setText(bashString);
                            try {
                                throw new FatalException("Error resulting from login shell");
                            } catch (FatalException e) {
                                ErrorHandler.handle(e);
                            }
                    }
                }
            }
        });

        loginField.setCaretPosition(bashString.length());
        loginFrame.getContentPane().add(loginField);

        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                loginField.requestFocus();
            }
        });

        File Users = new File("users/");
        String[] directories = Users.list((current, name) -> new File(current, name).isDirectory());

        loginFrame.setVisible(true);
        loginFrame.enterAnimation();

        if (directories != null && directories.length == 0)
            priorityPrintingList.add("No users found; please type \"create\"");

        loginTypingAnimation(loginArea);
    }

    private void recognize(String Username, char[] Password) {
        try {
            if (loginFrame != null) {
                loginField.setEchoChar((char)0);
                loginField.setText(bashString);
            }

            if (SecurityUtil.checkPassword(Username, SecurityUtil.toHexString(SecurityUtil.getSHA(Password)))) {
                IOUtil.readUserData();
                doLoginAnimations = false;

                if (loginFrame != null)
                    loginFrame.closeAnimation();

                if (consoleFrame != null)
                    AnimationUtil.closeAnimation(consoleFrame);

                console();

                //this if block needs to be in console, stuff to do specifically for user on first login
                if (IOUtil.getUserData("IntroMusic").equals("1")) {
                    LinkedList<String> MusicList = new LinkedList<>();

                    File UserMusicDir = new File("users/" + ConsoleFrame.getUUID() + "/Music");

                    String[] FileNames = UserMusicDir.list();

                    if (FileNames != null)
                        for (String fileName : FileNames)
                            if (fileName.endsWith(".mp3"))
                                MusicList.add(fileName);

                    if (!MusicList.isEmpty())
                        IOUtil.playAudio(
                                "users/" + ConsoleFrame.getUUID() + "/Music/" +
                                        (FileNames[NumberUtil.randInt(0, FileNames.length - 1)]));
                    else
                        IOUtil.playAudio("sys/audio/Suprise.mp3");
                        //todo change me
                }
            } else if (loginFrame != null && loginFrame.isVisible()) {
                loginField.setText("");

                for (char c: Password)
                    c = '\0';
                username = "";

                loginField.requestFocusInWindow();
            } else
                login();
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }
    }

    //todo move to consoleFrame
    private void exitFullscreen() {
        ConsoleFrame.initBackgrounds(); //todo there was a background error here when I deleted a a background and flipping didn't work
        LinkedList<File> backgrounds = ConsoleFrame.getBackgrounds();
        int index = ConsoleFrame.getBackgroundIndex();
        String backFile = backgrounds.get(index).toString();

        int width = 0;
        int height = 0;

        ImageIcon backIcon;

        switch (ConsoleFrame.getConsoleDirection()) {
            case TOP:
                backIcon = new ImageIcon(backFile);
                width = backIcon.getIconWidth();
                height = backIcon.getIconHeight();
                parentLabel.setIcon(backIcon);

                break;
            case BOTTOM:
                backIcon = new ImageIcon(backFile);
                width = backIcon.getIconWidth();
                height = backIcon.getIconHeight();
                parentLabel.setIcon(new ImageIcon(ImageUtil.getRotatedImage(ConsoleFrame.getCurrentBackgroundFile().toString(), ConsoleFrame.getConsoleDirection())));

                break;
            default:
                backIcon = new ImageIcon(backFile);

                if (ConsoleFrame.getConsoleDirection() == Direction.LEFT || ConsoleFrame.getConsoleDirection() == Direction.RIGHT) {
                    height = backIcon.getIconWidth();
                    width = backIcon.getIconHeight();
                }

                parentLabel.setIcon(new ImageIcon(ImageUtil.getRotatedImage(ConsoleFrame.getCurrentBackgroundFile().toString(), ConsoleFrame.getConsoleDirection())));

                break;
        }

        consoleFrame.setBounds(0, 0, width, height);
        parentPane.setBounds(0, 0, width, height);
        parentLabel.setBounds(0, 0, width, height);

        outputArea.setBounds(0, 0, width - 20, height - 204);
        outputScroll.setBounds(10, 62, width - 20, height - 204);
        inputField.setBounds(10, 82 + outputArea.getHeight(), width - 20, height - (outputArea.getHeight() + 62 + 40));
        consoleDragLabel.setBounds(0, 0, width, 30);
        minimize.setBounds(width - 81, 4, 22, 20);
        alternateBackground.setBounds(width - 54, 4, 22, 20);
        close.setBounds(width - 27, 4, 22, 20);
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 -
                        CyderFrame.getMinWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) / 2 - 13,
                2, CyderFrame.getMinWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) + 26, 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();

        consoleFrame.setLocationRelativeTo(null);

        if (editUserFrame != null && editUserFrame.isVisible())
            editUserFrame.requestFocus();
    }

    //todo move to consoleFrame
    private void switchBackground() {
        new Thread(() -> {
            try {
                ConsoleFrame.initBackgrounds();

                LinkedList<File> backgrounds = ConsoleFrame.getBackgrounds();
                int oldIndex = (ConsoleFrame.getBackgroundIndex() == 0 ? backgrounds.size() - 1 : ConsoleFrame.getBackgroundIndex() - 1);
                String oldBackFile = backgrounds.get(oldIndex).toString();
                String newBackFile = ConsoleFrame.getCurrentBackgroundFile().toString();

                ImageIcon oldBack = new ImageIcon(oldBackFile);
                BufferedImage newBack = ImageIO.read(new File(newBackFile));

                BufferedImage temporaryImage;
                int temporaryWidth = 0;
                int temporaryHeight = 0;

                if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                    oldBack = new ImageIcon(ImageUtil.resizeImage((int) SystemUtil.getScreenSize().getWidth(),
                            (int) SystemUtil.getScreenSize().getHeight(), new File(oldBackFile)));
                    newBack = ImageUtil.resizeImage((int) SystemUtil.getScreenSize().getWidth(),
                            (int) SystemUtil.getScreenSize().getHeight(),
                            new File(newBackFile));
                    temporaryImage = ImageUtil.resizeImage((int) SystemUtil.getScreenSize().getWidth(),
                            (int) SystemUtil.getScreenSize().getHeight(),
                            new File(oldBackFile));
                    temporaryWidth = temporaryImage.getWidth();
                    temporaryHeight = temporaryImage.getHeight();
                } else {
                    newBack = ImageUtil.resizeImage(newBack.getWidth(), newBack.getHeight(), new File(newBackFile));
                    temporaryImage = ImageUtil.resizeImage(newBack.getWidth(), newBack.getHeight(), new File(oldBackFile));
                    temporaryWidth = temporaryImage.getWidth();
                    temporaryHeight = temporaryImage.getHeight();
                }

                refreshConsoleFrame();

                consoleFrame.setLocationRelativeTo(null);
                //set new frame relative to old frame
                //we need to get bounds and location of old frame, determine it's center
                //find the center of the new image
                //and align that center with the old center using math

                //based on last slide direction
                if (slidLeft) {
                    JLabel temporaryLabel = new JLabel();

                    //setting proper icons to labels to give animation the effect of sliding
                    parentLabel.setIcon(new ImageIcon(newBack));
                    temporaryLabel.setIcon(new ImageIcon(temporaryImage));

                    //add temporary label
                    parentPane.add(temporaryLabel);

                    //set proper bounds
                    parentLabel.setBounds(-temporaryWidth, 0, temporaryWidth, temporaryHeight);
                    temporaryLabel.setBounds(0, 0, temporaryWidth, temporaryHeight);

                    int[] parts = AnimationUtil.getDelayIncrement(temporaryWidth);

                    //animate the labels
                    AnimationUtil.jLabelXRight(0, temporaryWidth, parts[0], parts[1], temporaryLabel);
                    AnimationUtil.jLabelXRight(-temporaryWidth, 0, parts[0], parts[1], parentLabel);
                } else {
                    JLabel temporaryLabel = new JLabel();
                    parentLabel.setIcon(new ImageIcon(newBack));
                    temporaryLabel.setIcon(new ImageIcon(temporaryImage));
                    parentPane.add(temporaryLabel);
                    parentLabel.setBounds(temporaryWidth, 0, temporaryWidth, temporaryHeight);
                    temporaryLabel.setBounds(0, 0, temporaryWidth, temporaryHeight);

                    int[] parts = AnimationUtil.getDelayIncrement(temporaryWidth);

                    AnimationUtil.jLabelXLeft(0, -temporaryWidth, parts[0], parts[1], temporaryLabel);
                    AnimationUtil.jLabelXLeft(temporaryWidth, 0, parts[0], parts[1], parentLabel);
                }

                //invert scrolling direction for next time
                slidLeft = !slidLeft;
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }).start();
    }

    //move to consoleframe
    private void refreshConsoleFrame() {
        ConsoleFrame.initBackgrounds();
        LinkedList<File> backgrounds = ConsoleFrame.getBackgrounds();
        String backFile = backgrounds.get(ConsoleFrame.getBackgroundIndex()).toString();

        ImageIcon backIcon = new ImageIcon(backFile);
        BufferedImage fullimg = null;

        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
            fullimg = ImageUtil.resizeImage((int) SystemUtil.getScreenSize().getWidth(),
                    (int) SystemUtil.getScreenSize().getHeight(), new File(backFile));
        } else {
            try {
                fullimg = ImageIO.read(new File(backFile));
            } catch (IOException e) {
                ErrorHandler.handle(e);
            }
        }

        int fullW = fullimg.getWidth();
        int fullH = fullimg.getHeight();

        parentLabel.setIcon(new ImageIcon(fullimg));

        consoleFrame.setBounds(0, 0, fullW, fullH);
        parentPane.setBounds(0, 0, fullW, fullH);
        parentLabel.setBounds(0, 0, fullW, fullH);

        outputArea.setBounds(0, 0, fullW - 20, fullH - 204);
        outputScroll.setBounds(10, 62, fullW - 20, fullH - 204);
        inputField.setBounds(10, 82 + outputArea.getHeight(), fullW - 20, fullH - (outputArea.getHeight() + 62 + 40));
        consoleDragLabel.setBounds(0, 0, fullW, 30);
        minimize.setBounds(fullW - 81, 4, 22, 20);
        alternateBackground.setBounds(fullW - 54, 4, 22, 20);
        close.setBounds(fullW - 27, 4, 22, 20);
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 -
                        CyderFrame.getMinWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) / 2 - 13,
                2, CyderFrame.getMinWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) + 26, 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();
    }

    //todo move to input handler
    private void clc() {
        outputArea.setText("");
        inputField.setText("");
    }

    //input handler
    private void handleSecond(String input) {
        try {
            String desc = stringUtil.getUserInputDesc();

            if (desc.equalsIgnoreCase("url") && !stringUtil.empytStr(input)) {
                NetworkUtil.internetConnect(new URI(input));
            } else if (desc.equalsIgnoreCase("prime") && input != null && !input.equals("")) {
                int num = Integer.parseInt(input);

                if (num <= 0) {
                    println("The inger " + num + " is not a prime number because it is negative.");
                } else if (num == 1) {
                    println("The inger 1 is not a prime number by the definition of a prime number.");
                } else if (num == 2) {
                    println("The integer 2 is indeed a prime number.");
                }

                ArrayList<Integer> Numbers = new ArrayList<>();

                for (int i = 3; i < Math.ceil(Math.sqrt(num)); i += 2) {
                    if (num % i == 0) {
                        Numbers.add(i);
                    }
                }

                if (Numbers.isEmpty()) {
                    println("The integer " + num + " is indeed a prime number.");
                } else {
                    println("The integer " + num + " is not a prime number because it is divisible by " + Numbers);
                }
            } else if (desc.equalsIgnoreCase("google") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                NetworkUtil.internetConnect("https://www.google.com/search?q=" + input);
            } else if (desc.equalsIgnoreCase("youtube") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                NetworkUtil.internetConnect("https://www.youtube.com/results?search_query=" + input);
            } else if (desc.equalsIgnoreCase("math") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                NetworkUtil.internetConnect("https://www.wolframalpha.com/input/?i=" + input);
            } else if (desc.equalsIgnoreCase("binary")) {
                if (input.matches("[0-9]+") && !stringUtil.empytStr(input)) {
                    println(input + " converted to binary equals: " + Integer.toBinaryString(Integer.parseInt(input)));
                } else {
                    println("Your value must only contain numbers.");
                }
            } else if (desc.equalsIgnoreCase("disco") && input != null && !input.equals("")) {
                println("I hope you're not the only one at this party.");
                SystemUtil.disco(Integer.parseInt(input));
            } else if (desc.equalsIgnoreCase("youtube word search") && input != null && !input.equals("")) {
                String browse = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
                browse = browse.replace("REPLACE", input).replace(" ", "+");
                NetworkUtil.internetConnect(browse);
            } else if (desc.equalsIgnoreCase("random youtube")) {
                try {
                    int threads = Integer.parseInt(input);

                    //todo
//                    notify("The" + (threads > 1 ? " scripts have " : " script has ") + "started. At any point, type \"stop script\"",
//                            4000, ArrowDirection.TOP, VanishDirection.TOP, parentPane, (threads > 1 ? 620 : 610));

                    for (int i = 0; i < threads; i++) {
                        YoutubeThread current = new YoutubeThread(outputArea);
                        youtubeThreads.add(current);
                    }
                } catch (NumberFormatException e) {
                    println("Invalid input for number of threads to start.");
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            } else if (desc.equalsIgnoreCase("anagram1")) {
                println("Enter your second word");
                anagram = input;
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("anagram2");
            } else if (desc.equalsIgnoreCase("anagram2")) {
                if (anagram.length() != input.length()) {
                    println("These words are not anagrams of each other.");
                } else if (anagram.equalsIgnoreCase(input)) {
                    println("These words are in fact anagrams of each other.");
                } else {
                    char[] W1C = anagram.toLowerCase().toCharArray();
                    char[] W2C = input.toLowerCase().toCharArray();
                    Arrays.sort(W1C);
                    Arrays.sort(W2C);

                    if (Arrays.equals(W1C, W2C)) {
                        println("These words are in fact anagrams of each other.");
                    } else {
                        println("These words are not anagrams of each other.");
                    }
                }

                anagram = "";
            } else if (desc.equalsIgnoreCase("pixelate") && input != null && !input.equals("")) {
                println("Pixelating " + pixelateFile.getName() + " with a pixel block size of " + input + "...");
                ImageUtil.pixelate(pixelateFile, Integer.parseInt(input));
            } else if (desc.equalsIgnoreCase("alphabetize")) {
                char[] Sorted = input.toCharArray();
                Arrays.sort(Sorted);
                println("\"" + input + "\" alphabetically organized is \"" + new String(Sorted) + "\".");
            } else if (desc.equalsIgnoreCase("suggestion")) {
                stringUtil.logSuggestion(input);
            } else if (desc.equalsIgnoreCase("addbackgrounds")) {
                if (InputUtil.confirmation(input)) {
                    editUser();
                    NetworkUtil.internetConnect("https://images.google.com/");
                } else
                    println("Okay nevermind then");
            } else if (desc.equalsIgnoreCase("logoff")) {
                if (InputUtil.confirmation(input)) {
                    String shutdownCmd = "shutdown -l";
                    Runtime.getRuntime().exec(shutdownCmd);
                } else
                    println("Okay nevermind then");
            } else if (desc.equalsIgnoreCase("deleteuser")) {
                if (!InputUtil.confirmation(input)) {
                    println("User " + ConsoleFrame.getUsername() + " was not removed.");
                    return;
                }

                AnimationUtil.closeAnimation(consoleFrame);
                SystemUtil.deleteFolder(new File("users/" + ConsoleFrame.getUUID()));

                String dep = SecurityUtil.getDeprecatedUUID();

                File renamed = new File("users/" + dep);
                while (renamed.exists()) {
                    dep = SecurityUtil.getDeprecatedUUID();
                    renamed = new File("users/" + dep);
                }

                File old = new File("users/" + ConsoleFrame.getUUID());
                old.renameTo(renamed);

                Frame[] frames = Frame.getFrames();

                for (Frame f : frames)
                    f.dispose();

                login();
            } else if (desc.equalsIgnoreCase("pixelatebackground")) {
                BufferedImage img = ImageUtil.pixelate(ImageIO.read(ConsoleFrame.getCurrentBackgroundFile().getAbsoluteFile()), Integer.parseInt(input));

                String searchName = ConsoleFrame.getCurrentBackgroundFile().getName().replace(".png", "")
                        + "_Pixelated_Pixel_Size_" + Integer.parseInt(input) + ".png";

                File saveFile = new File("users/" + ConsoleFrame.getUUID() +
                        "/Backgrounds/" + searchName);

                ImageIO.write(img, "png", saveFile);

                LinkedList<File> backgrounds = ConsoleFrame.getBackgrounds();

                for (int i = 0; i < backgrounds.size(); i++) {
                    if (backgrounds.get(i).getName().equals(searchName)) {
                        parentLabel.setIcon(new ImageIcon(backgrounds.get(i).toString()));
                        parentLabel.setToolTipText(backgrounds.get(i).getName().replace(".png", ""));
                        ConsoleFrame.setBackgroundIndex(i);
                    }
                }

                println("Background pixelated and saved as a separate background file.");

                exitFullscreen();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void handle(String input) {
        try {
            operation = input;
            String firstWord = stringUtil.firstWord(operation);

            if (handleMath(operation))
                return;

            if (evaluateExpression(operation))
                return;

            if (StringUtil.filterLanguage(operation)) {
                println("Sorry, " + ConsoleFrame.getUsername() + ", but that language is prohibited.");
                operation = "";
            } else if (stringUtil.isPalindrome(operation.replace(" ", "")) && operation.length() > 3) {
                println("Nice palindrome.");
            } else if (((hasWord("quit") && !hasWord("db")) ||
                    (eic("leave") || (hasWord("stop") && !hasWord("music") && !hasWord("script") && !hasWord("scripts")) ||
                            hasWord("exit") || eic("close"))) && !has("dance")) {
                exit();
            } else if (hasWord("consolidate") && (hasWord("windows") || hasWord("frames"))) {
                Frame[] frames = Frame.getFrames();

                int x = consoleFrame.getX();
                int y = consoleFrame.getY();

                for (Frame f : frames)
                    if (f.isVisible())
                        f.setLocation(x, y);
            } else if (hasWord("bletchy")) {
                stringUtil.setOutputArea(outputArea);
                stringUtil.bletchy(operation, false, 50);
            } else if ((hasWord("flip") && hasWord("coin")) || (hasWord("heads") && hasWord("tails"))) {
                if (Math.random() <= 0.0001) {
                    println("You're not going to beleive this, but it landed on its side.");
                } else if (Math.random() <= 0.5) {
                    println("It's Heads!");
                } else {
                    println("It's Tails!");
                }
            } else if ((eic("hello") || has("whats up") || hasWord("hi"))
                    && (!hasWord("print") && !hasWord("bletchy") && !hasWord("echo") &&
                    !hasWord("youtube") && !hasWord("google") && !hasWord("wikipedia") &&
                    !hasWord("synonym") && !hasWord("define"))) {
                int choice = NumberUtil.randInt(1, 7);

                switch (choice) {
                    case 1:
                        println("Hello, " + ConsoleFrame.getUsername() + ".");
                        break;
                    case 2:
                        if (TimeUtil.isEvening())
                            println("Good evening, " + ConsoleFrame.getUsername() + ". How can I help?");
                        else if (TimeUtil.isMorning())
                            println("Good monring, " + ConsoleFrame.getUsername() + ". How can I help?");
                        else
                            println("Good afternoon, " + ConsoleFrame.getUsername() + ". How can I help?");
                        break;
                    case 3:
                        println("What's up, " + ConsoleFrame.getUsername() + "?");
                        break;
                    case 4:
                        println("How are you doing, " + ConsoleFrame.getUsername() + "?");
                        break;
                    case 5:
                        println("Greetings, " + ConsoleFrame.getUsername() + ".");
                        break;
                    case 6:
                        println("I'm here....");
                        break;
                    case 7:
                        println("Go ahead...");
                        break;
                }
            } else if (hasWord("bye") || (hasWord("james") && hasWord("arthur"))) {
                println("Just say you won't let go.");
            } else if (hasWord("time") && hasWord("what")) {
                println(TimeUtil.weatherTime());
            } else if (eic("die") || (hasWord("roll") && hasWord("die"))) {
                int Roll = ThreadLocalRandom.current().nextInt(1, 7);
                println("You rolled a " + Roll + ".");
            } else if (eic("lol")) {
                println("My memes are better.");
            } else if ((hasWord("thank") && hasWord("you")) || hasWord("thanks")) {
                println("You're welcome.");
            } else if (hasWord("you") && hasWord("cool")) {
                println("I know.");
            } else if (has("paint")) {
                String param = "C:/Windows/system32/mspaint.exe";
                Runtime.getRuntime().exec(param);
            } else if (hasWord("scrub")) {
                stringUtil.setOutputArea(outputArea);
                stringUtil.bletchy("No you!", false, 50);
            } else if (eic("break;")) {
                println("Thankfully I am over my infinite while loop days.");
            } else if (hasWord("url")) {
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("url");
                println("Enter your desired URL");
            } else if (hasWord("temperature") || eic("temp")) {
                TempConverter tc = new TempConverter();
            } else if (has("click me")) {
                ClickMe.clickMe();
            } else if ((hasWord("how") && hasWord("are") && hasWord("you")) && !hasWord("age") && !hasWord("old")) {
                println("I am feeling like a programmed response. Thank you for asking.");
            } else if (hasWord("how") && hasWord("day")) {
                println("I was having fun until you started asking me questions.");
            } else if (has("How old are you") || (hasWord("what") && hasWord("age"))) {
                stringUtil.bletchy("As old as my tongue and a little bit older than my teeth, wait...", false, 50);
            } else if (((hasWord("who") || hasWord("what")) && has("you"))) {
                println("My name is Cyder. I am a tool built by Nathan Cheshire for programmers/advanced users.");
            } else if (hasWord("helpful") && hasWord("you")) {
                println("I will always do my best to serve you.");
            } else if (eic("k")) {
                println("Fun Fact: the letter 'K' comes from the Greek letter kappa, which was taken "
                        + "from the Semitic kap, the symbol for an open hand. It is this very hand which "
                        + "will be slapping you in the face for saying 'k' to me.");
            } else if (hasWord("phone") || hasWord("dialer") || hasWord("call")) {
                Phone p = new Phone();
            } else if (hasWord("reset") && hasWord("mouse")) {
                SystemUtil.resetMouse();
            } else if (eic("logoff")) {
                println("Are you sure you want to log off your computer? This is not Cyder we are talking about (Enter yes/no)");
                stringUtil.setUserInputDesc("logoff");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            } else if (eic("clc") || eic("cls") || eic("clear") || (hasWord("clear") && hasWord("screen"))) {
                clc();
            } else if (eic("no")) {
                println("Yes");
            } else if (eic("nope")) {
                println("yep");
            } else if (eic("yes")) {
                println("no");
            } else if (eic("yep")) {
                println("nope");
            } else if (has("how can I help")) {
                println("That's my line :P");
            } else if (hasWord("siri") || hasWord("jarvis") || hasWord("alexa")) {
                println("Whata bunch of losers.");
            } else if ((hasWord("mississippi") && hasWord("state") && hasWord("university")) || eic("msu")) {
                printImage("sys/pictures/msu.png");
            } else if (has("toy") && has("story")) {
                IOUtil.playAudio("sys/audio/TheClaw.mp3");
            } else if (has("stop") && has("music")) {
                IOUtil.stopMusic();
            } else if (hasWord("reset") && hasWord("clipboard")) {
                StringSelection selection = new StringSelection(null);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                println("Clipboard has been reset.");
            } else if ((has("graphing") && has("calculator")) || has("desmos") || has("graphing")) {
                NetworkUtil.internetConnect("https://www.desmos.com/calculator");
            } else if (has("airHeads xtremes") || has("candy")) {
                NetworkUtil.internetConnect("http://airheads.com/candy#xtremes");
            } else if (hasWord("prime")) {
                println("Enter any positive integer and I will tell you if it's prime and what it's divisible by.");
                stringUtil.setUserInputDesc("prime");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            } else if (hasWord("youtube") && (!has("word search") && !has("random") && !has("thumbnail"))) {
                println("What would you like to search YouTube for?");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("youtube");
            } else if ((hasWord("google"))) {
                println("What would you like to Google?");
                stringUtil.setUserInputDesc("google");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            } else if (eic("404")) {
                NetworkUtil.internetConnect("http://google.com/=");
            } else if ((hasWord("calculator") || hasWord("calc")) && !has("graphing")) {
                Calculator c = new Calculator();
            } else if (firstWord.equalsIgnoreCase("echo")) {
                String[] sentences = operation.split(" ");
                for (int i = 1; i < sentences.length; i++) {
                    print(sentences[i] + " ");
                }

                println("");
            } else if ((firstWord.equalsIgnoreCase("print") || firstWord.equalsIgnoreCase("println"))) {
                String[] sentences = operation.split(" ");

                for (int i = 1; i < sentences.length; i++) {
                    print(sentences[i] + " ");
                }

                println("");
            } else if (hasWord("triangle")) {
                NetworkUtil.internetConnect("https://www.triangle-calculator.com/");
            } else if (hasWord("why")) {
                println("Why not?");
            } else if (hasWord("why not")) {
                println("Why?");
            } else if (hasWord("groovy")) {
                println("Alright Scooby Doo.");
            } else if (hasWord("luck")) {
                if (Math.random() * 100 <= 0.001) {
                    println("YOU WON!!");
                } else {
                    println("You are not lucky today.");
                }
            } else if (has("are you sure") || has("are you certain")) {
                if (Math.random() <= 0.5) {
                    println("No");
                } else {
                    println("Yes");
                }
            } else if (has("math") && !eic("mathsh")) {
                println("What math operation would you like to perform?");
                stringUtil.setUserInputDesc("math");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            } else if (eic("nathan")) {
                printlnImage("sys/pictures/me.png");
            } else if ((eic("error") || eic("errors")) && !hasWord("throw")) {
                File WhereItIs = new File("users/" + ConsoleFrame.getUUID() + "/Throws/");
                Desktop.getDesktop().open(WhereItIs);
            } else if (eic("help")) {
                stringUtil.help(outputArea);
            } else if (hasWord("light") && hasWord("saber")) {
                IOUtil.playAudio("sys/audio/Lightsaber.mp3");
            } else if (hasWord("xbox")) {
                IOUtil.playAudio("sys/audio/xbox.mp3");
            } else if (has("star") && has("trek")) {
                IOUtil.playAudio("sys/audio/StarTrek.mp3");
            } else if (eic("cmd") || (hasWord("command") && hasWord("prompt"))) {
                File WhereItIs = new File("c:\\Windows\\System32\\cmd.exe");
                Desktop.getDesktop().open(WhereItIs);
            } else if (hasWord("shakespeare")) {
                int rand = NumberUtil.randInt(1, 2);

                if (rand == 1) {
                    println("Glamis hath murdered sleep, and therefore Cawdor shall sleep no more, Macbeth shall sleep no more.");
                } else {
                    println("To be, or not to be, that is the question: Whether 'tis nobler in the mind to suffer the slings and arrows of "
                            + "outrageous fortune, or to take arms against a sea of troubles and by opposing end them.");
                }
            } else if (hasWord("windows")) {
                IOUtil.playAudio("sys/audio/windows.mp3");
            } else if (hasWord("binary")) {
                println("Enter a decimal number to be converted to binary.");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("binary");
            } else if (hasWord("pizza")) {
                Pizza p = new Pizza();
            } else if (hasWord("imposible")) {
                println("¿Lo es?");
            } else if (eic("look")) {
                println("L()()K ---->> !FREE STUFF! <<---- L()()K");
            } else if (eic("Cyder?")) {
                println("Yes?");
            } else if (firstWord.equalsIgnoreCase("define")) {
                //todo parse string and print response here
            } else if (hasWord("wikipedia")) {
                //todo parse string and print response here
            } else if (firstWord.equalsIgnoreCase("synonym")) {
                //todo parse string and print response here
            } else if (hasWord("board")) {
                NetworkUtil.internetConnect("http://gameninja.com//games//fly-squirrel-fly.html");
            } else if (hasWord("open cd")) {
                SystemUtil.openCD("D:\\");
            } else if (hasWord("close cd")) {
                SystemUtil.closeCD("D:\\");
            } else if (hasWord("font") && hasWord("reset")) {
                inputField.setFont(CyderFonts.defaultFont);
                outputArea.setFont(CyderFonts.defaultFont);
                println("The font has been reset.");
                IOUtil.writeUserData("Fonts", outputArea.getFont().getName());
            } else if (hasWord("reset") && hasWord("color")) {
                outputArea.setForeground(CyderColors.vanila);
                inputField.setForeground(CyderColors.vanila);
                println("The text color has been reset.");
                IOUtil.writeUserData("Foreground", ColorUtil.rgbtohexString(CyderColors.defaultColor));
            } else if (eic("top left")) {
                consoleFrame.setLocation(0, 0);
            } else if (eic("top right")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int X = (int) rect.getMaxX() - consoleFrame.getWidth();
                consoleFrame.setLocation(X, 0);
            } else if (eic("bottom left")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int Y = (int) rect.getMaxY() - consoleFrame.getHeight();
                consoleFrame.setLocation(0, Y);
            } else if (eic("bottom right")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int X = (int) rect.getMaxX();
                int Y = (int) rect.getMaxY();
                consoleFrame.setLocation(X - consoleFrame.getWidth(), Y - consoleFrame.getHeight());
            } else if (eic("middle") || eic("center")) {
                consoleFrame.setLocationRelativeTo(null);
            } else if (hasWord("random") && hasWord("youtube")) {
                killAllYoutube();
                println("How many isntances of the script do you want to start?");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("random youtube");
            } else if (hasWord("arduino")) {
                NetworkUtil.internetConnect("https://www.arduino.cc/");
            } else if (has("rasberry pi")) {
                NetworkUtil.internetConnect("https://www.raspberrypi.org/");
            } else if (eic("&&")) {
                println("||");
            } else if (eic("||")) {
                println("&&");
            } else if (eic("&")) {
                println("|");
            } else if (eic("|")) {
                println("&");
            } else if (eic("youtube word search")) {
                println("Enter the desired word you would like to find in a YouTube URL");
                stringUtil.setUserInputDesc("youtube word search");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            } else if (hasWord("disco")) {
                println("How many iterations would you like to disco for? (Enter a positive integer)");
                stringUtil.setUserInputMode(true);
                inputField.requestFocus();
                stringUtil.setUserInputDesc("disco");
            } else if (hasWord("there") && hasWord("no") && hasWord("internet")) {
                println("Sucks to be you.");
            } else if (eic("i hate you")) {
                println("That's not very nice.");
            } else if (eic("netsh")) {
                File WhereItIs = new File("C:\\Windows\\system32\\netsh.exe");

                Desktop.getDesktop().open(WhereItIs);
            } else if (hasWord("java") && hasWord("properties")) {
                StatUtil.javaProperties();
            } else if ((hasWord("edit") && hasWord("user")) ||
                    (hasWord("font") && !hasWord("reset")) ||
                    (hasWord("color") && !hasWord("reset") && !hasWord("converter")) || (eic("preferences") || eic("prefs"))) {
                editUser();
            } else if (hasWord("story") && hasWord("tell")) {
                println("It was a lazy day. Cyder was enjoying a deep sleep when suddenly " + ConsoleFrame.getUsername() + " started talking to Cyder."
                        + " It was at this moment that Cyder knew its day had been ruined.");
            } else if (eic("hey")) {
                IOUtil.playAudio("sys/audio/heyya.mp3");
            } else if (eic("panic")) {
                exit();
            } else if (hasWord("hash") || hasWord("hashser")) {
                new Hasher();
            } else if (hasWord("home")) {
                println("There's no place like localhost/127.0.0.1");
            } else if (eic("search") || eic("dir") || (hasWord("file") && hasWord("search")) || eic("directory") || eic("ls")) {
                DirectorySearch ds = new DirectorySearch();
            } else if (hasWord("I") && hasWord("love")) {
                println("Sorry, " + ConsoleFrame.getUsername() + ", but I don't understand human emotions or affections.");
            } else if (hasWord("vexento")) {
                NetworkUtil.internetConnect("https://www.youtube.com/user/Vexento/videos");
            } else if (hasWord("minecraft")) {
                Minecraft mw = new Minecraft();
            } else if (eic("loop")) {
                println("ErrorHandler.handle(\"loop\");");
            } else if (hasWord("cyder") && has("dir")) {
                if (SecurityUtil.compMACAddress(SecurityUtil.getMACAddress())) {
                    String CurrentDir = System.getProperty("user.dir");
                    IOUtil.openFile(CurrentDir);
                } else {
                    println("Sorry, " + ConsoleFrame.getUsername() + ", but you don't have permission to do that.");
                }
            } else if ((has("tic") && has("tac") && has("toe")) || eic("TTT")) {
                TicTacToe ttt = new TicTacToe();
                ttt.startTicTacToe();
            } else if (hasWord("note") || hasWord("notes")) {
                new Notes();
            } else if ((hasWord("youtube") && hasWord("thumbnail")) || (hasWord("yt") && hasWord("thumb"))) {
                YouTubeThumbnail yttn = new YouTubeThumbnail();
            } else if (hasWord("papers") && hasWord("please")) {
                NetworkUtil.internetConnect("http://papersplea.se/");
            } else if (eic("java")) {
                printImage("sys/pictures/Duke.png");
            } else if (hasWord("coffee")) {
                NetworkUtil.internetConnect("https://www.google.com/search?q=coffe+shops+near+me");
            } else if (hasWord("ip")) {
                println(InetAddress.getLocalHost().getHostAddress());
            } else if (hasWord("html") || hasWord("html5")) {
                consoleFrame.setIconImage(new ImageIcon("sys/pictures/html5.png").getImage());
                printlnImage("sys/pictures/html5.png");
            } else if (hasWord("css")) {
                consoleFrame.setIconImage(new ImageIcon("sys/pictures/css.png").getImage());
                printlnImage("sys/pictures/css.png");
            } else if (hasWord("computer") && hasWord("properties")) {
                println("This may take a second, since this feature counts your PC's free memory");
                StatUtil.computerProperties();
            } else if (hasWord("system") && hasWord("properties")) {
                StatUtil.systemProperties();
            } else if ((hasWord("pixelate") || hasWord("distort")) && (hasWord("image") || hasWord("picture"))) {
                pixelateFile = IOUtil.getFile();

                if (!pixelateFile.getName().endsWith(".png")) {
                    println("Sorry, " + ConsoleFrame.getUsername() + ", but this feature only supports PNG images");
                } else if (pixelateFile != null) {
                    println("Enter your pixel size (Enter a positive integer)");
                    stringUtil.setUserInputDesc("pixelate");
                    inputField.requestFocus();
                    stringUtil.setUserInputMode(true);
                }
            } else if (hasWord("donuts")) {
                NetworkUtil.internetConnect("https://www.dunkindonuts.com/en/food-drinks/donuts/donuts");
            } else if (hasWord("anagram")) {
                println("This function will tell you if two"
                        + "words are anagrams of each other."
                        + " Enter your first word");
                stringUtil.setUserInputDesc("anagram1");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);

            } else if (eic("controlc") && !outputArea.isFocusOwner()) {
                stringUtil.setUserInputMode(false);
                killAllYoutube();
                stringUtil.killBletchy();
                println("Escaped");
            } else if (has("alphabet") && (hasWord("sort") || hasWord("organize") || hasWord("arrange"))) {
                println("Enter your word to be alphabetically rearranged");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("alphabetize");
            } else if (hasWord("mp3") || hasWord("music")) {
                IOUtil.mp3("");
            } else if (hasWord("bai")) {
                NetworkUtil.internetConnect("http://www.drinkbai.com");
            } else if (has("occam") && hasWord("razor")) {
                NetworkUtil.internetConnect("http://en.wikipedia.org/wiki/Occam%27s_razor");
            } else if (hasWord("cyder") && (has("picture") || has("image"))) {
                if (SecurityUtil.compMACAddress(SecurityUtil.getMACAddress())) {
                    IOUtil.openFile("sys/pictures");
                } else {
                    println("Sorry, " + ConsoleFrame.getUsername() + ", but you do not have permission to access that.");
                }
            } else if (hasWord("when") && hasWord("thanksgiving")) {
                int year = Calendar.getInstance().get(Calendar.YEAR);
                LocalDate RealTG = LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
                println("Thanksgiving this year is on the " + RealTG.getDayOfMonth() + " of November.");
            } else if (hasWord("location") || (hasWord("where") && hasWord("am") && hasWord("i"))) {
                println("You are currently in " + IPUtil.getUserCity() + ", " +
                        IPUtil.getUserState() + " and your Internet Service Provider is " + IPUtil.getIsp());
            } else if (hasWord("fibonacci")) {
                for (long i : NumberUtil.fib(0, 1, 100))
                    println(i);
            } else if (hasWord("throw") && hasWord("error")) {
                throw new CyderException("Error thrown on " + TimeUtil.userTime());
            } else if (hasWord("asdf")) {
                println("Who is the spiciest meme lord?");
            } else if (hasWord("qwerty")) {
                println("I prefer Dvorak, but I also like Colemak, Maltron, and JCUKEN.");
            } else if (hasWord("thor")) {
                println("Piss off, ghost.");
            } else if (eic("about:blank")) {
                NetworkUtil.internetConnect("about:blank");
            } else if (hasWord("weather")) {
                Weather ww = new Weather();
            } else if (eic("hide")) {
                minimize.doClick();
            } else if (hasWord("stop") && hasWord("script")) {
                killAllYoutube();
                println("YouTube scripts have been killed.");
            } else if (hasWord("debug") && hasWord("menu")) {
                StatUtil.debugMenu(outputArea);
            } else if (hasWord("hangman")) {
                Hangman Hanger = new Hangman();
                Hanger.startHangman();
            } else if (hasWord("rgb") || hasWord("hex") || (hasWord("color") && hasWord("converter"))) {
                ColorConverter.colorConverter();
            } else if (hasWord("danc    e")) {
                Frame[] frames = Frame.getFrames();
                for (Frame f : frames)
                    if (f instanceof CyderFrame)
                        ((CyderFrame) (f)).dance();
                //todo make dance cancelable by user,
                //method to handle ctrl + c actions within each frame
            } else if (hasWord("clear") && (hasWord("operation") ||
                    hasWord("command")) && hasWord("list")) {
                operationList.clear();
                scrollingIndex = 0;
                //todo log these in chat log. Tags: [USER], [SYSTEM], [EXCEPTION] (link to exception file)
                println("Command history reset.");
            } else if (eic("pin") || eic("login")) {
                login();
            } else if ((hasWord("delete") ||
                    hasWord("remove")) &&
                    (hasWord("user") ||
                            hasWord("account"))) {

                println("Are you sure you want to permanently delete this account? This action cannot be undone! (yes/no)");
                stringUtil.setUserInputMode(true);
                inputField.requestFocus();
                stringUtil.setUserInputDesc("deleteuser");
            } else if ((hasWord("create") || hasWord("new")) && hasWord("user")) {
                createUser();
            } else if (hasWord("pixelate") && hasWord("background")) {
                println("Enter your pixel size (a positive integer)");
                stringUtil.setUserInputDesc("pixelatebackground");
                stringUtil.setUserInputMode(true);
                inputField.requestFocus();
            } else if (hasWord("long") && hasWord("word")) {
                int count = 0;

                String[] words = operation.split(" ");

                for (String word : words)
                    if (word.equalsIgnoreCase("long"))
                        count++;

                for (int i = 0; i < count; i++)
                    print("pneumonoultramicroscopicsilicovolcanoconiosis");

                println("");
            } else if (eic("logic")) {
                IOUtil.playAudio("sys/audio/commando.mp3");
            } else if (eic("1-800-273-8255") || eic("18002738255")) {
                IOUtil.playAudio("sys/audio/1800.mp3");
            } else if (hasWord("resize") && (hasWord("image") || hasWord("picture"))) {
                ImageResizer IR = new ImageResizer();
            } else if (hasWord("barrel") && hasWord("roll")) {
                //todo ConsoleFrme.barrelRoll();
            } else if (hasWord("analyze") && hasWord("code")) {
                //file by file
                println(StatUtil.fileByFileAnalyze(new File("src")));
                //overview
                println("Lines of code: " +
                        StatUtil.totalJavaLines(new File("src")));
                println("Number of java files: " +
                        StatUtil.totalJavaFiles(new File("src")));
                println("Number of comments: " +
                        StatUtil.totalComments(new File("src")));
                println("Blank lines: " +
                        StatUtil.totalBlankLines(new File("src")));
                println("Total: " +
                        (StatUtil.totalBlankLines(new File("src"))
                        + StatUtil.totalJavaLines(new File("src"))));

            } else if (hasWord("threads") && !hasWord("daemon")) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                println("Executor services are identified by pool tags\nThreads are identified by the thread tag:\n");

                for (int i = 0; i < num; i++)
                    if (!printThreads[i].isDaemon())
                        println(printThreads[i].getName());
            } else if (hasWord("threads") && hasWord("daemon")) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                println("Executor services are identified by pool tags\nThreads are identified by the thread tag:\n");

                for (int i = 0; i < num; i++)
                    println(printThreads[i].getName());
            } else if (eic("rotateBackground")) {
                //todo ConsoleFrame.rotateBackground(5);
            } else if (hasWord("press") && (hasWord("F17") || hasWord("f17"))) {
                new Robot().keyPress(KeyEvent.VK_F17);
            } else if (hasWord("logout")) {
                for (Frame f : Frame.getFrames()) {
                    if (f instanceof CyderFrame)
                        ((CyderFrame) f).closeAnimation();
                    else
                        f.dispose();
                }
                consoleFrame = null;
                login();
            } else if ((hasWord("wipe") || hasWord("clear") || hasWord("delete")) && has("error")) {
                if (SecurityUtil.compMACAddress(SecurityUtil.getMACAddress())) {
                    IOUtil.wipeErrors();

                    println("Deleted all user erorrs");
                } else
                    println("Sorry, " + ConsoleFrame.getUsername() + ", but you don't have permission to do that.");
            } else if (hasWord("debug") && hasWord("windows")) {
                StatUtil.allStats(outputArea);
            } else if (hasWord("random") && hasWord("background")) {
                //todo press alternate background random number of times
            } else if (hasWord("output") && hasWord("border")) {
                //todo set output border
            } else if (hasWord("input") && hasWord("border")) {
                //todo set input border
            } else if ((hasWord("full") && hasWord("screen") || hasWord("fullscreen"))) {
                //todo enter full screen
            } else if (hasWord("fill") && hasWord("in")) {
                //todo fill input field
            } else if (hasWord("fill") && hasWord("out")) {
                //todo fill output area
            } else if (hasWord("alex") && hasWord("trebek")) {
                println("Do you mean who is alex trebek?");
            } else if (hasWord("christmas") && hasWord("card") && hasWord("2020")) {
                Cards.Christmas2020();
            } else if (hasWord("number") && hasWord("word")) {
                NumberUtil.numberToWord();
            } else if (hasWord("Quake") && (hasWord("three") || hasWord("3"))) {
                NetworkUtil.internetConnect("https://www.youtube.com/watch?v=p8u_k2LIZyo&ab_channel=Nemean");
            } else if (eic("test")) {
                //todo preference slider like from ThinMatrix's games, slide from left to right and change colors for on and reverse for off
                //todo be able to copy from outupt area without canceling using ctrl + c, check if selected text or not

                //todo preference refresh moves console frame and flashes window

                //todo photoviewer needs more consistnet window size and it moves the window to center every time, keep relative

                //todo fipping console resets output area position

                TestFrame tf = new TestFrame();
                CyderButton button = new CyderButton("Test Button");
                button.setBounds(40,40,140,40);
                tf.testFrame.add(button);

            } else if (hasWord("frame") && has("title")) {
                Frame[] frames = Frame.getFrames();
                for (Frame f : frames)
                    if (f instanceof CyderFrame) {
                        println(f.getTitle());
                    } else {
                        println(f.getTitle());
                    }
            }

            else {
                println("Sorry, " + ConsoleFrame.getUsername() + ", but I don't recognize that command." +
                        " You can make a suggestion by clicking the \"Suggest something\" button.");

                new Thread(() -> {
                    try {
                        ImageIcon blinkIcon = new ImageIcon("sys/pictures/suggestion2.png");
                        ImageIcon regularIcon = new ImageIcon("sys/pictures/suggestion1.png");

                        for (int i = 0 ; i < 4 ; i++) {
                            suggestionButton.setIcon(blinkIcon);
                            Thread.sleep(300);
                            suggestionButton.setIcon(regularIcon);
                            Thread.sleep(300);
                        }
                    } catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                }, "suggestionButton flash").start();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //input handler
    private boolean evaluateExpression(String userInput) {
        try {
            println(new DoubleEvaluator().evaluate(StringUtil.firstCharToLowerCase(userInput.trim())));
            return true;
        } catch (Exception ignored) {}

        return false;
    }

    //input handler
    private boolean handleMath(String userInput) {
        int firstParen = userInput.indexOf("(");
        int comma = userInput.indexOf(",");
        int lastParen = userInput.indexOf(")");

        String mathop;
        double param1 = 0.0;
        double param2 = 0.0;

        try {
            if (firstParen != -1) {
                mathop = userInput.substring(0, firstParen);

                if (comma != -1) {
                    param1 = Double.parseDouble(userInput.substring(firstParen + 1, comma));

                    if (lastParen != -1) {
                        param2 = Double.parseDouble(userInput.substring(comma + 1, lastParen));
                    }
                } else if (lastParen != -1) {
                    param1 = Double.parseDouble(userInput.substring(firstParen + 1, lastParen));
                }

                if (mathop.equalsIgnoreCase("abs")) {
                    println(Math.abs(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("ceil")) {
                    println(Math.ceil(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("floor")) {
                    println(Math.floor(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("log")) {
                    println(Math.log(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("log10")) {
                    println(Math.log10(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("max")) {
                    println(Math.max(param1, param2));
                    return true;
                } else if (mathop.equalsIgnoreCase("min")) {
                    println(Math.min(param1, param2));
                    return true;
                } else if (mathop.equalsIgnoreCase("pow")) {
                    println(Math.pow(param1, param2));
                    return true;
                } else if (mathop.equalsIgnoreCase("round")) {
                    println(Math.round(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("sqrt")) {
                    println(Math.sqrt(param1));
                    return true;
                } else if (mathop.equalsIgnoreCase("convert2")) {
                    println(Integer.toBinaryString((int) (param1)));
                    return true;
                }
            }
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }

        return false;
    }

    //handler method
    private void printlnImage(String filename) {
        outputArea.insertIcon(new ImageIcon(filename));
        println("");
    }

    //handler method
    public static void printImage(String filename) {
        outputArea.insertIcon(new ImageIcon(filename));
    }

    //handler method
    private void print(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage, null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private boolean eic(String EIC) {
        return operation.equalsIgnoreCase(EIC);
    }

    //handler method
    private boolean has(String compare) {
        String ThisComp = compare.toLowerCase();
        String ThisOp = operation.toLowerCase();

        return ThisOp.contains(ThisComp);
    }

    //handler method
    private boolean hasWord(String compare) {
        String ThisComp = compare.toLowerCase();
        String ThisOp = operation.toLowerCase();

        if (ThisOp.equals(ThisComp) || ThisOp.contains(' ' + ThisComp + ' ') || ThisOp.contains(' ' + ThisComp) || ThisOp.contains(ThisComp + ' '))
            return true;

        else return ThisOp.contains(ThisComp + ' ');
    }

    //console frame probably-------------------------------------------------------

    //Edit user vars
    private CyderFrame editUserFrame;
    private CyderScrollPane musicBackgroundScroll;
    private CyderButton addMusicBackground;
    private CyderButton openMusicBackground;
    private CyderButton deleteMusicBackground;
    private JList<?> musicBackgroundSelectionList;
    private List<String> musicBackgroundNameList;
    private List<File> musicBackgroundList;
    private CyderButton changeUsername;
    private CyderButton changePassword;
    private CyderButton forwardPanel;
    private CyderButton backwardPanel;
    private JLabel switchingPanel;
    private int prefsPanelIndex;

    public void editUser() {
        if (editUserFrame != null)
            editUserFrame.closeAnimation();

        editUserFrame = new CyderFrame(1000, 800, new ImageIcon(DEFAULT_BACKGROUND_PATH));
        editUserFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        editUserFrame.setTitle("Edit User");

        switchingPanel = new JLabel();
        switchingPanel.setForeground(new Color(255, 255, 255));
        switchingPanel.setBounds(140, 70, 720, 500);
        switchingPanel.setOpaque(true);
        switchingPanel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        switchingPanel.setBackground(new Color(255, 255, 255));
        editUserFrame.getContentPane().add(switchingPanel);

        switchToMusicAndBackgrounds();

        backwardPanel = new CyderButton("< Prev");
        backwardPanel.setBackground(CyderColors.regularRed);
        backwardPanel.setColors(CyderColors.regularRed);
        backwardPanel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        backwardPanel.setFont(CyderFonts.weatherFontSmall);
        backwardPanel.addActionListener(e -> lastEditUser());
        backwardPanel.setBounds(20, 380, 100, 40);
        editUserFrame.getContentPane().add(backwardPanel);

        forwardPanel = new CyderButton("Next >");
        forwardPanel.setBackground(CyderColors.regularRed);
        forwardPanel.setColors(CyderColors.regularRed);
        forwardPanel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        forwardPanel.setFont(CyderFonts.weatherFontSmall);
        forwardPanel.addActionListener(e -> nextEditUser());
        forwardPanel.setBounds(1000 - 120, 380, 100, 40);
        editUserFrame.getContentPane().add(forwardPanel);

        JTextField changeUsernameField = new JTextField(10);
        changeUsernameField.addActionListener(e -> changeUsername.doClick());
        changeUsernameField.setFont(CyderFonts.weatherFontSmall);
        changeUsernameField.setSelectionColor(CyderColors.selectionColor);
        changeUsernameField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changeUsernameField.setBounds(100, 700, 300, 40);
        editUserFrame.getContentPane().add(changeUsernameField);

        changeUsername = new CyderButton("Change Username");
        changeUsername.setBackground(CyderColors.regularRed);
        changeUsername.setColors(CyderColors.regularRed);
        changeUsername.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changeUsername.setFont(CyderFonts.weatherFontSmall);
        changeUsername.addActionListener(e -> {
            String newUsername = changeUsernameField.getText();
            if (!stringUtil.empytStr(newUsername)) {
                IOUtil.changeUsername(newUsername);
                editUserFrame.inform("Username successfully changed", "");
                consoleFrame.setTitle(IOUtil.getSystemData("Version") + " Cyder [" + newUsername + "]");
                changeUsernameField.setText("");
            }
        });
        changeUsername.setBounds(100, 750, 300, 40);
        editUserFrame.getContentPane().add(changeUsername);

        CyderButton deleteUser = new CyderButton("Delete User");
        deleteUser.setBackground(CyderColors.regularRed);
        deleteUser.setColors(CyderColors.regularRed);
        deleteUser.setBorder(new LineBorder(CyderColors.navy, 5, false));
        deleteUser.setFont(CyderFonts.weatherFontSmall);
        deleteUser.addActionListener(e -> {
            println("Are you sure you want to permanently delete this account? This action cannot be undone! (yes/no)");
            stringUtil.setUserInputMode(true);
            inputField.requestFocus();
            stringUtil.setUserInputDesc("deleteuser");
        });
        deleteUser.setBounds(425, 700, 150, 90);
        editUserFrame.getContentPane().add(deleteUser);

        JPasswordField changePasswordField = new JPasswordField(10);
        changePasswordField.addActionListener(e -> changePassword.doClick());
        changePasswordField.setFont(CyderFonts.weatherFontSmall);
        changePasswordField.setSelectionColor(CyderColors.selectionColor);
        changePasswordField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changePasswordField.setToolTipText("New password");
        changePasswordField.setBounds(600, 700, 300, 40);
        editUserFrame.getContentPane().add(changePasswordField);

        changePassword = new CyderButton("Change Password");
        changePassword.setBackground(CyderColors.regularRed);
        changePassword.setColors(CyderColors.regularRed);
        changePassword.setBorder(new LineBorder(CyderColors.navy, 5, false));
        changePassword.setFont(CyderFonts.weatherFontSmall);
        changePassword.addActionListener(e -> {
            char[] newPassword = changePasswordField.getPassword();

            if (newPassword.length > 4) {
                IOUtil.changePassword(newPassword);
                editUserFrame.inform("Password successfully changed", "");
                changePasswordField.setText("");
            } else {
                editUserFrame.inform("Sorry, " + ConsoleFrame.getUsername() + ", " +
                        "but your password must be greater than 4 characters for security reasons.", "");
                changePasswordField.setText("");
            }

            for (char c : newPassword) {
                c = '\0';
            }
        });
        changePassword.setBounds(600, 750, 300, 40);
        editUserFrame.getContentPane().add(changePassword);

        editUserFrame.setLocationRelativeTo(null);
        editUserFrame.setVisible(true);
        editUserFrame.requestFocus();
        editUserFrame.setAlwaysOnTop(true);
    }

    public void initMusicBackgroundList() {
        File backgroundDir = new File("users/" + ConsoleFrame.getUUID() + "/Backgrounds");
        File musicDir = new File("users/" + ConsoleFrame.getUUID() + "/Music");

        musicBackgroundList = new LinkedList<>();
        musicBackgroundNameList = new LinkedList<>();

        for (File file : backgroundDir.listFiles()) {
            if (file.getName().endsWith((".png"))) {
                musicBackgroundList.add(file.getAbsoluteFile());
                musicBackgroundNameList.add(file.getName().replace(".png", ""));
            }
        }

        for (File file : musicDir.listFiles()) {
            if (file.getName().endsWith((".mp3"))) {
                musicBackgroundList.add(file.getAbsoluteFile());
                musicBackgroundNameList.add(file.getName().replace(".mp3", ""));
            }
        }

        String[] BackgroundsArray = new String[musicBackgroundNameList.size()];
        BackgroundsArray = musicBackgroundNameList.toArray(BackgroundsArray);

        musicBackgroundSelectionList = new JList(BackgroundsArray);
        musicBackgroundSelectionList.setFont(CyderFonts.weatherFontSmall);
        musicBackgroundSelectionList.setForeground(CyderColors.navy);
        musicBackgroundSelectionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && musicBackgroundSelectionList.getSelectedIndex() != -1) {
                    openMusicBackground.doClick();
                }
            }
        });

        musicBackgroundSelectionList.setSelectionBackground(CyderColors.selectionColor);
    }

    private void nextEditUser() {
        switchingPanel.removeAll();
        switchingPanel.revalidate();
        switchingPanel.repaint();
        editUserFrame.revalidate();
        editUserFrame.repaint();

        prefsPanelIndex++;

        if (prefsPanelIndex == 3)
            prefsPanelIndex = 0;

        switch (prefsPanelIndex) {
            case 0:
                switchToMusicAndBackgrounds();
                break;
            case 1:
                switchToFontAndColor();
                break;

            case 2:
                switchToPreferences();
                break;
        }
    }

    private void lastEditUser() {
        switchingPanel.removeAll();
        switchingPanel.revalidate();
        switchingPanel.repaint();
        editUserFrame.revalidate();
        editUserFrame.repaint();

        prefsPanelIndex--;

        if (prefsPanelIndex == -1)
            prefsPanelIndex = 2;

        switch (prefsPanelIndex) {
            case 0:
                switchToMusicAndBackgrounds();
                break;
            case 1:
                switchToFontAndColor();
                break;

            case 2:
                switchToPreferences();
                break;
        }
    }

    private void switchToMusicAndBackgrounds() {
        JLabel BackgroundLabel = new JLabel("Music & Backgrounds", SwingConstants.CENTER);
        BackgroundLabel.setFont(CyderFonts.weatherFontBig);
        BackgroundLabel.setBounds(720 / 2 - 375 / 2, 10, 375, 40);
        switchingPanel.add(BackgroundLabel);

        initMusicBackgroundList();

        musicBackgroundSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        musicBackgroundScroll = new CyderScrollPane(musicBackgroundSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        musicBackgroundScroll.setSize(400, 400);
        musicBackgroundScroll.setFont(CyderFonts.weatherFontBig);
        musicBackgroundScroll.setThumbColor(CyderColors.regularRed);
        musicBackgroundSelectionList.setBackground(new Color(255, 255, 255));
        musicBackgroundScroll.getViewport().setBackground(new Color(0, 0, 0, 0));
        musicBackgroundScroll.setBounds(20, 60, 680, 360);
        switchingPanel.add(musicBackgroundScroll);

        addMusicBackground = new CyderButton("Add");
        addMusicBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        addMusicBackground.setColors(CyderColors.regularRed);
        addMusicBackground.setFocusPainted(false);
        addMusicBackground.setBackground(CyderColors.regularRed);
        addMusicBackground.addActionListener(e -> {
            try {
                //if this is too small or big, where is it resized and why is it too big?
                File addFile = IOUtil.getFile();

                if (addFile == null)
                    return;

                Path copyPath = new File(addFile.getAbsolutePath()).toPath();

                if (addFile != null && addFile.getName().endsWith(".png")) {
                    File Destination = new File("users/" + ConsoleFrame.getUUID() + "/Backgrounds/" + addFile.getName());
                    Files.copy(copyPath, Destination.toPath());
                    initMusicBackgroundList();
                    musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                    musicBackgroundScroll.revalidate();
                } else if (addFile != null && addFile.getName().endsWith(".mp3")) {
                    File Destination = new File("users/" + ConsoleFrame.getUUID() + "/Music/" + addFile.getName());
                    Files.copy(copyPath, Destination.toPath());
                    initMusicBackgroundList();
                    musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                    musicBackgroundScroll.revalidate();
                } else {
                    editUserFrame.inform("Sorry, " + ConsoleFrame.getUsername() + ", but you can only add PNGs and MP3s", "Error");
                }

                ConsoleFrame.resizeBackgrounds();
            } catch (Exception exc) {
                ErrorHandler.handle(exc);
            }
        });
        addMusicBackground.setFont(CyderFonts.weatherFontSmall);
        addMusicBackground.setBounds(20, 440, 213, 40);
        switchingPanel.add(addMusicBackground);

        openMusicBackground = new CyderButton("Open");
        openMusicBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        openMusicBackground.setColors(CyderColors.regularRed);
        openMusicBackground.setFocusPainted(false);
        openMusicBackground.setBackground(CyderColors.regularRed);
        openMusicBackground.setFont(CyderFonts.weatherFontSmall);
        openMusicBackground.addActionListener(e -> {
            List<?> ClickedSelectionList = musicBackgroundSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < musicBackgroundNameList.size(); i++) {
                    if (ClickedSelection.equals(musicBackgroundNameList.get(i))) {
                        ClickedSelectionPath = musicBackgroundList.get(i);
                        break;
                    }
                }

                if (ClickedSelectionPath != null) {
                    if (ClickedSelectionPath.getName().endsWith(".png")) {
                        PhotoViewer pv = new PhotoViewer(ClickedSelectionPath);
                        pv.start();
                    } else if (ClickedSelectionPath.getName().endsWith(".mp3")) {
                        IOUtil.mp3(ClickedSelectionPath.toString());
                    }
                }
            }
        });
        openMusicBackground.setBounds(20 + 213 + 20, 440, 213, 40);
        switchingPanel.add(openMusicBackground);

        deleteMusicBackground = new CyderButton("Delete");
        deleteMusicBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        deleteMusicBackground.setColors(CyderColors.regularRed);
        deleteMusicBackground.addActionListener(e -> {
            if (!musicBackgroundSelectionList.getSelectedValuesList().isEmpty()) {
                List<?> ClickedSelectionListMusic = musicBackgroundSelectionList.getSelectedValuesList();

                File ClickedSelectionPath = null;

                if (!ClickedSelectionListMusic.isEmpty()) {
                    String ClickedSelection = ClickedSelectionListMusic.get(0).toString();

                    for (int i = 0; i < musicBackgroundNameList.size(); i++) {
                        if (ClickedSelection.equals(musicBackgroundNameList.get(i))) {
                            ClickedSelectionPath = musicBackgroundList.get(i);

                            break;
                        }
                    }

                    if (ClickedSelection.equalsIgnoreCase(ConsoleFrame.getCurrentBackgroundFile().getName().replace(".png", "")))
                        editUserFrame.inform("Unable to delete the background you are currently using", "Error");

                    else {
                        ClickedSelectionPath.delete();
                        initMusicBackgroundList();
                        musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                        musicBackgroundScroll.revalidate();

                        if (ClickedSelection.endsWith(".mp3"))
                            println("Music: " + ClickedSelectionPath.getName().replace(".mp3", "") + " successfully deleted.");
                        else if (ClickedSelection.endsWith(".png")) {
                            println("Background: " + ClickedSelectionPath.getName().replace(".png", "") + " successfully deleted.");

                            LinkedList<File> paths = ConsoleFrame.getBackgrounds();
                            for (int i = 0; i < paths.size(); i++) {
                                if (paths.get(i).equals(ConsoleFrame.getCurrentBackgroundFile())) {
                                    ConsoleFrame.setBackgroundIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        deleteMusicBackground.setBackground(CyderColors.regularRed);
        deleteMusicBackground.setFont(CyderFonts.weatherFontSmall);
        deleteMusicBackground.setBounds(20 + 213 + 20 + 213 + 20, 440, 213, 40);
        switchingPanel.add(deleteMusicBackground);

        //todo add a button to rename the selected content in the row with open, delete, and add

        switchingPanel.revalidate();
    }

    private void switchToFontAndColor() {
        JLabel TitleLabel = new JLabel("Colors & Font", SwingConstants.CENTER);
        TitleLabel.setFont(CyderFonts.weatherFontBig);
        TitleLabel.setBounds(720 / 2 - 375 / 2, 10, 375, 40);
        switchingPanel.add(TitleLabel);

        int colorOffsetX = 340;
        int colorOffsetY = 10;

        JLabel ColorLabel = new JLabel("Text Color");
        ColorLabel.setFont(CyderFonts.weatherFontBig);
        ColorLabel.setForeground(CyderColors.navy);
        ColorLabel.setBounds(120 + colorOffsetX, 50 + colorOffsetY, 300, 30);
        switchingPanel.add(ColorLabel);

        JLabel hexLabel = new JLabel("HEX:");
        hexLabel.setFont(CyderFonts.weatherFontSmall);
        hexLabel.setForeground(CyderColors.navy);
        hexLabel.setBounds(30 + colorOffsetX, 110 + colorOffsetY, 70, 30);
        switchingPanel.add(hexLabel);

        JLabel rgbLabel = new JLabel("RGB:");
        rgbLabel.setFont(CyderFonts.weatherFontSmall);
        rgbLabel.setForeground(CyderColors.navy);
        rgbLabel.setBounds(30 + colorOffsetX, 180 + colorOffsetY, 70, 30);
        switchingPanel.add(rgbLabel);

        JTextField colorBlock = new JTextField();
        colorBlock.setBackground(CyderColors.navy);
        colorBlock.setFocusable(false);
        colorBlock.setCursor(null);
        colorBlock.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground")));
        colorBlock.setToolTipText("Color Preview");
        colorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        colorBlock.setBounds(330 + colorOffsetX, 100 + colorOffsetY, 40, 120);
        switchingPanel.add(colorBlock);

        JTextField rgbField = new JTextField(CyderColors.navy.getRed() + "," + CyderColors.navy.getGreen() + "," + CyderColors.navy.getBlue());

        JTextField hexField = new JTextField(IOUtil.getUserData("Foreground"));
        hexField.setForeground(CyderColors.navy);
        hexField.setFont(CyderFonts.weatherFontBig);
        hexField.setBackground(new Color(0, 0, 0, 0));
        hexField.setSelectionColor(CyderColors.selectionColor);
        hexField.setToolTipText("Hex Value");
        hexField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        JTextField finalHexField1 = hexField;
        JTextField finalRgbField = rgbField;
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            try {
                Color c = ColorUtil.hextorgbColor(finalHexField1.getText());
                finalRgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
                colorBlock.setBackground(c);
            } catch (Exception ignored) {}
            }
        });
        hexField.setBounds(100 + colorOffsetX, 100 + colorOffsetY, 220, 50);
        hexField.setOpaque(false);
        switchingPanel.add(hexField);

        rgbField.setForeground(CyderColors.navy);
        rgbField.setFont(CyderFonts.weatherFontBig);
        rgbField.setBackground(new Color(0, 0, 0, 0));
        rgbField.setSelectionColor(CyderColors.selectionColor);
        rgbField.setToolTipText("RGB Value");
        Color c = ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground"));
        rgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
        rgbField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        JTextField finalRgbField1 = rgbField;
        rgbField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            try {
                String[] parts = finalRgbField1.getText().split(",");
                Color c = new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                hexField.setText(ColorUtil.rgbtohexString(c));
                colorBlock.setBackground(c);
            } catch (Exception e) {
                ErrorHandler.silentHandle(e);
            }
            }
        });
        rgbField.setBounds(100 + colorOffsetX, 170 + colorOffsetY, 220, 50);
        rgbField.setOpaque(false);
        switchingPanel.add(rgbField);

        CyderButton applyColor = new CyderButton("Apply Color");
        applyColor.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        applyColor.setColors(CyderColors.regularRed);
        applyColor.setToolTipText("Apply");
        applyColor.setFont(CyderFonts.weatherFontSmall);
        applyColor.setFocusPainted(false);
        applyColor.setBackground(CyderColors.regularRed);
        applyColor.addActionListener(e -> {
            IOUtil.writeUserData("Foreground", hexField.getText());

            Color updateC = ColorUtil.hextorgbColor(hexField.getText());

            outputArea.setForeground(updateC);
            inputField.setForeground(updateC);

            println("The Color [" + updateC.getRed() + "," + updateC.getGreen() + "," + updateC.getBlue() + "] has been applied.");
        });
        applyColor.setBounds(450, 240 + colorOffsetY, 200, 40);
        switchingPanel.add(applyColor);

        JLabel FillLabel = new JLabel("Fill Color");
        FillLabel.setFont(CyderFonts.weatherFontBig);
        FillLabel.setForeground(CyderColors.navy);
        FillLabel.setBounds(120 + colorOffsetX, 330 + colorOffsetY, 300, 30);
        switchingPanel.add(FillLabel);

        JLabel hexLabelFill = new JLabel("HEX:");
        hexLabelFill.setFont(CyderFonts.weatherFontSmall);
        hexLabelFill.setForeground(CyderColors.navy);
        hexLabelFill.setBounds(30 + colorOffsetX, 390 + colorOffsetY, 70, 30);
        switchingPanel.add(hexLabelFill);

        JTextField colorBlockFill = new JTextField();
        colorBlockFill.setBackground(CyderColors.navy);
        colorBlockFill.setFocusable(false);
        colorBlockFill.setCursor(null);
        colorBlockFill.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
        colorBlockFill.setToolTipText("Color Preview");
        colorBlockFill.setBorder(new LineBorder(CyderColors.navy, 5, false));
        colorBlockFill.setBounds(330 + colorOffsetX, 340 + colorOffsetY, 40, 120);
        switchingPanel.add(colorBlockFill);

        JTextField hexFieldFill = new JTextField(String.format("#%02X%02X%02X", CyderColors.navy.getRed(),
                CyderColors.navy.getGreen(), CyderColors.navy.getBlue()).replace("#", ""));

        hexFieldFill.setForeground(CyderColors.navy);
        hexFieldFill.setText(IOUtil.getUserData("Background"));
        hexFieldFill.setFont(CyderFonts.weatherFontSmall);
        hexFieldFill.setBackground(new Color(255, 255, 255));
        hexFieldFill.setSelectionColor(CyderColors.selectionColor);
        hexFieldFill.setToolTipText("Input field and output area fill color if enabled");
        hexFieldFill.setBorder(new LineBorder(CyderColors.navy, 5, false));
        hexFieldFill.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            try {
                colorBlockFill.setBackground(ColorUtil.hextorgbColor(hexFieldFill.getText()));
                IOUtil.writeUserData("Background", hexFieldFill.getText());
            } catch (Exception ignored) {
                if (hexFieldFill.getText().length() == 6)
                    editUserFrame.notify("Invalid color");
            }
            }
        });
        hexFieldFill.setBounds(100 + colorOffsetX, 380 + colorOffsetY, 220, 50);
        hexFieldFill.setOpaque(false);
        switchingPanel.add(hexFieldFill);

        JLabel FontLabel = new JLabel("Fonts");
        FontLabel.setFont(new Font(IOUtil.getUserData("Font"),Font.BOLD, 30));
        FontLabel.setForeground(CyderColors.navy);
        FontLabel.setBounds(150, 60, 300, 30);
        switchingPanel.add(FontLabel);

        String[] Fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        fontList = new JList(Fonts);
        fontList.setSelectionBackground(CyderColors.selectionColor);
        fontList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fontList.setFont(CyderFonts.weatherFontSmall);

        for (int i = 0 ; i < Fonts.length ; i++)
            if (Fonts[i].equals(IOUtil.getUserData("Font")))
                fontList.setSelectedIndex(i);


        CyderScrollPane FontListScroll = new CyderScrollPane(fontList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        FontListScroll.setThumbColor(CyderColors.intellijPink);
        FontListScroll.setBorder(new LineBorder(CyderColors.navy, 5, true));

        CyderButton applyFont = new CyderButton("Apply Font");
        applyFont.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        applyFont.setColors(CyderColors.regularRed);
        applyFont.setToolTipText("Apply");
        applyFont.setFont(CyderFonts.weatherFontSmall);
        applyFont.setFocusPainted(false);
        applyFont.setBackground(CyderColors.regularRed);
        applyFont.addActionListener(e -> {
            String FontS = (String) fontList.getSelectedValue();

            if (FontS != null) {
                Font ApplyFont = new Font(FontS, Font.BOLD, 30);
                outputArea.setFont(ApplyFont);
                inputField.setFont(ApplyFont);
                IOUtil.writeUserData("Font", FontS);
                println("The font \"" + FontS + "\" has been applied.");
            }
        });
        applyFont.setBounds(100, 420, 200, 40);
        switchingPanel.add(applyFont);

        fontList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    applyFont.doClick();
                } else {
                    try {
                        FontLabel.setFont(new Font(fontList.getSelectedValue().toString(), Font.BOLD, 30));
                    } catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                }
            }
        });

        fontList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                JList t = (JList) e.getSource();
                int index = t.locationToIndex(e.getPoint());

                FontLabel.setFont(new Font(t.getModel().getElementAt(index).toString(), Font.BOLD, 30));
            }
        });

        FontListScroll.setBounds(50, 100, 300, 300);
        switchingPanel.add(FontListScroll, Component.CENTER_ALIGNMENT);

        switchingPanel.revalidate();
    }

    //ignoreing first two: name, password
    private final String[] prefIDS = {"intromusic",
            "debugwindows","randombackground","outputborder","inputborder","hourlychimes","silenceerrors",
            "fullscreen","outputfill","inputfill","clockonconsole","showseconds"};

    private final String[] prefNames = {"Intro Music",
            "Debug Windows","Random Background","Output Border","Input Border","Hourly Chimes","Silence Errors",
            "Fullscreen","Output Fill","Input Fill","Clock On Console","Show Seconds"};

    private final String[] prefToolTips = {"Play intro music on start",
            "Show debug menus on startup","Choose a random background on startup",
            "Draw a border around the output area","Draw a border around the input area",
            "Chime every hour","Don't open errors externally",
            "Fullscreen cyder (Extremely experimental)","Fill the output area with the color specified in the \"Fonts & Colors\" panel",
            "Fill the input area with the color specified in the \"Fonts & Colors\" panel",
            "Show a clock at the top of the console","Show seconds on the console clock if enabled"};


    //todo utilize this for creating user
    //todo toggle chat filter pref
    //todo toggle for menu animation slide up or left

    //todo corrupted users aren't saved to downloads, saved to directory up, should save to same dir as src, fix

    private void switchToPreferences() {
        //switchingpanel is a label
        JPanel preferencePanel = new JPanel();
        preferencePanel.setLayout(new BoxLayout(preferencePanel,BoxLayout.Y_AXIS));
        preferencePanel.setBounds(0,0,720,500);

        CyderLabel prefsTitle = new CyderLabel("Preferences");
        prefsTitle.setFont(CyderFonts.weatherFontBig);
        preferencePanel.add(prefsTitle);

        for (int i = 0 ; i < prefIDS.length ; i++) {
            CyderLabel preferenceLabel = new CyderLabel(prefNames[i]);
            preferenceLabel.setForeground(IOUtil.getUserData(prefIDS[i]).equals("1") ? CyderColors.regularRed : CyderColors.navy);
            preferenceLabel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
            preferenceLabel.setToolTipText(prefToolTips[i]);
            preferenceLabel.setFont(CyderFonts.defaultFontSmall);
            preferencePanel.add(preferenceLabel);

            int localIndex = i;

            preferenceLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    boolean wasSelected = IOUtil.getUserData((prefIDS[localIndex])).equalsIgnoreCase("1");
                    IOUtil.writeUserData(prefIDS[localIndex], wasSelected ? "0" : "1");

                    preferenceLabel.setForeground(
                            wasSelected ? CyderColors.navy : CyderColors.regularRed);

                    refreshPrefs();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    preferenceLabel.setForeground(
                            IOUtil.getUserData(prefIDS[localIndex]).equalsIgnoreCase("1") ?
                                    CyderColors.navy : CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    preferenceLabel.setForeground(
                            IOUtil.getUserData(prefIDS[localIndex]).equalsIgnoreCase("0") ?
                                    CyderColors.navy : CyderColors.regularRed);
                }
            });
        }

        CyderScrollPane preferenceScroll = new CyderScrollPane(preferencePanel);
        preferenceScroll.setBorder(BorderFactory.createLineBorder(CyderColors.navy, 5));
        preferenceScroll.setBounds(0,0,720,500);
        switchingPanel.add(preferenceScroll, SwingConstants.CENTER);

        switchingPanel.revalidate();
    }

    //todo individual functions to pass specifically singular pref change
    //todo sort out bug where console frame flashes and position is set to null
    public void refreshPrefs() {
        if (IOUtil.getUserData("OutputBorder").equals("0"))
            outputScroll.setBorder(BorderFactory.createEmptyBorder());
        else
            outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")), 3, true));

        if (IOUtil.getUserData("InputBorder").equals("0"))
            inputField.setBorder(BorderFactory.createEmptyBorder());
        else
            inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")), 3, true));

        if (IOUtil.getUserData("FullScreen").equals("0") && ConsoleFrame.isFullscreen())
            exitFullscreen();
        else if (IOUtil.getUserData("FullScreen").equals("1"))
            refreshConsoleFrame();

        consoleClockLabel.setVisible(IOUtil.getUserData("ClockOnConsole").equals("1"));

        if (consoleClockLabel.isVisible())
            updateConsoleClock = true;

        if (consoleClockLabel.isVisible()) {
            if (IOUtil.getUserData("ShowSeconds").equals("1"))
                consoleClockLabel.setText(TimeUtil.consoleSecondTime());
            else
                consoleClockLabel.setText(TimeUtil.consoleTime());
        }

        if (IOUtil.getUserData("ShowSeconds").equals("0"))
            consoleClockLabel.setText(TimeUtil.consoleTime());
        else
            consoleClockLabel.setText(TimeUtil.consoleSecondTime());

        if (IOUtil.getUserData("OutputFill").equals("0")) {
            outputArea.setBackground(null);
            outputArea.setOpaque(false);
        } else {
            outputArea.setOpaque(true);
            outputArea.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
            outputArea.repaint();
            outputArea.revalidate();
        }

        if (IOUtil.getUserData("InputFill").equals("0")) {
            inputField.setBackground(null);
            inputField.setOpaque(false);
        } else {
            inputField.setOpaque(true);
            inputField.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
            inputField.repaint();
            inputField.revalidate();
        }

        consoleFrame.repaint();
    }

    //todo convert repeated paths to string contants

    //CreateUser class in genesis
    public void createUser() {
        createUserBackground = null;

        if (createUserFrame != null)
            createUserFrame.closeAnimation();

        createUserFrame = new CyderFrame(356, 473, new ImageIcon(DEFAULT_BACKGROUND_PATH));
        createUserFrame.setTitle("Create User");

        JLabel NameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        NameLabel.setFont(CyderFonts.weatherFontSmall);
        NameLabel.setBounds(120, 30, 121, 30);
        createUserFrame.getContentPane().add(NameLabel);

        newUserName = new JTextField(15);
        newUserName.setSelectionColor(CyderColors.selectionColor);
        newUserName.setFont(CyderFonts.weatherFontSmall);
        newUserName.setForeground(CyderColors.navy);
        newUserName.setFont(CyderFonts.weatherFontSmall);
        newUserName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (newUserName.getText().length() > 15) {
                evt.consume();
            }
            }
        });

        newUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
        newUserName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }
        });

        newUserName.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserName.setBounds(60, 70, 240, 40);
        createUserFrame.getContentPane().add(newUserName);

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(CyderFonts.weatherFontSmall);
        passwordLabel.setForeground(CyderColors.navy);
        passwordLabel.setBounds(60, 120, 240, 30);
        createUserFrame.getContentPane().add(passwordLabel);

        JLabel matchPasswords = new JLabel("Passwords match", SwingConstants.CENTER);

        newUserPassword = new JPasswordField(15);
        newUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                    matchPasswords.setText("Passwords match");
                    matchPasswords.setForeground(CyderColors.regularGreen);
                } else {
                    matchPasswords.setText("Passwords do not match");
                    matchPasswords.setForeground(CyderColors.regularRed);
                }
            }
        });
        newUserPassword.setFont(CyderFonts.weatherFontSmall);
        newUserPassword.setForeground(CyderColors.navy);
        newUserPassword.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserPassword.setSelectedTextColor(CyderColors.selectionColor);
        newUserPassword.setBounds(60, 160, 240, 40);
        createUserFrame.getContentPane().add(newUserPassword);

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(CyderFonts.weatherFontSmall);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setBounds(60, 210, 240, 30);
        createUserFrame.getContentPane().add(passwordLabelConf);

        newUserPasswordconf = new JPasswordField(15);
        newUserPasswordconf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                    matchPasswords.setText("Passwords match");
                    matchPasswords.setForeground(CyderColors.regularGreen);
                } else {
                    matchPasswords.setText("Passwords do not match");
                    matchPasswords.setForeground(CyderColors.regularRed);
                }
            }
        });

        newUserPasswordconf.setFont(CyderFonts.weatherFontSmall);
        newUserPasswordconf.setForeground(CyderColors.navy);
        newUserPasswordconf.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserPasswordconf.setSelectedTextColor(CyderColors.selectionColor);
        newUserPasswordconf.setBounds(60, 250, 240, 40);
        createUserFrame.getContentPane().add(newUserPasswordconf);

        matchPasswords.setFont(CyderFonts.weatherFontSmall);
        matchPasswords.setForeground(CyderColors.regularGreen);
        matchPasswords.setBounds(32, 300, 300, 30);
        createUserFrame.getContentPane().add(matchPasswords);

        chooseBackground = new CyderButton("Choose background");
        chooseBackground.setToolTipText("ClickMe me to choose a background");
        chooseBackground.setFont(CyderFonts.weatherFontSmall);
        chooseBackground.setBackground(CyderColors.regularRed);
        chooseBackground.setColors(CyderColors.regularRed);
        chooseBackground.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    File temp = IOUtil.getFile();
                    if (temp != null) {
                        createUserBackground = temp;
                    }

                    if (temp != null && !Files.probeContentType(Paths.get(createUserBackground.getAbsolutePath())).endsWith("png")) {
                        createUserBackground = null;
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    if (createUserBackground != null) {
                        chooseBackground.setText(createUserBackground.getName());
                    } else {
                        chooseBackground.setToolTipText("No File Chosen");
                    }
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chooseBackground.setToolTipText("Choose background");
            }
        });

        chooseBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        chooseBackground.setBounds(60, 340, 240, 40);
        createUserFrame.getContentPane().add(chooseBackground);

        createNewUser = new CyderButton("Create User");
        createNewUser.setFont(CyderFonts.weatherFontSmall);
        createNewUser.setBackground(CyderColors.regularRed);
        createNewUser.setColors(CyderColors.regularRed);
        createNewUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            try {
                String uuid = SecurityUtil.generateUUID();
                File folder = new File("users/" + uuid);

                while (folder.exists()) {
                    uuid = SecurityUtil.generateUUID();
                    folder = new File("users/" + uuid);
                }

                char[] pass = newUserPassword.getPassword();
                char[] passconf = newUserPasswordconf.getPassword();

                boolean alreadyExists = false;
                File[] files = new File("users").listFiles();

                for (File f : files) {
                    File data = new File(f.getAbsolutePath() + "/Userdata.txt");
                    BufferedReader partReader = new BufferedReader(new FileReader(data));
                    String line = partReader.readLine();
                    while (line != null) {
                        String[] parts = line.split(":");
                        if (parts[0].equalsIgnoreCase("Name") && parts[1].equalsIgnoreCase(newUserName.getText().trim())) {
                            alreadyExists = true;
                            break;
                        }

                        line = partReader.readLine();
                    }

                    partReader.close();

                    if (alreadyExists) break;
                }

                if (stringUtil.empytStr(newUserName.getText()) || pass == null || passconf == null
                        || uuid.equals("") || pass.equals("") || passconf.equals("") || uuid.length() == 0) {
                    createUserFrame.inform("Sorry, but one of the required fields was left blank.\nPlease try again.", "");
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                } else if (alreadyExists) {
                    createUserFrame.inform("Sorry, but that username is already in use.\nPlease try a different one.", "");
                    newUserName.setText("");
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                } else if (!Arrays.equals(pass, passconf) && pass.length > 0) {
                    createUserFrame.inform("Sorry, but your passwords did not match. Please try again.", "");
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                } else if (pass.length < 5) {
                    createUserFrame.inform("Sorry, but your password length should be greater than\n"
                            + "four characters for security reasons. Please add more characters.", "");

                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                } else {
                    if (createUserBackground == null) {
                        createUserFrame.inform("No background image was chosen so we're going to give you a sweet one ;)", "No background");
                        createUserBackground = new File("sys/pictures/DefaultBackground.png");
                    }

                    File NewUserFolder = new File("users/" + uuid);
                    File backgrounds = new File("users/" + uuid + "/Backgrounds");
                    File music = new File("users/" + uuid + "/Music");
                    File notes = new File("users/" + uuid + "/Notes");

                    NewUserFolder.mkdirs();
                    backgrounds.mkdir();
                    music.mkdir();
                    notes.mkdir();

                    ImageIO.write(ImageIO.read(createUserBackground), "png",
                            new File("users/" + uuid + "/Backgrounds/" + createUserBackground.getName()));

                    BufferedWriter newUserWriter = new BufferedWriter(new FileWriter(
                            "users/" + uuid + "/Userdata.txt"));

                    //todo copy from a template here and replace the REPLACE keywords with username and password
                    LinkedList<String> data = new LinkedList<>();
                    data.add("Name:" + newUserName.getText().trim());
                    data.add("Password:" + SecurityUtil.toHexString(SecurityUtil.getSHA(pass)));

                    data.add("Font:tahoma");
                    data.add("Foreground:000000");
                    data.add("Background:FFFFFF");

                    data.add("IntroMusic:0");
                    data.add("DebugWindows:0");
                    data.add("RandomBackground:0");
                    data.add("OutputBorder:0");
                    data.add("InputBorder:0");

                    data.add("HourlyChimes:1");
                    data.add("SilenceErrors:1");
                    data.add("FullScreen:0");
                    data.add("OutputFill:0");
                    data.add("InputFill:0");

                    data.add("ClockOnConsole:1");
                    data.add("ShowSeconds:1");

                    for (String d : data) {
                        newUserWriter.write(d);
                        newUserWriter.newLine();
                    }

                    newUserWriter.close();

                    createUserFrame.closeAnimation();

                    createUserFrame.inform("The new user \"" + newUserName.getText().trim() + "\" has been created successfully.", "");

                    createUserFrame.closeAnimation();

                    if ((!consoleFrame.isVisible() && loginFrame != null) || (new File("users/").length() == 1)) {
                        loginFrame.closeAnimation();
                        recognize(newUserName.getText().trim(), pass);
                    }
                }

                for (char c : pass)
                    c = '\0';

                for (char c : passconf)
                    c = '\0';
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
            }
        });

        createNewUser.setBorder(new LineBorder(CyderColors.navy, 5, false));
        createNewUser.setFont(CyderFonts.weatherFontSmall);
        createNewUser.setBounds(60, 390, 240, 40);
        createUserFrame.getContentPane().add(createNewUser);

        JFrame relativeFrame = (loginFrame != null && loginFrame.isActive() && loginFrame.isVisible() ?
                loginFrame : (consoleFrame != null && consoleFrame.isActive() && consoleFrame.isVisible() ?
                consoleFrame : null));

        createUserFrame.setLocationRelativeTo(relativeFrame);
        createUserFrame.setVisible(true);
        newUserName.requestFocus();
    }

    //console frame
    private void minimizeMenu() {
        if (menuLabel.isVisible()) {
            AnimationUtil.jLabelXLeft(0, -150, 10, 8, menuLabel);

            Thread waitThread = new Thread(() -> {
                try {
                    //todo make this number dynamic
                    Thread.sleep(186);
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }

                menuLabel.setVisible(false);
                menuButton.setIcon(new ImageIcon("sys/pictures/menuSide1.png"));
            });
            waitThread.start();
        }
    }

    //should be in MasterYoutube class
    private void killAllYoutube() {
        for (YoutubeThread ytt : youtubeThreads)
            ytt.kill();
    }

    /**
     * Exiting method, stuff that you should do before exiting should go here. Stuff fatal to program exeuction, however,
     * should be placed in {@link CyderMain#shutdown()} which is what the shutdown hook calls
     */
    private void exit() {
        IOUtil.readUserData();
        IOUtil.writeUserData("Fonts", outputArea.getFont().getName());
        IOUtil.writeUserData("Foreground", ColorUtil.rgbtohexString(outputArea.getForeground()));

        AnimationUtil.closeAnimation(consoleFrame);
        killAllYoutube();
        stringUtil.killBletchy();

        try {
            CyderMain.exitingSem.acquire();
            //we never release exiting sem since we are expecting to exit the program
            System.exit(25);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * This is called from the shutdown hook, things imperitive to do
     * no matter what, before we close, System.exit has already been called here
     * so you shouldn't do any reading or writing to files or anything with locks/semaphores
     */
    private void shutdown() {
        //delete temp dir
        IOUtil.deleteTempDir();
        //delete all getter files
        //todo move these to tmp dir
        new File("InputMessage.txt").delete();
        new File("File.txt").delete();
        new File("String.txt").delete();
    }
}