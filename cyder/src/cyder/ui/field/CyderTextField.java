package cyder.ui.field;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.utils.ColorUtil;
import cyder.utils.ImageUtil;
import cyder.utils.StringUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Cyder implementation of a text field.
 */
public class CyderTextField extends JTextField {
    /**
     * The character limit.
     */
    private int limit;

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
     * The default border for Cyder text fields.
     */
    private static final LineBorder DEFAULT_BORDER = new LineBorder(CyderColors.navy, 5, false);

    /**
     * The border currently set on this text field.
     */
    private Border border = DEFAULT_BORDER;

    /**
     * Constructs a new Cyder TextField object with no character limit.
     */
    public CyderTextField() {
        this(0);
    }

    /**
     * Constructs a new Cyder TextField object.
     *
     * @param charLimit the character limit for the text field
     */
    public CyderTextField(int charLimit) {
        super(charLimit == 0 ? Integer.MAX_VALUE : charLimit);

        this.limit = charLimit == 0 ? Integer.MAX_VALUE : charLimit;
        this.keyEventRegexMatcher = null;

        addKeyListener(regexAndLimitKeyListener);
        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());
        addHintTextFocusListener();
        addHintTextKeyListener();

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

    KeyListener regexAndLimitKeyListener = new KeyAdapter() {
        public void keyTyped(KeyEvent evt) {
            regexAndLimitMatcherLogic();
        }

        public void keyPressed(KeyEvent evt) {
            regexAndLimitMatcherLogic();
        }

        public void keyReleased(KeyEvent evt) {
            regexAndLimitMatcherLogic();
        }
    };

    @ForReadability
    private void regexAndLimitMatcherLogic() {
        if (getText().length() > limit) {
            setText(getText().substring(0, getText().length() - 1));
            Toolkit.getDefaultToolkit().beep();
        } else if (keyEventRegexMatcher != null
                && !keyEventRegexMatcher.isEmpty()
                && getText() != null
                && !getText().isEmpty()) {
            if (!currentTextMatchesPattern()) {
                setText(getText().substring(0, getText().length() - 1));
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    /**
     * Returns whether the current text matches the currently set pattern.
     *
     * @return whether the current text matches the currently set pattern
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean currentTextMatchesPattern() {
        checkNotNull(getText());
        checkNotNull(keyEventRegexMatcher);
        checkNotNull(keyEventRegexPattern);

        return keyEventRegexPattern.matcher(getText()).matches();
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
        keyEventRegexMatcher = regex;
        keyEventRegexPattern = Pattern.compile(keyEventRegexMatcher);
    }

    /**
     * The hex color matcher compiled pattern.
     */
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("[A-Fa-f0-9]{0,6}");

    /**
     * Sets the regex matcher to only accept hex color codes
     */
    public void setHexColorRegexMatcher() {
        keyEventRegexMatcher = HEX_COLOR_PATTERN.pattern();
        keyEventRegexPattern = HEX_COLOR_PATTERN;
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
        this.limit = limit;

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
        return limit;
    }

    /**
     * The padding between the border and left or right icon label and the start of the field input.
     */
    private static final int ADDITIONAL_ICON_LABEL_ADDING = 5;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBorder(Border border) {
        int len = getHeight() - 2 * ICON_LABEL_PADDING + ADDITIONAL_ICON_LABEL_ADDING;
        int leftInsets = leftIcon != null ? len : 0;
        int rightInsets = rightIcon != null ? len : 0;

        this.border = border;
        Border paddingBorder = new EmptyBorder(0, leftInsets, 0, rightInsets);
        super.setBorder(new CompoundBorder(border, paddingBorder));
    }

    /**
     * The key listener used to auto-capitalize the first letter of the field.
     */
    private final KeyAdapter autoCapitalizeListener = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            if (getText().length() == 1) {
                setText(getText().toUpperCase());
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (getText().length() == 1) {
                setText(getText().toUpperCase());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (getText().length() == 1) {
                setText(getText().toUpperCase());
            }
        }
    };

    /**
     * Whether auto capitalization is on.
     */
    private boolean autoCapitalize;

    /**
     * Sets whether to capitalize the first letter of the form.
     *
     * @param enable whether to capitalize the first letter of the form
     */
    public void setAutoCapitalization(boolean enable) {
        if (enable && !autoCapitalize) {
            addKeyListener(autoCapitalizeListener);
        } else {
            removeKeyListener(autoCapitalizeListener);
        }

        autoCapitalize = enable;
    }

    /**
     * Adds auto capitalization to the provided text field.
     *
     * @param textField the text field to add auto capitalization to
     */
    public static void addAutoCapitalizationAdapter(JTextField textField) {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                autoCapitalizationLogic(textField);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                autoCapitalizationLogic(textField);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                autoCapitalizationLogic(textField);
            }
        });
    }

