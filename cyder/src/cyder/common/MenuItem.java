package cyder.common;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A CyderFrame menu item.
 * This class is a wrapper to associate a label with a possible
 * AtomicBoolean which dictates the state of the menu item.
 * <p>
 * Instances of this class are immutable.
 */
@Immutable
public class MenuItem {
    /**
     * The menu item label.
     */
    private final JLabel label;

    /**
     * The atomic boolean to update the state of the menu item.
     */
    private final AtomicBoolean state;

    /**
     * Constructs a new menu item.
     *
     * @param label the label for the menu item
     * @param state the possible atomic boolean to externally update the state of the menu item
     */
    public MenuItem(JLabel label, AtomicBoolean state) {
        Preconditions.checkNotNull(label);
        Preconditions.checkArgument(!label.getText().isEmpty());

        this.label = label;
        this.state = state;
    }

    /**
     * Returns the menu item label.
     *
     * @return the menu item label
     */
    public JLabel getLabel() {
        return label;
    }

    /**
     * Returns the atomic boolean for the menu label.
     *
     * @return the atomic boolean for the menu label
     */
    public AtomicBoolean getState() {
        return state;
    }
}
