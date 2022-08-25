package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.IgnoreThread;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Matcher;

/**
 * Utility methods revolving around networking, urls, servers, etc.
 */
public class NetworkUtil {
    public static final String LATENCY_IP_KEY = "latency_ip";
    public static final String LATENCY_PORT_KEY = "latency_port";
    public static final String LATENCY_NAME = "latency_name";

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
     * @return whether connection to the internet is slow
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
     * Whether the high ping check is/should be running currently.
     */
    private static final AtomicBoolean highPingCheckerRunning = new AtomicBoolean();

    /**
     * The function used by the high ping checker to provide to TimeUtil.
     */
    private static final Function<Void, Boolean> shouldExit = ignored ->
            Console.INSTANCE.isClosed() || !highPingCheckerRunning.get();

    /**
     * The timeout between checking for high ping (two minutes).
     */
    private static final int HIGH_PING_TIMEOUT = 1000 * 60 * 2;

    /**
     * The timeout between checking for the high ping checker's exit condition (six seconds).
     */
    private static final int HIGH_PING_EXIT_CHECK = 1000 * 6;

    /**
     * Starts the high ping checker.
     */
    public static void startHighPingChecker() {
        if (highPingCheckerRunning.get()) return;

        highPingCheckerRunning.set(true);

        CyderThreadRunner.submit(() -> {
            try {
                while (highPingCheckerRunning.get()) {
                    setHighLatency(!decentPing());

                    TimeUtil.sleepWithChecks(HIGH_PING_TIMEOUT, HIGH_PING_EXIT_CHECK, shouldExit);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.HighPingChecker.getName());
    }

    /**
     * Ends the high ping checker
     */
    public static void endDecentPingChecker() {
        highPingCheckerRunning.set(false);
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
     * So no head?
     */
    private static final String HEAD = "HEAD";

    /**
     * Pings an HTTP URL. This effectively sends a HEAD request and returns <code>true</code>
     * if the response code is in the 200-399 range.
     *
     * @param url The HTTP URL to be pinged
     * @return whether the given HTTP URL has returned response code 200-399 on a HEAD request within the
     * given timeout
     */
    public static boolean siteReachable(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        if (url.startsWith("https")) url = url.replaceAll("^https", "http");

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(SITE_PING_TIMEOUT);
            connection.setReadTimeout(SITE_PING_TIMEOUT);
            connection.setRequestMethod(HEAD);
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (Exception ignored) {}

        return false;
    }

    /**
     * The name of the ip to ping by default when determining a user's latency.
     */
    public static final String LATENCY_GOOGLE_HOST_NAME = "Google";

    /**
     * The port to use when pinging google to determine a user's latency.
     */
    public static final int LATENCY_GOOGLE_PORT = 80;

    /**
     * The ip to use when pinging google to determine a user's latency.
     * DNS changing for this would be highly unlikely. In the future, this might
     * be changed to a remote database value.
     */
    public static final String LATENCY_GOOGLE_IP = "172.217.4.78";

    private static String LATENCY_IP = LATENCY_GOOGLE_IP;
    private static int LATENCY_PORT = LATENCY_GOOGLE_PORT;
    private static String LATENCY_HOST_NAME = LATENCY_GOOGLE_HOST_NAME;

    static {
        if (PropLoader.propExists(LATENCY_IP_KEY)) {
            LATENCY_IP = PropLoader.getString(LATENCY_IP_KEY);
            Logger.log(Logger.Tag.DEBUG, "Set latency ip as " + LATENCY_IP);
        }

        if (PropLoader.propExists(LATENCY_PORT_KEY)) {
            LATENCY_PORT = PropLoader.getInteger(LATENCY_PORT_KEY);
            Logger.log(Logger.Tag.DEBUG, "Set latency port as " + LATENCY_PORT);
        }

        if (PropLoader.propExists(LATENCY_NAME)) {
            LATENCY_HOST_NAME = PropLoader.getString(LATENCY_NAME);
            Logger.log(Logger.Tag.DEBUG, "Set latency host name as " + LATENCY_HOST_NAME);
        }
    }

    /**
     * Returns the latency of the host system to google.com.
     *
     * @param timeout the time in ms to wait before timing out
     * @return the latency in ms between the host and google.com
     */
    public static int latency(int timeout) {
        Socket socket = new Socket();
        SocketAddress address = new InetSocketAddress(LATENCY_IP, LATENCY_PORT);
        long start = System.currentTimeMillis();

        try {
            socket.connect(address, timeout);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        long stop = System.currentTimeMillis();
        int latency = (int) (stop - start);

        try {
            socket.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        Logger.log(Logger.Tag.DEBUG, "Latency of " + LATENCY_HOST_NAME
                + " found to be " + TimeUtil.millisToFormattedString(latency));

        return latency;
    }

    /**
     * The default timeout to use when pinging google to determine a user's latency.
     */
    public static final int DEFAULT_LATENCY_TIMEOUT = 2000;

    /**
     * Pings {@link #LATENCY_GOOGLE_IP} to find the latency.
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
     * Reads from the provided url and returns the response.
     *
     * @param urlString the string of the url to ping and get contents from
     * @return the resulting url response
     */
    @CanIgnoreReturnValue /* sometimes used to ensure a url is valid as a precondition */
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
     * Returns the title of the provided url according to JSoup.
     *
     * @param url the url to get the title of
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
            return Optional.of(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }

    /**
     * The url for determining network details.
     */
    public static final String ispQueryUrl = "https://www.whatismyisp.com///";

    /**
     * A record used to store the data after {@link #getIspAndNetworkDetails} is invoked.
     */
    public record IspQueryResult(String isp, String hostname, String ip, String city, String state, String country) {}

    private static final String ispClassName = "block text-4xl";
    private static final String cityStateCountryClassName = "grid grid-cols-3 gap-2 px-6 pb-6";
    private static final String ipHostnameClassName = "prose";

    private static final int cityIndex = 2;
    private static final int stateIndex = 4;
    private static final int countryIndex = 6;

    /**
     * Returns information about this user's isp, their ip, location, city, state/region, and country.
     *
     * @return information about this user's isp, their ip, location, city, state/region, and country
     */
    public static IspQueryResult getIspAndNetworkDetails() {
        Document locationDocument = null;

        try {
            locationDocument = Jsoup.connect(ispQueryUrl).get();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (locationDocument == null) {
            throw new FatalException("Could not obtain document from isp query url");
        }

        String isp = locationDocument.getElementsByClass(ispClassName).text();
        String city = locationDocument.getElementsByClass(cityStateCountryClassName)
                .get(0).getAllElements().get(cityIndex).text();
        String state = locationDocument.getElementsByClass(cityStateCountryClassName)
                .get(0).getAllElements().get(stateIndex).text();
        String country = locationDocument.getElementsByClass(cityStateCountryClassName)
                .get(0).getAllElements().get(countryIndex).text();
        String ip = filterIp(locationDocument.getElementsByClass(ipHostnameClassName).get(2).text());
        while (ip.endsWith(".")) {
            ip = ip.substring(0, ip.length() - 2);
        }
        String hostname = filterHostname(locationDocument.getElementsByClass(ipHostnameClassName).get(3).text());

        return new IspQueryResult(isp, hostname, ip, city, state, country);
    }

    @ForReadability
    private static String filterIp(String rawClassResult) {
        return rawClassResult.replaceAll("[^0-9.]", "").replaceAll("[.]{2,}", ".");
    }

    @ForReadability
    private static String filterHostname(String rawClassResult) {
        rawClassResult = rawClassResult.substring(rawClassResult.indexOf("'") + 1);
        return rawClassResult.substring(0, rawClassResult.indexOf("'"));
    }
}
