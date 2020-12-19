import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.*;
import com.cyder.exception.CyderException;
import com.cyder.exception.FatalException;
import com.cyder.games.Hangman;
import com.cyder.games.TicTacToe;
import com.cyder.handler.ErrorHandler;
import com.cyder.handler.PhotoViewer;
import com.cyder.threads.YoutubeThread;
import com.cyder.ui.*;
import com.cyder.utilities.*;
import com.cyder.widgets.*;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.Border;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

//todo locks for reading and writing to files

//todo enter animations for notifications

//todo general util goes away
//todo default val for all enums

//todo if location isn't found say so and say certain features might not work
//todo weather will not work if IP cannot find location, happened in captiva florida

//todo orange vs blue border for working in background doesnt seem to work

//todo light mode and dark mode (switch vanila and navy for informs)

//todo temporarily toggle prefs for via input field keywords

//todo change background color for console frame so like not navy

//todo some scrolls with borders are not fitted properly

//todo start animations usage

//todo cyder label
//todo cyder progress bar

//todo make password more secure maybe salt or something?
//todo double hash sha perhaps to avoid someone just hashing their own password and pasting it in

//todo make it so all you can have is jar and system files and it'll create everything else such as dirs

//todo move File.txt, String.txt, and InputMessage.txt to tmp directory

//todo barrel roll and switching console dir doesn't work in full screen

//todo if a pref keyword doesn't exist in userdata, add it and set to default

//todo be able to set background to a solid color and make that an image and save it

//todo utilize colors, fonts, font weights, and new lines now
// <html>test<br/><i>second line but italics<i/><br/>third!!<br/><p style="color:rgb(252, 251, 227)">fourth with color</p>
// <p style="font-family:verdana">fifth with font</p></html>

//todo perlin-noise GUI swap between 2D and 3D and add color range too
//todo make a widget version of cyder that you can swap between big window and widget version, background is get cropped image
//todo make pixelating pictures it's own widget

//todo add a systems error dir if no users <- if possibility of no user put here too (see readData() loop)
//todo add a handle that you can use when unsure if there is a user to avoid looping until stackoverflow

