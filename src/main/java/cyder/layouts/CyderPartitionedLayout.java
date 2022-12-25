package cyder.layouts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import cyder.annotations.ForReadability;
import cyder.ui.CyderPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * A layout designed to allow partitioning of a row or column into different parts each taking up
 * a certain percent of the whole partitioned area. One might want 30%, 20%, and 50% for a row
 * and this will allow the user to do so.
 */
public class CyderPartitionedLayout extends CyderLayout {
    /**
     * The range all partitions must fall within.
     */
    public static final Range<Float> PARTITION_RANGE = Range.closed(0.0f, 100.0f);

    /**
     * The maximum partition value which all partitions and
     * the sum of all partitions must be less than or equal to.
     */
    public static final float MAX_PARTITION = PARTITION_RANGE.upperEndpoint();

    /**
     * The amount of partition space to assign a component added without a provided partition space.
     */
    public static final float DEFAULT_PARTITION_SPACE_PERCENT = 10;

    /**
     * The partition space to partition to a new component being added without a specified partition space.
     */
    private float newComponentPartitionSpace = DEFAULT_PARTITION_SPACE_PERCENT;

    /**
     * The possible directions to lay components out.
     */
    public enum PartitionDirection {
        /**
         * The components are laid out in a row.
         */

        ROW,
        /**
         * The components are laid out in a column.
         */
        COLUMN
    }

    /**
     * The current direction to lay the partitioned components out.
     */
    private PartitionDirection partitionDirection = PartitionDirection.COLUMN;

    /**
     * The alignment of a component within a partition space provided it is smaller than the partitioned area.
     */
    public enum PartitionAlignment {
        TOP_LEFT, TOP, TOP_RIGHT,
        LEFT, CENTER, RIGHT,
        BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
    }

    /**
     * The default component alignment for new components.
     */
    private PartitionAlignment newComponentPartitionAlignment = PartitionAlignment.CENTER;

    /**
     * The direct components managed by this layout.
     */
    private final ArrayList<PartitionedComponent> components;

    /**
     * The CyderPanel this layout manager will manage.
     */
    private CyderPanel associatedPanel;

    /**
     * Constructs a new partitioned layout.
     */
    public CyderPartitionedLayout() {
        components = new ArrayList<>();
    }

    /**
     * Returns the {@link PartitionDirection}, that of {@link PartitionDirection#COLUMN}
     * or {@link PartitionDirection#ROW}
     *
     * @return the partition direction
     */
    public PartitionDirection getPartitionDirection() {
        return partitionDirection;
    }

