package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.utilities.AnimationUtil;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        INDETERMINITE
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
    private String indeterminiteText = "?";

    /**
     * The text to use for the on state.
     */
    private String offText = "0";

    /**
     * Constructs a new switch from the provided parameters.
     *
     * @param width the width of the switch
     * @param height the height of the switch
     * @param startingState the state to initialize the switch in
     */
    public CyderSwitch(int width, int height, State startingState) {
        this.width = width;
        this.height = height;
        state = startingState;

        setSize(width, height);
        setBorder(new LineBorder(CyderColors.navy, 5, false));

        switchButton = new CyderButton();
        switchButton.addActionListener(e -> {
            if (state == State.OFF)
                setState(State.ON);
            else
                setState(State.OFF);
        });

        switchButton.setForeground(CyderColors.regularPink);
        switchButton.setColors(CyderColors.navy);
        switchButton.setFont(CyderFonts.defaultFontSmall);
        switchButton.setSize((int) (this.width * ((double) buttonPercent / 100)) - 10, this.height - 20);
        switchButton.setLocation(10, 10);
        add(switchButton);

        setState(startingState);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(LoggerTag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * Constructs a new swtich with the inital state as off.
     *
     * @param width the switch width
     * @param height the switch height
     */
    public CyderSwitch(int width, int height) {
        this(width, height, State.OFF);
    }

    /**
     * Constructs a new switch with a width of 400, a height of 120, and a state of off.
     */
    public CyderSwitch () {
        this(400,120);
    }

    /**
     * Sets and animates the state of the switch.
     *
     * @param state the new state of the switch
     */
    public void setState(State state) {
        boolean shouldAniamte = (isVisible() && getParent() != null && this.state != state);

        this.state = state;

        switch(state) {
            case ON:
                switchButton.setText(onText);
                if (shouldAniamte)
                    AnimationUtil.componentRight(switchButton.getX(), width - switchButton.getWidth() - 10,
                            animationDelay, 8, switchButton);
                switchButton.setLocation(width - switchButton.getWidth() - 10, switchButton.getY());
                break;
            case OFF:
                switchButton.setText(offText);
                if (shouldAniamte)
                    AnimationUtil.componentLeft(switchButton.getX(), 10, animationDelay, 8, switchButton);
                switchButton.setLocation(10, switchButton.getY());
                break;
            case INDETERMINITE:
                switchButton.setText(indeterminiteText);
                if (switchButton.getX() > 10) {
                    if (shouldAniamte) {
                        switchButton.setLocation(width - switchButton.getWidth() - 10, 10);
                        AnimationUtil.componentLeft(switchButton.getX(),
                                width / 2 - switchButton.getWidth() / 2, animationDelay, 8, switchButton);
                    }
                    switchButton.setLocation(width / 2 - switchButton.getWidth() / 2,10);
                } else {
                    if (shouldAniamte) {
                        switchButton.setLocation(10,10);
                        AnimationUtil.componentRight(10, width / 2 - switchButton.getWidth() / 2,
                                animationDelay, 8, switchButton);
                    }
                    switchButton.setLocation(10,10);
                }
                break;
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
    public State getNextState() {
        switch (state) {
            case ON:
            case INDETERMINITE:
                return State.OFF;
            case OFF:
                return State.ON;
            default:
                throw new IllegalStateException("Invalid switch state: " + state);
        }
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
        switchButton.setSize((int) (width * ((double) this.buttonPercent / 100)) - 10, height - 20);

        switch (state) {
            case ON:
                switchButton.setLocation(width - switchButton.getWidth() - 10,10);
                break;
            case OFF:
                switchButton.setLocation(10,10);
                break;
            case INDETERMINITE:
                switchButton.setLocation(width / 2 - switchButton.getWidth() / 2, 10);
                break;
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
     * Returns the text used for the indeterminite state.
     *
     * @return the text used for the indeterminite state
     */
    public String getIndeterminiteText() {
        return indeterminiteText;
    }

    /**
     * Sets the text used for the indeterminite state.
     *
     * @param indeterminiteText the text used for the indeterminite state
     */
    public void setIndeterminiteText(String indeterminiteText) {
        this.indeterminiteText = indeterminiteText;
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
     * @param b whether the switch is enabled
     */
    @Override
    public void setEnabled(boolean b) {
        switchButton.setEnabled(b);
        super.setEnabled(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}
