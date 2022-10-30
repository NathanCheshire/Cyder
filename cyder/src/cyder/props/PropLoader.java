package cyder.props;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.file.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static cyder.props.PropConstants.*;

/**
 * A class for loading ini props from props.ini used throughout Cyder.
 */
public final class PropLoader {
    /**
     * The props immutable list.
     */
    private static ImmutableList<Prop> props;

    /**
     * Whether to log the next prop that is loaded.
     * Props which should not be logged when loaded should be
     * annotated with the {@link Annotation#NO_LOG} annotation.
     */
    private static boolean logNextProp = true;

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
        Preconditions.checkArgument(propsLoaded);
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
                Logger.log(LogTag.DEBUG, "Found prop file: " + file);
            }
        });

        Arrays.stream(propFilesArray).forEach(PropLoader::injectAnnotations);

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

                for (String line : lines) {
                    if (isComment(line)) {
                        continue;
                    } else if (StringUtil.isNullOrEmpty(line)) {
                        continue;
                    } else if (isNoLogAnnotation(line)) {
                        logNextProp = false;
                        continue;
                    }

                    Prop addProp = extractProp(line);
                    if (propsList.contains(addProp)) {
                        throw new FatalException("Duplicate prop found: " + addProp);
                    }

                    propsList.add(addProp);

                    Logger.log(LogTag.PROP_LOADED, "[key = " + addProp.key()
                            + (logNextProp ? ", value = " + addProp.value() : "") + CyderStrings.closingBracket);

                    logNextProp = true;
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

    /**
     * Injects necessary annotations into the provided prop file.
     *
     * @param file the file to inject annotations into.
     */
    private static void injectAnnotations(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        injectNoLogAnnotations(file);
    }

    /**
     * Injects {@link Annotation#NO_LOG} annotations for
     * props found which end in {@link PropConstants#KEY_PROP_SUFFIX}.
     *
     * @param file the prop file to insert annotations if needed
     */
    private static void injectNoLogAnnotations(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        ArrayList<String> originalLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                originalLines.add(line);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String previousLine = "";

            for (String line : originalLines) {
                try {
                    Prop extractedProp = extractProp(line);
                    if (extractedProp.key().trim().endsWith(KEY_PROP_SUFFIX)
                            && !previousLine.trim().equals(Annotation.NO_LOG.getAnnotation())) {
                        writer.write(Annotation.NO_LOG.getAnnotation());
                        writer.newLine();

                        logInjection(Annotation.NO_LOG.getAnnotation(), extractedProp, file);
                    }
                } catch (Exception ignored) {}

                writer.write(line);
                writer.newLine();

                previousLine = line;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Logs an injection.
     *
     * @param line the line an injection was performed above
     * @param prop the prop the injection was performed for
     * @param file the file the injection was performed on
     */
    @ForReadability
    private static void logInjection(String line, Prop prop, File file) {
        String log = "Injected " + line + " for prop: " + prop.key()
                + ", prop file = " + file.getName();
        Logger.log(LogTag.DEBUG, log);
    }
}
