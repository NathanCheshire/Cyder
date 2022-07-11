package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.user.UserFile;
import cyder.utils.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;

/**
 * A handler for things which require internet access and may reach out to external domains for data.
 */
public class NetworkHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private NetworkHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"define", "wikisum", "ip", "pastebin", "download", "usb", "curl", "whereami", "networkdevices"})
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
            getInputHandler().println("Devices connected to " + OSUtil.getComputerName() + " via USB protocol:");

            for (String line : IOUtil.getUsbDevices()) {
                getInputHandler().println(line);
            }
        } else if (getInputHandler().commandIs("download")) {
            if (getInputHandler().checkArgsLength(1)) {
                if (NetworkUtil.isValidUrl(getInputHandler().getArg(0))) {
                    String responseName = NetworkUtil.getUrlTitle(getInputHandler().getArg(0));
                    String saveName = SecurityUtil.generateUuid();

                    if (responseName != null) {
                        if (!responseName.isEmpty()) {
                            saveName = responseName;
                        }
                    }

                    File saveFile = new File(OSUtil.buildPath(Dynamic.PATH, "users",
                            Console.INSTANCE.getUUID(), UserFile.FILES.getName(), saveName));

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
                getInputHandler().println("Curl command usage: curl URL");
            }
        } else if (getInputHandler().commandIs("whereami")) {
            CyderThreadRunner.submit(() -> {
                try {
                    String url = CyderUrls.LOCATION_URL;

                    Document locationDocument = Jsoup.connect(url).get();
                    Elements primary = locationDocument.getElementsByClass("desktop-title-content");
                    Elements secondary = locationDocument.getElementsByClass("desktop-title-subcontent");

                    getInputHandler().println("You are currently in " + primary.text() + ", " + secondary.text());

                    String isp = "NOT FOUND";

                    String[] lines = NetworkUtil.readUrl(CyderUrls.ISP_URL).split("\n");

                    for (String line : lines) {
                        Matcher matcher = CyderRegexPatterns.whereAmIPattern.matcher(line);
                        if (matcher.find()) {
                            isp = matcher.group(1);
                        }
                    }

                    getInputHandler().println("Your ISP is " + StringUtil.capsFirstWords(isp));
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Location Finder");
        } else if (getInputHandler().commandIs("networkdevices")) {
            getInputHandler().println(OSUtil.getNetworkDevicesString());
        } else {
            ret = false;
        }

        return ret;
    }
}
