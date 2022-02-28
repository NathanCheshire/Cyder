package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderGrid;
import cyder.ui.objects.GridNode;
import cyder.ui.objects.NotificationBuilder;

import java.awt.*;
import java.util.*;

/**
 * Convexhull widget that solve a convexhull problem using a CyderGrid as the drawing label.
 */
public class ConvexHullWidget {

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
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = "convexhull", description = "A convex hull algorithm visualizer")
    public static void showGUI() {
        Logger.log(Logger.Tag.WIDGET_OPENED, "CONVEX HULL");

        if (hullFrame != null) {
            hullFrame.dispose();
        }

        hullFrame = new CyderFrame(800,850);
        hullFrame.setTitle("Convex hull");

        gridComponent = new CyderGrid(100,700);
        gridComponent.setDrawGridLines(false);
        gridComponent.setBounds(50, 50,700, 700);
        hullFrame.getContentPane().add(gridComponent);
        gridComponent.setDrawExtendedBorder(false);
        gridComponent.setResizable(false);
        gridComponent.setNodeColor(CyderColors.regularPink);
        gridComponent.installClickPlacer();
        gridComponent.installDragPlacer();

        CyderButton solveButton = new CyderButton("Solve");
        solveButton.setBounds(50, 700 + 80,325, 40);
        solveButton.addActionListener(e -> solveAndUpdate());
        hullFrame.getContentPane().add(solveButton);

        CyderButton resetButton = new CyderButton("Reset");
        resetButton.addActionListener(e -> reset());
        resetButton.setBounds(50 + 375, 700 + 80,325, 40);
        hullFrame.getContentPane().add(resetButton);

        hullFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
        hullFrame.setVisible(true);
    }

    /**
     * Solves the convex hull problem and
     */
    private static void solveAndUpdate() {
        // let points be the list of points
        LinkedList<Point> points = new LinkedList<>();

        for (GridNode gn : gridComponent.getGridNodes()) {
           points.add(new Point(gn.getX(), gn.getY()));
        }

        // can't make a polygon with less than 3 points
//        if (points.size() < 3)
//            return;

        // solve using O(nlogn) method
        LinkedList<Point> hull = solveGrahamScan(points);

        // add point nodes
        for (Point p : hull) {
            gridComponent.addNode(new GridNode(CyderColors.navy, (int) p.getX(), (int) p.getY()));
        }

        // recursively draw the line
        addMidPoints(points.get(0), points.get(1));

        // repaint to update nodes with line
        gridComponent.repaint();
    }

    private static void addMidPoints(Point p0, Point p1) {
        // base case one
        if (p0 == p1)
            return;

        int midPointX = (p1.x + p0.x) / 2;
        int midPointY = (p1.y + p0.y) / 2;
        Point newPoint = new Point(midPointX, midPointY);

        // base case two
        if (newPoint.equals(p0) || newPoint.equals(p1))
            return;

        gridComponent.addNode(new GridNode(CyderColors.navy, midPointX, midPointY));
        addMidPoints(p0, newPoint);
        addMidPoints(newPoint, p1);
    }

    private static LinkedList<Point> solveGrahamScan(LinkedList<Point> points) {
        Deque<Point> stack = new ArrayDeque<>();

        Point minYPoint = getMinY(points);
        sortByAngle(points, minYPoint);

        stack.push(points.get(0));
        stack.push(points.get(1));

        for (int i = 2, size = points.size(); i < size; i++) {
            Point next = points.get(i);
            Point p = stack.pop();

            while (stack.peek() != null && ccw(stack.peek(), p, next) <= 0) {
                p = stack.pop();
            }

            stack.push(p);
            stack.push(points.get(i));
        }


        Point p = stack.pop();

        if (ccw(stack.peek(), p, minYPoint) > 0) {
            stack.push(p);
        }

        return new LinkedList<>(stack);
    }

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

    private static int ccw(Point a, Point b, Point c) {
        float area = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);

        // clockwise
        if (area < 0)
            return -1;

        // counter-clockwise
        if (area > 0)
            return 1;

        // collinear
        return 0;
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    private static void sortByAngle(LinkedList<Point> points, Point ref) {
        points.sort((b, c) -> {
            if (b == ref) return -1;
            if (c == ref) return 1;


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
     * Clears the board.
     */
    private static void reset() {
        if (gridComponent.getNodeCount() == 0)
            return;

        NotificationBuilder builder = new NotificationBuilder("Cleared "
                + gridComponent.getNodeCount() + " nodes");
        builder.setViewDuration(2000);
        builder.setArrowDir(Direction.RIGHT);
        builder.setNotificationDirection(NotificationDirection.TOP_RIGHT);

        hullFrame.notify(builder);
        gridComponent.clear();
    }
}
