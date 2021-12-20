package cyder.testing;

import cyder.consts.CyderColors;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
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
        try {
            CyderFrame splashFrame = CyderFrame.getBorderlessFrame(600,600);
            splashFrame.setTitle("Cyder Splash");

            new Thread(() -> {
                try {
                    JLabel cBlock = new JLabel(new ImageIcon("static/pictures/C.png"));
                    cBlock.setBounds(20,600 / 2 - 150 / 2, 150, 150);
                    splashFrame.getContentPane().add(cBlock);

                    JLabel yBlock = new JLabel(new ImageIcon("static/pictures/Y.png"));
                    yBlock.setBounds(600 - 150 - 20,600 / 2 - 150 / 2, 150, 150);
                    splashFrame.getContentPane().add(yBlock);

                    while (cBlock.getX() < 600 / 2 - cBlock.getWidth() / 2) {
                        cBlock.setLocation(cBlock.getX() + 5, cBlock.getY());
                        yBlock.setLocation(yBlock.getX() - 5, yBlock.getY());
                        Thread.sleep(15);
                    }

                    int longSide = 200;
                    int sub = 40;
                    long delay = 5;

                    JLabel topBorder = new JLabel() {
                      @Override
                      public void paintComponent(Graphics g) {
                          g.setColor(CyderColors.regularBlue);
                          g.fillRect(0,0,longSide,10);
                      }
                    };
                    topBorder.setBounds(600 / 2 - longSide / 2,- 10, longSide,10);
                    splashFrame.getContentPane().add(topBorder);

                    while (topBorder.getY() < 600 / 2 - (longSide - sub) / 2 - 20) {
                        topBorder.setLocation(topBorder.getX(), topBorder.getY() + 5);
                        Thread.sleep(delay);
                    }

                    JLabel rightBorder = new JLabel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            g.setColor(CyderColors.regularBlue);
                            g.fillRect(0,0,10,longSide);
                        }
                    };
                    rightBorder.setBounds(600 ,splashFrame.getHeight() / 2 - longSide / 2, 10,longSide);
                    splashFrame.getContentPane().add(rightBorder);

                    while (rightBorder.getX() > 600 / 2 + (longSide - sub) / 2 + 10) {
                        rightBorder.setLocation(rightBorder.getX() - 5, rightBorder.getY());
                        Thread.sleep(delay);
                    }

                    JLabel bottomBorder = new JLabel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            g.setColor(CyderColors.regularBlue);
                            g.fillRect(0,0,longSide,10);
                        }
                    };
                    bottomBorder.setBounds(600 / 2 - longSide / 2,splashFrame.getHeight() + 10, longSide,10);
                    splashFrame.getContentPane().add(bottomBorder);

                    while (bottomBorder.getY() > 600 / 2 + (longSide - sub) / 2 + 10) {
                        bottomBorder.setLocation(bottomBorder.getX(), bottomBorder.getY() - 5);
                        Thread.sleep(delay);
                    }

                    JLabel leftBorder = new JLabel() {
                        @Override
                        public void paintComponent(Graphics g) {
                            g.setColor(CyderColors.regularBlue);
                            g.fillRect(0,0,10,longSide);
                        }
                    };
                    leftBorder.setBounds(-10 ,splashFrame.getHeight() / 2 - longSide / 2, 10,longSide);
                    splashFrame.getContentPane().add(leftBorder);

                    while (leftBorder.getX() < 600 / 2 - (longSide - sub) / 2 - 20) {
                        leftBorder.setLocation(leftBorder.getX() + 5, leftBorder.getY());
                        Thread.sleep(delay);
                    }

                    // then animate in Cyder on top
                    // and by nathan cheshire on the bottom
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }).start();

            splashFrame.setVisible(true);
            splashFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
