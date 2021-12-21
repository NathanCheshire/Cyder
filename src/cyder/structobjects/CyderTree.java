package cyder.structobjects;

public class CyderTree {
    private Node root;

    public Node getRoot() {
        return this.root;
    }

    public CyderTree() {
        this.root = null;
    }

    public CyderTree(String data) {
        this.root = new Node(data, null, null);
    }

    public boolean contains(String data) {
        return innerContains(root, data);
    }

    //need to check both since this isn't a BST, it's kind of like a heap
    private boolean innerContains(Node root, String data) {
        if (root.data.equals(data))
            return true;
        else {
            boolean left = root.leftChild != null && innerContains(root.leftChild, data);
            boolean right = root.rightChild != null && innerContains(root.rightChild, data);

            return left || right;
        }
    }

    public int size() {
        if (this.root == null)
            throw new EmptyTreeException("Empty Tree");

        return innerSize(root);
    }

    private int innerSize(Node start) {
        return (start == null ? 0 :
                (start.leftChild != null ? innerSize(start.leftChild) : 0) +
                        (start.rightChild != null ? innerSize(start.rightChild) : 0));
    }

    public int depth() {
        return maxDepth(root);
    }

    private int maxDepth(Node start) {
        int leftDepth = 0;
        int rightDepth = 0;

        if (start.leftChild != null)
            leftDepth = maxDepth(start.leftChild);
        if (start.rightChild != null)
            rightDepth = maxDepth(start.rightChild);

        //end of tree so starting here
        if (start.leftChild == null && start.rightChild == null)
            return 1; //does this logic even work

        return Math.max(leftDepth, rightDepth);
    }

    public void insert(String data) {
        //simply insert into available space
    }

    public Object remove(String data) {
        return innerRemove(root, data);
    }

    private String innerRemove(Node currentNode, String data) {
        //recurse until we find the data, if data is not found throw an exception
        //once we find the node we need to step a level back to get the parent
        //after getting the parent, link children of the node to the parent or other nodes on the same level
        // look up pseudocode for this online or something
        return null;
    }

    public String toString() {
        return inOrderList(root);
    }

    //make pre order and post order too
    private String inOrderList(Node start) {
        if (start == null)
            throw new EmptyTreeException();
        else if (start.leftChild == null && start.rightChild == null) {
            return String.valueOf(start.data);
        } else if (start.leftChild == null && start.rightChild != null) {
            return start.data + "," + start.rightChild.data;
        } else if (start.leftChild != null && start.rightChild == null) {
            return start.leftChild.data + "," + start.data;
        }
        //both shouldn't be null at this point
        else {
            return start.leftChild.data + "," + start.data + "," + start.rightChild.data;
        }
    }

    private static class Node {
        public Node leftChild;
        public Node rightChild;
        public String data;

        public Node(String data, Node left, Node right) {
            this.data = data;
            this.leftChild = left;
            this.rightChild = right;
        }
    }

    public static class EmptyTreeException extends RuntimeException {
        public EmptyTreeException(String errorMessage) {
            super(errorMessage);
        }

        public EmptyTreeException() {
            super("Empty Tree");
        }
    }
}
