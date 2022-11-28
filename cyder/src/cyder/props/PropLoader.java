package cyder.props;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import static cyder.props.PropConstants.*;

/**
 * A class for loading ini props from props.ini used throughout Cyder.
 */
public final class PropLoader {
    /**
     * The character a line must end with to interpret the next line as being the same prop.
     */
    private static final String multiLinePropSuffix = "\\";

    /**
     * The props immutable list.
     */
    private static ImmutableList<Prop> props = ImmutableList.of();

    /**
     * Whether the props have been loaded.
     */
    private static boolean propsLoaded;

    /**
     * Suppress default constructor.
     */
    private PropLoader() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Reloads the props from the found prop files.
     */
    public static void reloadProps() {
        propsLoaded = false;
        props = ImmutableList.of();
        loadProps();
    }

    /**
     * Returns the props list.
     *
     * @return the props list
     */
    public static ImmutableList<Prop> getProps() {
        return props;
    }

    /**
     * Returns whether the props have been loaded.
     *
     * @return whether the props have been loaded
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean arePropsLoaded() {
        return propsLoaded;
    }

    /**
     * Returns whether a prop with the provided key can be found.
     *
     * @param key the key
     * @return whether a prop with the provided key can be found
     */
    public static boolean propExists(String key) {
        Preconditions.checkNotNull(key);

        return props.stream().anyMatch(prop -> prop.key().equals(key));
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static String getString(String key) {
        Preconditions.checkArgument(propsLoaded);
        Preconditions.checkNotNull(key);

        for (Prop prop : props) {
            if (prop.key().equals(key)) {
                return prop.value();
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + CyderStrings.quote);
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static boolean getBoolean(String key) {
        Preconditions.checkArgument(propsLoaded);
        Preconditions.checkNotNull(key);

        for (Prop prop : props) {
            if (prop.key().equals(key)) {
                return prop.value().equals("1") || prop.value().equalsIgnoreCase("true");
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + CyderStrings.quote);
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static int getInteger(String key) {
        Preconditions.checkArgument(propsLoaded);
        Preconditions.checkNotNull(key);

        for (Prop prop : props) {
            if (prop.key().equals(key)) {
                return Integer.parseInt(prop.value());
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + CyderStrings.quote);
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static float getFloat(String key) {
        Preconditions.checkArgument(propsLoaded);
        Preconditions.checkNotNull(key);

        for (Prop prop : props) {
            if (prop.key().equals(key)) {
                return Float.parseFloat(prop.value());
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + CyderStrings.quote);
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static double getDouble(String key) {
        Preconditions.checkArgument(propsLoaded);
        Preconditions.checkNotNull(key);

        for (Prop prop : props) {
            if (prop.key().equals(key)) {
                return Double.parseDouble(prop.value());
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + CyderStrings.quote);
    }

    /**
     * Loads the props from all discovered prop files.
     */
    public static void loadProps() {
        Preconditions.checkArgument(!propsLoaded);

        ArrayList<File> propFiles = new ArrayList<>();

        File propsDirectory = new File(PROPS_DIR_NAME);
        File[] propFilesArray = propsDirectory.listFiles();

        if (propFilesArray == null || propFilesArray.length == 1) {
            throw new FatalException("Could not find any prop files");
        }

        Arrays.stream(propFilesArray).forEach(file -> {
            if (file.getName().startsWith(PROP_FILE_PREFIX) && FileUtil.validateExtension(file, PROP_EXTENSION)) {
                propFiles.add(file);
                Logger.log(LogTag.PROPS_ACTION, "Found prop file: " + file);
            }
        });

        try {
            ArrayList<Prop> propsList = new ArrayList<>();

            propFiles.forEach(propFile -> {
                ArrayList<String> lines = new ArrayList<>();

                try (BufferedReader reader = new BufferedReader(new FileReader(propFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) lines.add(line);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                boolean logNextProp = true;
                StringBuilder previousLinesOfMultilineProp = new StringBuilder();

                for (String line : lines) {
                    if (isComment(line)) {
                        continue;
                    } else if (StringUtil.isNullOrEmpty(line)) {
                        continue;
                    } else if (isNoLogAnnotation(line)) {
                        logNextProp = false;
                        continue;
                    } else if (line.endsWith(multiLinePropSuffix)) {
                        previousLinesOfMultilineProp.append(line.substring(0, line.length() - 1).trim());
                        continue;
                    }

                    String fullLine = previousLinesOfMultilineProp.toString();
                    fullLine += fullLine.isEmpty() ? line : StringUtil.trimLeft(line);

                    Prop addProp = extractProp(fullLine);
                    if (propsList.contains(addProp)) {
                        throw new FatalException("Duplicate prop found: " + addProp);
                    }

                    propsList.add(addProp);
                    Logger.log(LogTag.PROPS_ACTION, "[key: " + addProp.key()
                            + (logNextProp ? ", value: " + addProp.value() : "") + CyderStrings.closingBracket);

                    logNextProp = true;
                    previousLinesOfMultilineProp = new StringBuilder();
                }
            });

            props = ImmutableList.copyOf(propsList);
        } catch (Exception e) {
            // Props aren't loaded if this isn't a reloading meaning ExceptionHandler won't help us :/
            e.printStackTrace();
            props = ImmutableList.of();
        } finally {
            propsLoaded = true;
        }
    }

    /**
     * Returns whether provided line is indicative of a comment in a prop file
     *
     * @param line the line to parse a prop from
     * @return whether the provided line is a comment
     */
    @ForReadability
    private static boolean isComment(String line) {
        return line.trim().startsWith(COMMENT_PATTERN);
    }

    /**
     * Returns whether the provided line is a "no log" annotation.
     *
     * @param line the line to parse a prop from
     * @return whether the provided line is a no log annotation
     */
    @ForReadability
    private static boolean isNoLogAnnotation(String line) {
        return line.trim().equals(Annotation.NO_LOG.getAnnotation());
    }

    /**
     * Attempts to extract a prop key and value from the provided line.
     *
     * @param line the line to extract the prop from
     * @return the extracted prop
     */
    private static Prop extractProp(String line) {
        Preconditions.checkNotNull(line);

        String[] parts = line.split(KEY_VALUE_SEPARATOR);
        Preconditions.checkArgument(parts.length > 1);

        if (parts.length == 2) {
            String key = parts[0].trim();
            String value = parts[1].trim();
            return new Prop(key, value);
        }

        // Figure out where key ends and value starts
        int lastKeyIndex = -1;
        for (int i = 0 ; i < parts.length - 1 ; i++) {
            if (partIsEscaped(parts[i])) {
                parts[i] = parts[i].substring(0, parts[i].length() - 1);
                continue;
            }

            if (lastKeyIndex != -1) throw new IllegalStateException("Could not parse line: " + line);
            lastKeyIndex = i;
        }

        if (lastKeyIndex == -1) throw new IllegalStateException("Could not parse line: " + line);

        // Build key
        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 0 ; i <= lastKeyIndex ; i++) {
            keyBuilder.append(parts[i]);

            if (i != lastKeyIndex) {
                keyBuilder.append(KEY_VALUE_SEPARATOR);
            }
        }

        // Build value
        StringBuilder valueBuilder = new StringBuilder();
        for (int i = lastKeyIndex + 1 ; i < parts.length ; i++) {
            valueBuilder.append(parts[i]);

            if (i != parts.length - 1) {
                valueBuilder.append(KEY_VALUE_SEPARATOR);
            }
        }

        String key = keyBuilder.toString().trim();
        String value = valueBuilder.toString().trim();
        return new Prop(key, value);
    }

    /**
     * Returns whether the provided part ends with the escape pattern.
     *
     * @param part the part
     * @return whether the provided part ends with the escape pattern
     */
    @ForReadability
    private static boolean partIsEscaped(String part) {
        Preconditions.checkNotNull(part);

        return part.endsWith(escapeSequence);
    }
}
