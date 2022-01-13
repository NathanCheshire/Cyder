package cyder.layouts;

import cyder.ui.CyderPanel;
import cyder.utilities.ReflectionUtil;

import java.awt.*;
import java.util.ArrayList;

public class CyderFlowLayout extends CyderBaseLayout {

    private static final Alignment DEFAULT_ALIGNMENT = Alignment.CENTER;
    private Alignment alignment = DEFAULT_ALIGNMENT;

    private static final int DEFAULT_HGAP = 5;
    private static final int DEFAULT_VGAP = 5;
    private int hgap = DEFAULT_HGAP;
    private int vgap = DEFAULT_VGAP;

    //todo be able to change these
    private static final int DEFAULT_HPADDING = 5;
    private static final int DEFAULT_VPADDING = 5;
    private int hpadding = DEFAULT_HPADDING;
    private int vpadding = DEFAULT_VPADDING;

    public enum Alignment {
        LEFT, CENTER, RIGHT, CENTER_STATIC
    }

    public CyderFlowLayout() {
        this(Alignment.CENTER, DEFAULT_HGAP, DEFAULT_VGAP);
    }

    public CyderFlowLayout(int hgap, int vgap) {
        this(Alignment.CENTER, hgap, vgap);
    }

    public CyderFlowLayout(Alignment alignment) {
        this(alignment, DEFAULT_HGAP, DEFAULT_VGAP);
    }

    public CyderFlowLayout(Alignment alignment, int hgap, int vgap) {
        this.alignment = alignment;
        this.hgap = hgap;
        this.vgap = vgap;
    }

    private ArrayList<Component> flowComponents = new ArrayList<>();

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
                    //okay so now center the current current row component on
                    // the horizontal line y = currentHeightCenteringInc
                    // The left/right positioning is determined by this.alignment

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
                   //todo can't simply invert left since that places components
                    // mirred and not in the order that they were added in
            }
        }

        if (focusOwner != null)
            focusOwner.requestFocus();
    }

    private CyderPanel associatedPanel;

    @Override
    public void setAssociatedPanel(CyderPanel panel) {
        this.associatedPanel = panel;
        revalidateComponents();
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }


}
