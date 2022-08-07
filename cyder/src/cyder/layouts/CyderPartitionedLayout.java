package cyder.layouts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import cyder.ui.CyderPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A layout designed to allow partitioning of a row or column into different parts each taking up
 * a certain percent of the whole partitioned area. One might want 30%, 20%, and 50% for a row
 * and this will allow the user to do so.
 */
public class CyderPartitionedLayout extends CyderLayout {
    /**
     * The range all partitions must fall within.
     */
    public static final Range<Integer> PARTITION_RANGE = Range.closed(0, 100);

    /**
     * The maximum partition value which all partitions and
     * the sum of all partitions must be less than or equal to.
     */
    public static final int MAX_PARTITION = PARTITION_RANGE.upperEndpoint();

    /**
     * The amount of partition space to assign a component added without a provided partition space.
     */
    public static final int DEFAULT_PARTITION_SPACE_PERCENT = 10;

    /**
     * The partition space to partition to a new component being added without a specified partition space.
     */
    private int newComponentPartitionSpace = DEFAULT_PARTITION_SPACE_PERCENT;

    /**
     * The sum of all partitions from {@link #partitions}.
     */
    private int partitionsSum;

    /**
     * The list of all added partitions.
     */
    private final LinkedList<Integer> partitions;

    /**
     * The possible directions to lay components out.
     */
    public enum PartitionDirection {
        ROW, COLUMN
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
     * Constructs a new partitioned layout.
     */
    public CyderPartitionedLayout() {
        partitions = new LinkedList<>();
        components = new ArrayList<>();
        partitionsSum = 0;
    }

    /**
     * The direct components managed by this layout.
     */
    private final ArrayList<PartitionedComponent> components;

    /**
     * Returns a list of all the partitions.
     *
     * @return a list of all the partitions
     */
    public ImmutableList<Integer> getPartitions() {
        return ImmutableList.copyOf(partitions);
    }

    /**
     * Returns the sum of all partitions.
     *
     * @return the sum of all partitions
     */
    public int getPartitionsSum() {
        return partitionsSum;
    }

    /**
     * Returns the partition for the component at the specified index.
     *
     * @param index the index of the component to return the partition of
     * @return the partition for the component at the specified index
     */
    public int getPartition(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < partitions.size());

        return partitions.get(index);
    }