    /**
     * The logic for performing auto capitalization on a text field.
     *
     * @param textField the text field
     */
    @ForReadability
    private static void autoCapitalizationLogic(JTextField textField) {
        if (textField.getText().length() == 1) {
            textField.setText(textField.getText().toUpperCase());
        }
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
     * The default flash color.
     */
    private static final Color DEFAULT_FLASH_COLOR = CyderColors.regularRed;

    /**
     * The time a flash animation takes.
     */
    private static final int flashDurationMs = 500;

    /**
     * The name of the flash animation thread.
     */
    private static final String FLASH_ANIMATION_THREAD_NAME = "Cyder Text Field Flash Animator";

    /**
     * Performs a flash on the field.
     */
    public void flashField() {
        Color startingColor = getForeground();
        ImmutableList<Color> flashColors = ColorUtil.getFlashColors(DEFAULT_FLASH_COLOR, startingColor);
        int timeout = flashDurationMs / flashColors.size();

        CyderThreadRunner.submit(() -> {
            flashColors.forEach(color -> {
                setForeground(color);
                repaint();
                ThreadUtil.sleep(timeout);
            });

            setForeground(startingColor);
        }, FLASH_ANIMATION_THREAD_NAME);
    }

    // ---------------
    // Hint text logic
    // ---------------

    /**
     * The hint text for the field.
     */
    private String hintText;

    /**
     * The hint text label for the field.
     */
    private JLabel hintTextLabel;

    /**
     * Possible hint text alignments.
     */
    public enum HintTextAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * The hint text alignment for this field.
     */
    private HintTextAlignment hintTextAlignment = HintTextAlignment.LEFT;

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
        if (hintTextLabel == null) addHintTextLabel();
        hintTextLabel.setText(hintText);

        Dimension size = getSize();
        int iconLen = getHeight() - 2 * ICON_LABEL_PADDING;

        int start = HINT_LABEL_PADDING;
        if (leftIcon != null) {
            start = iconLen + ADDITIONAL_ICON_LABEL_ADDING + HINT_LABEL_PADDING;
        }

        int end = getWidth();
        if (rightIcon != null) {
            end = getWidth() - iconLen - HINT_LABEL_PADDING;
        }

        int width = end - start;
        int defaultHeight = (int) size.getHeight() - 2 * HINT_LABEL_PADDING;
        hintTextLabel.setBounds(start, HINT_LABEL_PADDING, width, defaultHeight);

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
     * Adds the hint text focus listener to this field.
     */
    @ForReadability
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
    @ForReadability
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

    /**
     * The padding for the hint text label on this component.
     */
    private static final int HINT_LABEL_PADDING = 5;

    /**
     * Adds the hint text label to this component.
     */
    private void addHintTextLabel() {
        hintTextLabel = new JLabel();
        add(hintTextLabel);
        refreshHintText();
        hintTextLabel.setVisible(true);
    }

    // ---------
    // Left icon
    // ---------

    private static final int ICON_LABEL_PADDING = 5;

    /**
     * The label to hold the left icon.
     */
    private JLabel leftIconLabel;

    /**
     * The left icon.
     */
    private ImageIcon leftIcon;

    /**
     * Sets the left icon for this text field.
     *
     * @param leftIcon the left icon for this text field
     */
    public void setLeftIcon(ImageIcon leftIcon) {
        this.leftIcon = Preconditions.checkNotNull(leftIcon);
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
     * Refreshes the left icon bounds, border, icon, and visibility.
     */
    private void refreshLeftIcon() {
        if (leftIcon == null) return;
        if (leftIconLabel == null) addLeftIconLabel();

        int len = getHeight() - 2 * ICON_LABEL_PADDING;
        if (leftIcon.getIconWidth() > len || leftIcon.getIconHeight() > len) {
            leftIcon = ImageUtil.resizeImage(leftIcon, len, len);
        }

        setBorder(border);
        leftIconLabel.setIcon(leftIcon);
        leftIconLabel.setVisible(true);
        leftIconLabel.setBounds(ICON_LABEL_PADDING, ICON_LABEL_PADDING, len, len);
    }

    /**
     * Creates and adds the left icon label to this component.
     */
    private void addLeftIconLabel() {
        leftIconLabel = new JLabel();
        add(leftIconLabel);
        refreshLeftIcon();
    }

    // ----------
    // Right Icon
    // ----------

    /**
     * The label to hold the right icon.
     */
    private JLabel rightIconLabel;

    /**
     * The right icon.
     */
    private ImageIcon rightIcon;

    /**
     * Sets the right icon for this text field.
     *
     * @param rightIcon the right icon for this text field
     */
    public void setRightIcon(ImageIcon rightIcon) {
        this.rightIcon = Preconditions.checkNotNull(rightIcon);
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
     * Refreshes the right icon bounds, border, icon, and visibility.
     */
    private void refreshRightIcon() {
        if (rightIcon == null) return;
        if (rightIconLabel == null) addRightIconLabel();

        int len = getHeight() - 2 * ICON_LABEL_PADDING;
        if (rightIcon.getIconWidth() > len || rightIcon.getIconHeight() > len) {
            rightIcon = ImageUtil.resizeImage(rightIcon, len, len);
        }

        setBorder(border);
        rightIconLabel.setIcon(rightIcon);
        rightIconLabel.setVisible(true);
        rightIconLabel.setBounds(getWidth() - ICON_LABEL_PADDING - len, ICON_LABEL_PADDING, len, len);
    }

    /**
     * Creates and adds the right icon label to this component.
     */
    private void addRightIconLabel() {
        rightIconLabel = new JLabel();
        add(rightIconLabel);
        refreshRightIcon();
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
