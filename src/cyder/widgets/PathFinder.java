package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;

public class PathFinder {
    private static double squareLen = 25;
    private static JLabel gridLabel;

    public static void showGUI() {
        CyderFrame pathFindingFrame = new CyderFrame(1000,1000);
        pathFindingFrame.setTitle("Path finding visualizer");

        gridLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);

                if (gridLabel != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(CyderColors.navy);
                    g2d.setStroke(new BasicStroke(2));

                    //horizontal lines
                    for (int y = 0 ; y < gridLabel.getHeight() ; y += Math.floor(gridLabel.getHeight() / squareLen) - 1) {
                        g2d.drawLine(1, y + 1, gridLabel.getWidth() - 1, y + 1);
                    }

                    //vertical lines
                    for (int x = 0 ; x < gridLabel.getWidth() ; x += Math.floor(gridLabel.getWidth() / squareLen) - 1) {
                        g2d.drawLine(x + 1, 1, x + 1, gridLabel.getWidth() - 1);
                    }

                    for (int x = 0 ; x < 45 ; x++) {
                        for (int y = 0 ; y < 45 ; y++) {
                            //g2d.fillRect(1 + squareLen * x, 1 + squareLen* y, squareLen, squareLen);
                        }
                    }
                }
            }
        };
        gridLabel.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                //up
                if (e.getWheelRotation() == -1) {
                    squareLen++;
                    gridLabel.repaint();
                } else {
                    squareLen--;
                    gridLabel.repaint();
                }
            }
        });
        gridLabel.setSize(802,802);
        gridLabel.setLocation(100,100);
        pathFindingFrame.getContentPane().add(gridLabel);

        pathFindingFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(pathFindingFrame);
    }
}
