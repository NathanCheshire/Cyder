package cyder.ui.selection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.animation.AnimationUtil;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.button.CyderButton;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * An animated binary switch with smooth transition animations.
 */
public class CyderSwitch extends JLabel {
    /**
     * The width of this switch.
     */
    private int width;

    /**
     * The height of this switch.
     */
    private int height;

    /**
     * The current state of the switch.
     */
    private CyderSwitchState state;

    /**
     * The inner button used to control the switch state.
     */
    private final CyderButton switchButton;

    /**
     * The percentage of the switch that the button should take.
     */
    private float buttonPercent = 25;

    /**
     * The delay between animation frames.
     */
    private int animationDelay = 4;

    /**
     * The text to use for the on state.
     */
    private String onText = "1";

    /**
     * The text to use for the off state.
     */
    private String indeterminateText = "?";

    /**
     * The text to use for the on state.
     */
    private String offText = "0";

    /**
     * The line border used for switches.
     */
    private static final LineBorder lineBorder = new LineBorder(CyderColors.navy, 5, false);

    /**
     * The default width of a switch.
     */
    private static final int DEFAULT_WIDTH = 400;

    /**
     * The default height of a switch.
     */
    private static final int DEFAULT_HEIGHT = 120;

    /**
     * The y padding of the switch button and the bounding box.
     */
    private static final int buttonYPadding = 10;

    /**
     * The x padding of the switch button and the bounding box.
     */
    private static final int buttonXPadding = 10;

    /**
     * The increment for state animations.
     */
    private int animationIncrement = 8;

    /**
     * The range the button percent must fall within.
     */
    private static final Range<Float> buttonPercentRange = Range.open(0.0f, 100.0f);

    /**
     * Constructs a new switch with a width of 400, a height of 120, and a state of off.
     */
    public CyderSwitch() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Constructs a new switch with an initial state of {@link CyderSwitchState#OFF}.
     *
     * @param width  the switch width
     * @param height the switch height
     */
    public CyderSwitch(int width, int height) {
        this(width, height, CyderSwitchState.OFF);
    }

    /**
     * Constructs a new switch from the provided parameters.
     *
     * @param size          the size of the switch
     * @param startingState the initial state for the switch
     */
    public CyderSwitch(Dimension size, CyderSwitchState startingState) {
        this(size.getSize().width, size.getSize().height, startingState);
    }

    /**
     * Constructs a new switch from the provided parameters.
     *
     * @param width         the width of the switch
     * @param height        the height of the switch
     * @param startingState the initial state for the switch
     */
    public CyderSwitch(int width, int height, CyderSwitchState startingState) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);

        this.width = width;
        this.height = height;
        state = Preconditions.checkNotNull(startingState);

        setSize(width, height);
        setBorder(lineBorder);

        switchButton = new CyderButton();
        switchButton.addActionListener(e -> {
            if (getState() == CyderSwitchState.OFF) {
                setState(CyderSwitchState.ON);
            } else {
                setState(CyderSwitchState.OFF);
            }
        });

        switchButton.setForeground(CyderColors.regularPink);
        switchButton.setBackground(CyderColors.navy);
        switchButton.setFont(CyderFonts.DEFAULT_FONT_SMALL);

        Dimension size = calculateSwitchButtonSize();
        switchButton.setSize(size.width, size.height);

        switchButton.setLocation(buttonXPadding, buttonYPadding);
        add(switchButton);

        setState(startingState);

        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Calculates the size of the switch button depending on the set button percentage.
     *
     * @return the main axis switch size.
     */
    @ForReadability
    private Dimension calculateSwitchButtonSize() {
        int w = (int) (this.width * (buttonPercent / buttonPercentRange.upperEndpoint())) - buttonXPadding;
        int h = this.height - 2 * buttonYPadding;

        return new Dimension(w, h);
    }

    /**
     * Returns the animation increment.
     *
     * @return the animation increment
     */
    public int getAnimationIncrement() {
        return animationIncrement;
    }

    /**
     * Sets the animation increment.
     *
     * @param animationIncrement the animation increment
     */
    public void setAnimationIncrement(int animationIncrement) {
        this.animationIncrement = animationIncrement;
    }

    /**
     * Sets and animates the state of the switch.
     *
     * @param state the new state of the switch
     */
    public void setState(CyderSwitchState state) {
        Preconditions.checkNotNull(state);

        if (this.state == state) {
            refreshButtonText();
            repaint();
            return;
        }

        this.state = state;

        boolean shouldAnimate = (isVisible() && getParent() != null);

        refreshButtonText();

        switch (state) {
            case ON -> {
                if (shouldAnimate) {
                    AnimationUtil.componentRight(switchButton.getX(),
                            width - switchButton.getWidth() - buttonXPadding,
                            animationDelay, animationIncrement, switchButton);
                }
                switchButton.setLocation(width - switchButton.getWidth() - buttonXPadding, buttonYPadding);
            }
            case OFF -> {
                if (shouldAnimate) {
                    AnimationUtil.componentLeft(switchButton.getX(), buttonXPadding,
                            animationDelay, animationIncrement, switchButton);
                }
                switchButton.setLocation(buttonXPadding, buttonYPadding);
            }
            case INDETERMINATE -> {
                if (switchButton.getX() > buttonXPadding) {
                    if (shouldAnimate) {
                        switchButton.setLocation(width - switchButton.getWidth() - buttonXPadding, buttonYPadding);
                        AnimationUtil.componentLeft(switchButton.getX(), width / 2 - switchButton.getWidth() / 2,
                                animationDelay, animationIncrement, switchButton);
                    }
                    switchButton.setLocation(width / 2 - switchButton.getWidth() / 2, buttonYPadding);
                } else {
                    if (shouldAnimate) {
                        switchButton.setLocation(buttonXPadding, buttonYPadding);
                        AnimationUtil.componentRight(buttonXPadding, width / 2 - switchButton.getWidth() / 2,
                                animationDelay, animationIncrement, switchButton);
                    }
                    switchButton.setLocation(buttonXPadding, buttonYPadding);
                }
            }
        }

        repaint();
    }

