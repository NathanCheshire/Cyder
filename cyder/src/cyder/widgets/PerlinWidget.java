package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.math.NumberUtil;
import cyder.ui.CyderGrid;
import cyder.ui.button.CyderButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.selection.CyderCheckbox;
import cyder.ui.selection.CyderComboBox;
import cyder.ui.slider.CyderSliderUi;
import cyder.utils.SimplexNoiseUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

/**
 * A visualizer for two dimensional perlin-noise and three-dimensional open simplex noise.
 */
@Vanilla
@CyderAuthor
public final class PerlinWidget {
    /**
     * Suppress default constructor.
     */
    private PerlinWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The checkbox for animation.
     */
    private static CyderCheckbox animateCheckBox;

    /**
     * The button which chooses a new seed for the noise.
     */
    private static CyderButton generate;

    /**
     * The button which iterates to the next iteration of the noise.
     */
    private static CyderButton nextIteration;

    /**
     * The frame used for animation
     */
    private static CyderFrame perlinFrame;

    /**
     * The overridden label to paint the noise calculations on.
     */
    private static JLabel noiseLabel;

    /**
     * The minimum feature size for open simplex noise.
     */
    public static final double MINIMUM_FEATURE_SIZE = 24.0;

    /**
     * The default feature size for open simplex noise.
     */
    public static final double DEFAULT_FEATURE_SIZE = 24.0;

    /**
     * The maximum feature size for open simplex noise.
     */
    public static final double MAXIMUM_FEATURE_SIZE = MINIMUM_FEATURE_SIZE * 2.0;

    /**
     * The feature size for open simplex noise.
     */
    private static double FEATURE_SIZE = DEFAULT_FEATURE_SIZE;

    /**
     * The open simplex noise object.
     */
    private static SimplexNoiseUtil noise = new SimplexNoiseUtil(0);

    /**
     * The time step current at.
     */
    private static double timeStep;

    /**
     * The slider used to change the open simplex noise feature size.
     */
    private static JSlider featureSlider;

    /**
     * The combo box used to change dimensions.
     */
    private static CyderComboBox comboBox;

    /**
     * The state used for two dimensions of noise.
     */
    private static final CyderComboBox.ComboItem twoDimensionState =
            new CyderComboBox.ComboItem("2D");

    /**
     * The state used for three dimensions of noise (technically 4).
     */
    private static final CyderComboBox.ComboItem threeDimensionState =
            new CyderComboBox.ComboItem("3D");

    /**
     * The slider used to determine the speed of the animation.
     */
    private static JSlider speedSlider;

    /**
     * The default value for the speed slider.
     */
    private static int speedSliderValue = 400;

    /**
     * The maximum value for the speed slider.
     */
    private static final int speedSliderMaxValue = 500;

    /**
     * The minimum value for the speed slider.
     */
    private static final int speedSliderMinValue = 0;

    /**
     * The resolution of open simplex noise.
     */
    private static final int resolution = 600;

    /**
     * The node array to store the open simplex noise.
     */
    private static CyderGrid.GridNode[][] _3DNoise;

    /**
     * The array to store the perlin noise.
     */
    private static float[] _2DNoise;

    /**
     * The random object used for randomizing the noise seeds.
     */
    private static final Random rand = new Random();

    /**
     * The animation timer.
     */
    private static Timer timer;

    /**
     * The seeding value to use for open simplex noise.
     */
    private static float[][] instanceSeed;

    /**
     * The number of octaves for open simplex (iterations).
     */
    private static int octaves = 1;

    /**
     * The maximum number of octaves (iterations).
     */
    private static final int maxOctaves = 10;

    /**
     * Determines whether the frame has been requested to close or is already closed.
     */
    private static boolean closed = true;

