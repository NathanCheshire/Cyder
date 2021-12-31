package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderStrings;
import cyder.handlers.internal.ErrorHandler;
import cyder.handlers.internal.PopupHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;

import javax.swing.*;
import java.awt.*;

public class CyderSplash {
    private static boolean splashShown = false;
    private static CyderFrame splashFrame;

    private CyderSplash() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

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

                        Font nathanFont = new Font("Agency FB", Font.BOLD, 50);
                        CyderLabel loadingLabel = new CyderLabel("By Nathan Cheshire");
                        loadingLabel.setFont(nathanFont);
                        loadingLabel.setForeground(CyderColors.vanila);
                        loadingLabel.setBounds(0, 600, 600, CyderFrame.getMinHeight("By Nathan Cheshire",nathanFont));
                        splashFrame.getContentPane().add(loadingLabel);

                        while (loadingLabel.getY() > 600 / 2 + 150 / 2 + loadingLabel.getHeight() + 30) {
                            loadingLabel.setLocation(loadingLabel.getX(), loadingLabel.getY() - 5);
                            Thread.sleep(5);
                        }

                        Thread.sleep(500);

                        String message = CyderSplash.loadingMessage;
                        int dotTimeout = 400;

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

                            if (splashFrame.isDispoed())
                                return;
                        }

                        if (splashFrame != null) {
                            splashFrame.dispose(true);

                            //this has been going on for over a minute at this point if the program reaches here
                            // clearly something is wrong so exit
                            PopupHandler.inform("idk what happened but you screwed something up", "Startup Exception",
                                    null, null, () -> GenesisShare.exit(-100));
                        }
                    } catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                },"Splash Animation").start();

                splashFrame.setVisible(true);
                splashFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Splash Loader").start();
    }

    public static CyderFrame getSplashFrame() {
        return splashFrame;
    }

    private static String loadingMessage = "Loading Components";

    public static void setLoadingMessage(String loadingMessage) {
        if (loadingMessage.trim().length() > 0)
            CyderSplash.loadingMessage = loadingMessage.trim();
    }
}
