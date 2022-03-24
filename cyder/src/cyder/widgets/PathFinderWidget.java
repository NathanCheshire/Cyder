package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.SliderShape;
import cyder.exceptions.IllegalMethodException;
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

/**
 * A pathfinding widget to visualize Dijkstras path finding algorithm and the A* algorithm
 * with Euclidean distance and Manhattan distance as valid A* heuristics.
 */
public class PathFinderWidget {
    /**
     * Whether the animation is currently running.
     */
    private static boolean animationRunning;

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

    private static CyderSwitch heuristicSwitch;
    private static CyderSwitch algorithmSwitch;

    private static CyderButton startButton;
    private static CyderButton reset;

    private static JSlider speedSlider;

    /**
     * The list containing the wall nodes drawn by the user.
     */
    private static final LinkedList<PathNode> wallNodes = new LinkedList<>();

    /**
     * The list containing the pathable nodes
     * (nodes which are not walls, the start node. or the goal node).
     */
    private static final LinkedList<PathNode> pathableNodes = new LinkedList<>();

    /**
     * The nodes in the current path provided one was found.
     */
    private static LinkedList<PathNode> computedPathNodes = new LinkedList<>();

    /**
     * The current index of the node in computedPathNodes to set to pathAnimationColor.
     */
    private static int pathAnimationIndex;

    /**
     * The current state of the A* algorithm.
     */
    private static PathingState currentPathingState = PathingState.NOT_STARTED; // todo utilize

    /**
     * The valid states of the A* algorithm.
     */
    private enum PathingState {
        /**
         * The algorithm is finished and found a path.
         */
        PATH_FOUND,
        /**
         * The algorithm is finished but no path was found. :(
         */
        PATH_NOT_FOUND,
        /**
         * The algorithm is imcomplete and may be resumed.
         */
        PAUSED,
        /**
         * The algorithm has not yet begun (Widget just opened or reset invoked).
         */
        NOT_STARTED,
        /**
         * The algorithm is currently underway, whether this be the first time it
         * has begun, or the 1000th time it has been paused and resumed.
         */
        RUNNING,
    }
    
    // todo do away with? just access switch directly, next state? is that a function? if not make it
    private static boolean performDijkstras;

    private static int heuristicIndex;
    private static final String[] heuristics = {"Manhattan","Euclidean"};

    /**
     * The color used for pathable nodes in the open list
     */
    private static final Color pathableOpenColor = new Color(254, 104, 88);

    /**
     * The color usdd for pathable nodes that have been removed from the open list.
     */
    private static final Color pathableClosedColor = new Color(121, 236, 135);

    /**
     * The color used for walls.
     */
    private static final Color wallsColor = CyderColors.navy;

    /**
     * The color used for the goal node.
     */
    private static final Color goalNodeColor = CyderColors.regularOrange;

    /**
     * The color used for the start node.
     */
    private static final Color startNodeColor = CyderColors.regularPink;

    /**
     * The dcolor used for the found path.
     */
    private static final Color pathColor = CyderColors.regularBlue;

    /**
     * The color used for the path found animation trickle.
     */
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

        // todo setup function should be equiv to reset function, just call reset function here
        wallNodes.clear();
        pathableNodes.clear();
        computedPathNodes.clear();
        startNode = new PathNode(0,0);
        goalNode = new PathNode(25, 25);
        currentPathingState = PathingState.NOT_STARTED;
        animationRunning = false;

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

        reset = new CyderButton("Reset");
        reset.setBounds(400,890, 170, 40);
        reset.addActionListener(e -> {
            // todo method for this to call upon startup as well
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
            // todo reset pathing status
            pathfindingGrid.setNodeDimensionLength(DEFAULT_NODES);
            currentPathingState = PathingState.NOT_STARTED;

            diagonalBox.setEnabled(true);
            heuristicSwitch.setEnabled(true);
            algorithmSwitch.setEnabled(true);

            heuristicSwitch.setState(CyderSwitch.State.OFF);
            algorithmSwitch.setState(CyderSwitch.State.ON);
            performDijkstras = false;

            // todo function for resetting the start/end nodes
            startNode = new PathNode(0,0);
            goalNode = new PathNode(25, 25);
        });
        pathFindingFrame.getContentPane().add(reset);

