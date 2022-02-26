package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.ui.CyderFrame;
import cyder.ui.CyderGrid;
import cyder.ui.DragLabel;

public class PaintWidget {
    private static CyderFrame paintFrame;

    /**
     * Prevent illegal class instantiation.
     */
    private PaintWidget() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }

    /**
     * ShowGUI method standard.
     */
    @Widget(triggers = {"paint", "draw"}, description =
            "A painting widget")
    public static void showGUI() {
        if (paintFrame != null)
            paintFrame.dispose(true);

        int len = 800;

        CyderFrame paintFrame = new CyderFrame(len + 100,len + DragLabel.DEFAULT_HEIGHT + 100);
        paintFrame.setTitle("Paint");
        paintFrame.setBackground(CyderIcons.defaultBackgroundLarge);

        CyderGrid cyderGrid = new CyderGrid(160, len);
        cyderGrid.setBounds(50,DragLabel.DEFAULT_HEIGHT + 50, len, len);
        paintFrame.getContentPane().add(cyderGrid);
        cyderGrid.setDrawExtendedBorder(true);
        cyderGrid.setResizable(true);
        cyderGrid.setDrawGridLines(false);
        cyderGrid.installClickPlacer();
        cyderGrid.installDragPlacer();
        cyderGrid.setSmoothScrollilng(true);

        System.out.println(CyderGrid.getNodesForMaxWidth(len));

        paintFrame.setVisible(true);
        paintFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }
}
