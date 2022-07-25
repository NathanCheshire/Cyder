package cyder.layouts;

import java.awt.*;

/**
 * A class for holding a component with its assigned alignment within the {@link CyderPartitionedLayout}.
 */
public class PartitionedComponent {
    /**
     * The component.
     */
    private Component component;

    /**
     * The alignment for the component.
     */
    private CyderPartitionedLayout.PartitionAlignment alignment;

    /**
     * Constructs a new partitioned component.
     *
     * @param component the component reference
     * @param alignment the alignment for this component
     */
    public PartitionedComponent(Component component, CyderPartitionedLayout.PartitionAlignment alignment) {
        this.component = component;
        this.alignment = alignment;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Sets the component.
     *
     * @param component the component
     */
    public void setComponent(Component component) {
        this.component = component;
    }

    /**
     * Returns the partition alignment for the component.
     *
     * @return the partition alignment for the component
     */
    public CyderPartitionedLayout.PartitionAlignment getAlignment() {
        return alignment;
    }

    /**
     * Sets the partition alignment for the component.
     *
     * @param alignment the partition alignment for the component
     */
    public void setAlignment(CyderPartitionedLayout.PartitionAlignment alignment) {
        this.alignment = alignment;
    }
}
