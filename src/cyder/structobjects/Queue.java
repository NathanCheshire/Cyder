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
       //empty tree
        if (front == null) {
            Node frontAndBack = new Node(data, null, null);
            front = frontAndBack;
            back = frontAndBack;
        } else {
            Node newFront = new Node(data, null, front);
            front = newFront;
        }
    }

    //add last
    public void addLast(T data) {
        //empty tree
        if (back == null) {
            Node frontAndBack = new Node(data,null,null);
            front = frontAndBack;
            back = frontAndBack;
        } else {
            Node newBack = new Node(data, back,null);
            back = newBack;
        }
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

            while (ref != null) {
                ret.append(ref.getData());
                ref = ref.getPrevious();

                if (ref != null)
                    ret.append(" -> ");
            }
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

            while (ref != null) {
                ret.append(ref.getData());
                ref = ref.getNext();

                if (ref != null)
                    ret.append(" -> ");
            }
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
