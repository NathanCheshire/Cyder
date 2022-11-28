package cyder.props;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import cyder.utils.StringUtil;

/**
 * An optional positional argument to adjust a setting about Cyder.
 *
 * @param <T> the type of the value of the prop
 */
@Immutable
public final class Proper<T> {
    /**
     * The name of the prop.
     */
    private final String name;

    /**
     * The default value of the prop.
     */
    private final T defaultValue;

    /**
     * Constructs a new prop.
     *
     * @param name         the name of the prop
     * @param defaultValue the default value of the prop
     */
    public Proper(String name, T defaultValue) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(!name.isEmpty());
        Preconditions.checkNotNull(defaultValue);

        this.name = name;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the name of the prop.
     *
     * @return the name of the prop
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the default value of the prop.
     *
     * @return the default value of the prop
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the value of this prop by first checking the prop files for the
     * prop and if not present, returning the default value.
     *
     * @return the prop value
     */
    public T getValue() {
        // todo search props and if not present, default value

        return defaultValue;
    }

    /**
     * Returns whether the value of the prop is present. This is always true unless the prop
     * type is a String. In that case, the String could be empty.
     *
     * @return whether the prop value is present
     */
    public boolean valuePresent() {
        if (defaultValue instanceof String string) {
            return !StringUtil.isNullOrEmpty(string);
        }

        return true;
    }

    /**
     * The empty string, used for default key values to indicate there is no default value present.
     */
    private static final String EMPTY = "";

    /*
    Regular props.
     */

    public static final Proper<Boolean> propsReloadable = new Proper<>("props_reloadable", false);
    public static final Proper<String> defaultLocation = new Proper<>("default_location", "Tampa,FL,USA");
    public static final Proper<ImmutableList<String>> ignoreData = new Proper<>("ignore_data", ImmutableList.of(
            "typinganimation", "showseconds", "roundedwindows", "windowcolor", "audiolength", "capsmode",
            "typingsound", "showbusyicon", "clockonconsole", "consoleclockformat", "doanimations"));
    public static final Proper<String> fontMetric = new Proper<>("font_metric", "bold");
    public static final Proper<Integer> maxFontSize = new Proper<>("max_font_size", 50);
    public static final Proper<Integer> minFontSize = new Proper<>("min_font_size", 25);

    /**
     * The font for the console clock if enabled.
     */
    public static final Proper<String> consoleClockFontName = new Proper<>("console_clock_font_name", "Agency FB");

    /**
     * The font size for the console clock if enabled.
     */
    public static final Proper<Integer> consoleClockFontSize = new Proper<>("console_clock_font_size", 26);

    /**
     * Whether testing mode is active.
     * (Any CyderTest annotations found with the trigger of "test" will be invoked immediately following a Console load)
     */
    public static final Proper<Boolean> testingMode = new Proper<>("testing_mode", true);

    /*
    Props which should be logged or tracked by VCS.
     */

    public static final Proper<String> weatherKey = new Proper<>("weather_key", EMPTY);
    public static final Proper<String> mapQuestApiKey = new Proper<>("map_quest_api_key", EMPTY);
    public static final Proper<String> youtubeApi3key = new Proper<>("youtube_api_3_key", EMPTY);
    public static final Proper<String> ipKey = new Proper<>("ip_key", EMPTY);
    public static final Proper<String> debugHashName = new Proper<>("debug_hash_name", EMPTY);
    public static final Proper<String> debugHashPassword = new Proper<>("debug_hash_password", EMPTY);
}
