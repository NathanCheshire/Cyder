package cyder.network;

import com.google.common.base.Preconditions;
import cyder.constants.CyderUrls;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.remote.ip.IpData;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

// todo this should be renamed and refactored to an enum singleton manager IpDataManager
/**
 * Utility methods for ip data queries.
 */
public final class IpUtil {
    /**
     * Suppress default constructor.
     */
    private IpUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The most recent IpData object.
     */
    private static final AtomicReference<IpData> mostRecentIpData = new AtomicReference<>();

    /**
     * Updates the ip data object encapsulated and returns it.
     *
     * @return the encapsulated ip data object
     */
    public static IpData getIpData() {
        Preconditions.checkState(Props.ipKey.valuePresent());

        IpData mostRecent = mostRecentIpData.get();
        if (mostRecent != null) {
            return mostRecent;
        } else {
            Optional<IpData> optionalData = pullIpData();
            if (optionalData.isEmpty()) throw new FatalException("Could not get IP data");
            IpData data = optionalData.get();
            mostRecentIpData.set(data);
            return data;
        }
    }

    /**
     * Pulls and serializes ip data into an ip data object and returns that object if found. Empty optional else.
     *
     * @return an ip data object
     */
    public static Optional<IpData> pullIpData() {
        Preconditions.checkState(Props.ipKey.valuePresent());

        String key = Props.ipKey.getValue();

        String url = CyderUrls.IPDATA_BASE + key;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            return Optional.of(SerializationUtil.fromJson(reader, IpData.class));
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }
}
