package cyder.network;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

/** A representation of a USB device connected to the host computer. */
@Immutable
public class UsbDevice {
    /** The status of the usb device. */
    private final String status;

    /** The type of the usb device. */
    private final String type;

    /** The name of the device. */
    private final String friendlyName;

    /** The instance id of the device. */
    private final String instanceId;

    /** Suppress default constructor. */
    private UsbDevice() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs a new usb device.
     *
     * @param status       the status of the usb device
     * @param type         the type of the usb device
     * @param friendlyName the name of the device
     * @param instanceId   the instance id of the device
     */
    public UsbDevice(String status, String type, String friendlyName, String instanceId) {
        Preconditions.checkNotNull(status);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(friendlyName);
        Preconditions.checkNotNull(instanceId);
        Preconditions.checkArgument(!status.isEmpty());
        Preconditions.checkArgument(!type.isEmpty());
        Preconditions.checkArgument(!friendlyName.isEmpty());
        Preconditions.checkArgument(!instanceId.isEmpty());

        this.status = status;
        this.type = type;
        this.friendlyName = friendlyName;
        this.instanceId = instanceId;
    }

    /**
     * Returns the status of the usb device.
     *
     * @return the status of the usb device
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the type of the usb device.
     *
     * @return the type of the usb device
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the name of the device.
     *
     * @return the name of the device
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Returns the instance id of the device.
     *
     * @return the instance id of the device
     */
    public String getInstanceId() {
        return instanceId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "UsbDevice{"
                + "status=" + CyderStrings.quote + status + CyderStrings.quote
                + ", type=" + CyderStrings.quote + type + CyderStrings.quote
                + ", friendlyName=" + CyderStrings.quote + friendlyName + CyderStrings.quote
                + ", instanceId=" + CyderStrings.quote + instanceId + CyderStrings.quote + "}";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof UsbDevice)) {
            return false;
        }

        UsbDevice other = (UsbDevice) o;
        return getStatus().equals(other.getStatus())
                && getType().equals(other.getType())
                && getFriendlyName().equals(other.getFriendlyName())
                && getInstanceId().equals(other.getInstanceId());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int ret = status.hashCode();
        ret += 31 * ret + type.hashCode();
        ret += 31 * ret + friendlyName.hashCode();
        ret += 31 * ret + instanceId.hashCode();
        return ret;
    }
}