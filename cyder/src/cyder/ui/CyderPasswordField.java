package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.SessionHandler;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CyderPasswordField extends JPasswordField {
    public CyderPasswordField() {
        setFont(new Font("Agency FB",Font.BOLD, 20));
        setEchoChar(CyderStrings.ECHO_CHAR);
        setSelectionColor(CyderColors.selectionColor);
        setBorder(new LineBorder(CyderColors.navy, 5, false));
        setForeground(CyderColors.navy);
        setCaretColor(CyderColors.navy);
        setCaret(new CyderCaret(CyderColors.navy));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SessionHandler.log(SessionHandler.Tag.ACTION, e.getComponent());
            }
        });
    }

    private CyderPasswordField(int col) {}

    private CyderPasswordField(String text) {}

    private CyderPasswordField(String text, int col) {}

    private CyderPasswordField(Document doc, String text, int col) {}

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}
