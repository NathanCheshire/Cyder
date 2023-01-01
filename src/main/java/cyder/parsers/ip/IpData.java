package cyder.parsers.ip;

import com.google.common.collect.ImmutableList;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.ArrayList;
import java.util.Objects;

/**
 * An object for parsing returned ip data timezone objects.
 */
public class IpData {
    /**
     * The ip address.
     */
    private String ip;

    /**
     * Whether the country is of the EU.
     */
    private boolean is_eu;

    /**
     * The city name where the IP address originates from.
     */
    private String city;

    /**
     * The region name where the IP address originates from.
     */
    private String region;

    /**
     * The region code for the region the IP address originates from.
     */
    private String region_code;

    /**
     * The country name where the IP address originates from.
     */
    private String country_name;

    /**
     * The country code where the IP address originates from.
     */
    private String country_code;

    /**
     * The continent name where the IP address originates from.
     */
    private String continent_name;

    /**
     * The continent code where the IP address originates from.
     */
    private String continent_code;

    /**
     * An approximate latitudinal location for the IP Address. Often near the center of population.
     */
    private String latitude;

    /**
     * An approximate longitudinal location for the IP Address. Often near the center of population.
     */
    private String longitude;

    /**
     * The Postal code for where the IP Address is located.
     */
    private String postal;

    /**
     * The International Calling Code for the country where the IP Address is located.
     */
    private String calling_code;

    /**
     * A link to a PNG/SVG file with the flag of the country where the IP Address is located.
     */
    private String flag;

    /**
     * An emoji version of the flag of the country where the IP Address is located.
     */
    private String emoji_flag;

    /**
     * The Unicode for the emoji flag.
     */
    private String emoji_unicode;

    /**
     * The ASN object.
     */
    private Asn asn;

    /**
     * The list of languages for this address.
     */
    private ArrayList<Language> languages;

    /**
     * The currency for this address.
     */
    private Currency currency;

    /**
     * The timezone for this address.
     */
    private TimeZone time_zone;

    /**
     * The threat status for this address.
     */
    private Threat threat;

    /**
     * The number of queries used by the provided key in a designated time frame.
     */
    private String count;

    /**
     * Constructs a new IpData object.
     */
    public IpData() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the ip address.
     *
     * @return the ip address
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns whether the country is of the EU.
     *
     * @return whether the country is of the EU
     */
    public boolean isIs_eu() {
        return is_eu;
    }

    /**
     * Returns the city name where the IP address originates from.
     *
     * @return the city name where the IP address originates from
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns the region code for the region the IP address originates from.
     *
     * @return the region code for the region the IP address originates from
     */
    public String getRegion() {
        return region;
    }

    /**
     * Returns the region code for the region the IP address originates from.
     *
     * @return the region code for the region the IP address originates from
     */
    public String getRegion_code() {
        return region_code;
    }

    /**
     * Returns the country code where the IP address originates from.
     *
     * @return the country code where the IP address originates from
     */
    public String getCountry_name() {
        return country_name;
    }

    /**
     * Returns the country code where the IP address originates from.
     *
     * @return the country code where the IP address originates from
     */
    public String getCountry_code() {
        return country_code;
    }

    /**
     * Returns the continent name where the IP address originates from.
     *
     * @return the continent name where the IP address originates from
     */
    public String getContinent_name() {
        return continent_name;
    }

    /**
     * Returns the continent code where the IP address originates from.
     *
     * @return the continent code where the IP address originates from
     */
    public String getContinent_code() {
        return continent_code;
    }

    /**
     * Returns an approximate latitudinal location for the IP Address. Often near the center of population.
     *
     * @return an approximate latitudinal location for the IP Address. Often near the center of population
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * Returns an approximate longitudinal location for the IP Address. Often near the center of population.
     *
     * @return an approximate longitudinal location for the IP Address. Often near the center of population
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * Returns the postal code for where the IP Address is located.
     *
     * @return the postal code for where the IP Address is located
     */
    public String getPostal() {
        return postal;
    }

    /**
     * Returns the International Calling Code for the country where the IP Address is located.
     *
     * @return the International Calling Code for the country where the IP Address is located
     */
    public String getCalling_code() {
        return calling_code;
    }

    /**
     * Returns a link to a PNG/SVG file with the flag of the country where the IP Address is located.
     *
     * @return a link to a PNG/SVG file with the flag of the country where the IP Address is located
     */
    public String getFlag() {
        return flag;
    }

