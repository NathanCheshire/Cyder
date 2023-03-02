package cyder.widgets;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.annotations.*;
import cyder.constants.CyderColors;
import cyder.enumerations.CyderInspection;
import cyder.enumerations.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.getter.GetFileBuilder;
import cyder.getter.GetInputBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.layouts.CyderGridLayout;
import cyder.layouts.CyderPartitionedLayout;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.ui.UiUtil;
import cyder.ui.button.CyderButton;
import cyder.ui.frame.CyderFrame;
import cyder.ui.grid.CyderGrid;
import cyder.ui.grid.GridNode;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderPanel;
import cyder.ui.selection.CyderComboBox;
import cyder.ui.selection.CyderComboBoxState;
import cyder.ui.selection.CyderSwitch;
import cyder.ui.selection.CyderSwitchState;
import cyder.ui.slider.CyderSliderUi;
import cyder.ui.slider.ThumbShape;
import cyder.user.UserUtil;
import cyder.utils.ArrayUtil;
import cyder.utils.OsUtil;
import cyder.utils.SerializationUtil;
import cyder.utils.StaticUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Conway's game of life visualizer.
 */
@Vanilla
@CyderAuthor
public final class GameOfLifeWidget {
    /**
     * Suppress default constructor.
     */
    private GameOfLifeWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

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
    private static CyderButton stopSimulationButton;

    /**
     * The combo box to cycle through the built-in presets.
     */
    private static CyderComboBox presetComboBox;

    /**
     * The slider to speed up/slow down the simulation.
     */
    private static JSlider iterationsPerSecondSlider;

    /**
     * The switch to toggle between detecting oscillations.
     */
    private static CyderSwitch detectOscillationsSwitch;

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
    private static ArrayList<GridNode> beforeStartingState;

    /**
     * The last state of the grid.
     */
    private static ArrayList<GridNode> lastState = new ArrayList<>();

    /**
     * The conway states loaded from static JSON conway directory.
     */
    private static ArrayList<ConwayState> correspondingConwayStates;

    /**
     * The switcher states to cycle between the states loaded from static JSON conway directory.
     */
    private static ArrayList<CyderComboBoxState> comboItems;

    /**
     * The minimum dimensional node length for the inner cyder grid.
     */
    private static final int MIN_NODES = 50;

    /**
     * The width of the widget frame.
     */
    private static final int FRAME_WIDTH = 600;

    /**
     * The height of the widget frame.
     */
    private static final int FRAME_HEIGHT = 920;

    /**
     * The reset string.
     */
    private static final String RESET = "Reset";

    /**
     * The simulate string.
     */
    private static final String SIMULATE = "Simulate";

    /**
     * The widget frame title.
     */
    private static final String TITLE = "Conway's Game of Life";

    /**
     * The load string.
     */
    private static final String LOAD = "Load";

    /**
     * The save string.
     */
    private static final String SAVE = "Save";

    /**
     * The clear string.
     */
    private static final String CLEAR = "Clear";

    /**
     * The conway string.
     */
    private static final String CONWAY = "conway";

    /**
     * The name of the thread which loads conway states.
     */
    private static final String CONWAY_STATE_LOADER_THREAD_NAME = "Conway State Loader";

    /**
     * The stop text.
     */
    private static final String STOP = "Stop";

    /**
     * The length of the grid.
     */
    private static final int gridLength = 550;

    /**
     * The initial node length on the grid.
     */
    private static final int gridNodes = 50;

    /**
     * The size of the primary widget controls.
     */
    private static final Dimension primaryControlSize = new Dimension(160, 40);

    /**
     * The size of the labels above the grid.
     */
    private static final Dimension topLabelSize = new Dimension(240, 30);

    /**
     * The size of the Cyder switches.
     */
    private static final Dimension switchSize = new Dimension(200, 55);

    /**
     * The delay in ms between preset combo box actions.
     */
    private static final int presetComboBoxDelay = 800;

    /**
     * The last time a preset combo action was invoked.
     */
    private static long lastPresetComboBoxAction = 0;

    /**
     * The left and right padding for the slider.
     */
    private static final int sliderPadding = 25;

    /**
     * The border length for the grid.
     */
    private static final int gridBorderLength = 3;

