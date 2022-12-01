package cyder.utils;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Optional;

/**
 * Utilities related to astronomy.
 */
public final class AstronomyUtil {
    /** The url to query for moon phase data. */
    private static final String MOON_PHASE_URL = "https://www.timeanddate.com/moon/phases/";

    /** The html moon element id. */
    private static final String CURRENT_MOON_ID = "cur-moon";

    /** The html moon percent element id. */
    private static final String CURRENT_MOON_PERCENT_ID = "cur-moon-percent";

    /** The html moon phase element id. */
    private static final String PHASE_ID = "qlook";

    /** The src constant. */
    private static final String SRC = "SRC";

    /** The img constant for extracting the moon phase image. */
    private static final String IMG = "img";

    /** The a tag constant. */
    private static final String A_TAG = "a";

    /**
     * Suppress default constructor.
     */
    private AstronomyUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * A record representing a moon phase as defined by stats from
     * <a href="https://www.moongiant.com/phase/today/">moongiant</a>
     */
    public record MoonPhase(String phase, double illumination, String urlImage) {}

    /**
     * Returns the current moon phase.
     *
     * @return the current moon phase if present. Empty optional else
     * @throws IllegalCallerException if any of the following cannot be found: phase, illumination, url image
     */
    public static Optional<MoonPhase> getCurrentMoonPhase() {
        String phase = null;
        double illumination = -1;
        String urlImage = null;

        try {
            Document doc = Jsoup.connect(MOON_PHASE_URL).get();

            // Getting image
            Element moonImageContainer = doc.getElementById(CURRENT_MOON_ID);
            if (moonImageContainer != null) {
                Elements moonImageElements = moonImageContainer.getAllElements();

                if (moonImageElements.size() > 0) {
                    Element imageElement = moonImageElements.get(0).select(IMG).first();

                    if (imageElement != null) {
                        urlImage = imageElement.absUrl(SRC);
                    }
                }
            }

            // Getting illumination
            Element moonPercentElement = doc.getElementById(CURRENT_MOON_PERCENT_ID);
            if (moonPercentElement != null) {
                illumination = Double.parseDouble(moonPercentElement.text().replace("%", ""));
            }

            // Getting phase
            Element phaseElement = doc.getElementById(PHASE_ID);
            if (phaseElement != null) {
                phase = phaseElement.select(A_TAG).text();
            }
        } catch (HttpStatusException e) {
            return Optional.empty();
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
            return Optional.empty();
        }

        if (phase == null) {
            throw new IllegalCallerException("Could not find phase");
        } else if (illumination == -1) {
            throw new IllegalCallerException("Could not find illumination");
        } else if (urlImage == null) {
            throw new IllegalCallerException("Could not find url image");
        }

        return Optional.of(new MoonPhase(phase, illumination, urlImage));
    }
}
