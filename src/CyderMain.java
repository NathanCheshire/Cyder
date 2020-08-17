import com.cyder.exception.CyderException;
import com.cyder.exception.FatalException;
import com.cyder.games.Hangman;
import com.cyder.games.TicTacToe;
import com.cyder.ui.*;
import com.cyder.utilities.*;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

//todo adding backgrounds doens't work
//todo convert all swing dependencies to CyderFrames and absolute layout placement
//todo temperature converter redo with no jpanels since it's too big right now
//todo when setting title of frame, don't actually just put title in top left corner

public class CyderMain{
    //console vars
    private JTextPane outputArea;
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
    private JLayeredPane parentPanel;
    private JButton suggestionButton;
    private JButton menuButton;
    private JFrame loginFrame;
    private JTextField nameField;
    private JPasswordField pass;
    private JLabel newUserLabel;
    private JLabel menuLabel;

    //Objects for main use
    private Util mainUtil = new Util();
    private CyderAnimation animation = new CyderAnimation();
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

    //Edit user vars
    private JFrame editUserFrame;
    private CyderScrollPane backgroundListScroll;
    private CyderButton openBackground;
    private CyderButton addMusic;
    private CyderButton openMusic;
    private JList<?> backgroundSelectionList;
    private CyderScrollPane musicListScroll;
    private List<File> musicList;
    private List<String> musicNameList;
    private JList<?> musicSelectionList;
    private List<File> backgroundsList;
    private List<String> backgroundsNameList;
    private CyderButton changeUsername;
    private CyderButton changePassword;

    //font vars
    private JList fontList;

    //notification var
    private static Notification consoleNotification;

    //create user vars
    private JFrame createUserFrame;
    private JPasswordField newUserPasswordconf;
    private JPasswordField newUserPassword;
    private JTextField newUserName;
    private CyderButton createNewUser;
    private CyderButton chooseBackground;
    private File createUserBackground;

    //pixealte file
    private File pixelateFile;

    //Linked List of youtube scripts
    private LinkedList<youtubeThread> youtubeThreads = new LinkedList<>();

    //sliding background var
    private boolean slidLeft;

    private specialDay specialDayNotifier;

    //call constructor
    public static void main(String[] ignored) {
        new CyderMain();
    }

    private CyderMain() {
        //Fix scaling issue for high DPI displays like nathanLenovo which is 2560x1440
        System.setProperty("sun.java2d.uiScale","1.0");

        UIManager.put("ToolTip.background", mainUtil.consoleColor);
        UIManager.put("ToolTip.border", mainUtil.tooltipBorderColor);
        UIManager.put("ToolTip.font", mainUtil.tahoma);
        UIManager.put("ToolTip.foreground", mainUtil.tooltipForegroundColor);

        //security var for main developer's PC
        boolean nathanLenovo = mainUtil.compMACAddress(mainUtil.getMACAddress());

        if (!mainUtil.released() && !nathanLenovo)
            System.exit(0);

        if (nathanLenovo)
            mainUtil.setDebugMode(true);

        mainUtil.varInit();
        backgroundProcess();

        if (nathanLenovo && !mainUtil.released())
            recognize("Nathan", "13201320".toCharArray());

        else
            login(false);
    }

