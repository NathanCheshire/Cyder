package cyder.py;

import cyder.handlers.internal.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.utilities.SystemUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PyExecutor {
    public static void executeUSBq() {
        executePy("src/cyder/py/USBQ.py");
    }

    public static void executePy(String pythonScriptPath) {
        new Thread(() -> {
            try {
                if (SystemUtil.getOS().toLowerCase().contains("windows")) {
                    String deviceString = null;

                    try {
                        //create and execute python script
                        Process p = Runtime.getRuntime().exec("python " + pythonScriptPath);

                        //get contents of output
                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                        //print outputs to the console frame
                        while ((deviceString = inputReader.readLine()) != null) {
                            ConsoleFrame.getConsoleFrame().getInputHandler().println(deviceString);
                        }
                    }
                    catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Python script executor. Script: " + pythonScriptPath).start();
    }
}
