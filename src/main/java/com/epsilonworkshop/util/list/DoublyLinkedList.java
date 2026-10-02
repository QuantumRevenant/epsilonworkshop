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
public final class DoublyLinkedList<T> implements LinkedListADT<T> {

    private static class Node<T> {
        T value;
        Node<T> prev;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;
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
        newNode.next = head;

        if (head != null) {
            head.prev = newNode;
        } else {
            tail = newNode;
        }
        head = newNode;
        size++;
    }

    @Override
    public void insertLast(T value) {
        Node<T> newNode = new Node<>(value);
        newNode.prev = tail;

        if (tail != null) {
            tail.next = newNode;
        } else {
            head = newNode;
        }
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

        Node<T> target = nodeAt(index);

        if (target.prev != null) {
            target.prev.next = target.next;
        } else {
            head = target.next; // era el primero
        }

        if (target.next != null) {
            target.next.prev = target.prev;
        } else {
            tail = target.prev; // era el último
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
        Node<T> current = head;
        int index = 0;
        while (current != null) {
            if (Objects.equals(current.value, value)) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    @Override
    public List<T> toList() {
        List<T> result = new ArrayList<>(size);
        Node<T> current = head;
        while (current != null) {
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

    /**
     * Recorre desde head o desde tail, lo que esté más cerca del índice buscado.
     * Ventaja real de la doble enlazada frente a la simple: en el peor caso
     * (índice cerca del final), evita recorrer toda la lista desde el inicio.
     */
    private Node<T> nodeAt(int index) {
        if (index <= size / 2) {
            Node<T> current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            return current;
        } else {
            Node<T> current = tail;
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
