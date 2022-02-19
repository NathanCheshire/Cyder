package cyder.python;

import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;
import cyder.utilities.OSUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PyExecutor {
    /**
     * Executor service used for running python scripts.
     */
    private static final ExecutorService pexecutor = Executors.newSingleThreadExecutor(
            new CyderThreadFactory("Python Script Executor"));

    /**
     * Executes the USBq.py script.
     */
    public static Future<ArrayList<String>> executeUSBq() {
        return pexecutor.submit(() -> {
            ArrayList<String> ret = new ArrayList<>();

            try {
                Runtime rt = Runtime.getRuntime();
                String[] commands = {"python",
                        OSUtil.buildPath("cyder","src","cyder","python","USBq.py")};

                //noinspection CallToRuntimeExec
                Process proc = rt.exec(commands);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;

                boolean start = false;

                while ((line = stdInput.readLine()) != null) {
                    if (line.equals("Connected devices: ") )
                        start = true;

                    if (start)
                        ret.add(line);
                }
            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);
            }

            return ret;
        });
    }
}