    /**
     * Shows the perlin noise widget.
     */
    @Widget(triggers = "perlin", description = "Perlin noise visualizer/open simplex noise visualizer")
    public static void showGui() {
        UiUtil.closeIfOpen(perlinFrame);

        //set closed
        closed = false;

        //init with random
        _2DNoise = new float[resolution];
        _3DNoise = new CyderGrid.GridNode[resolution][resolution];

        timeStep = 0;

        for (int x = 0 ; x < resolution ; x++) {
            for (int y = 0 ; y < resolution ; y++) {
                _3DNoise[x][y] = new CyderGrid.GridNode(x, y);
            }
        }

        instanceSeed = new float[resolution][resolution];

        timer = new Timer(speedSliderMaxValue - speedSliderValue, animationAction);

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
        _2DNoise = generate2DNoise(instanceSeed[0], octaves);

        //ui constructions
        perlinFrame = new CyderFrame(600 + 10, 750 + CyderDragLabel.DEFAULT_HEIGHT);
        perlinFrame.setTitle("Noise");

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

                //draw noise
                if (comboBox.getCurrentState().equals(twoDimensionState)) {
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
                        g2d.fillRect(x, minY, 2, 2);

                        lenDown += 2;

                        //green grass
                        g2d.setColor(CyderColors.regularGreen);
                        g2d.fillRect(x, minY + lenDown, 2, 10);

                        lenDown += 10;

                        // brown dirt
                        g2d.setColor(CyderColors.brownDirt);
                        g2d.fillRect(x, minY + lenDown, 2, 20);

                        lenDown += 20;

                        //gray stone
                        g2d.setColor(Color.darkGray);
                        g2d.fillRect(x, minY + lenDown, 2, resolution - (minY + lenDown));
                    }
                } else {
                    for (int i = 0 ; i < resolution ; i++) {
                        for (int j = 0 ; j < resolution ; j++) {
                            g2d.setColor(_3DNoise[i][j].getColor());
                            g2d.fillRect(i, j, 1, 1);
                        }
                    }
                }

                //draw border lines last
                g2d.setColor(CyderColors.navy);

