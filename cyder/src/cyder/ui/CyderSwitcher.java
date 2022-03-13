package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.ui.objects.SwitcherState;

import javax.swing.*;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * A combo box which cycles through the possible values.
 */
public class CyderSwitcher extends JLabel {
    /**
     * The field in which the current value is displayed in.
     */
    private final CyderTextField valueDisplayField;

    /**
     * The switch button which iterates over the switch states.
     */
    private final CyderButton iterationButton;

    /**
     * The current switch state.
     */
    private SwitcherState currentState;

    /**
     * The list of valid states for this switcher.
     */
    private final ArrayList<SwitcherState> states;

    /**
     * The width of the whole switcher.
     */
    private final int width;

    /**
     * The height of the whole switcher.
     */
    private final int height;

    /**
     * Constructs a new CyderSwitcher.
     *
     * @param width the width of the whole switch
     * @param height the height of the whole switch
     * @param states the valid states
     * @param startingState the starting state
     */
    public CyderSwitcher(int width, int height, ArrayList<SwitcherState> states, SwitcherState startingState) {
        if (width < 0)
            throw new IllegalArgumentException("Width is less than 0");
        if (height < 0)
            throw new IllegalArgumentException("Height is less than 0");

        Preconditions.checkNotNull(states, "Provided states are null");
        Preconditions.checkNotNull(startingState, "Provided starting state is null");

        if (states.size() < 1)
            throw new IllegalArgumentException("Provided states is empty");

        if (!states.contains(startingState))
            throw new IllegalArgumentException("Provided states do not contain the starting state");

        this.states = states;
        currentState = startingState;
        this.width = width;
        this.height = height;

        setSize(width, height);

        int borderOffset = 5;

        valueDisplayField = new CyderTextField(0);
        valueDisplayField.setEditable(false);
        valueDisplayField.setFocusable(false);
        valueDisplayField.setSize(width - height + borderOffset, height);
        add(valueDisplayField);
        valueDisplayField.setText(currentState.getDisplayValue());
        valueDisplayField.setToolTipText(currentState.getMappedValue());

        iterationButton = new CyderButton(CyderStrings.downArrow);
        iterationButton.setSize(height, height);
        iterationButton.setLocation(width - height, 0);
        add(iterationButton);

        iterationButton.addActionListener(e -> {
            currentState = getNextState();
            valueDisplayField.setText(currentState.getDisplayValue());
            valueDisplayField.setToolTipText(currentState.getMappedValue());
        });

        valueDisplayField.setText(startingState.getDisplayValue());

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the width of this switcher.
     *
     * @return the width of this switcher
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this switcher.
     *
     * @return the height of this switcher
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the button used for iteration.
     *
     * @return the button used for iteration
     */
    public CyderButton getIterationButton() {
        return iterationButton;
    }

    /**
     * Returns the states of this switcher.
     *
     * @return the states of this switcher
     */
    public ArrayList<SwitcherState> getStates() {
        return states;
    }

    /**
     * Returns the current state of this switcher.
     *
     * @return the current state of this switcher
     */
    public SwitcherState getCurrentState() {
        return currentState;
    }

    /**
     * Returns the state just after the current state.
     *
     * @return the state just after the current state
     */
    public SwitcherState getNextState() {
        int currentIndex = 0;

        for (int i = 0 ; i < states.size() ; i++) {
            if (states.get(i) == currentState) {
                currentIndex = i;
                break;
            }
        }

        // if current state is end, next state is start
        if (currentIndex == states.size() - 1)
            return states.get(0);
        // otherwise the next state is just incremented
        else
            return states.get(currentIndex + 1);
    }

    /**
     * Sets the current state of this switcher.
     *
     * @param currentState the current state of this switcher
     */
    public void setCurrentState(SwitcherState currentState) {
        this.currentState = currentState;
        valueDisplayField.setText(currentState.getDisplayValue());
    }


    /**
     * Invokes the provided function before the state changes.
     *
     * @param function the provided function to invoke before the state changes.
     */
    public void addOnChangeListener(Function<Void, Void> function) {
        iterationButton.addActionListener((OptionalParam) -> function.apply(null));
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
        valueDisplayField.setEnabled(enabled);
        iterationButton.setEnabled(enabled);
    }
}
