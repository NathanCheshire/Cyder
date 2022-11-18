package cyder.ui.field;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.annotations.CyderTest;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.frame.CyderFrame;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A modern looking text field.
 */
public class CyderModernTextField extends JTextField {
    /**
     * The range percents must fall into.
     */
    private static final Range<Float> percentageRange = Range.closed(0.0f, 100.0f);

    /**
     * The name of the ripple animation incrementer thread.
     */
    private static final String rippleAnimationIncrementerThreadName =
            "CyderModernTextField Ripple Animation Incrementer";

    /**
     * The name of the ripple animation decrementer thread.
     */
    private static final String rippleAnimationDecrementerThreadName =
            "CyderModernTextField Ripple Animation Decrementer";

    /**
     * The range of valid timeouts for the ripple animation.
     */
    private static final Range<Integer> rippleAnimationTimeoutRange = Range.closed(1, 1000);

    /**
     * The inset values for the top, left, and right default border.
     */
    private static final int topLeftRightBorderInsets = 0;

    /**
     * The default ripple label thickness.
     */
    private static final int defaultRippleLabelThickness = 4;

    /**
     * The current ripple label thickness.
     */
    private int rippleLabelThickness = defaultRippleLabelThickness;

    /**
     * Whether the ripple animation should be performed
     */
    private final AtomicBoolean shouldPerformFocusRipple = new AtomicBoolean(true);

    /**
     * Whether this component has focus.
     */
    private final AtomicBoolean focused = new AtomicBoolean(false);

    /**
     * The current width of the ripple label.
     */
    private final AtomicInteger rippleLabelWidth = new AtomicInteger(0);

    /**
     * The ripple animation color.
     */
    private Color rippleColor = CyderColors.regularRed;

    /**
     * The amount to increment or decrement the ripple animation by each frame.
     */
    private int rippleAnimationDelta = 5;

    /**
     * The delay between ripple animation frames in ms.
     */
    private int rippleAnimationTimeout = 2;

    /**
     * The minimum percentage of the width the ripple label can be.
     */
    private float minRippleLabelLengthPercentage = percentageRange.lowerEndpoint();

    /**
     * The maximum percentage of the width the ripple label can be.
     */
    private float maxRippleLabelWidthPercentage = percentageRange.upperEndpoint();

    /**
     * Whether the ripple decrement animation is currently underway.
     */
    private final AtomicBoolean inRippleDecrementAnimation = new AtomicBoolean(false);

    /**
     * Whether the ripple increment animation is currently underway.
     */
    private final AtomicBoolean inRippleIncrementAnimation = new AtomicBoolean(false);

    /**
     * The background color of this text field.
     */
    private Color backgroundColor = CyderColors.vanilla;

    /**
     * Whether auto-capitalization should be performed.
     */
    private final AtomicBoolean shouldPerformAutoCapitalization = new AtomicBoolean(true);

    /**
     * Whether the character limit should be enforced on key events.
     */
    private final AtomicBoolean shouldEnforceCharacterLimit = new AtomicBoolean(false);

    /**
     * Constructs a new modern text field.
     */
    public CyderModernTextField() {
        this("");
    }

