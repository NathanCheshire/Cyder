package cyder.utilities;

import cyder.handlers.internal.ErrorHandler;
import cyder.handlers.internal.SessionHandler;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkUtil {

    private NetworkUtil() {} //private constructor to avoid object creation

    public static void internetConnect(String URL) {
        Desktop Internet = Desktop.getDesktop();
        try {
            Internet.browse(new URI(URL));
            SessionHandler.log(SessionHandler.Tag.LINK, URL);
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public static String getMonitorStatsString() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < gs.length; i++) {
            DisplayMode dm = gs[i].getDisplayMode();
            sb.append(i);
            sb.append(", width: ");
            sb.append(dm.getWidth());
            sb.append(", height: ");
            sb.append(dm.getHeight());
            sb.append(", bit depth: ");
            sb.append(dm.getBitDepth());
            sb.append(", refresh rate: ");
            sb.append(dm.getRefreshRate());
            sb.append("\n");
        }

        return sb.toString();
    }


    public static String getNetworkDevicesString() {
        StringBuilder sb = new StringBuilder();

        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface netint : Collections.list(nets)) {
                sb.append("Display name:").append(netint.getDisplayName()).append("\n");
                sb.append("Name:").append(netint.getName()).append("\n");
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return sb.toString();
    }

    public static Enumeration<NetworkInterface> getNetworkDevices() throws SocketException {
        return NetworkInterface.getNetworkInterfaces();
    }

    public static void internetConnect(URI URI) {
        Desktop Internet = Desktop.getDesktop();
        try {
            Internet.browse(URI);
            SessionHandler.log(SessionHandler.Tag.LINK, URI.getPath());
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public static boolean siteReachable(String URL) {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 " + URL);
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return false;
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return true;
    }

    public static int latency(int timeout) {
        Socket Sock = new Socket();
        SocketAddress Address = new InetSocketAddress("www.google.com", 80);
        long start = System.currentTimeMillis();

        try {
            Sock.connect(Address, timeout);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        long stop = System.currentTimeMillis();
        int Latency = (int) (stop - start);

        try {
            Sock.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return Latency;
    }

    public int latency() {
        Socket Sock = new Socket();
        SocketAddress Address = new InetSocketAddress("www.google.com", 80);
        int Timeout = 2000;
        long start = System.currentTimeMillis();

        try {
            Sock.connect(Address, Timeout);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        long stop = System.currentTimeMillis();
        int Latency = (int) (stop - start);

        try {
            Sock.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return Latency;
    }

    public static boolean internetReachable() {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 www.google.com");
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return true;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
    }

    public static String readUrl(String urlString) {
        String ret = null;
        BufferedReader reader = null;
        StringBuilder sb = null;

        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            sb = new StringBuilder();
            int read;
            char[] chars = new char[1024];

            while ((read = reader.read(chars)) != -1)
                sb.append(chars, 0, read);

            if (reader != null)
                reader.close();
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        } finally {
            return sb.toString();
        }
    }

    public static String getURLTitle(String URL) {
        String ret = null;

        try {
            org.jsoup.nodes.Document document = Jsoup.connect(URL).get();
            ret = document.title();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            return ret;
        }
    }
}
