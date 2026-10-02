/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.epsilonworkshop.util.list;

import java.util.List;

/**
 *
 * @author sebap
 */
public interface LinkedListADT<T> {
    
    // ================= CRUD =================

    void insertAt(int index, T value);

    void insertFirst(T value);

    void insertLast(T value);

    T readAt(int index);

    void updateAt(int index, T value);

    T deleteAt(int index);

    // ================= Utilidades =================

    int size();

    boolean isEmpty();

    boolean contains(T value);

    int indexOf(T value);

    /** Recorre la lista y la vuelca a un List<T> estándar, para pintar en JTable. */
    List<T> toList();

    void clear();
}