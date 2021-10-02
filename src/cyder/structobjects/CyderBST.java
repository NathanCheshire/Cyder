package cyder.structobjects;

public class CyderBST {
    private Node root;

    public Node getRoot() {
        return this.root;
    }

    public CyderBST() {
        this.root = null;
    }

    public CyderBST(String data) {
        this.root = new Node(data, null, null);
    }

    public boolean contains(String data) {
        //todo traverse tree via comparisons until we find it or come to null
        return false;
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
            return 1; //todo does this logic even work

        return Math.max(leftDepth, rightDepth);
    }

    public void insert(String data) {
        //todo have to find where data goes to left or right until we get to null then we can make that the child
    }

    public Object remove(String data) {
        return innerRemove(root, data);
    }

    private String innerRemove(Node currentNode, String data) {
        //todo we should be checking children so that we can link the node we are removing's
        // parent to it's own children. We don't have a way to go back
        return null;
    }

    public String toString() {
        return inOrderList(root);
    }

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
