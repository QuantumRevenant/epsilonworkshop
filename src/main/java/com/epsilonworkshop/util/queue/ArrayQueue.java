/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.queue;

import com.epsilonworkshop.util.array.SlotInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Cola estática circular: arreglo de tamaño fijo, un índice `front` y un
 * contador `size`. El final se deduce: (front + size - 1) % capacidad. Al
 * llegar al último índice del arreglo, el siguiente elemento vuelve al 0, así
 * se reutilizan los huecos que dejan los desencolados.
 *
 * @author sebap
 */
public final class ArrayQueue<T> implements QueueADT<T> {

    private final T[] items;
    private int front = 0;
    private int size = 0;

    @SuppressWarnings("unchecked")
    public ArrayQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que 0");
        }
        this.items = (T[]) new Object[capacity];
    }

    @Override
    public void enqueue(T value) {
        if (isFull()) {
            throw new IllegalStateException(
                    "La cola está llena (capacidad " + items.length + ")");
        }
        int slot = (front + size) % items.length;
        items[slot] = value;
        size++;
    }

    @Override
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        T value = items[front];
        items[front] = null; // suelta la referencia
        front = (front + 1) % items.length;
        size--;
        return value;
    }

    @Override
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        return items[front];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int capacity() {
        return items.length;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean isFull() {
        return size == items.length;
    }

    @Override
    public void clear() {
        Arrays.fill(items, null);
        front = 0;
        size = 0;
    }

    @Override
    public List<T> toList() {
        List<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(items[(front + i) % items.length]);
        }
        return result;
    }

    @Override
    public List<SlotInfo> describeSlots() {
        List<SlotInfo> result = new ArrayList<>(items.length);
        int rear = (front + size - 1 + items.length) % items.length;

        for (int i = 0; i < items.length; i++) {
            boolean occupied = size > 0
                    && ((i - front + items.length) % items.length) < size;

            String marker = "";
            if (occupied) {
                boolean isFront = (i == front);
                boolean isRear = (i == rear);
                if (isFront && isRear) {
                    marker = "FRENTE/FINAL";
                } else if (isFront) {
                    marker = "FRENTE";
                } else if (isRear) {
                    marker = "FINAL";
                }
            }

            result.add(new SlotInfo(i, occupied ? String.valueOf(items[i]) : "—", marker));
        }
        return result;
    }
}