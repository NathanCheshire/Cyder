package cyder.layouts;

import cyder.handlers.internal.Logger;
import cyder.ui.CyderPanel;
import cyder.utils.ReflectionUtil;

import java.awt.*;
import java.util.ArrayList;

/**
 * A simple flow layout to quickly add components and ensure their
 * visibility on the frame provided the frame is big enough.
 */
public class CyderFlowLayout extends CyderLayout {
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
    private static final int DEFAULT_HORIZONTAL_GAP = 5;

    /**
     * The default vertical gap between components.
     */
    private static final int DEFAULT_VERTICAL_GAP = 5;

    /**
     * The horizontal gap of this layout between components.
     */
    private int horizontalGap;

    /**
     * The vertical gap of this layout between components.
     */
    private int verticalGap;

    /**
     * The default horizontal padding between the frame left and right.
     */
    private static final int DEFAULT_HORIZONTAL_PADDING = 5;

    /**
     * The default vertical padding between the frame top and bottom.
     */
    private static final int DEFAULT_VERTICAL_PADDING = 5;

    /**
     * The horizontal padding of this layout.
     */
    private int horizontalPadding = DEFAULT_HORIZONTAL_PADDING;

    /**
     * The vertical padding of this layout.
     */
    private int verticalPadding = DEFAULT_VERTICAL_PADDING;

    /**
     * Constructs a new FlowLayout with horizontal alignment CENTER,
     * vertical alignment of TOP, and component gaps of 5 pixels.
     */
    public CyderFlowLayout() {
        this(HorizontalAlignment.CENTER, VerticalAlignment.TOP, DEFAULT_HORIZONTAL_GAP, DEFAULT_VERTICAL_GAP);
    }

    /**
     * Constructs a new Flowlayout with CENTER horizontal alignment,
     * a vertical alignment of TOP, and component gaps of 5 pixels.
     *
     * @param horizontalGap the horizontal gap value to use
     * @param verticalGap   the vertical gap value to use
     */
    public CyderFlowLayout(int horizontalGap, int verticalGap)
    {
        this(HorizontalAlignment.CENTER, VerticalAlignment.TOP, horizontalGap, verticalGap);
    }

    /**
     * Constructs a new FlowLayout with the provided alignment and component gaps of 5 pixels.
     *
     * @param horizontalAlignment the horizontal alignment to use
     * @param verticalAlignment   the vertical alignment to use
     */
    public CyderFlowLayout(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
        this(horizontalAlignment, verticalAlignment, DEFAULT_HORIZONTAL_GAP, DEFAULT_VERTICAL_GAP);
    }

