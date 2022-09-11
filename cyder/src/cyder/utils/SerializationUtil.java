package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

/**
 * A class for serializing data from a string or url source into a provided parser base class.
 */
public final class SerializationUtil {
    /**
     * Suppress default constructor.
     */
    private SerializationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The master Gson object used for all of Cyder.
     */
    private static final Gson gson = new Gson();

    /**
     * Serializes the contents contained in the provided string.
     *
     * @param json  the json string to format
     * @param clazz the class to serialize the json string to
     * @param <T>   the type of the class to serialize
     * @return the serialized class
     */
    public static <T> T serialize(String json, Class<T> clazz) {
        Preconditions.checkNotNull(json);
        Preconditions.checkArgument(!json.isEmpty());
        Preconditions.checkNotNull(clazz);

        return gson.fromJson(json, clazz);
    }

    /**
     * Serializes the contents contained in the provided file.
     *
     * @param file  the file containing the json to serialize
     * @param clazz the class to serialize the json string to
     * @param <T>   the type of the class to serialize
     * @return the serialized class
     */
    public static <T> T serialize(File file, Class<T> clazz) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkNotNull(clazz);

        try (Reader reader = new BufferedReader(new FileReader(file))) {
            return gson.fromJson(reader, clazz);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new FatalException("Could not serialize contents of file: " + file.getAbsolutePath());
    }

    /**
     * Serializes the contents contained in the provided file.
     *
     * @param reader the reader to read from to obtain the text to serialize
     * @param clazz  the class to serialize the json string to
     * @param <T>    the type of the class to serialize
     * @return the serialized class
     */
    public static <T> T serialize(Reader reader, Class<T> clazz) {
        Preconditions.checkNotNull(reader);
        Preconditions.checkNotNull(clazz);

        return gson.fromJson(reader, clazz);
    }
}
