//package declaration
package com.cyder.utilities;

import com.cyder.handler.PhotoViewer;
import com.cyder.handler.TextEditor;
import com.cyder.obj.NST;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderSliderUI;
import com.cyder.ui.DragLabel;
import javazoom.jl.player.Player;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Port;
import javax.swing.*;
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
    private ImageIcon cyderIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\CyderIcon.png");
    private ImageIcon cyderIconBlink = new ImageIcon("src\\com\\cyder\\io\\pictures\\CyderIconBlink.png");
    private ImageIcon scaledCyderIcon = new ImageIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\CyderIcon.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));
    private ImageIcon scaledCyderIconBlink = new ImageIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\CyderIconBlink.png").getImage().getScaledInstance(25, 25, Image.SCALE_DEFAULT));

    private String cyderVer = "Maple";

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

    //restore vars
    private int musicRestoreX;
    private int musicRestoreY;
    private int debugRestoreX;
    private int debugRestoreY;

    //debug vars
    private JFrame debugFrame;

    //pixel vars
    private JFrame pixelFrame;

    //media vars
    private JFrame mediaFrame;
    private JLabel displayLabel;

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

            getIPData();

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

        return "NA";
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

    public void setCyderVer(String ver) {
        this.cyderVer = ver;
    }

    public boolean OnLastBackground() {
        return (validBackgroundPaths.length == currentBackgroundIndex + 1);
    }

    public File[] getValidBackgroundPaths() {
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
            pv.start();
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
            String eFileString = "src\\com\\cyder\\exception\\throws\\" + errorTime() + ".error";
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