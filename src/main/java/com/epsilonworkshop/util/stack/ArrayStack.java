/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.stack;

import com.epsilonworkshop.util.array.SlotInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Pila estática: arreglo de tamaño fijo y un índice `top` que apunta a la cima.
 *
 * @author sebap
 */
public final class ArrayStack<T> implements StackADT<T> {

    private final T[] items;
    private int top = -1; // índice de la cima; -1 significa pila vacía

    @SuppressWarnings("unchecked")
    public ArrayStack(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que 0");
        }
        this.items = (T[]) new Object[capacity];
    }

    @Override
    public void push(T value) {
        if (isFull()) {
            throw new IllegalStateException(
                    "La pila está llena (capacidad " + items.length + ")");
        }
        items[++top] = value;
    }

    @Override
    public T pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("La pila está vacía");
        }
        T value = items[top];
        items[top--] = null; // suelta la referencia para no retener el objeto
        return value;
    }

    @Override
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("La pila está vacía");
        }
        return items[top];
    }

    @Override
    public int size() {
        return top + 1;
    }

    @Override
    public int capacity() {
        return items.length;
    }

    @Override
    public boolean isEmpty() {
        return top == -1;
    }

    @Override
    public boolean isFull() {
        return top == items.length - 1;
    }

    @Override
    public void clear() {
        Arrays.fill(items, null);
        top = -1;
    }

    @Override
    public List<T> toList() {
        List<T> result = new ArrayList<>(size());
        for (int i = top; i >= 0; i--) {
            result.add(items[i]);
        }
        return result;
    }

    @Override
    public List<SlotInfo> describeSlots() {
        List<SlotInfo> result = new ArrayList<>(items.length);
        for (int i = 0; i < items.length; i++) {
            String value = (i <= top) ? String.valueOf(items[i]) : "—";
            String marker = (i == top) ? "CIMA" : "";
            result.add(new SlotInfo(i, value, marker));
        }
        return result;
    }
}