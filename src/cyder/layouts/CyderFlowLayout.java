package cyder.layouts;

import cyder.ui.CyderPanel;
import cyder.utilities.ReflectionUtil;

import java.awt.*;
import java.util.ArrayList;

public class CyderFlowLayout extends CyderBaseLayout {
    //alignment determines what to do with excess space
    private static final Alignment DEFAULT_ALIGNMENT = Alignment.CENTER;
    private Alignment alignment = DEFAULT_ALIGNMENT;

    //gaps are the spacing between the components themselves
    private static final int DEFAULT_HGAP = 5;
    private static final int DEFAULT_VGAP = 5;
    private int hgap = DEFAULT_HGAP;
    private int vgap = DEFAULT_VGAP;

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
    private ArrayList<Component> flowComponents = new ArrayList<>();

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
        if (flowComponents.size() < 1 || associatedPanel == null || associatedPanel.getWidth() == 0)
            return;

        //for focus restoration after moving components
        Component focusOwner = null;

        ArrayList<ArrayList<Component>> rows = new ArrayList<>();
        ArrayList<Component> currentRow = new ArrayList<>();

        int currentWidthAcc = 0;

        //5 width 5, means that it's our panel width minus twice the dfeault horiz padding
        int maxWidth = associatedPanel.getWidth() - 2 * hpadding;

        //figure out all the rows and the most that can fit on each row
        for (Component flowComponent : flowComponents) {
            //focus check
            if (flowComponent.isFocusOwner() && focusOwner == null)
                focusOwner = flowComponent;

            //if we cannot fit the component on the current row
            if (currentWidthAcc + flowComponent.getWidth() + hgap > maxWidth) {
                //if nothing in this row, add flow component to it then proceed to new row
                if (currentRow.size() < 1) {
                    //add current row to rows list after adding component
                    currentRow.add(flowComponent);
                    rows.add(currentRow);

                    //make new row
                    currentRow = new ArrayList<>();

                    //reset current width acc
                    currentWidthAcc = 0;
                }
                //otherwise simly proceed to new row
                else {
                    //add current row to rows list
                    rows.add(currentRow);

                    //make new row
                    currentRow = new ArrayList<>();

                    //reset current width acc
                    currentWidthAcc = 0;

                    //increment current width
                    currentWidthAcc += flowComponent.getWidth() + hgap;

                    //add the component to the current row
                    currentRow.add(flowComponent);
                }
            }
            //otherwise it can fit on this row so do so
            else {
                //increment current width
                currentWidthAcc += flowComponent.getWidth() + hgap;

                //add the component to the current row
                currentRow.add(flowComponent);
            }
        }

        //add final row to rows if it has components since it was not added above
        // due to it not exceeding the max length
        if (currentRow.size() > 0) {
            rows.add(currentRow);
        }

        int currentHeightCenteringInc = vpadding;

        //while more rows exist
        while (!rows.isEmpty()) {
            //get the current row of components
            currentRow = rows.remove(0);

            //find max height to use for centering
            int maxHeight = currentRow.get(0).getHeight();

            for (Component flowComponent : currentRow) {
                if (flowComponent.getHeight() > maxHeight)
                    maxHeight = flowComponent.getHeight();
            }

            //increment the centering height by the maxHeight for
            // now / 2 + the vert padding value
            currentHeightCenteringInc += (maxHeight / 2);

            switch (alignment) {
                case LEFT:
                    int currentX = hpadding;

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        //this will always work since currentHeightCenteringInc is guaranteed
                        // to be >= currentFlowComp.height / 2
                        flowComponent.setLocation(currentX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        //add to panel
                        if (associatedPanel != null) {
                            associatedPanel.add(flowComponent);
                        }

                        //increment x by current width plus hgap
                        currentX += flowComponent.getWidth() + hgap;
                    }

                    //moving on to the next row so increment the height centering
                    // var by the vertical gap and the rest of the current row's max height
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;
                case CENTER:
                    int necessaryWidth = 0;
                    int componentCount = 0;
                    for (Component flowComponent : currentRow) {
                        necessaryWidth += flowComponent.getWidth() + hgap;
                        componentCount++;
                    }

                    necessaryWidth -= hgap;

                    int addSep = (maxWidth - necessaryWidth) / (componentCount + 1);

                    currentX = hpadding + addSep;

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        //this will always work since currentHeightCenteringInc is guaranteed
                        // to be >= currentFlowComp.height / 2
                        flowComponent.setLocation(currentX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        //add to panel
                        if (associatedPanel != null) {
                            associatedPanel.add(flowComponent);
                        }

                        //increment x by current width plus hgap
                        currentX += flowComponent.getWidth() + hgap + addSep;
                    }

                    //moving on to the next row so increment the height centering
                    // var by the vertical gap and the rest of the current row's max height
                    currentHeightCenteringInc += vgap + (maxHeight / 2);

                    break;
                case CENTER_STATIC:
                    necessaryWidth = 0;
                    for (Component flowComponent : currentRow) {
                        necessaryWidth += flowComponent.getWidth() + hgap;
                    }

                    necessaryWidth -= hgap;

                    int offsetX = (maxWidth - necessaryWidth) / 2;

                    currentX = hpadding + offsetX;

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        //this will always work since currentHeightCenteringInc is guaranteed
                        // to be >= currentFlowComp.height / 2
                        flowComponent.setLocation(currentX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        //add to panel
                        if (associatedPanel != null) {
                            associatedPanel.add(flowComponent);
                        }

                        //increment x by current width plus hgap
                        currentX += flowComponent.getWidth() + hgap;
                    }

                    //moving on to the next row so increment the height centering
                    // var by the vertical gap and the rest of the current row's max height
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;
                case RIGHT:
                    necessaryWidth = 0;
                    for (Component flowComponent : currentRow) {
                        necessaryWidth += flowComponent.getWidth() + hgap;
                    }

                    necessaryWidth -= hgap;

                    offsetX = maxWidth - necessaryWidth;

                    currentX = hpadding + offsetX;

                    //set component locations based on centering line and currentX
                    for (Component flowComponent : currentRow) {
                        //this will always work since currentHeightCenteringInc is guaranteed
                        // to be >= currentFlowComp.height / 2
                        flowComponent.setLocation(currentX,
                                currentHeightCenteringInc - (flowComponent.getHeight() / 2));

                        if (associatedPanel != null) {
                            associatedPanel.add(flowComponent);
                        }

                        currentX += flowComponent.getWidth() + hgap;
                    }

                    currentHeightCenteringInc += vgap + (maxHeight / 2);
            }
        }

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
