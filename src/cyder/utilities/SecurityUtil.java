package cyder.utilities;

import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    public static byte[] getSHA(char[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();

            for (char c : input)
                sb.append(c);

            return md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
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

    public static boolean newCheckPassword(String name, String pass) {
        try {
            //delete possible corrupted users
            IOUtil.cleanUsers();

            //get all users
            File[] UUIDs = new File("users").listFiles();
            LinkedList<File> userBins = new LinkedList<>();

            //get all valid users
            for (File user : UUIDs) {
                userBins.add(new File(user.getAbsolutePath() + "/userdata.bin"));
            }

            //loop through all users and extract the name and password fields
            for (int i = 0 ; i < userBins.size() ; i++) {
                String binUsername = IOUtil.extractUserData(userBins.get(i), "username");
                String binPassword = IOUtil.extractUserData(userBins.get(i), "password");

                //if it's the one we're looking for, set consoel UUID, free resources, and return true
                if (pass.equals(binPassword) && name.equalsIgnoreCase(binUsername)) {
                    ConsoleFrame.setUUID(UUIDs[i].getName());
                    return true;
                }
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
    }

    public static boolean checkPassword(String name, String pass) {
        try {
            //delete possible corrupted users
            IOUtil.cleanUsers();

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
                if (pass.equals(filepass) && name.equalsIgnoreCase(filename)) {
                    ConsoleFrame.setUUID(UUIDs[i].getName());
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
