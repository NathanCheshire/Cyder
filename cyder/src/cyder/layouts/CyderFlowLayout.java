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

        // find the max component height of each row
        ArrayList<Integer> maxRowHeights = new ArrayList<>();
        for (ArrayList<Component> row : rows) {
            int currentRowMax = 0;

            for (Component rowComponent : row) {
                currentRowMax = Math.max(currentRowMax, rowComponent.getHeight());
            }

            maxRowHeights.add(currentRowMax);
        }

        // now figure out how many rows we can show
        // and from that, we can determine how to do vertical alignment
        // by having an initial offset and a gap which mayo not be equal to the one provided.
        int numRows = 0;
        int heightAcc = 0;

        // for all row heights
        for (int maxRowHeight : maxRowHeights) {
            // if we can fit it in the view even if it's a singular pixel
            if (heightAcc + maxRowHeight < associatedPanel.getHeight()) {
                heightAcc += maxRowHeight;
                numRows++;
            } else break;
        }

        // ensure that we always do at least 1 row, less than 1 should be impossible
        numRows = Math.max(1, numRows);

        // the horizontal line to center the current row on,
        // this is based off of  the vertical alignment
        int currentHeightCenteringInc = 0;

        // figure out starting height and increment based off of vertical alignment
        switch (verticalAlignment) {
            // this is the default, components are layered down
            // with the minimum spacing in between them (vertical padding)
            case TOP:
                currentHeightCenteringInc += 5;
                break;
            // component rows are spaced evenly to take up the whole space available
            case CENTER:
                // todo
                break;
            // component rows are placed to border the bottom with the minimum
            // padding in between them
            case BOTTOM:
                int rowHeights = 0;

                for (int aMaxHeight : maxRowHeights) {
                    rowHeights += aMaxHeight;
                }

                currentHeightCenteringInc = associatedPanel.getHeight()
                        - vgap * rows.size() - rowHeights;
                break;
            // compoent rows are spaced with the minimum gap and placed at the center
            case CENTER_STATIC:
                int sumRowHeights = 0;

                for (int aMaxHeight : maxRowHeights) {
                    sumRowHeights += aMaxHeight + vgap;
                }

                // 1 less than num components always
                sumRowHeights -= vgap;

                currentHeightCenteringInc = associatedPanel.getHeight() / 2 - sumRowHeights / 2;

                break;
            default:
                throw new IllegalArgumentException("Invalid vertical alignment: " + verticalAlignment);
        }

        // for all the rows we can show
        for (int i = 0 ; i < numRows ; i++) {
            currentRow = rows.remove(0);

            // the current max height for the row from the above computed list
            int maxHeight = maxRowHeights.remove(0);

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

                    break;
            }

            // todo figure out next row start, vgap will be the main thing to change
            // increment the centering line by the other half of the max height
            currentHeightCenteringInc +=  maxHeight / 2;

            // additionally increment by the vertical gap which may or may not be
            // the one passed in depending on the horizontal alingment
            switch (verticalAlignment) {
                // the default gap
                case TOP:
                    currentHeightCenteringInc += vgap;
                    break;
                // component rows are spaced evenly to take up the whole space available
                case CENTER:
                    // todo
                    break;
                // the default gap since we've already translated down by a proper starting amount
                case BOTTOM:
                    currentHeightCenteringInc += vgap;
                    break;
                // the default gap since we've already translated down by a proper starting amount
                case CENTER_STATIC:
                    currentHeightCenteringInc += vgap;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid vertical alignment: " + verticalAlignment);
            }

            // if the next row's starting y value is not visible at
            // all (exceeds the panel's height), then we can stop rendering rows
            if (!maxRowHeights.isEmpty() &&
                    (currentHeightCenteringInc - maxRowHeights.get(0) / 2) > associatedPanel.getHeight()) {
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