    /**
     * Sets the {@link PartitionDirection}, that of {@link PartitionDirection#COLUMN}
     * or {@link PartitionDirection#ROW}
     *
     * @param partitionDirection the new partition direction
     */
    public void setPartitionDirection(PartitionDirection partitionDirection) {
        if (this.partitionDirection == partitionDirection) {
            return;
        }

        this.partitionDirection = partitionDirection;

        revalidateComponents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImmutableList<Component> getLayoutComponents() {
        ArrayList<Component> ret = new ArrayList<>(components.size());
        components.forEach(component -> ret.add(component.getComponent()));
        return ImmutableList.copyOf(ret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPackSize() {
        /*
        Doesn't exactly make sense since the size depends on the provided viewport
        meaning the returned size would always be equal to the viewport's size.
         */
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Sets the CyderPanel to manage. Components this LM has been given thus far
     * will be evaluated and added to the Panel.
     *
     * @param associatedPanel the CyderPanel to manage
     */
    @Override
    public void setAssociatedPanel(CyderPanel associatedPanel) {
        this.associatedPanel = Preconditions.checkNotNull(associatedPanel);

        revalidateComponents();
    }

    /**
     * Returns the partition space given to a new component if none is specified.
     *
     * @return the partition space given to a new component if none is specified
     */
    public float getNewComponentPartitionSpace() {
        return newComponentPartitionSpace;
    }

    /**
     * Sets the partition space given to a new component if none is specified.
     *
     * @param newComponentPartitionSpace the partition space given to a new component if none is specified
     */
    public void setNewComponentPartitionSpace(float newComponentPartitionSpace) {
        this.newComponentPartitionSpace = newComponentPartitionSpace;
    }

    /**
     * Returns the partition alignment for new components.
     *
     * @return the partition alignment for new components
     */
    public PartitionAlignment getNewComponentPartitionAlignment() {
        return newComponentPartitionAlignment;
    }

    /**
     * Sets the partition alignment for new components.
     *
     * @param newComponentPartitionAlignment the partition alignment for new components
     */
    public void setNewComponentPartitionAlignment(PartitionAlignment newComponentPartitionAlignment) {
        this.newComponentPartitionAlignment = newComponentPartitionAlignment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revalidateComponents() {
        if (associatedPanel == null || associatedPanel.getWidth() == 0
                || associatedPanel.getHeight() == 0) return;

        Component focusOwner = null;

        int currentComponentStart = 0;

        int parentWidth = associatedPanel.getWidth();
        int parentHeight = associatedPanel.getHeight();

        for (PartitionedComponent partitionedComponent : components) {
            Component component = partitionedComponent.getComponent();
            PartitionAlignment alignment = partitionedComponent.getAlignment();
            float partition = partitionedComponent.getPartition();

            int componentPartitionedLength;
            switch (partitionDirection) {
                case ROW -> {
                    if (partition == Float.MAX_VALUE) {
                        componentPartitionedLength = component.getWidth();
                    } else {
                        componentPartitionedLength = (int) ((partition / MAX_PARTITION) * parentWidth);
                    }
                }
                case COLUMN -> {
                    if (partition == Float.MAX_VALUE) {
                        componentPartitionedLength = component.getHeight();
                    } else {
                        componentPartitionedLength = (int) ((partition / MAX_PARTITION) * parentHeight);
                    }
                }
                default -> throw new IllegalStateException("Invalid partition direction: " + partitionDirection);
            }

            // indicates a spacer in the layout
            if (component == null) {
                currentComponentStart += componentPartitionedLength;
                continue;
            }

            if (component.isFocusOwner() && focusOwner == null) {
                focusOwner = component;
            }

            switch (partitionDirection) {
                case ROW -> {
                    switch (alignment) {
                        case TOP_LEFT -> component.setLocation(currentComponentStart, 0);
                        case TOP -> component.setLocation(currentComponentStart
                                + componentPartitionedLength / 2 - component.getWidth() / 2, 0);
                        case TOP_RIGHT -> component.setLocation(currentComponentStart
                                + componentPartitionedLength - component.getWidth(), 0);
                        case LEFT -> component.setLocation(currentComponentStart,
                                parentHeight / 2 - component.getWidth() / 2);
                        case CENTER -> component.setLocation(currentComponentStart
                                        + componentPartitionedLength / 2 - component.getWidth() / 2,
                                parentHeight / 2 - component.getHeight() / 2);
                        case RIGHT -> component.setLocation(currentComponentStart
                                        + componentPartitionedLength / 2 - component.getWidth() / 2,
                                parentHeight / 2 - component.getWidth() / 2);
                        case BOTTOM_LEFT -> component.setLocation(currentComponentStart,
                                parentHeight - component.getHeight());
                        case BOTTOM -> component.setLocation(currentComponentStart
                                        + componentPartitionedLength / 2 - component.getWidth() / 2,
                                parentHeight - component.getHeight());
                        case BOTTOM_RIGHT -> component.setLocation(currentComponentStart
                                        + componentPartitionedLength - component.getWidth(),
                                parentHeight - component.getHeight());
                        default -> throw new IllegalArgumentException("Invalid alignment direction: " + alignment);
                    }
                }
                case COLUMN -> {
                    switch (alignment) {
                        case TOP_LEFT -> component.setLocation(0, currentComponentStart);
                        case TOP -> component.setLocation(parentWidth / 2 - component.getWidth() / 2,
                                currentComponentStart);
                        case TOP_RIGHT -> component.setLocation(parentWidth - component.getWidth(),
                                currentComponentStart);
                        case LEFT -> component.setLocation(0,
                                currentComponentStart + componentPartitionedLength / 2 - component.getHeight() / 2);
                        case CENTER -> component.setLocation(parentWidth / 2 - component.getWidth() / 2,
                                currentComponentStart + componentPartitionedLength / 2 - component.getHeight() / 2);
                        case RIGHT -> component.setLocation(parentWidth - component.getWidth(),
                                currentComponentStart + componentPartitionedLength / 2 - component.getHeight() / 2);
                        case BOTTOM_LEFT -> component.setLocation(0,
                                currentComponentStart + componentPartitionedLength - component.getHeight());
                        case BOTTOM -> component.setLocation(parentWidth / 2 - component.getWidth() / 2,
                                currentComponentStart + componentPartitionedLength - component.getHeight());
                        case BOTTOM_RIGHT -> component.setLocation(parentWidth - component.getWidth(),
                                currentComponentStart + componentPartitionedLength - component.getHeight());
                        default -> throw new IllegalArgumentException("Invalid alignment direction: " + alignment);
                    }
                }
                default -> throw new IllegalArgumentException("Invalid partition direction: " + partitionDirection);
            }

            currentComponentStart += componentPartitionedLength;

            associatedPanel.add(component);

            if (component instanceof CyderPanel panel) {
                panel.revalidateComponents();
            }
        }

        if (focusOwner != null) {
            focusOwner.requestFocus();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeComponent(Component component) {
        Preconditions.checkNotNull(component);
        Preconditions.checkArgument(alreadyInComponents(component));

        for (int i = 0 ; i < components.size() ; i++) {
            if (components.get(i).getComponent().equals(component)) {
                removeComponent(i);
                return;
            }
        }
    }

    /**
     * Removes the component at the specified index from the components list.
     * This also removes the partition and returns the new space to the list of available space.
     * If the associated panel is set, the contained component is removed from the panel.
     *
     * @param index the component at the specified index from the components list
     */
    public void removeComponent(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < components.size());

        if (associatedPanel != null) associatedPanel.remove(components.get(index).getComponent());
        components.remove(index);
    }

    /**
     * {@inheritDoc}
     */
    public void addComponent(Component component) {
        addComponent(component, newComponentPartitionSpace);
    }

    /**
     * Adds the provided component to the end of the components
     * list and provides the partition space requested.
     *
     * @param component      the component to add to the components list
     * @param partitionSpace the space to be partitioned for this component
     */
    public void addComponent(Component component, float partitionSpace) {
        addComponent(component, partitionSpace, newComponentPartitionAlignment);
    }

    /**
     * Adds the provided component to the end of the components
     * list and provides the partition space requested.
     *
     * @param component          the component to add to the components list
     * @param partitionSpace     the space to be partitioned for this component
     * @param partitionAlignment the alignment for the partitioned component if it
     *                           does not precisely fit the partitioned area
     */
    public void addComponent(Component component, float partitionSpace, PartitionAlignment partitionAlignment) {
        Preconditions.checkNotNull(component);
        Preconditions.checkArgument(PARTITION_RANGE.contains(partitionSpace));
        Preconditions.checkNotNull(partitionAlignment);
        Preconditions.checkState(!alreadyInComponents(component));

        PartitionedComponent partitionedComponent = new PartitionedComponent(component, partitionAlignment);
        partitionedComponent.setPartition(partitionSpace);
        components.add(partitionedComponent);

        revalidateComponents();
    }

    /**
     * Adds the provided component to the layout. This component will always take up the minimal partition
     * space required to be completely visible.
     *
     * @param component the component
     */
    public void addComponentMaintainSize(Component component) {
        addComponentMaintainSize(component, PartitionAlignment.CENTER);
    }

    /**
     * Adds the provided component to the layout. This component will always take up the minimal partition
     * space required to be completely visible.
     *
     * @param component          the component
     * @param partitionAlignment the alignment of the component
     */
    public void addComponentMaintainSize(Component component, PartitionAlignment partitionAlignment) {
        Preconditions.checkNotNull(component);
        Preconditions.checkNotNull(partitionAlignment);
        Preconditions.checkState(!alreadyInComponents(component));

        PartitionedComponent partitionedComponent = new PartitionedComponent(component, partitionAlignment);
        partitionedComponent.setPartition(Float.MAX_VALUE);
        components.add(partitionedComponent);

        revalidateComponents();
    }

    /**
     * Returns whether the provided component is in the components list already.
     *
     * @param component the component
     * @return whether the provided component is in the components list already
     */
    @ForReadability
    private boolean alreadyInComponents(Component component) {
        Preconditions.checkNotNull(component);

        for (PartitionedComponent partitionedComponent : components) {
            if (partitionedComponent.getComponent().equals(component)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a spacer as the next component with the provided partition.
     *
     * @param partition the partition the spacer should take up
     */
    public void spacer(float partition) {
        addComponent(new JLabel(), partition);
    }

    /**
     * Replaces the component at the provided index with the new component.
     *
     * @param component the component to replace the old component with
     * @param index     the index of the component to replace
     */
    public void setComponent(Component component, int index) {
        Preconditions.checkNotNull(component);
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < components.size());

        PartitionedComponent remove = components.get(index);
        float partition = remove.getPartition();
        PartitionAlignment alignment = remove.getAlignment();
        if (associatedPanel != null) {
            associatedPanel.remove(remove.getComponent());
        }

        PartitionedComponent newComponent = new PartitionedComponent(component, alignment);
        newComponent.setPartition(partition);
        components.set(index, newComponent);

        revalidateComponents();
    }

    /**
     * Clears all the partitions and components associated with this layout.
     */
    public void clearComponents() {
        components.clear();
    }
}