    /**
     * Constructs a new modern text field with the provided initial text.
     *
     * @param text the initial text
     */
    public CyderModernTextField(String text) {
        super(text);

        addRippleFocusListener();
        addAutoCapitalizationKeyListener();
        addCharacterLimitKeyListener();

        setBackground(backgroundColor);
        setSelectionColor(CyderColors.selectionColor);
        setFont(CyderFonts.DEFAULT_FONT_SMALL);

        setForeground(CyderColors.navy);
        setCaretColor(CyderColors.navy);
        setCaret(new CyderCaret(CyderColors.navy));

        refreshBorder();
        CyderModernTextField.this.add(rippleLabel);

        setOpaque(true);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Suppress a constructor.
     */
    @SuppressWarnings("unused")
    public CyderModernTextField(int columns) {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Suppress a constructor.
     */
    @SuppressWarnings("unused")
    public CyderModernTextField(String text, int columns) {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Suppress a constructor.
     */
    @SuppressWarnings("unused")
    public CyderModernTextField(Document doc, String text, int columns) {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Returns whether the ripple animation should be performed on focus events.
     *
     * @return whether the ripple animation should be performed on focus events
     */
    public boolean getShouldPerformFocusRipple() {
        return shouldPerformFocusRipple.get();
    }

    /**
     * Sets whether the ripple animation should be performed on focus events.
     *
     * @param shouldPerformFocusRipple whether the ripple animation should be performed on focus events
     */
    public void setShouldPerformFocusRipple(boolean shouldPerformFocusRipple) {
        this.shouldPerformFocusRipple.set(shouldPerformFocusRipple);
    }

    /**
     * Returns the ripple label thickness.
     *
     * @return the ripple label thickness
     */
    public int getRippleLabelThickness() {
        return rippleLabelThickness;
    }

    /**
     * Sets the ripple label thickness.
     *
     * @param rippleLabelThickness the ripple label thickness
     */
    public void setRippleLabelThickness(int rippleLabelThickness) {
        this.rippleLabelThickness = rippleLabelThickness;

        refreshBorder();
        refreshRippleLabelBounds();
    }

    /**
     * Returns the ripple color.
     *
     * @return the ripple color
     */
    public Color getRippleColor() {
        return rippleColor;
    }

    /**
     * Sets the ripple color.
     *
     * @param rippleColor the ripple color
     */
    public void setRippleColor(Color rippleColor) {
        Preconditions.checkNotNull(rippleColor);

        this.rippleColor = rippleColor;
    }

    /**
     * Adds the ripple focus listener to this text field.
     */
    @ForReadability
    private void addRippleFocusListener() {
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                focused.set(true);
                refreshRippleLabelBounds();

                if (!shouldPerformFocusRipple.get()) {
                    rippleLabelWidth.set((int) (getWidth() * maxRippleLabelWidthPercentage));
                    rippleLabel.setSize(rippleLabelWidth.get(), rippleLabelThickness);
                    rippleLabel.repaint();
                    return;
                }

                startRippleIncrementAnimation();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused.set(false);
                refreshRippleLabelBounds();

                if (!shouldPerformFocusRipple.get()) {
                    rippleLabelWidth.set((int) (getWidth() * minRippleLabelLengthPercentage));
                    rippleLabel.setSize(rippleLabelWidth.get(), rippleLabelThickness);
                    rippleLabel.repaint();
                    return;
                }

                startRippleDecrementAnimation();
            }
        });
    }

    /**
     * The label for the ripple animation.
     */
    private final JLabel rippleLabel = new JLabel() {
        @Override
        public void paint(Graphics g) {
            g.setColor(rippleColor);
            g.fillRect(0, 0, rippleLabelWidth.get(), rippleLabelThickness);
        }
    };

    /**
     * Returns the increment/decrement for the ripple animation.
     *
     * @return the increment/decrement for the ripple animation
     */
    public int getRippleAnimationDelta() {
        return rippleAnimationDelta;
    }

    /**
     * Sets the increment/decrement for the ripple animation.
     *
     * @param rippleAnimationDelta the increment/decrement for the ripple animation
     */
    public void setRippleAnimationDelta(int rippleAnimationDelta) {
        Preconditions.checkArgument(rippleAnimationDelta > 0);

        this.rippleAnimationDelta = rippleAnimationDelta;
    }

    /**
     * Returns the timeout between ripple animation frames in ms.
     *
     * @return the timeout between ripple animation frames in ms
     */
    public int getRippleAnimationTimeout() {
        return rippleAnimationTimeout;
    }

    /**
     * Sets the timeout between ripple animation frames in ms.
     *
     * @param rippleAnimationTimeout the timeout between ripple animation frames in ms
     */
    public void setRippleAnimationTimeout(int rippleAnimationTimeout) {
        Preconditions.checkArgument(rippleAnimationTimeoutRange.contains(rippleAnimationTimeout));

        this.rippleAnimationTimeout = rippleAnimationTimeout;
    }

    /**
     * Returns whether the ripple increment animation is currently underway.
     *
     * @return the ripple increment animation is currently underway
     */
    public boolean inRippleIncrementAnimation() {
        return inRippleIncrementAnimation.get();
    }

    /**
     * Starts the increment ripple animation.
     */
    @ForReadability
    private void startRippleIncrementAnimation() {
        Preconditions.checkState(focused.get());

        inRippleIncrementAnimation.set(true);

        CyderThreadRunner.submit(() -> {
            while (rippleLabelWidth.get() < getWidth() && focused.get() && shouldPerformFocusRipple.get()) {
                rippleLabelWidth.getAndAdd(rippleAnimationDelta);
                rippleLabel.setSize(rippleLabelWidth.get(), rippleLabelThickness);
                rippleLabel.repaint();
                ThreadUtil.sleep(rippleAnimationTimeout);
            }

            rippleLabelWidth.set((int) (getWidth() * maxRippleLabelWidthPercentage));
            rippleLabel.setSize(rippleLabelWidth.get(), rippleLabelThickness);
            rippleLabel.repaint();

            inRippleIncrementAnimation.set(false);
        }, rippleAnimationIncrementerThreadName);
    }

    /**
     * Returns the minimum percentage of the width the ripple label can be.
     *
     * @return the minimum percentage of the width the ripple label can be
     */
    public float getMinRippleLabelLengthPercentage() {
        return minRippleLabelLengthPercentage;
    }

    /**
     * Sets the minimum percentage of the width the ripple label can be.
     *
     * @param minRippleLabelLengthPercentage the minimum percentage of the width the ripple label can be
     */
    public void setMinRippleLabelLengthPercentage(float minRippleLabelLengthPercentage) {
        Preconditions.checkArgument(percentageRange.contains(minRippleLabelLengthPercentage));

        this.minRippleLabelLengthPercentage = minRippleLabelLengthPercentage;
    }

    /**
     * Returns the maximum percentage of the width the ripple label can be.
     *
     * @return the maximum percentage of the width the ripple label can be
     */
    public float getMaxRippleLabelWidthPercentage() {
        return maxRippleLabelWidthPercentage;
    }

    /**
     * Sets the maximum percentage of the width the ripple label can be.
     *
     * @param maxRippleLabelWidthPercentage the maximum percentage of the width the ripple label can be
     */
    public void setMaxRippleLabelWidthPercentage(float maxRippleLabelWidthPercentage) {
        Preconditions.checkArgument(percentageRange.contains(maxRippleLabelWidthPercentage));

        this.maxRippleLabelWidthPercentage = maxRippleLabelWidthPercentage;
    }

    /**
     * Returns whether the ripple decrement animation is currently underway.
     *
     * @return whether the ripple decrement animation is currently underway
     */
    public boolean inRippleDecrementAnimation() {
        return inRippleDecrementAnimation.get();
    }

    /**
     * Starts the ripple decrement animation.
     */
    @ForReadability
    private void startRippleDecrementAnimation() {
        Preconditions.checkState(!focused.get());

        inRippleDecrementAnimation.set(true);

        CyderThreadRunner.submit(() -> {
            while (rippleLabelWidth.get() > 0 && !focused.get() && shouldPerformFocusRipple.get()) {
                rippleLabelWidth.getAndAdd(-rippleAnimationDelta);
                rippleLabel.setSize(rippleLabelWidth.get(), rippleLabelThickness);
                rippleLabel.repaint();
                ThreadUtil.sleep(rippleAnimationTimeout);
            }

            rippleLabelWidth.set((int) (getWidth() * minRippleLabelLengthPercentage));
            rippleLabel.setSize(rippleLabelWidth.get(), rippleLabelThickness);
            rippleLabel.repaint();

            inRippleDecrementAnimation.set(false);
        }, rippleAnimationDecrementerThreadName);
    }

    @CyderTest
    public static void test() {
        CyderFrame frame = new CyderFrame(600, 400);
        frame.setTitle("Test");

        CyderModernTextField cyderModernTextField = new CyderModernTextField("test");
        cyderModernTextField.setBounds(100, 100, 400, 40);
        frame.getContentPane().add(cyderModernTextField);

        CyderModernTextField cyderModernTextField2 = new CyderModernTextField("test 2");
        cyderModernTextField2.setBounds(100, 200, 400, 40);
        frame.getContentPane().add(cyderModernTextField2);

        frame.finalizeAndShow();
    }

    /**
     * Refreshes the ripple label bounds.
     */
    private void refreshRippleLabelBounds() {
        // Three because top and bottom of empty border and the bottom line border (this ripple border)
        rippleLabel.setBounds(0, getHeight() - 3 * rippleLabelThickness,
                rippleLabelWidth.get(), rippleLabelThickness);
    }

    /**
     * Refreshes the border meaning the border thickness will also be refreshed.
     */
    public void refreshBorder() {
        setBorder(null);
    }

    /**
     * Refreshes the thickness of the empty border.
     * This should only be invoked internally and not by clients.
     *
     * @param ignored the border to be ignored
     */
    public void setBorder(Border ignored) {
        super.setBorder(BorderFactory.createEmptyBorder(topLeftRightBorderInsets, topLeftRightBorderInsets,
                rippleLabelThickness, topLeftRightBorderInsets));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackground(Color newBackgroundColor) {
        Preconditions.checkNotNull(newBackgroundColor);

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
     * Returns whether auto-capitalization should be performed.
     *
     * @return whether auto-capitalization should be performed
     */
    private boolean shouldPerformAutoCapitalization() {
        return shouldPerformAutoCapitalization.get();
    }

    /**
     * Sets whether auto-capitalization should be performed.
     *
     * @param shouldPerformAutoCapitalization whether auto-capitalization should be performed
     */
    private void setShouldPerformAutoCapitalization(boolean shouldPerformAutoCapitalization) {
        this.shouldPerformAutoCapitalization.set(shouldPerformAutoCapitalization);
        autoCapitalizationLogic();
    }

    /**
     * Adds the auto-capitalization key listener to this text field.
     */
    private void addAutoCapitalizationKeyListener() {
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                autoCapitalizationLogic();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                autoCapitalizationLogic();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                autoCapitalizationLogic();
            }
        });
    }

