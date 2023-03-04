package cyder.widgets;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.*;
import cyder.constants.CyderColors;
import cyder.enumerations.CyderInspection;
import cyder.exceptions.IllegalMethodException;
import cyder.layouts.CyderPartitionedLayout;
import cyder.strings.CyderStrings;
import cyder.ui.UiUtil;
import cyder.ui.button.CyderButton;
import cyder.ui.frame.CyderFrame;
import cyder.ui.grid.CyderGrid;
import cyder.ui.grid.GridNode;
import cyder.ui.pane.CyderPanel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * Convex hull widget that solve a convex hull problem using a CyderGrid as the drawing label.
 */
@Vanilla
@CyderAuthor
public final class ConvexHullWidget {
    /**
     * The CyderFrame to use for the convex hull widget.
     */
    private static CyderFrame hullFrame;

    /**
     * The grid to use to represent points in space.
     */
    private static CyderGrid gridComponent;

    /**
     * Suppress default constructor.
     */
    private ConvexHullWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The title of the frame.
     */
    private static final String FRAME_TITLE = "Convex Hull";

    /**
     * The solve button text.
     */
    private static final String SOLVE = "Solve";

    /**
     * The reset button text.
     */
    private static final String RESET = "Reset";

    /**
     * The number of grid nodes.
     */
    private static final int GRID_NODES = 175;

    /**
     * The length of the grid.
     */
    private static final int GRID_LENGTH = 700;

    /**
     * The padding around the edges of the grid.
     */
    private static final int GRID_PADDING = 3;

    /**
     * The size of the parent component for the grid.
     */
    private static final int GRID_PARENT_LEN = GRID_LENGTH + 2 * GRID_PADDING;

    /**
     * The width of the frame.
     */
    private static final int FRAME_WIDTH = 800;

    /**
     * The height of the frame.
     */
    private static final int FRAME_HEIGHT = 850;

    /**
     * The size of the buttons.
     */
    private static final Dimension BUTTON_SIZE = new Dimension(300, 40);

    /**
     * The top and bottom button padding.
     */
    private static final int BUTTON_Y_PADDING = 10;

    /**
     * The color of the nodes the user places on the grid.
     */
    private static final Color PLACED_NODE_COLOR = CyderColors.regularPink;

    /**
     * The color of the nodes placed by the algorithm.
     */
    private static final Color WALL_NODE_COLOR = CyderColors.navy;

    /**
     * The border for the grid component.
     */
    private static final LineBorder GRID_PARENT_BORDER = new LineBorder(CyderColors.navy, GRID_PADDING);

    /**
     * The minimum number of points required to form a polygon in 2D space.
     */
    private static final int MIN_POLYGON_POINTS = 3;

    /**
     * The text to display in a notification if the four corners of the grid are occupied.
     */
    private static final String FOUR_CORNERS = "Congratulations, you played yourself";

