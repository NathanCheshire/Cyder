package cyder.ui.field;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.LogTag;
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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A password field customized for Cyder.
 */
public class CyderPasswordField extends CyderTextField {
    /**
     * The default font for the password field.
     */
    public static final Font DEFAULT_FONT = new Font("Agency FB", Font.BOLD, 20);

    /**
     * The current echo char.
     */
    private char echoChar = CyderStrings.ECHO_CHAR;

    /**
     * The actual characters currently held by this password field.
     */
    private final ArrayList<Character> actualCharacters;

    /**
     * Constructs a new CyderPasswordField.
     */
    public CyderPasswordField() {
        setEchoChar(echoChar);
        setForeground(CyderColors.navy);
        setSelectionColor(CyderColors.selectionColor);
        setFont(DEFAULT_FONT);
        setBorder(new LineBorder(CyderColors.navy, 5, false));
        setCaret(new CyderCaret(CyderColors.navy));
        setCaretColor(CyderColors.navy);

        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());
        addFocusListener(generateSecurityFocusAdapter(this));
        addKeyListener(generatePasswordObfuscationKeyAdapter(this));

        actualCharacters = new ArrayList<>();

        setShiftShowsPassword(true);

        Logger.log(LogTag.OBJECT_CREATION, this);
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
     * Deprecated method, use {@link #getPassword()}
     *
     * @throws IllegalMethodException if invoked
     */
    @Deprecated
    @Override
    public String getText() {
        throw new IllegalMethodException("Deprecated");
    }

    /**
     * Returns the password currently held by this field.
     *
     * @return the password currently held by this field
     */
    public char[] getPassword() {
        char[] ret = new char[actualCharacters.size()];
        for (int i = 0 ; i < actualCharacters.size() ; i++) {
            ret[i] = actualCharacters.get(i);
        }
        return ret;
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
     * Generates and returns a key adapter for all password fields to use to obfuscate the text.
     *
     * @param passwordField the password field
     * @return the generated key adapter
     */
    private static KeyAdapter generatePasswordObfuscationKeyAdapter(CyderPasswordField passwordField) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyChar());
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
     * Sets the echo char of this password field.
     *
     * @param echoChar the echo char of this password field
     */
    public void setEchoChar(char echoChar) {
        this.echoChar = echoChar;
        // todo repaint like in the key adapter method
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
    public static AtomicBoolean addShiftShowsPasswordListener(JTextField passwordField) {
        AtomicBoolean shiftShowsPasswordEnabled = new AtomicBoolean(true);

        if (passwordField instanceof CyderPasswordField cyderPasswordField) {
            passwordField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SHIFT && shiftShowsPasswordEnabled.get()) {
                        cyderPasswordField.setEchoChar((char) 0);
                        int pos = passwordField.getCaretPosition();
                        passwordField.setCaret(passwordField.getCaret());
                        passwordField.setCaretPosition(pos);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SHIFT && shiftShowsPasswordEnabled.get()) {
                        cyderPasswordField.setEchoChar(CyderStrings.ECHO_CHAR);
                        int pos = passwordField.getCaretPosition();
                        passwordField.setCaret(passwordField.getCaret());
                        passwordField.setCaretPosition(pos);
                    }
                }
            });
        } else if (passwordField instanceof JPasswordField jPasswordField) {
            passwordField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SHIFT && shiftShowsPasswordEnabled.get()) {
                        jPasswordField.setEchoChar((char) 0);
                        int pos = passwordField.getCaretPosition();
                        passwordField.setCaret(passwordField.getCaret());
                        passwordField.setCaretPosition(pos);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SHIFT && shiftShowsPasswordEnabled.get()) {
                        jPasswordField.setEchoChar(CyderStrings.ECHO_CHAR);
                        int pos = passwordField.getCaretPosition();
                        passwordField.setCaret(passwordField.getCaret());
                        passwordField.setCaretPosition(pos);
                    }
                }
            });
        }

        return shiftShowsPasswordEnabled;
    }
}
