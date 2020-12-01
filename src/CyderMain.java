import com.cyder.exception.CyderException;
import com.cyder.exception.FatalException;
import com.cyder.games.Hangman;
import com.cyder.games.TicTacToe;
import com.cyder.handler.PhotoViewer;
import com.cyder.handler.TestClass;
import com.cyder.threads.YoutubeThread;
import com.cyder.ui.*;
import com.cyder.utilities.*;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

//todo put all background checking things in one thread
//todo fix double chime on hour glitch
//todo when doing confirmations through the console, pull it to front and then push it back
//todo make prefs for filled output area and input field
//todo let color for text be inputed in rgb format too
//todo be able to set background to a solid color and make that an image and save it

//todo utilize colors, fonts, font weights, and new lines now
//<html>test<br/><i>second line but italics<i/><br/>third!!<br/><p style="color:rgb(252, 251, 227)">fourth with color</p>
// <p style="font-family:verdana">fifth with font</p></html>

//todo notes and textviewer non-swing dependent

//todo perlin-noise GUI swap between 2D and 3D and add color range too
//todo make a widget version of cyder that you can swap between big window and widget version, background is get cropped image
//todo make pixelating pictures it's own widget

//todo hangman use cyder frame
//todo photo viewer renmaing needs to be cyderframe
//todo utilize start animations after you fix it

//todo make an animation util class
//todo network util class
//todo ui utils class

//todo further class separation from GeneralUtil.java

//todo add a systems error dir if no users <- if possibility of no user put here too (see readData() loop)
//todo add a handle that you can use when unsure if there is a user to avoid looping until stackoverflow

//todo I feel like a lot of stuff should be static since it means it belongs to the class an not an instance of it

//todo cyder frame should have a notify method that will drop down from center and back up
//todo enter animation toggle for notification

//todo make the frame and drag label stay when switching backgrounds and the image be separate
//todo you kind of did this in login with the sliding text, then notification will not go over it

//todo double hash sha perhaps to avoid someone just hashing their own password and pasting it in

//todo hot key in menu to kill background processes like bletchy and youtube threads, anything that makes the icon yellow basically

//todo allow users to map up to three internet links on the menu, add a bar to sep system from user stuff

public class CyderMain{
    //console vars
    private static JTextPane outputArea;
    private JTextField inputField;
    private JFrame consoleFrame;
    private JButton minimize;
    private JButton close;
    private JLabel consoleClockLabel;
    private boolean updateConsoleClock;
    private JLabel loginLabel;
    private JLabel loginLabel2;
    private JLabel loginLabel3;
    private JLabel parentLabel;
    private JLabel temporaryLabel;
    private JLabel loginDragLabel;
    private CyderScrollPane outputScroll;
    private JButton alternateBackground;
    private JLabel consoleDragLabel;
    private JLayeredPane parentPane;
    private JButton suggestionButton;
    private JButton menuButton;
    private JFrame loginFrame;
    private JTextField nameField;
    private JPasswordField pass;
    private JLabel newUserLabel;
    private JLabel menuLabel;

    //Objects for main use
    private GeneralUtil mainGeneralUtil;
    private StringUtil stringUtil;
    private CyderAnimation animation;
    private Notes userNotes;

    //operation var
    private static ArrayList<String> operationList = new ArrayList<>();
    private static int scrollingIndex;

    //deiconified restore vars
    private int restoreX;
    private int restoreY;

    //drag pos vars;
    private int xMouse;
    private int yMouse;

    //handler case vars
    private String operation;

    //anagram one var
    private String anagram;

    //font vars
    private JList fontList;

    //create user vars
    private CyderFrame createUserFrame;
    private JPasswordField newUserPasswordconf;
    private JPasswordField newUserPassword;
    private JTextField newUserName;
    private CyderButton createNewUser;
    private CyderButton chooseBackground;
    private File createUserBackground;

    //minecraft class so we can only have one
    private MinecraftWidget mw;

    //notificaiton
    private static Notification consoleNotification;

    //pixealte file
    private File pixelateFile;

    //Linked List of youtube scripts
    private LinkedList<YoutubeThread> youtubeThreads = new LinkedList<>();

    //sliding background var
    private boolean slidLeft;

    //notifications for holidays
    private SpecialDay specialDayNotifier;

    //notify test vars
    private int notificaitonTestWidth;
    private String notificationTestString;

    //call constructor
    public static void main(String[] CA) {
        new CyderMain();
        logArgs(CA);
    }

