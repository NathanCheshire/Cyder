package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderGrid;
import cyder.utils.UiUtil;

import java.awt.*;
import java.util.*;

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
     * Instantiation of convex hull not allowed.
     */
    private ConvexHullWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Shows the convex hull widget.
     */
    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = {"convex", "convex hull"}, description = "A convex hull algorithm visualizer")
    public static void showGui() {
        UiUtil.closeIfOpen(hullFrame);

        hullFrame = new CyderFrame(800, 850);
        hullFrame.setTitle("Convex Hull Visualizer");

        gridComponent = new CyderGrid(100, 700);
        gridComponent.setDrawGridLines(false);
        gridComponent.setBounds(50, 50, 700, 700);
        hullFrame.getContentPane().add(gridComponent);
        gridComponent.setResizable(false);
        gridComponent.setNodeColor(CyderColors.navy);
        gridComponent.setBackground(CyderColors.vanilla);
        gridComponent.installClickListener();
        gridComponent.installDragListener();
        gridComponent.setSaveStates(false);

        CyderButton solveButton = new CyderButton("Solve");
        solveButton.setBounds(50, 700 + 80, 325, 40);
        solveButton.addActionListener(e -> solveAndUpdate());
        hullFrame.getContentPane().add(solveButton);

        CyderButton resetButton = new CyderButton("Reset");
        resetButton.addActionListener(e -> reset());
        resetButton.setBounds(50 + 375, 700 + 80, 325, 40);
        hullFrame.getContentPane().add(resetButton);

        hullFrame.finalizeAndShow();
    }

    /**
     * Solves the convex hull and draws the lines on the grid.
     */
    private static void solveAndUpdate() {
        gridComponent.setGridNodes(new LinkedList<>(gridComponent.getGridNodes()));

        // initialize list of grid points
        LinkedList<Point> points = new LinkedList<>();

        // get all grid nodes that the user placed
        for (CyderGrid.GridNode gn : gridComponent.getGridNodes()) {
            points.add(new Point(gn.getX(), gn.getY()));
        }

        // can't make a polygon with less than 3 points
        if (points.size() < 3) {
            return;
        }

        // solve using O(nlogn) method
        LinkedList<Point> hull = solveGrahamScan(points);

        // for all the hull points, connect a line between the nodes
        for (int i = 0 ; i < hull.size() ; i++) {
            Point p0 = hull.get(i);
            Point p1;

            // if p0 is the last point
            if (i == hull.size() - 1) {
                p1 = hull.get(0);
            } else {
                p1 = hull.get(i + 1);
            }

            // add a point between the two points with our line color
            addMidPoints(p0, p1);
        }

        CyderGrid.GridNode upperLeft = new CyderGrid.GridNode(CyderColors.navy, 0, 0);
        CyderGrid.GridNode upperRight = new CyderGrid.GridNode(CyderColors.navy, 0,
                gridComponent.getNodeDimensionLength() - 1);
        CyderGrid.GridNode bottomLeft = new CyderGrid.GridNode(CyderColors.navy,
                gridComponent.getNodeDimensionLength() - 1, 0);
        CyderGrid.GridNode bottomRight = new CyderGrid.GridNode(CyderColors.navy,
                gridComponent.getNodeDimensionLength() - 1,
                gridComponent.getNodeDimensionLength() - 1);

        LinkedList<CyderGrid.GridNode> cornerNodes = new LinkedList<>();

        cornerNodes.add(upperLeft);
        cornerNodes.add(upperRight);
        cornerNodes.add(bottomLeft);
        cornerNodes.add(bottomRight);

        if (gridComponent.getGridNodes().containsAll(cornerNodes)) {
            hullFrame.notify("Congratulations, you played yourself");
        }

        // repaint to update nodes with line
        gridComponent.repaint();
    }

    /**
     * Finds the middle point between the provided points and adds it to the grid.
     *
     * @param p0 the first point
     * @param p1 the second point
     */
    private static void addMidPoints(Point p0, Point p1) {
        // base case one
        if (p0 == p1) {
            return;
        }

        int midPointX = (p1.x + p0.x) / 2;
        int midPointY = (p1.y + p0.y) / 2;
        Point newPoint = new Point(midPointX, midPointY);

        // base case two
        if (newPoint.equals(p0) || newPoint.equals(p1)) {
            return;
        }

        CyderGrid.GridNode add = new CyderGrid.GridNode(CyderColors.navy, midPointX, midPointY);
        if (gridComponent.contains(add)) {
            gridComponent.removeNode(add);
        }
        gridComponent.addNode(add);

        addMidPoints(p0, newPoint);
        addMidPoints(newPoint, p1);
    }

    /**
     * Solves the convex hull problem given the list of points.
     *
     * @param points the list of points
     * @return the points in the convex hull
     */
    private static LinkedList<Point> solveGrahamScan(LinkedList<? extends Point> points) {
        Deque<Point> stack = new ArrayDeque<>();

        Point minYPoint = getMinY(points);
        sortByAngle(points, minYPoint);

        stack.push(points.get(0));
        stack.push(points.get(1));

        for (int i = 2, size = points.size() ; i < size ; i++) {
            Point next = points.get(i);
            Point p = stack.pop();

            while (stack.peek() != null && ccw(stack.peek(), p, next) <= 0) {
                p = stack.pop();
            }

            stack.push(p);
            stack.push(points.get(i));
        }

        Point p = stack.pop();

        if (stack.peek() == null)
            return new LinkedList<>();

        if (ccw(stack.peek(), p, minYPoint) > 0) {
            stack.push(p);
        }

        return new LinkedList<>(stack);
    }

    /**
     * Returns the node with the minimum y value from the provided list.
     *
     * @param points the list of points
     * @return the point with the minimum y value
     */
    private static Point getMinY(Collection<? extends Point> points) {
        Iterator<? extends Point> it = points.iterator();
        Point min = it.next();

        while (it.hasNext()) {
            Point point = it.next();
            if (point.y <= min.y) {
                if (point.y < min.y) {
                    min = point;
                } else if (point.x < min.x) { // point.y==min.y, pick left most one
                    min = point;
                }
            }
        }

        return min;
    }

    /**
     * Returns whether the turn between a and b will be counter-clockwise.
     *
     * @param a the first point
     * @param b the second point
     * @param c the reference point
     * @return whether the turn between a and b will be counter-clockwise
     */
    private static int ccw(Point a, Point b, Point c) {
        float area = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);

        // clockwise
        if (area < 0) {
            return -1;
        }

        // counter-clockwise
        if (area > 0) {
            return 1;
        }

        // collinear
        return 0;
    }

    /**
     * Sorts the list of points by angle using the reference point.
     *
     * @param points the list of points
     * @param ref    the reference point
     */
    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    private static void sortByAngle(LinkedList<? extends Point> points, Point ref) {
        points.sort((b, c) -> {
            if (b == ref) {
                return -1;
            }

            if (c == ref) {
                return 1;
            }

            int ccw = ccw(ref, b, c);

            if (ccw == 0) {
                if (Float.compare(b.x, c.x) == 0) {
                    return b.y < c.y ? -1 : 1;
                } else {
                    return b.x < c.x ? -1 : 1;
                }
            } else {
                return ccw * -1;
            }
        });
    }

    /**
     * Clears the grid.
     */
    private static void reset() {
        if (gridComponent.getNodeCount() == 0) {
            return;
        }

        gridComponent.clearGrid();
    }
}
