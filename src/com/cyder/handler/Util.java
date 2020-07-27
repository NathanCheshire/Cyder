//package declaration
package com.cyder.handler;

import com.cyder.obj.NST;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderScrollPane;
import com.cyder.ui.CyderSliderUI;
import com.cyder.ui.DragLabel;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import javazoom.jl.player.Player;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Port;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    //public fonts
    public Font weatherFontSmall = new Font("Segoe UI Black", Font.BOLD, 20);
    public Font weatherFontBig = new Font("Segoe UI Black", Font.BOLD, 30);
    public Font loginFont = new Font("Comic Sans MS", Font.BOLD, 30);
    public Font defaultFontSmall = new Font("tahoma", Font.BOLD, 15);
    public Font buttonFont = new Font("Dialog Font", Font.BOLD, 15);
    public Font defaultFont = new Font("tahoma", Font.BOLD, 30);
    public Font tahoma = new Font("tahoma", Font.BOLD, 20);

    //public colors
    public Color selectionColor = new Color(204,153,0);
    public Color regularGreen = new Color(60, 167, 92);
    public Color calculatorOrange = new Color(255,140,0);
    public Color regularRed = new Color(223,85,83);
    public Color intellijPink = new Color(236,64,122);
    public Color consoleColor = new Color(39, 40, 34);
    public Color vanila = new Color(252, 251, 227);
    public Color tttblue = new Color(71, 81, 117);
    public Color navy = new Color(26, 32, 51);

    //Cyder direct vars
    private ImageIcon cyderTrayIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\cyderTrayIcon.png");
    private ImageIcon cyderIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\cyderIcon.png");
    private ImageIcon scaledCyderIcon = new ImageIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\cyderIcon.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));
    private String cyderVer = "Apple";

    //uservars
    private LinkedList<NST> userData = new LinkedList<>();
    private String userUUID;
    private String username;
    private Color usercolor;
    private Font userfont;
    private String os;
    private int currentBackgroundIndex = 0;
    private File[] validBackgroundPaths;
    private String userCity;
    private String userState;
    private String userStateAbr;
    private String isp;
    private String lat;
    private String lon;
    private String userCountry;
    private String userCountryAbr;
    private String userIP;
    private String userPostalCode;
    private String userFlagURL;
    private boolean debugButton;
    private boolean consoleClock;

    //weather vars
    private String sunrise;
    private String sunset;
    private String weatherIcon;
    private String weatherCondition;
    private String windSpeed;
    private String visibility;
    private String temperature;
    private String humidity;
    private String pressure;
    private String cloudCover;
    private String uvIndex;
    private String windBearing;

    //local tray vars
    private final TrayIcon trayIcon = new TrayIcon(cyderTrayIcon.getImage(), cyderVer + " [" + username + "]");
    private final SystemTray tray = SystemTray.getSystemTray();

    //screen and mouse vars
    private int screenX;
    private int screenY;
    private int xMouse;
    private int yMouse;

    //restore vars
    private int weatherRestoreY;
    private int weatherRestoreX;
    private int musicRestoreX;
    private int musicRestoreY;
    private int debugRestoreX;
    private int debugRestoreY;

    //debug vars
    private JFrame debugFrame;

    //pixel vars
    private JFrame pixelFrame;

    //minecraft vars
    private JFrame minecraftFrame;
    private JLabel realmsLabel;
    private JLabel chestLabel;
    private JLabel hamLabel;
    private JLabel blockLabel;
    private JLabel locationLabel;

    //weather vars
    private JLabel currentWeatherLabel;
    private JLabel temperatureLabel;
    private JLabel uvIndexLabel;
    private JLabel cloudCoverLabel;
    private JLabel windSpeedLabel;
    private JLabel windDirectionLabel;
    private JLabel humidityLabel;
    private JLabel pressureLabel;
    private JLabel sunsetLabel;
    private JLabel sunriseLabel;
    private JLabel visibilityLabel;

    //media vars
    private JFrame mediaFrame;

    private JButton closeDebug;
    private JLabel displayLabel;
    private JFrame weatherFrame;
    private JButton closeWeather;
    private JButton minimizeWeather;
    private JLabel currentTimeLabel;

    //music vars
    private ScrollLabel musicScroll;
    private JFrame musicFrame;
    private JSlider musicVolumeSlider;
    private JButton selectMusicDir;
    private JButton playPauseMusic;
    private boolean musicStopped;
    private JButton lastMusic;
    private JButton nextMusic;
    private JButton stopMusic;
    private JButton loopMusic;
    private JButton closeMusic;
    private JLabel musicTitleLabel;
    private JLabel musicVolumeLabel;
    private int currentMusicIndex;
    private File[] musicFiles;
    private Player mp3Player;
    private Player player;
    private BufferedInputStream bis;
    private FileInputStream fis;
    private long pauseLocation;
    private long songTotalLength;
    private boolean playIcon = true;
    private boolean repeatAudio;

    //update vars
    private boolean userInputMode;
    private boolean updateWeather;
    private boolean updateClock;

    //boolean vars
    private boolean debugMode;
    private boolean mathShellMode;
    private boolean hideOnClose;
    private boolean oneMathPrint;
    private boolean alwaysOnTop;

    //phone vars
    private JFrame phoneFrame;
    private JLabel numberLabel;
    private String phoneNum;

    //calculator vars
    private JFrame calculatorFrame;
    private JTextField calculatorField;
    private String calculatorExpression = "";

    //pizza vars
    private JFrame pizzaFrame;
    private JTextField enterNameField;
    private JTextField enterName;
    private JRadioButton medium;
    private JRadioButton small;
    private JRadioButton large;
    private JList<?> pizzaTopingsList;
    private JComboBox<?> pizzaCrustCombo;
    private JTextArea orderCommentsTextArea;
    private JCheckBox breadSticks;
    private JCheckBox salad;
    private JCheckBox soda;

    //scrolling var
    private int currentDowns;

    //note vars
    private JFrame noteEditorFrame;
    private JTextArea noteEditArea;
    private JTextField noteEditField;
    private File currentUserNote;
    private JFrame newNoteFrame;
    private JTextField newNoteField;
    private JTextArea newNoteArea;
    private JFrame noteFrame;
    private CyderScrollPane noteListScroll;
    private JList<?> fileSelectionList;
    private List<String> noteNameList;
    private List<File> noteList;
    private CyderButton openNote;

    //drawing vars
    private JFrame pictureFrame;
    private CyderButton closeDraw;

    //temperature vars
    private JFrame temperatureFrame;
    private JTextField startingValue;
    private JRadioButton oldFahrenheit;
    private JRadioButton newFahrenheit;
    private JRadioButton oldCelsius;
    private JRadioButton newCelsius;
    private JRadioButton oldKelvin;
    private JRadioButton newKelvin;
    private ButtonGroup radioNewValueGroup;
    private ButtonGroup radioCurrentValueGroup;

    //click me var
    private JFrame clickMeFrame;

    //youtube thumbnail vars
    private JFrame yttnFrame;
    private CyderButton getYTTN;
    private JTextField yttnField;

    //backbround vars
    private int backgroundX;
    private int backgroundY;

    //hash vars
    private JFrame hashFrame;
    private JPasswordField hashField;

    //custom dir searc hvars
    private JFrame dirFrame;
    private JTextField dirField;
    private CyderScrollPane dirScroll;
    private JList<?> directoryNameList;
    private JList<?> directoryList;
    private JPanel dirSearchParentPanel;

    private JFrame informFrame;

    //used for second handle
    private String userInputDesc;

    public Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    public boolean getUserInputMode() {
        return this.userInputMode;
    }

    public void setUserInputMode(boolean b) {
        this.userInputMode = b;
    }

    public String getUserInputDesc() {
        return this.userInputDesc;
    }

    public void setUserInputDesc(String s) {
        this.userInputDesc = s;
    }

    private int getScreenWidth() {
        this.screenX = this.getScreenSize().width;
        return this.screenX;
    }

    private int getScreenHeight() {
        this.screenY = this.getScreenSize().height;
        return this.screenY;
    }

    public void dance(JFrame frame) {
        Thread DanceThread = new Thread(() -> {
            try {
                int delay = 10;

                frame.setAlwaysOnTop(true);
                frame.setLocationRelativeTo(null);

                Point point = frame.getLocationOnScreen();

                int x = (int) point.getX();
                int y = (int) point.getY();

                int restoreX = x;
                int restoreY = y;

                for (int i = y; i <= (-frame.getHeight()); i += 10) {
                    Thread.sleep(delay);
                    frame.setLocation(x, i);
                }

                frame.setLocation(screenX / 2 - frame.getWidth() / 2, screenY - frame.getHeight());
                point = frame.getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i <= (screenX - frame.getWidth()); i += 10) {
                    Thread.sleep(delay);
                    frame.setLocation(i, y);
                }

                frame.setLocation(screenX - frame.getWidth(), screenY - frame.getHeight());
                point = frame.getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = y; i >= -10; i -= 10) {
                    Thread.sleep(delay);
                    frame.setLocation(x, i);
                }

                frame.setLocation(screenX - frame.getWidth(), 0);
                point = frame.getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i >= 10; i -= 10) {
                    Thread.sleep(delay);
                    frame.setLocation(i, y);
                }

                frame.setLocation(0, 0);
                point = frame.getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = y; i <= (screenY - frame.getHeight()); i += 10) {
                    Thread.sleep(delay);
                    frame.setLocation(x, i);
                }

                frame.setLocation(0, screenY - frame.getHeight());
                point = frame.getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i <= (screenX / 2 - frame.getWidth() / 2); i += 10) {
                    Thread.sleep(delay);
                    frame.setLocation(i, y);
                }

                frame.setLocation(screenX / 2 - frame.getWidth() / 2, screenY - frame.getHeight());
                int acc = frame.getY();
                x = frame.getX();

                while (frame.getY() >= (screenY / 2 - frame.getHeight() / 2)) {
                    Thread.sleep(delay);
                    acc -= 10;
                    frame.setLocation(x, acc);
                }

                frame.setLocation(restoreX, restoreY);
                frame.setAlwaysOnTop(false);

            }

            catch (Exception e) {
                handle(e);
            }
        });

        DanceThread.start();
    }

    public String errorTime() {
        DateFormat dateFormat = new SimpleDateFormat("MMddyy-HH-mmaa");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public String userTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEEEEE, MM/dd/yyyy hh:mmaa zzz");
        return dateFormatter.format(Time);
    }

    public String consoleTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEEEEE hh:mmaa");
        return dateFormatter.format(Time);
    }


    public String weatherTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ss aa zzz EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
        return dateFormatter.format(Time);
    }

    public String weatherThreadTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:maa zzz EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
        return dateFormatter.format(Time);
    }

    public void closeAnimation(JFrame frame) {
        try {
            if (frame != null && frame.isVisible()) {
                Point point = frame.getLocationOnScreen();
                int x = (int) point.getX();
                int y = (int) point.getY();

                for (int i = y; i >= 0 - frame.getHeight(); i -= 15) {
                    Thread.sleep(1);
                    frame.setLocation(x, i);
                }
            }
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public void minimizeAnimation(JFrame frame) {
        Point point = frame.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        try {
            for (int i = y; i <= getScreenHeight(); i += 15) {
                Thread.sleep(1);
                frame.setLocation(x, i);
            }

            frame.setState(JFrame.ICONIFIED);
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public void internetConnect(String URL) {
        Desktop Internet = Desktop.getDesktop();
        try {
            Internet.browse(new URI(URL));
        } catch (Exception ex) {
            handle(ex);
        }
    }

    public void internetConnect(URI URI) {
        Desktop Internet = Desktop.getDesktop();
        try {
            Internet.browse(URI);
        } catch (Exception ex) {
            handle(ex);
        }
    }

    public void minecraftWidget() {
        if (minecraftFrame != null) {
            closeAnimation(minecraftFrame);
            minecraftFrame.dispose();
        }

        minecraftFrame = new JFrame();

        minecraftFrame.setTitle("Minecraft Widget");

        minecraftFrame.setSize(1263, 160);

        minecraftFrame.setUndecorated(true);

        minecraftFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        minecraftFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        JLabel minecraftLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Minecraft.png"));

        minecraftFrame.setContentPane(minecraftLabel);

        DragLabel minecraftDragLabel = new DragLabel(1263,27,minecraftFrame);

        minecraftDragLabel.setBounds(0, 0, 1263, 27);

        minecraftLabel.add(minecraftDragLabel);

        blockLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Block.png"));

        blockLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                internetConnect("https://my.minecraft.net/en-us/store/minecraft/");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\BlockEnter.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\BlockExit.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }
        });

        blockLabel.setBounds(83, 46, 50, 45);

        minecraftLabel.add(blockLabel);

        realmsLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Realms.png"));

        realmsLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                internetConnect("https://minecraft.net/en-us/realms/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\RealmsEnter.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\RealmsExit.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }
        });

        realmsLabel.setBounds(196, 51, 70, 45);

        minecraftLabel.add(realmsLabel);

        chestLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Chest.png"));

        chestLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                internetConnect("https://minecraft.net/en-us/store/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\ChestEnter.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\ChestExit.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }
        });

        chestLabel.setBounds(1009, 44, 60, 50);

        minecraftLabel.add(chestLabel);

        hamLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Hamburger.png"));

        hamLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                internetConnect("https://minecraft.net/en-us/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\HamburgerEnter.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\HamburgerExit.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }
        });

        hamLabel.setBounds(1135, 52, 42, 40);

        minecraftLabel.add(hamLabel);

        minecraftFrame.setVisible(true);

        getScreenSize();

        minecraftFrame.setLocation(screenX / 2 - 1263 / 2, screenY - 240);

        minecraftFrame.setAlwaysOnTop(true);

        minecraftFrame.setResizable(false);

        minecraftFrame.setIconImage(new ImageIcon("src\\com\\cyder\\io\\pictures\\Block.png").getImage());
    }

    public ImageIcon getCyderTrayIcon() {
        return this.cyderTrayIcon;
    }

    public ImageIcon getCyderIcon() {
        return this.cyderIcon;
    }

    public ImageIcon getScaledCyderIcon() {return this.scaledCyderIcon;}

    public String getCyderVer() {
        return this.cyderVer;
    }

    public Color getAColor(String Title) {
        JColorChooser ColorChooser = new JColorChooser();
        ColorChooser.setFont(tahoma);

        Color ReturnColor;
        JFrame bodgeFrame = new JFrame();
        bodgeFrame.setIconImage(getCyderIcon().getImage());

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

            SwingUtilities.updateComponentTreeUI(bodgeFrame);
        }

        catch (Exception e) {
            handle(e);
        }

        ReturnColor = ColorChooser.showDialog(bodgeFrame, Title, vanila);

        return (ReturnColor == null ? getUsercolor() : ReturnColor);
    }

    public String rgbtohex(int r, int g, int b) {
        return String.format("#%02x%02x%02x", r, g, b);
    }

    public String rgbtohex(Color Color) {
        return String.format("#%02x%02x%02x", Color.getRed(), Color.getGreen(), Color.getBlue());
    }

    public String fillString(int count, String c) {
        StringBuilder sb = new StringBuilder(count);

        for (int i = 0; i < count; i++) {
            sb.append(c);
        }

        return sb.toString();
    }

    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public File getFile() {
        try {
            File WhereItIs = new File("src\\com\\cyder\\io\\jars\\FileChooser.jar");
            Desktop.getDesktop().open(WhereItIs);
            File f = new File("File.txt");
            f.delete();
            while (!f.exists()) {}
            Thread.sleep(200);

            BufferedReader waitReader = new BufferedReader(new FileReader("File.txt"));
            File chosenFile = new File(waitReader.readLine());
            f.delete();
            waitReader.close();

            return (chosenFile.getName().equalsIgnoreCase("null") ? null : chosenFile);
        }

        catch (Exception e) {
            handle(e);
        }

        return null;
    }

    public String getMACAddress() {
        byte[] MAC = null;

        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface NI = NetworkInterface.getByInetAddress(address);
            MAC = NI.getHardwareAddress();
        } catch (Exception e) {
            handle(e);
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < MAC.length; i++) {
            sb.append(String.format("%02X%s", MAC[i], (i < MAC.length - 1) ? "-" : ""));
        }

        return sb.toString();
    }

    public boolean compMACAddress(String mac) {
        return toHexString(getSHA(mac.toCharArray())).equals("5c486915459709261d6d9af79dd1be29fea375fe59a8392f64369d2c6da0816e");
    }

    private String getWindowsUsername() {
        return System.getProperty("user.name");
    }

    private String getOS() {
        return System.getProperty("os.name");
    }

    public boolean released() {
        return false;
    }

    public void varInit() {
        String windowsUserName = getWindowsUsername();
        this.os = getOS();
        getLocation();
        this.screenX = getScreenWidth();
        this.screenY = getScreenHeight();
    }

    private String getComputerName() {
        String name = null;

        try {
            InetAddress Add = InetAddress.getLocalHost();
            name = Add.getHostName();
        } catch (Exception e) {
            handle(e);
        }

        return name;
    }

    public void getLocation() {
        try {
            String Key = "https://api.ipdata.co/?api-key=8eac4e7ab34eb235c4a888bfdbedc8bb8093ec1490790d139cf58932";

            URL Querry = new URL(Key);

            BufferedReader BR = new BufferedReader(new InputStreamReader(Querry.openStream()));

            String CurrentLine;

            while ((CurrentLine = BR.readLine()) != null) {
                if (CurrentLine.contains("city")) {
                    userCity = (CurrentLine.replace("city", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"region\"")) {
                    userState = (CurrentLine.replace("region", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"region_code\"")) {
                    userStateAbr = (CurrentLine.replace("region_code", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("asn")) {
                    CurrentLine = BR.readLine();
                    CurrentLine = BR.readLine();
                    isp = (CurrentLine.replace("name", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"country_name\"")) {
                    userCountry = (CurrentLine.replace("country_name", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"country_code\"")) {
                    userCountryAbr = (CurrentLine.replace("country_code", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"latitude\"")) {
                    lat = (CurrentLine.replace("latitude", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"longitude\"")) {
                    lon = (CurrentLine.replace("longitude", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"ip\"")) {
                    userIP = (CurrentLine.replace("ip", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"flag\"")) {
                    userFlagURL = (CurrentLine.replace("\"flag\"", "").replace("\"","").replace(",", "").trim()).replaceFirst(":","");
                }

                else if (CurrentLine.contains("postal")) {
                    userPostalCode = (CurrentLine.replace("\"postal\"", "").replace("\"","").replace(",", "").replace(":", "").trim());
                }
            }
            BR.close();
        } catch (Exception e) {
            handle(e);
        }
    }

    private boolean siteReachable(String URL) {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 " + URL);
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return false;
            }
        }

        catch (Exception e) {
            handle(e);
        }

        return true;
    }

    private int latency() {
        Socket Sock = new Socket();
        SocketAddress Address = new InetSocketAddress("www.google.com", 80);
        int Timeout = 2000;
        long start = System.currentTimeMillis();

        try {
            Sock.connect(Address, Timeout);
        } catch (Exception e) {
            handle(e);
        }

        long stop = System.currentTimeMillis();
        int Latency = (int) (stop - start);

        try {
            Sock.close();
        } catch (Exception e) {
            handle(e);
        }

        return Latency;
    }

    private String phoneNumFormat(String num) {
        num = num.replaceAll("[^\\d.]", "");
        int len = num.length();

        if (len == 0) {
            return "#";
        }

        else if (len > 0 && len <= 4) {
            return num;
        }

        else if (len == 5) {
            return (num.substring(0,1) + "-" + num.substring(1,len));
        }

        else if (len == 6) {
            return (num.substring(0,2) + "-" + num.substring(2,len));
        }

        else if (len == 7) {
            return (num.substring(0,3) + "-" + num.substring(3,len));
        }

        else if (len == 8) {
            return ("(" + num.substring(0,1) + ") " + num.substring(1,4) + " " + num.substring(4,len));
        }

        else if (len == 9) {
            return ("(" + num.substring(0,2) + ") " + num.substring(2,5) + " " + num.substring(5,len));
        }

        else if (len == 10) {
            return ("(" + num.substring(0,3) + ") " + num.substring(3,6) + " " + num.substring(6,len));
        }

        else if (len > 10) {
            String leadingDigits = num.substring(0, len - 10);
            int offset = leadingDigits.length();

            return (leadingDigits + " (" + num.substring(offset,3 + offset) + ") " + num.substring(3 + offset,6 + offset) + " " + num.substring(6 + offset,len));
        }

        else {
            return null;
        }
    }

    public void debug() {
        try {
            if (debugFrame != null) {
                closeAnimation(debugFrame);
                debugFrame.dispose();
            }

            debugFrame = new JFrame();

            debugFrame.setTitle("debug Menu");

            debugFrame.setSize(1350, 900);

            debugFrame.setUndecorated(true);

            debugFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JLabel debugLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\DebugBackground.png"));

            debugFrame.setContentPane(debugLabel);

            DragLabel debugDragLabel = new DragLabel(1350,27,debugFrame);

            debugDragLabel.setBounds(0, 0, 1350, 27);

            debugLabel.add(debugDragLabel);

            displayLabel = new JLabel("", SwingConstants.LEFT);

            displayLabel.setFont(new Font("Segoe UI Black", Font.BOLD, 22));

            displayLabel.setForeground(new Color(50,50,100));

            DecimalFormat GigaFormater = new DecimalFormat("##.###");

            double GigaBytes = ((double) Runtime.getRuntime().freeMemory()) / 1024 / 1024 / 1024;

            InetAddress address = InetAddress.getLocalHost();

            NetworkInterface NI = NetworkInterface.getByInetAddress(address);

            getLocation();

            BufferedImage flag = ImageIO.read(new URL(getUserFlag()));

            JLabel FlagLabel = new JLabel(new ImageIcon(flag));

            debugLabel.add(FlagLabel);

            FlagLabel.setBounds(debugFrame.getWidth() - 2 * flag.getWidth(),
                    (debugFrame.getHeight() - flag.getHeight()) / 2 , flag.getWidth(), flag.getHeight());

            displayLabel.setText("<html>" + "Time requested: " + weatherTime() + "<br/>ISP: " + getUserISP() + "<br/>IP: " + userIP +
                    "<br/>Postal Code: " + getUserPostalCode() + "<br/>City: " + userCity + "<br/>State: "
                    + userState + "<br/>Country: " + userCountry + " (" + userCountryAbr + ")"
                    + "<br/>Latitude: " + lat + " Degrees N<br/>Longitude: " + lon + " Degrees W<br/>latency: " + latency() + " ms<br/>Google Reachable: "
                    + siteReachable("https://www.google.com") + "<br/>YouTube Reachable: " + siteReachable("https://www.youtube.com") + "<br/>Apple Reachable: "
                    + siteReachable("https://www.apple.com") + "<br/>Microsoft Reachable: " + siteReachable("https://www.microsoft.com//en-us//")
                    + "<br/>User Name: " + getWindowsUsername() + "<br/>Computer Name: " + getComputerName() + "<br/>Available Cores: " + Runtime.getRuntime().availableProcessors()
                    + "<br/>Available Memory: " + Runtime.getRuntime().freeMemory() + " Bytes [" + GigaFormater.format(GigaBytes) + " GigaBytes]<br/>Operating System: "
                    + os + "<br/>Java Version: " + System.getProperty("java.version") + "<br/>Network Interface Name: " + NI.getName() + "<br/>NI Display Name: "
                    + NI.getDisplayName() + "<br/>Network MTU: " + NI.getMTU() + "<br/>Host Address: " + address.getHostAddress() + "<br/>Local Host Address: "
                    + address.getLocalHost() + "<br/>Loopback Address: " + address.getLoopbackAddress() + "</html>");

            displayLabel.setFocusable(true);

            displayLabel.setToolTipText("Click to copy stats to clipboard");

            displayLabel.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {

                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    StringSelection selection = new StringSelection(displayLabel.getText());
                    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

            debugLabel.add(displayLabel);

            displayLabel.setBounds(50, 50, 1300, 850);

            debugFrame.setVisible(true);

            debugFrame.setLocationRelativeTo(null);

            debugFrame.setAlwaysOnTop(true);

            debugFrame.setAlwaysOnTop(false);

            debugFrame.setResizable(false);

            debugFrame.setIconImage(getCyderIcon().getImage());
        } catch (Exception e) {
            //handle(e);
            e.printStackTrace();
        }
    }

    private String[] combineArrays(String[] a, String[] b) {
        int length = a.length + b.length;
        String[] result = new String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private void refreshWeather() {
        Thread WeatherThread = new Thread(() -> {
            try {
                while (updateWeather) {
                    Thread.sleep(1800000);

                    locationLabel.setText(userCity + ", " + userStateAbr);

                    currentWeatherLabel.setText(capsFirst(weatherCondition));

                    temperatureLabel.setText("temperature: " + temperature + "F");

                    uvIndexLabel.setText("UV Index: " + uvIndex);

                    cloudCoverLabel.setText("Cloud Cover: " + cloudCover);

                    windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");

                    windDirectionLabel.setText("Wind Direction: " + windBearing + ", " + getWindDirection());

                    humidityLabel.setText("Humidity: " + humidity + "%");

                    pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");

                    visibilityLabel.setText("visibility: " + Double.parseDouble(visibility) / 1000 + "mi");

                    sunriseLabel.setText(sunrise + "am");

                    sunsetLabel.setText(sunset + "pm");
                }
            } catch (Exception e) {
                handle(e);
            }
        });

        WeatherThread.start();
    }

    private void weatherStats() {
        try {
            initWeatherVars();

            String DarkString = "https://api.darksky.net/forecast/3fc1a7e3f22eb67764c7dc222144bbe0/" + lat + "," + lon + "?exclude=currently,minutely,daily,alerts,flags";

            URL URL = new URL(DarkString);
            BufferedReader WeatherReader = new BufferedReader(new InputStreamReader(URL.openStream()));
            String PureJSON = WeatherReader.readLine().split("}")[0] + "}]}}";
            PureJSON = PureJSON.split("data\":")[1].replace("[", "");
            PureJSON = PureJSON.split("},")[0];
            PureJSON = PureJSON.replace("}", "").replace("{", "").replace("]", "").replace("\"", "");
            String[] parts = PureJSON.split(",");

            for (String part : parts) {
                if (part.contains("temperature") && !part.contains("apparent")) {
                    temperature = part.replace("temperature:", "");
                } else if (part.contains("summary")) {
                    String weatherSummary = part.replace("summary:", "");
                } else if (part.contains("pressure")) {
                    pressure = part.replace("pressure:", "");
                    pressure = pressure.substring(0, Math.min(pressure.length(), 4));
                } else if (part.contains("humidity")) {
                    humidity = part.replace("humidity:", "").replace("0.", "");
                } else if (part.contains("cloudCover")) {
                    cloudCover = part.replace("cloudCover:", "").replace("0.", "") + "%";
                } else if (part.contains("uvIndex")) {
                    uvIndex = part.replace("uvIndex:", "");
                }
            }
        } catch (Exception e) {
            handle(e);
        }
    }

    private void initWeatherVars() {
        try {
            getLocation();

            String Key = "2d790dd0766f1da62af488f101380c75";

            String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" + userCity + "&appid=" + Key + "&units=imperial";

            URL URL = new URL(OpenString);

            BufferedReader WeatherReader = new BufferedReader(new InputStreamReader(URL.openStream()));

            String[] Fields = {"", ""};

            String Line;

            while ((Line = WeatherReader.readLine()) != null) {
                String[] LineArray = Line.replace("{", "").replace("}", "")
                        .replace(":", "").replace("\"", "").replace("[", "")
                        .replace("]", "").replace(":", "").split(",");

                Fields = combineArrays(Fields, LineArray);
            }

            WeatherReader.close();

            for (String field : Fields) {
                if (field.contains("sunrise")) {
                    sunrise = field.replaceAll("[^\\d.]", "");
                } else if (field.contains("sunset")) {
                    sunset = field.replaceAll("[^\\d.]", "");
                } else if (field.contains("icon")) {
                    weatherIcon = field.replace("icon", "");
                } else if (field.contains("speed")) {
                    windSpeed = field.replaceAll("[^\\d.]", "");
                } else if (field.contains("deg")) {
                    windBearing = field.replaceAll("[^\\d.]", "");
                } else if (field.contains("description")) {
                    weatherCondition = field.replace("description", "");
                } else if (field.contains("visibility")) {
                    visibility = field.replaceAll("[^\\d.]", "");
                }
            }

            SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm");

            Date SunriseTime = new Date((long) Integer.parseInt(sunrise) * 1000);
            sunrise = dateFormatter.format(SunriseTime);

            Date SunsetTime = new Date((long) Integer.parseInt(sunset) * 1000);
            sunset = dateFormatter.format(SunsetTime);

            Date Time = new Date();

            if (Time.getTime() > SunsetTime.getTime()) {
                weatherIcon = weatherIcon.replace("d", "n");
            }
        } catch (Exception e) {
            handle(e);
        }
    }

    private String getWindDirection() {
        initWeatherVars();

        double bear = Double.parseDouble(windBearing);

        if (bear == 0) {
            return "N";
        } else if (bear == 90) {
            return "E";
        } else if (bear == 180) {
            return "S";
        } else if (bear == 270) {
            return "W";
        } else if (bear > 0 && bear < 90) {
            return "NE";
        } else if (bear > 90 && bear < 180) {
            return "SE";
        } else if (bear > 180 && bear < 270) {
            return "SW";
        } else if (bear > 270 && bear < 360) {
            return "NW";
        }
        return null;
    }

    private String capsFirst(String Word) {
        StringBuilder SB = new StringBuilder(Word.length());
        String[] Words = Word.split(" ");

        for (String word : Words) {
            SB.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }

        return SB.toString();
    }

    public void weatherWidget() {
        weatherStats();

        getLocation();

        if (weatherFrame != null) {
            closeAnimation(weatherFrame);
            weatherFrame.dispose();
        }

        weatherFrame = new JFrame();

        weatherFrame.setTitle("Weather");

        weatherFrame.setSize(1080, 608);

        weatherFrame.setUndecorated(true);

        weatherFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                weatherFrame.setLocation(weatherRestoreX, weatherRestoreY);
                weatherFrame.setVisible(true);
            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        weatherFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel weatherLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Weather.png"));

        weatherFrame.setContentPane(weatherLabel);

        DragLabel weatherDragLabel = new DragLabel(1080,24, weatherFrame);

        weatherDragLabel.setBounds(0, 0, 1080, 24);

        weatherLabel.add(weatherDragLabel);

        currentTimeLabel = new JLabel();

        currentTimeLabel.setForeground(vanila);

        currentTimeLabel.setFont(weatherFontSmall);

        currentTimeLabel.setBounds(16, 57, 600, 30);

        currentTimeLabel.setText(weatherTime());

        weatherLabel.add(currentTimeLabel, SwingConstants.CENTER);

        locationLabel = new JLabel();

        locationLabel.setForeground(vanila);

        locationLabel.setFont(weatherFontSmall);

        locationLabel.setBounds(16, 113, 300, 30);

        locationLabel.setText(userCity + ", " + userStateAbr);

        weatherLabel.add(locationLabel, SwingConstants.CENTER);

        JLabel currentWeatherIconLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\" + weatherIcon + ".png"));

        currentWeatherIconLabel.setBounds(175, 100, 100, 100);

        weatherLabel.add(currentWeatherIconLabel);

        currentWeatherLabel = new JLabel();

        currentWeatherLabel.setForeground(vanila);

        currentWeatherLabel.setFont(weatherFontSmall);

        currentWeatherLabel.setBounds(16, 170, 250, 20);

        currentWeatherLabel.setText(capsFirst(weatherCondition));

        weatherLabel.add(currentWeatherLabel);

        temperatureLabel = new JLabel();

        temperatureLabel.setForeground(vanila);

        temperatureLabel.setFont(weatherFontSmall);

        temperatureLabel.setBounds(16, 270, 300, 30);

        temperatureLabel.setText("temperature: " + temperature + "F");

        weatherLabel.add(temperatureLabel);

        uvIndexLabel = new JLabel();

        uvIndexLabel.setForeground(vanila);

        uvIndexLabel.setFont(weatherFontSmall);

        uvIndexLabel.setBounds(16, 310, 200, 30);

        uvIndexLabel.setText("UV Index: " + uvIndex);

        weatherLabel.add(uvIndexLabel);

        cloudCoverLabel = new JLabel();

        cloudCoverLabel.setForeground(vanila);

        cloudCoverLabel.setFont(weatherFontSmall);

        cloudCoverLabel.setBounds(16, 350, 200, 30);

        cloudCoverLabel.setText("Cloud Cover: " + cloudCover);

        weatherLabel.add(cloudCoverLabel);

        windSpeedLabel = new JLabel();

        windSpeedLabel.setForeground(vanila);

        windSpeedLabel.setFont(weatherFontSmall);

        windSpeedLabel.setBounds(16, 390, 300, 30);

        windSpeedLabel.setText("Wind Speed: " + windSpeed + "mph");

        weatherLabel.add(windSpeedLabel);

        windDirectionLabel = new JLabel();

        windDirectionLabel.setForeground(vanila);

        windDirectionLabel.setFont(weatherFontSmall);

        windDirectionLabel.setBounds(16, 430, 300, 30);

        windDirectionLabel.setText("Wind Direction: " + windBearing + ", " + getWindDirection());

        weatherLabel.add(windDirectionLabel);

        humidityLabel = new JLabel();

        humidityLabel.setForeground(vanila);

        humidityLabel.setFont(weatherFontSmall);

        humidityLabel.setBounds(16, 470, 300, 30);

        humidityLabel.setText("Humidity: " + humidity + "%");

        weatherLabel.add(humidityLabel, SwingConstants.CENTER);

        pressureLabel = new JLabel();

        pressureLabel.setForeground(vanila);

        pressureLabel.setFont(weatherFontSmall);

        pressureLabel.setBounds(16, 510, 300, 30);

        pressureLabel.setText("Pressure: " + Double.parseDouble(pressure) / 1000 + "atm");

        weatherLabel.add(pressureLabel, SwingConstants.CENTER);

        visibilityLabel = new JLabel();

        visibilityLabel.setForeground(vanila);

        visibilityLabel.setFont(weatherFontSmall);

        visibilityLabel.setBounds(16, 550, 300, 30);

        visibilityLabel.setText("visibility: " + Double.parseDouble(visibility) / 1000 + "mi");

        weatherLabel.add(visibilityLabel, SwingConstants.CENTER);

        sunriseLabel = new JLabel();

        sunriseLabel.setForeground(vanila);

        sunriseLabel.setFont(weatherFontSmall);

        sunriseLabel.setBounds(825, 517, 125, 30);

        sunriseLabel.setText(sunrise + "am");

        weatherLabel.add(sunriseLabel, SwingConstants.CENTER);

        sunsetLabel = new JLabel();

        sunsetLabel.setForeground(vanila);

        sunsetLabel.setFont(weatherFontSmall);

        sunsetLabel.setBounds(950, 519, 120, 30);

        sunsetLabel.setText(sunset + "pm");

        weatherLabel.add(sunsetLabel, SwingConstants.CENTER);

        weatherFrame.setVisible(true);

        weatherFrame.setLocationRelativeTo(null);

        weatherFrame.setAlwaysOnTop(true);

        weatherFrame.setAlwaysOnTop(false);

        weatherFrame.setResizable(false);

        weatherFrame.setIconImage(getCyderIcon().getImage());

        updateClock = true;

        refreshClock();

        updateWeather = true;

        refreshWeather();
    }

    private void refreshClock() {
        Thread TimeThread = new Thread(() -> {
            try {
                while (updateClock) {
                    Thread.sleep(1000);
                    currentTimeLabel.setText(weatherTime());
                }
            } catch (Exception e) {
                handle(e);
            }
        });

        TimeThread.start();
    }

    public void resetMouse() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int centerX = screenSize.width / 2;
            int centerY = screenSize.height / 2;
            Robot Rob = new Robot();
            Rob.mouseMove(centerX, centerY);
        } catch (Exception ex) {
            handle(ex);
        }
    }

    public byte[] getSHA(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(new String(input).getBytes(StandardCharsets.UTF_8));
        }

        catch (Exception ex) {
            handle(ex);
        }

        return null;
    }

    public String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public String generateUUID() {
        try {
            MessageDigest salt =MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            return UUID.nameUUIDFromBytes(salt.digest()).toString();
        }

        catch (Exception e) {
            handle(e);
        }

        return null;
    }

    public boolean checkPassword(String name, String pass) {
        try {
            File[] users = new File("src\\com\\cyder\\io\\users").listFiles();
            LinkedList<File> userDataFiles = new LinkedList<>();

            for (File f : users) {
                if (!f.getName().contains("DeprecatedUser")) {
                    userDataFiles.add(new File(f.getAbsolutePath() + "\\Userdata.txt"));
                }
            }

            for (int i = 0 ; i < userDataFiles.size() ; i++) {
                BufferedReader currentRead = new BufferedReader(new FileReader(userDataFiles.get(i)));

                String filename = null;
                String filepass = null;
                String Line = currentRead.readLine();

                while (Line != null) {
                    String[] parts = Line.split(":");

                    if (parts[0].equalsIgnoreCase("Name")) {
                        filename = parts[1];
                    } else if (parts[0].equalsIgnoreCase("Password")) {
                        filepass = parts[1];
                    }

                    Line = currentRead.readLine();
                }

                if (pass.equals(filepass) && name.equals(filename)) {
                    setUserUUID(users[i].getName());
                    setUsername(name);
                    return true;
                }
            }
        }

        catch (Exception e) {
            handle(e);
        }

        return false;
    }

    public String firstNumber(String Search) {
        Pattern Pat = Pattern.compile("\\d+");
        Matcher m = Pat.matcher(Search);
        return m.find() ? m.group() : null;
    }

    public void inform(String message, String title, int width, int height) {
        try {
            if (informFrame != null) {
                closeAnimation(informFrame);
                informFrame.dispose();
            }

            informFrame = new JFrame();

            informFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            informFrame.setTitle(title);

            informFrame.setSize(width, height);

            informFrame.setUndecorated(true);

            JLabel consoleLabel = new JLabel(new ImageIcon(resizeImage(width, height, new File("src\\com\\cyder\\io\\pictures\\InformBackground.png"))));

            informFrame.setContentPane(consoleLabel);

            informFrame.addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getXOnScreen();
                    int y = e.getYOnScreen();

                    if (informFrame != null && informFrame.isFocused()) {
                        informFrame.setLocation(x - xMouse, y - yMouse);
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    xMouse = e.getX();
                    yMouse = e.getY();
                }
            });

            JLabel desc = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");

            desc.setHorizontalAlignment(JLabel.CENTER);
            desc.setVerticalAlignment(JLabel.CENTER);

            desc.setForeground(vanila);

            desc.setFont(weatherFontSmall);

            desc.setBounds(10, 10, width - 20, height - 100);

            JLabel dismiss = new JLabel("Dismiss");

            dismiss.setHorizontalAlignment(JLabel.CENTER);
            dismiss.setVerticalAlignment(JLabel.CENTER);

            dismiss.setForeground(vanila);

            dismiss.setFont(weatherFontBig);

            dismiss.setBounds(20, height - 70, width - 20, 40);

            dismiss.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {

                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    closeAnimation(informFrame);
                    informFrame.dispose();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    dismiss.setForeground(regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dismiss.setForeground(vanila);
                }
            });

            consoleLabel.add(desc);

            consoleLabel.add(dismiss);

            consoleLabel.setBorder(new LineBorder(navy,5,false));

            informFrame.setVisible(true);

            informFrame.setLocationRelativeTo(null);

            informFrame.setAlwaysOnTop(true);

            informFrame.setResizable(false);

            informFrame.setIconImage(getCyderIcon().getImage());
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public String firstWord(String Word) {
        String[] sentences = Word.split(" ");
        return sentences[0];
    }

    public String toBinary(int value) {
        String binaryResult;

        if (value > 0) {
            int columnExponent = 0;
            int remainingValue = value;

            while (Math.pow(2, columnExponent) <= value) {
                columnExponent = columnExponent + 1;
            }

            String binaryResultUse = "";

            do {
                columnExponent = columnExponent - 1;
                int columnWeight = (int) Math.pow(2, columnExponent);

                if (columnWeight <= remainingValue) {
                    binaryResultUse = binaryResultUse + "1";
                    remainingValue = remainingValue - columnWeight;
                } else {
                    binaryResultUse = binaryResultUse + "0";
                }
            }

            while (columnExponent > 0);

            return binaryResultUse;
        }

        binaryResult = "0";
        return binaryResult;
    }

    public boolean internetReachable() {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 www.google.com");
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return true;
            }
        } catch (Exception e) {
            handle(e);
        }

        return false;
    }

    public void setDebugMode(boolean b) {
        this.debugMode = b;
    }

    public boolean getDebugMode() {
        return this.debugMode;
    }

    public void closeCD(String drive) {
        try {
            File file = File.createTempFile("CDROM", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set wmp = CreateObject(\"WMPlayer.OCX\") \n"
                    + "Set cd = wmp.cdromCollection.getByDriveSpecifier(\""
                    + drive + "\") \n"
                    + "cd.Eject \n "
                    + "cd.Eject ";
            fw.write(vbs);
            fw.close();
            Runtime.getRuntime().exec("wscript " + file.getPath()).waitFor();
        } catch (Exception ex) {
            handle(ex);
        }
    }

    public void openCD(String drive) {
        try {
            File file = File.createTempFile("CDROM", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);
            String vbs = "Set wmp = CreateObject(\"WMPlayer.OCX\") \n"
                    + "Set cd = wmp.cdromCollection.getByDriveSpecifier(\""
                    + drive + "\") \n"
                    + "cd.Eject";
            fw.write(vbs);
            fw.close();

            Runtime.getRuntime().exec("wscript " + file.getPath()).waitFor();
        } catch (Exception ex) {
            handle(ex);
        }
    }
    public void systemProperties() {
        JFrame systemPropFrame = new JFrame();

        systemPropFrame.setResizable(false);

        systemPropFrame.setTitle("System Properties");

        systemPropFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        systemPropFrame.setIconImage(getCyderIcon().getImage());

        JPanel parentPanel = new JPanel();

        parentPanel.setLayout(new GridLayout(2, 7, 5, 5));

        CyderButton fileSep = new CyderButton("File Separator");
        fileSep.setBorder(new LineBorder(navy,5,false));
        fileSep.setFont(weatherFontSmall);
        fileSep.setBackground(regularRed);
        fileSep.setColors(regularRed);
        parentPanel.add(fileSep);
        fileSep.addActionListener(e -> inform(System.getProperty("file.separator"),"",700,200));

        CyderButton classPath = new CyderButton("Class Path");
        classPath.setBorder(new LineBorder(navy,5,false));
        classPath.setFont(weatherFontSmall);
        classPath.setBackground(regularRed);
        classPath.setColors(regularRed);
        parentPanel.add(classPath);
        char[] chars = System.getProperty("java.class.path").toCharArray();
        String build = "";
        for (char aChar : chars) {
            if (aChar == ';')
                build += "<br/>";
            else
                build += aChar;
        }
        String finalBuild = build;
        classPath.addActionListener(e -> inform(finalBuild,"",1000,400));

        CyderButton home = new CyderButton("Java Home");
        home.setBorder(new LineBorder(navy,5,false));
        home.setFont(weatherFontSmall);
        home.setBackground(regularRed);
        home.setColors(regularRed);
        parentPanel.add(home);
        home.addActionListener(e -> inform(System.getProperty("java.home"),"",700,200));

        CyderButton vendor = new CyderButton("Java Vendor");
        vendor.setBorder(new LineBorder(navy,5,false));
        vendor.setFont(weatherFontSmall);
        vendor.setBackground(regularRed);
        vendor.setColors(regularRed);
        parentPanel.add(vendor);
        vendor.addActionListener(e -> inform(System.getProperty("java.vendor"),"",700,200));

        CyderButton vendorurl = new CyderButton("Java Vendor URL");
        vendorurl.setBorder(new LineBorder(navy,5,false));
        vendorurl.setFont(weatherFontSmall);
        vendorurl.setBackground(regularRed);
        vendorurl.setColors(regularRed);
        parentPanel.add(vendorurl);
        vendorurl.addActionListener(e -> inform(System.getProperty("java.vendor.url"),"",700,200));

        CyderButton version = new CyderButton("Java Version");
        version.setBorder(new LineBorder(navy,5,false));
        version.setFont(weatherFontSmall);
        version.setBackground(regularRed);
        version.setColors(regularRed);
        parentPanel.add(version);
        version.addActionListener(e -> inform(System.getProperty("java.version"),"",700,200));

        CyderButton linesep = new CyderButton("Line Separator");
        linesep.setBorder(new LineBorder(navy,5,false));
        linesep.setFont(weatherFontSmall);
        linesep.setBackground(regularRed);
        linesep.setColors(regularRed);
        parentPanel.add(linesep);
        linesep.addActionListener(e -> inform(System.getProperty("line.separator"),"",700,200));

        CyderButton osarch = new CyderButton("OS Architecture");
        osarch.setBorder(new LineBorder(navy,5,false));
        osarch.setFont(weatherFontSmall);
        osarch.setBackground(regularRed);
        osarch.setColors(regularRed);
        parentPanel.add(osarch);
        osarch.addActionListener(e -> inform(System.getProperty("os.arch"),"",700,200));

        CyderButton osname = new CyderButton("OS Name");
        osname.setBorder(new LineBorder(navy,5,false));
        osname.setFont(weatherFontSmall);
        osname.setBackground(regularRed);
        osname.setColors(regularRed);
        parentPanel.add(osname);
        osname.addActionListener(e -> inform(System.getProperty("os.name"),"",700,200));

        CyderButton osver = new CyderButton("OS Version");
        osver.setBorder(new LineBorder(navy,5,false));
        osver.setFont(weatherFontSmall);
        osver.setBackground(regularRed);
        osver.setColors(regularRed);
        parentPanel.add(osver);
        osver.addActionListener(e -> inform(System.getProperty("os.version"),"",700,200));

        CyderButton pathsep = new CyderButton("Path Separator");
        pathsep.setBorder(new LineBorder(navy,5,false));
        pathsep.setFont(weatherFontSmall);
        pathsep.setBackground(regularRed);
        pathsep.setColors(regularRed);
        parentPanel.add(pathsep);
        pathsep.addActionListener(e -> inform(System.getProperty("path.separator"),"",700,200));

        CyderButton userdir = new CyderButton("User Directory");
        userdir.setBorder(new LineBorder(navy,5,false));
        userdir.setFont(weatherFontSmall);
        userdir.setBackground(regularRed);
        userdir.setColors(regularRed);
        parentPanel.add(userdir);
        userdir.addActionListener(e -> inform(System.getProperty("user.dir"),"",700,200));

        CyderButton userhome = new CyderButton("User Home");
        userhome.setBorder(new LineBorder(navy,5,false));
        userhome.setFont(weatherFontSmall);
        userhome.setBackground(regularRed);
        userhome.setColors(regularRed);
        parentPanel.add(userhome);
        userhome.addActionListener(e -> inform(System.getProperty("user.home"),"",700,200));

        CyderButton username = new CyderButton("Username");
        username.setBorder(new LineBorder(navy,5,false));
        username.setFont(weatherFontSmall);
        username.setBackground(regularRed);
        username.setColors(regularRed);
        parentPanel.add(username);
        username.addActionListener(e -> inform(System.getProperty("user.name"),"",700,200));

        parentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        systemPropFrame.add(parentPanel);

        systemPropFrame.pack();

        systemPropFrame.setLocationRelativeTo(null);

        systemPropFrame.setVisible(true);

        systemPropFrame.setAlwaysOnTop(true);

        systemPropFrame.setAlwaysOnTop(false);

        systemPropFrame.requestFocus();
    }

    public void pixelate(File path, int pixelSize) {
        try {
            BufferedImage ReturnImage = ImageUtil.pixelate(ImageIO.read(path), pixelSize);

            String NewName = path.getName().replace(".png", "") + "_Pixelated_Pixel_Size_" + pixelSize + ".png";

            if (pixelFrame != null) {
                closeAnimation(pixelFrame);
                pixelFrame.dispose();
            }

            pixelFrame = new JFrame();

            pixelFrame.setUndecorated(true);

            pixelFrame.setTitle("Approve Pixelation");

            pixelFrame.addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getXOnScreen();
                    int y = e.getYOnScreen();

                    if (pixelFrame != null && pixelFrame.isFocused()) {
                        pixelFrame.setLocation(x - xMouse, y - yMouse);
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    xMouse = e.getX();
                    yMouse = e.getY();
                }
            });

            pixelFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel ParentPanel = new JPanel();

            ParentPanel.setLayout(new BorderLayout());

            pixelFrame.setContentPane(ParentPanel);

            JLabel PictureLabel = new JLabel(new ImageIcon(ReturnImage));

            ParentPanel.add(PictureLabel, BorderLayout.CENTER);

            CyderButton approveImage = new CyderButton("Approve Image");

            approveImage.setFocusPainted(false);

            approveImage.setBackground(navy);

            approveImage.setColors(navy);

            approveImage.setBorder(new LineBorder(navy,3,false));

            approveImage.setForeground(vanila);

            approveImage.setFont(weatherFontSmall);

            approveImage.addActionListener(e -> {
                try {
                    ImageIO.write(ReturnImage, "png", new File("C:\\Users\\" + getWindowsUsername() + "\\Downloads\\" + NewName));
                } catch (Exception exc) {
                    handle(exc);
                }

                closeAnimation(pixelFrame);
                pixelFrame.dispose();
                inform("The distorted image has been saved to your Downloads folder.","", 400, 200);
            });

            approveImage.setSize(pixelFrame.getX(), 20);

            CyderButton rejectImage = new CyderButton("Reject Image");

            rejectImage.setFocusPainted(false);

            rejectImage.setBackground(regularRed);

            rejectImage.setBorder(new LineBorder(navy,3,false));

            rejectImage.setColors(regularRed);

            rejectImage.setFont(weatherFontSmall);

            rejectImage.addActionListener(e -> {
                closeAnimation(pixelFrame);
                pixelFrame.dispose();
            });

            rejectImage.setSize(pixelFrame.getX(), 20);

            ParentPanel.add(rejectImage, BorderLayout.PAGE_START);

            ParentPanel.add(approveImage, BorderLayout.PAGE_END);

            ParentPanel.repaint();

            pixelFrame.pack();

            pixelFrame.setVisible(true);

            pixelFrame.setLocationRelativeTo(null);

            pixelFrame.setAlwaysOnTop(true);

            pixelFrame.setResizable(false);

            pixelFrame.setIconImage(cyderIcon.getImage());
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public void mp3(File StartPlaying) {
        if (musicFrame != null) {
            closeAnimation(musicFrame);
            musicFrame.dispose();
        }

        musicFrame = new JFrame();

        musicFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        musicFrame.setUndecorated(true);

        musicFrame.setTitle("MP3 player");

        musicFrame.setIconImage(getCyderIcon().getImage());

        musicFrame.setBounds(0, 0, 1000, 563);

        JLabel musicLabel = new JLabel();

        musicLabel.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\mp3.png"));

        musicLabel.setBounds(0, 0, 1000, 563);

        musicFrame.setContentPane(musicLabel);

        ImageIcon mini1 = new ImageIcon("src\\com\\cyder\\io\\pictures\\minimize1.png");
        ImageIcon mini2 = new ImageIcon("src\\com\\cyder\\io\\pictures\\minimize2.png");

        ImageIcon close1 = new ImageIcon("src\\com\\cyder\\io\\pictures\\Close1.png");
        ImageIcon close2 = new ImageIcon("src\\com\\cyder\\io\\pictures\\Close2.png");

        JLabel musicDragLabel = new JLabel();

        musicDragLabel.setBackground(new Color(20,20,20));

        musicDragLabel.setBounds(0, 0, 1000, 22);

        musicDragLabel.setOpaque(true);

        musicDragLabel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (musicFrame != null && musicFrame.isFocused()) {
                    musicFrame.setLocation(x - xMouse, y - yMouse);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xMouse = e.getX();
                yMouse = e.getY();
            }
        });

        musicFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeiconified(WindowEvent e) {
                musicFrame.setLocation(musicRestoreX,musicRestoreY);
                musicFrame.setVisible(true);
                musicFrame.requestFocus();
            }
        });

        JButton close = new JButton("");

        close.setToolTipText("Close");

        close.addActionListener(e -> {
            closeAnimation(musicFrame);
            stopMusic.doClick();
            musicFrame.dispose();
        });

        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                close.setIcon(close2);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                close.setIcon(close1);
            }
        });

        close.setBounds(1000 - 26, 0, 22, 20);

        close.setIcon(close1);

        close.setContentAreaFilled(false);

        close.setBorderPainted(false);

        close.setFocusPainted(false);

        musicDragLabel.add(close);

        JButton minimize = new JButton("");

        minimize.setToolTipText("Minimize");

        minimize.addActionListener(e -> {
            musicRestoreX = musicFrame.getX();
            musicRestoreY = musicFrame.getY();
            minimizeAnimation(musicFrame);
        });

        minimize.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                minimize.setIcon(mini2);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                minimize.setIcon(mini1);
            }
        });

        minimize.setBounds(1000 - 52, 0, 22, 20);

        minimize.setIcon(mini1);

        minimize.setContentAreaFilled(false);

        minimize.setBorderPainted(false);

        minimize.setFocusPainted(false);

        musicDragLabel.add(minimize);

        musicLabel.add(musicDragLabel);

        musicTitleLabel = new JLabel("", SwingConstants.CENTER);

        musicTitleLabel.setBounds(280, 38, 400, 30);

        musicTitleLabel.setToolTipText("Currently Playing");

        musicTitleLabel.setFont(new Font("tahoma", Font.BOLD, 18));

        musicTitleLabel.setForeground(vanila);

        musicTitleLabel.setText("No Audio Currently Playing");

        musicLabel.add(musicTitleLabel);

        musicVolumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);

        CyderSliderUI UI = new CyderSliderUI(musicVolumeSlider);

        UI.setFillColor(vanila);
        UI.setOutlineColor(vanila);
        UI.setNewValColor(vanila);
        UI.setOldValColor(vanila);
        UI.setStroke(new BasicStroke(3.0f));

        musicVolumeSlider.setUI(UI);

        musicVolumeSlider.setBounds(352, 499, 385, 63);

        musicVolumeSlider.setMinimum(0);

        musicVolumeSlider.setMaximum(100);

        musicVolumeSlider.setMajorTickSpacing(5);

        musicVolumeSlider.setMinorTickSpacing(1);

        musicVolumeSlider.setPaintTicks(false);

        musicVolumeSlider.setPaintLabels(false);

        musicVolumeSlider.setVisible(true);

        musicVolumeSlider.setValue(50);

        musicVolumeSlider.setFont(new Font("HeadPlane", Font.BOLD, 18));

        musicVolumeSlider.addChangeListener(e -> {
            Port.Info Source = Port.Info.SPEAKER;
            Port.Info Headphones = Port.Info.HEADPHONE;

            if (AudioSystem.isLineSupported(Source)) {
                try {
                    Port outline = (Port) AudioSystem.getLine(Source);
                    outline.open();
                    FloatControl VolumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
                    VolumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
                    musicVolumeLabel.setText(musicVolumeSlider.getValue() + "%");
                } catch (Exception ex) {
                    handle(ex);
                }
            }

            if (AudioSystem.isLineSupported(Headphones)) {
                try {
                    Port outline = (Port) AudioSystem.getLine(Headphones);
                    outline.open();
                    FloatControl VolumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
                    VolumeControl.setValue((float) (musicVolumeSlider.getValue() * 0.01));
                    musicVolumeLabel.setText(musicVolumeSlider.getValue() + "%");
                } catch (Exception exc) {
                    handle(exc);
                }
            }
        });

        musicVolumeSlider.setOpaque(false);

        musicVolumeSlider.setToolTipText("Volume");

        musicVolumeSlider.setFocusable(false);

        musicLabel.add(musicVolumeSlider);

        musicVolumeLabel = new JLabel("", SwingConstants.CENTER);

        musicVolumeLabel.setBounds(250, 499, 100, 60);

        musicVolumeLabel.setToolTipText("");

        musicVolumeLabel.setFont(new Font("tahoma", Font.BOLD, 18));

        musicVolumeLabel.setForeground(vanila);

        musicVolumeLabel.setText(musicVolumeSlider.getValue() + "%");

        musicLabel.add(musicVolumeLabel);

        playPauseMusic = new JButton("");

        playPauseMusic.setToolTipText("play");

        playPauseMusic.addActionListener(e -> {
            if (mp3Player != null) {
                if (!playIcon) {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png"));
                    playPauseMusic.setToolTipText("play");
                    playIcon = true;
                }

                else {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Pause.png"));
                    playPauseMusic.setToolTipText("Pause");
                    playIcon = false;
                }

                if (playIcon) {
                    try {
                        pauseLocation = fis.available();
                    }

                    catch (Exception exc) {
                        handle(exc);
                    }

                    stopScrolling();
                    mp3Player.close();
                }

                else {
                    try {
                        fis = new FileInputStream(musicFiles[currentMusicIndex]);
                        bis = new BufferedInputStream(fis);
                        mp3Player = new Player(bis);

                        if (pauseLocation == 0) {
                            fis.skip(0);
                        }

                        else {

                            if (songTotalLength - pauseLocation <= 0) {
                                fis.skip(0);
                            }

                            else {
                                fis.skip(songTotalLength - pauseLocation);
                            }
                        }

                        resumeMusic();
                    }

                    catch (Exception ex) {
                        handle(ex);
                    }
                }
            }
        });

        playPauseMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (playIcon) {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\PlayHover.png"));
                }

                else {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\PauseHover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (playIcon)
                {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png"));
                }

                else
                {
                    playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Pause.png"));
                }
            }
        });

        playPauseMusic.setBounds(121, 263, 75, 75);

        ImageIcon Play = new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png");

        playPauseMusic.setIcon(Play);

        musicLabel.add(playPauseMusic);

        playPauseMusic.setFocusPainted(false);

        playPauseMusic.setOpaque(false);

        playPauseMusic.setContentAreaFilled(false);

        playPauseMusic.setBorderPainted(false);

        lastMusic = new JButton("");

        lastMusic.setToolTipText("Last Audio");

        lastMusic.addActionListener(e -> {
            repeatAudio = false;

            if (mp3Player != null) {
                if (currentMusicIndex - 1 >= 0) {
                    currentMusicIndex -= 1;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                }

                else if (currentMusicIndex == 0) {
                    currentMusicIndex = musicFiles.length - 1;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                }
            }
        });

        lastMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lastMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SkipBackHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SkipBack.png"));
            }
        });

        lastMusic.setBounds(121, 363, 75, 75);

        ImageIcon Last = new ImageIcon("src\\com\\cyder\\io\\pictures\\SkipBack.png");

        lastMusic.setIcon(Last);

        musicLabel.add(lastMusic);

        lastMusic.setFocusPainted(false);

        lastMusic.setOpaque(false);

        lastMusic.setContentAreaFilled(false);

        lastMusic.setBorderPainted(false);

        nextMusic = new JButton("");

        nextMusic.setToolTipText("Next Audio");

        nextMusic.addActionListener(e -> {
            repeatAudio = false;

            if (mp3Player != null) {
                if (currentMusicIndex + 1 <= musicFiles.length - 1) {
                    currentMusicIndex += 1;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                }

                else if (currentMusicIndex + 1 == musicFiles.length) {
                    currentMusicIndex = 0;
                    mp3Player.close();
                    mp3Player = null;
                    stopScrolling();
                    play(musicFiles[currentMusicIndex]);
                }
            }
        });

        nextMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                nextMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SkipHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Skip.png"));
            }
        });

        nextMusic.setBounds(121, 463, 75, 75);

        ImageIcon Next = new ImageIcon("src\\com\\cyder\\io\\pictures\\Skip.png");

        nextMusic.setIcon(Next);

        musicLabel.add(nextMusic);

        nextMusic.setFocusPainted(false);

        nextMusic.setOpaque(false);

        nextMusic.setContentAreaFilled(false);

        nextMusic.setBorderPainted(false);

        loopMusic = new JButton("");

        loopMusic.setToolTipText("Loop Audio");

        loopMusic.addActionListener(e -> {
            if (!repeatAudio) {
                loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Repeat.png"));
                loopMusic.setToolTipText("Loop Audio");
                repeatAudio = true;
            }

            else {
                loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\NoRepeat.png"));
                loopMusic.setToolTipText("Loop Audio");
                repeatAudio = false;
            }
        });

        loopMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (repeatAudio)
                {
                    loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\RepeatHover.png"));
                }

                else
                {
                    loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\NoRepeatHover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (repeatAudio)
                {
                    loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Repeat.png"));
                }

                else
                {
                    loopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\NoRepeat.png"));
                }
            }
        });

        loopMusic.setBounds(50, 363, 76, 76);

        ImageIcon Loop = new ImageIcon("src\\com\\cyder\\io\\pictures\\NoRepeat.png");

        loopMusic.setIcon(Loop);

        musicLabel.add(loopMusic);

        loopMusic.setFocusPainted(false);

        loopMusic.setOpaque(false);

        loopMusic.setContentAreaFilled(false);

        loopMusic.setBorderPainted(false);

        stopMusic = new JButton("");

        stopMusic.setToolTipText("Stop");

        stopMusic.addActionListener(e -> {
            if (mp3Player != null) {
                mp3Player.close();
                musicTitleLabel.setText("No audio currently playing");
                playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png"));
                playPauseMusic.setToolTipText("play");
                playIcon = true;
                pauseLocation = 0;
                songTotalLength = 0;
                musicStopped = true;
                stopScrolling();
            }
        });

        stopMusic.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                stopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\StopHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                stopMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Stop.png"));
            }
        });

        stopMusic.setBounds(50, 263, 75, 75);

        ImageIcon Stop = new ImageIcon("src\\com\\cyder\\io\\pictures\\Stop.png");

        stopMusic.setIcon(Stop);

        musicLabel.add(stopMusic);

        stopMusic.setFocusPainted(false);

        stopMusic.setOpaque(false);

        stopMusic.setContentAreaFilled(false);

        stopMusic.setBorderPainted(false);

        selectMusicDir = new JButton("");

        selectMusicDir.setToolTipText("Open File");

        selectMusicDir.addActionListener(e -> {
            File SelectedFile = getFile();

            if (!SelectedFile.toString().endsWith("mp3")) {
                if (mp3Player == null) {
                    inform("Sorry, " + username + ", but that's not an mp3 file.","", 400, 200);
                }
            }

            else {
                File[] SelectedFileDir = SelectedFile.getParentFile().listFiles();
                ArrayList<File> ValidFiles = new ArrayList<>();
                for (int i = 0; i < (SelectedFileDir != null ? SelectedFileDir.length : 0); i++) {
                    if (SelectedFileDir[i].toString().endsWith(".mp3")) {
                        ValidFiles.add(SelectedFileDir[i]);
                    }
                }

                for (int j = 0 ; j < ValidFiles.size() ; j++) {
                    if (ValidFiles.get(j).equals(SelectedFile)) {
                        currentMusicIndex = j;
                    }
                }

                musicFiles = ValidFiles.toArray(new File[ValidFiles.size()]);
                play(musicFiles[currentMusicIndex]);
            }
        });

        selectMusicDir.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                selectMusicDir.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SelectFileHover.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selectMusicDir.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\SelectFile.png"));
            }
        });

        selectMusicDir.setBounds(50, 463, 75, 75);

        ImageIcon File = new ImageIcon("src\\com\\cyder\\io\\pictures\\SelectFile.png");

        selectMusicDir.setIcon(File);

        musicLabel.add(selectMusicDir);

        selectMusicDir.setFocusPainted(false);

        selectMusicDir.setOpaque(false);

        selectMusicDir.setContentAreaFilled(false);

        selectMusicDir.setBorderPainted(false);

        musicFrame.setLocationRelativeTo(null);

        musicFrame.setVisible(true);

        musicFrame.setAlwaysOnTop(true);

        musicFrame.setAlwaysOnTop(false);

        musicFrame.requestFocus();

        if (StartPlaying != null) {
            initMusic(StartPlaying);
        }

        else {
            try {
                File[] SelectedFileDir = new File("src\\com\\cyder\\io\\users\\" + getUserUUID() + "\\Music\\" ).listFiles();
                ArrayList<File> ValidFiles = new ArrayList<>();
                if (SelectedFileDir == null)
                    return;

                for (int i = 0; i < SelectedFileDir.length; i++) {
                    if (SelectedFileDir[i].toString().endsWith(".mp3")) {
                        ValidFiles.add(SelectedFileDir[i]);

                        if (File.equals(ValidFiles.get(i))) {
                            currentMusicIndex = i;
                        }
                    }
                }

                musicFiles = ValidFiles.toArray(new File[ValidFiles.size()]);

                if (musicFiles.length != 0) {
                    play(musicFiles[currentMusicIndex]);
                }
            }

            catch (Exception e) {
                handle(e);
            }
        }
    }

    private void initMusic(File File) {
        File[] SelectedFileDir = File.getParentFile().listFiles();
        ArrayList<File> ValidFiles = new ArrayList<>();

        for (java.io.File file : SelectedFileDir) {
            if (file.toString().endsWith(".mp3")) {
                ValidFiles.add(file);
            }
        }

        for (int i = 0 ; i < ValidFiles.size() ; i++) {
            if (ValidFiles.get(i).equals(File)) {
                currentMusicIndex = i;
            }
        }

        musicFiles = ValidFiles.toArray(new File[ValidFiles.size()]);
        play(musicFiles[currentMusicIndex]);
    }

    private void play(File path) {
        try {
            if (mp3Player != null) {
                mp3Player.close();
                mp3Player = null;
            }

            fis = new FileInputStream(path.toString());
            bis = new BufferedInputStream(fis);
            mp3Player = new Player(bis);
            songTotalLength = fis.available();
            startScrolling();
        }

        catch (Exception e) {
            handle(e);
        }

        new Thread(() -> {
            try {
                playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\Pause.png"));
                playPauseMusic.setToolTipText("Pause");

                playIcon = false;
                mp3Player.play();

                if (repeatAudio) {
                    play(musicFiles[currentMusicIndex]);
                }

                playPauseMusic.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\play.png"));
                playPauseMusic.setToolTipText("play");
                playIcon = true;
            }

            catch (Exception e) {
                handle(e);
            }
        }).start();
    }

    private void resumeMusic() {
        startScrolling();
        new Thread(() -> {
            try {
                mp3Player.play();
                if (repeatAudio) {
                    play(musicFiles[currentMusicIndex]);
                }
            } catch (Exception e) {
                handle(e);
            }
        }).start();
    }

    public void phoneDialer() {
        if (phoneFrame != null) {
            closeAnimation(phoneFrame);
            phoneFrame.dispose();
        }

        phoneFrame = new JFrame();

        phoneFrame.setTitle("Phone");

        phoneFrame.setLocationRelativeTo(null);

        phoneFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel MyPanel = (JPanel) phoneFrame.getContentPane();

        MyPanel.setLayout(new BoxLayout(MyPanel, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel();

        numberLabel = new JLabel("#");

        topPanel.add(numberLabel);

        numberLabel.setFont(weatherFontSmall);

        numberLabel.setBorder(new LineBorder(navy,5,false));

        JPanel ButtonsPanel = new JPanel();

        ButtonsPanel.setLayout(new GridLayout(4, 3, 5, 5));

        MyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        CyderButton zero = new CyderButton("0");
        zero.setBorder(new LineBorder(navy,5,false));
        CyderButton one = new CyderButton("1");
        one.setBorder(new LineBorder(navy,5,false));
        CyderButton two = new CyderButton("2");
        two.setBorder(new LineBorder(navy,5,false));
        CyderButton three = new CyderButton("3");
        three.setBorder(new LineBorder(navy,5,false));
        CyderButton four = new CyderButton("4");
        four.setBorder(new LineBorder(navy,5,false));
        CyderButton five = new CyderButton("5");
        five.setBorder(new LineBorder(navy,5,false));
        CyderButton six = new CyderButton("6");
        six.setBorder(new LineBorder(navy,5,false));
        CyderButton seven = new CyderButton("7");
        seven.setBorder(new LineBorder(navy,5,false));
        CyderButton eight = new CyderButton("8");
        eight.setBorder(new LineBorder(navy,5,false));
        CyderButton nine = new CyderButton("9");
        nine.setBorder(new LineBorder(navy,5,false));
        CyderButton back = new CyderButton("<X");
        back.setBorder(new LineBorder(navy,5,false));
        CyderButton dialNumber = new CyderButton("Call");
        dialNumber.setBorder(new LineBorder(navy,3,false));
        
        ButtonsPanel.add(one);
        one.setColors(regularRed);
        ButtonsPanel.add(two);
        two.setColors(regularRed);
        ButtonsPanel.add(three);
        three.setColors(regularRed);
        ButtonsPanel.add(four);
        four.setColors(regularRed);
        ButtonsPanel.add(five);
        five.setColors(regularRed);
        ButtonsPanel.add(six);
        six.setColors(regularRed);
        ButtonsPanel.add(seven);
        seven.setColors(regularRed);
        ButtonsPanel.add(eight);
        eight.setColors(regularRed);
        ButtonsPanel.add(nine);
        nine.setColors(regularRed);
        ButtonsPanel.add(dialNumber);
        dialNumber.setColors(regularRed);
        ButtonsPanel.add(zero);
        zero.setColors(regularRed);
        ButtonsPanel.add(back);
        back.setColors(regularRed);
        MyPanel.add(topPanel);

        one.setFocusPainted(false);
        two.setFocusPainted(false);
        three.setFocusPainted(false);
        four.setFocusPainted(false);
        five.setFocusPainted(false);
        six.setFocusPainted(false);
        seven.setFocusPainted(false);
        eight.setFocusPainted(false);
        nine.setFocusPainted(false);
        zero.setFocusPainted(false);
        dialNumber.setFocusPainted(false);
        back.setFocusPainted(false);

        MyPanel.add(ButtonsPanel);

        phoneFrame.setIconImage(getCyderIcon().getImage());

        phoneFrame.setVisible(true);

        phoneFrame.setAlwaysOnTop(true);

        phoneFrame.setAlwaysOnTop(false);

        one.addActionListener(e -> {
            phoneNum = phoneNum + "1";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        two.addActionListener(e -> {
            phoneNum = phoneNum + "2";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        three.addActionListener(e -> {
            phoneNum = phoneNum + "3";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        four.addActionListener(e -> {
            phoneNum = phoneNum + "4";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        five.addActionListener(e -> {
            phoneNum = phoneNum + "5";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        six.addActionListener(e -> {
            phoneNum = phoneNum + "6";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        seven.addActionListener(e -> {
            phoneNum = phoneNum + "7";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        eight.addActionListener(e -> {
            phoneNum = phoneNum + "8";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        nine.addActionListener(e -> {
            phoneNum = phoneNum + "9";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        zero.addActionListener(e -> {
            phoneNum = phoneNum + "0";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        back.addActionListener(e -> {
            if (phoneNum.length() > 0) {
                phoneNum = phoneNum.substring(0, phoneNum.length() - 1);

                numberLabel.setText(phoneNumFormat(phoneNum));
            }
        });

        one.setBackground(new Color(223, 85, 83));

        one.setFont(weatherFontBig);

        two.setFocusPainted(false);

        two.setBackground(new Color(223, 85, 83));

        two.setFont(weatherFontBig);

        three.setFocusPainted(false);

        three.setBackground(new Color(223, 85, 83));

        three.setFont(weatherFontBig);

        four.setFocusPainted(false);

        four.setBackground(new Color(223, 85, 83));

        four.setFont(weatherFontBig);

        five.setFocusPainted(false);

        five.setBackground(new Color(223, 85, 83));

        five.setFont(weatherFontBig);

        six.setFocusPainted(false);

        six.setBackground(new Color(223, 85, 83));

        six.setFont(weatherFontBig);

        seven.setFocusPainted(false);

        seven.setBackground(new Color(223, 85, 83));

        seven.setFont(weatherFontBig);

        eight.setFocusPainted(false);

        eight.setBackground(new Color(223, 85, 83));

        eight.setFont(weatherFontBig);

        nine.setFocusPainted(false);

        nine.setBackground(new Color(223, 85, 83));

        nine.setFont(weatherFontBig);

        zero.setFocusPainted(false);

        zero.setBackground(new Color(223, 85, 83));

        zero.setFont(weatherFontBig);

        back.setFocusPainted(false);

        back.setBackground(new Color(223, 85, 83));

        back.setFont(weatherFontBig);

        dialNumber.setFocusPainted(false);

        dialNumber.setBackground(new Color(223, 85, 83));

        dialNumber.setFont(weatherFontBig);

        dialNumber.addActionListener(e -> {
            if (phoneNum.length() > 0) {
                inform("Dialing: " + phoneNum,"", 200, 200);
                phoneNum = "";
            }
        });

        phoneFrame.pack();

        phoneFrame.setVisible(true);

        phoneFrame.setResizable(false);

        phoneFrame.setLocationRelativeTo(null);
    }

    public void playMusic(String FilePath) {
        try {
            stopMusic();
            FileInputStream FileInputStream = new FileInputStream(FilePath);
            player = new Player(FileInputStream);
            Thread MusicThread = new Thread(() -> {
                try {
                    player.play();
                }

                catch (Exception e) {
                    handle(e);
                }
            });

            MusicThread.start();
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public void stopMusic() {
        try {
            if (player != null && !player.isComplete()) {
                player.close();
            }

            player = null;
            stopScrolling();
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public void here() {
        inform("here","Here", 100, 100);
    }

    public void calculator() {
        calculatorExpression = "";

        if (calculatorFrame != null) {
            closeAnimation(calculatorFrame);

            calculatorFrame.dispose();
        }

        calculatorFrame = new JFrame();

        calculatorFrame.setResizable(false);

        calculatorFrame.setTitle("Calculator");

        calculatorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        calculatorFrame.setResizable(false);

        calculatorFrame.setIconImage(getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel, BoxLayout.Y_AXIS));

        calculatorField = new JTextField(20);

        calculatorField.setSelectionColor(selectionColor);

        calculatorField.setToolTipText("(rad not deg)");

        calculatorField.setText("");

        calculatorField.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(navy,5,false)));

        calculatorField.setFont(weatherFontBig);

        ParentPanel.add(calculatorField);

        GridLayout ButtonLayout = new GridLayout(5, 4, 5, 5);

        JPanel CalcButtonPanel = new JPanel();

        CalcButtonPanel.setLayout(ButtonLayout);

        CyderButton calculatorAdd = new CyderButton("+");

        calculatorAdd.setColors(calculatorOrange);

        calculatorAdd.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorAdd);

        calculatorAdd.setFocusPainted(false);

        calculatorAdd.setBackground(calculatorOrange);

        calculatorAdd.setFont(weatherFontSmall);

        calculatorAdd.addActionListener(e -> {
            calculatorExpression += "+";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSubtract = new CyderButton("-");

        calculatorSubtract.setColors(calculatorOrange);

        calculatorSubtract.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorSubtract);

        calculatorSubtract.setFocusPainted(false);

        calculatorSubtract.setBackground(calculatorOrange);

        calculatorSubtract.setFont(weatherFontSmall);

        calculatorSubtract.addActionListener(e -> {
            calculatorExpression += "-";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorMultiply = new CyderButton("*");

        calculatorMultiply.setColors(calculatorOrange);

        calculatorMultiply.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorMultiply);

        calculatorMultiply.setFocusPainted(false);

        calculatorMultiply.setBackground(calculatorOrange);

        calculatorMultiply.setFont(weatherFontSmall);

        calculatorMultiply.addActionListener(e -> {
            calculatorExpression += "*";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDivide = new CyderButton("/");

        calculatorDivide.setColors(calculatorOrange);

        calculatorDivide.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorDivide);

        calculatorDivide.setFocusPainted(false);

        calculatorDivide.setBackground(calculatorOrange);

        calculatorDivide.setFont(weatherFontSmall);

        calculatorDivide.addActionListener(e -> {
            calculatorExpression += "/";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSeven = new CyderButton("7");

        calculatorSeven.setColors(calculatorOrange);

        calculatorSeven.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorSeven);

        calculatorSeven.setFocusPainted(false);

        calculatorSeven.setBackground(calculatorOrange);

        calculatorSeven.setFont(weatherFontSmall);

        calculatorSeven.addActionListener(e -> {
            calculatorExpression += "7";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEight = new CyderButton("8");

        calculatorEight.setColors(calculatorOrange);

        calculatorEight.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorEight);

        calculatorEight.setFocusPainted(false);

        calculatorEight.setBackground(calculatorOrange);

        calculatorEight.setFont(weatherFontSmall);

        calculatorEight.addActionListener(e -> {
            calculatorExpression += "8";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorNine = new CyderButton("9");

        calculatorNine.setColors(calculatorOrange);

        calculatorNine.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorNine);

        calculatorNine.setFocusPainted(false);

        calculatorNine.setBackground(calculatorOrange);

        calculatorNine.setFont(weatherFontSmall);

        calculatorNine.addActionListener(e -> {
            calculatorExpression += "9";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEquals = new CyderButton("=");

        calculatorEquals.setColors(calculatorOrange);

        calculatorEquals.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorEquals);

        calculatorEquals.setFocusPainted(false);

        calculatorEquals.setBackground(calculatorOrange);

        calculatorEquals.setFont(weatherFontSmall);

        calculatorEquals.addActionListener(e -> {
            try {
                inform("Answer:<br/>" + new DoubleEvaluator().evaluate(calculatorField.getText().trim()), "Result", calculatorFrame.getWidth(),calculatorFrame.getHeight());
            }

            catch (Exception exc) {
                inform("Unrecognized expression. Please use multiplication signs after parenthesis and check the exact syntax of your expression for common" +
                        " errors such as missing delimiters.<br/>Note that this calculator does support typing in the Text Field and can handle more complicated" +
                        "<br/>expressions such as sin, cos, tan, log, ln, floor, etc.","", calculatorFrame.getWidth(), calculatorFrame.getHeight());
                handle(exc);
            }
        });

        calculatorField.addActionListener(e -> calculatorEquals.doClick());

        CyderButton calculatorFour = new CyderButton("4");

        calculatorFour.setColors(calculatorOrange);

        calculatorFour.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorFour);

        calculatorFour.setFocusPainted(false);

        calculatorFour.setBackground(calculatorOrange);

        calculatorFour.setFont(weatherFontSmall);

        calculatorFour.addActionListener(e -> {
            calculatorExpression += "4";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorFive = new CyderButton("5");

        calculatorFive.setColors(calculatorOrange);

        calculatorFive.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorFive);

        calculatorFive.setFocusPainted(false);

        calculatorFive.setBackground(calculatorOrange);

        calculatorFive.setFont(weatherFontSmall);

        calculatorFive.addActionListener(e -> {
            calculatorExpression += "5";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSix = new CyderButton("6");

        calculatorSix.setColors(calculatorOrange);

        calculatorSix.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorSix);

        calculatorSix.setFocusPainted(false);

        calculatorSix.setBackground(calculatorOrange);

        calculatorSix.setFont(weatherFontSmall);

        calculatorSix.addActionListener(e -> {
            calculatorExpression += "6";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorClear = new CyderButton("CE");

        calculatorClear.setColors(calculatorOrange);

        calculatorClear.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorClear);

        calculatorClear.setFocusPainted(false);

        calculatorClear.setBackground(calculatorOrange);

        calculatorClear.setFont(weatherFontSmall);

        calculatorClear.addActionListener(e -> {
            calculatorExpression = "";
            calculatorField.setText("");
        });

        CyderButton calculatorOne = new CyderButton("1");

        calculatorOne.setColors(calculatorOrange);

        calculatorOne.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorOne);

        calculatorOne.setFocusPainted(false);

        calculatorOne.setBackground(calculatorOrange);

        calculatorOne.setFont(weatherFontSmall);

        calculatorOne.addActionListener(e -> {
            calculatorExpression += "1";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorTwo = new CyderButton("2");

        calculatorTwo.setColors(calculatorOrange);

        calculatorTwo.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorTwo);

        calculatorTwo.setFocusPainted(false);

        calculatorTwo.setBackground(calculatorOrange);

        calculatorTwo.setFont(weatherFontSmall);

        calculatorTwo.addActionListener(e -> {
            calculatorExpression += "2";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorThree = new CyderButton("3");

        calculatorThree.setColors(calculatorOrange);

        calculatorThree.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorThree);

        calculatorThree.setFocusPainted(false);

        calculatorThree.setBackground(calculatorOrange);

        calculatorThree.setFont(weatherFontSmall);

        calculatorThree.addActionListener(e -> {
            calculatorExpression += "3";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorUndo = new CyderButton("<--");

        calculatorUndo.setColors(calculatorOrange);

        calculatorUndo.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorUndo);

        calculatorUndo.setFocusPainted(false);

        calculatorUndo.setBackground(calculatorOrange);

        calculatorUndo.setFont(weatherFontSmall);

        calculatorUndo.addActionListener(e -> {
            calculatorExpression = (calculatorExpression == null || calculatorExpression.length() == 0)
                    ? "" : (calculatorExpression.substring(0, calculatorExpression.length() - 1));
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorZero = new CyderButton("0");

        calculatorZero.setColors(calculatorOrange);

        calculatorZero.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorZero);

        calculatorZero.setFocusPainted(false);

        calculatorZero.setBackground(calculatorOrange);

        calculatorZero.setFont(weatherFontSmall);

        calculatorZero.addActionListener(e -> {
            calculatorExpression += "0";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDecimal = new CyderButton(".");

        calculatorDecimal.setColors(calculatorOrange);

        calculatorDecimal.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorDecimal);

        calculatorDecimal.setFocusPainted(false);

        calculatorDecimal.setBackground(calculatorOrange);

        calculatorDecimal.setFont(weatherFontSmall);

        calculatorDecimal.addActionListener(e -> {
            calculatorExpression += ".";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorOpenP = new CyderButton("(");

        calculatorOpenP.setColors(calculatorOrange);

        calculatorOpenP.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorOpenP);

        calculatorOpenP.setFocusPainted(false);

        calculatorOpenP.setBackground(calculatorOrange);

        calculatorOpenP.setFont(weatherFontSmall);

        calculatorOpenP.addActionListener(e -> {
            calculatorExpression += "(";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorCloseP = new CyderButton(")");

        calculatorCloseP.setColors(calculatorOrange);

        calculatorCloseP.setBorder(new LineBorder(navy,5,false));

        CalcButtonPanel.add(calculatorCloseP);

        calculatorCloseP.setFocusPainted(false);

        calculatorCloseP.setBackground(calculatorOrange);

        calculatorCloseP.setFont(weatherFontSmall);

        calculatorCloseP.addActionListener(e -> {
            calculatorExpression += ")";
            calculatorField.setText(calculatorExpression);
        });

        CalcButtonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        ParentPanel.add(CalcButtonPanel);

        calculatorFrame.add(ParentPanel);

        calculatorFrame.pack();

        calculatorFrame.setLocationRelativeTo(null);

        calculatorFrame.setVisible(true);

        calculatorFrame.setAlwaysOnTop(true);

        calculatorFrame.setAlwaysOnTop(false);

        calculatorFrame.requestFocus();
    }

    public void pizzaPlace() {
        if (pizzaFrame != null) {
            closeAnimation(pizzaFrame);
            pizzaFrame.dispose();
        }

        pizzaFrame = new JFrame();
        pizzaFrame.setTitle("Pizza");
        pizzaFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pizzaFrame.setIconImage(getCyderIcon().getImage());
        pizzaFrame.setResizable(false);

        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));
        parentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel CustomerName = new JLabel("Name");
        CustomerName.setFont(weatherFontSmall);
        CustomerName.setForeground(navy);
        JPanel namePanel = new JPanel();
        namePanel.add(CustomerName, SwingConstants.CENTER);

        enterName = new JTextField(20);
        enterName.setSelectionColor(selectionColor);
        enterName.setForeground(navy);
        enterName.setFont(weatherFontSmall);
        enterName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (enterName.getText().length() == 1) {
                    enterName.setText(enterName.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (enterName.getText().length() == 1) {
                    enterName.setText(enterName.getText().toUpperCase());
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (enterName.getText().length() == 1) {
                    enterName.setText(enterName.getText().toUpperCase());
                }
            }
        });

        enterName.setBorder(new LineBorder(navy,5,false));
        JPanel nameFieldPanel = new JPanel();
        nameFieldPanel.add(enterName, SwingConstants.CENTER);

        JPanel pizzaSizeLabelPanel = new JPanel();
        JLabel pizzaSizeLabel = new JLabel("Pizza Size");
        pizzaSizeLabel.setFont(weatherFontSmall);
        pizzaSizeLabel.setForeground(navy);
        pizzaSizeLabelPanel.add(pizzaSizeLabel, SwingConstants.CENTER);

        JPanel pizzaSizePanel = new JPanel();
        small = new JRadioButton("small");
        small.setFont(weatherFontSmall);
        small.setForeground(navy);
        medium = new JRadioButton("medium");
        medium.setFont(weatherFontSmall);
        medium.setForeground(navy);
        large = new JRadioButton("Large");
        large.setFont(weatherFontSmall);
        large.setForeground(navy);
        ButtonGroup pizzaSizeGroup = new ButtonGroup();
        pizzaSizeGroup.add(small);
        pizzaSizeGroup.add(medium);
        pizzaSizeGroup.add(large);
        pizzaSizePanel.setBorder(new LineBorder(navy,5,false));
        pizzaSizePanel.add(small);
        pizzaSizePanel.add(medium);
        pizzaSizePanel.add(large);

        JPanel crustPanel = new JPanel();
        JLabel crustLabel = new JLabel("Crust Type");
        crustLabel.setFont(weatherFontSmall);
        crustLabel.setForeground(navy);
        crustPanel.add(crustLabel,SwingConstants.CENTER);


        String[] CrustTypeChoice = {"Thin", "Thick", "Deep Dish", "Classic", "Tavern", "Seasonal"};
        pizzaCrustCombo = new JComboBox(CrustTypeChoice);
        pizzaCrustCombo.setBorder(new LineBorder(navy,5,false));
        pizzaCrustCombo.setForeground(navy);
        pizzaCrustCombo.setFont(weatherFontSmall);
        JPanel crustChoicePanel = new JPanel();
        crustChoicePanel.add(pizzaCrustCombo,SwingConstants.CENTER);

        JPanel pizzaTopingsLabelPanel = new JPanel();
        JLabel Topings = new JLabel("Topings");
        Topings.setFont(weatherFontSmall);
        Topings.setForeground(navy);
        pizzaTopingsLabelPanel.add(Topings, SwingConstants.CENTER);

        String[] pizzaTopingsStrList = {"Pepperoni", "Sausage", "Green Peppers",
                "Onions", "Tomatoes", "Anchovies", "Bacon", "Chicken", "Beef",
                "Olives", "Mushrooms"};
        JList pizzaToppingsJList = new JList(pizzaTopingsStrList);
        pizzaTopingsList = pizzaToppingsJList;
        pizzaTopingsList.setForeground(navy);
        pizzaTopingsList.setFont(weatherFontSmall);
        pizzaTopingsList.setSelectionBackground(selectionColor);
        pizzaTopingsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        CyderScrollPane PizzaTopingsListScroll = new CyderScrollPane(pizzaTopingsList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        PizzaTopingsListScroll.setThumbColor(regularRed);
        PizzaTopingsListScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        PizzaTopingsListScroll.getViewport().setBorder(null);
        PizzaTopingsListScroll.setViewportBorder(null);
        PizzaTopingsListScroll.setBorder(new LineBorder(navy,5,false));
        PizzaTopingsListScroll.setPreferredSize(new Dimension(200,200));
        JPanel pizzaTopingsScrollPanel = new JPanel();
        pizzaTopingsScrollPanel.add(PizzaTopingsListScroll, SwingConstants.CENTER);

        JPanel extrasLabelPanel = new JPanel();
        JLabel Extra = new JLabel("Extras");
        Extra.setForeground(navy);
        Extra.setFont(weatherFontSmall);
        extrasLabelPanel.add(Extra,SwingConstants.CENTER);

        JPanel extraCheckPanel = new JPanel();
        breadSticks = new JCheckBox("Breadsticks");
        breadSticks.setForeground(navy);
        breadSticks.setFont(weatherFontSmall);
        salad = new JCheckBox("Salad");
        salad.setForeground(navy);
        salad.setFont(weatherFontSmall);
        soda = new JCheckBox("Soda");
        soda.setForeground(navy);
        soda.setFont(weatherFontSmall);
        extraCheckPanel.add(breadSticks);
        extraCheckPanel.add(salad);
        extraCheckPanel.add(soda);

        JLabel orderCommentsLabel = new JLabel("Order Comments");
        orderCommentsLabel.setFont(weatherFontSmall);
        orderCommentsLabel.setForeground(navy);
        JPanel orderCommentsLabelPanel = new JPanel();
        orderCommentsLabelPanel.add(orderCommentsLabel, SwingConstants.CENTER);

        orderCommentsTextArea = new JTextArea(5,20);
        orderCommentsTextArea.setFont(weatherFontSmall);
        orderCommentsTextArea.setAutoscrolls(true);
        orderCommentsTextArea.setLineWrap(true);
        orderCommentsTextArea.setWrapStyleWord(true);
        orderCommentsTextArea.setSelectedTextColor(selectionColor);
        orderCommentsTextArea.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane orderCommentsScroll = new CyderScrollPane(orderCommentsTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        orderCommentsScroll.setThumbColor(regularRed);
        orderCommentsScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        orderCommentsScroll.getViewport().setBorder(null);
        orderCommentsScroll.setViewportBorder(null);
        orderCommentsScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(navy,5,false)));
        orderCommentsScroll.setPreferredSize(new Dimension(400,200));
        JPanel scrollPanel = new JPanel();
        scrollPanel.add(orderCommentsScroll, SwingConstants.CENTER);

        JPanel BottomButtons = new JPanel();
        CyderButton placeOrder = new CyderButton("Place Order");
        placeOrder.setFont(weatherFontSmall);
        placeOrder.setColors(regularRed);
        placeOrder.addActionListener(e -> {
            if (enterName.getText().length() <= 0) {
                inform("Sorry, but you must enter a name.","", 400, 200);
            }

            else {
                String Name = enterName.getText().substring(0, 1).toUpperCase() + enterName.getText().substring(1) + "<br/>";
                String Size;

                if (small.isSelected()) {
                    Size = "small<br/>";
                }

                else if (medium.isSelected()) {
                    Size = "medium<br/>";
                }

                else {
                    Size = "Large<br/>";
                }

                String Crust = pizzaCrustCombo.getSelectedItem() + "<br/>";
                List<?> TopingsList = pizzaTopingsList.getSelectedValuesList();
                ArrayList<String> TopingsArrList = new ArrayList<>();

                for (Object o : TopingsList) {
                    TopingsArrList.add(o.toString());
                }

                if (TopingsArrList.isEmpty()) {
                    TopingsArrList.add("Plain");
                }

                StringBuilder TopingsChosen = new StringBuilder();

                for (String s : TopingsArrList) {
                    TopingsChosen.append(s).append("<br/>");
                }

                String Extras = "";

                if (breadSticks.isSelected()) {
                    Extras += "Breadsticks<br/>";
                }

                if (salad.isSelected()) {
                    Extras += "Salad<br/>";
                }

                if (soda.isSelected()) {
                    Extras += "Soda<br/>";
                }

                String Comments = orderCommentsTextArea.getText().trim();

                if (Extras.length() == 0) {
                    Extras = "";
                }

                else {
                    Extras = "<br/>Extras: " + "<br/>" + Extras;
                }

                

                if (Comments.length() == 0) {
                    inform("Customer Name: " + "<br/>" + Name + "<br/><br/>" + "Size: "
                            + "<br/>" + Size + "<br/><br/>" + "Crust: " + "<br/>" + Crust + "<br/><br/>" + "Topings: " + "<br/><br/>" + TopingsChosen
                            + "<br/><br/>" + Extras,"", 500, 1200);
                }

                else {
                    inform("Customer Name: " + "<br/>" + Name + "<br/><br/>" + "Size: "
                            + "<br/>" + Size + "<br/><br/>" + "Crust Type: " + "<br/>" + Crust + "<br/><br/>" + "Topings: " + "<br/>" + TopingsChosen
                            + "<br/>" + Extras + "<br/><br/>Comments: " + "<br/><br/>" + Comments,"", 500, 1200);
                }
            }
        });

        CyderButton resetPizza = new CyderButton("Reset Values");
        resetPizza.setColors(regularRed);
        resetPizza.setFont(weatherFontSmall);
        resetPizza.addActionListener(e -> {
            enterName.setText("");
            pizzaSizeGroup.clearSelection();
            pizzaCrustCombo.setSelectedItem("Thin");
            pizzaTopingsList.clearSelection();
            breadSticks.setSelected(false);
            salad.setSelected(false);
            soda.setSelected(false);
            orderCommentsTextArea.setText("");
        });

        resetPizza.setFocusPainted(false);
        resetPizza.setBorder(new LineBorder(navy,5,false));
        placeOrder.setBorder(new LineBorder(navy,5,false));
        resetPizza.setBackground(regularRed);
        placeOrder.setBackground(regularRed);
        placeOrder.setFocusPainted(false);
        BottomButtons.add(placeOrder);
        BottomButtons.add(resetPizza);

        parentPanel.add(namePanel);
        parentPanel.add(nameFieldPanel);
        parentPanel.add(pizzaSizeLabelPanel);
        parentPanel.add(pizzaSizePanel);
        parentPanel.add(crustPanel);
        parentPanel.add(crustChoicePanel);
        parentPanel.add(pizzaTopingsLabelPanel);
        parentPanel.add(pizzaTopingsScrollPanel);
        parentPanel.add(extrasLabelPanel);
        parentPanel.add(extraCheckPanel);
        parentPanel.add(orderCommentsLabelPanel);
        parentPanel.add(scrollPanel);
        parentPanel.add(BottomButtons);

        pizzaFrame.add(parentPanel);
        pizzaFrame.pack();
        pizzaFrame.setVisible(true);
        pizzaFrame.setLocationRelativeTo(null);
    }

    public boolean getMathShellMode() {
        return this.mathShellMode;
    }

    public void setMathShellMode(boolean state) {
        this.mathShellMode = state;
    }

    public boolean getOneMathPrint() {
        return this.oneMathPrint;
    }

    public void setOneMathPrint(boolean state) {
        this.oneMathPrint = state;
    }

    public int getCurrentDowns() {
        return this.currentDowns;
    }

    public void setCurrentDowns(int num) {
        this.currentDowns = num;
    }

    private void openNote(File File) {
        if (noteEditorFrame != null) {
            closeAnimation(noteEditorFrame);
            noteEditorFrame.dispose();
        }

        noteEditorFrame = new JFrame();

        noteEditorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        noteEditorFrame.setUndecorated(false);

        noteEditorFrame.setTitle("Editing note: " + File.getName().replace(".txt", ""));

        noteEditorFrame.setResizable(false);

        noteEditorFrame.setIconImage(getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        noteEditorFrame.setContentPane(ParentPanel);

        ParentPanel.setLayout(new BorderLayout());

        noteEditField = new JTextField(20);

        noteEditField.setToolTipText("Change Name");

        noteEditField.setSelectionColor(selectionColor);

        noteEditField.setText(File.getName().replaceFirst(".txt",""));

        noteEditField.setFont(weatherFontSmall);

        noteEditField.setForeground(navy);

        noteEditField.setBorder(new LineBorder(navy,5,false));

        ParentPanel.add(noteEditField, BorderLayout.PAGE_START);

        noteEditArea = new JTextArea(20, 20);

        noteEditArea.setSelectedTextColor(selectionColor);

        noteEditArea.setFont(weatherFontSmall);

        noteEditArea.setForeground(navy);

        noteEditArea.setEditable(true);

        noteEditArea.setAutoscrolls(true);

        noteEditArea.setLineWrap(true);

        noteEditArea.setWrapStyleWord(true);

        noteEditArea.setFocusable(true);

        CyderScrollPane noteScroll = new CyderScrollPane(noteEditArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteScroll.setThumbColor(regularRed);

        noteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        noteScroll.getViewport().setBorder(null);

        noteScroll.setViewportBorder(null);

        noteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(navy,5,false)));

        noteScroll.setPreferredSize(new Dimension(570,780));

        ParentPanel.add(noteScroll, BorderLayout.CENTER);

        try {
            BufferedReader InitReader = new BufferedReader(new FileReader(File));
            String Line = InitReader.readLine();

            while (Line != null) {
                noteEditArea.append(Line + "\n");
                Line = InitReader.readLine();
            }

            InitReader.close();
        }

        catch (Exception e) {
            handle(e);
        }

        currentUserNote = File;

        CyderButton saveNote = new CyderButton("Save & Resign");

        saveNote.setColors(regularRed);

        saveNote.setBorder(new LineBorder(navy,5,false));

        saveNote.setFocusPainted(false);

        saveNote.setBackground(regularRed);

        saveNote.setFont(weatherFontSmall);

        saveNote.addActionListener(e -> {
            try {
                BufferedWriter SaveWriter = new BufferedWriter(new FileWriter(currentUserNote, false));
                SaveWriter.write(noteEditArea.getText());
                SaveWriter.close();

                File newName = null;

                if (noteEditField.getText().length() > 0) {
                    newName = new File(File.getAbsolutePath().replace(File.getName(),noteEditField.getText() + ".txt"));
                    File.renameTo(newName);
                    inform(newName.getName().replace(".txt", "") + " has been successfully saved.","", 400, 200);
                    initializeNotesList();
                    noteListScroll.setViewportView(fileSelectionList);
                    noteListScroll.revalidate();
                }

                else {
                    inform(currentUserNote.getName().replace(".txt", "") + " has been successfully saved.","", 400, 200);
                }
                closeAnimation(noteEditorFrame);
                noteEditorFrame.dispose();
            }

            catch (Exception exc) {
                handle(exc);
            }
        });

        JPanel SavePanel = new JPanel();

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        SavePanel.add(saveNote, SwingConstants.CENTER);

        ParentPanel.add(SavePanel, BorderLayout.PAGE_END);

        noteEditorFrame.pack();

        noteEditorFrame.setVisible(true);

        noteEditArea.requestFocus();

        noteEditorFrame.setLocationRelativeTo(null);
    }

    public boolean isPalindrome(char[] Word) {
        int start = 0;
        int end = Word.length - 1;

        while (end > start) {
            if (Word[start] != Word[end]) {
                return false;
            }

            start++;
            end--;
        }

        return true;
    }

    public String getUserUUID() {
        return this.userUUID;
    }

    public void setUserUUID(String s) {
        this.userUUID = s;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsercolor(Color c) {
        this.usercolor = c;
    }

    public Color getUsercolor() {
        return this.usercolor;
    }

    public void setUserfont(Font f) {
        this.userfont = f;
    }

    public Font getUserfont() {
        return this.userfont;
    }

    public void setCyderVer(String ver) {
        this.cyderVer = ver;
    }

    public boolean OnLastBackground() {
        return (validBackgroundPaths.length == currentBackgroundIndex + 1);
    }

    public File[] getValidBackgroundPaths() {
        return this.validBackgroundPaths;
    }

    private void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    public static void sBeep() {
        Toolkit.getDefaultToolkit().beep();
    }

    public boolean isChristmas() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 12 && Date == 25);
    }

    public boolean isHalloween() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 10 && Date == 31);
    }

    public boolean isIndependenceDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 7 && Date == 4);
    }

    public boolean isThanksgiving() {
        Calendar Checker = Calendar.getInstance();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        LocalDate RealTG = LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
        return (Month == 11 && Date == RealTG.getDayOfMonth());
    }

    public boolean isAprilFoolsDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 4 && Date == 1);
    }

    public void test() {
        try {
            System.out.println(getUserUUID());
        }

        catch (Exception e){
            handle(e);
        }
    }

    public void draw(String ImageName) {
        JFrame bodgeFrame = new JFrame();

        if (pictureFrame != null) {
            closeAnimation(pictureFrame);
            pictureFrame.dispose();
        }

        BufferedImage Image = null;

        try {
            Image = ImageIO.read(new File(ImageName));
        }

        catch (Exception ex) {
            handle(ex);
        }

        pictureFrame = new JFrame();

        pictureFrame.setUndecorated(true);

        pictureFrame.setTitle(new File(ImageName).getName().replace(".png", ""));

        pictureFrame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (pictureFrame != null && pictureFrame.isFocused()) {
                    pictureFrame.setLocation(x - xMouse, y - yMouse);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xMouse = e.getX();
                yMouse = e.getY();
            }
        });

        pictureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BorderLayout());

        pictureFrame.setContentPane(ParentPanel);

        JLabel PictureLabel = new JLabel(new ImageIcon(Image));

        ParentPanel.add(PictureLabel, BorderLayout.PAGE_START);

        closeDraw = new CyderButton("Close");

        closeDraw.setColors(regularRed);

        closeDraw.setBorder(new LineBorder(navy,5,false));

        closeDraw.setFocusPainted(false);

        closeDraw.setBackground(regularRed);

        closeDraw.setFont(weatherFontSmall);

        closeDraw.addActionListener(e -> {
            closeAnimation(pictureFrame);
            pictureFrame.dispose();
            pictureFrame = null;
        });

        closeDraw.setSize(pictureFrame.getX(),20);

        ParentPanel.add(closeDraw,BorderLayout.PAGE_END);

        ParentPanel.repaint();

        pictureFrame.pack();

        pictureFrame.setVisible(true);

        pictureFrame.setLocationRelativeTo(null);

        pictureFrame.setAlwaysOnTop(true);

        pictureFrame.setAlwaysOnTop(false);

        pictureFrame.setResizable(false);

        pictureFrame.setIconImage(getCyderIcon().getImage());
    }

    public void draw(BufferedImage ImageName) {
        if (pictureFrame != null) {
            closeAnimation(pictureFrame);
            pictureFrame.dispose();
        }

        BufferedImage Image = null;

        try {
            Image = ImageName;
        }

        catch (Exception exce) {
            handle(exce);
        }

        pictureFrame = new JFrame();

        pictureFrame.setUndecorated(true);

        pictureFrame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (pictureFrame != null && pictureFrame.isFocused()) {
                    pictureFrame.setLocation(x - xMouse, y - yMouse);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xMouse = e.getX();
                yMouse = e.getY();
            }
        });

        pictureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BorderLayout());

        pictureFrame.setContentPane(ParentPanel);

        JLabel PictureLabel = new JLabel(new ImageIcon(Image));

        ParentPanel.add(PictureLabel, BorderLayout.PAGE_START);

        closeDraw = new CyderButton("Close");

        closeDraw.setBorder(new LineBorder(navy,5,false));

        closeDraw.setColors(regularRed);

        closeDraw.setFocusPainted(false);

        closeDraw.setBackground(regularRed);

        closeDraw.setFont(weatherFontSmall);

        closeDraw.addActionListener(e -> {
            closeAnimation(pictureFrame);
            pictureFrame.dispose();
        });

        closeDraw.setSize(pictureFrame.getX(),20);

        ParentPanel.add(closeDraw,BorderLayout.PAGE_END);

        ParentPanel.repaint();

        pictureFrame.pack();

        pictureFrame.setVisible(true);

        pictureFrame.setLocationRelativeTo(null);

        pictureFrame.setAlwaysOnTop(true);

        pictureFrame.setAlwaysOnTop(false);

        pictureFrame.setResizable(false);

        pictureFrame.setIconImage(getCyderIcon().getImage());
    }

    public void temperature() {
        if (temperatureFrame != null) {
            closeAnimation(temperatureFrame);
            temperatureFrame.dispose();
        }

        temperatureFrame = new JFrame();

        temperatureFrame.setTitle("Temperature Converter");

        temperatureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel ParentPanel = (JPanel) temperatureFrame.getContentPane();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel Value = new JPanel();

        JLabel ValueLabel = new JLabel("Measurement: ");

        ValueLabel.setFont(weatherFontSmall);

        startingValue = new JTextField(20);

        startingValue.setBorder(new LineBorder(navy,5,false));

        startingValue.setForeground(navy);

        startingValue.setSelectionColor(selectionColor);

        startingValue.setFont(weatherFontSmall);

        Value.add(ValueLabel);

        Value.add(startingValue);

        ParentPanel.add(Value);

        JPanel RadioCurrentValue = new JPanel();

        JLabel CurrentValue = new JLabel("Current temperature Unit: ");

        CurrentValue.setFont(weatherFontSmall);

        RadioCurrentValue.add(CurrentValue);

        oldFahrenheit =  new JRadioButton("Fahrenheit");

        oldCelsius =  new JRadioButton("Celsius");

        oldKelvin = new JRadioButton("Kelvin");

        oldFahrenheit.setFont(weatherFontSmall);

        oldCelsius.setFont(weatherFontSmall);

        oldKelvin.setFont(weatherFontSmall);

        radioCurrentValueGroup = new ButtonGroup();

        radioCurrentValueGroup.add(oldFahrenheit);

        radioCurrentValueGroup.add(oldCelsius);

        radioCurrentValueGroup.add(oldKelvin);

        ParentPanel.add(RadioCurrentValue);

        RadioCurrentValue.add(oldFahrenheit);

        RadioCurrentValue.add(oldCelsius);

        RadioCurrentValue.add(oldKelvin);

        JPanel RadioNewValue = new JPanel();

        JLabel NewValue = new JLabel("Conversion temperature Unit: ");

        NewValue.setFont(weatherFontSmall);

        RadioNewValue.add(NewValue);

        newFahrenheit =  new JRadioButton("Fahrenheit");

        newCelsius =  new JRadioButton("Celsius");

        newKelvin = new JRadioButton("Kelvin");

        newFahrenheit.setFont(weatherFontSmall);

        newCelsius.setFont(weatherFontSmall);

        newKelvin.setFont(weatherFontSmall);

        radioNewValueGroup = new ButtonGroup();

        radioNewValueGroup.add(newFahrenheit);

        radioNewValueGroup.add(newCelsius);

        radioNewValueGroup.add(newKelvin);

        ParentPanel.add(RadioNewValue);

        RadioNewValue.add(newFahrenheit);

        RadioNewValue.add(newCelsius);

        RadioNewValue.add(newKelvin);

        JPanel BottomButtons = new JPanel();

        CyderButton calculate = new CyderButton("Calculate");

        calculate.setBorder(new LineBorder(navy,5,false));

        calculate.addActionListener(e -> {
            try {
                DecimalFormat tempFormat = new DecimalFormat(".####");
                double CalculationValue = Double.parseDouble(startingValue.getText());

                if (oldKelvin.isSelected() && CalculationValue <= 0) {
                    inform("Temperatures below absolute zero are imposible.","", 400, 200);
                }

                else {
                    if (oldFahrenheit.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            inform("Get out of here with that. Your value is already in Fahrenheit.","", 400, 200);
                        }

                        else if (newCelsius.isSelected()) {
                            double CelsiusFromFahrenheit;

                            CelsiusFromFahrenheit = (CalculationValue - 32.0) / 1.8;

                            inform( CalculationValue + " Fahrenheit converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromFahrenheit),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newKelvin.isSelected()) {
                            double KelvinFromFahrenheit;

                            KelvinFromFahrenheit = (CalculationValue +459.67) * 5/9;

                            if (KelvinFromFahrenheit >= 0) {
                                inform(CalculationValue + " Fahrenheit converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromFahrenheit),"", 400, 200);

                                startingValue.setText("");

                                radioCurrentValueGroup.clearSelection();

                                radioNewValueGroup.clearSelection();
                            }

                            else {
                                inform("Temperatures below absolute zero are imposible.","", 400, 200);
                            }
                        }

                    }

                    else if (oldCelsius.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromCelsius;

                            FahrenheitFromCelsius = (CalculationValue *1.8) + 32;

                            inform(CalculationValue + " Celsius converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromCelsius),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newCelsius.isSelected()) {
                            inform("Get out of here with that. Your value is already in Celsius.","", 400, 200);
                        }

                        else if (newKelvin.isSelected()) {
                            double KelvinFromCelsius;

                            KelvinFromCelsius = CalculationValue + 273.15 ;

                            if (KelvinFromCelsius >= 0) {
                                inform(CalculationValue + " Celsius converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromCelsius),"", 400, 200);

                                startingValue.setText("");

                                radioCurrentValueGroup.clearSelection();

                                radioNewValueGroup.clearSelection();
                            }

                            else {
                                inform("Temperatures below absolute zero are imposible.","", 400, 200);
                            }
                        }

                    }

                    else if (oldKelvin.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromKelvin;

                            FahrenheitFromKelvin = CalculationValue * 1.8 - 459.67;

                            inform(CalculationValue + " Kelvin converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromKelvin),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newCelsius.isSelected()) {
                            double CelsiusFromKelvin;

                            CelsiusFromKelvin = CalculationValue - 273.15;

                            inform( CalculationValue + " Kelvin converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromKelvin),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newKelvin.isSelected()) {
                            inform("Get out of here with that. Your value is already in Kelvin","", 400, 200);
                        }
                    }

                    else {
                        inform("Please select your current temperature unit and the one you want to convet to.","", 400, 200);
                    }
                }
            }

            catch (Exception ex) {
                inform("Your value must only contain numbers.","", 400, 200);
            }
        });

        CyderButton resetValues = new CyderButton("Reset Values");

        resetValues.setBorder(new LineBorder(navy,5,false));

        resetValues.setColors(regularRed);
        calculate.setColors(regularRed);

        resetValues.addActionListener(e -> {
            startingValue.setText("");
            radioCurrentValueGroup.clearSelection();
            radioNewValueGroup.clearSelection();
        });

        calculate.setFocusPainted(false);

        calculate.setBackground(regularRed);

        calculate.setFont(weatherFontSmall);

        resetValues.setFocusPainted(false);

        resetValues.setBackground(regularRed);

        resetValues.setFont(weatherFontSmall);

        BottomButtons.add(calculate);

        BottomButtons.add(resetValues);

        ParentPanel.add(BottomButtons);

        temperatureFrame.pack();

        temperatureFrame.setVisible(true);

        temperatureFrame.setLocationRelativeTo(null);

        temperatureFrame.setIconImage(getCyderIcon().getImage());

        temperatureFrame.setAlwaysOnTop(true);

        temperatureFrame.setAlwaysOnTop(false);
    }

    public void clickMe() {
        try {
            if (clickMeFrame != null) {
                closeAnimation(clickMeFrame);
                clickMeFrame.dispose();
            }

            clickMeFrame = new JFrame();

            clickMeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            clickMeFrame.setTitle("");

            clickMeFrame.setSize(200, 100);

            clickMeFrame.setUndecorated(true);

            JLabel consoleLabel = new JLabel(new ImageIcon(resizeImage(200, 200, new File("src\\com\\cyder\\io\\pictures\\InformBackground.png"))));

            clickMeFrame.setContentPane(consoleLabel);

            clickMeFrame.addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getXOnScreen();
                    int y = e.getYOnScreen();

                    if (clickMeFrame != null && clickMeFrame.isFocused()) {
                        clickMeFrame.setLocation(x - xMouse, y - yMouse);
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    xMouse = e.getX();
                    yMouse = e.getY();
                }
            });

            JLabel dismiss = new JLabel("Click Me!");

            dismiss.setHorizontalAlignment(JLabel.CENTER);

            dismiss.setVerticalAlignment(JLabel.CENTER);

            dismiss.setForeground(vanila);

            dismiss.setFont(weatherFontBig.deriveFont(24f));

            dismiss.setBounds(20, 30, 150, 40);

            dismiss.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {

                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    clickMeFrame.dispose();
                    clickMe();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    dismiss.setForeground(regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dismiss.setForeground(vanila);
                }
            });

            consoleLabel.add(dismiss);

            consoleLabel.setBorder(new LineBorder(navy,5,false));

            clickMeFrame.setVisible(true);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();

            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();

            clickMeFrame.setLocation(randInt(0, (int) (rect.getMaxX() - 200)),randInt(0,(int) rect.getMaxY() - 200));

            clickMeFrame.setAlwaysOnTop(true);

            clickMeFrame.setResizable(false);

            clickMeFrame.setIconImage(getCyderIcon().getImage());
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public boolean filter(String FilterWord) {
        FilterWord = FilterWord.toLowerCase();

        try {
            FilterWord = FilterWord.replace("1","i").replace("!","i").replace("3","e")
                    .replace("4","a").replace("@","a").replace("5","s").replace("7","t")
                    .replace("0","o").replace("9","g").replace("%", "i").replace("#","h").replace("$","s");

            String fileName = "src\\com\\cyder\\io\\text\\v.txt";

            BufferedReader vReader = new  BufferedReader(new FileReader(fileName));

            String Line = vReader.readLine();

            while((Line != null && !Line.equals("") && Line.length() != 0))  {
                if (FilterWord.contains(Line)) {
                    return true;
                }
                Line = vReader.readLine();
            }
        }

        catch (Exception ex) {
            handle(ex);
        }

        return false;
    }

    public void disco(int iterations) {
        Thread DiscoThread = new Thread(() -> {
            try {
                boolean Fixed = false;
                boolean NumOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
                boolean CapsOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
                boolean ScrollOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);

                for (int i = 1; i < iterations ; i++) {
                    Robot Rob = new Robot();

                    if (!Fixed) {
                        Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
                        Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_CAPS_LOCK, false);
                        Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_SCROLL_LOCK, false);

                        Fixed = true;
                    }

                    Rob.keyPress(KeyEvent.VK_NUM_LOCK);
                    Rob.keyRelease(KeyEvent.VK_NUM_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_CAPS_LOCK);
                    Rob.keyRelease(KeyEvent.VK_CAPS_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_SCROLL_LOCK);
                    Rob.keyRelease(KeyEvent.VK_SCROLL_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_NUM_LOCK);
                    Rob.keyRelease(KeyEvent.VK_NUM_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_CAPS_LOCK);
                    Rob.keyRelease(KeyEvent.VK_CAPS_LOCK);

                    Thread.sleep(170);

                    Rob.keyPress(KeyEvent.VK_SCROLL_LOCK);
                    Rob.keyRelease(KeyEvent.VK_SCROLL_LOCK);

                    Thread.sleep(170);
                }

                Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, NumOn);
                Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_CAPS_LOCK, CapsOn);
                Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_SCROLL_LOCK, ScrollOn);
            }

            catch (Exception ex) {
                handle(ex);
            }
        });

        DiscoThread.start();
    }

    public BufferedImage resizeImage(int x, int y, File UneditedImage) {
        BufferedImage ReturnImage = null;

        try {
            File CurrentConsole = UneditedImage;
            Image ConsoleImage = ImageIO.read(CurrentConsole);
            Image TransferImage = ConsoleImage.getScaledInstance(x, y, Image.SCALE_SMOOTH);
            ReturnImage = new BufferedImage(TransferImage.getWidth(null), TransferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = ReturnImage.createGraphics();

            bGr.drawImage(TransferImage, 0, 0, null);
            bGr.dispose();
            return ReturnImage;
        }

        catch (Exception e) {
            handle(e);
        }

        return ReturnImage;
    }

    public void deleteFolder(File folder) {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File f: files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                }

                else {
                    f.delete();
                }
            }
        }

        folder.delete();
    }

    public void computerProperties() {
        File file;
        BufferedWriter fw;

        try {
            file = File.createTempFile("Computer Properties",".txt");
            file.deleteOnExit();
            fw = new BufferedWriter(new FileWriter(file));
            fw.write("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
            fw.write(System.getProperty("line.separator"));
            fw.write("Free memory (bytes): " + Runtime.getRuntime().freeMemory());
            fw.write(System.getProperty("line.separator"));
            long maxMemory = Runtime.getRuntime().maxMemory();
            fw.write("Maximum memory (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
            fw.write(System.getProperty("line.separator"));
            fw.write("Total memory available to JVM (bytes): " + Runtime.getRuntime().totalMemory());
            fw.write(System.getProperty("line.separator"));
            File[] roots = File.listRoots();

            for (File root : roots) {
                fw.write("File system root: " + root.getAbsolutePath());
                fw.write(System.getProperty("line.separator"));
                fw.write("Total space (bytes): " + root.getTotalSpace());
                fw.write(System.getProperty("line.separator"));
                fw.write("Free space (bytes): " + root.getFreeSpace());
                fw.write(System.getProperty("line.separator"));
                fw.write("Usable space (bytes): " + root.getUsableSpace());
            }

            fw.close();

            Desktop.getDesktop().open(file);
        }

        catch (Exception ex) {
            handle(ex);
        }
    }

    public void youTubeThumbnail() {
        if (yttnFrame != null) {
            closeAnimation(yttnFrame);
            yttnFrame.dispose();
        }

        yttnFrame = new JFrame();

        yttnFrame.setResizable(false);

        yttnFrame.setTitle("YouTube Thumbnail");

        yttnFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        yttnFrame.setIconImage(getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        JLabel VideoID = new JLabel("Enter a valid YouTube video ID");

        VideoID.setFont(weatherFontBig);

        VideoID.setForeground(navy);

        JPanel TopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        TopPanel.add(VideoID);

        ParentPanel.add(TopPanel);

        yttnField = new JTextField(30);

        yttnField.setBorder(new LineBorder(navy,5,false));

        yttnField.addActionListener(e -> getYTTN.doClick());

        yttnField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if(yttnField.getText().length() >= 11 && !(evt.getKeyChar()==KeyEvent.VK_DELETE || evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
                    beep();
                    evt.consume();
                }
            }
        });

        yttnField.addKeyListener(new java.awt.event.KeyAdapter() {
            char[] ValidChars = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                    'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2',
                    '3','4','5','6','7','8','9','-','_'};

            public void keyTyped(java.awt.event.KeyEvent evt) {
                boolean InArray = false;

                for (char c : ValidChars) {
                    if (c == evt.getKeyChar()) {
                        InArray = true;
                        break;
                    }
                }

                if (!InArray) {
                    evt.consume();
                }
            }
        });

        yttnField.setFont(weatherFontSmall);

        yttnField.setBorder(new LineBorder(navy,5,false));

        JPanel MiddlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        MiddlePanel.add(yttnField);

        ParentPanel.add(MiddlePanel);

        getYTTN = new CyderButton("Get Thumbnail");

        getYTTN.setBorder(new LineBorder(navy,5,false));

        getYTTN.setColors(regularRed);

        getYTTN.setFocusPainted(false);

        getYTTN.setBackground(regularRed);

        getYTTN.setFont(weatherFontSmall);

        getYTTN.addActionListener(e -> {
            closeAnimation(yttnFrame);

            yttnFrame.dispose();

            String YouTubeID = yttnField.getText();

            if (YouTubeID.length() < 11) {
                inform("Sorry, " + username + ", but that's not a valid YouTube video ID.","", 400, 200);
            }

            else {
                String YouTubeURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";
                YouTubeURL = YouTubeURL.replace("REPLACE", YouTubeID);
                URL url;

                try {
                    url = new URL(YouTubeURL);

                    BufferedImage Thumbnail = ImageIO.read(url);

                    JFrame thumbnailFrame = new JFrame();

                    thumbnailFrame.setUndecorated(true);

                    thumbnailFrame.setTitle(YouTubeID);

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

                    parentPanel.setBorder(new LineBorder(navy,10,false));

                    parentPanel.setLayout(new BorderLayout());

                    thumbnailFrame.setContentPane(parentPanel);

                    JLabel PictureLabel = new JLabel(new ImageIcon(Thumbnail));

                    PictureLabel.setToolTipText("Open video " + YouTubeID);

                    PictureLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            internetConnect("https://www.youtube.com/watch?v=" + YouTubeID);
                        }
                    });

                    parentPanel.add(PictureLabel, BorderLayout.PAGE_START);

                    CyderButton closeYT = new CyderButton("Close");

                    closeYT.setColors(regularRed);

                    closeYT.setBorder(new LineBorder(navy,5,false));

                    closeYT.setFocusPainted(false);

                    closeYT.setBackground(regularRed);

                    closeYT.setFont(weatherFontSmall);

                    closeYT.addActionListener(ev -> {
                        closeAnimation(thumbnailFrame);
                        thumbnailFrame.dispose();
                    });

                    closeYT.setSize(thumbnailFrame.getX(),20);

                    parentPanel.add(closeYT,BorderLayout.PAGE_END);

                    parentPanel.repaint();

                    thumbnailFrame.pack();

                    thumbnailFrame.setVisible(true);

                    thumbnailFrame.setLocationRelativeTo(null);

                    thumbnailFrame.setResizable(false);

                    thumbnailFrame.setIconImage(getCyderIcon().getImage());
                }

                catch (Exception exc) {
                    inform("Sorry, " + getUsername() + ", but that's not a valid YouTube video ID.","", 400, 200);
                }
            }
        });

        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        ButtonPanel.add(getYTTN);

        ParentPanel.add(ButtonPanel);

        ParentPanel.setBorder(new LineBorder(new Color(0, 0, 0)));

        yttnFrame.add(ParentPanel);

        yttnFrame.pack();

        yttnFrame.setLocationRelativeTo(null);

        yttnFrame.setVisible(true);

        yttnFrame.setAlwaysOnTop(true);

        yttnFrame.setAlwaysOnTop(false);

        yttnField.requestFocus();
    }

    public void setCurrentBackgroundIndex(int i) {
        this.currentBackgroundIndex = i;
    }

    public int getCurrentBackgroundIndex() {
        return this.currentBackgroundIndex;
    }

    public File getCurrentBackground() {
        return validBackgroundPaths[currentBackgroundIndex];
    }

    public void getBackgroundSize() {
        ImageIcon Size = new ImageIcon(getCurrentBackground().toString());

        if (getUserData("FullScreen").equalsIgnoreCase("1")) {
            backgroundX = (int) getScreenSize().getWidth();
            backgroundY = (int) getScreenSize().getHeight();
        }

        else {
            backgroundX = Size.getIconWidth();
            backgroundY = Size.getIconHeight();
        }
    }

    public int getBackgroundX() {
        return this.backgroundX;
    }

    public int getBackgroundY() {
        return this.backgroundY;
    }

    public void setBackgroundX(int x) {
        this.backgroundX = x;
    }

    public void setBackgroundY(int y) {
        this.backgroundY = y;
    }

    public void initBackgrounds() {
        File dir = new File("src\\com\\cyder\\io\\users\\" + getUserUUID() + "\\Backgrounds");

        FilenameFilter PNGFilter = (dir1, filename) -> filename.endsWith(".png");

        validBackgroundPaths = dir.listFiles(PNGFilter);
    }

    public void openFile(String FilePath) {
        //use our custom text editor
        if (FilePath.endsWith(".txt")) {
            TextEditor te = new TextEditor(FilePath);
        }

        else if (FilePath.endsWith(".png")) {
            PhotoViewer pv = new PhotoViewer(new File(FilePath));
            pv.draw();
        }

        //use our own mp3 player
        else if (FilePath.endsWith(".mp3")) {
            mp3(new File(FilePath));
        }

        //welp just open it outside of the program :(
        else {
            Desktop OpenFile = Desktop.getDesktop();

            try {
                File FileToOpen = new File(FilePath);
                URI FileURI = FileToOpen.toURI();
                OpenFile.browse(FileURI);
            }

            catch (Exception e) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + FilePath);
                }

                catch(Exception ex) {
                    handle(ex);
                }
            }
        }
    }

    public void createHash() {
        if (hashFrame != null) {
            closeAnimation(hashFrame);

            hashFrame.dispose();
        }

        hashFrame = new JFrame();

        hashFrame.setResizable(false);

        hashFrame.setTitle("Hasher");

        hashFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        hashFrame.setIconImage(getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        ParentPanel.setLayout(new BoxLayout(ParentPanel, BoxLayout.Y_AXIS));

        JPanel InstPanel = new JPanel();

        InstPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel Instructions = new JLabel("Enter your password to be hashed");

        Instructions.setForeground(navy);

        Instructions.setFont(weatherFontSmall);

        InstPanel.add(Instructions);

        ParentPanel.add(InstPanel);

        hashField = new JPasswordField(15);

        hashField.setForeground(navy);

        hashField.setFont(weatherFontSmall);

        hashField.setBorder(new LineBorder(navy,5,false));

        hashField.addActionListener(e -> {
            char[] Hash = hashField.getPassword();

            if (Hash.length > 0) {
                String PrintHash = toHexString(getSHA(hashField.getPassword()));
                closeAnimation(hashFrame);
                hashFrame.dispose();
                inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
                StringSelection selection = new StringSelection(PrintHash);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        });

        JPanel FieldPanel = new JPanel();

        FieldPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        FieldPanel.add(hashField);

        ParentPanel.add(FieldPanel);

        JPanel ButtonPanel = new JPanel();

        ButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        CyderButton hashButton = new CyderButton("Hash");

        hashButton.setColors(regularRed);

        hashButton.setBackground(regularRed);

        hashButton.setBorder(new LineBorder(navy,5,false));

        hashButton.setFont(weatherFontSmall);

        hashButton.addActionListener(e -> {
            String PrintHash = toHexString(getSHA(hashField.getPassword()));
            closeAnimation(hashFrame);
            hashFrame.dispose();
            inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
            StringSelection selection = new StringSelection(PrintHash);
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });

        hashButton.setFocusPainted(false);

        ButtonPanel.add(hashButton);

        ParentPanel.add(ButtonPanel);

        hashFrame.add(ParentPanel);

        hashFrame.pack();

        hashFrame.setLocationRelativeTo(null);

        hashFrame.setVisible(true);

        hashFrame.setAlwaysOnTop(true);
    }

    public void directorySearch() {
        if (dirFrame != null) {
            closeAnimation(dirFrame);
            dirFrame.dispose();
        }

        dirFrame = new JFrame();
        dirFrame.setTitle("Directory Search");
        dirFrame.setResizable(false);
        dirFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dirFrame.setIconImage(getCyderIcon().getImage());

        dirSearchParentPanel = new JPanel();
        dirSearchParentPanel.setLayout(new BorderLayout());

        dirField = new JTextField(40);
        dirField.setSelectionColor(selectionColor);
        dirField.setText(System.getProperty("user.dir"));
        dirField.setFont(weatherFontSmall);
        dirField.setForeground(navy);

        dirField.addActionListener(e -> {
            String newDir = dirField.getText();
            File ChosenDir = new File(newDir);

            if (ChosenDir.exists()) {
                if (ChosenDir.isDirectory()) {
                    directoryList = new JList(ChosenDir.listFiles());
                    File[] Files = ChosenDir.listFiles();
                    String[] Names = new String[0];
                    if (Files != null) {
                        Names = new String[Files.length];
                    }
                    if (Files != null) {
                        for (int i = 0 ; i < Files.length ; i++) {
                            Names[i] = Files[i].getName();
                        }
                    }

                    directoryNameList = new JList(Names);
                    directoryNameList.setFont(weatherFontSmall);
                    directoryNameList.setForeground(navy);
                    directoryNameList.setSelectionBackground(selectionColor);
                    directoryNameList.addMouseListener(directoryListener);
                    directoryNameList.addKeyListener(directoryEnterListener);
                    dirScroll.setViewportView(directoryNameList);
                    dirScroll.revalidate();
                    dirScroll.repaint();
                    dirSearchParentPanel.revalidate();
                    dirSearchParentPanel.repaint();
                    dirFrame.revalidate();
                    dirFrame.repaint();
                }

                else if (ChosenDir.isFile()) {
                    openFile(ChosenDir.getAbsolutePath());
                }
            }

            else {
                beep();
            }
        });

        JPanel dirFieldPanel = new JPanel();

        dirField.setBorder(new LineBorder(navy,5,false));

        dirFieldPanel.add(dirField);

        dirSearchParentPanel.add(dirFieldPanel, BorderLayout.PAGE_START);

        File[] DirFiles = new File(System.getProperty("user.dir")).listFiles();

        directoryList = new JList(DirFiles);

        directoryList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        File ChosenDir = new File(System.getProperty("user.dir"));

        directoryList = new JList(ChosenDir.listFiles());

        File[] Files = ChosenDir.listFiles();

        String[] Names = new String[0];

        if (Files != null) {
            Names = new String[Files.length];
        }

        if (Files != null) {
            for (int i = 0 ; i < Files.length ; i++) {
                Names[i] = Files[i].getName();
            }
        }

        directoryNameList = new JList(Names);

        directoryNameList.setFont(weatherFontSmall);

        directoryNameList.setSelectionBackground(selectionColor);

        directoryNameList.setForeground(navy);

        directoryNameList.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList<String> theList = (JList) mouseEvent.getSource();

                if (mouseEvent.getClickCount() == 2) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());

                    if (index >= 0) {
                        File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                        if (ChosenDir.isDirectory()) {
                            dirField.setText(ChosenDir.toString());

                            directoryList = new JList(ChosenDir.listFiles());

                            File[] Files = ChosenDir.listFiles();

                            String[] Names = new String[0];
                            if (Files != null) {
                                Names = new String[Files.length];
                            }

                            for (int i = 0 ; i < Files.length ; i++) {
                                Names[i] = Files[i].getName();
                            }

                            directoryNameList = new JList(Names);

                            directoryNameList.setFont(weatherFontSmall);
                            directoryNameList.setForeground(navy);

                            directoryNameList.addMouseListener(directoryListener);

                            directoryNameList.setSelectionBackground(selectionColor);

                            directoryNameList.addKeyListener(directoryEnterListener);

                            dirScroll.setViewportView(directoryNameList);

                            dirScroll.revalidate();

                            dirScroll.repaint();

                            dirSearchParentPanel.revalidate();

                            dirSearchParentPanel.repaint();

                            dirFrame.revalidate();

                            dirFrame.repaint();
                        }

                        else if (ChosenDir.isFile()) {
                            openFile(ChosenDir.getAbsolutePath());
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent arg0) {

            }

            @Override
            public void mouseReleased(MouseEvent arg0) {

            }
        });

        directoryNameList.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                int index = directoryNameList.getSelectedIndex();

                if (index >= 0) {
                    File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                    if (ChosenDir.isDirectory()) {
                        dirField.setText(ChosenDir.toString());

                        directoryList = new JList(ChosenDir.listFiles());

                        File[] Files = ChosenDir.listFiles();

                        String[] Names = new String[0];
                        if (Files != null) {
                            Names = new String[Files.length];
                        }

                        for (int i = 0; i < Files.length; i++) {
                            Names[i] = Files[i].getName();
                        }

                        directoryNameList = new JList(Names);

                        directoryNameList.setFont(weatherFontSmall);

                        directoryNameList.setForeground(navy);

                        directoryNameList.setSelectionBackground(selectionColor);

                        directoryNameList.addMouseListener(directoryListener);

                        directoryNameList.addKeyListener(directoryEnterListener);

                        dirScroll.setViewportView(directoryNameList);

                        dirScroll.revalidate();

                        dirScroll.repaint();

                        dirSearchParentPanel.revalidate();

                        dirSearchParentPanel.repaint();

                        dirFrame.revalidate();

                        dirFrame.repaint();
                    }

                    else if (ChosenDir.isFile()) {
                        openFile(ChosenDir.getAbsolutePath());
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        dirScroll = new CyderScrollPane(directoryNameList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        dirScroll.setThumbColor(regularRed);

        dirScroll.setFont(weatherFontSmall);
        dirScroll.setForeground(navy);
        dirScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(navy,5,false)));

        dirSearchParentPanel.add(dirScroll, BorderLayout.CENTER);

        dirSearchParentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        dirFrame.add(dirSearchParentPanel);
        dirFrame.pack();
        dirFrame.setLocationRelativeTo(null);
        dirFrame.setVisible(true);
        dirField.requestFocus();
    }

    private MouseListener directoryListener = new MouseListener() {
        public void mouseClicked(MouseEvent mouseEvent) {
            JList theList = (JList) mouseEvent.getSource();

            if (mouseEvent.getClickCount() == 2) {
                int index = theList.locationToIndex(mouseEvent.getPoint());

                if (index >= 0) {
                    File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                    if (ChosenDir.isDirectory()) {
                        dirField.setText(ChosenDir.toString());

                        directoryList = new JList(ChosenDir.listFiles());

                        File[] Files = ChosenDir.listFiles();

                        String[] Names = new String[Files.length];

                        for (int i = 0 ; i < Files.length ; i++) {
                            Names[i] = Files[i].getName();
                        }

                        directoryNameList = new JList(Names);

                        directoryNameList.setFont(weatherFontSmall);
                        directoryNameList.setForeground(navy);
                        directoryNameList.setSelectionBackground(selectionColor);

                        directoryNameList.addMouseListener(directoryListener);

                        directoryNameList.addKeyListener(directoryEnterListener);

                        dirScroll.setViewportView(directoryNameList);

                        dirScroll.revalidate();

                        dirScroll.repaint();

                        dirSearchParentPanel.revalidate();

                        dirSearchParentPanel.repaint();

                        dirFrame.revalidate();

                        dirFrame.repaint();
                    }

                    else if (ChosenDir.isFile()) {
                        openFile(ChosenDir.getAbsolutePath());
                    }
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent arg0) {

        }

        @Override
        public void mouseReleased(MouseEvent arg0) {

        }
    };

    private KeyListener directoryEnterListener = new KeyListener()
    {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            int index = directoryNameList.getSelectedIndex();

            if (index >= 0) {
                File ChosenDir = (File) directoryList.getModel().getElementAt(index);

                if (ChosenDir.isDirectory()) {
                    dirField.setText(ChosenDir.toString());

                    directoryList = new JList(ChosenDir.listFiles());

                    File[] Files = ChosenDir.listFiles();

                    String[] Names = new String[Files.length];

                    for (int i = 0 ; i < Files.length ; i++) {
                        Names[i] = Files[i].getName();
                    }

                    directoryNameList = new JList(Names);

                    directoryNameList.setFont(new Font("Sans Serif",Font.PLAIN, 18));

                    directoryNameList.addMouseListener(directoryListener);

                    directoryNameList.addKeyListener(directoryEnterListener);

                    dirScroll.setViewportView(directoryNameList);

                    dirScroll.revalidate();

                    dirScroll.repaint();

                    dirSearchParentPanel.revalidate();

                    dirSearchParentPanel.repaint();

                    dirFrame.revalidate();

                    dirFrame.repaint();
                }

                else if (ChosenDir.isFile()) {
                    openFile(ChosenDir.getAbsolutePath());
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    };

    private void addNote()
    {
        if (newNoteFrame != null)
        {
            closeAnimation(newNoteFrame);

            newNoteFrame.dispose();
        }

        newNoteFrame = new JFrame();

        newNoteFrame.setResizable(false);

        newNoteFrame.setTitle("New note");

        newNoteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        newNoteFrame.setIconImage(getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        JLabel FileNameLabel = new JLabel("Note Title");

        FileNameLabel.setFont(weatherFontSmall);

        JPanel TopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        TopPanel.add(FileNameLabel,SwingConstants.CENTER);

        ParentPanel.add(TopPanel,SwingConstants.CENTER);

        newNoteField = new JTextField(30);

        newNoteField.setFont(weatherFontSmall);

        newNoteField.setForeground(navy);

        newNoteField.setBorder(new LineBorder(navy,5,false));

        newNoteField.setSelectionColor(selectionColor);

        JPanel MiddlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        MiddlePanel.add(newNoteField);

        ParentPanel.add(MiddlePanel);

        JLabel NoteTextLabel = new JLabel("Note Contents");

        NoteTextLabel.setFont(weatherFontSmall);

        JPanel BottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        BottomPanel.add(NoteTextLabel);

        ParentPanel.add(BottomPanel);

        newNoteArea = new JTextArea(20,20);

        newNoteArea.setFont(weatherFontSmall);

        newNoteArea.setAutoscrolls(false);

        newNoteArea.setLineWrap(true);

        newNoteArea.setWrapStyleWord(true);

        newNoteArea.setSelectedTextColor(selectionColor);

        newNoteArea.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane NewNoteScroll = new CyderScrollPane(newNoteArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        NewNoteScroll.setThumbColor(regularRed);

        NewNoteScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        NewNoteScroll.getViewport().setBorder(null);

        NewNoteScroll.setViewportBorder(null);

        NewNoteScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(navy,5,false)));

        NewNoteScroll.setPreferredSize(new Dimension(570,780));

        ParentPanel.add(NewNoteScroll);

        CyderButton submitNewNote = new CyderButton("Create Note");

        submitNewNote.setBorder(new LineBorder(navy,5,false));

        submitNewNote.setFocusPainted(false);

        submitNewNote.setColors(regularRed);

        submitNewNote.setBackground(regularRed);

        submitNewNote.setFont(weatherFontSmall);

        submitNewNote.addActionListener(e -> {
            try {
                BufferedWriter NoteWriter = new BufferedWriter(new FileWriter(
                        "src\\com\\cyder\\io\\users\\" + getUserUUID() + "\\Notes\\" + newNoteField.getText() + ".txt",true));
                newNoteArea.write(NoteWriter);
                NoteWriter.close();
            }

            catch (Exception ex) {
                handle(ex);
            }

            closeAnimation(newNoteFrame);

            newNoteFrame.dispose();

            initializeNotesList();

            noteListScroll.setViewportView(fileSelectionList);

            noteListScroll.revalidate();
        });

        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        ButtonPanel.add(submitNewNote);

        ButtonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        ParentPanel.add(ButtonPanel);

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        newNoteFrame.add(ParentPanel);

        newNoteFrame.pack();

        newNoteFrame.setLocationRelativeTo(null);

        newNoteFrame.setVisible(true);

        newNoteField.requestFocus();
    }

    private void initializeNotesList() {
        File dir = new File("src\\com\\cyder\\io\\users\\" + username + "\\Notes");
        noteList = new LinkedList<>();
        noteNameList = new LinkedList<>();

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith((".txt"))) {
                noteList.add(file.getAbsoluteFile());
                noteNameList.add(file.getName().replace(".txt", ""));
            }
        }

        String[] NotesArray = new String[noteNameList.size()];
        NotesArray = noteNameList.toArray(NotesArray);
        fileSelectionList = new JList(NotesArray);
        fileSelectionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && fileSelectionList.getSelectedIndex() != -1) {
                    openNote.doClick();
                }
            }
        });

        fileSelectionList.setFont(weatherFontSmall);

        fileSelectionList.setForeground(navy);

        fileSelectionList.setSelectionBackground(selectionColor);
    }

    public void note() {
        if (noteFrame != null) {
            closeAnimation(noteFrame);
            noteFrame.dispose();
        }

        noteFrame = new JFrame();

        noteFrame.setResizable(false);

        noteFrame.setTitle("Notes");

        noteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        noteFrame.setResizable(false);

        noteFrame.setIconImage(getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        ParentPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(navy,5,false)));

        initializeNotesList();

        fileSelectionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        fileSelectionList.setFont(weatherFontSmall);

        fileSelectionList.setForeground(navy);

        fileSelectionList.setSelectionBackground(selectionColor);

        noteListScroll = new CyderScrollPane(fileSelectionList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        noteListScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(navy,5,false)));

        noteListScroll.setThumbColor(regularRed);

        noteListScroll.setPreferredSize(new Dimension(570,300));

        noteListScroll.setFont(weatherFontSmall);

        noteListScroll.setForeground(navy);

        ParentPanel.add(noteListScroll);

        JPanel ButtonPanel = new JPanel();

        ButtonPanel.setLayout(new GridLayout(1,3,5,5));

        CyderButton addNote = new CyderButton("Add Note");

        addNote.setColors(regularRed);

        addNote.setBorder(new LineBorder(navy,5,false));

        ButtonPanel.add(addNote);

        addNote.setFocusPainted(false);

        addNote.setBackground(regularRed);

        addNote.setFont(buttonFont);

        addNote.addActionListener(e -> addNote());

        openNote = new CyderButton("Open Note");

        openNote.setColors(regularRed);

        ButtonPanel.add(openNote);

        openNote.setFocusPainted(false);

        openNote.setBorder(new LineBorder(navy,5,false));

        openNote.setBackground(regularRed);

        openNote.setFont(buttonFont);

        openNote.addActionListener(e -> {
            List<?> ClickedSelectionList = fileSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < noteNameList.size() ; i++) {
                    if (ClickedSelection.equals(noteNameList.get(i))) {
                        ClickedSelectionPath = noteList.get(i);
                        break;
                    }
                }

                openNote(ClickedSelectionPath);
            }
        });

        CyderButton deleteNote = new CyderButton("Delete Note");

        deleteNote.setColors(regularRed);

        deleteNote.setBorder(new LineBorder(navy,5,false));

        ButtonPanel.add(deleteNote);

        deleteNote.setFocusPainted(false);

        deleteNote.setBackground(regularRed);

        deleteNote.setFont(buttonFont);

        deleteNote.addActionListener(e -> {
            List<?> ClickedSelectionList = fileSelectionList.getSelectedValuesList();

            if (!ClickedSelectionList.isEmpty()) {
                String ClickedSelection = ClickedSelectionList.get(0).toString();

                File ClickedSelectionPath = null;

                for (int i = 0; i < noteNameList.size() ; i++) {
                    if (ClickedSelection.equals(noteNameList.get(i))) {
                        ClickedSelectionPath = noteList.get(i);
                        break;
                    }
                }

                if (ClickedSelectionPath != null) {
                    ClickedSelectionPath.delete();
                }
                initializeNotesList();
                noteListScroll.setViewportView(fileSelectionList);
                noteListScroll.revalidate();
            }
        });

        ParentPanel.add(ButtonPanel);

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        noteFrame.add(ParentPanel);

        noteFrame.setVisible(true);

        noteFrame.pack();

        noteFrame.requestFocus();

        noteFrame.setLocationRelativeTo(null);
    }

    public void setHideOnClose(boolean b) {
        this.hideOnClose = b;
    }

    public boolean getHideOnClose() {
        return this.hideOnClose;
    }

    public String getUserCity() {
        return this.userCity;
    }

    public String getUserState() {
        return this.userState;
    }

    public String getUserOS() {
        return this.os;
    }

    public String getUserCountryAbr() {
        return this.userCountryAbr;
    }

    public String getUserIP() {
        return this.userIP;
    }

    public String getUserFlag() {
        return this.userFlagURL;
    }

    public String getUserPostalCode() {
        return this.userPostalCode;
    }

    public String getUserISP() {
        return this.isp;
    }

    public boolean ShouldISwitch() {
        return (validBackgroundPaths.length > currentBackgroundIndex + 1 && validBackgroundPaths.length > 1);
    }

    public void readUserData() {
        try {
            userData.clear();

            BufferedReader dataReader = new BufferedReader(new FileReader(
                    "src\\com\\cyder\\io\\users\\" + getUserUUID() + "\\Userdata.txt"));

            String Line = dataReader.readLine();

            while (Line != null) {
                String[] parts = Line.split(":");

                userData.add(new NST(parts[0], parts[1]));

                Line = dataReader.readLine();
            }

            dataReader.close();
        }

        catch(Exception e) {
            handle(e);
        }
    }

    public void writeUserData(String name, String value) {
        try {
            BufferedWriter userWriter = new BufferedWriter(new FileWriter(
                    "src\\com\\cyder\\io\\users\\" + getUserUUID() + "\\Userdata.txt", false));

            for (NST data : userData) {
                if (data.getName().equalsIgnoreCase(name)) {
                    data.setDescription(value);
                }

                userWriter.write(data.getName() + ":" + data.getDescription());
                userWriter.newLine();
            }

            userWriter.close();

            readUserData();
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public String getUserData(String name) {
        readUserData();

        for (NST data : userData) {
            if (data.getName().equalsIgnoreCase(name)) {
                return data.getDescription();
            }
        }

        return null;
    }

    public void javaProperties() {
        ArrayList<String> PropertiesList = new ArrayList<>();
        Properties Props = System.getProperties();
        Enumeration<?> keys = Props.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) Props.get(key);
            PropertiesList.add(key + ": " + value);
        }

        File file;
        BufferedWriter fw;

        try {
            file = File.createTempFile("Properties",".txt");
            file.deleteOnExit();
            fw = new BufferedWriter(new FileWriter(file));

            for (int i = 1 ; i < PropertiesList.size() ; i++) {
                String Line = PropertiesList.get(i);
                fw.write(Line);
                fw.write(System.getProperty("line.separator"));
            }

            fw.close();

            Desktop.getDesktop().open(file);
        }

        catch (Exception ex) {
            handle(ex);
        }
    }

    public boolean empytStr(String s) {
        return (s == null ? null: (s == null) || (s.trim().equals("")) || (s.trim().length() == 0));
    }

    public void resizeImages() {
        try {
            for (int i = 0 ; i < getValidBackgroundPaths().length ; i++) {
                File UneditedImage = getValidBackgroundPaths()[i];

                BufferedImage currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                setBackgroundX(currentImage.getWidth());
                setBackgroundY(currentImage.getHeight());

                double aspectRatio = getAspectRatio(currentImage);
                int imageType = currentImage.getType();

                if (getBackgroundX() > getScreenWidth() || getBackgroundY() > getScreenHeight()) {
                    inform("Resizing the background image:\n" + getValidBackgroundPaths()[i].getName() + "\nsince it exceeds your screen size.","", 700, 200);
                }

                while (getBackgroundX() > getScreenWidth() || getBackgroundY() > getScreenHeight()) {
                    currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                    int width = (int) (currentImage.getWidth() / aspectRatio);
                    int height = (int) (currentImage.getHeight() / aspectRatio);

                    BufferedImage saveImage = resizeImage(currentImage, imageType, width, height);

                    ImageIO.write(saveImage, "png", new File(getValidBackgroundPaths()[i].toString()));

                    setBackgroundX(saveImage.getWidth());
                    setBackgroundY(saveImage.getHeight());
                    getValidBackgroundPaths();
                }
            }

            getValidBackgroundPaths();
        }

        catch (Exception ex) {
            handle(ex);
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int type, int img_width, int img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();

        return resizedImage;
    }

    private double getAspectRatio(BufferedImage im) {
        return ((double) im.getWidth() / (double) im.getHeight());
    }

    public int getScreenResolution() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public void handle(Exception e) {
        try {
            String eFileString = "src\\com\\cyder\\io\\Errors\\" + errorTime() + ".error";
            File eFile = new File(eFileString);
            eFile.createNewFile();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String stackTrack = sw.toString();
            int lineNumber = e.getStackTrace()[0].getLineNumber();
            Class c = e.getClass();

            String write = "Error thrown by line: " + lineNumber + " from\n" + c +
                    "\n\nStack Trace:\n\n" + stackTrack;

            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(eFileString));
            errorWriter.write(write);
            errorWriter.newLine();
            errorWriter.flush();
            errorWriter.close();

            if (getUserData("SilenceErrors").equals("1"))
                return;
            openFile(eFileString);
        }

        catch (Exception ex) {
            if (debugMode) {
                e.printStackTrace();
            }
        }
    }

    public void setDebugButton(boolean b) {
        this.debugButton = b;
    }

    public boolean getDebugButton() {
        return this.debugButton;
    }

    public void setConsoleClock(boolean b) {
        this.consoleClock = b;
    }

    public boolean getConsoleClock() {
        return this.consoleClock;
    }

    public boolean confirmation(String input) {
        return (input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y"));
    }

    public boolean getAlwaysOnTopMode() {
        return this.alwaysOnTop;
    }

    public void setAlwaysOnTopMode(boolean b) {
        this.alwaysOnTop = b;
    }

    private void startScrolling() {
        musicScroll = new ScrollLabel(musicTitleLabel);
    }

    private void stopScrolling() {
        if (musicScroll != null)
            musicScroll.kill();
    }

    private class ScrollLabel {
        private JLabel effectLabel;
        boolean scroll;

        ScrollLabel(JLabel effLab) {
            effectLabel = effLab;
            scroll = true;

            try {
                int maxLen = 30;
                int delay = 200;
                String title = musicFiles[currentMusicIndex].getName().replace(".mp3","");
                int len = title.length();

                if (len > maxLen) {

                    scroll = true;

                    new Thread(() -> {
                        try {
                            while (true) {
                                String localTitle = musicFiles[currentMusicIndex].getName().replace(".mp3","");
                                int localLen = localTitle.length();
                                effectLabel.setText(localTitle.substring(0,26));

                                if (!scroll)
                                    return;

                                Thread.sleep(2000);

                                for (int i = 0 ; i <= localLen - 26; i++) {
                                    if (!scroll)
                                        return;

                                    effectLabel.setText(localTitle.substring(i, i + 26));

                                    if (!scroll)
                                        return;

                                    Thread.sleep(delay);
                                }

                                Thread.sleep(2000);

                                for (int i = localLen - 26 ; i >= 0 ; i--) {
                                    if (!scroll)
                                        return;

                                    effectLabel.setText(localTitle.substring(i, i + 26));

                                    if (!scroll)
                                        return;

                                    Thread.sleep(delay);
                                }
                            }
                        }

                        catch (Exception e) {
                            handle(e);
                        }
                    }).start();
                }

                else {
                    effectLabel.setText(title);
                }
            }

            catch (Exception e) {
                handle(e);
            }
        }

        public void kill() {
            scroll = false;
            effectLabel.setText("No Audio Currently Playing");
        }
    }

    public String getDeprecatedUUID() {
        String uuid = generateUUID();
        uuid = uuid.substring(0,9);
        return ("DeprecatedUser-" + uuid);
    }
}