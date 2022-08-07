package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.CyderInspection;
import cyder.handlers.internal.Logger;
import cyder.layouts.CyderPartitionedLayout;
import cyder.ui.CyderFrame;
import cyder.ui.CyderPanel;
import cyder.ui.CyderTextField;
import cyder.utils.ColorUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;

/**
 * A widget for converting between rgb and hex colors.
 */
@Vanilla
@CyderAuthor
public class ColorConverterWidget {
    /**
     * Returns a new instance of ColorConverterWidget.
     *
     * @return a new instance of ColorConverterWidget
     */
    public static ColorConverterWidget getInstance() {
        return new ColorConverterWidget();
    }

    /**
     * Creates a new Color Converter Widget.
     */
    private ColorConverterWidget() {
        // invoked via getInstance()
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * A widget for converting between rgb and hex colors.
     */
    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = {"color converter", "color"},
            description = "A color converter widget to convert from rgb to hex and vice versa")
    public static void showGui() {
        getInstance().innerShowGui();
    }

    public void innerShowGui() {
        int width = 300;
        int height = 400;

        CyderFrame colorFrame = new CyderFrame(width, height, CyderIcons.defaultBackground);
        colorFrame.setTitle("Color Converter");

        colorFrame.initializeResizing();
        colorFrame.setResizable(true);
        colorFrame.setBackgroundResizing(true);
        colorFrame.setMinimumSize(new Dimension(width, height));
        colorFrame.setMaximumSize(new Dimension(width, 2 * height));

        CyderPartitionedLayout layout = new CyderPartitionedLayout();

        JLabel colorLabel = new JLabel("Color preview");
        colorLabel.setHorizontalAlignment(JLabel.CENTER);
        colorLabel.setFont(CyderFonts.SEGOE_20);
        colorLabel.setForeground(CyderColors.navy);
        colorLabel.setSize(width, 30);

        JLabel hexLabel = new JLabel("Hex value");
        hexLabel.setHorizontalAlignment(JLabel.CENTER);
        hexLabel.setFont(CyderFonts.SEGOE_20);
        hexLabel.setForeground(CyderColors.navy);
        hexLabel.setSize(width, 30);

        JLabel rgbLabel = new JLabel("Rgb value");
        rgbLabel.setHorizontalAlignment(JLabel.CENTER);
        rgbLabel.setFont(CyderFonts.SEGOE_20);
        rgbLabel.setForeground(CyderColors.navy);
        rgbLabel.setSize(width, 30);

        JTextField colorBlock = new JTextField();
        colorBlock.setBackground(CyderColors.navy);
        colorBlock.setFocusable(false);
        colorBlock.setCursor(null);
        colorBlock.setToolTipText("Color Preview");
        colorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        colorBlock.setSize(220, 50);

        CyderTextField rgbField = new CyderTextField(11);
        rgbField.setHorizontalAlignment(JTextField.CENTER);
        rgbField.setText(CyderColors.navy.getRed() + ","
                + CyderColors.navy.getGreen() + "," + CyderColors.navy.getBlue());

        CyderTextField hexField = new CyderTextField(6);
        hexField.setKeyEventRegexMatcher(CyderRegexPatterns.hexPattern.pattern());
        hexField.setHorizontalAlignment(JTextField.CENTER);
        hexField.setText(String.format("#%02X%02X%02X", CyderColors.navy.getRed(),
                CyderColors.navy.getGreen(),
                CyderColors.navy.getBlue()).replace("#", ""));
        hexField.setBackground(new Color(0, 0, 0, 0));
        hexField.setToolTipText("Hex Value");
        hexField.setFont(hexField.getFont().deriveFont(26f));
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hexStringToColor(hexField.getText());
                    rgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
                    colorBlock.setBackground(c);
                } catch (Exception ignored) {}
            }
        });
        hexField.setSize(220, 50);
        hexField.setOpaque(false);

        rgbField.setBackground(new Color(0, 0, 0, 0));
        rgbField.setKeyEventRegexMatcher(CyderRegexPatterns.rgbPattern.pattern());
        rgbField.setToolTipText("RGB Value");
        rgbField.setFont(hexField.getFont().deriveFont(26f));
        rgbField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    String[] parts = rgbField.getText().split(",");
                    Color c = new Color(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]));
                    hexField.setText(ColorUtil.rgbToHexString(c));
                    colorBlock.setBackground(c);
                } catch (Exception ignored) {}
            }
        });
        rgbField.setSize(220, 50);
        rgbField.setOpaque(false);

        layout.spacer(10);
        layout.addComponent(hexLabel, 15);
        layout.addComponent(hexField, 10);
        layout.addComponent(rgbLabel, 15);
        layout.addComponent(rgbField, 10);
        layout.addComponent(colorLabel, 15);
        layout.addComponent(colorBlock, 15);
        layout.spacer(10);

        CyderPanel panel = new CyderPanel(layout);
        colorFrame.setCyderLayoutPanel(panel);

        colorFrame.finalizeAndShow();
    }
}
