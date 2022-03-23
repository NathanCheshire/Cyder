package cyder.widgets;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.SliderShape;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.ui.objects.GridNode;
import cyder.ui.objects.SwitcherState;
import cyder.utilities.FileUtil;
import cyder.utilities.GetterUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.objects.GetterBuilder;
import cyder.widgets.objects.ConwayState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Conway's game of life visualizer.
 */
public class GameOfLifeWidget {
    /**
     * The game of life frame.
     */
    private static CyderFrame conwayFrame;

    /**
     * The top-level grid used to display the current generation.
     */
    private static CyderGrid conwayGrid;

    /**
     * The button to reset the grid to the state it was in before the simulation was started.
     */
    private static CyderButton resetButton;

    /**
     * The button to begin/stop (pause) the simulation.
     */
    private static CyderButton simulateStopButton;

    /**
     * The button to clear the board and reset values
     */
    private static CyderButton clearButton;

    /**
     * The button to save the current grid state to a json object.
     */
    private static CyderButton saveButton;

    /**
     * The button to load a grid state from a json object.
     */
    private static CyderButton loadButton;

    /**
     * The checkbox to detect oscillations when the simulation devolves to two state swaps.
     */
    private static CyderCheckbox detectOscillationsCheckbox;

    /**
     * The checkbox to determine wheter to draw grid lines.
     */
    private static CyderCheckbox drawGridLinesCheckbox;

    /**
     * The label to let the user know what the speed slider controls.
     */
    private static CyderLabel speedLabel;

    /**
     * The switcher to cycle through the built-in presets.
     */
    private static CyderSwitcher presetSwitcher;

    /**
     * The slider to speed up/slow down the simulation.
     */
    private static JSlider iterationsPerSecondSlider;

    // end ui components

    /**
     * Whether the simulation is running
     */
    private static boolean simulationRunning;

    /**
     * The minimum allowable iterations per second.
     */
    private static final int MIN_ITERATIONS_PER_SECOND = 1;

    /**
     * The initial and default iterations per second.
     */
    private static final int DEFAULT_ITERATIONS_PER_SECOND = 45;

    /**
     * The number of iterations to compute per second.
     */
    private static int iterationsPerSecond = DEFAULT_ITERATIONS_PER_SECOND;

    /**
     * The maximum number of iterations per second.
     */
    private static final int MAX_ITERATIONS_PER_SECOND = 50;

    /**
     * The current generation the simulation is on.
     */
    private static int generation;

    /**
     * The current population of the current state.
     */
    private static int population;

    /**
     * The maximum population encountered for this simulation.
     */
    private static int maxPopulation;

    /**
     * The generation corresponding to the maximum population.
     */
    private static int correspondingGeneration;

    /**
     * The label to display which generation the simulation is on.
     */
    private static CyderLabel currentGenerationlabel;

    /**
     * The label to display the population for the current generation.
     */
    private static CyderLabel currentPopulationLabel;

    /**
     * The label to display the maximum population.
     */
    private static CyderLabel maxPopulationLabel;

    /**
     * The label to display the generation for the maximum population.
     */
    private static CyderLabel correspondingGenerationLabel;

    /**
     * Whether to detect oscillations.
     */
    private static boolean detectOscillations;

    /**
     * The state the grid was in before the user last pressed start.
     */
    private static LinkedList<GridNode> beforeStartingState;

    /**
     * The last state of the grid.
     */
    private static LinkedList<GridNode> lastState = new LinkedList<>();

    /**
     * The second to last state of the grid.
     */
    private static LinkedList<GridNode> secondTolastState = new LinkedList<>();

