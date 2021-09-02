package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.enums.SliderShape;
import cyder.handler.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.ColorUtil;
import cyder.utilities.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class PerlinNoise {
    //colors
    private static Color maxColor = Color.black;
    private static Color minColor = Color.black ;
    private static CyderTextField minColorField;
    private static CyderTextField maxColorField;

    //ui
    private static CyderCheckBox animateCheckBox;
    private static CyderButton generate;
    private static CyderButton nextIteration;
    private static CyderFrame perlinFrame;
    private static JLabel noiseLabel;

    private static CyderTextField dimensionField;
    private static CyderButton dimensionSwitchButton;
    private static String[] dimensions = {"2D","3D"};

    private static JSlider speedSlider;
    private static int sliderValue = 500;
    private static int sliderMaxValue = 1000;
    private static int sliderMaxDelay = 500; //ms

    private static int resolution = 512;
    private static float[][] _3DNoise;
    private static float[] _2DNoise;
    private static boolean _2DMode = true;

    private static Random rand = new Random();
    private static Timer timer;

    private static float[][] instanceSeed;
    private static int octaves = 1;
    private static int maxOctaves = 10;

    public static void showGUI() {
        //init with random
        _2DNoise = new float[resolution];
        _3DNoise = new float[resolution][resolution];

        timer = new Timer((int) ((float) sliderValue / (float) sliderMaxValue * sliderMaxDelay), animationAction);

        for (int i = 0 ; i < resolution ; i++) {
            _2DNoise[i] = rand.nextFloat();
        }

        for (int i = 0 ; i < resolution ; i++) {
            for (int j = 0 ; j < resolution ; j++) {
                _3DNoise[i][j] = rand.nextFloat();
            }
        }

        //set seed and reset octaves
        instanceSeed = _3DNoise;
        octaves = 1;

        //fill noise based on current session's seed
        _3DNoise = generate3DNoise(resolution, instanceSeed, octaves);
        _2DNoise = generate2DNoise(resolution, instanceSeed[0], octaves);

        //ui constructions
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
                if (dimensionField.getText().equals("2D")) {
                    g2d.setColor(Color.black);

                    for (int x = 0 ; x < resolution ; x++) {
                        if (x + 1 == resolution)
                            break;

                        float y = (float) ((_2DNoise[x] * resolution / 2.0) + resolution / 2.0);

                        if (minColor != maxColor) {
                            System.out.println("here");
                            g2d.setColor(getColor(_2DNoise[x]));
                        }

                        g2d.fillRect(x, (int) y,2,2);
                    }
                } else {
                    //todo 3D noise drawing implementation (should be same essentially just with nested for loop)
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
        animateCheckBox.setNotSelected();
        animateCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (timer != null && timer.isRunning())
                    timer.stop();
            }
        });
        animateCheckBox.setBounds(120,650,50,50);
        perlinFrame.getContentPane().add(animateCheckBox);

        CyderLabel animateLabel = new CyderLabel("Animate");
        animateLabel.setBounds(95,625, 100, 20);
        perlinFrame.getContentPane().add(animateLabel);

        generate = new CyderButton("Generate");
        generate.addActionListener(e -> generate());
        generate.setToolTipText("resets the seed, octaves, and current noise");
        generate.setBounds(230,630, 150, 40);
        perlinFrame.getContentPane().add(generate);

        nextIteration = new CyderButton("Next Iteration");
        nextIteration.addActionListener(e -> nextIteration());
        nextIteration.setToolTipText("increments the octave and displayed the revalidated noise");
        nextIteration.setBounds(400,630, 170, 40);
        perlinFrame.getContentPane().add(nextIteration);

        dimensionField = new CyderTextField(0);
        dimensionField.setFocusable(false);
        dimensionField.setBounds(490,675, 50, 40);
        dimensionField.setEditable(false);
        perlinFrame.getContentPane().add(dimensionField);
        dimensionField.setText(dimensions[0]);

        dimensionSwitchButton = new CyderButton("▼");
        dimensionSwitchButton.setBounds(530,675,40,40);
        perlinFrame.getContentPane().add(dimensionSwitchButton);
        dimensionSwitchButton.addActionListener(e -> dimensionField.setText(
                dimensionField.getText().equals(dimensions[0]) ? dimensions[1] : dimensions[0]));

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, sliderMaxValue, sliderValue);
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
        speedSlider.setValue(sliderValue);
        speedSlider.addChangeListener(e -> {
            sliderValue = speedSlider.getValue();
            timer.setDelay((int) ((float) sliderValue / (float) sliderMaxValue * sliderMaxDelay));
        });
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Animation Timeout");
        speedSlider.setFocusable(false);
        speedSlider.repaint();
        perlinFrame.getContentPane().add(speedSlider);

        CyderLabel minColorLabel = new CyderLabel("Min Color:");
        minColorLabel.setBounds(100,730, 100, 20);
        perlinFrame.getContentPane().add(minColorLabel);

        minColorField = new CyderTextField(6);
        minColorField.setRegexMatcher("[0-9A-Fa-f]+");
        minColorField.setBounds(195,720, 120, 40);
        perlinFrame.getContentPane().add(minColorField);
        minColorField.setText(ColorUtil.rgbtohexString(minColor));
        minColorField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                try {
                    minColor = ColorUtil.hextorgbColor(minColorField.getText());
                } catch (Exception ignored) {

                }
            }
        });

        CyderLabel maxColorLabel = new CyderLabel("Max Color:");
        maxColorLabel.setBounds(330,730, 100, 20);
        perlinFrame.getContentPane().add(maxColorLabel);

        maxColorField = new CyderTextField(6);
        maxColorField.setRegexMatcher("[0-9A-Fa-f]+");
        maxColorField.setBounds(425,720, 145, 40);
        perlinFrame.getContentPane().add(maxColorField);
        maxColorField.setText(ColorUtil.rgbtohexString(maxColor));
        maxColorField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                try {
                    maxColor = ColorUtil.hextorgbColor(maxColorField.getText());
                } catch (Exception ignored) {

                }
            }
        });

        perlinFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(perlinFrame);
    }

    //generates new noise based on a new random seed
    private static void generate() {
        if (animateCheckBox.isSelected()) {
           if (timer.isRunning()) {
               timer.stop();
               unlockUI();
               generate.setText("Generate");
           } else {
               lockUI();
               generate.setText("Stop");
               timer.start();
           }
        } else {
            if (timer.isRunning()) {
                timer.stop();
                unlockUI();
                generate.setText("Generate");
            }

            //new seed
            for (int i = 0 ; i < resolution ; i++) {
                for (int j = 0 ; j < resolution ; j++) {
                    instanceSeed[i][j] = rand.nextFloat();
                }
            }

            //reset octaves
            octaves = 1;

            //generate new noise based on random seed and update
            if (dimensionField.getText().equals("2D")) {
                _2DNoise = generate2DNoise(resolution, instanceSeed[0], octaves);
            } else {
                _3DNoise = generate3DNoise(resolution, instanceSeed, octaves);
            }

           //repaint
           noiseLabel.repaint();
        }
    }

    //generates new noise based on the current value
    private static void nextIteration() {
        //simply update noise based off of current value, meant to slowly step through,
        // so user can spam button and see at their own pace the algorithm working

        //serves no purpose during an animation
        if (timer != null && timer.isRunning())
            return;

        octaves++;

        if (octaves == maxOctaves)
            octaves = 1;

        _2DNoise = generate2DNoise(resolution, instanceSeed[0], octaves);
        noiseLabel.repaint();
    }

    private static float[][] generate3DNoise(int nCount, float[][] fSeed, int nOctaves) {
        float[][] ret = new float[resolution][resolution];

        for (int i = 0 ; i < resolution ; i++) {
            ret[i] = generate2DNoise(nCount, fSeed[i],nOctaves);
        }

        return ret;
    }

    public static float[] generate2DNoise(int nCount, float[] fSeed, int nOctaves) {
        float[] ret = new float[nCount];

        for (int x = 0 ; x < nCount ; x++) {
           float fNoise = 0.0f;
           float fScale = 1.0f;
           float fScaleAcc = 0.0f;

           for (int o = 0 ; o < nOctaves ; o++) {
               int nPitch = nCount >> o; //assuming octaves is a power of 2
               int nSample1 =  (x / nPitch) * nPitch;
               int nSample2 = (nSample1 + nPitch) % nCount;

               float fBlend = (float) (x - nSample1) / (float) nPitch;
               float fSample = (1.0f - fBlend) * fSeed[nSample1] + fBlend * fSeed[nSample2];
               fNoise +=  fSample * fScale;
               fScaleAcc += fScale;
               fScale = fScale / 2.0f;
           }

           ret[x] = fNoise / fScaleAcc;
        }

        return ret;
    }

    private static ActionListener animationAction = evt -> {
        octaves++;

        if (octaves == maxOctaves) {
            octaves = 1;

            //new seed
            for (int i = 0 ; i < resolution ; i++) {
                for (int j = 0 ; j < resolution ; j++) {
                    instanceSeed[i][j] = rand.nextFloat();
                }
            }
        }

        //generate new noise based on random seed and update
        if (dimensionField.getText().equals("2D")) {
            _2DNoise = generate2DNoise(resolution, instanceSeed[0], octaves);
        } else {
            _3DNoise = generate3DNoise(resolution, instanceSeed, octaves);
        }

        //repaint
        noiseLabel.repaint();
    };

    private static void lockUI() {
        animateCheckBox.setEnabled(false);
        nextIteration.setEnabled(false);
        dimensionSwitchButton.setEnabled(false);
        minColorField.setEnabled(false);
        maxColorField.setEnabled(false);
    }

    private static void unlockUI() {
        animateCheckBox.setEnabled(true);
        nextIteration.setEnabled(true);
        dimensionSwitchButton.setEnabled(true);
        minColorField.setEnabled(true);
        maxColorField.setEnabled(true);
    }

    private static Color getColor(float val) {
        float minHue = getHue(minColor);
        float maxHue = getHue(maxColor);

        float hue = val * maxHue + (1 - val) * minHue;
        Color c = new Color(Color.HSBtoRGB(hue, 1, 0.5f));

        return c;
    }

    private static int getHue(Color c) {
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();

        float min = Math.min(Math.min(red, green), blue);
        float max = Math.max(Math.max(red, green), blue);

        if (min == max) {
            return 0;
        }

        float hue = 0f;
        if (max == red) {
            hue = (green - blue) / (max - min);

        } else if (max == green) {
            hue = 2f + (blue - red) / (max - min);

        } else {
            hue = 4f + (red - green) / (max - min);
        }

        hue = hue * 60;
        if (hue < 0) hue = hue + 360;

        return Math.round(hue);
    }
}