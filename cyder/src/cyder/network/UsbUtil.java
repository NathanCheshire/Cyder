package cyder.network;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderRegexPatterns;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.process.ProcessResult;
import cyder.process.ProcessUtil;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadFactory;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utility methods related to usb devices.
 */
public final class UsbUtil {
    /**
     * Suppress default constructor.
     */
    private UsbUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The power shell executable name.
     */
    private static final String POWER_SHELL = "powershell.exe";

    /**
     * The command to list USB devices connected to the host computer.
     */
    private static final String usbConnectedDevicesCommand
            = "Get-PnpDevice -PresentOnly | Where-Object { $_.InstanceId -match '^USB' }";

    /**
     * The number of lines from the usb query output to ignore.
     */
    private static final int headerLines = 2;

    /**
     * The number of members contained in a {@link UsbDevice}.
     */
    private static final int usbDeviceMemberLength = 4;

    /**
     * The name for the executor service returned by {@link #getUsbDevices()}.
     */
    private static final String USB_DEVICE_THREAD_NAME = "USB Device Getter";

    /**
     * Returns a list of usb devices connected to this computer.
     *
     * @return a list of usb devices connected to this computer
     */
    public static Future<ImmutableList<UsbDevice>> getUsbDevices() {
        ArrayList<UsbDevice> ret = new ArrayList<>();

        String command = POWER_SHELL + CyderStrings.space + usbConnectedDevicesCommand;

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory(USB_DEVICE_THREAD_NAME)).submit(() -> {
            Future<ProcessResult> futureResult = ProcessUtil.getProcessOutput(command);
            while (!futureResult.isDone()) {
                Thread.onSpinWait();
            }

            ProcessResult result = futureResult.get();

            if (!result.getErrorOutput().isEmpty()) {
                throw new FatalException("Exception whilst trying to query USB devices");
            }

            ImmutableList<String> standardOutput = result.getStandardOutput();
            if (standardOutput.size() > headerLines) {
                standardOutput.stream().filter(line -> !line.isEmpty()).skip(headerLines).forEach(line -> {
                    String[] parts = line.split(CyderRegexPatterns.multipleWhiteSpaceRegex);
                    if (parts.length == usbDeviceMemberLength) {
                        int index = 0;
                        String status = parts[index++];
                        String clazz = parts[index++];
                        String friendlyName = parts[index++];
                        String instanceId = parts[index];
                        ret.add(new UsbDevice(status, clazz, friendlyName, instanceId));
                    }
                });
            }

            return ImmutableList.copyOf(ret);
        });
    }
}
