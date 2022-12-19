package main.java.cyder.utils;

import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.strings.CyderStrings;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Optional;

/**
 * Utilities related to astronomy.
 */
public final class AstronomyUtil {
    /**
     * The url to query for moon phase data.
     */
    private static final String MOON_PHASE_URL = "https://www.timeanddate.com/moon/phases/";

    /**
     * The html moon element id.
     */
    private static final String CURRENT_MOON_ID = "cur-moon";

    /**
     * The html moon percent element id.
     */
    private static final String CURRENT_MOON_PERCENT_ID = "cur-moon-percent";

    /**
     * The html moon phase element id.
     */
    private static final String PHASE_ID = "qlook";

    /**
     * The src constant.
     */
    private static final String SRC = "SRC";

    /**
     * The img constant for extracting the moon phase image.
     */
    private static final String IMG = "img";

    /**
     * The a tag constant.
     */
    private static final String A_TAG = "a";

    /**
     * The index of the moon image element within its parent element.
     */
    private static final int moonImageElementIndex = 0;

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
    public record MoonPhase(String phase, double illumination, String imageUrl) {}

    /**
     * Returns the current moon phase.
     *
     * @return the current moon phase if present. Empty optional else
     * @throws IllegalCallerException if any of the following cannot be found: phase, illumination, url image
     */
    public static Optional<MoonPhase> getCurrentMoonPhase() {
        String phase = null;
        double illumination = -1;
        String imageUrl = null;

        try {
            Document doc = Jsoup.connect(MOON_PHASE_URL).get();

            Element moonImageContainer = doc.getElementById(CURRENT_MOON_ID);
            if (moonImageContainer != null) {
                Elements moonImageElements = moonImageContainer.getAllElements();

                if (!moonImageElements.isEmpty()) {
                    Element imageElement = moonImageElements.get(moonImageElementIndex).select(IMG).first();

                    if (imageElement != null) {
                        imageUrl = imageElement.absUrl(SRC);
                    }
                }
            }

            Element moonPercentElement = doc.getElementById(CURRENT_MOON_PERCENT_ID);
            if (moonPercentElement != null) {
                illumination = Double.parseDouble(moonPercentElement.text().replace("%", ""));
            }

            Element phaseElement = doc.getElementById(PHASE_ID);
            if (phaseElement != null) {
                phase = phaseElement.select(A_TAG).text();
            }
        } catch (HttpStatusException e) {
            return Optional.empty();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
            return Optional.empty();
        }

        if (phase == null || imageUrl == null || illumination == -1) {
            return Optional.empty();
        }

        return Optional.of(new MoonPhase(phase, illumination, imageUrl));
    }
}
