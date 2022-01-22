package cyder.utilities;

import cyder.consts.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

public class AnimationUtil {
    //private constructor to avoid object creation
    private AnimationUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    /**
     * Moves the specified frame object up until it is no longer visible then invokes dispose
     * @param frame the frame object to close
     */
    public static void closeAnimation(Frame frame) {
        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).getTopDragLabel().disableDragging();
            ((CyderFrame) frame).getBottomDragLabel().disableDragging();
            ((CyderFrame) frame).getRightDragLabel().disableDragging();
            ((CyderFrame) frame).getLeftDragLabel().disableDragging();
        }

        try {
            if (frame != null && frame.isVisible()) {
                Point point = frame.getLocationOnScreen();
                int x = (int) point.getX();
                int y = (int) point.getY();

                for (int i = y; i >= -frame.getHeight(); i -= 15) {
                    Thread.sleep(0, 500);
                    frame.setLocation(x, i);
                }

                frame.dispose();
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Moves the specified frame object down until it is no longer visible then sets the frame's state
     * to Frame.ICONIFIED. This method works for anything that inherits from JFrame
     * @param frame the frame object to minimize and iconify
     */
    public static void minimizeAnimation(JFrame frame) {
        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).getTopDragLabel().disableDragging();
            ((CyderFrame) frame).getBottomDragLabel().disableDragging();
            ((CyderFrame) frame).getLeftDragLabel().disableDragging();
            ((CyderFrame) frame).getRightDragLabel().disableDragging();
        }

        Point point = frame.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        try {
            for (int i = y; i <= SystemUtil.getScreenHeight(); i += 15) {
                Thread.sleep(0, 250);
                frame.setLocation(x, i);
            }

            frame.setState(JFrame.ICONIFIED);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (frame instanceof CyderFrame) {
            ((CyderFrame) frame).getTopDragLabel().enableDragging();
            ((CyderFrame) frame).getBottomDragLabel().enableDragging();
            ((CyderFrame) frame).getLeftDragLabel().enableDragging();
            ((CyderFrame) frame).getRightDragLabel().enableDragging();
        }
    }

    /**
     * Master method to animate any component up in a separate thread
     * @param start the starting y value
     * @param stop the ending y value
     * @param delay the ms delay in between increments
     * @param increment the increment value
     * @param comp the component to move
     */
    public static void componentUp(final int start, final int stop, final int delay, final int increment, final Component comp) {
        if (comp.getY() == start)
            (new Thread(() -> {
                for (int i = start; i >= stop; i -= increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(comp.getX(), i);
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(comp.getX(), stop);
            },"component up thread")).start();
    }

    /**
     * Master method to animate any component down in a separate thread
     * @param start the starting y value
     * @param stop the ending y value
     * @param delay the ms delay in between increments
     * @param increment the increment value
     * @param comp the component to move
     */
    public static void componentDown(final int start, final int stop, final int delay, final int increment, final Component comp) {
        if (comp.getY() == start)
            (new Thread(() -> {
                for (int i = start; i <= stop; i += increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(comp.getX(), i);
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(comp.getX(), stop);
            },"component down thread")).start();
    }

    /**
     * Master method to animate any component left in a separate thread
     * @param start the starting x value
     * @param stop the ending x value
     * @param delay the ms delay in between increments
     * @param increment the increment value
     * @param comp the component to move
     */
    public static void componentLeft(final int start, final int stop, final int delay, final int increment, final Component comp) {
        if (comp.getX() == start)
            new Thread(() -> {
                for (int i = start; i >= stop; i -= increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(i, comp.getY());
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(stop, comp.getY());
            },"component left thread").start();
    }

    /**
     * Master method to animate any component right in a separate thread
     * @param start the starting x value
     * @param stop the ending x value
     * @param delay the ms delay in between increments
     * @param increment the increment value
     * @param comp the component to move
     */
    public static void componentRight(final int start, final int stop, final int delay, final int increment, final Component comp) {
        if (comp.getX() == start)
            (new Thread(() -> {
                for (int i = start; i <= stop; i += increment) {
                    try {
                        Thread.sleep(delay);
                        comp.setLocation(i, comp.getY());
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }
                comp.setLocation(stop, comp.getY());
            },"component right thread")).start();
    }

    /**
     *
     * @param start the starting y value
     * @param stop the ending y value
     * @param delay the ms delay in between increments
     * @param increment the increment value
     * @param jLabel the jlabel to animate
     * @deprecated use {@link AnimationUtil#componentUp(int, int, int, int, Component)}
     */
    @Deprecated
    public static void jLabelYUp(final int start, final int stop, final int delay, final int increment, final JLabel jLabel) {
        componentUp(start, stop, delay, increment, jLabel);
    }

    /**
     * This method takes in a width and outputs the increment and delay to obtain a near 1s animation time
     * Used purely for console background switching animations.
     * @param len the length of the side of which want to find an increment for
     * @return an array representing the delay in ms followed by the width increment
     */
    public static int[] getDelayIncrement(int len) {
        int[] ret = new int[2];
        //assign ret[0] to delay
        //assign ret[1] to inc

        try {
            //width below 600 now allowed
            if (len < 600) {
                throw new IllegalArgumentException("Background dimensions below 600x600 " +
                        "are not allowed and shouln't be possible; allow me to fix that");
            }

            double animationLen = 500; //ideally we want the animation to last 1000ms
            double increment = animationLen / len; //delay must be an int so if this is less than 1, problem

            if (len < 500) {
                //increment will be 1 and delay will be at least 1 if not greater
                ret[1] = 1;
                ret[0] = (int) Math.round(animationLen / len);

            } else if (len > 1000) {
                //delay is 1ms, minimum in ms delay
                ret[0] = 1;
                //increment will be 1 from 1001 to 1499
                //increment will be 2 from 1500 to 2000
                //increment will be 3 from 2001 to 2499
                ret[1] = (int) Math.round(len / animationLen);
            } else {
                //width is 1000 and our delay is 1000 which resulted in 1 here. Thus we return the ideal array
                ret[0] = (ret[1] = 1);
            }

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return new int[]{ret[0], ret[1]};
    }
}
