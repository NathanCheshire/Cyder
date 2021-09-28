package cyder.structobjects;

public class Queue<T> {
    Node front;
    Node back;

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

        return front.getData();
    }

    //peekLast
    public Object peekLast() {
        if (back == null)
            throw new EmptyQueueException();

        return back.getData();
    }

    //remove first
    public Object removeFirst() {
        if (front == null)
            throw new EmptyQueueException();

        Object ret = front.getData();

        //front is back and is the only Node in the queue
        if (front.getNext() == null) {
            front = null;
            back = null;
        }
        //queue has at least two elements currently
        else {
            Node ptr = front;
            //only two elements
            if (front.getNext().getNext() == null) {
                ptr = null;
                front = back;
            } else {
                //more than two
                front = front.getNext();
                ptr = null;
            }
        }

        return ret;
    }

    //remove last
    public Object removeLast() {
        if (back == null)
            throw new EmptyQueueException();

        Object ret = back.getData();

        //back is front and is the only Node in the queue
        if (back.getPrevious() == null) {
            back = null;
            front = null;
        }
        //queue has at least two elements currently
        else {
            Node ptr = back;
            //only two elements
            if (back.getPrevious().getPrevious() == null) {
                ptr = null;
                back = front;
            } else {
                //more than two
                back = back.getPrevious();
                ptr = null;
            }
        }

        return ret;
    }

    //add first
    public void addFirst(T data) {
        Node add = new Node(data, null, front);
        front = add;

        if (back == null)
            back = front;
    }

    //add last
    public void addLast(T data) {
        Node add = new Node(data, back, null);
        back = add;

        if (front == null)
            front = back;
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
            if (tmp.getData() == data) {
                ret = true;
                break;
            }

            tmp = tmp.getNext();
        }

        return ret;
    }

    //size
    public int size() {
        int ret = 0;

        Node tmp = front;
        while (tmp != null) {
            ret++;
            tmp = tmp.getNext();
        }

        return ret;
    }

    public String forwardTraversal() {
        return this.toString();
    }

    public String reverseTraversal() {
        StringBuilder ret = new StringBuilder();

        if (back == null) {
            ret.append("Emtpy queue");
        } else {
            Node ref = back;

            while (ref.getPrevious().getPrevious() != null) {
                ret.append(ref.getData()).append(" -> ");
                ref = ref.getPrevious();
            }

            ref = ref.getPrevious();
            ret.append(ref.getData());
        }

        return ret.toString();
    }

    //toString
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();

        if (front == null) {
            ret.append("Empty queue");
        } else {
            Node ref = front;

            //okay so we can't do a double next next approach for ANY data struct methods
            while (ref.getNext().getNext() != null) {
                ret.append(ref.getData()).append(" -> ");
                ref = ref.getNext();
            }

            ref = ref.getNext();
            ret.append(ref.getData());
        }

        return ret.toString();
    }

    public static class Node<T> {
        T data;
        Node previous;
        Node next;

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public Node getPrevious() {
            return previous;
        }

        public void setPrevious(Node previous) {
            this.previous = previous;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

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
