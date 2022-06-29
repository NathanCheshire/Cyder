package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;

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

        addMouseListener(loggingMouseAdapter);

        setShiftShowsPassword(true);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * The mouse adapter to log field actions.
     */
    private static final MouseAdapter loggingMouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
        }
    };

    /**
     * The key listener for showing/hiding the password.
     */
    private final KeyListener shiftShowsPasswordKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                setEchoChar((char) 0);
                int pos = getCaretPosition();
                setCaret(getCaret());
                setCaretPosition(pos);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                setEchoChar(CyderStrings.ECHO_CHAR);
                int pos = getCaretPosition();
                setCaret(getCaret());
                setCaretPosition(pos);
            }
        }
    };

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

    /**
     * Whether the shift shows password key listener is installed.
     */
    private boolean shiftShowsPassword;

    /**
     * Sets whether holding shift shows the password.
     *
     * @param shiftShowsPassword whether holding shift shows the password
     */
    public void setShiftShowsPassword(boolean shiftShowsPassword) {
        this.shiftShowsPassword = shiftShowsPassword;

        if (shiftShowsPassword) {
            addKeyListener(shiftShowsPasswordKeyListener);
        } else {
            removeKeyListener(shiftShowsPasswordKeyListener);
        }
    }

    /**
     * Returns whether holding shift shows the password
     *
     * @return whether holding shift shows the password
     */
    public boolean isShiftShowsPassword() {
        return shiftShowsPassword;
    }
}
