package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.ui.*;
import cyder.utilities.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Stack;

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
        int padding = 0;

        paintFrame = new CyderFrame(len + padding * 2,
                len + DragLabel.DEFAULT_HEIGHT + padding * 2);
        paintFrame.setTitle("Paint");
        paintFrame.setBackground(CyderIcons.defaultBackgroundLarge);

        CyderGrid cyderGrid = new CyderGrid(200, len);
        cyderGrid.setBounds(padding,DragLabel.DEFAULT_HEIGHT + padding - 5, len, len);
        paintFrame.getContentPane().add(cyderGrid);
        cyderGrid.setDrawExtendedBorder(true);
        cyderGrid.setResizable(true);
        cyderGrid.setDrawGridLines(false);
        cyderGrid.installClickPlacer();
        cyderGrid.installDragPlacer();
        cyderGrid.setSmoothScrolling(true);

        paintFrame.setVisible(true);
        paintFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());

        installControlFrames();
    }

    private static CyderFrame colorControlFrame;
    private static CyderFrame otherControlFrame;

    private static final Stack<Color> backwardColors = new Stack<>();
    private static final Stack<Color> forwardColors = new Stack<>();
    private static Color currentPaintColor = CyderColors.regularPink;

    private static void installControlFrames() {
        if (colorControlFrame != null)
            colorControlFrame.dispose();

        if (otherControlFrame != null)
            otherControlFrame.dispose();

        colorControlFrame = new CyderFrame(400,150);
        colorControlFrame.setTitle("Controls");

        CyderTextField colorHexField = new CyderTextField(11);
        colorHexField.setToolTipText("Format: 45FF00 for hex or 255,255,255 for rgb");
        colorHexField.setBounds(10, DragLabel.DEFAULT_HEIGHT + 10, 150, 40);
        colorControlFrame.getContentPane().add(colorHexField);
        colorHexField.setRegexMatcher(CyderRegexPatterns.rgbOrHex);
        colorHexField.addActionListener(e -> {
            String text = colorHexField.getText();

            if (text.contains(",")) {
                String[] parts = text.split(",");

                if (parts.length != 3) {
                    colorControlFrame.notify("Could not parse color");
                } else {
                    try {
                        int r = Integer.parseInt(parts[0]);
                        int g = Integer.parseInt(parts[1]);
                        int b = Integer.parseInt(parts[2]);

                        colorHexField.setForeground(new Color(r, g, b));
                        colorControlFrame.notify("Color parsed");
                    } catch (Exception ignored) {
                        colorControlFrame.notify("Could not parse color");
                    }
                }
            } else {
                try {
                    colorHexField.setForeground(ColorUtil.hexToRgb(colorHexField.getText()));
                } catch (Exception ignored) {
                    colorControlFrame.notify("Could not parse color");
                }
            }
        });
        colorHexField.setText(ColorUtil.rgbToHexString(currentPaintColor));

        CyderButton backwards = new CyderButton("<");
        backwards.setBounds(10, DragLabel.DEFAULT_HEIGHT + 10 + 10 + 40, 30, 40);
        colorControlFrame.getContentPane().add(backwards);
        backwards.addActionListener(e -> {
            //todo update tooltip, current paitn color, and repaint recentcolors label
        });

        CyderButton forwards = new CyderButton(">");
        forwards.setBounds(130, DragLabel.DEFAULT_HEIGHT + 10 + 10 + 40, 30, 40);
        colorControlFrame.getContentPane().add(forwards);
        forwards.addActionListener(e -> {
            //todo
        });

        int colorLabelX = 70;
        int colorLabelY = 40;
        JLabel recentColorsLabel = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(CyderColors.navy);
                g.fillRect(0,0, colorLabelX, colorLabelY);

                g.setColor(currentPaintColor);
                g.fillRect(5,5, colorLabelX - 10, colorLabelY - 10);
            }
        };
        recentColorsLabel.setBounds(50,DragLabel.DEFAULT_HEIGHT + 10 + 10 + 40, colorLabelX, colorLabelY);
        recentColorsLabel.setToolTipText(ColorUtil.rgbToHexString(currentPaintColor));
        colorControlFrame.getContentPane().add(recentColorsLabel);

        backwardColors.clear();
        forwardColors.clear();

        //recent colors use stacks for this

        colorControlFrame.setVisible(true);
        colorControlFrame.setLocation(paintFrame.getX(), paintFrame.getY() + paintFrame.getWidth() + 20);
    }
}
