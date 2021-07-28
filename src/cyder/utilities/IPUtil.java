package cyder.utilities;

import com.google.gson.Gson;
import cyder.handler.ErrorHandler;
import cyder.obj.IPData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class IPUtil {
    private IPUtil() {}

    private static IPData ipdata;

    public static IPData getIpdata() {
        parseData();
        return ipdata;
    }

    public static void parseData() {
        Gson gson = new Gson();
        String url = "https://api.ipdata.co/?api-key=" + IOUtil.getSystemData().getIpkey();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            ipdata = gson.fromJson(reader, IPData.class);
        } catch (IOException e) {
            ErrorHandler.handle(e);
        }
    }
}
