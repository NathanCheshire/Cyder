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

        Component focusOwner = null;

        ArrayList<ArrayList<FlowComponent>> rows = new ArrayList<>();
        ArrayList<FlowComponent> currentRow = new ArrayList<>();
        int currentWidthAcc = 0;

        int maxWidth = associatedPanel.getWidth() - 2 * DEFAULT_HPADDING;

        //figure out all the rows and the most that can fit on each row
        for (FlowComponent flowComponent : flowComponents) {
            //focus check
            if (flowComponent.getComponent().isFocusOwner() && focusOwner == null)
                focusOwner = flowComponent.getComponent();

            //if we cannot fit the component on the current row
            if (currentWidthAcc + flowComponent.getOriginalWidth() + hgap > maxWidth) {
                   //finish off the row by adding it to the rows list
                   rows.add(currentRow);
                   //make new row
                   currentRow = new ArrayList<>();
                   //reset current width acc
                   currentWidthAcc = flowComponent.getOriginalWidth() + hgap + 2 * DEFAULT_HPADDING;
            } else {
                //we can fit it so just add to the original width
                currentWidthAcc += flowComponent.getOriginalWidth() + hgap;
            }

            //add the component to the current row
            currentRow.add(flowComponent);
        }

        //add final row to rows if it has components since it was not added above
        // due to it not exceeding the max length
        if (currentRow.size() > 0)
            rows.add(currentRow);

        if (rows.size() < 1)
            throw new IllegalStateException("No rows were calculated");

        int currentHeightCenteringInc = DEFAULT_VPADDING;

        //while more rows exist
        while (rows.size() > 0) {
            //get the current row of components
            currentRow = rows.remove(0);

            //find max height to use for centering
            int maxHeight = currentRow.get(0).getOriginalHeight();
            //todo index out of bounds here if frame too small?

            for (FlowComponent flowComponent : currentRow) {
                if (flowComponent.getOriginalHeight() > maxHeight)
                    maxHeight = flowComponent.getOriginalHeight();
            }

            //increment the centering height by the maxHeight for
            // now / 2 + the vert padding value
            currentHeightCenteringInc += (maxHeight / 2);

            //make sure we can add components from this row to the frame
            //if not then break out of while since we're done setting component bounds
            if (currentHeightCenteringInc >= associatedPanel.getHeight())
                break;

            //todo how to switch on the Alignment
            // since moving frame should space components out evenly too
            // until we can fit another component on with necessary spacing still

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
                if (associatedPanel != null)
                    associatedPanel.add(flowComponent.getComponent());

                //increment x by current width plus hgap
                currentX += flowComponent.getOriginalWidth() + hgap;
            }

            //moving on to the next row so increment the height centering
            // var by the vertical gap
            currentHeightCenteringInc += vgap;
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
