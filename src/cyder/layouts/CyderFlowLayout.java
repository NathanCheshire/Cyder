package cyder.layouts;

import cyder.ui.CyderPanel;
import cyder.utilities.ReflectionUtil;

import java.awt.*;
import java.util.ArrayList;

public class CyderFlowLayout extends CyderBaseLayout {
    private int hgap;
    private int vgap;
    private Alignment alignment;

    private static final int DEFAULT_HGAP = 5;
    private static final int DEFAULT_VGAP = 5;

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
            return true;
        }

        return false;
    }

    @Override
    public boolean removeComponent(Component component) {
        for (FlowComponent flowComponent : flowComponents) {
            if (flowComponent.getComponent() == component) {
                flowComponents.remove(flowComponent);
                return true;
            }
        }

        return false;
    }

    @Override
    public void revalidateComponents() {
        Component focusOwner = null;

        ArrayList<ArrayList<FlowComponent>> rows = new ArrayList<>();
        ArrayList<FlowComponent> currentRow = new ArrayList<>();
        int currentWidthAcc = 0;

        for (FlowComponent flowComponent : flowComponents) {
            if (flowComponent.getComponent().isFocusOwner() && focusOwner == null)
                focusOwner = flowComponent.getComponent();

        }

        //todo add up widths of all components and figure out how many rows we will need

        //todo then from that separate the rows and figure out the max height of the component from that row

        //todo from there you can center components on that row based off of that

        //todo do that for each row and stop when a row will be completely invisible due to frame restrictions

        if (focusOwner != null)
            focusOwner.requestFocus();
    }

    private CyderPanel cyderPanel;

    @Override
    public void setAssociatedPanel(CyderPanel panel) {
        this.cyderPanel = panel;
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