    private CyderMain() {
        //this adds a shutdown hook so that we always do certain things on exit
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown,"exit-hook"));

        initObjects();
        initSystemProperties();
        initUIManager();

        mainGeneralUtil.cleanUpUsers();
        mainGeneralUtil.deleteTempDir();
        mainGeneralUtil.varInit();

        backgroundProcessChecker();

        boolean nathanLenovo = mainGeneralUtil.compMACAddress(mainGeneralUtil.getMACAddress());

        if (nathanLenovo) {
            mainGeneralUtil.setDebugMode(true);
            autoCypher();
        }

        else if (!mainGeneralUtil.released()) {
            System.exit(0);
        }

        else {
            login(false);
        }
    }

    private void initObjects() {
        mainGeneralUtil = new GeneralUtil();
        animation = new CyderAnimation();
        stringUtil = new StringUtil();
        stringUtil.setOutputArea(outputArea);
    }

    private void initSystemProperties() {
        //Fix scaling issue for high DPI displays like nathanLenovo which is 2560x1440
        System.setProperty("sun.java2d.uiScale","1.0");
    }

    private void initUIManager() {
        //this sets up special looking tooltips
        UIManager.put("ToolTip.background", mainGeneralUtil.consoleColor);
        UIManager.put("ToolTip.border", mainGeneralUtil.tooltipBorderColor);
        UIManager.put("ToolTip.font", mainGeneralUtil.tahoma);
        UIManager.put("ToolTip.foreground", mainGeneralUtil.tooltipForegroundColor);
    }

    private void autoCypher() {
        try {
            File autoCypher = new File("../autocypher.txt");
            File Users = new File("src/com/cyder/users/");

            if (autoCypher.exists() && Users.listFiles().length != 0) {
                BufferedReader ac = new BufferedReader(new FileReader(autoCypher));

                String line = ac.readLine();
                String[] parts = line.split(":");

                if (parts.length == 2 && !parts[0].equals("") && !parts[1].equals(""))
                    recognize(parts[0], parts[1].toCharArray());
            }

            else {
                login(false);
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void console() {
        try{
            mainGeneralUtil.initBackgrounds();
            mainGeneralUtil.getScreenSize();
            mainGeneralUtil.resizeImages();
            mainGeneralUtil.getValidBackgroundPaths();
            mainGeneralUtil.initBackgrounds();
            mainGeneralUtil.getScreenSize();
            mainGeneralUtil.getBackgroundSize();

            consoleFrame = new JFrame();
            consoleFrame.setUndecorated(true);
            consoleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            if (mainGeneralUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                mainGeneralUtil.setBackgroundX((int) mainGeneralUtil.getScreenSize().getWidth());
                mainGeneralUtil.setBackgroundY((int) mainGeneralUtil.getScreenSize().getHeight());
            }

            consoleFrame.setBounds(0, 0, mainGeneralUtil.getBackgroundX(), mainGeneralUtil.getBackgroundY());
            consoleFrame.setTitle(mainGeneralUtil.getCyderVer() + " Cyder [" + mainGeneralUtil.getUsername() + "]");

            parentPane = new JLayeredPane();
            parentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

            consoleFrame.setContentPane(parentPane);

            parentPane.setLayout(null);

            parentLabel = new JLabel();

            if (mainGeneralUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                parentLabel.setIcon(new ImageIcon(mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(), (int) mainGeneralUtil.getScreenSize().getHeight(), mainGeneralUtil.getCurrentBackground())));
                parentLabel.setBounds(0, 0, mainGeneralUtil.getBackgroundX(), mainGeneralUtil.getBackgroundY());
                mainGeneralUtil.setBackgroundX((int) mainGeneralUtil.getScreenSize().getWidth());
                mainGeneralUtil.setBackgroundY((int) mainGeneralUtil.getScreenSize().getHeight());
            }

            else {
                parentLabel.setIcon(new ImageIcon(mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().toString())));
                parentLabel.setBounds(0, 0, mainGeneralUtil.getBackgroundX(), mainGeneralUtil.getBackgroundY());
            }

            parentLabel.setBorder(new LineBorder(mainGeneralUtil.navy,8,false));
            parentLabel.setToolTipText(mainGeneralUtil.getCurrentBackground().getName().replace(".png", ""));

            parentPane.add(parentLabel,1,0);

            consoleFrame.setIconImage(mainGeneralUtil.getCyderIcon().getImage());

            outputArea = new JTextPane() {
                @Override
                public void setBorder(Border border) {
                    //no border
                }
            };
            outputArea.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                    inputField.requestFocus();
                }
            });

            outputArea.setEditable(false);
            outputArea.setAutoscrolls(true);
            outputArea.setBounds(10, 62, mainGeneralUtil.getBackgroundX() - 20, mainGeneralUtil.getBackgroundY() - 204);
            outputArea.setFocusable(true);
            outputArea.setSelectionColor(new Color(204,153,0));
            outputArea.setOpaque(true);
            outputArea.setBackground(new Color(0,0,0,0));

            outputScroll = new CyderScrollPane(outputArea,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            outputScroll.setThumbColor(mainGeneralUtil.intellijPink);
            outputScroll.getViewport().setBorder(null);
            outputScroll.getViewport().setOpaque(false);
            outputScroll.setOpaque(false);

            if (mainGeneralUtil.getUserData("OutputBorder").equalsIgnoreCase("1")) {
                outputScroll.setBorder(new LineBorder(mainGeneralUtil.vanila,3,true));
            }

            else {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            outputScroll.setBounds(10, 62, mainGeneralUtil.getBackgroundX() - 20, mainGeneralUtil.getBackgroundY() - 204);

            parentLabel.add(outputScroll);

            inputField = new JTextField(40);

            if (mainGeneralUtil.getUserData("InputBorder").equalsIgnoreCase("1")) {
                inputField.setBorder(new LineBorder(mainGeneralUtil.vanila,3,true));
            }

            else {
                inputField.setBorder(BorderFactory.createEmptyBorder());
            }


            inputField.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1) {
                        inputField.setText(inputField.getText().toUpperCase());
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
                        handle("controlc");
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_DOWN) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(mainGeneralUtil.CYDER_DOWN);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_RIGHT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(mainGeneralUtil.CYDER_RIGHT);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_UP) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(mainGeneralUtil.CYDER_UP);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_LEFT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(mainGeneralUtil.CYDER_LEFT);
                        exitFullscreen();
                    }
                }

                @Override
                public void keyReleased(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1) {
                        inputField.setText(inputField.getText().toUpperCase());
                    }
                }

                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1) {
                        inputField.setText(inputField.getText().toUpperCase());
                    }
                }
            });

            inputField.setToolTipText("Input Field");
            inputField.setSelectionColor(mainGeneralUtil.selectionColor);
            inputField.addKeyListener(commandScrolling);

            consoleFrame.addWindowListener(consoleEcho);

            inputField.setBounds(10, 82 + outputArea.getHeight(),
                    mainGeneralUtil.getBackgroundX() - 20, mainGeneralUtil.getBackgroundY() - (outputArea.getHeight() + 62 + 40));
            inputField.setOpaque(false);

            parentLabel.add(inputField);

            inputField.addActionListener(inputFieldAction);
            inputField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                }
            });

            inputField.setCaretColor(mainGeneralUtil.vanila);

            mainGeneralUtil.readUserData();

            Font Userfont = new Font(mainGeneralUtil.getUserData("Font"),Font.BOLD, 30);
            Color Usercolor = mainGeneralUtil.hextorgbColor(mainGeneralUtil.getUserData("Foreground"));

            mainGeneralUtil.setUsercolor(Usercolor);
            mainGeneralUtil.setUserfont(Userfont);

            inputField.setForeground(Usercolor);
            outputArea.setForeground(Usercolor);

            Color c = mainGeneralUtil.hextorgbColor(mainGeneralUtil.getUserData("Background"));

            if (mainGeneralUtil.getUserData("OutputFill").equals("1"))
                outputArea.setBackground(new Color(c.getRed(),c.getGreen(),c.getBlue(),Integer.parseInt(mainGeneralUtil.getUserData("Opacity"))));

            if (mainGeneralUtil.getUserData("InputFill").equals("1"))
                inputField.setBackground(new Color(c.getRed(),c.getGreen(),c.getBlue(),Integer.parseInt(mainGeneralUtil.getUserData("Opacity"))));

            inputField.setFont(Userfont);
            outputArea.setFont(Userfont);

            suggestionButton = new JButton("");
            suggestionButton.setToolTipText("Suggestions");
            suggestionButton.addActionListener(e -> {
                println("What feature would you like to suggestion? (Please include as much detail as possible such as what" +
                        "key words you should type and how it should be responded to and any options you think might be necessary)");
                mainGeneralUtil.setUserInputDesc("suggestion");
                mainGeneralUtil.setUserInputMode(true);
                inputField.requestFocus();
            });

            suggestionButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("src/com/cyder/io/pictures/suggestion2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("src/com/cyder/io/pictures/suggestion1.png"));
                }
            });

            suggestionButton.setBounds(32, 4, 22, 22);

            ImageIcon DebugIcon = new ImageIcon("src/com/cyder/io/pictures/suggestion1.png");

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

            ImageIcon MenuIcon = new ImageIcon("src/com/cyder/io/pictures/menuSide1.png");

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
                mainGeneralUtil.minimizeAnimation(consoleFrame);
                updateConsoleClock = false;
                consoleFrame.setState(Frame.ICONIFIED);
                minimizeMenu();
            });

            minimize.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    minimize.setIcon(new ImageIcon("src/com/cyder/io/pictures/Minimize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    minimize.setIcon(new ImageIcon("src/com/cyder/io/pictures/Minimize1.png"));
                }
            });

            minimize.setBounds(mainGeneralUtil.getBackgroundX() - 81, 4, 22, 20);

            ImageIcon mini = new ImageIcon("src/com/cyder/io/pictures/Minimize1.png");
            minimize.setIcon(mini);
            parentLabel.add(minimize);
            minimize.setFocusPainted(false);
            minimize.setOpaque(false);
            minimize.setContentAreaFilled(false);
            minimize.setBorderPainted(false);

            alternateBackground = new JButton("");
            alternateBackground.setToolTipText("Alternate Background");
            alternateBackground.addActionListener(e -> {
                mainGeneralUtil.initBackgrounds();

                if (mainGeneralUtil.canSwitchBackground() && mainGeneralUtil.getValidBackgroundPaths().length > 1) {
                    mainGeneralUtil.setCurrentBackgroundIndex(mainGeneralUtil.getCurrentBackgroundIndex() + 1);
                    switchBackground();
                }

                else if (mainGeneralUtil.OnLastBackground() && mainGeneralUtil.getValidBackgroundPaths().length > 1) {
                    mainGeneralUtil.setCurrentBackgroundIndex(0);
                    switchBackground();
                }

                else if (mainGeneralUtil.getValidBackgroundPaths().length == 1) {
                    println("You only have one background image. Would you like to add more? (Enter yes/no)");
                    inputField.requestFocus();
                    mainGeneralUtil.setUserInputMode(true);
                    mainGeneralUtil.setUserInputDesc("addbackgrounds");
                    inputField.requestFocus();
                }

                else {
                    try {
                        mainGeneralUtil.handle(new FatalException("Background DNE"));
                        println("Error in parsing background; perhaps it was deleted.");
                    }

                    catch (Exception ex) {
                        mainGeneralUtil.handle(ex);
                    }
                }
            });

            alternateBackground.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("src/com/cyder/io/pictures/ChangeSize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("src/com/cyder/io/pictures/ChangeSize1.png"));
                }
            });

            alternateBackground.setBounds(mainGeneralUtil.getBackgroundX() - 54, 4, 22, 20);

            ImageIcon Size = new ImageIcon("src/com/cyder/io/pictures/ChangeSize1.png");
            alternateBackground.setIcon(Size);

            parentLabel.add(alternateBackground);

            alternateBackground.setFocusPainted(false);
            alternateBackground.setOpaque(false);
            alternateBackground.setContentAreaFilled(false);
            alternateBackground.setBorderPainted(false);

            close = new JButton("");
            close.setToolTipText("Close");
            close.addActionListener(e -> {
                if (loginFrame != null && loginFrame.isVisible()) {
                    mainGeneralUtil.closeAnimation(consoleFrame);
                    consoleFrame = null;
                }

                else
                    exit();
            });

            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    close.setIcon(new ImageIcon("src/com/cyder/io/pictures/Close2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(new ImageIcon("src/com/cyder/io/pictures/Close1.png"));
                }
            });

            close.setBounds(mainGeneralUtil.getBackgroundX() - 27, 4, 22, 20);

            ImageIcon exit = new ImageIcon("src/com/cyder/io/pictures/Close1.png");

            close.setIcon(exit);

            parentLabel.add(close);

            close.setFocusPainted(false);
            close.setOpaque(false);
            close.setContentAreaFilled(false);
            close.setBorderPainted(false);

            consoleDragLabel = new JLabel();
            consoleDragLabel.setBounds(0,0, mainGeneralUtil.getBackgroundX(),30);
            consoleDragLabel.setOpaque(true);
            consoleDragLabel.setBackground(new Color(26,32,51));
            consoleDragLabel.addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getXOnScreen();
                    int y = e.getYOnScreen();

                    if (consoleFrame != null && consoleFrame.isFocused()) {
                        consoleFrame.setLocation(x - xMouse, y - yMouse);
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    xMouse = e.getX();
                    yMouse = e.getY();
                }
            });

            consoleClockLabel = new JLabel(mainGeneralUtil.consoleTime());
            consoleClockLabel.setFont(mainGeneralUtil.weatherFontSmall.deriveFont(20f));
            consoleClockLabel.setForeground(mainGeneralUtil.vanila);
            consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 20,
                    2,(consoleClockLabel.getText().length() * 17), 25);

            consoleDragLabel.add(consoleClockLabel, SwingConstants.CENTER);

            updateConsoleClock = mainGeneralUtil.getUserData("ClockOnConsole").equalsIgnoreCase("1");

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (consoleClockLabel.isVisible())
                    if (mainGeneralUtil.getUserData("ShowSeconds").equalsIgnoreCase("1"))
                        consoleClockLabel.setText(mainGeneralUtil.consoleSecondTime());
                    else
                        consoleClockLabel.setText(mainGeneralUtil.consoleTime());

                consoleClockLabel.setToolTipText(mainGeneralUtil.weatherThreadTime());

            },0, 500, TimeUnit.MILLISECONDS);

            consoleClockLabel.setVisible(updateConsoleClock);

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (mainGeneralUtil.getUserData("HourlyChimes").equalsIgnoreCase("1"))
                    mainGeneralUtil.playMusic("src/com/cyder/io/audio/chime.mp3");

            }, 3600 - LocalDateTime.now().getSecond() - LocalDateTime.now().getMinute() * 60, 3600, TimeUnit.SECONDS);

            parentLabel.add(consoleDragLabel);

            consoleFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeiconified(WindowEvent e) {
                    updateConsoleClock = true;
                    consoleFrame.setLocation(restoreX, restoreY);
                }
            });

            if (mainGeneralUtil.getUserData("RandomBackground").equals("1")) {
                int len = mainGeneralUtil.getValidBackgroundPaths().length;

                if (len <= 1)
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you only have one background file so there's no random element to be chosen.");

                else if (len > 1) {
                    try {
                        File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();

                        mainGeneralUtil.setCurrentBackgroundIndex(mainGeneralUtil.randInt(0, (backgrounds.length) - 1));

                        String newBackFile = mainGeneralUtil.getCurrentBackground().toString();

                        ImageIcon newBack;
                        int tempW = 0;
                        int tempH = 0;

                        if (mainGeneralUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                            newBack = new ImageIcon(mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(),
                                    (int) mainGeneralUtil.getScreenSize().getHeight(), new File(newBackFile)));
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        }

                        else {
                            newBack = new ImageIcon(newBackFile);
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        }

                        mainGeneralUtil.getBackgroundSize();

                        parentLabel.setIcon(newBack);

                        consoleFrame.setBounds(0, 0, tempW, tempH);
                        parentPane.setBounds(0, 0,  tempW, tempH);
                        parentLabel.setBounds(0, 0,  tempW, tempH);

                        outputArea.setBounds(0, 0, tempW - 20, tempH - 204);
                        outputScroll.setBounds(10, 62, tempW - 20, tempH - 204);
                        inputField.setBounds(10, 82 + outputArea.getHeight(), tempW - 20, tempH - (outputArea.getHeight() + 62 + 40));
                        consoleDragLabel.setBounds(0,0,tempW,30);
                        minimize.setBounds(tempW - 81, 4, 22, 20);
                        alternateBackground.setBounds(tempW - 54, 4, 22, 20);
                        close.setBounds(tempW - 27, 4, 22, 20);

                        inputField.requestFocus();

                        parentLabel.setIcon(newBack);

                        parentLabel.setToolTipText(mainGeneralUtil.getCurrentBackground().getName().replace(".png", ""));
                        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 20,
                                2,(consoleClockLabel.getText().length() * 17), 25);
                    }

                    catch (Exception e) {
                        mainGeneralUtil.handle(e);
                    }
                }

                else
                   throw new FatalException("Only one but also more than one background.");
            }

            mainGeneralUtil.startAnimation(consoleFrame);

            new Thread(() -> {
                if (!mainGeneralUtil.internetReachable())
                    notify("Internet connection slow or unavailble",
                            3000, Notification.TOP_ARROW, Notification.TOP_VANISH, parentPane,450);
            },"slow-internet-checker").start();


            if (mainGeneralUtil.getUserData("DebugWindows").equals("1")) {
                mainGeneralUtil.systemProperties();
                mainGeneralUtil.computerProperties();
                mainGeneralUtil.javaProperties();
                mainGeneralUtil.debugMenu(outputArea);
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private MouseAdapter consoleMenu = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src/com/cyder/io/pictures/menu2.png"));

                menuLabel = new JLabel("");
                menuLabel.setOpaque(true);
                menuLabel.setBackground(new Color(26,32,51));

                parentPane.add(menuLabel,1,0);

                menuLabel.setBounds(-150,30, 130,260);
                menuLabel.setVisible(true);

                JLabel calculatorLabel = new JLabel("Calculator");
                calculatorLabel.setFont(mainGeneralUtil.weatherFontSmall);
                calculatorLabel.setForeground(mainGeneralUtil.vanila);
                calculatorLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Calculator c = new Calculator();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        calculatorLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        calculatorLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                menuLabel.add(calculatorLabel);
                calculatorLabel.setBounds(5,20,150,20);

                JLabel musicLabel = new JLabel("Music");
                musicLabel.setFont(mainGeneralUtil.weatherFontSmall);
                musicLabel.setForeground(mainGeneralUtil.vanila);
                musicLabel.setBounds(5,50,150,20);
                menuLabel.add(musicLabel);
                musicLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mainGeneralUtil.mp3("", mainGeneralUtil.getUsername(), mainGeneralUtil.getUserUUID());
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        musicLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        musicLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel weatherLabel = new JLabel("Weather");
                weatherLabel.setFont(mainGeneralUtil.weatherFontSmall);
                weatherLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(weatherLabel);
                weatherLabel.setBounds(5,80,150,20);
                weatherLabel.setOpaque(false);
                weatherLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        WeatherWidget ww = new WeatherWidget();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        weatherLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        weatherLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel noteLabel = new JLabel("Notes");
                noteLabel.setFont(mainGeneralUtil.weatherFontSmall);
                noteLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(noteLabel);
                noteLabel.setBounds(5,110,150,20);
                noteLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        userNotes = new Notes(mainGeneralUtil.getUserUUID());
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        noteLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        noteLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel editUserLabel = new JLabel("Edit user");
                editUserLabel.setFont(mainGeneralUtil.weatherFontSmall);
                editUserLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(editUserLabel);
                editUserLabel.setBounds(5,140,150,20);
                editUserLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        editUser();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        editUserLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        editUserLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel temperatureLabel = new JLabel("Temp conv");
                temperatureLabel.setFont(mainGeneralUtil.weatherFontSmall);
                temperatureLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(temperatureLabel);
                temperatureLabel.setBounds(5,170,150,20);
                temperatureLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        TempConverter tc = new TempConverter();
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        temperatureLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        temperatureLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel youtubeLabel = new JLabel("YouTube");
                youtubeLabel.setFont(mainGeneralUtil.weatherFontSmall);
                youtubeLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(youtubeLabel);
                youtubeLabel.setBounds(5,200,150,20);
                youtubeLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mainGeneralUtil.internetConnect("https://youtube.com");
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        youtubeLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        youtubeLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                JLabel twitterLabel = new JLabel("Twitter");
                twitterLabel.setFont(mainGeneralUtil.weatherFontSmall);
                twitterLabel.setForeground(mainGeneralUtil.vanila);
                menuLabel.add(twitterLabel);
                twitterLabel.setBounds(5,230,150,20);
                twitterLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mainGeneralUtil.internetConnect("https://twitter.com");
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        twitterLabel.setForeground(mainGeneralUtil.regularRed);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        twitterLabel.setForeground(mainGeneralUtil.vanila);
                    }
                });

                animation.jLabelXRight(-150,0,10,8, menuLabel);
            }

            else if (menuLabel.isVisible()){
                minimizeMenu();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src/com/cyder/io/pictures/menu2.png"));
            }

            else {
                menuButton.setIcon(new ImageIcon("src/com/cyder/io/pictures/menuSide2.png"));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src/com/cyder/io/pictures/menu1.png"));
            }

            else {
                menuButton.setIcon(new ImageIcon("src/com/cyder/io/pictures/menuSide1.png"));
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
                }

                else if (code == KeyEvent.VK_UP) {
                    boolean Found = false;

                    for (int i = 0; i < operationList.size() ; i++) {
                        if (operationList.get(i).equals(inputField.getText())) {
                            Found = true;
                            break;
                        }

                        else if (!operationList.get(i).equals(inputField.getText()) && i == operationList.size() - 1) {
                            Found = false;
                            break;
                        }
                    }

                    if (inputField.getText() == null || inputField.getText().equals("")) {
                        mainGeneralUtil.setCurrentDowns(0);
                    }

                    else if (!Found) {
                        mainGeneralUtil.setCurrentDowns(0);
                    }

                    if (scrollingIndex - 1 >= 0) {
                        if (mainGeneralUtil.getCurrentDowns() != 0) {
                            scrollingIndex = scrollingIndex - 1;
                        }

                        inputField.setText(operationList.get(scrollingIndex));
                        mainGeneralUtil.setCurrentDowns(mainGeneralUtil.getCurrentDowns() + 1);
                    }

                    if (operationList.size() == 1) {
                        inputField.setText(operationList.get(0));
                    }
                }

                for (int i = 61440 ; i < 61452 ; i++) {
                    if (code == i) {
                        int seventeen = (i - 61427);

                        if (seventeen == 17)
                            mainGeneralUtil.playMusic("src/com/cyder/io/audio/f17.mp3");
                        else
                           println("Interesting F" + (i - 61427) + " key");
                    }
                }
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
        }
    };

    //when we first launch this will check for any special days in the special days class
    private WindowAdapter consoleEcho = new WindowAdapter() {
        public void windowOpened(WindowEvent e) {
        inputField.requestFocus();
        specialDayNotifier = new SpecialDay(parentPane);
        }
    };

    private void backgroundProcessChecker() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (consoleFrame != null) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                int threadCount = 0;

                for (int i = 0; i < num ; i++)
                    if (!printThreads[i].isDaemon() &&
                        !printThreads[i].getName().contains("pool") &&
                        !printThreads[i].getName().contains("AWT-EventQueue-0") &&
                        !printThreads[i].getName().contains("DestroyJavaVM"))

                        threadCount++;

                if (threadCount > 0)
                    consoleFrame.setIconImage(mainGeneralUtil.getCyderIconBlink().getImage());

                else
                    consoleFrame.setIconImage(mainGeneralUtil.getCyderIcon().getImage());
            }

        }, 0, 3, TimeUnit.SECONDS);
    }

    private Action inputFieldAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
        try {
            String originalOp = inputField.getText().trim();
            String op = originalOp;

            if (!mainGeneralUtil.empytStr(op)) {
                if (!(operationList.size() > 0 && operationList.get(operationList.size() - 1).equals(op))) {
                    operationList.add(op);
                }

                scrollingIndex = operationList.size() - 1;
                mainGeneralUtil.setCurrentDowns(0);

                if (!mainGeneralUtil.getUserInputMode()) {
                    handle(op);
                }

                else if (mainGeneralUtil.getUserInputMode()) {
                    mainGeneralUtil.setUserInputMode(false);
                    handleSecond(op);
                }
            }

            inputField.setText("");
        }

        catch (Exception ex) {
            mainGeneralUtil.handle(ex);
        }
        }
    };

    private void login(boolean AlreadyOpen) {
        if (loginFrame != null) {
            mainGeneralUtil.closeAnimation(loginFrame);
        }

        mainGeneralUtil.cleanUpUsers();

        //todo make cyderframe
        loginFrame = new JFrame();
        loginFrame.setUndecorated(true);

        if (!AlreadyOpen) {
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        else if (AlreadyOpen) {
            loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        loginFrame.setBounds(0, 0, 440, 520);
        loginFrame.setTitle("Cyder login");
        loginFrame.setIconImage(mainGeneralUtil.getCyderIcon().getImage());

        loginLabel = new JLabel();
        loginLabel.setVerticalTextPosition(SwingConstants.TOP);
        loginLabel.setVerticalAlignment(SwingConstants.TOP);
        loginLabel.setIcon(new ImageIcon("src/com/cyder/io/pictures/login.png"));
        loginLabel.setBounds(0, 0, 440, 520);
        loginLabel.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));

        loginFrame.setContentPane(loginLabel);

        loginLabel2 = new JLabel();
        loginLabel2.setVerticalTextPosition(SwingConstants.TOP);
        loginLabel2.setVerticalAlignment(SwingConstants.TOP);
        loginLabel2.setIcon(new ImageIcon("src/com/cyder/io/pictures/Login2.png"));
        loginLabel2.setBounds(440,0 , 440, 520);

        loginLabel.add(loginLabel2);

        loginLabel3 = new JLabel();
        loginLabel3.setVerticalTextPosition(SwingConstants.TOP);
        loginLabel3.setVerticalAlignment(SwingConstants.TOP);
        loginLabel3.setIcon(new ImageIcon("src/com/cyder/io/pictures/Login3.png"));
        loginLabel3.setBounds(880,0 , 440, 520);

        loginLabel.add(loginLabel3);

        loginAnimation();

        DragLabel LoginDragLabel = new DragLabel(440,30,loginFrame);
        JLabel buildLabel = new JLabel("Build " + mainGeneralUtil.getCyderVer());
        buildLabel.setForeground(mainGeneralUtil.vanila);
        buildLabel.setFont(mainGeneralUtil.weatherFontSmall.deriveFont(20f));
        buildLabel.setBounds(LoginDragLabel.getWidth() / 2 - (buildLabel.getText().length() * 11)/2,
                2,(buildLabel.getText().length() * 17), 25);
        LoginDragLabel.add(buildLabel);
        loginLabel.add(LoginDragLabel);

        nameField = new JTextField(20);
        nameField.setToolTipText("Username");
        nameField.setBounds(64,279,327,41);
        nameField.setBackground(new Color(0,0,0,0));
        nameField.setSelectionColor(mainGeneralUtil.selectionColor);
        nameField.setBorder(null);
        nameField.setFont(mainGeneralUtil.weatherFontSmall.deriveFont(30f));
        nameField.setForeground(new Color(42,52,61));
        nameField.setCaretColor(mainGeneralUtil.navy);
        nameField.addActionListener(e -> nameField.requestFocusInWindow());
        nameField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (nameField.getText().length() == 1) {
                    nameField.setText(nameField.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (nameField.getText().length() == 1) {
                    nameField.setText(nameField.getText().toUpperCase());
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (nameField.getText().length() == 1) {
                    nameField.setText(nameField.getText().toUpperCase());
                }
            }
        });

        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (nameField.getText().length() > 15) {
                evt.consume();
            }

            if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                pass.requestFocus();
            }
            }
        });

        nameField.setBorder(BorderFactory.createEmptyBorder());
        nameField.setOpaque(false);

        loginLabel.add(nameField);

        pass = new JPasswordField();
        pass.setToolTipText("Password");
        pass.setBounds(64,348,327,41);
        pass.setBackground(new Color(0,0,0,0));
        pass.setSelectionColor(mainGeneralUtil.selectionColor);
        pass.setBorder(null);
        pass.setFont(mainGeneralUtil.weatherFontBig.deriveFont(50f));
        pass.setForeground(new Color(42,52,61));
        pass.setCaretColor(mainGeneralUtil.navy);
        pass.addActionListener(e -> {
            String Username = nameField.getText().trim();

            if (!mainGeneralUtil.empytStr(Username)) {
                Username = Username.substring(0, 1).toUpperCase() + Username.substring(1);

                char[] Password = pass.getPassword();

                if (!mainGeneralUtil.empytStr(Username)) {
                    recognize(Username, Password);
                }

                for (char c : Password) {
                    c = '\0';
                }
            }
        });

        pass.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (pass.getPassword().length > 30) {
                evt.consume();
            }
            }
        });

        pass.setBorder(BorderFactory.createEmptyBorder());
        pass.setOpaque(false);

        loginLabel.add(pass);

        newUserLabel = new JLabel("Don't have an account?", SwingConstants.CENTER);
        newUserLabel.setFont(new Font("tahoma",Font.BOLD,18));
        newUserLabel.setForeground(mainGeneralUtil.vanila);
        newUserLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                createUser();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                newUserLabel.setText("Create an account!");
                newUserLabel.setForeground(mainGeneralUtil.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newUserLabel.setText("Don't have an account?");
                newUserLabel.setForeground(mainGeneralUtil.vanila);
            }
        });

        newUserLabel.setBounds(89,425,262,33);

        loginLabel.add(newUserLabel);

        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                nameField.requestFocus();
            }
        });

        File Users = new File("src/com/cyder/users/");
        String[] directories = Users.list((current, name) -> new File(current, name).isDirectory());

        mainGeneralUtil.startAnimation(loginFrame);

        if (directories != null && directories.length == 0)
            notify("<html>Psssst! Create a user,<br/>" + System.getProperty("user.name") + "</html>",
                2000, Notification.TOP_ARROW, Notification.TOP_VANISH, loginLabel, 230);
    }

    private void recognize(String Username, char[] Password) {
        try {
            mainGeneralUtil.setUsername(Username);

            if (mainGeneralUtil.checkPassword(Username, mainGeneralUtil.toHexString(mainGeneralUtil.getSHA(Password)))) {
                mainGeneralUtil.readUserData();
                mainGeneralUtil.closeAnimation(loginFrame);

                if (consoleFrame != null)
                    mainGeneralUtil.closeAnimation(consoleFrame);

                console();

                if (mainGeneralUtil.getUserData("IntroMusic").equals("1")) {
                    LinkedList<String> MusicList = new LinkedList<>();

                    File UserMusicDir = new File("src/com/cyder/users/" + mainGeneralUtil.getUserUUID() + "/Music");

                    String[] FileNames = UserMusicDir.list();

                    if (FileNames != null)
                        for (String fileName : FileNames)
                            if (fileName.endsWith(".mp3"))
                                MusicList.add(fileName);

                    if (!MusicList.isEmpty())
                        mainGeneralUtil.playMusic(
                                "src/com/cyder/users/" + mainGeneralUtil.getUserUUID() + "/Music/" +
                                        (FileNames[mainGeneralUtil.randInt(0,FileNames.length - 1)]));
                    else
                        mainGeneralUtil.playMusic("src/com/cyder/io/audio/Suprise.mp3");
                }
            }

            else if (loginFrame.isVisible()){
                nameField.setText("");
                pass.setText("");
                nameField.requestFocusInWindow();
                notify("Could not recognize user",
                        2000, Notification.TOP_ARROW, Notification.TOP_VANISH, loginLabel, 280);
            }

            else {
                login(false);
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void exitFullscreen() {
        mainGeneralUtil.initBackgrounds();
        File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();
        int index = mainGeneralUtil.getCurrentBackgroundIndex();
        String backFile = backgrounds[index].toString();

        int width = 0;
        int height = 0;

        if (mainGeneralUtil.getConsoleDirection() == mainGeneralUtil.CYDER_UP) {
            ImageIcon backIcon = new ImageIcon(backFile);
            width = backIcon.getIconWidth();
            height = backIcon.getIconHeight();
            parentLabel.setIcon(backIcon);
        }

        else if (mainGeneralUtil.getConsoleDirection() == mainGeneralUtil.CYDER_DOWN) {
            ImageIcon backIcon = new ImageIcon(backFile);
            width = backIcon.getIconWidth();
            height = backIcon.getIconHeight();
            parentLabel.setIcon(new ImageIcon(mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().toString())));
        }

        else {
            ImageIcon backIcon = new ImageIcon(backFile);

            if (mainGeneralUtil.getConsoleDirection() == mainGeneralUtil.CYDER_LEFT || mainGeneralUtil.getConsoleDirection() == mainGeneralUtil.CYDER_RIGHT) {
                height = backIcon.getIconWidth();
                width = backIcon.getIconHeight();
            }

            parentLabel.setIcon(new ImageIcon(mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().toString())));
        }

        mainGeneralUtil.getBackgroundSize();

        consoleFrame.setBounds(0, 0, width, height);
        parentPane.setBounds(0, 0,  width, height);
        parentLabel.setBounds(0, 0,  width, height);

        outputArea.setBounds(0, 0, width - 20, height - 204);
        outputScroll.setBounds(10, 62, width - 20, height - 204);
        inputField.setBounds(10, 82 + outputArea.getHeight(), width - 20, height - (outputArea.getHeight() + 62 + 40));
        consoleDragLabel.setBounds(0,0,width,30);
        minimize.setBounds(width - 81, 4, 22, 20);
        alternateBackground.setBounds(width - 54, 4, 22, 20);
        close.setBounds(width - 27, 4, 22, 20);
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 20,
                2,(consoleClockLabel.getText().length() * 17), 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();

        consoleFrame.setLocationRelativeTo(null);

        editUserFrame.setAlwaysOnTop(true);
        editUserFrame.setAlwaysOnTop(false);
    }

    private void refreshFullscreen() {
        mainGeneralUtil.initBackgrounds();
        File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();
        int index = mainGeneralUtil.getCurrentBackgroundIndex();
        String backFile = backgrounds[index].toString();

        ImageIcon backIcon = new ImageIcon(backFile);

        BufferedImage fullimg = mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(),
                (int) mainGeneralUtil.getScreenSize().getHeight(), new File(backFile));
        int fullW = fullimg.getWidth();
        int fullH = fullimg.getHeight();

        parentLabel.setIcon(new ImageIcon(fullimg));

        mainGeneralUtil.getBackgroundSize();

        consoleFrame.setBounds(0, 0, fullW, fullH);
        parentPane.setBounds(0, 0,  fullW, fullH);
        parentLabel.setBounds(0, 0,  fullW, fullH);

        outputArea.setBounds(0, 0, fullW - 20, fullH - 204);
        outputScroll.setBounds(10, 62, fullW - 20, fullH - 204);
        inputField.setBounds(10, 82 + outputArea.getHeight(), fullW - 20, fullH - (outputArea.getHeight() + 62 + 40));
        consoleDragLabel.setBounds(0,0,fullW,30);
        minimize.setBounds(fullW - 81, 4, 22, 20);
        alternateBackground.setBounds(fullW - 54, 4, 22, 20);
        close.setBounds(fullW - 27, 4, 22, 20);
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 20,
                2,(consoleClockLabel.getText().length() * 17), 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();

        consoleFrame.setLocationRelativeTo(null);

        editUserFrame.setAlwaysOnTop(true);
        editUserFrame.setAlwaysOnTop(false);
    }

    private void switchBackground() {
        Thread slideThread = new Thread(() -> {
            try {
                mainGeneralUtil.initBackgrounds();

                File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();
                int oldIndex = (mainGeneralUtil.getCurrentBackgroundIndex() == 0 ? backgrounds.length - 1 : mainGeneralUtil.getCurrentBackgroundIndex() - 1);
                String oldBackFile = backgrounds[oldIndex].toString();
                String newBackFile = mainGeneralUtil.getCurrentBackground().toString();

                ImageIcon oldBack = new ImageIcon(oldBackFile);
                BufferedImage newBack = ImageIO.read(new File(newBackFile));

                BufferedImage temporaryImage;
                int tempW = 0;
                int tempH = 0;
                
                if (mainGeneralUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                    oldBack = new ImageIcon(mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(),
                            (int) mainGeneralUtil.getScreenSize().getHeight(),new File(oldBackFile)));
                    newBack = mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(), (int) mainGeneralUtil.getScreenSize().getHeight(),
                            new File(newBackFile));
                    temporaryImage = mainGeneralUtil.resizeImage((int) mainGeneralUtil.getScreenSize().getWidth(), (int) mainGeneralUtil.getScreenSize().getHeight(),
                            new File(oldBackFile));
                    tempW = temporaryImage.getWidth();
                    tempH = temporaryImage.getHeight();
                }

                else {
                    newBack = mainGeneralUtil.resizeImage(newBack.getWidth(), newBack.getHeight(),new File(newBackFile));
                    temporaryImage = mainGeneralUtil.resizeImage(newBack.getWidth(), newBack.getHeight(), new File(oldBackFile));
                    tempW = temporaryImage.getWidth();
                    tempH = temporaryImage.getHeight();
                }

                mainGeneralUtil.getBackgroundSize();

                consoleFrame.setBounds(0, 0, tempW, tempH);
                parentPane.setBounds(0, 0,  tempW, tempH);
                parentLabel.setBounds(0, 0,  tempW, tempH);

                outputArea.setBounds(0, 0, tempW - 20, tempH - 204);
                outputScroll.setBounds(10, 62, tempW - 20, tempH - 204);
                inputField.setBounds(10, 82 + outputArea.getHeight(), tempW - 20, tempH - (outputArea.getHeight() + 62 + 40));
                consoleDragLabel.setBounds(0,0,tempW,30);
                minimize.setBounds(tempW - 81, 4, 22, 20);
                alternateBackground.setBounds(tempW - 54, 4, 22, 20);
                close.setBounds(tempW - 27, 4, 22, 20);

                consoleFrame.repaint();
                consoleFrame.setVisible(true);
                consoleFrame.requestFocus();
                inputField.requestFocus();

                consoleFrame.setLocationRelativeTo(null);

                if (slidLeft) {
                    temporaryLabel = new JLabel();
                    parentLabel.setIcon(new ImageIcon(newBack));
                    temporaryLabel.setIcon(new ImageIcon(temporaryImage));
                    parentPane.add(temporaryLabel);
                    parentLabel.setBounds(-tempW, 0, tempW, tempH);
                    temporaryLabel.setBounds(0, 0 ,tempW, tempH);

                    int[] parts = getDelayIncrement(tempW);

                    animation.jLabelXRight(0, tempW, parts[0], parts[1], temporaryLabel);
                    animation.jLabelXRight(-tempW, 0 ,parts[0], parts[1], parentLabel);
                }

                else {
                    temporaryLabel = new JLabel();
                    parentLabel.setIcon(new ImageIcon(newBack));
                    temporaryLabel.setIcon(new ImageIcon(temporaryImage));
                    parentPane.add(temporaryLabel);
                    parentLabel.setBounds(tempW, 0, tempW, tempH);
                    temporaryLabel.setBounds(0, 0 ,tempW, tempH);

                    int[] parts = getDelayIncrement(tempW);

                    animation.jLabelXLeft(0, -tempW, parts[0], parts[1], temporaryLabel);
                    animation.jLabelXLeft(tempW, 0 ,parts[0], parts[1], parentLabel);
                }

                slidLeft = !slidLeft;

                parentLabel.setToolTipText(mainGeneralUtil.getCurrentBackground().getName().replace(".png", ""));
                consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 20,
                        2,(consoleClockLabel.getText().length() * 17), 25);
            }

            catch (Exception e) {
                mainGeneralUtil.handle(e);
            }
        });

        slideThread.start();
    }

    private int[] getDelayIncrement(int width) {
        try {
            LinkedList<Integer> divisibles = new LinkedList<>();

            for (int i = 1 ; i <= width / 2 ; i++) {
                if (width % i == 0)
                    divisibles.add(i);
            }

            int desired = 10;
            int distance = Math.abs(divisibles.get(0)- desired);
            int index = 0;

            for(int i = 1; i < divisibles.size(); i++){
                int curDist = Math.abs(divisibles.get(i) - desired);

                if(curDist < distance){
                    index = i;

                    distance = curDist;
                }
            }

            int inc = divisibles.get(index);
            return new int[] {1, inc};
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }

        return null;
    }

    private void loginAnimation() {
        Thread slideThread = new Thread() {
            int count;

            @Override
            public void run() {
                try {
                    while (true) {
                        long scrollDelay = 2000;
                        int miliDelay = 5;
                        int increment = 2;

                        switch (count) {
                            case 0:
                                loginLabel.setBounds(0,0,440,520);
                                loginLabel2.setBounds(440,0,440,520);
                                
                                Thread.sleep(scrollDelay);

                                animation.jLabelXLeft(440, 0 ,miliDelay, increment, loginLabel2);

                                Thread.sleep(scrollDelay);

                                count = 1;
                                break;
                            case 1:
                                Thread.sleep(scrollDelay);

                                loginLabel2.setBounds(0,0,440,520);
                                loginLabel3.setBounds(440,0,440,520);
                                animation.jLabelXLeft(0, -440, miliDelay, increment, loginLabel2);
                                animation.jLabelXLeft(440, 0 ,miliDelay, increment, loginLabel3);

                                Thread.sleep(scrollDelay);

                                count = 2;
                                break;
                            case 2:
                                Thread.sleep(scrollDelay);

                                loginLabel3.setBounds(0,0,440,520);
                                loginLabel2.setBounds(-440,0,440,520);
                                animation.jLabelXRight(0, 440, miliDelay, increment, loginLabel3);
                                animation.jLabelXRight(-440,0,miliDelay,increment, loginLabel2);

                                Thread.sleep(scrollDelay);

                                count = 1;
                                break;
                        }
                    }
                }

                catch (Exception e) {
                    mainGeneralUtil.handle(e);
                }
            }
        };

        slideThread.start();
    }

    private void clc() {
        outputArea.setText("");
        inputField.setText("");
    }

    private void handleSecond(String input) {
        try {
            String desc = mainGeneralUtil.getUserInputDesc();

            if (desc.equalsIgnoreCase("url") && !mainGeneralUtil.empytStr(input)) {
                URI URI = new URI(input);
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect(URI);
            }

            else if (desc.equalsIgnoreCase("prime") && input != null && !input.equals("")) {
                int num = Integer.parseInt(input);

                if (num <= 0) {
                    println("The inger " + num + " is not a prime number because it is negative.");
                }

                else if (num == 1) {
                    println("The inger 1 is not a prime number by the definition of a prime number.");
                }

                else if (num == 2) {
                    println("The integer 2 is indeed a prime number.");
                }

                ArrayList<Integer> Numbers = new ArrayList<>();

                for (int i = 3 ; i < Math.ceil(Math.sqrt(num)) ; i += 2) {
                    if (num % i == 0) {
                        Numbers.add(i);
                    }
                }

                if (Numbers.isEmpty()) {
                    println("The integer " + num + " is indeed a prime number.");
                }

                else {
                    println("The integer " + num + " is not a prime number because it is divisible by " + Numbers);
                }
            }

            else if (desc.equalsIgnoreCase("google") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect("https://www.google.com/search?q=" + input);
            }

            else if (desc.equalsIgnoreCase("youtube")&& input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect("https://www.youtube.com/results?search_query=" + input);
            }

            else if (desc.equalsIgnoreCase("math") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect("https://www.wolframalpha.com/input/?i=" + input);
            }

            else if (desc.equalsIgnoreCase("binary")) {
                if (input.matches("[0-9]+") && !mainGeneralUtil.empytStr(input)) {
                    String Print = mainGeneralUtil.toBinary(Integer.parseInt(input));
                    println(input + " converted to binary equals: " + Print);
                }

                else {
                    println("Your value must only contain numbers.");
                }
            }

            else if (desc.equalsIgnoreCase("wiki") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ","_");
                println("Attempting to connect...");
                mainGeneralUtil.internetConnect("https://en.wikipedia.org/wiki/" + input);
            }

            else if (desc.equalsIgnoreCase("disco") && input != null && !input.equals("")) {
                println("I hope you're not the only one at this party.");
                mainGeneralUtil.disco(Integer.parseInt(input));
            }

            else if (desc.equalsIgnoreCase("youtube word search") && input != null && !input.equals("")) {
                String browse = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
                browse = browse.replace("REPLACE", input).replace(" ", "+");
                mainGeneralUtil.internetConnect(browse);
            }

            else if (desc.equalsIgnoreCase("random youtube")) {
               try {
                    int threads = Integer.parseInt(input);

                    notify("The" + (threads > 1 ? " scripts have " : " script has ") + "started. At any point, type \"stop script\"",
                            4000, Notification.TOP_ARROW, Notification.TOP_VANISH, parentPane, (threads > 1 ? 620 : 610));

                    for (int i = 0 ; i < threads ; i++) {
                        YoutubeThread current = new YoutubeThread(outputArea);
                        youtubeThreads.add(current);
                    }
                }

                catch (NumberFormatException e) {
                    println("Invalid input for number of threads to start.");
                }

               catch (Exception e) {
                   mainGeneralUtil.handle(e);
               }
            }

            else if (desc.equalsIgnoreCase("anagram1")) {
                println("Enter your second word");
                anagram = input;
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("anagram2");
            }

            else if (desc.equalsIgnoreCase("anagram2")) {
                if (anagram.length() != input.length()) {
                    println("These words are not anagrams of each other.");
                }

                else if (anagram.equalsIgnoreCase(input)) {
                    println("These words are in fact anagrams of each other.");
                }

                else {
                    char[] W1C = anagram.toLowerCase().toCharArray();
                    char[] W2C = input.toLowerCase().toCharArray();
                    Arrays.sort(W1C);
                    Arrays.sort(W2C);

                    if (Arrays.equals(W1C, W2C)) {
                        println("These words are in fact anagrams of each other.");
                    }

                    else {
                        println("These words are not anagrams of each other.");
                    }
                }

                anagram = "";
            }

            else if (desc.equalsIgnoreCase("pixelate") && input != null && !input.equals("")) {
                println("Pixelating " + pixelateFile.getName() + " with a pixel block size of " + input + "...");
                mainGeneralUtil.pixelate(pixelateFile, Integer.parseInt(input));
            }

            else if (desc.equalsIgnoreCase("alphabetize")) {
                char[] Sorted = input.toCharArray();
                Arrays.sort(Sorted);
                println("\"" + input + "\" alphabetically organized is \"" + new String(Sorted) + "\".");
            }

            else if (desc.equalsIgnoreCase("suggestion")) {
                logToDo(input);
            }

            else if (desc.equalsIgnoreCase("addbackgrounds")) {
                if (mainGeneralUtil.confirmation(input)) {
                    editUser();
                    mainGeneralUtil.internetConnect("https://images.google.com/");
                }

                else
                    println("Okay nevermind then");
            }

            else if (desc.equalsIgnoreCase("logoff")) {
                if (mainGeneralUtil.confirmation(input)) {
                    String shutdownCmd = "shutdown -l";
                    Runtime.getRuntime().exec(shutdownCmd);
                }

                else
                    println("Okay nevermind then");
            }

            else if (desc.equalsIgnoreCase("deleteuser")) {
                if (!mainGeneralUtil.confirmation(input)) {
                    println("User " + mainGeneralUtil.getUsername() + " was not removed.");
                    return;
                }

                mainGeneralUtil.closeAnimation(consoleFrame);
                mainGeneralUtil.deleteFolder(new File("src/com/cyder/users/" + mainGeneralUtil.getUserUUID()));

                String dep = mainGeneralUtil.getDeprecatedUUID();

                File renamed = new File("src/com/cyder/users/" + dep);
                while (renamed.exists()) {
                    dep = mainGeneralUtil.getDeprecatedUUID();
                    renamed = new File("src/com/cyder/users/" + dep);
                }

                File old = new File("src/com/cyder/users/" + mainGeneralUtil.getUserUUID());
                old.renameTo(renamed);

                login(false);
            }

            else if (desc.equalsIgnoreCase("pixelatebackground")) {
                BufferedImage img = ImageUtil.pixelate(ImageIO.read(mainGeneralUtil.getCurrentBackground().getAbsoluteFile()), Integer.parseInt(input));

                String searchName = mainGeneralUtil.getCurrentBackground().getName().replace(".png", "")
                        + "_Pixelated_Pixel_Size_" + Integer.parseInt(input) + ".png";

                File saveFile = new File("src/com/cyder/users/" + mainGeneralUtil.getUserUUID() +
                        "/Backgrounds/" + searchName);

                ImageIO.write(img, "png", saveFile);

                mainGeneralUtil.initBackgrounds();

                File[] backgrounds = mainGeneralUtil.getValidBackgroundPaths();

                for (int i = 0 ; i < backgrounds.length ; i++) {
                    if (backgrounds[i].getName().equals(searchName)) {
                        parentLabel.setIcon(new ImageIcon(backgrounds[i].toString()));
                        parentLabel.setToolTipText(backgrounds[i].getName().replace(".png",""));
                        mainGeneralUtil.setCurrentBackgroundIndex(i);
                    }
                }

                println("Background pixelated and saved as a separate background file.");

                exitFullscreen();
            }

            else if (desc.equalsIgnoreCase("test notify one")) {
                notificationTestString = input;
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("test notify two");
                println("Enter notify width in pixels");
            }

            else if (desc.equalsIgnoreCase("test notify two")) {
                notificaitonTestWidth = Integer.parseInt(input);
                notify(notificationTestString, 2000,
                        Notification.TOP_ARROW, Notification.TOP_VANISH, parentPane, notificaitonTestWidth);
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void handle(String input) {
        try {
            operation = input;

            String firstWord = mainGeneralUtil.firstWord(operation);

            mainGeneralUtil.setHandledMath(false);

            handleMath(operation);

            if (mainGeneralUtil.filter(operation)) {
                println("Sorry, " + mainGeneralUtil.getUsername() + ", but that language is prohibited.");
                operation = "";
            }

            else if (mainGeneralUtil.isPalindrome(operation.replace(" ", "").toCharArray()) && operation.length() > 3){
                println("Nice palindrome.");
            }

            else if (((hasWord("quit") && !hasWord("db")) ||
                    (eic("leave") || (hasWord("stop") && !hasWord("music") && !hasWord("script") && !hasWord("scripts")) ||
                            hasWord("exit") || eic("close"))) && !has("dance"))
            {
                exit();
            }

            else if (hasWord("test") && hasWord("notify")) {
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("test notify one");
                println("Enter notify string");
            }

            else if (hasWord("consolidate") && (hasWord("windows") || hasWord("frames"))) {
                Frame[] frames = Frame.getFrames();

                int x = consoleFrame.getX();
                int y = consoleFrame.getY();

                for(Frame f: frames)
                   if (f.isVisible())
                       f.setLocation(x,y);
            }

            else if (hasWord("bletchy")) {
                stringUtil.setOutputArea(outputArea);
                stringUtil.bletchy(operation,false,50);
            }

            else if ((hasWord("flip") &&  hasWord("coin")) || (hasWord("heads") && hasWord("tails"))) {
                if (Math.random() <= 0.0001) {
                    println("You're not going to beleive this, but it landed on its side.");
                }

                else if (Math.random() <= 0.5) {
                    println("It's Heads!");
                }

                else {
                    println("It's Tails!");
                }
            }

            else if ((eic("hello") || has("whats up") || hasWord("hi"))
                    && (!hasWord("print") &&  !hasWord("bletchy") && !hasWord("echo") &&
                    !hasWord("youtube") && !hasWord("google") && !hasWord("wikipedia") &&
                    !hasWord("synonym") && !hasWord("define"))) {
                int choice = mainGeneralUtil.randInt(1,6);

                switch(choice) {
                    case 1:
                        println("Hello " + mainGeneralUtil.getUsername() + ".");
                        break;
                    case 2:
                        println("Hi " + mainGeneralUtil.getUsername() + "." );
                        break;
                    case 3:
                        println("What's up " + mainGeneralUtil.getUsername() + "?");
                        break;
                    case 4:
                        println("How are you doing, " + mainGeneralUtil.getUsername() + "?");
                        break;
                    case 5:
                        println("Greetings, human " + mainGeneralUtil.getUsername() + ".");
                        break;
                    case 6:
                        println("Hi, " + mainGeneralUtil.getUsername() + ", I'm Cyder.");
                        break;
                }
            }

            else if (hasWord("bye") || (hasWord("james") && hasWord("arthur"))) {
                println("Just say you won't let go.");
            }

            else if (hasWord("time") && hasWord("what")) {
                println(mainGeneralUtil.weatherTime());
            }

            else if (eic("die") || (hasWord("roll") && hasWord("die"))) {
                int Roll = ThreadLocalRandom.current().nextInt(1, 7);
                println("You rolled a " + Roll + ".");
            }

            else if (eic("lol")) {
                println("My memes are better.");
            }

            else if ((hasWord("thank") && hasWord("you")) || hasWord("thanks")) {
                println("You're welcome.");
            }

            else if (hasWord("you") && hasWord("cool")) {
                println("I know.");
            }

            else if (has("paint")) {
                String param = "C:/Windows/system32/mspaint.exe";
                Runtime.getRuntime().exec(param);
            }

            else if (eic("pi")) {
                println(Math.PI);
            }

            else if (hasWord("euler") || eic("e")) {
                println("Leonhard Euler's number is " + Math.E);
            }

            else if (hasWord("scrub")) {
                stringUtil.setOutputArea(outputArea);
                stringUtil.bletchy("No you!",false,50);
            }

            else if (eic("break;")) {
                println("Thankfully I am over my infinite while loop days.");
            }

            else if (hasWord("url")) {
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("url");
                println("Enter your desired URL");
            }

            else if (hasWord("temperature") || eic("temp")) {
                TempConverter tc = new TempConverter();
            }

            else if (has("click me")) {
                mainGeneralUtil.clickMe();
            }

            else if ((hasWord("how") && hasWord("are") && hasWord("you")) && !hasWord("age") && !hasWord("old")) {
                println("I am feeling like a programmed response. Thank you for asking.");
            }

            else if (hasWord("how") && hasWord("day")) {
                println("I was having fun until you started asking me questions.");
            }

            else if (has("How old are you") || (hasWord("what") && hasWord("age"))) {
                stringUtil.setOutputArea(outputArea);
                stringUtil.bletchy("I am 2^8",false,50);
            }

            else if (((hasWord("who") || hasWord("what")) && has("you")) && hasWord("name")) {
                println("I am Cyder (Acronym pending :P)");
            }

            else if (hasWord("helpful") && hasWord("you")) {
                println("I will always do my best to serve you.");
            }

            else if (eic("k")) {
                println("Fun Fact: the letter 'K' comes from the Greek letter kappa, which was taken "
                        + "from the Semitic kap, the symbol for an open hand. It is this very hand which "
                        + "will be slapping you in the face for saying 'k' to me.");
            }


            else if (hasWord("phone") || hasWord("dialer") || hasWord(" call")) {
                Phone p = new Phone();
            }

            else if (hasWord("reset") && hasWord("mouse")) {
                mainGeneralUtil.resetMouse();
            }

            else if (eic("logoff")) {
               println("Are you sure you want to log off your computer?\nThis is not Cyder we are talking about (Enter yes/no)");
               mainGeneralUtil.setUserInputDesc("logoff");
               inputField.requestFocus();
               mainGeneralUtil.setUserInputMode(true);
            }

            else if (eic("clc") || eic("cls") || eic("clear") || (hasWord("clear") && hasWord("screen"))) {
                clc();
            }

            else if (eic("no")) {
                println("Yes");
            }

            else if (eic("nope")) {
                println("yep");
            }

            else if (eic("yes")) {
                println("no");
            }

            else if (eic("yep")) {
                println("nope");
            }

            else if (has("how can I help")) {
                println("That's my line :P");
            }

            else if (hasWord("siri") || hasWord("jarvis") || hasWord("alexa")) {
                println("Whata bunch of losers.");
            }

            else if ((hasWord("mississippi") && hasWord("state") && hasWord("university")) || eic("msu")) {
                printImage("src/com/cyder/io/pictures/msu.png");
            }

            else if (hasWord("toystory")) {
                mainGeneralUtil.playMusic("src/com/cyder/io/audio/TheClaw.mp3");
            }

            else if (has("stop") && has("music")) {
                mainGeneralUtil.stopMusic();
            }

            else if (hasWord("reset") && hasWord("clipboard")) {
                StringSelection selection = new StringSelection(null);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                println("Clipboard has been reset.");
            }

            else if ((has("graphing") && has("calculator")) || has("desmos") || has("graphing")) {
                mainGeneralUtil.internetConnect("https://www.desmos.com/calculator");
            }

            else if (has("airHeads xtremes") || has("candy")) {
                mainGeneralUtil.internetConnect("http://airheads.com/candy#xtremes");
            }

            else if (hasWord("prime")) {
                println("Enter any positive integer and I will tell you if it's prime and what it's divisible by.");
                mainGeneralUtil.setUserInputDesc("prime");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (hasWord("youtube") && (!has("word search") && !has("mode") && !has("random") && !has("thumbnail"))) {
                println("What would you like to search YouTube for?");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("youtube");
            }

            else if ((hasWord("google") && !has("mode") && !has("stupid"))) {
                println("What would you like to Google?");
                mainGeneralUtil.setUserInputDesc("google");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (eic("404")) {
                mainGeneralUtil.internetConnect("http://google.com/=");
            }

            else if (hasWord("calculator") && !has("graphing")) {
                Calculator c = new Calculator();
            }

            else if (firstWord.equalsIgnoreCase("echo")) {
                String[] sentences = operation.split(" ");
                for (int i = 1; i<sentences.length;i++) {
                    print(sentences[i] + " ");
                }

                println("");
            }

            else if ((firstWord.equalsIgnoreCase("print") || firstWord.equalsIgnoreCase("println")) && !has("mode")) {
                String[] sentences = operation.split(" ");

                for (int i = 1 ; i < sentences.length ; i++) {
                    print(sentences[i] + " ");
                }

                println("");
            }

            else if (hasWord("triangle")) {
                mainGeneralUtil.internetConnect("https://www.triangle-calculator.com/");
            }

            else if (hasWord("why")) {
                println("Why not?");
            }

            else if (hasWord("why not")) {
                println("Why?");
            }

            else if (hasWord("groovy")) {
                println("Alright Scooby Doo.");
            }

            else if (eic("dbquit")) {
                println("Debug mode exited");
                mainGeneralUtil.setDebugMode(false);
            }

            else if (hasWord("luck")) {
                if (Math.random() * 100 <= 0.001) {
                    println("YOU WON!!");
                }

                else {
                    println("You are not lucky today.");
                }
            }

            else if (has("are you sure") || has("are you certain")) {
                if (Math.random() <= 0.5) {
                    println("No");
                }

                else {
                    println("Yes");
                }
            }

            else if (has("math") && !eic("mathsh")) {
                println("What math operation would you like to perform?");
                mainGeneralUtil.setUserInputDesc("math");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (eic("nathan")) {
                printlnImage("src/com/cyder/io/pictures/me.png");
            }

            else if (has("always on top mode")) {
                if (hasWord("true")) {
                    println("Always on top mode has been set to true.");
                    mainGeneralUtil.setAlwaysOnTopMode(true);
                    consoleFrame.setAlwaysOnTop(true);
                }

                else if (hasWord("false")) {
                    println("Always on top mode has been set to false.");
                    mainGeneralUtil.setAlwaysOnTopMode(false);
                    consoleFrame.setAlwaysOnTop(false);
                }

                else {
                    println("Please specify the boolean value of always on top mode.");
                }
            }

            else if ((eic("error") || eic("errors")) && !hasWord("throw")) {
                if (mainGeneralUtil.getDebugMode()) {
                    File WhereItIs = new File("src/com/cyder/users/" + mainGeneralUtil.getUserUUID() + "/Throws/");
                    Desktop.getDesktop().open(WhereItIs);
                }

                else {
                    println("There are no errors here.");
                }
            }

            else if (eic("help")) {
                stringUtil.help(outputArea);
            }

            else if (hasWord("light") && hasWord("saber")) {
                mainGeneralUtil.playMusic("src/com/cyder/io/audio/Lightsaber.mp3");
            }

            else if (hasWord("xbox")) {
                mainGeneralUtil.playMusic("src/com/cyder/io/audio/xbox.mp3");
            }

            else if (has("star") && has("trek")) {
                mainGeneralUtil.playMusic("src/com/cyder/io/audio/StarTrek.mp3");
            }

            else if (eic("cmd") || (hasWord("command") && hasWord("prompt"))) {
                File WhereItIs = new File("c:\\Windows\\System32\\cmd.exe");
                Desktop.getDesktop().open(WhereItIs);
            }

            else if (hasWord("shakespeare")) {
                int rand = mainGeneralUtil.randInt(1,2);

                if (rand == 1) {
                    println("Glamis hath murdered sleep, and therefore Cawdor shall sleep no more, Macbeth shall sleep no more.");
                }

                else {
                    println("To be, or not to be, that is the question: Whether 'tis nobler in the mind to suffer the slings and arrows of "
                            + "outrageous fortune, or to take arms against a sea of troubles and by opposing end them.");
                }
            }

            else if (hasWord("windows")) {
                mainGeneralUtil.playMusic("src/com/cyder/io/audio/windows.mp3");
            }

            else if (hasWord("binary")) {
                println("Enter a decimal number to be converted to binary.");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("binary");
            }

            else if (hasWord("pizza")) {
                Pizza p = new Pizza();
            }

            else if (hasWord("imposible")) {
                println("¿Lo es?");
            }

            else if (eic("look")) {
                println("L()()K ---->> !FREE STUFF! <<---- L()()K");
            }

            else if (eic("Cyder?")) {
                println("Yes?");
            }

            else if (firstWord.equalsIgnoreCase("define")) {
                String Define = operation.toLowerCase().replace("'", "").replace(" ", "+").replace("define", "");

                mainGeneralUtil.internetConnect("http://www.dictionary.com/browse/" + Define + "?s=t");
            }

            else if (hasWord("wikipedia")) {
                println("What would you like to look up on Wikipedia?");
                mainGeneralUtil.setUserInputDesc("wiki");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (firstWord.equalsIgnoreCase("synonym")) {
                String Syn = operation.replace("synonym","");
                Syn = Syn.replace("'", "").replace(" ", "+");
                mainGeneralUtil.internetConnect("http://www.thesaurus.com//browse//" + Syn);
            }

            else if (hasWord("board")) {
                mainGeneralUtil.internetConnect("http://gameninja.com//games//fly-squirrel-fly.html");
            }

            else if (hasWord("open cd")) {
                mainGeneralUtil.openCD("D:\\");
            }

            else if (hasWord("close cd")) {
                mainGeneralUtil.closeCD("D:\\");
            }

            else if (hasWord("font") && hasWord("reset")) {
                inputField.setFont(mainGeneralUtil.defaultFont);
                outputArea.setFont(mainGeneralUtil.defaultFont);
                println("The font has been reset.");
            }

            else if (hasWord("reset") && hasWord("color")) {
                outputArea.setForeground(mainGeneralUtil.vanila);
                inputField.setForeground(mainGeneralUtil.vanila);
                println("The text color has been reset.");
            }

            else if (eic("top left")) {
                consoleFrame.setLocation(0,0);
            }

            else if (eic("top right")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int X = (int) rect.getMaxX() - consoleFrame.getWidth();
                consoleFrame.setLocation(X, 0);
            }

            else if (eic("bottom left")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int Y = (int) rect.getMaxY() - consoleFrame.getHeight();
                consoleFrame.setLocation(0, Y);
            }

            else if (eic("bottom right")) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
                int X = (int) rect.getMaxX();
                int Y = (int) rect.getMaxY();
                consoleFrame.setLocation(X - consoleFrame.getWidth(),Y - consoleFrame.getHeight());
            }

            else if (eic("middle") || eic("center")) {
                consoleFrame.setLocationRelativeTo(null);
            }

            else if (hasWord("random") && hasWord("youtube")) {
                println("How many isntances of the script do you want to start?");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("random youtube");
            }

            else if (hasWord("arduino")) {
                mainGeneralUtil.internetConnect("https://www.arduino.cc/");
            }

            else if (has("rasberry pi")) {
                mainGeneralUtil.internetConnect("https://www.raspberrypi.org/");
            }

            else if (eic("&&")) {
                println("||");
            }

            else if (eic("||")) {
                println("&&");
            }

            else if (eic("youtube word search")) {
                println("Enter the desired word you would like to find in a YouTube URL");
                mainGeneralUtil.setUserInputDesc("youtube word search");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
            }

            else if (hasWord("disco")) {
                println("How many iterations would you like to disco for? (Enter a positive integer)");
                mainGeneralUtil.setUserInputMode(true);
                inputField.requestFocus();
                mainGeneralUtil.setUserInputDesc("disco");
            }

            else if (hasWord("game")) {
                File WhereItIs = new File("src/com/cyder/io/jars/Jailbreak.jar");
                Desktop.getDesktop().open(WhereItIs);
            }

            else if (hasWord("there") && hasWord("no") && hasWord("internet")) {
                println("Sucks to be you.");
            }

            else if (eic("i hate you")) {
                println("That's not very nice.");
            }

            else if (eic("netsh")) {
                File WhereItIs = new File("C:\\Windows\\system32\\netsh.exe");

                Desktop.getDesktop().open(WhereItIs);
            }

            else if (hasWord("java") && hasWord("properties")) {
                mainGeneralUtil.javaProperties();
            }

            else if ((hasWord("edit") && hasWord ("user")) || (hasWord("font") && !hasWord("reset")) || (hasWord("color") && !hasWord("reset")) || (eic("preferences") || eic("prefs"))) {
                editUser();
            }

            else if (hasWord("story") && hasWord("tell")) {
                println("It was a lazy day. Cyder was enjoying a deep sleep when suddenly " + mainGeneralUtil.getUsername() + " started talking to Cyder."
                        + " It was at this moment that Cyder knew its day had been ruined.");
            }

            else if (eic("hey")) {
                mainGeneralUtil.playMusic("src/com/cyder/io/audio/heyya.mp3");
            }

            else if (eic("panic")) {
                exit();
            }

            else if (hasWord("hash") || hasWord("hashser")) {
                new Hasher();
            }

            else if (hasWord("home")) {
                println("There's no place like localhost/127.0.0.1");
            }

            else if (eic("search") || eic("dir") || (hasWord("file") && hasWord("search")) || eic("directory") || eic("ls")) {
                DirectorySearch ds = new DirectorySearch();
            }

            else if (hasWord("I") && hasWord("love")) {
                println("Sorry, " + mainGeneralUtil.getUsername() + ", but I don't understand human emotions or affections.");
            }

            else if (hasWord("vexento")) {
                mainGeneralUtil.internetConnect("https://www.youtube.com/user/Vexento/videos");
            }

            else if (hasWord("minecraft")) {
                mw = new MinecraftWidget();
            }

            else if (eic("loop")) {
                println("mainGeneralUtil.handle(\"loop\");");
            }

            else if (hasWord("cyder") && has("dir")) {
                if (mainGeneralUtil.getDebugMode()) {
                    String CurrentDir = System.getProperty("user.dir");
                    mainGeneralUtil.openFile(CurrentDir);
                }

                else {
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you don't have permission to do that.");
                }
            }

            else if ((has("tic") && has("tac") && has("toe")) || eic("TTT")) {
                TicTacToe ttt = new TicTacToe();
                ttt.startTicTacToe();
            }

            else if (hasWord("note") || hasWord("notes")) {
                userNotes = new Notes(mainGeneralUtil.getUserUUID());
            }

            else if ((hasWord("youtube") && hasWord("thumbnail")) || (hasWord("yt") && hasWord("thumb"))) {
                YouTubeThumbnail yttn = new YouTubeThumbnail();
            }

            else if (hasWord("papers") && hasWord("please")) {
                mainGeneralUtil.internetConnect("http://papersplea.se/");
            }

            else if (eic("java")) {
                println("public class main {");
                println("      public static void main(String[] args) {");
                println("            System.out.println(\"Hello World!\");");
                println("      }");
                println("}");
            }

            else if (hasWord("coffee")) {
                mainGeneralUtil.internetConnect("https://www.google.com/search?q=coffe+shops+near+me");
            }

            else if (hasWord("ip")) {
                println(InetAddress.getLocalHost().getHostAddress());
            }

            else if(hasWord("html") || hasWord("html5")) {
                consoleFrame.setIconImage(new ImageIcon("src/com/cyder/io/pictures/html5.png").getImage());
                printlnImage("src/com/cyder/io/pictures/html5.png");
            }

            else if (hasWord("css")) {
                consoleFrame.setIconImage(new ImageIcon("src/com/cyder/io/pictures/css.png").getImage());
                printlnImage("src/com/cyder/io/pictures/css.png");
            }

            else if(hasWord("computer") && hasWord("properties")) {
                println("This may take a second, stand by...");
                mainGeneralUtil.computerProperties();
            }

            else if (hasWord("system") && hasWord("properties")) {
                mainGeneralUtil.systemProperties();
            }

            else if ((hasWord("pixelate") || hasWord("distort")) && (hasWord("image") || hasWord("picture"))) {
                pixelateFile = mainGeneralUtil.getFile();

                if (!pixelateFile.getName().endsWith(".png")) {
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but this feature only supports PNG images");
                }

                else if (pixelateFile != null) {
                    println("Enter your pixel size (Enter a positive integer)");
                    mainGeneralUtil.setUserInputDesc("pixelate");
                    inputField.requestFocus();
                    mainGeneralUtil.setUserInputMode(true);
                }
            }

            else if (hasWord("donuts")) {
                mainGeneralUtil.internetConnect("https://www.dunkindonuts.com/en/food-drinks/donuts/donuts");
            }

            else if (hasWord("anagram")) {
                println("This function will tell you if two"
                        + "words are anagrams of each other."
                        + " Enter your first word");
                mainGeneralUtil.setUserInputDesc("anagram1");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);

            }

            else if (eic("controlc")) {
                mainGeneralUtil.setUserInputMode(false);
                killAllYoutube();
                stringUtil.killBletchy();
                println("Escaped");
            }

            else if (has("alphabet") && (hasWord("sort") || hasWord("organize") || hasWord("arrange"))) {
                println("Enter your word to be alphabetically rearranged");
                inputField.requestFocus();
                mainGeneralUtil.setUserInputMode(true);
                mainGeneralUtil.setUserInputDesc("alphabetize");
            }

            else if (hasWord("mp3") || hasWord("music")) {
                mainGeneralUtil.mp3("", mainGeneralUtil.getUsername(), mainGeneralUtil.getUserUUID());
            }

            else if (hasWord("bai")) {
                mainGeneralUtil.internetConnect("http://www.drinkbai.com");
            }

            else if (has("occam") && hasWord("razor")) {
                mainGeneralUtil.internetConnect("http://en.wikipedia.org/wiki/Occam%27s_razor");
            }

            else if (hasWord("cyder") && (has("picture") || has("image"))) {
                if (mainGeneralUtil.getDebugMode()) {
                    mainGeneralUtil.openFile("src/com/cyder/io/pictures");
                }

                else {
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you do not have permission to access that.");
                }
            }

            else if (hasWord("when") && hasWord("thanksgiving")) {
                int year = Calendar.getInstance().get(Calendar.YEAR);
                LocalDate RealTG = LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
                println("Thanksgiving this year is on the " + RealTG.getDayOfMonth() + " of November.");
            }

            else if (hasWord("location") || (hasWord("where") && hasWord("am") && hasWord("i"))) {
                println("You are currently in " + new InternetProtocolUtil().getUserCity() + ", " +
                        new InternetProtocolUtil().getUserState() + " and your Internet Service Provider is " + new InternetProtocolUtil().getIsp());
            }

            else if (hasWord("fibonacci")) {
                fib(0,1);
            }

            else if (hasWord("throw") && hasWord("error")) {
                throw new CyderException("Error thrown on " + mainGeneralUtil.userTime());
            }

            else if (hasWord("asdf")) {
                println("Who is the spiciest meme lord?");
            }

            else if (hasWord("qwerty")) {
                println("I prefer Dvorak, but I also like Colemak, Maltron, and JCUKEN.");
            }

            else if (hasWord("thor")) {
                println("Piss off, ghost.");
            }

            else if (eic("about:blank")) {
                mainGeneralUtil.internetConnect("about:blank");
            }

            else if (hasWord("weather")) {
                WeatherWidget ww = new WeatherWidget();
            }

            else if (eic("hide")) {
                minimize.doClick();
            }

            else if (hasWord("stop") && hasWord("script")) {
                println("YouTube scripts have been killed.");
                killAllYoutube();
                consoleFrame.setTitle(mainGeneralUtil.getCyderVer() + " [" + mainGeneralUtil.getUsername() + "]");
            }

            else if (hasWord("debug") && hasWord("menu")) {
                mainGeneralUtil.debugMenu(outputArea);
            }

            else if (hasWord("hangman")) {
                Hangman Hanger = new Hangman();
                Hanger.startHangman();
            }

            else if (hasWord("rgb") || hasWord("hex")) {
                mainGeneralUtil.colorConverter();
            }

            else if (hasWord("dance")) {
                mainGeneralUtil.dance(consoleFrame);
            }

            else if (hasWord("clear") && (
                    hasWord("operation") || hasWord("command")) &&
                    hasWord("list")) {
                operationList.clear();
                scrollingIndex = 0;
                println("The operation list has been cleared.");
            }

            else if (eic("pin") || eic("login")) {
                login(true);
            }

            else if ((hasWord("delete") ||
                    hasWord("remove")) &&
                    (hasWord("user") ||
                            hasWord("account"))) {

                println("Are you sure you want to permanently delete this account? This action cannot be undone! (yes/no)");
                mainGeneralUtil.setUserInputMode(true);
                inputField.requestFocus();
                mainGeneralUtil.setUserInputDesc("deleteuser");
            }

            else if ((hasWord("create") || hasWord("new")) &&
                    hasWord("user")) {
                createUser();
            }

            else if (hasWord("pixelate") && hasWord("background")) {
                println("Enter your pixel size (a positive integer)");
                mainGeneralUtil.setUserInputDesc("pixelatebackground");
                mainGeneralUtil.setUserInputMode(true);
                inputField.requestFocus();
            }

            else if (hasWord("long") && hasWord("word")) {
                int count = 0;

                String[] words = operation.split(" ");

                for (String word: words)
                    if (word.equalsIgnoreCase("long"))
                        count++;

                for (int i = 0 ; i < count ; i++)
                    print("pneumonoultramicroscopicsilicovolcanoconiosis");

                println("");
            }

            else if (eic("logic")) {
                mainGeneralUtil.playMusic("src/com/cyder/io/audio/commando.mp3");
            }

            else if (eic("1-800-273-8255") || eic("18002738255")) {
                mainGeneralUtil.playMusic("src/com/cyder/io/audio/1800.mp3");
            }

            else if (hasWord("resize") && (hasWord("image") || hasWord("picture"))) {
                ImageResizer IR = new ImageResizer();
            }

            else if (hasWord("barrel") && hasWord("roll")) {
                barrelRoll();
            }

            else if (hasWord("lines") && hasWord("code")) {
                println("Total lines of code: " + mainGeneralUtil.totalCodeLines(new File(System.getProperty("user.dir"))));
            }

            else if (hasWord("threads") && !hasWord("daemon")) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                for (int i = 0; i < num ; i++)
                    if (!printThreads[i].isDaemon())
                        println(printThreads[i]);
            }

            else if (hasWord("threads") && hasWord("daemon")) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                for (int i = 0; i < num ; i++)
                    println(printThreads[i]);
            }

            else if (eic("askew")) {
                askew();
            }

            else if (hasWord("press") && (hasWord("F17") || hasWord("f17"))) {
                Robot rob = new Robot();
                rob.keyPress(KeyEvent.VK_F17);
            }

            else if (hasWord("logout")) {
                mainGeneralUtil.closeAnimation(consoleFrame);
                login(false);
            }

            else if (eic("test")) {
                new TestClass(outputArea);
            }

            else if ((hasWord("wipe") || hasWord("clear") || hasWord("delete")) && has("error")) {
                if (mainGeneralUtil.getDebugMode()) {
                    mainGeneralUtil.wipeErrors();

                    println("Deleted all user erorrs");
                }

                else
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you don't have permission to do that.");
            }

            else if (!mainGeneralUtil.getHandledMath()){
                println("Sorry, " + mainGeneralUtil.getUsername() + ", but I don't recognize that command." +
                        " You can make a suggestion by clicking the \"Suggest something\" button.");
            }
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void handleMath(String op) {
        int firstParen = op.indexOf("(");
        int comma = op.indexOf(",");
        int lastParen = op.indexOf(")");

        String mathop;
        double param1 = 0.0;
        double param2 = 0.0;

        try {
            if (firstParen != -1) {
                mathop = op.substring(0,firstParen);

                if (comma != -1) {
                    param1 = Double.parseDouble(op.substring(firstParen+1,comma));

                    if (lastParen != -1) {
                        param2 =  Double.parseDouble(op.substring(comma+1,lastParen));
                    }
                }

                else if (lastParen != -1) {
                    param1 =  Double.parseDouble(op.substring(firstParen+1,lastParen));
                }

                if (mathop.equalsIgnoreCase("abs")) {
                    println(Math.abs(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("ceil")) {
                    println(Math.ceil(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("floor")) {
                    println(Math.floor(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("log")) {
                    println(Math.log(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("log10")) {
                    println(Math.log10(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("max")) {
                    println(Math.max(param1,param2));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("min")) {
                    println(Math.min(param1,param2));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("pow")) {
                    println(Math.pow(param1,param2));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("round")) {
                    println(Math.round(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("sqrt")) {
                    println(Math.sqrt(param1));
                    mainGeneralUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("convert2")) {
                    println(mainGeneralUtil.toBinary((int)(param1)));
                    mainGeneralUtil.setHandledMath(true);
                }
            }
        }

        catch(Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void printlnImage(String filename) {
        outputArea.insertIcon(new ImageIcon(filename));
        println("");
    }

    public static void printImage(String filename) {
        outputArea.insertIcon(new ImageIcon(filename));
    }

    private void print(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage, null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private boolean eic(String EIC) {
        return operation.equalsIgnoreCase(EIC);
    }

    private boolean has(String compare) {
        String ThisComp = compare.toLowerCase();
        String ThisOp = operation.toLowerCase();

        return ThisOp.contains(ThisComp);
    }

    private boolean hasWord(String compare) {
        String ThisComp = compare.toLowerCase();
        String ThisOp = operation.toLowerCase();

        if (ThisOp.equals(ThisComp) || ThisOp.contains(' ' + ThisComp + ' ') || ThisOp.contains(' ' + ThisComp) || ThisOp.contains(ThisComp + ' '))
            return true;

        else return ThisOp.contains(ThisComp + ' ');
    }

    //todo move to separate handler
    private void logToDo(String input) {
        try {
            if (input != null && !input.equals("") && !mainGeneralUtil.filter(input) && input.length() > 10 && !mainGeneralUtil.filter(input)) {
                BufferedWriter sugWriter = new BufferedWriter(new FileWriter("src/com/cyder/io/text/add.txt", true));

                sugWriter.write("User " + mainGeneralUtil.getUsername() + " at " + mainGeneralUtil.weatherThreadTime() + " made the suggestion: ");
                sugWriter.write(System.getProperty("line.separator"));

                sugWriter.write(input);

                sugWriter.write(System.getProperty("line.separator"));
                sugWriter.write(System.getProperty("line.separator"));

                sugWriter.flush();
                sugWriter.close();

                println("Request registered.");
                sugWriter.close();
            }
        }

        catch (Exception ex) {
            mainGeneralUtil.handle(ex);
        }
    }

    public void fib(int a, int b) {
        try {
            int c = a + b;
            println(c);
            if (c < 2147483647/2)
                fib(b, c);
            else
                println("Integer limit reached");
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void changeUsername(String newName) {
        try {
            mainGeneralUtil.readUserData();
            mainGeneralUtil.writeUserData("name",newName);

            mainGeneralUtil.setUsername(newName);
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    private void changePassword(char[] newPassword) {
        try {
            mainGeneralUtil.readUserData();
            mainGeneralUtil.writeUserData("password", mainGeneralUtil.toHexString(mainGeneralUtil.getSHA(newPassword)));
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    public void initMusicBackgroundList() {
        File backgroundDir = new File("src/com/cyder/users/" + mainGeneralUtil.getUserUUID() + "/Backgrounds");
        File musicDir = new File("src/com/cyder/users/" + mainGeneralUtil.getUserUUID() + "/Music");

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
        musicBackgroundSelectionList.setFont(mainGeneralUtil.weatherFontSmall);
        musicBackgroundSelectionList.setForeground(mainGeneralUtil.navy);
        musicBackgroundSelectionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2 && musicBackgroundSelectionList.getSelectedIndex() != -1) {
                openMusicBackground.doClick();
            }
            }
        });

        musicBackgroundSelectionList.setSelectionBackground(mainGeneralUtil.selectionColor);
    }

    //todo barrel roll and switching console dir doesn't work in full screen
    //todo inform you can only add png and mp3s if they select something else

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
            mainGeneralUtil.closeAnimation(editUserFrame);

        editUserFrame = new CyderFrame(1000,800,new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        editUserFrame.setTitlePosition(CyderFrame.LEFT_TITLE);
        editUserFrame.setTitle("Edit User");

        switchingPanel = new JLabel();
        switchingPanel.setForeground(new Color(255,255,255));
        switchingPanel.setBounds(140,70,720, 500);
        switchingPanel.setOpaque(true);
        switchingPanel.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        switchingPanel.setBackground(new Color(255,255,255));
        editUserFrame.getContentPane().add(switchingPanel);

        switchToPreferences();
        prefsPanelIndex = 2;

        backwardPanel = new CyderButton("< Prev");
        backwardPanel.setBackground(mainGeneralUtil.regularRed);
        backwardPanel.setColors(mainGeneralUtil.regularRed);
        backwardPanel.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        backwardPanel.setFont(mainGeneralUtil.weatherFontSmall);
        backwardPanel.addActionListener(e -> lastEditUser());
        backwardPanel.setBounds(20,380,100,40);
        editUserFrame.getContentPane().add(backwardPanel);

        forwardPanel = new CyderButton("Next >");
        forwardPanel.setBackground(mainGeneralUtil.regularRed);
        forwardPanel.setColors(mainGeneralUtil.regularRed);
        forwardPanel.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        forwardPanel.setFont(mainGeneralUtil.weatherFontSmall);
        forwardPanel.addActionListener(e -> nextEditUser());
        forwardPanel.setBounds(1000 - 120,380,100,40);
        editUserFrame.getContentPane().add(forwardPanel);

        JTextField changeUsernameField = new JTextField(10);
        changeUsernameField.addActionListener(e -> changeUsername.doClick());
        changeUsernameField.setFont(mainGeneralUtil.weatherFontSmall);
        changeUsernameField.setSelectionColor(mainGeneralUtil.selectionColor);
        changeUsernameField.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        changeUsernameField.setBounds(100,700,300,40);
        editUserFrame.getContentPane().add(changeUsernameField);

        changeUsername = new CyderButton("Change Username");
        changeUsername.setBackground(mainGeneralUtil.regularRed);
        changeUsername.setColors(mainGeneralUtil.regularRed);
        changeUsername.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        changeUsername.setFont(mainGeneralUtil.weatherFontSmall);
        changeUsername.addActionListener(e -> {
            String newUsername = changeUsernameField.getText();
            if (!mainGeneralUtil.empytStr(newUsername)) {
                changeUsername(newUsername);
                mainGeneralUtil.inform("Username successfully changed","", 300, 200);
                mainGeneralUtil.refreshUsername(consoleFrame);
                changeUsernameField.setText("");
            }
        });
        changeUsername.setBounds(100,750,300,40);
        editUserFrame.getContentPane().add(changeUsername);

        CyderButton deleteUser = new CyderButton("Delete User");
        deleteUser.setBackground(mainGeneralUtil.regularRed);
        deleteUser.setColors(mainGeneralUtil.regularRed);
        deleteUser.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        deleteUser.setFont(mainGeneralUtil.weatherFontSmall);
        deleteUser.addActionListener(e -> {
            println("Are you sure you want to permanently delete this account? This action cannot be undone! (yes/no)");
            mainGeneralUtil.setUserInputMode(true);
            inputField.requestFocus();
            mainGeneralUtil.setUserInputDesc("deleteuser");
        });
        deleteUser.setBounds(425,700,150,90);
        editUserFrame.getContentPane().add(deleteUser);

        JPasswordField changePasswordField = new JPasswordField(10);
        changePasswordField.addActionListener(e -> changePassword.doClick());
        changePasswordField.setFont(mainGeneralUtil.weatherFontSmall);
        changePasswordField.setSelectionColor(mainGeneralUtil.selectionColor);
        changePasswordField.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        changePasswordField.setToolTipText("New password");
        changePasswordField.setBounds(600,700,300,40);
        editUserFrame.getContentPane().add(changePasswordField);

        changePassword = new CyderButton("Change Password");
        changePassword.setBackground(mainGeneralUtil.regularRed);
        changePassword.setColors(mainGeneralUtil.regularRed);
        changePassword.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        changePassword.setFont(mainGeneralUtil.weatherFontSmall);
        changePassword.addActionListener(e -> {
            char[] newPassword = changePasswordField.getPassword();

            if (newPassword.length > 4) {
                changePassword(newPassword);
                mainGeneralUtil.inform("Password successfully changed","", 300, 200);
                changePasswordField.setText("");
            }

            else {
                mainGeneralUtil.inform("Sorry, " + mainGeneralUtil.getUsername() + ", " +
                        "but your password must be greater than 4 characters for security reasons.","", 500, 300);
                changePasswordField.setText("");
            }

            for (char c : newPassword) {
                c = '\0';
            }
        });
        changePassword.setBounds(600,750,300,40);
        editUserFrame.getContentPane().add(changePassword);

        editUserFrame.setLocationRelativeTo(null);
        editUserFrame.setVisible(true);
        editUserFrame.requestFocus();
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
        BackgroundLabel.setFont(mainGeneralUtil.weatherFontBig);
        BackgroundLabel.setBounds(720 / 2 - 375 / 2,10,375,40);
        switchingPanel.add(BackgroundLabel);

        initMusicBackgroundList();

        musicBackgroundSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        musicBackgroundScroll = new CyderScrollPane(musicBackgroundSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        musicBackgroundScroll.setSize(400, 400);
        musicBackgroundScroll.setFont(mainGeneralUtil.weatherFontBig);
        musicBackgroundScroll.setThumbColor(mainGeneralUtil.regularRed);
        musicBackgroundSelectionList.setBackground(new Color(255,255,255));
        musicBackgroundScroll.getViewport().setBackground(new Color(0,0,0,0));
        musicBackgroundScroll.setBounds(20,60,680,360);
        switchingPanel.add(musicBackgroundScroll);

        addMusicBackground = new CyderButton("Add");
        addMusicBackground.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        addMusicBackground.setColors(mainGeneralUtil.regularRed);
        addMusicBackground.setFocusPainted(false);
        addMusicBackground.setBackground(mainGeneralUtil.regularRed);
        addMusicBackground.addActionListener(e -> {
            try {
                File addFile = mainGeneralUtil.getFile();

                if (addFile == null)
                    return;

                Path copyPath = new File(addFile.getAbsolutePath()).toPath();

                if (addFile != null && addFile.getName().endsWith(".png")) {
                    File Destination = new File("src/com/cyder/users/" + mainGeneralUtil.getUserUUID() + "/Backgrounds/" + addFile.getName());
                    Files.copy(copyPath, Destination.toPath());
                    initMusicBackgroundList();
                    musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                    musicBackgroundScroll.revalidate();
                }

                else if (addFile != null && addFile.getName().endsWith(".mp3")) {
                    File Destination = new File("src/com/cyder/users/" + mainGeneralUtil.getUserUUID() + "/Music/" + addFile.getName());
                    Files.copy(copyPath, Destination.toPath());
                    initMusicBackgroundList();
                    musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                    musicBackgroundScroll.revalidate();
                }

                else {
                    mainGeneralUtil.inform("Sorry, " + mainGeneralUtil.getUsername() + ", but you can only add PNGs and MP3s", "Error",400,200);
                }
            }

            catch (Exception exc) {
                mainGeneralUtil.handle(exc);
            }
        });
        addMusicBackground.setFont(mainGeneralUtil.weatherFontSmall);
        addMusicBackground.setBounds(20,440,213,40);
        switchingPanel.add(addMusicBackground);

        openMusicBackground = new CyderButton("Open");
        openMusicBackground.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        openMusicBackground.setColors(mainGeneralUtil.regularRed);
        openMusicBackground.setFocusPainted(false);
        openMusicBackground.setBackground(mainGeneralUtil.regularRed);
        openMusicBackground.setFont(mainGeneralUtil.weatherFontSmall);
        openMusicBackground.addActionListener(e -> {
            List<?> ClickedSelectionList = musicBackgroundSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < musicBackgroundNameList.size() ; i++) {
                    if (ClickedSelection.equals(musicBackgroundNameList.get(i))) {
                        ClickedSelectionPath = musicBackgroundList.get(i);
                        break;
                    }
                }

                if (ClickedSelectionPath != null) {
                    if (ClickedSelectionPath.getName().endsWith(".png")) {
                        PhotoViewer pv = new PhotoViewer(ClickedSelectionPath);
                        pv.start();
                    }

                    else if (ClickedSelectionPath.getName().endsWith(".mp3")) {
                        mainGeneralUtil.mp3(ClickedSelectionPath.toString(),mainGeneralUtil.getUsername(),mainGeneralUtil.getUserUUID());
                    }
                }
            }
        });
        openMusicBackground.setBounds(20 + 213 + 20,440,213,40);
        switchingPanel.add(openMusicBackground);

        deleteMusicBackground = new CyderButton("Delete");
        deleteMusicBackground.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        deleteMusicBackground.setColors(mainGeneralUtil.regularRed);
        deleteMusicBackground.addActionListener(e -> {
            if (!musicBackgroundSelectionList.getSelectedValuesList().isEmpty()) {
                List<?> ClickedSelectionListMusic = musicBackgroundSelectionList.getSelectedValuesList();

                File ClickedSelectionPath = null;

                if (!ClickedSelectionListMusic.isEmpty()) {
                    String ClickedSelection = ClickedSelectionListMusic.get(0).toString();

                    for (int i = 0; i < musicBackgroundNameList.size() ; i++) {
                        if (ClickedSelection.equals(musicBackgroundNameList.get(i))) {
                            ClickedSelectionPath = musicBackgroundList.get(i);

                            break;
                        }
                    }

                    if (ClickedSelection.equalsIgnoreCase(mainGeneralUtil.getCurrentBackground().getName().replace(".png","")))
                        mainGeneralUtil.inform("Unable to delete the background you are currently using","Error",400,150);

                    else {
                        ClickedSelectionPath.delete();
                        initMusicBackgroundList();
                        musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                        musicBackgroundScroll.revalidate();

                        if (ClickedSelection.endsWith(".mp3"))
                            println("Music: " + ClickedSelectionPath.getName().replace(".mp3","") + " successfully deleted.");
                        else if (ClickedSelection.endsWith(".png")) {
                            println("Background: " + ClickedSelectionPath.getName().replace(".png","") + " successfully deleted.");

                            File[] paths = mainGeneralUtil.getValidBackgroundPaths();
                            for (int i = 0 ; i < paths.length ; i++) {
                                if (paths[i].equals(mainGeneralUtil.getCurrentBackground())) {
                                    mainGeneralUtil.setCurrentBackgroundIndex(i);
                                    break;
                                }
                            }

                            mainGeneralUtil.initBackgrounds();
                        }
                    }
                }
            }
        });
        deleteMusicBackground.setBackground(mainGeneralUtil.regularRed);
        deleteMusicBackground.setFont(mainGeneralUtil.weatherFontSmall);
        deleteMusicBackground.setBounds(20 + 213 + 20 + 213 + 20,440,213,40);
        switchingPanel.add(deleteMusicBackground);

        switchingPanel.revalidate();
    }

    private void switchToFontAndColor() {
        //todo copy from colorconverter and old font changer
    }

    ImageIcon selected = new ImageIcon("src/com/cyder/io/pictures/checkbox1.png");
    ImageIcon notSelected = new ImageIcon("src/com/cyder/io/pictures/checkbox2.png");

    private void switchToPreferences() {
        JLabel prefsTitle = new JLabel("Preferences");
        prefsTitle.setFont(mainGeneralUtil.weatherFontBig);
        prefsTitle.setForeground(mainGeneralUtil.navy);
        prefsTitle.setHorizontalAlignment(JLabel.CENTER);
        prefsTitle.setBounds(720 / 2 - 250 / 2,10,250,30);
        switchingPanel.add(prefsTitle);

        JLabel introMusicTitle = new JLabel("Intro Music");
        introMusicTitle.setFont(mainGeneralUtil.weatherFontSmall);
        introMusicTitle.setForeground(mainGeneralUtil.navy);
        introMusicTitle.setHorizontalAlignment(JLabel.CENTER);
        introMusicTitle.setBounds(20,50,130,25);
        switchingPanel.add(introMusicTitle);

        JLabel debugWindowsLabel = new JLabel("Debug");
        debugWindowsLabel.setFont(mainGeneralUtil.weatherFontSmall);
        debugWindowsLabel.setForeground(mainGeneralUtil.navy);
        debugWindowsLabel.setHorizontalAlignment(JLabel.CENTER);
        debugWindowsLabel.setBounds(130,50,160,25);
        switchingPanel.add(debugWindowsLabel);

        JLabel randomBackgroundLabel = new JLabel("Random Back");
        randomBackgroundLabel.setFont(mainGeneralUtil.weatherFontSmall);
        randomBackgroundLabel.setForeground(mainGeneralUtil.navy);
        randomBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        randomBackgroundLabel.setBounds(150 + 120,50,160,25);
        switchingPanel.add(randomBackgroundLabel);

        JLabel outputBorderLabel = new JLabel("Out Border");
        outputBorderLabel.setFont(mainGeneralUtil.weatherFontSmall);
        outputBorderLabel.setForeground(mainGeneralUtil.navy);
        outputBorderLabel.setHorizontalAlignment(JLabel.CENTER);
        outputBorderLabel.setBounds(150 + 20 + 10 + 150 + 90,50,160,25);
        switchingPanel.add(outputBorderLabel);

        JLabel inputBorderLabel = new JLabel("In Border");
        inputBorderLabel.setFont(mainGeneralUtil.weatherFontSmall);
        inputBorderLabel.setForeground(mainGeneralUtil.navy);
        inputBorderLabel.setHorizontalAlignment(JLabel.CENTER);
        inputBorderLabel.setBounds(150 + 20 + 20 + 150 + 225,50,160,25);
        switchingPanel.add(inputBorderLabel);

        JLabel introMusic = new JLabel();
        introMusic.setHorizontalAlignment(JLabel.CENTER);
        introMusic.setSize(100,100);
        introMusic.setIcon((mainGeneralUtil.getUserData("IntroMusic").equals("1") ? selected : notSelected));
        introMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("IntroMusic").equals("1");
            mainGeneralUtil.writeUserData("IntroMusic", (wasSelected ? "0" : "1"));
            introMusic.setIcon((wasSelected ? notSelected : selected));
            }
        });
        introMusic.setBounds(20, 80,100,100);
        switchingPanel.add(introMusic);

        JLabel debugWindows = new JLabel();
        debugWindows.setHorizontalAlignment(JLabel.CENTER);
        debugWindows.setSize(100,100);
        debugWindows.setIcon((mainGeneralUtil.getUserData("DebugWindows").equals("1") ? selected : notSelected));
        debugWindows.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("DebugWindows").equals("1");
            mainGeneralUtil.writeUserData("DebugWindows", (wasSelected ? "0" : "1"));
            debugWindows.setIcon((wasSelected ? notSelected : selected));
            }
        });
        debugWindows.setBounds(20 + 45 + 100, 80,100,100);
        switchingPanel.add(debugWindows);

        JLabel randBackgroundLabel = new JLabel();
        randBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        randBackgroundLabel.setSize(100,100);
        randBackgroundLabel.setIcon((mainGeneralUtil.getUserData("RandomBackground").equals("1") ? selected : notSelected));
        randBackgroundLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("RandomBackground").equals("1");
            mainGeneralUtil.writeUserData("RandomBackground", (wasSelected ? "0" : "1"));
            randBackgroundLabel.setIcon((wasSelected ? notSelected : selected));
            }
        });
        randBackgroundLabel.setBounds(20 + 2 * 45 + 2 * 100, 80,100,100);
        switchingPanel.add(randBackgroundLabel);

        JLabel outputBorder = new JLabel();
        outputBorder.setHorizontalAlignment(JLabel.CENTER);
        outputBorder.setSize(100,100);
        outputBorder.setIcon((mainGeneralUtil.getUserData("OutputBorder").equals("1") ? selected : notSelected));
        outputBorder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("OutputBorder").equals("1");
            mainGeneralUtil.writeUserData("OutputBorder", (wasSelected ? "0" : "1"));
            outputBorder.setIcon((wasSelected ? notSelected : selected));
            if (wasSelected) {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            else {
                outputScroll.setBorder(new LineBorder(mainGeneralUtil.vanila,3,true)); //todo background color
            }

            consoleFrame.revalidate();
            }
        });
        outputBorder.setBounds(20 + 3 * 45 + 3 * 100, 80,100,100);
        switchingPanel.add(outputBorder);

        JLabel inputBorder = new JLabel();
        inputBorder.setHorizontalAlignment(JLabel.CENTER);
        inputBorder.setSize(100,100);
        inputBorder.setIcon((mainGeneralUtil.getUserData("InputBorder").equals("1") ? selected : notSelected));
        inputBorder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("InputBorder").equals("1");
            mainGeneralUtil.writeUserData("InputBorder", (wasSelected ? "0" : "1"));
            inputBorder.setIcon((wasSelected ? notSelected : selected));

            if (wasSelected) {
                inputField.setBorder(BorderFactory.createEmptyBorder());
            }

            else {
                inputField.setBorder(new LineBorder(mainGeneralUtil.vanila,3,true)); //todo background color
            }

            consoleFrame.revalidate();
            }
        });
        inputBorder.setBounds(20 + 4 * 45 + 4 * 100, 80,100,100);
        switchingPanel.add(inputBorder);

        JLabel hourlyChimesLabel = new JLabel("Hour Chimes");
        hourlyChimesLabel.setFont(mainGeneralUtil.weatherFontSmall);
        hourlyChimesLabel.setForeground(mainGeneralUtil.navy);
        hourlyChimesLabel.setHorizontalAlignment(JLabel.CENTER);
        hourlyChimesLabel.setBounds(5,210,170,30);
        switchingPanel.add(hourlyChimesLabel);

        JLabel silenceLabel = new JLabel("No Errors");
        silenceLabel.setFont(mainGeneralUtil.weatherFontSmall);
        silenceLabel.setForeground(mainGeneralUtil.navy);
        silenceLabel.setHorizontalAlignment(JLabel.CENTER);
        silenceLabel.setBounds(150,210,150,30);
        switchingPanel.add(silenceLabel);

        JLabel fullscreenLabel = new JLabel("Fullscreen");
        fullscreenLabel.setFont(mainGeneralUtil.weatherFontSmall);
        fullscreenLabel.setForeground(mainGeneralUtil.navy);
        fullscreenLabel.setHorizontalAlignment(JLabel.CENTER);
        fullscreenLabel.setBounds(285,210,170,30);
        switchingPanel.add(fullscreenLabel);

        JLabel outputFillLabel = new JLabel("Fill Out");
        outputFillLabel.setFont(mainGeneralUtil.weatherFontSmall);
        outputFillLabel.setForeground(mainGeneralUtil.navy);
        outputFillLabel.setHorizontalAlignment(JLabel.CENTER);
        outputFillLabel.setBounds(420,210,170,30);
        switchingPanel.add(outputFillLabel);

        JLabel inputFillLabel = new JLabel("Fill In");
        inputFillLabel.setFont(mainGeneralUtil.weatherFontSmall);
        inputFillLabel.setForeground(mainGeneralUtil.navy);
        inputFillLabel.setHorizontalAlignment(JLabel.CENTER);
        inputFillLabel.setBounds(560,210,170,30);
        switchingPanel.add(inputFillLabel);

        JLabel hourlyChimes = new JLabel();
        hourlyChimes.setHorizontalAlignment(JLabel.CENTER);
        hourlyChimes.setSize(100,100);
        hourlyChimes.setIcon((mainGeneralUtil.getUserData("HourlyChimes").equals("1") ? selected : notSelected));
        hourlyChimes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("HourlyChimes").equals("1");
            mainGeneralUtil.writeUserData("HourlyChimes", (wasSelected ? "0" : "1"));
            hourlyChimes.setIcon((wasSelected ? notSelected : selected));
            }
        });
        hourlyChimes.setBounds(20, 235,100,100);
        switchingPanel.add(hourlyChimes);

        JLabel silenceErrors = new JLabel();
        silenceErrors.setHorizontalAlignment(JLabel.CENTER);
        silenceErrors.setSize(100,100);
        silenceErrors.setIcon((mainGeneralUtil.getUserData("SilenceErrors").equals("1") ? selected : notSelected));
        silenceErrors.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("SilenceErrors").equals("1");
            mainGeneralUtil.writeUserData("SilenceErrors", (wasSelected ? "0" : "1"));
            silenceErrors.setIcon((wasSelected ? notSelected : selected));
            }

        });
        silenceErrors.setBounds(20 + 100 + 45, 235,100,100);
        switchingPanel.add(silenceErrors);

        JLabel fullscreen = new JLabel();
        fullscreen.setHorizontalAlignment(JLabel.CENTER);
        fullscreen.setSize(100,100);
        fullscreen.setIcon((mainGeneralUtil.getUserData("FullScreen").equals("1") ? selected : notSelected));
        fullscreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("FullScreen").equals("1");
            mainGeneralUtil.writeUserData("FullScreen", (wasSelected ? "0" : "1"));
            fullscreen.setIcon((wasSelected ? notSelected : selected));
            if (wasSelected) {
                exitFullscreen();
            }

            else {
                refreshFullscreen();
            }
            }
        });
        fullscreen.setBounds(20 + 2 * 100 + 2 * 45, 235,100,100);
        switchingPanel.add(fullscreen);

        JLabel outputFill = new JLabel();
        outputFill.setHorizontalAlignment(JLabel.CENTER);
        outputFill.setSize(100,100);
        outputFill.setIcon((mainGeneralUtil.getUserData("OutputFill").equals("1") ? selected : notSelected));
        outputFill.setBounds(20 + 3 * 100 + 3 * 45, 235,100,100);
        switchingPanel.add(outputFill);

        JLabel inputFill = new JLabel();
        inputFill.setHorizontalAlignment(JLabel.CENTER);
        inputFill.setSize(100,100);
        inputFill.setIcon((mainGeneralUtil.getUserData("InputFill").equals("1") ? selected : notSelected));
        inputFill.setBounds(20 + 4 * 100 + 4 * 45, 235,100,100);
        switchingPanel.add(inputFill);

        JLabel clockLabel = new JLabel("Console Clock");
        clockLabel.setFont(mainGeneralUtil.weatherFontSmall);
        clockLabel.setForeground(mainGeneralUtil.navy);
        clockLabel.setHorizontalAlignment(JLabel.CENTER);
        clockLabel.setBounds(20,380,170,25);
        switchingPanel.add(clockLabel);

        JLabel showSecondsLabel = new JLabel("Clock Seconds");
        showSecondsLabel.setFont(mainGeneralUtil.weatherFontSmall);
        showSecondsLabel.setForeground(mainGeneralUtil.navy);
        showSecondsLabel.setHorizontalAlignment(JLabel.CENTER);
        showSecondsLabel.setBounds(220,380,170,25);
        switchingPanel.add(showSecondsLabel);

        JLabel clockOnConsole = new JLabel();
        clockOnConsole.setHorizontalAlignment(JLabel.CENTER);
        clockOnConsole.setSize(100,100);
        clockOnConsole.setIcon((mainGeneralUtil.getUserData("ClockOnConsole").equals("1") ? selected : notSelected));
        clockOnConsole.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("ClockOnConsole").equals("1");
            mainGeneralUtil.writeUserData("ClockOnConsole", (wasSelected ? "0" : "1"));
            clockOnConsole.setIcon((wasSelected ? notSelected : selected));
            consoleClockLabel.setVisible(!wasSelected);

            if (consoleClockLabel.isVisible())
                updateConsoleClock = true;

            consoleFrame.revalidate();

            if (consoleClockLabel.isVisible()) {
                if (mainGeneralUtil.getUserData("ShowSeconds").equals("1"))
                    consoleClockLabel.setText(mainGeneralUtil.consoleSecondTime());
                else
                    consoleClockLabel.setText(mainGeneralUtil.consoleTime());
            }
            }
        });
        clockOnConsole.setBounds(50,400,100,100);
        switchingPanel.add(clockOnConsole);

        JLabel showSeconds = new JLabel();
        showSeconds.setHorizontalAlignment(JLabel.CENTER);
        showSeconds.setSize(100,100);
        showSeconds.setIcon((mainGeneralUtil.getUserData("ShowSeconds").equals("1") ? selected : notSelected));
        showSeconds.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("ShowSeconds").equals("1");
            mainGeneralUtil.writeUserData("ShowSeconds", (wasSelected ? "0" : "1"));
            showSeconds.setIcon((wasSelected ? notSelected : selected));

            if (wasSelected)
                consoleClockLabel.setText(mainGeneralUtil.consoleTime());
            else
                consoleClockLabel.setText(mainGeneralUtil.consoleSecondTime());
            }
        });
        showSeconds.setBounds(50 + 200,400,100,100);
        switchingPanel.add(showSeconds);

        JSlider opacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        CyderSliderUI UI = new CyderSliderUI(opacitySlider);

        UI.setFillColor(Color.darkGray);
        UI.setOutlineColor(mainGeneralUtil.vanila);
        UI.setNewValColor(mainGeneralUtil.regularRed);
        UI.setOldValColor(mainGeneralUtil.regularBlue);
        UI.setSliderShape(CyderSliderUI.RECTANGLE);
        UI.setStroke(new BasicStroke(3.0f));

        opacitySlider.setUI(UI);
        opacitySlider.setMinimum(0);
        opacitySlider.setMaximum(100);
        opacitySlider.setMajorTickSpacing(5);
        opacitySlider.setMinorTickSpacing(1);
        opacitySlider.setPaintTicks(false);
        opacitySlider.setPaintLabels(false);
        opacitySlider.setVisible(true);
        opacitySlider.setValue((int)(Double.parseDouble(mainGeneralUtil.getUserData("Opacity")) / 255.0 * 100.0));
        opacitySlider.setFont(new Font("HeadPlane", Font.BOLD, 18));
        opacitySlider.addChangeListener(e -> {
            mainGeneralUtil.writeUserData("Opacity",(int) (255.0 * (opacitySlider.getValue() / 100.0)) + "");

            if (mainGeneralUtil.getUserData("OutputFill").equals("1")) {
                Color userC = mainGeneralUtil.hextorgbColor(mainGeneralUtil.getUserData("Background"));
                outputArea.setBackground(new Color(userC.getRed(),userC.getGreen(),userC.getBlue(),Integer.parseInt(mainGeneralUtil.getUserData("Opacity"))));
                outputArea.revalidate();
                outputArea.repaint();

                consoleFrame.revalidate();
            }

            if (mainGeneralUtil.getUserData("InputFill").equals("1")) {
                //todo copy from above when working
                //todo kind of works when you first enable the checkbox for opacity so start there
                //todo fix rendering artifacts with setOpaque methods
            }
        });

        outputFill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("OutputFill").equals("1");
            mainGeneralUtil.writeUserData("OutputFill", (wasSelected ? "0" : "1"));
            outputFill.setIcon((wasSelected ? notSelected : selected));

            if (wasSelected) {
                outputArea.setOpaque(false);
                outputScroll.setOpaque(false);

                outputArea.setBackground(null);
                consoleFrame.revalidate();
            }

            else {
                outputArea.setOpaque(true);
                outputScroll.setOpaque(true);

                Color userC = mainGeneralUtil.hextorgbColor(mainGeneralUtil.getUserData("Background"));
                outputArea.setBackground(new Color(userC.getRed(),userC.getGreen(),userC.getBlue(),Integer.parseInt(mainGeneralUtil.getUserData("Opacity"))));
                consoleFrame.revalidate();
            }
            }
        });

        inputFill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainGeneralUtil.getUserData("InputFill").equals("1");
            mainGeneralUtil.writeUserData("InputFill", (wasSelected ? "0" : "1"));
            inputFill.setIcon((wasSelected ? notSelected : selected));

            if (wasSelected) {
                inputField.setOpaque(false);
                inputField.setBackground(null);
                consoleFrame.revalidate();
            }

            else {
                Color userC = mainGeneralUtil.hextorgbColor(mainGeneralUtil.getUserData("Background"));
                inputField.setBackground(new Color(userC.getRed(),userC.getGreen(),userC.getBlue(),Integer.parseInt(mainGeneralUtil.getUserData("Opacity"))));
                consoleFrame.revalidate();
            }
            }
        });

        opacitySlider.setOpaque(false);
        opacitySlider.setToolTipText("Fill Opacity");
        opacitySlider.setFocusable(false);

        opacitySlider.setBounds(470,370,220,50);
        switchingPanel.add(opacitySlider);

        //todo color field with preview

        switchingPanel.revalidate();
    }

    //todo if we're using bobby because there are no background images, copy it to backgrounds

    //todo if a pref keyword doesn't exist in userdata, add it and set to default

    //todo on closing console frame if login open, close all other windows except login frame

    //todo on jlabels that you change the text like the don't have a user, create one,
    // copy whatever you did there to the passwords match or don't match and take note for future use
    //todo call this a cyder label

    //todo on startup make sure input and output are painted or filled with color selected

    //todo also save background and opacity now on shutdown when you save foreground

    //todo add more to cyderargs

    public void createUser() {
        createUserBackground = null;

        if (createUserFrame != null)
            mainGeneralUtil.closeAnimation(createUserFrame);

        createUserFrame = new CyderFrame(356,473,new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        createUserFrame.setTitle("Create User");

        JLabel NameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        NameLabel.setFont(mainGeneralUtil.weatherFontSmall);
        NameLabel.setBounds(120,30,121,30); //todo bounds for labels
        createUserFrame.getContentPane().add(NameLabel);

        newUserName = new JTextField(15);
        newUserName.setSelectionColor(mainGeneralUtil.selectionColor);
        newUserName.setFont(mainGeneralUtil.weatherFontSmall);
        newUserName.setForeground(mainGeneralUtil.navy);
        newUserName.setFont(mainGeneralUtil.weatherFontSmall);
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

        newUserName.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        newUserName.setBounds(60,70,240,40);
        createUserFrame.getContentPane().add(newUserName);

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(mainGeneralUtil.weatherFontSmall);
        passwordLabel.setForeground(mainGeneralUtil.navy);
        passwordLabel.setBounds(60,120,240,30);
        createUserFrame.getContentPane().add(passwordLabel);

        JLabel matchPasswords = new JLabel("Passwords match");

        newUserPassword = new JPasswordField(15);
        newUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("<html><div style='text-align: center;'>Passwords match</div></html>");
                matchPasswords.setForeground(mainGeneralUtil.regularGreen);
            }

            else {
                matchPasswords.setText("<html><div style='text-align: center;'>Passwords don't match</div></html>");
                matchPasswords.setForeground(mainGeneralUtil.regularRed);
            }
            }
        });
        newUserPassword.setFont(mainGeneralUtil.weatherFontSmall);
        newUserPassword.setForeground(mainGeneralUtil.navy);
        newUserPassword.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        newUserPassword.setSelectedTextColor(mainGeneralUtil.selectionColor);
        newUserPassword.setBounds(60,160,240,40);
        createUserFrame.getContentPane().add(newUserPassword);

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(mainGeneralUtil.weatherFontSmall);
        passwordLabelConf.setForeground(mainGeneralUtil.navy);
        passwordLabelConf.setBounds(60,210,240,30);
        createUserFrame.getContentPane().add(passwordLabelConf);

        newUserPasswordconf = new JPasswordField(15);
        newUserPasswordconf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("<html><div style='text-align: center;'>Passwords match</div></html>");
                matchPasswords.setForeground(mainGeneralUtil.regularGreen);
            }

            else {
                matchPasswords.setText("<html><div style='text-align: center;'>Passwords don't match</div></html>");
                matchPasswords.setForeground(mainGeneralUtil.regularRed);
            }
            }
        });

        newUserPasswordconf.setFont(mainGeneralUtil.weatherFontSmall);
        newUserPasswordconf.setForeground(mainGeneralUtil.navy);
        newUserPasswordconf.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        newUserPasswordconf.setSelectedTextColor(mainGeneralUtil.selectionColor);
        newUserPasswordconf.setBounds(60,250,240,40);
        createUserFrame.getContentPane().add(newUserPasswordconf);

        matchPasswords.setFont(mainGeneralUtil.weatherFontSmall);
        matchPasswords.setForeground(mainGeneralUtil.regularGreen);
        matchPasswords.setBounds(65,300,300,30);
        createUserFrame.getContentPane().add(matchPasswords);

        chooseBackground = new CyderButton("Choose background");
        chooseBackground.setToolTipText("Click me to choose a background");
        chooseBackground.setFont(mainGeneralUtil.weatherFontSmall);
        chooseBackground.setBackground(mainGeneralUtil.regularRed);
        chooseBackground.setColors(mainGeneralUtil.regularRed);
        chooseBackground.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    File temp = mainGeneralUtil.getFile();
                    if (temp != null) {
                        createUserBackground = temp;
                    }

                    if (temp != null && !Files.probeContentType(Paths.get(createUserBackground.getAbsolutePath())).endsWith("png")) {
                        createUserBackground = null;
                    }
                }

                catch (Exception exc) {
                    exc.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    if (createUserBackground != null) {
                        chooseBackground.setText(createUserBackground.getName());
                    }

                    else {
                        chooseBackground.setToolTipText("No File Chosen");
                    }
                }

                catch (Exception ex) {
                    mainGeneralUtil.handle(ex);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chooseBackground.setToolTipText("Choose background");
            }
        });

        chooseBackground.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        chooseBackground.setBounds(60,340,240,40);
        createUserFrame.getContentPane().add(chooseBackground);

        createNewUser = new CyderButton("Create User");
        createNewUser.setFont(mainGeneralUtil.weatherFontSmall);
        createNewUser.setBackground(mainGeneralUtil.regularRed);
        createNewUser.setColors(mainGeneralUtil.regularRed);
        createNewUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            try {
                String uuid = mainGeneralUtil.generateUUID();
                File folder = new File("src/com/cyder/users/" + uuid);

                while (folder.exists()) {
                    uuid = mainGeneralUtil.generateUUID();
                    folder = new File("src/com/cyder/users/" + uuid);
                }

                char[] pass = newUserPassword.getPassword();
                char[] passconf = newUserPasswordconf.getPassword();

                boolean alreadyExists = false;
                File[] files = new File("src/com/cyder/users").listFiles();

                for (File f: files) {
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

                    if (alreadyExists) break;
                }

                if (mainGeneralUtil.empytStr(newUserName.getText()) || pass == null || passconf == null
                        || uuid.equals("") || pass.equals("") || passconf.equals("") || uuid.length() == 0) {
                    mainGeneralUtil.inform("Sorry, but one of the required fields was left blank.\nPlease try again.","", 400, 300);
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (alreadyExists) {
                    mainGeneralUtil.inform("Sorry, but that username is already in use.\nPlease try a different one.", "", 400, 300);
                    newUserName.setText("");
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (!Arrays.equals(pass, passconf) && pass.length > 0) {
                    mainGeneralUtil.inform("Sorry, but your passwords did not match. Please try again.", "",400, 300);
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (pass.length < 5) {
                    mainGeneralUtil.inform("Sorry, but your password length should be greater than\n"
                            + "four characters for security reasons. Please add more characters.", "", 400, 300);

                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else {
                    if (createUserBackground == null) {
                        mainGeneralUtil.inform("No background image was chosen so we're going to give you a sweet one ;)", "No background", 700, 230);
                        createUserBackground = new File("src/com/cyder/io/pictures/bobby.png");
                    }

                    File NewUserFolder = new File("src/com/cyder/users/" + uuid);
                    File backgrounds = new File("src/com/cyder/users/" + uuid + "/Backgrounds");
                    File music = new File("src/com/cyder/users/" + uuid + "/Music");
                    File notes = new File("src/com/cyder/users/" + uuid + "/Notes");

                    NewUserFolder.mkdirs();
                    backgrounds.mkdir();
                    music.mkdir();
                    notes.mkdir();

                    //todo make it easy to add preferences

                    ImageIO.write(ImageIO.read(createUserBackground), "png",
                            new File("src/com/cyder/users/" + uuid + "/Backgrounds/" + createUserBackground.getName()));

                    BufferedWriter newUserWriter = new BufferedWriter(new FileWriter(
                            "src/com/cyder/users/" + uuid + "/Userdata.txt"));

                    LinkedList<String> data = new LinkedList<>();
                    data.add("Name:" + newUserName.getText().trim());
                    data.add("Font:tahoma");
                    data.add("Foreground:FCFBE3");
                    data.add("Background:FFFFFF");//todo
                    data.add("Opacity:0");//todo
                    data.add("Password:" + mainGeneralUtil.toHexString(mainGeneralUtil.getSHA(pass)));//todo change to diff name? more secure?

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

                    mainGeneralUtil.closeAnimation(createUserFrame);

                    mainGeneralUtil.inform("The new user \"" + newUserName.getText().trim() + "\" has been created successfully.", "", 500, 300);

                    if (consoleFrame != null)
                        mainGeneralUtil.closeAnimation(createUserFrame);

                    else {
                        mainGeneralUtil.closeAnimation(createUserFrame);
                        mainGeneralUtil.closeAnimation(loginFrame);
                        recognize(newUserName.getText().trim(),pass);
                    }
                }

                //proper password handling in Java
                for (char c : pass)
                    c = '\0';

                for (char c : passconf)
                    c = '\0';
            }

            catch (Exception ex) {
                mainGeneralUtil.handle(ex);
            }
            }
        });

        createNewUser.setBorder(new LineBorder(mainGeneralUtil.navy,5,false));
        createNewUser.setFont(mainGeneralUtil.weatherFontSmall);
        createNewUser.setBounds(60,390,240,40);
        createUserFrame.getContentPane().add(createNewUser);

        createUserFrame.setLocationRelativeTo(null);
        createUserFrame.setVisible(true);
        newUserName.requestFocus();
    }

    private void minimizeMenu() {
        if (menuLabel.isVisible()) {
            animation.jLabelXLeft(0,-150,10,8, menuLabel);

            Thread waitThread = new Thread(() -> {
                try {
                    Thread.sleep(186);
                }

                catch (Exception ex) {
                    mainGeneralUtil.handle(ex);
                }

                menuLabel.setVisible(false);
                menuButton.setIcon(new ImageIcon("src/com/cyder/io/pictures/menuSide1.png"));
            });

            waitThread.start();
        }
    }

    private void killAllYoutube() {
        for (YoutubeThread ytt : youtubeThreads) {
            ytt.kill();
        }
    }

    //todo move file.txt to temp dir, file used for filechooser

    private void askew() {
        consoleFrame.setBackground(mainGeneralUtil.navy);
        parentLabel.setIcon(new ImageIcon(mainGeneralUtil.rotateImageByDegrees(mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().getAbsolutePath()),3)));
    }


    private void barrelRoll() {
        consoleFrame.setBackground(mainGeneralUtil.navy);
        mainGeneralUtil.getValidBackgroundPaths();

        int originConsoleDIr = mainGeneralUtil.getConsoleDirection();
        BufferedImage master = mainGeneralUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().getAbsolutePath());

        Timer timer = null;
        Timer finalTimer = timer;
        timer = new Timer(10, new ActionListener() {
            private double angle = 0;
            private double delta = 2.0;

            BufferedImage rotated;

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += delta;
                if (angle > 360) {
                    return;
                }
                rotated = mainGeneralUtil.rotateImageByDegrees(master, angle);
                parentLabel.setIcon(new ImageIcon(rotated));
            }
        });
        timer.start();
    }

    //exiting method, system.exit will call shutdown hook which wil then call shutdown();
    private void exit() {
        mainGeneralUtil.closeAnimation(consoleFrame);
        killAllYoutube();
        stringUtil.killBletchy();
        System.exit(0);
    }

    //todo add more to cyderargs.log
    //todo make cyderargs push to bottom so new stuff is at top
    //todo move users out of cyder and into same dir as com

    private void shutdown() {
        try {
            Font SaveFont = outputArea.getFont();
            String SaveFontName = SaveFont.getName();
            Color SaveColor = outputArea.getForeground();

            mainGeneralUtil.readUserData();
            mainGeneralUtil.writeUserData("Font",SaveFontName);
            mainGeneralUtil.writeUserData("Foreground",mainGeneralUtil.rgbtohexString(SaveColor));

            mainGeneralUtil.deleteTempDir();
        }

        catch (Exception e) {
            mainGeneralUtil.handle(e);
        }
    }

    //todo can this go to some util method when you separate methods out of here and GeneralUtil?
    private static void logArgs(String[] cyderArgs) {
        try {
            if (cyderArgs.length == 0)
                cyderArgs = new String[]{"Started by " + System.getProperty("user.name")};

            File log = new File("src/CyderArgs.log");

            if (!log.exists())
                log.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(log, true));

            String argsString = "";

            for (int i = 0 ; i < cyderArgs.length ; i++) {
                if (i != 0)
                    argsString += ",";
                argsString += cyderArgs[i];
            }

            Date current = new Date();
            DateFormat argsFormat = new SimpleDateFormat("MM-dd-yy HH:mm:ss");
            bw.write(argsFormat.format(current) + " : " + argsString);
            bw.newLine();
            bw.flush();
            bw.close();
        }

        catch (Exception e) {
            new GeneralUtil().staticHandle(e);
        }
    }

    //todo remove this once loginFrame is cyderFrame
    public void notify(String htmltext, int delay, int arrowDir, int vanishDir, JLabel parent, int width) {
        if (consoleNotification != null && consoleNotification.isVisible())
            consoleNotification.kill();

        consoleNotification = new Notification();

        int w = width;
        int h = 40;

        int lastIndex = 0;

        while (lastIndex != -1){

            lastIndex = htmltext.indexOf("<br/>",lastIndex);

            if (lastIndex != -1){
                h += 30;
                lastIndex += "<br/>".length();
            }
        }

        if (h == 40)
            h = 30;

        consoleNotification.setWidth(w);
        consoleNotification.setHeight(h);
        consoleNotification.setArrow(arrowDir);

        JLabel text = new JLabel();
        text.setText(htmltext);
        text.setFont(mainGeneralUtil.weatherFontSmall);
        text.setForeground(mainGeneralUtil.navy);
        text.setBounds(14,10,w * 2,h);
        consoleNotification.add(text);
        consoleNotification.setBounds(parent.getWidth() / 2 - (w/2),30,w * 2,h * 2);
        parent.add(consoleNotification,1,0);
        parent.repaint();

        consoleNotification.vanish(vanishDir, parent, delay);
    }

    //todo make a centered one that vanishes to top
    public void notify(String htmltext, int delay, int arrowDir, int vanishDir, JLayeredPane parent, int width) {
        if (consoleNotification != null && consoleNotification.isVisible())
            consoleNotification.kill();

        consoleNotification = new Notification();

        int w = width;
        int h = 40;

        int lastIndex = 0;

        while (lastIndex != -1){

            lastIndex = htmltext.indexOf("<br/>",lastIndex);

            if (lastIndex != -1){
                h += 30;
                lastIndex += "<br/>".length();
            }
        }

        if (h == 40)
            h = 30;

        consoleNotification.setWidth(w);
        consoleNotification.setHeight(h);
        consoleNotification.setArrow(arrowDir);

        JLabel text = new JLabel();
        text.setText(htmltext);
        text.setFont(mainGeneralUtil.weatherFontSmall);
        text.setForeground(mainGeneralUtil.navy);
        text.setBounds(14,10,w * 2,h);
        consoleNotification.add(text);
        consoleNotification.setBounds(parent.getWidth() / 2 - (w/2),30,w * 2,h * 2);
        parent.add(consoleNotification,1,0);
        parent.repaint();

        consoleNotification.vanish(vanishDir, parent, delay);
    }
}