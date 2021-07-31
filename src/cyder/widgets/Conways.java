package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Conways {
    private int[][] grid;

    public Conways() {
        grid = new int[45][45];
        CyderFrame cf = new CyderFrame(940,1050, CyderImages.defaultBackgroundLarge);
        cf.setTitle("Conway's Game of Life");

        JLabel gridLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(CyderColors.navy);
                g2d.setStroke(new BasicStroke(2));

                int gridLen = 90;
                int squareLen = 20;

                //vertical lines
                for (int i = 0 ; i <= gridLen * squareLen ; i += squareLen) {
                    g2d.drawLine(i + 1, 1, i + 1, gridLen * squareLen + 1);
                }

                //horizontal lines
                for (int i = 0 ; i <= gridLen * squareLen ; i += squareLen) {
                    g2d.drawLine(1, i + 1, gridLen * squareLen + 1, i + 1);
                }

                for (int x = 0 ; x < 45 ; x++) {
                    for (int y = 0 ; y < 45 ; y++) {
                        if (grid[x][y] == 1) {
                            g2d.fillRect(1 + squareLen * x, 1 + squareLen* y, squareLen, squareLen);
                        }
                    }
                }
            }
        };
        gridLabel.setOpaque(true);
        gridLabel.setBackground(Color.white);
        gridLabel.setBounds(20,60,902,902);
        cf.getContentPane().add(gridLabel);
        gridLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double x = Math.floor(((e.getX() + 2) / 20));
                double y = Math.floor(((e.getY() + 2) / 20));

                //todo toggle instead of set to 1
                if (x < 45 && y < 45 && x >= 0 && y >= 0) {
                    grid[(int) x][(int) y] = 1;
                    gridLabel.repaint();
                }
            }
        });

        //buttons: reset, simulate, generate random

        cf.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(cf);
    }
}
