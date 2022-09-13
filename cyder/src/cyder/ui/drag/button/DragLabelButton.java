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
    ArrayList<Runnable> clickActions = new ArrayList<>();

    default void setClickAction(Runnable clickAction) {
        Preconditions.checkNotNull(clickAction);
        clearClickActions();
        clickActions.add(clickAction);
    }

    default void addClickAction(Runnable clickAction) {
        Preconditions.checkNotNull(clickAction);
        clickActions.add(clickAction);
    }

    default void removeClickAction(Runnable clickAction) {
        Preconditions.checkNotNull(clickAction);
        clickActions.remove(clickAction);
    }

    default void clearClickActions() {
        clickActions.clear();
    }

    default void invokeClickActions() {
        for (Runnable clickAction : clickActions) {
            clickAction.run();
        }
    }

    ArrayList<Runnable> mouseOverActions = new ArrayList<>();

    default void setMouseOverAction(Runnable mouseOverAction) {
        Preconditions.checkNotNull(mouseOverAction);
        clearMouseOvertActions();
        mouseOverActions.add(mouseOverAction);
    }

    default void addMouseOverAction(Runnable mouseOverAction) {
        Preconditions.checkNotNull(mouseOverAction);

        mouseOverActions.add(mouseOverAction);
    }

    default void removeMouseOverAction(Runnable mouseOverAction) {
        Preconditions.checkNotNull(mouseOverAction);

        mouseOverActions.remove(mouseOverAction);
    }

    default void clearMouseOvertActions() {
        mouseOverActions.clear();
    }

    default void invokeMouseOverActions() {
        for (Runnable mouseOverAction : mouseOverActions) {
            mouseOverAction.run();
        }
    }

    ArrayList<Runnable> mouseExitActions = new ArrayList<>();

    default void setMouseExitAction(Runnable mouseExitAction) {
        Preconditions.checkNotNull(mouseExitAction);
        clearMouseExitActions();
        mouseExitActions.add(mouseExitAction);
    }

    default void addMouseExitAction(Runnable mouseExitAction) {
        Preconditions.checkNotNull(mouseExitAction);

        mouseExitActions.add(mouseExitAction);
    }

    default void removeMouseExitAction(Runnable mouseExitAction) {
        Preconditions.checkNotNull(mouseExitAction);

        mouseExitActions.remove(mouseExitAction);
    }

    default void clearMouseExitActions() {
        mouseExitActions.clear();
    }

    default void invokeMouseExitActions() {
        for (Runnable mouseExitAction : mouseExitActions) {
            mouseExitAction.run();
        }
    }

    ArrayList<Runnable> focusGainedActions = new ArrayList<>();

    default void setFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);
        clearFocusGainedActions();
        focusGainedActions.add(focusGainedAction);
    }

    default void addFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);

        focusGainedActions.add(focusGainedAction);
    }

    default void removeFocusGainedAction(Runnable focusGainedAction) {
        Preconditions.checkNotNull(focusGainedAction);

        focusGainedActions.remove(focusGainedAction);
    }

    default void clearFocusGainedActions() {
        focusGainedActions.clear();
    }

    default void invokeFocusGainedActions() {
        for (Runnable focusGainedAction : focusGainedActions) {
            focusGainedAction.run();
        }
    }

    ArrayList<Runnable> focusLostActions = new ArrayList<>();

    default void setFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);
        clearFocusLostActions();
        focusLostActions.add(focusLostAction);
    }

    default void addFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);

        focusLostActions.add(focusLostAction);
    }

    default void removeFocusLostAction(Runnable focusLostAction) {
        Preconditions.checkNotNull(focusLostAction);

        focusLostActions.remove(focusLostAction);
    }

    default void clearFocusLostActions() {
        focusLostActions.clear();
    }

    default void invokeFocusLostActions() {
        for (Runnable focusLostAction : focusLostActions) {
            focusLostAction.run();
        }
    }

    Color defaultColor = CyderColors.vanilla;
    Color defaultHoverAndFocusColor = CyderColors.regularRed;

    void paintLogic(Graphics g);

    void addDefaultMouseAdapter();

    void addDefaultFocusAdapter();

    AtomicBoolean focused = new AtomicBoolean();
    AtomicBoolean mouseIn = new AtomicBoolean();

    void setFocused(boolean focused);

    void setMouseIn(boolean mouseIn);

    DragLabelButtonSize DEFAULT_SIZE = DragLabelButtonSize.SMALL;
    DragLabelButtonSize size = DEFAULT_SIZE;
}
