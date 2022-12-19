package main.java.cyder.ui.drag.button;

import main.java.cyder.constants.CyderColors;
import main.java.cyder.ui.drag.DragLabelButtonSize;

import java.awt.*;

/**
 * An interface for all drag label buttons to implement.
 */
public interface ICyderDragLabelButton {
    /**
     * Sets the size of this drag label button and repaints this button.
     *
     * @param size the new size of this drag label button
     */
    void setSize(DragLabelButtonSize size);

    /**
     * Clears the click actions list and adds the provided click action
     *
     * @param clickAction the click action to add
     */
    void setClickAction(Runnable clickAction);

    /**
     * Adds the provided click action to the click actions list.
     *
     * @param clickAction the click action to add
     */
    void addClickAction(Runnable clickAction);

    /**
     * Removes the provided click action from the click actions list.
     *
     * @param clickAction the click action to remove
     */
    void removeClickAction(Runnable clickAction);

    /**
     * Clears the click actions list.
     */
    void clearClickActions();

    /**
     * Invokes all click actions.
     */
    void invokeClickActions();

    /**
     * Clears the mouse over actions and adds the provided action to the mouse over actions list.
     *
     * @param mouseOverAction the mouse over action to set
     */
    void setMouseOverAction(Runnable mouseOverAction);

    /**
     * Adds the mouse over action to the mouse over actions list.
     *
     * @param mouseOverAction the mouse over action to add
     */
    void addMouseOverAction(Runnable mouseOverAction);

    /**
     * Removes the mouse over action from the mouse over actions list.
     *
     * @param mouseOverAction the mouse over action to remove
     */
    void removeMouseOverAction(Runnable mouseOverAction);

    /**
     * Clears the mouse over actions list.
     */
    void clearMouseOverActions();

    /**
     * Invokes all mouse over actions.
     */
    void invokeMouseOverActions();

    /**
     * Clears the mouse exit actions and adds the provided mouse exit action.
     *
     * @param mouseExitAction the mouse exit action to set
     */
    void setMouseExitAction(Runnable mouseExitAction);

    /**
     * Adds the provided mouse exit action to the mouse exit actions list.
     *
     * @param mouseExitAction the mouse exit action to add
     */
    void addMouseExitAction(Runnable mouseExitAction);

    /**
     * Removes the provided mouse exit action from the mouse exit actions list.
     *
     * @param mouseExitAction the mouse exit action to remove
     */
    void removeMouseExitAction(Runnable mouseExitAction);

    /**
     * Clears the mouse exit actions list.
     */
    void clearMouseExitActions();

    /**
     * Invokes all mouse exit actions.
     */
    void invokeMouseExitActions();

    /**
     * Clears the focus gained actions and adds the provided focus gained action.
     *
     * @param focusGainedAction the focus gained action to set
     */
    void setFocusGainedAction(Runnable focusGainedAction);

    /**
     * Adds the provided focus gained action to the focus gained actions list.
     *
     * @param focusGainedAction the focus gained action to add
     */
    void addFocusGainedAction(Runnable focusGainedAction);

    /**
     * Removes the focus gained action from the focus gained actions list.
     *
     * @param focusGainedAction the focus gained action to remove
     */
    void removeFocusGainedAction(Runnable focusGainedAction);

    /**
     * Clears the focus gained actions list.
     */
    void clearFocusGainedActions();

    /**
     * Invokes all focus gained actions.
     */
    void invokeFocusGainedActions();

    /**
     * Clears the focus lost actions list and adds the provided focus lost action.
     *
     * @param focusLostAction the focus lost action to set
     */
    void setFocusLostAction(Runnable focusLostAction);

    /**
     * Adds the provided focus lost action to the focus lost actions list.
     *
     * @param focusLostAction the focus lost action to add
     */
    void addFocusLostAction(Runnable focusLostAction);

    /**
     * Removes the focus lost action from the focus lost actions list.
     *
     * @param focusLostAction the focus lost action to remove
     */
    void removeFocusLostAction(Runnable focusLostAction);

    /**
     * Clears the focus lost actions list.
     */
    void clearFocusLostActions();

    /**
     * Invokes all focus lost actions.
     */
    void invokeFocusLostActions();

    /**
     * The default color for a non hovered or focused button.
     */
    Color defaultColor = CyderColors.vanilla;

    /**
     * The hover and focus color.
     */
    Color defaultHoverAndFocusColor = CyderColors.regularRed;

    /**
     * Returns the color to paint the button as based on the current state
     *
     * @return the color to paint the button
     */
    Color getPaintColor();

    /**
     * Adds the default mouse adapter to this button.
     */
    void addDefaultMouseAdapter();

    /**
     * Adds the default focus adapter to this button.
     */
    void addDefaultFocusAdapter();

    /**
     * Sets whether this button is focused.
     *
     * @param focused whether this button is focused
     */
    void setFocused(boolean focused);

    /**
     * Sets whether the mouse is in this button.
     *
     * @param mouseIn whether the mouse is in this button
     */
    void setMouseIn(boolean mouseIn);

    /**
     * Returns whether this button is focused.
     *
     * @return whether this button is focused
     */
    boolean getFocused();

    /**
     * Returns whether the mouse is in this button.
     *
     * @return whether the mouse is in this button
     */
    boolean getMouseIn();

    /**
     * Sets whether this drag label button is for the console.
     * This means focus will be enabled and the default focus adapter will be added if enabled.
     *
     * @param forConsole whether this drag label button is for the console.
     */
    void setForConsole(boolean forConsole);
}
