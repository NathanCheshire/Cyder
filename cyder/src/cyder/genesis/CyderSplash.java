package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.objects.HarmonicRectangle;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.objects.InformBuilder;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.ImageUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

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
     * The width and height for the borderless splash frame.
     */
    private static final int FRAME_LEN = 600;

    /**
     * The length of the blue borders that animate in to surround the Cyder logo.
     */
    private static final int LOGO_BORDER_LEN = 200;

    /**
     * The padding from the borders to the Cyder logo.
     */
    private static final int LOGO_BORDER_PADDING = 40;

    /**
     * The timeout between loading label updates.
     */
    private static final int loadingLabelUpdateTimeout = 25;

    /**
     * The maximum seconds of the splash should be visible for.
     */
    private static final int loadingLabelSeconds = 30;

    /**
     * The nuber of times to update the loading label.
     */
    private static final int loadingLabelUpdateIterations = (loadingLabelSeconds * 1000) / loadingLabelUpdateTimeout;

    /**
     * The timeout beefore starting to display loading messages after finishing the splash animation.
     */
    private static final int loadingMessageStartTimeout = 800;

    /**
     * The font used for the loading label messages.
     */
    private static final Font loadingLabelFont = new Font("Agency FB", Font.BOLD, 40);

    /**
     * The font used for the author signature.
     */
    private static final Font developerSignatureFont = new Font("Condiment", Font.BOLD, 50);

    /**
     * Instantiation of CyderSplash is not allowed
     */
    private CyderSplash() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The padding between the top/bottom of the frame and the harmonic rectangles.
     */
    private static final int harmonicYPadding = 10;

    /**
     * The padding between the left/right of the frame and the harmonic rectangles.
     */
    private static int harmonicXPadding = 20;

    /**
     * The padding between harmonic rectangles themselves.
     */
    private static final int harmonicXInnerPadding = 10;

    /**
     * The number of harmonic rectangles to draw
     */
    private static final int numHarmonicRectangles = 20;

    /**
     * The list of harmonic rectangles.
     */
    private static final LinkedList<HarmonicRectangle> harmonicRectangles = new LinkedList<>();

    /**
     * Shows the splash screen as long as it has not already been shown.
     */
    public static void showSplash() {
        if (splashShown)
            throw new IllegalStateException("Program has already been loaded");

        splashShown = true;

        CyderThreadRunner.submit(() -> {
            try {
                splashFrame = CyderFrame.generateBorderlessFrame(FRAME_LEN, FRAME_LEN, CyderColors.navy);
                splashFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        for (HarmonicRectangle rectangle : harmonicRectangles) {
                            rectangle.stopAnimation();
                        }
                    }

                    @Override
                    public void windowClosed(WindowEvent e) {
                        for (HarmonicRectangle rectangle : harmonicRectangles) {
                            rectangle.stopAnimation();
                        }
                    }
                });
                splashFrame.setTitle("Cyder Splash");

                // set AlwaysOnTop, this will be quickly turned off
                splashFrame.setAlwaysOnTop(true);

                CyderThreadRunner.submit(() -> {
                    try {
                        JLabel cBlock = new JLabel(generateCIcon());
                        cBlock.setBounds(20, FRAME_LEN / 2 - ICON_LEN / 2, ICON_LEN, ICON_LEN);
                        splashFrame.getContentPane().add(cBlock);

                        JLabel yBlock = new JLabel(generateYIcon());
                        yBlock.setBounds(FRAME_LEN - ICON_LEN - 20,
                                FRAME_LEN / 2 - ICON_LEN / 2, ICON_LEN, ICON_LEN);
                        splashFrame.getContentPane().add(yBlock);

                        while (cBlock.getX() < FRAME_LEN / 2 - cBlock.getWidth() / 2) {
                            cBlock.setLocation(cBlock.getX() + 5, cBlock.getY());
                            yBlock.setLocation(yBlock.getX() - 5, yBlock.getY());
                            Thread.sleep(6);
                        }

                        JLabel topBorder = new JLabel() {
                            @Override
                            public void paintComponent(Graphics g) {
                                g.setColor(CyderColors.regularBlue);
                                g.fillRect(0, 0, LOGO_BORDER_LEN, 10);
                            }
                        };
                        topBorder.setBounds(FRAME_LEN / 2 - LOGO_BORDER_LEN / 2, -10, LOGO_BORDER_LEN, 10);
                        splashFrame.getContentPane().add(topBorder);

                        while (topBorder.getY() < FRAME_LEN / 2 - (LOGO_BORDER_LEN - LOGO_BORDER_PADDING) / 2 - 20) {
                            topBorder.setLocation(topBorder.getX(), topBorder.getY() + 5);
                            Thread.sleep(5);
                        }

                        JLabel rightBorder = new JLabel() {
                            @Override
                            public void paintComponent(Graphics g) {
                                g.setColor(CyderColors.regularBlue);
                                g.fillRect(0, 0, 10, LOGO_BORDER_LEN);
                            }
                        };
                        rightBorder.setBounds(FRAME_LEN, splashFrame.getHeight() / 2 - LOGO_BORDER_LEN / 2,
                                10, LOGO_BORDER_LEN);
                        splashFrame.getContentPane().add(rightBorder);

                        while (rightBorder.getX() > FRAME_LEN / 2 + (LOGO_BORDER_LEN - LOGO_BORDER_PADDING) / 2 + 10) {
                            rightBorder.setLocation(rightBorder.getX() - 5, rightBorder.getY());
                            Thread.sleep(3);
                        }

                        JLabel bottomBorder = new JLabel() {
                            @Override
                            public void paintComponent(Graphics g) {
                                g.setColor(CyderColors.regularBlue);
                                g.fillRect(0, 0, LOGO_BORDER_LEN, 10);
                            }
                        };
                        bottomBorder.setBounds(FRAME_LEN / 2 - LOGO_BORDER_LEN / 2,
                                splashFrame.getHeight() + 10, LOGO_BORDER_LEN, 10);
                        splashFrame.getContentPane().add(bottomBorder);

                        while (bottomBorder.getY() > FRAME_LEN / 2 + (LOGO_BORDER_LEN - LOGO_BORDER_PADDING) / 2 + 10) {
                            bottomBorder.setLocation(bottomBorder.getX(), bottomBorder.getY() - 5);
                            Thread.sleep(3);
                        }

                        JLabel leftBorder = new JLabel() {
                            @Override
                            public void paintComponent(Graphics g) {
                                g.setColor(CyderColors.regularBlue);
                                g.fillRect(0, 0, 10, LOGO_BORDER_LEN);
                            }
                        };
                        leftBorder.setBounds(-10, splashFrame.getHeight() / 2
                                - LOGO_BORDER_LEN / 2, 10, LOGO_BORDER_LEN);
                        splashFrame.getContentPane().add(leftBorder);

                        while (leftBorder.getX() < FRAME_LEN / 2 - (LOGO_BORDER_LEN - LOGO_BORDER_PADDING) / 2 - 20) {
                            leftBorder.setLocation(leftBorder.getX() + 5, leftBorder.getY());
                            Thread.sleep(3);
                        }

                        CyderLabel creatorLabel = new CyderLabel("By Nathan Cheshire");
                        creatorLabel.setFont(developerSignatureFont);
                        creatorLabel.setForeground(CyderColors.vanila);
                        creatorLabel.setBounds(0, FRAME_LEN, FRAME_LEN,
                                StringUtil.getMinHeight(creatorLabel.getText(), developerSignatureFont) + 10);
                        splashFrame.getContentPane().add(creatorLabel);

                        while (creatorLabel.getY() > FRAME_LEN / 2 + ICON_LEN / 2 + 40) {
                            creatorLabel.setLocation(creatorLabel.getX(), creatorLabel.getY() - 5);
                            Thread.sleep(5);
                        }

                        Thread.sleep(loadingMessageStartTimeout);

                        loadingLabel = new CyderLabel(CyderSplash.loadingMessage);
                        loadingLabel.setFocusable(false);
                        loadingLabel.setFont(loadingLabelFont);
                        loadingLabel.setForeground(CyderColors.vanila);
                        loadingLabel.setSize(FRAME_LEN,
                                StringUtil.getMinHeight(CyderSplash.loadingMessage, loadingLabelFont));
                        loadingLabel.setLocation(0, FRAME_LEN - 100);

                        splashFrame.getContentPane().add(loadingLabel);

                        CyderThreadRunner.submit(() -> {
                            try {
                                for (int i = 0 ; i < loadingLabelUpdateIterations ; i++) {
                                    loadingLabel.setText(CyderSplash.loadingMessage);
                                    loadingLabel.repaint();

                                    Thread.sleep(loadingLabelUpdateTimeout);

                                    // if disposed, exit thread
                                    if (splashFrame.isDisposed()) {
                                        loadingLabel.setText("Subroutines Complete");
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                ExceptionHandler.handle(e);
                            }
                        }, "Splash Loading Label Updator");

                        int rectLen = (FRAME_LEN - 2 * harmonicXPadding - (numHarmonicRectangles - 1)
                                * harmonicXInnerPadding) / numHarmonicRectangles;

                        // re-evalidate xPadding to ensure in center
                        harmonicXPadding = (FRAME_LEN - rectLen * numHarmonicRectangles - harmonicXInnerPadding
                                * (numHarmonicRectangles - 1)) / 2;


                        for (int i = 0 ; i < numHarmonicRectangles ; i++) {
                            int x = harmonicXPadding + i * rectLen + i * harmonicXInnerPadding;
                            HarmonicRectangle harmonicRectangle = new HarmonicRectangle(
                                    rectLen, 40, rectLen, 60);
                            harmonicRectangle.setHarmonicDirection(HarmonicRectangle.HarmonicDirection.VERTICAL);
                            harmonicRectangle.setAnimationInc(2);
                            harmonicRectangle.setAnimationDelay(25);
                            harmonicRectangle.setLocation(x, harmonicYPadding);
                            splashFrame.getContentPane().add(harmonicRectangle);
                            harmonicRectangles.add(harmonicRectangle);
                        }

                        for (HarmonicRectangle rectangle : harmonicRectangles) {
                            rectangle.startAnimation();
                            Thread.sleep(100);
                        }

                        // wait for disposal or show error messsage
                        Thread.sleep(loadingLabelSeconds * 1000);

                        // to be safe always set message back to whatever it was
                        loadingLabel.setText(CyderSplash.loadingMessage);
                        loadingLabel.repaint();

                        // if frame is still active and it should have been dispoed
                        if (!disposed && CyderToggles.DISPOSE_SPLASH) {
                            splashFrame.dispose(true);

                            // this has been going on for over a minute at this point if the program reaches here
                            // clearly something is wrong so exit
                            InformBuilder builder = new InformBuilder(
                                    "Splash failed to be disposed; Console failed to load");
                            builder.setTitle("Startup Exception");
                            builder.setPostCloseAction(() -> OSUtil.exit(ExitCondition.FatalTimeout));
                            InformHandler.inform(builder);
                        }
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }, "Splash Animation");

                splashFrame.finalizeAndShow();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Splash Loader");
    }

    /**
     * Disposes the splashFrame using fast close.
     */
    public static void fastDispose() {
        if (disposed)
            return;
        if (!CyderToggles.DISPOSE_SPLASH)
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
     * The length of the C and Y icons.
     */
    private static final int ICON_LEN = 150;

    /**
     * The length of the primary letter axis.
     */
    private static final int LETTER_LEN = 30;

    /**
     * Generates and returns the C symbol for the splash animation.
     *
     * @return the C symbol for the splash
     */
    private static ImageIcon generateCIcon() {
        BufferedImage drawMe = new BufferedImage(ICON_LEN, ICON_LEN, BufferedImage.TYPE_INT_ARGB);
        Graphics g = drawMe.getGraphics();
        g.setColor(CyderColors.vanila);
        g.fillRect(0, 0, 95, 25);
        g.fillRect(0, 125, 95, 25);
        g.fillRect(0, 0, LETTER_LEN, ICON_LEN);

        return ImageUtil.toImageIcon(drawMe);
    }

    /**
     * Generates and returns the Y symbol for the splash animation.
     *
     * @return the Y symbol for the splash animation
     */
    private static ImageIcon generateYIcon() {
        BufferedImage drawMe = new BufferedImage(ICON_LEN, ICON_LEN, BufferedImage.TYPE_INT_ARGB);
        Graphics g = drawMe.getGraphics();
        g.setColor(CyderColors.vanila);
        g.fillRect(ICON_LEN - LETTER_LEN, 0, LETTER_LEN, ICON_LEN);
        g.fillRect(60, 60, 95, LETTER_LEN);

        return ImageUtil.toImageIcon(drawMe);
    }
}
