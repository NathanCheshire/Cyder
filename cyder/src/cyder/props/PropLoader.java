package cyder.props;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CheckReturnValue;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;

import static cyder.props.PropConstants.*;

/**
 * A class for loading ini props from props.ini used throughout Cyder.
 */
public final class PropLoader {
    /**
     * The props immutable list.
     */
    private static ImmutableList<Prop> props = ImmutableList.of();

    /**
     * The new props map of keys to the string values which require casting.
     */
    private static ImmutableMap<String, String> newProps = ImmutableMap.of();

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
     * Returns the props list size.
     *
     * @return the props list
     */
    public static int getNumProps() {
        return props.size();
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
     * Returns the string for the prop with the provided key from the props list if found. Empty optional else.
     *
     * @param key the key of the prop to find within the present prop files
     * @return the prop value string from the located prop file is present. Empty optional else
     */
    @CheckReturnValue
    static Optional<String> getStringProp(String key) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(!key.isEmpty());

        if (newProps.containsKey(key)) {
            String value = newProps.get(key);
            assert value != null;
            return Optional.of(value);
        }

        return Optional.empty();
    }

    /**
     * Loads the props from all discovered prop files.
     */
    public static void loadProps() {
        Preconditions.checkArgument(!propsLoaded);

        ArrayList<File> propFiles = new ArrayList<>();

        File propsDirectory = new File(PROPS_DIR_NAME);
        File[] propFilesArray = propsDirectory.listFiles();

        // todo this shouldn't be an exception, they're optional
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
            LinkedHashMap<String, String> newPropsList = new LinkedHashMap<>();

            propFiles.forEach(propFile -> {
                ImmutableList<String> currentFileLines = ImmutableList.of();

                try {
                    String fileContents = FileUtil.readFileContents(propFile);
                    currentFileLines = ImmutableList.copyOf(fileContents.split(splitPropFileContentsAt));
                } catch (IOException e) {
                    ExceptionHandler.handle(e);
                }

                boolean logNextProp = true;
                StringBuilder previousLinesOfMultilineProp = new StringBuilder();

                for (String line : currentFileLines) {
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
                    newPropsList.put(addProp.key(), addProp.value());
                    Logger.log(LogTag.PROPS_ACTION, "[key: " + addProp.key()
                            + (logNextProp ? ", value: " + addProp.value() : "") + CyderStrings.closingBracket);

                    logNextProp = true;
                    previousLinesOfMultilineProp = new StringBuilder();
                }
            });

            props = ImmutableList.copyOf(propsList);
            newProps = ImmutableMap.copyOf(newPropsList);

            propsLoaded = true;
        } catch (Exception e) {
            ExceptionHandler.handle(e);

            props = ImmutableList.of();
            newProps = ImmutableMap.of();

            propsLoaded = false;
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
        Preconditions.checkNotNull(line);

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
        Preconditions.checkNotNull(line);

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
            if (parts[i].endsWith(escapeSequence)) {
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
}
