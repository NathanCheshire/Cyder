package cyder.utilities;

import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

public class AnimationUtil {

    //private constructor to avoid object creation
    private AnimationUtil() {}

    /**
     * Moves the specified frame object up until it is no longer visible then invokes dispose
     * @param frame - the frame object to close
     */
    public static void closeAnimation(Frame frame) {
        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).getTopDragLabel().disableDragging();
        }

        try {
            if (frame != null && frame.isVisible()) {
                Point point = frame.getLocationOnScreen();
                int x = (int) point.getX();
                int y = (int) point.getY();

                for (int i = y; i >= 0 - frame.getHeight(); i -= 15) {
                    Thread.sleep(1);
                    frame.setLocation(x, i);
                }

                frame.dispose();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).getTopDragLabel().enableDragging();
        }
    }

    /**
     * Moves the specified frame object down until it is no longer visible then sets the frame's state
     * to Frame.ICONIFIED
     * @param frame - the frame object to minimize and iconify
     */
    public static void minimizeAnimation(JFrame frame) {
        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).getTopDragLabel().disableDragging();
        }

        Point point = frame.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        try {
            for (int i = y; i <= SystemUtil.getScreenHeight(); i += 15) {
                Thread.sleep(1);
                frame.setLocation(x, i);
            }

            frame.setState(JFrame.ICONIFIED);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).getTopDragLabel().enableDragging();
        }
    }

    /**
     * Moves the specified frame object from the top down until it is in the center of the screen
     * @param frame - the frame object to display
     */
    public static void enterAnimation(JFrame frame) {
        frame.setVisible(false);
        frame.setLocationRelativeTo(null);

        int to = frame.getY();
        frame.setLocation(frame.getX(), 0 - frame.getHeight());

        frame.setVisible(true);

        for (int i = 0 - frame.getHeight(); i < to; i += 15) {
            frame.setLocation(frame.getX(), i);
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }

        frame.setLocationRelativeTo(null);
    }

    /**
     * Master method to animate any component up in a separate thread
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param comp - the component to move
     */
    public static void componentUp(final int start, final int stop, final int delay, final int increment, final Component comp) {
        if (comp.getY() == start)
            (new Thread(() -> {
                for (int i = start; i >= stop; i -= increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(comp.getX(), i);
                    } catch (InterruptedException e) {
                        ErrorHandler.handle(e);
                    }
                }
                comp.setLocation(comp.getX(), stop);
            },"component up thread")).start();
    }

    /**
     * Master method to animate any component down in a separate thread
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param comp - the component to move
     */
    public static void componentDown(final int start, final int stop, final int delay, final int increment, final Component comp) {
        if (comp.getY() == start)
            (new Thread(() -> {
                for (int i = start; i <= stop; i += increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(comp.getX(), i);
                    } catch (InterruptedException e) {
                        ErrorHandler.handle(e);
                    }
                }
                comp.setLocation(comp.getX(), stop);
            },"component down thread")).start();
    }

    /**
     * Master method to animate any component left in a separate thread
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param comp - the component to move
     */
    public static void componentLeft(final int start, final int stop, final int delay, final int increment, final Component comp) {
        if (comp.getX() == start)
            new Thread(() -> {
                for (int i = start; i >= stop; i -= increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(i, comp.getY());
                    } catch (InterruptedException e) {
                        ErrorHandler.handle(e);
                    }
                }
                comp.setLocation(stop, comp.getY());
            },"component left thread").start();
    }

    /**
     * Master method to animate any component right in a separate thread
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param comp - the component to move
     */
    public static void componentRight(final int start, final int stop, final int delay, final int increment, final Component comp) {
        if (comp.getX() == start)
            (new Thread(() -> {
                for (int i = start; i <= stop; i += increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(i, comp.getY());
                    } catch (InterruptedException e) {
                        ErrorHandler.handle(e);
                    }
                }
                comp.setLocation(stop, comp.getY());
            },"component right thread")).start();
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jLabel - the jlabel to animate
     * @deprecated use {@link AnimationUtil#componentUp(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jLabelYUp(final int start, final int stop, final int delay, final int increment, final JLabel jLabel) {
        componentUp(start, stop, delay, increment, jLabel);
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jLabel - the jlabel to animate
     * @deprecated use {@link AnimationUtil#componentDown(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jLabelYDown(final int start, final int stop, final int delay, final int increment, final JLabel jLabel) {
       componentDown(start, stop, delay, increment, jLabel);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jLabel - the jlabel to animate
     * @deprecated use {@link AnimationUtil#componentLeft(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jLabelXLeft(final int start, final int stop, final int delay, final int increment, final JLabel jLabel) {
        componentLeft(start,stop,delay,increment, jLabel);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jLabel - the jlabel to animate
     * @deprecated use {@link AnimationUtil#componentRight(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jLabelXRight(final int start, final int stop, final int delay, final int increment, final JLabel jLabel) {
        componentRight(start,stop,delay,increment,jLabel);
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jTextField - the jtextfield to animate
     * @deprecated use {@link AnimationUtil#componentUp(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jTextFieldYUp(final int start, final int stop, final int delay, final int increment, final JTextField jTextField) {
        componentUp(start,stop,delay,increment,jTextField);
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jTextField - the jtextfield to animate
     * @deprecated use {@link AnimationUtil#componentDown(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jTextFieldYDown(final int start, final int stop, final int delay, final int increment, final JTextField jTextField) {
        componentDown(start,stop,delay,increment,jTextField);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jTextField - the jtextfield to animate
     * @deprecated use {@link AnimationUtil#componentLeft(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jTextFieldXLeft(final int start, final int stop, final int delay, final int increment, final JTextField jTextField) {
        componentLeft(start,stop,delay,increment,jTextField);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jTextField - the jtextfield to animate
     * @deprecated use {@link AnimationUtil#componentRight(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jTextFieldXRight(final int start, final int stop, final int delay, final int increment, final JTextField jTextField) {
        componentRight(start,stop,delay,increment,jTextField);
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jButton - the button to animate
     * @deprecated use {@link AnimationUtil#componentUp(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jButtonYUp(final int start, final int stop, final int delay, final int increment, final JButton jButton) {
        componentUp(start,stop,delay,increment,jButton);
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jButton - the button to animate
     * @deprecated use {@link AnimationUtil#componentDown(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jButtonYDown(final int start, final int stop, final int delay, final int increment, final JButton jButton) {
        componentDown(start,stop,delay,increment,jButton);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jButton - the button to animate
     * @deprecated use {@link AnimationUtil#componentLeft(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jButtonXLeft(final int start, final int stop, final int delay, final int increment, final JButton jButton) {
        componentLeft(start,stop,delay,increment,jButton);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jButton - the button to animate
     * @deprecated use {@link AnimationUtil#componentRight(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jButtonXRight(final int start, final int stop, final int delay, final int increment, final JButton jButton) {
        componentRight(start,stop,delay,increment,jButton);
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jPasswordField - the passwordfield to animate
     * @deprecated use {@link AnimationUtil#componentUp(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jPasswordFieldYUp(final int start, final int stop, final int delay, final int increment, final JPasswordField jPasswordField) {
        componentUp(start,stop,delay,increment,jPasswordField);
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jPasswordField - the passwordfield to animate
     * @deprecated use {@link AnimationUtil#componentDown(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jPasswordFieldYDown(final int start, final int stop, final int delay, final int increment, final JPasswordField jPasswordField) {
       componentDown(start,stop,delay,increment,jPasswordField);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jPasswordField - the passwordfield to animate
     * @deprecated use {@link AnimationUtil#componentLeft(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jPasswordFieldXLeft(final int start, final int stop, final int delay, final int increment, final JPasswordField jPasswordField) {
        componentLeft(start,stop,delay,increment, jPasswordField);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jPasswordField - the passwordfield to animate
     * @deprecated use {@link AnimationUtil#componentRight(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jPasswordFieldXRight(final int start, final int stop, final int delay, final int increment, final JPasswordField jPasswordField) {
        componentRight(start,stop,delay,increment,jPasswordField);
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jScrollPane - the jScrollPane to animate
     * @deprecated use {@link AnimationUtil#componentUp(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jTextAreaYUp(final int start, final int stop, final int delay, final int increment, final JScrollPane jScrollPane) {
        componentUp(start,stop,delay,increment,jScrollPane);
    }

    /**
     *
     * @param start - the starting y value
     * @param stop - the ending y value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jScrollPane - the jScrollPane to animate
     * @deprecated use {@link AnimationUtil#componentDown(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jTextAreaYDown(final int start, final int stop, final int delay, final int increment, final JScrollPane jScrollPane) {
        componentDown(start,stop,delay,increment, jScrollPane);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jScrollPane - the jScrollPane to animate
     * @deprecated use {@link AnimationUtil#componentLeft(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jTextAreaXLeft(final int start, final int stop, final int delay, final int increment, final JScrollPane jScrollPane) {
        componentLeft(start,stop,delay,increment,jScrollPane);
    }

    /**
     *
     * @param start - the starting x value
     * @param stop - the ending x value
     * @param delay - the ms delay in between increments
     * @param increment - the increment value
     * @param jScrollPane - the jScrollPane to animate
     * @deprecated use {@link AnimationUtil#componentRight(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jTextAreaXRight(final int start, final int stop, final int delay, final int increment, final JScrollPane jScrollPane) {
        componentRight(start,stop,delay,increment,jScrollPane);
    }

    /**
     * This method takes in a width and outputs the increment and delay to obtain a near 1s animation time
     * Used purely for console background switching animations.
     * @param width - the width of which want to find an increment for
     * @return an array representing the delay in ms followed by the width increment
     */
    public static int[] getDelayIncrement(int width) {
        int[] ret = new int[2];
        //assign ret[0] to delay
        //assign ret[1] to inc

        try {
            //width below 400 now allowed
            if (width < 600)
                throw new FatalException("Background dimensions below 600x600 " +
                        "are not allowed and shouln't be possible; allow me to fix that");
            ConsoleFrame.resizeBackgrounds();

            double animationLen = 500; //ideally we want the animation to last 1000ms
            double increment = animationLen / width; //delay must be an int so if this is less than 1, problem

            if (width < 500) {
                //increment will be 1 and delay will be at least 1 if not greater
                ret[1] = 1;
                ret[0] = (int) Math.round(animationLen / width);

            } else if (width > 1000) {
                //delay is 1ms, minimum in ms delay
                ret[0] = 1;
                //increment will be 1 from 1001 to 1499
                //increment will be 2 from 1500 to 2000
                //increment will be 3 from 2001 to 2499
                ret[1] = (int) Math.round(width / animationLen);
            } else {
                //width is 1000 and our delay is 1000 which resulted in 1 here. Thus we return the ideal array
                ret[0] = (ret[1] = 1);
            }

        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return new int[]{1, 10};
    }
}
