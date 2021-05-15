package cyder.utilities;

import cyder.handler.ErrorHandler;

import java.awt.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;

public class NetworkUtil {

    private NetworkUtil() {} //private constructor to avoid object creation

    public static void internetConnect(String URL) {
        Desktop Internet = Desktop.getDesktop();
        try {
            Internet.browse(new URI(URL));
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public static void internetConnect(URI URI) {
        Desktop Internet = Desktop.getDesktop();
        try {
            Internet.browse(URI);
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }
    }

    public static boolean siteReachable(String URL) {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 " + URL);
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return false;
            }
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return true;
    }

    public static int latency(int timeout) {
        Socket Sock = new Socket();
        SocketAddress Address = new InetSocketAddress("www.google.com", 80);
        long start = System.currentTimeMillis();

        try {
            Sock.connect(Address, timeout);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        long stop = System.currentTimeMillis();
        int Latency = (int) (stop - start);

        try {
            Sock.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return Latency;
    }

    public int latency() {
        Socket Sock = new Socket();
        SocketAddress Address = new InetSocketAddress("www.google.com", 80);
        int Timeout = 2000;
        long start = System.currentTimeMillis();

        try {
            Sock.connect(Address, Timeout);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        long stop = System.currentTimeMillis();
        int Latency = (int) (stop - start);

        try {
            Sock.close();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return Latency;
    }

    public static boolean internetReachable() {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 www.google.com");
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return true;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return false;
    }
}
