package cyder.utils;

import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.remote.ip.IPData;
import cyder.user.UserUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Utility methods for ip data queries.
 */
public final class IPUtil {
    /**
     * Suppress default constructor.
     */
    private IPUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The current ip data object.
     */
    private static IPData ipdata;

    /**
     * Whether the ip data has been fetched at least once during this Cyder instance.
     */
    private static boolean fetched;

    /**
     * Updates the ip data object encapsulated and returns it.
     *
     * @return the encapsulated ip data object
     */
    public static IPData getIpData() {
        if (!fetched) {
            refreshIpData();
            fetched = true;
        }
        return ipdata;
    }

    /**
     * Refreshes this object's IPData var
     */
    public static void refreshIpData() {
        if (UserUtil.getCyderUser() == null) return;

        String key = PropLoader.getString("ip_key");

        if (key.trim().isEmpty()) {
            Console.INSTANCE.getConsoleCyderFrame().inform("Sorry, but the IP Key has "
                    + "not been set or is invalid, as a result, many features of Cyder will not "
                    + "work as intended. Please see the fields panel of the user editor to learn"
                    + " how to acquire a key and set it.", "IP Key Not Set");
            return;
        }

        String url = CyderUrls.IPDATA_BASE + key;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            ipdata = SerializationUtil.fromJson(reader, IPData.class);
        } catch (IOException e) {
            ExceptionHandler.silentHandle(e);
        }
    }
}
