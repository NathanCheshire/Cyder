package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.ui.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Vector;

public class ConvexHull {
    private static JLabel hullLabel;
    private static Vector<Point> points;

    public static void ShowVisualizer() {
        points = new Vector<>();

        CyderFrame hullFrame = new CyderFrame(800,800);
        hullFrame.setTitle("Convex Hull");

        CyderLabel titleLabel = new CyderLabel("Convex Hull Visualizer");
        titleLabel.setFont(CyderFonts.weatherFontSmall);
        titleLabel.setBounds(250, 40, 300, 40);
        hullFrame.getContentPane().add(titleLabel);

        hullLabel = new JLabel();
        hullLabel.setBounds(20,90,760,640);
        hullLabel.setBorder(new LineBorder(CyderColors.navy, 4));
        hullFrame.getContentPane().add(hullLabel);

        CyderButton resetPoints = new CyderButton("Reset");
        resetPoints.addActionListener(e -> {
            //todo clear points list
            //todo reset board
        });
        resetPoints.setBounds(20, 90 + 640 + 10, 240, 40);
        hullFrame.getContentPane().add(resetPoints);

        CyderTextField algorithmField = new CyderTextField(0);
        algorithmField.setEditable(false);
        algorithmField.setBounds(20 + 20 + 240, 90 + 640 + 10, 240 - 35, 40);
        hullFrame.getContentPane().add(algorithmField);

        CyderButton arrowButton = new CyderButton("▼");
        arrowButton.addActionListener(e -> {
            //todo cycle algorithms in list
        });
        arrowButton.setBounds(20 + 20 + 240 - 40 + 240, 90 + 640 + 10, 40, 40);
        hullFrame.getContentPane().add(arrowButton);

        CyderButton computeButton = new CyderButton("Solve");
        computeButton.addActionListener(e -> {
            //todo solve and update board
        });
        computeButton.setBounds(240 + 240 + 20 + 20 + 20, 90 + 640 + 10, 240, 40);
        hullFrame.getContentPane().add(computeButton);

        hullFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(hullFrame);
    }

    /**
     * Function to find the orientation of a triplet pair
     * @param p - first point p
     * @param q - second point q
     * @param r - third point r
     * @return 0 if the points are co-linear, 1 if the points go clockwise, 2 if they go counter-clockwise
     */
    public static int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

        if (val == 0) {
            return 0;
        }

        return (val > 0) ? 1 : 2;
    }

    public static Vector<Point> convexHullJarvis(Point[] points, int n) {
        if (n < 3)
            return null;

        Vector<Point> hull = new Vector<>();

        int leftMostPoint = 0;

        for (int i = 1; i < n; i++)
            if (points[i].x < points[leftMostPoint].x)
                leftMostPoint = i;

        int p = leftMostPoint;
        int q;

        do {
            hull.add(points[p]);
            q = (p + 1) % n;

            for (int i = 0 ; i < n ; i++) {
                if (orientation(points[p], points[i], points[q]) == 2) {
                    q = i;
                }
            }

            p = q;

        } while (p != leftMostPoint);

        return hull;
    }
}
