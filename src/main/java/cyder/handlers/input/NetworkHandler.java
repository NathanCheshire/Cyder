package cyder.handlers.input;

import com.google.common.collect.ImmutableList;
import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderUrls;
import cyder.enumerations.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.IpDataManager;
import cyder.network.NetworkUtil;
import cyder.network.ScrapingUtil;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.usb.UsbDevice;
import cyder.usb.UsbUtil;
import cyder.user.UserFile;
import cyder.utils.MapUtil;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * A handler for things which require internet access and may reach out to external domains for data.
 */
public class NetworkHandler extends InputHandler {
    /**
     * The name of the waiter thread for getting the usb devices.
     */
    private static final String USB_DEVICE_WAITER_THREAD_NAME = "Usb Device Waiter";

    /**
     * The name of the thread for performing the whereami command.
     */
    private static final String WHEREAMI_THREAD_NAME = "Whereami Information Finder";

    /**
     * The length of the map shown for the where am i command.
     */
    private static final int WHERE_AM_I_MAP_LENGTH = 200;

    /**
     * Suppress default constructor.
     */
    private NetworkHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"define", "wikisum", "ip", "pastebin", "download", "usb", "curl", "whereami", "network devices"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("define")) {
            if (!getInputHandler().checkArgsLength(0)) {
                getInputHandler().println(StringUtil.getDefinition(getInputHandler().argsToString()));
            } else {
                getInputHandler().println("define usage: define YOUR_WORD/expression");
            }
        } else if (getInputHandler().commandIs("wikisum")) {
            if (!getInputHandler().checkArgsLength(0)) {
                String wikiSumSearch = getInputHandler().argsToString();
                Optional<String> wikiSumOptional = StringUtil.getWikipediaSummary(wikiSumSearch);
                if (wikiSumOptional.isPresent()) {
                    String wikiSum = wikiSumOptional.get();
                    getInputHandler().println(wikiSum);
                } else {
                    getInputHandler().print("Wikipedia article not found");
                }
            } else {
                getInputHandler().println("wikisum usage: wikisum YOUR_WORD/expression");
            }
        } else if (getInputHandler().commandIs("ip")) {
            String ipDataFoundIp = IpDataManager.INSTANCE.getIpData().getIp();
            getInputHandler().println(Objects.requireNonNullElseGet(ipDataFoundIp,
                    () -> NetworkUtil.getIp().orElse("IP not found")));
        } else if (getInputHandler().commandIs("pastebin")) {
            if (getInputHandler().checkArgsLength(1)) {
                String urlString;
                if (getInputHandler().getArg(0).contains("pastebin.com")) {
                    urlString = getInputHandler().getArg(0);
                } else {
                    urlString = CyderUrls.PASTEBIN_RAW_BASE + getInputHandler().getArg(1);
                }

                try {
                    URL url = new URL(urlString);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        getInputHandler().println(line);
                    }

                    reader.close();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    getInputHandler().println("Unknown pastebin url/UUID");
                }
            } else {
                getInputHandler().println("pastebin usage: pastebin [URL/UUID]\nExample: pastebin xa7sJvNm");
            }
        } else if (getInputHandler().commandIs("usb")) {
            CyderThreadRunner.submit(() -> {
                getInputHandler().println("Devices connected to " + OsUtil.getComputerName() + " via USB protocol:");

                Future<ImmutableList<UsbDevice>> futureDevices = UsbUtil.getUsbDevices();
                while (!futureDevices.isDone()) Thread.onSpinWait();

                try {
                    futureDevices.get().forEach(device -> {
                        getInputHandler().println("Status: " + device.getStatus());
                        getInputHandler().println("Type: " + device.getType());
                        getInputHandler().println("Friendly name: " + device.getFriendlyName());
                        getInputHandler().println("Instance ID: " + device.getInstanceId());
                        getInputHandler().println("-------------------------");
                    });
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, USB_DEVICE_WAITER_THREAD_NAME);
        } else if (getInputHandler().commandIs("download")) {
            if (getInputHandler().checkArgsLength(1)) {
                if (NetworkUtil.isValidUrl(getInputHandler().getArg(0))) {
                    Optional<String> optionalResponseName = NetworkUtil.getUrlTitle(getInputHandler().getArg(0));
                    String saveName = SecurityUtil.generateUuid();

                    if (optionalResponseName.isPresent()) {
                        String responseName = optionalResponseName.get();
                        if (!responseName.isEmpty()) {
                            saveName = responseName;
                        }
                    }

                    File saveFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(), Console.INSTANCE.getUuid(),
                            UserFile.FILES.getName(), saveName);

                    getInputHandler().println("Saving file: " + saveName + " to files directory");

                    CyderThreadRunner.submit(() -> {
                        try {
                            if (NetworkUtil.downloadResource(getInputHandler().getArg(0), saveFile)) {
                                getInputHandler().println("Successfully saved");
                            } else {
                                getInputHandler().println("Error: could not download at this time");
                            }
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }, "File URL Downloader");
                } else {
                    getInputHandler().println("Invalid url");
                }
            } else {
                getInputHandler().println("download usage: download [YOUR LINK]");
            }
        } else if (getInputHandler().commandIs("curl")) {
            if (getInputHandler().checkArgsLength(1)) {
                if (NetworkUtil.isValidUrl(getInputHandler().getArg(0))) {
                    try {
                        URL url = new URL(getInputHandler().getArg(0));
                        HttpURLConnection http = (HttpURLConnection) url.openConnection();

                        getInputHandler().println(NetworkUtil.readUrl(getInputHandler().getArg(0)));
                        getInputHandler().println("Response: " + http.getResponseCode()
                                + CyderStrings.space + http.getResponseMessage());

                        http.disconnect();
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                } else {
                    getInputHandler().println("Invalid url");
                }
            } else {
                getInputHandler().println("Curl command usage: curl [URL]");
            }
        } else if (getInputHandler().inputIgnoringSpacesMatches("whereami")) {
            CyderThreadRunner.submit(() -> {
                ScrapingUtil.IspQueryResult result = ScrapingUtil.getIspAndNetworkDetails();
                getInputHandler().println("You live in " + result.city() + ", " + result.state());
                getInputHandler().println("Your country is: " + result.country());
                getInputHandler().println("Your ip is: " + result.ip());
                getInputHandler().println("Your isp is: " + result.isp());
                getInputHandler().println("Your hostname is: " + result.hostname());

                MapUtil.Builder builder = new MapUtil.Builder(WHERE_AM_I_MAP_LENGTH, WHERE_AM_I_MAP_LENGTH,
                        Props.mapQuestApiKey.getValue());
                builder.setScaleBar(false);
                builder.setLocationString(result.city()
                        .replaceAll(CyderRegexPatterns.whiteSpaceRegex, NetworkUtil.URL_SPACE) + ","
                        + result.state().replaceAll(CyderRegexPatterns.whiteSpaceRegex, NetworkUtil.URL_SPACE) + ","
                        + result.country().replaceAll(CyderRegexPatterns.whiteSpaceRegex, NetworkUtil.URL_SPACE));
                builder.setZoomLevel(8);
                builder.setFilterWaterMark(true);

                try {
                    ImageIcon icon = MapUtil.getMapView(builder);
                    getInputHandler().println(icon);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, WHEREAMI_THREAD_NAME);
        } else if (getInputHandler().inputIgnoringSpacesMatches("networkdevices")) {
            OsUtil.getNetworkDevices().forEach(networkDevice -> {
                getInputHandler().println("Name: " + networkDevice.name());
                getInputHandler().println("Display name: " + networkDevice.displayName());
            });
        } else {
            ret = false;
        }

        return ret;
    }
}
