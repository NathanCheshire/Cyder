package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.strings.CyderStrings;

import java.io.*;
import java.lang.reflect.Type;

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
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Deserializes the contents contained in the provided string.
     *
     * @param json  the json string to format
     * @param clazz the class to serialize the json string to
     * @param <T>   the type of the class to serialize
     * @return the serialized class
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
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
    public static <T> T fromJson(File file, Class<T> clazz) {
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
    public static <T> T fromJson(Reader reader, Class<T> clazz) {
        Preconditions.checkNotNull(reader);
        Preconditions.checkNotNull(clazz);

        return gson.fromJson(reader, clazz);
    }

    /**
     * Serializes the provided json to the provided type.
     *
     * @param json the json string to serialize
     * @param type the type to serialize to
     * @param <T>  the type of the type to serialize
     * @return the serialized json
     */
    public static <T> T fromJson(String json, Type type) {
        Preconditions.checkNotNull(json);
        Preconditions.checkArgument(!json.isEmpty());
        Preconditions.checkNotNull(type);

        return gson.fromJson(json, type);
    }

    /**
     * Serializes the provided object and writes it using the provided writer.
     *
     * @param object the object to serialize
     * @param writer the writer to write the serialized object to
     */
    public static void toJson(Object object, Appendable writer) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(writer);

        gson.toJson(object, writer);
    }

    /**
     * Serializes and returns the provided object to a string.
     *
     * @param object the object to serialize
     * @return the serialized object
     */
    public static String toJson(Object object) {
        Preconditions.checkNotNull(object);

        return gson.toJson(object);
    }

    /**
     * Serializes the provided object and writes the json string to the provided file.
     *
     * @param object the object to serialize
     * @param file   the file to write the serialized object to
     * @return whether the serialization completed successfully and the contents were written to the provided file
     */
    @CanIgnoreReturnValue
    public static boolean toJson(Object object, File file) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());

        try {
            toJson(object, new FileWriter(file));
            return true;
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        return false;
    }
}
