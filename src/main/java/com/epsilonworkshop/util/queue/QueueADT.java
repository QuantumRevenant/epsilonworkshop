/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.queue;

import com.epsilonworkshop.util.array.SlotInfo;
import java.util.List;

/**
 * Contrato de una cola (FIFO) de capacidad fija.
 *
 * @author sebap
 */
public interface QueueADT<T> {

    /** Encola al final. Lanza IllegalStateException si la cola está llena. */
    void enqueue(T value);

    /** Desencola y devuelve el frente. Lanza NoSuchElementException si está vacía. */
    T dequeue();

    /** Devuelve el frente sin quitarlo. Lanza NoSuchElementException si está vacía. */
    T peek();

    int size();

    int capacity();

    boolean isEmpty();

    boolean isFull();

    void clear();

    /** Elementos desde el frente hasta el final (el orden en que saldrían). */
    List<T> toList();

    /** Las casillas del arreglo completo, marcando frente y final. */
    List<SlotInfo> describeSlots();
}