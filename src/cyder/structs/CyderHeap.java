package cyder.structs;

public class CyderHeap {
    private Object[] heapStruct;
    private int size = 0;
    private int maxSize;
    private Type type;

    public static int ABSOLUTE_MIN_SIZE = 1;

    public enum Type {
        MIN,MAX
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        fixHeap();
    }

    public CyderHeap(Object data, int maxSize, Type type) {
        if (maxSize < ABSOLUTE_MIN_SIZE)
            throw new IllegalStateException("Provided length does not allow the provided data to exist within the heap");

        this.maxSize = maxSize;
        heapStruct = new Object[maxSize];

        if (data != null)
            heapStruct[0] = data;

        this.type = type;
        this.size = 1;
    }

    public CyderHeap(Object[] data, int maxSize, Type type) {
        if (data == null || data.length == 0){
            this.maxSize = maxSize;
            heapStruct = new Object[maxSize];
            this.size = data.length;
        } else {
            if (data.length > maxSize)
                throw new IllegalStateException("Provided data exceeds provided max length");
            else if (maxSize < ABSOLUTE_MIN_SIZE)
                throw new IllegalStateException("Absolute min size exceeded");

            System.arraycopy(data, 0, heapStruct, 0, data.length);

            this.size = data.length;
        }

        this.type = type;
    }

    /**
     * Rebuilds the heap depending on the heap type: min or max
     */
    public void fixHeap() {
        switch (this.type) {
            case MAX:
                maxHeapify();
                break;
            case MIN:
                minHeapify();
                break;
        }
    }

    /**
     * Finds the left child index of the node at the provided index.
     * @param position the index of the parent to find the left child of
     * @return the index of the left child
     */
    private int leftChildIndex(int position) {
        return 2 * position;
    }

    /**
     * Finds the right child index of the node at the provided index.
     * @param position the index of the parent to find the right child of
     * @return the index of the right child
     */
    private int rightChildIndex(int position) {
        return 2 * position + 1;
    }

    /**
     * Finds the index of the parent of the node at the provided index.
     * @param position the index to find the parent of
     * @return the index of the parent
     */
    private int parent(int position) {
        return position / 2;
    }

    public void minHeapify() {

    }

    public void maxHeapify() {

    }

    /**
     * Swaps the two elements at the provided indicies
     * @param index1 the first index
     * @param index2 the second index
     */
    private void swap(int index1, int index2) {
        if (index1 < 0 || index2 < 0 || index1 > size || index2 > size)
            throw new IndexOutOfBoundsException("Provided index is out of bounds");
        if (index1 == index2)
            return;

        Object uno = heapStruct[index1];
        heapStruct[index1] = heapStruct[index2];
        heapStruct[index2] = uno;
    } //todo test this with two element heap and print functions for a custom object

    public void insert(Object data) {
        if (this.size + 1 < maxSize)
            throw new IllegalStateException("CyderHeap cannot add any more elements");

        heapStruct[size] = data;

        this.size++;
        fixHeap();
    }

    /**
     * Removes the root from the heap as is the point of a heap data structure.
     */
    public Object remove() {
        if (this.size - 1 < 0)
            throw new IllegalStateException("CyderHeap is empty");

        Object ret = heapStruct[0];
        //todo this should be a copy and not a ref, then fix heap

        this.size--;
        fixHeap();

        return ret;
    }

    /**
     * Returns a string representation of the CyderHeap data structure.
     * @return the string representation promised
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < this.size ; i++) {
            sb.append(heapStruct[i]);

            if (i != this.size - 1)
                sb.append(" -> ");
        }

        return sb.toString();
    }
}
