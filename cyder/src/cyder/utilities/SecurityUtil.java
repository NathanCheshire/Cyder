package cyder.utilities;

import cyder.constants.CyderStrings;
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
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    // todo remove, use !released or something
    public static boolean nathanLenovo() {
        try {
            return true;
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return false;
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
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        //clear possible sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    public static byte[] getSHA256(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(toBytes(input));
        }

        catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return null;
    }

    public static byte[] getSHA1(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(toBytes(input));
        }

        catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return null;
    }

    public static byte[] getMD5(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(toBytes(input));
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return null;
    }

    public static byte[] getSHA256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }

        return null;
    }

    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public static String getDeprecatedUUID() {
        return "VoidUser-" + generateUUID().substring(0,8);
    }

    public static String generateUUID() {
        try {
            MessageDigest salt = MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            return UUID.nameUUIDFromBytes(salt.digest()).toString();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return null;
    }
}
