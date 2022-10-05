package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.layouts.CyderGridLayout;
import cyder.layouts.CyderPartitionedLayout;
import cyder.math.NumberUtil;
import cyder.ui.CyderGrid;
import cyder.ui.CyderPanel;
import cyder.ui.button.CyderButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.selection.CyderSwitch;
import cyder.ui.slider.CyderSliderUi;
import cyder.utils.SimplexNoiseUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
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
     * The button which starts the animation for the noise.
     */
    private static CyderButton animateButton;

    /**
     * The button which iterates to the next iteration of the noise.
     */
    private static CyderButton stepButton;

    /**
     * The button to regenerate the noise.
     */
    private static CyderButton regenerateButton;

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
    private static double featureSize = DEFAULT_FEATURE_SIZE;

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
     * The dimension switch.
     */
    private static CyderSwitch dimensionSwitch;

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
    private static CyderGrid.GridNode[][] noise3D;

    /**
     * The array to store the perlin noise.
     */
    private static float[] noise2D;

    /**
     * The random object used for randomizing the noise seeds.
     */
    private static final Random random = new Random();

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
     * The frame title string.
     */
    private static final String PERLIN = "Perlin Noise";

    /**
     * The stroke for drawing on the noise label.
     */
    private static final BasicStroke stroke = new BasicStroke(2);

    /**
     * The text for the step button.
     */
    private static final String STEP = "Step";

    /**
     * The time step increment.
     */
    private static final double timeStepIncrement = 0.1;

    /**
     * The stop string.
     */
    private static final String STOP = "Stop";

    /**
     * Half of the limit for 8-bit color values.
     */
    private static final float halfEightBitColorLimit = 127.5f;

    /**
     * The feature slider tooltip text.
     */
    private static final String THREE_D_FEATURE_SIZE = "3D Feature Size";

    /**
     * The animate string.
     */
    private static final String ANIMATE = "Animate";

    /**
     * The tooltip for the speed slider.
     */
    private static final String ANIMATE_TIMEOUT = "Animation Timeout";

    /**
     * The height of the sliders.
     */
    private static final int sliderHeight = 40;

    /**
     * The maximum feature slider value.
     */
    private static final int maxFeatureSliderValue = 1000;

    /**
     * The minimum feature slider value.
     */
    private static final int minFeatureSliderValue = 0;

    /**
     * The default feature slider value.
     */
    private static final int defaultFeatureSliderValue = (maxFeatureSliderValue - minFeatureSliderValue) / 2;

    /**
     * The grayscale multiplier value.
     */
    private static final int grayscaleMultiplier = 0x010101;

    /**
     * The length of the top line for 2D noise.
     */
    private static final int topLineLength = 2;

    /**
     * The length of the grass for 2D noise.
     */
    private static final int grassLength = 10;

    /**
     * The length of the dirt for 2D noise.
     */
    private static final int dirtLength = 20;

    /**
     * Shows the perlin noise widget.
     */
    @Widget(triggers = {"perlin", "noise"}, description = "Perlin noise visualizer/open simplex noise visualizer")
    public static void showGui() {
        UiUtil.closeIfOpen(perlinFrame);

        closed = false;
        timeStep = 0;
        octaves = 1;

        instanceSeed = new float[resolution][resolution];
        generateNewSeed();

        initializeNoise();

        timer = new Timer(speedSliderMaxValue - speedSliderValue, animationAction);

        int framePadding = 25;
        int frameWidth = resolution + 2 * framePadding;
        int interactionComponentsHeight = 240;
        int frameHeight = resolution + 2 * framePadding + CyderDragLabel.DEFAULT_HEIGHT + interactionComponentsHeight;
        perlinFrame = new CyderFrame(frameWidth, frameHeight);
        perlinFrame.setTitle(PERLIN);
        perlinFrame.addPreCloseAction(PerlinWidget::preCloseActions);

        noiseLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                if (closed) return;
                super.paint(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.darkGray);
                g2d.setStroke(stroke);

                boolean twoDimensionalNoise = dimensionSwitch.getState() == CyderSwitch.State.OFF;
                if (twoDimensionalNoise) {
                    draw2DNoise(g2d);
                } else {
                    draw3DNoise(g2d);
                }
            }
        };
        noiseLabel.setBounds(5, 5, resolution, resolution);

        JLabel noiseParentLabel = new JLabel();
        noiseParentLabel.setSize(resolution + 10, resolution + 10);
        noiseParentLabel.setBorder(new LineBorder(CyderColors.navy, 5));
        noiseParentLabel.add(noiseLabel);

        int frameComponentXPadding = 25;

        CyderLabel animateLabel = new CyderLabel(ANIMATE);
        animateLabel.setSize(100, 20);

        animateButton = new CyderButton(ANIMATE);
        animateButton.addActionListener(e -> generate());
        animateButton.setToolTipText("Animate Perlin Noise");
        animateButton.setSize(180, 40);

        stepButton = new CyderButton(STEP);
        stepButton.addActionListener(e -> nextIteration());
        stepButton.setToolTipText("Increments the octave and displayed the revalidated noise");
        stepButton.setSize(180, 40);

        regenerateButton = new CyderButton("Regenerate");
        regenerateButton.addActionListener(e -> regenerateButtonAction());
        regenerateButton.setToolTipText("Regenerates the noise");
        regenerateButton.setSize(180, 40);

        speedSlider = new JSlider(JSlider.HORIZONTAL, speedSliderMinValue, speedSliderMaxValue, speedSliderValue);

        CyderSliderUi speedSliderUi = new CyderSliderUi(speedSlider);
        speedSliderUi.setThumbRadius(25);
        speedSliderUi.setThumbShape(CyderSliderUi.ThumbShape.CIRCLE);
        speedSliderUi.setThumbFillColor(Color.black);
        speedSliderUi.setThumbOutlineColor(CyderColors.navy);
        speedSliderUi.setRightThumbColor(CyderColors.regularBlue);
        speedSliderUi.setLeftThumbColor(CyderColors.regularPink);
        speedSliderUi.setTrackStroke(new BasicStroke(3.0f));

        speedSlider.setUI(speedSliderUi);
        speedSlider.setSize(resolution - 2 * frameComponentXPadding, sliderHeight);
        speedSlider.setPaintTicks(false);
        speedSlider.setPaintLabels(false);
        speedSlider.setVisible(true);
        speedSlider.setValue(speedSliderValue);
        speedSlider.addChangeListener(e -> speedSliderChangeAction());
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText(ANIMATE_TIMEOUT);
        speedSlider.setFocusable(false);
        speedSlider.repaint();

        featureSlider = new JSlider(JSlider.HORIZONTAL, minFeatureSliderValue, maxFeatureSliderValue,
                defaultFeatureSliderValue);

        CyderSliderUi featureSliderUi = new CyderSliderUi(featureSlider);
        featureSliderUi.setThumbRadius(25);
        featureSliderUi.setThumbShape(CyderSliderUi.ThumbShape.CIRCLE);
        featureSliderUi.setThumbFillColor(Color.black);
        featureSliderUi.setThumbOutlineColor(CyderColors.navy);
        featureSliderUi.setRightThumbColor(CyderColors.regularBlue);
        featureSliderUi.setLeftThumbColor(CyderColors.regularPink);
        featureSliderUi.setTrackStroke(new BasicStroke(3.0f));

        featureSlider.setUI(featureSliderUi);
        featureSlider.setSize(resolution - 2 * frameComponentXPadding, sliderHeight);
        featureSlider.setPaintTicks(false);
        featureSlider.setPaintLabels(false);
        featureSlider.setVisible(true);
        featureSlider.setValue(defaultFeatureSliderValue);
        featureSlider.addChangeListener(e -> featureSliderChangeAction());
        featureSlider.setOpaque(false);
        featureSlider.setToolTipText(THREE_D_FEATURE_SIZE);
        featureSlider.setFocusable(false);
        featureSlider.repaint();

        dimensionSwitch = new CyderSwitch(180, 50, CyderSwitch.State.OFF);
        dimensionSwitch.setSize(180, 50);
        dimensionSwitch.setState(CyderSwitch.State.OFF);
        dimensionSwitch.repaint();
        dimensionSwitch.setOffText("2D");
        dimensionSwitch.setOnText("3D");

        CyderPartitionedLayout partitionedLayout = new CyderPartitionedLayout();
        partitionedLayout.spacer(2);
        partitionedLayout.addComponent(noiseParentLabel, 70);

        CyderLabel speedSliderLabel = new CyderLabel("Speed");
        speedSliderLabel.setSize(200, 40);
        CyderLabel featureSliderLabel = new CyderLabel("Feature Size");
        featureSliderLabel.setSize(200, 40);

        partitionedLayout.addComponent(speedSliderLabel, 4);
        partitionedLayout.addComponent(speedSlider, 2);
        partitionedLayout.addComponent(featureSliderLabel, 4);
        partitionedLayout.addComponent(featureSlider, 2);

        CyderGridLayout gridLayout = new CyderGridLayout(3, 1);
        gridLayout.addComponent(animateButton);
        gridLayout.addComponent(stepButton);
        gridLayout.addComponent(regenerateButton);
        CyderPanel gridPanel = new CyderPanel(gridLayout);
        gridPanel.setSize(600, 40);
        partitionedLayout.addComponent(gridPanel, 5);

        partitionedLayout.spacer(4);
        CyderGridLayout bottomGrid = new CyderGridLayout(3, 1);
        gridLayout.addComponent(new JLabel());
        bottomGrid.addComponent(dimensionSwitch);
        bottomGrid.addComponent(new JLabel());
        CyderPanel bottomGridPanel = new CyderPanel(bottomGrid);
        bottomGridPanel.setSize(600, 50);
        partitionedLayout.addComponent(bottomGridPanel, 3);

        perlinFrame.setCyderLayout(partitionedLayout);
        perlinFrame.finalizeAndShow();
    }

    private static void regenerateButtonAction() {
        if (dimensionSwitch.getState().equals(CyderSwitch.State.OFF)) {
            if (timer.isRunning()) {
                timer.stop();
                unlockUI();
            }

            generateNewSeed();

            //reset octaves
            octaves = 1;

            //new noise
            noise2D = generate2DNoise(instanceSeed[0], octaves);
        } else {
            //reset timeStep
            timeStep = 0;

            noise = new SimplexNoiseUtil(NumberUtil.randInt(1000));
            for (int y = 0 ; y < resolution ; y++) {
                for (int x = 0 ; x < resolution ; x++) {
                    double value = noise.eval(x / featureSize, y / featureSize, timeStep);
                    noise3D[x][y].setColor(generateGrayscaleColor(value));
                    noise3D[x][y].setX(x);
                    noise3D[x][y].setY(y);
                }
            }
        }

        noiseLabel.repaint();
    }

    /**
     * Initializes 2D and 3D noise.
     */
    @ForReadability
    private static void initializeNoise() {
        noise2D = new float[resolution];
        noise2D = generate2DNoise(instanceSeed[0], octaves);

        noise3D = new CyderGrid.GridNode[resolution][resolution];
        for (int x = 0 ; x < resolution ; x++) {
            for (int y = 0 ; y < resolution ; y++) {
                noise3D[x][y] = new CyderGrid.GridNode(x, y);
            }
        }
    }

    /**
     * The actions to invoke on a feature slider value change.
     */
    @ForReadability
    private static void featureSliderChangeAction() {
        featureSize = (featureSlider.getValue() / (float) maxFeatureSliderValue)
                * (MAXIMUM_FEATURE_SIZE - MINIMUM_FEATURE_SIZE) + MINIMUM_FEATURE_SIZE;

        if (dimensionSwitch.getState().equals(CyderSwitch.State.ON) && !timer.isRunning()) {
            for (int y = 0 ; y < resolution ; y++) {
                for (int x = 0 ; x < resolution ; x++) {
                    double value = noise.eval(x / featureSize, y / featureSize, timeStep);
                    Color color = generateGrayscaleColor(value);

                    CyderGrid.GridNode ref = noise3D[x][y];

                    ref.setColor(color);
                    ref.setX(x);
                    ref.setY(y);
                }
            }

            noiseLabel.repaint();
        }
    }

    /**
     * The pre close action to invoke when the frame's dispose function has been called.
     */
    @ForReadability
    private static void preCloseActions() {
        noise2D = null;
        noise3D = null;
        closed = true;

        stopTimerIfRunning();
    }

    /**
     * The actions to invoke on a speed slider value change.
     */
    @ForReadability
    private static void speedSliderChangeAction() {
        speedSliderValue = speedSlider.getValue();
        timer.setDelay(speedSliderMaxValue - speedSliderValue);
    }

    /**
     * Draws two dimension noise on the noise label.
     *
     * @param g2d the 2D graphics object
     */
    @ForReadability
    private static void draw2DNoise(Graphics2D g2d) {
        int width = 2;

        for (int x = 0 ; x < resolution - 1 ; x++) {
            float y = (float) ((noise2D[x] * resolution / 2.0) + resolution / 2.0);
            int minY = (int) y;
            int lenDown = 0;

            // Draw top line
            g2d.setColor(Color.black);
            g2d.fillRect(x, minY, width, topLineLength);
            lenDown += topLineLength;

            // Draw grass
            g2d.setColor(CyderColors.regularGreen);
            g2d.fillRect(x, minY + lenDown, width, grassLength);
            lenDown += grassLength;

            // Draw dirt
            g2d.setColor(CyderColors.brownDirt);
            g2d.fillRect(x, minY + lenDown, width, dirtLength);
            lenDown += dirtLength;

            // Draw stone
            int stoneLength = resolution - (minY + lenDown);
            g2d.setColor(Color.darkGray);
            g2d.fillRect(x, minY + lenDown, width, stoneLength);
        }
    }

    /**
     * Draws three dimensional noise on the noise label.
     *
     * @param g2d the 2D graphics object
     */
    @ForReadability
    private static void draw3DNoise(Graphics2D g2d) {
        int len = 1;

        for (int i = 0 ; i < resolution ; i++) {
            for (int j = 0 ; j < resolution ; j++) {
                g2d.setColor(noise3D[i][j].getColor());
                g2d.fillRect(i, j, len, len);
            }
        }
    }

    /**
     * The actions to invoke when the combo box iteration button is clicked.
     */
    @ForReadability
    private static void comboBoxAction() {
        nextIteration();

        timeStep += timeStepIncrement;
        for (int y = 0 ; y < resolution ; y++) {
            for (int x = 0 ; x < resolution ; x++) {
                double value = noise.eval(x / featureSize, y / featureSize, timeStep);
                noise3D[x][y].setColor(generateGrayscaleColor(value));
                noise3D[x][y].setX(x);
                noise3D[x][y].setY(y);
            }
        }
        noiseLabel.repaint();
    }

    /**
     * Stops the timer if running.
     */
    @ForReadability
    private static void stopTimerIfRunning() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    /**
     * Generates new noise based on the current random seed.
     */
    private static void generate() {
        if (closed) return;

        if (timer.isRunning()) {
            timer.stop();
            animateButton.setText(ANIMATE);
            unlockUI();
        } else {
            lockUI();
            animateButton.setText(STOP);
            timer.start();
        }
    }

    /**
     * Generates the new iteration of noise from the current noise.
     */
    private static void nextIteration() {
        if (closed) return;

        if (dimensionSwitch.getState().equals(CyderSwitch.State.OFF)) {
            if (timer != null && timer.isRunning()) return;

            octaves++;
            if (octaves == maxOctaves) {
                octaves = 1;
            }

            noise2D = generate2DNoise(instanceSeed[0], octaves);
        } else {
            //serves no purpose during an animation
            if (timer != null && timer.isRunning()) return;

            timeStep += timeStepIncrement;

            for (int y = 0 ; y < resolution ; y++) {
                for (int x = 0 ; x < resolution ; x++) {
                    if (closed) return;

                    double value = noise.eval(x / featureSize, y / featureSize, timeStep);
                    noise3D[x][y].setColor(generateGrayscaleColor(value));
                    noise3D[x][y].setX(x);
                    noise3D[x][y].setY(y);
                }
            }
        }

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

            for (int octave = 0 ; octave < nOctaves ; octave++) {
                /*
                Assuming octave is a power of two

                assert (octave & (octave - 1)) == 0;
                 */
                int nPitch = resolution >> octave;
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
        if (closed) return;

        octaves++;

        if (octaves == maxOctaves) {
            octaves = 1;
            generateNewSeed();
        }

        if (dimensionSwitch.getState().equals(CyderSwitch.State.OFF)) {
            noise2D = generate2DNoise(instanceSeed[0], octaves);
        } else {
            timeStep += timeStepIncrement;

            for (int y = 0 ; y < resolution ; y++) {
                for (int x = 0 ; x < resolution ; x++) {
                    if (closed) return;

                    double value = noise.eval(x / featureSize, y / featureSize, timeStep);
                    noise3D[x][y].setColor(generateGrayscaleColor(value));
                    noise3D[x][y].setX(x);
                    noise3D[x][y].setY(y);
                }
            }
        }

        noiseLabel.repaint();
    };

    @ForReadability
    private static void generateNewSeed() {
        for (int i = 0 ; i < resolution ; i++) {
            for (int j = 0 ; j < resolution ; j++) {
                instanceSeed[i][j] = random.nextFloat();
            }
        }
    }

    /**
     * Generates a grayscale color from the double value.
     *
     * @param value the value to map to a grayscale color
     * @return a grayscale color unique to the double provided
     */
    private static Color generateGrayscaleColor(double value) {
        return new Color(grayscaleMultiplier * (int) ((value + 1) * halfEightBitColorLimit));
    }

    /**
     * Locks the perlin UI.
     */
    private static void lockUI() {
        regenerateButton.setEnabled(false);
        stepButton.setEnabled(false);
        dimensionSwitch.setEnabled(false);
        featureSlider.setEnabled(false);
    }

    /**
     * Unlocks the perlin UI.
     */
    private static void unlockUI() {
        regenerateButton.setEnabled(true);
        stepButton.setEnabled(true);
        dimensionSwitch.setEnabled(true);
        featureSlider.setEnabled(true);
    }
}