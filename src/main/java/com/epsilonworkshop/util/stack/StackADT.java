/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.stack;
 
import com.epsilonworkshop.util.array.SlotInfo;
import java.util.List;
 
/**
 * Contrato de una pila (LIFO) de capacidad fija.
 *
 * @author sebap
 */
public interface StackADT<T> {
 
    /** Apila un elemento. Lanza IllegalStateException si la pila está llena. */
    void push(T value);
 
    /** Desapila y devuelve la cima. Lanza NoSuchElementException si está vacía. */
    T pop();
 
    /** Devuelve la cima sin quitarla. Lanza NoSuchElementException si está vacía. */
    T peek();
 
    int size();
 
    int capacity();
 
    boolean isEmpty();
 
    boolean isFull();
 
    void clear();
 
    /** Elementos desde la cima hasta la base (el orden en que saldrían). */
    List<T> toList();
 
    /** Las casillas del arreglo completo, marcando dónde está la cima. */
    List<SlotInfo> describeSlots();
}
