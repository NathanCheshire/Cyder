package cyder.testing;

import cyder.consts.CyderColors;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.utilities.ImageUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import static java.lang.System.out;

public class DebugConsole {
    private static boolean open = false;
    private static JTextPane printArea = new JTextPane();
    private static CyderScrollPane printScroll;
    private static StringUtil printingUtil = new StringUtil(printArea);
    private static CyderFrame debugFrame;

    //here incase we close the window so we can open it back up and be in the same place
    private static LinkedList<String> lines = new LinkedList<>();

    public static <T> void print(T objMaybe) {
        //this should be the only System.out.print call in the whole program
        out.print(objMaybe);

        if (!open) {
            initDebugWindow();

            printArea.setText("");

            //append everything needed to frame
            for (String str : lines) {
                printingUtil.print(str);
            }
        } else {
            bringMenuToFront();
        }

        //add new to lines and print
        lines.add(objMaybe.toString());
        printingUtil.print(objMaybe.toString());
    }

    public static <T> void println(T objMaybe) {
       print(objMaybe + "\n");
    }

    private static void bringMenuToFront() {
        if (debugFrame == null)
            throw new IllegalArgumentException("Frame is null");

        if (debugFrame.getState() == JFrame.ICONIFIED) {
            initDebugWindow();
        }
    }

    public static Semaphore debugWindowOpeningSem = new Semaphore(1);

    private static void initDebugWindow() {
        try {
            debugWindowOpeningSem.acquire();

            //just acquired sem so make sure that it isn't already open again
            if (!open) {
                if (debugFrame != null)
                    debugFrame.dispose();

                debugFrame = new CyderFrame(1050,400, ImageUtil.imageIconFromColor(new Color(21,23,24)));
                debugFrame.setTitle("Debug");
                debugFrame.setBackground(new Color(21,23,24));

                printArea.setBounds(20, 40, 500 - 40, 500 - 80);
                printArea.setBackground(new Color(21,23,24));
                printArea.setBorder(null);
                printArea.setFocusable(false);
                printArea.setEditable(false);
                printArea.setFont(new Font("Agency FB",Font.BOLD, 26));
                printArea.setForeground(new Color(85,181,219));
                printArea.setCaretColor(printArea.getForeground());

                printScroll = new CyderScrollPane(printArea,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                printScroll.setThumbColor(CyderColors.intellijPink);
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
                debugFrame.setAlwaysOnTop(true);
                debugFrame.setLocation(0, SystemUtil.getScreenHeight() - debugFrame.getHeight());

                open = true;
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        } finally {
            debugWindowOpeningSem.release();
        }
    }

    public static void launchTests() {
        //todo frames should be simply printed icons, of cyder logo but first letter of frame there
        // generate the icon image for this as needed and choose from different colors (from intellij logo red, blue, orange)
        // and print to scrolling pane, limit defaults and print those first, they don't go away.
        // when you spawn a new frame, link it to the menu pane so that a post close action is to remove itself from the pane

        //todo there should be a cool menu/window for debug stats pane

        //todo change data that can be a boolean/other types in sys.json and userdata.json to their respective types

        //todo closing perlin is laggy if we animated at any point, perhaps threads aren't properly exited?
        // make each frame keep a list of threads and use a kill condition so before animation we can kill all threads accodiated with the frame

        //todo reorganize triggers for input handler and think about a better way to do it

        //todo don't allow a user to be logged in multiple times, technically it's possible

        //MessagingWidget.showGUI();
    }
}
