package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.UUID;

/**
 * Static utility class containing methods related to security.
 */
public class SecurityUtil {
    /**
     * Prevent illegal class instantiation.
     */
    private SecurityUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Converts the given char array to a byte array without using string object.
     * This way any possible security issues that arise from the nature of String pool are avoided.
     * Remember to use Arrays.fill(bytes, (byte) 0) for bytes or Arrays.fill(chars, '\u0000') for chars
     * when finished with the byte/char array.
     *
     * @param chars the char array to be converted to byte array
     * @return the byte array representing the given char array
     */
    private static byte[] toBytes(char[] chars) {
        Preconditions.checkNotNull(chars);
        Preconditions.checkArgument(chars.length > 0);

        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());

        // Clear possible sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    /**
     * The string used for a SHA256 digest.
     */
    private static final String SHA256 = "SHA-256";

    /**
     * The string used for a SHA1 digest.
     */
    private static final String SHA1 = "SHA-1";

    /**
     * The string used for an MD5 digest.
     */
    private static final String MD5 = "MD5";

    /**
     * Returns a byte array of the provided char array after hashing via the SHA256 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getSHA256(char[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return null;
    }

    /**
     * Returns a byte array of the provided char array after hashing via the SHA1 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getSHA1(char[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(SHA1);
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return null;
    }

    /**
     * Returns a byte array of the provided char array after hashing via the MD5 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getMD5(char[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(MD5);
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return null;
    }

    /**
     * Returns a byte array of the provided byte array after hashing via the SHA256 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getSHA256(byte[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            return md.digest(input);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return null;
    }

    /**
     * Returns a string representing the byte array.
     *
     * @param hash the array of bytes
     * @return a string representing the byte array
     */
    public static String toHexString(byte[] hash) {
        Preconditions.checkNotNull(hash);
        Preconditions.checkArgument(hash.length > 0);

        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    /**
     * Returns a unique uuid using the sha256 algorithm and the
     * standard {@link UUID#nameUUIDFromBytes(byte[])} method.
     *
     * @return a unique uuid
     */
    public static String generateUuid() {
        try {
            MessageDigest salt = MessageDigest.getInstance(SHA256);
            salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            return UUID.nameUUIDFromBytes(salt.digest()).toString();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return null;
    }
}
