package cyder.layouts;

import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderPanel;
import cyder.utilities.ReflectionUtil;

import java.awt.*;
import java.util.ArrayList;

/**
 * A simple flow layout to quickly add components and ensure their
 * visibility on the frame provided the frame is big enough.
 */
public class CyderFlowLayout extends CyderBaseLayout {
    /**
     * The default horizontal alignment.
     */
    public static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER;

    /**
     * The default vertical alignment.
     */
    public static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.TOP;

    /**
     * The horizontal alignment of this layout.
     */
    private final HorizontalAlignment horizontalAlignment;

    /**
     * The vertical alignment of this layout.
     */
    private final VerticalAlignment verticalAlignment;

    /**
     * The default horizontal gap between components.
     */
    private static final int DEFAULT_HGAP = 5;

    /**
     * The default vertical gap between components.
     */
    private static final int DEFAULT_VGAP = 5;

    /**
     * The horizontal gap of this layout between components.
     */
    private int hgap;

    /**
     * The vertical gap of this layout between components.
     */
    private int vgap;

    //padding is the spacing between components the frame bounds

    /**
     * The default horizontal padding between the frame left and right.
     */
    private static final int DEFAULT_HPADDING = 5;

    /**
     * The default vertical padding between the frame top and bottom.
     */
    private static final int DEFAULT_VPADDING = 5;

    /**
     * The horizontal padding of this layout.
     */
    private int hpadding = DEFAULT_HPADDING;

    /**
     * The vertical padding of this layout.
     */
    private int vpadding = DEFAULT_VPADDING;

    /**
     * An alignment property to determine how components are layed out
     * on a specific axis and what to do with excess space.
     */
    public enum HorizontalAlignment {
        /**
         * Components are aligned on the left with minimum spacing in between.
         */
        LEFT,
        /**
         * Components are centered and excess space is placed in between components.
         */
        CENTER,
        /**
         * Components are aligned on the right with minimum spacing in between.
         */
        RIGHT,
        /**
         * Components are centered absolutely with the excess space placed evenly
         * on on the left of the left most component and the right of the right most
         * component.
         */
        CENTER_STATIC
    }

    /**
     * An alignment property to determine how components are layed out
     * on a specific axis and what to do with excess space.
     */
    public enum VerticalAlignment {
        /**
         * Components are aligned on the top with minimum spacing in between.
         */
        TOP,
        /**
         * Components are centered and excess space is placed in between components.
         */
        CENTER,
        /**
         * Components are aligned on the bottom with minimum spacing in between.
         */
        BOTTOM,
        /**
         * Components are centered absolutely with the excess space placed evenly
         * on the absolute top of the top most component and bottom of the bottom
         * most component.
         */
        CENTER_STATIC
    }

    /**
     * Constructs a new FlowLayout with horizontal alignment CENTER,
     * vertical alignment of TOP, and component gaps of 5 pixels.
     */
    public CyderFlowLayout() {
        this(HorizontalAlignment.CENTER, VerticalAlignment.TOP, DEFAULT_HGAP, DEFAULT_VGAP);
    }

    /**
     * Construcsts a new Flowlayout with CENTER horizontal alignment,
     * a vertical alignment of TOP, and component gaps of 5 pixels.
     *
     * @param hgap the horizontal gap value to use
     * @param vgap the vertical gap value to use
     */
    public CyderFlowLayout(int hgap, int vgap) {
        this(HorizontalAlignment.CENTER, VerticalAlignment.TOP, hgap, vgap);
    }

    /**
     * Constructs a new FlowLayout with the provided alignment and component gaps of 5 pixels.
     *
     * @param horizontalAlignment the horizontal alignment to use
     * @param verticalAlignment the vertical alignment to use
     */
    public CyderFlowLayout(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
        this(horizontalAlignment, verticalAlignment, DEFAULT_HGAP, DEFAULT_VGAP);
    }

