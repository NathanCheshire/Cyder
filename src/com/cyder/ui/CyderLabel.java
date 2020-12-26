package com.cyder.ui;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;

import javax.swing.*;

public class CyderLabel extends JLabel {
    public CyderLabel() {
        setText("CyderLabel default text");
        setForeground(CyderColors.navy);
        setFont(CyderFonts.weatherFontSmall);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
    }

    public CyderLabel(String text) {
        setText(text);
        setForeground(CyderColors.navy);
        setFont(CyderFonts.weatherFontSmall);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
    }
}
