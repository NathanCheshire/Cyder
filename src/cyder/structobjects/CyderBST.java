package cyder.structobjects;

public class CyderBST<T> {
    private Node root; //todo getters

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
        return 0;
    }

    public int depth() {
        return 0;
    }

    public void insert(T data) {

    }

    public Object remove() {
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
