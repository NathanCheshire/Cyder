package cyder.ui.field;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderRegexPatterns;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.UiUtil;
import cyder.utils.ColorUtil;
import cyder.utils.ImageUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * A Cyder TextField component.
 */
public class CyderTextField extends JTextField {
    /**
     * The padding for the hint text label.
     */
    private static final int hintTextLabelPadding = 5;

    /**
     * The padding for the left and right icon labels.
     */
    private static final int iconLabelPadding = 5;

    /**
     * The padding between the border and left or right icon label and the start of the field input.
     */
    private static final int iconLabelFieldTextPadding = 5;

    /**
     * The flash color.
     */
    private static final Color flashColor = CyderColors.regularRed;

    /**
     * The time a flash animation takes.
     */
    private static final int flashDurationMs = 500;

    /**
     * The name of the flash animation thread.
     */
    private static final String flashAnimationThreadName = "Cyder Text Field Flash Animator";

    /**
     * The character limit.
     */
    private int characterLimit;

    /**
     * The background color of the field.
     */
    private Color backgroundColor = CyderColors.vanilla;

    /**
     * The regex to restrict entered text to.
     */
    private String keyEventRegexMatcher;

    /**
     * The pattern to use for matching against keyEventRegexMatcher.
     */
    private Pattern keyEventRegexPattern;

    /**
     * The border currently set on this text field.
     */
    private Border border = new LineBorder(CyderColors.navy, 5, false);

    /**
     * Whether auto capitalization is enabled.
     */
    private final AtomicBoolean autoCapitalizationEnabled = new AtomicBoolean(false);

    /**
     * Whether this field is currently flashing.
     */
    private final AtomicBoolean fieldFlashing = new AtomicBoolean();

    /**
     * The hint text for the field.
     */
    private String hintText;

    /**
     * The hint text label for the field.
     */
    private JLabel hintTextLabel;

    /**
     * The hint text alignment for this field.
     */
    private HintTextAlignment hintTextAlignment = HintTextAlignment.LEFT;

    /**
     * The label to hold the left icon.
     */
    private JLabel leftIconLabel;

    /**
     * The left icon.
     */
    private ImageIcon leftIcon;

    /**
     * The label to hold the right icon.
     */
    private JLabel rightIconLabel;

    /**
     * The right icon.
     */
    private ImageIcon rightIcon;

