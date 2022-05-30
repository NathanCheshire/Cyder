package cyder.widgets;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import com.google.gson.Gson;
import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.builders.GetterBuilder;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.utilities.*;

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
@SuppressWarnings("unused")
@Vanilla
@CyderAuthor
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
     * The button to begin/stop (pause) the simulation.
     */
    private static CyderButton simulateStopButton;

    /**
     * The checkbox to detect oscillations when the simulation devolves to two state swaps.
     */
    private static CyderCheckbox detectOscillationsCheckbox;

    /**
     * The checkbox to determine whether to draw grid lines.
     */
    private static CyderCheckbox drawGridLinesCheckbox;

    /**
     * The combo box to cycle through the built-in presets.
     */
    private static CyderComboBox presetComboBox;

    /**
     * The slider to speed up/slow down the simulation.
     */
    private static JSlider iterationsPerSecondSlider;

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
    private static final int MAX_ITERATIONS_PER_SECOND = 100;

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
     * The first corresponding generation to achieve the current maximum population.
     */
    private static int firstCorrespondingGeneration;

    /**
     * The label to display which generation the simulation is on.
     */
    private static CyderLabel currentGenerationLabel;

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
     * The state the grid was in before the user last pressed start.
     */
    private static LinkedList<CyderGrid.GridNode> beforeStartingState;

    /**
     * The last state of the grid.
     */
    private static LinkedList<CyderGrid.GridNode> lastState = new LinkedList<>();

    /**
     * The conway states loaded from static/json/conway.
     */
    private static ArrayList<ConwayState> correspondingConwayStates;

    /**
     * The switcher states to cycle between the states loaded from static/json/conway.
     */
    private static ArrayList<CyderComboBox.CyderComboItem> cyderComboItems;

    /**
     * Gson object used for converting to/from json files to save conway states.
     */
    private static final Gson conwayGson = new Gson();

    /**
     * The minimum dimensional node length for the inner cyder grid.
     */
    private static final int MIN_NODES = 50;

    /**
     * Suppress default constructor.
     */
    private GameOfLifeWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = {"conway", "conways", "game of life"}, description = "Conway's game of life visualizer")
    public static void showGui() {
        if (conwayFrame != null)
            conwayFrame.disposeIfActive();

        conwayFrame = new CyderFrame(600, 860);
        conwayFrame.setTitle("Conway's Game of Life");

        currentPopulationLabel = new CyderLabel();
        currentPopulationLabel.setBounds(25, CyderDragLabel.DEFAULT_HEIGHT, 240, 30);
        conwayFrame.getContentPane().add(currentPopulationLabel);

        currentGenerationLabel = new CyderLabel();
        currentGenerationLabel.setBounds(25 + 240 + 20, CyderDragLabel.DEFAULT_HEIGHT, 240, 30);
        conwayFrame.getContentPane().add(currentGenerationLabel);

        maxPopulationLabel = new CyderLabel();
        maxPopulationLabel.setBounds(25, CyderDragLabel.DEFAULT_HEIGHT + 30, 240, 30);
        conwayFrame.getContentPane().add(maxPopulationLabel);

        correspondingGenerationLabel = new CyderLabel();
        correspondingGenerationLabel.setBounds(25 + 240 + 20,
                CyderDragLabel.DEFAULT_HEIGHT + 30, 240, 30);
        conwayFrame.getContentPane().add(correspondingGenerationLabel);

        conwayGrid = new CyderGrid(50, 550);
        conwayGrid.setBounds(25, CyderDragLabel.DEFAULT_HEIGHT + 30 * 2 + 10, 550, 550);
        conwayGrid.setMinNodes(MIN_NODES);
        conwayGrid.setMaxNodes(150);
        conwayGrid.setDrawGridLines(false);
        conwayGrid.setBackground(CyderColors.vanilla);
        conwayGrid.setResizable(true);
        conwayGrid.setSmoothScrolling(true);
        conwayGrid.installClickAndDragPlacer();
        conwayFrame.getContentPane().add(conwayGrid);
        conwayGrid.setSaveStates(false);

        CyderButton resetButton = new CyderButton("Reset");
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
                    conwayGrid.setResizable(false);
                    start();
                } else {
                    conwayFrame.notify("Place at least one node");
                }
            }
        });

        CyderButton clearButton = new CyderButton("Clear");
        clearButton.setBounds(25 + 15 + 160 + 20 + 160 + 20,
                conwayGrid.getY() + conwayGrid.getHeight() + 10, 160, 40);
        conwayFrame.getContentPane().add(clearButton);
        clearButton.addActionListener(e -> resetSimulation());

        loadConwayStates();

        presetComboBox = new CyderComboBox(160, 40, cyderComboItems, cyderComboItems.get(0));
        presetComboBox.getIterationButton().addActionListener(e -> {
            CyderComboBox.CyderComboItem nextState = presetComboBox.getNextState();

            for (int i = 0 ; i < cyderComboItems.size() ; i++) {
                if (cyderComboItems.get(i).equals(nextState)) {
                    beforeStartingState = new LinkedList<>();

                    for (Point point : correspondingConwayStates.get(i).nodes()) {
                        beforeStartingState.add(new CyderGrid.GridNode((int) point.getX(), (int) point.getY()));
                    }

                    conwayFrame.notify("Loaded state: " + correspondingConwayStates.get(i).name());
                    conwayGrid.setNodeDimensionLength(correspondingConwayStates.get(i).gridSize());

                    break;
                }
            }

            resetToPreviousState();
        });
        presetComboBox.setBounds(25 + 15,
                conwayGrid.getY() + conwayGrid.getHeight() + 10 + 50, 160, 40);
        conwayFrame.getContentPane().add(presetComboBox);

        CyderButton saveButton = new CyderButton("Save");
        saveButton.setBounds(25 + 15 + 160 + 20,
                conwayGrid.getY() + conwayGrid.getHeight() + 10 + 50, 160, 40);
        conwayFrame.getContentPane().add(saveButton);
        saveButton.addActionListener(e -> toFile());

        CyderButton loadButton = new CyderButton("Load");
        loadButton.setBounds(25 + 15 + 160 + 20 + 160 + 20,
                conwayGrid.getY() + conwayGrid.getHeight() + 10 + 50, 160, 40);
        conwayFrame.getContentPane().add(loadButton);
        loadButton.addActionListener(e -> CyderThreadRunner.submit(() -> {
            GetterBuilder builder = new GetterBuilder("Load state");
            builder.setRelativeTo(conwayFrame);
            File loadFile = GetterUtil.getInstance().getFile(builder);

            if (loadFile != null && loadFile.exists()
                    && FileUtil.validateExtension(loadFile, ".json")) {
                fromJson(loadFile);
            }
        }, "Conway State Loader"));

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
                conwayGrid.setDrawGridLines(drawGridLinesCheckbox.isSelected());
                conwayGrid.repaint();
            }
        });
        drawGridLinesCheckbox.setToolTipText("Draw Grid Lines");

        iterationsPerSecondSlider = new JSlider(JSlider.HORIZONTAL, MIN_ITERATIONS_PER_SECOND,
                MAX_ITERATIONS_PER_SECOND, DEFAULT_ITERATIONS_PER_SECOND);
        CyderSliderUI UI = new CyderSliderUI(iterationsPerSecondSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(CyderSliderUI.SliderShape.RECT);
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

        resetSimulation();
        conwayFrame.finalizeAndShow();
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
        firstCorrespondingGeneration = 0;

        updateLabels();
    }

    /**
     * Updates the statistic labels based on the currently set values.
     */
    public static void updateLabels() {
        currentGenerationLabel.setText("Generation: " + generation);
        currentPopulationLabel.setText("Population: " + population);
        maxPopulationLabel.setText("Max Population: " + maxPopulation);

        if (firstCorrespondingGeneration == 0 || firstCorrespondingGeneration == generation) {
            correspondingGenerationLabel.setText("Corr Gen: " + correspondingGeneration);
        } else {
            correspondingGenerationLabel.setText("Corr Gen: " + correspondingGeneration
                    + ", first: " + firstCorrespondingGeneration);
        }
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
     * Performs any stopping actions needed to properly stop the simulation.
     */
    private static void stop() {
        simulationRunning = false;
        simulateStopButton.setText("Simulate");
        conwayGrid.installClickAndDragPlacer();
        conwayGrid.setResizable(true);
    }

    /**
     * Starts the simulation.
     */
    private static void start() {
        beforeStartingState = new LinkedList<>(conwayGrid.getGridNodes());

        CyderThreadRunner.submit(() -> {
            while (simulationRunning) {
                try {
                    LinkedList<CyderGrid.GridNode> nextState = new LinkedList<>();

                    int[][] nextGen = nextGeneration(cyderGridToConwayGrid(
                            conwayGrid.getGridNodes(), conwayGrid.getNodeDimensionLength()));
                    for (int i = 0 ; i < nextGen.length ; i++) {
                        for (int j = 0 ; j < nextGen[0].length ; j++) {
                            if (nextGen[i][j] == 1) {
                                nextState.add(new CyderGrid.GridNode(i, j));
                            }
                        }
                    }

                    if (nextState.equals(conwayGrid.getGridNodes())) {
                        conwayFrame.revokeAllNotifications();
                        conwayFrame.notify("Simulation stabilized at generation: " + generation);
                        stop();
                        return;
                    } else if (detectOscillationsCheckbox.isSelected() && nextState.equals(lastState)) {
                        conwayFrame.revokeAllNotifications();
                        conwayFrame.notify("Detected oscillation at generation: " + generation);
                        stop();
                        return;
                    } else if (nextState.isEmpty()) {
                        conwayFrame.revokeAllNotifications();
                        conwayFrame.notify("Simulation ended with total " +
                                "elimination at generation: " + generation);
                        stop();
                        return;
                    }

                    // advance last state
                    lastState = new LinkedList<>(conwayGrid.getGridNodes());

                    // set new state
                    conwayGrid.setGridNodes(nextState);
                    conwayGrid.repaint();

                    generation++;
                    population = nextState.size();

                    if (population > maxPopulation) {
                        firstCorrespondingGeneration = generation;

                        maxPopulation = population;
                        correspondingGeneration = generation;
                    } else if (population == maxPopulation) {
                        correspondingGeneration = generation;
                    }

                    updateLabels();

                    // timeout based on current iterations per second
                    Thread.sleep(1000 / iterationsPerSecond);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        }, "Conway Simulator");
    }

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
            ConwayState loadState = conwayGson.fromJson(reader, ConwayState.class);
            reader.close();

            resetSimulation();

            conwayGrid.setNodeDimensionLength(loadState.gridSize());

            for (Point p : loadState.nodes()) {
                conwayGrid.addNode(new CyderGrid.GridNode((int) p.getX(), (int) p.getY()));
            }

            conwayFrame.notify("Loaded state: " + loadState.name());
            beforeStartingState = new LinkedList<>(conwayGrid.getGridNodes());

            resetStats();
            population = loadState.nodes().size();
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

            if (StringUtil.isNull(saveName))
                return;

            String filename = saveName + ".json";

            if (OSUtil.isValidFilename(filename)) {
                File saveFile = UserUtil.createFileInUserSpace(filename);

                LinkedList<Point> points = new LinkedList<>();

                for (CyderGrid.GridNode node : conwayGrid.getGridNodes()) {
                    points.add(new Point(node.getX(), node.getY()));
                }

                ConwayState state = new ConwayState(saveName,
                        conwayGrid.getNodeDimensionLength(), points);

                try {
                    FileWriter writer = new FileWriter(saveFile);
                    conwayGson.toJson(state, writer);
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
     * @param nodes the list of cyder grid nodes
     * @param len   the dimensional length of the grid
     * @return the 2D array consisting of 1s and 0s
     */
    private static int[][] cyderGridToConwayGrid(LinkedList<CyderGrid.GridNode> nodes, int len) {
        int[][] ret = new int[len][len];

        for (CyderGrid.GridNode node : nodes) {
            ret[node.getX()][node.getY()] = 1;
        }

        return ret;
    }

    /**
     * Computes the next generation based on the current generation.
     *
     * @param currentGeneration the current generation
     * @return the next generation
     */
    private static int[][] nextGeneration(int[][] currentGeneration) {
        Preconditions.checkNotNull(currentGeneration);
        Preconditions.checkArgument(currentGeneration.length >= MIN_NODES);
        Preconditions.checkArgument(currentGeneration[0].length >= MIN_NODES);

        int[][] ret = new int[currentGeneration.length][currentGeneration[0].length];

        for (int l = 1 ; l < currentGeneration.length - 1 ; l++) {
            for (int m = 1 ; m < currentGeneration[0].length - 1 ; m++) {
                int aliveNeighbours = 0;
                for (int i = -1 ; i <= 1 ; i++) {
                    for (int j = -1 ; j <= 1 ; j++) {
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

    /**
     * Loads the preset conway states from static/json/conway.
     */
    private static void loadConwayStates() {
        cyderComboItems = new ArrayList<>();
        correspondingConwayStates = new ArrayList<>();

        File statesDir = new File(OSUtil.buildPath("static", "json", "conway"));

        if (statesDir.exists()) {
            File[] statesDirFiles = statesDir.listFiles();

            if (statesDirFiles != null && statesDirFiles.length > 0) {
                for (File json : statesDirFiles) {
                    if (FileUtil.validateExtension(json, ".json")) {
                        try {
                            Reader reader = new FileReader(json);
                            ConwayState loadState = conwayGson.fromJson(reader, ConwayState.class);
                            reader.close();

                            correspondingConwayStates.add(loadState);
                            cyderComboItems.add(new CyderComboBox.CyderComboItem(loadState.name()));
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else {
                presetComboBox.getIterationButton().setEnabled(false);
            }
        }
    }

    /**
     * An object used to store a Conway's game of life grid state.
     */
    @Immutable
    private record ConwayState(String name, int gridSize, LinkedList<Point> nodes) {
    }
}
