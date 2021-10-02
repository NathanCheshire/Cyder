package cyder.structobjects;

public class CyderBST<T> {
    private Node root;

    public Node getRoot() {
        return this.root;
    }

    public CyderBST() {
        this.root = null;
    }

    public CyderBST(T data) {
        this.root = new Node(data, null, null);
    }

    public boolean contains(Object o) {
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

    public void insert(T data) {
        //todo have to find where data goes to left or right until we get to null then we can make that the child
    }

    public Object remove(Node n) {
        Object ret = n.data;
        innerRemove(root, n);
        return ret;
    }

    private void innerRemove(Node currentNode, Node lookingFor) {
        //todo should be a child of currentNode so that we can set the chilren to it if it has children
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

    private static class Node<T> {
        public Node leftChild;
        public Node rightChild;
        public T data;

        public Node(T data, Node left, Node right) {
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
