package cyder.network;

import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.strings.CyderStrings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Utilities related to web-scraping.
 */
public final class ScrapingUtil {
    /**
     * The url for determining network details.
     */
    public static final String ispQueryUrl = "https://www.whatismyisp.com/";

    /**
     * A record used to store the data after {@link #getIspAndNetworkDetails} is invoked.
     */
    public record IspQueryResult(String isp, String hostname, String ip, String city, String state, String country) {}

    /**
     * The class name of the isp html element.
     * This is Tailwind and prone to change.
     */
    private static final String ispClassName = "block text-4xl";

    /**
     * The class name of the html element containing the city, state, and country.
     * This is Tailwind and prone to change.
     */
    private static final String cityStateCountryClassName = "grid grid-cols-3 gap-2 px-6 pb-6";

    /**
     * The class name of the html element containing the host name.
     */
    private static final String hostnameClassName = "prose";

    /**
     * The index of the city element in its parent element.
     */
    private static final int cityIndex = 2;

    /**
     * The index of the state element in its parent element.
     */
    private static final int stateIndex = 4;

    /**
     * The index of the country element in its parent element.
     */
    private static final int countryIndex = 6;

    /**
     * The index of the hostname in its parent element.
     */
    private static final int hostnameIndex = 3;

    /**
     * The class name of the element containing the ip.
     */
    private static final String ipElementClassName = "px-14 font-semibold break-all";

    /**
     * The index of the ip element inside its parent.
     */
    private static final int ipElementIndex = 0;

    /**
     * Suppress default constructor.
     */
    private ScrapingUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Returns information about this user's isp, their ip, location, city, state/region, and country.
     *
     * @return information about this user's isp, their ip, location, city, state/region, and country
     */
    public static IspQueryResult getIspAndNetworkDetails() {
        Document locationDocument = null;

        try {
            locationDocument = Jsoup.connect(ispQueryUrl).get();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (locationDocument == null) {
            throw new FatalException("Could not obtain document from isp query url");
        }

        String isp = locationDocument.getElementsByClass(ispClassName).text();
        Elements cityStateCountryElements = locationDocument.getElementsByClass(cityStateCountryClassName);
        if (cityStateCountryElements.size() < 1) {
            throw new FatalException("Could not parse document for city state country element");
        }
        Element firstCityStateCountryElement = cityStateCountryElements.get(0);
        Elements cityStateCountryElementAllElements = firstCityStateCountryElement.getAllElements();
        if (cityStateCountryElementAllElements.size() < countryIndex - 1) {
            throw new FatalException("Not enough city state country sub elements");
        }
        String city = cityStateCountryElementAllElements.get(cityIndex).text();
        String state = cityStateCountryElementAllElements.get(stateIndex).text();
        String country = cityStateCountryElementAllElements.get(countryIndex).text();

        Elements hostnameElements = locationDocument.getElementsByClass(hostnameClassName);
        if (hostnameElements.size() < hostnameIndex) {
            throw new FatalException("Not enough hostname elements");
        }

        String rawHostname = hostnameElements.get(hostnameIndex).text();
        String rawClassResult = rawHostname.substring(rawHostname.indexOf(CyderStrings.singleQuote) + 1);
        String hostname = rawClassResult.substring(0, rawClassResult.indexOf(CyderStrings.singleQuote));

        Elements ipElements = locationDocument.getElementsByClass(ipElementClassName);
        if (ipElements.isEmpty()) {
            throw new FatalException("Not enough ip elements");
        }
        Element ipElement = ipElements.get(ipElementIndex);
        String ip = ipElement.text().replaceAll(CyderRegexPatterns.nonNumberAndPeriodRegex, "");

        return new IspQueryResult(isp, hostname, ip, city, state, country);
    }
}
