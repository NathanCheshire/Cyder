package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.SliderShape;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;

public class GameOfLifeWidget {
    private static int[][] grid;
    private static boolean simulationRunning;

    private static int iterationsPerSecond = 10;
    private static final int maxIterationsPerSecond = 50;

    private static final int defaultGridLen = 45;
    private static final int currentGridLen = defaultGridLen;

    private static JLabel gridLabel;
    private static CyderButton simulateButton;
    private static CyderFrame conwayFrame;

    private static CyderLabel iterationLabel;
    private static CyderLabel populationLabel;
    private static CyderLabel maxPopulationLabel;

    private static boolean detectOscillations;

    private static int generationCount;
    private static int populationCount;
    private static int maxPopulation;
    private static int maxPopulationGeneration;

    private GameOfLifeWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = "conway", description = "Conway's game of life visualizer")
    public static void showGUI() {
        

        grid = new int[defaultGridLen][defaultGridLen];
        conwayFrame = new CyderFrame(940,1120, CyderIcons.defaultBackgroundLarge);
        conwayFrame.setTitle("Conway's Game of Life");

        gridLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                //drawing of grid
                populationCount = 0;
                super.paint(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(CyderColors.navy);
                g2d.setStroke(new BasicStroke(2));

                int gridLen = 90;
                int squareLen = 20;

                //vertical lines
                for (int i = 0 ; i <= gridLen * squareLen ; i += squareLen) {
                    g2d.drawLine(i + 1, 1, i + 1, gridLen * squareLen + 1);
                }

                //horizontal lines
                for (int i = 0 ; i <= gridLen * squareLen ; i += squareLen) {
                    g2d.drawLine(1, i + 1, gridLen * squareLen + 1, i + 1);
                }

                for (int x = 0 ; x < currentGridLen ; x++) {
                    for (int y = 0 ; y < currentGridLen ; y++) {
                        if (grid[x][y] == 1) {
                            populationCount++;
                            g2d.fillRect(1 + squareLen * x, 1 + squareLen* y, squareLen, squareLen);
                        }
                    }
                }

                populationLabel.setText("Population: " + populationCount);

                if (populationCount >= maxPopulation) {
                    maxPopulation = populationCount;
                    maxPopulationGeneration = generationCount;
                    maxPopulationLabel.setText("Max Population: " + maxPopulation + " [Gen " + maxPopulationGeneration + "]");
                }
            }
        };
        gridLabel.setOpaque(true);
        gridLabel.setBackground(Color.white);
        gridLabel.setBounds(20,80,902,902);
        conwayFrame.getContentPane().add(gridLabel);
        gridLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //placing blocks or taking them away based on mouse clicks
                if (!simulationRunning) {
                    double x = Math.floor(((e.getX() + 2) / 20.0));
                    double y = Math.floor(((e.getY() + 2) / 20.0));

                    if (x < currentGridLen - 1 && y < currentGridLen - 1 && x >= 1 && y >= 1) {
                        switch (grid[(int) x][(int) y]) {
                            case 0:
                                grid[(int) x][(int) y] = 1;
                                break;
                            case 1:
                                grid[(int) x][(int) y] = 0;
                                break;
                            default:
                                throw new RuntimeException("Illegal identifier in grid array");
                        }

                        gridLabel.repaint();
                    }
                }
            }
        });

        gridLabel.addMouseMotionListener(new MouseMotionAdapter() {
            //drawing walls by dragging
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!simulationRunning) {
                    double x = Math.floor(((e.getX() + 2) / 20.0));
                    double y = Math.floor(((e.getY() + 2) / 20.0));

                    if (x < currentGridLen - 1 && y < currentGridLen - 1 && x >= 1 && y >= 1) {
                        grid[(int) x][(int) y] = 1;
                        gridLabel.repaint();
                    }
                }
            }
        });

        iterationLabel = new CyderLabel("Generation: 0");
        iterationLabel.setFont(CyderFonts.defaultFont);
        iterationLabel.setBounds(20,32, 287, 30);
        conwayFrame.getContentPane().add(iterationLabel);

        populationLabel = new CyderLabel("Population: 0");
        populationLabel.setFont(CyderFonts.defaultFont);
        populationLabel.setBounds(20 + 287 + 20,32, 287, 30);
        conwayFrame.getContentPane().add(populationLabel);

        maxPopulationLabel = new CyderLabel("Max Population: 0 [Gen 0]");
        maxPopulationLabel.setFont(CyderFonts.defaultFont);
        maxPopulationLabel.setBounds(20 + 287 + 20 + 287 + 20,32, 287, 30);
        conwayFrame.getContentPane().add(maxPopulationLabel);

        CyderButton resetButton = new CyderButton("Reset");
        resetButton.addActionListener(e -> reset());
        resetButton.setBounds(20,70 + 30 + 902, (902 - 20) / 3, 40);
        conwayFrame.getContentPane().add(resetButton);

        simulateButton = new CyderButton("Simulate");
        simulateButton.addActionListener(e -> {
            if (simulationRunning) {
                simulateButton.setText("Simulate");

                CyderThreadRunner.submit(() -> {
                    try {
                        simulateButton.setEnabled(false);
                        Thread.sleep(2L * iterationsPerSecond);
                        simulateButton.setEnabled(true);
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                }, "Conway's Game of Life start button timeout");
            } else {
                start();
                simulateButton.setText("Stop");
            }

            simulationRunning = !simulationRunning;
        });
        simulateButton.setBounds(20 + (902 - 20) / 3 + 10,70 + 30 + 902, (902 - 20) / 3, 40);
        conwayFrame.getContentPane().add(simulateButton);

        CyderSwitch switcher = new CyderSwitch((902 - 20) / 3, 60, CyderSwitch.State.INDETERMINITE);
        switcher.setOnText("Gliders");
        switcher.setIndeterminiteText("Presets");
        switcher.setButtonPercent(50);
        switcher.setOffText("Copperhead");
        switcher.setBounds(20 + 2* (902 - 20) / 3 + 20,70 + 30 + 902 + 20, (902 - 20) / 3, 80);
        conwayFrame.getContentPane().add(switcher);
        switcher.getSwitchButton().addActionListener(e -> {
            switch (switcher.getState()) {
                case ON:
                case INDETERMINITE:
                    setPreset("Copperhead");
                    break;
                case OFF:
                    setPreset("Gliders");
                    break;
                default:
                    throw new IllegalStateException("Illegal switch state: " + switcher.getState());
            }
        });

        CyderCheckbox oscillationDetector = new CyderCheckbox();
        oscillationDetector.setToolTipText("Detect oscillations");
        oscillationDetector.setBounds(60,70 + 30 + 902 + 55,50,50);
        oscillationDetector.setNotSelected();
        oscillationDetector.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                detectOscillations = !detectOscillations;
            }
        });
        conwayFrame.getContentPane().add(oscillationDetector);

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 200);
        CyderSliderUI UI = new CyderSliderUI(speedSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(SliderShape.RECT);
        UI.setFillColor(Color.black);
        UI.setOutlineColor(CyderColors.navy);
        UI.setNewValColor(CyderColors.regularBlue);
        UI.setOldValColor(CyderColors.regularPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        speedSlider.setUI(UI);
        speedSlider.setBounds(80 + 60,70 + 30 + 902 + 55, 440, 40);
        speedSlider.setPaintTicks(false);
        speedSlider.setPaintLabels(false);
        speedSlider.setVisible(true);

        speedSlider.addChangeListener(e -> iterationsPerSecond = Math.max((int) (maxIterationsPerSecond *
                ((double) speedSlider.getValue() /  (double) speedSlider.getMaximum())),1));
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Generations per second");
        speedSlider.setFocusable(false);
        speedSlider.repaint();
        conwayFrame.getContentPane().add(speedSlider);

        conwayFrame.setVisible(true);
        conwayFrame.setLocationRelativeTo(CyderShare.getDominantFrame());
        conwayFrame.addPreCloseAction(() -> simulationRunning = false);
    }

    private static void setPreset(String preset) {
        if (grid.length < 45 || grid[0].length < 45) {
            conwayFrame.notify("In order to use presets, please make the grid at least 45x45");
            return;
        }

        if (preset.equals("Gliders")) {
            reset();
            grid = new int[currentGridLen][currentGridLen];

            grid[1][5] = 1;
            grid[1][6] = 1;
            grid[2][5] = 1;
            grid[2][6] = 1;
            grid[11][5] = 1;
            grid[11][6] = 1;
            grid[11][7] = 1;
            grid[12][4] = 1;
            grid[12][8] = 1;
            grid[13][3] = 1;
            grid[13][9] = 1;
            grid[14][3] = 1;
            grid[14][9] = 1;
            grid[15][6] = 1;
            grid[16][4] = 1;
            grid[16][8] = 1;
            grid[17][5] = 1;
            grid[17][6] = 1;
            grid[17][7] = 1;
            grid[18][6] = 1;
            grid[21][3] = 1;
            grid[21][4] = 1;
            grid[21][5] = 1;
            grid[22][3] = 1;
            grid[22][4] = 1;
            grid[22][5] = 1;
            grid[23][2] = 1;
            grid[23][6] = 1;
            grid[25][1] = 1;
            grid[25][2] = 1;
            grid[25][6] = 1;
            grid[25][7] = 1;
            grid[35][3] = 1;
            grid[35][4] = 1;
            grid[36][3] = 1;
            grid[36][4] = 1;

            gridLabel.repaint();
        } else if (preset.equals("Copperhead")) {
            reset();
            grid = new int[currentGridLen][currentGridLen];

            //bottom square
            grid[22][42] = 1;
            grid[23][42] = 1;
            grid[22][41] = 1;
            grid[23][41] = 1;

            //next row up...
            grid[21][40] = 1;
            grid[24][40] = 1;

            grid[20][39] = 1;
            grid[21][39] = 1;
            grid[22][39] = 1;
            grid[23][39] = 1;
            grid[24][39] = 1;
            grid[25][39] = 1;

            grid[20][38] = 1;
            grid[21][38] = 1;
            grid[24][38] = 1;
            grid[25][38] = 1;

            grid[20][37] = 1;
            grid[25][37] = 1;

            grid[20][35] = 1;
            grid[25][35] = 1;

            grid[20][34] = 1;
            grid[25][34] = 1;

            grid[20][34] = 1;
            grid[22][34] = 1;
            grid[23][34] = 1;
            grid[25][34] = 1;

            grid[21][33] = 1;
            grid[24][33] = 1;

            grid[21][31] = 1;
            grid[22][31] = 1;
            grid[23][31] = 1;
            grid[24][31] = 1;

            gridLabel.repaint();
        }
    }

    private static void reset() {
        CyderThreadRunner.submit(() -> {
            try {
                simulateButton.setEnabled(false);
                Thread.sleep(2L * iterationsPerSecond);
                simulateButton.setEnabled(true);
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, "Conway's Game of Life start button timeout");
        simulateButton.setText("Simulate");

        simulationRunning = false;
        maxPopulation = 0;
        maxPopulationGeneration = 0;
        generationCount = 0;
        iterationLabel.setText("Generation: 0");
        maxPopulationLabel.setText("Max Population: 0 [Gen 0]");
        grid = new int[currentGridLen][currentGridLen];
        gridLabel.repaint();
    }

    private static int[][] lastGen;
    private static int[][] secondToLastGen;

    private static void start() {
        CyderThreadRunner.submit(() -> {
            while (simulationRunning) {
                try {
                    //update grids
                    secondToLastGen = lastGen;
                    lastGen = grid;
                    grid = nextGeneration(grid);

                    generationCount++;
                    gridLabel.repaint();

                    iterationLabel.setText("Generation: " + generationCount);

                    boolean empty = true;

                    for (int x = 0 ; x < grid.length ; x++) {
                        for (int y = 0 ; y < grid[0].length ; y++) {
                            if (grid[x][y] == 1) {
                                empty = false;
                                break;
                            }
                        }
                    }

                    if (empty) {
                        conwayFrame.notify("Simulation ended with total elimination");

                        simulateButton.setText("Simulate");
                        simulationRunning = false;
                        gridLabel.repaint();

                        break;
                    }

                    if (detectOscillations && secondToLastGen != null && Arrays.deepEquals(secondToLastGen, grid)) {
                        conwayFrame.notify("Simulation will oscillate between the two last states indefinitely");

                        simulateButton.setText("Simulate");
                        simulationRunning = false;
                        gridLabel.repaint();

                        break;
                    }

                    Thread.sleep(1000 / iterationsPerSecond);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }
        },"Conway's Game of Life game thread");
    }

    private static int[][] nextGeneration(int[][] currentGeneration) {
        if (currentGeneration == null || currentGeneration.length < 3 || currentGeneration[0].length < 3)
            throw new IllegalArgumentException("Null or invalid board");

        int[][] ret = new int[currentGeneration.length][currentGeneration[0].length];

        for (int l = 1 ; l < currentGeneration.length - 1 ; l++) {
            for (int m = 1 ; m < currentGeneration[0].length - 1 ; m++) {
                int aliveNeighbours = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        aliveNeighbours += grid[l + i][m + j];
                    }
                }

                aliveNeighbours -= grid[l][m];

                if ((grid[l][m] == 1) && (aliveNeighbours < 2)) {
                    ret[l][m] = 0;
                } else if ((grid[l][m] == 1) && (aliveNeighbours > 3)) {
                    ret[l][m] = 0;
                } else if ((grid[l][m] == 0) && (aliveNeighbours == 3)) {
                    ret[l][m] = 1;
                } else {
                    ret[l][m] = grid[l][m];
                }
            }
        }

        return ret;
    }
}
