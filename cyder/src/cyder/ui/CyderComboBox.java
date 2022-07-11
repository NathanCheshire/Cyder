package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.Logger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * A combo box which cycles through the possible values.
 */
public class CyderComboBox extends JLabel {
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
    private ComboItem currentState;

    /**
     * The list of valid states for this switcher.
     */
    private final ArrayList<ComboItem> states;

    /**
     * The width of the whole switcher.
     */
    private final int width;

    /**
     * The height of the whole switcher.
     */
    private final int height;

    /**
     * Constructs a new CyderComboBox. Note, you will need to add an action listener
     * to the internal button if you wish to invoke actions whenever the
     * switch button is clicked. Use the getNextState() and getCurrentState() as needed.
     *
     * @param width         the width of the whole switch
     * @param height        the height of the whole switch
     * @param states        the valid states
     * @param startingState the starting state
     */
    @SuppressWarnings("SuspiciousNameCombination") // incorrect
    public CyderComboBox(int width, int height, ArrayList<ComboItem> states, ComboItem startingState) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(states, "Provided states are null");
        Preconditions.checkNotNull(startingState, "Provided starting state is null");
        Preconditions.checkArgument(!states.isEmpty(), "No states provided");
        Preconditions.checkArgument(states.contains(startingState),
                "Provided states do not contain the starting state");

        this.width = width;
        this.height = height;

        this.states = states;
        currentState = startingState;

        setSize(width, height);

        int borderOffset = 5;

        valueDisplayField = new CyderTextField(0);
        valueDisplayField.setEditable(false);
        valueDisplayField.setFocusable(false);
        valueDisplayField.setSize(width - height + borderOffset, height);
        add(valueDisplayField);
        valueDisplayField.setText(currentState.displayValue());
        valueDisplayField.setToolTipText(currentState.mappedValue());

        iterationButton = new CyderButton(CyderStrings.DOWN_ARROW);
        iterationButton.setSize(height, height);
        iterationButton.setLocation(width - height, 0);
        add(iterationButton);

        iterationButton.addActionListener(e -> {
            currentState = getNextState();
            valueDisplayField.setText(currentState.displayValue());
            valueDisplayField.setToolTipText(currentState.mappedValue());
        });

        valueDisplayField.setText(startingState.displayValue());

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
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
    public ArrayList<ComboItem> getStates() {
        return states;
    }

    /**
     * Returns the current state of this switcher.
     *
     * @return the current state of this switcher
     */
    public ComboItem getCurrentState() {
        return currentState;
    }

    /**
     * Returns the state just after the current state.
     *
     * @return the state just after the current state
     */
    public ComboItem getNextState() {
        int currentIndex = 0;

        for (int i = 0 ; i < states.size() ; i++) {
            if (states.get(i) == currentState) {
                currentIndex = i;
                break;
            }
        }

        // if current state is end, next state is start
        if (currentIndex == states.size() - 1) {
            return states.get(0);
        }
        // otherwise the next state is just incremented
        else {
            return states.get(currentIndex + 1);
        }
    }

    /**
     * Sets the current state of this switcher.
     *
     * @param currentState the current state of this switcher
     */
    public void setCurrentState(ComboItem currentState) {
        this.currentState = currentState;
        valueDisplayField.setText(currentState.displayValue());
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

    /**
     * An enum used to map a preview value to the actual value to switch on.
     */
    public record ComboItem(String displayValue, String mappedValue) {
        /**
         * Constructs a new switch state
         *
         * @param value the display value and underlying map value of the state
         */
        public ComboItem(String value) {
            this(value, value);
        }

        /**
         * Constructs a new switch state
         *
         * @param displayValue the display value of the state
         * @param mappedValue  the underlying value of the state
         */
        public ComboItem(String displayValue, String mappedValue) {
            Preconditions.checkNotNull(displayValue);
            Preconditions.checkNotNull(mappedValue);

            this.displayValue = displayValue;
            this.mappedValue = mappedValue;

            Logger.log(Logger.Tag.OBJECT_CREATION, this);
        }
    }
}
