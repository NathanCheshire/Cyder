package cyder.widgets;

import cyder.algorithoms.OpenSimplexAlgorithms;
import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.enums.SliderShape;
import cyder.genesis.GenesisShare;
import cyder.ui.*;
import cyder.utilities.ImageUtil;
import cyder.utilities.NumberUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class PerlinNoise {
    //ui
    private static CyderCheckBox animateCheckBox;
    private static CyderButton generate;
    private static CyderButton nextIteration;
    private static CyderFrame perlinFrame;
    private static JLabel noiseLabel;

    private static CyderTextField dimensionField;
    private static CyderButton dimensionSwitchButton;
    private static String[] dimensions = {"2D","3D"};

    //open simplex vars
    private static double FEATURE_SIZE = 24.0;
    private static OpenSimplexAlgorithms noise;
    private static double timeStep = 0;
    private static JSlider featureSlider;
    private static double minFeatureSize = 24.0;
    private static double maxFeatureSize = minFeatureSize * 2.0;

    private static JSlider speedSlider;
    private static int sliderValue = 500;
    private static int sliderMaxValue = 1000;
    private static int sliderMaxDelay = 500; //ms

    private static int resolution = 512;
    private static Node[][] _3DNoise;
    private static float[] _2DNoise;
    private static boolean _2DMode = true;

    private static Random rand = new Random();
    private static Timer timer;

    private static float[][] instanceSeed;
    private static int octaves = 1;
    private static int maxOctaves = 10;

    private static boolean closed = true;

    @Widget("perlin")
    public static void showGUI() {
        //set closed
        closed = false;

        //init with random
        _2DNoise = new float[resolution];
        _3DNoise = new Node[resolution][resolution];

        noise = new OpenSimplexAlgorithms(0);
        timeStep = 0;

        for (int x = 0 ; x < resolution ; x++) {
            for (int y = 0 ; y < resolution ; y++) {
                _3DNoise[x][y] = new Node(x,y);
            }
        }

        instanceSeed = new float[resolution][resolution];

        timer = new Timer((int) ((float) sliderValue / (float) sliderMaxValue * sliderMaxDelay), animationAction);

        for (int i = 0 ; i < resolution ; i++) {
            _2DNoise[i] = rand.nextFloat();
        }

        //set seed and octaves
        for (int i = 0 ; i < resolution ; i++) {
            for (int j = 0 ; j < resolution ; j++) {
                instanceSeed[i][j] = rand.nextFloat();
            }
        }

        octaves = 1;

        //fill noise based on current session's seed
        _2DNoise = generate2DNoise(resolution, instanceSeed[0], octaves);

        //ui constructions
        perlinFrame = new CyderFrame(512 + 200,750,
               new ImageIcon(ImageUtil.getImageGradient(1000,750,
               new Color(252,245,255),
               new Color(164,154,187),
               new Color(249, 233, 241))));
        perlinFrame.setTitle("Perlin Noise");

        perlinFrame.addPreCloseAction(() -> {
            _2DNoise = null;
            _3DNoise = null;
            closed = true;

            if (timer != null && timer.isRunning())
                timer.stop();
        });

        //stop the animation when we are trying to close the frame if it's running
        perlinFrame.addPreCloseAction(() -> timer.stop());

        noiseLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                if (closed)
                    return;

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
                        int minY = (int) y;
                        int lenDown = 0;
                        //range is between y and resolution

                        //top line
                        g2d.setColor(Color.black);
                        g2d.fillRect(x, minY,2, 2);

                        lenDown += 2;

                        //green grass
                        g2d.setColor(CyderColors.regularGreen);
                        g2d.fillRect(x, minY + lenDown,2, 10);

                        lenDown += 10;

                        //brown dirt
                        g2d.setColor(new Color(131,101,57));
                        g2d.fillRect(x, minY + lenDown,2, 20);

                        lenDown += 20;

                        //gray stone
                        g2d.setColor(Color.darkGray);
                        g2d.fillRect(x, minY + lenDown,2, resolution - (minY + lenDown));
                    }
                } else {
                    for (int i = 0 ; i < resolution ; i++) {
                        for (int j = 0 ; j < resolution ; j++) {
                            g2d.setColor(_3DNoise[i][j].getColor());
                            g2d.fillRect(i,j,1,1);
                        }
                    }
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

        featureSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
        CyderSliderUI UI2 = new CyderSliderUI(featureSlider);
        UI2.setThumbStroke(new BasicStroke(2.0f));
        UI2.setSliderShape(SliderShape.RECT);
        UI2.setFillColor(Color.black);
        UI2.setOutlineColor(CyderColors.navy);
        UI2.setNewValColor(CyderColors.regularBlue);
        UI2.setOldValColor(CyderColors.intellijPink);
        UI2.setTrackStroke(new BasicStroke(3.0f));
        featureSlider.setUI(UI2);
        featureSlider.setBounds(230, 710, 250, 40);
        featureSlider.setPaintTicks(false);
        featureSlider.setPaintLabels(false);
        featureSlider.setVisible(true);
        featureSlider.setValue(500);
        featureSlider.addChangeListener(e -> {
            FEATURE_SIZE = (featureSlider.getValue() / 1000.0) * (maxFeatureSize - minFeatureSize) + minFeatureSize;

            if (dimensionField.getText().equals("3D") && !timer.isRunning()) {
                for (int y = 0; y < resolution; y++) {
                    for (int x = 0; x < resolution; x++) {
                        double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, timeStep);
                        _3DNoise[x][y].setColor(new Color(0x010101 * (int)((value + 1) * 127.5)));
                        _3DNoise[x][y].setX(x);
                        _3DNoise[x][y].setY(y);
                    }
                }

                noiseLabel.repaint();
            }
        });
        featureSlider.setOpaque(false);
        featureSlider.setToolTipText("3D Feature Size");
        featureSlider.setFocusable(false);
        featureSlider.repaint();
        perlinFrame.getContentPane().add(featureSlider);

        perlinFrame.setVisible(true);
        perlinFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    //generates new noise based on a new random seed
    private static void generate() {
        if (closed)
            return;

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
            if (dimensionField.getText().equals("2D")) {
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

                //new noise
                _2DNoise = generate2DNoise(resolution, instanceSeed[0], octaves);
            } else {
                //reset timeStep
                timeStep = 0;

                noise = new OpenSimplexAlgorithms(NumberUtil.randInt(0,1000));
                for (int y = 0; y < resolution; y++) {
                    for (int x = 0; x < resolution; x++) {
                        double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, timeStep);
                        _3DNoise[x][y].setColor(new Color(0x010101 * (int)((value + 1) * 127.5)));
                        _3DNoise[x][y].setX(x);
                        _3DNoise[x][y].setY(y);
                    }
                }
            }

            //repaint
            noiseLabel.repaint();
        }
    }

    //generates new noise based on the current value
    private static void nextIteration() {
        if (closed)
            return;

        if (dimensionField.getText().equals("2D")) {
            //serves no purpose during an animation
            if (timer != null && timer.isRunning())
                return;

            octaves++;

            if (octaves == maxOctaves)
                octaves = 1;

            _2DNoise = generate2DNoise(resolution, instanceSeed[0], octaves);
        } else {
            timeStep += 0.1;
            for (int y = 0; y < resolution; y++) {
                for (int x = 0; x < resolution; x++) {
                    double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, timeStep);
                    _3DNoise[x][y].setColor(new Color(0x010101 * (int)((value + 1) * 127.5)));
                    _3DNoise[x][y].setX(x);
                    _3DNoise[x][y].setY(y);
                }
            }
        }

        //repaint
        noiseLabel.repaint();
    }

    /**
     * Generates perlin noise based on common algorithm implementation
     * @param nCount he number of points in the line
     * @param fSeed the seed value
     * @param nOctaves the number of iterations to perform the algorithm on
     * @return 2D perlin noise representation (values are between 0 and 1)
     */
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
        if (closed)
            return;

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
            timeStep += 0.1;

            for (int y = 0; y < resolution; y++) {
                for (int x = 0; x < resolution; x++) {
                    double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, timeStep);
                    _3DNoise[x][y].setColor(new Color(0x010101 * (int)((value + 1) * 127.5)));
                    _3DNoise[x][y].setX(x);
                    _3DNoise[x][y].setY(y);
                }
            }
        }

        //repaint
        noiseLabel.repaint();
    };

    private static void lockUI() {
        animateCheckBox.setEnabled(false);
        nextIteration.setEnabled(false);
        dimensionSwitchButton.setEnabled(false);
        featureSlider.setEnabled(false);
    }

    private static void unlockUI() {
        animateCheckBox.setEnabled(true);
        nextIteration.setEnabled(true);
        dimensionSwitchButton.setEnabled(true);
        featureSlider.setEnabled(true);
    }

    private static Color getColor(float val) {
        return Color.decode(String.valueOf(0x010101 * (int)((val + 1) * 127.5)));
    }

    private static class Node {
        private int x;
        private int y;
        private Color color;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }
    }
}