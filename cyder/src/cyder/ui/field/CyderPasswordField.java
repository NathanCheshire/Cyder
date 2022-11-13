package cyder.ui.field;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.ImageUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
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
     * The border currently set on this text field.
     */
    private Border border = new LineBorder(CyderColors.navy, 5, false);

    /**
     * Whether the shift shows password key listener is installed.
     */
    private final AtomicBoolean shiftShowsPassword;

    // -------------------
    // Primary constructor
    // -------------------

    /**
     * Constructs a new CyderPasswordField.
     */
    public CyderPasswordField() {
        setEchoChar(CyderStrings.ECHO_CHAR);
        setForeground(CyderColors.navy);
        setSelectionColor(CyderColors.selectionColor);
        setFont(DEFAULT_FONT);
        setBorder(border);
        setCaret(new CyderCaret(CyderColors.navy));
        setCaretColor(CyderColors.navy);

        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());
        addSecurityFocusAdapter();

        shiftShowsPassword = new AtomicBoolean(true);
        addKeyListener(generateShiftShowsPasswordKeyListener(this, shiftShowsPassword));
        setShiftShowsPassword(true);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    // -------------------------------
    // Suppressed default constructors
    // -------------------------------

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(int columns) {
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
    private CyderPasswordField(String text, int columns) {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Suppress a default constructor.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private CyderPasswordField(Document document, String text, int columns) {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Adds the security focus adapter for this password field.
     */
    private void addSecurityFocusAdapter() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                refresh();
            }

            @Override
            public void focusLost(FocusEvent e) {
                refresh();
            }
        });
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
     * Sets whether holding shift shows the password.
     *
     * @param shiftShowsPassword whether holding shift shows the password
     */
    public void setShiftShowsPassword(boolean shiftShowsPassword) {
        this.shiftShowsPassword.set(shiftShowsPassword);
    }

    /**
     * Generates a shift shows password key listener for the provided password field.
     *
     * @param passwordField      the password field
     * @param shiftShowsPassword the boolean determining whether shift should show the password
     * @return the key listener to add to the provided password field.
     */
    private static KeyListener generateShiftShowsPasswordKeyListener(JPasswordField passwordField,
                                                                     AtomicBoolean shiftShowsPassword) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT && shiftShowsPassword.get()) {
                    passwordField.setEchoChar((char) 0);
                    int pos = passwordField.getCaretPosition();
                    passwordField.setCaret(passwordField.getCaret());
                    passwordField.setCaretPosition(pos);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT && shiftShowsPassword.get()) {
                    passwordField.setEchoChar(CyderStrings.ECHO_CHAR);
                    int pos = passwordField.getCaretPosition();
                    passwordField.setCaret(passwordField.getCaret());
                    passwordField.setCaretPosition(pos);
                }
            }
        };
    }

    /**
     * Adds the shift shows password listener to the provided password field.
     *
     * @param passwordField the password field to add the listener too
     * @return an atomic boolean to toggle the state of the password listener
     */
    @CanIgnoreReturnValue
    public static AtomicBoolean addShiftShowsPasswordListener(JPasswordField passwordField) {
        Preconditions.checkNotNull(passwordField);

        AtomicBoolean localEnabled = new AtomicBoolean(true);
        passwordField.addKeyListener(generateShiftShowsPasswordKeyListener(passwordField, localEnabled));
        return localEnabled;
    }

    // ---------------
    // Left icon logic
    // ---------------

    private ImageIcon leftIcon;

    private JLabel leftIconLabel;

    /**
     * The padding for the left icon label.
     */
    private static final int iconLabelPadding = 5;

    /**
     * The padding between the border and left icon label and the start of the field text.
     */
    private static final int iconLabelFieldTextPadding = 5;

    /**
     * Sets the left icon for this text field.
     *
     * @param leftIcon the left icon for this text field
     */
    public void setLeftIcon(ImageIcon leftIcon) {
        Preconditions.checkNotNull(leftIcon);

        this.leftIcon = leftIcon;
        refreshLeftIcon();
    }

    /**
     * Removes the left icon from this text field.
     */
    private void removeLeftIcon() {
        leftIcon = null;
        leftIconLabel.setVisible(false);
        refreshLeftIcon();
    }

    /**
     * Refreshes the bounds, border, icon, and visibility of the left icon.
     */
    private void refreshLeftIcon() {
        if (leftIcon == null) return;

        if (leftIconLabel == null) {
            leftIconLabel = new JLabel();
            add(leftIconLabel);
        }

        int len = getHeight() - 2 * iconLabelPadding;
        if (leftIcon.getIconWidth() > len || leftIcon.getIconHeight() > len) {
            leftIcon = ImageUtil.ensureFitsInBounds(leftIcon, new Dimension(len, len));
        }

        setBorder(border);
        leftIconLabel.setIcon(leftIcon);
        leftIconLabel.setVisible(true);
        leftIconLabel.setBounds(iconLabelPadding, iconLabelPadding, len, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBorder(Border border) {
        int len = getHeight() - 2 * iconLabelPadding + iconLabelFieldTextPadding;
        int leftInsets = leftIcon != null ? len : 0;

        this.border = border;
        Border paddingBorder = new EmptyBorder(0, leftInsets, 0, 0);
        super.setBorder(new CompoundBorder(border, paddingBorder));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        refreshLeftIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        refreshLeftIcon();
    }
}