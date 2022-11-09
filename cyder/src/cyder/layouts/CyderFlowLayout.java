package cyder.layouts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.ForReadability;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.CyderPanel;
import cyder.utils.StringUtil;

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
     * The comprehensive list of components managed by this layout.
     */
    private final ArrayList<Component> components = new ArrayList<>();

    /**
     * The CyderPanel that this layout is in control of. This is where the
     * width and height that we are in control from comes and it is what we add/remove
     * components to/from.
     */
    private CyderPanel associatedPanel;

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
    public CyderFlowLayout(int horizontalGap, int verticalGap) {
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
                           VerticalAlignment verticalAlignment,
                           int horizontalGap,
                           int verticalGap) {
        this.horizontalAlignment = Preconditions.checkNotNull(horizontalAlignment);
        this.verticalAlignment = Preconditions.checkNotNull(verticalAlignment);

        Preconditions.checkArgument(horizontalGap > -1);
        Preconditions.checkArgument(verticalGap > -1);
        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the components managed by this layout.
     *
     * @return the components managed by this layout
     */
    public final ImmutableList<Component> getLayoutComponents() {
        return ImmutableList.copyOf(components);
    }

    /**
     * Adds the provided component to the panel if the panel does not already contain it.
     *
     * @param component the component to add to the panel
     */
    @Override
    public void addComponent(Component component) {
        Preconditions.checkNotNull(component);
        Preconditions.checkState(!components.contains(component));

        components.add(component);
        revalidateComponents();
    }

    /**
     * Removes the specified component from the panel if it exists on the panel.
     *
     * @param component the component to remove from the panel
     */
    @Override
    public void removeComponent(Component component) {
        for (Component flowComponent : components) {
            if (flowComponent.equals(component)) {
                components.remove(flowComponent);
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
        if (!shouldRevalidateComponents()) return;

        Component focusOwner = null;
        ArrayList<ArrayList<Component>> rows = new ArrayList<>();
        ArrayList<Component> currentRow = new ArrayList<>();

        int currentRowWidth = 0;
        int maxRowWidth = associatedPanel.getWidth() - 2 * horizontalPadding;

        // validate all rows and figure out if we can display some/all rows
        for (Component flowComponent : components) {
            // find the focus owner to reset after revalidation
            if (flowComponent.isFocusOwner() && focusOwner == null) {
                focusOwner = flowComponent;
            }

            // if this component cannot start on this row, then wrap it to a new row
            if (currentRowWidth + flowComponent.getWidth() + horizontalGap > maxRowWidth) {
                // Ensure at least one component on this row
                if (currentRow.isEmpty()) {
                    currentRow.add(flowComponent);
                    rows.add(currentRow);

                    currentRow = new ArrayList<>();
                    currentRowWidth = 0;
                } else {
                    // Something already on row so add and make new row list

                    rows.add(currentRow);

                    currentRow = new ArrayList<>();
                    currentRowWidth = flowComponent.getWidth() + horizontalGap;
                    currentRow.add(flowComponent);
                }
            } else { // Component fits on this row
                currentRowWidth += flowComponent.getWidth() + horizontalGap;
                currentRow.add(flowComponent);
            }
        }

        // If loop exited before hitting row width limit
        if (!currentRow.isEmpty()) rows.add(currentRow);

        // Find the max component height of each row
        ArrayList<Integer> maxRowHeights = new ArrayList<>();
        for (ArrayList<Component> row : rows) {
            int currentRowMax = 0;
            for (Component rowComponent : row) currentRowMax = Math.max(currentRowMax, rowComponent.getHeight());
            maxRowHeights.add(currentRowMax);
        }

        // Calculate how many rows of components we can show
        int numRows = 0;
        int currentHeight = 0;
        int panelHeight = associatedPanel.getHeight();
        for (int maxRowHeight : maxRowHeights) {
            // If we can fit part of the next row on the panel
            if (currentHeight + maxRowHeight < panelHeight) {
                currentHeight += maxRowHeight;
                numRows++;
            } else {
                break;
            }
        }
        numRows = Math.max(1, numRows);

        // The horizontal line to center the current row on
        int currentHeightCenteringY = 0;
        // The value to increment currentHeightCenteringY by if verticalAlignment is CENTER
        int currentHeightCenterIncrement = 0;

        // Figure out the above vars
        switch (verticalAlignment) {
            // Default, components are laid out from top to bottom with minimum vertical spacing
            case TOP -> currentHeightCenteringY += verticalGap;
            // Component rows are spaced evenly to take up the space available
            case CENTER -> {
                int rowHeightsOfVisibleRows = 0;
                for (int i = 0 ; i < numRows ; i++) rowHeightsOfVisibleRows += maxRowHeights.get(i);
                currentHeightCenterIncrement = (associatedPanel.getHeight() - rowHeightsOfVisibleRows) / (numRows + 1);
                currentHeightCenteringY = currentHeightCenterIncrement;
            }
            // Component rows are placed to border the bottom with minimum vertical spacing
            case BOTTOM -> {
                int rowHeights = 0;
                for (int aMaxHeight : maxRowHeights) rowHeights += aMaxHeight;
                currentHeightCenteringY = associatedPanel.getHeight() - verticalGap * rows.size() - rowHeights;
            }
            // Component rows are spaced with the minimum vertical spacing and centered in the available space
            case CENTER_STATIC -> {
                int sumRowHeights = 0;
                for (int aMaxHeight : maxRowHeights) sumRowHeights += aMaxHeight + verticalGap;
                // one less than num components always
                sumRowHeights -= verticalGap;
                currentHeightCenteringY = associatedPanel.getHeight() / 2 - sumRowHeights / 2;
            }
            default -> throw new IllegalArgumentException("Invalid vertical alignment: " + verticalAlignment);
        }

        for (int i = 0 ; i < numRows ; i++) {
            currentRow = rows.remove(0);
            int currentRowMaxHeight = maxRowHeights.remove(0);
            currentHeightCenteringY += (currentRowMaxHeight / 2);

            // Figure out how much width this row requires
            int currentRowNecessaryWidth = 0;
            for (Component flowComponent : currentRow) {
                currentRowNecessaryWidth += flowComponent.getWidth() + horizontalGap;
            }
            // Account for last addition of horizontal gap
            currentRowNecessaryWidth -= horizontalGap;

            // Figure out how to place current row components on the content pane
            switch (horizontalAlignment) {
                // left means all components on left with the minimum spacing in between
                case LEFT:
                    // the left most point we can go
                    int currentLeftX = horizontalPadding;

                    // for all components on this row, set their locations
                    // on the center line and the currentLeftX
                    for (Component flowComponent : currentRow) {
                        // this is guaranteed to work since
                        // currentHeightCenteringInc >= currentFlowComp.height / 2 is always true
                        flowComponent.setLocation(currentLeftX,
                                currentHeightCenteringY - (flowComponent.getHeight() / 2));

                        // add the component to the panel (sometimes necessary, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by the width and gap needed
                        currentLeftX += flowComponent.getWidth() + horizontalGap;

                        if (flowComponent instanceof CyderPanel panel) {
                            panel.revalidateComponents();
                        }
                    }

                    break;

                // center means the components are centered and excess space is placed
                // evenly in the padding and gap values
                case CENTER:
                    // figure out how much excess space we have for this row
                    int partitionedRemainingWidth = (maxRowWidth - currentRowNecessaryWidth) / (currentRow.size() + 1);

                    // the current x incrementer based off of the minimum x and a partitioned width value
                    int currentCenterX = horizontalPadding + partitionedRemainingWidth;

                    // for all the components
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since
                        // currentHeightCenteringInc >= (currentFlowComp.height / 2) is always true
                        flowComponent.setLocation(currentCenterX,
                                currentHeightCenteringY - (flowComponent.getHeight() / 2));

                        // add the component (sometimes needed, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by the width, gap, and a partition width
                        currentCenterX += flowComponent.getWidth() + horizontalGap + partitionedRemainingWidth;

                        if (flowComponent instanceof CyderPanel panel) {
                            panel.revalidateComponents();
                        }
                    }

                    break;

                // center static means the components are grouped together with minimum spacing
                // and placed in the center, excess space is placed on the left and right
                case CENTER_STATIC:
                    // find the starting x
                    int centeringXAcc = horizontalPadding + (maxRowWidth - currentRowNecessaryWidth) / 2;

                    // for all components on this row
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since
                        // currentHeightCenteringInc >= (currentFlowComp.height / 2) is always true
                        flowComponent.setLocation(centeringXAcc,
                                currentHeightCenteringY - (flowComponent.getHeight() / 2));

                        // add the component (sometimes needed, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by width and a gap
                        centeringXAcc += flowComponent.getWidth() + horizontalGap;

                        if (flowComponent instanceof CyderPanel panel) {
                            panel.revalidateComponents();
                        }
                    }

                    break;

                // right means the minimum spacing between components
                // with the rightward component bordering the frame
                case RIGHT:
                    // the start of the row
                    int currentRightX = horizontalPadding + (maxRowWidth - currentRowNecessaryWidth);

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        // the below statement will always work since >= (currentFlowComp.height / 2)
                        // is always true
                        flowComponent.setLocation(currentRightX,
                                currentHeightCenteringY - (flowComponent.getHeight() / 2));

                        // add the component (sometimes needed, usually not)
                        associatedPanel.add(flowComponent);

                        // increment by width and a gap
                        currentRightX += flowComponent.getWidth() + horizontalGap;

                        if (flowComponent instanceof CyderPanel panel) {
                            panel.revalidateComponents();
                        }
                    }

                    break;
            }

            // Increment the centering line by the other half of the current row's max component height
            currentHeightCenteringY += currentRowMaxHeight / 2;

            // Increment vertical gap based on vertical alignment
            switch (verticalAlignment) {
                // Component rows are spaced evenly to take up the whole space available
                case CENTER -> currentHeightCenteringY += currentHeightCenterIncrement;
                // The default gap since we've already translated down by a proper starting amount
                case TOP, BOTTOM, CENTER_STATIC -> currentHeightCenteringY += verticalGap;
                default -> throw new IllegalArgumentException("Invalid vertical alignment: " + verticalAlignment);
            }

            // If the next row's starting y value is not visible at, then we can stop rendering rows
            if (!maxRowHeights.isEmpty() && (currentHeightCenteringY - maxRowHeights.get(0) / 2)
                    > associatedPanel.getHeight()) break;
        }

        // todo methods for finding and restoring?
        // Restore focus if we found a component that was the focus owner
        if (focusOwner != null) focusOwner.requestFocus();
    }

    /**
     * Returns whether the {@link #revalidateComponents()} method should actually revalidate.
     * Used primarily is a precondition.
     *
     * @return whether the components managed by this layout should be revalidated.
     */
    @ForReadability
    private boolean shouldRevalidateComponents() {
        return components.size() > 0 && associatedPanel != null
                && associatedPanel.getWidth() > 0 && associatedPanel.getHeight() > 0;
    }

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
    @Override
    public Dimension getPackSize() {
        int maxRowWidth = 0;
        int height = 2 * verticalGap;

        int currentRowWidth = 2 * horizontalGap;
        int currentRowMaxComponentHeight = 0;
        int currentRowComponentCount = 0;

        for (Component component : components) {
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
                    currentRowComponentCount = 1;
                }
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
        return StringUtil.commonCyderUiToString(this);
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
