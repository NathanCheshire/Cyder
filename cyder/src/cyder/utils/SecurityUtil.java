package cyder.utils;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

import java.io.File;
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
public final class SecurityUtil {
    /**
     * Suppress default constructor.
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
    public static byte[] getSha256(char[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        throw new FatalException("Unable to compute SHA256 of input");
    }

    /**
     * Returns a byte array of the provided char array after hashing via the SHA1 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getSha1(char[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(SHA1);
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        throw new FatalException("Unable to compute SHA1 of input");
    }

    /**
     * Returns a byte array of the provided char array after hashing via the MD5 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getMd5(char[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(MD5);
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        throw new FatalException("Unable to compute MD5 of input");
    }

    /**
     * Returns a byte array of the provided byte array after hashing via the SHA256 algorithm.
     *
     * @param input the input char array
     * @return the hashed character array converted to bytes
     */
    public static byte[] getSha256(byte[] input) {
        Preconditions.checkNotNull(input);
        Preconditions.checkArgument(input.length > 0);

        try {
            MessageDigest md = MessageDigest.getInstance(SHA256);
            return md.digest(input);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        throw new FatalException("Unable to compute SHA256 of input");
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

        throw new FatalException("Unable to compute SHA256 of input");
    }

    /**
     * Returns a unique uuid that does not exist for all current Cyder users.
     * Note it's insanely unlikely that a hash would be generated for a user which
     * already exists but, nevertheless this method exists.
     *
     * @return a unique uuid that does not exist for all current Cyder users
     */
    public static String generateUuidForUser() {
        String uuid = SecurityUtil.generateUuid();
        File userFolder = Dynamic.buildDynamic(Dynamic.USERS.getDirectoryName(), uuid);

        while (userFolder.exists()) {
            uuid = SecurityUtil.generateUuid();
            userFolder = Dynamic.buildDynamic(Dynamic.USERS.getDirectoryName(), uuid);
        }

        return uuid;
    }

    /**
     * Double hashes the provided password using sha256 and returns the hex string representing the password.
     *
     * @param password the password to double hash
     * @return the double hashed password
     */
    public static String doubleHashToHex(char[] password) {
        Preconditions.checkNotNull(password);

        return SecurityUtil.toHexString(SecurityUtil.getSha256(
                SecurityUtil.toHexString(SecurityUtil.getSha256(password)).toCharArray()));
    }
}
