package cyder.layouts;

import com.google.common.base.Preconditions;

import java.awt.*;

/** A class for holding a component with its assigned alignment within the {@link CyderPartitionedLayout}. */
public class PartitionedComponent {
    /** The component. */
    private Component component;

    /** The alignment for the component. */
    private CyderPartitionedLayout.PartitionAlignment alignment;

    /** The partition for this component. */
    private float partition;

    /**
     * Constructs a new partitioned component.
     *
     * @param component the component reference
     * @param alignment the alignment for this component
     */
    public PartitionedComponent(Component component, CyderPartitionedLayout.PartitionAlignment alignment) {
        this.component = Preconditions.checkNotNull(component);
        this.alignment = Preconditions.checkNotNull(alignment);
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

    /**
     * Sets the partition for this component.
     *
     * @return the partition for this component
     */
    public float getPartition() {
        return partition;
    }

    /**
     * Returns the partition for this component.
     *
     * @param partition the partition for this component
     */
    public void setPartition(float partition) {
        this.partition = partition;
    }

}
