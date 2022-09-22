package cyder.genesis;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.FileUtil;

import java.io.*;
import java.util.ArrayList;

/**
 * A class for loading ini props from props.ini used throughout Cyder.
 */
public final class PropLoader {
    /**
     * Lines which start with this are marked as a comment and not parsed as props.
     */
    public static final String COMMENT_PATTERN = "#";

    /**
     * The props immutable list.
     */
    private static ImmutableList<Prop> props;

    /**
     * Whether to log the next prop that is loaded.
     * Props which should not be logged when loaded should be
     * annotated with the {@link #NO_LOG_ANNOTATION} annotation.
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

        return props.stream().anyMatch(prop -> prop.key.equals(key));
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
            if (prop.key.equals(key)) {
                return prop.value;
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
            if (prop.key.equals(key)) {
                return prop.value.equals("1") || prop.value.equalsIgnoreCase("true");
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
            if (prop.key.equals(key)) {
                return Integer.parseInt(prop.value);
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
            if (prop.key.equals(key)) {
                return Float.parseFloat(prop.value);
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
            if (prop.key.equals(key)) {
                return Double.parseDouble(prop.value);
            }
        }

        throw new IllegalArgumentException("Prop with key not found: key = \"" + key + "\"");
    }

    /**
     * Loads the props from all discovered prop files.
     */
    static void loadProps() {
        Preconditions.checkArgument(!propsLoaded);

        ArrayList<File> propFiles = new ArrayList<>();

        File propsDirectory = new File("props");
        File[] propFilesArray = propsDirectory.listFiles();

        if (propFilesArray == null || propFilesArray.length < 1) {
            throw new FatalException("Could not find any prop files");
        }

        for (File f : propFilesArray) {
            if (f.getName().startsWith("prop") && FileUtil.validateExtension(f, ".ini")) {
                propFiles.add(f);
                Logger.log(LogTag.DEBUG, "Found prop file: " + f);
            }
        }

        for (File f : propFilesArray) {
            injectNoLogAnnotations(f);
        }

        try {
            ArrayList<Prop> propsList = new ArrayList<>();

            for (File propFile : propFiles) {
                BufferedReader reader = new BufferedReader(new FileReader(propFile));
                String line;

                while ((line = reader.readLine()) != null) {
                    // comment
                    if (line.trim().startsWith(COMMENT_PATTERN)) {
                        continue;
                    }
                    // blank line
                    else if (line.trim().isEmpty()) {
                        continue;
                    }
                    // hide next prop value
                    else if (line.trim().equals(NO_LOG_ANNOTATION)) {
                        logNextProp = false;
                        continue;
                    }

                    Prop addProp = extractProp(line);

                    propsList.add(addProp);

                    Logger.log(LogTag.PROP_LOADED, "[key = " + addProp.key
                            + (logNextProp ? ", value = " + addProp.value : "") + "]");

                    logNextProp = true;
                }

                reader.close();
            }

            props = ImmutableList.copyOf(propsList);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            props = ImmutableList.of();
        } finally {
            propsLoaded = true;
        }
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
                // if it's an escaped comma, continue
                if (parts[i].endsWith("\\")) {
                    parts[i] = parts[i].substring(0, parts[i].length() - 1);
                    continue;
                }

                // should be real comma so ensure not already set
                if (lastKeyIndex != -1)
                    throw new IllegalStateException("Could not parse line: " + line);

                // set last index of key parts
                lastKeyIndex = i;
            }

            if (lastKeyIndex == -1) {
                throw new IllegalStateException("Could not parse line: " + line);
            }

            StringBuilder key = new StringBuilder();
            StringBuilder value = new StringBuilder();

            for (int i = 0 ; i <= lastKeyIndex ; i++) {
                key.append(parts[i]);

                if (i != lastKeyIndex) {
                    key.append(":");
                }
            }

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
     * The annotation to skip logging a prop value.
     */
    private static final String NO_LOG_ANNOTATION = "@no_log";

    /**
     * The pattern a key must have for a no log annotation to be injected.
     */
    private static final String KEY_PATTERN = "_key";

    /**
     * Injects {@link #NO_LOG_ANNOTATION} annotations for props found which end in {@link #KEY_PATTERN}.
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
                    if (extractedProp.key.trim().endsWith(KEY_PATTERN)
                            && !previousLine.trim().equals(NO_LOG_ANNOTATION)) {
                        writer.write(NO_LOG_ANNOTATION);
                        writer.newLine();
                        Logger.log(LogTag.DEBUG, "Injected " + NO_LOG_ANNOTATION + " for prop: "
                                + extractedProp.key + ", prop file = " + file.getName());
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
     * A prop object mapping a key to a value of the props.ini file.
     */
    public static record Prop(String key, String value) {}
}
