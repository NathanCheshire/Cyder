package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.ui.enums.SliderShape;
import cyder.ui.objects.GridNode;
import cyder.widgets.objects.PathNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * A pathfinding widget to visualize Dijkstras path finding algorithm and the A* algorithm
 * with Euclidean distance and Manhattan distance as valid A* heuristics.
 */
@Vanilla
@CyderAuthor
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
    private static final int DEFAULT_NODES = 25;

    /**
     * The maximum number of nodes for the path grid.
     */
    private static final int MAX_NODES = 100;

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

    /**
     * The checkbox to place the starting node.
     */
    private static CyderCheckbox placeStartBox;

    /**
     * The checkbox to place the goal node.
     */
    private static CyderCheckbox placeGoalBox;

    /**
     * The checkbox dictating whether to draw grid lines on the grid.
     */
    private static CyderCheckbox drawGridLinesBox;

    /**
     * The heuristic switcher to switch between Euclidean and
     * Manhattan distances as heuristics for A*.
     */
    private static CyderSwitch heuristicSwitch;

    /**
     * The off text for the heuristic switch.
     */
    private static final String HEURISTIC_OFF = "Manhattan";

    /**
     * The on text for the heuristic switch.
     */
    private static final String HEURISTIC_ON = "Euclidean";

    /**
     * The algorithm switcher to swith beteween A* and Dijkstras.
     */
    private static CyderSwitch algorithmSwitch;

    /**
     * The text to use for the algorithm OFF state.
     */
    private static final String ALGORITHM_OFF = "A*";

    /**
     * The text to use for the algorithm ON state.
     */
    private static final String ALGORITHM_ON = "Dijkstras";

    /**
     * The button to start/pause the animation.
     */
    private static CyderButton startPauseButton;

    /**
     * The button to reset the widget state back to the default.
     */
    private static CyderButton reset;

    /**
     * The slider to determine the speed of the animation.
     */
    private static JSlider speedSlider;

    /**
     * The maximum slider value.
     */
    private static final int MAX_SLIDER_VALUE = 100;

    /**
     * The minimum slider value.
     */
    private static final int MIN_SLIDER_VALUE = 1;

    /**
     * The default slider value in between the min and max values.
     */
    private static final int DEFAULT_SLIDER_VALUE = (MIN_SLIDER_VALUE + MAX_SLIDER_VALUE) / 2;

    /**
     * The timeout in ms between the path animation refresh.
     */
    private static final int PATH_TRICLE_TIMEOUT = 30;

    /**
     * The current state of the A* algorithm.
     */
    private static PathingState currentPathingState = PathingState.NOT_STARTED;

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

    /**
     * The label to display the current state on.
     */
    private static CyderLabel currentStateLabel;

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
    private static final Point DEFAULT_START_POINT = new Point(0, 0);

    /**
     * The default point the goal node is placed at.
     */
    private static final Point DEFAULT_GOAL_POINT = new Point(DEFAULT_NODES - 1, DEFAULT_NODES - 1);

    /**
     * The font to use for the state label.
     */
    private static final Font STATE_LABEL_FONT = new Font("Agency FB", Font.BOLD, 40);

    /**
     * The nodes which may be pathed through on the current grid state.
     */
    private static final LinkedList<PathNode> pathableNodes = new LinkedList<>();

    /**
     * The nodes which are in the queue to be pathed through.
     */
    private static final PriorityQueue<PathNode> openNodes = new PriorityQueue<>(new NodeComparator());

    /**
     * The currnet path animation object.
     * This is always killed before being set to a new object,
     * similar to how things are handled in AudioPlayer.
     */
    private static PathAnimator currentPathAnimator;

    /**
     * Suppress default constructor.
     */
    private PathFinderWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = {"path", "pathfinder", "A*"},
            description = "A pathfinding visualizer for A* and Dijkstras algorithms")
    public static void showGui() {
        if (pathFindingFrame != null)
            pathFindingFrame.dispose();

        pathFindingFrame = new CyderFrame(1000, 1070, CyderIcons.defaultBackgroundLarge);
        pathFindingFrame.setTitle("Pathfinding Visualizer");

        pathfindingGrid = new CyderGrid(DEFAULT_NODES, 800);
        pathfindingGrid.setBounds(100, 80, 800, 800);
        pathfindingGrid.setMinNodes(DEFAULT_NODES);
        pathfindingGrid.setMaxNodes(MAX_NODES);
        pathfindingGrid.setDrawGridLines(true);
        pathfindingGrid.setBackground(CyderColors.vanila);
        pathfindingGrid.setResizable(true);
        pathfindingGrid.setSmoothScrolling(true);
        pathFindingFrame.getContentPane().add(pathfindingGrid);
        pathfindingGrid.setSaveStates(false);

        currentStateLabel = new CyderLabel();
        currentStateLabel.setFont(STATE_LABEL_FONT);
        currentStateLabel.setBounds(40, CyderDragLabel.DEFAULT_HEIGHT,
                pathFindingFrame.getWidth() - 80, 50);
        pathFindingFrame.getContentPane().add(currentStateLabel);

        int startY = pathfindingGrid.getY() + pathfindingGrid.getHeight();
        int startX = pathfindingGrid.getX();

        CyderLabel placeStartLabel = new CyderLabel("Start");
        placeStartLabel.setBounds(startX - 50, startY + 5, 150, 40);
        pathFindingFrame.getContentPane().add(placeStartLabel);

        placeStartBox = new CyderCheckbox();
        placeStartBox.setToolTipText("Place start node");
        placeStartBox.setBounds(startX, startY + 40, 50, 50);
        pathFindingFrame.getContentPane().add(placeStartBox);
        placeStartBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (placeStartBox.isSelected()) {
                    pathfindingGrid.setNodeColor(startNodeColor);

                    pathfindingGrid.invokeWhenNodePlaced(() -> {
                        pathfindingGrid.removeNodesOfColor(startNodeColor);

                        placeStartBox.setNotSelected();
                        pathfindingGrid.setNodeColor(wallsColor);
                    });

                    // other actions
                    deleteWallsCheckBox.setNotSelected();
                    pathfindingGrid.setMode(CyderGrid.Mode.ADD);
                } else {
                    pathfindingGrid.setNodeColor(wallsColor);
                }
            }
        });

        CyderLabel placeGoalLabel = new CyderLabel("Goal");
        placeGoalLabel.setBounds(startX - 50 + 80, startY + 5, 150, 40);
        pathFindingFrame.getContentPane().add(placeGoalLabel);

        placeGoalBox = new CyderCheckbox();
        placeGoalBox.setToolTipText("Place goal node");
        placeGoalBox.setBounds(startX + 80, startY + 40, 50, 50);
        pathFindingFrame.getContentPane().add(placeGoalBox);
        placeGoalBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (placeGoalBox.isSelected()) {
                    pathfindingGrid.setNodeColor(goalNodeColor);

                    pathfindingGrid.invokeWhenNodePlaced(() -> {
                        pathfindingGrid.removeNodesOfColor(goalNodeColor);

                        placeGoalBox.setNotSelected();
                        pathfindingGrid.setNodeColor(wallsColor);
                    });

                    // other actions
                    deleteWallsCheckBox.setNotSelected();
                    pathfindingGrid.setMode(CyderGrid.Mode.ADD);
                } else {
                    pathfindingGrid.setNodeColor(wallsColor);
                }
            }
        });

        CyderCheckboxGroup nodeGroup = new CyderCheckboxGroup();
        nodeGroup.addCheckbox(placeStartBox);
        nodeGroup.addCheckbox(placeGoalBox);

        CyderLabel deleteWallsLabel = new CyderLabel("Delete walls");
        deleteWallsLabel.setBounds(startX - 50 + 80 * 2, startY + 5, 150, 40);
        pathFindingFrame.getContentPane().add(deleteWallsLabel);

        deleteWallsCheckBox = new CyderCheckbox();
        deleteWallsCheckBox.setToolTipText("Delete Walls");
        deleteWallsCheckBox.setBounds(startX + 80 * 2, startY + 40, 50, 50);
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
        showStepsLabel.setBounds(startX - 50, startY + 5 + 80, 150, 40);
        pathFindingFrame.getContentPane().add(showStepsLabel);

        showStepsBox = new CyderCheckbox();
        showStepsBox.setToolTipText("Show steps");
        showStepsBox.setBounds(startX, startY + 40 + 80, 50, 50);
        pathFindingFrame.getContentPane().add(showStepsBox);

        CyderLabel allowDiagonalsLabel = new CyderLabel("Diagonals");
        allowDiagonalsLabel.setBounds(startX - 50 + 80, startY + 5 + 80, 150, 40);
        pathFindingFrame.getContentPane().add(allowDiagonalsLabel);

        diagonalBox = new CyderCheckbox();
        diagonalBox.setToolTipText("Allow diagonals");
        diagonalBox.setBounds(startX + 80, startY + 40 + 80, 50, 50);
        pathFindingFrame.getContentPane().add(diagonalBox);

        CyderLabel drawGridLinesLabel = new CyderLabel("Grid Lines");
        drawGridLinesLabel.setBounds(startX - 50 + 80 * 2, startY + 5 + 80, 150, 40);
        pathFindingFrame.getContentPane().add(drawGridLinesLabel);

        drawGridLinesBox = new CyderCheckbox();
        drawGridLinesBox.setToolTipText("Draw grid lines");
        drawGridLinesBox.setBounds(startX + 80 * 2, startY + 40 + 80, 50, 50);
        drawGridLinesBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (drawGridLinesBox.isSelected()) {
                    pathfindingGrid.setDrawGridLines(true);
                } else {
                    pathfindingGrid.setDrawGridLines(false);
                }
            }
        });
        pathFindingFrame.getContentPane().add(drawGridLinesBox);

        reset = new CyderButton("Reset");
        reset.setBounds(350, startY + 40 - 20, 180, 50);
        reset.addActionListener(e -> reset());
        pathFindingFrame.getContentPane().add(reset);

        startPauseButton = new CyderButton("Start");
        startPauseButton.setBounds(350, startY + 40 + 80, 180, 50);
        startPauseButton.addActionListener(e -> {
            // start must be placed
            if (pathfindingGrid.getNodesOfColor(startNodeColor).isEmpty()) {
                pathFindingFrame.notify("Start node not set");
                return;
            }

            // goal must be placed
            if (pathfindingGrid.getNodesOfColor(goalNodeColor).isEmpty()) {
                pathFindingFrame.notify("End node not set");
                return;
            }

            // if not running
            if (currentPathingState != PathingState.RUNNING) {
                disableUiElements();
                startPauseButton.setText("Stop");

                // resume if paused
                if (currentPathingState == PathingState.PAUSED) {
                    // transition state
                    currentPathingState = PathingState.RUNNING;

                    // all setup was already performed so just start stepping again
                    startMainWhile();
                }
                // otherwise first start so setup then run
                else {
                    searchSetup();
                }
            } else {
                currentPathingState = PathingState.PAUSED;
                startPauseButton.setText("Resume");
                updateStateLabel();
                enableUiElements();
            }
        });
        pathFindingFrame.getContentPane().add(startPauseButton);

        heuristicSwitch = new CyderSwitch(350, 50);
        heuristicSwitch.setOffText(HEURISTIC_OFF);
        heuristicSwitch.setOnText(HEURISTIC_ON);
        heuristicSwitch.setToolTipText("A* Heuristic");
        heuristicSwitch.setBounds(550, startY + 40 - 20, 350, 50);
        heuristicSwitch.setButtonPercent(50);
        pathFindingFrame.getContentPane().add(heuristicSwitch);

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
        speedSlider.setBounds(350, startY + 40 + 35, 350 + 180 + 20, 40);
        speedSlider.setPaintTicks(false);
        speedSlider.setPaintLabels(false);
        speedSlider.setVisible(true);
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Pathfinding Speed");
        speedSlider.setFocusable(false);
        pathFindingFrame.getContentPane().add(speedSlider);
        // no change listener since the sleep value is used as soon as possible

        algorithmSwitch = new CyderSwitch(350, 50);
        algorithmSwitch.setOffText(ALGORITHM_OFF);
        algorithmSwitch.setOnText(ALGORITHM_ON);
        algorithmSwitch.setToolTipText("Algorithm Switcher");
        algorithmSwitch.setBounds(550, startY + 40 + 80, 350, 50);
        algorithmSwitch.setButtonPercent(50);
        pathFindingFrame.getContentPane().add(algorithmSwitch);

        reset();

        pathFindingFrame.finalizeAndShow();
    }

    /**
     * Performs the setup necessary to start path finding such as
     * initializing the lists, finding the start and goal nodes,
     * and converting GridNodes to PathNodes.
     */
    private static void searchSetup() {
        // this is only invoked if start and goal are set so safe not to check here
        endPathAnimator();
        removePathingNodes();

        // find walls that we cannot path through
        LinkedList<GridNode> walls = pathfindingGrid.getNodesOfColor(wallsColor);

        // get goal node and convert to path node
        GridNode gridGoal = pathfindingGrid.getNodesOfColor(goalNodeColor).get(0);
        goalNode = new PathNode(gridGoal.getX(), gridGoal.getY());
        goalNode.setParent(null);

        // get start node and convert to path node
        GridNode gridStart = pathfindingGrid.getNodesOfColor(startNodeColor).get(0);
        startNode = new PathNode(gridStart.getX(), gridStart.getY());
        startNode.setParent(null);

        // find new pathable nodes which aren't start, goal, or walls
        pathableNodes.clear();
        for (int x = 0 ; x < pathfindingGrid.getNodeDimensionLength() ; x++) {
            for (int y = 0 ; y < pathfindingGrid.getNodeDimensionLength() ; y++) {
                GridNode node = new GridNode(x, y);

                // skip walls
                if (walls.contains(node))
                    continue;

                // skip start
                if (startNode.equals(node))
                    continue;

                // skip goal node
                if (goalNode.equals(node))
                    continue;

                // otherwise it's a pathable node so convert and add
                pathableNodes.add(new PathNode(node.getX(), node.getY()));
            }
        }

        // reset open nodes
        openNodes.clear();

        // put start in the open after setting costs
        startNode.setG(0);
        startNode.setH(heuristic(goalNode));
        openNodes.add(startNode);

        // update state
        currentPathingState = PathingState.RUNNING;

        // disable ui elements
        disableUiElements();

        // disable zooming
        pathfindingGrid.setResizable(false);

        // start main program loop
        startMainWhile();
    }

    /**
     * Starts the main A* while loop.
     * All setup of lists must be performed before invoking this method.
     */
    private static void startMainWhile() {
        updateStateLabel();

        CyderThreadRunner.submit(() -> {
            try {
                while (currentPathingState == PathingState.RUNNING) {
                    pathStep();

                    if (showStepsBox.isSelected()) {
                        lockingRepaintGrid();
                        Thread.sleep(MAX_SLIDER_VALUE - speedSlider.getValue());
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Path Solver");
    }

    /**
     * Takes a step towards the goal node according to
     * the current heuristic and pathable nodes.
     * <p>
     * This is equivalent to what is computed in the primary A* while loop.
     * A future feature could be added to allow the algorithm to be
     * stepped through via this method.
     */
    private static void pathStep() {
        if (!openNodes.isEmpty()) {
            PathNode min = openNodes.poll();

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

            for (PathNode neighbor : neighbors) {
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

            // path hasn't been found yet but one may still
            // exist since there are nodes in the open list
            // therefore refresh the grid
            for (PathNode pathNode : pathableNodes) {
                if (pathNode.equals(startNode) || pathNode.equals(goalNode))
                    continue;

                if (openNodes.contains(pathNode)) {
                    lockingAddNode(new GridNode(pathableOpenColor, pathNode.getX(), pathNode.getY()));
                } else if (pathNode.getParent() != null) {
                    lockingAddNode(new GridNode(pathableClosedColor, pathNode.getX(), pathNode.getY()));
                }
            }
        } else pathNotFound();
    }

    /**
     * Performs the actions necessary following a path
     * from the start to the goal node being found.
     */
    private static void pathFound() {
        // set state
        currentPathingState = PathingState.PATH_FOUND;

        // reset button text
        startPauseButton.setText("Start");

        enableUiElements();
        updateStateLabel();

        pathfindingGrid.setResizable(true);

        // traverse from goal back to start to construct the path
        PathNode refNode = goalNode.getParent();
        ArrayList<Point> pathForward = new ArrayList<>();
        while (refNode != startNode) {
            pathForward.add(new Point(refNode.getX(), refNode.getY()));
            refNode = refNode.getParent();
        }

        // reverse the path so that it goes from start to goal
        ArrayList<Point> pathReversed = new ArrayList<>();
        for (int i = pathForward.size() - 1 ; i > -1 ; i--) {
            pathReversed.add(pathForward.get(i));
        }

        // start path tricle animation thread
        // this simply changes the color of the actual grid nodes based on the
        // nodes within the found path
        currentPathAnimator = new PathAnimator(pathReversed);
    }

    /**
     * A animator class to animate the path found animation.
     */
    private static class PathAnimator {
        /**
         * The dcolor used for the found path.
         */
        private static final Color PATH_COLOR = CyderColors.regularBlue;

        /**
         * The color used for the path found animation trickle.
         */
        private static final Color PATH_ANIMATION_COLOR = new Color(34, 216, 248);

        /**
         * Whether this animation has been killed
         */
        private boolean killed;

        /**
         * The list of points to animate.
         */
        private final ArrayList<Point> pathPoints;

        /**
         * Constructs and starts a new path animator.
         *
         * @param pathPoints the list of points to animate.
         */
        public PathAnimator(ArrayList<Point> pathPoints) {
            this.pathPoints = pathPoints;

            CyderThreadRunner.submit(() -> {
                try {
                    for (int i = 0 ; i < pathPoints.size() ; i++) {
                        if (killed)
                            return;

                        GridNode updateNode = null;

                        for (GridNode node : pathfindingGrid.getGridNodes()) {
                            if (killed)
                                return;

                            if (node.getX() == pathPoints.get(i).getX()
                                    && node.getY() == pathPoints.get(i).getY()) {
                                updateNode = node;
                                break;
                            }
                        }

                        if (updateNode.getColor().equals(PATH_ANIMATION_COLOR)
                                || updateNode.getColor().equals(pathableClosedColor)) {
                            lockingAddNode(new GridNode(PATH_ANIMATION_COLOR, updateNode.getX(), updateNode.getY()));
                            lockingRepaintGrid();
                            Thread.sleep(PATH_TRICLE_TIMEOUT);
                        }
                    }

                    while (true) {
                        // moving path dot from start to goal
                        for (Point p : pathPoints) {
                            if (killed)
                                return;

                            Optional<GridNode> overridePoint = pathfindingGrid.getNodeAtPoint(p);
                            if (overridePoint.isPresent()
                                    && (overridePoint.get().getColor().equals(PATH_ANIMATION_COLOR)
                                    || overridePoint.get().getColor().equals(PATH_COLOR))) {
                                lockingAddNode(new GridNode(PATH_COLOR, (int) p.getX(), (int) p.getY()));
                                lockingRepaintGrid();
                            }

                            if (killed)
                                return;

                            Thread.sleep(PATH_TRICLE_TIMEOUT);

                            if (killed)
                                return;

                            overridePoint = pathfindingGrid.getNodeAtPoint(p);
                            if (overridePoint.isPresent()
                                    && (overridePoint.get().getColor().equals(PATH_ANIMATION_COLOR)
                                    || overridePoint.get().getColor().equals(PATH_COLOR))) {
                                lockingAddNode(new GridNode(PATH_ANIMATION_COLOR, (int) p.getX(), (int) p.getY()));
                                lockingRepaintGrid();
                            }
                        }

                        if (killed)
                            return;

                        // moving path dot from goal to start
                        for (int i = pathPoints.size() - 1 ; i >= 0 ; i--) {
                            if (killed)
                                return;

                            Optional<GridNode> overridePoint = pathfindingGrid.getNodeAtPoint(pathPoints.get(i));
                            if (overridePoint.isPresent()
                                    && (overridePoint.get().getColor().equals(PATH_ANIMATION_COLOR)
                                    || overridePoint.get().getColor().equals(PATH_COLOR))) {
                                lockingAddNode(new GridNode(PATH_COLOR,
                                        (int) pathPoints.get(i).getX(),
                                        (int) pathPoints.get(i).getY()));
                                lockingRepaintGrid();
                            }

                            if (killed)
                                return;

                            Thread.sleep(PATH_TRICLE_TIMEOUT);

                            if (killed)
                                return;

                            overridePoint = pathfindingGrid.getNodeAtPoint(pathPoints.get(i));
                            if (overridePoint.isPresent()
                                    && (overridePoint.get().getColor().equals(PATH_ANIMATION_COLOR)
                                    || overridePoint.get().getColor().equals(PATH_COLOR))) {
                                lockingAddNode(new GridNode(PATH_ANIMATION_COLOR,
                                        (int) pathPoints.get(i).getX(),
                                        (int) pathPoints.get(i).getY()));
                                lockingRepaintGrid();
                            }

                            if (killed)
                                return;
                        }
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
                lockingRepaintGrid();
            }, "Pathfinding Path Animator");
        }

        /**
         * Kills this path animator.
         */
        public void kill() {
            killed = true;
        }
    }

    /**
     * Performs the actions necessary following a path
     * from the start to the goal node was not found.
     */
    private static void pathNotFound() {
        // set state
        currentPathingState = PathingState.PATH_NOT_FOUND;
        updateStateLabel();

        // reset button text
        startPauseButton.setText("Start");

        // enable ui elements
        enableUiElements();

        pathfindingGrid.setResizable(true);

        lockingRepaintGrid();
    }

    /**
     * Enables the UI elements during the pathfinding animation.
     */
    private static void enableUiElements() {
        deleteWallsCheckBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        diagonalBox.setEnabled(true);
        placeStartBox.setEnabled(true);
        placeGoalBox.setEnabled(true);
        drawGridLinesBox.setEnabled(true);

        heuristicSwitch.setEnabled(true);
        algorithmSwitch.setEnabled(true);

        pathfindingGrid.installClickAndDragPlacer();
    }

    /**
     * Disables the UI elements during the pathfinding animation.
     */
    private static void disableUiElements() {
        deleteWallsCheckBox.setEnabled(false);
        showStepsBox.setEnabled(false);
        diagonalBox.setEnabled(false);
        placeStartBox.setEnabled(false);
        placeGoalBox.setEnabled(false);
        drawGridLinesBox.setEnabled(false);

        heuristicSwitch.setEnabled(false);
        algorithmSwitch.setEnabled(false);

        pathfindingGrid.uninstallClickAndDragPLacer();
    }

    /**
     * Resets all the checkboxes to their default state.
     */
    private static void resetCheckboxStates() {
        deleteWallsCheckBox.setSelected(false);
        showStepsBox.setSelected(false);
        diagonalBox.setSelected(false);
        placeStartBox.setSelected(false);
        placeGoalBox.setSelected(false);
        drawGridLinesBox.setSelected(true);
        pathfindingGrid.setDrawGridLines(true);
    }

    /**
     * Resets the algorithm and heuristic switchers to their default states.
     */
    private static void resetSwitcherStates() {
        // corresponds to Manhattan
        heuristicSwitch.setState(CyderSwitch.State.OFF);

        // corresponds to A*
        algorithmSwitch.setState(CyderSwitch.State.OFF);
    }

    /**
     * Updates the state label
     */
    private static void updateStateLabel() {
        switch (currentPathingState) {
            case PAUSED:
                currentStateLabel.setText("State: Paused");
                break;
            case RUNNING:
                currentStateLabel.setText("State: Running...");
                break;
            case PATH_FOUND:
                currentStateLabel.setText("State: Path found");
                break;
            case PATH_NOT_FOUND:
                currentStateLabel.setText("State: No path found");
                break;
            case NOT_STARTED:
                currentStateLabel.setText("State: Not Started");
                break;
            default:
                throw new IllegalStateException("Invalid currentPathingState: " + currentPathingState);
        }
    }

    /**
     * Removes all nodes having to do with the pathfinding algorithm such as
     * open nodes, closed nodes, and blue path nodes.
     * Note this method does not repaint the grid.
     */
    private static void removePathingNodes() {
        pathfindingGrid.removeNodesOfColor(pathableClosedColor);
        pathfindingGrid.removeNodesOfColor(pathableOpenColor);
    }

    /**
     * Resets the start and goal nodes to their default.
     * Note this method does not repaint the grid.
     */
    private static void resetStartAndGoalNodes() {
        pathfindingGrid.removeNodesOfColor(startNodeColor);
        pathfindingGrid.removeNodesOfColor(goalNodeColor);

        startNode = new PathNode(DEFAULT_START_POINT);
        goalNode = new PathNode(DEFAULT_GOAL_POINT);

        lockingAddNode(new GridNode(startNodeColor, startNode.getX(), startNode.getY()));
        lockingAddNode(new GridNode(goalNodeColor, goalNode.getX(), goalNode.getY()));
    }

    /**
     * Removes all walls from the grid.
     * Note this method does not repaint the grid.
     */
    private static void removeWalls() {
        pathfindingGrid.removeNodesOfColor(wallsColor);
    }

    /**
     * Kills the path animator and sets it to null.
     */
    private static void endPathAnimator() {
        if (currentPathAnimator != null) {
            currentPathAnimator.kill();
            currentPathAnimator = null;

            pathfindingGrid.removeNodesOfColor(PathAnimator.PATH_COLOR);
            pathfindingGrid.removeNodesOfColor(PathAnimator.PATH_ANIMATION_COLOR);
        }
    }

    /**
     * Resets the visualizer as if the widget was just opened.
     */
    public static void reset() {
        endPathAnimator();

        // ensure ui elements are enabled
        enableUiElements();

        // ensure start button has proper text
        startPauseButton.setText("Start");

        resetCheckboxStates();
        resetSwitcherStates();
        removePathingNodes();
        resetStartAndGoalNodes();
        removeWalls();

        // reset state
        currentPathingState = PathingState.NOT_STARTED;
        updateStateLabel();

        // reset initial grid length
        pathfindingGrid.setNodeDimensionLength(DEFAULT_NODES);

        // reset slider value
        speedSlider.setValue(DEFAULT_SLIDER_VALUE);

        // ensure nodes can be drawn on the grid
        pathfindingGrid.installClickAndDragPlacer();

        pathfindingGrid.setResizable(true);

        // finally repaint grid
        lockingRepaintGrid();
    }

    /**
     * The semaphore used to achieve thread safety when
     * adding/removing to/from the grid and repainting.
     */
    private static final Semaphore semaphore = new Semaphore(1);

    /**
     * Repaints the pathfinding grid in a thread-safe way.
     */
    private static void lockingRepaintGrid() {
        try {
            semaphore.acquire();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        pathfindingGrid.repaint();
        semaphore.release();
    }

    /**
     * Adds the node to the pathfinding grid in thread-safe way.
     *
     * @param node the node to add to the grid
     */
    private static void lockingAddNode(GridNode node) {
        try {
            semaphore.acquire();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        pathfindingGrid.addNode(node);
        semaphore.release();
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
        // algorithm on corresponds to Dijkstras
        return algorithmSwitch.getState() == CyderSwitch.State.ON
                ? 1 : (heuristicSwitch.getState() == CyderSwitch.State.OFF
                ? manhattanDistance(n, goalNode) : euclideanDistance(n, goalNode));
    }

    /**
     * Calcualtes the g cost from the provided node to the start node.
     * This uses Euclidean distance by definition of g cost.
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