    /**
     * Shows the convex hull widget.
     */
    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = {"convex", "convex hull"}, description = "A convex hull algorithm visualizer")
    public static void showGui() {
        UiUtil.closeIfOpen(hullFrame);

        hullFrame = new CyderFrame.Builder()
                .setWidth(FRAME_WIDTH)
                .setHeight(FRAME_HEIGHT)
                .setTitle(FRAME_TITLE)
                .build();

        JLabel gridComponentParent = new JLabel();
        gridComponentParent.setSize(GRID_PARENT_LEN, GRID_PARENT_LEN);
        gridComponentParent.setBorder(GRID_PARENT_BORDER);

        gridComponent = new CyderGrid(GRID_NODES, GRID_LENGTH);
        gridComponent.setDrawGridLines(false);
        gridComponent.setBounds(GRID_PADDING, GRID_PADDING, GRID_LENGTH, GRID_LENGTH);
        gridComponent.setResizable(false);
        gridComponent.setNodeColor(PLACED_NODE_COLOR);
        gridComponent.setBackground(CyderColors.vanilla);
        gridComponent.installClickListener();
        gridComponent.installDragListener();
        gridComponent.setSaveStates(false);
        gridComponentParent.add(gridComponent);

        CyderPartitionedLayout buttonPartitionedLayout = new CyderPartitionedLayout();
        buttonPartitionedLayout.setPartitionDirection(CyderPartitionedLayout.PartitionDirection.ROW);

        buttonPartitionedLayout.spacer(10);
        CyderButton solveButton = new CyderButton(SOLVE);
        solveButton.setSize(BUTTON_SIZE);
        solveButton.addActionListener(e -> solveButtonAction());
        buttonPartitionedLayout.addComponent(solveButton, 40);

        CyderButton resetButton = new CyderButton(RESET);
        resetButton.addActionListener(e -> reset());
        resetButton.setSize(BUTTON_SIZE);
        buttonPartitionedLayout.addComponent(resetButton, 40);
        buttonPartitionedLayout.spacer(10);

        CyderPartitionedLayout partitionedLayout = new CyderPartitionedLayout();
        partitionedLayout.spacer(10);

        partitionedLayout.addComponent(gridComponentParent, 70);

        partitionedLayout.spacer(10);

        CyderPanel buttonPanel = new CyderPanel(buttonPartitionedLayout);
        buttonPanel.setSize(FRAME_WIDTH, (int) (BUTTON_SIZE.getHeight() + 2 * BUTTON_Y_PADDING));
        partitionedLayout.addComponent(buttonPanel, 10);

        hullFrame.setCyderLayout(partitionedLayout);
        hullFrame.finalizeAndShow();
    }

    /**
     * The actions to invoke when the solve button is pressed.
     */
    private static void solveButtonAction() {
        gridComponent.setGridNodes(new ArrayList<>(gridComponent.getNodesOfColor(PLACED_NODE_COLOR)));

        if (gridComponent.getNodeCount() < MIN_POLYGON_POINTS) {
            hullFrame.notify(MIN_POLYGON_POINTS + " points are required to create a polygon in 2D space");
            return;
        }

        ArrayList<Point> points = new ArrayList<>();
        gridComponent.getGridNodes().forEach(node -> points.add(new Point(node.getX(), node.getY())));

        ImmutableList<Point> hull = solveGrahamScan(points);
        connectHullPointsWithLines(hull);
        checkFourCorners();
        gridComponent.repaint();
    }

    /**
     * Connects all points contained within the provided list with lines on the grid.
     *
     * @param hullPoints the points of the convex hull to connect with lines
     */
    private static void connectHullPointsWithLines(ImmutableList<Point> hullPoints) {
        Preconditions.checkNotNull(hullPoints);
        Preconditions.checkArgument(hullPoints.size() >= MIN_POLYGON_POINTS);

        for (int i = 0 ; i < hullPoints.size() ; i++) {
            Point firstPoint = hullPoints.get(i);
            int secondPointIndex = (i == hullPoints.size() - 1) ? 0 : i + 1;
            Point secondPoint = hullPoints.get(secondPointIndex);

            addMidPointsToGrid(firstPoint, secondPoint);
        }
    }

    /**
     * Checks to see if the four corner grid nodes have a user-placed node in them.
     */
    @ForReadability
    private static void checkFourCorners() {
        int min = 0;
        int max = gridComponent.getNodeDimensionLength() - 1;
        ImmutableList<GridNode> cornerNodes = ImmutableList.of(
                /* Color is irrelevant here */
                new GridNode(WALL_NODE_COLOR, min, min),
                new GridNode(WALL_NODE_COLOR, min, max),
                new GridNode(WALL_NODE_COLOR, max, min),
                new GridNode(WALL_NODE_COLOR, max, max)
        );

        if (gridComponent.getGridNodes().containsAll(cornerNodes)) {
            hullFrame.notify(FOUR_CORNERS);
        }
    }

    /**
     * Recursively finds the middle point between the provided points and adds
     * it to the grid meaning a line between the original two provided points
     * is created on the grid.
     *
     * @param firstPoint  the first point
     * @param secondPoint the second point
     */
    private static void addMidPointsToGrid(Point firstPoint, Point secondPoint) {
        Preconditions.checkNotNull(firstPoint);
        Preconditions.checkNotNull(secondPoint);

        if (firstPoint.equals(secondPoint)) return;

        int midXPoints = (int) (secondPoint.getX() + firstPoint.getX()) / 2;
        int midYPoints = (int) (secondPoint.getY() + firstPoint.getY()) / 2;
        Point midPoint = new Point(midXPoints, midYPoints);

        if (midPoint.equals(firstPoint) || midPoint.equals(secondPoint)) return;

        GridNode gridMidPoint = new GridNode(WALL_NODE_COLOR, midXPoints, midYPoints);
        if (gridComponent.contains(gridMidPoint)) {
            gridComponent.removeNode(gridMidPoint);
        }
        gridComponent.addNode(gridMidPoint);

        addMidPointsToGrid(firstPoint, midPoint);
        addMidPointsToGrid(midPoint, secondPoint);
    }

    /**
     * Solves the convex hull problem given the list of points.
     *
     * @param points the list of points
     * @return the points in the convex hull
     */
    private static ImmutableList<Point> solveGrahamScan(ArrayList<Point> points) {
        Deque<Point> stack = new ArrayDeque<>();

        Point bottomLeftMostPoint = getBottomLeftMostPoint(points);
        sortPointsByAngle(points, bottomLeftMostPoint);

        int pointIndex = 0;
        stack.push(points.get(pointIndex++));
        stack.push(points.get(pointIndex++));

        for (int i = pointIndex, size = points.size() ; i < size ; i++) {
            Point next = points.get(i);
            Point poppedPoint = stack.pop();

            while (stack.peek() != null) {
                PointRotation rotation = determineRotation(stack.peek(), poppedPoint, next);
                if (rotation == PointRotation.COUNTER_CLOCK_WISE) break;
                poppedPoint = stack.pop();
            }

            stack.push(poppedPoint);
            stack.push(points.get(i));
        }

        Point poppedPoint = stack.pop();
        if (stack.peek() == null) {
            return ImmutableList.of();
        }

        PointRotation rotation = determineRotation(stack.peek(), poppedPoint, bottomLeftMostPoint);
        if (rotation == PointRotation.COUNTER_CLOCK_WISE) {
            stack.push(poppedPoint);
        }

        return ImmutableList.copyOf(stack);
    }

    /**
     * Returns the node with the minimum y value from the provided list.
     * If two points contain the same y value, the point with the minimum x value is returned.
     *
     * @param points the list of points
     * @return the bottom left most point
     */
    private static Point getBottomLeftMostPoint(ArrayList<Point> points) {
        Preconditions.checkNotNull(points);
        Preconditions.checkArgument(!points.isEmpty());

        Point min = points.get(0);
        for (Point point : points) {
            if (point.getY() <= min.getY()) {
                if (point.getY() < min.getY()) {
                    min = point;
                }
                // Choose leftmost point if same y value
                else if (point.getX() < min.getX()) {
                    min = point;
                }
            }
        }

        return min;
    }

    /**
     * The possible degrees of rotate between two points and a reference point.
     */
    private enum PointRotation {
        /**
         * The points result in a clock wise turn.
         */
        CLOCK_WISE,
        /**
         * The points result in a counter clock wise turn.
         */
        COUNTER_CLOCK_WISE,
        /**
         * The points are co-linear.
         */
        CO_LINEAR
    }

    /**
     * Determines the rotation between the first point and second point relative to the reference point.
     *
     * @param firstPoint     the first point
     * @param secondPoint    the second point
     * @param referencePoint the reference point
     * @return the rotation between the first point and second point relative to the reference point
     */
    private static PointRotation determineRotation(Point firstPoint, Point secondPoint, Point referencePoint) {
        Preconditions.checkNotNull(firstPoint);
        Preconditions.checkNotNull(secondPoint);
        Preconditions.checkNotNull(referencePoint);

        float area = (secondPoint.x - firstPoint.x) * (referencePoint.y - firstPoint.y)
                - (secondPoint.y - firstPoint.y) * (referencePoint.x - firstPoint.x);

        if (area < 0) {
            return PointRotation.CLOCK_WISE;
        } else if (area > 0) {
            return PointRotation.COUNTER_CLOCK_WISE;
        } else {
            return PointRotation.CO_LINEAR;
        }
    }

    /**
     * Sorts the list of points by angle using the reference point.
     *
     * @param points         the list of points
     * @param referencePoint the reference point
     */
    private static void sortPointsByAngle(ArrayList<Point> points, Point referencePoint) {
        Preconditions.checkNotNull(points);
        Preconditions.checkNotNull(referencePoint);

        points.sort((firstPoint, secondPoint) -> {
            if (firstPoint == referencePoint) {
                return -1;
            } else if (secondPoint == referencePoint) {
                return 1;
            } else if (firstPoint == secondPoint) {
                return 0;
            }

            PointRotation rotation = determineRotation(referencePoint, firstPoint, secondPoint);
            if (rotation == PointRotation.CO_LINEAR) {
                if (Double.compare(firstPoint.getX(), secondPoint.getX()) == 0) {
                    return firstPoint.getY() < secondPoint.getY() ? -1 : 1;
                } else {
                    return firstPoint.getX() < secondPoint.getX() ? -1 : 1;
                }
            } else if (rotation == PointRotation.CLOCK_WISE) {
                return 1;
            } else if (rotation == PointRotation.COUNTER_CLOCK_WISE) {
                return -1;
            }

            throw new IllegalStateException("Invalid rotation: " + rotation);
        });
    }

    /**
     * Resets the convex hull state and grid.
     */
    private static void reset() {
        gridComponent.clearGrid();
    }
}