    /**
     * Returns the emoji version of the flag of the country where the IP Address is located.
     *
     * @return the emoji version of the flag of the country where the IP Address is located
     */
    public String getEmoji_flag() {
        return emoji_flag;
    }

    /**
     * Returns the Unicode for the emoji flag.
     *
     * @return the Unicode for the emoji flag
     */
    public String getEmoji_unicode() {
        return emoji_unicode;
    }

    /**
     * Returns the ASN object.
     *
     * @return the ASN object
     */
    public Asn getAsn() {
        return asn;
    }

    /**
     * Returns the list of languages for this address.
     *
     * @return the list of languages for this address
     */
    public ImmutableList<Language> getLanguages() {
        return ImmutableList.copyOf(languages);
    }

    /**
     * Returns the currency for this address.
     *
     * @return the currency for this address
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Returns the timezone for this address.
     *
     * @return the timezone for this address
     */
    public TimeZone getTime_zone() {
        return time_zone;
    }

    /**
     * Returns the threat status for this address.
     *
     * @return the threat status for this address
     */
    public Threat getThreat() {
        return threat;
    }

    /**
     * Returns the number of queries used by the provided key in a designated time frame.
     *
     * @return the number of queries used by the provided key in a designated time frame
     */
    public String getCount() {
        return count;
    }

    // -------
    // Setters
    // -------

    /**
     * Sets the ip address.
     *
     * @param ip the ip address
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Sets whether the country is of the EU.
     *
     * @param is_eu whether the country is of the EU
     */
    public void setIs_eu(boolean is_eu) {
        this.is_eu = is_eu;
    }

    /**
     * Sets city name where the IP address originates from.
     *
     * @param city city name where the IP address originates from
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Sets the region code for the region the IP address originates from.
     *
     * @param region the region code for the region the IP address originates from
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Sets the region code for the region the IP address originates from.
     *
     * @param region_code the region code for the region the IP address originates from
     */
    public void setRegion_code(String region_code) {
        this.region_code = region_code;
    }

    /**
     * Sets the country code where the IP address originates from.
     *
     * @param country_name country code where the IP address originates from
     */
    public void setCountry_name(String country_name) {
        this.country_name = country_name;
    }

    /**
     * Sets the country code where the IP address originates from.
     *
     * @param country_code the country code where the IP address originates from
     */
    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    /**
     * Sets the continent name where the IP address originates from.
     *
     * @param continent_name continent name where the IP address originates from
     */
    public void setContinent_name(String continent_name) {
        this.continent_name = continent_name;
    }

    /**
     * Sets the continent code where the IP address originates from.
     *
     * @param continent_code the continent code where the IP address originates from
     */
    public void setContinent_code(String continent_code) {
        this.continent_code = continent_code;
    }

    /**
     * Sets an approximate latitudinal location for the IP Address. Often near the center of population.
     *
     * @param latitude an approximate latitudinal location for the IP Address. Often near the center of population
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * Sets an approximate longitudinal location for the IP Address. Often near the center of population.
     *
     * @param longitude an approximate longitudinal location for the IP Address. Often near the center of population
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * Sets the postal code for where the IP Address is located.
     *
     * @param postal the \postal code for where the IP Address is located
     */
    public void setPostal(String postal) {
        this.postal = postal;
    }

    /**
     * Sets the International Calling Code for the country where the IP Address is located.
     *
     * @param calling_code the International Calling Code for the country where the IP Address is located
     */
    public void setCalling_code(String calling_code) {
        this.calling_code = calling_code;
    }

    /**
     * Sets a link to a PNG/SVG file with the flag of the country where the IP Address is located.
     *
     * @param flag a link to a PNG/SVG file with the flag of the country where the IP Address is located
     */
    public void setFlag(String flag) {
        this.flag = flag;
    }

    /**
     * Sets the emoji version of the flag of the country where the IP Address is located.
     *
     * @param emoji_flag the emoji version of the flag of the country where the IP Address is located
     */
    public void setEmoji_flag(String emoji_flag) {
        this.emoji_flag = emoji_flag;
    }

    /**
     * Sets the Unicode for the emoji flag.
     *
     * @param emoji_unicode the Unicode for the emoji flag
     */
    public void setEmoji_unicode(String emoji_unicode) {
        this.emoji_unicode = emoji_unicode;
    }

    /**
     * Sets the ASN object.
     *
     * @param asn the ASN object
     */
    public void setAsn(Asn asn) {
        this.asn = asn;
    }

