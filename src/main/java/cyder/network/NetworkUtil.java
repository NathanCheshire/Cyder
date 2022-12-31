package cyder.network;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.IgnoreThread;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Utility methods revolving around networking, urls, servers, etc.
 */
@SuppressWarnings("unused") /* Response codes */
public final class NetworkUtil {
    /**
     * The local host string.
     */
    public static final String LOCALHOST = "localhost";

    /**
     * The range a port must fall into.
     */
    public static final Range<Integer> portRange = Range.closed(1024, 65535);

    /**
     * The string used to represent a space in a url.
     */
    public static final String URL_SPACE = "%20";

    /**
     * The time in ms to wait for a local port to bind to determine whether it is available.
     */
    private static final int LOCAL_PORT_AVAILABLE_TIMEOUT = 400;

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
    private static void setHighLatency(boolean highLatency) {
        NetworkUtil.highLatency = highLatency;
    }

    /**
     * Whether the high ping check is/should be running currently.
     */
    private static final AtomicBoolean highPingCheckerRunning = new AtomicBoolean();

    /**
     * The function used by the high ping checker to provide to TimeUtil.
     */
    private static final Supplier<Boolean> shouldExitHighPingCheckerThread = () ->
            Console.INSTANCE.isClosed() || !highPingCheckerRunning.get();

    /**
     * The timeout between checking for high ping.
     */
    private static final int HIGH_PING_TIMEOUT = (int) (TimeUtil.MILLISECONDS_IN_SECOND
            * TimeUtil.SECONDS_IN_MINUTE * 2.0f);

    /**
     * The timeout between checking for the high ping checker's exit condition.
     */
    private static final int HIGH_PING_EXIT_CHECK = (int) (TimeUtil.MILLISECONDS_IN_SECOND * 2.0f);

    /**
     * Starts the high ping checker.
     */
    public static void startHighPingChecker() {
        if (highPingCheckerRunning.get()) return;

        highPingCheckerRunning.set(true);

        CyderThreadRunner.submit(() -> {
            try {
                while (highPingCheckerRunning.get()) {
                    setHighLatency(!LatencyManager.INSTANCE.currentConnectionHasDecentPing());

                    ThreadUtil.sleepWithChecks(HIGH_PING_TIMEOUT, HIGH_PING_EXIT_CHECK,
                            shouldExitHighPingCheckerThread);
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
     * Returns whether the high ping checker is running.
     *
     * @return whether the high ping checker is running
     */
    public static boolean highPingCheckerRunning() {
        return highPingCheckerRunning.get();
    }

    /**
     * Opens the provided url using the native browser.
     *
     * @param url the url to open
     * @return a pointer to the {@link Process} instance that opened the url if successful. Empty optional else
     */
    @CheckReturnValue
    @CanIgnoreReturnValue
    public static Optional<Process> openUrl(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkArgument(isValidUrl(url));

        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "start", url);
            Process process = builder.start();
            Logger.log(LogTag.LINK, url);
            return Optional.of(process);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return Optional.empty();
    }

    /**
     * The timeout value when determining if a site is reachable.
     */
    public static final int SITE_PING_TIMEOUT = (int) (TimeUtil.MILLISECONDS_IN_SECOND * 5);

    /**
     * The slash slash for urls.
     */
    private static final String slashSlash = "//";

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
     * Pings an HTTP URL. This effectively sends a HEAD request and returns {@code true}
     * if the response code is contained by {@link #SITE_REACHABLE_RESPONSE_CODE_RANGE}.
     *
     * @param url The HTTP URL to be pinged
     * @return whether the given HTTP URL has returned response code 200-399 on a HEAD request within the
     * given timeout
     */
    public static boolean urlReachable(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        if (url.startsWith(HTTPS)) {
            url = url.replaceAll("^" + HTTPS, HTTP);
        }

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
     * Reads from the provided url and returns the response.
     *
     * @param urlString the string of the url to ping and get contents from
     * @return the resulting url response
     */
    @CanIgnoreReturnValue /* Can be used to ensure a url is valid as a Precondition */
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
            ExceptionHandler.handle(e);
        }

        throw new FatalException("Error reading from url: " + urlString);
    }

    /**
     * Returns the title of the provided url according to {@link Jsoup}.
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
     * Note this method is blocking, invocation of it should be in a
     * surrounding thread as to not block the primary thread.
     *
     * @param urlResource   the link to download the file from
     * @param referenceFile the file to save the resource to
     * @return whether the downloading concluded without errors
     */
    public static boolean downloadResource(String urlResource, File referenceFile) throws IOException {
        Preconditions.checkNotNull(urlResource);
        Preconditions.checkArgument(!urlResource.isEmpty());
        Preconditions.checkArgument(isValidUrl(urlResource));
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
     * Returns whether the local port is available for binding.
     * Note this method blocks for approximately {@link #LOCAL_PORT_AVAILABLE_TIMEOUT}ms.
     *
     * @param port the local port
     * @return whether the local port is available for binding
     */
    public static boolean localPortAvailable(int port) {
        Preconditions.checkArgument(portRange.contains(port));

        AtomicBoolean ret = new AtomicBoolean(false);

        CyderThreadRunner.submit(() -> {
            try {
                ServerSocket socket = new ServerSocket(port);
                ret.set(true);
                socket.close();
            } catch (Exception ignored) {}
        }, "Local Port Available Finder, port: " + port);

        ThreadUtil.sleep(LOCAL_PORT_AVAILABLE_TIMEOUT);

        return ret.get();
    }
}
