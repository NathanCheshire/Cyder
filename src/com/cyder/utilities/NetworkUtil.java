package com.cyder.utilities;

import java.awt.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;

public class NetworkUtil {

    private GeneralUtil gu;

    public NetworkUtil() {
        gu = new GeneralUtil();
    }

    public void internetConnect(String URL) {
        Desktop Internet = Desktop.getDesktop();
        try {
            Internet.browse(new URI(URL));
        } catch (Exception ex) {
            gu.handle(ex);
        }
    }

    public void internetConnect(URI URI) {
        Desktop Internet = Desktop.getDesktop();
        try {
            Internet.browse(URI);
        } catch (Exception ex) {
            gu.handle(ex);
        }
    }

    public boolean compMACAddress(String mac) {
        //todo make this more secure
        return gu.toHexString(gu.getSHA(mac.toCharArray())).equals("5c486915459709261d6d9af79dd1be29fea375fe59a8392f64369d2c6da0816e");
    }

    public boolean siteReachable(String URL) {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 " + URL);
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return false;
            }
        }

        catch (Exception e) {
            gu.handle(e);
        }

        return true;
    }

    public int latency(int timeout) {
        Socket Sock = new Socket();
        SocketAddress Address = new InetSocketAddress("www.google.com", 80);
        long start = System.currentTimeMillis();

        try {
            Sock.connect(Address, timeout);
        } catch (Exception e) {
            gu.handle(e);
        }

        long stop = System.currentTimeMillis();
        int Latency = (int) (stop - start);

        try {
            Sock.close();
        } catch (Exception e) {
            gu.handle(e);
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
            gu.handle(e);
        }

        long stop = System.currentTimeMillis();
        int Latency = (int) (stop - start);

        try {
            Sock.close();
        } catch (Exception e) {
            gu.handle(e);
        }

        return Latency;
    }

    public boolean internetReachable() {
        Process Ping;

        try {
            Ping = java.lang.Runtime.getRuntime().exec("ping -n 1 www.google.com");
            int ReturnValue = Ping.waitFor();
            if (ReturnValue == 0) {
                return true;
            }
        } catch (Exception e) {
            gu.handle(e);
        }

        return false;
    }
}
