package cyder.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.*;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.user.data.MappedExecutable;

import java.io.*;
import java.lang.reflect.Type;

/**
 * A class for serializing data from a string or url source into a provided parser base class.
 */
public final class SerializationUtil {
    /**
     * A deserializer class for deserializing lists into ImmutableLists.
     */
    private static final class JsonDeserializer implements com.google.gson.JsonDeserializer<ImmutableList<?>> {
        @Override
        public ImmutableList<?> deserialize(final JsonElement json, final Type type,
                                            final JsonDeserializationContext context) throws JsonParseException {
            JsonArray array = json.getAsJsonArray();
            ImmutableList.Builder<MappedExecutable> executableBuilder = new ImmutableList.Builder<>();
            array.forEach(jsonElement -> executableBuilder.add(
                    SerializationUtil.fromJson(jsonElement.toString(), MappedExecutable.class)));
            return ImmutableList.copyOf(array);
        }
    }

    /**
     * The master Gson object used for all of Cyder.
     */
    private static final Gson gson = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .registerTypeAdapter(ImmutableList.class, new JsonDeserializer())
            .create();

    /**
     * The number of chars to log prior to a deserialization or after a serialization of an object.
     */
    private static final int charsToLog = 50;

    /**
     * Suppress default constructor.
     */
    private SerializationUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

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

        T ret = gson.fromJson(json, clazz);
        log(LogTag.OBJECT_DESERIALIZATION, clazz, json);
        return ret;
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
            T ret = gson.fromJson(reader, clazz);
            log(LogTag.OBJECT_DESERIALIZATION, clazz, gson.toJson(ret));
            return ret;
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

        T ret = gson.fromJson(reader, clazz);
        log(LogTag.OBJECT_DESERIALIZATION, clazz, gson.toJson(ret));
        return ret;
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

        T ret = gson.fromJson(json, type);
        log(LogTag.OBJECT_DESERIALIZATION, type, json);
        return ret;
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
        log(LogTag.OBJECT_SERIALIZATION, object.getClass(), gson.toJson(object));
    }

    /**
     * Serializes and returns the provided object to a string.
     *
     * @param object the object to serialize
     * @return the serialized object
     */
    public static String toJson(Object object) {
        Preconditions.checkNotNull(object);

        String ret = gson.toJson(object);
        log(LogTag.OBJECT_SERIALIZATION, object.getClass(), ret);
        return ret;
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

        boolean ret = true;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            toJson(object, writer);
        } catch (IOException e) {
            ExceptionHandler.handle(e);
            ret = false;
        }

        return ret;
    }

    /**
     * Logs a serialization or deserialization action.
     *
     * @param tag                 the primary log tag to use
     * @param classOrType         the class or type serialized/deserialized
     * @param serializationString the string which was deserialized or the result of a serialization
     * @param <T>                 the type of the class or Type
     */
    private static <T> void log(LogTag tag, T classOrType, String serializationString) {
        if (shouldIgnoreObjectSerializationOrDeserialization(classOrType)) return;

        String classOrTypeTag;
        if (classOrType instanceof Class<?> clazz) {
            classOrTypeTag = ReflectionUtil.getBottomLevelClass(clazz);
        } else if (classOrType instanceof Type type) {
            classOrTypeTag = type.getTypeName();
        } else {
            throw new FatalException("Failed to get name for: " + classOrType);
        }

        ImmutableList<String> tags = ImmutableList.of(
                tag.getLogName(), classOrTypeTag
        );

        String logStatement = serializationString;
        int length = serializationString.length();
        if (length > charsToLog) {
            String firstPart = logStatement.substring(0, charsToLog / 2);
            String secondPart = logStatement.substring(length - charsToLog / 2 - 1, length);
            logStatement = CyderStrings.quote + firstPart + CyderStrings.quote
                    + CyderStrings.dots + CyderStrings.quote + secondPart + CyderStrings.quote;
        }
        Logger.log(tags, logStatement);
    }

    /**
     * Returns whether the provided object or type should not be logged when serialized/deserialized.
     *
     * @param classOrType the class or type
     * @param <T>         the type, one of Class or type
     * @return whether the provided object or type should not be logged
     */
    private static <T> boolean shouldIgnoreObjectSerializationOrDeserialization(T classOrType) {
        ImmutableList<String> ignoreClasses = Props.ignoreSerializationData.getValue().getList();
        if (ignoreClasses.contains("all")) return true;

        if (classOrType instanceof Class<?> clazz) {
            return StringUtil.in(ReflectionUtil.getBottomLevelClass(clazz), true, ignoreClasses);
        } else if (classOrType instanceof Type type) {
            return StringUtil.in(type.getTypeName(), true, ignoreClasses);
        }

        return true;
    }
}
