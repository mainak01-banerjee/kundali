package tech.mainak.kundali.kundali_2.Datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CircularLinkedList {

    public class House {
        private String name;
        private ArrayList<Object> data;
        private House next;

        private House(String name) {
            this.name = name;
            data = new ArrayList<>(15);
            next = null;
        }

        private House(String name, ArrayList<Object> data) {
            this.name = name;
            this.data = data;
            next = null;
        }
    }

    private House head;
    private House tail;
    private int size = 0;

    public CircularLinkedList() {}

    public CircularLinkedList(String name) {
        head = new House(name);
        tail = head;
        tail.next = head;
        size++;
    }

    public CircularLinkedList(String name, ArrayList<Object> data) {
        head = new House(name, data);
        tail = head;
        tail.next = head;
        size++;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int size() {
        return size;
    }

    public boolean add(String name, ArrayList<Object> data) {
        House newHouse = new House(name);
        newHouse.data = data;

        if (isEmpty()) {
            head = newHouse;
            tail = head;
        } else {
            tail.next = newHouse;
            tail = newHouse;
        }
        tail.next = head;
        size++;
        return true;
    }

    public ArrayList<Object> getDataArray(String houseName) {
        if (isEmpty()) {
            return null;
        }

        House ptr = head;
        do {
            if (ptr.name.equals(houseName)) {
                return ptr.data;
            }
            ptr = ptr.next;
        } while (ptr != head);

        return null;
    }
    @Override
    public String toString() {
        String str="";
        if (isEmpty()) {
            return "List is empty";
        }

        House ptr = head;
        int i = 1;
        do {
            str=str+"House "+i+": "+ptr.name+", Data: "+ptr.data+" ";
            //System.out.println("House " + i + ": " + ptr.name + ", Data: " + ptr.data);
            ptr = ptr.next;
            i++;
        } while (ptr != head);
        return str;
    }

    public Map<String,ArrayList<Object>> getDataMap() {
        Map<String,ArrayList<Object>> map = new LinkedHashMap<>();
        if (isEmpty()) {
            return null;
        }
        House ptr = head;
        int i = 1;
        do{
            String houseNumber="house_"+i;
            String houseName=ptr.name ;
            ArrayList<Object> houseData=ptr.data;
            houseData.add(houseName);
            map.put(houseNumber,houseData);
            ptr = ptr.next;
            i++;
        }while(ptr != head);

        return map;

    }

    public boolean editData(String houseName, ArrayList<Object> newData) {
        ArrayList<Object> existingData = getDataArray(houseName);
        if (existingData == null) {
            return false;
        }
        House ptr = head;
        do {
            if (ptr.name.equals(houseName)) {
                ptr.data = newData;
                return true;
            }
            ptr = ptr.next;
        }
        while (ptr != head);
        return true;
    }

    public ArrayList<String> getNames() {
        if (isEmpty()) {
            throw new RuntimeException("List is empty");
        }
        ArrayList<String> names = new ArrayList<>();
        House ptr = head;
        do {
            names.add(ptr.name);
            ptr = ptr.next;
        }
        while (ptr != head);

        return names;
    }
}