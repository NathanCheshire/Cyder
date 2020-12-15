
package com.cyder.utilities;

import com.cyder.exception.FatalException;
import com.cyder.handler.PhotoViewer;
import com.cyder.handler.TextEditor;
import com.cyder.obj.NST;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.widgets.MPEGPlayer;
import javazoom.jl.player.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class GeneralUtil {

    //todo enums package

    //static strings used for test cases
    public static final String HERE = "here";

    public static final String LENGTH_ZERO = "";
    public static final String LENGTH_ONE = "0";
    public static final String LENGTH_TWO = "01";
    public static final String LENGTH_THREE = "012";
    public static final String LENGTH_FOUR = "0123";
    public static final String LENGTH_FIVE = "01234";
    public static final String LENGTH_SIX = "102345";
    public static final String LENGTH_SEVEN = "0123456";
    public static final String LENGTH_EIGHT = "01234567";
    public static final String LENGTH_NINE = "012345678";

    public static final String QUICK_BROWN_FOX = "The quick brown fox jumps over the lazy dog";

    public static final String SUPER_LONG = "pneumonoultramicroscopicsilicovolcanoconiosis," +
                                             "pneumonoultramicroscopicsilicovolcanoconiosi," +
                                             "pneumonoultramicroscopicsilicovolcanoconiosis!" +
                                             "There, I said it!";
    //integer bounds
    public static final int INFINITY = Integer.MAX_VALUE;
    public static final int NEG_INFINITY = Integer.MIN_VALUE;

    //public fonts
    public Font weatherFontSmall = new Font("Segoe UI Black", Font.BOLD, 20);
    public Font weatherFontBig = new Font("Segoe UI Black", Font.BOLD, 30);
    public Font loginFont = new Font("Comic Sans MS", Font.BOLD, 30);
    public Font defaultFontSmall = new Font("tahoma", Font.BOLD, 15);
    public Font defaultFont = new Font("tahoma", Font.BOLD, 30);
    public Font tahoma = new Font("tahoma", Font.BOLD, 20);

    //public colors
    public Color selectionColor = new Color(204,153,0);
    public Color regularGreen = new Color(60, 167, 92);
    public Color regularBlue = new Color(38,168,255);
    public Color calculatorOrange = new Color(255,140,0);
    public Color regularRed = new Color(223,85,83);
    public Color intellijPink = new Color(236,64,122);
    public Color consoleColor = new Color(39, 40, 34);
    public Color tooltipBorderColor = new Color(26, 32, 51);
    public Color tooltipForegroundColor = new Color(85,85,255);
    public Color tooltipBackgroundColor = new Color(0,0,0);
    public Color vanila = new Color(252, 251, 227);
    public Color defaultColor = new Color(252, 251, 227);
    public Color tttblue = new Color(71, 81, 117);
    public Color navy = new Color(26, 32, 51);

    //uservars
    private LinkedList<NST> userData = new LinkedList<>();
    private static String userUUID;
    private static String username;
    private static Color usercolor;
    private static Font userfont;
    private static String os;
    private static int currentBackgroundIndex = 0;
    private static File[] validBackgroundPaths;
    private boolean consoleClock;

    //console orientation var
    public static int CYDER_UP = 0;
    public static int CYDER_RIGHT = 1;
    public static int CYDER_DOWN = 2;
    public static int CYDER_LEFT = 3;
    private int consoleDirection;

    //boolean vars
    private boolean handledMath;
    private boolean hideOnClose;
    private boolean oneMathPrint;
    private boolean alwaysOnTop;

    //scrolling var
    private int currentDowns;

    //drawing vars
    private JFrame pictureFrame;
    private CyderButton closeDraw;

    //backbround vars
    private int backgroundX;
    private int backgroundY;

    //static player so only one instance ever exists
    public static MPEGPlayer CyderPlayer;
    private static Player player;

    //time util
    public String errorTime() {
        DateFormat dateFormat = new SimpleDateFormat("MMddyy-HH-mmaa");
        Date date = new Date();
        return dateFormat.format(date);
    }
    //time util
    public String userTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEEEEE, MM/dd/yyyy hh:mmaa zzz");
        return dateFormatter.format(Time);
    }
    //time util
    public String consoleTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEEEEE h:mmaa");
        return dateFormatter.format(Time);
    }
    //time util
    public String consoleSecondTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEEEEE h:mm:ssaa");
        return dateFormatter.format(Time);
    }
    //time util
    public String weatherTime() {
        Date Time = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm:ss aa zzz EEEEEEEEEEEEE MMMMMMMMMMMMMMMMMM dd, yyyy");
        return dateFormatter.format(Time);
    }

    //num util
    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    //This was the best bodge I ever pulled off
    public File getFile() {
        try {
            File WhereItIs = new File("src/com/cyder/io/jars/FileChooser.jar");
            Desktop.getDesktop().open(WhereItIs);
            File f = new File("File.txt");
            f.delete();
            while (!f.exists()) {
                Thread.onSpinWait();
            }

            Thread.sleep(200);

            BufferedReader waitReader = new BufferedReader(new FileReader("File.txt")); //todo move to temp dir
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

    //networ kutil
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

    //todo make dir for title, released, and such
    public boolean released() {
        return false;
    }

    //change to user by user so in user util
    public String getIPKey() {
        try {
            BufferedReader keyReader = new BufferedReader(new FileReader("src/com/cyder/io/text/keys.txt"));
            String line = "";

            while ((line = keyReader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts[0].equals("ip")) {
                    return parts[1];
                }
            }
        }

        catch (Exception ex) {
            handle(ex);
        }

        return null;
    }

    //change to user by user so in user util
    public String getWeatherKey() {
        try {
            BufferedReader keyReader = new BufferedReader(new FileReader("src/com/cyder/io/text/keys.txt"));
            String line = "";

            while ((line = keyReader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts[0].equals("weather")) {
                    return parts[1];
                }

            }
        }

        catch (Exception ex) {
            handle(ex);
        }

        return null;
    }

    //text popups
    public void debugMenu(JTextPane outputArea) {
        try {
            DecimalFormat gFormater = new DecimalFormat("##.###");
            double gBytes = Double.parseDouble(gFormater.format((((double) Runtime.getRuntime().freeMemory()) / 1024 / 1024 / 1024)));
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface netIn = NetworkInterface.getByInetAddress(address);

            InternetProtocolUtil ipu = new InternetProtocolUtil();

            BufferedImage flag = ImageIO.read(new URL(new InternetProtocolUtil().getUserFlagURL()));

            double x = flag.getWidth();
            double y = flag.getHeight();

            outputArea.insertIcon(new ImageIcon(resizeImage(flag, 1, (int) (2 * x), (int) (2 * y))));

            NetworkUtil nu = new NetworkUtil();
            SystemUtil su = new SystemUtil();

            String[] lines = {"Time requested: " + weatherTime(),
                    "ISP: " + ipu.getIsp(),
                    "IP: " + ipu.getUserIP(),
                    "Postal Code: " + ipu.getUserPostalCode(),
                    "City: " + ipu.getUserCity(),
                    "State: " + ipu.getUserState(),
                    "Country: " + ipu.getUserCountry() + " (" + ipu.getUserCountryAbr() + ")",
                    "Latitude: " + ipu.getLat() + " Degrees N",
                    "Longitude: " + ipu.getLon() + " Degrees W",
                    "latency: " + nu.latency(10000) + " ms",
                    "Google Reachable: " + nu.siteReachable("https://www.google.com"),
                    "YouTube Reachable: " + nu.siteReachable("https://www.youtube.com"),
                    "Apple Reachable: " + nu.siteReachable("https://www.apple.com"),
                    "Microsoft Reachable: " + nu.siteReachable("https://www.microsoft.com//en-us//"),
                    "User Name: " + su.getWindowsUsername(),
                    "Computer Name: " + su.getComputerName(),
                    "Available Cores: " + Runtime.getRuntime().availableProcessors(),
                    "Available Memory: " + gBytes + " GigaBytes",
                    "Operating System: " + os,
                    "Java Version: " + System.getProperty("java.version"),
                    "Network Interface Name: " + netIn.getName(),
                    "Network Interface Display Name: " + netIn.getDisplayName(),
                    "Network MTU: " + netIn.getMTU(),
                    "Host Address: " + address.getHostAddress(),
                    "Local Host Address: " + address.getLocalHost(),
                    "Loopback Address: " + address.getLoopbackAddress()};

            createAndOpenTmpFile("DebugProperties",".txt",lines);
        }

        catch (Exception e) {
            handle(e);
        }
    }

    //security util
    public byte[] getSHA(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();

            for (char c : input)
                sb.append(c);

            return md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
        }

        catch (Exception ex) {
            handle(ex);
        }

        return null;
    }
    //security util
    public String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
    //security util
    public String getDeprecatedUUID() {
        return "VoidUser-" + generateUUID().substring(0,8);
    }
    public String generateUUID() {
        try {
            MessageDigest salt = MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            return UUID.nameUUIDFromBytes(salt.digest()).toString();
        }

        catch (Exception e) {
            handle(e);
        }

        return null;
    }
    //security util
    public boolean checkPassword(String name, String pass) {
        try {
            File[] users = new File("src/users").listFiles();
            LinkedList<File> userDataFiles = new LinkedList<>();

            for (File f : users) {
                if (!f.getName().contains("DeprecatedUser")) {
                    userDataFiles.add(new File(f.getAbsolutePath() + "/Userdata.txt"));
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

    //todo make widget
    public void colorConverter() {
        CyderFrame colorFrame = new CyderFrame(400,300,new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        colorFrame.setTitle("Color Converter");

        JLabel hexLabel = new JLabel("HEX:");
        hexLabel.setFont(weatherFontSmall);
        hexLabel.setForeground(navy);
        hexLabel.setBounds(30, 110,70, 30);
        colorFrame.getContentPane().add(hexLabel);

        JLabel rgbLabel = new JLabel("RGB:");
        rgbLabel.setFont(weatherFontSmall);
        rgbLabel.setForeground(navy);
        rgbLabel.setBounds(30, 180,70,30);
        colorFrame.getContentPane().add(rgbLabel);

        JTextField colorBlock = new JTextField();
        colorBlock.setBackground(navy);
        colorBlock.setFocusable(false);
        colorBlock.setCursor(null);
        colorBlock.setToolTipText("Color Preview");
        colorBlock.setBorder(new LineBorder(navy, 5, false));
        colorBlock.setBounds(330, 100, 40, 120);
        colorFrame.getContentPane().add(colorBlock);

        JTextField rgbField = new JTextField(navy.getRed() + "," + navy.getGreen() + "," + navy.getBlue());

        JTextField hexField = new JTextField(String.format("#%02X%02X%02X", navy.getRed(), navy.getGreen(), navy.getBlue()).replace("#",""));
        hexField.setForeground(navy);
        hexField.setFont(weatherFontBig);
        hexField.setBackground(new Color(0,0,0,0));
        hexField.setSelectionColor(selectionColor);
        hexField.setToolTipText("Hex Value");
        hexField.setBorder(new LineBorder(navy,5,false));
        JTextField finalHexField1 = hexField;
        JTextField finalRgbField = rgbField;
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = hextorgbColor(finalHexField1.getText());
                    finalRgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
                    colorBlock.setBackground(c);
                }

                catch (Exception ignored) {}
            }
        });
        hexField.setBounds(100, 100,220, 50);
        hexField.setOpaque(false);
        colorFrame.getContentPane().add(hexField);

        rgbField.setForeground(navy);
        rgbField.setFont(weatherFontBig);
        rgbField.setBackground(new Color(0,0,0,0));
        rgbField.setSelectionColor(selectionColor);
        rgbField.setToolTipText("RGB Value");
        rgbField.setBorder(new LineBorder(navy,5,false));
        JTextField finalRgbField1 = rgbField;
        rgbField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    String[] parts = finalRgbField1.getText().split(",");
                    Color c = new Color(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                    hexField.setText(rgbtohexString(c));
                    colorBlock.setBackground(c);
                }

                catch (Exception ignored) {}
            }
        });
        rgbField.setBounds(100, 170,220, 50);
        rgbField.setOpaque(false);
        colorFrame.getContentPane().add(rgbField);

        colorFrame.setVisible(true);
        colorFrame.setLocationRelativeTo(null);
    }

    //color util
    public Color hextorgbColor(String hex) {
        return new Color(Integer.valueOf(hex.substring(0,2),16),Integer.valueOf(hex.substring(2,4),16),Integer.valueOf(hex.substring(4,6),16));
    }
    public String hextorgbString(String hex) {
        return Integer.valueOf(hex.substring(0,2),16) + "," + Integer.valueOf(hex.substring(2,4),16) + "," + Integer.valueOf(hex.substring(4,6),16);
    }
    public String rgbtohexString(Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    //make own class
    public void inform(String text, String title, int width, int height) {
        try {
            CyderFrame informFrame = new CyderFrame(width,height,new ImageIcon(new ImageUtil().imageFromColor(width,height,navy)));
            informFrame.setTitle(title);

            JLabel desc = new JLabel("<html><div style='text-align: center;'>" + text + "</div></html>");

            desc.setHorizontalAlignment(JLabel.CENTER);
            desc.setVerticalAlignment(JLabel.CENTER);
            ImageUtil iu = new ImageUtil();
            desc.setForeground(vanila);
            desc.setFont(weatherFontSmall.deriveFont(22f));
            desc.setBounds(10, 35, width - 20, height - 35 * 2);

            informFrame.getContentPane().add(desc);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(null);
            informFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            handle(e);
        }
    }

    //num util
    public String toBinary(int value) {
        String bin;

        if (value > 0) {
            int colExp = 0;
            int val = value;

            while (Math.pow(2, colExp) <= value) {
                colExp = colExp + 1;
            }

            bin = "";

            do {
                colExp--;
                int columnWeight = (int) Math.pow(2, colExp);

                if (columnWeight <= val) {
                    bin += "1";
                    val -= columnWeight;
                }

                else
                    bin += "0";
            }

            while (colExp > 0);

            return bin;
        }

        return "NaN";
    }

    //popup utils
    public void systemProperties() {
        ArrayList<String> arrayLines = new ArrayList<>();
        arrayLines.add("File Separator: " + System.getProperty("file.separator"));
        arrayLines.add("Class Path: " + System.getProperty("java.class.path"));
        arrayLines.add("Java Home: " + System.getProperty("java.home"));
        arrayLines.add("Java Vendor: " + System.getProperty("java.vendor"));
        arrayLines.add("Java Vendor URL: " + System.getProperty("java.vendor.url"));
        arrayLines.add("Java Version: " + System.getProperty("java.version"));
        arrayLines.add("Line Separator: " + System.getProperty("line.separator"));
        arrayLines.add("OS Architecture: " + System.getProperty("os.arch"));
        arrayLines.add("OS Name: " + System.getProperty("os.name"));
        arrayLines.add("OS Version: " + System.getProperty("os.version"));
        arrayLines.add("OS Path Separator: " + System.getProperty("path.separator"));
        arrayLines.add("User Directory: " + System.getProperty("user.dir"));
        arrayLines.add("User Home: " + System.getProperty("user.home"));
        arrayLines.add("Computer Username: " + System.getProperty("user.name"));

        String[] lines = new String[arrayLines.size()];

        for (int i = 0 ; i < arrayLines.size() ; i++)
            lines[i] = arrayLines.get(i);

        createAndOpenTmpFile("SystemProperties",".txt",lines);
    }

    public int getCurrentDowns() {
        return this.currentDowns;
    }

    public void setCurrentDowns(int num) {
        this.currentDowns = num;
    }

    public boolean getHandledMath() {
        return this.handledMath;
    }
    public void setHandledMath(boolean b) {
        this.handledMath = b;
    }

    //user utils
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

    //move to consoleframe
    public boolean OnLastBackground() {
        return (validBackgroundPaths.length == currentBackgroundIndex + 1);
    }
    public File[] getValidBackgroundPaths() {
        initBackgrounds();
        return this.validBackgroundPaths;
    }

    //date util
    public boolean isChristmas() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 12 && Date == 25);
    }
    //date util
    public boolean isHalloween() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 10 && Date == 31);
    }
    //date util
    public boolean isIndependenceDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 7 && Date == 4);
    }
    //date util
    public boolean isThanksgiving() {
        Calendar Checker = Calendar.getInstance();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        LocalDate RealTG = LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY));
        return (Month == 11 && Date == RealTG.getDayOfMonth());
    }
    //date util
    public boolean isAprilFoolsDay() {
        Calendar Checker = Calendar.getInstance();
        int Month = Checker.get(Calendar.MONTH) + 1;
        int Date = Checker.get(Calendar.DATE);
        return (Month == 4 && Date == 1);
    }

    //widget
    public void clickMe() {
        try {
            CyderFrame clickMeFrame = new CyderFrame(200,100,new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
            clickMeFrame.setTitlePosition(CyderFrame.CENTER_TITLE);
            clickMeFrame.setTitle("");

            JLabel dismiss = new JLabel("Click Me!");
            dismiss.setHorizontalAlignment(JLabel.CENTER);
            dismiss.setVerticalAlignment(JLabel.CENTER);
            dismiss.setForeground(navy);
            dismiss.setFont(weatherFontBig.deriveFont(24f));
            dismiss.setBounds(20, 40, 150, 40);
            dismiss.addMouseListener(new MouseAdapter() {
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
                    dismiss.setForeground(navy);
                }
            });

            clickMeFrame.getContentPane().add(dismiss);

            clickMeFrame.setVisible(true);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            clickMeFrame.setLocation(randInt(0, (int) (rect.getMaxX() - 200)),randInt(0,(int) rect.getMaxY() - 200));
            clickMeFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            handle(e);
        }
    }

    //image utils
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

    //popup
    public void computerProperties() {
        ArrayList<String> arrayLines = new ArrayList<>();

        arrayLines.add("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
        arrayLines.add("Free memory (bytes): " + Runtime.getRuntime().freeMemory());

        long maxMemory = Runtime.getRuntime().maxMemory();

        arrayLines.add("Maximum memory (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));
        arrayLines.add("Total memory available to JVM (bytes): " + Runtime.getRuntime().totalMemory());

        File[] roots = File.listRoots();

        for (File root : roots) {
            arrayLines.add("File system root: " + root.getAbsolutePath());
            arrayLines.add("Total space (bytes): " + root.getTotalSpace());
            arrayLines.add("Free space (bytes): " + root.getFreeSpace());
            arrayLines.add("Usable space (bytes): " + root.getUsableSpace());
        }

        String[] lines = new String[arrayLines.size()];

        for (int i = 0 ; i < arrayLines.size() ; i++) {
            lines[i] = arrayLines.get(i);
        }

        createAndOpenTmpFile("Computer Properties",".txt", lines);
    }

    //consoleframe
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
            backgroundX = new SystemUtil().getScreenWidth();
            backgroundY = new SystemUtil().getScreenHeight();
        }

        else {
            backgroundX = Size.getIconWidth();
            backgroundY = Size.getIconHeight();
        }
    }

    //consoleframe
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

    //console frame
    public void initBackgrounds() {
        try {
            File dir = new File("src/users/" + getUserUUID() + "/Backgrounds");
            FilenameFilter PNGFilter = (dir1, filename) -> filename.endsWith(".png");
            validBackgroundPaths = dir.listFiles(PNGFilter);

            if (validBackgroundPaths.length == 0)
                validBackgroundPaths = new File[]{new File("src/com/cyder/io/pictures/Bobby.png")};
        }

        catch (ArrayIndexOutOfBoundsException ex) {
            handle(ex);
            handle(new FatalException(ex.getMessage()));
        }
    }

    //io utils
    public void openFile(String FilePath) {
        //use our custom text editor
        if (FilePath.endsWith(".txt")) {
            TextEditor te = new TextEditor(FilePath);
        }

        else if (FilePath.endsWith(".png")) {
            PhotoViewer pv = new PhotoViewer(new File(FilePath));
            pv.start();
        }

        //use our own mp3 player
        else if (FilePath.endsWith(".mp3")) {
            CyderPlayer = new MPEGPlayer(new File(FilePath), getUsername(), getUserUUID());
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

    //io utils
    public void openFileOutsideProgram(String filePath) {
        Desktop OpenFile = Desktop.getDesktop();

        try {
            File FileToOpen = new File(filePath);
            URI FileURI = FileToOpen.toURI();
            OpenFile.browse(FileURI);
        }

        catch (Exception e) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + filePath);
            }

            catch(Exception ex) {
                handle(ex);
            }
        }
    }

    //put in consoleframe class
    public boolean canSwitchBackground() {
        return (validBackgroundPaths.length > currentBackgroundIndex + 1 && validBackgroundPaths.length > 1);
    }

    //io utils
    public void readUserData() {
        try {
            userData.clear();
            String user = getUserUUID();

            if (user == null)
                return;

            BufferedReader dataReader = new BufferedReader(new FileReader(
                    "src/users/" + user + "/Userdata.txt"));

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

    //io utils (something here is broken since sometimes userdata will end up empty)
    public void writeUserData(String name, String value) {
        try {
            BufferedWriter userWriter = new BufferedWriter(new FileWriter(
                    "src/users/" + getUserUUID() + "/Userdata.txt", false));

            for (NST data : userData) {
                if (data.getName().equalsIgnoreCase(name)) {
                    data.setData(value);
                }

                userWriter.write(data.getName() + ":" + data.getData());
                userWriter.newLine();
            }

            userWriter.close();

            readUserData();
        }

        catch (Exception e) {
            handle(e);
        }
    }

    //io utils
    public String getUserData(String name) {
        readUserData();

        if (userData.isEmpty())
            return null;

        for (NST data : userData) {
            if (data.getName().equalsIgnoreCase(name)) {
                return data.getData();
            }
        }

        return null;
    }

    //popup util
    public void javaProperties() {
        ArrayList<String> PropertiesList = new ArrayList<>();
        Properties Props = System.getProperties();

        Enumeration<?> keys = Props.keys();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) Props.get(key);
            PropertiesList.add(key + ": " + value);
        }

        String[] lines = new String[PropertiesList.size()];

        for (int i =  0 ; i < PropertiesList.size() ; i++) {
            lines[i] = PropertiesList.get(i);
        }

        createAndOpenTmpFile("JavaProperties",".txt", lines);
    }

    //image utils
    public void resizeImages() {
        try {
            for (int i = 0 ; i < getValidBackgroundPaths().length ; i++) {
                File UneditedImage = getValidBackgroundPaths()[i];

                BufferedImage currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                setBackgroundX(currentImage.getWidth());
                setBackgroundY(currentImage.getHeight());

                double aspectRatio = getAspectRatio(currentImage);
                int imageType = currentImage.getType();

                if (getBackgroundX() > new SystemUtil().getScreenWidth() || getBackgroundY() > new SystemUtil().getScreenHeight()) {
                    inform("Resized the background image \"" + getValidBackgroundPaths()[i].getName() + "\" since it was too big " +
                            "(That's what she said ahahahahah hahaha ha ha so funny).","System Action", 700, 200);
                }

                while (getBackgroundX() > new SystemUtil().getScreenWidth() || getBackgroundY() > new SystemUtil().getScreenHeight()) {
                    currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                    int width = (int) (currentImage.getWidth() / aspectRatio);
                    int height = (int) (currentImage.getHeight() / aspectRatio);

                    BufferedImage saveImage = resizeImage(currentImage, imageType, width, height);

                    ImageIO.write(saveImage, "png", new File(getValidBackgroundPaths()[i].toString()));

                    setBackgroundX(saveImage.getWidth());
                    setBackgroundY(saveImage.getHeight());
                    getValidBackgroundPaths();
                }

                if (getBackgroundX() < 600 || getBackgroundY() < 600) {
                    inform("Resized the background image \"" + getValidBackgroundPaths()[i].getName() + "\" since it was too small.","System Action", 700, 200);
                }

                while (getBackgroundX() < 600 || getBackgroundY() < 600) {
                    currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                    int width = (int) (currentImage.getWidth() * aspectRatio);
                    int height = (int) (currentImage.getHeight() * aspectRatio);

                    BufferedImage saveImage = resizeImage(currentImage, imageType, width, height);

                    ImageIO.write(saveImage, "png", new File(getValidBackgroundPaths()[i].toString()));

                    setBackgroundX(saveImage.getWidth());
                    setBackgroundY(saveImage.getHeight());
                    getValidBackgroundPaths();
                }

                if (isPrime(getBackgroundX())) {
                    currentImage = ImageIO.read(new File(getValidBackgroundPaths()[i].toString()));

                    int width = currentImage.getWidth() + 1;
                    int height = currentImage.getHeight() + 1;

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

    //image utils
    private static BufferedImage resizeImage(BufferedImage originalImage, int type, int img_width, int img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();

        return resizedImage;
    }

    //image utils
    private double getAspectRatio(BufferedImage im) {
        return ((double) im.getWidth() / (double) im.getHeight());
    }
    public int getScreenResolution() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public static void staticHandle(Exception e) {
        new GeneralUtil().handle(e);
    }
    //handle class
    public void handle(Exception e) {
        try {
            File throwsDir = new File("src/users/" + getUserUUID() + "/Throws/");

            if (!throwsDir.exists())
                throwsDir.mkdir();

            String eFileString = "src/users/" + getUserUUID() + "/Throws/" + errorTime() + ".error";
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
            if (getUserData("SilenceErrors") != null && getUserData("SilenceErrors").equals("0")) {
                System.out.println("Exception in error logger:\n\n");
                e.printStackTrace();
                //todo show popup on cyderframe
            }
        }
    }

    //prefs utils?
    public void setConsoleClock(boolean b) {
        this.consoleClock = b;
    }
    public boolean getConsoleClock() {
        return this.consoleClock;
    }

    //input utils with getinput
    public boolean confirmation(String input) {
        return (input.toLowerCase().contains("yes") || input.equalsIgnoreCase("y"));
    }

    //static music player widget
    public void mp3(String FilePath, String user, String uuid) {
        if (CyderPlayer != null)
            CyderPlayer.kill();

        stopMusic();
        CyderPlayer = new MPEGPlayer(new File(FilePath), user, uuid);
    }

    //static music player widget
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

    //static music player widget
    public void stopMusic() {
        try {
            if (player != null && !player.isComplete()) {
                player.close();
            }
        }

        catch (Exception e) {
            handle(e);
        }
    }

    //put in consoleframe class
    public int getConsoleDirection() {
        return this.consoleDirection;
    }
    public void setConsoleDirection(int d) {
        this.consoleDirection = d;
    }

    //image utils
    public BufferedImage getBi(File imageFile) {
        try {
            return ImageIO.read(imageFile);
        }

        catch (Exception e) {
            handle(e);
        }

        return null;
    }

    //image utils
    public BufferedImage getBi(String filename) {
        try {
            return ImageIO.read(new File(filename));
        }

        catch (Exception e) {
            handle(e);
        }

        return null;
    }

    //image utils
    public BufferedImage getRotatedImage(String name) {
        switch(this.consoleDirection) {
            case 0:
                return getBi(name);
            case 1:
                return rotateImageByDegrees(getBi(name),90);
            case 2:
                return rotateImageByDegrees(getBi(name),180);
            case 3:
                return rotateImageByDegrees(getBi(name),-90);
        }

        return null;
    }

    //image utils
    //Used for barrel roll and flip screen hotkeys, credit: MadProgrammer from StackOverflow
    public BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {
        double rads = Math.toRadians(angle);

        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int w = img.getWidth();
        int h = img.getHeight();

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        at.rotate(rads, w / 2, h / 2);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    //num utils
    public boolean isPrime(int num) {
        ArrayList<Integer> Numbers = new ArrayList<>();

        for (int i = 3 ; i < Math.ceil(Math.sqrt(num)) ; i += 2)
            if (num % i == 0)
                Numbers.add(i);

        return Numbers.isEmpty();
    }

    //num util
    public int totalCodeLines(File startDir) {
        int ret = 0;

        if (startDir.isDirectory()) {
            File[] files = startDir.listFiles();

            for (File f : files)
                ret += totalCodeLines(f);
        }

        else if (startDir.getName().endsWith(".java")) {
            try {
                BufferedReader lineReader = new BufferedReader(new FileReader(startDir));
                String line = "";
                int localRet = 0;

                while ((line = lineReader.readLine()) != null)
                    localRet++;

                return localRet;
            }

            catch (Exception ex) {
                handle(ex);
            }
        }

        return ret;
    }

    //io utils
    public void cleanUpUsers() {
        File top = new File("src/users");
        File[] users = top.listFiles();

        for (File userDir : users) {
            if (!userDir.isDirectory())
                return;

            File[] currentUserFiles = userDir.listFiles();

            if (currentUserFiles.length == 1 && currentUserFiles[0].getName().equalsIgnoreCase("Userdata.txt"))
                new SystemUtil().deleteFolder(userDir);
        }
    }

    //io util
    public void deleteTempDir() {
        try {
            File tmpDir = new File("src/tmp");
            new SystemUtil().deleteFolder(tmpDir);
        } catch (Exception e) {
            handle(e);
        }
    }

    //io utils
    public void createAndOpenTmpFile(String filename, String extension, String[] lines) {
        try {
            File tmpDir = new File("src/tmp");

            if (!tmpDir.exists())
                tmpDir.mkdir();

            File tmpFile = new File(tmpDir + "/" + filename + extension);

            if (!tmpFile.exists())
                tmpFile.createNewFile();

            BufferedWriter tmpFileWriter = new BufferedWriter(new FileWriter(tmpFile));

            for (String line: lines) {
                tmpFileWriter.write(line);
                tmpFileWriter.newLine();
            }

            tmpFileWriter.flush();
            tmpFileWriter.close();

            openFileOutsideProgram(tmpFile.getAbsolutePath());
        }

        catch (Exception e) {
            handle(e);
        }
    }

    //ui utils
    public int xOffsetForCenterJLabel(int compWidth, String title) {
        return (int) Math.floor(5 + (compWidth / 2.0)) - (((int) Math.ceil(14 * title.length())) / 2);
    }

    //io utils
    public void wipeErrors() {
        File topDir = new File("src/users");
        File[] users = topDir.listFiles();

        for (File f : users) {
            if (f.isDirectory()) {
                File throwDir = new File("src/users/" + f.getName() + "/throws");
                if (throwDir.exists()) new SystemUtil().deleteFolder(throwDir);
            }
        }
    }
}