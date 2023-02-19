package cyder.ui.frame.tooltip;

import com.google.common.base.Preconditions;
import cyder.managers.ProgramModeManager;
import cyder.ui.UiUtil;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.enumerations.FrameType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * A controller for the tooltip menu for a particular {@link CyderFrame}.
 */
public class TooltipMenuController {
    /**
     * The tooltip menu component border color.
     */
    private static final Color borderColor = Color.black;

    /**
     * The default tooltip menu width.
     */
    private static final int defaultWidth = 120;

    /**
     * The tooltip menu border length
     */
    private static final int borderLength = 5;

    /**
     * The height necessary for a single tooltip menu item.
     */
    private static final int itemLabelHeight = 30;

    /**
     * The frame this controller has control over.
     */
    private final CyderFrame controlFrame;

    /**
     * The generated menu label this controller uses for the tooltip menu label.
     */
    private JLabel tooltipMenuLabel;

    /**
     * The menu items for this tooltip menu controller.
     */
    private final ArrayList<JLabel> menuItems = new ArrayList<>();

    /**
     * Constructs a new tooltip menu controller.
     *
     * @param controlFrame the frame this controller will control
     */
    public TooltipMenuController(CyderFrame controlFrame) {
        Preconditions.checkNotNull(controlFrame);
        Preconditions.checkArgument(controlFrame.getFrameType() == FrameType.DEFAULT);
        Preconditions.checkArgument(!controlFrame.isBorderlessFrame());

        this.controlFrame = controlFrame;
        initializeMenuItems();
        constructTooltipMenuLabel();
    }

    /**
     * Returns the frame this controller controls.
     *
     * @return the frame this controller controls
     */
    public CyderFrame getControlFrame() {
        return controlFrame;
    }

    /**
     * Shows the tooltip menu label on the control frame relative to the provided trigger point.
     *
     * @param triggerPoint the point at which the user clicked to trigger showing the tooltip menu label
     */
    public void show(Point triggerPoint) {
        Preconditions.checkNotNull(triggerPoint);
        double x = triggerPoint.getX();
        double y = triggerPoint.getY();
        Preconditions.checkArgument(x >= 0 && x <= controlFrame.getWidth());
        Preconditions.checkArgument(y >= 0 && y <= controlFrame.getHeight());

        // todo revoke old label if present
    }

    /**
     * Animates out the tooltip menu label.
     */
    public void animateOut() {
        // todo need a way to escape this immediately with no side effects
    }

    /**
     * Initializes the menu items the tooltip menu label will show.
     */
    private void initializeMenuItems() {
        menuItems.add(new TooltipMenuItem("To back")
                .addMouseClickAction(this::animateOut)
                //.addMouseClickAction(this::toBack)
                .buildMenuItemLabel());
        menuItems.add(new TooltipMenuItem("Frame location")
                .addMouseClickAction(this::animateOut)
                //  .addMouseClickAction(this::onFrameLocationTooltipMenuItemPressed)
                .buildMenuItemLabel());

        if (controlFrame.isResizingAllowed()) {
            menuItems.add(new TooltipMenuItem("Frame size")
                    .addMouseClickAction(this::animateOut)
                    //  .addMouseClickAction(this::onFrameSizeTooltipMenuItemPressed)
                    .buildMenuItemLabel());
        }
        if (ProgramModeManager.INSTANCE.getProgramMode().hasDeveloperPriorityLevel()) {
            menuItems.add(new TooltipMenuItem("Screenshot")
                    .addMouseClickAction(this::animateOut)
                    .addMouseClickAction(() -> {
                        tooltipMenuLabel.setVisible(false);
                        UiUtil.screenshotCyderFrame(controlFrame);
                        controlFrame.notify("Saved screenshot to your user's Files directory");
                    })
                    .buildMenuItemLabel());
        }
    }

    /**
     * Constructs the tooltip menu label for this controller.
     */
    private void constructTooltipMenuLabel() {
        synchronized (controlFrame) {
            tooltipMenuLabel = new JLabel();
        }
    }

    /**
     * Returns the width to use for the tooltip menu label.
     *
     * @return the width to use for the tooltip menu label
     */
    private int calculateWidth() {
        int necessaryWidth = 2 * borderLength + defaultWidth;
        int maxWidthOnParent = controlFrame.getWidth()
                - controlFrame.getLeftDragLabel().getWidth()
                - controlFrame.getRightDragLabel().getWidth();
        return Math.min(necessaryWidth, maxWidthOnParent);
    }

    /**
     * Returns the height to use for the tooltip menu label.
     *
     * @return the height to use for the tooltip menu label
     */
    private int calculateHeight() {
        int necessaryHeight = 2 * borderLength + itemLabelHeight * menuItems.size();
        int maxHeightOnParent = controlFrame.getHeight()
                - controlFrame.getTopDragLabel().getHeight()
                - controlFrame.getBottomDragLabel().getHeight();
        return Math.min(necessaryHeight, maxHeightOnParent);
    }
}
