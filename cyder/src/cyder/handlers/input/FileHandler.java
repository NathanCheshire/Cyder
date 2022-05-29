package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.enums.DynamicDirectory;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.Logger;
import cyder.utilities.IOUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.SpotlightUtil;

import java.io.File;

/**
 * A handler related to files and manipulation of them.
 */
public class FileHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private FileHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle({"wipelogs", "opencurrentlog", "openlastlog", "wipe", ""})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("wipelogs")) {
            OSUtil.deleteFile(OSUtil.buildFile(
                    DynamicDirectory.DYNAMIC_PATH, DynamicDirectory.LOGS.getDirectoryName()));
            getInputHandler().println("Logs wiped");
        } else if (getInputHandler().commandIs("opencurrentlog")) {
            IOUtil.openFileOutsideProgram(Logger.getCurrentLog().getAbsolutePath());
        } else if (getInputHandler().commandIs("openlastlog")) {
            File[] logs = Logger.getCurrentLog().getParentFile().listFiles();

            if (logs != null) {
                if (logs.length == 1) {
                    getInputHandler().println("No previous logs found");
                } else if (logs.length > 1) {
                    IOUtil.openFileOutsideProgram(logs[logs.length - 2].getAbsolutePath());
                }
            }
        } else if (getInputHandler().commandAndArgsToString().equalsIgnoreCase("wipe spotlights")) {
            SpotlightUtil.wipeSpotlights();
        } else if (getInputHandler().commandIs("wipe")) {
            if (getInputHandler().checkArgsLength(1)) {
                File requestedDeleteFile = new File(OSUtil.buildPath(
                        DynamicDirectory.DYNAMIC_PATH, "users",
                        ConsoleFrame.INSTANCE.getUUID(), getInputHandler().getArg(0)));
                if (requestedDeleteFile.exists()) {
                    if (requestedDeleteFile.isDirectory()) {
                        if (OSUtil.deleteFile(requestedDeleteFile)) {
                            getInputHandler().println("Successfully deleted: "
                                    + requestedDeleteFile.getAbsolutePath());
                        } else {
                            getInputHandler().println("Could not delete folder at this time");
                        }
                    } else if (requestedDeleteFile.isFile()) {
                        if (OSUtil.deleteFile(requestedDeleteFile)) {
                            getInputHandler().println("Successfully deleted "
                                    + requestedDeleteFile.getAbsolutePath());
                        } else {
                            getInputHandler().println("Unable to delete file at this time");
                        }
                    } else {
                        throw new IllegalStateException(
                                "File is not a file nor directory. " + CyderStrings.europeanToymaker);
                    }
                } else {
                    getInputHandler().println("Requested file does not exist: "
                            + requestedDeleteFile.getAbsolutePath());
                }
            } else {
                getInputHandler().print("Wipe command usage: wipe [directory/file within your user directory]");
            }
        } else {
            ret = false;
        }

        return ret;
    }
}
