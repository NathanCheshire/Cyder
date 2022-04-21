package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.layouts.CyderGridLayout;
import cyder.ui.CyderFrame;
import cyder.ui.CyderPanel;
import cyder.ui.CyderTextField;
import cyder.utilities.ColorUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;

/**
 * A widget for converting between rgb and hex colors.
 */
@Vanilla
@CyderAuthor(author = "Nathan Cheshire")
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
        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * A widget for converting between rgb and hex colors.
     */
    @Widget(triggers = {"colorconverter", "color"},
            description = "A color converter widget to convert from rgb to hex and vice versa")
    public static void showGui() {
        getInstance().innerShowGui();
    }

    public void innerShowGui() {
        CyderFrame colorFrame = new CyderFrame(300,400, CyderIcons.defaultBackground);
        colorFrame.setTitle("Color Converter");
        colorFrame.initializeResizing();
        colorFrame.setResizable(true);
        colorFrame.setBackgroundResizing(true);
        colorFrame.setMinimumSize(new Dimension(300, 400));
        colorFrame.setMaximumSize(new Dimension(300, 800));

        CyderGridLayout layout = new CyderGridLayout(1,5);

        JLabel hexLabel = new JLabel("Hex Value");
        hexLabel.setFont(CyderFonts.segoe20);
        hexLabel.setForeground(CyderColors.navy);
        hexLabel.setSize(120, 30);
        layout.addComponent(hexLabel, 0, 0, CyderGridLayout.Position.MIDDLE_CENTER);

        JLabel rgbLabel = new JLabel("RGB Value");
        rgbLabel.setFont(CyderFonts.segoe20);
        rgbLabel.setForeground(CyderColors.navy);
        rgbLabel.setSize(120,30);
        layout.addComponent(rgbLabel, 0, 3, CyderGridLayout.Position.MIDDLE_CENTER);

        JTextField colorBlock = new JTextField();
        colorBlock.setBackground(CyderColors.navy);
        colorBlock.setFocusable(false);
        colorBlock.setCursor(null);
        colorBlock.setToolTipText("Color Preview");
        colorBlock.setBorder(new LineBorder(CyderColors.navy, 5, false));
        colorBlock.setSize(220, 50);
        layout.addComponent(colorBlock, 0, 2, CyderGridLayout.Position.MIDDLE_CENTER);

        CyderTextField rgbField = new CyderTextField(11);
        rgbField.setHorizontalAlignment(JTextField.CENTER);
        rgbField.setText(CyderColors.navy.getRed() + ","
                + CyderColors.navy.getGreen() + "," + CyderColors.navy.getBlue());

        CyderTextField hexField = new CyderTextField(6);
        hexField.setKeyEventRegexMatcher(CyderRegexPatterns.hexPattern);
        hexField.setHorizontalAlignment(JTextField.CENTER);
        hexField.setText(String.format("#%02X%02X%02X", CyderColors.navy.getRed(),
                CyderColors.navy.getGreen(),
                CyderColors.navy.getBlue()).replace("#",""));
        hexField.setBackground(new Color(0,0,0,0));
        hexField.setToolTipText("Hex Value");
        hexField.setFont(hexField.getFont().deriveFont(26f));
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    Color c = ColorUtil.hexToRgb(hexField.getText());
                    rgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
                    colorBlock.setBackground(c);
                }

                catch (Exception ignored) {}
            }
        });
        hexField.setSize(220, 50);
        hexField.setOpaque(false);
        layout.addComponent(hexField, 0, 1, CyderGridLayout.Position.MIDDLE_CENTER);

        rgbField.setBackground(new Color(0,0,0,0));
        rgbField.setKeyEventRegexMatcher(CyderRegexPatterns.rgbPattern);
        rgbField.setToolTipText("RGB Value");
        rgbField.setFont(hexField.getFont().deriveFont(26f));
        rgbField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            try {
                String[] parts = rgbField.getText().split(",");
                Color c = new Color(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                hexField.setText(ColorUtil.rgbToHexString(c));
                colorBlock.setBackground(c);
            }

            catch (Exception ignored) {}
            }
        });
        rgbField.setSize(220, 50);
        rgbField.setOpaque(false);
        layout.addComponent(rgbField, 0, 4, CyderGridLayout.Position.MIDDLE_CENTER);

        CyderPanel panel = new CyderPanel(layout);
        colorFrame.setLayoutPanel(panel);

        colorFrame.finalizeAndShow();
    }
}
