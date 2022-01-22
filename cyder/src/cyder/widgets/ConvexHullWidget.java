package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.ui.*;
import cyder.algorithoms.GrahamScanAlgorithms;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class ConvexHullWidget implements WidgetBase {
    private static JLabel hullLabel;
    private static Vector<Point> boardPoints;
    private static Vector<Point> hullPoints;
    private static CyderFrame hullFrame;
    private static CyderLabel titleLabel;
    private static CyderButton resetPoints;
    private static CyderButton computeButton;
    private static CyderComboBox algorithmCombo;

    private static String[] algorithms = new String[] {"Wrapping","Graham Scan"};

    private ConvexHullWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(trigger = "convex hull", description = "A convex hull algorithm visualizer")
    public static void showGUI() {
        boardPoints = new Vector<>();

        hullFrame = new CyderFrame(800,800);
        hullFrame.setTitle("Convex Hull");

        hullLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setColor(CyderColors.vanila);
                g2d.fillRect(0,0,800,800);

                g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int size = 10;

                if (boardPoints.size() > 0) {
                    g2d.setColor(Color.black);
                    for (Point p : boardPoints) {
                        g2d.fillOval((int) p.getX(), (int) p.getY(), size, size);
                    }
                }

                if (hullPoints != null && hullPoints.size() > 0) {
                    g2d.setColor(CyderColors.regularPink);
                    for (Point p : hullPoints) {
                        g2d.fillOval((int) p.getX(), (int) p.getY(), size, size);
                    }
                }

                if (hullPoints != null && hullPoints.size() > 0) {
                    g2d.setColor(CyderColors.regularPink);

                    for (int i = 0 ; i < hullPoints.size() ; i++) {
                        int inc = i + 1 == hullPoints.size() ? 0 : i + 1;
                        g2d.drawLine((int) hullPoints.get(i).getX(),(int)  hullPoints.get(i).getY(),
                                (int) hullPoints.get(inc).getX(), (int) hullPoints.get(inc).getY());
                    }
                }
            }
        };
        hullLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getX() > 4 && e.getY() > 4
                    && e.getX() < 760 - 4 && e.getY() < 710 - 4) {
                    Point addPoint = new Point(e.getX(), e.getY());
                    boardPoints.add(addPoint);
                    hullLabel.repaint();
                }
            }
        });
        hullLabel.setBounds(20,40,760,690);
        hullLabel.setBorder(new LineBorder(CyderColors.navy, 4));
        hullFrame.getContentPane().add(hullLabel);

        resetPoints = new CyderButton("Reset");
        resetPoints.addActionListener(e -> {
            boardPoints = new Vector<>();
            hullLabel.repaint();
        });
        resetPoints.setBounds(20, 90 + 640 + 10, 240, 40);
        hullFrame.getContentPane().add(resetPoints);

        algorithmCombo = new CyderComboBox(240, 40, algorithms);
        algorithmCombo.setBounds(20 + 20 + 240, 90 + 640 + 10, 240, 40);
        hullFrame.getContentPane().add(algorithmCombo);

        computeButton = new CyderButton("Solve");
        computeButton.addActionListener(e -> solveAndUpdate());
        computeButton.setBounds(240 + 240 + 20 + 20 + 20, 90 + 640 + 10, 240, 40);
        hullFrame.getContentPane().add(computeButton);

        hullFrame.setVisible(true);
        hullFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    private static void solveAndUpdate() {
        if (boardPoints == null || boardPoints.size() == 0)
            return;

        switch (algorithmCombo.getIndex()) {
            case 0:
                 hullPoints = convexHullJarvis(boardPoints, boardPoints.size());
                 hullLabel.repaint();
                break;
            case 1:
                hullPoints = GrahamScanAlgorithms.getConvexHull(boardPoints);
                hullLabel.repaint();
                break;
        }
    }

    /**
     * Function to find the orientation of a triplet pair
     * @param p first point p
     * @param q second point q
     * @param r third point r
     * @return 0 if the points are co-linear, 1 if the points go clockwise, 2 if they go counter-clockwise
     */
    private static int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

        if (val == 0) {
            return 0;
        }

        return (val > 0) ? 1 : 2;
    }

    public static Vector<Point> convexHullJarvis(Vector<Point> points, int n) {
        if (n < 3)
            return null;

        Vector<Point> hull = new Vector<>();

        int leftMostPoint = 0;

        for (int i = 1; i < n; i++)
            if (points.get(i).x < points.get(leftMostPoint).x)
                leftMostPoint = i;

        int p = leftMostPoint;
        int q;

        do {
            hull.add(points.get(p));
            q = (p + 1) % n;

            for (int i = 0 ; i < n ; i++) {
                if (orientation(points.get(p), points.get(i), points.get(q)) == 2) {
                    q = i;
                }
            }

            p = q;

        } while (p != leftMostPoint);

        return hull;
    }
}
