package cyder.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.PropLoader;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Utility methods revolving around networking, urls, servers, etc.
 */
@SuppressWarnings("unused") /* Response codes */
public class NetworkUtil {
    /**
     * The string used to represent a space in a url.
     */
    public static final String URL_SPACE = "%20";

    /**
     * The key for obtaining the latency ip prop.
     */
    public static final String LATENCY_IP_KEY = "latency_ip";

    /**
     * The key for obtaining the latency port prop.
     */
    public static final String LATENCY_PORT_KEY = "latency_port";

    /**
     * The key for obtaining the latency name prop.
     */
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
     * The timeout between checking for high ping.
     */
    private static final int HIGH_PING_TIMEOUT = (int) (TimeUtil.MILLISECONDS_IN_SECOND
            * TimeUtil.SECONDS_IN_MINUTE * 2.0f);

    /**
     * The timeout between checking for the high ping checker's exit condition.
     */
    private static final int HIGH_PING_EXIT_CHECK = (int) (TimeUtil.MILLISECONDS_IN_SECOND * 6.0f);

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

                    ThreadUtil.sleepWithChecks(HIGH_PING_TIMEOUT, HIGH_PING_EXIT_CHECK, shouldExit);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, IgnoreThread.HighPingChecker.getName());
    }

    /**
     * Ends the high ping checker subroutine.
     */
    public static void terminateHighPingChecker() {
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
            Logger.log(LogTag.LINK, url);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * The timeout value when determining if a site is reachable.
     */
    public static final int SITE_PING_TIMEOUT = (int) (TimeUtil.MILLISECONDS_IN_SECOND * 5);

    /**
     * So no head?
     */
    private static final String HEAD = "HEAD";

    /**
     * The minimum HTTP response code indicating a successful response.
     */
    public static final int MIN_SUCCESSFUL_RESPONSE_CODE = 200;

    /**
     * The maximum HTTP response code indicating a successful response.
     */
    public static final int MAX_SUCCESSFUL_RESPONSE_CODE = 299;

    /**
     * The minimum HTTP response code indicating a successful redirection.
     */
    public static final int MIN_REDIRECTED_RESPONSE_CODE = 300;

    /**
     * The maximum HTTP response code indicating a successful redirection.
     */
    public static final int MAX_REDIRECTED_RESPONSE_CODE = 399;

    /**
     * The range of response codes that indicate a website as reachable/readable.
     */
    public static final Range<Integer> SITE_REACHABLE_RESPONSE_CODE_RANGE
            = Range.closed(MIN_SUCCESSFUL_RESPONSE_CODE, MAX_REDIRECTED_RESPONSE_CODE);

    /**
     * The prefix for https urls.
     */
    private static final String HTTPS = "https";

    /**
     * The prefix for http urls.
     */
    private static final String HTTP = "http";

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
        if (url.startsWith(HTTPS)) url = url.replaceAll("^" + HTTPS, HTTP);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(SITE_PING_TIMEOUT);
            connection.setReadTimeout(SITE_PING_TIMEOUT);
            connection.setRequestMethod(HEAD);

            int responseCode = connection.getResponseCode();
            return SITE_REACHABLE_RESPONSE_CODE_RANGE.contains(responseCode);
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

    /**
     * The currently set latency ip.
     */
    private static String latencyIp = LATENCY_GOOGLE_IP;

    /**
     * The currently set latency port.
     */
    private static int latencyPort = LATENCY_GOOGLE_PORT;

    /**
     * The currently set latency host name.
     */
    private static String latencyHostName = LATENCY_GOOGLE_HOST_NAME;

    static {
        if (PropLoader.propExists(LATENCY_IP_KEY)) {
            latencyIp = PropLoader.getString(LATENCY_IP_KEY);
            Logger.log(LogTag.DEBUG, "Set latency ip as " + latencyIp);
        }

        if (PropLoader.propExists(LATENCY_PORT_KEY)) {
            latencyPort = PropLoader.getInteger(LATENCY_PORT_KEY);
            Logger.log(LogTag.DEBUG, "Set latency port as " + latencyPort);
        }

        if (PropLoader.propExists(LATENCY_NAME)) {
            latencyHostName = PropLoader.getString(LATENCY_NAME);
            Logger.log(LogTag.DEBUG, "Set latency host name as " + latencyHostName);
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
        SocketAddress address = new InetSocketAddress(latencyIp, latencyPort);
        long start = System.currentTimeMillis();

        try {
            socket.connect(address, timeout);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            return Integer.MAX_VALUE;
        }

        long stop = System.currentTimeMillis();
        int latency = (int) (stop - start);

        try {
            socket.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        Logger.log(LogTag.DEBUG, "Latency of "
                + latencyIp + CyderStrings.colon + latencyPort
                + " (" + latencyHostName + ") found to be " + TimeUtil.formatMillis(latency));

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
    @CanIgnoreReturnValue /* Can be used to ensure a url is valid as a precondition */
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
    public static Optional<String> getUrlTitle(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        String ret = null;

        try {
            Document document = Jsoup.connect(url).get();
            return Optional.of(document.title());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
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

        return CyderRegexPatterns.urlFormationPattern.matcher(url).matches();
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
    public static final String ispQueryUrl = "https://www.whatismyisp.com/";

    /**
     * A record used to store the data after {@link #getIspAndNetworkDetails} is invoked.
     */
    public record IspQueryResult(String isp, String hostname, String ip, String city, String state, String country) {}

    /**
     * The class name of the isp html element.
     * This is Tailwind and prone to change.
     */
    private static final String ispClassName = "block text-4xl";

    /**
     * The class name of the html element containing the city, state, and country.
     * This is Tailwind and prone to change.
     */
    private static final String cityStateCountryClassName = "grid grid-cols-3 gap-2 px-6 pb-6";

    /**
     * The class name of the html element containing the host name.
     */
    private static final String hostnameClassName = "prose";

    /**
     * The index of the city element in its parent element.
     */
    private static final int cityIndex = 2;

    /**
     * The index of the state element in its parent element.
     */
    private static final int stateIndex = 4;

    /**
     * The index of the country element in its parent element.
     */
    private static final int countryIndex = 6;

    /**
     * The index of the hostname in its parent element.
     */
    private static final int hostnameIndex = 3;

    /**
     * The class name of the element containing the ip.
     */
    private static final String ipElementClassName = "px-14 font-semibold break-all";

    /**
     * The index of the ip element inside its parent.
     */
    private static final int ipElementIndex = 0;

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
        Elements cityStateCountryElements = locationDocument.getElementsByClass(cityStateCountryClassName);
        if (cityStateCountryElements.size() < 1) {
            throw new FatalException("Could not parse document for city state country element");
        }
        Element firstCityStateCountryElement = cityStateCountryElements.get(0);
        Elements cityStateCountryElementAllElements = firstCityStateCountryElement.getAllElements();
        if (cityStateCountryElementAllElements.size() < countryIndex - 1) {
            throw new FatalException("Not enough city state country sub elements");
        }
        String city = cityStateCountryElementAllElements.get(cityIndex).text();
        String state = cityStateCountryElementAllElements.get(stateIndex).text();
        String country = cityStateCountryElementAllElements.get(countryIndex).text();

        Elements hostnameElements = locationDocument.getElementsByClass(hostnameClassName);
        if (hostnameElements.size() < hostnameIndex) {
            throw new FatalException("Not enough hostname elements");
        }

        String rawHostname = hostnameElements.get(hostnameIndex).text();
        String hostname = filterHostname(rawHostname);

        Elements ipElements = locationDocument.getElementsByClass(ipElementClassName);
        if (ipElements.isEmpty()) {
            throw new FatalException("Not enough ip elements");
        }
        Element ipElement = ipElements.get(ipElementIndex);
        String ip = ipElement.text().replaceAll(CyderRegexPatterns.nonNumberAndPeriodRegex, "");

        return new IspQueryResult(isp, hostname, ip, city, state, country);
    }

    /**
     * Filters the hostname out of the rest of the text of the element.
     *
     * @param rawClassResult the element text
     * @return the filtered hostname
     */
    @ForReadability
    private static String filterHostname(String rawClassResult) {
        rawClassResult = rawClassResult.substring(rawClassResult.indexOf("'") + 1);
        return rawClassResult.substring(0, rawClassResult.indexOf("'"));
    }
}
