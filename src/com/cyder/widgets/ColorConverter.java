package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.ColorUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;

import static com.cyder.Constants.CyderColors.navy;

public class ColorConverter {
    public static void colorConverter() {
        CyderFrame colorFrame = new CyderFrame(400,300,new ImageIcon("src/com/cyder/sys/pictures/DebugBackground.png"));
        colorFrame.setTitle("Color Converter");

        JLabel hexLabel = new JLabel("HEX:");
        hexLabel.setFont(CyderFonts.weatherFontSmall);
        hexLabel.setForeground(navy);
        hexLabel.setBounds(30, 110,70, 30);
        colorFrame.getContentPane().add(hexLabel);

        JLabel rgbLabel = new JLabel("RGB:");
        rgbLabel.setFont(CyderFonts.weatherFontSmall);
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

        JTextField rgbField = new JTextField(navy.getRed() + "," + navy.getGreen() + "," + navy.getBlue());

        JTextField hexField = new JTextField(String.format("#%02X%02X%02X", navy.getRed(), navy.getGreen(), navy.getBlue()).replace("#",""));
        hexField.setForeground(navy);
        hexField.setFont(CyderFonts.weatherFontBig);
        hexField.setBackground(new Color(0,0,0,0));
        hexField.setSelectionColor(CyderColors.selectionColor);
        hexField.setToolTipText("Hex Value");
        hexField.setBorder(new LineBorder(navy,5,false));
        JTextField finalHexField1 = hexField;
        JTextField finalRgbField = rgbField;
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

        rgbField.setForeground(navy);
        rgbField.setFont(CyderFonts.weatherFontBig);
        rgbField.setBackground(new Color(0,0,0,0));
        rgbField.setSelectionColor(CyderColors.selectionColor);
        rgbField.setToolTipText("RGB Value");
        rgbField.setBorder(new LineBorder(navy,5,false));
        JTextField finalRgbField1 = rgbField;
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
        colorFrame.setLocationRelativeTo(null);
    }
}
