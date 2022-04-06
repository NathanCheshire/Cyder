package cyder.layouts;

import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderPanel;
import cyder.utilities.ReflectionUtil;

import java.awt.*;
import java.util.ArrayList;

public class CyderFlowLayout extends CyderBaseLayout {
    //alignment determines what to do with excess space
    private static final Alignment DEFAULT_ALIGNMENT = Alignment.CENTER;
    private final Alignment alignment;

    //gaps are the spacing between the components themselves
    private static final int DEFAULT_HGAP = 5;
    private static final int DEFAULT_VGAP = 5;
    private int hgap;
    private int vgap;

    //padding is the spacing between components the frame bounds
    private static final int DEFAULT_HPADDING = 5;
    private static final int DEFAULT_VPADDING = 5;
    private int hpadding = DEFAULT_HPADDING;
    private int vpadding = DEFAULT_VPADDING;

    //alignment enum used to determine what to do with excess space
    public enum Alignment {
        LEFT, CENTER, RIGHT, CENTER_STATIC
    }

    /**
     * Defaault constructor for a CyderFlowLayout which initializes this layout with
     * alignment = CENTER, hgap = 5, and vgap = 5.
     */
    public CyderFlowLayout() {
        this(Alignment.CENTER, DEFAULT_HGAP, DEFAULT_VGAP);
    }


    /**
     * Defaault constructor for a CyderFlowLayout which initializes this layout with
     * alignment = CENTER, hgap = hgap, and vgap = vgap.
     *
     * @param hgap the horizontal gap value to use
     * @param vgap the vertical gap value to use
     */
    public CyderFlowLayout(int hgap, int vgap) {
        this(Alignment.CENTER, hgap, vgap);
    }

    /**
     * Default constructor for a CyderFlowLayout which initializes the layout with
     * alignment = alignment, hgap = 5, and vgap = 5
     *
     * @param alignment the alignment enum to use
     */
    public CyderFlowLayout(Alignment alignment) {
        this(alignment, DEFAULT_HGAP, DEFAULT_VGAP);
    }

    /**
     * Primary constructor for a CyderFlowLayout which initializes the layout
     * with the provided parameters.
     *
     * @param alignment the alignment to use to determine what to do with excess space
     * @param hgap the horizontal spacing value
     * @param vgap the vertical spacing value
     */
    public CyderFlowLayout(Alignment alignment, int hgap, int vgap) {
        this.alignment = alignment;
        this.hgap = hgap;
        this.vgap = vgap;

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    // keep track of components on this panel
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

    // todo are we centering components in the middle?

    /**
     * Revalidates the component sizes for the FlowLayout and repaints the linked panel so that the
     * component positions are updated.
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
            switch (alignment) {
                //left case means we align the components on the
                // left side with minimum spacing in between
                case LEFT:
                    //the current starting value is simply the horizontal padding value
                    int currentLeftX = hpadding;

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since currentHeightCenteringInc
                        // is guaranteed to be >= (currentFlowComp.height / 2)
                        flowComponent.setLocation(currentLeftX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        //add the component to the panel in case it was not added before
                        associatedPanel.add(flowComponent);

                        //increment x by current width plus the horizontal gap
                        currentLeftX += flowComponent.getWidth() + hgap;
                    }

                    //increment the centering line by the remaining maximum
                    // height for the current row plus the vertical gap
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;

                //center means we center the components and
                // evenly divide possibly extra space between both
                // padding values and gap values
                case CENTER:
                    //figure out how much extra padding we can give to gaps between components
                    int partitionedRemainingWidth = (maxWidth - necessaryWidth) / (componentCount + 1);

                    //the current x incrementer is the padding value with the first partitioned width
                    int currentCenterX = hpadding + partitionedRemainingWidth;

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since currentHeightCenteringInc
                        // is guaranteed to be >= (currentFlowComp.height / 2)
                        flowComponent.setLocation(currentCenterX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        //add the component to the panel in case it was not added before
                        associatedPanel.add(flowComponent);

                        //increment our centering x by the component's width, the horizontal gap,
                        // and the partitioned gap value
                        currentCenterX += flowComponent.getWidth() + hgap + partitionedRemainingWidth;
                    }

                    //increment the centering line by the remaining maximum
                    // height for the current row plus the vertical gap
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;

                //center static means simply center the components
                // with minimum spacing between them
                case CENTER_STATIC:
                    //the starting x is simply padding plus the
                    // value to center the row on the panel
                    int currentCenterStaticX = hpadding + (maxWidth - necessaryWidth) / 2;

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since currentHeightCenteringInc
                        // is guaranteed to be >= (currentFlowComp.height / 2)
                        flowComponent.setLocation(currentCenterStaticX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        //add the component to the panel in case it was not added before
                        associatedPanel.add(flowComponent);

                        //increment x by current width plus hgap
                        currentCenterStaticX += flowComponent.getWidth() + hgap;
                    }

                    //increment the centering line by the remaining maximum
                    // height for the current row plus the vertical gap
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;

                 //right means that we align the components to the
                // right with minimum spacing between components
                case RIGHT:
                    //the current component's starting x is the padding value
                    // plus the maximum width minus the total width needed for the row
                    int currentRightX = hpadding + (maxWidth - necessaryWidth);

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        //the below statement will always work since currentHeightCenteringInc
                        // is guaranteed to be >= (currentFlowComp.height / 2)
                        flowComponent.setLocation(currentRightX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        //add the component to the panel in case it was not added before
                        associatedPanel.add(flowComponent);

                        //increment currentX by the width of the component we j
                        // ust added and the necessary gap value
                        currentRightX += flowComponent.getWidth() + hgap;
                    }

                    //increment the centering line by the remaining maximum
                    // height for the current row plus the vertical gap
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;
            }
        }

        //restore focus if we found a component that was the focus owner
        if (focusOwner != null)
            focusOwner.requestFocus();
    }

    /**
     * The CyderPanel that this layout is in control of. This is where the
     * width and height that we are in control from comes and it is what we add/remove
     * components to/from.
     */
    private CyderPanel associatedPanel;

    /**
     * Sets the associated panel for this to calculate bounds based off of and place components onto.
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
