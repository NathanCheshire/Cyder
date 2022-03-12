package cyder.layouts;

import cyder.ui.CyderPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * All CyderLayouts should extend this class which will force them
 * to have the required methods of both a CyderLayout and also a JLabel.
 */
public class CyderBaseLayout extends JLabel implements ICyderLayout {
    /*
    ICyderLayout methods to force a child class to override them
     */

    @Override
    public void addComponent(Component component) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void removeComponent(Component component) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void revalidateComponents() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void setAssociatedPanel(CyderPanel panel) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public ArrayList<Component> getLayoutComponents() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
    Override add/remove component methods so that a user doesn't
    accidentally call them and wonder why their components aren't
    appearing on the CyderFrame.
     */

    /*
    Adds
     */

    @Override
    public Component add(Component c) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void add(PopupMenu popup) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public Component add(Component comp, int index) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public Component add(String name, Component comp) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void add(@NotNull Component comp, Object constraints) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        throw new UnsupportedOperationException("Unsupported");
    }

    /*
    Removes
     */

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void remove(int index) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void remove(Component comp) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void remove(MenuComponent popup) {
        throw new UnsupportedOperationException("Unsupported");
    }
}
