package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.ui.*;
import cyder.utilities.ImageUtil;
import cyder.utilities.NumberUtil;

import javax.swing.*;
import java.awt.*;

public class PerlinNoise {
    private static Color maxColor;
    private static Color minColor;
    private static CyderCheckBox animateCheckBox;
    private static CyderButton generate;
    private static CyderTextField dimensionField;
    private static CyderButton dimensionSwitchButton;
    private static CyderFrame perlinFrame;
    private static JLabel noiseLabel;

    private static double[][] noise;

    public static void showGUI() {
        noise = new double[797][797];

        for (int x = 0 ; x < 797 - 3; x++) {
            for (int y = 0 ; y < 797 - 3; y++) {
                noise[x][y] = NumberUtil.randInt(0,1);
            }
        }

        perlinFrame = new CyderFrame(1000,1100,
               new ImageIcon(ImageUtil.getImageGradient(1000,1100,
               new Color(252,245,255),
               new Color(164,154,187),
               new Color(249, 233, 241))));
        perlinFrame.setTitle("Perlin noise");

        noiseLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.darkGray);
                g2d.setStroke(new BasicStroke(2));

                int labelWidth = noiseLabel.getWidth();
                int labelHeight = noiseLabel.getHeight();
                int drawTo = labelWidth;

                g2d.setColor(CyderColors.navy);

                g2d.drawLine(1, 1, 1, drawTo - 1);
                g2d.drawLine(1, 1, drawTo - 1, 1);
                g2d.drawLine(drawTo - 1, 1, drawTo - 1, drawTo - 1);
                g2d.drawLine(1, drawTo - 1, drawTo - 1, drawTo - 1);

                for (int x = 0 ; x < 797; x++) {
                    for (int y = 0 ; y < 797; y++) {
                        if (noise[x][y] == 1)
                            g2d.setColor(Color.white);
                        else
                            g2d.setColor(Color.black);

                        g2d.drawRect(x + 3,y + 3,1,1);
                    }
                }
            }
        };
       noiseLabel.setBounds(100,100, 800, 800);
       perlinFrame.getContentPane().add(noiseLabel);

       perlinFrame.setVisible(true);
       ConsoleFrame.getConsoleFrame().setFrameRelative(perlinFrame);
    }
}
