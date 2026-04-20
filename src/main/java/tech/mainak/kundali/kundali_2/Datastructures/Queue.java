package tech.mainak.kundali.kundali_2.Datastructures;

import java.util.ArrayList;

public class Queue {
    private class Node {
        private String data;
        private Node next;

        private Node(String data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;    // front of queue
    private Node tail;    // back of queue
    private int size;     // track queue size

    public Queue() {
        size = 0;
    }

    public Queue(String data) {
        head = new Node(data);
        tail = head;
        size = 1;
    }

    public Queue(ArrayList<String> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        head = new Node(data.get(0));
        Node current = head;
        for (int i = 1; i < data.size(); i++) {
            current.next = new Node(data.get(i));
            current = current.next;
        }
        tail = current;
        size = data.size();
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int size() {
        return size;
    }

    public void enqueue(String data) {
        Node newNode = new Node(data);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    public String dequeue() {
        if (isEmpty()) {
            return null;
        }
        String data = head.data;
        head = head.next;
        size--;
        if (head == null) {
            tail = null;  // Queue is now empty
        }
        return data;
    }

    public String peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "Empty Queue";
        }
        StringBuilder result = new StringBuilder("{ ");
        Node ptr = head;
        while (ptr != null) {
            result.append(ptr.data);
            if (ptr.next != null) {
                result.append(" , ");
            }
            ptr = ptr.next;
        }
        result.append(" }");
        return result.toString();
    }

    public ArrayList<String> toList() {
        ArrayList<String> result = new ArrayList<>(size);  // Initialize with capacity
        Node ptr = head;
        while (ptr != null) {
            result.add(ptr.data);
            ptr = ptr.next;
        }
        return result;
    }
}