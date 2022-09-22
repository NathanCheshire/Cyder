package cyder.ui.selection;

import com.google.common.base.Preconditions;
import cyder.animation.AnimationUtil;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.button.CyderButton;
import cyder.utils.ReflectionUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * An animated binary switch with smooth transition animations.
 */
public class CyderSwitch extends JLabel {
    /**
     * The possible states for the switch.
     */
    public enum State {
        /**
         * On the right.
         */
        ON,
        /**
         * On the left.
         */
        OFF,
        /**
         * In the middle.
         */
        INDETERMINATE
    }

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
    private State state;

    /**
     * The inner button used to control the switch state.
     */
    private final CyderButton switchButton;

    /**
     * The percentage of the switch that the button should take.
     */
    private int buttonPercent = 25;

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
     * Constructs a new switch from the provided parameters.
     *
     * @param width         the width of the switch
     * @param height        the height of the switch
     * @param startingState the state to initialize the switch in
     */
    public CyderSwitch(int width, int height, State startingState) {
        this.width = width;
        this.height = height;
        state = startingState;

        setSize(width, height);
        setBorder(lineBorder);

        switchButton = new CyderButton();
        switchButton.addActionListener(switchButtonActionListener);

        switchButton.setForeground(CyderColors.regularPink);
        switchButton.setColors(CyderColors.navy);
        switchButton.setFont(CyderFonts.DEFAULT_FONT_SMALL);

        Dimension size = calculateSwitchButtonSize();
        switchButton.setSize(size.width, size.height);
        switchButton.setLocation(10, 10);
        add(switchButton);

        setState(startingState);

        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Constructs a new switch with the initial state as off.
     *
     * @param width  the switch width
     * @param height the switch height
     */
    public CyderSwitch(int width, int height) {
        this(width, height, State.OFF);
    }

    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 120;

    /**
     * Constructs a new switch with a width of 400, a height of 120, and a state of off.
     */
    public CyderSwitch() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    private Dimension calculateSwitchButtonSize() {
        return new Dimension((int) (this.width * ((double) buttonPercent / 100)) - 10,
                this.height - 20);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final ActionListener switchButtonActionListener = e -> {
        if (state == State.OFF) {
            setState(State.ON);
        } else {
            setState(State.OFF);
        }
    };

    private static final int switchButtonY = 10;
    private static final int switchButtonLeftX = 10;

    /**
     * The increment for state animations.
     */
    private int animationIncrement = 8;

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
    public void setState(State state) {
        Preconditions.checkNotNull(state);

        if (this.state == state) return;

        this.state = state;

        boolean shouldAnimate = (isVisible() && getParent() != null);
        switch (state) {
            case ON -> {
                switchButton.setText(onText);
                if (shouldAnimate) {
                    AnimationUtil.componentRight(switchButton.getX(),
                            width - switchButton.getWidth() - switchButtonLeftX,
                            animationDelay, animationIncrement, switchButton);
                }
                switchButton.setLocation(width - switchButton.getWidth() - switchButtonLeftX, switchButtonY);
            }
            case OFF -> {
                switchButton.setText(offText);
                if (shouldAnimate) {
                    AnimationUtil.componentLeft(switchButton.getX(), switchButtonLeftX,
                            animationDelay, animationIncrement, switchButton);
                }
                switchButton.setLocation(switchButtonLeftX, switchButtonY);
            }
            case INDETERMINATE -> {
                switchButton.setText(indeterminateText);
                if (switchButton.getX() > switchButtonLeftX) {
                    if (shouldAnimate) {
                        switchButton.setLocation(width - switchButton.getWidth() - switchButtonLeftX, switchButtonY);
                        AnimationUtil.componentLeft(switchButton.getX(), width / 2 - switchButton.getWidth() / 2,
                                animationDelay, animationIncrement, switchButton);
                    }
                    switchButton.setLocation(width / 2 - switchButton.getWidth() / 2, switchButtonY);
                } else {
                    if (shouldAnimate) {
                        switchButton.setLocation(switchButtonLeftX, switchButtonY);
                        AnimationUtil.componentRight(switchButtonLeftX, width / 2 - switchButton.getWidth() / 2,
                                animationDelay, animationIncrement, switchButton);
                    }
                    switchButton.setLocation(switchButtonLeftX, switchButtonY);
                }
            }
        }

        repaint();
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
    public State getState() {
        return state;
    }

    /**
     * Returns the next state that the switch will be set to following a switch action.
     *
     * @return the next state that the switch will be set to following a switch action
     */
    @SuppressWarnings("UnnecessaryDefault")
    public State getNextState() {
        return switch (state) {
            case ON, INDETERMINATE -> State.OFF;
            case OFF -> State.ON;
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
        this.width = width;
    }

    /**
     * Sets the height of this switch.
     *
     * @param height the height of this switch
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Sets the button percentage.
     *
     * @return the button percentage
     */
    public int getButtonPercent() {
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
    public void setButtonPercent(int buttonPercent) {
        this.buttonPercent = buttonPercent;
        Dimension size = calculateSwitchButtonSize();
        switchButton.setSize(size.width, size.height);

        switch (state) {
            case ON -> switchButton.setLocation(width - switchButton.getWidth() - switchButtonLeftX, switchButtonY);
            case OFF -> switchButton.setLocation(switchButtonLeftX, switchButtonY);
            case INDETERMINATE -> switchButton.setLocation(width / 2 - switchButton.getWidth() / 2, switchButtonY);
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
        this.onText = onText;
        setState(state);
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
        this.offText = offText;
        setState(state);
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
        return ReflectionUtil.commonCyderUiToString(this);
    }
}
