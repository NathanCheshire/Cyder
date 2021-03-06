package cyder.ui;

import cyder.consts.CyderColors;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.Document;
import java.awt.*;

public class CyderPasswordField extends JPasswordField {
    public CyderPasswordField() {
        setFont(new Font("Agency FB",Font.BOLD, 20));
        setSelectionColor(CyderColors.selectionColor);
        setBorder(new LineBorder(CyderColors.navy, 5, false));
        setForeground(CyderColors.navy);
        setCaretColor(CyderColors.navy);
        setCaret(new CyderCaret(CyderColors.navy));
    }

    private CyderPasswordField(int col) {}

    private CyderPasswordField(String text) {}

    private CyderPasswordField(String text, int col) {}

    private CyderPasswordField(Document doc, String text, int col) {}
}