        startButton = new CyderButton("Start");
        startButton.setBounds(400,940, 170, 40);
        startButton.addActionListener(e -> {
            // ensure pathing is possible
            if (startNode == null) {
                pathFindingFrame.notify("Start node not set");
                return;
            } else if (goalNode == null) {
                pathFindingFrame.notify("End node not set");
                return;
            }

            // if not running
            if (currentPathingState != PathingState.RUNNING) {
                diagonalBox.setEnabled(false);
                heuristicSwitch.setEnabled(false);
                algorithmSwitch.setEnabled(false);
                diagonalBox.setEnabled(false);
                deleteWallsCheckBox.setEnabled(false);
                showStepsBox.setEnabled(false);
                startButton.setText("Stop");
                // todo methods for the above

                // transition state
                currentPathingState = PathingState.RUNNING;

                // resume if paused
                if (currentPathingState == PathingState.PAUSED) {
                    // todo resume animation
                }
                // otherwise first start so setup then run
                else {
                    searchSetup();
                }
            } else {
                currentPathingState = PathingState.PAUSED;

                // todo update UI elements
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
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Pathfinding Speed");
        speedSlider.setFocusable(false);
        speedSlider.repaint();
        pathFindingFrame.getContentPane().add(speedSlider);
        // no change listener since the sleep value is used as soon as possible

        algorithmSwitch = new CyderSwitch(400,50);
        algorithmSwitch.setOffText("Dijkstras");
        algorithmSwitch.setOnText("A*");
        algorithmSwitch.setToolTipText("Algorithm Switcher");
        algorithmSwitch.setBounds(pathFindingFrame.getWidth() / 2 - 400 / 2, 1000, 400, 50);
        algorithmSwitch.setButtonPercent(50);
        algorithmSwitch.setState(CyderSwitch.State.ON);
        pathFindingFrame.getContentPane().add(algorithmSwitch);

        algorithmSwitch.getSwitchButton().addActionListener(e -> performDijkstras = !performDijkstras);

        pathFindingFrame.finalizeAndShow();
    }

    /**
     * todo will go away and be replaced by method to draw path as long as the path text is path found
     * after a path is found
     */
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

        // todo reset pathing status

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
            // todo
            //pathfinderAnimationTimer.start();
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
        currentPathingState = PathingState.PATH_FOUND;

        // todo ui stuff
        startButton.setText("Start");
        diagonalBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        deleteWallsCheckBox.setEnabled(true);
        heuristicSwitch.setEnabled(true);
        algorithmSwitch.setEnabled(true);

        // traverse from goal back to start to construct the path
        PathNode refNode = goalNode.getParent();
        while (refNode != startNode) {
            computedPathNodes.add(refNode);
            refNode = refNode.getParent();
        }

        // reverse the path so that it goes from start to goal
        LinkedList<PathNode> pathReversed = new LinkedList<>();
        for (int i = computedPathNodes.size() - 1; i > -1 ; i--) {
            pathReversed.add(computedPathNodes.get(i));
        }
        computedPathNodes = pathReversed;

        pathfindingGrid.repaint();
    }

    //indicates a path was not found so takes the proper actions given this criteria
    private static void pathNotFound() {
        currentPathingState = PathingState.PATH_NOT_FOUND;

        // todo method for these
        startButton.setText("Start");
        diagonalBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        heuristicSwitch.setEnabled(true);
        algorithmSwitch.setEnabled(true);
        deleteWallsCheckBox.setEnabled(true);

        computedPathNodes.clear();
        pathAnimationIndex = 0;

        pathfindingGrid.repaint();
    }

    /**
     * Enables the UI elements during the pathfinding animation.
     */
    private static void enableUiElements() {
        deleteWallsCheckBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        diagonalBox.setEnabled(true);
        placeStartBox.setEnabled(true);
        placeEndBox.setEnabled(true);

        heuristicSwitch.setEnabled(true);
        algorithmSwitch.setEnabled(true);
    }

    /**
     * Disables the UI elements during the pathfinding animation.
     */
    private static void disableUiElements() {
        deleteWallsCheckBox.setEnabled(false);
        showStepsBox.setEnabled(false);
        diagonalBox.setEnabled(false);
        placeStartBox.setEnabled(false);
        placeEndBox.setEnabled(false);

        heuristicSwitch.setEnabled(false);
        algorithmSwitch.setEnabled(false);
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
