package cyder.layouts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.util.LinkedList;

/**
 * A layout designed to allow partitioning of a row or column into different parts each taking up
 * a certain percent of the whole partitioned area. One might want 30%, 20%, and 50% for a row
 * and this will allow the user to do so.
 */
public class CyderPartitionedLayout extends CyderLayout {
    public static final Range<Integer> PARTITION_RANGE = Range.closed(0, 100);
    public static final int MAX_PARTITION = 100;

    private int partitionsSum = 0;

    private final LinkedList<Integer> partitions;

    public enum PartitionDirection {
        ROW, COLUMN
    }

    private PartitionDirection partitionDirection = PartitionDirection.COLUMN;

    public CyderPartitionedLayout() {
        this(0);
    }

    public CyderPartitionedLayout(int firstPartition) {
        Preconditions.checkArgument(PARTITION_RANGE.contains(firstPartition));

        partitions = new LinkedList<>();
        partitions.add(firstPartition);
        partitionsSum += firstPartition;
    }

    public CyderPartitionedLayout(int firstPartition, int... otherPartitions) {
        Preconditions.checkArgument(PARTITION_RANGE.contains(firstPartition));

        partitions = new LinkedList<>();
        partitions.add(firstPartition);
        partitionsSum += firstPartition;

        for (int partition : otherPartitions) {
            if (PARTITION_RANGE.contains(partition + partitionsSum)) {
                partitions.add(partition);
                partitionsSum += partition;
            } else {
                throw new IllegalArgumentException("Could not add partition as will cause overflow. Partition: "
                        + partition + ", totalPercent: " + partitionsSum);
            }
        }
    }

    public ImmutableList<Integer> getPartitions() {
        return ImmutableList.copyOf(partitions);
    }

    public int getPartitionsSum() {
        return partitionsSum;
    }

    public int getPartition(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < partitions.size());

        return partitions.get(index);
    }

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

        // todo revalidate
    }

    public PartitionDirection getPartitionDirection() {
        return partitionDirection;
    }

    public void setPartitionDirection(PartitionDirection partitionDirection) {
        this.partitionDirection = partitionDirection;
    }
}
