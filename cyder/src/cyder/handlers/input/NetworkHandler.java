package cyder.handlers.input;

import com.google.common.collect.ImmutableList;
import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.network.UsbDevice;
import cyder.network.UsbUtil;
import cyder.threads.CyderThreadRunner;
import cyder.user.UserFile;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;
import cyder.utils.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
                getInputHandler().println(StringUtil.getWikipediaSummary(getInputHandler().argsToString()));
            } else {
                getInputHandler().println("wikisum usage: wikisum YOUR_WORD/expression");
            }
        } else if (getInputHandler().commandIs("ip")) {
            getInputHandler().println(NetworkUtil.getIp().orElse("IP not found"));
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
                    ExceptionHandler.silentHandle(e);
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

                    File saveFile = new File(OsUtil.buildPath(Dynamic.PATH,
                            Dynamic.USERS.getDirectoryName(), Console.INSTANCE.getUuid(),
                            UserFile.FILES.getName(), saveName));

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
                                + " " + http.getResponseMessage());

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
            NetworkUtil.IspQueryResult result = NetworkUtil.getIspAndNetworkDetails();
            getInputHandler().println("You live in " + result.city() + ", " + result.state());
            getInputHandler().println("Your country is: " + result.country());
            getInputHandler().println("Your ip is: " + result.ip());
            getInputHandler().println("Your isp is: " + result.isp());
            getInputHandler().println("Your hostname is: " + result.hostname());
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
