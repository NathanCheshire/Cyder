package cyder.genesis;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * A class for loading ini props from props.ini used throughout Cyder.
 */
public class PropLoader {
    /**
     * The props file.
     */
    private static final File propFile = new File("props.ini");

    /**
     * Suppress default constructor.
     */
    private PropLoader() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * A prop object mapping a key to a value of the props.ini file.
     */
    public static record Prop(String key, String value) {
    }

    static {
        reloadProps();
    }

    /**
     * The props array
     */
    private static ImmutableList<Prop> props;

    /**
     * Returns an immutable list of props.
     *
     * @return an immutable list of props
     */
    public static ImmutableList<Prop> getProps() {
        return props;
    }

    /**
     * Loads the props from the prop file.
     */
    public static void reloadProps() {
        Preconditions.checkArgument(propFile.exists());
        ExceptionHandler.checkFatalCondition(propFile.exists(), "Prop file DNE");

        try (BufferedReader reader = new BufferedReader(new FileReader(propFile))) {
            ArrayList<Prop> propsList = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                if (parts.length == 2) {
                    propsList.add(new Prop(parts[0], parts[1]));
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

                    propsList.add(new Prop(key.toString(), value.toString()));
                }
            }

            props = ImmutableList.copyOf(propsList);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            props = ImmutableList.of();
        }
    }

    /**
     * Returns the prop value with the provided key.
     *
     * @param key the key to get the prop value of
     * @return the prop value with the provided key
     */
    public static String get(String key) {
        for (Prop prop : props) {
            if (prop.key.equals(key)) {
                return prop.value;
            }
        }

        return null;
    }
}
