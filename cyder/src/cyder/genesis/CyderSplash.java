package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.objects.InformBuilder;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.ImageUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The splash screen for Cyder when it is originally first launched.
 */
public class CyderSplash {
    /**
     * Whether the splash screen has been shown.
     */
    private static boolean splashShown;

    /**
     * Whether the splash has been disposed this instance.
     */
    private static boolean disposed;

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
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * Shows the splash screen as long as it has not already been shown.
     */
    public static void showSplash() {
        if (splashShown)
            throw new IllegalStateException("Program has already been loaded");

        splashShown = true;

        CyderThreadRunner.submit(() -> {
            try {
                splashFrame = new CyderFrame(600);
                splashFrame.setTitle("Cyder Splash");

                // set AlwaysOnTop, this will be quickly turned off
                splashFrame.setAlwaysOnTop(true);

                CyderThreadRunner.submit(() -> {
                    try {
                        JLabel cBlock = new JLabel(generateCIcon());
                        cBlock.setBounds(20,600 / 2 - 150 / 2, 150, 150);
                        splashFrame.getContentPane().add(cBlock);

                        JLabel yBlock = new JLabel(generateYIcon());
                        yBlock.setBounds(600 - 150 - 20,600 / 2 - 150 / 2, 150, 150);
                        splashFrame.getContentPane().add(yBlock);

                        while (cBlock.getX() < 600 / 2 - cBlock.getWidth() / 2) {
                            cBlock.setLocation(cBlock.getX() + 5, cBlock.getY());
                            yBlock.setLocation(yBlock.getX() - 5, yBlock.getY());
                            Thread.sleep(6);
                        }

                        int longSide = 200;
                        int sub = 40;
                        long delay = 3;

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
                        cyderLabel.setBounds(0, -StringUtil.getMinHeight("Cyder",cyderFont),
                                600, StringUtil.getMinHeight("Cyder",cyderFont));
                        splashFrame.getContentPane().add(cyderLabel);

                        while (cyderLabel.getY() < 600 / 2 - 150 / 2 - cyderLabel.getHeight() - 30) {
                            cyderLabel.setLocation(cyderLabel.getX(), cyderLabel.getY() + 5);
                            Thread.sleep(5);
                        }

                        Font nathanFont = new Font("Darling In Paris", Font.BOLD, 40);

                        CyderLabel creatorLabel = new CyderLabel("By Nathan Cheshire");
                        creatorLabel.setFont(nathanFont);
                        creatorLabel.setForeground(CyderColors.vanila);
                        creatorLabel.setBounds(0, 600, 600,
                                StringUtil.getMinHeight("By Nathan Cheshire",nathanFont) + 10);
                        splashFrame.getContentPane().add(creatorLabel);

                        while (creatorLabel.getY() > 600 / 2 + 150 / 2 + creatorLabel.getHeight() + 10) {
                            creatorLabel.setLocation(creatorLabel.getX(), creatorLabel.getY() - 5);
                            Thread.sleep(5);
                        }

                        // animation finished so remove on top mode
                        splashFrame.setAlwaysOnTop(false);

                        Thread.sleep(800);

                        String message = CyderSplash.loadingMessage;
                        int dotTimeout = 400;
                        Font newFont = new Font("Agency FB", Font.BOLD, 50);

                        loadingLabel = new CyderLabel("Test text");
                        loadingLabel.setFont(newFont);
                        loadingLabel.setForeground(CyderColors.vanila);
                        loadingLabel.setSize(600, StringUtil.getMinHeight(CyderStrings.europeanToymaker, newFont));
                        loadingLabel.setLocation(0,510);
                        splashFrame.getContentPane().add(loadingLabel);

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

                            // if disposed, exit thread
                            if (splashFrame.isDisposed()) {
                                loadingLabel.setText("Subroutines Complete");
                                return;
                            }
                        }

                        // if frame is still active and it should have been dispoed
                        if (splashFrame != null && CyderShare.isDisposeFlash()) {
                            splashFrame.dispose(true);

                            // this has been going on for over a minute at this point if the program reaches here
                            // clearly something is wrong so exit
                            InformBuilder builder = new InformBuilder("idk what happened but you screwed something up");
                            builder.setTitle("Startup Exception");
                            builder.setPostCloseAction(() -> CyderShare.exit(ExitCondition.FatalTimeout));
                            InformHandler.inform(builder);
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                },"Splash Animation");

                splashFrame.finalizeAndShow();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        },"Splash Loader");
    }

    /**
     * Disposes the splashFrame using fast close.
     */
    public static void fastDispose() {
        if (disposed)
            return;
        if (!CyderShare.isDisposeFlash())
            return;

        splashFrame.dispose(true);
        disposed = true;
    }

    /**
     * Returns the splash frame.
     *
     * @return the splash frame
     */
    public static CyderFrame getSplashFrame() {
        return splashFrame;
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

        if (!loadingMessage.trim().isEmpty())
            CyderSplash.loadingMessage = loadingMessage.trim();

        if (loadingLabel != null) {
            loadingLabel.setText(loadingMessage);
            loadingLabel.revalidate();
            loadingLabel.repaint();
        }
    }

    /**
     * Generates and returns the C symbol for the splash animation.
     *
     * @return the C symbol for the splash
     */
    private static ImageIcon generateCIcon() {
        BufferedImage drawMe = new BufferedImage(150,150, BufferedImage.TYPE_INT_ARGB);
        Graphics g = drawMe.getGraphics();
        g.setColor(CyderColors.vanila);
        g.fillRect(0,0, 95, 25);
        g.fillRect(0,125, 95, 25);
        g.fillRect(0,0, 30, 150);

        return ImageUtil.toImageIcon(drawMe);
    }

    /**
     * Generates and returns the Y symbol for the splash animation.
     *
     * @return the Y symbol for the splash animation
     */
    private static ImageIcon generateYIcon() {
        BufferedImage drawMe = new BufferedImage(150,150, BufferedImage.TYPE_INT_ARGB);
        Graphics g = drawMe.getGraphics();
        g.setColor(CyderColors.vanila);
        g.fillRect(120,0, 30, 150);
        g.fillRect(60,60, 95, 30);

        return ImageUtil.toImageIcon(drawMe);
    }
}
