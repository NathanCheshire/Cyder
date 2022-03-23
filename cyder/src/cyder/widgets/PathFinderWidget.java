package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.SliderShape;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.ui.objects.GridNode;
import cyder.widgets.objects.PathNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class PathFinderWidget {
    private static CyderGrid pathGrid;

    private static CyderCheckbox showStepsBox;
    private static CyderCheckbox diagonalBox;

    // todo utilze
    private static CyderCheckbox placeStartBox;
    private static CyderCheckbox placeEndBox;

    private static CyderCheckbox deleteWallsCheckBox;
    private static CyderFrame pathFindingFrame;
    private static CyderButton startButton;
    private static JSlider speedSlider;

    private static PathNode start;
    private static PathNode end;
    private static LinkedList<PathNode> walls;
    private static LinkedList<PathNode> pathableNodes;

    private static LinkedList<PathNode> path;
    private static int pathIndex;

    private static Timer timer;
    private static int timeoutMS = 50;
    private static final int maxTimeoutMs = 100;

    private static boolean paused;

    private static final String PATH_FOUND = "PATH FOUND";
    private static final String PATH_NOT_FOUND = "PATH NOT FOUND";
    private static String pathText = PATH_NOT_FOUND;

    private static CyderSwitch heuristicSwitch;
    private static CyderSwitch dijkstraSwitch;
    private static boolean performDijkstras;

    private static int heuristicIndex;
    private static final String[] heuristics = {"Manhattan","Euclidean"};

    /**
     * Suppress default constructor.
     */
    private PathFinderWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = {"path","pathfinder"}, description = "A pathfinding visualizer for A* and Dijkstras algorithms")
    public static void showGUI() {
        if (pathFindingFrame != null)
            pathFindingFrame.dispose();

        timer = new Timer(timeoutMS, evt -> pathStep());
        timer.setDelay(timeoutMS);
        walls = new LinkedList<>();
        pathableNodes = new LinkedList<>();
        path.clear();
        start = new PathNode(0,0);
        end = new PathNode(25, 25);
        pathText = "";
        paused = false;

        pathFindingFrame = new CyderFrame(1000,1070, CyderIcons.defaultBackgroundLarge);
        pathFindingFrame.setTitle("Pathfinding visualizer");

        //pathable node in open
        Color pathableOpenColor = new Color(254, 104, 88);
        Color pathableClosedColor = new Color(121, 236, 135);
        Color wallsColor = CyderColors.navy;
        Color endNodeColor = CyderColors.regularOrange;
        Color startNodeColor = CyderColors.regularPink;
        Color pathColor = CyderColors.regularBlue;
        Color pathAnimationColor = new Color(34,216,248);

        int DEFAULT_NODES = 50; // todo extract out

        pathGrid = new CyderGrid(DEFAULT_NODES, 800);
        pathGrid.setBounds(100, 80, 800, 800);
        pathGrid.setMinNodes(DEFAULT_NODES);
        pathGrid.setMaxNodes(150);
        pathGrid.setDrawGridLines(false);
        pathGrid.setDrawExtendedBorder(true);
        pathGrid.setBackground(CyderColors.vanila);
        pathGrid.setResizable(true);
        pathGrid.setSmoothScrolling(true);
        pathGrid.installClickAndDragPlacer();
        pathFindingFrame.getContentPane().add(pathGrid);
        pathGrid.setSaveStates(false);

        // todo path found / not found label "pathText" in navy

        // todo start / stop checkbox

        // todo link delete to grid's delete

        CyderLabel deleteWallsLabel = new CyderLabel("Delete Walls");
        deleteWallsLabel.setBounds(120,885,100,30);
        pathFindingFrame.getContentPane().add(deleteWallsLabel);

        deleteWallsCheckBox = new CyderCheckbox();
        deleteWallsCheckBox.setNotSelected();
        deleteWallsCheckBox.setBounds(150, 920,50,50);
        pathFindingFrame.getContentPane().add(deleteWallsCheckBox);
        deleteWallsCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // todo link to grid
            }
        });

        CyderLabel showStepsLabel = new CyderLabel("Steps");
        showStepsLabel.setBounds(75 + 70 + 67,885,100,30);
        pathFindingFrame.getContentPane().add(showStepsLabel);

        showStepsBox = new CyderCheckbox();
        showStepsBox.setNotSelected();
        showStepsBox.setBounds(240, 920,50,50);
        pathFindingFrame.getContentPane().add(showStepsBox);

        CyderLabel diagonalStepsLabel = new CyderLabel("Diagonals");
        diagonalStepsLabel.setBounds(75 + 70 + 75 + 65,885,100,30);
        pathFindingFrame.getContentPane().add(diagonalStepsLabel);

        diagonalBox = new CyderCheckbox();
        diagonalBox.setNotSelected();
        diagonalBox.setBounds(310, 920,50,50);
        pathFindingFrame.getContentPane().add(diagonalBox);

        CyderButton reset = new CyderButton("Reset");
        reset.setBounds(400,890, 170, 40);
        reset.addActionListener(e -> {
            timer.stop();
            startButton.setText("Start");
            diagonalBox.setNotSelected();
            showStepsBox.setNotSelected();
            deleteWallsCheckBox.setNotSelected();
            diagonalBox.setEnabled(true);
            showStepsBox.setEnabled(true);
            deleteWallsCheckBox.setEnabled(true);
            speedSlider.setValue(50);
            start = null;
            end = null;
            walls = new LinkedList<>();
            pathableNodes = new LinkedList<>();
            path.clear();
            pathText = "";
            //todo default nodes reset
            paused = false;

            diagonalBox.setEnabled(true);
            heuristicSwitch.setEnabled(true);
            dijkstraSwitch.setEnabled(true);
            heuristicSwitch.setState(CyderSwitch.State.OFF);
            dijkstraSwitch.setState(CyderSwitch.State.ON);
            performDijkstras = false;
            start = new PathNode(0,0);
            end = new PathNode(25, 25);
            //todo default nodes reset
        });
        pathFindingFrame.getContentPane().add(reset);

        startButton = new CyderButton("Start");
        startButton.setBounds(400,940, 170, 40);
        startButton.addActionListener(e -> {
            if (start == null || end == null) {
                pathFindingFrame.notify("Start/end nodes not set");
            } else if (!timer.isRunning()) {
                diagonalBox.setEnabled(false);
                heuristicSwitch.setEnabled(false);
                dijkstraSwitch.setEnabled(false);
                diagonalBox.setEnabled(false);
                deleteWallsCheckBox.setEnabled(false);
                showStepsBox.setEnabled(false);

                startButton.setText("Stop");
                pathText = "";

                if (paused)
                    timer.start();
                else
                    searchSetup();
            } else {
                timer.stop();
                startButton.setText("Start");
                pathText = "";

                paused = showStepsBox.isSelected();
            }
        });
        pathFindingFrame.getContentPane().add(startButton);

        heuristicSwitch = new CyderSwitch(290,50);
        heuristicSwitch.setOffText("Manhattan");
        heuristicSwitch.setOnText("Euclidean");
        heuristicSwitch.setToolTipText("A* Heuristic");
        heuristicSwitch.setBounds(600, 930, 290, 50);
        heuristicSwitch.setButtonPercent(50);
        heuristicSwitch.setState(CyderSwitch.State.OFF);
        pathFindingFrame.getContentPane().add(heuristicSwitch);

        heuristicSwitch.getSwitchButton().addActionListener(e -> {
            if (heuristicIndex == 1) {
                heuristicIndex = 0;
            } else {
                heuristicIndex = 1;
            }
        });

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 50);
        CyderSliderUI UI = new CyderSliderUI(speedSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(SliderShape.RECT);
        UI.setFillColor(Color.black);
        UI.setOutlineColor(CyderColors.navy);
        UI.setNewValColor(CyderColors.regularBlue);
        UI.setOldValColor(CyderColors.regularPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        speedSlider.setUI(UI);
        speedSlider.setBounds(600, 880, 290, 40);
        speedSlider.setPaintTicks(false);
        speedSlider.setPaintLabels(false);
        speedSlider.setVisible(true);

        speedSlider.setValue(50);
        timeoutMS = (int) (maxTimeoutMs *
                ((double) speedSlider.getValue() /  (double) speedSlider.getMaximum()));

        speedSlider.addChangeListener(e -> {
            timeoutMS = (int) (maxTimeoutMs *
                    ((double) speedSlider.getValue() /  (double) speedSlider.getMaximum()));
            timer.setDelay(timeoutMS);
        });
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Pathfinding Animation Timeout");
        speedSlider.setFocusable(false);
        speedSlider.repaint();
        pathFindingFrame.getContentPane().add(speedSlider);

        dijkstraSwitch = new CyderSwitch(400,50);
        dijkstraSwitch.setOffText("Dijkstras");
        dijkstraSwitch.setOnText("A*");
        dijkstraSwitch.setToolTipText("Algorithm Switcher");
        dijkstraSwitch.setBounds(pathFindingFrame.getWidth() / 2 - 400 / 2, 1000, 400, 50);
        dijkstraSwitch.setButtonPercent(50);
        dijkstraSwitch.setState(CyderSwitch.State.ON);
        pathFindingFrame.getContentPane().add(dijkstraSwitch);

        dijkstraSwitch.getSwitchButton().addActionListener(e -> performDijkstras = !performDijkstras);

        pathFindingFrame.setVisible(true);
        pathFindingFrame.setLocationRelativeTo(CyderShare.getDominantFrame());

        Timer pathDrawingTimer = new Timer(50, pathDrawingAnimation);
        pathDrawingTimer.start();

        Timer pathLabelTimer = new Timer(1000, pathLabelAnimation);
        pathLabelTimer.start();
    }

    private static final ActionListener pathDrawingAnimation = evt -> {
        try {
            if (pathIndex + 1 < path.size())
                pathIndex++;
            else
                pathIndex = 0;

            // todo fix me
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    };

    private static final ActionListener pathLabelAnimation = evt -> {
        //todo use a ripple label with font like prompt
    };

    //open list outside methods to allow access to a* inner
    private static PriorityQueue<PathNode> open;

    //performs the setup for the A* algorithm so that the timer can call update to interate over the next nodes
    private static void searchSetup() {
        //get rid of lingering path if exists
        path.clear();
        pathIndex = 0;
        pathText = "";
        end.setParent(null);
        start.setParent(null);

        pathableNodes = new LinkedList<>();
        for (GridNode node : pathGrid.getGridNodes()) {
            PathNode addNode = new PathNode(node.getX(), node.getY());

            boolean isWall = false;

            for (PathNode n : walls) {
                if (n.equals(addNode)) {
                    isWall = true;
                    break;
                }
            }

            if (!isWall) {
                pathableNodes.add(addNode);
            }
        }

        open = new PriorityQueue<>(new NodeComparator());

        //put start in the open
        start.setG(0);
        start.setH(heuristic(end));
        open.add(start);

        //animation chosen
        if (showStepsBox.isSelected()) {
            timer.start();
            //spins off below action listener to update grid until path found or no path found or user intervention
        } else {
            //instantly solve and paint grid and animate path if found and show words PATH or NO PATH
            // use a separate thread though to avoid lag
            CyderThreadRunner.submit(() -> {
                while (end.getParent() == null) {
                    pathStep();
                }

                if (end.getParent() != null) {
                    pathFound();
                } else {
                    pathNotFound();
                }
            },"Pathfinder Thread");
        }
    }

    //a singular iteration of the while loop of the A* algorithm
    private static void pathStep() {
        if (!open.isEmpty()) {
            PathNode min = open.poll();
            open.remove(min);

             if (min.equals(end)) {
                end.setParent(min.getParent());

                pathFound();
                return;
            }

            //generate neihbors of this current node
            LinkedList<PathNode> neighbors = new LinkedList<>();

            for (PathNode possibleNeighbor : pathableNodes) {
                if (areOrthogonalNeighbors(possibleNeighbor, min) ||
                        (areDiagonalNeighbors(possibleNeighbor, min) && diagonalBox.isSelected())) {
                    neighbors.add(possibleNeighbor);
                }
            }

            for (PathNode neighbor: neighbors) {
                //calculate new H
                double newH = heuristic(neighbor);

                if (newH < neighbor.getH()) {
                    neighbor.setH(newH);
                    neighbor.setParent(min);
                    neighbor.setG(min.getG() + euclideanDistance(min, neighbor));

                    if (!open.contains(neighbor)) {
                        open.add(neighbor);
                    }
                }
            }
        } else {
            if (end.getParent() != null) {
                pathFound();
            } else {
                pathNotFound();
            }
        }
    }

    private static void pathFound() {
        timer.stop();
        startButton.setText("Start");
        diagonalBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        deleteWallsCheckBox.setEnabled(true);
        heuristicSwitch.setEnabled(true);
        dijkstraSwitch.setEnabled(true);
        paused = false;

        pathText = "PATH FOUND";

        PathNode refNode = end.getParent();

        while (refNode != start) {
            path.add(refNode);
            refNode = refNode.getParent();
        }

        LinkedList<PathNode> pathReversed = new LinkedList<>();

        for (int i = path.size() - 1 ; i > -1 ; i--) {
            pathReversed.add(path.get(i));
        }

        path = pathReversed;

        pathGrid.repaint();
    }

    //indicates a path was not found so takes the proper actions given this criteria
    private static void pathNotFound() {
        timer.stop();
        startButton.setText("Start");
        diagonalBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        heuristicSwitch.setEnabled(true);
        dijkstraSwitch.setEnabled(true);
        deleteWallsCheckBox.setEnabled(true);
        paused = false;

        pathText = PATH_NOT_FOUND;

        path.clear();
        pathIndex = 0;

        pathGrid.repaint();
    }

    private static boolean areDiagonalNeighbors(PathNode n1, PathNode n2) {
        return (n1.getX() == n2.getX() + 1 && n1.getY() == n2.getY() + 1) ||
                (n1.getX() == n2.getX() + 1 && n1.getY() == n2.getY() - 1) ||
                (n1.getX() == n2.getX() - 1 && n1.getY() == n2.getY() - 1) ||
                (n1.getX() == n2.getX() - 1 && n1.getY() == n2.getY() + 1);
    }

    private static boolean areOrthogonalNeighbors(PathNode n1, PathNode n2) {
        return (n1.getX() == n2.getX() && n1.getY() == n2.getY() + 1) ||
                (n1.getX() == n2.getX() && n1.getY() == n2.getY() - 1) ||
                (n1.getX() == n2.getX() + 1 && n1.getY() == n2.getY()) ||
                (n1.getX() == n2.getX() - 1 && n1.getY() == n2.getY());
    }

    //distance from node to end
    private static double heuristic(PathNode n) {
        if (performDijkstras)
            return 1;

        if (heuristicIndex == 0)
            return manhattanDistance(n, end);
        else
            return euclideanDistance(n, end);
    }

    //distance from node to start
    private static double calcGCost(PathNode n) {
        return euclideanDistance(n, start);
    }

    private static double euclideanDistance(PathNode n1, PathNode n2) {
        return Math.sqrt(Math.pow((n1.getX() - n2.getX()), 2) + Math.pow((n1.getY() - n2.getY()), 2));
    }

    private static double manhattanDistance(PathNode n1, PathNode n2) {
        return Math.abs(n1.getX() - n2.getX()) + Math.abs(n1.getY() - n2.getY());
    }

    private static class NodeComparator implements Comparator<PathNode> {
        @Override
        public int compare(PathNode node1, PathNode node2) {
            if (node1.getF() > node2.getF())
                return 1;
            else if (node1.getF() < node2.getF())
                return -1;
            else {
                return Double.compare(node1.getH(), node2.getH());
            }
        }
    }
}
