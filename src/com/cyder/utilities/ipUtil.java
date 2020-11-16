package com.cyder.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ipUtil {

    public static String getUserCity() {
        return userCity;
    }

    public static String getUserState() {
        return userState;
    }

    public static String getUserCountry() {
        return userCountry;
    }

    public static String getUserStateAbr() {
        return userStateAbr;
    }

    public static String getUserCountryAbr() {
        return userCountryAbr;
    }

    public static String getUserIP() {
        return userIP;
    }

    public static String getUserPostalCode() {
        return userPostalCode;
    }

    public static String getUserFlagURL() {
        return userFlagURL;
    }

    public static String getLat() {
        return lat;
    }

    public static String getLon() {
        return lon;
    }

    public static String getIsp() {
        return isp;
    }

    public static String userCity;
    public static String userState;
    public static String userCountry;
    public static String userStateAbr;
    public static String userCountryAbr;
    public static String userIP;
    public static String userPostalCode;
    public static String userFlagURL;
    public static String lat;
    public static String lon;
    public static String isp;

    private static Util util;

    public ipUtil() {
        util = new Util();

        try {
            String Key = "https://api.ipdata.co/?api-key=8eac4e7ab34eb235c4a888bfdbedc8bb8093ec1490790d139cf58932";
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
            util.handle(e);
        }
    }
}