    /**
     * Sets the list of languages for this address.
     *
     * @param languages the list of languages for this address
     */
    public void setLanguages(ArrayList<Language> languages) {
        this.languages = languages;
    }

    /**
     * Sets the currency for this address.
     *
     * @param currency the currency for this address
     */
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    /**
     * Sets the timezone for this address.
     *
     * @param time_zone the timezone for this address
     */
    public void setTime_zone(TimeZone time_zone) {
        this.time_zone = time_zone;
    }

    /**
     * Sets the threat status for this address.
     *
     * @param threat the threat status for this address
     */
    public void setThreat(Threat threat) {
        this.threat = threat;
    }

    /**
     * Sets the number of queries used by the provided key in a designated time frame.
     *
     * @param count the number of queries used by the provided key in a designated time frame
     */
    public void setCount(String count) {
        this.count = count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof IpData)) {
            return false;
        }

        IpData other = (IpData) o;

        return Objects.equals(ip, other.ip)
                && Objects.equals(is_eu, other.is_eu)
                && Objects.equals(city, other.city)
                && Objects.equals(region, other.region)
                && Objects.equals(region_code, other.region_code)
                && Objects.equals(country_name, other.country_name)
                && Objects.equals(country_code, other.country_code)
                && Objects.equals(continent_name, other.continent_name)
                && Objects.equals(continent_code, other.continent_code)
                && Objects.equals(latitude, other.latitude)
                && Objects.equals(longitude, other.longitude)
                && Objects.equals(postal, other.postal)
                && Objects.equals(calling_code, other.calling_code)
                && Objects.equals(flag, other.flag)
                && Objects.equals(emoji_flag, other.emoji_flag)
                && Objects.equals(emoji_unicode, other.emoji_unicode)
                && Objects.equals(asn, other.asn)
                && Objects.equals(languages, other.languages)
                && Objects.equals(currency, other.currency)
                && Objects.equals(time_zone, other.time_zone)
                && Objects.equals(threat, other.threat)
                && Objects.equals(count, other.count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = 0;

        if (ip != null) ret = 31 * ret + ip.hashCode();
        ret = 31 * ret + Boolean.hashCode(is_eu);
        if (city != null) ret = 31 * ret + city.hashCode();
        if (region != null) ret = 31 * ret + region.hashCode();
        if (region_code != null) ret = 31 * ret + region_code.hashCode();
        if (country_name != null) ret = 31 * ret + country_name.hashCode();
        if (country_code != null) ret = 31 * ret + country_code.hashCode();
        if (continent_name != null) ret = 31 * ret + continent_name.hashCode();
        if (latitude != null) ret = 31 * ret + latitude.hashCode();
        if (longitude != null) ret = 31 * ret + longitude.hashCode();
        if (postal != null) ret = 31 * ret + postal.hashCode();
        if (calling_code != null) ret = 31 * ret + calling_code.hashCode();
        if (flag != null) ret = 31 * ret + flag.hashCode();
        if (emoji_flag != null) ret = 31 * ret + emoji_flag.hashCode();
        if (emoji_unicode != null) ret = 31 * ret + emoji_unicode.hashCode();
        if (asn != null) ret = 31 * ret + asn.hashCode();
        if (languages != null) ret = 31 * ret + languages.hashCode();
        if (currency != null) ret = 31 * ret + currency.hashCode();
        if (time_zone != null) ret = 31 * ret + time_zone.hashCode();
        if (threat != null) ret = 31 * ret + threat.hashCode();
        if (count != null) ret = 31 * ret + count.hashCode();

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "IpData{"
                + "ip=\"" + ip + "\""
                + ", is_eu=" + is_eu
                + ", city=\"" + city + "\""
                + ", region=\"" + region + "\""
                + ", region_code=\"" + region_code + "\""
                + ", country_name=\"" + country_name + "\""
                + ", country_code=\"" + country_code + "\""
                + ", continent_name=\"" + continent_name + "\""
                + ", continent_code=\"" + continent_code + "\""
                + ", latitude=\"" + latitude + "\""
                + ", longitude=\"" + longitude + "\""
                + ", postal=\"" + postal + "\""
                + ", calling_code=\"" + calling_code + "\""
                + ", flag=\"" + flag + "\""
                + ", emoji_flag=\"" + emoji_flag + "\""
                + ", emoji_unicode=\"" + emoji_unicode + "\""
                + ", asn=" + asn
                + ", languages=" + languages
                + ", currency=" + currency
                + ", time_zone=" + time_zone
                + ", threat=" + threat
                + ", count=\"" + count + "\""
                + "}";
    }
}
