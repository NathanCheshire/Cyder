package cyder.ui.drag.button;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.ui.drag.DragLabelButtonSize;
import cyder.ui.frame.CyderFrame;

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

    void setMouseOverAction(Runnable mouseOverAction);

    void addMouseOverAction(Runnable mouseOverAction);

    void removeMouseOverAction(Runnable mouseOverAction);

    void clearMouseOvertActions();

    void invokeMouseOverActions();

    ArrayList<Runnable> mouseExitActions = new ArrayList<>();

    void setMouseExitAction(Runnable mouseExitAction);

    void addMouseExitAction(Runnable mouseExitAction);

    void removeMouseExitAction(Runnable mouseExitAction);

    void clearMouseExitActions();

    void invokeMouseExitActions();

    ArrayList<Runnable> focusGainedActions = new ArrayList<>();

    void setFocusGainedAction(Runnable focusGainedAction);

    void addFocusGainedAction(Runnable focusGainedAction);

    void removeFocusGainedAction(Runnable focusGainedAction);

    void clearFocusGainedActions();

    void invokeFocusGainedActions();

    ArrayList<Runnable> focusLostActions = new ArrayList<>();

    void setFocusLostAction(Runnable focusLostAction);

    void addFocusLostAction(Runnable focusLostAction);

    void removeFocusLostAction(Runnable focusLostAction);

    void clearFocusLostActions();

    void invokeFocusLostActions();

    Color defaultColor = CyderColors.vanilla;
    Color hoverAndFocusColor = CyderColors.regularRed;

    void paintLogic(Graphics g);

    void addDefaultMouseAdapter();

    void addDefaultFocusAdapter();

    AtomicBoolean focused = new AtomicBoolean();
    AtomicBoolean mouseIn = new AtomicBoolean();

    void setFocused(boolean focused);

    void setMouseIn(boolean mouseIn);

    DragLabelButtonSize DEFAULT_SIZE = DragLabelButtonSize.SMALL;
    DragLabelButtonSize size = DEFAULT_SIZE;

    CyderFrame effectFrame = null;

    default void setEffectFrame(CyderFrame effectFrame) {
        effectFrame = Preconditions.checkNotNull(effectFrame);
        ;
    }

    default CyderFrame getEffectFrame() {
        return effectFrame;
    }
}
