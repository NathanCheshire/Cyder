package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class GameOfLifeWidget {
    private static int[][] grid;
    private static boolean simulationRunning;
    private static int framesPerSecond = 10;
    private static JLabel gridLabel;
    private static CyderButton simulateButton;
    private static CyderFrame cf;

    private static CyderLabel iterationLabel;
    private static CyderLabel populationLabel;
    private static CyderLabel maxPopulationLabel;

    private static int generationCount = 0;
    private static int populationCount = 0;
    private static int maxPopulation = 0;

    private GameOfLifeWidget() {}

    public static void showGUI() {
        grid = new int[45][45];
        cf = new CyderFrame(940,1050, CyderImages.defaultBackgroundLarge);
        cf.setTitle("Conway's Game of Life");

        gridLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
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

                for (int x = 0 ; x < 45 ; x++) {
                    for (int y = 0 ; y < 45 ; y++) {
                        if (grid[x][y] == 1) {
                            populationCount++;
                            g2d.fillRect(1 + squareLen * x, 1 + squareLen* y, squareLen, squareLen);
                        }
                    }
                }

                populationLabel.setText("Population: " + populationCount);

                if (populationCount > maxPopulation) {
                    maxPopulation = populationCount;
                    maxPopulationLabel.setText("Max Population: " + maxPopulation);
                }
            }
        };
        gridLabel.setOpaque(true);
        gridLabel.setBackground(Color.white);
        gridLabel.setBounds(20,60,902,902);
        cf.getContentPane().add(gridLabel);
        gridLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!simulationRunning) {
                    double x = Math.floor(((e.getX() + 2) / 20));
                    double y = Math.floor(((e.getY() + 2) / 20));

                    if (x < 44 && y < 44 && x >= 1 && y >= 1) {
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
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!simulationRunning) {
                    double x = Math.floor(((e.getX() + 2) / 20));
                    double y = Math.floor(((e.getY() + 2) / 20));

                    if (x < 44 && y < 44 && x >= 1 && y >= 1) {
                        grid[(int) x][(int) y] = 1;
                        gridLabel.repaint();
                    }
                }
            }
        });

        iterationLabel = new CyderLabel("Generation: 0");
        iterationLabel.setBounds(20,32, 150, 30);
        cf.getContentPane().add(iterationLabel);

        populationLabel = new CyderLabel("Population: 0");
        populationLabel.setBounds(180,32, 150, 30);
        cf.getContentPane().add(populationLabel);

        maxPopulationLabel = new CyderLabel("Max Population: 0");
        maxPopulationLabel.setBounds(340,32, 150, 30);
        cf.getContentPane().add(maxPopulationLabel);

        CyderButton resetButton = new CyderButton("Reset");
        resetButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    simulateButton.setEnabled(false);
                    Thread.sleep(2L * framesPerSecond);
                    simulateButton.setEnabled(true);
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }, "Conway's Game of Life start button timeout").start();
            simulateButton.setText("Simulate");

            simulationRunning = false;
            maxPopulation = 0;
            generationCount = 0;
            iterationLabel.setText("Generation: 0");
            maxPopulationLabel.setText("Max Population: 0");
            grid = new int[45][45];
            gridLabel.repaint();
        });
        resetButton.setBounds(20,70 + 10 + 902, (902 - 20) / 3, 40);
        resetButton.setColors(CyderColors.intellijPink);
        cf.getContentPane().add(resetButton);

        simulateButton = new CyderButton("Simulate");
        simulateButton.addActionListener(e -> {
            if (simulationRunning) {
                simulateButton.setText("Simulate");

                new Thread(() -> {
                    try {
                        simulateButton.setEnabled(false);
                        Thread.sleep(2L * framesPerSecond);
                        simulateButton.setEnabled(true);
                    } catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                }, "Conway's Game of Life start button timeout").start();
            } else {
                start();
                simulateButton.setText("Stop");
            }

            simulationRunning = !simulationRunning;
        });
        simulateButton.setBounds(20 + (902 - 20) / 3 + 10,70 + 10 + 902, (902 - 20) / 3, 40);
        simulateButton.setColors(CyderColors.intellijPink);
        cf.getContentPane().add(simulateButton);

        CyderButton gliderButton = new CyderButton("Gliders");
        gliderButton.addActionListener(e -> {
            resetButton.doClick();
            grid = new int[45][45];

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
        });
        gliderButton.setBounds(20 + 2* (902 - 20) / 3 + 20,70 + 10 + 902, (902 - 20) / 3, 40);
        gliderButton.setColors(CyderColors.intellijPink);
        cf.getContentPane().add(gliderButton);

        cf.setVisible(true);
        cf.setLocationRelativeTo(GenesisShare.getDominantFrame());
        cf.addPreCloseAction(() -> {

        });
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void start() {
        new Thread(() -> {
            while (simulationRunning) {
                try {
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
                        cf.notify("Simulation ended with total elimination");
                        break;
                    }

                    Thread.sleep(1000 / framesPerSecond);
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }
        },"Conway's Game of Life game thread").start();
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
