package com.cyder.utilities;

import com.cyder.handler.ErrorHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class IPUtil {

    private IPUtil() {} //private constructor to avoid object creation

    public static String getUserCity() {
        init();
        return userCity;
    }

    public static String getUserState() {
        init();
        return userState;
    }

    public static String getUserCountry() {
        init();
        return userCountry;
    }

    public static String getUserStateAbr() {
        init();
        return userStateAbr;
    }

    public static String getUserCountryAbr() {
        init();
        return userCountryAbr;
    }

    public static String getUserIP() {
        init();
        return userIP;
    }

    public static String getUserPostalCode() {
        init();
        return userPostalCode;
    }

    public static String getUserFlagURL() {
        init();
        return userFlagURL;
    }

    public static String getLat() {
        init();
        return lat;
    }

    public static String getLon() {
        init();
        return lon;
    }

    public static String getIsp() {
        init();
        return isp;
    }

    private static String userCity;
    private static String userState;
    private static String userCountry;
    private static String userStateAbr;
    private static String userCountryAbr;
    private static String userIP;
    private static String userPostalCode;
    private static String userFlagURL;
    private static String lat;
    private static String lon;
    private static String isp;

    private static void init() {
        try {
            String Key = "https://api.ipdata.co/?api-key=" + IOUtil.getSystemData("IP");
            URL Querry = new URL(Key);
            BufferedReader BR = new BufferedReader(new InputStreamReader(Querry.openStream()));
            String CurrentLine;

            while ((CurrentLine = BR.readLine()) != null) {
                if (CurrentLine.contains("city")) {
                    userCity = (CurrentLine.replace("city", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"region\"")) {
                    userState = (CurrentLine.replace("region", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"region_code\"")) {
                    userStateAbr = (CurrentLine.replace("region_code", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("asn")) {
                    CurrentLine = BR.readLine();
                    CurrentLine = BR.readLine();
                    isp = (CurrentLine.replace("name", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"country_name\"")) {
                    userCountry = (CurrentLine.replace("country_name", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"country_code\"")) {
                    userCountryAbr = (CurrentLine.replace("country_code", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"latitude\"")) {
                    lat = (CurrentLine.replace("latitude", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"longitude\"")) {
                    lon = (CurrentLine.replace("longitude", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"ip\"")) {
                    userIP = (CurrentLine.replace("ip", "").replace(",", "").replace("\"", "").replace(":", "").trim());
                }

                else if (CurrentLine.contains("\"flag\"")) {
                    userFlagURL = (CurrentLine.replace("\"flag\"", "").replace("\"","").replace(",", "").trim()).replaceFirst(":","");
                }

                else if (CurrentLine.contains("postal")) {
                    userPostalCode = (CurrentLine.replace("\"postal\"", "").replace("\"","").replace(",", "").replace(":", "").trim());
                }
            }
            BR.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
