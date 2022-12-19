package main.java.cyder.handlers.input;

import main.java.cyder.annotations.Handle;
import main.java.cyder.console.Console;
import main.java.cyder.enums.Dynamic;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.files.DosAttribute;
import main.java.cyder.files.FileUtil;
import main.java.cyder.logging.Logger;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.utils.OsUtil;
import main.java.cyder.utils.SpotlightUtil;

import java.io.File;
import java.util.Arrays;

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

    @Handle({"wipe logs", "open current log", "open last log", "wipe", "cmd", "dos attributes"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputIgnoringSpacesMatches("wipe logs")) {
            OsUtil.deleteFile(Dynamic.buildDynamic(Dynamic.LOGS.getFileName()));
            getInputHandler().println("Logs wiped");
        } else if (getInputHandler().inputIgnoringSpacesMatches("open current log")) {
            FileUtil.openResourceUsingNativeProgram(Logger.getCurrentLogFile().getAbsolutePath());
        } else if (getInputHandler().inputIgnoringSpacesMatches("open last log")) {
            File[] logs = Logger.getCurrentLogFile().getParentFile().listFiles();

            if (logs != null) {
                if (logs.length == 1) {
                    getInputHandler().println("No previous logs found");
                } else if (logs.length > 1) {
                    FileUtil.openResourceUsingNativeProgram(logs[logs.length - 2].getAbsolutePath());
                }
            }
        } else if (getInputHandler().inputIgnoringSpacesMatches("wipe spot lights")) {
            SpotlightUtil.wipeSpotlights();
        } else if (getInputHandler().commandIs("wipe")) {
            if (getInputHandler().checkArgsLength(1)) {
                File requestedDeleteFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
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
        } else if (getInputHandler().inputIgnoringSpacesAndCaseStartsWith("dos attributes")) {
            if (getInputHandler().checkArgsLength(2)) {
                File file = new File(getInputHandler().getArg(1));
                if (file.exists()) {
                    getInputHandler().println("DOS attributes for \"" + FileUtil.getFilename(file) + "\"");
                    getInputHandler().println("------------------------");
                    Arrays.stream(DosAttribute.values()).forEach(dosAttribute ->
                            getInputHandler().println(dosAttribute.getMethodName() + ": "
                                    + DosAttribute.getAttribute(file, dosAttribute)));
                } else {
                    getInputHandler().println("Provided file does not exist, absolute path: " + file.getAbsolutePath());
                    getInputHandler().print("Cwd: " + new File(".").getAbsolutePath());
                }
            } else {
                getInputHandler().println("DOS attributes command usage: dos attributes path/to/my/file.txt");
            }
        } else {
            ret = false;
        }

        return ret;
    }
}
