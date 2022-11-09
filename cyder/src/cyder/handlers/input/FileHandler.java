package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.Logger;
import cyder.utils.IoUtil;
import cyder.utils.OsUtil;
import cyder.utils.SpotlightUtil;

import java.io.File;

/**
 * A handler related to files and manipulation of them.
 */
public class FileHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private FileHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"wipe logs", "open current log", "open last log", "wipe"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputIgnoringSpacesMatches("wipe logs")) {
            OsUtil.deleteFile(Dynamic.buildDynamic(Dynamic.LOGS.getDirectoryName()));
            getInputHandler().println("Logs wiped");
        } else if (getInputHandler().inputIgnoringSpacesMatches("open current log")) {
            IoUtil.openFileOutsideProgram(Logger.getCurrentLog().getAbsolutePath());
        } else if (getInputHandler().inputIgnoringSpacesMatches("open last log")) {
            File[] logs = Logger.getCurrentLog().getParentFile().listFiles();

            if (logs != null) {
                if (logs.length == 1) {
                    getInputHandler().println("No previous logs found");
                } else if (logs.length > 1) {
                    IoUtil.openFileOutsideProgram(logs[logs.length - 2].getAbsolutePath());
                }
            }
        } else if (getInputHandler().inputIgnoringSpacesMatches("wipe spot lights")) {
            SpotlightUtil.wipeSpotlights();
        } else if (getInputHandler().commandIs("wipe")) {
            if (getInputHandler().checkArgsLength(1)) {
                File requestedDeleteFile = Dynamic.buildDynamic(Dynamic.USERS.getDirectoryName(),
                        Console.INSTANCE.getUuid(), getInputHandler().getArg(0));
                if (requestedDeleteFile.exists()) {
                    if (requestedDeleteFile.isDirectory()) {
                        if (OsUtil.deleteFile(requestedDeleteFile)) {
                            getInputHandler().println("Successfully deleted: "
                                    + requestedDeleteFile.getAbsolutePath());
                        } else {
                            getInputHandler().println("Could not delete folder at this time");
                        }
                    } else if (requestedDeleteFile.isFile()) {
                        if (OsUtil.deleteFile(requestedDeleteFile)) {
                            getInputHandler().println("Successfully deleted "
                                    + requestedDeleteFile.getAbsolutePath());
                        } else {
                            getInputHandler().println("Unable to delete file at this time");
                        }
                    } else {
                        throw new IllegalStateException(
                                "File is not a file nor directory. " + CyderStrings.EUROPEAN_TOY_MAKER);
                    }
                } else {
                    getInputHandler().println("Requested file does not exist: "
                            + requestedDeleteFile.getAbsolutePath());
                }
            } else {
                getInputHandler().print("Wipe command usage: wipe [directory/file within your user directory]");
            }
        } else if (getInputHandler().commandIs("cmd")) {
            OsUtil.openShell();
        } else {
            ret = false;
        }

        return ret;
    }
}
