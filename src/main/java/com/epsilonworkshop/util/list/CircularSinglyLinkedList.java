/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sebap
 */
public final class CircularSinglyLinkedList<T> implements LinkedListADT<T> {

    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail; // tail.next siempre apunta a head (nunca null, salvo lista vacía)
    private int size;

    @Override
    public void insertAt(int index, T value) {
        validateInsertIndex(index);

        if (index == 0) {
            insertFirst(value);
            return;
        }
        if (index == size) {
            insertLast(value);
            return;
        }

        Node<T> previous = nodeAt(index - 1);
        Node<T> newNode = new Node<>(value);
        newNode.next = previous.next;
        previous.next = newNode;
        size++;
    }

    @Override
    public void insertFirst(T value) {
        Node<T> newNode = new Node<>(value);

        if (head == null) {
            head = newNode;
            tail = newNode;
            newNode.next = newNode; // se apunta a sí mismo: único nodo, circular
        } else {
            newNode.next = head;
            head = newNode;
            tail.next = head; // cierra el círculo con el nuevo head
        }
        size++;
    }

    @Override
    public void insertLast(T value) {
        if (head == null) {
            insertFirst(value);
            return;
        }

        Node<T> newNode = new Node<>(value);
        newNode.next = head; // cierra el círculo de entrada
        tail.next = newNode;
        tail = newNode;
        size++;
    }

    @Override
    public T readAt(int index) {
        validateExistingIndex(index);
        return nodeAt(index).value;
    }

    @Override
    public void updateAt(int index, T value) {
        validateExistingIndex(index);
        nodeAt(index).value = value;
    }

    @Override
    public T deleteAt(int index) {
        validateExistingIndex(index);

        if (size == 1) {
            T value = head.value;
            head = null;
            tail = null;
            size = 0;
            return value;
        }

        if (index == 0) {
            Node<T> removed = head;
            head = head.next;
            tail.next = head; // el círculo se vuelve a cerrar con el nuevo head
            size--;
            return removed.value;
        }

        Node<T> previous = nodeAt(index - 1);
        Node<T> removed = previous.next;
        previous.next = removed.next;
        if (removed == tail) {
            tail = previous;
        }
        size--;
        return removed.value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(T value) {
        return indexOf(value) != -1;
    }

    @Override
    public int indexOf(T value) {
        if (head == null) {
            return -1;
        }
        Node<T> current = head;
        for (int i = 0; i < size; i++) {
            if (Objects.equals(current.value, value)) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public List<T> toList() {
        List<T> result = new ArrayList<>(size);
        if (head == null) {
            return result;
        }
        Node<T> current = head;
        for (int i = 0; i < size; i++) {
            result.add(current.value);
            current = current.next;
        }
        return result;
    }

    @Override
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    // ================= privados =================

    private Node<T> nodeAt(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    private void validateExistingIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
    }

    private void validateInsertIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
    }
}