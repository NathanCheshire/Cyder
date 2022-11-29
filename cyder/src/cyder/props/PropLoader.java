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
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;

import static cyder.props.PropConstants.*;

/**
 * A class for loading props from prop files from the props directory for usage throughout Cyder.
 */
public final class PropLoader {
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
        newProps = ImmutableMap.of();
        loadProps();
    }

    /**
     * Returns the props list size.
     *
     * @return the props list
     */
    public static int getNumProps() {
        return newProps.size();
    }

    /**
     * Returns the string for the prop with the provided key from the props list if found. Empty optional else.
     *
     * @param key the key of the prop to find within the present prop files
     * @return the prop value string from the located prop file is present. Empty optional else
     */
    @CheckReturnValue
    static Optional<String> getStringPropFromFile(String key) {
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
    static void loadProps() {
        Preconditions.checkArgument(!propsLoaded);

        ArrayList<File> propFiles = new ArrayList<>();

        File propsDirectory = new File(propDirectoryName);
        File[] propFilesArray = propsDirectory.listFiles();

        if (propFilesArray == null || propFilesArray.length == 0) {
            newProps = ImmutableMap.of();
            propsLoaded = false;
            return;
        }

        Arrays.stream(propFilesArray).forEach(file -> {
            if (file.getName().startsWith(propFilePrefix) && FileUtil.validateExtension(file, propExtension)) {
                propFiles.add(file);
                Logger.log(LogTag.PROPS_ACTION, "Found prop file: " + file);
            }
        });

        try {
            ArrayList<Pair<String, String>> propsList = new ArrayList<>();
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

                    Pair<String, String> addProp = extractProp(fullLine);
                    if (propsList.contains(addProp)) {
                        throw new FatalException("Duplicate prop found: " + addProp);
                    }

                    propsList.add(addProp);
                    newPropsList.put(addProp.getKey(), addProp.getValue());
                    Logger.log(LogTag.PROPS_ACTION, "[key: " + addProp.getKey()
                            + (logNextProp ? ", value: " + addProp.getValue() : "") + CyderStrings.closingBracket);

                    logNextProp = true;
                    previousLinesOfMultilineProp = new StringBuilder();
                }
            });

            newProps = ImmutableMap.copyOf(newPropsList);
            propsLoaded = true;
        } catch (Exception e) {
            ExceptionHandler.handle(e);

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

        return line.trim().startsWith(commentPrefix);
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
    private static Pair<String, String> extractProp(String line) {
        Preconditions.checkNotNull(line);
        Preconditions.checkArgument(!line.isEmpty());

        String[] parts = line.split(keyValueSeparator);
        Preconditions.checkArgument(parts.length > 1);

        if (parts.length == 2) {
            String key = parts[0].trim();
            String value = parts[1].trim();
            return Pair.of(key, value);
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
                keyBuilder.append(keyValueSeparator);
            }
        }

        // Build value
        StringBuilder valueBuilder = new StringBuilder();
        for (int i = lastKeyIndex + 1 ; i < parts.length ; i++) {
            valueBuilder.append(parts[i]);

            if (i != parts.length - 1) {
                valueBuilder.append(keyValueSeparator);
            }
        }

        String key = keyBuilder.toString().trim();
        String value = valueBuilder.toString().trim();
        return Pair.of(key, value);
    }
}
