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

    private static int resolution = 512;
    private static float[][] _2DNoise;
    private static float[] _1DNoise;
    private static boolean _2DMode = true;

    private static int octaves = 20;

    public static void showGUI() {
        //init with random
        for (int i = 0 ; i < resolution ; i++) {
            _1DNoise[i] = (float) NumberUtil.randInt(0, Integer.MAX_VALUE) / Float.MAX_VALUE;
        }

        for (int i = 0 ; i < resolution ; i++) {
            for (int j = 0 ; j < resolution ; j++) {
                _2DNoise[i][j] = (float) NumberUtil.randInt(0, Integer.MAX_VALUE) / Float.MAX_VALUE;
            }
        }

        //init noises
        _2DNoise = generate2DNoise(_2DNoise);
        _1DNoise = generate1DNoise(_1DNoise, octaves);

        perlinFrame = new CyderFrame(512 + 200,512 + 300,
               new ImageIcon(ImageUtil.getImageGradient(1000,1100,
               new Color(252,245,255),
               new Color(164,154,187),
               new Color(249, 233, 241))));
        perlinFrame.setTitle("Perlin noise");

        noiseLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);

                //setup
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.darkGray);
                g2d.setStroke(new BasicStroke(2));

                int labelWidth = noiseLabel.getWidth();
                int labelHeight = noiseLabel.getHeight();
                int drawTo = labelWidth;

                //draw noise


                //draw border lines last
                g2d.setColor(CyderColors.navy);

                g2d.drawLine(1, 1, 1, drawTo - 1);
                g2d.drawLine(1, 1, drawTo - 1, 1);
                g2d.drawLine(drawTo - 1, 1, drawTo - 1, drawTo - 1);
                g2d.drawLine(1, drawTo - 1, drawTo - 1, drawTo - 1);
            }
        };
       noiseLabel.setBounds(100,100, resolution, resolution);
       perlinFrame.getContentPane().add(noiseLabel);

       perlinFrame.setVisible(true);
       ConsoleFrame.getConsoleFrame().setFrameRelative(perlinFrame);
    }

    private static float[][] generate2DNoise(float[][] seed) {
        return null;
    }

    private static float[] generate1DNoise(float[] seed, int octaves) {
        float[] ret = new float[resolution];

        for (int x = 0 ; x < resolution ; x++) {
            float fNoise = 0.0f;
            float fScale = 1.0f;
            float fScaleAcc = 0.0f;

            for (int o = 0 ; o < octaves ; o++) {
                int pitch = resolution >> o;
                int sample1 = (x / pitch) * pitch;
                int sample2 = (sample1 + pitch) % resolution;

                float fBlend = (float) (x - sample1) / (float) pitch;
                float fSample = (1.0f - fBlend) * seed[sample1] + fBlend * seed[sample2];
                fNoise += fSample * fScale;
                fScaleAcc += fScale;
                fScale = fScale / 2.0f;
            }

            ret[x] = fNoise / fScaleAcc;
        }

        return ret;
    }
}
