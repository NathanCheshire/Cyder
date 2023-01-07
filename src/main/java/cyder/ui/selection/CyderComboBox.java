package cyder.ui.selection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;

/**
 * A combo box which cycles through the possible values.
 */
public class CyderComboBox extends JLabel {
    /**
     * The border offset for constructing the field toggle button.
     */
    private static final int borderOffset = 5;

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
    private CyderComboBoxState currentState;

    /**
     * The list of valid states for this switcher.
     */
    private final Collection<CyderComboBoxState> states;

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
    public CyderComboBox(int width, int height, Collection<CyderComboBoxState> states,
                         CyderComboBoxState startingState) {
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
        valueDisplayField.setText(currentState.getDisplayValue());
        valueDisplayField.setToolTipText(currentState.getMappedValue());

        int iterationButtonLength = Math.min(width, height);
        iterationButton = new CyderButton(CyderStrings.DOWN_ARROW);
        iterationButton.setSize(iterationButtonLength, iterationButtonLength);
        iterationButton.setLocation(width - height, 0);
        add(iterationButton);

        iterationButton.addActionListener(e -> {
            currentState = getNextState();
            valueDisplayField.setText(currentState.getDisplayValue());
            valueDisplayField.setToolTipText(currentState.getMappedValue());
        });

        valueDisplayField.setText(startingState.getDisplayValue());

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
    public Collection<CyderComboBoxState> getStates() {
        return ImmutableList.copyOf(states);
    }

    /**
     * Returns the current state of this switcher.
     *
     * @return the current state of this switcher
     */
    public CyderComboBoxState getCurrentState() {
        return currentState;
    }

    /**
     * Returns the state just after the current state.
     *
     * @return the state just after the current state
     */
    public CyderComboBoxState getNextState() {
        Preconditions.checkNotNull(states);
        Preconditions.checkState(!states.isEmpty());

        Iterator<CyderComboBoxState> iterator = states.iterator();
        CyderComboBoxState firstItem = null;

        while (iterator.hasNext()) {
            CyderComboBoxState currentIterationItem = iterator.next();
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
    public void setCurrentState(CyderComboBoxState currentState) {
        this.currentState = Preconditions.checkNotNull(currentState);
        valueDisplayField.setText(currentState.getDisplayValue());
    }

    /**
     * Invokes the provided runnable before the state changes.
     *
     * @param runnable the provided runnable to invoke before the state changes
     */
    public void addOnChangeRunnable(Runnable runnable) {
        Preconditions.checkNotNull(runnable);

        iterationButton.addActionListener(e -> runnable.run());
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
        valueDisplayField.setEnabled(enabled);
        iterationButton.setEnabled(enabled);
    }

}
