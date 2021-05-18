package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderImages;
import cyder.enums.*;
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
import java.util.LinkedList;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static cyder.constants.CyderStrings.DEFAULT_BACKGROUND_PATH;

//bug: log in from user as a different user then delete that second user
// it will use the first user's name and say that it was corrupted

/*
    Commenting etiquette I will follow (attempt to that is)

    -todos shall be placed where I deem them  most logical such as a to-do to fix a
     certain function shall be placed above that function

    -helpful comments shall be placed anywhere if something is not self explanatory

    -method @params @args @retuns, etc. shall be placed on most methods

    -generic features I plan to implement at some point will go under the to-do section of the readme
 */

public class CyderMain {
    //todo shared package
    public static Semaphore exitingSem;

    //specific to an instance of a handler method
    private LinkedList<YoutubeThread> youtubeThreads = new LinkedList<>();

    //todo login spins off of main of autocypher fails
    private static CyderFrame loginFrame;
    private static JPasswordField loginField;
    private static int loginMode;
    private String username;
    private char[] password;

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

    public static void main(String[] CA) {
        new CyderMain(CA);
    }

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
            File Users = new File("src/users/");

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
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            outputScroll.setThumbColor(CyderColors.intellijPink);
            outputScroll.getViewport().setOpaque(false);
            outputScroll.setOpaque(false);

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
                        ConsoleFrame.setConsoleDirection(ConsoleDirection.DOWN);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_RIGHT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.setConsoleDirection(ConsoleDirection.RIGHT);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_UP) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.setConsoleDirection(ConsoleDirection.UP);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_LEFT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.setConsoleDirection(ConsoleDirection.LEFT);
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

            inputField.setCaretColor(CyderColors.vanila);

            IOUtil.readUserData();

            inputField.setForeground(ConsoleFrame.getUserColor());
            outputArea.setForeground(ConsoleFrame.getUserColor());

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
                println("What feature would you like to suggestion? (Please include as much detail as possible such as what" +
                        "key words you should type and how it should be responded to and any options you think might be necessary)");
                stringUtil.setUserInputDesc("suggestion");
                stringUtil.setUserInputMode(true);
                inputField.requestFocus();
            });

            suggestionButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("src/cyder/sys/pictures/suggestion2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("src/cyder/sys/pictures/suggestion1.png"));
                }
            });

            suggestionButton.setBounds(32, 4, 22, 22);

            ImageIcon DebugIcon = new ImageIcon("src/cyder/sys/pictures/suggestion1.png");

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

            ImageIcon MenuIcon = new ImageIcon("src/cyder/sys/pictures/menuSide1.png");

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
                    alternateBackground.setIcon(new ImageIcon("src/cyder/sys/pictures/ChangeSize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("src/cyder/sys/pictures/ChangeSize1.png"));
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
            alternateBackground.setIcon(new ImageIcon("src/cyder//sys/pictures/ChangeSize1.png"));
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
                            CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) / 2 - 13,
                    2, CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) + 26, 25);

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
                    IOUtil.playAudio("src/cyder//sys/audio/chime.mp3");

            }, 3600 - LocalDateTime.now().getSecond() - LocalDateTime.now().getMinute() * 60, 3600, TimeUnit.SECONDS);

            parentLabel.add(consoleDragLabel);

            consoleFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeiconified(WindowEvent e) {
                    updateConsoleClock = true;
                    consoleFrame.setLocation(restoreX, restoreY);
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
                                        CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) / 2 - 13,
                                2, CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) + 26, 25);
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
                menuButton.setIcon(new ImageIcon("src/cyder//sys/pictures/menu2.png"));

                menuLabel = new JLabel("");
                menuLabel.setOpaque(true);
                menuLabel.setBackground(new Color(26, 32, 51));

                parentPane.add(menuLabel, 1, 0);

                menuLabel.setBounds(-150, 30, 130, 290);
                menuLabel.setVisible(true);

                JLabel calculatorLabel = new JLabel("Calculator");
                calculatorLabel.setFont(CyderFonts.weatherFontSmall);
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
                calculatorLabel.setBounds(5, 20, 150, 20);

                JLabel musicLabel = new JLabel("Music");
                musicLabel.setFont(CyderFonts.weatherFontSmall);
                musicLabel.setForeground(CyderColors.vanila);
                musicLabel.setBounds(5, 50, 150, 20);
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
                weatherLabel.setFont(CyderFonts.weatherFontSmall);
                weatherLabel.setForeground(CyderColors.vanila);
                menuLabel.add(weatherLabel);
                weatherLabel.setBounds(5, 80, 150, 20);
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
                noteLabel.setFont(CyderFonts.weatherFontSmall);
                noteLabel.setForeground(CyderColors.vanila);
                menuLabel.add(noteLabel);
                noteLabel.setBounds(5, 110, 150, 20);
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
                editUserLabel.setFont(CyderFonts.weatherFontSmall);
                editUserLabel.setForeground(CyderColors.vanila);
                menuLabel.add(editUserLabel);
                editUserLabel.setBounds(5, 140, 150, 20);
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
                temperatureLabel.setFont(CyderFonts.weatherFontSmall);
                temperatureLabel.setForeground(CyderColors.vanila);
                menuLabel.add(temperatureLabel);
                temperatureLabel.setBounds(5, 170, 150, 20);
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
                youtubeLabel.setFont(CyderFonts.weatherFontSmall);
                youtubeLabel.setForeground(CyderColors.vanila);
                menuLabel.add(youtubeLabel);
                youtubeLabel.setBounds(5, 200, 150, 20);
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
                twitterLabel.setFont(CyderFonts.weatherFontSmall);
                twitterLabel.setForeground(CyderColors.vanila);
                menuLabel.add(twitterLabel);
                twitterLabel.setBounds(5, 230, 150, 20);
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
                logoutLabel.setFont(CyderFonts.weatherFontSmall);
                logoutLabel.setForeground(CyderColors.vanila);
                menuLabel.add(logoutLabel);
                logoutLabel.setBounds(5, 255, 150, 30);
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

                AnimationUtil.jLabelXRight(-150, 0, 10, 8, menuLabel);
            } else if (menuLabel.isVisible()) {
                minimizeMenu();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src/cyder//sys/pictures/menu2.png"));
            } else {
                menuButton.setIcon(new ImageIcon("src/cyder//sys/pictures/menuSide2.png"));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src/cyder//sys/pictures/menu1.png"));
            } else {
                menuButton.setIcon(new ImageIcon("src/cyder//sys/pictures/menuSide1.png"));
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
                                IOUtil.playAudio("src/cyder//sys/audio/f17.mp3");
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
    // will this work for multiple things on the same day?
    //todo consolidate with console frame in one time run method
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

    //todo move to consoleFrame
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

    //todo if we add to an empty queue, it won't get printed

    private void typingPrint(String print, JTextPane refArea) {
        try {
            StyledDocument document = (StyledDocument) refArea.getDocument();
            document.insertString(document.getLength(), print, null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private LinkedList<String> printingList = new LinkedList<>();

    private void typing(JTextPane refArea) {
        SimpleDateFormat versionFormatter = new SimpleDateFormat("MM.dd.yy");
        printingList.add("Cyder version: " + versionFormatter.format(new Date()) + "\n");
        printingList.add("Type \"h\" for a list of valid commands\n");
        printingList.add("Build: Soultree\n");
        printingList.add("Author: Nathan Cheshire\n");
        printingList.add("Design OS: Windows 10+\n");
        printingList.add("Design JVM: 8+\n");
        printingList.add("Description: A programmer's swiss army knife\n");

        int charTimeout = 40;
        int lineTimeout = 1800;

        try {
            new Thread(() -> {
                try {
                    while (!printingList.isEmpty()) {
                        String line = printingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            typingPrint(String.valueOf(c), refArea);
                            Thread.sleep(charTimeout);
                        }

                        Thread.sleep(lineTimeout);
                    }
                }

                catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            },"login animation").start();
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    protected final void login() {
        loginMode = 0;

        if (loginFrame != null)
            loginFrame.closeAnimation();

        IOUtil.cleanUpUsers();

        loginFrame = new CyderFrame(400, 400, new ImageIcon("src/cyder/sys/pictures/login.png"));
        loginFrame.setTitlePosition(TitlePosition.LEFT);
        loginFrame.setTitle(IOUtil.getSystemData("Version") + " login");
        loginFrame.setBackground(new Color(21,23,24));

        if (consoleFrame == null || !consoleFrame.isActive() || !consoleFrame.isVisible())
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        else
            loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextPane loginArea = new JTextPane();
        loginArea.setBounds(20, 40, 360, 280);
        loginArea.setBackground(new Color(21,23,24));
        loginArea.setBorder(null);
        loginArea.setFocusable(false);
        loginArea.setFont(new Font("Agency FB",Font.BOLD, 26));
        loginArea.setForeground(new Color(85,181,219));
        loginArea.setCaretColor(new Color(85,181,219));

        CyderScrollPane loginScroll = new CyderScrollPane(loginArea,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        loginScroll.setThumbColor(CyderColors.intellijPink);
        loginScroll.setBounds(20, 40, 360, 280);
        loginScroll.getViewport().setOpaque(false);
        loginScroll.setOpaque(false);
        loginScroll.setBorder(null);

        loginFrame.getContentPane().add(loginScroll);

        typing(loginArea);

        loginField = new JPasswordField(20);
        loginField.setEchoChar((char)0);
        loginField.setBounds(20, 340, 360, 40);
        loginField.setBackground(new Color(21,23,24));
        loginField.setBorder(null);
        loginField.setSelectionColor(CyderColors.selectionColor);
        loginField.setFont(new Font("Agency FB",Font.BOLD, 26));
        loginField.setForeground(new Color(85,181,219));
        loginField.setCaretColor(new Color(85,181,219));
        loginField.addActionListener(e -> loginField.requestFocusInWindow());
        loginField.addKeyListener(new KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (evt.getKeyChar() == '\n') {
                char[] input = loginField.getPassword();
                switch (loginMode) {
                    case 0:
                        try {
                            if (Arrays.equals(input,"create".toCharArray())) {
                                createUser();
                                loginField.setText("");
                                loginMode = 0;
                            } else if (Arrays.equals(input,"login".toCharArray())) {
                                loginField.setText("");
                                printingList.addFirst("Awaiting Username\n");
                                loginMode = 1;
                            } else if (Arrays.equals(input,"login admin".toCharArray())) {
                                loginField.setText("");
                                printingList.addFirst("Feature not yet implemented\n");
                                loginMode = 0;
                            } else if (Arrays.equals(input,"quit".toCharArray())) {
                                loginFrame.closeAnimation();
                            } else if (Arrays.equals(input,"h".toCharArray())) {
                                loginField.setText("");
                                printingList.addFirst("Valid commands: create, login, login admin, quit, h\n");
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
                        StyledDocument document = (StyledDocument) loginArea.getDocument();
                        try {
                            printingList.addFirst("Awaiting Password\n");
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }
                        outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        break;

                    case 2:
                        password = input;
                        loginField.setText("");
                        recognize(username,password);

                        if (password != null)
                            for (char c: password)
                                c = '\0';

                        loginMode = 0;
                        break;

                    default:
                        try {
                            throw new FatalException("Error resulting from login shell");
                        } catch (FatalException e) {
                            ErrorHandler.handle(e);
                        }
                }
            }
            }
        });

        loginFrame.getContentPane().add(loginField);

        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                loginField.requestFocus();
            }
        });

        File Users = new File("src/users/");
        String[] directories = Users.list((current, name) -> new File(current, name).isDirectory());

        loginFrame.setVisible(true);
        loginFrame.enterAnimation();

        if (directories != null && directories.length == 0)
            loginFrame.notify("<html><b>" + System.getProperty("user.name")
                            + ":<br/>There are no users<br/>please create one</b></html>",
                    4000, ArrowDirection.TOP);
    }

    private void recognize(String Username, char[] Password) {
        try {
            if (SecurityUtil.checkPassword(Username, SecurityUtil.toHexString(SecurityUtil.getSHA(Password)))) {
                IOUtil.readUserData();

                if (loginFrame != null)
                    loginFrame.closeAnimation();

                if (consoleFrame != null)
                    AnimationUtil.closeAnimation(consoleFrame);

                console();

                //this if block needs to be in console, stuff to do specifically for user on first login
                if (IOUtil.getUserData("IntroMusic").equals("1")) {
                    LinkedList<String> MusicList = new LinkedList<>();

                    File UserMusicDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Music");

                    String[] FileNames = UserMusicDir.list();

                    if (FileNames != null)
                        for (String fileName : FileNames)
                            if (fileName.endsWith(".mp3"))
                                MusicList.add(fileName);

                    if (!MusicList.isEmpty())
                        IOUtil.playAudio(
                                "src/users/" + ConsoleFrame.getUUID() + "/Music/" +
                                        (FileNames[NumberUtil.randInt(0, FileNames.length - 1)]));
                    else
                        IOUtil.playAudio("src/cyder//sys/audio/Suprise.mp3");
                }
            } else if (loginFrame != null && loginFrame.isVisible()) {
                loginField.setText("");

                for (char c: password)
                    c = '\0';
                username = "";

                loginField.requestFocusInWindow();
                loginFrame.notify("Could not recognize user",
                        2000, ArrowDirection.TOP, StartDirection.TOP, VanishDirection.TOP);
                loginField.setEchoChar((char)0);
            } else
                login();
        } catch (Exception e) {
            ErrorHandler.handle(e); //todo system error handle
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
            case UP:
                backIcon = new ImageIcon(backFile);
                width = backIcon.getIconWidth();
                height = backIcon.getIconHeight();
                parentLabel.setIcon(backIcon);

                break;
            case DOWN:
                backIcon = new ImageIcon(backFile);
                width = backIcon.getIconWidth();
                height = backIcon.getIconHeight();
                parentLabel.setIcon(new ImageIcon(ImageUtil.getRotatedImage(ConsoleFrame.getCurrentBackgroundFile().toString(), ConsoleFrame.getConsoleDirection())));

                break;
            default:
                backIcon = new ImageIcon(backFile);

                if (ConsoleFrame.getConsoleDirection() == ConsoleDirection.LEFT || ConsoleFrame.getConsoleDirection() == ConsoleDirection.RIGHT) {
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
                        CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) / 2 - 13,
                2, CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) + 26, 25);

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
                        CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) / 2 - 13,
                2, CyderFrame.getTitleWidth(consoleClockLabel.getText(), consoleClockLabel.getFont()) + 26, 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();

        consoleFrame.setLocationRelativeTo(null);

        if (editUserFrame != null)
            editUserFrame.setAlwaysOnTop(true);
    }

    //todo move to input handler
    private void clc() {
        outputArea.setText("");
        inputField.setText("");
    }

    //todo move to input handler
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
                    String Print = NumberUtil.toBinary(Integer.parseInt(input));
                    println(input + " converted to binary equals: " + Print);
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
                stringUtil.logToDo(input);
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
                SystemUtil.deleteFolder(new File("src/users/" + ConsoleFrame.getUUID()));

                String dep = SecurityUtil.getDeprecatedUUID();

                File renamed = new File("src/users/" + dep);
                while (renamed.exists()) {
                    dep = SecurityUtil.getDeprecatedUUID();
                    renamed = new File("src/users/" + dep);
                }

                File old = new File("src/users/" + ConsoleFrame.getUUID());
                old.renameTo(renamed);

                Frame[] frames = Frame.getFrames();

                for (Frame f : frames)
                    f.dispose();

                login();
            } else if (desc.equalsIgnoreCase("pixelatebackground")) {
                BufferedImage img = ImageUtil.pixelate(ImageIO.read(ConsoleFrame.getCurrentBackgroundFile().getAbsoluteFile()), Integer.parseInt(input));

                String searchName = ConsoleFrame.getCurrentBackgroundFile().getName().replace(".png", "")
                        + "_Pixelated_Pixel_Size_" + Integer.parseInt(input) + ".png";

                File saveFile = new File("src/users/" + ConsoleFrame.getUUID() +
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

            if (stringUtil.filterLanguage(operation)) {
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
            } else if (eic("pi")) {
                println(Math.PI);
            } else if (hasWord("euler") || eic("e")) {
                println("Leonhard Euler's number is " + Math.E);
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
                printImage("src/cyder//sys/pictures/msu.png");
            } else if (has("toy") && has("story")) {
                IOUtil.playAudio("src/cyder//sys/audio/TheClaw.mp3");
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
            } else if (hasWord("calculator") && !has("graphing")) {
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
                printlnImage("src/cyder//sys/pictures/me.png");
            } else if ((eic("error") || eic("errors")) && !hasWord("throw")) {
                File WhereItIs = new File("src/users/" + ConsoleFrame.getUUID() + "/Throws/");
                Desktop.getDesktop().open(WhereItIs);
            } else if (eic("help")) {
                stringUtil.help(outputArea);
            } else if (hasWord("light") && hasWord("saber")) {
                IOUtil.playAudio("src/cyder//sys/audio/Lightsaber.mp3");
            } else if (hasWord("xbox")) {
                IOUtil.playAudio("src/cyder//sys/audio/xbox.mp3");
            } else if (has("star") && has("trek")) {
                IOUtil.playAudio("src/cyder//sys/audio/StarTrek.mp3");
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
                IOUtil.playAudio("src/cyder//sys/audio/windows.mp3");
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
                IOUtil.playAudio("src/cyder//sys/audio/heyya.mp3");
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
                printImage("src/cyder//sys/pictures/Duke.png");
            } else if (hasWord("coffee")) {
                NetworkUtil.internetConnect("https://www.google.com/search?q=coffe+shops+near+me");
            } else if (hasWord("ip")) {
                println(InetAddress.getLocalHost().getHostAddress());
            } else if (hasWord("html") || hasWord("html5")) {
                consoleFrame.setIconImage(new ImageIcon("src/cyder//sys/pictures/html5.png").getImage());
                printlnImage("src/cyder//sys/pictures/html5.png");
            } else if (hasWord("css")) {
                consoleFrame.setIconImage(new ImageIcon("src/cyder//sys/pictures/css.png").getImage());
                printlnImage("src/cyder//sys/pictures/css.png");
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

            } else if (eic("controlc")) {
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
                    IOUtil.openFile("src/cyder//sys/pictures");
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
                IOUtil.playAudio("src/cyder//sys/audio/commando.mp3");
            } else if (eic("1-800-273-8255") || eic("18002738255")) {
                IOUtil.playAudio("src/cyder//sys/audio/1800.mp3");
            } else if (hasWord("resize") && (hasWord("image") || hasWord("picture"))) {
                ImageResizer IR = new ImageResizer();
            } else if (hasWord("barrel") && hasWord("roll")) {
                //todo ConsoleFrme.barrelRoll();
            } else if (hasWord("analyze") && hasWord("code")) {
                println("Lines of code: " + NumberUtil.totalJavaLines(new File(System.getProperty("user.dir"))));
                println("Number of java files: " + NumberUtil.totalJavaFiles(new File(System.getProperty("user.dir"))));
                println("Number of comments: " + NumberUtil.totalComments(new File(System.getProperty("user.dir"))));
                println("Blank lines: " + NumberUtil.totalBlankLines(new File(System.getProperty("user.dir"))));
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
            } else if (eic("askew")) {
                //todo ConsoleFrame.askew(5);
            } else if (hasWord("press") && (hasWord("F17") || hasWord("f17"))) {
                new Robot().keyPress(KeyEvent.VK_F17);
            } else if (hasWord("logout")) {
                AnimationUtil.closeAnimation(consoleFrame);
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
                println("Did you mean who is alex trebek?");
            } else if (hasWord("test")) {
                //basic frame for UI testing setup below
                CyderFrame testFrame = new CyderFrame(500, 200, new ImageIcon(DEFAULT_BACKGROUND_PATH));
                testFrame.setTitle("My Frame Title");
                testFrame.setTitlePosition(TitlePosition.CENTER);
                testFrame.initResizing();
                testFrame.setSnapSize(new Dimension(1, 1));
                testFrame.setBackgroundResizing(true);

                final TitlePosition[] current = {testFrame.getTitlePosition()};

                CyderButton alternateTitleButton = new CyderButton("Alternate Title");
                alternateTitleButton.setBounds(40, 40, 250, 40);
                alternateTitleButton.addActionListener(e -> {
                    current[0] = (current[0] == TitlePosition.CENTER ? TitlePosition.LEFT : TitlePosition.CENTER);
                    testFrame.setTitlePosition(current[0]);
                });
                testFrame.getContentPane().add(alternateTitleButton);

                testFrame.setVisible(true);
                testFrame.setLocationRelativeTo(null);
            } else if (hasWord("christmas") && hasWord("card") && hasWord("2020")) {
                Cards.Christmas2020();
            } else if (hasWord("number") && hasWord("word")) {
                NumberUtil.numberToWord();
            } else if (hasWord("Quake") && (hasWord("three") || hasWord("3"))) {
                NetworkUtil.internetConnect("https://www.youtube.com/watch?v=p8u_k2LIZyo&ab_channel=Nemean");
            } else {
                println("Sorry, " + ConsoleFrame.getUsername() + ", but I don't recognize that command." +
                        " You can make a suggestion by clicking the \"Suggest something\" button.");

                new Thread(() -> {
                    try {
                        ImageIcon img2 = new ImageIcon("src/cyder//sys/pictures/suggestion2.png");
                        ImageIcon img1 = new ImageIcon("src/cyder//sys/pictures/suggestion1.png");

                        suggestionButton.setIcon(img2);
                        Thread.sleep(300);
                        suggestionButton.setIcon(img1);
                        Thread.sleep(300);
                        suggestionButton.setIcon(img2);
                        Thread.sleep(300);
                        suggestionButton.setIcon(img1);
                        Thread.sleep(300);
                        suggestionButton.setIcon(img2);
                        Thread.sleep(300);
                        suggestionButton.setIcon(img1);
                        Thread.sleep(300);
                        suggestionButton.setIcon(img2);
                        Thread.sleep(300);
                        suggestionButton.setIcon(img1);
                    } catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                }, "suggestionButton flash").start();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //handler method
    private boolean handleMath(String op) {
        int firstParen = op.indexOf("(");
        int comma = op.indexOf(",");
        int lastParen = op.indexOf(")");

        String mathop;
        double param1 = 0.0;
        double param2 = 0.0;

        try {
            if (firstParen != -1) {
                mathop = op.substring(0, firstParen);

                if (comma != -1) {
                    param1 = Double.parseDouble(op.substring(firstParen + 1, comma));

                    if (lastParen != -1) {
                        param2 = Double.parseDouble(op.substring(comma + 1, lastParen));
                    }
                } else if (lastParen != -1) {
                    param1 = Double.parseDouble(op.substring(firstParen + 1, lastParen));
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
                    println(NumberUtil.toBinary((int) (param1)));
                    return true;
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
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
        editUserFrame.setTitlePosition(TitlePosition.LEFT);
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
        editUserFrame.setAlwaysOnTop(true);
        editUserFrame.setVisible(true);
        editUserFrame.requestFocus();
    }

    public void initMusicBackgroundList() {
        File backgroundDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Backgrounds");
        File musicDir = new File("src/users/" + ConsoleFrame.getUUID() + "/Music");

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
                    File Destination = new File("src/users/" + ConsoleFrame.getUUID() + "/Backgrounds/" + addFile.getName());
                    Files.copy(copyPath, Destination.toPath());
                    initMusicBackgroundList();
                    musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                    musicBackgroundScroll.revalidate();
                } else if (addFile != null && addFile.getName().endsWith(".mp3")) {
                    File Destination = new File("src/users/" + ConsoleFrame.getUUID() + "/Music/" + addFile.getName());
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

        //todo rename button next to main three

        switchingPanel.revalidate();
    }

    private void switchToFontAndColor() {
        JLabel TitleLabel = new JLabel("Foreground & Font", SwingConstants.CENTER);
        TitleLabel.setFont(CyderFonts.weatherFontBig);
        TitleLabel.setBounds(720 / 2 - 375 / 2, 10, 375, 40);
        switchingPanel.add(TitleLabel);

        int colorOffsetX = 340;
        int colorOffsetY = 100;

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
                } catch (Exception ignored) {
                }
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
        applyColor.setBounds(460, 420, 200, 40);
        switchingPanel.add(applyColor);

        JLabel FontLabel = new JLabel("Fonts");
        FontLabel.setFont(CyderFonts.weatherFontBig);
        FontLabel.setForeground(CyderColors.navy);
        FontLabel.setBounds(150, 60, 300, 30);
        switchingPanel.add(FontLabel);

        String[] Fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        fontList = new JList(Fonts);
        fontList.setSelectionBackground(CyderColors.selectionColor);
        fontList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fontList.setFont(CyderFonts.weatherFontSmall);

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

    //todo edit user handler
    //this edit user stuff should probably be in consoleframe
    //todo make scrollable table of description and checkboxes here
    private void switchToPreferences() {
        JLabel prefsTitle = new JLabel("Preferences");
        prefsTitle.setFont(CyderFonts.weatherFontBig);
        prefsTitle.setForeground(CyderColors.navy);
        prefsTitle.setHorizontalAlignment(JLabel.CENTER);
        prefsTitle.setBounds(720 / 2 - 250 / 2, 10, 250, 30);
        switchingPanel.add(prefsTitle);

        JLabel introMusicTitle = new JLabel("Intro Music");
        introMusicTitle.setFont(CyderFonts.weatherFontSmall);
        introMusicTitle.setForeground(CyderColors.navy);
        introMusicTitle.setHorizontalAlignment(JLabel.CENTER);
        introMusicTitle.setBounds(20, 50, 130, 25);
        switchingPanel.add(introMusicTitle);

        JLabel debugWindowsLabel = new JLabel("Debug");
        debugWindowsLabel.setFont(CyderFonts.weatherFontSmall);
        debugWindowsLabel.setForeground(CyderColors.navy);
        debugWindowsLabel.setHorizontalAlignment(JLabel.CENTER);
        debugWindowsLabel.setBounds(130, 50, 160, 25);
        switchingPanel.add(debugWindowsLabel);

        JLabel randomBackgroundLabel = new JLabel("Random Back");
        randomBackgroundLabel.setFont(CyderFonts.weatherFontSmall);
        randomBackgroundLabel.setForeground(CyderColors.navy);
        randomBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        randomBackgroundLabel.setBounds(150 + 120, 50, 160, 25);
        switchingPanel.add(randomBackgroundLabel);

        JLabel outputBorderLabel = new JLabel("Out Border");
        outputBorderLabel.setFont(CyderFonts.weatherFontSmall);
        outputBorderLabel.setForeground(CyderColors.navy);
        outputBorderLabel.setHorizontalAlignment(JLabel.CENTER);
        outputBorderLabel.setBounds(150 + 20 + 10 + 150 + 90, 50, 160, 25);
        switchingPanel.add(outputBorderLabel);

        JLabel inputBorderLabel = new JLabel("In Border");
        inputBorderLabel.setFont(CyderFonts.weatherFontSmall);
        inputBorderLabel.setForeground(CyderColors.navy);
        inputBorderLabel.setHorizontalAlignment(JLabel.CENTER);
        inputBorderLabel.setBounds(150 + 20 + 20 + 150 + 225, 50, 160, 25);
        switchingPanel.add(inputBorderLabel);

        //todo copy this for all checkboxes and put in 2 column table
        CyderCheckBox introMusic = new CyderCheckBox();
        introMusic.setToolTipText("Play intro music on start");
        if (IOUtil.getUserData("IntroMusic").equals("1"))
            introMusic.setSelected();
        introMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                IOUtil.writeUserData("IntroMusic", (introMusic.isSelected() ? "1" : "0"));
            }
        });
        introMusic.setBounds(20, 80, 50, 50);
        switchingPanel.add(introMusic);

        JLabel debugWindows = new JLabel();
        debugWindows.setToolTipText("Show debug windows on start");
        debugWindows.setHorizontalAlignment(JLabel.CENTER);
        debugWindows.setSize(100, 100);
        debugWindows.setIcon((IOUtil.getUserData("DebugWindows").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        debugWindows.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("DebugWindows").equals("1");
            IOUtil.writeUserData("DebugWindows", (wasSelected ? "0" : "1"));
            debugWindows.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));
            }
        });
        debugWindows.setBounds(20 + 45 + 100, 80, 100, 100);
        switchingPanel.add(debugWindows);

        JLabel randBackgroundLabel = new JLabel();
        randBackgroundLabel.setToolTipText("Choose a random background on start");
        randBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        randBackgroundLabel.setSize(100, 100);
        randBackgroundLabel.setIcon((IOUtil.getUserData("RandomBackground").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        randBackgroundLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("RandomBackground").equals("1");
            IOUtil.writeUserData("RandomBackground", (wasSelected ? "0" : "1"));
            randBackgroundLabel.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));
            }
        });
        randBackgroundLabel.setBounds(20 + 2 * 45 + 2 * 100, 80, 100, 100);
        switchingPanel.add(randBackgroundLabel);

        JLabel outputBorder = new JLabel();
        outputBorder.setToolTipText("Draw a border around the output area");
        outputBorder.setHorizontalAlignment(JLabel.CENTER);
        outputBorder.setSize(100, 100);
        outputBorder.setIcon((IOUtil.getUserData("OutputBorder").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        outputBorder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("OutputBorder").equals("1");
            IOUtil.writeUserData("OutputBorder", (wasSelected ? "0" : "1"));
            outputBorder.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));
            if (wasSelected) {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            } else {
                outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")), 3, true));
            }

            consoleFrame.revalidate();
            }
        });
        outputBorder.setBounds(20 + 3 * 45 + 3 * 100, 80, 100, 100);
        switchingPanel.add(outputBorder);

        JLabel inputBorder = new JLabel();
        inputBorder.setToolTipText("Draw a border around the input field");
        inputBorder.setHorizontalAlignment(JLabel.CENTER);
        inputBorder.setSize(100, 100);
        inputBorder.setIcon((IOUtil.getUserData("InputBorder").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        inputBorder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("InputBorder").equals("1");
            IOUtil.writeUserData("InputBorder", (wasSelected ? "0" : "1"));
            inputBorder.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));

            if (wasSelected) {
                inputField.setBorder(BorderFactory.createEmptyBorder());
            } else {
                inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")), 3, true));
            }

            consoleFrame.revalidate();
            }
        });
        inputBorder.setBounds(20 + 4 * 45 + 4 * 100, 80, 100, 100);
        switchingPanel.add(inputBorder);

        JLabel hourlyChimesLabel = new JLabel("Hour Chimes");
        hourlyChimesLabel.setFont(CyderFonts.weatherFontSmall);
        hourlyChimesLabel.setForeground(CyderColors.navy);
        hourlyChimesLabel.setHorizontalAlignment(JLabel.CENTER);
        hourlyChimesLabel.setBounds(5, 210, 170, 30);
        switchingPanel.add(hourlyChimesLabel);

        JLabel silenceLabel = new JLabel("No Errors");
        silenceLabel.setFont(CyderFonts.weatherFontSmall);
        silenceLabel.setForeground(CyderColors.navy);
        silenceLabel.setHorizontalAlignment(JLabel.CENTER);
        silenceLabel.setBounds(150, 210, 150, 30);
        switchingPanel.add(silenceLabel);

        JLabel fullscreenLabel = new JLabel("Fullscreen");
        fullscreenLabel.setFont(CyderFonts.weatherFontSmall);
        fullscreenLabel.setForeground(CyderColors.navy);
        fullscreenLabel.setHorizontalAlignment(JLabel.CENTER);
        fullscreenLabel.setBounds(285, 210, 170, 30);
        switchingPanel.add(fullscreenLabel);

        JLabel outputFillLabel = new JLabel("Fill Out");
        outputFillLabel.setFont(CyderFonts.weatherFontSmall);
        outputFillLabel.setForeground(CyderColors.navy);
        outputFillLabel.setHorizontalAlignment(JLabel.CENTER);
        outputFillLabel.setBounds(420, 210, 170, 30);
        switchingPanel.add(outputFillLabel);

        JLabel inputFillLabel = new JLabel("Fill In");
        inputFillLabel.setFont(CyderFonts.weatherFontSmall);
        inputFillLabel.setForeground(CyderColors.navy);
        inputFillLabel.setHorizontalAlignment(JLabel.CENTER);
        inputFillLabel.setBounds(560, 210, 170, 30);
        switchingPanel.add(inputFillLabel);

        JLabel hourlyChimes = new JLabel();
        hourlyChimes.setToolTipText("Chime every hour");
        hourlyChimes.setHorizontalAlignment(JLabel.CENTER);
        hourlyChimes.setSize(100, 100);
        hourlyChimes.setIcon((IOUtil.getUserData("HourlyChimes").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        hourlyChimes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("HourlyChimes").equals("1");
            IOUtil.writeUserData("HourlyChimes", (wasSelected ? "0" : "1"));
            hourlyChimes.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));
            }
        });
        hourlyChimes.setBounds(20, 235, 100, 100);
        switchingPanel.add(hourlyChimes);

        JLabel silenceErrors = new JLabel();
        silenceErrors.setToolTipText("Hide errors that occur");
        silenceErrors.setHorizontalAlignment(JLabel.CENTER);
        silenceErrors.setSize(100, 100);
        silenceErrors.setIcon((IOUtil.getUserData("SilenceErrors").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        silenceErrors.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("SilenceErrors").equals("1");
            IOUtil.writeUserData("SilenceErrors", (wasSelected ? "0" : "1"));
            silenceErrors.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));
            }

        });
        silenceErrors.setBounds(20 + 100 + 45, 235, 100, 100);
        switchingPanel.add(silenceErrors);

        JLabel fullscreen = new JLabel();
        fullscreen.setToolTipText("Toggle between fullscreen (Extremely Experimental)");
        fullscreen.setHorizontalAlignment(JLabel.CENTER);
        fullscreen.setSize(100, 100);
        fullscreen.setIcon((IOUtil.getUserData("FullScreen").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        fullscreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("FullScreen").equals("1");
            IOUtil.writeUserData("FullScreen", (wasSelected ? "0" : "1"));
            fullscreen.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));
            if (wasSelected)
                exitFullscreen();
            else
                refreshConsoleFrame();
            }
        });
        fullscreen.setBounds(20 + 2 * 100 + 2 * 45, 235, 100, 100);
        switchingPanel.add(fullscreen);

        JLabel outputFill = new JLabel();
        outputFill.setToolTipText("Fill the output area with your custom color");
        outputFill.setHorizontalAlignment(JLabel.CENTER);
        outputFill.setSize(100, 100);
        outputFill.setIcon((IOUtil.getUserData("OutputFill").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        outputFill.setBounds(20 + 3 * 100 + 3 * 45, 235, 100, 100);
        switchingPanel.add(outputFill);

        JLabel inputFill = new JLabel();
        inputFill.setToolTipText("Fill the input field with your custom color");
        inputFill.setHorizontalAlignment(JLabel.CENTER);
        inputFill.setSize(100, 100);
        inputFill.setIcon((IOUtil.getUserData("InputFill").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        inputFill.setBounds(20 + 4 * 100 + 4 * 45, 235, 100, 100);
        switchingPanel.add(inputFill);

        JLabel clockLabel = new JLabel("Console Clock");
        clockLabel.setFont(CyderFonts.weatherFontSmall);
        clockLabel.setForeground(CyderColors.navy);
        clockLabel.setHorizontalAlignment(JLabel.CENTER);
        clockLabel.setBounds(20, 380, 170, 25);
        switchingPanel.add(clockLabel);

        JLabel showSecondsLabel = new JLabel("Clock Seconds");
        showSecondsLabel.setFont(CyderFonts.weatherFontSmall);
        showSecondsLabel.setForeground(CyderColors.navy);
        showSecondsLabel.setHorizontalAlignment(JLabel.CENTER);
        showSecondsLabel.setBounds(220, 380, 170, 25);
        switchingPanel.add(showSecondsLabel);

        JLabel clockOnConsole = new JLabel();
        clockOnConsole.setToolTipText("Show clock at top of main window");
        clockOnConsole.setHorizontalAlignment(JLabel.CENTER);
        clockOnConsole.setSize(100, 100);
        clockOnConsole.setIcon((IOUtil.getUserData("ClockOnConsole").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        clockOnConsole.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("ClockOnConsole").equals("1");
            IOUtil.writeUserData("ClockOnConsole", (wasSelected ? "0" : "1"));
            clockOnConsole.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));
            consoleClockLabel.setVisible(!wasSelected);

            if (consoleClockLabel.isVisible())
                updateConsoleClock = true;

            consoleFrame.revalidate();

            if (consoleClockLabel.isVisible()) {
                if (IOUtil.getUserData("ShowSeconds").equals("1"))
                    consoleClockLabel.setText(TimeUtil.consoleSecondTime());
                else
                    consoleClockLabel.setText(TimeUtil.consoleTime());
            }
            }
        });
        clockOnConsole.setBounds(50, 400, 100, 100);
        switchingPanel.add(clockOnConsole);

        JLabel showSeconds = new JLabel();
        showSeconds.setToolTipText("Show seconds on console clock");
        showSeconds.setHorizontalAlignment(JLabel.CENTER);
        showSeconds.setSize(100, 100);
        showSeconds.setIcon((IOUtil.getUserData("ShowSeconds").equals("1") ? CyderImages.checkboxSelected : CyderImages.checkboxNotSelected));
        showSeconds.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("ShowSeconds").equals("1");
            IOUtil.writeUserData("ShowSeconds", (wasSelected ? "0" : "1"));
            showSeconds.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));

            if (wasSelected)
                consoleClockLabel.setText(TimeUtil.consoleTime());
            else
                consoleClockLabel.setText(TimeUtil.consoleSecondTime());
            }
        });
        showSeconds.setBounds(50 + 200, 400, 100, 100);
        switchingPanel.add(showSeconds);

        outputFill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("OutputFill").equals("1");
            IOUtil.writeUserData("OutputFill", (wasSelected ? "0" : "1"));
            outputFill.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));

            if (wasSelected) {
                outputArea.setBackground(null);
                outputArea.setOpaque(false);
                consoleFrame.revalidate();
            } else {
                outputArea.setOpaque(true);
                outputArea.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
                outputArea.repaint();
                outputArea.revalidate();
                consoleFrame.revalidate();
            }
            }
        });

        inputFill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("InputFill").equals("1");
            IOUtil.writeUserData("InputFill", (wasSelected ? "0" : "1"));
            inputFill.setIcon((wasSelected ? CyderImages.checkboxNotSelected : CyderImages.checkboxSelected));

            if (wasSelected) {
                inputField.setBackground(null);
                inputField.setOpaque(false);
                consoleFrame.revalidate();
            } else {
                inputField.setOpaque(true);
                inputField.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
                inputField.repaint();
                inputField.revalidate();
                consoleFrame.revalidate();
            }
            }
        });

        JLabel hexLabel = new JLabel("Hex Code");
        hexLabel.setFont(CyderFonts.weatherFontSmall);
        hexLabel.setForeground(CyderColors.navy);
        hexLabel.setHorizontalAlignment(JLabel.CENTER);
        hexLabel.setBounds(434, 380, 150, 30);
        switchingPanel.add(hexLabel);

        JTextField colorBlock = new JTextField();
        colorBlock.setBackground(CyderColors.navy);
        colorBlock.setFocusable(false);
        colorBlock.setCursor(null);
        colorBlock.setToolTipText("Color Preview");
        colorBlock.setBackground(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")));
        colorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        colorBlock.setBounds(630, 380, 40, 100);
        switchingPanel.add(colorBlock);

        JTextField hexField = new JTextField(String.format("#%02X%02X%02X", CyderColors.navy.getRed(),
                CyderColors.navy.getGreen(), CyderColors.navy.getBlue()).replace("#", ""));
        hexField.setForeground(CyderColors.navy);
        hexField.setText(IOUtil.getUserData("Background"));
        hexField.setFont(CyderFonts.weatherFontSmall);
        hexField.setBackground(new Color(255, 255, 255));
        hexField.setSelectionColor(CyderColors.selectionColor);
        hexField.setToolTipText("Input field and output area fill color if enabled");
        hexField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            try {
                colorBlock.setBackground(ColorUtil.hextorgbColor(hexField.getText()));
                IOUtil.writeUserData("Background", hexField.getText());
            } catch (Exception ignored) {
            }
            }
        });
        hexField.setBounds(460, 420, 100, 40);
        switchingPanel.add(hexField);

        switchingPanel.revalidate();
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
                    File folder = new File("src/users/" + uuid);

                    while (folder.exists()) {
                        uuid = SecurityUtil.generateUUID();
                        folder = new File("src/users/" + uuid);
                    }

                    char[] pass = newUserPassword.getPassword();
                    char[] passconf = newUserPasswordconf.getPassword();

                    boolean alreadyExists = false;
                    File[] files = new File("src/users").listFiles();

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
                            createUserBackground = new File("src/cyder//sys/pictures/DefaultBackground.png");
                        }

                        File NewUserFolder = new File("src/users/" + uuid);
                        File backgrounds = new File("src/users/" + uuid + "/Backgrounds");
                        File music = new File("src/users/" + uuid + "/Music");
                        File notes = new File("src/users/" + uuid + "/Notes");

                        NewUserFolder.mkdirs();
                        backgrounds.mkdir();
                        music.mkdir();
                        notes.mkdir();

                        ImageIO.write(ImageIO.read(createUserBackground), "png",
                                new File("src/users/" + uuid + "/Backgrounds/" + createUserBackground.getName()));

                        BufferedWriter newUserWriter = new BufferedWriter(new FileWriter(
                                "src/users/" + uuid + "/Userdata.txt"));

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

                        if ((!consoleFrame.isVisible() && loginFrame != null) || (new File("src/users/").length() == 1)) {
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
                menuButton.setIcon(new ImageIcon("src/cyder//sys/pictures/menuSide1.png"));
            });
            waitThread.start();
        }
    }

    //should be in MasterYoutube class
    private void killAllYoutube() {
        for (YoutubeThread ytt : youtubeThreads)
            ytt.kill();
    }

    //exiting method, system.exit will call shutdown hook which wil then call shutdown();
    private void exit() {
        IOUtil.readUserData();
        IOUtil.writeUserData("Fonts", outputArea.getFont().getName());
        IOUtil.writeUserData("Foreground", ColorUtil.rgbtohexString(outputArea.getForeground()));

        AnimationUtil.closeAnimation(consoleFrame);
        killAllYoutube();
        stringUtil.killBletchy();

        try {
            CyderMain.exitingSem.acquire();
            CyderMain.exitingSem.release();
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
        new File("InputMessage.txt").delete();
        new File("File.txt").delete();
        new File("String.txt").delete();
    }

    public void checkFiles() {
        //todo if a certain file is missing, attempt to download it
    }

    public void downloadFile(String httpString, boolean important) {
        //todo download httpString file if secure internet connection, if we can't get it and important, exit and inform
    }
}