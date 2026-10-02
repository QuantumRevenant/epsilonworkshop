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
public final class CircularDoublyLinkedList<T> implements LinkedListADT<T> {

    private static class Node<T> {
        T value;
        Node<T> prev;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private int size;
    // No se guarda tail aparte: head.prev ES el tail, siempre -- propiedad
    // directa del círculo doble, no hace falta un campo adicional.

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

        Node<T> current = nodeAt(index);
        Node<T> newNode = new Node<>(value);
        Node<T> previous = current.prev;

        newNode.prev = previous;
        newNode.next = current;
        previous.next = newNode;
        current.prev = newNode;
        size++;
    }

    @Override
    public void insertFirst(T value) {
        Node<T> newNode = new Node<>(value);

        if (head == null) {
            newNode.next = newNode;
            newNode.prev = newNode;
            head = newNode;
        } else {
            Node<T> tail = head.prev;

            newNode.next = head;
            newNode.prev = tail;
            tail.next = newNode;
            head.prev = newNode;
            head = newNode;
        }
        size++;
    }

    @Override
    public void insertLast(T value) {
        if (head == null) {
            insertFirst(value);
            return;
        }

        Node<T> tail = head.prev;
        Node<T> newNode = new Node<>(value);

        newNode.prev = tail;
        newNode.next = head;
        tail.next = newNode;
        head.prev = newNode;
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

        Node<T> target = nodeAt(index);

        if (size == 1) {
            T value = head.value;
            head = null;
            size = 0;
            return value;
        }

        target.prev.next = target.next;
        target.next.prev = target.prev;

        if (target == head) {
            head = target.next;
        }

        size--;
        return target.value;
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
        size = 0;
    }

    // ================= privados =================

    /** Mismo truco de la doble lineal: recorre desde el extremo más cercano. */
    private Node<T> nodeAt(int index) {
        if (index <= size / 2) {
            Node<T> current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current;
        } else {
            Node<T> current = head.prev; // head.prev = tail
            for (int i = size - 1; i > index; i--) {
                current = current.prev;
            }
            return current;
        }
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