                g2d.drawLine(1, 1, 1, labelWidth - 1);
                g2d.drawLine(1, 1, labelWidth - 1, 1);
                g2d.drawLine(labelWidth - 1, 1, labelWidth - 1, labelWidth - 1);
                g2d.drawLine(1, labelWidth - 1, labelWidth - 1, labelWidth - 1);
            }
        };
        noiseLabel.setBounds(1, 0, resolution, resolution);
        perlinFrame.getContentPane().add(noiseLabel);

        animateCheckBox = new CyderCheckbox();
        animateCheckBox.setNotChecked();
        animateCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (timer != null && timer.isRunning())
                    timer.stop();
            }
        });
        animateCheckBox.setBounds(25, 630, 50, 50);
        perlinFrame.getContentPane().add(animateCheckBox);

        CyderLabel animateLabel = new CyderLabel("Animate");
        animateLabel.setBounds(0, 605, 100, 20);
        perlinFrame.getContentPane().add(animateLabel);

        generate = new CyderButton("Generate");
        generate.addActionListener(e -> generate());
        generate.setToolTipText("resets the seed, octaves, and current noise");
        generate.setBounds(25 + 25 + 50, 630, 180, 40);
        perlinFrame.getContentPane().add(generate);

        nextIteration = new CyderButton("Next Iteration");
        nextIteration.addActionListener(e -> nextIteration());
        nextIteration.setToolTipText("increments the octave and displayed the revalidated noise");
        nextIteration.setBounds(25 + 25 + 50 + 180 + 25, 630, 180, 40);
        perlinFrame.getContentPane().add(nextIteration);

        ArrayList<CyderComboBox.ComboItem> states = new ArrayList<>();
        states.add(twoDimensionState);
        states.add(threeDimensionState);
        comboBox = new CyderComboBox(80, 40, states, twoDimensionState);
        comboBox.setBounds(25 + 25 + 50 + 180 + 25 + 180 + 25, 630, 80, 40);
        perlinFrame.getContentPane().add(comboBox);
        comboBox.getIterationButton().addActionListener(e -> {
            nextIteration();

            timeStep += 0.1;
            for (int y = 0 ; y < resolution ; y++) {
                for (int x = 0 ; x < resolution ; x++) {
                    double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, timeStep);
                    _3DNoise[x][y].setColor(generateGrayscaleColor(value));
                    _3DNoise[x][y].setX(x);
                    _3DNoise[x][y].setY(y);
                }
            }
            noiseLabel.repaint();
        });

        speedSlider = new JSlider(JSlider.HORIZONTAL, speedSliderMinValue, speedSliderMaxValue, speedSliderValue);
        CyderSliderUi UI = new CyderSliderUi(speedSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setThumbShape(CyderSliderUi.ThumbShape.RECT);
        UI.setThumbFillColor(Color.black);
        UI.setThumbOutlineColor(CyderColors.navy);
        UI.setRightThumbColor(CyderColors.regularBlue);
        UI.setLeftThumbColor(CyderColors.regularPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        speedSlider.setUI(UI);
        speedSlider.setBounds(25, 630 + 50, 600 - 50, 40);
        speedSlider.setPaintTicks(false);
        speedSlider.setPaintLabels(false);
        speedSlider.setVisible(true);
        speedSlider.setValue(speedSliderValue);
        speedSlider.addChangeListener(e -> {
            speedSliderValue = speedSlider.getValue();
            timer.setDelay(speedSliderMaxValue - speedSliderValue);
        });
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Animation Timeout");
        speedSlider.setFocusable(false);
        speedSlider.repaint();
        perlinFrame.getContentPane().add(speedSlider);

        featureSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
        CyderSliderUi UI2 = new CyderSliderUi(featureSlider);
        UI2.setThumbStroke(new BasicStroke(2.0f));
        UI2.setThumbShape(CyderSliderUi.ThumbShape.RECT);
        UI2.setThumbFillColor(Color.black);
        UI2.setThumbOutlineColor(CyderColors.navy);
        UI2.setRightThumbColor(CyderColors.regularBlue);
        UI2.setLeftThumbColor(CyderColors.regularPink);
        UI2.setTrackStroke(new BasicStroke(3.0f));
        featureSlider.setUI(UI2);
        featureSlider.setBounds(25, 630 + 50 + 50, 600 - 50, 40);
        featureSlider.setPaintTicks(false);
        featureSlider.setPaintLabels(false);
        featureSlider.setVisible(true);
        featureSlider.setValue(500);
        featureSlider.addChangeListener(e -> {
            FEATURE_SIZE = (featureSlider.getValue() / 1000.0)
                    * (MAXIMUM_FEATURE_SIZE - MINIMUM_FEATURE_SIZE) + MINIMUM_FEATURE_SIZE;

            if (comboBox.getCurrentState().equals(threeDimensionState) && !timer.isRunning()) {
                for (int y = 0 ; y < resolution ; y++) {
                    for (int x = 0 ; x < resolution ; x++) {
                        double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, timeStep);
                        _3DNoise[x][y].setColor(generateGrayscaleColor(value));
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

        perlinFrame.finalizeAndShow();
    }

    /**
     * Generates new noise based on the current random seed.
     */
    private static void generate() {
        if (closed)
            return;

        if (animateCheckBox.isChecked()) {
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
            if (comboBox.getCurrentState().equals(twoDimensionState)) {
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
                _2DNoise = generate2DNoise(instanceSeed[0], octaves);
            } else {
                //reset timeStep
                timeStep = 0;

                noise = new SimplexNoiseUtil(NumberUtil.randInt(1000));
                for (int y = 0 ; y < resolution ; y++) {
                    for (int x = 0 ; x < resolution ; x++) {
                        double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, timeStep);
                        _3DNoise[x][y].setColor(generateGrayscaleColor(value));
                        _3DNoise[x][y].setX(x);
                        _3DNoise[x][y].setY(y);
                    }
                }
            }

            //repaint
            noiseLabel.repaint();
        }
    }

    /**
     * Generates the new iteration of noise from the current noise.
     */
    private static void nextIteration() {
        if (closed)
            return;

        if (comboBox.getCurrentState().equals(twoDimensionState)) {
            //serves no purpose during an animation
            if (timer != null && timer.isRunning())
                return;

            octaves++;

            if (octaves == maxOctaves)
                octaves = 1;

            _2DNoise = generate2DNoise(instanceSeed[0], octaves);
        } else {
            //serves no purpose during an animation
            if (timer != null && timer.isRunning())
                return;

            timeStep += 0.1;

            for (int y = 0 ; y < resolution ; y++) {
                for (int x = 0 ; x < resolution ; x++) {
                    // just to be safe
                    if (closed)
                        return;

                    double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, timeStep);
                    _3DNoise[x][y].setColor(generateGrayscaleColor(value));
                    _3DNoise[x][y].setX(x);
                    _3DNoise[x][y].setY(y);
                }
            }
        }

        //repaint
        noiseLabel.repaint();
    }

    /**
     * Generates perlin noise based on common algorithm implementation.
     *
     * @param fSeed    the seed value
     * @param nOctaves the number of iterations to perform the algorithm on
     * @return 2D perlin noise representation (values are between 0 and 1)
     */
    private static float[] generate2DNoise(float[] fSeed, int nOctaves) {
        float[] ret = new float[resolution];

        for (int x = 0 ; x < resolution ; x++) {
            float fNoise = 0.0f;
            float fScale = 1.0f;
            float fScaleAcc = 0.0f;

            for (int o = 0 ; o < nOctaves ; o++) {
                int nPitch = resolution >> o; //assuming octaves is a power of 2
                int nSample1 = (x / nPitch) * nPitch;
                int nSample2 = (nSample1 + nPitch) % resolution;

                float fBlend = (float) (x - nSample1) / (float) nPitch;
                float fSample = (1.0f - fBlend) * fSeed[nSample1] + fBlend * fSeed[nSample2];
                fNoise += fSample * fScale;
                fScaleAcc += fScale;
                fScale = fScale / 2.0f;
            }

            ret[x] = fNoise / fScaleAcc;
        }

        return ret;
    }

    /**
     * The function for the timer to invoke when noise animation is enabled.
     */
    private static final ActionListener animationAction = evt -> {
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
        if (comboBox.getCurrentState().equals(twoDimensionState)) {
            _2DNoise = generate2DNoise(instanceSeed[0], octaves);
        } else {
            timeStep += 0.1;

            for (int y = 0 ; y < resolution ; y++) {
                for (int x = 0 ; x < resolution ; x++) {
                    // just to be safe
                    if (closed)
                        return;

                    double value = noise.eval(x / FEATURE_SIZE, y / FEATURE_SIZE, timeStep);
                    _3DNoise[x][y].setColor(generateGrayscaleColor(value));
                    _3DNoise[x][y].setX(x);
                    _3DNoise[x][y].setY(y);
                }
            }
        }

        //repaint
        noiseLabel.repaint();
    };

    /**
     * Generates a grayscale color from the double value.
     *
     * @param value the value to map to a grayscale color
     * @return a grayscale color unique to the double provided
     */
    private static Color generateGrayscaleColor(double value) {
        return new Color(0x010101 * (int) ((value + 1) * 127.5));
    }

    /**
     * Locks the perlin UI.
     */
    private static void lockUI() {
        animateCheckBox.setEnabled(false);
        nextIteration.setEnabled(false);
        comboBox.setEnabled(false);
        featureSlider.setEnabled(false);
    }

    /**
     * Unlocks the perlin UI.
     */
    private static void unlockUI() {
        animateCheckBox.setEnabled(true);
        nextIteration.setEnabled(true);
        comboBox.setEnabled(true);
        featureSlider.setEnabled(true);
    }
}