    /**
     * Suppress default constructor.
     */
    private GameOfLifeWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @SuppressCyderInspections(values = "WidgetInspection")
    @Widget(triggers = {"conway","conways","game of life"}, description = "Conway's game of life visualizer")
    public static void showGUI() {
        if (conwayFrame != null)
            conwayFrame.disposeIfActive();

        conwayFrame = new CyderFrame(600,820);
        conwayFrame.setTitle("Conway's Game of Life");

        currentPopulationLabel = new CyderLabel();
        currentPopulationLabel.setBounds(20, 20, 140, 40);
        conwayFrame.getContentPane().add(currentPopulationLabel);

        currentGenerationlabel = new CyderLabel();
        currentGenerationlabel.setBounds(20 + 140, 20, 140, 40);
        conwayFrame.getContentPane().add(currentGenerationlabel);

        maxPopulationLabel = new CyderLabel();
        maxPopulationLabel.setBounds(20 + 140 * 2, 20, 140, 40);
        conwayFrame.getContentPane().add(maxPopulationLabel);

        correspondingGenerationLabel = new CyderLabel();
        correspondingGenerationLabel.setBounds(20 + 140 * 3, 20, 140, 40);
        conwayFrame.getContentPane().add(correspondingGenerationLabel);

        // init label text
        updateLabels();

        conwayGrid = new CyderGrid(50, 550);
        conwayGrid.setBounds(25, 25 + CyderDragLabel.DEFAULT_HEIGHT, 550, 550);
        conwayGrid.setMinNodes(50);
        conwayGrid.setMaxNodes(150);
        conwayGrid.setDrawGridLines(false);
        conwayGrid.setDrawExtendedBorder(true);
        conwayGrid.setBackground(CyderColors.vanila);
        conwayGrid.setResizable(true);
        conwayGrid.setSmoothScrolling(true);
        conwayGrid.installClickAndDragPlacer();
        conwayFrame.getContentPane().add(conwayGrid);

        resetButton = new CyderButton("Reset");
        resetButton.setBounds(25 + 15, conwayGrid.getY() + conwayGrid.getHeight() + 10, 160, 40);
        conwayFrame.getContentPane().add(resetButton);
        resetButton.addActionListener(e -> resetToPreviousState());

        simulateStopButton = new CyderButton("Simulate");
        simulateStopButton.setBounds(25 + 15 + 160 + 20,
                conwayGrid.getY() + conwayGrid.getHeight() + 10, 160, 40);
        conwayFrame.getContentPane().add(simulateStopButton);
        simulateStopButton.addActionListener(e -> {
            if (simulationRunning) {
                stop();
            } else {
                if (conwayGrid.getNodeCount() > 0) {
                    simulationRunning = true;
                    simulateStopButton.setText("Stop");
                    conwayGrid.uninstallClickAndDragPLacer();
                    start();
                } else {
                    conwayFrame.notify("Place at least one node");
                }
            }
        });

        clearButton = new CyderButton("Clear");
        clearButton.setBounds(25 + 15 + 160 + 20 + 160 + 20,
                conwayGrid.getY() + conwayGrid.getHeight() + 10, 160, 40);
        conwayFrame.getContentPane().add(clearButton);
        clearButton.addActionListener(e -> resetSimulation());

        ArrayList<SwitcherState> states = new ArrayList<>();
        states.add(new SwitcherState("Gliders"));
        states.add(new SwitcherState("Copper"));

        presetSwitcher = new CyderSwitcher(160, 40, states, states.get(0));
        presetSwitcher.getIterationButton().addActionListener(e -> {
            SwitcherState nextState = presetSwitcher.getNextState();

            if (nextState.equals(states.get(0))) {
                // todo load Gliders.json (create and save)
                //fromJson(GLIDERS);
            } else {
                //fromJson(COPPERHEAD);
                // todo load Copperhead.json (create and save)
            }
        });
        presetSwitcher.setBounds(25 + 15,
                conwayGrid.getY() + conwayGrid.getHeight() + 10 + 50, 160, 40);
        conwayFrame.getContentPane().add(presetSwitcher);

        saveButton = new CyderButton("Save");
        saveButton.setBounds(25 + 15 + 160 + 20,
                conwayGrid.getY() + conwayGrid.getHeight() + 10 + 50, 160, 40);
        conwayFrame.getContentPane().add(saveButton);
        saveButton.addActionListener(e -> toFile());

        loadButton = new CyderButton("Load");
        loadButton.setBounds(25 + 15 + 160 + 20 + 160 + 20,
                conwayGrid.getY() + conwayGrid.getHeight() + 10 + 50, 160, 40);
        conwayFrame.getContentPane().add(loadButton);
        loadButton.addActionListener(e -> {
           CyderThreadRunner.submit(() -> {
               GetterBuilder builder = new GetterBuilder("Load state");
               builder.setRelativeTo(conwayFrame);
               File loadFile = GetterUtil.getInstance().getFile(builder);

               if (loadFile != null && loadFile.exists()
                       && FileUtil.validateExtension(loadFile, ".json")) {
                   fromJson(loadFile);
               }
           }, "Conway State Loader");
        });

        CyderLabel detectOscillationsLabel = new CyderLabel("Oscillations");
        detectOscillationsLabel.setBounds(15,
                conwayGrid.getY() + conwayGrid.getHeight() + 100, 100, 40);
        conwayFrame.getContentPane().add(detectOscillationsLabel);

        detectOscillationsCheckbox = new CyderCheckbox(true);
        detectOscillationsCheckbox.setBounds(25 + 15,
                conwayGrid.getY() + conwayGrid.getHeight() + 10 + 50 + 50 + 10 + 20, 50, 50);
        conwayFrame.getContentPane().add(detectOscillationsCheckbox);
        detectOscillationsCheckbox.setToolTipText("Detect Oscillations");

        CyderLabel gridLinesLabel = new CyderLabel("Grid Lines");
        gridLinesLabel.setBounds(15 + 85,
                conwayGrid.getY() + conwayGrid.getHeight() + 100, 100, 40);
        conwayFrame.getContentPane().add(gridLinesLabel);

        drawGridLinesCheckbox = new CyderCheckbox(false);
        drawGridLinesCheckbox.setBounds(25 + 15 + 50 + 10 + 20,
                conwayGrid.getY() + conwayGrid.getHeight() + 10 + 50 + 50 + 10 + 20, 50, 50);
        conwayFrame.getContentPane().add(drawGridLinesCheckbox);
        drawGridLinesCheckbox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (drawGridLinesCheckbox.isSelected()) {
                    conwayGrid.setDrawGridLines(true);
                    conwayGrid.repaint();
                } else {
                    conwayGrid.setDrawGridLines(false);
                    conwayGrid.repaint();
                }
            }
        });
        drawGridLinesCheckbox.setToolTipText("Draw Grid Lines");

        iterationsPerSecondSlider = new JSlider(JSlider.HORIZONTAL, MIN_ITERATIONS_PER_SECOND,
                MAX_ITERATIONS_PER_SECOND, DEFAULT_ITERATIONS_PER_SECOND);
        CyderSliderUI UI = new CyderSliderUI(iterationsPerSecondSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(SliderShape.RECT);
        UI.setFillColor(Color.black);
        UI.setOutlineColor(CyderColors.navy);
        UI.setNewValColor(CyderColors.regularBlue);
        UI.setOldValColor(CyderColors.regularPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        iterationsPerSecondSlider.setUI(UI);
        iterationsPerSecondSlider.setBounds(25 + 15 + 50 + 10 + 100,
                conwayGrid.getY() + conwayGrid.getHeight() + 5 + 50 + 50 + 20 + 20, 350, 40);
        iterationsPerSecondSlider.setPaintTicks(false);
        iterationsPerSecondSlider.setPaintLabels(false);
        iterationsPerSecondSlider.setVisible(true);
        iterationsPerSecondSlider.addChangeListener(e -> iterationsPerSecond = iterationsPerSecondSlider.getValue());
        iterationsPerSecondSlider.setOpaque(false);
        iterationsPerSecondSlider.setToolTipText("Iterations per second");
        iterationsPerSecondSlider.setFocusable(false);
        iterationsPerSecondSlider.repaint();
        conwayFrame.getContentPane().add(iterationsPerSecondSlider);

        conwayFrame.setLocationRelativeTo(CyderShare.getDominantFrame());
        conwayFrame.setVisible(true);
    }

    /**
     * Resets the simulation and all values back to their default.
     */
    private static void resetSimulation() {
        stop();

        iterationsPerSecond = DEFAULT_ITERATIONS_PER_SECOND;

        conwayGrid.setNodeDimensionLength(50);
        conwayGrid.clearGrid();
        conwayGrid.repaint();

        detectOscillationsCheckbox.setSelected();
        iterationsPerSecondSlider.setValue(DEFAULT_ITERATIONS_PER_SECOND);
        iterationsPerSecond = DEFAULT_ITERATIONS_PER_SECOND;

        beforeStartingState = null;

        resetStats();
    }

    /**
     * Resets the population/generation statistics and labels.
     */
    private static void resetStats() {
        generation = 0;
        population = 0;
        maxPopulation = 0;
        correspondingGeneration = 0;

        updateLabels();
    }

    /**
     * Updates the statistic labels based on the currently set values.
     */
    public static void updateLabels() {
        currentGenerationlabel.setText("Generation: " + generation);
        currentPopulationLabel.setText("Population: " + population);
        maxPopulationLabel.setText("Max Pop: " + maxPopulation);
        correspondingGenerationLabel.setText("Gen: " + correspondingGeneration);
    }

    /**
     * Sets the grid to the state it was in before beginning the simulation.
     */
    private static void resetToPreviousState() {
        if (beforeStartingState == null)
            return;

        stop();
        conwayGrid.setGridState(beforeStartingState);
        conwayGrid.repaint();

        resetStats();
        population = beforeStartingState.size();
        updateLabels();
    }

    /**
     * Performs any stopping actions needed to properly stop the simualtion.
     */
    private static void stop() {
        simulationRunning = false;
        simulateStopButton.setText("Simulate");
        conwayGrid.installClickAndDragPlacer();
    }

    /**
     * Starts the simulation.
     */
    private static void start() {
        beforeStartingState = new LinkedList<>(conwayGrid.getGridNodes());

        CyderThreadRunner.submit(() -> {
            while (simulationRunning) {
                try {
                    LinkedList<GridNode> nextState = new LinkedList<>();

                    int[][] nextGen = nextGeneration(cyderGridToConwayGrid(lastState));
                    for (int i = 0 ; i < nextGen.length ; i++) {
                        for (int j = 0 ; j < nextGen[0].length ; j++) {
                            if (nextGen[i][j] == 1) {
                                nextState.add(new GridNode(i, j));
                            }
                        }
                    }

                    // todo detect oscillations broken still for some reason
                    if (detectOscillations && nextState.equals(secondTolastState)) {
                        conwayFrame.notify("Detected Oscillation");
                        stop();
                        return;
                    } else if (nextState.equals(lastState)) {
                        if (nextState.isEmpty()) {
                            conwayFrame.notify("Simulation ended with total " +
                                    "elimination at generation: " + generation);
                        } else {
                            conwayFrame.notify("Simulation ended at generation: " + generation);
                        }

                        stop();
                        return;
                    }

                    // advance second to last state
                    secondTolastState = new LinkedList<>(lastState);

                    // advance last state
                    lastState = conwayGrid.getGridNodes();

                    // set new state
                    conwayGrid.setGridNodes(nextState);
                    conwayGrid.repaint();

                    generation++;
                    population = nextState.size();

                    if (population > maxPopulation) {
                        maxPopulation = population;
                        correspondingGeneration = generation;
                    }

                    updateLabels();

                    // timeout based on current iterations per second
                    Thread.sleep(1000 / iterationsPerSecond);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        },"Conway Simulator");
    }

    private static final Gson gson = new Gson();

    /**
     * Loads the conway state from the provided json file and sets the current grid state to it.
     *
     * @param jsonFile the json file to load the state from
     */
    private static void fromJson(File jsonFile) {
        Preconditions.checkNotNull(jsonFile);
        Preconditions.checkArgument(jsonFile.exists());

        try {
            Reader reader = new FileReader(jsonFile);
            ConwayState loadState = gson.fromJson(reader, ConwayState.class);
            reader.close();

            resetSimulation();

            conwayGrid.setNodeDimensionLength(loadState.getGridSize());

            for (Point p : loadState.getNodes()) {
                conwayGrid.addNode(new GridNode((int) p.getX(), (int) p.getY()));
            }

            conwayFrame.notify("Loaded state: " + loadState.getName());
            beforeStartingState = new LinkedList<>(conwayGrid.getGridNodes());

            resetStats();
            population = loadState.getNodes().size();
            updateLabels();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            conwayFrame.notify("Could not parse json as a valid ConwayState object");
        }
    }

    /**
     * Saves the current grid state to a json which can be loaded.
     */
    private static void toFile() {
        CyderThreadRunner.submit(() -> {
            if (conwayGrid.getNodeCount() == 0) {
                conwayFrame.notify("Place at least one node");
                return;
            }

            GetterBuilder builder = new GetterBuilder("Save name");
            builder.setRelativeTo(conwayFrame);
            builder.setFieldTooltip("A valid filename");
            builder.setSubmitButtonText("Save Conway State");
            String saveName = GetterUtil.getInstance().getString(builder);

            String filename = saveName + ".json";

            if (OSUtil.isValidFilename(filename)) {
                File saveFile = OSUtil.createFileInUserSpace(filename);

                LinkedList<Point> points = new LinkedList<>();

                for (GridNode node : conwayGrid.getGridNodes()) {
                    points.add(new Point(node.getX(), node.getY()));
                }

                ConwayState state = new ConwayState(saveName,
                        conwayGrid.getNodeDimensionLength(), points);

                try {
                    FileWriter writer = new FileWriter(saveFile);
                    gson.toJson(state, writer);
                    writer.close();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    conwayFrame.notify("Save state failed");
                }
            } else {
                conwayFrame.notify("Invalid save name");
            }
        }, "Conway State Saver");
    }

    /**
     * Converts the CyderGrid nodes to the 2D integer array
     * needed to compute the next Conway iteration.
     *
     * @param nodes the list of cydergrid nodes
     * @return the 2D array consisting of 1s and 0s
     */
    private static int[][] cyderGridToConwayGrid(LinkedList<GridNode> nodes) {
        int dim = conwayGrid.getNodeDimensionLength();
        int[][] ret = new int[dim][dim];

        for (GridNode node : conwayGrid.getGridNodes()) {
            ret[node.getX()][node.getY()] = 1;
        }

        return ret;
    }

    //private static ArrayListz

    /**
     * Computes the next generation based on the current generation.
     *
     * @param currentGeneration the current generation
     * @return the next generation
     */
    private static int[][] nextGeneration(int[][] currentGeneration) {
        if (currentGeneration == null || currentGeneration.length < 3 || currentGeneration[0].length < 3)
            throw new IllegalArgumentException("Null or invalid board");

        int[][] ret = new int[currentGeneration.length][currentGeneration[0].length];

        for (int l = 1 ; l < currentGeneration.length - 1 ; l++) {
            for (int m = 1 ; m < currentGeneration[0].length - 1 ; m++) {
                int aliveNeighbours = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        aliveNeighbours += currentGeneration[l + i][m + j];
                    }
                }

                aliveNeighbours -= currentGeneration[l][m];

                if ((currentGeneration[l][m] == 1) && (aliveNeighbours < 2)) {
                    ret[l][m] = 0;
                } else if ((currentGeneration[l][m] == 1) && (aliveNeighbours > 3)) {
                    ret[l][m] = 0;
                } else if ((currentGeneration[l][m] == 0) && (aliveNeighbours == 3)) {
                    ret[l][m] = 1;
                } else {
                    ret[l][m] = currentGeneration[l][m];
                }
            }
        }

        return ret;
    }
}
