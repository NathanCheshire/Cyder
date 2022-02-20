package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderGrid;

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
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(triggers = "convexhull", description = "A convex hull algorithm visualizer")
    public static void showGUI() {
        Logger.log(Logger.Tag.WIDGET_OPENED, "CONVEX HULL");

        if (hullFrame != null) {
            hullFrame.dispose();
        }

        hullFrame = new CyderFrame(800,850);
        hullFrame.setTitle("Convex hull");

        gridComponent = new CyderGrid(30,700);
        //gridComponent.setDrawGridLines(false);
        gridComponent.setBounds(50, 50,700, 700);
        hullFrame.getContentPane().add(gridComponent);
        gridComponent.setDrawExtendedBorder(true);
        gridComponent.setResizable(true);
        gridComponent.setMinNodes(20);
        gridComponent.installClickPlacer();

        CyderButton solveButton = new CyderButton("Solve");
        solveButton.setBounds(50, 700 + 80,325, 40);
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
        //todo
    }

    /**
     * Clears the board.
     */
    private static void reset() {
        if (gridComponent.getNodeCount() == 0)
            return;

        hullFrame.notify("Cleared " + gridComponent.getNodeCount() + " nodes");
        gridComponent.clear();
    }
}
