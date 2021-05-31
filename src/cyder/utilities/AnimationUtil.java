package cyder.utilities;

import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;

import javax.swing.*;
import java.awt.*;

public class AnimationUtil {
    private AnimationUtil() {
    } //private constructor to avoid object creation

    public static void closeAnimation(Frame frame) {
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
    }

    public static void minimizeAnimation(JFrame frame) {
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
    }

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


    public static void jLabelYUp(final int start, final int stop, final int delay, final int increment, final JLabel jLabel) {
        if (jLabel.getY() == start)
            (new Thread(() -> {
                while (jLabel.getY() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jLabel.setLocation(jLabel.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jLabel.setLocation(jLabel.getX(), stop);
            })).start();
    }

    public static void jLabelYDown(final int start, final int stop, final int delay, final int increment, final JLabel jLabel) {
        if (jLabel.getY() == start)
            (new Thread(() -> {
                while (jLabel.getY() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jLabel.setLocation(jLabel.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jLabel.setLocation(jLabel.getX(), stop);
            })).start();
    }

    public static void jLabelXLeft(final int start, final int stop, final int delay, final int increment, final JLabel jLabel) {
        if (jLabel.getX() == start)
            (new Thread(() -> {
                while (jLabel.getX() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jLabel.setLocation(i, jLabel.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jLabel.setLocation(stop, jLabel.getY());
            })).start();
    }

    public static void jLabelXRight(final int start, final int stop, final int delay, final int increment, final JLabel jLabel) {
        if (jLabel.getX() == start)
            (new Thread(() -> {
                while (jLabel.getX() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jLabel.setLocation(i, jLabel.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jLabel.setLocation(stop, jLabel.getY());
            })).start();
    }

    public static void jTextFieldYUp(final int start, final int stop, final int delay, final int increment, final JTextField jTextField) {
        if (jTextField.getY() == start)
            (new Thread(() -> {
                while (jTextField.getY() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jTextField.setLocation(jTextField.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jTextField.setLocation(jTextField.getX(), stop);
            })).start();
    }

    public static void jTextFieldYDown(final int start, final int stop, final int delay, final int increment, final JTextField jTextField) {
        if (jTextField.getY() == start)
            (new Thread(() -> {
                while (jTextField.getY() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jTextField.setLocation(jTextField.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jTextField.setLocation(jTextField.getX(), stop);
            })).start();
    }

    public static void jTextFieldXLeft(final int start, final int stop, final int delay, final int increment, final JTextField jTextField) {
        if (jTextField.getX() == start)
            (new Thread(() -> {
                while (jTextField.getX() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jTextField.setLocation(i, jTextField.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jTextField.setLocation(stop, jTextField.getY());
            })).start();
    }

    public static void jTextFieldXRight(final int start, final int stop, final int delay, final int increment, final JTextField jTextField) {
        if (jTextField.getX() == start)
            (new Thread(() -> {
                while (jTextField.getX() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jTextField.setLocation(i, jTextField.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jTextField.setLocation(stop, jTextField.getY());
            })).start();
    }

    public static void jButtonYUp(final int start, final int stop, final int delay, final int increment, final JButton jButton) {
        if (jButton.getY() == start)
            (new Thread(() -> {
                while (jButton.getY() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jButton.setLocation(jButton.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jButton.setLocation(jButton.getX(), stop);
            })).start();
    }

    public static void jButtonYDown(final int start, final int stop, final int delay, final int increment, final JButton jButton) {
        if (jButton.getY() == start)
            (new Thread(() -> {
                while (jButton.getY() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jButton.setLocation(jButton.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jButton.setLocation(jButton.getX(), stop);
            })).start();
    }

    public static void jButtonXLeft(final int start, final int stop, final int delay, final int increment, final JButton jButton) {
        if (jButton.getX() == start)
            (new Thread(() -> {
                while (jButton.getX() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jButton.setLocation(i, jButton.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jButton.setLocation(stop, jButton.getY());
            })).start();
    }

    public static void jButtonXRight(final int start, final int stop, final int delay, final int increment, final JButton jButton) {
        if (jButton.getX() == start)
            (new Thread(() -> {
                while (jButton.getX() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jButton.setLocation(i, jButton.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jButton.setLocation(stop, jButton.getY());
            })).start();
    }

    public static void jPasswordFieldYUp(final int start, final int stop, final int delay, final int increment, final JPasswordField jPasswordField) {
        if (jPasswordField.getY() == start)
            (new Thread(() -> {
                while (jPasswordField.getY() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jPasswordField.setLocation(jPasswordField.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jPasswordField.setLocation(jPasswordField.getX(), stop);
            })).start();
    }

    public static void jPasswordFieldYDown(final int start, final int stop, final int delay, final int increment, final JPasswordField jPasswordField) {
        if (jPasswordField.getY() == start)
            (new Thread(() -> {
                while (jPasswordField.getY() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jPasswordField.setLocation(jPasswordField.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jPasswordField.setLocation(jPasswordField.getX(), stop);
            })).start();
    }

    public static void jPasswordFieldXLeft(final int start, final int stop, final int delay, final int increment, final JPasswordField jPasswordField) {
        if (jPasswordField.getX() == start)
            (new Thread(() -> {
                while (jPasswordField.getX() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jPasswordField.setLocation(i, jPasswordField.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jPasswordField.setLocation(stop, jPasswordField.getY());
            })).start();
    }

    public static void jPasswordFieldXRight(final int start, final int stop, final int delay, final int increment, final JPasswordField jPasswordField) {
        if (jPasswordField.getX() == start)
            (new Thread(() -> {
                while (jPasswordField.getX() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jPasswordField.setLocation(i, jPasswordField.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jPasswordField.setLocation(stop, jPasswordField.getY());
            })).start();
    }

    public static void jTextAreaYUp(final int start, final int stop, final int delay, final int increment, final JScrollPane jScrollPane) {
        if (jScrollPane.getY() == start)
            (new Thread(() -> {
                while (jScrollPane.getY() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jScrollPane.setLocation(jScrollPane.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jScrollPane.setLocation(jScrollPane.getX(), stop);
            })).start();
    }

    public static void jTextAreaYDown(final int start, final int stop, final int delay, final int increment, final JScrollPane jScrollPane) {
        if (jScrollPane.getY() == start)
            (new Thread(() -> {
                while (jScrollPane.getY() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jScrollPane.setLocation(jScrollPane.getX(), i);
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jScrollPane.setLocation(jScrollPane.getX(), stop);
            })).start();
    }

    public static void jTextAreaXLeft(final int start, final int stop, final int delay, final int increment, final JScrollPane jScrollPane) {
        if (jScrollPane.getX() == start)
            (new Thread(() -> {
                while (jScrollPane.getX() > stop) {
                    for (int i = start; i >= stop; i -= increment) {
                        try {
                            Thread.sleep(delay);
                            jScrollPane.setLocation(i, jScrollPane.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jScrollPane.setLocation(stop, jScrollPane.getY());
            })).start();
    }

    public static void jTextAreaXRight(final int start, final int stop, final int delay, final int increment, final JScrollPane jScrollPane) {
        if (jScrollPane.getX() == start)
            (new Thread(() -> {
                while (jScrollPane.getX() <= start) {
                    for (int i = start; i <= stop; i += increment) {
                        try {
                            Thread.sleep(delay);
                            jScrollPane.setLocation(i, jScrollPane.getY());
                        } catch (InterruptedException e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }
                jScrollPane.setLocation(stop, jScrollPane.getY());
            })).start();
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

            double animationLen = 1000; //ideally we want the animation to last 1000ms
            double increment = animationLen / width; //delay must be an int so if this is less than 1, problem

            if (width < 1000) {
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

        return ret;
    }
}
