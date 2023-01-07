package cyder.ui.button;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.ToStringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.drag.CyderDraggableComponent;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalButtonUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * A custom styled button for use throughout Cyder.
 */
public class CyderButton extends JButton {
    /**
     * The text color to use when the button is disabled.
     */
    public static final Color DISABLED_TEXT_COLOR = Color.black;

    /**
     * The background color to use when the button is disabled.
     */
    public static final Color DISABLED_BACKGROUND_COLOR = Color.darkGray;

    /**
     * The amount to delay by when alerting.
     */
    public static final int ALERT_DELAY = 300;

    /**
     * The number of times to alert by default.
     */
    public static final int ALERT_ITERATIONS = 8;

    /**
     * The hover color for when the button is hovered via a mouse.
     */
    private Color hoverBackgroundColor;

    /**
     * The pressed color for when the button is currently pressed via a mouse.
     */
    private Color pressedBackgroundColor;

    /**
     * Whether threads for this object have been killed.
     */
    private boolean threadsKilled;

    /**
     * The background color of the button.
     */
    private Color backgroundColor;

    /**
     * The text to place to the left of the text on {@link #setText(String)} calls.
     */
    private String leftTextPadding = "";

    /**
     * The text to place to the right of the text on {@link #setText(String)} calls.
     */
    private String rightTextPadding = "";

    /**
     * Constructs a new CyderButton
     */
    public CyderButton() {
        this("");
    }

    /**
     * Constructs a new CyderButton with the provided text.
     *
     * @param text the text to use for the button
     */
    public CyderButton(String text) {
        super(Preconditions.checkNotNull(text));

        super.setContentAreaFilled(false);

        addMouseMotionListener(new CyderDraggableComponent());
        addActionListener(e -> Logger.log(LogTag.UI_ACTION, this));

        setFont(CyderFonts.SEGOE_20);
        setBackground(CyderColors.buttonColor);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setBorder(new LineBorder(CyderColors.navy, 5, false));

        setUI(generateMetalButtonUi());

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Generates and returns a new metal button UI.
     *
     * @return a new metal button UI
     */
    private static MetalButtonUI generateMetalButtonUi() {
        return new MetalButtonUI() {
            @Override
            protected Color getDisabledTextColor() {
                return DISABLED_TEXT_COLOR;
            }

            @Override
            protected Color getSelectColor() {
                return DISABLED_BACKGROUND_COLOR;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(Graphics g) {
        setFocusPainted(false);
        if (getModel().isPressed()) {
            g.setColor(pressedBackgroundColor);
        } else if (getModel().isRollover()) {
            g.setColor(hoverBackgroundColor);
        } else {
            g.setColor(getBackground());
        }

        g.fillRect(0, 0, getWidth(), getHeight());

        super.paintComponent(g);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setText(String text) {
        Preconditions.checkNotNull(text);

        super.setText((leftTextPadding != null ? leftTextPadding : "")
                + text.trim() + (rightTextPadding != null ? rightTextPadding : ""));
    }

    /**
     * Does nothing for a CyderButton.
     *
     * @param ignored a boolean which will be ignored
     */
    @Override
    @Deprecated
    public void setContentAreaFilled(boolean ignored) {}

    /**
     * Sets the background color of this button.
     * The hover color is set to {@link Color#darker()} and the pressed color
     * is set to color.darker().darker().
     *
     * @param color the background color
     */
    @Override
    public void setBackground(Color color) {
        super.setBackground(Preconditions.checkNotNull(color));

        backgroundColor = color;
        pressedBackgroundColor = color.darker().darker();
        hoverBackgroundColor = color.darker();
    }

    /**
     * Returns the hover background color.
     *
     * @return the hover background color
     */
    public Color getHoverBackgroundColor() {
        return hoverBackgroundColor;
    }

    /**
     * Sets the hover background color.
     *
     * @param hoverBackgroundColor the hover background color
     */
    public void setHoverBackgroundColor(Color hoverBackgroundColor) {
        Preconditions.checkNotNull(hoverBackgroundColor);

        this.hoverBackgroundColor = hoverBackgroundColor;
    }

    /**
     * Returns the pressed background color.
     *
     * @return the pressed background color
     */
    public Color getPressedBackgroundColor() {
        return pressedBackgroundColor;
    }

    /**
     * Sets the pressed background color.
     *
     * @param pressedBackgroundColor the pressed background color
     */
    public void setPressedBackgroundColor(Color pressedBackgroundColor) {
        Preconditions.checkNotNull(pressedBackgroundColor);

        this.pressedBackgroundColor = pressedBackgroundColor;
    }

    /**
     * Invokes an alert using {@link #ALERT_ITERATIONS}
     */
    public void alert() {
        alert(ALERT_ITERATIONS);
    }

    /**
     * Invokes an alert using the provided number of iterations
     *
     * @param iterations the number of times to iterate for the alert
     */
    public void alert(int iterations) {
        Preconditions.checkArgument(iterations > 0);

        CyderThreadRunner.submit(() -> {
            try {
                Color background = backgroundColor;
                Color darkerBackground = backgroundColor.darker();

                for (int i = 0 ; i < iterations ; i++) {
                    setBackground(darkerBackground);
                    ThreadUtil.sleep(ALERT_DELAY);

                    setBackground(background);
                    ThreadUtil.sleep(ALERT_DELAY);

                    if (getParent() == null) {
                        killThreads();
                    }

                    if (threadsKilled) {
                        return;
                    }
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, getName() + " Alerter");
    }

    /**
     * Kills any threads associated with this CyderButton instance.
     */
    public void killThreads() {
        threadsKilled = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringUtil.commonUiComponentToString(this);
    }

    /**
     * Adds the default focus listener to this CyderButton.
     */
    public void addDefaultFocusListener() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);

                if (isEnabled()) {
                    setBackground(backgroundColor.darker());
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);

                if (isEnabled()) {
                    setBackground(backgroundColor);
                }
            }
        });
    }

    /**
     * Returns the left text padding.
     *
     * @return the left text padding
     */
    public String getLeftTextPadding() {
        return leftTextPadding;
    }

    /**
     * Sets the left text padding.
     *
     * @param leftTextPadding the left text padding
     */
    public void setLeftTextPadding(String leftTextPadding) {
        Preconditions.checkNotNull(leftTextPadding);

        this.leftTextPadding = leftTextPadding;
        setText(getText());
    }

    /**
     * Returns the right text padding.
     *
     * @return the right text padding
     */
    public String getRightTextPadding() {
        return rightTextPadding;
    }

    /**
     * Sets the right text padding.
     *
     * @param rightTextPadding the right text padding
     */
    public void setRightTextPadding(String rightTextPadding) {
        Preconditions.checkNotNull(rightTextPadding);

        this.rightTextPadding = rightTextPadding;
        setText(getText());
    }
}