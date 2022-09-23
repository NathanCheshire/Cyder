package cyder.props;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.FileUtil;
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

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
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

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
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

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
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

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
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

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
    }

    private static final String PROP_EXTENSION = ".ini";
    private static final String PROP_FILE_PREFIX = "prop";

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
                try (BufferedReader reader = new BufferedReader(new FileReader(propFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (isComment(line)) {
                            continue;
                        } else if (StringUtil.isNullOrEmpty(line)) {
                            continue;
                        } else if (isNoLogAnnotation(line)) {
                            logNextProp = false;
                            continue;
                        }

                        Prop addProp = extractProp(line);

                        propsList.add(addProp);

                        Logger.log(LogTag.PROP_LOADED, "[key = " + addProp.key()
                                + (logNextProp ? ", value = " + addProp.value() : "") + "]");

                        logNextProp = true;
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            });

            props = ImmutableList.copyOf(propsList);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            props = ImmutableList.of();
        } finally {
            propsLoaded = true;
        }
    }

    @ForReadability
    private static boolean isComment(String line) {
        return line.trim().startsWith(COMMENT_PATTERN);
    }

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

        String[] parts = line.split(":");
        Preconditions.checkArgument(parts.length > 1);

        Prop prop;

        if (parts.length == 2) {
            prop = new Prop(parts[0].trim(), parts[1].trim());
        } else {
            int lastKeyIndex = -1;

            for (int i = 0 ; i < parts.length - 1 ; i++) {
                if (escapedString(parts[i])) {
                    parts[i] = parts[i].substring(0, parts[i].length() - 1);
                    continue;
                }

                if (lastKeyIndex != -1) throw new IllegalStateException("Could not parse line: " + line);
                lastKeyIndex = i;
            }

            if (lastKeyIndex == -1) throw new IllegalStateException("Could not parse line: " + line);

            StringBuilder key = new StringBuilder();
            StringBuilder value = new StringBuilder();

            // Build key
            for (int i = 0 ; i <= lastKeyIndex ; i++) {
                key.append(parts[i]);

                if (i != lastKeyIndex) {
                    key.append(":");
                }
            }

            // Build value
            for (int i = lastKeyIndex + 1 ; i < parts.length ; i++) {
                value.append(parts[i]);

                if (i != parts.length - 1) {
                    value.append(":");
                }
            }

            prop = new Prop(key.toString().trim(), value.toString().trim());
        }

        return prop;
    }

    /**
     * The escape char for comma.
     */
    private static final String escapeSequence = "\\";

    /**
     * Returns whether the provided line ends with the escape pattern.
     *
     * @param line the line
     * @return whether the provided line ends with the escape pattern
     */
    @ForReadability
    private static boolean escapedString(String line) {
        Preconditions.checkNotNull(line);

        return line.endsWith(escapeSequence);
    }

    /**
     * Injects necessary annotations into the provided prop file.
     *
     * @param file the file to inject annotations into.
     */
    private static void injectAnnotations(File file) {
        Preconditions.checkNotNull(file);

        injectNoLogAnnotations(file);
    }

    /**
     * Injects {@link Annotation#NO_LOG} annotations for
     * props found which end in {@link PropConstants#KEY_PROP_SUFFIX}.
     *
     * @param file the prop file to insert annotations if needed
     */
    private static void injectNoLogAnnotations(File file) {
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
