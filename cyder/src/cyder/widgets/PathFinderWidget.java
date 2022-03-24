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
    /**
     * The pathfinding frame.
     */
    private static CyderFrame pathFindingFrame;

    /**
     * The grid component for the visualization.
     */
    private static CyderGrid pathfindingGrid;

    /**
     * The default number of nodes for the path grid.
     */
    private static final int DEFAULT_NODES = 50;

    /**
     * The checkbox dictating whether to perform an animation of the
     * A* algorithm or instantly solve the problem if possible.
     */
    private static CyderCheckbox showStepsBox;

    /**
     * The checkbox dictating whether pathing to a diagonal neighbor is allowable.
     */
    private static CyderCheckbox diagonalBox;

    /**
     * The checkbox dictating whether the grid mode is ADD or DELETE.
     */
    private static CyderCheckbox deleteWallsCheckBox;

    // todo utilze
    private static CyderCheckbox placeStartBox;
    private static CyderCheckbox placeEndBox;

    private static CyderButton startButton;
    private static JSlider speedSlider;

    private static final LinkedList<PathNode> wallNodes = new LinkedList<>();
    private static final LinkedList<PathNode> pathableNodes = new LinkedList<>();

    // not final due to reversed
    private static LinkedList<PathNode> computedPathNodes = new LinkedList<>();

    private static int pathAnimationIndex;

    // todo get rid of timers and just use booleans
    private static Timer pathfinderAnimationTimer;
    private static int timeoutMS;
    private static final int MAX_TIMEOUT_MS = 1000;

    private static boolean algorithmPaused;

    private static final String PATH_FOUND = "PATH FOUND";
    private static final String PATH_NOT_FOUND = "PATH NOT FOUND";
    private static String pathText = PATH_NOT_FOUND;

    private static CyderSwitch heuristicSwitch;
    private static CyderSwitch dijkstraSwitch;
    private static boolean performDijkstras;

    private static int heuristicIndex;
    private static final String[] heuristics = {"Manhattan","Euclidean"};

    private static final Color pathableOpenColor = new Color(254, 104, 88);
    private static final Color pathableClosedColor = new Color(121, 236, 135);
    private static final Color wallsColor = CyderColors.navy;
    private static final Color endNodeColor = CyderColors.regularOrange;
    private static final Color startNodeColor = CyderColors.regularPink;
    private static final Color pathColor = CyderColors.regularBlue;
    private static final Color pathAnimationColor = new Color(34,216,248);

    /**
     * The node which the pathfinding starts from.
     * By default this is the top left corner (0,0).
     */
    private static PathNode startNode;

    /**
     * The node which A* attempts to path to.
     * By default this is the bottom rigth corner (DEFAULT_NODES - 1, DEFAULT_NODES - 1).
     */
    private static PathNode goalNode;

    /**
     * The default point the starting node is placed at.
     */
    private static final Point DEFAULT_START_POINT = new Point(0,0);

    /**
     * The default point the goal node is placed at.
     */
    private static final Point DEFAULT_GOAL_POINT = new Point(DEFAULT_NODES - 1, DEFAULT_NODES - 1);

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

        // todo setup function should be equiv to reset function
        pathfinderAnimationTimer = new Timer(timeoutMS, evt -> pathStep());
        pathfinderAnimationTimer.setDelay(timeoutMS);
        wallNodes.clear();
        pathableNodes.clear();
        computedPathNodes.clear();
        startNode = new PathNode(0,0);
        goalNode = new PathNode(25, 25);
        pathText = PATH_NOT_FOUND;
        algorithmPaused = false;

        pathFindingFrame = new CyderFrame(1000,1070, CyderIcons.defaultBackgroundLarge);
        pathFindingFrame.setTitle("Pathfinding visualizer");

        pathfindingGrid = new CyderGrid(DEFAULT_NODES, 800);
        pathfindingGrid.setBounds(100, 80, 800, 800);
        pathfindingGrid.setMinNodes(DEFAULT_NODES);
        pathfindingGrid.setMaxNodes(200);
        pathfindingGrid.setDrawGridLines(false);
        pathfindingGrid.setDrawExtendedBorder(true);
        pathfindingGrid.setBackground(CyderColors.vanila);
        pathfindingGrid.setResizable(true);
        pathfindingGrid.setSmoothScrolling(true);
        pathfindingGrid.installClickAndDragPlacer();
        pathFindingFrame.getContentPane().add(pathfindingGrid);
        pathfindingGrid.setSaveStates(false);

        // todo path found / not found label "pathText" in navy

        // todo start / stop checkbox

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

                if (pathfindingGrid.getMode() == CyderGrid.Mode.ADD) {
                    pathfindingGrid.setMode(CyderGrid.Mode.DELETE);
                } else {
                    pathfindingGrid.setMode(CyderGrid.Mode.ADD);
                }
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
            pathfinderAnimationTimer.stop();
            startButton.setText("Start");
            diagonalBox.setNotSelected();
            showStepsBox.setNotSelected();
            deleteWallsCheckBox.setNotSelected();
            diagonalBox.setEnabled(true);
            showStepsBox.setEnabled(true);
            deleteWallsCheckBox.setEnabled(true);
            speedSlider.setValue(50);
            startNode = null;
            goalNode = null;
            wallNodes.clear();
            pathableNodes.clear();
            computedPathNodes.clear();
            pathText = PATH_NOT_FOUND;
            pathfindingGrid.setNodeDimensionLength(DEFAULT_NODES);
            algorithmPaused = false;

            diagonalBox.setEnabled(true);
            heuristicSwitch.setEnabled(true);
            dijkstraSwitch.setEnabled(true);

            heuristicSwitch.setState(CyderSwitch.State.OFF);
            dijkstraSwitch.setState(CyderSwitch.State.ON);
            performDijkstras = false;

            // todo function for resetting the start/end nodes
            startNode = new PathNode(0,0);
            goalNode = new PathNode(25, 25);
        });
        pathFindingFrame.getContentPane().add(reset);

        startButton = new CyderButton("Start");
        startButton.setBounds(400,940, 170, 40);
        startButton.addActionListener(e -> {
            if (startNode == null || goalNode == null) {
                pathFindingFrame.notify("Start/end nodes not set");
            } else if (!pathfinderAnimationTimer.isRunning()) {
                diagonalBox.setEnabled(false);
                heuristicSwitch.setEnabled(false);
                dijkstraSwitch.setEnabled(false);
                diagonalBox.setEnabled(false);
                deleteWallsCheckBox.setEnabled(false);
                showStepsBox.setEnabled(false);

                startButton.setText("Stop");
                pathText = "";

                if (algorithmPaused)
                    pathfinderAnimationTimer.start();
                else
                    searchSetup();
            } else {
                pathfinderAnimationTimer.stop();
                startButton.setText("Start");
                pathText = "";

                algorithmPaused = showStepsBox.isSelected();
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

        int DEFAULT_SLIDER_VALUE = 500;
        int MAX_SLIDER_VALUE = 1000;
        int MIN_SLIDER_VALUE = 0;

        speedSlider = new JSlider(JSlider.HORIZONTAL, MIN_SLIDER_VALUE,
                MAX_SLIDER_VALUE, DEFAULT_SLIDER_VALUE);
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

        // todo bounds should be between 0 and 1000 ms

        // todo make higher value speed up animation, max should be no delay

        speedSlider.addChangeListener(e -> {
            // timeout is equal to the max value minus the slider value
            // meaning the max value of 1000 corresponds to the max speed,
            // or a delay of 0ms
            timeoutMS = MAX_SLIDER_VALUE - speedSlider.getValue();

            // todo no need for this when timer goes away
            pathfinderAnimationTimer.setDelay(timeoutMS);
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
            if (pathAnimationIndex + 1 < computedPathNodes.size())
                pathAnimationIndex++;
            else
                pathAnimationIndex = 0;

            // todo fix
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    };

    private static final ActionListener pathLabelAnimation = evt -> {
        //todo use a ripple label with font like prompt

        // actually just make it a status label with pathing... and then
        // ripple if path is found and not if path isn't found
    };


    /**
     * The A* "open" list which is pulled from by comparing each node's heuristic.
     */
    private static final PriorityQueue<PathNode> openNodes = new PriorityQueue<>(new NodeComparator());

    //performs the setup for the A* algorithm so that the timer can call update to interate over the next nodes
    private static void searchSetup() {
        // reset path
        computedPathNodes.clear();
        pathAnimationIndex = 0;

        // reset progress label
        pathText = PATH_NOT_FOUND;

        // reset start and goal parents
        goalNode.setParent(null);
        startNode.setParent(null);

        // clear nodes to path through
        pathableNodes.clear();

        // todo need conversion functions?
        for (GridNode node : pathfindingGrid.getGridNodes()) {
            PathNode addNode = new PathNode(node.getX(), node.getY());

            boolean isWall = false;

            for (PathNode n : wallNodes) {
                if (n.equals(addNode)) {
                    isWall = true;
                    break;
                }
            }

            if (!isWall) {
                pathableNodes.add(addNode);
            }
        }

        // ensure open list is empty
        openNodes.clear();

        //put start in the open
        startNode.setG(0);
        startNode.setH(heuristic(goalNode));
        openNodes.add(startNode);

        //animation chosen
        if (showStepsBox.isSelected()) {
            pathfinderAnimationTimer.start();
            //spins off below action listener to update grid until path found or no path found or user intervention
        } else {
            //instantly solve and paint grid and animate path if found and show words PATH or NO PATH
            // use a separate thread though to avoid lag
            CyderThreadRunner.submit(() -> {
                while (goalNode.getParent() == null) {
                    pathStep();
                }

                if (goalNode.getParent() != null) {
                    pathFound();
                } else {
                    pathNotFound();
                }
            },"Path Solver");
        }
    }

    /**
     * Takes a step towards the goal node according to
     * the current heuristic and pathable nodes.
     *
     * This is equivalent to what is computed in the primary A* while loop.
     * A future feature could be added to allow the algorithm to be
     * stepped through via this method.
     */
    private static void pathStep() {
        if (!openNodes.isEmpty()) {
            PathNode min = openNodes.poll();
            openNodes.remove(min);

             if (min.equals(goalNode)) {
                goalNode.setParent(min.getParent());

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

                    if (!openNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            }
        } else {
            if (goalNode.getParent() != null) {
                pathFound();
            } else {
                pathNotFound();
            }
        }
    }

    private static void pathFound() {
        pathfinderAnimationTimer.stop();
        startButton.setText("Start");
        diagonalBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        deleteWallsCheckBox.setEnabled(true);
        heuristicSwitch.setEnabled(true);
        dijkstraSwitch.setEnabled(true);
        algorithmPaused = false;

        pathText = "PATH FOUND";

        PathNode refNode = goalNode.getParent();

        while (refNode != startNode) {
            computedPathNodes.add(refNode);
            refNode = refNode.getParent();
        }

        LinkedList<PathNode> pathReversed = new LinkedList<>();

        for (int i = computedPathNodes.size() - 1; i > -1 ; i--) {
            pathReversed.add(computedPathNodes.get(i));
        }

        computedPathNodes = pathReversed;

        pathfindingGrid.repaint();
    }

    //indicates a path was not found so takes the proper actions given this criteria
    private static void pathNotFound() {
        pathfinderAnimationTimer.stop();
        startButton.setText("Start");
        diagonalBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        heuristicSwitch.setEnabled(true);
        dijkstraSwitch.setEnabled(true);
        deleteWallsCheckBox.setEnabled(true);
        algorithmPaused = false;

        pathText = PATH_NOT_FOUND;

        computedPathNodes.clear();
        pathAnimationIndex = 0;

        pathfindingGrid.repaint();
    }

    /**
     * Returns whether the provided nodes are diagonal neighbors.
     *
     * @param n1 the first node
     * @param n2 the second node
     * @return whether the provided nodes are diagonal neighbors
     */
    private static boolean areDiagonalNeighbors(PathNode n1, PathNode n2) {
        return (n1.getX() == n2.getX() + 1 && n1.getY() == n2.getY() + 1) ||
                (n1.getX() == n2.getX() + 1 && n1.getY() == n2.getY() - 1) ||
                (n1.getX() == n2.getX() - 1 && n1.getY() == n2.getY() - 1) ||
                (n1.getX() == n2.getX() - 1 && n1.getY() == n2.getY() + 1);
    }

    /**
     * Returns whether the provided nodes are orthogonal neighbors.
     *
     * @param n1 the first node
     * @param n2 the second node
     * @return whether the provided nodes are orthogonal neighbors
     */
    private static boolean areOrthogonalNeighbors(PathNode n1, PathNode n2) {
        return (n1.getX() == n2.getX() && n1.getY() == n2.getY() + 1) ||
                (n1.getX() == n2.getX() && n1.getY() == n2.getY() - 1) ||
                (n1.getX() == n2.getX() + 1 && n1.getY() == n2.getY()) ||
                (n1.getX() == n2.getX() - 1 && n1.getY() == n2.getY());
    }

    /**
     * Calculates the heuristic from the provided node to the goal node
     * using the currently set heuristic.
     *
     * @param n the node to calculate the heuristc of
     * @return the cost to path from the provided node to the goal
     */
    private static double heuristic(PathNode n) {
        return performDijkstras ? 1 : (heuristicIndex == 0
                ? manhattanDistance(n, goalNode) : euclideanDistance(n, goalNode));
    }

    /**
     * Calcualtes the g cost from the provided node to the start node.
     * This uses Euclidean distance by definition.
     *
     * @param n the node to calculate the g cost of
     * @return the g cost of the provided node
     */
    private static double calcGCost(PathNode n) {
        return euclideanDistance(n, startNode);
    }

    /**
     * Returns the Euclidean distance between the two nodes.
     *
     * @param n1 the first noDe
     * @param n2 the second node
     * @return the Euclidean distance between the two nodes
     */
    private static double euclideanDistance(PathNode n1, PathNode n2) {
        return Math.sqrt(Math.pow((n1.getX() - n2.getX()), 2) + Math.pow((n1.getY() - n2.getY()), 2));
    }

    /**
     * Returns the Manhattan distance between the two nodes.
     *
     * @param n1 the first node
     * @param n2 the second node
     * @return the Manhattan distance between the two nodes
     */
    private static double manhattanDistance(PathNode n1, PathNode n2) {
        return Math.abs(n1.getX() - n2.getX()) + Math.abs(n1.getY() - n2.getY());
    }

    /**
     * The node comparator to use for the node queue.
     */
    private static class NodeComparator implements Comparator<PathNode> {
        @Override
        public int compare(PathNode node1, PathNode node2) {
            if (node1.getF() > node2.getF()) {
                return 1;
            } else if (node1.getF() < node2.getF()) {
                return -1;
            } else {
                return Double.compare(node1.getH(), node2.getH());
            }
        }
    }
}
