package cyder.weather;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.parsers.remote.weather.WeatherData;
import cyder.props.Props;
import cyder.utils.SerializationUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

/** Utilities related to weather and the weather API, that of Open Weather Map, utilized by Cyder. */
public final class WeatherUtil {
    /** The app id argument. */
    private static final String APP_ID = "&appid=";

    /** The units argument for the weather data. */
    private static final String UNITS_ARG = "&units=";

    /** Suppress default constructor. */
    private WeatherUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /** Possible measurement scales, that of imperial or metric. */
    public enum MeasurementScale {
        /** The imperial measurement scale. */
        IMPERIAL("imperial"),

        /** The metric measurement scale. */
        METRIC("metric");

        private final String weatherDataRepresentation;

        MeasurementScale(String weatherDataRepresentation) {
            this.weatherDataRepresentation = weatherDataRepresentation;
        }

        /**
         * Returns the weather data representation for this measurement scale.
         *
         * @return the weather data representation for this measurement scale
         */
        public String getWeatherDataRepresentation() {
            return weatherDataRepresentation;
        }
    }

    /**
     * Validates the weather key from the propkeys.ini file.
     *
     * @return whether the weather key was valid
     */
    private static boolean validateWeatherKey() {
        String openString = CyderUrls.OPEN_WEATHER_BASE
                + Props.defaultLocation.getValue()
                + APP_ID + Props.weatherKey.getValue()
                + UNITS_ARG + MeasurementScale.IMPERIAL.getWeatherDataRepresentation();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new URL(openString).openStream()))) {
            reader.readLine();
            return true;
        } catch (Exception ex) {
            ExceptionHandler.silentHandle(ex);
        }

        return false;
    }

    /**
     * Returns the weather data object for the provided location string if available. Empty optional else.
     *
     * @param locationString the location string such as "Starkville,Ms,USA"
     * @return the weather data object for the provided location string if available. Empty optional else
     */
    public static Optional<WeatherData> getWeatherData(String locationString) {
        Preconditions.checkNotNull(locationString);
        Preconditions.checkArgument(!locationString.isEmpty());
        Preconditions.checkState(Props.weatherKey.valuePresent());

        String weatherKey = Props.weatherKey.getValue();

        if (weatherKey.isEmpty()) {
            return Optional.empty();
        }

        String OpenString = CyderUrls.OPEN_WEATHER_BASE + locationString + APP_ID
                + weatherKey + UNITS_ARG + MeasurementScale.IMPERIAL.getWeatherDataRepresentation();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(OpenString).openStream()))) {
            return Optional.of(SerializationUtil.fromJson(reader, WeatherData.class));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return Optional.empty();
    }
}