    /**
     * The length of the grid parent.
     */
    private static final int gridParentLength = gridLength + 2 * gridBorderLength;

    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = {CONWAY, "conways", "game of life", "conways game of life"},
            description = "Conway's game of life visualizer")
    public static void showGui() {
        UiUtil.closeIfOpen(conwayFrame);

        loadConwayStates();

        conwayFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT);
        conwayFrame.setTitle(TITLE);

        CyderPartitionedLayout partitionedLayout = new CyderPartitionedLayout();

        CyderGridLayout topLabelsGrid = new CyderGridLayout(2, 2);

        currentPopulationLabel = new CyderLabel();
        currentPopulationLabel.setSize(topLabelSize);
        topLabelsGrid.addComponent(currentPopulationLabel);

        maxPopulationLabel = new CyderLabel();
        maxPopulationLabel.setSize(topLabelSize);
        topLabelsGrid.addComponent(maxPopulationLabel);

        currentGenerationLabel = new CyderLabel();
        currentGenerationLabel.setSize(topLabelSize);
        topLabelsGrid.addComponent(currentGenerationLabel);

        correspondingGenerationLabel = new CyderLabel();
        correspondingGenerationLabel.setSize(topLabelSize);
        topLabelsGrid.addComponent(correspondingGenerationLabel);

        partitionedLayout.spacer(1);

        CyderPanel topLabelsPanel = new CyderPanel(topLabelsGrid);
        topLabelsPanel.setSize((int) (FRAME_WIDTH / 1.5), 50);
        partitionedLayout.addComponentMaintainSize(topLabelsPanel, CyderPartitionedLayout.PartitionAlignment.TOP);

        partitionedLayout.spacer(0.5f);

        conwayGrid = new CyderGrid(gridNodes, gridLength);
        conwayGrid.setSize(gridLength, gridLength);
        conwayGrid.setMinNodes(MIN_NODES);
        conwayGrid.setMaxNodes(150);
        conwayGrid.setDrawGridLines(false);
        conwayGrid.setBackground(CyderColors.vanilla);
        conwayGrid.setResizable(true);
        conwayGrid.setSmoothScrolling(true);
        conwayGrid.installClickAndDragPlacer();
        conwayGrid.setSaveStates(false);
        conwayGrid.setLocation(gridBorderLength, gridBorderLength);

        JLabel conwayGridParent = new JLabel();
        conwayGridParent.setBorder(new LineBorder(CyderColors.navy, gridBorderLength));
        conwayGridParent.setSize(gridParentLength, gridParentLength);
        conwayGridParent.add(conwayGrid);
        partitionedLayout.addComponentMaintainSize(conwayGridParent, CyderPartitionedLayout.PartitionAlignment.TOP);

        partitionedLayout.spacer(1);

        CyderGridLayout primaryControlGrid = new CyderGridLayout(2, 3);

        stopSimulationButton = new CyderButton(SIMULATE);
        stopSimulationButton.setSize(primaryControlSize);
        stopSimulationButton.addActionListener(e -> stopSimulationButtonAction());
        primaryControlGrid.addComponent(stopSimulationButton);

        CyderButton resetButton = new CyderButton(RESET);
        resetButton.setSize(primaryControlSize);
        resetButton.addActionListener(e -> resetToPreviousState());
        primaryControlGrid.addComponent(resetButton);

        CyderButton loadButton = new CyderButton(LOAD);
        loadButton.setSize(primaryControlSize);
        loadButton.addActionListener(e -> loadButtonAction());
        primaryControlGrid.addComponent(loadButton);

        presetComboBox = new CyderComboBox(primaryControlSize.width, primaryControlSize.height,
                comboItems, comboItems.get(0));
        presetComboBox.getIterationButton().addActionListener(e -> presetComboBoxAction());
        presetComboBox.setSize(primaryControlSize);
        primaryControlGrid.addComponent(presetComboBox);

        CyderButton clearButton = new CyderButton(CLEAR);
        clearButton.setSize(primaryControlSize);
        clearButton.addActionListener(e -> resetSimulation());
        primaryControlGrid.addComponent(clearButton);

        CyderButton saveButton = new CyderButton(SAVE);
        saveButton.setSize(primaryControlSize);
        saveButton.addActionListener(e -> toFile());
        primaryControlGrid.addComponent(saveButton);

        CyderPanel primaryControlPanel = new CyderPanel(primaryControlGrid);
        primaryControlPanel.setSize(450, 140);
        partitionedLayout.addComponentMaintainSize(primaryControlPanel);

        CyderGridLayout switchGrid = new CyderGridLayout(2, 1);

        detectOscillationsSwitch = new CyderSwitch(switchSize, CyderSwitchState.ON);
        detectOscillationsSwitch.setSize(switchSize);
        detectOscillationsSwitch.setState(CyderSwitchState.ON);
        detectOscillationsSwitch.setButtonPercent(50);
        detectOscillationsSwitch.setOnText("Oscillations");
        detectOscillationsSwitch.setOffText("Ignore");
        switchGrid.addComponent(detectOscillationsSwitch);

        CyderSwitch drawGridLinesSwitch = new CyderSwitch(switchSize, CyderSwitchState.OFF);
        drawGridLinesSwitch.setSize(switchSize);
        drawGridLinesSwitch.getSwitchButton().addActionListener(e -> {
            CyderSwitchState nextState = drawGridLinesSwitch.getNextState();
            conwayGrid.setDrawGridLines(nextState.equals(CyderSwitchState.ON));
            conwayGrid.repaint();
        });
        drawGridLinesSwitch.setOffText("No Grid");
        drawGridLinesSwitch.setOnText("Grid");
        drawGridLinesSwitch.setButtonPercent(50);
        switchGrid.addComponent(drawGridLinesSwitch);

        CyderPanel switchGridPanel = new CyderPanel(switchGrid);
        switchGridPanel.setSize(450, 60);
        partitionedLayout.addComponentMaintainSize(switchGridPanel);

        iterationsPerSecondSlider = new JSlider(JSlider.HORIZONTAL, MIN_ITERATIONS_PER_SECOND,
                MAX_ITERATIONS_PER_SECOND, DEFAULT_ITERATIONS_PER_SECOND);
        CyderSliderUi sliderUi = new CyderSliderUi(iterationsPerSecondSlider);
        sliderUi.setThumbStroke(new BasicStroke(2.0f));
        sliderUi.setThumbRadius(25);
        sliderUi.setThumbShape(ThumbShape.CIRCLE);
        sliderUi.setThumbFillColor(Color.black);
        sliderUi.setThumbOutlineColor(CyderColors.navy);
        sliderUi.setRightThumbColor(CyderColors.regularBlue);
        sliderUi.setLeftThumbColor(CyderColors.regularPink);
        sliderUi.setTrackStroke(new BasicStroke(3.0f));
        iterationsPerSecondSlider.setUI(sliderUi);
        iterationsPerSecondSlider.setSize(350, 40);
        iterationsPerSecondSlider.setPaintTicks(false);
        iterationsPerSecondSlider.setPaintLabels(false);
        iterationsPerSecondSlider.setVisible(true);
        iterationsPerSecondSlider.addChangeListener(e -> iterationsSliderChangeAction());
        iterationsPerSecondSlider.setOpaque(false);
        iterationsPerSecondSlider.setToolTipText("Iterations per second");
        iterationsPerSecondSlider.setFocusable(false);
        iterationsPerSecondSlider.repaint();
        iterationsPerSecondSlider.setSize(FRAME_WIDTH - 2 * sliderPadding, 40);

        partitionedLayout.spacer(0.5f);
        partitionedLayout.addComponentMaintainSize(iterationsPerSecondSlider);
        partitionedLayout.spacer(0.5f);

        resetSimulation();
        conwayFrame.setCyderLayout(partitionedLayout);
        conwayFrame.finalizeAndShow();
    }

    /**
     * The actions to invoke when a change of the iterations slider is encountered.
     */
    @ForReadability
    private static void iterationsSliderChangeAction() {
        iterationsPerSecond = iterationsPerSecondSlider.getValue();
    }

    /**
     * The actions to invoke when the present combo box button is clicked.
     */
    @ForReadability
    private static void presetComboBoxAction() {
        long now = System.currentTimeMillis();
        if (lastPresetComboBoxAction + presetComboBoxDelay > now) return;
        lastPresetComboBoxAction = now;

        CyderComboBoxState nextState = presetComboBox.getNextState();

        for (int i = 0 ; i < comboItems.size() ; i++) {
            if (comboItems.get(i).equals(nextState)) {
                beforeStartingState = new ArrayList<>();

                correspondingConwayStates.get(i).getNodes().forEach(point ->
                        beforeStartingState.add(new GridNode((int) point.getX(), (int) point.getY())));

                conwayFrame.revokeAllNotifications();
                conwayFrame.notify("Loaded state: " + correspondingConwayStates.get(i).getName());
                conwayGrid.setNodeDimensionLength(correspondingConwayStates.get(i).getGridSize());

                break;
            }
        }

        resetToPreviousState();
    }

    /**
     * The actions to invoke when the load button is pressed.
     */
    @ForReadability
    private static void loadButtonAction() {
        CyderThreadRunner.submit(() -> {
            Optional<File> optionalFile = GetterUtil.getInstance().getFile(new GetFileBuilder("Load state")
                    .setRelativeTo(conwayFrame));
            if (optionalFile.isEmpty()) return;
            File file = optionalFile.get();

            if (file.exists() && FileUtil.validateExtension(file, Extension.JSON.getExtension())) {
                fromJson(file);
            }
        }, CONWAY_STATE_LOADER_THREAD_NAME);
    }

    /**
     * The actions to invoke when the stop simulation button is clicked.
     */
    @ForReadability
    private static void stopSimulationButtonAction() {
        if (simulationRunning) {
            stopSimulation();
        } else if (conwayGrid.getNodeCount() > 0) {
            simulationRunning = true;
            stopSimulationButton.setText(STOP);
            conwayGrid.uninstallClickAndDragPlacer();
            conwayGrid.setResizable(false);
            start();
        } else {
            conwayFrame.notify("Place at least one node");
        }
    }

    /**
     * Resets the simulation and all values back to their default.
     */
    private static void resetSimulation() {
        stopSimulation();

        iterationsPerSecond = DEFAULT_ITERATIONS_PER_SECOND;

        conwayGrid.setNodeDimensionLength(50);
        conwayGrid.clearGrid();
        conwayGrid.repaint();

        detectOscillationsSwitch.setState(CyderSwitchState.ON);
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
        if (beforeStartingState == null) return;

        stopSimulation();

        conwayGrid.setGridState(beforeStartingState);
        conwayGrid.repaint();

        resetStats();
        population = beforeStartingState.size();
        updateLabels();
    }

    /**
     * Performs any stopping actions needed to properly stop the simulation.
     */
    private static void stopSimulation() {
        simulationRunning = false;
        stopSimulationButton.setText(SIMULATE);
        conwayGrid.installClickAndDragPlacer();
        conwayGrid.setResizable(true);
    }

    /**
     * The name of the conway simulation thread.
     */
    private static final String CONWAY_SIMULATOR_THREAD_NAME = "Conway Simulator";

    /**
     * Starts the simulation.
     */
    private static void start() {
        beforeStartingState = new ArrayList<>(conwayGrid.getGridNodes());

        CyderThreadRunner.submit(() -> {
            while (simulationRunning) {
                try {
                    ArrayList<GridNode> nextState = new ArrayList<>();

                    int[][] nextGen = nextGeneration(cyderGridToConwayGrid(conwayGrid.getGridNodes()));
                    for (int i = 0 ; i < nextGen.length ; i++) {
                        for (int j = 0 ; j < nextGen[0].length ; j++) {
                            if (nextGen[i][j] == 1) {
                                nextState.add(new GridNode(i, j));
                            }
                        }
                    }

                    if (nextState.equals(conwayGrid.getGridNodes())) {
                        conwayFrame.revokeAllNotifications();
                        conwayFrame.notify("Simulation stabilized at generation: " + generation);
                        stopSimulation();
                        return;
                    } else if (detectOscillationsSwitch.getState().equals(CyderSwitchState.ON)
                            && nextState.equals(lastState)) {
                        conwayFrame.revokeAllNotifications();
                        conwayFrame.notify("Detected oscillation at generation: " + generation);
                        stopSimulation();
                        return;
                    } else if (nextState.isEmpty()) {
                        conwayFrame.revokeAllNotifications();
                        conwayFrame.notify("Simulation ended with total elimination at generation: "
                                + generation);
                        stopSimulation();
                        return;
                    }

                    // advance last state
                    lastState = new ArrayList<>(conwayGrid.getGridNodes());

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
                    ThreadUtil.sleep((long) (TimeUtil.millisInSecond / iterationsPerSecond));
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        }, CONWAY_SIMULATOR_THREAD_NAME);
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
            ConwayState loadState = SerializationUtil.fromJson(reader, ConwayState.class);
            reader.close();

            resetSimulation();

            conwayGrid.setNodeDimensionLength(loadState.getGridSize());
            loadState.getNodes().forEach(point -> conwayGrid.addNode(
                    new GridNode((int) point.getX(), (int) point.getY())));

            conwayFrame.notify("Loaded state: " + loadState.getName());
            beforeStartingState = new ArrayList<>(conwayGrid.getGridNodes());

            resetStats();
            population = loadState.getNodes().size();
            updateLabels();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            conwayFrame.notify("Could not parse json as a valid ConwayState object");
        }
    }

    /**
     * The name of the thread to save a conway grid state.
     */
    private static final String CONWAY_STATE_SAVER_THREAD_NAME = "Conway State Saver";

    /**
     * Saves the current grid state to a json which can be loaded.
     */
    private static void toFile() {
        CyderThreadRunner.submit(() -> {
            if (conwayGrid.getNodeCount() == 0) {
                conwayFrame.notify("Place at least one node");
                return;
            }

            Optional<String> optionalSaveName = GetterUtil.getInstance().getInput(
                    new GetInputBuilder("Save name", "Save Conway state file name")
                            .setRelativeTo(conwayFrame)
                            .setFieldHintText("Valid filename")
                            .setSubmitButtonText("Save Conway State"));
            if (optionalSaveName.isEmpty()) return;
            String saveName = optionalSaveName.get();

            String filename = saveName + Extension.JSON.getExtension();

            if (OsUtil.isValidFilename(filename)) {
                File saveFile = UserUtil.createFileInUserSpace(filename);

                ArrayList<Point> points = new ArrayList<>();

                conwayGrid.getGridNodes().forEach(node -> points.add(new Point(node.getX(), node.getY())));
                ConwayState state = new ConwayState(saveName, conwayGrid.getNodeDimensionLength(), points);

                try {
                    FileWriter writer = new FileWriter(saveFile);
                    SerializationUtil.toJson(state, writer);
                    writer.close();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    conwayFrame.notify("Save state failed");
                }
            } else {
                conwayFrame.notify("Invalid save name");
            }
        }, CONWAY_STATE_SAVER_THREAD_NAME);
    }

    /**
     * Converts the CyderGrid nodes to a 2D integer array
     * needed to compute the next Conway iteration.
     *
     * @param nodes the list of cyder grid nodes
     * @return the 2D array consisting of 1s and 0s
     */
    private static int[][] cyderGridToConwayGrid(Collection<GridNode> nodes) {
        int len = conwayGrid.getNodeDimensionLength();

        int[][] ret = new int[len][len];
        nodes.forEach(node -> {
            int x = node.getX();
            int y = node.getY();

            if (x < len && y < len) {
                ret[x][y] = 1;
            }
        });
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
     * Loads the preset conway states from static JSON conway.
     */
    private static void loadConwayStates() {
        comboItems = new ArrayList<>();
        correspondingConwayStates = new ArrayList<>();

        File statesDir = StaticUtil.getStaticDirectory(CONWAY);

        if (statesDir.exists()) {
            File[] statesDirFiles = statesDir.listFiles();

            if (ArrayUtil.nullOrEmpty(statesDirFiles)) {
                presetComboBox.getIterationButton().setEnabled(false);
                return;
            }

            Arrays.stream(statesDirFiles).filter(jsonStateFile ->
                            FileUtil.validateExtension(jsonStateFile, Extension.JSON.getExtension()))
                    .forEach(jsonStateFile -> {
                        try {
                            Reader reader = new FileReader(jsonStateFile);
                            ConwayState loadState = SerializationUtil.fromJson(reader, ConwayState.class);
                            reader.close();

                            correspondingConwayStates.add(loadState);
                            comboItems.add(new CyderComboBoxState(loadState.getName()));
                        } catch (Exception e) {
                            Logger.log(LogTag.SYSTEM_IO, "Failed to load conway state: " + jsonStateFile);
                            ExceptionHandler.handle(e);
                        }
                    });
        }
    }

    /**
     * An object used to store a Conway's game of life grid state.
     */
    @SuppressWarnings("ClassCanBeRecord") /* GSON */
    @Immutable
    private static class ConwayState {
        /**
         * The name of the conway state.
         */
        private final String name;

        /**
         * The grid length for the saved state.
         */
        private final int gridSize;

        /**
         * The list of nodes for the saves state.
         */
        private final ArrayList<Point> nodes;

        /**
         * Constructs a new conway state.
         *
         * @param name     the name of the state
         * @param gridSize the size of the nxn grid
         * @param nodes    the nodes to place for the state
         */
        public ConwayState(String name, int gridSize, ArrayList<Point> nodes) {
            this.name = Preconditions.checkNotNull(name);
            this.gridSize = gridSize;
            this.nodes = Preconditions.checkNotNull(nodes);
        }

        /**
         * Returns the name of this conway state.
         *
         * @return the name of this conway state
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the grid size of this conway state.
         *
         * @return the grid size of this conway state
         */
        public int getGridSize() {
            return gridSize;
        }

        /**
         * Returns the list of points for this conway state.
         *
         * @return the list of points for this conway state
         */
        public ArrayList<Point> getNodes() {
            return nodes;
        }
    }
}
