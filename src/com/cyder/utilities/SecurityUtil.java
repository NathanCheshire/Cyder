package com.cyder.utilities;

import com.cyder.handler.ErrorHandler;

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

    public static boolean compMACAddress(String mac) {

        //todo make this more secure
        return toHexString(getSHA(mac.toCharArray())).equals
                ("5c486915459709261d6d9af79dd1be29fea375fe59a8392f64369d2c6da0816e");
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

    public static boolean checkPassword(String name, String pass) {
        try {
            File[] users = new File("src/users").listFiles();
            LinkedList<File> userDataFiles = new LinkedList<>();

            for (File f : users) {
                if (!f.getName().contains("DeprecatedUser")) {
                    userDataFiles.add(new File(f.getAbsolutePath() + "/Userdata.txt"));
                }
            }

            for (int i = 0 ; i < userDataFiles.size() ; i++) {
                BufferedReader currentRead = new BufferedReader(new FileReader(userDataFiles.get(i)));

                String filename = null;
                String filepass = null;
                String Line = currentRead.readLine();

                while (Line != null) {
                    String[] parts = Line.split(":");

                    if (parts[0].equalsIgnoreCase("Name")) {
                        filename = parts[1];
                    } else if (parts[0].equalsIgnoreCase("Password")) {
                        filepass = parts[1];
                    }

                    Line = currentRead.readLine();
                }

                if (pass.equals(filepass) && name.equals(filename)) {
                    GeneralUtil.setUserUUID(users[i].getName());
                    GeneralUtil.setUsername(name);
                    return true;
                }
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
    }
}
