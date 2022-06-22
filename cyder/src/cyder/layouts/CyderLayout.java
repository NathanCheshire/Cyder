package cyder.layouts;

import cyder.exceptions.IllegalMethodException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * A base layout class to allow Cyder layouts to have the necessary component properties
 * while suppressing possibly confusing methods.
 */
public class CyderLayout extends JLabel implements ICyderLayout {
    /*
    Override add methods so that a user doesn't
    accidentally call them and wonder why their components aren't
    appearing on the CyderFrame.
     */

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public Component add(Component c) {
        throw new IllegalMethodException("Unsupported");
    }

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public void add(PopupMenu popup) {
        throw new IllegalMethodException("Unsupported");
    }

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public Component add(Component comp, int index) {
        throw new IllegalMethodException("Unsupported");
    }

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public Component add(String name, Component comp) {
        throw new IllegalMethodException("Unsupported");
    }

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public void add(@NotNull Component comp, Object constraints) {
        throw new IllegalMethodException("Unsupported");
    }

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public void add(Component comp, Object constraints, int index) {
        throw new IllegalMethodException("Unsupported");
    }

    /*
    Override remove methods so that a user doesn't
    accidentally call them and wonder why their components aren't
    disappearing on the CyderFrame.
     */

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public void removeAll() {
        throw new IllegalMethodException("Unsupported");
    }

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public void remove(int index) {
        throw new IllegalMethodException("Unsupported");
    }

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public void remove(Component comp) {
        throw new IllegalMethodException("Unsupported");
    }

    /**
     * Illegal method for a CyderLayout.
     */
    @Override
    @Deprecated
    public void remove(MenuComponent popup) {
        throw new IllegalMethodException("Unsupported");
    }
}