    /**
     * Constructs a new CyderFlowLayout with the provided alignment and gaps
     * between the components of "horizontalGap" pixels and "verticalGap" pixels.
     *
     * @param horizontalAlignment the alignment to use to determine what
     *                            to do with excess space on the horizontal axis
     * @param verticalAlignment   the alignment to use to determine what
     *                            to do with excess space on the vertical axis
     * @param horizontalGap       the horizontal spacing value
     * @param verticalGap         the vertical spacing value
     */
    public CyderFlowLayout(HorizontalAlignment horizontalAlignment,
                           VerticalAlignment verticalAlignment, int horizontalGap, int verticalGap)
    {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
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
            if (flowComponent == component)
            {
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
        int maxWidth = associatedPanel.getWidth() - 2 * horizontalPadding;

        // validate all rows and figure out if we can display some/all rows
        for (Component flowComponent : flowComponents) {
            // find the focus owner to reset after revalidation
            if (flowComponent.isFocusOwner() && focusOwner == null) {
                focusOwner = flowComponent;
            }

            // if this component cannot start on this row, then wrap it to a new row
            if (currentWidthAcc + flowComponent.getWidth() + horizontalGap > maxWidth) {
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
                    currentWidthAcc = flowComponent.getWidth() + horizontalGap;
                    currentRow.add(flowComponent);
                }
            }
            // component can fit on this row
            else {
                // increment width acc
                currentWidthAcc += flowComponent.getWidth() + horizontalGap;

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
            } else {
                break;
            }
        }

        // ensure that we always do at least 1 row, less than 1 should be impossible
        numRows = Math.max(1, numRows);

        // the horizontal line to center the current row on,
        // this is based off of  the vertical alignment
        int currentHeightCenteringInc = 0;

        int centerPartition = 0;

        // figure out starting height and increment based off of vertical alignment
        switch (verticalAlignment) {
            // this is the default, components are layered down
            // with the minimum spacing in between them (vertical padding)
            case TOP:
                currentHeightCenteringInc += verticalGap;
                break;
            // component rows are spaced evenly to take up the whole space available
            case CENTER:
                int rowHeightsOfVisibleRows = 0;
                for (int i = 0; i < numRows; i++) {
                    rowHeightsOfVisibleRows += maxRowHeights.get(i);
                }

                centerPartition = (associatedPanel.getHeight()
                        - rowHeightsOfVisibleRows) / (numRows + 1);

                currentHeightCenteringInc = centerPartition;

                break;
            // component rows are placed to border the bottom with the minimum
            // padding in between them
            case BOTTOM:
                int rowHeights = 0;

                for (int aMaxHeight : maxRowHeights) {
                    rowHeights += aMaxHeight;
                }

                currentHeightCenteringInc = associatedPanel.getHeight()
                        - verticalGap * rows.size() - rowHeights;
                break;
            // component rows are spaced with the minimum gap and placed at the center
            case CENTER_STATIC:
                int sumRowHeights = 0;

                for (int aMaxHeight : maxRowHeights) {
                    sumRowHeights += aMaxHeight + verticalGap;
                }

                // one less than num components always
                sumRowHeights -= verticalGap;

                currentHeightCenteringInc = associatedPanel.getHeight() / 2 - sumRowHeights / 2;

                break;
            default:
                throw new IllegalArgumentException("Invalid vertical alignment: " + verticalAlignment);
        }

        // for all the rows we can show
        for (int i = 0; i < numRows; i++) {
            currentRow = rows.remove(0);

            // the current max height for the row from the above computed list
            int maxHeight = maxRowHeights.remove(0);

            // the centering horizontal start line is now the
            // padding plus half the max component height
            currentHeightCenteringInc += (maxHeight / 2);

            // now figure out how much width this row really requires (we know it will fit).
            // the initial value is -horizontalGap to offset the last component having a horizontalGap after it
            int necessaryWidth = -horizontalGap;
            int componentCount = 0;
            for (Component flowComponent : currentRow) {
                necessaryWidth += flowComponent.getWidth() + horizontalGap;
                componentCount++;
            }

            // based on alignment figure out how to place the rows on the pane
            // and what to do with excess space
            switch (horizontalAlignment) {
                // left means all components on left with the minimum spacing in between
                case LEFT:
                    // the left most point we can go
                    int currentLeftX = horizontalPadding;

                    // for all components on this row, set their locations
                    // on the center line and the currentLeftX
                    for (Component flowComponent : currentRow)
                    {
                        // this is guaranteed to work since
                        // currentHeightCenteringInc >= currentFlowComp.height / 2 is always true
                        flowComponent.setLocation(currentLeftX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        // add the component to the panel (sometimes necessary, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by the width and gap needed
                        currentLeftX += flowComponent.getWidth() + horizontalGap;
                    }

                    break;

                // center means the components are centered and excess space is placed
                // evenly in the padding and gap values
                case CENTER:
                    // figure out how much excess space we have for this row
                    int partitionedRemainingWidth = (maxWidth - necessaryWidth) / (componentCount + 1);

                    // the current x incrementer based off of the minimum x and a partitioned width value
                    int currentCenterX = horizontalPadding + partitionedRemainingWidth;

                    // for all the components
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since
                        // currentHeightCenteringInc >= (currentFlowComp.height / 2) is always true
                        flowComponent.setLocation(currentCenterX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        // add the component (sometimes needed, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by the width, gap, and a partition width
                        currentCenterX += flowComponent.getWidth() + horizontalGap + partitionedRemainingWidth;
                    }

                    break;

                // center static means the components are grouped together with minimum spacing
                // and placed in the center, excess space is placed on the left and right
                case CENTER_STATIC:
                    // find the starting x
                    int centeringXAcc = horizontalPadding + (maxWidth - necessaryWidth) / 2;

                    // for all components on this row
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since
                        // currentHeightCenteringInc >= (currentFlowComp.height / 2) is always true
                        flowComponent.setLocation(centeringXAcc,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        // add the component (sometimes needed, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by width and a gap
                        centeringXAcc += flowComponent.getWidth() + horizontalGap;
                    }

                    break;

                // right means the minimum spacing between components
                // with the rightward component bordering the frame
                case RIGHT:
                    // the start of the row
                    int currentRightX = horizontalPadding + (maxWidth - necessaryWidth);

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        // the below statement will always work since >= (currentFlowComp.height / 2)
                        // is always true
                        flowComponent.setLocation(currentRightX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        // add the component (sometimes needed, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by width and a gap
                        currentRightX += flowComponent.getWidth() + horizontalGap;
                    }

                    break;
            }

            // increment the centering line by the other half of the max height
            currentHeightCenteringInc += maxHeight / 2;

            // additionally increment by the vertical gap which may or may not be
            // the one passed in depending on the horizontal alignment
            switch (verticalAlignment) {
                // component rows are spaced evenly to take up the whole space available
                case CENTER:
                    currentHeightCenteringInc += centerPartition;
                    break;
                // the default gap since we've already translated down by a proper starting amount
                case TOP:
                case BOTTOM:
                case CENTER_STATIC:
                    currentHeightCenteringInc += verticalGap;
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
     * {@inheritDoc}
     */
    public Dimension getPackSize() {
        int maxRowWidth = 0;
        int height = 2 * verticalGap;

        int currentRowWidth = 2 * horizontalGap;
        int currentRowMaxComponentHeight = 0;
        int currentRowComponentCount = 0;

        for (Component component : flowComponents) {
            int componentWidth = component.getWidth();
            int componentHeight = component.getHeight();

            // If component will cause row to overflow
            if (currentRowWidth + componentWidth + horizontalGap > associatedPanel.getWidth()) {
                if (currentRowComponentCount == 0) {
                    currentRowWidth += componentWidth;
                    maxRowWidth = Math.max(maxRowWidth, currentRowWidth);
                    height += componentHeight;

                    currentRowWidth = 2 * horizontalGap;
                    currentRowMaxComponentHeight = 0;
                } else {
                    maxRowWidth = Math.max(maxRowWidth, currentRowWidth);
                    height += currentRowMaxComponentHeight;
                    currentRowWidth = 2 * horizontalGap + componentWidth;
                    currentRowMaxComponentHeight = componentHeight;
                }

                currentRowComponentCount = 0;
            } else {
                currentRowWidth += componentWidth + horizontalGap;
                currentRowMaxComponentHeight = Math.max(currentRowMaxComponentHeight, componentHeight);
                currentRowComponentCount++;
            }
        }

        return new Dimension(maxRowWidth, height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUiToString(this);
    }

    /**
     * Returns the gap space between horizontal components.
     *
     * @return the horizontal gape to place between components
     */
    public int getHorizontalGap() {
        return horizontalGap;
    }

    /**
     * Sets the horizontal gap between components.
     *
     * @param horizontalGap the horizontal gap between components
     */
    public void setHorizontalGap(int horizontalGap) {
        this.horizontalGap = horizontalGap;
    }

    /**
     * Returns the gap space between vertical components.
     *
     * @return the vertical gap to place between components
     */
    public int getVerticalGap() {
        return verticalGap;
    }

    /**
     * Sets the vertical gap between components.
     *
     * @param verticalGap the vertical gap between components
     */
    public void setVerticalGap(int verticalGap) {
        this.verticalGap = verticalGap;
    }

    /**
     * Returns the horizontal padding value to use for the left and right of the frame.
     *
     * @return the horizontal padding value to use for the left and right of the frame
     */
    public int getHorizontalPadding() {
        return horizontalPadding;
    }

    /**
     * Sets the horizontal padding value to use for the left and right of the frame.
     *
     * @param horizontalPadding the horizontal padding value to use for the left and right of the frame
     */
    public void setHorizontalPadding(int horizontalPadding) {
        this.horizontalPadding = horizontalPadding;
    }

    /**
     * Returns the vertical padding value to use for the top and bottom of the frame.
     *
     * @return the vertical padding value to use for the top and bottom of the frame
     */
    public int getVerticalPadding() {
        return verticalPadding;
    }

    /**
     * Sets the vertical padding value to use for the top and bottom of the frame.
     *
     * @param verticalPadding the vertical padding value to use for the top and bottom of the frame
     */
    public void setVerticalPadding(int verticalPadding) {
        this.verticalPadding = verticalPadding;
    }
}
