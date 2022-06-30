package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.IgnoreThread;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;

/**
 * Utility methods revolving around networking, urls, servers, etc.
 */
public class NetworkUtil {
    /**
     * Suppress default constructor.
     */
    private NetworkUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
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

    /**
     * The function used by the high ping checker to provide to TimeUtil.
     */
    private static final Function<Void, Boolean> exit = ignored -> ConsoleFrame.INSTANCE.isClosed();

    // todo be able to start and stop this with other console executors
    static {
        CyderThreadRunner.submit(() -> {
            try {
                while (true) {
                    setHighLatency(!decentPing());

                    TimeUtil.sleepWithChecks(1000 * 60 * 2,
                            1000 * 30, exit);
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
     * The timeout value when determining if a site is reachable.
     */
    public static final int SITE_PING_TIMEOUT = 5000;

    /**
     * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code>
     * if the response code is in the 200-399 range.
     *
     * @param url The HTTP URL to be pinged.
     * @return whether the given HTTP URL has returned response code 200-399 on a HEAD request within the
     *         given timeout
     */
    public static boolean siteReachable(String url) {
        url = url.replaceFirst("^https", "http");

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(SITE_PING_TIMEOUT);
            connection.setReadTimeout(SITE_PING_TIMEOUT);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (Exception ignored) {}

        return false;
    }

    /**
     * The port to use when pinging google to determine a user's latency.
     */
    public static final int LATENCY_GOOGLE_PORT = 80;

    /**
     * The default timeout to use when pinging google to determine a user's latency.
     */
    public static final int DEFAULT_LATENCY_TIMEOUT = 2000;

    /**
     * Returns the latency of the host system to google.com.
     *
     * @param timeout the time in ms to wait before timing out
     * @return the latency in ms between the host and google.com
     */
    public static int latency(int timeout) {
        Socket Sock = new Socket();
        SocketAddress Address = new InetSocketAddress(CyderUrls.GOOGLE, LATENCY_GOOGLE_PORT);
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
        return latency(DEFAULT_LATENCY_TIMEOUT);
    }

    /**
     * The maximum possible ping for Cyder to consider a user's connection "decent."
     */
    public static final int DECENT_PING_MAXIMUM_LATENCY = 5000;

    /**
     * Determines if the connection to the internet is usable by pinging google.com.
     *
     * @return if the connection to the internet is usable
     */
    public static boolean decentPing() {
        return latency(DECENT_PING_MAXIMUM_LATENCY) < DECENT_PING_MAXIMUM_LATENCY;
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

        try {
            URL url = new URL(urlString);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            int read;
            char[] chars = new char[DOWNLOAD_RESOURCE_BUFFER_SIZE];

            while ((read = reader.read(chars)) != -1) {
                sb.append(chars, 0, read);
            }

            reader.close();
            return sb.toString();
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        }

        throw new IllegalCallerException("Error reading from url: " + urlString);
    }

    /**
     * Returns the title of the provided url.
     *
     * @param url the url to get the title of.
     * @return the title of the provided url
     */
    public static String getUrlTitle(String url) {
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
    public static boolean isValidUrl(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        Matcher regexMatcher = CyderRegexPatterns.urlFormationPattern.matcher(url);
        return regexMatcher.matches();
    }

    /**
     * The size of the buffer when downloading resources from a Url or reading a Url.
     */
    public static final int DOWNLOAD_RESOURCE_BUFFER_SIZE = 1024;

    /**
     * Downloads the resource at the provided link and save it to the provided file.
     *
     * @param urlResource   the link to download the file from
     * @param referenceFile the file to save the resource to
     * @return whether the downloading concluded without errors
     */
    public static boolean downloadResource(String urlResource, File referenceFile) throws IOException {
        Preconditions.checkNotNull(urlResource);
        Preconditions.checkArgument(isValidUrl(urlResource));
        Preconditions.checkArgument(!urlResource.isEmpty());
        Preconditions.checkNotNull(referenceFile);
        Preconditions.checkArgument(!referenceFile.exists());

        boolean created = false;

        if (!referenceFile.exists()) {
            try {
                created = referenceFile.createNewFile();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                return false;
            }
        }

        if (!created) {
            throw new IOException("Could not create reference file: " + referenceFile);
        }

        try (BufferedInputStream in = new BufferedInputStream(new URL(urlResource).openStream()) ;
             FileOutputStream fileOutputStream = new FileOutputStream(referenceFile)) {

            byte[] dataBuffer = new byte[DOWNLOAD_RESOURCE_BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = in.read(dataBuffer, 0, DOWNLOAD_RESOURCE_BUFFER_SIZE)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e);
            return false;
        }

        return true;
    }

    /**
     * Returns the ip of the user's computer if found.
     *
     * @return the ip of the user's computer if found
     */
    public static Optional<String> getIp() {
        try {
            return Optional.of(InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }
}
