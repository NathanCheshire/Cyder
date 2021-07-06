package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.enums.Direction;
import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.threads.CyderThreadFactory;
import cyder.utilities.*;
import cyder.widgets.GenericInform;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class ConsoleFrame extends CyderFrame {
    //the one and only console frame method
    private static ConsoleFrame theConsoleFrame = new ConsoleFrame();

    public static ConsoleFrame getConsoleFrame() {
        return theConsoleFrame;
    }

    private ConsoleFrame() {} //no instantiation this way

    /**
     * Assuming uuid has been set, this will launch the whole of the program.
     * Main class is used for user auth then calls ConsoleFrame so under current program structure,
     * only one instance of console frame should ever exist.
     */

    private CyderScrollPane outputScroll;
    public static JTextPane outputArea;
    private JPasswordField inputField;

    private JLabel consoleClockLabel;
    private JLabel menuLabel;

    private JButton minimize;
    private JButton close;
    private JButton suggestionButton;
    private JButton menuButton;
    private JButton alternateBackground;

    private boolean updateConsoleClock;
    private boolean menuGenerated;

    private String consoleBashString;

    private boolean backgroundProcessCheckerStarted = false;
    private boolean drawConsoleLines = false;
    private boolean consoleLinesDrawn = false;
    private Color lineColor = Color.white;

    private CyderFrame theActualConsoleFrame;

    private void start(String UUID) {
        resizeBackgrounds();
        initBackgrounds();

        try {
            //set line color and bash string
            consoleBashString = getUsername() + "@Cyder:~$ ";
            lineColor = ImageUtil.getDominantColorOpposite(ImageIO.read(getCurrentBackgroundFile()));

            //handle random background by setting a random background index
            if (IOUtil.getUserData("RandomBackground").equals("1")) {
                if (getBackgrounds().size() <= 1) {
                    notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() + ", " +
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

            } else {
                w = getCurrentBackgroundImageIcon().getIconWidth();
                h = getCurrentBackgroundImageIcon().getIconHeight();
                usage = new ImageIcon(ImageUtil.getRotatedImage(
                        getCurrentBackgroundFile().toString(),getConsoleDirection()));
            }

            //override the CyderFrame we use for ConsoleFrame to add in the debug lines
            theActualConsoleFrame = new CyderFrame(w, h) {
                @Override
                public void paint(Graphics g) {
                    super.paint(g);

                    if (drawConsoleLines && !consoleLinesDrawn) {
                        try {
                            Graphics2D g2d = (Graphics2D) g;

                            BufferedImage img = null;
                            int w = 0;
                            int h = 0;

                            img = ImageUtil.resizeImage(25,25, getCurrentBackgroundFile());
                            w = img.getWidth(null);
                            h = img.getHeight(null);

                            g2d.setPaint(lineColor);
                            int strokeThickness = 4;
                            g2d.setStroke(new BasicStroke(strokeThickness));

                            g2d.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
                            g2d.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);

                            if (img != null) {
                                g2d.drawImage(img, getWidth() / 2 - w / 2, getHeight() / 2 - h / 2, null);
                            }

                            consoleLinesDrawn = true;
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
            };

            //todo linked to inputhandler: consolePrintingAnimation();

            //we should always be using controlled exits so this is why we use DO_NOTHING_ON_CLOSE
            theActualConsoleFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            theActualConsoleFrame.setTitlePosition(TitlePosition.CENTER);

            //todo need to overeride setting title so that it's always center clock but super title is this
            theActualConsoleFrame.setTitle(IOUtil.getSystemData("Version") +
                    " Cyder [" + ConsoleFrame.getConsoleFrame().getUsername() + "]");

            //todo work in resizing somewhere? maybe allow/disallow this in Sys.ini

            ((JLabel) (theActualConsoleFrame.getContentPane()))
                    .setToolTipText(StringUtil.getFilename(getCurrentBackgroundFile().getName()));

            outputArea = new JTextPane();
            outputArea.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    //todo minimizeMenu();
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
            theActualConsoleFrame.getContentPane().add(outputScroll);

            //output area settings complete; starting input field
            inputField = new JPasswordField(40);
            inputField.setEchoChar((char)0);
            inputField.setText(consoleBashString);

            if (IOUtil.getUserData("InputBorder").equalsIgnoreCase("1")) {
                inputField.setBorder(new LineBorder(ColorUtil.hextorgbColor
                        (IOUtil.getUserData("Background")), 3, false));
            } else {
                inputField.setBorder(BorderFactory.createEmptyBorder()); //todo test null border?
            }

            //input field key listeners such as auto-capitalization, escaping, and console rotations
            inputField.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (inputField.getPassword().length == consoleBashString.length() + 1) {
                        inputField.setText(consoleBashString +
                                String.valueOf(inputField.getPassword()).substring(consoleBashString.length()).toUpperCase());
                    }
                    if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
                        //todo linked handler handle("controlc");
                    }
                    if ((e.getKeyCode() == KeyEvent.VK_DOWN) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.getConsoleFrame().setConsoleDirection(Direction.BOTTOM);
                        //todo here exitFullscreen();
                    }
                    if ((e.getKeyCode() == KeyEvent.VK_RIGHT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.getConsoleFrame().setConsoleDirection(Direction.RIGHT);
                        //todo here exitFullscreen();
                    }
                    if ((e.getKeyCode() == KeyEvent.VK_UP) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.getConsoleFrame().setConsoleDirection(Direction.TOP);
                        //todo here exitFullscreen();
                    }
                    if ((e.getKeyCode() == KeyEvent.VK_LEFT) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)) {
                        ConsoleFrame.getConsoleFrame().setConsoleDirection(Direction.LEFT);
                        //todo here exitFullscreen();
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

                        theActualConsoleFrame.repaint();
                    }
                }

                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    //capitalization
                    if (inputField.getPassword().length == consoleBashString.length() + 1) {
                        inputField.setText(consoleBashString + String.valueOf(
                                inputField.getPassword()).substring(consoleBashString.length()).toUpperCase());
                    }
                    //debug lines
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
                    //todo genesis share exit(-404);
                }
            });

            //a bodge to update the caret position if it goes before an allowed index for console bash string
            new Thread(() -> {
                try {
                    while (theActualConsoleFrame != null) {
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
            //todo actually copy over and put here inputField.addKeyListener(commandScrolling);
            inputField.setCaretPosition(consoleBashString.length());

            theActualConsoleFrame.addWindowListener(new WindowAdapter() {
                public void windowOpened(WindowEvent e) {
                    inputField.requestFocus();
                    onLaunch();
                }
            });

            inputField.setBounds(10, 82 + outputArea.getHeight(),getBackgroundWidth() - 20,
                    getBackgroundHeight() - (outputArea.getHeight() + 62 + 40));
            inputField.setOpaque(false);
            theActualConsoleFrame.getContentPane().add(inputField);
            //todo copy over inputField.addActionListener(inputFieldAction);
            inputField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    //todo here minimizeMenu();
                }
            });

            inputField.setCaretColor(ConsoleFrame.getConsoleFrame().getUserForegroundColor());
            inputField.setCaret(new CyderCaret(ConsoleFrame.getConsoleFrame().getUserForegroundColor()));
            inputField.setForeground(ConsoleFrame.getConsoleFrame().getUserForegroundColor());
            inputField.setFont(ConsoleFrame.getConsoleFrame().getUserFont());

            Color fillColor = ColorUtil.hextorgbColor(IOUtil.getUserData("Background"));

            if (IOUtil.getUserData("OutputFill").equals("1")) {
                outputArea.setOpaque(true);
                outputArea.setBackground(fillColor);
                outputArea.repaint();
                outputArea.revalidate();
                theActualConsoleFrame.revalidate(); //todo is this needed?
            }

            if (IOUtil.getUserData("InputFill").equals("1")) {
                inputField.setBackground(fillColor);
            }

            suggestionButton = new JButton("");
            suggestionButton.setToolTipText("Suggestions");
            suggestionButton.addActionListener(e -> {
                notify("What feature would you like to suggest? " +
                        "(Please include as much detail as possible such as " +
                        "how the feature should be triggered and how the program should responded; be detailed)");
                //todo linkedHandler.getstringUtil.setUserInputDesc("suggestion");
                //todo linkedHandler.getstringUtil.setUserInputMode(true);
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
            getTopDragLabel().add(suggestionButton);
            suggestionButton.setFocusPainted(false);
            suggestionButton.setOpaque(false);
            suggestionButton.setContentAreaFilled(false);
            suggestionButton.setBorderPainted(false);

            menuButton = new JButton("");
            menuLabel = new JLabel();
            menuLabel.setVisible(false);
            menuButton.setToolTipText("Menu");
            //todo here menuButton.addMouseListener(consoleMenu);
            menuButton.setBounds(4, 4, 22, 22);
            menuButton.setIcon(new ImageIcon("sys/pictures/icons/menuSide1.png"));
            getTopDragLabel().add(menuButton);
            menuButton.setVisible(true);
            menuButton.setFocusPainted(false);
            menuButton.setOpaque(false);
            menuButton.setContentAreaFilled(false);
            menuButton.setBorderPainted(false);

            //todo override drag label stuff and directly add your own buttons
            LinkedList<JButton> customButtonList = new LinkedList<>();

            minimize = new JButton("");
            minimize.setToolTipText("Minimize");
            minimize.addActionListener(e -> {
                //restoreX = consoleFrame.getX();
                //restoreY = consoleFrame.getY();
                AnimationUtil.minimizeAnimation(theActualConsoleFrame);
                updateConsoleClock = false;
                theActualConsoleFrame.setState(Frame.ICONIFIED);
                //todo here minimizeMenu();
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
            minimize.setBounds(ConsoleFrame.getConsoleFrame().getBackgroundWidth() - 81, 4, 22, 20);
            minimize.setIcon(CyderImages.minimizeIcon);
            minimize.setFocusPainted(false);
            minimize.setOpaque(false);
            minimize.setContentAreaFilled(false);
            minimize.setBorderPainted(false);
            customButtonList.add(minimize);


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
                        lineColor = ImageUtil.getDominantColorOpposite(ImageIO.read(ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile()));

                        if (ConsoleFrame.getConsoleFrame().canSwitchBackground() && ConsoleFrame.getConsoleFrame().getBackgrounds().size() > 1) {
                            ConsoleFrame.getConsoleFrame().incBackgroundIndex();
                            switchBackground();
                        } else if (ConsoleFrame.getConsoleFrame().onLastBackground() && ConsoleFrame.getConsoleFrame().getBackgrounds().size() > 1) {
                            ConsoleFrame.getConsoleFrame().setBackgroundIndex(0);
                            switchBackground();
                        } else if (ConsoleFrame.getConsoleFrame().getBackgrounds().size() == 1) {
                            ConsoleFrame.this.notify("You only have one background image. Would you like to add more? (Enter yes/no)");
                            inputField.requestFocus();
                            //todo handler stringUtil.setUserInputMode(true);
                            //todo handler stringUtil.setUserInputDesc("addbackgrounds");
                            inputField.requestFocus();
                        }
                    } catch (Exception ex) {
                        ErrorHandler.handle(new FatalException("Background DNE"));
                        ConsoleFrame.this.notify("Error in parsing background; perhaps it was deleted.");
                    }
                }
            });

            alternateBackground.setBounds(ConsoleFrame.getConsoleFrame().getBackgroundWidth() - 54,
                    4, 22, 20);
            alternateBackground.setIcon(new ImageIcon("sys/pictures/icons/ChangeSize1.png"));
            alternateBackground.setFocusPainted(false);
            alternateBackground.setOpaque(false);
            alternateBackground.setContentAreaFilled(false);
            alternateBackground.setBorderPainted(false);
            customButtonList.add(alternateBackground);

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
                    //todo genesis share exit(25);
                }
            });

            close.setBounds(ConsoleFrame.getConsoleFrame().getBackgroundWidth() - 27, 4, 22, 20);
            close.setIcon(CyderImages.closeIcon);
            close.setFocusPainted(false);
            close.setOpaque(false);
            close.setContentAreaFilled(false);
            close.setBorderPainted(false);
            customButtonList.add(close);

            getTopDragLabel().setButtonsList(customButtonList);

            //this turns into setting a center title
            consoleClockLabel = new JLabel(TimeUtil.consoleTime(), SwingConstants.CENTER);
            consoleClockLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(20f));
            consoleClockLabel.setForeground(CyderColors.vanila);
            //bounds not needed to be set since the executor service handles that
            getTopDragLabel().add(consoleClockLabel);

            updateConsoleClock = IOUtil.getUserData("ClockOnConsole").equalsIgnoreCase("1");

            //todo make a method to start/end executors

            Executors.newSingleThreadScheduledExecutor(
                    new CyderThreadFactory("ConsoleClock Updater")).scheduleAtFixedRate(() -> {
                if (consoleClockLabel.isVisible())
                    if (IOUtil.getUserData("ShowSeconds").equalsIgnoreCase("1")) {
                        String time = TimeUtil.consoleSecondTime();
                        int clockWidth = CyderFrame.getMinWidth(time, consoleClockLabel.getFont()) + 10;
                        int clockHeight = CyderFrame.getMinHeight(time, consoleClockLabel.getFont());

                        consoleClockLabel.setBounds(getWidth() / 2 - clockWidth / 2,
                                -5, clockWidth, clockHeight);
                        consoleClockLabel.setText(time);
                    } else {
                        String time = TimeUtil.consoleTime();
                        int clockWidth = CyderFrame.getMinWidth(time, consoleClockLabel.getFont()) + 10;
                        int clockHeight = CyderFrame.getMinHeight(time, consoleClockLabel.getFont());

                        consoleClockLabel.setBounds(getWidth() / 2 - clockWidth / 2,
                                -5, clockWidth, clockHeight);
                        consoleClockLabel.setText(time);
                    }
            }, 0, 500, TimeUnit.MILLISECONDS);
            consoleClockLabel.setVisible(updateConsoleClock);

            Executors.newSingleThreadScheduledExecutor(
                    new CyderThreadFactory("Hourly Chime Checker")).scheduleAtFixedRate(() -> {
                if (IOUtil.getUserData("HourlyChimes").equalsIgnoreCase("1"))
                    IOUtil.playAudio("sys/audio/chime.mp3", outputArea, false);
            }, 3600 - LocalDateTime.now().getMinute() * 60 - LocalDateTime.now().getSecond(), 3600, SECONDS);

            //todo start/end executors via methods
            theActualConsoleFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeiconified(WindowEvent e) {
                    updateConsoleClock = true;
                }
                @Override
                public void windowIconified(WindowEvent e) {
                    updateConsoleClock = false;
                }
            });

            //dispose login frame now to avoid final frame disposed checker seeing that there are no frames
            // and exiting the program when we have just logged in
            //TODO do this in main after calling ConsoleFrame.start();
