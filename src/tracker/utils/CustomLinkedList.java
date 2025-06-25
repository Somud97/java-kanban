package tracker.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tracker.model.Task;

public class CustomLinkedList<T extends Task> {
    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }
    }

    private final Map<Integer, Node<T>> nodeMap = new HashMap<>();
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public CustomLinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    // Добавление элемента в конец списка
    public void linkLast(T item) {
        Node<T> newNode = new Node<>(item);
        nodeMap.put(item.getId(), newNode);

        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    public void removeNode(int id) {
        Node<T> nodeToRemove = nodeMap.get(id);
        if (nodeToRemove == null) return;

        if (nodeToRemove.prev != null) {
            nodeToRemove.prev.next = nodeToRemove.next;
        } else {
            head = nodeToRemove.next;
        }

        if (nodeToRemove.next != null) {
            nodeToRemove.next.prev = nodeToRemove.prev;
        } else {
            tail = nodeToRemove.prev;
        }

        nodeMap.remove(id);
        size--;
    }

    // Получение списка элементов
    public List<T> getTasks() {
        List<T> tasks = new ArrayList<>();
        Node<T> current = head;
        while (current != null) {
            tasks.add(current.data);
            current = current.next;
        }
        return tasks;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
        nodeMap.clear();
    }
}