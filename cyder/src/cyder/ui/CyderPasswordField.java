package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A Cyder password field.
 */
public class CyderPasswordField extends JPasswordField {
    /**
     * The default font for the password field.
     */
    public static final Font DEFAULT_FONT = new Font("Agency FB", Font.BOLD, 20);

    /**
     * Constructs a new CyderPasswordField.
     */
    public CyderPasswordField() {
        setEchoChar(CyderStrings.ECHO_CHAR);

        setForeground(CyderColors.navy);
        setSelectionColor(CyderColors.selectionColor);

        setFont(DEFAULT_FONT);

        setBorder(new LineBorder(CyderColors.navy, 5, false));

        setCaret(new CyderCaret(CyderColors.navy));
        setCaretColor(CyderColors.navy);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(int col) {
        throw new IllegalMethodException("Illegal constructor");
    }

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(String text) {
        throw new IllegalMethodException("Illegal constructor");
    }

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(String text, int col) {
        throw new IllegalMethodException("Illegal constructor");
    }

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(Document doc, String text, int col) {
        throw new IllegalMethodException("Illegal constructor");
    }
}