//            if (loginFrame != null)
//                loginFrame.closeAnimation();

            //network connection checker
            Executors.newSingleThreadScheduledExecutor(
                    new CyderThreadFactory("Stable Network Connection Checker")).scheduleAtFixedRate(() -> {
                        //update console clock tells us if we're iconified or not
                if (!NetworkUtil.internetReachable() && updateConsoleClock) {
                    notify("Sorry, " + ConsoleFrame.getConsoleFrame().getUsername() +
                            ", but I had trouble connecting to the internet.\n" +
                            "As a result, some features may not work properly.");
                }
            }, 0, 5, MINUTES);
            consoleClockLabel.setVisible(updateConsoleClock);

            //todo executors should be started once from main and not from console() method

            //final frame disposed checker
            Executors.newSingleThreadScheduledExecutor(
                    new CyderThreadFactory("Final Frame Disposed Checker")).scheduleAtFixedRate(() -> {
                Frame[] frames = Frame.getFrames();
                int validFrames = 0;

                for (Frame f : frames) {
                    if (f.isShowing()) {
                        validFrames++;
                    }
                }

                if (validFrames < 1) {
                    //todo genesis share exit(120);
                }
            }, 10, 5, SECONDS);


            //todo isn't this already set?
            lineColor = ImageUtil.getDominantColorOpposite(ImageIO.read(ConsoleFrame.getConsoleFrame().getCurrentBackgroundFile()));

            theActualConsoleFrame.enterAnimation();

            //after frame construction is complete and we've animated the entry of the frame----------------------------
            //todo moved to onLaunch
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //one time run things such as notifying due to special days, debug properties,
    // last start time, and auto testing
    private void onLaunch() {
        //special day events
        if (TimeUtil.isChristmas())
            notify("Merry Christmas!");

        if (TimeUtil.isHalloween())
            notify("Happy Halloween!");

        if (TimeUtil.isIndependenceDay())
            notify("Happy 4th of July!");

        if (TimeUtil.isThanksgiving())
            notify("Happy Thanksgiving!");

        if (TimeUtil.isAprilFoolsDay())
            notify("Happy April Fool Day!");

        //preference handlers here
        if (IOUtil.getUserData("DebugWindows").equals("1")) {
            StatUtil.systemProperties();
            StatUtil.computerProperties();
            StatUtil.javaProperties();
            StatUtil.debugMenu(outputArea);
        }

        //Auto test in upon start debug mode
        if (SecurityUtil.nathanLenovo()) {
            //todo handle("test"); for the linked input handler
        }

        //last start time operations
        if (TimeUtil.milisToDays(System.currentTimeMillis() -
                Long.parseLong(IOUtil.getUserData("laststart"))) > 1) {
            notify("Welcome back, " + ConsoleFrame.getConsoleFrame().getUsername() + "!");
        }

        IOUtil.writeUserData("laststart",System.currentTimeMillis() + "");
    }

    @Override
    public void repaint() {
        super.repaint();

        //todo fullscreen/rotation ops

        //input field bounds
        //output area bounds
        //suggestion button bounds
        //menu button bounds
    }

    private String UUID;

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

    private int fontMetric = Font.BOLD;

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

    private int fontSize = 30;

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
                    GenericInform.inform("Resized the background image \"" + currentFile.getName() + "\" since it was too big.",
                            "System Action");

                //while the image dimensions are greater than the screen dimensions,
                // divide the image dimensions by the the aspect ratio if it will result in a smaller number
                // if it won't then we divide by 1/aspectRatio which will result in a smaller number if the first did not
                while (backgroundWidth > screenWidth || backgroundHeight > screenHeight) {
                    backgroundWidth = (int) (backgroundWidth / ((aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio)));
                    backgroundHeight = (int) (backgroundHeight / ((aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio)));
                }

                //inform the user we are changing the size of the image
                if (backgroundWidth < 600 && backgroundHeight < 600)
                    GenericInform.inform("Resized the background image \"" + getBackgrounds().get(i).getName()
                            + "\" since it was too small.", "System Action");

                //while the image dimensions are less than 800x800, multiply the image dimensions by the
                // aspect ratio if it will result in a bigger number, if it won't, multiply it by 1.0 / aspectRatio
                // which will result in a number greater than 1.0 if the first option failed.
                while (backgroundWidth < 800 && backgroundHeight < 800) {
                    backgroundWidth = (int) (backgroundWidth * (aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio));
                    backgroundHeight = (int) (backgroundHeight * (aspectRatio < 1.0 ? 1.0 / aspectRatio : aspectRatio));
                }

                //todo test background switching with prime number width/height

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

    private LinkedList<File> backgroundFiles;

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
                ImageIO.write(bi, "png", new File("users/" + getUsername()
                        + "/Backgrounds/Default.png"));

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

    private int backgroundIndex;

    public int getBackgroundIndex() {
        return backgroundIndex;
    }

    public void setBackgroundIndex(int i) {
        backgroundIndex = i;
    }

    public void incBackgroundIndex() {
        backgroundIndex += 1;
    }

    public void decBackgroundIndex() {
        backgroundIndex -= 1;
    }

    private File backgroundFile;

    public File getCurrentBackgroundFile() {
        backgroundFile = backgroundFiles.get(backgroundIndex);
        return backgroundFile;
    }

    private ImageIcon backgroundImageIcon;

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
            if (backgroundIndex + 1 == backgroundFiles.size() - 1) {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(0)));
            } else {
                ret = new ImageIcon(ImageIO.read(backgroundFiles.get(backgroundFiles.size() + 1)));
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

    private Direction lastSlideDirection = Direction.TOP;

    //todo make the frame and drag label stay when switching backgrounds
    // and the image be separate (inside of consoleframe class)

    //todo: you kind of did this in login with the sliding text, then notification
    // will not go over it and only the background will slide
    // to do this, just have a backgroundLabel that you can slide in and out

    //todo make changing background animation no more than one second (so redo the method to calculate step)
    // make it also retain a console orientation when transitioning (both full screen or not full screen)

    //if this returns false then we didn't switch so we should tell the user they should add more backgrounds
    public boolean switchBackground() {
        try {
            //if we only have one background we can't switch
            if (!(backgroundFiles.size() > backgroundIndex + 1 && backgroundFiles.size() > 1))
                return false;

            ImageIcon oldBack = getCurrentBackgroundImageIcon();
            ImageIcon newBack = getNextBackgroundImageIcon();

            //get the dimensions which we will flip to, the next image
            int width = newBack.getIconWidth();
            int height = newBack.getIconHeight();

            //are we full screened and are we rotated?
            boolean fullscreen = IOUtil.getUserData("FullScreen").equalsIgnoreCase("1");
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

                    oldBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(oldBack), -90));
                    newBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(newBack), -90));
                }

                //not full screen and oriented right
                else if (direction == Direction.RIGHT) {
                    width = width + height;
                    height = width - height;
                    width = width - height;

                    oldBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(oldBack), 90));
                    newBack = new ImageIcon(ImageUtil.rotateImageByDegrees(ImageUtil.ImageIcon2BufferedImage(newBack), 90));
                }
            }

            //todo make sure console rotations are impossible in full screen

            //make master image to set to background and slide
            ImageIcon combinedIcon;

            //todo bug found, on logout, should reset console dir (will be fixed with cyderframe instances holding entire cyder instance essentially)
            //stop music and basically everything on close, (mp3 music continues)

            //todo before combining images, we need to make sure they're the same size, duhhhhh
            oldBack = ImageUtil.resizeImage(oldBack, width, height);
            newBack = ImageUtil.resizeImage(newBack, width, height);

            switch (lastSlideDirection) {
                case LEFT:
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.BOTTOM);
                    //todo set image bounds
                    //todo setbackground tod this new image
                    int[] delayInc = AnimationUtil.getDelayIncrement(height);
                    //new CyderAnimation().jLabelYUp(0, -height, 10, 10, iconLabel);
                    //todo slide up by height so init bounds are 0,height,width,height
                    //todo set actual icon to background

                    //rest all new bounds

                    lastSlideDirection = Direction.TOP;
                    break;

                case TOP:
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.LEFT);
                    //todo slide right by width so init bounds are -width,0,width,height

                    lastSlideDirection = Direction.RIGHT;
                    break;

                case RIGHT:
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.TOP);
                    //todo slide down by height so init bounds are 0,-height,width,height

                    lastSlideDirection = Direction.BOTTOM;
                    break;

                case BOTTOM:
                    combinedIcon = ImageUtil.combineImages(oldBack, newBack, Direction.RIGHT);
                    //todo slide left by width so init bounds are width,0,width,height

                    lastSlideDirection = Direction.LEFT;
                    break;
            }

            //todo change tooltip for background
            //todo refresh consoleclock bounds
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

    private boolean consoleClockEnabled;

    public void setConsoleClock(Boolean enable) {
        IOUtil.writeUserData("ClockOnConsole", enable ? "1" : "0");
        consoleClockEnabled = enable;

        if (enable) {
            setTitle("");
            //start up executor
        } else {
            setTitle("");
            //end executor task if running
        }
    }

    public boolean consoleClockEnabled() {
        return consoleClockEnabled;
    }

    private Direction consoleDir = Direction.TOP;

    public void setConsoleDirection(Direction conDir) {
        consoleDir = conDir;
        getConsoleFrame().repaint();
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
        ImageIcon masterIcon = (ImageIcon) ((JLabel) getConsoleFrame().getContentPane()).getIcon();
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
                    ((JLabel) getConsoleFrame().getContentPane()).setIcon(new ImageIcon(rotated));
                    return;
                }
                rotated = ImageUtil.rotateImageByDegrees(master, angle);
                ((JLabel) getConsoleFrame().getContentPane()).setIcon(new ImageIcon(rotated));
            }
        });
        timer.start();
    }

    private boolean fullscreen = false;

    public void setFullscreen(Boolean enable) {
        fullscreen = enable;

        //this should recalculate bounds for components depending on console direction and fullscreen mode
        repaint();
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    private int scrollingDowns;

    public int getScrollingDowns() {
        return scrollingDowns;
    }

    public void setScrollingDowns(int downs) {
        scrollingDowns = downs;
    }

    public void incScrollingDowns() {
        scrollingDowns += 1;
    }

    public void decScrollingDowns() {
        scrollingDowns -= 1;
    }

    public boolean onLastBackground() {
        initBackgrounds();
        return backgroundFiles.size() == backgroundIndex + 1;
    }

    public boolean canSwitchBackground() {
        return backgroundFiles.size() > backgroundIndex + 1;
    }
}
