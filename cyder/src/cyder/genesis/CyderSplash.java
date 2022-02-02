package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.PopupHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;

import javax.swing.*;
import java.awt.*;

/**
 * The splash screen for Cyder when it is originally first launched.
 */
public class CyderSplash {
    /**
     * Whether or not the splash screen has been shown.
     */
    private static boolean splashShown = false;

    /**
     * Whether the splash has been disposed this instance.
     */
    private static boolean disposed = false;

    /**
     * The splash screen CyderFrame.
     */
    private static CyderFrame splashFrame;

    /**
     * The label used to display what Cyder is currently doing in the startup routine.
     */
    private static CyderLabel loadingLabel;

    /**
     * Instantiation of CyderSplash is not allowed
     */
    private CyderSplash() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Shows the splash screen as long as it has not already been shown.
     */
    public static void showSplash() {
        if (splashShown)
            throw new IllegalStateException("Program has already been loaded");

        splashShown = true;

        new Thread(() -> {
            try {
                splashFrame = CyderFrame.getBorderlessFrame(600,600);
                splashFrame.setTitle("Cyder Splash");
                splashFrame.setAlwaysOnTop(true);

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
                            Thread.sleep(8);
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
                            Thread.sleep(5);
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

                        Font cyderFont = new Font("Agency FB", Font.BOLD, 80);
                        CyderLabel cyderLabel = new CyderLabel("Cyder");
                        cyderLabel.setFont(cyderFont);
                        cyderLabel.setForeground(CyderColors.vanila);
                        cyderLabel.setBounds(0, -CyderFrame.getMinHeight("Cyder",cyderFont),
                                600,CyderFrame.getMinHeight("Cyder",cyderFont));
                        splashFrame.getContentPane().add(cyderLabel);

                        while (cyderLabel.getY() < 600 / 2 - 150 / 2 - cyderLabel.getHeight() - 30) {
                            cyderLabel.setLocation(cyderLabel.getX(), cyderLabel.getY() + 5);
                            Thread.sleep(5);
                        }

                        Font nathanFont = new Font("Darling In Paris", Font.BOLD, 40);

                        loadingLabel = new CyderLabel("By Nathan Cheshire");
                        loadingLabel.setFont(nathanFont);
                        loadingLabel.setForeground(CyderColors.vanila);
                        loadingLabel.setBounds(0, 600, 600,
                                CyderFrame.getMinHeight("By Nathan Cheshire",nathanFont));
                        splashFrame.getContentPane().add(loadingLabel);

                        while (loadingLabel.getY() > 600 / 2 + 150 / 2 + loadingLabel.getHeight() + 30) {
                            loadingLabel.setLocation(loadingLabel.getX(), loadingLabel.getY() - 5);
                            Thread.sleep(5);
                        }

                        Thread.sleep(500);

                        String message = CyderSplash.loadingMessage;
                        int dotTimeout = 400;
                        Font newFont = new Font("Agency FB", Font.BOLD, 50);

                        loadingLabel.setFont(newFont);
                        loadingLabel.setBounds(0, 600, 600,
                                CyderFrame.getMinHeight("By Nathan Cheshire",nathanFont));

                        for (int i = 0 ; i < 30 ; i++) {
                            loadingLabel.setText(message);
                            loadingLabel.repaint();
                            Thread.sleep(dotTimeout);
                            loadingLabel.setText(message + ".");
                            loadingLabel.repaint();
                            Thread.sleep(dotTimeout);
                            loadingLabel.setText(message + "..");
                            loadingLabel.repaint();
                            Thread.sleep(dotTimeout);
                            loadingLabel.setText(message + "...");
                            Thread.sleep(dotTimeout);
                            loadingLabel.repaint();

                            if (splashFrame.isDisposed())
                                return;
                        }

                        if (splashFrame != null) {
                            splashFrame.dispose(true);

                            //this has been going on for over a minute at this point if the program reaches here
                            // clearly something is wrong so exit
                            PopupHandler.inform("idk what happened but you screwed something up",
                                    "Startup Exception", null,
                                    null, () -> CyderCommon.exit(-100));
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                },"Splash Animation").start();

                splashFrame.setVisible(true);
                splashFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },"Splash Loader").start();
    }

    /**
     * Disposes the splashFrame using fast close.
     */
    public static void fastDispose() {
        if (disposed)
            return;
        splashFrame.dispose(true);
        disposed = true;
    }

    /**
     * The loading message to display on the loading label.
     */
    private static String loadingMessage = "Loading Components";

    /**
     * Sets the loading label and updates the splash frame.
     *
     * @param loadingMessage the message to set to the loading label
     */
    public static void setLoadingMessage(String loadingMessage) {
        if (splashFrame == null || splashFrame.isDisposed())
            return;

        if (loadingMessage.trim().length() > 0)
            CyderSplash.loadingMessage = loadingMessage.trim();

        if (loadingLabel != null) {
            loadingLabel.revalidate();
            loadingLabel.repaint();
        }
    }
}
