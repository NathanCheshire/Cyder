package cyder.parsers.remote.ip;

import java.util.LinkedList;

/**
 * An object for parsing returned ip data timezone objects.
 */
public class IpData {
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
}
