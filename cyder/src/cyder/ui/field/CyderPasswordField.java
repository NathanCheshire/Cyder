package cyder.ui.field;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.Logger;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A password field customized for Cyder.
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

        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());
        addFocusListener(generateSecurityFocusAdapter(this));

        setShiftShowsPassword(true);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(int col) {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(String text) {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(String text, int col) {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(Document doc, String text, int col) {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Generates and returns the security focus adapter for password fields.
     *
     * @param passwordField the field the focus adapter will be applied to
     * @return the security focus adapter for password fields
     */
    private static FocusAdapter generateSecurityFocusAdapter(CyderPasswordField passwordField) {
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.refresh();
            }

            @Override
            public void focusLost(FocusEvent e) {
                passwordField.refresh();
            }
        };
    }

    /**
     * Refreshes the caret position, caret, and echo character of this password field.
     */
    public void refresh() {
        setEchoChar(CyderStrings.ECHO_CHAR);
        setCaret(getCaret());
        setCaretPosition(getPassword().length);
    }

    /**
     * Whether the shift shows password key listener is installed.
     */
    private boolean shiftShowsPassword;

    /**
     * The atomic boolean which manages the state of shiftShowsPassword
     */
    private AtomicBoolean existingShiftShowsPassword;

    /**
     * Sets whether holding shift shows the password.
     *
     * @param shiftShowsPassword whether holding shift shows the password
     */
    public void setShiftShowsPassword(boolean shiftShowsPassword) {
        this.shiftShowsPassword = shiftShowsPassword;

        if (existingShiftShowsPassword != null) {
            existingShiftShowsPassword.set(shiftShowsPassword);
        } else if (shiftShowsPassword) {
            existingShiftShowsPassword = addShiftShowsPasswordListener(this);
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

    /**
     * Adds the shift shows password listener to the provided password field.
     *
     * @param passwordField the password field to add the listener too
     * @return an atomic boolean to toggle the state of the password listener
     */
    @CanIgnoreReturnValue
    public static AtomicBoolean addShiftShowsPasswordListener(JPasswordField passwordField) {
        AtomicBoolean shiftShowsPasswordEnabled = new AtomicBoolean(true);

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT && shiftShowsPasswordEnabled.get()) {
                    passwordField.setEchoChar((char) 0);
                    int pos = passwordField.getCaretPosition();
                    passwordField.setCaret(passwordField.getCaret());
                    passwordField.setCaretPosition(pos);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT && shiftShowsPasswordEnabled.get()) {
                    passwordField.setEchoChar(CyderStrings.ECHO_CHAR);
                    int pos = passwordField.getCaretPosition();
                    passwordField.setCaret(passwordField.getCaret());
                    passwordField.setCaretPosition(pos);
                }
            }
        });

        return shiftShowsPasswordEnabled;
    }
}
