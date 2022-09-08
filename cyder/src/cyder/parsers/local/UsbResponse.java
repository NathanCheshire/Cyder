package cyder.parsers.local;

import java.util.ArrayList;

// todo local backend package

/**
 * A serialize class for a local backend USB GET request.
 */
public class UsbResponse {
    /**
     * The PyUsb master string containing all devices connected via usb to this computer.
     */
    private String usb;

    /**
     * Constructs a new usb response object.
     *
     * @param usb the PyUsb master string containing all devices connected via usb to this computer
     */
    public UsbResponse(String usb) {
        this.usb = usb;
    }

    /**
     * Returns the PyUsb master string containing all devices connected via usb to this computer
     *
     * @return the PyUsb master string containing all devices connected via usb to this computer
     */
    public String getUsb() {
        return usb;
    }

    /**
     * Sets the PyUsb master string containing all devices connected via usb to this computer
     *
     * @param usb he PyUsb master string containing all devices connected via usb to this computer
     */
    public void setUsb(String usb) {
        this.usb = usb;
    }

    /**
     * Parses the master string returned by PyUsb into a list of readable USB devices.
     *
     * @return a list of readable USB devices
     */
    public ArrayList<String> parse() {
        ArrayList<String> ret = new ArrayList<>();

        // Remove leftover brackets
        String trimmedUsb = usb.replace("[", "").replace("]", "");

        for (String part : trimmedUsb.split(",")) {
            ret.add(part.replaceAll("\\s+", "")
                    .replace("=", "")
                    .replaceAll("\\r", ""));
        }

        return ret;
    }
}
