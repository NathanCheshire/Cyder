package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.enums.SliderShape;
import cyder.ui.*;
import cyder.utilities.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class PerlinNoise {
    private static Color maxColor;
    private static Color minColor;

    private static CyderCheckBox animateCheckBox;
    private static CyderButton generate;
    private static CyderButton nextIteration;
    private static CyderTextField dimensionField;
    private static CyderButton dimensionSwitchButton;
    private static String[] dimensions = {"2D","3D"};
    private static JSlider speedSlider;
    private static int sliderValue = 500;

    private static CyderFrame perlinFrame;
    private static JLabel noiseLabel;

    private static int resolution = 512;
    private static float[][] _2DNoise;
    private static float[] _1DNoise;
    private static boolean _2DMode = true;

    private static Random rand = new Random();

    private static Timer timer;

    public static void showGUI() {
        //init with random
        _1DNoise = new float[resolution];
        _2DNoise = new float[resolution][resolution];

        //todotimer = new Timer(, sliderValue);

        for (int i = 0 ; i < resolution ; i++) {
            _1DNoise[i] = 0.0F;
        }

        for (int i = 0 ; i < resolution ; i++) {
            for (int j = 0 ; j < resolution ; j++) {
                _2DNoise[i][j] = 0.0F;
            }
        }

        //init noises
        _2DNoise = generate2DNoise(_2DNoise);
        _1DNoise = generate1DNoise(_1DNoise);

        perlinFrame = new CyderFrame(512 + 200,512 + 300,
               new ImageIcon(ImageUtil.getImageGradient(1000,1100,
               new Color(252,245,255),
               new Color(164,154,187),
               new Color(249, 233, 241))));
        perlinFrame.setTitle("Perlin Noise");

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
                g2d.setColor(Color.black);
                for (int x = 0 ; x < resolution ; x++) {
                    if (x + 1 == resolution)
                        break;

                    float y = (float) ((_1DNoise[x] * resolution / 2.0) + resolution / 2.0);
                    float yn = (float) ((_1DNoise[x + 1] * resolution / 2.0) + resolution / 2.0);

                    g2d.drawLine(x,(int) y, x + 1, (int) yn);
                }

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

        animateCheckBox = new CyderCheckBox();
        animateCheckBox.setSelected();
        animateCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (timer.isRunning())
                    timer.stop();
            }
        });
        animateCheckBox.setBounds(120,650,50,50);
        perlinFrame.getContentPane().add(animateCheckBox);

        CyderLabel animateLable = new CyderLabel("Animate");
        animateLable.setBounds(95,625, 100, 20);
        perlinFrame.getContentPane().add(animateLable);

        generate = new CyderButton("Generate");
        generate.addActionListener(e -> {
            generate();
        });
        generate.setBounds(230,630, 150, 40);
        perlinFrame.getContentPane().add(generate);

        nextIteration = new CyderButton("Next Iteration");
        nextIteration.addActionListener(e -> {
            nextIteration();
        });
        nextIteration.setBounds(400,630, 170, 40);
        perlinFrame.getContentPane().add(nextIteration);

        dimensionField = new CyderTextField(0);
        dimensionField.setFocusable(false);
        dimensionField.setBounds(490,680, 50, 40);
        dimensionField.setEditable(false);
        perlinFrame.getContentPane().add(dimensionField);
        dimensionField.setText(dimensions[0]);

        dimensionSwitchButton = new CyderButton("▼");
        dimensionSwitchButton.setBounds(530,680,40,40);
        perlinFrame.getContentPane().add(dimensionSwitchButton);
        dimensionSwitchButton.addActionListener(e -> dimensionField.setText(
                dimensionField.getText().equals(dimensions[0]) ? dimensions[1] : dimensions[0]));

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, sliderValue);
        CyderSliderUI UI = new CyderSliderUI(speedSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(SliderShape.RECT);
        UI.setFillColor(Color.black);
        UI.setOutlineColor(CyderColors.navy);
        UI.setNewValColor(CyderColors.regularBlue);
        UI.setOldValColor(CyderColors.intellijPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        speedSlider.setUI(UI);
        speedSlider.setBounds(230, 680, 250, 40);
        speedSlider.setPaintTicks(false);
        speedSlider.setPaintLabels(false);
        speedSlider.setVisible(true);
        speedSlider.setValue(500);
        speedSlider.addChangeListener(e -> {
            sliderValue = speedSlider.getValue();
            //todo timer.setdelay
        });
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Pathfinding Animation Timeout");
        speedSlider.setFocusable(false);
        speedSlider.repaint();
        perlinFrame.getContentPane().add(speedSlider);

        perlinFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(perlinFrame);
    }

    //generates new noise based on a new random seed
    private static void generate() {
        if (animateCheckBox.isSelected()) {
            //lock ui elements in place and generate noise based off of current value at a
            // speed corresponding to sliderval * SOME_CONST
        } else {
            //simply generate new noise and update
        }
    }

    //generates new noise based on the current value
    private static void nextIteration() {
        //simply update noise based off of current value, meant to slowly step through,
        // so user can spam button and see at their own pace the algorithm working
    }

    private static float[][] generate2DNoise(float[][] seed) {
        return null;
    }

    private static float[] generate1DNoise(float[] seed) {
        float[] ret = new float[resolution];

        return ret;
    }
}
