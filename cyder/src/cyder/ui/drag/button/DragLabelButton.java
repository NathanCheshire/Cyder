package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.ui.drag.DragLabelButtonSize;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An interface for all drag label buttons to implement.
 */
public interface DragLabelButton {
    /**
     * The actions to invoke when this button is pressed.
     */
    ArrayList<Runnable> clickActions = new ArrayList<>();

    /**
     * Clears the click actions list and adds the provided click action
     *
     * @param clickAction the click action to add
     */
    default void setClickAction(Runnable clickAction) {
        Preconditions.checkNotNull(clickAction);
        clearClickActions();
        clickActions.add(clickAction);
    }

    /**
     * Adds the provided click action to the click actions list.
     *
     * @param clickAction the click action to add
     */
    default void addClickAction(Runnable clickAction) {
        Preconditions.checkNotNull(clickAction);
        clickActions.add(clickAction);
    }

    /**
     * Removes the provided click action from the click actions list.
     *
     * @param clickAction the click action to remove
     */
    default void removeClickAction(Runnable clickAction) {
        Preconditions.checkNotNull(clickAction);
        clickActions.remove(clickAction);
    }

    /**
     * Clears the click actions list.
     */
    default void clearClickActions() {
        clickActions.clear();
    }

    /**
     * Invokes all click actions.
     */
    default void invokeClickActions() {
        for (Runnable clickAction : clickActions) {
            clickAction.run();
        }
    }

    /**
     * The actions to invoke on a mouse over event.
     */
    ArrayList<Runnable> mouseOverActions = new ArrayList<>();

    /**
     * Clears the mouse over actions and adds the provided action to the mouse over actions list.
     *
     * @param mouseOverAction the mouse over action to set
     */
    default void setMouseOverAction(Runnable mouseOverAction) {
        Preconditions.checkNotNull(mouseOverAction);
        clearMouseOverActions();
        mouseOverActions.add(mouseOverAction);
    }

    /**
     * Adds the mouse over action to the mouse over actions list.
     *
     * @param mouseOverAction the mouse over action to add
     */
    default void addMouseOverAction(Runnable mouseOverAction) {
        Preconditions.checkNotNull(mouseOverAction);

        mouseOverActions.add(mouseOverAction);
    }

    /**
     * Removes the mouse over action from the mouse over actions list.
     *
     * @param mouseOverAction the mouse over action to remove
     */
    default void removeMouseOverAction(Runnable mouseOverAction) {
        Preconditions.checkNotNull(mouseOverAction);

        mouseOverActions.remove(mouseOverAction);
    }

    /**
     * Clears the mouse over actions list.
     */
    default void clearMouseOverActions() {
        mouseOverActions.clear();
    }

    /**
     * Invokes all mouse over actions.
     */
    default void invokeMouseOverActions() {
        for (Runnable mouseOverAction : mouseOverActions) {
            mouseOverAction.run();
        }
    }

    /**
     * The actions to invoke on a mouse exit event.
     */
    ArrayList<Runnable> mouseExitActions = new ArrayList<>();

    /**
     * Clears the mouse exit actions and adds the provided mouse exit action.
     *
     * @param mouseExitAction the mouse exit action to set
     */
    default void setMouseExitAction(Runnable mouseExitAction) {
        Preconditions.checkNotNull(mouseExitAction);
        clearMouseExitActions();
        mouseExitActions.add(mouseExitAction);
    }

    /**
     * Adds the provided mouse exit action to the mouse exit actions list.
     *
     * @param mouseExitAction the mouse exit action to add
     */
    default void addMouseExitAction(Runnable mouseExitAction) {
        Preconditions.checkNotNull(mouseExitAction);

        mouseExitActions.add(mouseExitAction);
    }

    /**
     * Removes the provided mouse exit action from the mouse exit actions list.
     *
     * @param mouseExitAction the mouse exit action to remove
     */
    default void removeMouseExitAction(Runnable mouseExitAction) {
        Preconditions.checkNotNull(mouseExitAction);

        mouseExitActions.remove(mouseExitAction);
    }

    /**
     * Clears the mouse exit actions list.
     */
    default void clearMouseExitActions() {
        mouseExitActions.clear();
    }

    /**
     * Invokes all mouse exit actions.
     */
    default void invokeMouseExitActions() {
        for (Runnable mouseExitAction : mouseExitActions) {
            mouseExitAction.run();
        }
    }

    /**
     * The actions to invoke on a focus gained event.
     */
    ArrayList<Runnable> focusGainedActions = new ArrayList<>();

    /**
     * Clears the focus gained actions and adds the provided focus gained action.
     *
     * @param focusGainedAction the focus gained action to set
     */
    default void setFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);
        clearFocusGainedActions();
        focusGainedActions.add(focusGainedAction);
    }

    /**
     * Adds the provided focus gained action to the focus gained actions list.
     *
     * @param focusGainedAction the focus gained action to add
     */
    default void addFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);

        focusGainedActions.add(focusGainedAction);
    }

    /**
     * Removes the focus gained action from the focus gained actions list.
     *
     * @param focusGainedAction the focus gained action to remove
     */
    default void removeFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);

        focusGainedActions.remove(focusGainedAction);
    }

    /**
     * Clears the focus gained actions list.
     */
    default void clearFocusGainedActions() {
        focusGainedActions.clear();
    }

    /**
     * Invokes all focus gained actions.
     */
    default void invokeFocusGainedActions() {
        for (Runnable focusGainedAction : focusGainedActions) {
            focusGainedAction.run();
        }
    }

    /**
     * The actions to invoke on a focus lost event.
     */
    ArrayList<Runnable> focusLostActions = new ArrayList<>();

    /**
     * Clears the focus lost actions list and adds the provided focus lost action.
     *
     * @param focusLostAction the focus lost action to set
     */
    default void setFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);
        clearFocusLostActions();
        focusLostActions.add(focusLostAction);
    }

    /**
     * Adds the provided focus lost action to the focus lost actions list.
     *
     * @param focusLostAction the focus lost action to add
     */
    default void addFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);

        focusLostActions.add(focusLostAction);
    }

    /**
     * Removes the focus lost action from the focus lost actions list.
     *
     * @param focusLostAction the focus lost action to remove
     */
    default void removeFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);

        focusLostActions.remove(focusLostAction);
    }

    /**
     * Clears the focus lost actions list.
     */
    default void clearFocusLostActions() {
        focusLostActions.clear();
    }

    /**
     * Invokes all focus lost actions.
     */
    default void invokeFocusLostActions() {
        for (Runnable focusLostAction : focusLostActions) {
            focusLostAction.run();
        }
    }

    /**
     * The default color for a non hovered or focused button.
     */
    Color defaultColor = CyderColors.vanilla;

    /**
     * The hover and focus color.
     */
    Color defaultHoverAndFocusColor = CyderColors.regularRed;

    /**
     * The logic to invoke to paint the drag label button.
     *
     * @param g the graphics object
     */
    void paintLogic(Graphics g);

    // todo need to somehow ensure paint is overridden
    // todo add a size and set size method which repaints

    /**
     * Adds the default mouse adapter to this button.
     */
    void addDefaultMouseAdapter();

    /**
     * Adds the default focus adapter to this button.
     */
    void addDefaultFocusAdapter();

    /**
     * Whether the button is focused.
     */
    AtomicBoolean focused = new AtomicBoolean();

    /**
     * Whether the mouse is in this button.
     */
    AtomicBoolean mouseIn = new AtomicBoolean();

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
     * The default size of a drag label button.
     */
    DragLabelButtonSize DEFAULT_SIZE = DragLabelButtonSize.SMALL;
}
