package cyder.widgets;

import com.google.common.base.Preconditions;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.widgets.objects.ConwayState;

import javax.swing.*;
import java.io.File;

/**
 * Conway's game of life visualizer.
 */
public class GameOfLifeWidget {
    // ui components

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
    private static JSlider speedSlider;

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
    private static final int DEFAULT_ITERATIONS_PER_SECOND = 10;

    /**
     * The number of iterations to compute per second.
     */
    private static int iterationsPerSecond = DEFAULT_ITERATIONS_PER_SECOND;

    /**
     * The maximum number of iterations per second.
     */
    private static final int MAX_ITERATIONS_PER_SECOND = 50;

    /**
     * The minimum allowable grid length
     */
    private static final int MIN_GRID_LENGTH = 50;

    /**
     * The initial and default grid length.
     */
    private static final int DEFAULT_GRID_LENGTH = MIN_GRID_LENGTH;

    /**
     * The current grid length.
     */
    private static int currentGridLength = DEFAULT_GRID_LENGTH;

    /**
     * The maximum grid length
     */
    private static final int MAX_GRID_LENGTH = 150;

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
     * The last generation computed.
     */
    private static int[][] lastGen;

    /**
     * The generation before the last generation.
     */
    private static int[][] secondToLastGen;

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

        conwayFrame = new CyderFrame(600,750);
        conwayFrame.setTitle("Conway's Game of Life");

        conwayGrid = new CyderGrid(DEFAULT_GRID_LENGTH, 550);
        conwayGrid.setBounds(25, 25 + CyderDragLabel.DEFAULT_HEIGHT, 550, 550);
        conwayGrid.setMinNodes(MIN_GRID_LENGTH);
        conwayGrid.setMaxNodes(MAX_GRID_LENGTH);
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
            // todo
        });

        clearButton = new CyderButton("Clear");
        clearButton.setBounds(25 + 15 + 160 + 20 + 160 + 20,
                conwayGrid.getY() + conwayGrid.getHeight() + 10, 160, 40);
        conwayFrame.getContentPane().add(clearButton);
        clearButton.addActionListener(e -> resetSimulation());

        conwayFrame.setLocationRelativeTo(CyderShare.getDominantFrame());
        conwayFrame.setVisible(true);
    }

    /**
     * Sets the preset to one of the default presets.
     *
     * @param conwayState the conway state object to load a saved state from
     */
    private static void setPreset(ConwayState conwayState) {
        Preconditions.checkNotNull(conwayState);

        // todo copy gliders and copperhead presets after drawing after implementing state saving
    }

    /**
     * Resets the simulation and all values back to their default.
     */
    private static void resetSimulation() {
       simulationRunning = false;
       iterationsPerSecond = DEFAULT_ITERATIONS_PER_SECOND;
       currentGridLength = DEFAULT_GRID_LENGTH;

       // todo reset all states as if widget was just opened
    }

    private static void resetToPreviousState() {

    }

    /**
     * Starts the simulation thread.
     */
    private static void start() {
        CyderThreadRunner.submit(() -> {
            while (simulationRunning) {
                // todo
            }
        },"Conway Simulation Generator");
    }

    /**
     * Loads the conway state from the provided json file and sets the current grid state to it.
     *
     * @param jsonFile the json file to load the state from
     */
    private static void fromJson(File jsonFile) {
        Preconditions.checkNotNull(jsonFile);
        // todo
    }

    /**
     * Saves the current grid state to a json which can be loaded.
     *
     * @param jsonFile the json file to save the current grid state to
     */
    private static void toFile(File jsonFile) {
        Preconditions.checkNotNull(jsonFile);
        // todo
    }

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
