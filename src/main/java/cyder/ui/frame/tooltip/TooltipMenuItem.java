package cyder.ui.frame.tooltip;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.ui.frame.CyderFrame;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * A menu item for a {@link CyderFrame} tooltip menu.
 */
public class TooltipMenuItem {
    /**
     * The list of actions to invoke upon a mouse click event.
     */
    private final ArrayList<Runnable> mouseClickActions = new ArrayList<>();

    /**
     * The list of actions to invoke upon a mouse enter event.
     */
    private final ArrayList<Runnable> mouseEnterActions = new ArrayList<>();

    /**
     * The list of actions to invoke upon a mouse exit event.
     */
    private final ArrayList<Runnable> mouseExitActions = new ArrayList<>();

    /**
     * The text to be displayed for this tooltip menu item.
     */
    private final String text;

    /**
     * Constructs a new tooltip menu item.
     *
     * @param text the text for the tooltip menu item
     */
    public TooltipMenuItem(String text) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());

        this.text = text;
    }

    /**
     * Adds the provided action to the list of actions to invoke upon a mouse click action.
     *
     * @param action the click action
     */
    public void addMouseClickAction(Runnable action) {
        Preconditions.checkNotNull(action);
        Preconditions.checkArgument(!mouseClickActions.contains(action));

        mouseClickActions.add(action);
    }

    /**
     * Adds the provided action to the list of actions to invoke upon a mouse enter action.
     *
     * @param action the enter action
     */
    public void addMouseEnterAction(Runnable action) {
        Preconditions.checkNotNull(action);
        Preconditions.checkArgument(!mouseEnterActions.contains(action));

        mouseEnterActions.add(action);
    }

    /**
     * Adds the provided action to the list of actions to invoke upon a mouse exit action.
     *
     * @param action the exit action
     */
    public void addMouseExitAction(Runnable action) {
        Preconditions.checkNotNull(action);
        Preconditions.checkArgument(!mouseExitActions.contains(action));

        mouseExitActions.add(action);
    }

    /**
     * Constructs a label using the set properties for a tooltip menu.
     *
     * @return a label using the set properties for a tooltip menu
     */
    public JLabel buildMenuItemLabel() {
        JLabel ret = new JLabel(text);
        ret.setForeground(CyderColors.vanilla);
        ret.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        ret.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseClickActions.forEach(Runnable::run);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ret.setForeground(CyderColors.regularRed);
                mouseEnterActions.forEach(Runnable::run);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ret.setForeground(CyderColors.vanilla);
                mouseExitActions.forEach(Runnable::run);
            }
        });

        return ret;
    }
}
