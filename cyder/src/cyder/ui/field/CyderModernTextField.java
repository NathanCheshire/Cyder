package cyder.ui.field;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A modern looking text field.
 */
public class CyderModernTextField extends JTextField {
    private Color fieldForeground = CyderColors.navy;
    private Font fieldFont = CyderFonts.DEFAULT_FONT_SMALL;
    private Color fieldBackground = CyderColors.vanilla;

    private static final int defaultBorderThickness = 4;
    private static final Color defaultBorderColor = CyderColors.navy;

    private int borderThickness = defaultBorderThickness;
    private Color borderColor = defaultBorderColor;

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

        setBackground(fieldBackground);
        setSelectionColor(CyderColors.selectionColor);
        setFont(fieldFont);
        setForeground(fieldForeground);
        setCaretColor(fieldForeground);
        setCaret(new CyderCaret(fieldForeground));

        // todo this bottom value needs to always reflect the thickness set of the border currently
        setBorder(BorderFactory.createEmptyBorder(0, 0, borderThickness, 0));
        CyderModernTextField.this.add(borderLabel);

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

    // todo accessor mutator
    private final AtomicBoolean shouldPerformFocusRipple = new AtomicBoolean(true);
    private final AtomicBoolean focused = new AtomicBoolean(false);

    /**
     * Adds the ripple focus listener to this text field.
     */
    @ForReadability
    private void addRippleFocusListener() {
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                focused.set(true);
                if (!shouldPerformFocusRipple.get()) return;
                startRippleAnimation();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused.set(false);
                if (!shouldPerformFocusRipple.get()) return;
                recedeRippleAnimation();
            }
        });
    }

    // TODO min and max lengths for border

    private final AtomicInteger borderLabelWidth = new AtomicInteger(0);

    private final JLabel borderLabel = new JLabel() {
        @Override
        public void paint(Graphics g) {
            g.setColor(CyderColors.regularRed);
            g.fillRect(0, 0, borderLabelWidth.get(), borderThickness);
        }
    };

    /**
     * The amount to increment or decrement the ripple animation by each frame.
     */
    private int rippleAnimationDelta = 5;

    /**
     * The delay between ripple animation frames in ms.
     */
    private int rippleAnimationTimeout = 2;

    /**
     * The name of the ripple animation incrementer thread.
     */
    private static final String rippleAnimationIncrementerThreadName =
            "CyderModernTextField Ripple Animation Incrementer";

    @ForReadability
    private void startRippleAnimation() {
        refreshRippleLabelBounds();

        CyderThreadRunner.submit(() -> {
            while (borderLabelWidth.get() < getWidth() && focused.get()) {
                borderLabelWidth.getAndAdd(rippleAnimationDelta);
                borderLabel.setSize(borderLabelWidth.get(), borderThickness);
                borderLabel.repaint();
                ThreadUtil.sleep(rippleAnimationTimeout);
            }

            borderLabelWidth.set(getWidth());
            borderLabel.setSize(borderLabelWidth.get(), borderThickness);
            borderLabel.repaint();
        }, rippleAnimationIncrementerThreadName);
    }

    /**
     * The name of the ripple animation decrementer thread.
     */
    private static final String rippleAnimationDecrementerThreadName =
            "CyderModernTextField Ripple Animation Decrementer";

    private void recedeRippleAnimation() {
        refreshRippleLabelBounds();

        CyderThreadRunner.submit(() -> {
            while (borderLabelWidth.get() > 0 && !focused.get()) {
                borderLabelWidth.getAndAdd(-rippleAnimationDelta);
                borderLabel.setSize(borderLabelWidth.get(), borderThickness);
                borderLabel.repaint();
                ThreadUtil.sleep(rippleAnimationTimeout);
            }

            borderLabelWidth.set(0);
            borderLabel.setSize(borderLabelWidth.get(), borderThickness);
            borderLabel.repaint();
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
        borderLabel.setBounds(0, getHeight() - 3 * borderThickness, borderLabelWidth.get(), borderThickness);
    }

    // todo call this when borderThickness changes

    /**
     * Refreshes the thickness of the empty border.
     * This should only be invoked internally and not by clients.
     *
     * @param ignored the border to be ignored
     */
    public void setBorder(Border ignored) {
        super.setBorder(BorderFactory.createEmptyBorder(0, 0, borderThickness, 0));
    }
}