    /**
     * Sets the partition for the component at the specified index.
     *
     * @param index     the index of the partition to update
     * @param partition the new partition value
     */
    public void setPartition(int index, int partition) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < partitions.size());

        int oldPartition = partitions.get(index);

        if (partition > oldPartition) {
            int newPartitionSum = partitionsSum + partition - oldPartition;

            if (newPartitionSum > MAX_PARTITION) {
                throw new IllegalArgumentException("Requested partition overriding old partition at"
                        + " provided index will cause partition sum to be: " + newPartitionSum);
            }

        }

        partitions.set(index, partition);
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
    public ArrayList<Component> getLayoutComponents() {
        ArrayList<Component> ret = new ArrayList<>(components.size());

        components.forEach(component -> ret.add(component.getComponent()));

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public Dimension getPackSize() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * The CyderPanel this layout manager will manage.
     */
    private CyderPanel associatedPanel;

    /**
     * Sets the CyderPanel to manage. Components this LM has been given thus far
     * will be evaluated and added to the Panel.
     *
     * @param associatedPanel the CyderPanel to manage
     */
    public void setAssociatedPanel(CyderPanel associatedPanel) {
        this.associatedPanel = Preconditions.checkNotNull(associatedPanel);

        revalidateComponents();
    }

    /**
     * Returns the partition space given to a new component if none is specified.
     *
     * @return the partition space given to a new component if none is specified
     */
    public int getNewComponentPartitionSpace() {
        return newComponentPartitionSpace;
    }

    /**
     * Sets the partition space given to a new component if none is specified.
     *
     * @param newComponentPartitionSpace the partition space given to a new component if none is specified
     */
    public void setNewComponentPartitionSpace(int newComponentPartitionSpace) {
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
    public void revalidateComponents() {
        if (associatedPanel == null) {
            return;
        }

        Component focusOwner = null;

        int currentComponentStart = 0;

        for (int i = 0 ; i < components.size() ; i++) {
            PartitionedComponent partitionedComponent = components.get(i);

            Component component = partitionedComponent.getComponent();
            PartitionAlignment alignment = partitionedComponent.getAlignment();

            int parentWidth = associatedPanel.getWidth();
            int parentHeight = associatedPanel.getHeight();

            int componentPartitionedLen = (int) switch (partitionDirection) {
                case ROW -> ((float) partitions.get(i) / MAX_PARTITION) * parentWidth;
                case COLUMN -> ((float) partitions.get(i) / MAX_PARTITION) * parentHeight;
            };

            // indicates a spacer in the layout
            if (component == null) {
                currentComponentStart += componentPartitionedLen;
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
                                + componentPartitionedLen / 2 - component.getWidth() / 2, 0);
                        case TOP_RIGHT -> component.setLocation(currentComponentStart
                                + componentPartitionedLen - component.getWidth(), 0);
                        case LEFT -> component.setLocation(currentComponentStart,
                                parentHeight / 2 - component.getWidth() / 2);
                        case CENTER -> component.setLocation(currentComponentStart
                                        + componentPartitionedLen / 2 - component.getWidth() / 2,
                                parentHeight / 2 - component.getHeight() / 2);
                        case RIGHT -> component.setLocation(currentComponentStart
                                        + componentPartitionedLen / 2 - component.getWidth() / 2,
                                parentHeight / 2 - component.getWidth() / 2);
                        case BOTTOM_LEFT -> component.setLocation(currentComponentStart,
                                parentHeight - component.getHeight());
                        case BOTTOM -> component.setLocation(currentComponentStart
                                        + componentPartitionedLen / 2 - component.getWidth() / 2,
                                parentHeight - component.getHeight());
                        case BOTTOM_RIGHT -> component.setLocation(currentComponentStart
                                        + componentPartitionedLen - component.getWidth(),
                                parentHeight - component.getHeight());
                        default -> throw new IllegalArgumentException("Invalid alignment direction: " + alignment);
                    }
                }
                case COLUMN -> {
                    switch (alignment) {
                        case TOP_LEFT -> component.setLocation(0, currentComponentStart);
                        case TOP -> component.setLocation(parentWidth / 2 - component.getWidth(),
                                currentComponentStart);
                        case TOP_RIGHT -> component.setLocation(parentWidth - component.getWidth(),
                                currentComponentStart);
                        case LEFT -> component.setLocation(0,
                                currentComponentStart + componentPartitionedLen / 2 - component.getHeight() / 2);
                        case CENTER -> component.setLocation(parentWidth / 2 - component.getWidth() / 2,
                                currentComponentStart + componentPartitionedLen / 2 - component.getHeight() / 2);
                        case RIGHT -> component.setLocation(parentWidth - component.getWidth(),
                                currentComponentStart + componentPartitionedLen / 2 - component.getHeight() / 2);
                        case BOTTOM_LEFT -> component.setLocation(0,
                                currentComponentStart + componentPartitionedLen - component.getHeight());
                        case BOTTOM -> component.setLocation(parentWidth / 2 - component.getWidth() / 2,
                                currentComponentStart + componentPartitionedLen - component.getHeight());
                        case BOTTOM_RIGHT -> component.setLocation(parentWidth - component.getWidth(),
                                currentComponentStart + componentPartitionedLen - component.getHeight());
                        default -> throw new IllegalArgumentException("Invalid alignment direction: " + alignment);
                    }
                }
                default -> throw new IllegalArgumentException("Invalid partition direction: " + partitionDirection);
            }

            currentComponentStart += componentPartitionedLen;

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
    public void removeComponent(Component component) {
        Preconditions.checkNotNull(component);

        int index = -1;

        for (int i = 0 ; i < components.size() ; i++) {
            if (components.get(i).getComponent().equals(component)) {
                index = i;
                break;
            }
        }

        Preconditions.checkArgument(index != -1);
        removeComponent(index);
    }

    /**
     * Removes the component at the specified index from the components list.
     * This also removes the partition and returns the new space to the list of available space.
     *
     * @param index the component at the specified index from the components list
     */
    public void removeComponent(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < partitions.size());

        int addBack = partitions.get(index);
        partitionsSum += addBack;

        partitions.remove(index);
        components.remove(index);
    }

    /**
     * {@inheritDoc}
     */
    public void addComponent(Component component) {
        Preconditions.checkNotNull(component);

        addComponent(component, newComponentPartitionSpace);
    }

    /**
     * Adds the provided component to the end of the components
     * list and provides the partition space requested.
     *
     * @param component      the component to add to the components list
     * @param partitionSpace the space to be partitioned for this component
     */
    public void addComponent(Component component, int partitionSpace) {
        Preconditions.checkNotNull(component);
        Preconditions.checkArgument(PARTITION_RANGE.contains(partitionSpace));
        Preconditions.checkArgument(partitionSpace + partitionsSum <= MAX_PARTITION);

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
    public void addComponent(Component component, int partitionSpace, PartitionAlignment partitionAlignment) {
        Preconditions.checkNotNull(component);
        Preconditions.checkArgument(PARTITION_RANGE.contains(partitionSpace));
        Preconditions.checkArgument(partitionSpace + partitionsSum <= MAX_PARTITION);
        Preconditions.checkNotNull(partitionAlignment);

        boolean in = false;
        for (PartitionedComponent partitionedComponent : components) {
            if (partitionedComponent.getComponent().equals(component)) {
                in = true;
                break;
            }
        }
        Preconditions.checkArgument(!in, "Layout already contains component");

        components.add(new PartitionedComponent(component, partitionAlignment));
        partitions.add(partitionSpace);
        partitionsSum += partitionSpace;

        revalidateComponents();
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

        components.set(index, new PartitionedComponent(component, components.get(index).getAlignment()));
    }

    /**
     * Clears all the partitions and components associated with this layout.
     */
    public void clearComponents() {
        components.clear();
        partitions.clear();

        partitionsSum = 0;
    }
}
