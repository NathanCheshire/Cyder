package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;

import javax.swing.*;

public class CyderLabel extends JLabel {
    public CyderLabel() {
        setText("<html>CyderLabel default text</html>");
        setForeground(CyderColors.navy);
        setFont(CyderFonts.weatherFontSmall);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
    }

    public CyderLabel(String text) {
        setText(text);
        setForeground(CyderColors.navy);
        setFont(CyderFonts.defaultFontSmall);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
    }

    @Override
    public void setText(String text) {
        if (!text.startsWith("<html>")) {
            super.setText("<html>" + text + "</html>");
        }

        else
            super.setText(text);
    }

    @Override
    public String toString() {
        return "CyderLabel object, hash=" + this.hashCode();
    }
}
