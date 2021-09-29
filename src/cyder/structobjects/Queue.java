package cyder.structobjects;

public class Queue<T> {
    transient Node front;
    transient Node back;

    //null constructor
    public Queue() {
        this.front = null;
        this.back = null;
    }

    //initial data constructor
    public Queue(T data) {
        this.front = new Node(data, null, null);
        this.back = front;
    }

    //peekFirst
    public Object peekFirst() {
        if (front == null)
            throw new EmptyQueueException();

        return front.data;
    }

    //peekLast
    public Object peekLast() {
        if (back == null)
            throw new EmptyQueueException();

        return back.data;
    }

    //remove first
    public Object removeFirst() {
        if (front == null)
            throw new EmptyQueueException();

        Node tmp = front;
        front = front.next;
        Object data = tmp.data;
        tmp = null;
        return data;
    }

    //remove last
    public Object removeLast() {
        if (back == null)
            throw new EmptyQueueException();

        Node tmp = back;
        back = back.previous;
        Object data = tmp.data;
        tmp = null;
        return data;
    }

    public void addFirst(T data) {
        final Node f = front;
        final Node newNode = new Node(data, null, f);
        front = newNode;
        if (f == null)
            back = newNode;
        else
            f.previous = newNode;
    }

    //add last
    public void addLast(T data) {

    }

    //enqueue
    public void enqueue(T data) {
        addLast(data);
    }

    //dequeue
    public Object dequeue() {
        return removeFirst();
    }

    //contains data
    public boolean contains(T data) {
        boolean ret = false;

        Node tmp = front;
        while (tmp != null) {
            if (tmp.data == data) {
                ret = true;
                break;
            }

            tmp = tmp.next;
        }

        return ret;
    }

    //size
    public int size() {
        int ret = 0;

        Node tmp = front;
        while (tmp != null) {
            ret++;
            tmp = tmp.next;
        }

        return ret;
    }

    public String forwardTraversal() {
        return toString();
    }

    public String reverseTraversal() {
        if (front == null || back == null) {
            return "Empty tree";
        } else {
            StringBuilder sb = new StringBuilder();

            Node ptr = back;

            while (ptr != null) {
                sb.append(ptr.data);
                ptr = ptr.previous;

                if (ptr != null)
                    sb.append(" -> ");
            }

            return sb.toString();
        }
    }

    //toString
    @Override
    public String toString() {
        if (front == null || back == null) {
            return "Empty tree";
        } else {
            StringBuilder sb = new StringBuilder();

            Node ptr = front;

            while (ptr != null) {
                sb.append(ptr.data);
                ptr = ptr.next;

                if (ptr != null)
                    sb.append(" -> ");
            }

            return sb.toString();
        }
    }

    public static class Node<T> {
        T data;
        Node previous;
        Node next;

        public Node(T data, Node previous, Node next) {
            this.data = data;
            this.previous = previous;
            this.next = next;
        }
    }

    public static class EmptyQueueException extends RuntimeException {
        public EmptyQueueException(String errorMessage) {
            super(errorMessage);
        }

        public EmptyQueueException() {
            super("Empty Queue");
        }
    }
}