    /**
     * Refreshes the switch button text.
     */
    public void refreshButtonText() {
        switch (state) {
            case ON -> switchButton.setText(onText);
            case OFF -> switchButton.setText(offText);
            case INDETERMINATE -> switchButton.setText(indeterminateText);
        }
    }

    /**
     * Returns the button used for the switch.
     *
     * @return the button used for the switch
     */
    public CyderButton getSwitchButton() {
        return switchButton;
    }

    /**
     * Returns the state of the switch.
     *
     * @return the state of the switch
     */
    public CyderSwitchState getState() {
        return state;
    }

    /**
     * Returns the next state that the switch will be set to following a switch action.
     *
     * @return the next state that the switch will be set to following a switch action
     */
    @SuppressWarnings("UnnecessaryDefault")
    public CyderSwitchState getNextState() {
        return switch (state) {
            case ON, INDETERMINATE -> CyderSwitchState.OFF;
            case OFF -> CyderSwitchState.ON;
            default -> throw new IllegalStateException("Invalid switch state: " + state);
        };
    }

    /**
     * Returns the switch of this switch.
     *
     * @return the switch of this switch
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this switch.
     *
     * @return the height of this switch
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Sets the width of this switch.
     *
     * @param width the width of this switch
     */
    public void setWidth(int width) {
        Preconditions.checkArgument(width > 0);
        this.width = width;
    }

    /**
     * Sets the height of this switch.
     *
     * @param height the height of this switch
     */
    public void setHeight(int height) {
        Preconditions.checkArgument(height > 0);
        this.height = height;
    }

    /**
     * Sets the button percentage.
     *
     * @return the button percentage
     */
    public float getButtonPercent() {
        return buttonPercent;
    }

    /**
     * Returns the animation delay.
     *
     * @return the animation delay
     */
    public int getAnimationDelay() {
        return animationDelay;
    }

    /**
     * Sets and updates the button percentage.
     *
     * @param buttonPercent and updates the button percentage
     */
    public void setButtonPercent(float buttonPercent) {
        Preconditions.checkArgument(buttonPercentRange.contains(buttonPercent));

        this.buttonPercent = buttonPercent;
        Dimension size = calculateSwitchButtonSize();
        switchButton.setSize(size.width, size.height);

        switch (state) {
            case ON -> switchButton.setLocation(width - switchButton.getWidth() - buttonXPadding, buttonYPadding);
            case OFF -> switchButton.setLocation(buttonXPadding, buttonYPadding);
            case INDETERMINATE -> switchButton.setLocation(width / 2 - switchButton.getWidth() / 2, buttonYPadding);
        }
    }

    /**
     * Sets the animation delay.
     *
     * @param animationDelay the animation delay
     */
    public void setAnimationDelay(int animationDelay) {
        this.animationDelay = animationDelay;
    }

    /**
     * Returns the text used for the on state.
     *
     * @return the text used for the on state
     */
    public String getOnText() {
        return onText;
    }

    /**
     * Sets the text used for the on state.
     *
     * @param onText the text used for the on state
     */
    public void setOnText(String onText) {
        Preconditions.checkNotNull(onText);
        Preconditions.checkArgument(!onText.isEmpty());

        this.onText = onText;
        setState(state);
        switchButton.repaint();
    }

    /**
     * Returns the text used for the indeterminate state.
     *
     * @return the text used for the indeterminate state
     */
    public String getIndeterminateText() {
        return indeterminateText;
    }

    /**
     * Sets the text used for the indeterminate state.
     *
     * @param indeterminateText the text used for the indeterminate state
     */
    public void setIndeterminateText(String indeterminateText) {
        Preconditions.checkNotNull(indeterminateText);
        Preconditions.checkArgument(!indeterminateText.isEmpty());

        this.indeterminateText = indeterminateText;
        setState(state);
    }

    /**
     * Returns the text used for the off state.
     *
     * @return the text used for the off state
     */
    public String getOffText() {
        return offText;
    }

    /**
     * Sets the text for the off state.
     *
     * @param offText the text for the off state
     */
    public void setOffText(String offText) {
        Preconditions.checkNotNull(offText);
        Preconditions.checkArgument(!offText.isEmpty());

        this.offText = offText;
        setState(state);
        switchButton.repaint();
    }

    /**
     * Sets whether the switch is enabled.
     *
     * @param enabled whether the switch is enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        switchButton.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CyderSwitch{"
                + "width=" + width
                + ", height=" + height
                + ", state=" + state
                + ", switchButton=" + switchButton
                + ", buttonPercent=" + buttonPercent
                + ", animationDelay=" + animationDelay
                + ", onText=" + onText
                + ", indeterminateText=" + indeterminateText
                + ", offText=" + offText
                + ", animationIncrement=" + animationIncrement + "}";
    }
}
