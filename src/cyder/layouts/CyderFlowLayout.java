package cyder.layouts;

import cyder.ui.CyderPanel;
import cyder.utilities.ReflectionUtil;

import java.awt.*;
import java.util.ArrayList;

public class CyderFlowLayout extends CyderBaseLayout {
    private int hgap = DEFAULT_HGAP;
    private int vgap = DEFAULT_VGAP;
    private Alignment alignment = Alignment.CENTER;

    private static final int DEFAULT_HGAP = 5;
    private static final int DEFAULT_VGAP = 5;

    //todo be able to change these
    private static final int DEFAULT_HPADDING = 5;
    private static final int DEFAULT_VPADDING = 5;

    public enum Alignment {
        LEFT, CENTER, RIGHT
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

    private ArrayList<FlowComponent> flowComponents = new ArrayList<>();

    @Override
    public boolean addComponent(Component component) {
        boolean contains = false;

        for (FlowComponent flowComponent : flowComponents) {
            if (flowComponent.getComponent() == component) {
                contains = true;
                break;
            }
        }

        if (!contains) {
            flowComponents.add(new FlowComponent(component, component.getWidth(), component.getHeight()));
            revalidateComponents();
            return true;
        }

        return false;
    }

    @Override
    public boolean removeComponent(Component component) {
        for (FlowComponent flowComponent : flowComponents) {
            if (flowComponent.getComponent() == component) {
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

        ArrayList<ArrayList<FlowComponent>> rows = new ArrayList<>();
        ArrayList<FlowComponent> currentRow = new ArrayList<>();

        int currentWidthAcc = 0;

        //5 width 5, means that it's our panel width minus twice the dfeault horiz padding
        int maxWidth = associatedPanel.getWidth() - 2 * DEFAULT_HPADDING;

        //figure out all the rows and the most that can fit on each row
        for (FlowComponent flowComponent : flowComponents) {
            //focus check
            if (flowComponent.getComponent().isFocusOwner() && focusOwner == null)
                focusOwner = flowComponent.getComponent();

            //if we cannot fit the component on the current row
            if (currentWidthAcc + flowComponent.getOriginalWidth() + hgap > maxWidth) {
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
                    currentWidthAcc += flowComponent.getOriginalWidth() + hgap;

                    //add the component to the current row
                    currentRow.add(flowComponent);
                }
            }
            //otherwise it can fit on this row so do so
            else {
                //increment current width
                currentWidthAcc += flowComponent.getOriginalWidth() + hgap;

                //add the component to the current row
                currentRow.add(flowComponent);
            }
        }

        //add final row to rows if it has components since it was not added above
        // due to it not exceeding the max length
        if (currentRow.size() > 0)
            rows.add(currentRow);

        int currentHeightCenteringInc = DEFAULT_VPADDING;

        //while more rows exist
        while (!rows.isEmpty()) {
            //get the current row of components
            currentRow = rows.remove(0);

            //find max height to use for centering
            int maxHeight = currentRow.get(0).getOriginalHeight();

            for (FlowComponent flowComponent : currentRow) {
                if (flowComponent.getOriginalHeight() > maxHeight)
                    maxHeight = flowComponent.getOriginalHeight();
            }

            //increment the centering height by the maxHeight for
            // now / 2 + the vert padding value
            currentHeightCenteringInc += (maxHeight / 2);

            //make sure we can add components from this row to the frame
            //if not then set rest of components to invisible
            if (currentHeightCenteringInc >= associatedPanel.getHeight()) {
                for (FlowComponent flowComponent : currentRow) {
                    flowComponent.getComponent().setVisible(false);
                }

                //now continue with the rest of the rows
                continue;
            }

            switch (alignment) {
                case LEFT:
                    //okay so now center the current current row component on
                    // the horizontal line y = currentHeightCenteringInc
                    // The left/right positioning is determined by this.alignment

                    int currentX = DEFAULT_HPADDING;

                    //set component locations based on centering line and currentX
                    for (FlowComponent flowComponent : currentRow) {
                        //this will always work since currentHeightCenteringInc is guaranteed
                        // to be >= currentFlowComp.height / 2
                        flowComponent.getComponent().setLocation(currentX,
                                currentHeightCenteringInc - (flowComponent.getOriginalHeight() / 2));

                        //add to panel
                        if (associatedPanel != null) {
                            associatedPanel.add(flowComponent.getComponent());

                            //set visible since it may have been set to invisible
                            flowComponent.getComponent().setVisible(true);
                        }

                        //increment x by current width plus hgap
                        currentX += flowComponent.getOriginalWidth() + hgap;
                    }

                    //moving on to the next row so increment the height centering
                    // var by the vertical gap and the rest of the current row's max height
                    currentHeightCenteringInc += vgap + (maxHeight / 2);
                    break;
                case CENTER:
                    //todo evenly space items on row based off of total width
                    // (we know we at least have the necessary hgap available
                    // but we may have more space to spread out)
                    break;
                case RIGHT:
                    //todo align items to the right with min spacings
                    break;
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

    //class so we know the original size of components for resize events
    private static class FlowComponent {
        private Component component;
        private int originalWidth;
        private int originalHeight;

        public FlowComponent(Component component, int originalWidth, int originalHeight) {
            this.component = component;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
        }

        public Component getComponent() {
            return component;
        }

        public void setComponent(Component component) {
            this.component = component;
        }

        public int getOriginalWidth() {
            return originalWidth;
        }

        public void setOriginalWidth(int originalWidth) {
            this.originalWidth = originalWidth;
        }

        public int getOriginalHeight() {
            return originalHeight;
        }

        public void setOriginalHeight(int originalHeight) {
            this.originalHeight = originalHeight;
        }
    }
}
