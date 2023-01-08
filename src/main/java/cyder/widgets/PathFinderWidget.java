package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.math.NumberUtil;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.UiUtil;
import cyder.ui.button.CyderButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.frame.CyderFrame;
import cyder.ui.grid.CyderGrid;
import cyder.ui.grid.GridNode;
import cyder.ui.label.CyderLabel;
import cyder.ui.selection.CyderCheckbox;
import cyder.ui.selection.CyderCheckboxGroup;
import cyder.ui.selection.CyderSwitch;
import cyder.ui.selection.CyderSwitchState;
import cyder.ui.slider.CyderSliderUi;
import cyder.ui.slider.ThumbShape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A pathfinding widget to visualize Dijkstra's path finding algorithm and the A* algorithm
 * with Euclidean distance and Manhattan distance as valid A* heuristics.
 */
@Vanilla
@CyderAuthor
public final class PathFinderWidget {
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
     * The algorithm switcher to switch between A* and Dijkstra's.
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
    private static final int PATH_TRICKLE_TIMEOUT = 30;

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
        PATH_FOUND("Path found"),
        /**
         * The algorithm is finished but no path was found. :(
         */
        PATH_NOT_FOUND("No path found"),
        /**
         * The algorithm is incomplete and may be resumed.
         */
        PAUSED("Paused"),
        /**
         * The algorithm has not yet begun (Widget just opened or reset invoked).
         */
        NOT_STARTED("Not Started"),
        /**
         * The algorithm is currently underway, whether this be the first time it
         * has begun, or the 1000th time it has been paused and resumed.
         */
        RUNNING("Running...");

        /**
         * The state label text for this pathing state.
         */
        private final String stateLabelText;

        PathingState(String stateLabelText) {
            this.stateLabelText = stateLabelText;
        }

        /**
         * Returns the state label text for this pathing state.
         *
         * @return the state label text for this pathing state
         */
        public String getStateLabelText() {
            return stateLabelText;
        }
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
     * The color used for pathable nodes that have been removed from the open list.
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
     * By default this is the bottom right corner (DEFAULT_NODES - 1, DEFAULT_NODES - 1).
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
     * The current path animation object.
     * This is always killed before being set to a new object,
     * similar to how things are handled in AudioPlayer.
     */
    private static PathTrickleAnimator currentPathAnimator;

    /**
     * The path solver thread name.
     */
    private static final String PATH_SOLVING_THREAD_NAME = "Path Solver";

    /**
     * The semaphore used to achieve thread safety when
     * adding/removing to/from the grid and repainting.
     */
    private static final Semaphore semaphore = new Semaphore(1);

    /**
     * The heuristic value for A* to be logically equivalent to Dijkstra's.
     */
    private static final int DIJKSTRA_HEURISTIC = 1;

    /**
     * The width of the frame.
     */
    private static final int FRAME_WIDTH = 1000;

    /**
     * The height of the frame.
     */
    private static final int FRAME_HEIGHT = 1070;

    /**
     * The widget title.
     */
    private static final String TITLE = "Pathfinding Visualizer";

    /**
     * The length of the grid component.
     */
    private static final int gridComponentLength = 800;

    /**
     * The text for the start button.
     */
    private static final String START = "Start";

    /**
     * The text for the reset button.
     */
    private static final String RESET = "Reset";

    /**
     * The text for the stop button.
     */
    private static final String STOP = "Stop";

    /**
     * The state label string prefix.
     */
    private static final String STATE = "State:";

    /**
     * The text for the resume button.
     */
    private static final String RESUME = "Resume";

