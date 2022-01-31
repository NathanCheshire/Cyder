package test.java;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.ui.CyderOutputPane;
import cyder.ui.CyderScrollPane;
import cyder.utilities.ImageUtil;
import cyder.utilities.ScreenUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

import static java.lang.System.out;

public class Debug {
    private Debug() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    private static boolean open = false;

    private static JTextPane printArea = new JTextPane();
    private static CyderOutputPane cyderOutputPane = new CyderOutputPane(printArea);
    private static StringUtil printingUtil = cyderOutputPane.getStringUtil();

    private static CyderFrame debugFrame;

    //here incase we close the window so we can open it back up and be in the same place
    private static LinkedList<String> lines = new LinkedList<>();

    public static <T> void print(T objMaybe) {
        print(objMaybe, false, true);
    }

    public static <T> void print(T objMaybe, boolean showDebugConsole, boolean log) {
        //this should be the only System.out.print call in the whole program
        out.print(objMaybe);

        //log the debug print (this is why you should debug print from here)
        if (log) {
            Logger.log(Logger.Tag.DEBUG_PRINT, objMaybe);
        }

        //add new line to lines
        lines.add(objMaybe.toString());

        if (showDebugConsole) {
            initDebugWindow();

            printArea.setText("");

            //append everything needed to frame
            for (String str : lines) {
                printingUtil.print(str);
            }
        }
    }

    public static <T> void println(T objMaybe) {
       print(objMaybe + "\n", false, true);
    }

    public static <T> void println(T objMaybe, boolean showDebugConsole) {
        print(objMaybe + "\n", showDebugConsole, true);
    }

    public static <T> void println(T objMaybe, boolean showDebugConsole, boolean log) {
        print(objMaybe + "\n", showDebugConsole, log);
    }

    private static void bringMenuToFront() {
        if (debugFrame == null)
            throw new IllegalArgumentException("Frame is null");

        if (debugFrame.getState() == JFrame.ICONIFIED) {
            initDebugWindow();
        }
    }

    private static synchronized void initDebugWindow() {
        try {
            //just acquired sem so make sure that it isn't already open again
            if (!open) {
                if (debugFrame != null)
                    debugFrame.dispose();

                debugFrame = new CyderFrame(1050,400, ImageUtil.imageIconFromColor(new Color(21,23,24)));
                debugFrame.setTitle("Debug");
                debugFrame.setFrameType(CyderFrame.FrameType.POPUP);
                debugFrame.setBackground(new Color(21,23,24));

                printArea.setBounds(20, 40, 500 - 40, 500 - 80);
                printArea.setBackground(new Color(21,23,24));
                printArea.setBorder(null);
                printArea.setFocusable(false);
                printArea.setEditable(false);
                printArea.setFont(new Font("Agency FB",Font.BOLD, 26));
                printArea.setForeground(new Color(85,181,219));
                printArea.setCaretColor(printArea.getForeground());

                CyderScrollPane printScroll = new CyderScrollPane(printArea,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                printScroll.setThumbColor(CyderColors.regularPink);
                printScroll.setBounds(20, 40, 1050 - 40, 400 - 80);
                printScroll.getViewport().setOpaque(false);
                printScroll.setOpaque(false);
                printScroll.setBorder(null);
                printArea.setAutoscrolls(true);

                debugFrame.getContentPane().add(printScroll);

                debugFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        open = false;
                    }

                    @Override
                    public void windowClosed(WindowEvent e) {
                        open = false;
                    }
                });

                debugFrame.setVisible(true);
                debugFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());

                open = true;
            } else {
                bringMenuToFront();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Runs the tests within the method.
     * This method is used purely for testing purposes.
     */
    public static void launchTests() {
        try {
            //todo right now this should be top right of secondary monitor
            int requestedMonitor = 0;
            int requestedX = -3000;
            int requestedY = -2000;
            int requestedWidth = 743;
            int requestedHeight = 708;
            boolean pinned = true;

            loadMonitorStats(requestedMonitor, requestedX, requestedY, requestedWidth, requestedHeight);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    public static void loadMonitorStats(int requestedMonitor, int requestedConsoleX,
                                        int requestedConsoleY, int consoleFrameBackgroundWidth,
                                        int consoleFrameBackgroundHeight) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = graphicsEnvironment.getScreenDevices();

        //if the monitor is valid, use its bounds
        if (requestedMonitor > -1 && requestedMonitor < screenDevices.length) {
            Rectangle requestedScreenBounds =
                    screenDevices[requestedMonitor].getDefaultConfiguration().getBounds();

            int monitorX = requestedScreenBounds.x;
            int monitorY = requestedScreenBounds.y;
            int monitorWidth = requestedScreenBounds.width;
            int monitorHeight = requestedScreenBounds.height;


            //todo we have bounds of the monitor and it's valid, WE MUST PUT IT ON THIS MONITOR!!!!

            //if too far right, set to max x for this monitor
            if (requestedConsoleX + consoleFrameBackgroundWidth > monitorX + monitorWidth) {
                requestedConsoleX = monitorX  + monitorWidth - consoleFrameBackgroundWidth;
            }

            //if too far left, set to min x for this monitor
            else if (requestedConsoleX < monitorX) {
                requestedConsoleX = monitorX;
            }

            //if too far down, set to max y for this monitor
            if (requestedConsoleY + consoleFrameBackgroundHeight > monitorY + monitorHeight) {
                requestedConsoleY = monitorY + monitorHeight - consoleFrameBackgroundHeight;
            }

            //if too far up, set to min y
            else if (requestedConsoleY < monitorY) {
                requestedConsoleY = monitorY;
            }
        }
        //todo remove duplicate code smell here by doing logic for monitor bounds first
        //otherwise use the default monitor's bounds
        else {
            //primary monitor bounds
            int minX = 0;
            int minY = 0;
            int maxX = ScreenUtil.getScreenWidth();
            int maxY = ScreenUtil.getScreenHeight();

            //if too far left, set to min x
            if (requestedConsoleX < minX) {
                requestedConsoleX = 0;
            }

            //if too far right, set to max x minus console width
            if (requestedConsoleX + consoleFrameBackgroundWidth > maxX) {
                requestedConsoleX = maxX - consoleFrameBackgroundWidth;
            }

            //if too far up, set to min y
            if (requestedConsoleY < minY) {
                requestedConsoleY = 0;
            }

            //if too far down, set to max y minus console height
            if (requestedConsoleY + consoleFrameBackgroundHeight > maxY) {
                requestedConsoleY = maxY - consoleFrameBackgroundHeight;
            }

        }

        //set the location to the calculated location
        ConsoleFrame.getConsoleFrame().setLocation(requestedConsoleX, requestedConsoleY);
    }

    /**
     * Launches the tests without invoking any Cyder setup.
     * WARNING: this could have unintended consequences. Before
     * utilizing this entry point, ensure what you are testing does
     * not require Cyder subroutines prior to executing.
     *
     * @param args the JVM command line arguments to ignore
     */
    public static void main(String[] args) {
        launchTests();
    }
}