//todo make the frame and drag label stay when switching backgrounds and the image be separate (inside of consoleframe class)
//todo you kind of did this in login with the sliding text, then notification will not go over it and only the background will slide

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
    private CyderFrame loginFrame;
    private JTextField nameField;
    private JPasswordField pass;
    private JLabel newUserLabel;
    private JLabel menuLabel;

    //Objects for main use
    private GeneralUtil mainGeneralUtil;
    private StringUtil stringUtil;
    private CyderAnimation animation;
    private Notes userNotes;
    private TimeUtil timeUtil;

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

    //frame util (to be removed once consoleframe extends cyderframe)
    private AnimationUtil frameAni;

    //create user vars
    private CyderFrame createUserFrame;
    private JPasswordField newUserPasswordconf;
    private JPasswordField newUserPassword;
    private JTextField newUserName;
    private CyderButton createNewUser;
    private CyderButton chooseBackground;
    private File createUserBackground;

    //notificaiton remove once extended
    private static Notification consoleNotification;

    //pixealte file
    private File pixelateFile;

    //font list for prefs
    private JList fontList;

    //Linked List of youtube scripts
    private LinkedList<YoutubeThread> youtubeThreads = new LinkedList<>();

    //sliding background var
    private boolean slidLeft;

    //notifications for holidays
    private SpecialDay specialDayNotifier;

    //network util
    private NetworkUtil networkUtil;
    private SystemUtil systemUtil;

    //boolean for drawing line
    private boolean drawLines = false;
    private boolean linesDrawn = false;
    private Color lineColor = Color.white;

    //call constructor
    public static void main(String[] CA) {
        new CyderMain(CA);
    }

    private CyderMain(String[] CA) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown,"exit-hook"));

        initObjects();
        initSystemProperties();
        initUIManager();

        IOUtil.cleanUpUsers();
        IOUtil.deleteTempDir();
        IOUtil.logArgs(CA);

        backgroundProcessChecker();

        if (SecurityUtil.nathanLenovo())
            autoCypher();

        else if (!IOUtil.getSystemData("Released").equals("1"))
            System.exit(0);

        else
            login();
    }

    private void initObjects() {
        mainGeneralUtil = new GeneralUtil();
        animation = new CyderAnimation();
        stringUtil = new StringUtil();
        stringUtil.setOutputArea(outputArea);
        timeUtil = new TimeUtil();
        frameAni = new AnimationUtil();
        networkUtil = new NetworkUtil();
        systemUtil = new SystemUtil();
    }

    private void initSystemProperties() {
        //Fix scaling issue for high DPI displays like nathanLenovo which is 2560x1440
        System.setProperty("sun.java2d.uiScale","1.0");
    }

    private void initUIManager() {
        //this sets up special looking tooltips
        UIManager.put("ToolTip.background", CyderColors.tooltipBackgroundColor);
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createLineBorder(CyderColors.tooltipBorderColor,2,true)));
        UIManager.put("ToolTip.font", CyderFonts.tahoma.deriveFont(22f));
        UIManager.put("ToolTip.foreground", CyderColors.tooltipForegroundColor);
    }

    private void autoCypher() {
        try {
            File autoCypher = new File("../autocypher.txt");
            File Users = new File("src/users/");

            if (autoCypher.exists() && Users.listFiles().length != 0) {
                BufferedReader ac = new BufferedReader(new FileReader(autoCypher));

                String line = ac.readLine();
                String[] parts = line.split(":");

                if (parts.length == 2 && !parts[0].equals("") && !parts[1].equals(""))
                    recognize(parts[0], parts[1].toCharArray());
            }

            else
                login();
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
            login();
        }
    }

    private void console() {
        try{
            mainGeneralUtil.refreshBackgrounds();
            mainGeneralUtil.resizeUserBackgrounds();
            mainGeneralUtil.getBackgrounds();
            mainGeneralUtil.refreshBackgrounds();

            consoleFrame = new JFrame() {
                @Override
                public void paint(Graphics g) {
                super.paint(g);

                if (drawLines && !linesDrawn) {
                    Graphics2D g2d = (Graphics2D) g;

                    g2d.setPaint(lineColor);
                    g2d.setStroke(new BasicStroke(5));

                    g2d.drawLine(consoleFrame.getWidth() / 2 - 3,32,consoleFrame.getWidth() / 2 - 3,consoleFrame.getHeight() - 12);
                    g2d.drawLine(10, consoleFrame.getHeight() / 2 - 3, consoleFrame.getWidth() - 12, consoleFrame.getHeight() / 2 - 3);

                    BufferedImage img = null;

                    try {
                        img = ImageIO.read(new File("src/com/cyder/sys/pictures/Neffex.png"));
                    }

                    catch (Exception e) {
                        ErrorHandler.handle(e);
                    }

                    int w = img.getWidth(null);
                    int h = img.getHeight(null);

                    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

                    g2d.drawImage(img, consoleFrame.getWidth() / 2 - w / 2, consoleFrame.getHeight() / 2 - h / 2, null);

                    linesDrawn = true;
                }
                }
            };
            consoleFrame.setUndecorated(true);
            consoleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                mainGeneralUtil.setBackgroundWidth((int) systemUtil.getScreenSize().getWidth());
                mainGeneralUtil.setBackgroundHeight((int) systemUtil.getScreenSize().getHeight());
            }

            consoleFrame.setBounds(0, 0, mainGeneralUtil.getBackgroundWidth(), mainGeneralUtil.getBackgroundHeight());
            consoleFrame.setTitle(IOUtil.getSystemData("Version") + " Cyder [" + mainGeneralUtil.getUsername() + "]");

            parentPane = new JLayeredPane();
            parentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

            consoleFrame.setContentPane(parentPane);

            parentPane.setLayout(null);

            parentLabel = new JLabel();
            parentLabel.setOpaque(false);

            if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                parentLabel.setIcon(new ImageIcon(ImageUtil.resizeImage((int) systemUtil.getScreenSize().getWidth(), (int) systemUtil.getScreenSize().getHeight(), mainGeneralUtil.getCurrentBackground())));
                parentLabel.setBounds(0, 0, mainGeneralUtil.getBackgroundWidth(), mainGeneralUtil.getBackgroundHeight());
                mainGeneralUtil.setBackgroundWidth((int) systemUtil.getScreenSize().getWidth());
                mainGeneralUtil.setBackgroundHeight((int) systemUtil.getScreenSize().getHeight());
            }

            else {
                parentLabel.setIcon(new ImageIcon(ImageUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().toString(),GeneralUtil.getConsoleDirection())));
                parentLabel.setBounds(0, 0, mainGeneralUtil.getBackgroundWidth(), mainGeneralUtil.getBackgroundHeight());
            }

            parentLabel.setBorder(new LineBorder(CyderColors.navy,8,false));
            parentLabel.setToolTipText(mainGeneralUtil.getCurrentBackground().getName().replace(".png", ""));

            parentPane.add(parentLabel,1,0);

            consoleFrame.setIconImage(systemUtil.getCyderIcon().getImage());

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
            outputArea.setBounds(10, 62, mainGeneralUtil.getBackgroundWidth() - 20, mainGeneralUtil.getBackgroundHeight() - 204);
            outputArea.setFocusable(true);
            outputArea.setSelectionColor(new Color(204,153,0));
            outputArea.setOpaque(false);
            outputArea.setBackground(new Color(0,0,0,0));

            outputScroll = new CyderScrollPane(outputArea,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            outputScroll.setThumbColor(CyderColors.intellijPink);
            outputScroll.getViewport().setBorder(null);
            outputScroll.getViewport().setOpaque(false);
            outputScroll.setOpaque(false);

            if (IOUtil.getUserData("OutputBorder").equalsIgnoreCase("1")) {
                outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")),3,true));
            }

            else {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            outputScroll.setBounds(10, 62, mainGeneralUtil.getBackgroundWidth() - 20, mainGeneralUtil.getBackgroundHeight() - 204);

            parentLabel.add(outputScroll);

            inputField = new JTextField(40);

            if (IOUtil.getUserData("InputBorder").equalsIgnoreCase("1")) {
                inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")),3,true));
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
                        mainGeneralUtil.setConsoleDirection(ConsoleDirection.DOWN);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_RIGHT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(ConsoleDirection.RIGHT);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_UP) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(ConsoleDirection.UP);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_LEFT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainGeneralUtil.setConsoleDirection(ConsoleDirection.LEFT);
                        exitFullscreen();
                    }

                    if ((KeyEvent.SHIFT_DOWN_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        if (!linesDrawn) {
                            drawLines = true;
                            consoleFrame.repaint();
                        }
                    }
                }

                @Override
                public void keyReleased(java.awt.event.KeyEvent e) {
                    if (inputField.getText().length() == 1) {
                        inputField.setText(inputField.getText().toUpperCase());
                    }

                    if ((KeyEvent.SHIFT_DOWN_MASK) != 0 && e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        drawLines = false;
                        linesDrawn = false;
                        consoleFrame.repaint();
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
            inputField.setSelectionColor(CyderColors.selectionColor);
            inputField.addKeyListener(commandScrolling);

            consoleFrame.addWindowListener(consoleEcho);

            inputField.setBounds(10, 82 + outputArea.getHeight(),
                    mainGeneralUtil.getBackgroundWidth() - 20, mainGeneralUtil.getBackgroundHeight() - (outputArea.getHeight() + 62 + 40));
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

            Font Userfont = new Font(IOUtil.getUserData("Font"),Font.BOLD, 30);
            Color Usercolor = ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground"));

            mainGeneralUtil.setUsercolor(Usercolor);
            mainGeneralUtil.setUserfont(Userfont);

            inputField.setForeground(Usercolor);
            outputArea.setForeground(Usercolor);

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


            inputField.setFont(Userfont);
            outputArea.setFont(Userfont);

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
                    suggestionButton.setIcon(new ImageIcon("src/com/cyder/sys/pictures/suggestion2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("src/com/cyder/sys/pictures/suggestion1.png"));
                }
            });

            suggestionButton.setBounds(32, 4, 22, 22);

            ImageIcon DebugIcon = new ImageIcon("src/com/cyder/sys/pictures/suggestion1.png");

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

            ImageIcon MenuIcon = new ImageIcon("src/com/cyder/sys/pictures/menuSide1.png");

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
                frameAni.minimizeAnimation(consoleFrame);
                updateConsoleClock = false;
                consoleFrame.setState(Frame.ICONIFIED);
                minimizeMenu();
            });

            minimize.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    minimize.setIcon(new ImageIcon("src/com/cyder/sys/pictures/Minimize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    minimize.setIcon(new ImageIcon("src/com/cyder/sys/pictures/Minimize1.png"));
                }
            });

            minimize.setBounds(mainGeneralUtil.getBackgroundWidth() - 81, 4, 22, 20);

            ImageIcon mini = new ImageIcon("src/com/cyder/sys/pictures/Minimize1.png");
            minimize.setIcon(mini);
            parentLabel.add(minimize);
            minimize.setFocusPainted(false);
            minimize.setOpaque(false);
            minimize.setContentAreaFilled(false);
            minimize.setBorderPainted(false);

            alternateBackground = new JButton("");
            alternateBackground.setToolTipText("Alternate Background");
            alternateBackground.addActionListener(e -> {
                mainGeneralUtil.refreshBackgrounds();

                try {
                    lineColor = new ImageUtil().getDominantColorOpposite(ImageIO.read(mainGeneralUtil.getCurrentBackground()));
                }

                catch (IOException ex) {
                    ErrorHandler.handle(ex);
                }

                if (mainGeneralUtil.canSwitchBackground() && mainGeneralUtil.getBackgrounds().length > 1) {
                    mainGeneralUtil.setCurrentBackgroundIndex(mainGeneralUtil.getCurrentBackgroundIndex() + 1);
                    switchBackground();
                }

                else if (mainGeneralUtil.onLastBackground() && mainGeneralUtil.getBackgrounds().length > 1) {
                    mainGeneralUtil.setCurrentBackgroundIndex(0);
                    switchBackground();
                }

                else if (mainGeneralUtil.getBackgrounds().length == 1) {
                    println("You only have one background image. Would you like to add more? (Enter yes/no)");
                    inputField.requestFocus();
                    stringUtil.setUserInputMode(true);
                    stringUtil.setUserInputDesc("addbackgrounds");
                    inputField.requestFocus();
                }

                else {
                    try {
                        ErrorHandler.handle(new FatalException("Background DNE"));
                        println("Error in parsing background; perhaps it was deleted.");
                    }

                    catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                }
            });

            alternateBackground.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("src/com/cyder/sys/pictures/ChangeSize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("src/com/cyder/sys/pictures/ChangeSize1.png"));
                }
            });

            alternateBackground.setBounds(mainGeneralUtil.getBackgroundWidth() - 54, 4, 22, 20);

            ImageIcon Size = new ImageIcon("src/com/cyder/sys/pictures/ChangeSize1.png");
            alternateBackground.setIcon(Size);

            parentLabel.add(alternateBackground);

            alternateBackground.setFocusPainted(false);
            alternateBackground.setOpaque(false);
            alternateBackground.setContentAreaFilled(false);
            alternateBackground.setBorderPainted(false);

            close = new JButton("");
            close.setToolTipText("Close");
            close.addActionListener(e -> exit());

            close.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    close.setIcon(new ImageIcon("src/com/cyder/sys/pictures/Close2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(new ImageIcon("src/com/cyder/sys/pictures/Close1.png"));
                }
            });

            close.setBounds(mainGeneralUtil.getBackgroundWidth() - 27, 4, 22, 20);

            ImageIcon exit = new ImageIcon("src/com/cyder/sys/pictures/Close1.png");

            close.setIcon(exit);

            parentLabel.add(close);

            close.setFocusPainted(false);
            close.setOpaque(false);
            close.setContentAreaFilled(false);
            close.setBorderPainted(false);

            consoleDragLabel = new JLabel();
            consoleDragLabel.setBounds(0,0, mainGeneralUtil.getBackgroundWidth(),30);
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

            consoleClockLabel = new JLabel(TimeUtil.consoleTime(), SwingConstants.CENTER);
            consoleClockLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(20f));
            consoleClockLabel.setForeground(CyderColors.vanila);
            consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 30,
                    2,(consoleClockLabel.getText().length() * 17), 25);

            consoleDragLabel.add(consoleClockLabel, SwingConstants.CENTER);

            updateConsoleClock = IOUtil.getUserData("ClockOnConsole").equalsIgnoreCase("1");

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (consoleClockLabel.isVisible())
                    if (IOUtil.getUserData("ShowSeconds").equalsIgnoreCase("1"))
                        consoleClockLabel.setText(TimeUtil.consoleSecondTime());
                    else
                        consoleClockLabel.setText(TimeUtil.consoleTime());

                consoleClockLabel.setToolTipText(timeUtil.weatherTime());

            },0, 500, TimeUnit.MILLISECONDS);

            consoleClockLabel.setVisible(updateConsoleClock);

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (IOUtil.getUserData("HourlyChimes").equalsIgnoreCase("1"))
                    IOUtil.playMusic("src/com/cyder/sys/audio/chime.mp3");

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
                int len = mainGeneralUtil.getBackgrounds().length;

                if (len <= 1)
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you only have one background file so there's no random element to be chosen.");

                else if (len > 1) {
                    try {
                        File[] backgrounds = mainGeneralUtil.getBackgrounds();

                        mainGeneralUtil.setCurrentBackgroundIndex(NumberUtil.randInt(0, (backgrounds.length) - 1));

                        String newBackFile = mainGeneralUtil.getCurrentBackground().toString();

                        ImageIcon newBack;
                        int tempW = 0;
                        int tempH = 0;

                        if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                            newBack = new ImageIcon(ImageUtil.resizeImage((int) systemUtil.getScreenSize().getWidth(),
                                    (int) systemUtil.getScreenSize().getHeight(), new File(newBackFile)));
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        }

                        else {
                            newBack = new ImageIcon(newBackFile);
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        }

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
                        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 30,
                                2,(consoleClockLabel.getText().length() * 17), 25);
                    }

                    catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                }

                else
                   throw new FatalException("Only one but also more than one background.");
            }

            frameAni.enterAnimation(consoleFrame);

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (!networkUtil.internetReachable())
                    notify("Internet connection slow or unavailble",
                            3000, ArrowDirection.TOP, VanishDirection.TOP, parentPane,450);
            },0, 10, TimeUnit.MINUTES);

            consoleClockLabel.setVisible(updateConsoleClock);

            lineColor = new ImageUtil().getDominantColorOpposite(ImageIO.read(mainGeneralUtil.getCurrentBackground()));

            if (IOUtil.getUserData("DebugWindows").equals("1")) {
                StatUtil.systemProperties();
                StatUtil.computerProperties();
                StatUtil.javaProperties();
                StatUtil.debugMenu(outputArea);
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private MouseAdapter consoleMenu = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src/com/cyder/sys/pictures/menu2.png"));

                menuLabel = new JLabel("");
                menuLabel.setOpaque(true);
                menuLabel.setBackground(new Color(26,32,51));

                parentPane.add(menuLabel,1,0);

                menuLabel.setBounds(-150,30, 130,290);
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
                calculatorLabel.setBounds(5,20,150,20);

                JLabel musicLabel = new JLabel("Music");
                musicLabel.setFont(CyderFonts.weatherFontSmall);
                musicLabel.setForeground(CyderColors.vanila);
                musicLabel.setBounds(5,50,150,20);
                menuLabel.add(musicLabel);
                musicLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        IOUtil.mp3("", mainGeneralUtil.getUsername(), mainGeneralUtil.getUserUUID());
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
                weatherLabel.setBounds(5,80,150,20);
                weatherLabel.setOpaque(false);
                weatherLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        WeatherWidget ww = new WeatherWidget();
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
                noteLabel.setBounds(5,110,150,20);
                noteLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        userNotes = new Notes(mainGeneralUtil.getUserUUID());
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
                editUserLabel.setBounds(5,140,150,20);
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
                temperatureLabel.setBounds(5,170,150,20);
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
                youtubeLabel.setBounds(5,200,150,20);
                youtubeLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        networkUtil.internetConnect("https://youtube.com");
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
                twitterLabel.setBounds(5,230,150,20);
                twitterLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        networkUtil.internetConnect("https://twitter.com");
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
                logoutLabel.setBounds(5,255,150,30);
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

                animation.jLabelXRight(-150,0,10,8, menuLabel);
            }

            else if (menuLabel.isVisible()){
                minimizeMenu();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src/com/cyder/sys/pictures/menu2.png"));
            }

            else {
                menuButton.setIcon(new ImageIcon("src/com/cyder/sys/pictures/menuSide2.png"));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (menuLabel.isVisible()) {
                menuButton.setIcon(new ImageIcon("src/com/cyder/sys/pictures/menu1.png"));
            }

            else {
                menuButton.setIcon(new ImageIcon("src/com/cyder/sys/pictures/menuSide1.png"));
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
                            IOUtil.playMusic("src/com/cyder/sys/audio/f17.mp3");
                        else
                           println("Interesting F" + (i - 61427) + " key");
                    }
                }
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
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
                    consoleFrame.setIconImage(systemUtil.getCyderIconBlink().getImage());

                else
                    consoleFrame.setIconImage(systemUtil.getCyderIcon().getImage());
            }

        }, 0, 3, TimeUnit.SECONDS);
    }

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
                    mainGeneralUtil.setCurrentDowns(0);

                    if (!stringUtil.getUserInputMode()) {
                        handle(op);
                    }

                    else if (stringUtil.getUserInputMode()) {
                        stringUtil.setUserInputMode(false);
                        handleSecond(op);
                    }
                }

                inputField.setText("");
            }

            catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }
    };

    private void login() {
        if (loginFrame != null)
            loginFrame.closeAnimation();

        IOUtil.cleanUpUsers();

        loginFrame = new CyderFrame(800,800,new ImageIcon("src/com/cyder/sys/pictures/login.png"));
        loginFrame.setTitlePosition(TitlePosition.LEFT);
        loginFrame.setTitle(IOUtil.getSystemData("Version") + " login");

        if (consoleFrame == null || !consoleFrame.isActive() || !consoleFrame.isVisible())
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        else
            loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        loginLabel2 = new JLabel();
        loginLabel2.setIcon(new ImageIcon("src/com/cyder/sys/pictures/Login2.png"));
        loginLabel2.setBounds(800,0 , 800, 800);

        loginFrame.getContentPane().add(loginLabel2);

        loginLabel3 = new JLabel();
        loginLabel3.setIcon(new ImageIcon("src/com/cyder/sys/pictures/Login3.png"));
        loginLabel3.setBounds(800,0 , 800, 800);

        loginFrame.getContentPane().add(loginLabel3);

        loginAnimation();

        nameField = new JTextField(20);
        nameField.setToolTipText("Username");
        nameField.setBounds(225,400,350,50);
        nameField.setBackground(CyderColors.vanila);
        nameField.setSelectionColor(CyderColors.selectionColor);
        nameField.setBorder(new LineBorder(CyderColors.navy,4,false));
        nameField.setFont(CyderFonts.weatherFontSmall.deriveFont(30f));
        nameField.setForeground(CyderColors.navy);
        nameField.setCaretColor(CyderColors.navy);
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
            if (nameField.getText().length() > 20) {
                evt.consume();
            }

            if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                pass.requestFocus();
            }
            }
        });

        loginFrame.getContentPane().add(nameField);

        pass = new JPasswordField();
        pass.setToolTipText("Password");
        pass.setBounds(225,500,350,50);
        pass.setBackground(CyderColors.vanila);
        pass.setSelectionColor(CyderColors.selectionColor);
        pass.setBorder(new LineBorder(CyderColors.navy,4,false));
        pass.setFont(CyderFonts.weatherFontBig.deriveFont(40f));
        pass.setForeground(CyderColors.navy);
        pass.setCaretColor(CyderColors.navy);
        pass.addActionListener(e -> {
            String Username = nameField.getText().trim();

            if (!stringUtil.empytStr(Username)) {
                Username = Username.substring(0, 1).toUpperCase() + Username.substring(1);

                char[] Password = pass.getPassword();

                if (!stringUtil.empytStr(Username)) {
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

        loginFrame.getContentPane().add(pass);

        newUserLabel = new JLabel("Don't have an account?", SwingConstants.CENTER);
        newUserLabel.setFont(new Font("tahoma",Font.BOLD,22));
        newUserLabel.setForeground(CyderColors.vanila);
        newUserLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                createUser();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                newUserLabel.setText("Create an account!");
                newUserLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newUserLabel.setText("Don't have an account?");
                newUserLabel.setForeground(CyderColors.vanila);
            }
        });

        newUserLabel.setBounds(265,650,270,35);

        loginFrame.getContentPane().add(newUserLabel);

        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                nameField.requestFocus();
            }
        });

        File Users = new File("src/users/");
        String[] directories = Users.list((current, name) -> new File(current, name).isDirectory());

        loginFrame.setVisible(true);
        loginFrame.enterAnimation();

        if (directories != null && directories.length == 0)
            loginFrame.notify("<html><b>" + System.getProperty("user.name") + ":<br/>There are no users<br/>please create one</b></html>",
                    4000, ArrowDirection.TOP, StartDirection.TOP, VanishDirection.TOP, 250);
    }

    private void recognize(String Username, char[] Password) {
        try {
            mainGeneralUtil.setUsername(Username);

            if (SecurityUtil.checkPassword(Username, SecurityUtil.toHexString(SecurityUtil.getSHA(Password)))) {
                IOUtil.readUserData();

                if (loginFrame != null)
                    loginFrame.closeAnimation();

                if (consoleFrame != null)
                    frameAni.closeAnimation(consoleFrame);

                console();

                if (IOUtil.getUserData("IntroMusic").equals("1")) {
                    LinkedList<String> MusicList = new LinkedList<>();

                    File UserMusicDir = new File("src/users/" + mainGeneralUtil.getUserUUID() + "/Music");

                    String[] FileNames = UserMusicDir.list();

                    if (FileNames != null)
                        for (String fileName : FileNames)
                            if (fileName.endsWith(".mp3"))
                                MusicList.add(fileName);

                    if (!MusicList.isEmpty())
                        IOUtil.playMusic(
                                "src/users/" + mainGeneralUtil.getUserUUID() + "/Music/" +
                                        (FileNames[NumberUtil.randInt(0,FileNames.length - 1)]));
                    else
                        IOUtil.playMusic("src/com/cyder/sys/audio/Suprise.mp3");
                }
            }

            else if (loginFrame.isVisible()){
                nameField.setText("");
                pass.setText("");
                nameField.requestFocusInWindow();
                loginFrame.notify("Could not recognize user",
                        2000, ArrowDirection.TOP, StartDirection.TOP, VanishDirection.TOP, 280);
            }

            else
                login();
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void exitFullscreen() {
        mainGeneralUtil.refreshBackgrounds();
        File[] backgrounds = mainGeneralUtil.getBackgrounds();
        int index = mainGeneralUtil.getCurrentBackgroundIndex();
        String backFile = backgrounds[index].toString();

        int width = 0;
        int height = 0;

        ImageIcon backIcon;

        switch (mainGeneralUtil.getConsoleDirection()) {
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
                parentLabel.setIcon(new ImageIcon(ImageUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().toString(),GeneralUtil.getConsoleDirection())));

                break;
            default:
                backIcon = new ImageIcon(backFile);

                if (mainGeneralUtil.getConsoleDirection() == ConsoleDirection.LEFT || mainGeneralUtil.getConsoleDirection() == ConsoleDirection.RIGHT) {
                    height = backIcon.getIconWidth();
                    width = backIcon.getIconHeight();
                }

                parentLabel.setIcon(new ImageIcon(ImageUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().toString(),GeneralUtil.getConsoleDirection())));

                break;
        }

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
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 30,
                2,(consoleClockLabel.getText().length() * 17), 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();

        consoleFrame.setLocationRelativeTo(null);

        if (editUserFrame != null && editUserFrame.isVisible())
            editUserFrame.requestFocus();
    }

    private void refreshFullscreen() {
        mainGeneralUtil.refreshBackgrounds();
        File[] backgrounds = mainGeneralUtil.getBackgrounds();
        int index = mainGeneralUtil.getCurrentBackgroundIndex();
        String backFile = backgrounds[index].toString();

        ImageIcon backIcon = new ImageIcon(backFile);

        BufferedImage fullimg = ImageUtil.resizeImage((int) systemUtil.getScreenSize().getWidth(),
                (int) systemUtil.getScreenSize().getHeight(), new File(backFile));
        int fullW = fullimg.getWidth();
        int fullH = fullimg.getHeight();

        parentLabel.setIcon(new ImageIcon(fullimg));

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
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 30,
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
                mainGeneralUtil.refreshBackgrounds();

                File[] backgrounds = mainGeneralUtil.getBackgrounds();
                int oldIndex = (mainGeneralUtil.getCurrentBackgroundIndex() == 0 ? backgrounds.length - 1 : mainGeneralUtil.getCurrentBackgroundIndex() - 1);
                String oldBackFile = backgrounds[oldIndex].toString();
                String newBackFile = mainGeneralUtil.getCurrentBackground().toString();

                ImageIcon oldBack = new ImageIcon(oldBackFile);
                BufferedImage newBack = ImageIO.read(new File(newBackFile));

                BufferedImage temporaryImage;
                int tempW = 0;
                int tempH = 0;
                
                if (IOUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                    oldBack = new ImageIcon(ImageUtil.resizeImage((int) systemUtil.getScreenSize().getWidth(),
                            (int) systemUtil.getScreenSize().getHeight(),new File(oldBackFile)));
                    newBack = ImageUtil.resizeImage((int) systemUtil.getScreenSize().getWidth(), (int) systemUtil.getScreenSize().getHeight(),
                            new File(newBackFile));
                    temporaryImage = ImageUtil.resizeImage((int) systemUtil.getScreenSize().getWidth(), (int) systemUtil.getScreenSize().getHeight(),
                            new File(oldBackFile));
                    tempW = temporaryImage.getWidth();
                    tempH = temporaryImage.getHeight();
                }

                else {
                    newBack = ImageUtil.resizeImage(newBack.getWidth(), newBack.getHeight(),new File(newBackFile));
                    temporaryImage = ImageUtil.resizeImage(newBack.getWidth(), newBack.getHeight(), new File(oldBackFile));
                    tempW = temporaryImage.getWidth();
                    tempH = temporaryImage.getHeight();
                }

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
                consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2 - 30,
                        2,(consoleClockLabel.getText().length() * 17), 25);
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
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
            ErrorHandler.handle(e);
        }

        return null;
    }

    private void loginAnimation() {
        new Thread() {
            int count = 2;

            @Override
            public void run() {
                try {
                    while (true) {
                        long scrollDelay = 4000;
                        int miliDelay = 1;
                        int increment = 2;

                        switch (count) {
                            case 0:
                                loginLabel2.setBounds(800,0,800,800);
                                
                                Thread.sleep(scrollDelay);

                                animation.jLabelXLeft(440, 0 ,miliDelay, increment, loginLabel2);

                                Thread.sleep(scrollDelay);

                                count = 1;
                                break;
                            case 1:
                                Thread.sleep(scrollDelay);

                                loginLabel2.setBounds(0,0,800,800);
                                loginLabel3.setBounds(800,0,800,800);
                                animation.jLabelXLeft(0, -800, miliDelay, increment, loginLabel2);
                                animation.jLabelXLeft(800, 0 ,miliDelay, increment, loginLabel3);

                                Thread.sleep(scrollDelay);

                                count = 2;
                                break;
                            case 2:
                                Thread.sleep(scrollDelay);

                                loginLabel3.setBounds(0,0,800,800);
                                loginLabel2.setBounds(-800,0,800,800);
                                animation.jLabelXRight(0, 800, miliDelay, increment, loginLabel3);
                                animation.jLabelXRight(-800,0,miliDelay,increment, loginLabel2);

                                Thread.sleep(scrollDelay);

                                count = 1;
                                break;
                        }
                    }
                }

                catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }
        }.start();
    }

    private void clc() {
        outputArea.setText("");
        inputField.setText("");
    }

    private void handleSecond(String input) {
        try {
            String desc = stringUtil.getUserInputDesc();

            if (desc.equalsIgnoreCase("url") && !stringUtil.empytStr(input)) {
                URI URI = new URI(input);
                println("Attempting to connect...");
                networkUtil.internetConnect(URI);
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
                networkUtil.internetConnect("https://www.google.com/search?q=" + input);
            }

            else if (desc.equalsIgnoreCase("youtube")&& input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                networkUtil.internetConnect("https://www.youtube.com/results?search_query=" + input);
            }

            else if (desc.equalsIgnoreCase("math") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                networkUtil.internetConnect("https://www.wolframalpha.com/input/?i=" + input);
            }

            else if (desc.equalsIgnoreCase("binary")) {
                if (input.matches("[0-9]+") && !stringUtil.empytStr(input)) {
                    String Print = NumberUtil.toBinary(Integer.parseInt(input));
                    println(input + " converted to binary equals: " + Print);
                }

                else {
                    println("Your value must only contain numbers.");
                }
            }

            else if (desc.equalsIgnoreCase("wiki") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ","_");
                println("Attempting to connect...");
                networkUtil.internetConnect("https://en.wikipedia.org/wiki/" + input);
            }

            else if (desc.equalsIgnoreCase("disco") && input != null && !input.equals("")) {
                println("I hope you're not the only one at this party.");
                systemUtil.disco(Integer.parseInt(input));
            }

            else if (desc.equalsIgnoreCase("youtube word search") && input != null && !input.equals("")) {
                String browse = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
                browse = browse.replace("REPLACE", input).replace(" ", "+");
                networkUtil.internetConnect(browse);
            }

            else if (desc.equalsIgnoreCase("random youtube")) {
               try {
                    int threads = Integer.parseInt(input);

                    notify("The" + (threads > 1 ? " scripts have " : " script has ") + "started. At any point, type \"stop script\"",
                            4000, ArrowDirection.TOP, VanishDirection.TOP, parentPane, (threads > 1 ? 620 : 610));

                    for (int i = 0 ; i < threads ; i++) {
                        YoutubeThread current = new YoutubeThread(outputArea);
                        youtubeThreads.add(current);
                    }
                }

                catch (NumberFormatException e) {
                    println("Invalid input for number of threads to start.");
                }

               catch (Exception e) {
                   ErrorHandler.handle(e);
               }
            }

            else if (desc.equalsIgnoreCase("anagram1")) {
                println("Enter your second word");
                anagram = input;
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("anagram2");
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
                new ImageUtil().pixelate(pixelateFile, Integer.parseInt(input));
            }

            else if (desc.equalsIgnoreCase("alphabetize")) {
                char[] Sorted = input.toCharArray();
                Arrays.sort(Sorted);
                println("\"" + input + "\" alphabetically organized is \"" + new String(Sorted) + "\".");
            }

            else if (desc.equalsIgnoreCase("suggestion")) {
                stringUtil.logToDo(input);
            }

            else if (desc.equalsIgnoreCase("addbackgrounds")) {
                if (InputUtil.confirmation(input)) {
                    editUser();
                    networkUtil.internetConnect("https://images.google.com/");
                }

                else
                    println("Okay nevermind then");
            }

            else if (desc.equalsIgnoreCase("logoff")) {
                if (InputUtil.confirmation(input)) {
                    String shutdownCmd = "shutdown -l";
                    Runtime.getRuntime().exec(shutdownCmd);
                }

                else
                    println("Okay nevermind then");
            }

            else if (desc.equalsIgnoreCase("deleteuser")) {
                if (!InputUtil.confirmation(input)) {
                    println("User " + mainGeneralUtil.getUsername() + " was not removed.");
                    return;
                }

                frameAni.closeAnimation(consoleFrame);
                systemUtil.deleteFolder(new File("src/users/" + mainGeneralUtil.getUserUUID()));

                String dep = SecurityUtil.getDeprecatedUUID();

                File renamed = new File("src/users/" + dep);
                while (renamed.exists()) {
                    dep = SecurityUtil.getDeprecatedUUID();
                    renamed = new File("src/users/" + dep);
                }

                File old = new File("src/users/" + mainGeneralUtil.getUserUUID());
                old.renameTo(renamed);

                Frame[] frames = Frame.getFrames();

                for(Frame f: frames)
                    f.dispose();

                login();
            }

            else if (desc.equalsIgnoreCase("pixelatebackground")) {
                BufferedImage img = ImageUtil.pixelate(ImageIO.read(mainGeneralUtil.getCurrentBackground().getAbsoluteFile()), Integer.parseInt(input));

                String searchName = mainGeneralUtil.getCurrentBackground().getName().replace(".png", "")
                        + "_Pixelated_Pixel_Size_" + Integer.parseInt(input) + ".png";

                File saveFile = new File("src/users/" + mainGeneralUtil.getUserUUID() +
                        "/Backgrounds/" + searchName);

                ImageIO.write(img, "png", saveFile);

                mainGeneralUtil.refreshBackgrounds();

                File[] backgrounds = mainGeneralUtil.getBackgrounds();

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
        }

        catch (Exception e) {
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
                println("Sorry, " + mainGeneralUtil.getUsername() + ", but that language is prohibited.");
                operation = "";
            }

            else if (stringUtil.isPalindrome(operation.replace(" ", "").toCharArray()) && operation.length() > 3){
                println("Nice palindrome.");
            }

            else if (((hasWord("quit") && !hasWord("db")) ||
                    (eic("leave") || (hasWord("stop") && !hasWord("music") && !hasWord("script") && !hasWord("scripts")) ||
                            hasWord("exit") || eic("close"))) && !has("dance"))
            {
                exit();
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
                int choice = NumberUtil.randInt(1,6);

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
                println(TimeUtil.weatherTime());
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
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("url");
                println("Enter your desired URL");
            }

            else if (hasWord("temperature") || eic("temp")) {
                TempConverter tc = new TempConverter();
            }

            else if (has("click me")) {
                ClickMe.clickMe();
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

            else if (((hasWord("who") || hasWord("what")) && has("you"))) {
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
                systemUtil.resetMouse();
            }

            else if (eic("logoff")) {
               println("Are you sure you want to log off your computer?\nThis is not Cyder we are talking about (Enter yes/no)");
                stringUtil.setUserInputDesc("logoff");
               inputField.requestFocus();
                stringUtil.setUserInputMode(true);
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
                printImage("src/com/cyder/sys/pictures/msu.png");
            }

            else if (hasWord("toystory")) {
                IOUtil.playMusic("src/com/cyder/sys/audio/TheClaw.mp3");
            }

            else if (has("stop") && has("music")) {
                IOUtil.stopMusic();
            }

            else if (hasWord("reset") && hasWord("clipboard")) {
                StringSelection selection = new StringSelection(null);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                println("Clipboard has been reset.");
            }

            else if ((has("graphing") && has("calculator")) || has("desmos") || has("graphing")) {
                networkUtil.internetConnect("https://www.desmos.com/calculator");
            }

            else if (has("airHeads xtremes") || has("candy")) {
                networkUtil.internetConnect("http://airheads.com/candy#xtremes");
            }

            else if (hasWord("prime")) {
                println("Enter any positive integer and I will tell you if it's prime and what it's divisible by.");
                stringUtil.setUserInputDesc("prime");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            }

            else if (hasWord("youtube") && (!has("word search") && !has("mode") && !has("random") && !has("thumbnail"))) {
                println("What would you like to search YouTube for?");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("youtube");
            }

            else if ((hasWord("google") && !has("mode") && !has("stupid"))) {
                println("What would you like to Google?");
                stringUtil.setUserInputDesc("google");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            }

            else if (eic("404")) {
                networkUtil.internetConnect("http://google.com/=");
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
                networkUtil.internetConnect("https://www.triangle-calculator.com/");
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
                stringUtil.setUserInputDesc("math");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            }

            else if (eic("nathan")) {
                printlnImage("src/com/cyder/sys/pictures/me.png");
            }

            else if ((eic("error") || eic("errors")) && !hasWord("throw")) {
                File WhereItIs = new File("src/users/" + mainGeneralUtil.getUserUUID() + "/Throws/");
                Desktop.getDesktop().open(WhereItIs);
            }

            else if (eic("help")) {
                stringUtil.help(outputArea);
            }

            else if (hasWord("light") && hasWord("saber")) {
                IOUtil.playMusic("src/com/cyder/sys/audio/Lightsaber.mp3");
            }

            else if (hasWord("xbox")) {
                IOUtil.playMusic("src/com/cyder/sys/audio/xbox.mp3");
            }

            else if (has("star") && has("trek")) {
                IOUtil.playMusic("src/com/cyder/sys/audio/StarTrek.mp3");
            }

            else if (eic("cmd") || (hasWord("command") && hasWord("prompt"))) {
                File WhereItIs = new File("c:\\Windows\\System32\\cmd.exe");
                Desktop.getDesktop().open(WhereItIs);
            }

            else if (hasWord("shakespeare")) {
                int rand = NumberUtil.randInt(1,2);

                if (rand == 1) {
                    println("Glamis hath murdered sleep, and therefore Cawdor shall sleep no more, Macbeth shall sleep no more.");
                }

                else {
                    println("To be, or not to be, that is the question: Whether 'tis nobler in the mind to suffer the slings and arrows of "
                            + "outrageous fortune, or to take arms against a sea of troubles and by opposing end them.");
                }
            }

            else if (hasWord("windows")) {
                IOUtil.playMusic("src/com/cyder/sys/audio/windows.mp3");
            }

            else if (hasWord("binary")) {
                println("Enter a decimal number to be converted to binary.");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("binary");
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

                networkUtil.internetConnect("http://www.dictionary.com/browse/" + Define + "?s=t");
            }

            else if (hasWord("wikipedia")) {
                println("What would you like to look up on Wikipedia?");
                stringUtil.setUserInputDesc("wiki");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            }

            else if (firstWord.equalsIgnoreCase("synonym")) {
                String Syn = operation.replace("synonym","");
                Syn = Syn.replace("'", "").replace(" ", "+");
                networkUtil.internetConnect("http://www.thesaurus.com//browse//" + Syn);
            }

            else if (hasWord("board")) {
                networkUtil.internetConnect("http://gameninja.com//games//fly-squirrel-fly.html");
            }

            else if (hasWord("open cd")) {
                systemUtil.openCD("D:\\");
            }

            else if (hasWord("close cd")) {
                systemUtil.closeCD("D:\\");
            }

            else if (hasWord("font") && hasWord("reset")) {
                inputField.setFont(CyderFonts.defaultFont);
                outputArea.setFont(CyderFonts.defaultFont);
                println("The font has been reset.");
                IOUtil.writeUserData("Fonts",outputArea.getFont().getName());
            }

            else if (hasWord("reset") && hasWord("color")) {
                outputArea.setForeground(CyderColors.vanila);
                inputField.setForeground(CyderColors.vanila);
                println("The text color has been reset.");
                IOUtil.writeUserData("Foreground",ColorUtil.rgbtohexString(CyderColors.defaultColor));
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
                killAllYoutube();
                println("How many isntances of the script do you want to start?");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("random youtube");
            }

            else if (hasWord("arduino")) {
                networkUtil.internetConnect("https://www.arduino.cc/");
            }

            else if (has("rasberry pi")) {
                networkUtil.internetConnect("https://www.raspberrypi.org/");
            }

            else if (eic("&&")) {
                println("||");
            }

            else if (eic("||")) {
                println("&&");
            }

            else if (eic("youtube word search")) {
                println("Enter the desired word you would like to find in a YouTube URL");
                stringUtil.setUserInputDesc("youtube word search");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
            }

            else if (hasWord("disco")) {
                println("How many iterations would you like to disco for? (Enter a positive integer)");
                stringUtil.setUserInputMode(true);
                inputField.requestFocus();
                stringUtil.setUserInputDesc("disco");
            }

            else if (hasWord("game")) {
                File WhereItIs = new File("src/com/cyder/sys/jars/Jailbreak.jar");
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
                StatUtil.javaProperties();
            }

            else if ((hasWord("edit") && hasWord ("user")) || (hasWord("font") && !hasWord("reset")) || (hasWord("color") && !hasWord("reset")) || (eic("preferences") || eic("prefs"))) {
                editUser();
            }

            else if (hasWord("story") && hasWord("tell")) {
                println("It was a lazy day. Cyder was enjoying a deep sleep when suddenly " + mainGeneralUtil.getUsername() + " started talking to Cyder."
                        + " It was at this moment that Cyder knew its day had been ruined.");
            }

            else if (eic("hey")) {
                IOUtil.playMusic("src/com/cyder/sys/audio/heyya.mp3");
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
                networkUtil.internetConnect("https://www.youtube.com/user/Vexento/videos");
            }

            else if (hasWord("minecraft")) {
                MinecraftWidget mw = new MinecraftWidget();
            }

            else if (eic("loop")) {
                println("ErrorHandler.handle(\"loop\");");
            }

            else if (hasWord("cyder") && has("dir")) {
                if (SecurityUtil.compMACAddress(SecurityUtil.getMACAddress())) {
                    String CurrentDir = System.getProperty("user.dir");
                    IOUtil.openFile(CurrentDir);
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
                networkUtil.internetConnect("http://papersplea.se/");
            }

            else if (eic("java")) {
                println("public class main {");
                println("      public static void main(String[] args) {");
                println("            System.out.println(\"Hello World!\");");
                println("      }");
                println("}");
            }

            else if (hasWord("coffee")) {
                networkUtil.internetConnect("https://www.google.com/search?q=coffe+shops+near+me");
            }

            else if (hasWord("ip")) {
                println(InetAddress.getLocalHost().getHostAddress());
            }

            else if(hasWord("html") || hasWord("html5")) {
                consoleFrame.setIconImage(new ImageIcon("src/com/cyder/sys/pictures/html5.png").getImage());
                printlnImage("src/com/cyder/sys/pictures/html5.png");
            }

            else if (hasWord("css")) {
                consoleFrame.setIconImage(new ImageIcon("src/com/cyder/sys/pictures/css.png").getImage());
                printlnImage("src/com/cyder/sys/pictures/css.png");
            }

            else if(hasWord("computer") && hasWord("properties")) {
                println("This may take a second, since this feature counts your PC's free memory");
                StatUtil.computerProperties();
            }

            else if (hasWord("system") && hasWord("properties")) {
                StatUtil.systemProperties();
            }

            else if ((hasWord("pixelate") || hasWord("distort")) && (hasWord("image") || hasWord("picture"))) {
                pixelateFile = IOUtil.getFile();

                if (!pixelateFile.getName().endsWith(".png")) {
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but this feature only supports PNG images");
                }

                else if (pixelateFile != null) {
                    println("Enter your pixel size (Enter a positive integer)");
                    stringUtil.setUserInputDesc("pixelate");
                    inputField.requestFocus();
                    stringUtil.setUserInputMode(true);
                }
            }

            else if (hasWord("donuts")) {
                networkUtil.internetConnect("https://www.dunkindonuts.com/en/food-drinks/donuts/donuts");
            }

            else if (hasWord("anagram")) {
                println("This function will tell you if two"
                        + "words are anagrams of each other."
                        + " Enter your first word");
                stringUtil.setUserInputDesc("anagram1");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);

            }

            else if (eic("controlc")) {
                stringUtil.setUserInputMode(false);
                killAllYoutube();
                stringUtil.killBletchy();
                println("Escaped");
            }

            else if (has("alphabet") && (hasWord("sort") || hasWord("organize") || hasWord("arrange"))) {
                println("Enter your word to be alphabetically rearranged");
                inputField.requestFocus();
                stringUtil.setUserInputMode(true);
                stringUtil.setUserInputDesc("alphabetize");
            }

            else if (hasWord("mp3") || hasWord("music")) {
                IOUtil.mp3("", mainGeneralUtil.getUsername(), mainGeneralUtil.getUserUUID());
            }

            else if (hasWord("bai")) {
                networkUtil.internetConnect("http://www.drinkbai.com");
            }

            else if (has("occam") && hasWord("razor")) {
                networkUtil.internetConnect("http://en.wikipedia.org/wiki/Occam%27s_razor");
            }

            else if (hasWord("cyder") && (has("picture") || has("image"))) {
                if (SecurityUtil.compMACAddress(SecurityUtil.getMACAddress())) {
                    IOUtil.openFile("src/com/cyder/sys/pictures");
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
                println("You are currently in " + new IPUtil().getUserCity() + ", " +
                        new IPUtil().getUserState() + " and your Internet Service Provider is " + new IPUtil().getIsp());
            }

            else if (hasWord("fibonacci")) {
                fib(0,1);
            }

            else if (hasWord("throw") && hasWord("error")) {
                throw new CyderException("Error thrown on " + TimeUtil.userTime());
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
                networkUtil.internetConnect("about:blank");
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
            }

            else if (hasWord("debug") && hasWord("menu")) {
                StatUtil.debugMenu(outputArea);
            }

            else if (hasWord("hangman")) {
                Hangman Hanger = new Hangman();
                Hanger.startHangman();
            }

            else if (hasWord("rgb") || hasWord("hex")) {
                ColorConverter.colorConverter();
            }

            else if (hasWord("dance")) {
                //todo for all CyderFrames, make them dance, make this action ctrl+c -able
            }

            else if (hasWord("clear") && (
                    hasWord("operation") || hasWord("command")) &&
                    hasWord("list")) {
                operationList.clear();
                scrollingIndex = 0;
                println("The operation list has been cleared.");
            }

            else if (eic("pin") || eic("login")) {
                login();
            }

            else if ((hasWord("delete") ||
                    hasWord("remove")) &&
                    (hasWord("user") ||
                            hasWord("account"))) {

                println("Are you sure you want to permanently delete this account? This action cannot be undone! (yes/no)");
                stringUtil.setUserInputMode(true);
                inputField.requestFocus();
                stringUtil.setUserInputDesc("deleteuser");
            }

            else if ((hasWord("create") || hasWord("new")) &&
                    hasWord("user")) {
                createUser();
            }

            else if (hasWord("pixelate") && hasWord("background")) {
                println("Enter your pixel size (a positive integer)");
                stringUtil.setUserInputDesc("pixelatebackground");
                stringUtil.setUserInputMode(true);
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
                IOUtil.playMusic("src/com/cyder/sys/audio/commando.mp3");
            }

            else if (eic("1-800-273-8255") || eic("18002738255")) {
                IOUtil.playMusic("src/com/cyder/sys/audio/1800.mp3");
            }

            else if (hasWord("resize") && (hasWord("image") || hasWord("picture"))) {
                ImageResizer IR = new ImageResizer();
            }

            else if (hasWord("barrel") && hasWord("roll")) {
                barrelRoll();
            }

            else if (hasWord("lines") && hasWord("code")) {
                println("Total lines of code: " + NumberUtil.totalCodeLines(new File(System.getProperty("user.dir"))));
            }

            else if (hasWord("threads") && !hasWord("daemon")) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                for (int i = 0; i < num ; i++)
                    if (!printThreads[i].isDaemon())
                        println(printThreads[i].getName());
            }

            else if (hasWord("threads") && hasWord("daemon")) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                for (int i = 0; i < num ; i++)
                    println(printThreads[i].getName());
            }

            else if (eic("askew")) {
                askew();
            }

            else if (hasWord("press") && (hasWord("F17") || hasWord("f17"))) {
                Robot rob = new Robot();
                rob.keyPress(KeyEvent.VK_F17);
            }

            else if (hasWord("logout")) {
                frameAni.closeAnimation(consoleFrame);
                login();
            }

            else if (hasWord("duke")) {
                printImage("src/com/cyder/sys/pictures/Duke.png");
            }

            else if ((hasWord("wipe") || hasWord("clear") || hasWord("delete")) && has("error")) {
                if (SecurityUtil.compMACAddress(SecurityUtil.getMACAddress())) {
                    IOUtil.wipeErrors();

                    println("Deleted all user erorrs");
                }

                else
                    println("Sorry, " + mainGeneralUtil.getUsername() + ", but you don't have permission to do that.");
            }

            else if (eic("test")) {
                CyderFrame cf = new CyderFrame(400,400,new ImageIcon("src/com/cyder/sys/pictures/DebugBackground.png"));
                cf.setLocationRelativeTo(null);
                cf.setVisible(true);
                cf.notify("Test text",3000,ArrowDirection.BOTTOM,150);
            }

            else {
                println("Sorry, " + mainGeneralUtil.getUsername() + ", but I don't recognize that command." +
                        " You can make a suggestion by clicking the \"Suggest something\" button.");
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private boolean handleMath(String op) {
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
                    return true;
                }

                else if (mathop.equalsIgnoreCase("ceil")) {
                    println(Math.ceil(param1));
                    return true;
                }

                else if (mathop.equalsIgnoreCase("floor")) {
                    println(Math.floor(param1));
                    return true;
                }

                else if (mathop.equalsIgnoreCase("log")) {
                    println(Math.log(param1));
                    return true;
                }

                else if (mathop.equalsIgnoreCase("log10")) {
                    println(Math.log10(param1));
                    return true;
                }

                else if (mathop.equalsIgnoreCase("max")) {
                    println(Math.max(param1,param2));
                    return true;
                }

                else if (mathop.equalsIgnoreCase("min")) {
                    println(Math.min(param1,param2));
                    return true;
                }

                else if (mathop.equalsIgnoreCase("pow")) {
                    println(Math.pow(param1,param2));
                    return true;
                }

                else if (mathop.equalsIgnoreCase("round")) {
                    println(Math.round(param1));
                    return true;
                }

                else if (mathop.equalsIgnoreCase("sqrt")) {
                    println(Math.sqrt(param1));
                    return true;
                }

                else if (mathop.equalsIgnoreCase("convert2")) {
                    println(NumberUtil.toBinary((int)(param1)));
                    return true;
                }
            }
        }

        catch(Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
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
            ErrorHandler.handle(e);
        }
    }

    private void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
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
            ErrorHandler.handle(e);
        }
    }

    private void changeUsername(String newName) {
        try {
            IOUtil.readUserData();
            IOUtil.writeUserData("name",newName);

            mainGeneralUtil.setUsername(newName);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void changePassword(char[] newPassword) {
        try {
            IOUtil.readUserData();
            IOUtil.writeUserData("password", SecurityUtil.toHexString(SecurityUtil.getSHA(newPassword)));
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void initMusicBackgroundList() {
        File backgroundDir = new File("src/users/" + mainGeneralUtil.getUserUUID() + "/Backgrounds");
        File musicDir = new File("src/users/" + mainGeneralUtil.getUserUUID() + "/Music");

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

        editUserFrame = new CyderFrame(1000,800,new ImageIcon("src/com/cyder/sys/pictures/DebugBackground.png"));
        editUserFrame.setTitlePosition(TitlePosition.LEFT);
        editUserFrame.setTitle("Edit User");

        switchingPanel = new JLabel();
        switchingPanel.setForeground(new Color(255,255,255));
        switchingPanel.setBounds(140,70,720, 500);
        switchingPanel.setOpaque(true);
        switchingPanel.setBorder(new LineBorder(CyderColors.navy,5,false));
        switchingPanel.setBackground(new Color(255,255,255));
        editUserFrame.getContentPane().add(switchingPanel);

        switchToMusicAndBackgrounds();

        backwardPanel = new CyderButton("< Prev");
        backwardPanel.setBackground(CyderColors.regularRed);
        backwardPanel.setColors(CyderColors.regularRed);
        backwardPanel.setBorder(new LineBorder(CyderColors.navy,5,false));
        backwardPanel.setFont(CyderFonts.weatherFontSmall);
        backwardPanel.addActionListener(e -> lastEditUser());
        backwardPanel.setBounds(20,380,100,40);
        editUserFrame.getContentPane().add(backwardPanel);

        forwardPanel = new CyderButton("Next >");
        forwardPanel.setBackground(CyderColors.regularRed);
        forwardPanel.setColors(CyderColors.regularRed);
        forwardPanel.setBorder(new LineBorder(CyderColors.navy,5,false));
        forwardPanel.setFont(CyderFonts.weatherFontSmall);
        forwardPanel.addActionListener(e -> nextEditUser());
        forwardPanel.setBounds(1000 - 120,380,100,40);
        editUserFrame.getContentPane().add(forwardPanel);

        JTextField changeUsernameField = new JTextField(10);
        changeUsernameField.addActionListener(e -> changeUsername.doClick());
        changeUsernameField.setFont(CyderFonts.weatherFontSmall);
        changeUsernameField.setSelectionColor(CyderColors.selectionColor);
        changeUsernameField.setBorder(new LineBorder(CyderColors.navy,5,false));
        changeUsernameField.setBounds(100,700,300,40);
        editUserFrame.getContentPane().add(changeUsernameField);

        changeUsername = new CyderButton("Change Username");
        changeUsername.setBackground(CyderColors.regularRed);
        changeUsername.setColors(CyderColors.regularRed);
        changeUsername.setBorder(new LineBorder(CyderColors.navy,5,false));
        changeUsername.setFont(CyderFonts.weatherFontSmall);
        changeUsername.addActionListener(e -> {
            String newUsername = changeUsernameField.getText();
            if (!stringUtil.empytStr(newUsername)) {
                changeUsername(newUsername);
                editUserFrame.inform("Username successfully changed","", 300, 200);
                consoleFrame.setTitle(IOUtil.getSystemData("Version") + " Cyder [" + newUsername + "]");
                changeUsernameField.setText("");
            }
        });
        changeUsername.setBounds(100,750,300,40);
        editUserFrame.getContentPane().add(changeUsername);

        CyderButton deleteUser = new CyderButton("Delete User");
        deleteUser.setBackground(CyderColors.regularRed);
        deleteUser.setColors(CyderColors.regularRed);
        deleteUser.setBorder(new LineBorder(CyderColors.navy,5,false));
        deleteUser.setFont(CyderFonts.weatherFontSmall);
        deleteUser.addActionListener(e -> {
            println("Are you sure you want to permanently delete this account? This action cannot be undone! (yes/no)");
            stringUtil.setUserInputMode(true);
            inputField.requestFocus();
            stringUtil.setUserInputDesc("deleteuser");
        });
        deleteUser.setBounds(425,700,150,90);
        editUserFrame.getContentPane().add(deleteUser);

        JPasswordField changePasswordField = new JPasswordField(10);
        changePasswordField.addActionListener(e -> changePassword.doClick());
        changePasswordField.setFont(CyderFonts.weatherFontSmall);
        changePasswordField.setSelectionColor(CyderColors.selectionColor);
        changePasswordField.setBorder(new LineBorder(CyderColors.navy,5,false));
        changePasswordField.setToolTipText("New password");
        changePasswordField.setBounds(600,700,300,40);
        editUserFrame.getContentPane().add(changePasswordField);

        changePassword = new CyderButton("Change Password");
        changePassword.setBackground(CyderColors.regularRed);
        changePassword.setColors(CyderColors.regularRed);
        changePassword.setBorder(new LineBorder(CyderColors.navy,5,false));
        changePassword.setFont(CyderFonts.weatherFontSmall);
        changePassword.addActionListener(e -> {
            char[] newPassword = changePasswordField.getPassword();

            if (newPassword.length > 4) {
                changePassword(newPassword);
                editUserFrame.inform("Password successfully changed","", 300, 200);
                changePasswordField.setText("");
            }

            else {
                editUserFrame.inform("Sorry, " + mainGeneralUtil.getUsername() + ", " +
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
        BackgroundLabel.setFont(CyderFonts.weatherFontBig);
        BackgroundLabel.setBounds(720 / 2 - 375 / 2,10,375,40);
        switchingPanel.add(BackgroundLabel);

        initMusicBackgroundList();

        musicBackgroundSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        musicBackgroundScroll = new CyderScrollPane(musicBackgroundSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        musicBackgroundScroll.setSize(400, 400);
        musicBackgroundScroll.setFont(CyderFonts.weatherFontBig);
        musicBackgroundScroll.setThumbColor(CyderColors.regularRed);
        musicBackgroundSelectionList.setBackground(new Color(255,255,255));
        musicBackgroundScroll.getViewport().setBackground(new Color(0,0,0,0));
        musicBackgroundScroll.setBounds(20,60,680,360);
        switchingPanel.add(musicBackgroundScroll);

        addMusicBackground = new CyderButton("Add");
        addMusicBackground.setBorder(new LineBorder(CyderColors.navy,5,false));
        addMusicBackground.setColors(CyderColors.regularRed);
        addMusicBackground.setFocusPainted(false);
        addMusicBackground.setBackground(CyderColors.regularRed);
        addMusicBackground.addActionListener(e -> {
            try {
                File addFile = IOUtil.getFile();

                if (addFile == null)
                    return;

                Path copyPath = new File(addFile.getAbsolutePath()).toPath();

                if (addFile != null && addFile.getName().endsWith(".png")) {
                    File Destination = new File("src/users/" + mainGeneralUtil.getUserUUID() + "/Backgrounds/" + addFile.getName());
                    Files.copy(copyPath, Destination.toPath());
                    initMusicBackgroundList();
                    musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                    musicBackgroundScroll.revalidate();
                }

                else if (addFile != null && addFile.getName().endsWith(".mp3")) {
                    File Destination = new File("src/users/" + mainGeneralUtil.getUserUUID() + "/Music/" + addFile.getName());
                    Files.copy(copyPath, Destination.toPath());
                    initMusicBackgroundList();
                    musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                    musicBackgroundScroll.revalidate();
                }

                else {
                    editUserFrame.inform("Sorry, " + mainGeneralUtil.getUsername() + ", but you can only add PNGs and MP3s", "Error",400,200);
                }
            }

            catch (Exception exc) {
                ErrorHandler.handle(exc);
            }
        });
        addMusicBackground.setFont(CyderFonts.weatherFontSmall);
        addMusicBackground.setBounds(20,440,213,40);
        switchingPanel.add(addMusicBackground);

        openMusicBackground = new CyderButton("Open");
        openMusicBackground.setBorder(new LineBorder(CyderColors.navy,5,false));
        openMusicBackground.setColors(CyderColors.regularRed);
        openMusicBackground.setFocusPainted(false);
        openMusicBackground.setBackground(CyderColors.regularRed);
        openMusicBackground.setFont(CyderFonts.weatherFontSmall);
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
                        IOUtil.mp3(ClickedSelectionPath.toString(),mainGeneralUtil.getUsername(),mainGeneralUtil.getUserUUID());
                    }
                }
            }
        });
        openMusicBackground.setBounds(20 + 213 + 20,440,213,40);
        switchingPanel.add(openMusicBackground);

        deleteMusicBackground = new CyderButton("Delete");
        deleteMusicBackground.setBorder(new LineBorder(CyderColors.navy,5,false));
        deleteMusicBackground.setColors(CyderColors.regularRed);
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
                        editUserFrame.inform("Unable to delete the background you are currently using","Error",400,200);

                    else {
                        ClickedSelectionPath.delete();
                        initMusicBackgroundList();
                        musicBackgroundScroll.setViewportView(musicBackgroundSelectionList);
                        musicBackgroundScroll.revalidate();

                        if (ClickedSelection.endsWith(".mp3"))
                            println("Music: " + ClickedSelectionPath.getName().replace(".mp3","") + " successfully deleted.");
                        else if (ClickedSelection.endsWith(".png")) {
                            println("Background: " + ClickedSelectionPath.getName().replace(".png","") + " successfully deleted.");

                            File[] paths = mainGeneralUtil.getBackgrounds();
                            for (int i = 0 ; i < paths.length ; i++) {
                                if (paths[i].equals(mainGeneralUtil.getCurrentBackground())) {
                                    mainGeneralUtil.setCurrentBackgroundIndex(i);
                                    break;
                                }
                            }

                            mainGeneralUtil.refreshBackgrounds();
                        }
                    }
                }
            }
        });
        deleteMusicBackground.setBackground(CyderColors.regularRed);
        deleteMusicBackground.setFont(CyderFonts.weatherFontSmall);
        deleteMusicBackground.setBounds(20 + 213 + 20 + 213 + 20,440,213,40);
        switchingPanel.add(deleteMusicBackground);

        switchingPanel.revalidate();
    }

    private void switchToFontAndColor() {
        JLabel TitleLabel = new JLabel("Foreground & Font", SwingConstants.CENTER);
        TitleLabel.setFont(CyderFonts.weatherFontBig);
        TitleLabel.setBounds(720 / 2 - 375 / 2,10,375,40);
        switchingPanel.add(TitleLabel);

        int colorOffsetX = 340;
        int colorOffsetY = 100;

        JLabel ColorLabel = new JLabel("Text Color");
        ColorLabel.setFont(CyderFonts.weatherFontBig);
        ColorLabel.setForeground(CyderColors.navy);
        ColorLabel.setBounds(120 + colorOffsetX, 50 + colorOffsetY,300, 30);
        switchingPanel.add(ColorLabel);

        JLabel hexLabel = new JLabel("HEX:");
        hexLabel.setFont(CyderFonts.weatherFontSmall);
        hexLabel.setForeground(CyderColors.navy);
        hexLabel.setBounds(30 + colorOffsetX, 110 + colorOffsetY,70, 30);
        switchingPanel.add(hexLabel);

        JLabel rgbLabel = new JLabel("RGB:");
        rgbLabel.setFont(CyderFonts.weatherFontSmall);
        rgbLabel.setForeground(CyderColors.navy);
        rgbLabel.setBounds(30 + colorOffsetX, 180 + colorOffsetY,70,30);
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
        hexField.setBackground(new Color(0,0,0,0));
        hexField.setSelectionColor(CyderColors.selectionColor);
        hexField.setToolTipText("Hex Value");
        hexField.setBorder(new LineBorder(CyderColors.navy,5,false));
        JTextField finalHexField1 = hexField;
        JTextField finalRgbField = rgbField;
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hextorgbColor(finalHexField1.getText());
                    finalRgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
                    colorBlock.setBackground(c);
                }

                catch (Exception ignored) {}
            }
        });
        hexField.setBounds(100 + colorOffsetX, 100 + colorOffsetY,220, 50);
        hexField.setOpaque(false);
        switchingPanel.add(hexField);

        rgbField.setForeground(CyderColors.navy);
        rgbField.setFont(CyderFonts.weatherFontBig);
        rgbField.setBackground(new Color(0,0,0,0));
        rgbField.setSelectionColor(CyderColors.selectionColor);
        rgbField.setToolTipText("RGB Value");
        Color c = ColorUtil.hextorgbColor(IOUtil.getUserData("Foreground"));
        rgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
        rgbField.setBorder(new LineBorder(CyderColors.navy,5,false));
        JTextField finalRgbField1 = rgbField;
        rgbField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    String[] parts = finalRgbField1.getText().split(",");
                    Color c = new Color(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                    hexField.setText(ColorUtil.rgbtohexString(c));
                    colorBlock.setBackground(c);
                }

                catch (Exception ignored) {}
            }
        });
        rgbField.setBounds(100 + colorOffsetX, 170 + colorOffsetY,220, 50);
        rgbField.setOpaque(false);
        switchingPanel.add(rgbField);

        CyderButton applyColor = new CyderButton("Apply Color");
        applyColor.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        applyColor.setColors(CyderColors.regularRed);
        applyColor.setToolTipText("Apply");
        applyColor.setFont(CyderFonts.weatherFontSmall);
        applyColor.setFocusPainted(false);
        applyColor.setBackground(CyderColors.regularRed);
        applyColor.addActionListener(e -> {
            IOUtil.writeUserData("Foreground",hexField.getText());

            Color updateC = ColorUtil.hextorgbColor(hexField.getText());

            outputArea.setForeground(updateC);
            inputField.setForeground(updateC);

            println("The Color [" + updateC.getRed() + "," + updateC.getGreen() + "," + updateC.getBlue() + "] has been applied.");
        });
        applyColor.setBounds(460,420,200,40);
        switchingPanel.add(applyColor);

        JLabel FontLabel = new JLabel("Fonts");
        FontLabel.setFont(CyderFonts.weatherFontBig);
        FontLabel.setForeground(CyderColors.navy);
        FontLabel.setBounds(150, 60,300, 30);
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
        FontListScroll.setBorder(new LineBorder(CyderColors.navy,5,true));

        CyderButton applyFont = new CyderButton("Apply Font");
        applyFont.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
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
                IOUtil.writeUserData("Font",FontS);
                println("The font \"" + FontS + "\" has been applied.");
            }
        });
        applyFont.setBounds(100,420,200,40);
        switchingPanel.add(applyFont);

        fontList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyFont.doClick();
            }

            else {
                try {
                    FontLabel.setFont(new Font(fontList.getSelectedValue().toString(), Font.BOLD, 30));
                }

                catch (Exception ex) {
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

        FontListScroll.setBounds(50,100,300,300);
        switchingPanel.add(FontListScroll, Component.CENTER_ALIGNMENT);

        switchingPanel.revalidate();
    }

    ImageIcon selected = new ImageIcon("src/com/cyder/sys/pictures/checkbox1.png");
    ImageIcon notSelected = new ImageIcon("src/com/cyder/sys/pictures/checkbox2.png");

    private void switchToPreferences() {
        JLabel prefsTitle = new JLabel("Preferences");
        prefsTitle.setFont(CyderFonts.weatherFontBig);
        prefsTitle.setForeground(CyderColors.navy);
        prefsTitle.setHorizontalAlignment(JLabel.CENTER);
        prefsTitle.setBounds(720 / 2 - 250 / 2,10,250,30);
        switchingPanel.add(prefsTitle);

        JLabel introMusicTitle = new JLabel("Intro Music");
        introMusicTitle.setFont(CyderFonts.weatherFontSmall);
        introMusicTitle.setForeground(CyderColors.navy);
        introMusicTitle.setHorizontalAlignment(JLabel.CENTER);
        introMusicTitle.setBounds(20,50,130,25);
        switchingPanel.add(introMusicTitle);

        JLabel debugWindowsLabel = new JLabel("Debug");
        debugWindowsLabel.setFont(CyderFonts.weatherFontSmall);
        debugWindowsLabel.setForeground(CyderColors.navy);
        debugWindowsLabel.setHorizontalAlignment(JLabel.CENTER);
        debugWindowsLabel.setBounds(130,50,160,25);
        switchingPanel.add(debugWindowsLabel);

        JLabel randomBackgroundLabel = new JLabel("Random Back");
        randomBackgroundLabel.setFont(CyderFonts.weatherFontSmall);
        randomBackgroundLabel.setForeground(CyderColors.navy);
        randomBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        randomBackgroundLabel.setBounds(150 + 120,50,160,25);
        switchingPanel.add(randomBackgroundLabel);

        JLabel outputBorderLabel = new JLabel("Out Border");
        outputBorderLabel.setFont(CyderFonts.weatherFontSmall);
        outputBorderLabel.setForeground(CyderColors.navy);
        outputBorderLabel.setHorizontalAlignment(JLabel.CENTER);
        outputBorderLabel.setBounds(150 + 20 + 10 + 150 + 90,50,160,25);
        switchingPanel.add(outputBorderLabel);

        JLabel inputBorderLabel = new JLabel("In Border");
        inputBorderLabel.setFont(CyderFonts.weatherFontSmall);
        inputBorderLabel.setForeground(CyderColors.navy);
        inputBorderLabel.setHorizontalAlignment(JLabel.CENTER);
        inputBorderLabel.setBounds(150 + 20 + 20 + 150 + 225,50,160,25);
        switchingPanel.add(inputBorderLabel);

        CheckBox introMusic = new CheckBox();
        introMusic.setToolTipText("Play intro music on start");

        if (IOUtil.getUserData("IntroMusic").equals("1"))
            introMusic.setSelected();

        introMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("IntroMusic").equals("1");
                IOUtil.writeUserData("IntroMusic", (wasSelected ? "0" : "1"));
            introMusic.setIcon((wasSelected ? notSelected : selected));
            }
        });
        introMusic.setBounds(20, 80,100,100);
        switchingPanel.add(introMusic);

        JLabel debugWindows = new JLabel();
        debugWindows.setToolTipText("Show debug windows on start");
        debugWindows.setHorizontalAlignment(JLabel.CENTER);
        debugWindows.setSize(100,100);
        debugWindows.setIcon((IOUtil.getUserData("DebugWindows").equals("1") ? selected : notSelected));
        debugWindows.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("DebugWindows").equals("1");
                IOUtil.writeUserData("DebugWindows", (wasSelected ? "0" : "1"));
            debugWindows.setIcon((wasSelected ? notSelected : selected));
            }
        });
        debugWindows.setBounds(20 + 45 + 100, 80,100,100);
        switchingPanel.add(debugWindows);

        JLabel randBackgroundLabel = new JLabel();
        randBackgroundLabel.setToolTipText("Choose a random background on start");
        randBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        randBackgroundLabel.setSize(100,100);
        randBackgroundLabel.setIcon((IOUtil.getUserData("RandomBackground").equals("1") ? selected : notSelected));
        randBackgroundLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("RandomBackground").equals("1");
                IOUtil.writeUserData("RandomBackground", (wasSelected ? "0" : "1"));
            randBackgroundLabel.setIcon((wasSelected ? notSelected : selected));
            }
        });
        randBackgroundLabel.setBounds(20 + 2 * 45 + 2 * 100, 80,100,100);
        switchingPanel.add(randBackgroundLabel);

        JLabel outputBorder = new JLabel();
        outputBorder.setToolTipText("Draw a border around the output area");
        outputBorder.setHorizontalAlignment(JLabel.CENTER);
        outputBorder.setSize(100,100);
        outputBorder.setIcon((IOUtil.getUserData("OutputBorder").equals("1") ? selected : notSelected));
        outputBorder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("OutputBorder").equals("1");
                IOUtil.writeUserData("OutputBorder", (wasSelected ? "0" : "1"));
            outputBorder.setIcon((wasSelected ? notSelected : selected));
            if (wasSelected) {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            else {
                outputScroll.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")),3,true));
            }

            consoleFrame.revalidate();
            }
        });
        outputBorder.setBounds(20 + 3 * 45 + 3 * 100, 80,100,100);
        switchingPanel.add(outputBorder);

        JLabel inputBorder = new JLabel();
        inputBorder.setToolTipText("Draw a border around the input field");
        inputBorder.setHorizontalAlignment(JLabel.CENTER);
        inputBorder.setSize(100,100);
        inputBorder.setIcon((IOUtil.getUserData("InputBorder").equals("1") ? selected : notSelected));
        inputBorder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("InputBorder").equals("1");
                IOUtil.writeUserData("InputBorder", (wasSelected ? "0" : "1"));
            inputBorder.setIcon((wasSelected ? notSelected : selected));

            if (wasSelected) {
                inputField.setBorder(BorderFactory.createEmptyBorder());
            }

            else {
                inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor(IOUtil.getUserData("Background")),3,true));
            }

            consoleFrame.revalidate();
            }
        });
        inputBorder.setBounds(20 + 4 * 45 + 4 * 100, 80,100,100);
        switchingPanel.add(inputBorder);

        JLabel hourlyChimesLabel = new JLabel("Hour Chimes");
        hourlyChimesLabel.setFont(CyderFonts.weatherFontSmall);
        hourlyChimesLabel.setForeground(CyderColors.navy);
        hourlyChimesLabel.setHorizontalAlignment(JLabel.CENTER);
        hourlyChimesLabel.setBounds(5,210,170,30);
        switchingPanel.add(hourlyChimesLabel);

        JLabel silenceLabel = new JLabel("No Errors");
        silenceLabel.setFont(CyderFonts.weatherFontSmall);
        silenceLabel.setForeground(CyderColors.navy);
        silenceLabel.setHorizontalAlignment(JLabel.CENTER);
        silenceLabel.setBounds(150,210,150,30);
        switchingPanel.add(silenceLabel);

        JLabel fullscreenLabel = new JLabel("Fullscreen");
        fullscreenLabel.setFont(CyderFonts.weatherFontSmall);
        fullscreenLabel.setForeground(CyderColors.navy);
        fullscreenLabel.setHorizontalAlignment(JLabel.CENTER);
        fullscreenLabel.setBounds(285,210,170,30);
        switchingPanel.add(fullscreenLabel);

        JLabel outputFillLabel = new JLabel("Fill Out");
        outputFillLabel.setFont(CyderFonts.weatherFontSmall);
        outputFillLabel.setForeground(CyderColors.navy);
        outputFillLabel.setHorizontalAlignment(JLabel.CENTER);
        outputFillLabel.setBounds(420,210,170,30);
        switchingPanel.add(outputFillLabel);

        JLabel inputFillLabel = new JLabel("Fill In");
        inputFillLabel.setFont(CyderFonts.weatherFontSmall);
        inputFillLabel.setForeground(CyderColors.navy);
        inputFillLabel.setHorizontalAlignment(JLabel.CENTER);
        inputFillLabel.setBounds(560,210,170,30);
        switchingPanel.add(inputFillLabel);

        JLabel hourlyChimes = new JLabel();
        hourlyChimes.setToolTipText("Chime every hour");
        hourlyChimes.setHorizontalAlignment(JLabel.CENTER);
        hourlyChimes.setSize(100,100);
        hourlyChimes.setIcon((IOUtil.getUserData("HourlyChimes").equals("1") ? selected : notSelected));
        hourlyChimes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("HourlyChimes").equals("1");
                IOUtil.writeUserData("HourlyChimes", (wasSelected ? "0" : "1"));
            hourlyChimes.setIcon((wasSelected ? notSelected : selected));
            }
        });
        hourlyChimes.setBounds(20, 235,100,100);
        switchingPanel.add(hourlyChimes);

        JLabel silenceErrors = new JLabel();
        silenceErrors.setToolTipText("Hide errors that occur");
        silenceErrors.setHorizontalAlignment(JLabel.CENTER);
        silenceErrors.setSize(100,100);
        silenceErrors.setIcon((IOUtil.getUserData("SilenceErrors").equals("1") ? selected : notSelected));
        silenceErrors.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("SilenceErrors").equals("1");
                IOUtil.writeUserData("SilenceErrors", (wasSelected ? "0" : "1"));
            silenceErrors.setIcon((wasSelected ? notSelected : selected));
            }

        });
        silenceErrors.setBounds(20 + 100 + 45, 235,100,100);
        switchingPanel.add(silenceErrors);

        JLabel fullscreen = new JLabel();
        fullscreen.setToolTipText("Toggle between fullscreen (Extremely Experimental)");
        fullscreen.setHorizontalAlignment(JLabel.CENTER);
        fullscreen.setSize(100,100);
        fullscreen.setIcon((IOUtil.getUserData("FullScreen").equals("1") ? selected : notSelected));
        fullscreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("FullScreen").equals("1");
                IOUtil.writeUserData("FullScreen", (wasSelected ? "0" : "1"));
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
        outputFill.setToolTipText("Fill the output area with your custom color");
        outputFill.setHorizontalAlignment(JLabel.CENTER);
        outputFill.setSize(100,100);
        outputFill.setIcon((IOUtil.getUserData("OutputFill").equals("1") ? selected : notSelected));
        outputFill.setBounds(20 + 3 * 100 + 3 * 45, 235,100,100);
        switchingPanel.add(outputFill);

        JLabel inputFill = new JLabel();
        inputFill.setToolTipText("Fill the input field with your custom color");
        inputFill.setHorizontalAlignment(JLabel.CENTER);
        inputFill.setSize(100,100);
        inputFill.setIcon((IOUtil.getUserData("InputFill").equals("1") ? selected : notSelected));
        inputFill.setBounds(20 + 4 * 100 + 4 * 45, 235,100,100);
        switchingPanel.add(inputFill);

        JLabel clockLabel = new JLabel("Console Clock");
        clockLabel.setFont(CyderFonts.weatherFontSmall);
        clockLabel.setForeground(CyderColors.navy);
        clockLabel.setHorizontalAlignment(JLabel.CENTER);
        clockLabel.setBounds(20,380,170,25);
        switchingPanel.add(clockLabel);

        JLabel showSecondsLabel = new JLabel("Clock Seconds");
        showSecondsLabel.setFont(CyderFonts.weatherFontSmall);
        showSecondsLabel.setForeground(CyderColors.navy);
        showSecondsLabel.setHorizontalAlignment(JLabel.CENTER);
        showSecondsLabel.setBounds(220,380,170,25);
        switchingPanel.add(showSecondsLabel);

        JLabel clockOnConsole = new JLabel();
        clockOnConsole.setToolTipText("Show clock at top of main window");
        clockOnConsole.setHorizontalAlignment(JLabel.CENTER);
        clockOnConsole.setSize(100,100);
        clockOnConsole.setIcon((IOUtil.getUserData("ClockOnConsole").equals("1") ? selected : notSelected));
        clockOnConsole.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("ClockOnConsole").equals("1");
                IOUtil.writeUserData("ClockOnConsole", (wasSelected ? "0" : "1"));
            clockOnConsole.setIcon((wasSelected ? notSelected : selected));
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
        clockOnConsole.setBounds(50,400,100,100);
        switchingPanel.add(clockOnConsole);

        JLabel showSeconds = new JLabel();
        showSeconds.setToolTipText("Show seconds on console clock");
        showSeconds.setHorizontalAlignment(JLabel.CENTER);
        showSeconds.setSize(100,100);
        showSeconds.setIcon((IOUtil.getUserData("ShowSeconds").equals("1") ? selected : notSelected));
        showSeconds.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = IOUtil.getUserData("ShowSeconds").equals("1");
                IOUtil.writeUserData("ShowSeconds", (wasSelected ? "0" : "1"));
            showSeconds.setIcon((wasSelected ? notSelected : selected));

            if (wasSelected)
                consoleClockLabel.setText(TimeUtil.consoleTime());
            else
                consoleClockLabel.setText(TimeUtil.consoleSecondTime());
            }
        });
        showSeconds.setBounds(50 + 200,400,100,100);
        switchingPanel.add(showSeconds);

        outputFill.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                boolean wasSelected = IOUtil.getUserData("OutputFill").equals("1");
                IOUtil.writeUserData("OutputFill", (wasSelected ? "0" : "1"));
                outputFill.setIcon((wasSelected ? notSelected : selected));

                if (wasSelected) {
                    outputArea.setBackground(null);
                    outputArea.setOpaque(false);
                    consoleFrame.revalidate();
                }

                else {
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
                inputFill.setIcon((wasSelected ? notSelected : selected));

                if (wasSelected) {
                    inputField.setBackground(null);
                    inputField.setOpaque(false);
                    consoleFrame.revalidate();
                }

                else {
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
        hexLabel.setBounds(434, 380,150, 30);
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

        JTextField hexField = new JTextField(String.format("#%02X%02X%02X", CyderColors.navy.getRed(), CyderColors.navy.getGreen(), CyderColors.navy.getBlue()).replace("#",""));
        hexField.setForeground(CyderColors.navy);
        hexField.setText(IOUtil.getUserData("Background"));
        hexField.setFont(CyderFonts.weatherFontSmall);
        hexField.setBackground(new Color(255,255,255));
        hexField.setSelectionColor(CyderColors.selectionColor);
        hexField.setToolTipText("Input field and output area fill color if enabled");
        hexField.setBorder(new LineBorder(CyderColors.navy,5,false));
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    colorBlock.setBackground(ColorUtil.hextorgbColor(hexField.getText()));
                    IOUtil.writeUserData("Background",hexField.getText());
                }

                catch (Exception ignored) {}
            }
        });
        hexField.setBounds(460, 420,100, 40);
        switchingPanel.add(hexField);

        switchingPanel.revalidate();
    }

    public void createUser() {
        createUserBackground = null;

        if (createUserFrame != null)
            createUserFrame.closeAnimation();

        createUserFrame = new CyderFrame(356,473,new ImageIcon("src/com/cyder/sys/pictures/DebugBackground.png"));
        createUserFrame.setTitle("Create User");

        JLabel NameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        NameLabel.setFont(CyderFonts.weatherFontSmall);
        NameLabel.setBounds(120,30,121,30);
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

        newUserName.setBorder(new LineBorder(CyderColors.navy,5,false));
        newUserName.setBounds(60,70,240,40);
        createUserFrame.getContentPane().add(newUserName);

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(CyderFonts.weatherFontSmall);
        passwordLabel.setForeground(CyderColors.navy);
        passwordLabel.setBounds(60,120,240,30);
        createUserFrame.getContentPane().add(passwordLabel);

        JLabel matchPasswords = new JLabel("Passwords match", SwingConstants.CENTER);

        newUserPassword = new JPasswordField(15);
        newUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("Passwords match");
                matchPasswords.setForeground(CyderColors.regularGreen);
            }

            else {
                matchPasswords.setText("Passwords do not match");
                matchPasswords.setForeground(CyderColors.regularRed);
            }
            }
        });
        newUserPassword.setFont(CyderFonts.weatherFontSmall);
        newUserPassword.setForeground(CyderColors.navy);
        newUserPassword.setBorder(new LineBorder(CyderColors.navy,5,false));
        newUserPassword.setSelectedTextColor(CyderColors.selectionColor);
        newUserPassword.setBounds(60,160,240,40);
        createUserFrame.getContentPane().add(newUserPassword);

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(CyderFonts.weatherFontSmall);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setBounds(60,210,240,30);
        createUserFrame.getContentPane().add(passwordLabelConf);

        newUserPasswordconf = new JPasswordField(15);
        newUserPasswordconf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("Passwords match");
                matchPasswords.setForeground(CyderColors.regularGreen);
            }

            else {
                matchPasswords.setText("Passwords do not match");
                matchPasswords.setForeground(CyderColors.regularRed);
            }
            }
        });

        newUserPasswordconf.setFont(CyderFonts.weatherFontSmall);
        newUserPasswordconf.setForeground(CyderColors.navy);
        newUserPasswordconf.setBorder(new LineBorder(CyderColors.navy,5,false));
        newUserPasswordconf.setSelectedTextColor(CyderColors.selectionColor);
        newUserPasswordconf.setBounds(60,250,240,40);
        createUserFrame.getContentPane().add(newUserPasswordconf);

        matchPasswords.setFont(CyderFonts.weatherFontSmall);
        matchPasswords.setForeground(CyderColors.regularGreen);
        matchPasswords.setBounds(32,300,300,30);
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
                    ErrorHandler.handle(ex);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chooseBackground.setToolTipText("Choose background");
            }
        });

        chooseBackground.setBorder(new LineBorder(CyderColors.navy,5,false));
        chooseBackground.setBounds(60,340,240,40);
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

                if (stringUtil.empytStr(newUserName.getText()) || pass == null || passconf == null
                        || uuid.equals("") || pass.equals("") || passconf.equals("") || uuid.length() == 0) {
                    createUserFrame.inform("Sorry, but one of the required fields was left blank.\nPlease try again.","", 400, 300);
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (alreadyExists) {
                    createUserFrame.inform("Sorry, but that username is already in use.\nPlease try a different one.", "", 400, 300);
                    newUserName.setText("");
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (!Arrays.equals(pass, passconf) && pass.length > 0) {
                    createUserFrame.inform("Sorry, but your passwords did not match. Please try again.", "",400, 300);
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (pass.length < 5) {
                    createUserFrame.inform("Sorry, but your password length should be greater than\n"
                            + "four characters for security reasons. Please add more characters.", "", 400, 300);

                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else {
                    if (createUserBackground == null) {
                        createUserFrame.inform("No background image was chosen so we're going to give you a sweet one ;)", "No background", 700, 230);
                        createUserBackground = new File("src/com/cyder/sys/pictures/bobby.png");
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

                    LinkedList<String> data = new LinkedList<>();
                    data.add("Name:" + newUserName.getText().trim());
                    data.add("Password:" + SecurityUtil.toHexString(SecurityUtil.getSHA(pass)));

                    data.add("CyderFonts:tahoma");
                    data.add("Foreground:FCFBE3");
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

                    createUserFrame.inform("The new user \"" + newUserName.getText().trim() + "\" has been created successfully.", "", 500, 300);

                    createUserFrame.closeAnimation();

                    if (!consoleFrame.isVisible() && loginFrame != null) {
                        loginFrame.closeAnimation();
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
                ErrorHandler.handle(ex);
            }
            }
        });

        createNewUser.setBorder(new LineBorder(CyderColors.navy,5,false));
        createNewUser.setFont(CyderFonts.weatherFontSmall);
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
                    ErrorHandler.handle(ex);
                }

                menuLabel.setVisible(false);
                menuButton.setIcon(new ImageIcon("src/com/cyder/sys/pictures/menuSide1.png"));
            });

            waitThread.start();
        }
    }

    private void killAllYoutube() {
        for (YoutubeThread ytt : youtubeThreads)
            ytt.kill();
    }

    private void askew() {
        consoleFrame.setBackground(CyderColors.navy);
        parentLabel.setIcon(new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().getAbsolutePath(),GeneralUtil.getConsoleDirection()),3)));
    }

    private void barrelRoll() {
        consoleFrame.setBackground(CyderColors.navy);
        mainGeneralUtil.getBackgrounds();

        ConsoleDirection originConsoleDIr = mainGeneralUtil.getConsoleDirection();
        BufferedImage master = ImageUtil.getRotatedImage(mainGeneralUtil.getCurrentBackground().getAbsolutePath(),GeneralUtil.getConsoleDirection());

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
                rotated = ImageUtil.rotateImageByDegrees(master, angle);
                parentLabel.setIcon(new ImageIcon(rotated));
            }
        });
        timer.start();
    }

    //exiting method, system.exit will call shutdown hook which wil then call shutdown();
    private void exit() {
        frameAni.closeAnimation(consoleFrame);
        killAllYoutube();
        stringUtil.killBletchy();
        System.exit(0);
    }

    private void shutdown() {
        try {
            IOUtil.readUserData();
            IOUtil.writeUserData("Fonts",outputArea.getFont().getName());
            IOUtil.writeUserData("Foreground",ColorUtil.rgbtohexString(outputArea.getForeground()));

            IOUtil.deleteTempDir();
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //todo remove once consoleframe extends cyderframe
    public void notify(String htmltext, int delay, ArrowDirection arrowDir, VanishDirection vanishDir, JLayeredPane parent, int width) {
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
        text.setFont(CyderFonts.weatherFontSmall);
        text.setForeground(CyderColors.navy);
        text.setBounds(14,10,w * 2,h);
        consoleNotification.add(text);
        consoleNotification.setBounds(parent.getWidth() / 2 - (w/2),30,w * 2,h * 2);
        parent.add(consoleNotification,1,0);
        parent.repaint();

        consoleNotification.vanish(vanishDir, parent, delay);
    }
}