    /**
     * Constructs a new CyderFlowLayout with the provided alignment and gaps
     * between the components of "hgap" pixels and "vgap" pixels.
     *
     * @param horizontalAlignment the alignment to use to determine what
     *                           to do with excess space on the horizontal axis
     * @param verticalAlignment the alignment to use to determine what
     *                          to do with excess space on the vertical axis
     * @param hgap the horizontal spacing value
     * @param vgap the vertical spacing value
     */
    public CyderFlowLayout(HorizontalAlignment horizontalAlignment,
                           VerticalAlignment verticalAlignment, int hgap, int vgap) {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.hgap = hgap;
        this.vgap = vgap;

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * The comprehensive list of components managed by this layout.
     */
    private final ArrayList<Component> flowComponents = new ArrayList<>();

    /**
     * Returns the components managed by this layout.
     *
     * @return the components managed by this layout
     */
    public final ArrayList<Component> getLayoutComponents() {
        return flowComponents;
    }

    /**
     * Adds the provided component to the panel if the panel does not already contain it.
     *
     * @param component the component to add to the panel
     */
    @Override
    public void addComponent(Component component) {
        boolean contains = false;

        for (Component flowComponent : flowComponents) {
            if (flowComponent == component) {
                contains = true;
                break;
            }
        }

        if (!contains) {
            flowComponents.add(component);
            revalidateComponents();
        }

    }

    /**
     * Removes the specified component from the panel if it exists on the panel.
     *
     * @param component the component to remove from the panel
     */
    @Override
    public void removeComponent(Component component) {
        for (Component flowComponent : flowComponents) {
            if (flowComponent == component) {
                flowComponents.remove(flowComponent);
                revalidateComponents();
                return;
            }
        }
    }

    // todo implement a vertical alingment with default of top (same as left)
    //  to not break any existing layouts

    /**
     * Revalidates the component sizes for the FlowLayout and repaints
     * the linked panel so that the component positions are updated.
     */
    @Override
    public void revalidateComponents() {
        // if no components or no panel, cannot revalidate
        if (flowComponents.size() < 1 || associatedPanel == null
                || associatedPanel.getWidth() == 0) {
            return;
        }

        // for attempted focus restoration
        Component focusOwner = null;

        // lists for organizing components
        ArrayList<ArrayList<Component>> rows = new ArrayList<>();
        ArrayList<Component> currentRow = new ArrayList<>();

        int currentWidthAcc = 0;

        // max allowable width per row is the panel width minus the horizontal padding
        int maxWidth = associatedPanel.getWidth() - 2 * hpadding;

        // validate all rows and figure out if we can display some/all rows
        for (Component flowComponent : flowComponents) {
            // find the focus owner to reset after revalidation
            if (flowComponent.isFocusOwner() && focusOwner == null) {
                focusOwner = flowComponent;
            }

            // if this component cannot start on this row, then wrap it to a new row
            if (currentWidthAcc + flowComponent.getWidth() + hgap > maxWidth) {
                // if nothing is on the current row
                if (currentRow.size() < 1) {
                    // add the component to this row
                    currentRow.add(flowComponent);

                    // add the row with a singular element to the rows list
                    rows.add(currentRow);

                    // reset to a new row to add to
                    currentRow = new ArrayList<>();
                    currentWidthAcc = 0;
                }
                // something is on this row so we need to add and reset
                else {
                    // add current row to rows list
                    rows.add(currentRow);

                    // reset row vars for new row
                    currentRow = new ArrayList<>();
                    currentWidthAcc = flowComponent.getWidth() + hgap;
                    currentRow.add(flowComponent);
                }
            }
            // component can fit on this row
            else {
                // increment width acc
                currentWidthAcc += flowComponent.getWidth() + hgap;

                // add component to row list
                currentRow.add(flowComponent);
            }
        }

        // may have run out of components before meeting the max width constraint
        // so if the current row has components, add the row to the list of rows
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        // the horizontal line to center the current row on,
        // this will be based off of the row's tallest component
        int currentHeightCenteringInc = vpadding;

        // for all the rows
        while (!rows.isEmpty()) {
            currentRow = rows.remove(0);

            // figure out the max component height for this
            // row to center the components on
            int maxHeight = currentRow.get(0).getHeight();
            for (Component flowComponent : currentRow) {
                maxHeight = Math.max(flowComponent.getHeight(), maxHeight);
            }

            // the centering horizontal start line is now the
            // padding plus half the max component height
            currentHeightCenteringInc += (maxHeight / 2);

            // now figure out how much width this row really requires (we know it will fit).
            // the initial value is -hgap to offset the last component having an hgap after it
            int necessaryWidth = - hgap;
            int componentCount = 0;
            for (Component flowComponent : currentRow) {
                necessaryWidth += flowComponent.getWidth() + hgap;
                componentCount++;
            }

            // based on alignment figure out how to place the rows on the pane
            // and what to do with excess space
            switch (horizontalAlignment) {
                // left means all components on left with the minimum spacing in between
                case LEFT:
                    // the left most point we can go
                    int currentLeftX = hpadding;

                    // for all components on this row, set their locations
                    // on the center line and the currentLeftX
                    for (Component flowComponent : currentRow) {
                        // this is guaranteed to work since
                        // currentHeightCenteringInc >= currentFlowComp.height / 2 is always true
                        flowComponent.setLocation(currentLeftX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        // add the component to the panel (sometimes necessary, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by the width and gap needed
                        currentLeftX += flowComponent.getWidth() + hgap;
                    }

                    // increment the centering line by the other half
                    // of the max height and the vertical gap
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;

                // center means the components are centered and excess space is placed
                // evenly in the padding and gap values
                case CENTER:
                    // figure out how much excess space we have for this row
                    int partitionedRemainingWidth = (maxWidth - necessaryWidth) / (componentCount + 1);

                    // the current x incrementer based off of the minimum x and a partitioned width value
                    int currentCenterX = hpadding + partitionedRemainingWidth;

                    // for all the components
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since
                        // currentHeightCenteringInc >= (currentFlowComp.height / 2) is always true
                        flowComponent.setLocation(currentCenterX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        // add the component (sometimes needed, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by the width, gap, and a partition width
                        currentCenterX += flowComponent.getWidth() + hgap + partitionedRemainingWidth;
                    }

                    // increment the centering line by the other half
                    // of the max height and the veritcal gap
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;

                // center static means the components are grouped together with minimum spacing
                // and placed in the center, excess space is placed on the left and right
                case CENTER_STATIC:
                    // find the starting x
                    int centeringXAcc = hpadding + (maxWidth - necessaryWidth) / 2;

                    // for all components on this row
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since
                        // currentHeightCenteringInc >= (currentFlowComp.height / 2) is always true
                        flowComponent.setLocation(centeringXAcc,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        // add the component (sometimes needed, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by width and a gap
                        centeringXAcc += flowComponent.getWidth() + hgap;
                    }

                    // increment the centering line by the other half
                    // of the max height and the vertical gap
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;

                // right means the minimum spacing between components
                // with the rightward component bordering the frame
                case RIGHT:
                    // the start of the row
                    int currentRightX = hpadding + (maxWidth - necessaryWidth);

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        // the below statement will always work since >= (currentFlowComp.height / 2)
                        // is always true
                        flowComponent.setLocation(currentRightX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        // add the component (sometimes needed, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by width and a gap
                        currentRightX += flowComponent.getWidth() + hgap;
                    }

                    // increment the centering line by the other half
                    // of the max height and the vertical gap
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;
            }
        }

        // restore focus if we found a component that was the focus owner
        if (focusOwner != null) {
            focusOwner.requestFocus();
        }
    }

    /**
     * The CyderPanel that this layout is in control of. This is where the
     * width and height that we are in control from comes and it is what we add/remove
     * components to/from.
     */
    private CyderPanel associatedPanel;

    /**
     * Sets the associated panel for this to calculate
     * bounds based off of and place components onto.
     *
     * @param panel the panel to use for bounds and to place components onto
     */
    @Override
    public void setAssociatedPanel(CyderPanel panel) {
        associatedPanel = panel;
        revalidateComponents();
    }

    /**
     * Returns the default UI reflected String representation of this object.
     *
     * @return The default UI reflected String representation of this object
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    /**
     * Returns the gap space between horizontal components.
     *
     * @return the horizontal gape to place between components
     */
    public int getHgap() {
        return hgap;
    }

    /**
     * Sets the horizontal gap between components.
     *
     * @param hgap the horizontal gap between components
     */
    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    /**
     * Returns the gap space between vertical components.
     *
     * @return the verical gap to place between components
     */
    public int getVgap() {
        return vgap;
    }

    /**
     * Sets the verical gap between components.
     *
     * @param vgap the vertical gap between components
     */
    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    /**
     * Returns the horizontal padding value to use for the left and right of the frame.
     *
     * @return the horizontal padding value to use for the left and right of the frame
     */
    public int getHpadding() {
        return hpadding;
    }

    /**
     * Sets the horizontal padding value to use for the left and right of the frame.
     *
     * @param hpadding the horizontal padding value to use for the left and right of the frame
     */
    public void setHpadding(int hpadding) {
        this.hpadding = hpadding;
    }

    /**
     * Returns the vertical padding value to use for the top and bottom of the frame.
     *
     * @return the vertical padding value to use for the top and bottom of the frame
     */
    public int getVpadding() {
        return vpadding;
    }


    /**
     * Sets the vertical padding value to use for the top and bottom of the frame.
     *
     * @param vpadding the vertical padding value to use for the top and bottom of the frame
     */
    public void setVpadding(int vpadding) {
        this.vpadding = vpadding;
    }
}
