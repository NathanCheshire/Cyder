package cyder.structs;

import java.util.EmptyStackException;

public class CyderStack<T> {
    private Node top = null;

    //null constructgor
    public CyderStack() {
        this.top = null;
    }

    //initial data constructor
    public CyderStack(T data) {
        top = new Node(data, top);
    }

    public void push(T data) {
        top = new Node(data, top);
    }

    //pop removes and returns top value
    public Object pop() {
        if (top == null)
            throw new EmptyStackException();

        Node tmp = top;
        top = top.getBelow();
        Object data = tmp.getData();
        tmp = null;
        return data;
    }

    //peek method returns top value
    public Object peek() {
        if (isEmpty())
            throw new EmptyStackException();

        return top.getData();
    }

    //isEmpty method
    public boolean isEmpty() {
        return top == null;
    }

    //print contents in order
    @Override
    public String toString() {
        if (top == null) {
            return "Empty tree";
        } else {
            StringBuilder sb = new StringBuilder();

            Node ptr = top;

            while (ptr != null) {
                sb.append(ptr.getData());
                ptr = ptr.getBelow();

                if (ptr != null)
                    sb.append(" -> ");
            }

            return sb.toString();
        }
    }

    public int size() {
        int ret = 0;

        Node tmp = top;
        while (tmp != null) {
            ret++;
            tmp = tmp.getBelow();
        }

        return ret;
    }

    //node class to be used
    private static class Node<T> {
        private T data;
        private Node below;

        public Node() {
            this.data = null;
            this.below = null;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public Node getBelow() {
            return below;
        }

        public void setBelow(Node below) {
            this.below = below;
        }

        public Node(T data, Node below) {
            this.data = data;
            this.below = below;

        }
    }
}
