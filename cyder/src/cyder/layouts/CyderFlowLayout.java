package cyder.layouts;

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
    }

    //keep track of components on this panel
    private final ArrayList<Component> flowComponents = new ArrayList<>();

    /**
     * Adds the provided component to the panel if the panel does not already contain it.
     *
     * @param component the component to add to the panel
     * @return whether or not the component was successfully added
     */
    @Override
    public boolean addComponent(Component component) {
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
            return true;
        }

        return false;
    }

    /**
     * Removes the specified component from the panel if it exists on the panel.
     *
     * @param component the component to remove from the panel
     * @return whether or not the component was successfully removed
     */
    @Override
    public boolean removeComponent(Component component) {
        for (Component flowComponent : flowComponents) {
            if (flowComponent == component) {
                flowComponents.remove(flowComponent);
                revalidateComponents();
                return true;
            }
        }

        return false;
    }

    /**
     * Revalidates the component sizes for the FlowLayout and repaints the linked panel so that the
     * component positions are updated.
     */
    @Override
    public void revalidateComponents() {
        //if we have no components or no panel then we can't revalidate
        if (flowComponents.size() < 1 ||
                associatedPanel == null ||
                associatedPanel.getWidth() == 0)
            return;

        //for focus restoration after moving components
        Component focusOwner = null;

        //lists for organizing components
        ArrayList<ArrayList<Component>> rows = new ArrayList<>();
        ArrayList<Component> currentRow = new ArrayList<>();

        int currentWidthAcc = 0;

        //maximum allocated width for our components plus their gaps
        // is the panel width minus our horizontal padding
        int maxWidth = associatedPanel.getWidth() - 2 * hpadding;

        //figure out all the rows and the most that can fit on each row
        for (Component flowComponent : flowComponents) {
            //find the first focus owner
            if (flowComponent.isFocusOwner() && focusOwner == null)
                focusOwner = flowComponent;

            //if we cannot fit the component on the current row then start a new one
            if (currentWidthAcc + flowComponent.getWidth() + hgap > maxWidth) {
                //if nothing is in the current row,
                // such as if the width is smaller than the first component's width
                if (currentRow.size() < 1) {
                    //add current row to rows list after adding
                    // component to the current row
                    currentRow.add(flowComponent);
                    rows.add(currentRow);

                    //reset row vars
                    currentRow = new ArrayList<>();
                    currentWidthAcc = 0;
                }
                //otherwise simly proceed to new row after adding
                // the current row to the rows list
                else {
                    //add current row to rows list
                    rows.add(currentRow);

                    //reset row vars for new row
                    currentRow = new ArrayList<>();
                    currentWidthAcc = flowComponent.getWidth() + hgap;
                    currentRow.add(flowComponent);
                }
            }
            //otherwise it can fit on the current row
            else {
                //increment current width
                currentWidthAcc += flowComponent.getWidth() + hgap;
                //add the component to the current row
                currentRow.add(flowComponent);
            }
        }

        //add final row to rows if it has a component since it might have
        // not been added above due to possibly running out of components
        // before meeting the maximum width constraint
        if (currentRow.size() > 0) {
            rows.add(currentRow);
        }

        //this is horizontal line that we center the current row
        // around and is based off of the row's tallest component
        int currentHeightCenteringInc = vpadding;

        //for each row
        while (!rows.isEmpty()) {
            //get the current row
            currentRow = rows.remove(0);

            //find max height component to use for centering
            // components on currentHeightCenteringInc
            int maxHeight = currentRow.get(0).getHeight();

            for (Component flowComponent : currentRow) {
                if (flowComponent.getHeight() > maxHeight)
                    maxHeight = flowComponent.getHeight();
            }

            //increment the centering height by the maxHeight for
            // now / 2 + the vert padding value
            currentHeightCenteringInc += (maxHeight / 2);

            //figure out how much width we need for this row
            // initial value is -hgap since the last component
            // in the below loop will have an unnecessary hgap after it
            int necessaryWidth = - hgap;
            int componentCount = 0;

            //sum width and component count
            for (Component flowComponent : currentRow) {
                necessaryWidth += flowComponent.getWidth() + hgap;
                componentCount++;
            }

            //switch on alignment to figure out how to place the rows
            // on the pane and what to do with possibly excess space
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

    //the panel that we will call .getWidth(), .getHeight() and add/remove components to/from
    private CyderPanel associatedPanel;

    /**
     * Sets the associated panel for this to calculate bounds based off of and place components onto.
     *
     * @param panel the panel to use for bounds and to place components onto
     */
    @Override
    public void setAssociatedPanel(CyderPanel panel) {
        this.associatedPanel = panel;
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
