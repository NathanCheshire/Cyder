package cyder.utilities;

import com.google.common.base.Preconditions;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.IgnoreThread;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;

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

    /**
     * Whether connection to the internet is slow.
     */
    private static boolean highLatency;

    /**
     * Returns whether connection to the internet is slow.
     *
     * @return whether connection to the internet is slow.
     */
    public static boolean isHighLatency() {
        return highLatency;
    }

    /**
     * Sets the value of highLatency.
     *
     * @param highLatency the value of high latency
     */
    public static void setHighLatency(boolean highLatency) {
        NetworkUtil.highLatency = highLatency;
    }

    static {
        CyderThreadRunner.submit(() -> {
            try {
                for (;;) {
                    if (!decentPing()) {
                        setHighLatency(true);
                    } else {
                        setHighLatency(false);
                    }

                    // sleep 2 minutes
                    Thread.sleep(1000 * 60 * 2);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.HighPingChecker.getName());
    }

    /**
     * Opens the provided url using the native browser.
     *
     * @param url the url to open
     */
    public static void openUrl(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        Desktop Internet = Desktop.getDesktop();

        try {
            Internet.browse(new URI(url));
            Logger.log(Logger.Tag.LINK, url);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Attempts to ping the provided url until it responds.
     *
     * @param url the url to ping
     * @return whether the url responded
     */
    public static boolean siteReachable(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 " + url);
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
        SocketAddress Address = new InetSocketAddress(CyderUrls.GOOGLE, 80);
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

    /**
     * Pings google to find the latency.
     *
     * @return the latency of the local internet connection to google.com
     */
    public int latency() {
        Socket Sock = new Socket();
        SocketAddress Address = new InetSocketAddress(CyderUrls.GOOGLE, 80);
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
        Preconditions.checkNotNull(urlString);
        Preconditions.checkArgument(!urlString.isEmpty());

        String ret = null;
        BufferedReader reader;
        StringBuilder sb = null;

        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            sb = new StringBuilder();
            int read;
            char[] chars = new char[BUFFER_SIZE];

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
     * @param url the url to get the title of.
     * @return the title of the provided url
     */
    public static String getURLTitle(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        String ret = null;

        try {
            Document document = Jsoup.connect(url).get();
            ret = document.title();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns whether the provided url is constructed properly
     *
     * @param url the url to check for proper form
     * @return whether the provided url is of a valid form
     */
    public static boolean isURL(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        Matcher regexMatcher = CyderRegexPatterns.urlFormationPattern.matcher(url);
        return regexMatcher.matches();
    }

    /**
     * The size of the buffer when downloading resources from a Url or reading a Url.
     */
    public static final int BUFFER_SIZE = 1024;

    /**
     * Downloads the resource at the provided link and save it to the provided file.
     *
     * @param urlResource   the link to download the file from
     * @param referenceFile the file to save the resource to
     * @return whether the downloading concluded without errors
     */
    public static boolean downloadResource(String urlResource, File referenceFile) {
        Preconditions.checkNotNull(urlResource);
        Preconditions.checkArgument(!urlResource.isEmpty());
        Preconditions.checkNotNull(referenceFile);
        Preconditions.checkArgument(!referenceFile.exists());

        if (referenceFile == null)
            throw new IllegalArgumentException("The provided reference file is null");
        if (urlResource == null || !isURL(urlResource))
            throw new IllegalArgumentException("The provided url is null or not a valid url");

        if (!referenceFile.exists()) {
            try {
                referenceFile.createNewFile();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                return false;
            }
        }

        try (BufferedInputStream in = new BufferedInputStream(new URL(urlResource).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(referenceFile)) {

            byte[] dataBuffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = in.read(dataBuffer, 0, BUFFER_SIZE)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e);
            return false;
        }

        return true;
    }
}
