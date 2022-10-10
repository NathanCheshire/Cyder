package cyder.network;

import com.google.common.collect.ImmutableList;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
     * A space character.
     */
    private static final String space = " ";

    /**
     * The number of lines from the usb query output to ignore.
     */
    private static final int headerLines = 2;

    /**
     * The number of members contained in a {@link UsbDevice}.
     */
    private static final int usbDeviceMemberLength = 4;

    /**
     * Returns a list of usb devices connected to this computer.
     *
     * @return a list of usb devices connected to this computer
     */
    public static Future<ImmutableList<UsbDevice>> getUsbDevices() {
        ArrayList<UsbDevice> ret = new ArrayList<>();

        ArrayList<String> standardOutput = new ArrayList<>();
        ArrayList<String> errorOutput = new ArrayList<>();

        String command = POWER_SHELL + space + usbConnectedDevicesCommand;

        return Executors.newSingleThreadExecutor(
                new CyderThreadFactory("Audio file preview generator")).submit(() -> {
            try {
                Process powerShellProcess = Runtime.getRuntime().exec(command);
                powerShellProcess.getOutputStream().close();

                String outputLine;
                BufferedReader outReader =
                        new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
                while ((outputLine = outReader.readLine()) != null) standardOutput.add(outputLine);
                outReader.close();

                String errorLine;
                BufferedReader errorReader =
                        new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream()));
                while ((errorLine = errorReader.readLine()) != null) errorOutput.add(errorLine);
                errorReader.close();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            if (!errorOutput.isEmpty()) {
                throw new FatalException("Exception whilst trying to query USB devices");
            }

            if (standardOutput.size() > headerLines) {
                standardOutput.stream().filter(line -> !line.isEmpty()).skip(headerLines).forEach(line -> {
                    String[] parts = line.split(CyderRegexPatterns.multipleSpacesRegex);
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
