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
import cyder.utils.StringUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
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
        setBorder(new LineBorder(CyderColors.navy, 5, false));
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
     * The text field's LineBorder if applicable.
     */
    private LineBorder lineBorder;

    /**
     * The color used for valid form data.
     */
    private final Color validFormDataColor = CyderColors.regularGreen;

    /**
     * The data used for invalid form data.
     */
    private final Color invalidFormDataColor = CyderColors.regularRed;

    /**
     * {@inheritDoc}
     * <p>
     * If line borders are used, then the invalid
     * and valid form data methods may be called.
     */
    @Override
    public void setBorder(Border border) {
        if (border instanceof LineBorder) {
            lineBorder = (LineBorder) border;
        } else {
            lineBorder = null;
        }

        // no need to cast since instanceof LineBorder is ensured
        super.setBorder(border);
    }

    /**
     * Sets the border to a green color to let the user know the provided input is valid.
     */
    public void informValidData() {
        checkArgument(lineBorder != null);

        setBorder(new LineBorder(validFormDataColor,
                lineBorder.getThickness(), lineBorder.getRoundedCorners()));
    }

    /**
     * Sets the border to a red color to let the user know the provided input is invalid.
     */
    public void informInvalidData() {
        checkArgument(lineBorder != null);

        setBorder(new LineBorder(invalidFormDataColor,
                lineBorder.getThickness(), lineBorder.getRoundedCorners()));
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

    /**
     * The hint text for the field.
     */
    private String hintText;

    /**
     * The hint text label for the field.
     */
    private JLabel hintTextLabel;

    /**
     * Whether the hint text should be shown if the proper conditions are met.
     */
    private boolean hintTextEnabled;

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
     * Sets whether the hint text should appear if the proper conditions are met.
     *
     * @param enabled whether the hint text should appear if the proper conditions are met
     */
    public void setHintTextEnabled(boolean enabled) {
        this.hintTextEnabled = enabled;
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
    }

    /**
     * Refreshes the hint text and label visibility.
     */
    private void refreshHintText() {
        if (hintTextLabel == null) addHintTextLabel();
        hintTextLabel.setText(hintText);

        Dimension size = getSize();
        hintTextLabel.setBounds(HINT_LABEL_PADDING, HINT_LABEL_PADDING,
                (int) size.getWidth() - 2 * HINT_LABEL_PADDING,
                (int) size.getHeight() - 2 * HINT_LABEL_PADDING);

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
                hintTextLabel.setVisible(false);
                refreshHintText();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().length() == 0) {
                    hintTextLabel.setVisible(true);
                }

                refreshHintText();
            }
        });
    }

    /**
     * Whether the char being typed currently is the first character in this field.
     * This is used to block refreshing of the hint text and label on the subsequent
     * released and typed events that will be generated.
     */
    private final AtomicBoolean enteringFirstChar = new AtomicBoolean();

    /**
     * Adds the hint text key listener to this field.
     */
    @ForReadability
    private void addHintTextKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (enteringFirstChar.get()) return;
                hintKeyListenerLogic();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                boolean backspace = e.getKeyCode() == KeyEvent.VK_BACK_SPACE;
                int length = getText().length();

                if (backspace && length == 1) {
                    hintTextLabel.setVisible(true);
                } else if (!backspace && length == 0) {
                    enteringFirstChar.set(true);
                    hintTextLabel.setVisible(false);
                } else {
                    enteringFirstChar.set(false);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (enteringFirstChar.get()) return;
                hintKeyListenerLogic();
            }
        });
    }

    /**
     * The logic for key pressed, released, and typed events from the hint text key listener.
     */
    @ForReadability
    private void hintKeyListenerLogic() {
        if (getText().length() != 0) {
            hintTextLabel.setVisible(false);
        } else if (hintTextEnabled) {
            hintTextLabel.setVisible(true);
        }
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
        hintTextEnabled = true;
        refreshHintText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        refreshHintText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        refreshHintText();
    }
}
