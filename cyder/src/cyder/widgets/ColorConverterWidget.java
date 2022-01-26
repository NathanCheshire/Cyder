package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderFonts;
import cyder.consts.CyderIcons;
import cyder.consts.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.SessionHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;
import cyder.utilities.ColorUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;

import static cyder.consts.CyderColors.navy;

public class ColorConverterWidget implements WidgetBase {
    private ColorConverterWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(trigger = {"color converter", "color"}, description = "A color converter widget to convert from rgb to hex and vice versa")
    public static void showGUI() {
        SessionHandler.log(SessionHandler.Tag.WIDGET_OPENED, "COLOR");

        CyderFrame colorFrame = new CyderFrame(400,300, CyderIcons.defaultBackground);
        colorFrame.setTitle("Color Converter");

        JLabel hexLabel = new JLabel("HEX:");
        hexLabel.setFont(CyderFonts.segoe20);
        hexLabel.setForeground(navy);
        hexLabel.setBounds(30, 110,70, 30);
        colorFrame.getContentPane().add(hexLabel);

        JLabel rgbLabel = new JLabel("RGB:");
        rgbLabel.setFont(CyderFonts.segoe20);
        rgbLabel.setForeground(navy);
        rgbLabel.setBounds(30, 180,70,30);
        colorFrame.getContentPane().add(rgbLabel);

        JTextField colorBlock = new JTextField();
        colorBlock.setBackground(navy);
        colorBlock.setFocusable(false);
        colorBlock.setCursor(null);
        colorBlock.setToolTipText("Color Preview");
        colorBlock.setBorder(new LineBorder(navy, 5, false));
        colorBlock.setBounds(330, 100, 40, 120);
        colorFrame.getContentPane().add(colorBlock);

        CyderTextField rgbField = new CyderTextField(11);
        rgbField.setText(navy.getRed() + "," + navy.getGreen() + "," + navy.getBlue());

        CyderTextField hexField = new CyderTextField(6);
        hexField.setText(String.format("#%02X%02X%02X", navy.getRed(), navy.getGreen(), navy.getBlue()).replace("#",""));
        hexField.setBackground(new Color(0,0,0,0));
        hexField.setToolTipText("Hex Value");
        hexField.setFont(hexField.getFont().deriveFont(26f));
        CyderTextField finalHexField1 = hexField;
        CyderTextField finalRgbField = rgbField;
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            try {
                Color c = ColorUtil.hextorgbColor(finalHexField1.getText());
                finalRgbField.setText(c.getRed() + "," + c.getGreen() + "," + c.getBlue());
                colorBlock.setBackground(c);
            }

            catch (Exception ignored) {}
            }
        });
        hexField.setBounds(100, 100,220, 50);
        hexField.setOpaque(false);
        colorFrame.getContentPane().add(hexField);

        rgbField.setBackground(new Color(0,0,0,0));
        rgbField.setToolTipText("RGB Value");
        rgbField.setFont(hexField.getFont().deriveFont(26f));
        CyderTextField finalRgbField1 = rgbField;
        rgbField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
            try {
                String[] parts = finalRgbField1.getText().split(",");
                Color c = new Color(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                hexField.setText(ColorUtil.rgbtohexString(c));
                colorBlock.setBackground(c);
            }

            catch (Exception ignored) {}
            }
        });
        rgbField.setBounds(100, 170,220, 50);
        rgbField.setOpaque(false);
        colorFrame.getContentPane().add(rgbField);

        colorFrame.setVisible(true);
        colorFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
    }
}
