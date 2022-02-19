package cyder.structures;

public class CyderQueue<T> {
    transient Node front;
    transient Node back;

    //null constructor
    public CyderQueue() {
        this.front = null;
        this.back = null;
    }

    //initial data constructor
    public CyderQueue(T data) {
        this.front = new Node(data, null, null);
        this.back = front;
    }

    public Object peekFirst() {
        if (front == null)
            throw new EmptyQueueException();

        return front.data;
    }

    public Object peekLast() {
        if (back == null)
            throw new EmptyQueueException();

        return back.data;
    }

    public Object removeFirst() {
        return remove(front);
    }

    public Object removeLast() {
       return remove(front);
    }

    private Object remove(Node n) {
        if (n == null)
            throw new IllegalArgumentException("Given Node is null");

        Object ret = null;

        if (n == front) {
            //removing front and setting it's next's previous to null
            if (front.next != null) {
                //has a next
                ret = front.data;
                front = front.next;
                front.previous = null;
            } else {
                //no next so removing means empty queue
                ret = front.data;
                front = null;
                back = null;
            }
        } else if (n == back) {
            //removing back, and setting it's previous' next to null
            if (back.previous != null) {
                //has a previous
                ret = back.data;
                back = null;
                front = null;
            } else {
                //no previous so removing means empty queue
                ret = back.data;
                front = null;
                back = null;
            }
        } else {
            //remove node and set it's previous's next to it's next
            // also set it's next's previous to it's own previous
            ret = n.data;

            //not front and not back here so we should have at least 3 elements in queue
            final Node prev = n.previous;
            final Node next = n.next;

            n.previous.next = next;
            n.next.previous = prev;
        }

        return ret;
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

    public void addLast(T data) {
        final Node b = back;
        final Node newNode = new Node(data, b, null);
        back = newNode;
        if (b == null)
            front = newNode;
        else
            b.next = newNode;
    }

    public void enqueue(T data) {
        addLast(data);
    }

    public Object dequeue() {
        return removeFirst();
    }

    public boolean contains(T data) {
        boolean ret = false;

        Node tmp = front;
        while (tmp != null) {
            if (tmp.data.equals(data)) {
                ret = true;
                break;
            }

            tmp = tmp.next;
        }

        return ret;
    }

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
            return "Empty Queue";
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

    @Override
    public String toString() {
        if (front == null || back == null) {
            return "Empty Queue";
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
        public T data;
        public Node previous;
        public Node next;

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
