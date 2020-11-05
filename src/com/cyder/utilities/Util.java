//package declaration
package com.cyder.utilities;

import com.cyder.handler.PhotoViewer;
import com.cyder.handler.TextEditor;
import com.cyder.obj.NBT;
import com.cyder.obj.NST;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import javazoom.jl.player.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    //static strings used for test cases
    public static String HERE = "here";

    public static String LENGTH_ZERO = "";
    public static String LENGTH_ONE = "1";
    public static String LENGTH_TWO = "12";
    public static String LENGTH_THREE = "123";
    public static String LENGTH_FOUR = "1234";
    public static String LENGTH_FIVE = "12345";
    public static String LENGTH_SIX = "123456";
    public static String LENGTH_SEVEN = "1234567";
    public static String LENGTH_EIGHT = "12345678";
    public static String LENGTH_NINE = "123456789";

    public static String QUICK_BROWN_FOX = "The quick brown fox jumps over the lazy dog";

    public static String SUPER_LONG = "pneumonoultramicroscopicsilicovolcanoconiosis," +
                                     "pneumonoultramicroscopicsilicovolcanoconiosi," +
                                     "pneumonoultramicroscopicsilicovolcanoconiosis!" +
                                     "There, I said it!";

    //integer bounds
    public static int INFINITY = Integer.MAX_VALUE;
    public static int NEG_INFINITY = Integer.MIN_VALUE;

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
    public Color calculatorOrange = new Color(255,140,0);
    public Color regularRed = new Color(223,85,83);
    public Color intellijPink = new Color(236,64,122);
    public Color consoleColor = new Color(39, 40, 34);
    public Color tooltipBorderColor = new Color(0,0,0);
    public Color tooltipForegroundColor = new Color(85,85,255);
    public Color vanila = new Color(252, 251, 227);
    public Color tttblue = new Color(71, 81, 117);
    public Color navy = new Color(26, 32, 51);

    //Cyder direct vars
    private ImageIcon cyderIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\CyderIcon.png");
    private ImageIcon cyderIconBlink = new ImageIcon("src\\com\\cyder\\io\\pictures\\CyderIconBlink.png");
    private ImageIcon scaledCyderIcon = new ImageIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\CyderIcon.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));
    private ImageIcon scaledCyderIconBlink = new ImageIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\CyderIconBlink.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));

    //cyder version
    private String cyderVer = "Soultree";

    //uservars
    private BufferedReader dataReader;
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
    private boolean consoleClock;

    //weather vars
    private String customCity;
    private String customState;
    private boolean useCustomLoc;
    private String sunrise;
    private String sunset;
    private String weatherIcon;
    private String weatherCondition;
    private String windSpeed;
    private String visibility;
    private String temperature;
    private String humidity;
    private String pressure;
    private String feelsLike;
    private String windBearing;

    //screen and mouse vars
    private int screenX;
    private int screenY;
    private int xMouse;
    private int yMouse;


    //console orientation var
    public static int CYDER_UP = 0;
    public static int CYDER_RIGHT = 1;
    public static int CYDER_DOWN = 2;
    public static int CYDER_LEFT = 3;
    private int consoleDirection;

    //pixel vars
    private JFrame pixelFrame;

    //media vars
    private JFrame mediaFrame;
    private JLabel displayLabel;

    //update vars
    private boolean userInputMode;

    //boolean vars
    private boolean debugMode;
    private boolean handledMath;
    private boolean hideOnClose;
    private boolean oneMathPrint;
    private boolean alwaysOnTop;

    //scrolling var
    private int currentDowns;

    //drawing vars
    private JFrame pictureFrame;
    private CyderButton closeDraw;

    //click me var
    private JFrame clickMeFrame;

    //backbround vars
    private int backgroundX;
    private int backgroundY;

    //static player so only one instance ever exists
    public static MPEGPlayer CyderPlayer;
    private static Player player;

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

    public void refreshUsername(JFrame frame) {
        frame.setTitle(getCyderVer() + " Cyder [" + getUsername() + "]");
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
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEEEEE h:mmaa");
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

                frame.dispose();
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

    public void startAnimation(JFrame frame) {
        frame.setVisible(false);
        frame.setLocationRelativeTo(null);

        int to = frame.getY();
        frame.setLocation(frame.getX(), 0 - frame.getHeight());

        frame.setVisible(true);

        for (int i = 0 - frame.getHeight() ; i < to ; i+= 15) {
            frame.setLocation(frame.getX(), i);
            try {
                Thread.sleep(1);
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }

        frame.setLocationRelativeTo(null);
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

    public ImageIcon getCyderIcon() {
        return this.cyderIcon;
    }

    public ImageIcon getCyderIconBlink() {
        return this.cyderIconBlink;
    }

    public ImageIcon getScaledCyderIcon() {return this.scaledCyderIcon;}

    public ImageIcon getScaledCyderIconBlink() {return this.scaledCyderIconBlink;}

    public String getCyderVer() {
        return this.cyderVer;
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

    //This was the best bodge I ever pulled off
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

    //tests for my mac addr
    public boolean compMACAddress(String mac) {
        return toHexString(getSHA(mac.toCharArray())).equals("5c486915459709261d6d9af79dd1be29fea375fe59a8392f64369d2c6da0816e");
    }

    public String getWindowsUsername() {
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
        if (internetReachable())
            getIPData();
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

    public String getIPKey() {
        try {
            BufferedReader keyReader = new BufferedReader(new FileReader("src\\com\\cyder\\io\\text\\keys.txt"));
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

    public String getWeatherKey() {
        try {
            BufferedReader keyReader = new BufferedReader(new FileReader("src\\com\\cyder\\io\\text\\keys.txt"));
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

    public void getIPData() {
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

    public void debugMenu() {
        try {
            DecimalFormat gFormater = new DecimalFormat("##.###");
            double gBytes = Double.parseDouble(gFormater.format((((double) Runtime.getRuntime().freeMemory()) / 1024 / 1024 / 1024)));
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface netIn = NetworkInterface.getByInetAddress(address);
            getIPData();

            BufferedImage flag = ImageIO.read(new URL(getUserFlag()));

            double x = flag.getWidth();
            double y = flag.getHeight();

            BufferedImage draw = resizeImage(flag, 1, (int) (150 * x / y),(int) (150 * x / y));

            //todo be able to pass this BI to main to draw to textpane

            String[] lines = {"Time requested: " + weatherTime(),
                    "ISP: " + getUserISP(),
                    "IP: " + userIP,
                    "Postal Code: " + getUserPostalCode(),
                    "City: " + userCity,
                    "State: " + userState,
                    "Country: " + userCountry + " (" + userCountryAbr + ")",
                    "Latitude: " + lat + " Degrees N",
                    "Longitude: " + lon + " Degrees W",
                    "latency: " + latency() + " ms",
                    "Google Reachable: " + siteReachable("https://www.google.com"),
                    "YouTube Reachable: " + siteReachable("https://www.youtube.com"),
                    "Apple Reachable: " + siteReachable("https://www.apple.com"),
                    "Microsoft Reachable: " + siteReachable("https://www.microsoft.com//en-us//"),
                    "User Name: " + getWindowsUsername(),
                    "Computer Name: " + getComputerName(),
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

    public String[] combineArrays(String[] a, String[] b) {
        int length = a.length + b.length;
        String[] result = new String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    protected void weatherStats() {
        try {
            getIPData();

            if (useCustomLoc) {
                userCity = customCity;
            }

            String OpenString = "https://api.openweathermap.org/data/2.5/weather?q=" + userCity + "&appid=2d790dd0766f1da62af488f101380c75&units=imperial";

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
                }
                else if (field.contains("sunset")) {
                    sunset = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("icon")) {
                    weatherIcon = field.replace("icon", "");
                }
                else if (field.contains("speed")) {
                    windSpeed = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("deg")) {
                    windBearing = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("description")) {
                    weatherCondition = field.replace("description", "");
                }
                else if (field.contains("visibility")) {
                    visibility = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("feels_like")) {
                    feelsLike = field.replaceAll("[^\\d.]", "");
                }
                else if (field.contains("pressure")) {
                    pressure = field.replaceAll("[^\\d.]", "");
                    pressure = pressure.substring(0, Math.min(pressure.length(), 4));
                }
                else if (field.contains("humidity")) {
                    humidity = field.replaceAll("[^\\d.]", "");
                } else if (field.contains("temp")) {
                    temperature = field.replaceAll("[^\\d.]", "");
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
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public String getWindDirection(String wb) {
        double bear = Double.parseDouble(wb);

        if (bear > 360)
            bear -= 360;

        String ret = "";

        if (bear > 270 || bear < 90)
            ret += "N";
        else if (bear == 270)
            return "W";
        else if (bear == 90)
            return "E";
        else
            ret += "S";

        if (bear > 0 && bear < 180)
            ret += "E";
        else if (bear == 180)
            return "S";
        else if (bear == 0 || bear == 360)
            return "N";
        else
            ret += "W";

        return ret;
    }

    public String capsFirst(String Word) {
        StringBuilder SB = new StringBuilder(Word.length());
        String[] Words = Word.split(" ");

        for (String word : Words) {
            SB.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }

        return SB.toString();
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

    public String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public String getDeprecatedUUID() {
        return "VoidUser-" + generateUUID().substring(0,8);
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
            File[] users = new File("src\\com\\cyder\\users").listFiles();
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

    public void colorConverter() {
        CyderFrame colorFrame = new CyderFrame(400,300,new ImageIcon("src\\com\\cyder\\io\\pictures\\DebugBackground.png"));
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

    public Color hextorgbColor(String hex) { return new Color(Integer.valueOf(hex.substring(0,2),16),Integer.valueOf(hex.substring(2,4),16),Integer.valueOf(hex.substring(4,6),16)); }
    public String hextorgbString(String hex) { return Integer.valueOf(hex.substring(0,2),16) + "," + Integer.valueOf(hex.substring(2,4),16) + "," + Integer.valueOf(hex.substring(4,6),16); }
    public String rgbtohexString(Color c) { return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }

    public void inform(String message, String title, int width, int height) {
        try {
            JFrame informFrame = new JFrame();
            informFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            informFrame.setTitle(title);
            informFrame.setSize(width, height);
            informFrame.setUndecorated(true);
            informFrame.setBackground(new Color(52,70,84));

            JLabel consoleLabel = new JLabel();
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
        String[] vbs = {"Set wmp = CreateObject(\"WMPlayer.OCX\")",
            "Set cd = wmp.cdromCollection.getByDriveSpecifier(\""
            + drive + "\")",
            "cd.Eject",
            "cd.Eject"};

        createAndOpenTmpFile("CDROM-CLOSE",".vbs",vbs);
    }

    public void openCD(String drive) {
        String[] vbs = {"Set wmp = CreateObject(\"WMPlayer.OCX\")",
                "Set cd = wmp.cdromCollection.getByDriveSpecifier(\""
                + drive + "\")",
                "cd.Eject"};

        createAndOpenTmpFile("CDROM-OPEN",".vbs",vbs);
    }
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

    public void pixelate(File path, int pixelSize) {
        try {
            BufferedImage ReturnImage = ImageUtil.pixelate(ImageIO.read(path), pixelSize);

            String NewName = path.getName().replace(".png", "") + "_Pixelated_Pixel_Size_" + pixelSize + ".png";

            if (pixelFrame != null)
                closeAnimation(pixelFrame);

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
                inform("The distorted image has been saved to your Downloads folder.","", 400, 200);
            });

            approveImage.setSize(pixelFrame.getX(), 20);

            CyderButton rejectImage = new CyderButton("Reject Image");

            rejectImage.setFocusPainted(false);

            rejectImage.setBackground(regularRed);

            rejectImage.setBorder(new LineBorder(navy,3,false));

            rejectImage.setColors(regularRed);

            rejectImage.setFont(weatherFontSmall);

            rejectImage.addActionListener(e -> closeAnimation(pixelFrame));

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

    public void here() {
        inform("here","Here", 100, 100);
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

    public boolean OnLastBackground() {
        return (validBackgroundPaths.length == currentBackgroundIndex + 1);
    }

    public File[] getValidBackgroundPaths() {
        initBackgrounds();
        return this.validBackgroundPaths;
    }

    protected void beep() {
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

    public void clickMe() {
        try {
            if (clickMeFrame != null)
                closeAnimation(clickMeFrame);

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
        File dir = new File("src\\com\\cyder\\users\\" + getUserUUID() + "\\Backgrounds");
        FilenameFilter PNGFilter = (dir1, filename) -> filename.endsWith(".png");
        validBackgroundPaths = dir.listFiles(PNGFilter);

        if (validBackgroundPaths.length == 0)
            validBackgroundPaths = new File[]{new File("src\\com\\cyder\\io\\pictures\\Bobby.png")};
    }

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

    public void setHideOnClose(boolean b) {
        this.hideOnClose = b;
    }

    public boolean getHideOnClose() {
        return this.hideOnClose;
    }

    public String getUserCity() {
        getIPData();
        return this.userCity;
    }

    public String getUserState() {
        getIPData();
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

    public boolean canSwitchBackground() {
        return (validBackgroundPaths.length > currentBackgroundIndex + 1 && validBackgroundPaths.length > 1);
    }

    public void readUserData() {
        try {
            userData.clear();

            String user = getUserUUID();

            dataReader = new BufferedReader(new FileReader(
                    "src\\com\\cyder\\users\\" + user + "\\Userdata.txt"));

            String Line = dataReader.readLine();

            while (Line != null) {
                String[] parts = Line.split(":");

                userData.add(new NST(parts[0], parts[1]));

                Line = dataReader.readLine();
            }

            dataReader.close();
        }

        catch(Exception e) {
            handle(e); //todo stack over flow looping between three methods occurs right now
            //comes through here so trace it
        }
    }

    public void writeUserData(String name, String value) {
        try {
            BufferedWriter userWriter = new BufferedWriter(new FileWriter(
                    "src\\com\\cyder\\users\\" + getUserUUID() + "\\Userdata.txt", false));

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

        String[] lines = new String[PropertiesList.size()];

        for (int i =  0 ; i < PropertiesList.size() ; i++) {
            lines[i] = PropertiesList.get(i);
        }

        createAndOpenTmpFile("JavaProperties",".txt", lines);
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
                    inform("Resized the background image \"" + getValidBackgroundPaths()[i].getName() + "\" since it was too big " +
                            "(That's what she said ahahahahah hahaha ha ha so funny).","", 700, 200);
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

                if (getBackgroundX() < 600 || getBackgroundY() < 600) {
                    inform("Resized the background image \"" + getValidBackgroundPaths()[i].getName() + "\" since it was too small.","", 700, 200);
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

    public static void staticHandle(Exception e) {
        new Util().handle(e);
    }

    public void handle(Exception e) {
        try {
            File throwsDir = new File("src\\com\\cyder\\users\\" + getUserUUID() + "\\Throws\\");

            if (!throwsDir.exists())
                throwsDir.mkdir();

            String eFileString = "src\\com\\cyder\\users\\" + getUserUUID() + "\\Throws\\" + errorTime() + ".error";
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
            if (debugMode && getUserData("SilenceErrors").equals("0")) {
                System.out.println("Exception in error logger:\n\n");
                e.printStackTrace();
            }
        }
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



    public void mp3(String FilePath, String user, String uuid) {
        if (CyderPlayer != null)
            CyderPlayer.kill();
        CyderPlayer = new MPEGPlayer(new File(FilePath), user, uuid);
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
        }

        catch (Exception e) {
            handle(e);
        }
    }

    public int getConsoleDirection() {
        return this.consoleDirection;
    }

    public void setConsoleDirection(int d) {
        this.consoleDirection = d;
    }

    public BufferedImage getBi(File imageFile) {
        try {
            return ImageIO.read(imageFile);
        }

        catch (Exception e) {
            handle(e);
        }

        return null;
    }

    public BufferedImage getBi(String filename) {
        try {
            return ImageIO.read(new File(filename));
        }

        catch (Exception e) {
            handle(e);
        }

        return null;
    }

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

    public boolean isPrime(int num) {
        ArrayList<Integer> Numbers = new ArrayList<>();

        for (int i = 3 ; i < Math.ceil(Math.sqrt(num)) ; i += 2)
            if (num % i == 0)
                Numbers.add(i);

        return Numbers.isEmpty();
    }

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

    public void cleanUpUsers() {
        File top = new File("src\\com\\cyder\\users");
        File[] users = top.listFiles();

        for (File userDir : users) {
            if (!userDir.isDirectory())
                return;

            File[] currentUserFiles = userDir.listFiles();

            if (currentUserFiles.length == 1 && currentUserFiles[0].getName().equalsIgnoreCase("Userdata.txt"))
                deleteFolder(userDir);
        }
    }

    public void deleteTempDir() {
        try {
            File tmpDir = new File("src/tmp");
            deleteFolder(tmpDir);
        } catch (Exception e) {
            handle(e);
        }
    }

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

    public int startToCenterJLabel(int compWidth, String title) {
        return (int) Math.floor(5 + (compWidth / 2.0)) - (((int) Math.ceil(14 * title.length())) / 2);
    }

    public void wipeErrors() {
        File topDir = new File("src/com/cyder/users");
        File[] users = topDir.listFiles();

        for (File f : users) {
            if (f.isDirectory()) {
                File throwDir = new File("src/com/cyder/users/" + f.getName() + "/throws");
                if (throwDir.exists()) deleteFolder(throwDir);
            }
        }
    }
}