    /**
     * Suppress default constructor.
     */
    private PathFinderWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = {"path", "pathfinder", "A*"},
            description = "A pathfinding visualizer for A* and Dijkstras algorithms")
    public static void showGui() {
        UiUtil.closeIfOpen(pathFindingFrame);

        pathFindingFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT);
        pathFindingFrame.setTitle(TITLE);

        pathfindingGrid = new CyderGrid(DEFAULT_NODES, gridComponentLength);
        pathfindingGrid.setBounds(100, 80, gridComponentLength, gridComponentLength);
        pathfindingGrid.setMinNodes(DEFAULT_NODES);
        pathfindingGrid.setMaxNodes(MAX_NODES);
        pathfindingGrid.setDrawGridLines(true);
        pathfindingGrid.setBackground(CyderColors.vanilla);
        pathfindingGrid.setResizable(true);
        pathfindingGrid.addOnResizeCallback(() -> {
            ArrayList<GridNode> goals = pathfindingGrid.getNodesOfColor(goalNodeColor);
            if (goals.size() == 1) {
                GridNode localGoal = goals.get(0);
                int maxGoalCoordinate = Math.max(localGoal.getX(), localGoal.getY());
                if (maxGoalCoordinate >= pathfindingGrid.getNodeDimensionLength()) {
                    pathfindingGrid.removeNodesOfColor(goalNodeColor);
                }
            }

            ArrayList<GridNode> starts = pathfindingGrid.getNodesOfColor(startNodeColor);
            if (starts.size() == 1) {
                GridNode localStart = starts.get(0);
                int maxStartCoordinate = Math.max(localStart.getX(), localStart.getY());
                if (maxStartCoordinate >= pathfindingGrid.getNodeDimensionLength()) {
                    pathfindingGrid.removeNodesOfColor(startNodeColor);
                }
            }
        });
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
        placeStartBox.addMouseListener(placeStartBoxMouseListener);

        CyderLabel placeGoalLabel = new CyderLabel("Goal");
        placeGoalLabel.setBounds(startX - 50 + 80, startY + 5, 150, 40);
        pathFindingFrame.getContentPane().add(placeGoalLabel);

        placeGoalBox = new CyderCheckbox();
        placeGoalBox.setToolTipText("Place goal node");
        placeGoalBox.setBounds(startX + 80, startY + 40, 50, 50);
        pathFindingFrame.getContentPane().add(placeGoalBox);
        placeGoalBox.addMouseListener(placeGoalMouseListener);

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
        deleteWallsCheckBox.addMouseListener(deleteWallsMouseListener);

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
        drawGridLinesBox.addMouseListener(drawGridLinesMouseListener);
        pathFindingFrame.getContentPane().add(drawGridLinesBox);

        CyderButton reset = new CyderButton(RESET);
        reset.setBounds(350, startY + 40 - 20, 180, 50);
        reset.addActionListener(e -> reset());
        pathFindingFrame.getContentPane().add(reset);

        startPauseButton = new CyderButton(START);
        startPauseButton.setBounds(350, startY + 40 + 80, 180, 50);
        startPauseButton.addActionListener(e -> startPauseButtonAction());
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
        CyderSliderUi speedSliderUi = new CyderSliderUi(speedSlider);
        speedSliderUi.setThumbStroke(new BasicStroke(2.0f));
        speedSliderUi.setThumbShape(ThumbShape.RECTANGLE);
        speedSliderUi.setThumbFillColor(Color.black);
        speedSliderUi.setThumbOutlineColor(CyderColors.navy);
        speedSliderUi.setRightThumbColor(CyderColors.regularBlue);
        speedSliderUi.setLeftThumbColor(CyderColors.regularPink);
        speedSliderUi.setTrackStroke(new BasicStroke(3.0f));
        speedSlider.setUI(speedSliderUi);
        speedSlider.setBounds(350, startY + 40 + 35, 350 + 180 + 20, 40);
        speedSlider.setPaintTicks(false);
        speedSlider.setPaintLabels(false);
        speedSlider.setVisible(true);
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Pathfinding Speed");
        speedSlider.setFocusable(false);
        pathFindingFrame.getContentPane().add(speedSlider);

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
     * The mouse listener for the place start checkbox.
     */
    private static final MouseListener placeStartBoxMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (placeStartBox.isChecked()) {
                pathfindingGrid.setNodeColor(startNodeColor);

                pathfindingGrid.removeInvokeWhenNodePlacedRunnables();
                pathfindingGrid.invokeWhenNodePlaced(() -> {
                    pathfindingGrid.removeNodesOfColor(startNodeColor);

                    placeStartBox.setNotChecked();
                    pathfindingGrid.setNodeColor(wallsColor);
                });

                deleteWallsCheckBox.setNotChecked();
                pathfindingGrid.setMode(CyderGrid.Mode.ADD);
            } else {
                pathfindingGrid.removeInvokeWhenNodePlacedRunnables();
                pathfindingGrid.setNodeColor(wallsColor);
            }
        }
    };

    /**
     * The mouse listener for the place goal checkbox.
     */
    private static final MouseListener placeGoalMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (placeGoalBox.isChecked()) {
                pathfindingGrid.setNodeColor(goalNodeColor);

                pathfindingGrid.removeInvokeWhenNodePlacedRunnables();
                pathfindingGrid.invokeWhenNodePlaced(() -> {
                    pathfindingGrid.removeNodesOfColor(goalNodeColor);

                    placeGoalBox.setNotChecked();
                    pathfindingGrid.setNodeColor(wallsColor);
                });

                deleteWallsCheckBox.setNotChecked();
                pathfindingGrid.setMode(CyderGrid.Mode.ADD);
            } else {
                pathfindingGrid.removeInvokeWhenNodePlacedRunnables();
                pathfindingGrid.setNodeColor(wallsColor);
            }
        }
    };

    /**
     * The mouse listener for the delete walls checkbox.
     */
    private static final MouseListener deleteWallsMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            if (pathfindingGrid.getMode() == CyderGrid.Mode.ADD) {
                pathfindingGrid.setMode(CyderGrid.Mode.DELETE);
            } else {
                pathfindingGrid.setMode(CyderGrid.Mode.ADD);
            }
        }
    };

    /**
     * The mouse listener for the draw grid lines checkbox.
     */
    private static final MouseListener drawGridLinesMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            pathfindingGrid.setDrawGridLines(drawGridLinesBox.isChecked());
        }
    };

    /**
     * The actions to invoke when the start/pause button is pressed.
     */
    private static void startPauseButtonAction() {
        if (pathfindingGrid.getNodesOfColor(startNodeColor).isEmpty()) {
            pathFindingFrame.notify("Start node not set");
            return;
        }

        if (pathfindingGrid.getNodesOfColor(goalNodeColor).isEmpty()) {
            pathFindingFrame.notify("End node not set");
            return;
        }

        if (currentPathingState == PathingState.RUNNING) {
            currentPathingState = PathingState.PAUSED;
            startPauseButton.setText(RESUME);
            updateStateLabel();
            enableUiElements();
            return;
        }

        disableUiElements();
        startPauseButton.setText(STOP);

        if (currentPathingState == PathingState.PAUSED) {
            currentPathingState = PathingState.RUNNING;
            startPathStepLoop();
        } else {
            searchSetup();
        }
    }

    /**
     * Performs the setup necessary to start path finding such as
     * initializing the lists, finding the start and goal nodes,
     * and converting GridNodes to PathNodes.
     */
    private static void searchSetup() {
        /*
        This method is only invoked if start and goal nodes are set.
         */

        endPathAnimator();
        removePathingNodes();

        ArrayList<GridNode> walls = pathfindingGrid.getNodesOfColor(wallsColor);

        GridNode gridGoal = pathfindingGrid.getNodesOfColor(goalNodeColor).get(0);
        goalNode = new PathNode(gridGoal.getX(), gridGoal.getY());
        goalNode.setParent(null);

        GridNode gridStart = pathfindingGrid.getNodesOfColor(startNodeColor).get(0);
        startNode = new PathNode(gridStart.getX(), gridStart.getY());
        startNode.setParent(null);

        pathableNodes.clear();
        for (int x = 0 ; x < pathfindingGrid.getNodeDimensionLength() ; x++) {
            for (int y = 0 ; y < pathfindingGrid.getNodeDimensionLength() ; y++) {
                GridNode node = new GridNode(x, y);

                /*
                Ignore walls for pathable nodes.

                Note to maintainers: if future node types are added and should not be pathable,
                 they should be tested for here.
                 */
                if (walls.contains(node)) {
                    continue;
                }

                pathableNodes.add(new PathNode(node.getX(), node.getY()));
            }
        }

        startNode.setG(0);
        startNode.setH(heuristic(goalNode));

        openNodes.clear();
        openNodes.add(startNode);

        currentPathingState = PathingState.RUNNING;

        disableUiElements();

        pathfindingGrid.setResizable(false);

        startPathStepLoop();
    }

    /**
     * Starts the main while loop which takes path steps until a path is
     * found or all reachable nodes have been checked.
     * All setup of lists must be performed before invoking this method.
     */
    private static void startPathStepLoop() {
        updateStateLabel();

        CyderThreadRunner.submit(() -> {
            while (currentPathingState == PathingState.RUNNING) {
                pathStep();

                if (showStepsBox.isChecked()) {
                    lockingRepaintGrid();
                    ThreadUtil.sleep(MAX_SLIDER_VALUE - speedSlider.getValue());
                }
            }
        }, PATH_SOLVING_THREAD_NAME);
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

            LinkedList<PathNode> neighbors = new LinkedList<>();
            for (PathNode possibleNeighbor : pathableNodes) {
                if (areOrthogonalNeighbors(possibleNeighbor, min)
                        || (areDiagonalNeighbors(possibleNeighbor, min) && diagonalBox.isChecked())) {
                    neighbors.add(possibleNeighbor);
                }
            }

            neighbors.forEach(neighbor -> {
                double currentHCost = heuristic(neighbor);

                if (currentHCost < neighbor.getH()) {
                    neighbor.setH(currentHCost);
                    neighbor.setParent(min);
                    neighbor.setG(min.getG() + euclideanDistance(min, neighbor));

                    if (!openNodes.contains(neighbor)) {
                        openNodes.add(neighbor);
                    }
                }
            });

            // Refresh grid colors based on current state of algorithm and nodes checked.
            pathableNodes.stream().filter(pathNode -> !pathNode.equals(startNode) && !pathNode.equals(goalNode))
                    .forEach(pathNode -> {
                        int x = pathNode.getX();
                        int y = pathNode.getY();

                        if (openNodes.contains(pathNode)) {
                            lockingAddNode(new GridNode(pathableOpenColor, x, y));
                        } else if (pathNode.getParent() != null) {
                            lockingAddNode(new GridNode(pathableClosedColor, x, y));
                        }
                    });
        } else {
            pathNotFound();
        }
    }

    /**
     * Performs the actions necessary following a path
     * from the start to the goal node being found.
     */
    private static void pathFound() {
        currentPathingState = PathingState.PATH_FOUND;

        startPauseButton.setText(START);

        enableUiElements();
        updateStateLabel();

        pathfindingGrid.setResizable(true);

        ArrayList<Point> pathForward = new ArrayList<>();

        PathNode refNode = goalNode.getParent();
        while (refNode != startNode) {
            pathForward.add(new Point(refNode.getX(), refNode.getY()));
            refNode = refNode.getParent();
        }

        ArrayList<Point> pathReversed = new ArrayList<>();

        for (int i = pathForward.size() - 1 ; i > -1 ; i--) {
            pathReversed.add(pathForward.get(i));
        }

        currentPathAnimator = new PathTrickleAnimator(pathReversed);
    }

    /**
     * A animator class to perform the path found animation.
     */
    private static class PathTrickleAnimator {
        /**
         * The trickle animation thread name.
         */
        private static final String PATH_TRICKLE_ANIMATION_THREAD_NAME = "Pathfinding Path Trickle Animator";

        /**
         * The color used for the found path.
         */
        private static final Color PATH_COLOR = CyderColors.regularBlue;

        /**
         * The color used for the path found animation trickle.
         */
        private static final Color PATH_ANIMATION_COLOR = new Color(34, 216, 248);

        /**
         * Whether this animation has been killed
         */
        private final AtomicBoolean killed = new AtomicBoolean(false);

        /**
         * Constructs and starts a new path animator.
         *
         * @param pathPoints the list of points to animate
         */
        public PathTrickleAnimator(ArrayList<Point> pathPoints) {
            CyderThreadRunner.submit(() -> {
                try {
                    // Draw initial path from start to goal
                    for (Point pathPoint : pathPoints) {
                        if (killed.get()) return;

                        GridNode updateNode = null;

                        for (GridNode node : pathfindingGrid.getGridNodes()) {
                            if (killed.get()) return;

                            if (node.getX() == pathPoint.getX() && node.getY() == pathPoint.getY()) {
                                updateNode = node;
                                break;
                            }
                        }

                        Color color = updateNode != null ? updateNode.getColor() : null;
                        if (color != null
                                && (color.equals(PATH_ANIMATION_COLOR) || color.equals(pathableClosedColor))) {

                            int x = updateNode.getX();
                            int y = updateNode.getY();
                            lockingAddNode(new GridNode(PATH_ANIMATION_COLOR, x, y));

                            lockingRepaintGrid();
                            ThreadUtil.sleep(PATH_TRICKLE_TIMEOUT);
                        }
                    }

                    while (true) {
                        // Trickle from start to goal
                        for (Point pathPoint : pathPoints) {
                            if (killed.get()) return;

                            Optional<GridNode> overridePoint = pathfindingGrid.getNodeAtPoint(pathPoint);
                            if (overridePoint.isPresent()
                                    && (overridePoint.get().getColor().equals(PATH_ANIMATION_COLOR)
                                    || overridePoint.get().getColor().equals(PATH_COLOR))) {
                                int x = (int) pathPoint.getX();
                                int y = (int) pathPoint.getY();
                                lockingAddNode(new GridNode(PATH_COLOR, x, y));

                                lockingRepaintGrid();
                            }

                            if (killed.get()) return;
                            ThreadUtil.sleep(PATH_TRICKLE_TIMEOUT);
                            if (killed.get()) return;

                            overridePoint = pathfindingGrid.getNodeAtPoint(pathPoint);
                            if (overridePoint.isPresent()
                                    && (overridePoint.get().getColor().equals(PATH_ANIMATION_COLOR)
                                    || overridePoint.get().getColor().equals(PATH_COLOR))) {

                                int x = (int) pathPoint.getX();
                                int y = (int) pathPoint.getY();
                                lockingAddNode(new GridNode(PATH_ANIMATION_COLOR, x, y));
                                lockingRepaintGrid();
                            }
                        }

                        if (killed.get()) return;

                        // Trickle from goal to start
                        for (int i = pathPoints.size() - 1 ; i >= 0 ; i--) {
                            Optional<GridNode> overridePoint
                                    = pathfindingGrid.getNodeAtPoint(pathPoints.get(i));
                            if (overridePoint.isPresent()
                                    && (overridePoint.get().getColor().equals(PATH_ANIMATION_COLOR)
                                    || overridePoint.get().getColor().equals(PATH_COLOR))) {
                                lockingAddNode(new GridNode(PATH_COLOR,
                                        (int) pathPoints.get(i).getX(),
                                        (int) pathPoints.get(i).getY()));
                                lockingRepaintGrid();
                            }

                            if (killed.get()) return;
                            ThreadUtil.sleep(PATH_TRICKLE_TIMEOUT);
                            if (killed.get()) return;

                            overridePoint = pathfindingGrid.getNodeAtPoint(pathPoints.get(i));
                            if (overridePoint.isPresent()
                                    && (overridePoint.get().getColor().equals(PATH_ANIMATION_COLOR)
                                    || overridePoint.get().getColor().equals(PATH_COLOR))) {
                                lockingAddNode(new GridNode(PATH_ANIMATION_COLOR,
                                        (int) pathPoints.get(i).getX(),
                                        (int) pathPoints.get(i).getY()));
                                lockingRepaintGrid();
                            }

                            if (killed.get()) return;
                        }
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
                lockingRepaintGrid();
            }, PATH_TRICKLE_ANIMATION_THREAD_NAME);
        }

        /**
         * Kills this path animator.
         */
        public void kill() {
            killed.set(true);
        }
    }

    /**
     * Performs the actions necessary following a path
     * from the start to the goal node was not found.
     */
    private static void pathNotFound() {
        currentPathingState = PathingState.PATH_NOT_FOUND;

        updateStateLabel();

        startPauseButton.setText(START);

        enableUiElements();

        pathfindingGrid.setResizable(true);

        lockingRepaintGrid();
    }

    /**
     * Enables the UI elements during the pathfinding animation.
     */
    private static void enableUiElements() {
        setUiElementsEnabled(true);
    }

    /**
     * Disables the UI elements during the pathfinding animation.
     */
    private static void disableUiElements() {
        setUiElementsEnabled(false);
    }

    /**
     * Sets whether the ui elements are enabled.
     *
     * @param enabled whether the ui elements are enabled
     */
    @ForReadability
    private static void setUiElementsEnabled(boolean enabled) {
        deleteWallsCheckBox.setEnabled(enabled);
        showStepsBox.setEnabled(enabled);
        diagonalBox.setEnabled(enabled);
        placeStartBox.setEnabled(enabled);
        placeGoalBox.setEnabled(enabled);
        drawGridLinesBox.setEnabled(enabled);

        heuristicSwitch.setEnabled(enabled);
        algorithmSwitch.setEnabled(enabled);

        if (enabled) {
            pathfindingGrid.installClickAndDragPlacer();
        } else {
            pathfindingGrid.uninstallClickAndDragPlacer();
        }
    }

    /**
     * Resets all the checkboxes to their default state.
     */
    private static void resetCheckboxStates() {
        deleteWallsCheckBox.setChecked(false);
        showStepsBox.setChecked(false);
        diagonalBox.setChecked(false);
        placeStartBox.setChecked(false);
        placeGoalBox.setChecked(false);
        drawGridLinesBox.setChecked(true);
        pathfindingGrid.setDrawGridLines(true);
    }

    /**
     * Resets the algorithm and heuristic switchers to their default states.
     */
    private static void resetSwitcherStates() {
        // Corresponds to Manhattan
        heuristicSwitch.setState(CyderSwitchState.OFF);
        // Corresponds to A*
        algorithmSwitch.setState(CyderSwitchState.OFF);
    }

    /**
     * Updates the state label based.
     */
    private static void updateStateLabel() {
        currentStateLabel.setText(STATE + CyderStrings.space + currentPathingState.getStateLabelText());
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
        if (currentPathAnimator == null) return;

        currentPathAnimator.kill();
        currentPathAnimator = null;

        pathfindingGrid.removeNodesOfColor(PathTrickleAnimator.PATH_COLOR);
        pathfindingGrid.removeNodesOfColor(PathTrickleAnimator.PATH_ANIMATION_COLOR);

    }

    /**
     * Resets the visualizer as if the widget was just opened.
     */
    public static void reset() {
        endPathAnimator();

        enableUiElements();

        startPauseButton.setText(START);

        resetCheckboxStates();
        resetSwitcherStates();
        removePathingNodes();
        resetStartAndGoalNodes();
        removeWalls();

        currentPathingState = PathingState.NOT_STARTED;
        updateStateLabel();

        pathfindingGrid.setNodeDimensionLength(DEFAULT_NODES);

        speedSlider.setValue(DEFAULT_SLIDER_VALUE);

        pathfindingGrid.installClickAndDragPlacer();

        pathfindingGrid.setResizable(true);

        lockingRepaintGrid();
    }

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
     * @param node1 the first node
     * @param node2 the second node
     * @return whether the provided nodes are diagonal neighbors
     */
    private static boolean areDiagonalNeighbors(PathNode node1, PathNode node2) {
        return (node1.getX() == node2.getX() + 1 && node1.getY() == node2.getY() + 1)
                || (node1.getX() == node2.getX() + 1 && node1.getY() == node2.getY() - 1)
                || (node1.getX() == node2.getX() - 1 && node1.getY() == node2.getY() - 1)
                || (node1.getX() == node2.getX() - 1 && node1.getY() == node2.getY() + 1);
    }

    /**
     * Returns whether the provided nodes are orthogonal neighbors.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @return whether the provided nodes are orthogonal neighbors
     */
    private static boolean areOrthogonalNeighbors(PathNode node1, PathNode node2) {
        return (node1.getX() == node2.getX() && node1.getY() == node2.getY() + 1)
                || (node1.getX() == node2.getX() && node1.getY() == node2.getY() - 1)
                || (node1.getX() == node2.getX() + 1 && node1.getY() == node2.getY())
                || (node1.getX() == node2.getX() - 1 && node1.getY() == node2.getY());
    }

    /**
     * Calculates the heuristic from the provided node to the goal node
     * using the currently set heuristic.
     *
     * @param node the node to calculate the heuristic of
     * @return the cost to path from the provided node to the goal
     */
    private static double heuristic(PathNode node) {
        boolean dijkstrasAlgorithm = algorithmSwitch.getState() == CyderSwitchState.ON;
        boolean euclideanDistance = heuristicSwitch.getState() == CyderSwitchState.ON;
        if (dijkstrasAlgorithm) {
            return DIJKSTRA_HEURISTIC;
        } else if (euclideanDistance) {
            return euclideanDistance(node, goalNode);
        } else {
            return manhattanDistance(node, goalNode);
        }
    }

    /**
     * Calculates the g cost from the provided node to the start node.
     * This uses Euclidean distance by definition of g cost.
     *
     * @param node the node to calculate the g cost of
     * @return the g cost of the provided node
     */
    private static double calcGCost(PathNode node) {
        return euclideanDistance(node, startNode);
    }

    /**
     * Returns the Euclidean distance between the two nodes.
     *
     * @param node1 the first noDe
     * @param node2 the second node
     * @return the Euclidean distance between the two nodes
     */
    private static double euclideanDistance(PathNode node1, PathNode node2) {
        return NumberUtil.calculateMagnitude(node1.getX() - node2.getX(),
                node1.getY() - node2.getY());
    }

    /**
     * Returns the Manhattan distance between the two nodes.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @return the Manhattan distance between the two nodes
     */
    private static double manhattanDistance(PathNode node1, PathNode node2) {
        return Math.abs(node1.getX() - node2.getX()) + Math.abs(node1.getY() - node2.getY());
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

    /**
     * A node object used for the pathfinding widget.
     */
    private static class PathNode {
        /**
         * The node's x value.
         */
        private int x;

        /**
         * The node's y value.
         */
        private int y;

        /**
         * The node's g value.
         */
        private double g = Integer.MAX_VALUE;

        /**
         * The node's heuristic value.
         */
        private double h = Integer.MAX_VALUE;

        /**
         * The node's parent.
         */
        private PathNode parent;

        /**
         * Constructs a new path node.
         *
         * @param x the initial x value
         * @param y the initial y value
         */
        public PathNode(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Suppress default constructor.
         */
        private PathNode() {
            throw new IllegalMethodException("Cannot create PathNode with default constructor");
        }

        /**
         * Constructs a new path node.
         *
         * @param p the point to use as the initial x,y
         */
        public PathNode(Point p) {
            this(p.x, p.y);
        }

        /**
         * Returns the x of the node.
         *
         * @return the x of the node
         */
        public int getX() {
            return x;
        }

        /**
         * Sets the x of the node.
         *
         * @param x the x of the node
         */
        public void setX(int x) {
            this.x = x;
        }

        /**
         * Returns the y of the node.
         *
         * @return the y of the node
         */
        public int getY() {
            return y;
        }

        /**
         * Sets the y of the node.
         *
         * @param y the y of the node
         */
        public void setY(int y) {
            this.y = y;
        }

        /**
         * Returns the g cost of the node.
         *
         * @return the g cost of the node
         */
        public double getG() {
            return g;
        }

        /**
         * Sets the g cost of the node.
         *
         * @param g the g cost of the node
         */
        public void setG(double g) {
            this.g = g;
        }

        /**
         * Returns the h cost of the node.
         *
         * @return the h cost of the node
         */
        public double getH() {
            return h;
        }

        /**
         * Sets the h cost of the node.
         *
         * @param h the h cost of the node
         */
        public void setH(double h) {
            this.h = h;
        }

        /**
         * Returns the f cost of the node.
         *
         * @return the f cost of the node
         */
        public double getF() {
            return h + g;
        }

        /**
         * Returns the parent of the node.
         *
         * @return the parent of the node
         */
        public PathNode getParent() {
            return parent;
        }

        /**
         * Sets the parent of the node.
         *
         * @param parent the parent of the node
         */
        public void setParent(PathNode parent) {
            this.parent = parent;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (!(o instanceof PathNode other))
                return false;
            else {
                return other.getX() == x && other.getY() == y;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return x + ", " + y;
        }
    }
}