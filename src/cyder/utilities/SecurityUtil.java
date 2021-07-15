package cyder.utilities;

import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedList;
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
        return toHexString(getSHA(mac.toCharArray())).equals(IOUtil.getSystemData("MMAC"));
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

    public static byte[] getSHA(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(toBytes(input));
        }

        catch (Exception ex) {
            ErrorHandler.handle(ex);
        }

        return null;
    }

    public static byte[] getSHA(byte[] input) {
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

    //todo binary reading should return bytes for password and not convert to string
    public static boolean newCheckPassword(String name, String pass) {
        return false;
    }

    public static boolean checkPassword(String name, String hashedPass) {
        try {
            IOUtil.cleanUsers();

            hashedPass = SecurityUtil.toHexString(SecurityUtil.getSHA(hashedPass.toCharArray()));

            //get all users
            File[] UUIDs = new File("users").listFiles();
            LinkedList<File> userDataFiles = new LinkedList<>();

            //get all valid users
            for (File user : UUIDs) {
                userDataFiles.add(new File(user.getAbsolutePath() + "/Userdata.txt")); //change to bin
            }

            //loop through all users and extract the name and password fields
            for (int i = 0 ; i < userDataFiles.size() ; i++) {
                //init objects
                BufferedReader currentRead = new BufferedReader(new FileReader(userDataFiles.get(i)));
                String filename = null;
                String filepass = null;
                String Line = currentRead.readLine();

                //loop through current file and find name and pass
                while (Line != null) {
                    String[] parts = Line.split(":");

                    if (parts[0].equalsIgnoreCase("Name")) {
                        filename = parts[1];
                    } else if (parts[0].equalsIgnoreCase("Password")) {
                        filepass = parts[1];
                    }

                    Line = currentRead.readLine();
                }

                //if it's the one we're looking for, set consoel UUID, free resources, and return true
                if (hashedPass.equalsIgnoreCase(filepass) && name.equalsIgnoreCase(filename)) {
                    ConsoleFrame.getConsoleFrame().setUUID(UUIDs[i].getName());
                    currentRead.close();
                    return true;
                }

                currentRead.close();
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
    }

    public static void clearCharArray(char[] arr) {
        for (char c : arr) {
            c = '\0';
        }
    }
}
