package cyder.utilities;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.*;
import java.net.*;

/**
 * Utility methods revolving around networking, urls, servers, etc.
 */
public class NetworkUtil {
    /**
     * Suppress default constructor.
     */
    private NetworkUtil() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    public static void openUrl(String URL) {
        Desktop Internet = Desktop.getDesktop();
        try {
            Internet.browse(new URI(URL));
            Logger.log(Logger.Tag.LINK, URL);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Attempts to ping the provided url until it responds.
     *
     * @param URL the url to ping
     * @return whether the url responded
     */
    public static boolean siteReachable(String URL) {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 " + URL);
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return false;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return true;
    }

    /**
     * Returns the latency of the host system to google.com.
     *
     * @param timeout the time in ms to wait before timing out
     * @return the latency in ms between the host and google.com
     */
    public static int latency(int timeout) {
        Socket Sock = new Socket();
        SocketAddress Address = new InetSocketAddress("www.google.com", 80);
        long start = System.currentTimeMillis();

        try {
            Sock.connect(Address, timeout);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        long stop = System.currentTimeMillis();
        int Latency = (int) (stop - start);

        try {
            Sock.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
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
            ExceptionHandler.handle(e);
        }

        long stop = System.currentTimeMillis();
        int Latency = (int) (stop - start);

        try {
            Sock.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Latency;
    }

    /**
     * Determines if the connection to the internet is usable by pinging google.com.
     *
     * @return if the connection to the internet is usable
     */
    public static boolean decentPing() {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 www.google.com");
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return true;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }

    /**
     * Reads from the provided url and returned the response.
     *
     * @param urlString the string of the url to ping and get contents from
     * @return the resulting url response
     */
    public static String readUrl(String urlString) {
        String ret = null;
        BufferedReader reader;
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
            ExceptionHandler.silentHandle(e);
        }

        return sb.toString();
    }

    /**
     * Returns the title of the provided url.
     *
     * @param URL the url to get the title of.
     * @return the title of the provided url
     */
    public static String getURLTitle(String URL) {
        String ret = null;

        try {
            Document document = Jsoup.connect(URL).get();
            ret = document.title();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns whether the provided url is valid.
     *
     * @param URL the url to check for validity
     * @return whether the provided url is valid
     */
    public static boolean isURL(String URL) {
        boolean ret;

        try {
            URL url = new URL(URL);
            URLConnection conn = url.openConnection();
            conn.connect();
            ret = true;
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
            ret = false;
        }

        return ret;
    }

    /**
     * Downloads the resource at the provided link and save it to the provided file.
     *
     * @param urlResource the link to download the file from
     * @param referenceFile the file to save the resource to
     * @return whether the downloading concluded without errors
     */
    public static boolean downloadResource(String urlResource, File referenceFile) {
        if (referenceFile == null)
            throw new IllegalArgumentException("The provided reference file is null");
        if (urlResource == null || !isURL(urlResource))
            throw new IllegalArgumentException("The provided url is null or not a valid url");

        if (!referenceFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                referenceFile.createNewFile();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                return false;
            }
        }

        try (BufferedInputStream in = new BufferedInputStream(new URL(urlResource).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(referenceFile)) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e);
            return false;
        }

        return true;
    }
 }