    private void console() {
        try{
            mainUtil.initBackgrounds();
            mainUtil.getScreenSize();
            mainUtil.resizeImages();
            mainUtil.getValidBackgroundPaths();
            mainUtil.initBackgrounds();
            mainUtil.getScreenSize();
            mainUtil.getBackgroundSize();

            consoleFrame = new JFrame();
            consoleFrame.setUndecorated(true);
            consoleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            if (mainUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                mainUtil.setBackgroundX((int) mainUtil.getScreenSize().getWidth());
                mainUtil.setBackgroundY((int) mainUtil.getScreenSize().getHeight());
            }

            consoleFrame.setBounds(0, 0, mainUtil.getBackgroundX(), mainUtil.getBackgroundY());
            consoleFrame.setTitle(mainUtil.getCyderVer() + " Cyder [" + mainUtil.getUsername() + "]");

            parentPanel = new JLayeredPane();
            parentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            consoleFrame.setContentPane(parentPanel);

            parentPanel.setLayout(null);

            parentLabel = new JLabel();

            if (mainUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                parentLabel.setIcon(new ImageIcon(mainUtil.resizeImage((int) mainUtil.getScreenSize().getWidth(), (int) mainUtil.getScreenSize().getHeight(), mainUtil.getCurrentBackground())));
                parentLabel.setBounds(0, 0, mainUtil.getBackgroundX(), mainUtil.getBackgroundY());
                mainUtil.setBackgroundX((int) mainUtil.getScreenSize().getWidth());
                mainUtil.setBackgroundY((int) mainUtil.getScreenSize().getHeight());
            }

            else {
                parentLabel.setIcon(new ImageIcon(mainUtil.getRotatedImage(mainUtil.getCurrentBackground().toString())));
                parentLabel.setBounds(0, 0, mainUtil.getBackgroundX(), mainUtil.getBackgroundY());
            }

            parentLabel.setBorder(new LineBorder(mainUtil.navy,8,false));
            parentLabel.setToolTipText(mainUtil.getCurrentBackground().getName().replace(".png", ""));

            parentPanel.add(parentLabel,1,0);

            consoleFrame.setIconImage(mainUtil.getCyderIcon().getImage());

            outputArea = new JTextPane() {
                @Override
                public void setBorder(Border border) {
                    //no border
                }
            };
            outputArea.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                    inputField.requestFocus();
                }

                @Override
                public void focusLost(FocusEvent e) {

                }
            });

            outputArea.setEditable(false);
            outputArea.setAutoscrolls(true);
            outputArea.setBounds(10, 62, mainUtil.getBackgroundX() - 20, mainUtil.getBackgroundY() - 204);
            outputArea.setFocusable(true);
            outputArea.setSelectionColor(new Color(204,153,0));
            outputArea.setOpaque(false);
            outputArea.setBackground(new Color(0,0,0,0));

            outputScroll = new CyderScrollPane(outputArea,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            outputScroll.setThumbColor(mainUtil.intellijPink);
            outputScroll.getViewport().setBorder(null);
            outputScroll.getViewport().setOpaque(false);
            outputScroll.setOpaque(false);

            if (mainUtil.getUserData("OutputBorder").equalsIgnoreCase("1")) {
                outputScroll.setBorder(new LineBorder(mainUtil.vanila,3,true));
            }

            else {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            outputScroll.setBounds(10, 62, mainUtil.getBackgroundX() - 20, mainUtil.getBackgroundY() - 204);

            parentLabel.add(outputScroll);

            inputField = new JTextField(40);

            if (mainUtil.getUserData("InputBorder").equalsIgnoreCase("1")) {
                inputField.setBorder(new LineBorder(mainUtil.vanila,3,true));
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
                        mainUtil.setConsoleDirection(mainUtil.CYDER_DOWN);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_RIGHT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainUtil.setConsoleDirection(mainUtil.CYDER_RIGHT);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_UP) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainUtil.setConsoleDirection(mainUtil.CYDER_UP);
                        exitFullscreen();
                    }

                    if ((e.getKeyCode() == KeyEvent.VK_LEFT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        mainUtil.setConsoleDirection(mainUtil.CYDER_LEFT);
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
            inputField.setSelectionColor(mainUtil.selectionColor);
            inputField.addKeyListener(commandScrolling);

            consoleFrame.addWindowListener(consoleEcho);

            inputField.setBounds(10, 82 + outputArea.getHeight(),
                    mainUtil.getBackgroundX() - 20, mainUtil.getBackgroundY() - (outputArea.getHeight() + 62 + 40));
            inputField.setOpaque(false);

            parentLabel.add(inputField);

            inputField.addActionListener(inputFieldAction);
            inputField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    minimizeMenu();
                }

                @Override
                public void focusLost(FocusEvent e) {

                }
            });

            inputField.setCaretColor(mainUtil.vanila);

            initUserfontAndColor();

            suggestionButton = new JButton("");
            suggestionButton.setToolTipText("Suggestions");
            suggestionButton.addActionListener(e -> {
                println("What feature would you like to suggestion? (Please include as much detail as possible such as what" +
                        "key words you should type and how it should be responded to and any options you think might be necessary)");
                mainUtil.setUserInputDesc("suggestion");
                mainUtil.setUserInputMode(true);
                inputField.requestFocus();
            });

            suggestionButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\suggestion2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    suggestionButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\suggestion1.png"));
                }
            });

            suggestionButton.setBounds(32, 4, 22, 22);

            ImageIcon DebugIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\suggestion1.png");

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

            menuButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!menuLabel.isVisible()) {
                        menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menu2.png"));

                        menuLabel = new JLabel("");
                        menuLabel.setOpaque(true);
                        menuLabel.setBackground(new Color(26,32,51));

                        parentPanel.add(menuLabel,1,0);

                        menuLabel.setBounds(-150,30, 130,260);
                        menuLabel.setVisible(true);

                        JLabel calculatorLabel = new JLabel("Calculator");
                        calculatorLabel.setFont(mainUtil.weatherFontSmall);
                        calculatorLabel.setForeground(mainUtil.vanila);
                        calculatorLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                Calculator c = new Calculator();
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                calculatorLabel.setForeground(mainUtil.regularRed);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                calculatorLabel.setForeground(mainUtil.vanila);
                            }
                        });

                        menuLabel.add(calculatorLabel);
                        calculatorLabel.setBounds(5,20,150,20);

                        JLabel musicLabel = new JLabel("Music");
                        musicLabel.setFont(mainUtil.weatherFontSmall);
                        musicLabel.setForeground(mainUtil.vanila);
                        musicLabel.setBounds(5,50,150,20);
                        menuLabel.add(musicLabel);
                        musicLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                mainUtil.mp3("", mainUtil.getUsername(), mainUtil.getUserUUID());
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                musicLabel.setForeground(mainUtil.regularRed);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                musicLabel.setForeground(mainUtil.vanila);
                            }
                        });

                        JLabel weatherLabel = new JLabel("Weather");
                        weatherLabel.setFont(mainUtil.weatherFontSmall);
                        weatherLabel.setForeground(mainUtil.vanila);
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
                                weatherLabel.setForeground(mainUtil.regularRed);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                weatherLabel.setForeground(mainUtil.vanila);
                            }
                        });

                        JLabel noteLabel = new JLabel("Notes");
                        noteLabel.setFont(mainUtil.weatherFontSmall);
                        noteLabel.setForeground(mainUtil.vanila);
                        menuLabel.add(noteLabel);
                        noteLabel.setBounds(5,110,150,20);
                        noteLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                userNotes = new Notes(mainUtil.getUserUUID());
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                noteLabel.setForeground(mainUtil.regularRed);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                noteLabel.setForeground(mainUtil.vanila);
                            }
                        });

                        JLabel editUserLabel = new JLabel("Edit user");
                        editUserLabel.setFont(mainUtil.weatherFontSmall);
                        editUserLabel.setForeground(mainUtil.vanila);
                        menuLabel.add(editUserLabel);
                        editUserLabel.setBounds(5,140,150,20);
                        editUserLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                editUser();
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                editUserLabel.setForeground(mainUtil.regularRed);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                editUserLabel.setForeground(mainUtil.vanila);
                            }
                        });

                        JLabel temperatureLabel = new JLabel("Temp conv");
                        temperatureLabel.setFont(mainUtil.weatherFontSmall);
                        temperatureLabel.setForeground(mainUtil.vanila);
                        menuLabel.add(temperatureLabel);
                        temperatureLabel.setBounds(5,170,150,20);
                        temperatureLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                TempConverter tc = new TempConverter();
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                temperatureLabel.setForeground(mainUtil.regularRed);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                temperatureLabel.setForeground(mainUtil.vanila);
                            }
                        });

                        JLabel youtubeLabel = new JLabel("YouTube");
                        youtubeLabel.setFont(mainUtil.weatherFontSmall);
                        youtubeLabel.setForeground(mainUtil.vanila);
                        menuLabel.add(youtubeLabel);
                        youtubeLabel.setBounds(5,200,150,20);
                        youtubeLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                mainUtil.internetConnect("https://youtube.com");
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                youtubeLabel.setForeground(mainUtil.regularRed);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                youtubeLabel.setForeground(mainUtil.vanila);
                            }
                        });

                        JLabel twitterLabel = new JLabel("Twitter");
                        twitterLabel.setFont(mainUtil.weatherFontSmall);
                        twitterLabel.setForeground(mainUtil.vanila);
                        menuLabel.add(twitterLabel);
                        twitterLabel.setBounds(5,230,150,20);
                        twitterLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                mainUtil.internetConnect("https://twitter.com");
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                twitterLabel.setForeground(mainUtil.regularRed);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                twitterLabel.setForeground(mainUtil.vanila);
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
                        menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menu2.png"));
                    }

                    else {
                        menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menuSide2.png"));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (menuLabel.isVisible()) {
                        menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menu1.png"));
                    }

                    else {
                        menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menuSide1.png"));
                    }
                }
            });

            menuButton.setBounds(4, 4, 22, 22);

            ImageIcon MenuIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\menuSide1.png");

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
                mainUtil.minimizeAnimation(consoleFrame);
                updateConsoleClock = false;
                consoleFrame.setState(Frame.ICONIFIED);
                minimizeMenu();
            });

            minimize.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    minimize.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Minimize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    minimize.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Minimize1.png"));
                }
            });

            minimize.setBounds(mainUtil.getBackgroundX() - 81, 4, 22, 20);

            ImageIcon mini = new ImageIcon("src\\com\\cyder\\io\\pictures\\Minimize1.png");
            minimize.setIcon(mini);
            parentLabel.add(minimize);
            minimize.setFocusPainted(false);
            minimize.setOpaque(false);
            minimize.setContentAreaFilled(false);
            minimize.setBorderPainted(false);

            alternateBackground = new JButton("");
            alternateBackground.setToolTipText("Alternate Background");
            alternateBackground.addActionListener(e -> {
                mainUtil.initBackgrounds();

                if (mainUtil.ShouldISwitch() && mainUtil.getValidBackgroundPaths().length > 1) {
                    mainUtil.setCurrentBackgroundIndex(mainUtil.getCurrentBackgroundIndex() + 1);
                    switchBackground();
                }

                else if (mainUtil.OnLastBackground() && mainUtil.getValidBackgroundPaths().length > 1) {
                    mainUtil.setCurrentBackgroundIndex(0);
                    switchBackground();
                }

                else if (mainUtil.getValidBackgroundPaths().length == 1) {
                    println("You only have one background image. Would you like to add more? (Enter yes/no)");
                    inputField.requestFocus();
                    mainUtil.setUserInputMode(true);
                    mainUtil.setUserInputDesc("addbackgrounds");
                }

                else {
                    try {
                        throw new FatalException("Background does not exist");
                    }

                    catch (FatalException ex) {
                       mainUtil.handle(ex);
                    }
                }
            });

            alternateBackground.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\ChangeSize2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    alternateBackground.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\ChangeSize1.png"));
                }
            });

            alternateBackground.setBounds(mainUtil.getBackgroundX() - 54, 4, 22, 20);

            ImageIcon Size = new ImageIcon("src\\com\\cyder\\io\\pictures\\ChangeSize1.png");
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
                    close.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Close2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    close.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Close1.png"));
                }
            });

            close.setBounds(mainUtil.getBackgroundX() - 27, 4, 22, 20);

            ImageIcon exit = new ImageIcon("src\\com\\cyder\\io\\pictures\\Close1.png");

            close.setIcon(exit);

            parentLabel.add(close);

            close.setFocusPainted(false);
            close.setOpaque(false);
            close.setContentAreaFilled(false);
            close.setBorderPainted(false);

            consoleDragLabel = new JLabel();
            consoleDragLabel.setBounds(0,0, mainUtil.getBackgroundX(),30);
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

            consoleDragLabel.setFont(mainUtil.weatherFontSmall);
            consoleDragLabel.setForeground(mainUtil.vanila);

            boolean showClock = mainUtil.getUserData("ClockOnConsole").equalsIgnoreCase("1");

            consoleClockLabel = new JLabel(mainUtil.consoleTime());
            consoleClockLabel.setFont(mainUtil.weatherFontSmall.deriveFont(20f));
            consoleClockLabel.setForeground(mainUtil.vanila);
            consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                    2,(consoleClockLabel.getText().length() * 17), 25);

            consoleDragLabel.add(consoleClockLabel, SwingConstants.CENTER);

            updateConsoleClock = showClock;

            refreshConsoleClock();

            consoleClockLabel.setVisible(showClock);

            if (mainUtil.getUserData("HourlyChimes").equalsIgnoreCase("1"))
                checkChime();

            parentLabel.add(consoleDragLabel);

            consoleFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeiconified(WindowEvent e) {
                    updateConsoleClock = true;
                    consoleFrame.setLocation(restoreX, restoreY);
                }
            });

            consoleFrame.setVisible(true);
            consoleFrame.setLocationRelativeTo(null);

            if (mainUtil.getUserData("RandomBackground").equals("1")) {
                int len = mainUtil.getValidBackgroundPaths().length;

                if (len <= 1)
                    println("Sorry, " + mainUtil.getUsername() + ", but you only have one background file so there's no random element to be chosen.");

                else if (len > 1) {
                    try {
                        File[] backgrounds = mainUtil.getValidBackgroundPaths();

                        mainUtil.setCurrentBackgroundIndex(mainUtil.randInt(0, (backgrounds.length) - 1));

                        String newBackFile = mainUtil.getCurrentBackground().toString();

                        ImageIcon newBack;
                        int tempW = 0;
                        int tempH = 0;

                        if (mainUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                            newBack = new ImageIcon(mainUtil.resizeImage((int) mainUtil.getScreenSize().getWidth(),
                                    (int) mainUtil.getScreenSize().getHeight(), new File(newBackFile)));
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        }

                        else {
                            newBack = new ImageIcon(newBackFile);
                            tempW = newBack.getIconWidth();
                            tempH = newBack.getIconHeight();
                        }

                        mainUtil.getBackgroundSize();

                        parentLabel.setIcon(newBack);

                        consoleFrame.setBounds(0, 0, tempW, tempH);
                        parentPanel.setBounds(0, 0,  tempW, tempH);
                        parentLabel.setBounds(0, 0,  tempW, tempH);

                        outputArea.setBounds(0, 0, tempW - 20, tempH - 204);
                        outputScroll.setBounds(10, 62, tempW - 20, tempH - 204);
                        inputField.setBounds(10, 82 + outputArea.getHeight(), tempW - 20, tempH - (outputArea.getHeight() + 62 + 40));
                        consoleDragLabel.setBounds(0,0,tempW,30);
                        minimize.setBounds(tempW - 81, 4, 22, 20);
                        alternateBackground.setBounds(tempW - 54, 4, 22, 20);
                        close.setBounds(tempW - 27, 4, 22, 20);

                        inputField.requestFocus();

                        consoleFrame.setLocationRelativeTo(null);
                        parentLabel.setIcon(newBack);

                        parentLabel.setToolTipText(mainUtil.getCurrentBackground().getName().replace(".png", ""));
                        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                                2,(consoleClockLabel.getText().length() * 17), 25);
                    }

                    catch (Exception e) {
                        mainUtil.handle(e);
                    }
                }

                else
                   throw new FatalException("Only one but also more than one background.");
            }

            new Thread(() -> {
                if (!mainUtil.internetReachable())
                    notification(mainUtil.getUsername() + ", internet connection slow or unavailble.",
                            3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel);
            }).start();
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

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
                            mainUtil.setCurrentDowns(0);
                        }

                        else if (!Found) {
                            mainUtil.setCurrentDowns(0);
                        }

                        if (scrollingIndex - 1 >= 0) {
                            if (mainUtil.getCurrentDowns() != 0) {
                                scrollingIndex = scrollingIndex - 1;
                            }

                            inputField.setText(operationList.get(scrollingIndex));
                            mainUtil.setCurrentDowns(mainUtil.getCurrentDowns() + 1);
                        }

                        if (operationList.size() == 1) {
                            inputField.setText(operationList.get(0));
                        }
                    }

                    for (int i = 61440 ; i < 61452 ; i++) {
                        if (code == i) {
                            try {
                                throw new FatalException("Interesting F" + (i - 61427) + " key");
                            }

                            catch (FatalException ex) {
                                mainUtil.handle(ex);
                            }
                        }
                    }
                }
            }

            catch (Exception e) {
                mainUtil.handle(e);
            }
        }
    };

    private class specialDay {
        private boolean kill = false;

        public specialDay() {
            if (!kill) {
                if (mainUtil.isChristmas())
                    notification("Merry Christmas!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel);

                if (mainUtil.isHalloween())
                    notification("Happy Halloween!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel);

                if (mainUtil.isIndependenceDay())
                    notification("Happy 4th of July", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel);

                if (mainUtil.isThanksgiving())
                    notification("Happy Thanksgiving!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel);

                if (mainUtil.isAprilFoolsDay())
                    notification("Happy April Fools Day!", 3000, Notification.TOP_ARROW, Notification.TOP_VANISH,parentPanel);

                kill = true;
            }
        }
    }

    //make a special day echo class
    private WindowAdapter consoleEcho = new WindowAdapter() {
        public void windowOpened(WindowEvent e) {
        inputField.requestFocus();
        specialDayNotifier = new specialDay();
        }
    };

    private void backgroundProcess() {
        try {
            new Thread(() -> {
                try {
                    boolean toggle = false;

                    while (true) {
                        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
                        int noThreads = currentGroup.activeCount();

                        if (noThreads > 6) {
                            consoleFrame.setIconImage(mainUtil.getCyderIconBlink().getImage());

                            Thread.sleep(5000);
                        }

                        else {
                            if (consoleFrame != null) {
                                consoleFrame.setIconImage(mainUtil.getCyderIcon().getImage());
                                Thread.sleep(5000);
                            }
                        }
                    }
                }

                catch (Exception e) {
                    mainUtil.handle(e);
                }
            }).start();
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private Action inputFieldAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
        try {
            String originalOp = inputField.getText().trim();
            String op = originalOp;

            if (!mainUtil.empytStr(op)) {
                if (!(operationList.size() > 0 && operationList.get(operationList.size() - 1).equals(op))) {
                    operationList.add(op);
                }

                scrollingIndex = operationList.size() - 1;
                mainUtil.setCurrentDowns(0);

                if (!mainUtil.getUserInputMode()) {
                    handle(op);
                }

                else if (mainUtil.getUserInputMode()) {
                    mainUtil.setUserInputMode(false);
                    handleSecond(op);
                }
            }

            inputField.setText("");
        }

        catch (Exception ex) {
            mainUtil.handle(ex);
        }
        }
    };

    private void exit() {
        mainUtil.setUsername(mainUtil.getUsername());
        mainUtil.setUsercolor(mainUtil.getUsercolor());
        mainUtil.setUserfont(mainUtil.getUserfont());

        saveFontColor();
        mainUtil.closeAnimation(consoleFrame);

        System.exit(0);
    }

    private void login(boolean AlreadyOpen) {
        if (loginFrame != null) {
            mainUtil.closeAnimation(loginFrame);
        }

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
        loginFrame.setIconImage(mainUtil.getCyderIcon().getImage());

        loginLabel = new JLabel();
        loginLabel.setVerticalTextPosition(SwingConstants.TOP);
        loginLabel.setVerticalAlignment(SwingConstants.TOP);
        loginLabel.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\login.png"));
        loginLabel.setBounds(0, 0, 440, 520);
        loginLabel.setBorder(new LineBorder(mainUtil.navy,5,false));

        loginFrame.setContentPane(loginLabel);

        loginLabel2 = new JLabel();
        loginLabel2.setVerticalTextPosition(SwingConstants.TOP);
        loginLabel2.setVerticalAlignment(SwingConstants.TOP);
        loginLabel2.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Login2.png"));
        loginLabel2.setBounds(440,0 , 440, 520);

        loginLabel.add(loginLabel2);

        loginLabel3 = new JLabel();
        loginLabel3.setVerticalTextPosition(SwingConstants.TOP);
        loginLabel3.setVerticalAlignment(SwingConstants.TOP);
        loginLabel3.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Login3.png"));
        loginLabel3.setBounds(880,0 , 440, 520);

        loginLabel.add(loginLabel3);

        loginAnimation();

        DragLabel LoginDragLabel = new DragLabel(440,30,loginFrame);
        JLabel buildLabel = new JLabel("Build " + mainUtil.getCyderVer());
        buildLabel.setForeground(mainUtil.vanila);
        buildLabel.setFont(mainUtil.weatherFontSmall.deriveFont(20f));
        buildLabel.setBounds(LoginDragLabel.getWidth() / 2 - (buildLabel.getText().length() * 11)/2,
                2,(buildLabel.getText().length() * 17), 25);
        LoginDragLabel.add(buildLabel);
        loginLabel.add(LoginDragLabel);

        nameField = new JTextField(20);
        nameField.setToolTipText("Username");
        nameField.setBounds(64,279,327,41);
        nameField.setBackground(new Color(0,0,0,0));
        nameField.setSelectionColor(mainUtil.selectionColor);
        nameField.setBorder(null);
        nameField.setFont(mainUtil.weatherFontSmall.deriveFont(30f));
        nameField.setForeground(new Color(42,52,61));
        nameField.setCaretColor(mainUtil.navy);
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
        pass.setSelectionColor(mainUtil.selectionColor);
        pass.setBorder(null);
        pass.setFont(mainUtil.weatherFontBig.deriveFont(50f));
        pass.setForeground(new Color(42,52,61));
        pass.setCaretColor(mainUtil.navy);
        pass.addActionListener(e -> {
            String Username = nameField.getText().trim();

            if (!mainUtil.empytStr(Username)) {
                Username = Username.substring(0, 1).toUpperCase() + Username.substring(1);

                char[] Password = pass.getPassword();

                if (!mainUtil.empytStr(Username)) {
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
        newUserLabel.setForeground(mainUtil.vanila);
        newUserLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                createUser();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                newUserLabel.setText("Create an account!");
                newUserLabel.setForeground(mainUtil.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newUserLabel.setText("Don't have an account?");
                newUserLabel.setForeground(mainUtil.vanila);
            }
        });

        newUserLabel.setBounds(89,425,262,33);

        loginLabel.add(newUserLabel);

        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                nameField.requestFocus();
            }
        });

        File Users = new File("src\\com\\cyder\\users\\");
        String[] directories = Users.list((current, name) -> new File(current, name).isDirectory());

        if (directories != null && directories.length == 0)
            notification("Psssst! Create a user, " + System.getProperty("user.name"),
                    2000, Notification.RIGHT_ARROW, Notification.RIGHT_VANISH, parentPanel);
    }

    private void recognize(String Username, char[] Password) {
        try {
            mainUtil.setUsername(Username);

            if (mainUtil.checkPassword(Username, mainUtil.toHexString(mainUtil.getSHA(Password)))) {
                mainUtil.readUserData();
                mainUtil.closeAnimation(loginFrame);

                if (consoleFrame != null)
                    mainUtil.closeAnimation(consoleFrame);

                console();

                if (mainUtil.getUserData("IntroMusic").equals("1")) {
                    LinkedList<String> MusicList = new LinkedList<>();

                    File UserMusicDir = new File("src\\com\\cyder\\users\\" + mainUtil.getUserUUID() + "\\Music");

                    String[] FileNames = UserMusicDir.list();

                    if (FileNames != null)
                        for (String fileName : FileNames)
                            if (fileName.endsWith(".mp3"))
                                MusicList.add(fileName);

                    if (!MusicList.isEmpty())
                        mainUtil.playMusic(
                                "src\\com\\cyder\\users\\" + mainUtil.getUserUUID() + "\\Music\\" +
                                        (FileNames[mainUtil.randInt(0,FileNames.length - 1)]));
                }
            }

            else {
                nameField.setText("");
                pass.setText("");
                nameField.requestFocusInWindow();
            }
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void exitFullscreen() {
        mainUtil.initBackgrounds();
        File[] backgrounds = mainUtil.getValidBackgroundPaths();
        int index = mainUtil.getCurrentBackgroundIndex();
        String backFile = backgrounds[index].toString();

        int width = 0;
        int height = 0;

        if (mainUtil.getConsoleDirection() == mainUtil.CYDER_UP) {
            ImageIcon backIcon = new ImageIcon(backFile);
            width = backIcon.getIconWidth();
            height = backIcon.getIconHeight();
            parentLabel.setIcon(backIcon);
        }

        else if (mainUtil.getConsoleDirection() == mainUtil.CYDER_DOWN) {
            ImageIcon backIcon = new ImageIcon(backFile);
            width = backIcon.getIconWidth();
            height = backIcon.getIconHeight();
            parentLabel.setIcon(new ImageIcon(mainUtil.getRotatedImage(mainUtil.getCurrentBackground().toString())));
        }

        else {
            ImageIcon backIcon = new ImageIcon(backFile);

            if (mainUtil.getConsoleDirection() == mainUtil.CYDER_LEFT || mainUtil.getConsoleDirection() == mainUtil.CYDER_RIGHT) {
                height = backIcon.getIconWidth();
                width = backIcon.getIconHeight();
            }

            parentLabel.setIcon(new ImageIcon(mainUtil.getRotatedImage(mainUtil.getCurrentBackground().toString())));
        }

        mainUtil.getBackgroundSize();

        consoleFrame.setBounds(0, 0, width, height);
        parentPanel.setBounds(0, 0,  width, height);
        parentLabel.setBounds(0, 0,  width, height);

        outputArea.setBounds(0, 0, width - 20, height - 204);
        outputScroll.setBounds(10, 62, width - 20, height - 204);
        inputField.setBounds(10, 82 + outputArea.getHeight(), width - 20, height - (outputArea.getHeight() + 62 + 40));
        consoleDragLabel.setBounds(0,0,width,30);
        minimize.setBounds(width - 81, 4, 22, 20);
        alternateBackground.setBounds(width - 54, 4, 22, 20);
        close.setBounds(width - 27, 4, 22, 20);
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                2,(consoleClockLabel.getText().length() * 17), 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();

        consoleFrame.setLocationRelativeTo(null);
    }

    private void refreshFullscreen() {
        mainUtil.initBackgrounds();
        File[] backgrounds = mainUtil.getValidBackgroundPaths();
        int index = mainUtil.getCurrentBackgroundIndex();
        String backFile = backgrounds[index].toString();

        ImageIcon backIcon = new ImageIcon(backFile);

        BufferedImage fullimg = mainUtil.resizeImage((int) mainUtil.getScreenSize().getWidth(),
                (int) mainUtil.getScreenSize().getHeight(), new File(backFile));
        int fullW = fullimg.getWidth();
        int fullH = fullimg.getHeight();

        parentLabel.setIcon(new ImageIcon(fullimg));

        mainUtil.getBackgroundSize();

        consoleFrame.setBounds(0, 0, fullW, fullH);
        parentPanel.setBounds(0, 0,  fullW, fullH);
        parentLabel.setBounds(0, 0,  fullW, fullH);

        outputArea.setBounds(0, 0, fullW - 20, fullH - 204);
        outputScroll.setBounds(10, 62, fullW - 20, fullH - 204);
        inputField.setBounds(10, 82 + outputArea.getHeight(), fullW - 20, fullH - (outputArea.getHeight() + 62 + 40));
        consoleDragLabel.setBounds(0,0,fullW,30);
        minimize.setBounds(fullW - 81, 4, 22, 20);
        alternateBackground.setBounds(fullW - 54, 4, 22, 20);
        close.setBounds(fullW - 27, 4, 22, 20);
        consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                2,(consoleClockLabel.getText().length() * 17), 25);

        consoleFrame.repaint();
        consoleFrame.setVisible(true);
        consoleFrame.requestFocus();
        inputField.requestFocus();

        consoleFrame.setLocationRelativeTo(null);
    }

    private void switchBackground() {
        Thread slideThread = new Thread(() -> {
            try {
                mainUtil.initBackgrounds();

                File[] backgrounds = mainUtil.getValidBackgroundPaths();
                int oldIndex = (mainUtil.getCurrentBackgroundIndex() == 0 ? backgrounds.length - 1 : mainUtil.getCurrentBackgroundIndex() - 1);
                String oldBackFile = backgrounds[oldIndex].toString();
                String newBackFile = mainUtil.getCurrentBackground().toString();

                ImageIcon oldBack = new ImageIcon(oldBackFile);
                BufferedImage newBack = ImageIO.read(new File(newBackFile));

                BufferedImage temporaryImage;
                int tempW = 0;
                int tempH = 0;
                
                if (mainUtil.getUserData("FullScreen").equalsIgnoreCase("1")) {
                    oldBack = new ImageIcon(mainUtil.resizeImage((int) mainUtil.getScreenSize().getWidth(),
                            (int) mainUtil.getScreenSize().getHeight(),new File(oldBackFile)));
                    newBack = mainUtil.resizeImage((int) mainUtil.getScreenSize().getWidth(), (int) mainUtil.getScreenSize().getHeight(),
                            new File(newBackFile));
                    temporaryImage = mainUtil.resizeImage((int) mainUtil.getScreenSize().getWidth(), (int) mainUtil.getScreenSize().getHeight(),
                            new File(oldBackFile));
                    tempW = temporaryImage.getWidth();
                    tempH = temporaryImage.getHeight();
                }

                else {
                    newBack = mainUtil.resizeImage(newBack.getWidth(), newBack.getHeight(),new File(newBackFile));
                    temporaryImage = mainUtil.resizeImage(newBack.getWidth(), newBack.getHeight(), new File(oldBackFile));
                    tempW = temporaryImage.getWidth();
                    tempH = temporaryImage.getHeight();
                }

                mainUtil.getBackgroundSize();

                consoleFrame.setBounds(0, 0, tempW, tempH);
                parentPanel.setBounds(0, 0,  tempW, tempH);
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
                    parentPanel.add(temporaryLabel);
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
                    parentPanel.add(temporaryLabel);
                    parentLabel.setBounds(tempW, 0, tempW, tempH);
                    temporaryLabel.setBounds(0, 0 ,tempW, tempH);

                    int[] parts = getDelayIncrement(tempW);

                    animation.jLabelXLeft(0, -tempW, parts[0], parts[1], temporaryLabel);
                    animation.jLabelXLeft(tempW, 0 ,parts[0], parts[1], parentLabel);
                }

                slidLeft = !slidLeft;

                parentLabel.setToolTipText(mainUtil.getCurrentBackground().getName().replace(".png", ""));
                consoleClockLabel.setBounds(consoleDragLabel.getWidth() / 2 - (consoleClockLabel.getText().length() * 13)/2,
                        2,(consoleClockLabel.getText().length() * 17), 25);
            }

            catch (Exception e) {
                mainUtil.handle(e);
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
            mainUtil.handle(e);
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
                    mainUtil.handle(e);
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
            String desc = mainUtil.getUserInputDesc();

            if (desc.equalsIgnoreCase("url") && !mainUtil.empytStr(input)) {
                URI URI = new URI(input);
                println("Attempting to connect...");
                mainUtil.internetConnect(URI);
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
                mainUtil.internetConnect("https://www.google.com/search?q=" + input);
            }

            else if (desc.equalsIgnoreCase("youtube")&& input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                mainUtil.internetConnect("https://www.youtube.com/results?search_query=" + input);
            }

            else if (desc.equalsIgnoreCase("math") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ", "+");
                println("Attempting to connect...");
                mainUtil.internetConnect("https://www.wolframalpha.com/input/?i=" + input);
            }

            else if (desc.equalsIgnoreCase("binary")) {
                if (input.matches("[0-9]+") && !mainUtil.empytStr(input)) {
                    String Print = mainUtil.toBinary(Integer.parseInt(input));
                    println(input + " converted to binary equals: " + Print);
                }

                else {
                    println("Your value must only contain numbers.");
                }
            }

            else if (desc.equalsIgnoreCase("wiki") && input != null && !input.equals("")) {
                input = input.replace("'", "").replace(" ","_");
                println("Attempting to connect...");
                mainUtil.internetConnect("https://en.wikipedia.org/wiki/" + input);
            }

            else if (desc.equalsIgnoreCase("disco") && input != null && !input.equals("")) {
                println("I hope you're not the only one at this party.");
                mainUtil.disco(Integer.parseInt(input));
            }

            else if (desc.equalsIgnoreCase("youtube word search") && input != null && !input.equals("")) {
                String browse = "https://www.google.com/search?q=allinurl:REPLACE site:youtube.com";
                browse = browse.replace("REPLACE", input).replace(" ", "+");
                mainUtil.internetConnect(browse);
            }

            else if (desc.equalsIgnoreCase("random youtube")) {
                try {
                    int threads = Integer.parseInt(input);

                    if (threads > 1) {
                        notification("The scripts have started. At any point, type \"stop script\"",
                                4000, Notification.RIGHT_ARROW, Notification.RIGHT_VANISH,parentPanel);
                    }

                    else {
                        notification("The script has started. At any point, type \"stop script\"",
                                4000, Notification.TOP_ARROW, Notification.RIGHT_VANISH,parentPanel);
                    }

                    randomYoutube(consoleFrame, threads);
                }

                catch (Exception e) {
                    println("Invalid input for number of threads to start.");
                }
            }

            else if (desc.equalsIgnoreCase("anagram1")) {
                println("Enter your second word");
                anagram = input;
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
                mainUtil.setUserInputDesc("anagram2");
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
                mainUtil.pixelate(pixelateFile, Integer.parseInt(input));
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
                if (mainUtil.confirmation(input)) {
                    mainUtil.openFile("src\\com\\cyder\\io\\pictures\\" + mainUtil.getUserUUID() + "\\Backgrounds");
                    mainUtil.internetConnect("https://images.google.com/");
                }
            }

            else if (desc.equalsIgnoreCase("logoff")) {
                if (mainUtil.confirmation(input)) {
                    String shutdownCmd = "shutdown -l";
                    Runtime.getRuntime().exec(shutdownCmd);
                }
            }

            else if (desc.equalsIgnoreCase("deletebackground")) {
                List<?> ClickedSelectionListBackground = backgroundSelectionList.getSelectedValuesList();

                File ClickedSelectionPath = null;

                if (!ClickedSelectionListBackground.isEmpty() && !ClickedSelectionListBackground.get(0).toString().equalsIgnoreCase(mainUtil.getCurrentBackground().getName().replace(".png",""))) {
                    String ClickedSelection = ClickedSelectionListBackground.get(0).toString();

                    for (int i = 0; i < backgroundsNameList.size() ; i++) {
                        if (ClickedSelection.equals(backgroundsNameList.get(i))) {
                            ClickedSelectionPath = backgroundsList.get(i);
                            break;
                        }
                    }

                    if (ClickedSelectionPath != null) {
                        ClickedSelectionPath.delete();
                    }
                    initializeBackgroundsList();
                    backgroundListScroll.setViewportView(backgroundSelectionList);
                    backgroundListScroll.revalidate();

                    if (mainUtil.confirmation(input)) {
                        println("Background: " + ClickedSelectionPath.getName().replace(".png","") + " successfully deleted.");
                        mainUtil.initBackgrounds();
                    }

                    else {
                        println("Background: " + ClickedSelectionPath.getName().replace(".png","") + " was not deleted.");
                    }

                    File[] paths = mainUtil.getValidBackgroundPaths();
                    for (int i = 0 ; i < paths.length ; i++) {
                        if (paths[i].equals(mainUtil.getCurrentBackground())) {
                            mainUtil.setCurrentBackgroundIndex(i);
                            break;
                        }
                    }
                }

                else {
                    println("Can't delete your current background.");
                }
            }

            else if (desc.equalsIgnoreCase("deletemusic")) {
                List<?> ClickedSelectionListMusic = musicSelectionList.getSelectedValuesList();

                File ClickedSelectionPath = null;

                if (!ClickedSelectionListMusic.isEmpty()) {
                    String ClickedSelection = ClickedSelectionListMusic.get(0).toString();

                    for (int i = 0; i < musicNameList.size() ; i++) {
                        if (ClickedSelection.equals(musicNameList.get(i))) {
                            ClickedSelectionPath = musicList.get(i);

                            break;
                        }
                    }

                    ClickedSelectionPath.delete();
                    initializeMusicList();
                    musicListScroll.setViewportView(musicSelectionList);
                    musicListScroll.revalidate();

                    if (mainUtil.confirmation(input)) {
                        println("Music: " + ClickedSelectionPath.getName().replace(".png","") + " successfully deleted.");
                    }

                    else {
                        println("Music: " + ClickedSelectionPath.getName().replace(".png","") + " was not deleted.");
                    }
                }
            }

            else if (desc.equalsIgnoreCase("deleteuser")) {
                if (!mainUtil.confirmation(input)) return;

                mainUtil.closeAnimation(consoleFrame);
                mainUtil.deleteFolder(new File("src\\com\\cyder\\users\\" + mainUtil.getUserUUID()));

                //fail safe if not able to delete
                String dep = mainUtil.getDeprecatedUUID();
                File renamed = new File("src\\com\\cyder\\users\\" + dep);
                while (renamed.exists()) {
                    dep = mainUtil.getDeprecatedUUID();
                    renamed = new File("src\\com\\cyder\\users\\" + dep);
                }

                File old = new File("src\\com\\cyder\\users\\" + mainUtil.getUserUUID());
                old.renameTo(renamed);
            }

            else if (desc.equalsIgnoreCase("pixelatebackground")) {
                BufferedImage img = ImageUtil.pixelate(ImageIO.read(mainUtil.getCurrentBackground().getAbsoluteFile()), Integer.parseInt(input));

                String searchName = mainUtil.getCurrentBackground().getName().replace(".png", "")
                        + "_Pixelated_Pixel_Size_" + Integer.parseInt(input) + ".png";

                File saveFile = new File("src\\com\\cyder\\users\\" + mainUtil.getUserUUID() +
                        "\\Backgrounds\\" + searchName);

                ImageIO.write(img, "png", saveFile);

                mainUtil.initBackgrounds();

                File[] backgrounds = mainUtil.getValidBackgroundPaths();

                for (int i = 0 ; i < backgrounds.length ; i++) {
                    if (backgrounds[i].getName().equals(searchName)) {
                        parentLabel.setIcon(new ImageIcon(backgrounds[i].toString()));
                        parentLabel.setToolTipText(backgrounds[i].getName().replace(".png",""));
                        mainUtil.setCurrentBackgroundIndex(i);
                    }
                }

                println("Background pixelated and saved as a separate background file.");

                exitFullscreen();
            }
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void handle(String input) {
        try {
            operation = input;

            String firstWord = mainUtil.firstWord(operation);

            mainUtil.setHandledMath(false);

            handleMath(operation);

            if (mainUtil.filter(operation)) {
                println("Sorry, " + mainUtil.getUsername() + ", but that language is prohibited.");
                operation = "";
            }

            else if (mainUtil.isPalindrome(operation.replace(" ", "").toCharArray()) && operation.length() > 3){
                println("Nice palindrome.");
            }

            else if (((hasWord("quit") && !hasWord("db")) ||
                    (eic("leave") || (hasWord("stop") && !hasWord("music") && !hasWord("script")) ||
                            hasWord("exit") || eic("close"))) && !has("dance"))
            {
                mainUtil.setUsername(mainUtil.getUsername());
                mainUtil.setUsercolor(mainUtil.getUsercolor());
                mainUtil.setUserfont(mainUtil.getUserfont());

                saveFontColor();
                mainUtil.closeAnimation(consoleFrame);
                
                System.exit(0);
            }

            else if (eic("test")) {
                for (int i = 0 ; i < 361 ; i++)
                    println(i + ":" + mainUtil.getWindDirection(i + ""));

            }

            else if (hasWord("bletchy")) {
                bletchy(operation);
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
                int choice = mainUtil.randInt(1,6);

                switch(choice) {
                    case 1:
                        println("Hello " + mainUtil.getUsername() + ".");
                        break;
                    case 2:
                        println("Hi " + mainUtil.getUsername() + "." );
                        break;
                    case 3:
                        println("What's up " + mainUtil.getUsername() + "?");
                        break;
                    case 4:
                        println("How are you doing, " + mainUtil.getUsername() + "?");
                        break;
                    case 5:
                        println("Greetings, human " + mainUtil.getUsername() + ".");
                        break;
                    case 6:
                        println("Hi, " + mainUtil.getUsername() + ", I'm Cyder.");
                        break;
                }
            }

            else if (hasWord("bye") || (hasWord("james") && hasWord("arthur"))) {
                println("Just say you won't let go.");
            }

            else if (hasWord("time") && hasWord("what")) {
                println(mainUtil.weatherTime());
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
                String param = "C:\\Windows\\system32\\mspaint.exe";
                Runtime.getRuntime().exec(param);
            }

            else if (eic("pi")) {
                println(Math.PI);
            }

            else if (hasWord("euler") || eic("e")) {
                println("Leonhard Euler's number is " + Math.E);
            }

            else if (hasWord("scrub")) {
                bletchy("No you!");
            }

            else if (eic("break;")) {
                println("Thankfully I am over my infinite while loop days.");
            }

            else if (hasWord("url")) {
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
                mainUtil.setUserInputDesc("url");
                println("Enter your desired URL");
            }

            else if (hasWord("temperature") || eic("temp")) {
                TempConverter tc = new TempConverter();
            }

            else if (has("click me")) {
                mainUtil.clickMe();
            }

            else if ((hasWord("how") && hasWord("are") && hasWord("you")) && !hasWord("age") && !hasWord("old")) {
                println("I am feeling like a programmed response. Thank you for asking.");
            }

            else if (hasWord("how") && hasWord("day")) {
                println("I was having fun until you started asking me questions.");
            }

            else if (has("How old are you") || (hasWord("what") && hasWord("age"))) {
                bletchy("I am 2^8");
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
                mainUtil.resetMouse();
            }

            else if (eic("log off")) {
               println("Are you sure you want to log off your computer? (Enter yes/no)");
               mainUtil.setUserInputDesc("logoff");
               inputField.requestFocus();
               mainUtil.setUserInputMode(true);
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
                printImage("src\\com\\cyder\\io\\pictures\\msu.png");
            }

            else if (hasWord("toystory")) {
                mainUtil.playMusic("src\\com\\cyder\\io\\audio\\TheClaw.mp3");
            }

            else if (has("stop") && has("music")) {
                mainUtil.stopMusic();
            }

            else if (hasWord("reset") && hasWord("clipboard")) {
                StringSelection selection = new StringSelection(null);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                println("Clipboard has been reset.");
            }

            else if ((has("graphing") && has("calculator")) || has("desmos") || has("graphing")) {
                mainUtil.internetConnect("https://www.desmos.com/calculator");
            }

            else if (has("airHeads xtremes") || has("candy")) {
                mainUtil.internetConnect("http://airheads.com/candy#xtremes");
            }

            else if (hasWord("prime")) {
                println("Enter any positive integer and I will tell you if it's prime and what it's divisible by.");
                mainUtil.setUserInputDesc("prime");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
            }

            else if (hasWord("youtube") && (!has("word search") && !has("mode") && !has("random") && !has("thumbnail"))) {
                println("What would you like to search YouTube for?");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
                mainUtil.setUserInputDesc("youtube");
            }

            else if ((hasWord("google") && !has("mode") && !has("stupid"))) {
                println("What would you like to Google?");
                mainUtil.setUserInputDesc("google");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
            }

            else if (eic("404")) {
                mainUtil.internetConnect("http://google.com/=");
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
                mainUtil.internetConnect("https://www.triangle-calculator.com/");
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
                mainUtil.setDebugMode(false);
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
                mainUtil.setUserInputDesc("math");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
            }

            else if (eic("nathan")) {
                printlnImage("src\\com\\cyder\\io\\pictures\\me.png");
            }

            else if (has("always on top mode")) {
                if (hasWord("true")) {
                    println("Always on top mode has been set to true.");
                    mainUtil.setAlwaysOnTopMode(true);
                    consoleFrame.setAlwaysOnTop(true);
                }

                else if (hasWord("false")) {
                    println("Always on top mode has been set to false.");
                    mainUtil.setAlwaysOnTopMode(false);
                    consoleFrame.setAlwaysOnTop(false);
                }

                else {
                    println("Please specify the boolean value of always on top mode.");
                }
            }

            else if (hasWord("error") && !hasWord("throw")) {
                if (mainUtil.getDebugMode()) {
                    File WhereItIs = new File("src\\com\\cyder\\exception\\throws");
                    Desktop.getDesktop().open(WhereItIs);
                }

                else {
                    println("There are no errors here.");
                }
            }

            else if (eic("help")) {
                help();
            }

            else if (hasWord("light") && hasWord("saber")) {
                mainUtil.playMusic("src\\com\\cyder\\io\\audio\\Lightsaber.mp3");
            }

            else if (hasWord("xbox")) {
                mainUtil.playMusic("src\\com\\cyder\\io\\audio\\xbox.mp3");
            }

            else if (has("star") && has("trek")) {
                mainUtil.playMusic("src\\com\\cyder\\io\\audio\\StarTrek.mp3");
            }

            else if (eic("cmd") || (hasWord("command") && hasWord("prompt"))) {
                File WhereItIs = new File("c:\\Windows\\System32\\cmd.exe");
                Desktop.getDesktop().open(WhereItIs);
            }

            else if (hasWord("shakespeare")) {
                int rand = mainUtil.randInt(1,2);

                if (rand == 1) {
                    println("Glamis hath murdered sleep, and therefore Cawdor shall sleep no more, Macbeth shall sleep no more.");
                }

                else {
                    println("To be, or not to be, that is the question: Whether 'tis nobler in the mind to suffer the slings and arrows of "
                            + "outrageous fortune, or to take arms against a sea of troubles and by opposing end them.");
                }
            }

            else if (hasWord("windows")) {
                mainUtil.playMusic("src\\com\\cyder\\io\\audio\\windows.mp3");
            }

            else if (hasWord("binary")) {
                println("Enter a decimal number to be converted to binary.");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
                mainUtil.setUserInputDesc("binary");
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

                mainUtil.internetConnect("http://www.dictionary.com/browse/" + Define + "?s=t");
            }

            else if (hasWord("wikipedia")) {
                println("What would you like to look up on Wikipedia?");
                mainUtil.setUserInputDesc("wiki");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
            }

            else if (firstWord.equalsIgnoreCase("synonym")) {
                String Syn = operation.replace("synonym","");
                Syn = Syn.replace("'", "").replace(" ", "+");
                mainUtil.internetConnect("http://www.thesaurus.com//browse//" + Syn);
            }

            else if (hasWord("board")) {
                mainUtil.internetConnect("http://gameninja.com//games//fly-squirrel-fly.html");
            }

            else if (hasWord("open cd")) {
                mainUtil.openCD("D:\\");
            }

            else if (hasWord("close cd")) {
                mainUtil.closeCD("D:\\");
            }

            else if (hasWord("font") && !hasWord("reset")) {
                editUser();
            }

            else if (hasWord("font") && hasWord("reset")) {
                inputField.setFont(mainUtil.defaultFont);
                outputArea.setFont(mainUtil.defaultFont);
                println("The font has been reset.");
                saveFontColor();
            }

            else if (hasWord("color") && !hasWord("reset")) {
                editUser();
            }

            else if (hasWord("reset") && hasWord("color")) {
                outputArea.setForeground(mainUtil.vanila);
                inputField.setForeground(mainUtil.vanila);
                println("The text color has been reset.");
                saveFontColor();
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
                mainUtil.setUserInputMode(true);
                mainUtil.setUserInputDesc("random youtube");
            }

            else if (hasWord("arduino")) {
                mainUtil.internetConnect("https://www.arduino.cc/");
            }

            else if (has("rasberry pi")) {
                mainUtil.internetConnect("https://www.raspberrypi.org/");
            }

            else if (eic("&&")) {
                println("||");
            }

            else if (eic("||")) {
                println("&&");
            }

            else if (eic("youtube word search")) {
                println("Enter the desired word you would like to find in a YouTube URL");
                mainUtil.setUserInputDesc("youtube word search");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
            }

            else if (hasWord("disco")) {
                println("How many iterations would you like to disco for? (Enter a positive integer)");
                mainUtil.setUserInputMode(true);
                inputField.requestFocus();
                mainUtil.setUserInputDesc("disco");
            }

            else if (hasWord("game")) {
                File WhereItIs = new File("src\\com\\cyder\\io\\jars\\Jailbreak.jar");
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
                mainUtil.javaProperties();
            }

            else if (eic("preferences") || eic("prefs")) {
                editUser();
            }

            else if (hasWord("story") && hasWord("tell")) {
                println("It was a lazy day. Cyder was enjoying a deep sleep when suddenly " + mainUtil.getUsername() + " started talking to Cyder."
                        + " It was at this moment that Cyder knew its day had been ruined.");
            }

            else if (eic("hey")) {
                mainUtil.playMusic("src\\com\\cyder\\io\\audio\\heyya.mp3");
            }

            else if (eic("panic")) {
                mainUtil.setUsername(mainUtil.getUsername());
                mainUtil.setUsercolor(mainUtil.getUsercolor());
                mainUtil.setUserfont(mainUtil.getUserfont());

                saveFontColor();
                mainUtil.closeAnimation(consoleFrame);

                
                System.exit(0);
            }

            else if (eic("hash")) {
                Hasher h = new Hasher();
            }

            else if (hasWord("home")) {
                println("There's no place like localhost/127.0.0.1");
            }

            else if (eic("search") || eic("dir") || (hasWord("file") && hasWord("search")) || eic("directory") || eic("ls")) {
                DirectorySearch ds = new DirectorySearch();
            }

            else if (hasWord("I") && hasWord("love")) {
                println("Sorry, " + mainUtil.getUsername() + ", but I don't understand human emotions or affections.");
            }

            else if (hasWord("vexento")) {
                mainUtil.internetConnect("https://www.youtube.com/user/Vexento/videos");
            }

            else if ((hasWord("minecraft") && !hasWord("icon")) || hasWord("mc")) {
                MinecraftWidget mw = new MinecraftWidget();
            }

            else if (hasWord("icon") && hasWord("minecraft")) {
                consoleFrame.setIconImage(new ImageIcon("src\\com\\cyder\\io\\pictures\\Chest.png").getImage());
            }

            else if (eic("loop")) {
                println("mainUtil.handle(\"loop\");");
            }

            else if (hasWord("cyder") && has("dir")) {
                if (mainUtil.getDebugMode()) {
                    String CurrentDir = System.getProperty("user.dir");
                    mainUtil.openFile(CurrentDir);
                }

                else {
                    println("Sorry, " + mainUtil.getUsername() + ", but you don't have permission to do that.");
                }
            }

            else if ((has("tic") && has("tac") && has("toe")) || eic("TTT")) {
                TicTacToe ttt = new TicTacToe();
                ttt.startTicTacToe();
            }

            else if (hasWord("note")) {
                userNotes = new Notes(mainUtil.getUserUUID());
            }

            else if ((hasWord("youtube") && hasWord("thumbnail")) || (hasWord("yt") && hasWord("thumb"))) {
                YouTubeThumbnail yttn = new YouTubeThumbnail();
            }

            else if (hasWord("papers") && hasWord("please")) {
                mainUtil.internetConnect("http://papersplea.se/");
            }

            else if (eic("java")) {
                println("public class main {");
                println("      public static void main(String[] args) {");
                println("            System.out.println(\"Hello World!\");");
                println("      }");
                println("}");
            }

            else if (hasWord("coffee")) {
                mainUtil.internetConnect("https://www.google.com/search?q=coffe+shops+near+me");
            }

            else if (hasWord("ip")) {
                println(InetAddress.getLocalHost().getHostAddress());
            }

            else if(hasWord("html") || hasWord("html5")) {
                consoleFrame.setIconImage(new ImageIcon("src\\com\\cyder\\io\\pictures\\html5.png").getImage());
                printlnImage("src\\com\\cyder\\io\\pictures\\html5.png");
            }

            else if (hasWord("css")) {
                consoleFrame.setIconImage(new ImageIcon("src\\com\\cyder\\io\\pictures\\css.png").getImage());
                printlnImage("src\\com\\cyder\\io\\pictures\\css.png");
            }

            else if(hasWord("computer") && hasWord("properties")) {
                println("This may take a second, stand by...");
                mainUtil.computerProperties();
            }

            else if (hasWord("system") && hasWord("properties")) {
                mainUtil.systemProperties();
            }

            else if ((hasWord("pixelate") || hasWord("distort")) && (hasWord("image") || hasWord("picture"))) {
                pixelateFile = mainUtil.getFile();

                if (!pixelateFile.getName().endsWith(".png")) {
                    notification("Sorry, " + mainUtil.getUsername() + ", but this feature only supports PNG images",
                            5000, Notification.TOP_ARROW, Notification.RIGHT_VANISH, parentPanel);
                }

                else if (pixelateFile != null) {
                    println("Enter your pixel size (Enter a positive integer)");
                    mainUtil.setUserInputDesc("pixelate");
                    inputField.requestFocus();
                    mainUtil.setUserInputMode(true);
                }
            }

            else if (hasWord("donuts")) {
                mainUtil.internetConnect("https://www.dunkindonuts.com/en/food-drinks/donuts/donuts");
            }

            else if (hasWord("anagram")) {
                println("This function will tell you if two"
                        + "words are anagrams of each other."
                        + " Enter your first word");
                mainUtil.setUserInputDesc("anagram1");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);

            }

            else if (eic("controlc")) {
                mainUtil.setUserInputMode(false);
                killAllYoutube();
                println("Escaped");
            }

            else if (has("alphabet") && (hasWord("sort") || hasWord("organize") || hasWord("arrange"))) {
                println("Enter your word to be alphabetically rearranged");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
                mainUtil.setUserInputDesc("alphabetize");
            }

            else if (hasWord("mp3") || hasWord("music")) {
                mainUtil.mp3("", mainUtil.getUsername(), mainUtil.getUserUUID());
            }

            else if (hasWord("bai")) {
                mainUtil.internetConnect("http://www.drinkbai.com");
            }

            else if (has("occam") && hasWord("razor")) {
                mainUtil.internetConnect("http://en.wikipedia.org/wiki/Occam%27s_razor");
            }

            else if (hasWord("cyder") && (hasWord("picture") ||hasWord("image"))) {
                if (mainUtil.getDebugMode()) {
                    mainUtil.openFile("src\\com\\cyder\\io\\pictures");
                }

                else {
                    println("Sorry, " + mainUtil.getUsername() + ", but you do not have permission to access that.");
                }
            }

            else if (hasWord("when") && hasWord("thanksgiving")) {
                int year = Calendar.getInstance().get(Calendar.YEAR);
                LocalDate RealTG = LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
                println("Thanksgiving this year is on the " + RealTG.getDayOfMonth() + " of November.");
            }

            else if (hasWord("location") || (hasWord("where") && hasWord("am") && hasWord("i"))) {
                println("You are currently in " + mainUtil.getUserCity() + ", " +
                        mainUtil.getUserState() + " and your Internet Service Provider is " + mainUtil.getUserISP());
            }

            else if (hasWord("fibonacci")) {
                fib(0,1);
            }

            else if (hasWord("edit") && hasWord ("user")) {
                editUser();
            }

            else if (hasWord("throw") && hasWord("error")) {
                throw new CyderException("Error thrown on " + mainUtil.userTime());
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
                mainUtil.internetConnect("about:blank");
            }

            else if (hasWord("weather")) {
                WeatherWidget ww = new WeatherWidget();
            }

            else if (eic("hide")) {
                consoleFrame.setVisible(false);
            }

            else if (hasWord("stop") && hasWord("script")) {
                println("YouTube scripts have been killed.");
                killAllYoutube();
                consoleFrame.setTitle(mainUtil.getCyderVer() + " [" + mainUtil.getUsername() + "]");
            }

            else if (eic("debug")) {
                if (mainUtil.getDebugMode()) {
                    mainUtil.debug();
                }

                else {
                    println("Sorry, " + mainUtil.getUsername() + ", but you do not have permission to use that feature.");
                }
            }

            else if (hasWord("hangman")) {
                Hangman Hanger = new Hangman();
                Hanger.startHangman();
            }

            else if (hasWord("rgb") || hasWord("hex")) {
                mainUtil.colorConverter();
            }

            else if (hasWord("dance")) {
                mainUtil.dance(consoleFrame);
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
                mainUtil.setUserInputMode(true);
                inputField.requestFocus();
                mainUtil.setUserInputDesc("deleteuser");
            }

            else if ((hasWord("create") || hasWord("new")) &&
                    hasWord("user")) {
                createUser();
            }

            else if (hasWord("pixelate") && hasWord("background")) {
                println("Enter your pixel size (a positive integer)");
                mainUtil.setUserInputDesc("pixelatebackground");
                mainUtil.setUserInputMode(true);
                inputField.requestFocus();
            }

            else if (eic("long word")) {
                println("pneumonoultramicroscopicsilicovolcanoconiosis");
            }

            else if (eic("logic")) {
                mainUtil.playMusic("src\\com\\cyder\\io\\audio\\commando.mp3");
            }

            else if (eic("1-800-273-8255") || eic("18002738255")) {
                mainUtil.playMusic("src\\com\\cyder\\io\\audio\\1800.mp3");
            }

            else if (hasWord("resize") && (hasWord("image") || hasWord("picture"))) {
                ImageResizer IR = new ImageResizer();
            }

            else if (hasWord("barrel") && hasWord("roll")) {
                barrelRoll();
            }

            else if (hasWord("lines") && hasWord("code")) {
                println("Total lines of code: " + mainUtil.totalCodeLines(new File(System.getProperty("user.dir"))));
            }

            else if (hasWord("current") && hasWord("threads")) {
                ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                int num = threadGroup.activeCount();
                Thread[] printThreads = new Thread[num];
                threadGroup.enumerate(printThreads);

                for (int i = 0; i < num ; i++)
                    println(printThreads[i]);
            }

            else if (!mainUtil.getHandledMath()){
                println("Sorry, " + mainUtil.getUsername() + ", but I don't recognize that command." +
                        " You can make a suggestion by clicking the \"Suggest something\" button.");
            }
        }

        catch (Exception e) {
            mainUtil.handle(e);
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
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("ceil")) {
                    println(Math.ceil(param1));
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("floor")) {
                    println(Math.floor(param1));
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("log")) {
                    println(Math.log(param1));
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("log10")) {
                    println(Math.log10(param1));
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("max")) {
                    println(Math.max(param1,param2));
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("min")) {
                    println(Math.min(param1,param2));
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("pow")) {
                    println(Math.pow(param1,param2));
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("round")) {
                    println(Math.round(param1));
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("sqrt")) {
                    println(Math.sqrt(param1));
                    mainUtil.setHandledMath(true);
                }

                else if (mathop.equalsIgnoreCase("convert2")) {
                    println(mainUtil.toBinary((int)(param1)));
                    mainUtil.setHandledMath(true);
                }
            }
        }

        catch(Exception e) {
            mainUtil.handle(e);
        }
    }

    private void printlnImage(String filename) {
        outputArea.insertIcon(new ImageIcon(filename));
        println("");
    }

    private void printImage(String filename) {
        outputArea.insertIcon(new ImageIcon(filename));
    }

    private void print(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage, null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void print(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Integer.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void print(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Double.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void print(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Boolean.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void print(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Float.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void print(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Long.toString(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void print(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), String.valueOf(Usage), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void print(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString(), null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void println(String Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void println(int Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void println(double Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void println(boolean Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void println(float Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void println(long Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void println(char Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void println(Object Usage) {
        try {
            StyledDocument document = (StyledDocument) outputArea.getDocument();
            document.insertString(document.getLength(), Usage.toString() + "\n", null);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        }

        catch (Exception e) {
            mainUtil.handle(e);
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

        if (ThisOp.equals(ThisComp) || ThisOp.contains(' ' + ThisComp + ' ') || ThisOp.contains(' ' + ThisComp))
            return true;

        else return ThisOp.contains(ThisComp + ' ');
    }

    private boolean startsWith(String op, String comp) {
        char[] opA = op.toLowerCase().toCharArray();

        char[] compA = comp.toLowerCase().toCharArray();

        for (int i = 0 ; i < comp.length() ; i++) {
            if (Math.min(compA.length, opA.length) >= i) {
                return false;
            }

            if (Math.min(compA.length, opA.length) < i && compA[i] != opA[i]) {
                return false;
            }

            else if (Math.min(compA.length, opA.length) < i && compA[i] == opA[i] && i == compA.length - 1 && opA[i+1] == ' ') {
                return true;
            }
        }

        return false;
    }

    private boolean endsWith(String op, String comp) {
        char[] opA = reverseArray(op.toLowerCase().toCharArray());
        char[] compA = reverseArray(comp.toLowerCase().toCharArray());

        for (int i = 0 ; i < comp.length() ; i++) {
            if (Math.min(opA.length, compA.length) >= i) {
                return false;
            }

            if (i < Math.min(opA.length, compA.length) && compA[i] != opA[i]) {
                return false;
            }

            else if (compA[i] == opA[i] && i == compA.length - 1 && opA[i+1] == ' ') {
                return true;
            }
        }

        return false;
    }

    private char[] reverseArray(char[] Array) {
        String reverse = new StringBuilder(new String(Array)).reverse().toString();
        return reverse.toCharArray();
    }

    private void logToDo(String input) {
        try {
            if (input != null && !input.equals("") && !mainUtil.filter(input) && input.length() > 6 && !mainUtil.filter(input)) {
                BufferedWriter sugWriter = new BufferedWriter(new FileWriter("src\\com\\cyder\\io\\text\\add.txt", true));

                sugWriter.write("User " + mainUtil.getUsername() + " at " + mainUtil.weatherThreadTime() + " made the suggestion: ");
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
            mainUtil.handle(ex);
        }
    }

    public void saveFontColor() {
        try {
            Font SaveFont = outputArea.getFont();
            String SaveFontName = SaveFont.getName();
            Color SaveColor = outputArea.getForeground();

            int saveColorR = SaveColor.getRed();
            int saveColorG = SaveColor.getGreen();
            int saveColorB = SaveColor.getBlue();

            mainUtil.readUserData();
            mainUtil.writeUserData("Font",SaveFontName);
            mainUtil.writeUserData("Red",saveColorR + "");
            mainUtil.writeUserData("Green",saveColorG + "");
            mainUtil.writeUserData("Blue",saveColorB + "");
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    public void initUserfontAndColor() {
        try {
            mainUtil.readUserData();

            Font Userfont = new Font(mainUtil.getUserData("Font"),Font.BOLD, 30);
            Color Usercolor = new Color(Integer.parseInt(mainUtil.getUserData("Red")),
                                        Integer.parseInt(mainUtil.getUserData("Green")),
                                        Integer.parseInt(mainUtil.getUserData("Blue")));

            mainUtil.setUsercolor(Usercolor);
            mainUtil.setUserfont(Userfont);

            inputField.setForeground(Usercolor);
            outputArea.setForeground(Usercolor);

            inputField.setFont(Userfont);
            outputArea.setFont(Userfont);
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    public void randomYoutube(JFrame frameForTitle, int threadCount) {
        frameForTitle.setTitle("YouTube script running");

        for (int i = 0 ; i < threadCount ; i++) {
            youtubeThread current = new youtubeThread();
            youtubeThreads.add(current);
        }
    }

    public void help() {
        String[] Helps = {"Pixalte a Picture", "Home", "Mathsh", "Pizza", "Vexento", "Youtube", "note", "Create a User"
                , "Binary", "Font", "Color", "Preferences", "Hasher", "Directory Search", "Tic Tac Toe", "Youtube Thumbnail", "Java"
                , "Tell me a story", "Coffee", "Papers Please", "Delete User", "YouTube Word Search", "System Properties", "Donuts"
                , "System Sounds", "Weather", "Music", "mp3", "dance", "hangman", "youtube script"};

        ArrayList<Integer> UniqueIndexes = new ArrayList<>();

        for (int i = 0; i < Helps.length; i++) {
            UniqueIndexes.add(i);
        }

        Collections.shuffle(UniqueIndexes);
        println("Try typing:");

        for (int i = 0; i < 10; i++) {
            println(Helps[UniqueIndexes.get(i)]);
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
            mainUtil.handle(e);
        }
    }

    private void changeUsername(String newName) {
        try {
            mainUtil.readUserData();
            mainUtil.writeUserData("name",newName);

            mainUtil.setUsername(newName);
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    private void changePassword(char[] newPassword) {
        try {
            mainUtil.readUserData();
            mainUtil.writeUserData("password",mainUtil.toHexString(mainUtil.getSHA(newPassword)));
        }

        catch (Exception e) {
            mainUtil.handle(e);
        }
    }

    public void editUser() {
        if (editUserFrame != null)
            mainUtil.closeAnimation(editUserFrame);

        editUserFrame = new JFrame();
        editUserFrame.setResizable(false);
        editUserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editUserFrame.setResizable(false);
        editUserFrame.setIconImage(mainUtil.getCyderIcon().getImage());
        editUserFrame.setTitle("Edit User");

        JPanel ParentPanel = new JPanel();
        ParentPanel.setLayout(new BoxLayout(ParentPanel, BoxLayout.Y_AXIS));
        ParentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel BackgroundLabel = new JLabel("Backgrounds", SwingConstants.CENTER);
        BackgroundLabel.setFont(mainUtil.weatherFontSmall);

        JPanel LabelPanel = new JPanel();
        LabelPanel.add(BackgroundLabel);

        initializeBackgroundsList();

        backgroundSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        backgroundListScroll = new CyderScrollPane(backgroundSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        backgroundListScroll.setSize(400, 400);
        backgroundListScroll.setBackground(mainUtil.vanila);
        backgroundListScroll.setFont(mainUtil.weatherFontBig);
        backgroundListScroll.setThumbColor(mainUtil.regularRed);

        JPanel ButtonPanel = new JPanel();
        ButtonPanel.setLayout(new GridLayout(1, 3, 5, 5));
        ButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        CyderButton addBackground = new CyderButton("Add Background");
        addBackground.setBorder(new LineBorder(mainUtil.navy,5,false));
        addBackground.setColors(mainUtil.regularRed);
        ButtonPanel.add(addBackground);
        addBackground.setFocusPainted(false);
        addBackground.setBackground(mainUtil.regularRed);
        addBackground.addActionListener(e -> {
            try {
                File AddBackground = mainUtil.getFile();

                if (addMusic.getName() != null && addMusic.getName().endsWith(".png")) {
                    File Destination = new File("src\\com\\cyder\\users\\" + mainUtil.getUserUUID() + "\\Backgrounds\\" + AddBackground.getName());
                    Files.copy(new File(AddBackground.getAbsolutePath()).toPath(), Destination.toPath());
                    initializeBackgroundsList();
                    backgroundListScroll.setViewportView(backgroundSelectionList);
                    backgroundListScroll.revalidate();
                }
            }

            catch (Exception exc) {
                mainUtil.handle(exc);
            }
        });
        addBackground.setFont(mainUtil.weatherFontSmall);

        openBackground = new CyderButton("Open Background");
        openBackground.setBorder(new LineBorder(mainUtil.navy,5,false));
        openBackground.setColors(mainUtil.regularRed);
        ButtonPanel.add(openBackground);
        openBackground.setFocusPainted(false);
        openBackground.setBackground(mainUtil.regularRed);
        openBackground.setFont(mainUtil.weatherFontSmall);
        openBackground.addActionListener(e -> {
            List<?> ClickedSelectionList = backgroundSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < backgroundsNameList.size() ; i++) {
                    if (ClickedSelection.equals(backgroundsNameList.get(i))) {
                        ClickedSelectionPath = backgroundsList.get(i);
                        break;
                    }
                }

                mainUtil.draw(ClickedSelectionPath != null ? ClickedSelectionPath.toString() : null);
            }
        });

        CyderButton deleteBackground = new CyderButton("Delete Background");
        deleteBackground.setBorder(new LineBorder(mainUtil.navy,5,false));
        deleteBackground.setColors(mainUtil.regularRed);
        ButtonPanel.add(deleteBackground);
        deleteBackground.addActionListener(e -> {
            if (mainUtil.getValidBackgroundPaths().length == 1) {
                println("Sorry, but that is your only background. Try adding a different one and then " +
                        "removing it if you still don't want " + mainUtil.getValidBackgroundPaths()[0].getName() + ".");
            }

            else if (!backgroundSelectionList.getSelectedValuesList().isEmpty()){
                println("You are about to delete a background file. This action cannot be undone."
                        + " Are you sure you wish to continue? (yes/no)");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
                mainUtil.setUserInputDesc("deletebackground");
                mainUtil.initBackgrounds();
            }
        });
        deleteBackground.setBackground(mainUtil.regularRed);
        deleteBackground.setFont(mainUtil.weatherFontSmall);

        JPanel BackgroundsPanel = new JPanel();
        BackgroundsPanel.setLayout(new BoxLayout(BackgroundsPanel, BoxLayout.Y_AXIS));
        BackgroundsPanel.add(LabelPanel);
        BackgroundsPanel.add(backgroundListScroll);
        BackgroundsPanel.add(ButtonPanel);
        BackgroundsPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(mainUtil.navy,5,false)));
        ParentPanel.add(BackgroundsPanel);

        JLabel MusicLabel = new JLabel("Music", SwingConstants.CENTER);
        MusicLabel.setFont(mainUtil.weatherFontSmall);

        JPanel MusicLabelPanel = new JPanel();
        MusicLabelPanel.add(MusicLabel);

        initializeMusicList();

        musicSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        musicListScroll = new CyderScrollPane(musicSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        musicListScroll.setThumbColor(mainUtil.regularRed);
        musicListScroll.setSize(400, 400);
        musicListScroll.setBackground(mainUtil.vanila);
        musicListScroll.setFont(mainUtil.weatherFontSmall);

        JPanel BottomButtonPanel = new JPanel();
        BottomButtonPanel.setLayout(new GridLayout(1, 3, 5, 5));

        addMusic = new CyderButton("Add Music");
        addMusic.setBorder(new LineBorder(mainUtil.navy,5,false));
        addMusic.setBackground(mainUtil.regularRed);
        addMusic.setColors(mainUtil.regularRed);
        addMusic.setFont(mainUtil.weatherFontSmall);
        BottomButtonPanel.add(addMusic);
        addMusic.addActionListener(e -> {
            try {
                File AddMusic = mainUtil.getFile();

                if (AddMusic != null && AddMusic.getName().endsWith(".mp3")) {
                    File Destination = new File("src\\com\\cyder\\users\\" + mainUtil.getUserUUID() + "\\Music\\" + AddMusic.getName());
                    Files.copy(new File(AddMusic.getAbsolutePath()).toPath(), Destination.toPath());
                    initializeMusicList();
                    musicListScroll.setViewportView(musicSelectionList);
                    musicListScroll.revalidate();
                }
            }

            catch (Exception exc) {
                mainUtil.handle(exc);
            }
        });

        openMusic = new CyderButton("Open Music");
        openMusic.setColors(mainUtil.regularRed);
        openMusic.setBorder(new LineBorder(mainUtil.navy,5,false));
        openMusic.setBackground(mainUtil.regularRed);
        openMusic.setFont(mainUtil.weatherFontSmall);
        BottomButtonPanel.add(openMusic);
        openMusic.setFocusPainted(false);
        openMusic.setBackground(mainUtil.regularRed);
        openMusic.addActionListener(e -> {
            List<?> ClickedSelectionList = musicSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < musicNameList.size() ; i++) {
                    if (ClickedSelection.equals(musicNameList.get(i))) {
                        ClickedSelectionPath = musicList.get(i);

                        break;
                    }
                }

                mainUtil.mp3(ClickedSelectionPath.getAbsolutePath(), mainUtil.getUsername(), mainUtil.getUserUUID());
            }
        });

        CyderButton deleteMusic = new CyderButton("Delete Music");
        deleteMusic.setColors(mainUtil.regularRed);
        deleteMusic.setBorder(new LineBorder(mainUtil.navy,5,false));
        deleteMusic.setBackground(mainUtil.regularRed);
        deleteMusic.setFont(mainUtil.weatherFontSmall);
        BottomButtonPanel.add(deleteMusic);
        deleteMusic.setFocusPainted(false);
        deleteMusic.setBackground(mainUtil.regularRed);
        deleteMusic.addActionListener(e -> {
            if (!musicSelectionList.getSelectedValuesList().isEmpty()) {
                println("You are about to delete a music file. This action cannot be undone."
                        + " Are you sure you wish to continue? (yes/no)");
                mainUtil.setUserInputDesc("deletemusic");
                inputField.requestFocus();
                mainUtil.setUserInputMode(true);
            }
        });

        BottomButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel MusicPanel = new JPanel();
        MusicPanel.setLayout(new BoxLayout(MusicPanel, BoxLayout.Y_AXIS));
        MusicPanel.add(MusicLabelPanel);
        MusicPanel.add(musicListScroll);
        MusicPanel.add(BottomButtonPanel);
        MusicPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(mainUtil.navy,5,false)));

        ParentPanel.add(MusicPanel);

        JPanel ChangeUsernamePanel = new JPanel();
        ChangeUsernamePanel.setLayout(new GridLayout(2, 1, 5, 5));
        JTextField changeUsernameField = new JTextField(10);
        changeUsernameField.addActionListener(e -> changeUsername.doClick());
        changeUsernameField.setFont(mainUtil.weatherFontSmall);
        changeUsernameField.setSelectionColor(mainUtil.selectionColor);
        changeUsername = new CyderButton("Change Username");
        changeUsername.setBackground(mainUtil.regularRed);
        changeUsername.setColors(mainUtil.regularRed);
        changeUsername.setBorder(new LineBorder(mainUtil.navy,5,false));
        changeUsername.setFont(mainUtil.weatherFontSmall);
        changeUsernameField.setBorder(new LineBorder(mainUtil.navy,5,false));
        ChangeUsernamePanel.add(changeUsernameField);
        ChangeUsernamePanel.add(changeUsername);
        changeUsernameField.setToolTipText("New username");
        changeUsername.addActionListener(e -> {
            String newUsername = changeUsernameField.getText();
            if (!mainUtil.empytStr(newUsername)) {
                changeUsername(newUsername);
                mainUtil.inform("Username successfully changed","", 300, 200);
                mainUtil.refreshUsername(consoleFrame);
                changeUsernameField.setText("");
            }
        });


        JPanel ChangePasswordPanel = new JPanel();
        ChangePasswordPanel.setLayout(new GridLayout(2, 1, 5, 5));

        changeUsername.setBackground(mainUtil.regularRed);
        JPasswordField changePasswordField = new JPasswordField(10);
        changePasswordField.addActionListener(e -> changePassword.doClick());
        changePasswordField.setFont(mainUtil.weatherFontSmall);
        changePasswordField.setSelectionColor(mainUtil.selectionColor);
        changePassword = new CyderButton("Change Password");
        changePassword.setBackground(mainUtil.regularRed);
        changePassword.setColors(mainUtil.regularRed);
        changePassword.setBorder(new LineBorder(mainUtil.navy,5,false));
        changePassword.setFont(mainUtil.weatherFontSmall);
        changePasswordField.setBorder(new LineBorder(mainUtil.navy,5,false));
        ChangePasswordPanel.add(changePasswordField);
        ChangePasswordPanel.add(changePassword);
        changePasswordField.setToolTipText("New password");
        changePassword.addActionListener(e -> {
            char[] newPassword = changePasswordField.getPassword();

            if (newPassword.length > 4) {
                changePassword(newPassword);
                mainUtil.inform("Password successfully changed","", 300, 200);
                changePasswordField.setText("");
            }

            else {
                mainUtil.inform("Sorry, " + mainUtil.getUsername() + ", " +
                        "but your password must be greater than 4 characters for security reasons.","", 500, 300);
                changePasswordField.setText("");
            }

            for (char c : newPassword) {
                c = '\0';
            }
        });

        changePassword.setBackground(mainUtil.regularRed);
        ChangeUsernamePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        ParentPanel.add(ChangeUsernamePanel);

        ImageIcon selected = new ImageIcon("src\\com\\cyder\\io\\pictures\\checkbox1.png");
        ImageIcon notSelected = new ImageIcon("src\\com\\cyder\\io\\pictures\\checkbox2.png");

        JPanel prefsPanel = new JPanel();
        prefsPanel.setLayout(new GridLayout(6,3,0,20));

        JLabel introMusicTitle = new JLabel("Intro Music");
        introMusicTitle.setFont(mainUtil.weatherFontSmall);
        introMusicTitle.setForeground(mainUtil.navy);
        introMusicTitle.setHorizontalAlignment(JLabel.CENTER);
        prefsPanel.add(introMusicTitle);

        JLabel debugWindowsLabel = new JLabel("Debug Windows");
        debugWindowsLabel.setFont(mainUtil.weatherFontSmall);
        debugWindowsLabel.setForeground(mainUtil.navy);
        debugWindowsLabel.setHorizontalAlignment(JLabel.CENTER);
        prefsPanel.add(debugWindowsLabel);

        JLabel randomBackgroundLabel = new JLabel("Random Background");
        randomBackgroundLabel.setFont(mainUtil.weatherFontSmall);
        randomBackgroundLabel.setForeground(mainUtil.navy);
        randomBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        prefsPanel.add(randomBackgroundLabel);

        JLabel introMusic = new JLabel();
        introMusic.setHorizontalAlignment(JLabel.CENTER);
        introMusic.setSize(100,100);
        introMusic.setIcon((mainUtil.getUserData("IntroMusic").equals("1") ? selected : notSelected));
        introMusic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainUtil.getUserData("IntroMusic").equals("1");
            mainUtil.writeUserData("IntroMusic", (wasSelected ? "0" : "1"));
            introMusic.setIcon((wasSelected ? notSelected : selected));
            }
        });

        prefsPanel.add(introMusic);

        JLabel debugWindows = new JLabel();
        debugWindows.setHorizontalAlignment(JLabel.CENTER);
        debugWindows.setSize(100,100);
        debugWindows.setIcon((mainUtil.getUserData("DebugWindows").equals("1") ? selected : notSelected));
        debugWindows.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainUtil.getUserData("DebugWindows").equals("1");
            mainUtil.writeUserData("DebugWindows", (wasSelected ? "0" : "1"));
            debugWindows.setIcon((wasSelected ? notSelected : selected));
            }
        });

        prefsPanel.add(debugWindows);

        JLabel randBackgroundLabel = new JLabel();
        randBackgroundLabel.setHorizontalAlignment(JLabel.CENTER);
        randBackgroundLabel.setSize(100,100);
        randBackgroundLabel.setIcon((mainUtil.getUserData("RandomBackground").equals("1") ? selected : notSelected));
        randBackgroundLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainUtil.getUserData("RandomBackground").equals("1");
            mainUtil.writeUserData("RandomBackground", (wasSelected ? "0" : "1"));
            randBackgroundLabel.setIcon((wasSelected ? notSelected : selected));
            }
        });

        prefsPanel.add(randBackgroundLabel);

        JLabel hourlyChimesLabel = new JLabel("Hourly Chimes");

        hourlyChimesLabel.setFont(mainUtil.weatherFontSmall);

        hourlyChimesLabel.setForeground(mainUtil.navy);

        hourlyChimesLabel.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(hourlyChimesLabel);

        JLabel clockLabel = new JLabel("Console Clock");

        clockLabel.setFont(mainUtil.weatherFontSmall);

        clockLabel.setForeground(mainUtil.navy);

        clockLabel.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(clockLabel);

        JLabel silenceLabel = new JLabel("Silence Errors");

        silenceLabel.setFont(mainUtil.weatherFontSmall);

        silenceLabel.setForeground(mainUtil.navy);

        silenceLabel.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(silenceLabel);

        JLabel hourlyChimes = new JLabel();

        hourlyChimes.setHorizontalAlignment(JLabel.CENTER);

        hourlyChimes.setSize(100,100);

        hourlyChimes.setIcon((mainUtil.getUserData("HourlyChimes").equals("1") ? selected : notSelected));

        hourlyChimes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainUtil.getUserData("HourlyChimes").equals("1");
            mainUtil.writeUserData("HourlyChimes", (wasSelected ? "0" : "1"));
            hourlyChimes.setIcon((wasSelected ? notSelected : selected));
            }
        });

        prefsPanel.add(hourlyChimes);

        JLabel clockOnConsole = new JLabel();

        clockOnConsole.setHorizontalAlignment(JLabel.CENTER);

        clockOnConsole.setSize(100,100);

        clockOnConsole.setIcon((mainUtil.getUserData("ClockOnConsole").equals("1") ? selected : notSelected));

        clockOnConsole.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainUtil.getUserData("ClockOnConsole").equals("1");
            mainUtil.writeUserData("ClockOnConsole", (wasSelected ? "0" : "1"));
            clockOnConsole.setIcon((wasSelected ? notSelected : selected));
            consoleClockLabel.setVisible(!wasSelected);
            updateConsoleClock = !wasSelected;
            consoleFrame.revalidate();
            }
        });

        prefsPanel.add(clockOnConsole);

        JLabel silenceErrors = new JLabel();

        silenceErrors.setHorizontalAlignment(JLabel.CENTER);

        silenceErrors.setSize(100,100);

        silenceErrors.setIcon((mainUtil.getUserData("SilenceErrors").equals("1") ? selected : notSelected));

        silenceErrors.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainUtil.getUserData("SilenceErrors").equals("1");
            mainUtil.writeUserData("SilenceErrors", (wasSelected ? "0" : "1"));
            silenceErrors.setIcon((wasSelected ? notSelected : selected));
            }

        });

        prefsPanel.add(silenceErrors);

        JLabel fullscreenLabel = new JLabel("Fullscreen");

        fullscreenLabel.setFont(mainUtil.weatherFontSmall);

        fullscreenLabel.setForeground(mainUtil.navy);

        fullscreenLabel.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(fullscreenLabel);

        JLabel outputBorder = new JLabel("Output Area Border");

        outputBorder.setFont(mainUtil.weatherFontSmall);

        outputBorder.setForeground(mainUtil.navy);

        outputBorder.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(outputBorder);

        JLabel inputBorder = new JLabel("Input Field Border");

        inputBorder.setFont(mainUtil.weatherFontSmall);

        inputBorder.setForeground(mainUtil.navy);

        inputBorder.setHorizontalAlignment(JLabel.CENTER);

        prefsPanel.add(inputBorder);

        JLabel fullscreen = new JLabel();

        fullscreen.setHorizontalAlignment(JLabel.CENTER);

        fullscreen.setSize(100,100);

        fullscreen.setIcon((mainUtil.getUserData("FullScreen").equals("1") ? selected : notSelected));

        fullscreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainUtil.getUserData("FullScreen").equals("1");
            mainUtil.writeUserData("FullScreen", (wasSelected ? "0" : "1"));
            fullscreen.setIcon((wasSelected ? notSelected : selected));
            if (wasSelected) {
                exitFullscreen();
            }

            else {
                refreshFullscreen();
            }
            }
        });

        prefsPanel.add(fullscreen);

        prefsPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(mainUtil.navy,5,false)));

        JLabel outputborder = new JLabel();

        outputborder.setHorizontalAlignment(JLabel.CENTER);

        outputborder.setSize(100,100);

        outputborder.setIcon((mainUtil.getUserData("OutputBorder").equals("1") ? selected : notSelected));

        outputborder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainUtil.getUserData("OutputBorder").equals("1");
            mainUtil.writeUserData("OutputBorder", (wasSelected ? "0" : "1"));
            outputborder.setIcon((wasSelected ? notSelected : selected));
            if (wasSelected) {
                outputScroll.setBorder(BorderFactory.createEmptyBorder());
            }

            else {
                outputScroll.setBorder(new LineBorder(mainUtil.vanila,3,true));
            }

            consoleFrame.revalidate();
            }
        });

        prefsPanel.add(outputborder);

        JLabel inputborder = new JLabel();

        inputborder.setHorizontalAlignment(JLabel.CENTER);

        inputborder.setSize(100,100);

        inputborder.setIcon((mainUtil.getUserData("InputBorder").equals("1") ? selected : notSelected));

        inputborder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            boolean wasSelected = mainUtil.getUserData("InputBorder").equals("1");
            mainUtil.writeUserData("InputBorder", (wasSelected ? "0" : "1"));
            inputborder.setIcon((wasSelected ? notSelected : selected));

            if (wasSelected) {
                inputField.setBorder(BorderFactory.createEmptyBorder());
            }

            else {
                inputField.setBorder(new LineBorder(mainUtil.vanila,3,true));
            }

            consoleFrame.revalidate();
            }
        });

        prefsPanel.add(inputborder);

        JPanel masterPanel = new JPanel();

        masterPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        masterPanel.setLayout(new GridLayout(1,2));

        masterPanel.add(ParentPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(prefsPanel);
        ChangePasswordPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel fontColorPanel = new JPanel();
        fontColorPanel.setLayout(new BoxLayout(fontColorPanel, BoxLayout.X_AXIS));
        fontColorPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        fontColorPanel.add(getFontPanel());
        fontColorPanel.add(getColorPanel());

        rightPanel.add(fontColorPanel);

        rightPanel.add(ChangePasswordPanel);
        masterPanel.add(rightPanel);

        editUserFrame.add(masterPanel);
        editUserFrame.pack();
        editUserFrame.setLocationRelativeTo(null);
        editUserFrame.setVisible(true);
        editUserFrame.setAlwaysOnTop(true);
        editUserFrame.setAlwaysOnTop(false);
        editUserFrame.requestFocus();
    }

    public void initializeMusicList() {
        File dir = new File("src\\com\\cyder\\users\\" + mainUtil.getUserUUID() + "\\Music");
        musicList = new LinkedList<>();
        musicNameList = new LinkedList<>();

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith((".mp3"))) {
                musicList.add(file.getAbsoluteFile());
                musicNameList.add(file.getName().replace(".mp3", ""));
            }
        }

        String[] MusicArray = new String[musicNameList.size()];

        MusicArray = musicNameList.toArray(MusicArray);

        musicSelectionList = new JList(MusicArray);

        musicSelectionList.setFont(mainUtil.weatherFontSmall);

        musicSelectionList.setForeground(mainUtil.navy);

        musicSelectionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2 && musicSelectionList.getSelectedIndex() != -1) {
                openMusic.doClick();
            }
            }
        });

        musicSelectionList.setSelectionBackground(mainUtil.selectionColor);
    }

    public void initializeBackgroundsList() {
        File dir = new File("src\\com\\cyder\\users\\" + mainUtil.getUserUUID() + "\\Backgrounds");
        backgroundsList = new LinkedList<>();
        backgroundsNameList = new LinkedList<>();

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith((".png"))) {
                backgroundsList.add(file.getAbsoluteFile());
                backgroundsNameList.add(file.getName().replace(".png", ""));
            }
        }

        String[] BackgroundsArray = new String[backgroundsNameList.size()];
        BackgroundsArray = backgroundsNameList.toArray(BackgroundsArray);
        backgroundSelectionList = new JList(BackgroundsArray);

        backgroundSelectionList.setFont(mainUtil.weatherFontSmall);

        backgroundSelectionList.setForeground(mainUtil.navy);

        backgroundSelectionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            if (evt.getClickCount() == 2 && backgroundSelectionList.getSelectedIndex() != -1) {
                openBackground.doClick();
            }
            }
        });

        backgroundSelectionList.setSelectionBackground(mainUtil.selectionColor);
    }

    private JPanel getColorPanel() {
        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.PAGE_AXIS));

        JLabel label = new JLabel("Select your desired color");
        label.setFont(mainUtil.weatherFontSmall);
        label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        JPanel labelPanel = new JPanel();
        labelPanel.add(label);
        parentPanel.add(labelPanel, Component.CENTER_ALIGNMENT);

        JTextField hexField = new JTextField("",10);
        hexField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            if (hexField.getText().length() > 6) {
                hexField.setText(hexField.getText().substring(0,hexField.getText().length() - 1));
                Toolkit.getDefaultToolkit().beep();
            }

            else {
                try {
                    String colorStr = hexField.getText();
                    label.setForeground(new Color(Integer.valueOf(colorStr.substring(0,2),16),
                            Integer.valueOf(colorStr.substring(2,4),16),
                            Integer.valueOf(colorStr.substring(4,6),16)));
                }

                catch (Exception ignored) {

                }
            }
            }
        });
        hexField.setFont(mainUtil.weatherFontSmall);
        hexField.setSelectionColor(mainUtil.selectionColor);
        hexField.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(2,2,2,2),
                new LineBorder(mainUtil.navy,5,false)));
        hexField.setToolTipText("Hex Color");

        JPanel fieldPanel = new JPanel();
        fieldPanel.add(hexField);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        parentPanel.add(fieldPanel);

        CyderButton apply = new CyderButton("Apply Color");
        apply.setFocusPainted(false);
        apply.setColors(mainUtil.regularRed);
        apply.setForeground(mainUtil.navy);
        apply.setBackground(mainUtil.regularRed);
        apply.setFont(mainUtil.weatherFontSmall);
        apply.addActionListener(e -> {
            Color newColor = label.getForeground();
            outputArea.setForeground(newColor);
            inputField.setForeground(newColor);

            if (newColor != mainUtil.getUsercolor()) {
                println("The color [" + newColor.getRed() + "," + newColor.getGreen() + "," + newColor.getBlue() + "] has been applied.");
                saveFontColor();
            }
        });

        JPanel applyPanel = new JPanel();
        applyPanel.add(apply);
        applyPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        parentPanel.add(applyPanel, Component.CENTER_ALIGNMENT);
        parentPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));

        return parentPanel;
    }

    private JPanel getFontPanel() {
        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("Select your desired font");

        label.setFont(mainUtil.weatherFontSmall);
        label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel labelPanel = new JPanel();

        labelPanel.add(label);
        parentPanel.add(labelPanel, Component.CENTER_ALIGNMENT);

        String[] Fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        fontList = new JList(Fonts);
        fontList.setSelectionBackground(mainUtil.selectionColor);
        fontList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fontList.setFont(mainUtil.weatherFontSmall);

        CyderScrollPane FontListScroll = new CyderScrollPane(fontList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        FontListScroll.setThumbColor(mainUtil.intellijPink);
        FontListScroll.setBorder(new LineBorder(mainUtil.navy,5,true));

        CyderButton applyFont = new CyderButton("Apply Font");
        applyFont.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        applyFont.setColors(mainUtil.regularRed);
        applyFont.setToolTipText("Apply");
        applyFont.setFont(mainUtil.weatherFontSmall);
        applyFont.setFocusPainted(false);
        applyFont.setBackground(mainUtil.regularRed);
        applyFont.addActionListener(e -> {
            String FontS = (String) fontList.getSelectedValue();

            if (FontS != null) {
                Font ApplyFont = new Font(FontS, Font.BOLD, 30);
                outputArea.setFont(ApplyFont);
                inputField.setFont(ApplyFont);
                println("The font \"" + FontS + "\" has been applied.");
            }
        });

        fontList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyFont.doClick();
            }

            else {
                try {
                    label.setFont(new Font(fontList.getSelectedValue().toString(), Font.BOLD, 20));
                }

                catch (Exception ex) {
                    mainUtil.handle(ex);
                }
            }
            }
        });

        fontList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            JList t = (JList) e.getSource();
            int index = t.locationToIndex(e.getPoint());

            label.setFont(new Font(t.getModel().getElementAt(index).toString(), Font.BOLD, 20));
            }
        });

        parentPanel.add(FontListScroll, Component.CENTER_ALIGNMENT);

        JPanel apply = new JPanel();
        apply.add(applyFont);
        apply.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        parentPanel.add(apply, Component.CENTER_ALIGNMENT);

        return parentPanel;
    }

    private Runnable closeRunable = new Runnable() {
        @Override
        public void run() {
            mainUtil.closeAnimation(consoleFrame);

            saveFontColor();
            mainUtil.closeAnimation(consoleFrame);
            
            System.exit(0);
        }
    };

    public void closeAtHourMinute(int Hour, int Minute) {
        Calendar CloseCalendar = Calendar.getInstance();
        CloseCalendar.add(Calendar.DAY_OF_MONTH, 0);
        CloseCalendar.set(Calendar.HOUR_OF_DAY, Hour);
        CloseCalendar.set(Calendar.MINUTE, Minute);
        CloseCalendar.set(Calendar.SECOND, 0);
        CloseCalendar.set(Calendar.MILLISECOND, 0);
        long HowMany = (CloseCalendar.getTimeInMillis() - System.currentTimeMillis());
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(closeRunable,HowMany, TimeUnit.MILLISECONDS);
    }

    public void createUser() {
        createUserBackground = null;

        if (createUserFrame != null)
            mainUtil.closeAnimation(createUserFrame);

        createUserFrame = new JFrame();
        createUserFrame.setTitle("Create User");
        createUserFrame.setResizable(false);
        createUserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createUserFrame.setIconImage(mainUtil.getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();
        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        JLabel NameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        NameLabel.setFont(mainUtil.weatherFontSmall);

        JPanel NameLabelPanel = new JPanel();

        NameLabelPanel.add(NameLabel, SwingConstants.CENTER);

        ParentPanel.add(NameLabelPanel);

        newUserName = new JTextField(15);

        newUserName.setSelectionColor(mainUtil.selectionColor);

        newUserName.setFont(mainUtil.weatherFontSmall);

        newUserName.setForeground(mainUtil.navy);

        newUserName.setFont(mainUtil.weatherFontSmall);

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

        newUserName.setBorder(new LineBorder(mainUtil.navy,5,false));

        JPanel userNameFieldPanel = new JPanel();

        userNameFieldPanel.add(newUserName);

        ParentPanel.add(userNameFieldPanel);

        JLabel PasswordLabel = new JLabel("Password: ", SwingConstants.CENTER);

        PasswordLabel.setFont(mainUtil.weatherFontSmall);

        PasswordLabel.setForeground(mainUtil.navy);

        JLabel PasswordConfLabel = new JLabel("Re-enter pass: ", SwingConstants.CENTER);

        PasswordConfLabel.setFont(mainUtil.weatherFontSmall);

        PasswordConfLabel.setForeground(mainUtil.navy);

        JLabel matchPasswords = new JLabel("Passwords match");

        newUserPassword = new JPasswordField(15);
        newUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("Passwords match");
                matchPasswords.setForeground(mainUtil.regularGreen);
            }

            else {
                matchPasswords.setText("Passwords do not match");
                matchPasswords.setForeground(mainUtil.regularRed);
            }
            }
        });

        newUserPassword.setFont(mainUtil.weatherFontSmall);

        newUserPassword.setForeground(mainUtil.navy);

        newUserPassword.setBorder(new LineBorder(new Color(0, 0, 0)));

        newUserPassword.setSelectedTextColor(mainUtil.selectionColor);

        newUserPasswordconf = new JPasswordField(15);
        newUserPasswordconf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                matchPasswords.setText("Passwords match");
                matchPasswords.setForeground(mainUtil.regularGreen);
            }

            else {
                matchPasswords.setText("Passwords do not match");
                matchPasswords.setForeground(mainUtil.regularRed);
            }
            }
        });

        newUserPasswordconf.setFont(mainUtil.weatherFontSmall);

        newUserPasswordconf.setForeground(mainUtil.navy);

        newUserPasswordconf.setBorder(new LineBorder(new Color(0, 0, 0)));

        newUserPasswordconf.setSelectedTextColor(mainUtil.selectionColor);

        JPanel PasswordLabelPanel = new JPanel();

        PasswordLabelPanel.add(PasswordLabel, SwingConstants.CENTER);

        ParentPanel.add(PasswordLabelPanel);

        JPanel newPassPanel = new JPanel();

        newPassPanel.add(newUserPassword);

        newUserPassword.setBorder(new LineBorder(mainUtil.navy,5,false));

        ParentPanel.add(newPassPanel);

        JPanel PasswordConfLabelPanel = new JPanel();

        PasswordConfLabelPanel.add(PasswordConfLabel,SwingConstants.CENTER);

        JPanel passConf = new JPanel();

        passConf.add(newUserPasswordconf, SwingConstants.CENTER);

        newUserPasswordconf.setBorder(new LineBorder(mainUtil.navy,5,false));

        ParentPanel.add(PasswordConfLabelPanel);

        ParentPanel.add(passConf);

        matchPasswords.setFont(mainUtil.weatherFontSmall);
        matchPasswords.setForeground(mainUtil.regularGreen);
        JPanel matchPasswordsPanel = new JPanel();
        matchPasswordsPanel.add(matchPasswords);
        ParentPanel.add(matchPasswordsPanel);

        chooseBackground = new CyderButton("Choose background");

        chooseBackground.setToolTipText("Click me to choose a background");

        chooseBackground.setFont(mainUtil.weatherFontSmall);

        chooseBackground.setBackground(mainUtil.regularRed);

        chooseBackground.setColors(mainUtil.regularRed);

        chooseBackground.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    File temp = mainUtil.getFile();
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
                    mainUtil.handle(ex);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chooseBackground.setToolTipText("Choose background");
            }
        });

        chooseBackground.setBorder(new LineBorder(mainUtil.navy,5,false));

        JPanel BackgroundPanel = new JPanel();

        BackgroundPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        BackgroundPanel.add(chooseBackground);

        ParentPanel.add(BackgroundPanel);

        createNewUser = new CyderButton("Create User");

        createNewUser.setFont(mainUtil.weatherFontSmall);

        createNewUser.setBackground(mainUtil.regularRed);

        createNewUser.setColors(mainUtil.regularRed);

        createNewUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
            try {
                String uuid = mainUtil.generateUUID();
                File folder = new File("src\\com\\cyder\\users\\" + uuid);

                while (folder.exists()) {
                    uuid = mainUtil.generateUUID();
                    folder = new File("src\\com\\cyder\\users\\" + uuid);
                }

                char[] pass = newUserPassword.getPassword();
                char[] passconf = newUserPasswordconf.getPassword();

                boolean alreadyExists = false;
                File[] files = new File("src\\com\\cyder\\users").listFiles();

                for (File f: files) {
                    File data = new File(f.getAbsolutePath() + "\\Userdata.txt");
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

                if (mainUtil.empytStr(newUserName.getText()) || pass == null || passconf == null || createUserBackground == null ||
                        createUserBackground.getName().equals("No file chosen")
                        || uuid.equals("") || pass.equals("") || passconf.equals("") || uuid.length() == 0) {
                    mainUtil.inform("Sorry, but one of the required fields was left blank.\nPlease try again.","", 400, 300);
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (alreadyExists) {
                    mainUtil.inform("Sorry, but that username is already in use.\nPlease try a different one.", "", 400, 300);
                    newUserName.setText("");
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (!Arrays.equals(pass, passconf) && pass.length > 0) {
                    mainUtil.inform("Sorry, but your passwords did not match. Please try again.", "",400, 300);
                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else if (pass.length < 5) {
                    mainUtil.inform("Sorry, but your password length should be greater than\n"
                            + "four characters for security reasons. Please add more characters.", "", 400, 300);

                    newUserPassword.setText("");
                    newUserPasswordconf.setText("");
                }

                else {
                    File NewUserFolder = new File("src\\com\\cyder\\users\\" + uuid);
                    File backgrounds = new File("src\\com\\cyder\\users\\" + uuid + "\\Backgrounds");
                    File music = new File("src\\com\\cyder\\users\\" + uuid + "\\Music");
                    File notes = new File("src\\com\\cyder\\users\\" + uuid + "\\Notes");

                    NewUserFolder.mkdirs();
                    backgrounds.mkdir();
                    music.mkdir();
                    notes.mkdir();

                    ImageIO.write(ImageIO.read(createUserBackground), "png",
                            new File("src\\com\\cyder\\users\\" + uuid + "\\Backgrounds\\" + createUserBackground.getName()));

                    BufferedWriter newUserWriter = new BufferedWriter(new FileWriter(
                            "src\\com\\cyder\\users\\" + uuid + "\\Userdata.txt"));

                    LinkedList<String> data = new LinkedList<>();
                    data.add("Name:" + newUserName.getText().trim());
                    data.add("Font:tahoma");
                    data.add("Red:252");
                    data.add("Green:251");
                    data.add("Blue:227");
                    data.add("Password:" + mainUtil.toHexString(mainUtil.getSHA(pass)));
                    data.add("IntroMusic:0");
                    data.add("DebugWindows:0");
                    data.add("RandomBackground:0");
                    data.add("HourlyChimes:1");
                    data.add("ClockOnConsole:1");
                    data.add("SilenceErrors:1");
                    data.add("FullScreen:0");
                    data.add("OutputBorder:0");
                    data.add("InputBorder:0");

                    for (String d : data) {
                        newUserWriter.write(d);
                        newUserWriter.newLine();
                    }

                    newUserWriter.close();

                    mainUtil.closeAnimation(createUserFrame);

                    mainUtil.inform("The new user \"" + newUserName.getText().trim() + "\" has been created successfully.", "", 500, 300);

                    if (consoleFrame != null)
                        mainUtil.closeAnimation(createUserFrame);

                    else {
                        mainUtil.closeAnimation(createUserFrame);
                        recognize(newUserName.getText().trim(),pass);
                    }
                }

                for (char c : pass)
                    c = '\0';

                for (char c : passconf)
                    c = '\0';
            }

            catch (Exception ex) {
                mainUtil.handle(ex);
            }
            }
        });

        createNewUser.setBorder(new LineBorder(mainUtil.navy,5,false));

        createNewUser.setFont(mainUtil.weatherFontSmall);

        JPanel CreatePanel = new JPanel();

        CreatePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        CreatePanel.add(createNewUser);

        ParentPanel.add(CreatePanel);

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        createUserFrame.add(ParentPanel);

        createUserFrame.pack();
        createUserFrame.setLocationRelativeTo(null);
        createUserFrame.setVisible(true);
        createUserFrame.setAlwaysOnTop(true);
        createUserFrame.setAlwaysOnTop(false);

        newUserName.requestFocus();
    }

    private void refreshConsoleClock() {
        Thread TimeThread = new Thread(() -> {
            try {
                while (updateConsoleClock) {
                    Thread.sleep(3000);
                    consoleClockLabel.setText(mainUtil.consoleTime());
                    consoleClockLabel.setToolTipText(mainUtil.weatherThreadTime());
                }
            }

            catch (Exception e) {
                mainUtil.handle(e);
            }
        });

        TimeThread.start();
    }

    private void checkChime() {
        Thread ChimeThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(4000);
                    Calendar now = Calendar.getInstance();
                    if (now.get(Calendar.MINUTE) == 0 && now.get(Calendar.SECOND) < 5)
                        mainUtil.playMusic("src\\com\\cyder\\io\\audio\\chime.mp3");
                }
            }

            catch (Exception e) {
                mainUtil.handle(e);
            }
        });

        ChimeThread.start();
    }

    private void minimizeMenu() {
        if (menuLabel.isVisible()) {
            animation.jLabelXLeft(0,-150,10,8, menuLabel);

            Thread waitThread = new Thread(() -> {
                try {
                    Thread.sleep(186);
                }

                catch (Exception ex) {
                    mainUtil.handle(ex);
                }

                menuLabel.setVisible(false);

                menuButton.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\menuSide1.png"));
            });

            waitThread.start();
        }
    }

    private void bletchy(String str) {
        str = str.toLowerCase();
        str = str.replaceFirst("(?:bletchy)+", "").trim();
        final String s = str;

        Thread bletchyThread = new Thread(() -> {
            int len = s.length();


            char[] chars = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

            for (int i = 1 ; i < len ; i++) {
                for (int j = 0 ; j < 7 ; j++) {

                    String current = "";

                    for (int k = 0 ; k <= len ; k++) {
                        current += chars[mainUtil.randInt(0,25)];
                    }

                    println((s.substring(0,i) + current.substring(i, len)).toUpperCase());

                    try {
                        Thread.sleep(50);
                    }

                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    clc();
                }
            }

            println(s.toUpperCase());
        });

        bletchyThread.start();
    }

    private class youtubeThread  {
        private boolean exit = false;
        youtubeThread() {
            Thread tread = new Thread(() -> {
                while (!exit) {
                    String Start;
                    String UUID;

                    char[] ValidChars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2',
                            '3', '4', '5', '6', '7', '8', '9', '-', '_'};

                    for (int j = 1; j < 100000; j++) {
                        try {
                            if (exit)
                                break;

                            Start = "https://www.youtube.com/watch?v=";
                            UUID = "";
                            StringBuilder UUIDBuilder = new StringBuilder(UUID);

                            for (int i = 1; i < 12; i++)
                                UUIDBuilder.append(ValidChars[mainUtil.randInt(0, 63)]);

                            UUID = UUIDBuilder.toString();

                            println("Checked UUID: " + UUID);
                            Start = Start + UUID;
                            String YouTubeURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";

                            BufferedImage Thumbnail = ImageIO.read(new URL(YouTubeURL.replace("REPLACE", UUID)));
                            killAllYoutube();
                            println("YouTube script found valid video with UUID: " + UUID);

                            JFrame thumbnailFrame = new JFrame();

                            thumbnailFrame.setUndecorated(true);

                            thumbnailFrame.setTitle(UUID);

                            thumbnailFrame.addMouseMotionListener(new MouseMotionListener() {
                                @Override
                                public void mouseDragged(MouseEvent e) {
                                    int x = e.getXOnScreen();
                                    int y = e.getYOnScreen();

                                    if (thumbnailFrame != null && thumbnailFrame.isFocused()) {
                                        thumbnailFrame.setLocation(x - xMouse, y - yMouse);
                                    }
                                }

                                @Override
                                public void mouseMoved(MouseEvent e) {
                                    xMouse = e.getX();
                                    yMouse = e.getY();
                                }
                            });

                            thumbnailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                            JPanel parentPanel = new JPanel();

                            parentPanel.setBorder(new LineBorder(mainUtil.navy, 10, false));

                            parentPanel.setLayout(new BorderLayout());

                            thumbnailFrame.setContentPane(parentPanel);

                            JLabel PictureLabel = new JLabel(new ImageIcon(Thumbnail));

                            PictureLabel.setToolTipText("Open video " + UUID);

                            String video = "https://www.youtube.com/watch?v=" + UUID;

                            PictureLabel.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                    mainUtil.internetConnect(video);
                                }
                            });

                            parentPanel.add(PictureLabel, BorderLayout.PAGE_START);

                            CyderButton closeYT = new CyderButton("Close");

                            closeYT.setColors(mainUtil.regularRed);

                            closeYT.setBorder(new LineBorder(mainUtil.navy, 5, false));

                            closeYT.setFocusPainted(false);

                            closeYT.setBackground(mainUtil.regularRed);

                            closeYT.setFont(mainUtil.weatherFontSmall);

                            closeYT.addActionListener(ev -> mainUtil.closeAnimation(thumbnailFrame));

                            closeYT.setSize(thumbnailFrame.getX(), 20);

                            parentPanel.add(closeYT, BorderLayout.PAGE_END);

                            parentPanel.repaint();

                            thumbnailFrame.pack();
                            thumbnailFrame.setVisible(true);
                            thumbnailFrame.setLocationRelativeTo(null);
                            thumbnailFrame.setResizable(false);
                            thumbnailFrame.setIconImage(mainUtil.getCyderIcon().getImage());

                            break;
                        }

                        catch (Exception ignored) {}
                    }
                }
            });

            tread.start();
        }

        public void kill() {
            this.exit = true;
        }
    }

    private void killAllYoutube() {
        for (youtubeThread ytt: youtubeThreads) {
            ytt.kill();
        }
    }

    public void notification(String htmltext, int delay, int arrowDir, int vanishDir, JLayeredPane parent) {
        if (consoleNotification != null && consoleNotification.isVisible())
            consoleNotification.kill();

        consoleNotification = new Notification();

        //width does not work still
        int w = (int) Math.ceil(12 * htmltext.length());
        int h = 30;

        consoleNotification.setWidth(w);
        consoleNotification.setHeight(h);
        consoleNotification.setArrow(arrowDir);

        JLabel text = new JLabel(htmltext);
        text.setFont(mainUtil.weatherFontSmall);
        text.setForeground(mainUtil.navy);
        text.setBounds(14,10,w * 2,h);
        consoleNotification.add(text);
        consoleNotification.setBounds(parent.getWidth() - (w + 30),30,w * 2,h * 2);
        parent.add(consoleNotification,1,0);
        parent.repaint();

        consoleNotification.vanish(vanishDir, parent, delay);
    }

    private void barrelRoll() {
        consoleFrame.setBackground(mainUtil.navy);
        mainUtil.getValidBackgroundPaths();

        Timer timer = null;
        Timer finalTimer = timer;
        timer = new Timer(10, new ActionListener() {
            private double angle = 0;
            private double delta = 1.0;

            BufferedImage master = mainUtil.getBi(mainUtil.getCurrentBackground());
            BufferedImage rotated;

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += delta;
                if (angle >= 360) {
                    parentLabel.setIcon(new ImageIcon(mainUtil.getCurrentBackground().toString()));
                    return;
                }
                rotated = mainUtil.rotateImageByDegrees(master, angle);
                parentLabel.setIcon(new ImageIcon(rotated));
            }
        });
        timer.start();
    }
}