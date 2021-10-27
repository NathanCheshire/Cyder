package cyder.utilities;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import cyder.handler.ErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

public class IPUtil {
    private IPUtil() {}

    private static IPData ipdata;
    private static boolean fetched = false;

    public static IPData getIpdata() {
        if (!fetched) {
            parseData();
            fetched = true;
        }
        return ipdata;
    }

    /**
     * Refreshes this object's IPData var
     */
    public static void parseData() {
        Gson gson = new Gson();
        String url = "https://api.ipdata.co/?api-key=" + UserUtil.extractUser().getIpkey();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            ipdata = gson.fromJson(reader, IPData.class);
        } catch (IOException e) {
            ErrorHandler.handle(e);
        }
    }

    //IPData inner class

    public static class IPData {
        private String ip;
        private boolean is_eu;
        private String city;
        private String region;
        private String region_code;
        private String country_name;
        private String country_code;
        private String continent_name;
        private String continent_code;
        private String latitude;
        private String longitude;
        private String postal;
        private String calling_code;
        private String flag;
        private String emoji_flag;
        private String emoji_unicode;
        private Asn asn;
        private LinkedList<Language> languages;
        private Currency currency;
        private TimeZone time_zone;
        private Threat threat;
        private String count;

        public String getIp() {
            return ip;
        }

        public boolean isIs_eu() {
            return is_eu;
        }

        public String getCity() {
            return city;
        }

        public String getRegion() {
            return region;
        }

        public String getRegion_code() {
            return region_code;
        }

        public String getCountry_name() {
            return country_name;
        }

        public String getCountry_code() {
            return country_code;
        }

        public String getContinent_name() {
            return continent_name;
        }

        public String getContinent_code() {
            return continent_code;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public String getPostal() {
            return postal;
        }

        public String getCalling_code() {
            return calling_code;
        }

        public String getFlag() {
            return flag;
        }

        public String getEmoji_flag() {
            return emoji_flag;
        }

        public String getEmoji_unicode() {
            return emoji_unicode;
        }

        public Asn getAsn() {
            return asn;
        }

        public LinkedList<Language> getLanguages() {
            return languages;
        }

        public Currency getCurrency() {
            return currency;
        }

        public TimeZone getTime_zone() {
            return time_zone;
        }

        public Threat getThreat() {
            return threat;
        }

        public String getCount() {
            return count;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setIs_eu(boolean is_eu) {
            this.is_eu = is_eu;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public void setRegion_code(String region_code) {
            this.region_code = region_code;
        }

        public void setCountry_name(String country_name) {
            this.country_name = country_name;
        }

        public void setCountry_code(String country_code) {
            this.country_code = country_code;
        }

        public void setContinent_name(String continent_name) {
            this.continent_name = continent_name;
        }

        public void setContinent_code(String continent_code) {
            this.continent_code = continent_code;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public void setPostal(String postal) {
            this.postal = postal;
        }

        public void setCalling_code(String calling_code) {
            this.calling_code = calling_code;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public void setEmoji_flag(String emoji_flag) {
            this.emoji_flag = emoji_flag;
        }

        public void setEmoji_unicode(String emoji_unicode) {
            this.emoji_unicode = emoji_unicode;
        }

        public void setAsn(Asn asn) {
            this.asn = asn;
        }

        public void setLanguages(LinkedList<Language> languages) {
            this.languages = languages;
        }

        public void setCurrency(Currency currency) {
            this.currency = currency;
        }

        public void setTime_zone(TimeZone time_zone) {
            this.time_zone = time_zone;
        }

        public void setThreat(Threat threat) {
            this.threat = threat;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public static class Asn {
            private String asn;
            private String name;
            private String domain;
            private String route;
            private String type;

            public void setAsn(String asn) {
                this.asn = asn;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setDomain(String domain) {
                this.domain = domain;
            }

            public void setRoute(String route) {
                this.route = route;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getAsn() {
                return asn;
            }

            public String getName() {
                return name;
            }

            public String getDomain() {
                return domain;
            }

            public String getRoute() {
                return route;
            }

            public String getType() {
                return type;
            }
        }

        public static class Language {
            private String name;

            @SerializedName("native")
            private String native_;
        }

        public static class Currency {
            private String name;
            private String code;
            private String symbol;

            @SerializedName("native")
            private String _native;
            private String plural;

            public void setName(String name) {
                this.name = name;
            }

            public void setCode(String code) {
                this.code = code;
            }

            public void setSymbol(String symbol) {
                this.symbol = symbol;
            }

            public void set_native(String _native) {
                this._native = _native;
            }

            public void setPlural(String plural) {
                this.plural = plural;
            }

            public String getName() {
                return name;
            }

            public String getCode() {
                return code;
            }

            public String getSymbol() {
                return symbol;
            }

            public String get_native() {
                return _native;
            }

            public String getPlural() {
                return plural;
            }
        }

        public static class TimeZone {
            private String name;
            private String abbr;
            private String offset;
            private boolean is_dst;
            private String current_time;

            public String getName() {
                return name;
            }

            public String getAbbr() {
                return abbr;
            }

            public String getOffset() {
                return offset;
            }

            public boolean isIs_dst() {
                return is_dst;
            }

            public String getCurrent_time() {
                return current_time;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setAbbr(String abbr) {
                this.abbr = abbr;
            }

            public void setOffset(String offset) {
                this.offset = offset;
            }

            public void setIs_dst(boolean is_dst) {
                this.is_dst = is_dst;
            }

            public void setCurrent_time(String current_time) {
                this.current_time = current_time;
            }
        }

        public static class Threat {
            private boolean is_tor;
            private boolean is_proxy;
            private boolean is_anonymous;
            private boolean is_known_attacker;
            private boolean is_known_abuser;
            private boolean is_threat;
            private boolean is_bogon;

            public void setIs_tor(boolean is_tor) {
                this.is_tor = is_tor;
            }

            public void setIs_proxy(boolean is_proxy) {
                this.is_proxy = is_proxy;
            }

            public void setIs_anonymous(boolean is_anonymous) {
                this.is_anonymous = is_anonymous;
            }

            public void setIs_known_attacker(boolean is_known_attacker) {
                this.is_known_attacker = is_known_attacker;
            }

            public void setIs_known_abuser(boolean is_known_abuser) {
                this.is_known_abuser = is_known_abuser;
            }

            public void setIs_threat(boolean is_threat) {
                this.is_threat = is_threat;
            }

            public void setIs_bogon(boolean is_bogon) {
                this.is_bogon = is_bogon;
            }

            public boolean isIs_tor() {
                return is_tor;
            }

            public boolean isIs_proxy() {
                return is_proxy;
            }

            public boolean isIs_anonymous() {
                return is_anonymous;
            }

            public boolean isIs_known_attacker() {
                return is_known_attacker;
            }

            public boolean isIs_known_abuser() {
                return is_known_abuser;
            }

            public boolean isIs_threat() {
                return is_threat;
            }

            public boolean isIs_bogon() {
                return is_bogon;
            }
        }
    }
}
