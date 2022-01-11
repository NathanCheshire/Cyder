package cyder.layouts;

import cyder.ui.CyderPanel;
import cyder.utilities.ReflectionUtil;

import java.awt.*;

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

    @Override
    public boolean addComponent(Component component) {


        return false;
    }

    @Override
    public boolean removeComponent(Component component) {


        return false;
    }

    @Override
    public void revalidateComponents() {

    }

    @Override
    public void setAssociatedPanel(CyderPanel panel) {

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
