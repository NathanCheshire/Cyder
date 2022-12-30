package cyder.network;

import com.google.common.base.Preconditions;
import cyder.constants.CyderUrls;
import cyder.exceptions.FatalException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.parsers.remote.ip.IpData;
import cyder.props.Props;
import cyder.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A manager for this session's IP data.
 */
public enum IpDataManager {
    /**
     * The IpDataManager instance.
     */
    INSTANCE;

    IpDataManager() {
        Logger.log(LogTag.OBJECT_CREATION,
                "IpDataManager constructed, initializing encapsulated IpData object");
    }

    /**
     * The most recent IpData object.
     */
    private final AtomicReference<IpData> ipData = new AtomicReference<>();

    /**
     * Updates the ip data object encapsulated and returns it.
     *
     * @return the encapsulated ip data object
     */
    public IpData getIpData() {
        IpData ret = ipData.get();
        if (ret != null) return ret;

        Optional<IpData> pulledData = pullIpData();
        if (pulledData.isPresent()) {
            ret = ipData.get();
            ipData.set(ret);
            return ret;
        }

        throw new FatalException("Could not fetch IP data");
    }

    /**
     * Pulls and serializes the ip data into an ip data object and
     * returns that object if successful. Empty optional else.
     *
     * @return the ip data object
     */
    private Optional<IpData> pullIpData() {
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
