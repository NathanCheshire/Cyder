package cyder.ui.selection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderStrings;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/** A combo box which cycles through the possible values. */
public class CyderComboBox extends JLabel {
    /** The field in which the current value is displayed in. */
    private final CyderTextField valueDisplayField;

    /** The switch button which iterates over the switch states. */
    private final CyderButton iterationButton;

    /** The current switch state. */
    private ComboItem currentState;

    /** The list of valid states for this switcher. */
    private final Collection<ComboItem> states;

    /** The width of the whole switcher. */
    private final int width;

    /** The height of the whole switcher. */
    private final int height;

    /** The border offset for constructing the field toggle button. */
    private static final int borderOffset = 5;

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
    public CyderComboBox(int width, int height, Collection<ComboItem> states, ComboItem startingState) {
        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        Preconditions.checkNotNull(states);
        Preconditions.checkNotNull(startingState);
        Preconditions.checkArgument(!states.isEmpty());
        Preconditions.checkArgument(states.contains(startingState));

        this.width = width;
        this.height = height;
        this.states = states;
        this.currentState = startingState;

        setSize(width, height);

        valueDisplayField = new CyderTextField();
        valueDisplayField.setEditable(false);
        valueDisplayField.setFocusable(false);
        valueDisplayField.setSize(width - height + borderOffset, height);
        add(valueDisplayField);
        valueDisplayField.setText(currentState.displayValue());
        valueDisplayField.setToolTipText(currentState.mappedValue());

        int iterationButtonLength = Math.min(width, height);
        iterationButton = new CyderButton(CyderStrings.DOWN_ARROW);
        iterationButton.setSize(iterationButtonLength, iterationButtonLength);
        iterationButton.setLocation(width - height, 0);
        add(iterationButton);

        iterationButton.addActionListener(e -> {
            currentState = getNextState();
            valueDisplayField.setText(currentState.displayValue());
            valueDisplayField.setToolTipText(currentState.mappedValue());
        });

        valueDisplayField.setText(startingState.displayValue());

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the width of this switcher.
     *
     * @return the width of this switcher
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this switcher.
     *
     * @return the height of this switcher
     */
    @Override
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
    public Collection<ComboItem> getStates() {
        return ImmutableList.copyOf(states);
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
        Preconditions.checkNotNull(states);
        Preconditions.checkState(!states.isEmpty());

        Iterator<ComboItem> iterator = states.iterator();
        ComboItem firstItem = null;

        while (iterator.hasNext()) {
            ComboItem currentIterationItem = iterator.next();
            if (firstItem == null) {
                firstItem = currentIterationItem;
            }

            if (currentIterationItem.equals(currentState)) {
                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    return firstItem;
                }
            }
        }

        throw new IllegalStateException("Could not get next state");
    }

    /**
     * Sets the current state of this switcher.
     *
     * @param currentState the current state of this switcher
     */
    public void setCurrentState(ComboItem currentState) {
        this.currentState = Preconditions.checkNotNull(currentState);
        valueDisplayField.setText(currentState.displayValue());
    }

    // todo runnable?
    /**
     * Invokes the provided function before the state changes.
     *
     * @param function the provided function to invoke before the state changes
     */
    public void addOnChangeListener(Function<Void, Void> function) {
        Preconditions.checkNotNull(function);

        iterationButton.addActionListener((OptionalParam) -> function.apply(null));
    }

    /** {@inheritDoc} */
    public void setEnabled(boolean enabled) {
        valueDisplayField.setEnabled(enabled);
        iterationButton.setEnabled(enabled);
    }

    /** An enum used to map a preview value to the actual value to switch on. */
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
            this.displayValue = Preconditions.checkNotNull(displayValue);
            this.mappedValue = Preconditions.checkNotNull(mappedValue);

            Logger.log(LogTag.OBJECT_CREATION, this);
        }
    }
}
