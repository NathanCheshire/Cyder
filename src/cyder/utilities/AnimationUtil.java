package cyder.utilities;

import cyder.handler.ErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class AnimationUtil {
    private AnimationUtil() {
    } //private constructor to avoid object creation

    public static void closeAnimation(JFrame frame) {
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

    //TODO redo this method for the new console frame
    public static int[] getDelayIncrement(int width) {
        try {
            LinkedList<Integer> divisibles = new LinkedList<>();

            for (int i = 1; i <= width / 2; i++) {
                if (width % i == 0)
                    divisibles.add(i);
            }

            int desired = 10;
            int distance = Math.abs(divisibles.get(0) - desired);
            int index = 0;

            for (int i = 1; i < divisibles.size(); i++) {
                int curDist = Math.abs(divisibles.get(i) - desired);

                if (curDist < distance) {
                    index = i;

                    distance = curDist;
                }
            }

            int inc = divisibles.get(index);
            return new int[]{1, inc};
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }
}
