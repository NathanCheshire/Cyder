package com.cyder.utilities;

import com.cyder.handler.ErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class AnimationUtil {
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
        }

        catch (Exception e) {
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
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public static void enterAnimation(JFrame frame) {
        frame.setVisible(false);
        frame.setLocationRelativeTo(null);

        int to = frame.getY();
        frame.setLocation(frame.getX(), 0 - frame.getHeight());

        frame.setVisible(true);

        for (int i = 0 - frame.getHeight() ; i < to ; i+= 15) {
            frame.setLocation(frame.getX(), i);
            try {
                Thread.sleep(1);
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }

        frame.setLocationRelativeTo(null);
    }

    public static int[] getDelayIncrement(int width) {
        try {
            LinkedList<Integer> divisibles = new LinkedList<>();

            for (int i = 1 ; i <= width / 2 ; i++) {
                if (width % i == 0)
                    divisibles.add(i);
            }

            int desired = 10;
            int distance = Math.abs(divisibles.get(0)- desired);
            int index = 0;

            for(int i = 1; i < divisibles.size(); i++){
                int curDist = Math.abs(divisibles.get(i) - desired);

                if(curDist < distance){
                    index = i;

                    distance = curDist;
                }
            }

            int inc = divisibles.get(index);
            return new int[] {1, inc};
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }
}
