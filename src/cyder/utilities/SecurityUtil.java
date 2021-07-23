package cyder.utilities;

import cyder.handler.ErrorHandler;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.UUID;

public class SecurityUtil {
    private SecurityUtil() {} //private constructor to avoid object creation

    public static String getMACAddress() {
        byte[] MAC = null;

        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface NI = NetworkInterface.getByInetAddress(address);
            MAC = NI.getHardwareAddress();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < MAC.length; i++) {
            sb.append(String.format("%02X%s", MAC[i], (i < MAC.length - 1) ? "-" : ""));
        }

        return sb.toString();
    }

    public static boolean nathanLenovo() {
        return compMACAddress(getMACAddress());
    }

    public static boolean compMACAddress(String mac) {
        return toHexString(getSHA256(mac.toCharArray())).equals(IOUtil.getSystemData("MMAC"));
    }

    /**
     * Converts the given char array to a byte array without using string object.
     * This way any possible security issues that arise from the nature of String pool are avoided.
     * Remember to use Arrays.fill(bytes, (byte) 0) for bytes or Arrays.fill(chars, '\u0000') for chars
     * when finished with the byte/char array.
     *
     * @param chars - the char array to be converted to byte array
     * @return - the byte array representing the given char array
     */
    public static byte[] toBytes(char[] chars) {
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
            ErrorHandler.handle(ex);
        }

        return null;
    }

    public static byte[] getSHA1(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(toBytes(input));
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }

        return null;
    }

    public static byte[] getMD5(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(toBytes(input));
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }

        return null;
    }

    public static byte[] getSHA256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
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
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    public static void clearCharArray(char[] arr) {
        for (char c : arr) {
            c = '\0';
        }
    }
}
