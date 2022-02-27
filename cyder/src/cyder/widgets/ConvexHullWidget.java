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
import cyder.ui.objects.NotificationBuilder;

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
        //todo figure out outer points and draw lines between them
    }

    /**
     * Clears the board.
     */
    private static void reset() {
        if (gridComponent.getNodeCount() == 0)
            return;

        NotificationBuilder builder = new NotificationBuilder("Cleared " + gridComponent.getNodeCount() + " nodes");
        builder.setViewDuration(2000);
        builder.setArrowDir(Direction.RIGHT);
        builder.setNotificationDirection(NotificationDirection.TOP_RIGHT);

        hullFrame.notify(builder);
        gridComponent.clear();
    }
}
