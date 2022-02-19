package cyder.structures;

public class CyderLinkedList<T> {
    transient public Node front;
    transient public Node back;

    public CyderLinkedList() {
        front = null;
        back = null;
    }

    public CyderLinkedList(T data) {
        Node frontAndBack = new Node(data, null, null);
        this.front = frontAndBack;
        this.back = frontAndBack;
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

    public Object removeFirst() {
        return remove(front);
    }

    public Object removeLast() {
        return remove(back);
    }

    public Object remove(Node n) {
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

    public Object getFirst() {
        if (front == null)
            throw new EmptyListException("CyderLinkedList is empty");

        return front.data;
    }

    public Object getLast() {
        if (back == null)
            throw new EmptyListException("CyderLinkedList is empty");

        return back.data;
    }

    public boolean contains(Object o) {
        boolean ret = false;

        Node ptr = front;

        while (ptr != null) {
            if (o.equals(ptr.data)) {
                ret = true;
                break;
            }

            ptr = ptr.next;
        }

        return ret;
    }

    public String forwardTraversal() {
        return this.toString();
    }

    public String reverseTraversal() {
        if (front == null || back == null) {
            return "Empty List";
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
           return "Empty List";
       } else {
           StringBuilder ret = new StringBuilder();

           Node ptr = front;

           while (ptr != null) {
               ret.append(ptr.data);
               ptr = ptr.next;

               if (ptr != null)
                   ret.append(" -> ");
           }

           return ret.toString();
       }
    }

    public int size() {
        int count = 0;

        Node ptr = front;

        while (ptr != null) {
            count++;
            ptr = ptr.next;
        }

        return count;
    }

    private static class Node<T> {
        public T data;
        public Node previous;
        public Node next;

        public Node(T data, Node prev, Node next) {
            this.data = data;
            this.previous = prev;
            this.next = next;
        }
    }

    public static class EmptyListException extends RuntimeException {
        public EmptyListException(String errorMessage) {
            super(errorMessage);
        }

        public EmptyListException() {
            super("Empty List");
        }
    }
}