    /**
     * The logic to perform on key events from the auto capitalization listener.
     */
    @ForReadability
    private void autoCapitalizationLogic() {
        if (shouldPerformAutoCapitalization.get()) {
            String text = getText();
            if (text.length() == 0) {
                setText(text.toUpperCase());
            }
        }
    }

    /**
     * Sets whether the character limit should be enforced.
     *
     * @return whether the character limit should be enforced
     */
    public boolean shouldEnforceCharacterLimit() {
        return shouldEnforceCharacterLimit.get();
    }

    /**
     * Sets whether the character limit should be enforced.
     *
     * @param shouldEnforceCharacterLimit whether the character limit should be enforced
     */
    public void setShouldEnforceCharacterLimit(boolean shouldEnforceCharacterLimit) {
        this.shouldEnforceCharacterLimit.set(shouldEnforceCharacterLimit);
        characterLimitLogic();
    }

    /**
     * The character limit for this field.
     */
    private int characterLimit = Integer.MAX_VALUE;

    /**
     * Returns the character limit for this text field.
     *
     * @return the character limit for this text field
     */
    public int getCharacterLimit() {
        return characterLimit;
    }

    /**
     * Sets the character limit for this text field.
     *
     * @param characterLimit the character limit for this text field
     */
    public void setCharacterLimit(int characterLimit) {
        Preconditions.checkArgument(characterLimit > -1);

        this.characterLimit = characterLimit;
        characterLimitLogic();
    }

    /**
     * Adds the character limit key listener to this text field.
     */
    private void addCharacterLimitKeyListener() {
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                characterLimitLogic();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                characterLimitLogic();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                characterLimitLogic();
            }
        });
    }

    /**
     * The logic to perform one key events of the character limit listener.
     */
    @ForReadability
    private void characterLimitLogic() {
        if (shouldEnforceCharacterLimit.get()) {
            String text = getText();
            if (text.length() > characterLimit) {
                setText(text.substring(0, characterLimit));
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    // todo regex listener

    // todo flash

    // todo hint text

    // todo override set size and bounds, need to refresh ripple label bounds
}