    /**
     * Constructs a new Cyder TextField object with no character limit.
     */
    public CyderTextField() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructs a new CyderTextField object.
     *
     * @param characterLimit the character limit for the text field
     */
    public CyderTextField(int characterLimit) {
        super(characterLimit);

        this.characterLimit = characterLimit;
        this.keyEventRegexMatcher = null;

        addRegexAndCharLimitKeyListener();
        addHintTextFocusListener();
        addHintTextKeyListener();
        addAutoCapitalizationKeyListener();
        addMouseListener(UiUtil.generateUiActionLoggingMouseAdapter());

        setBackground(backgroundColor);
        setSelectionColor(CyderColors.selectionColor);
        setFont(CyderFonts.SEGOE_20);
        setForeground(CyderColors.navy);
        setCaretColor(CyderColors.navy);
        setCaret(new CyderCaret(CyderColors.navy));
        setBorder(border);
        setOpaque(true);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Adds the regex and char limit key listener to this text field.
     */
    private void addRegexAndCharLimitKeyListener() {
        addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent event) {
                regexAndLimitMatcherLogic();
            }

            public void keyPressed(KeyEvent event) {
                regexAndLimitMatcherLogic();
            }

            public void keyReleased(KeyEvent event) {
                regexAndLimitMatcherLogic();
            }
        });
    }

    /**
     * The logic for keyTyped, keyPressed,and keyReleased events from the regex and char limit listener.
     */
    @ForReadability
    private void regexAndLimitMatcherLogic() {
        String currentText = getText();

        if (currentText.length() > characterLimit) {
            setText(currentText.substring(0, currentText.length() - 1));
            Toolkit.getDefaultToolkit().beep();
        } else if (keyEventRegexMatcher != null
                && !keyEventRegexMatcher.isEmpty()
                && !currentText.isEmpty() &&
                !keyEventRegexPattern.matcher(currentText).matches()) {
            setText(currentText.substring(0, currentText.length() - 1));
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackground(Color newBackgroundColor) {
        super.setBackground(newBackgroundColor);
        backgroundColor = newBackgroundColor;
        setOpaque(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getBackground() {
        return backgroundColor;
    }

    /**
     * Sets the regex to restrict the input to. Note that this is applied every key event.
     * To validate a pattern which may be valid and not complete until some time after the
     * user initially started typing, you'll need to do the validation on your own by grabbing
     * the text and matching it before using the input.
     *
     * @param regex the regex to restrict the input to
     */
    public void setKeyEventRegexMatcher(String regex) {
        Preconditions.checkNotNull(regex);
        Preconditions.checkArgument(!regex.isEmpty());

        keyEventRegexMatcher = regex;
        keyEventRegexPattern = Pattern.compile(keyEventRegexMatcher);
    }

    /**
     * Sets the regex matcher to only accept hex color codes
     */
    public void setHexColorRegexMatcher() {
        keyEventRegexMatcher = CyderRegexPatterns.hexPattern.pattern();
        keyEventRegexPattern = CyderRegexPatterns.hexPattern;
    }

    /**
     * Removes the regex from the text field.
     */
    public void removeKeyEventRegexMatcher() {
        keyEventRegexMatcher = null;
        keyEventRegexPattern = null;
    }

    /**
     * Returns the regex matcher for the text field.
     *
     * @return the regex matcher for the text field
     */
    public String getKeyEventRegexMatcher() {
        return keyEventRegexMatcher;
    }

    /**
     * Returns the compiled pattern for the text field based on the keyEventRegexMatcher.
     *
     * @return the compiled pattern for the text field based on the keyEventRegexMatcher
     */
    public Pattern getKeyEventRegexPattern() {
        return keyEventRegexPattern;
    }

    /**
     * Sets the character limit. Any chars outside of the limit are trimmed away.
     *
     * @param limit the character limit
     */
    public void setCharLimit(int limit) {
        Preconditions.checkArgument(limit >= 0);

        this.characterLimit = limit;

        if (getText().length() > limit) {
            setText(getText().substring(0, limit + 1));
        }
    }

    /**
     * Returns the character limit for the text field.
     *
     * @return the character limit for the text field
     */
    public int getCharLimit() {
        return characterLimit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBorder(Border border) {
        int len = getHeight() - 2 * iconLabelPadding + iconLabelFieldTextPadding;
        int leftInsets = leftIcon != null ? len : 0;
        int rightInsets = rightIcon != null ? len : 0;

        this.border = border;
        Border paddingBorder = new EmptyBorder(0, leftInsets, 0, rightInsets);
        super.setBorder(new CompoundBorder(border, paddingBorder));
    }

    /**
     * Sets whether to capitalize the first letter of the form.
     *
     * @param enable whether to capitalize the first letter of the form
     */
    public void setAutoCapitalization(boolean enable) {
        autoCapitalizationEnabled.set(enable);
    }

    /**
     * Adds auto capitalization to this text field.
     */
    public void addAutoCapitalizationKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (autoCapitalizationEnabled.get()) autoCapitalizationLogic();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (autoCapitalizationEnabled.get()) autoCapitalizationLogic();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (autoCapitalizationEnabled.get()) autoCapitalizationLogic();
            }
        });
    }

    /**
     * The logic for performing auto capitalization on this text field.
     */
    @ForReadability
    private void autoCapitalizationLogic() {
        String text = getText();
        if (text.length() == 1) setText(text.toUpperCase());
    }

    /**
     * Returns the field text but trimmed and with multiple occurrences
     * of whitespace in the String replaced with one whitespace char.
     *
     * @return the text with trimming performed
     */
    public String getTrimmedText() {
        return StringUtil.getTrimmedText(getText());
    }

    /**
     * Performs a flash on the field.
     */
    public void flashField() {
        if (fieldFlashing.get()) return;
        fieldFlashing.set(true);

        Color startingColor = getForeground();
        ImmutableList<Color> flashColors = ColorUtil.getFlashColors(flashColor, startingColor);
        int timeout = flashDurationMs / flashColors.size();

        CyderThreadRunner.submit(() -> {
            flashColors.forEach(color -> {
                setForeground(color);
                repaint();
                ThreadUtil.sleep(timeout);
            });

            fieldFlashing.set(false);
            setForeground(startingColor);
        }, flashAnimationThreadName);
    }

    // ---------------
    // Hint text logic
    // ---------------

    /**
     * Possible hint text alignments.
     */
    public enum HintTextAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * Returns the hint text alignment for this field.
     *
     * @return the hint text alignment for this field
     */
    public HintTextAlignment getHintTextAlignment() {
        return hintTextAlignment;
    }

    /**
     * Sets the hint text alignment for this field.
     *
     * @param hintTextAlignment the hint text alignment for this field
     */
    public void setHintTextAlignment(HintTextAlignment hintTextAlignment) {
        Preconditions.checkNotNull(hintTextAlignment);

        this.hintTextAlignment = hintTextAlignment;
        refreshHintText();
    }

    /**
     * Sets the hint text for this field.
     *
     * @param text the hint text for this field
     */
    public void setHintText(String text) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());

        this.hintText = text;
        refreshHintText();
        hintTextLabel.setVisible(true);
    }

    /**
     * Refreshes the hint text and label visibility.
     */
    private void refreshHintText() {
        if (hintTextLabel == null) {
            hintTextLabel = new JLabel();
            add(hintTextLabel);
            refreshHintText();
            hintTextLabel.setVisible(true);
        }

        hintTextLabel.setText(hintText);

        Dimension size = getSize();
        int iconLen = getHeight() - 2 * iconLabelPadding;

        int start = hintTextLabelPadding;
        if (leftIcon != null) {
            start = iconLen + iconLabelFieldTextPadding + hintTextLabelPadding;
        }

        int end = getWidth();
        if (rightIcon != null) {
            end = getWidth() - iconLen - hintTextLabelPadding;
        }

        int width = end - start;
        int defaultHeight = (int) size.getHeight() - 2 * hintTextLabelPadding;
        hintTextLabel.setBounds(start, hintTextLabelPadding, width, defaultHeight);

        switch (hintTextAlignment) {
            case LEFT -> hintTextLabel.setHorizontalAlignment(JLabel.LEFT);
            case CENTER -> hintTextLabel.setHorizontalAlignment(JLabel.CENTER);
            case RIGHT -> hintTextLabel.setHorizontalAlignment(JLabel.RIGHT);
        }

        hintTextLabel.setForeground(getForeground());
        hintTextLabel.setFont(getFont());
        hintTextLabel.repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHorizontalAlignment(int alignment) {
        switch (alignment) {
            case JTextField.LEFT -> setHintTextAlignment(HintTextAlignment.LEFT);
            case JTextField.CENTER -> setHintTextAlignment(HintTextAlignment.CENTER);
            case JTextField.RIGHT -> setHintTextAlignment(HintTextAlignment.RIGHT);
            default -> {}
        }

        super.setHorizontalAlignment(alignment);
    }

    /**
     * Adds the hint text focus listener to this field.
     */
    private void addHintTextFocusListener() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().length() != 0) {
                    hintTextLabel.setVisible(false);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().length() == 0) {
                    hintTextLabel.setVisible(true);
                }
            }
        });
    }

    /**
     * Adds the hint text key listener to this field.
     */
    private void addHintTextKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                hintTextLabel.setVisible(getText().isEmpty());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                hintTextLabel.setVisible(getText().isEmpty());
            }
        });
    }

    // ---------
    // Left icon
    // ---------

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

    // ----------
    // Right Icon
    // ----------

    /**
     * Sets the right icon for this text field.
     *
     * @param rightIcon the right icon for this text field
     */
    public void setRightIcon(ImageIcon rightIcon) {
        Preconditions.checkNotNull(rightIcon);

        this.rightIcon = rightIcon;
        refreshRightIcon();
    }

    /**
     * Removes the right icon from this text field.
     */
    private void removeRightIcon() {
        rightIcon = null;
        rightIconLabel.setVisible(false);
        refreshRightIcon();
    }

    /**
     * Refreshes the bounds, border, icon, and visibility of the right icon.
     */
    private void refreshRightIcon() {
        if (rightIcon == null) return;

        if (rightIconLabel == null) {
            rightIconLabel = new JLabel();
            add(rightIconLabel);
        }

        int len = getHeight() - 2 * iconLabelPadding;
        if (rightIcon.getIconWidth() > len || rightIcon.getIconHeight() > len) {
            rightIcon = ImageUtil.ensureFitsInBounds(rightIcon, new Dimension(len, len));
        }

        setBorder(border);
        rightIconLabel.setIcon(rightIcon);
        rightIconLabel.setVisible(true);
        rightIconLabel.setBounds(getWidth() - iconLabelPadding - len, iconLabelPadding, len, len);
    }

    /**
     * Refreshes the left and right icons.
     */
    public void refreshLeftAndRightIcons() {
        refreshLeftIcon();
        refreshRightIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        refreshHintText();
        refreshLeftAndRightIcons();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        refreshHintText();
        refreshLeftAndRightIcons();
    }
}
