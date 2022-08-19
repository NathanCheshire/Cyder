package cyder.exceptions;

/**
 * An exception for when a device such as a monitor or keyboard cannot be located.
 */
public class DeviceNotFoundException extends IllegalArgumentException {
    public DeviceNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
