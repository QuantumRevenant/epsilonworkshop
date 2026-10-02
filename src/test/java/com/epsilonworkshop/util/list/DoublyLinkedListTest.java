/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sebap
 */
public class DoublyLinkedListTest {

    private DoublyLinkedList<String> list;

    @BeforeEach
    void setUp() {
        list = new DoublyLinkedList<>();
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        assertTrue(list.isEmpty());
    }

    @Test
    void shouldInsertFirstAndLastCorrectly() {
        list.insertLast("B");
        list.insertFirst("A");
        list.insertLast("C");

        assertEquals(java.util.List.of("A", "B", "C"), list.toList());
    }

    @Test
    void shouldInsertAtMiddleIndex() {
        list.insertLast("A");
        list.insertLast("C");
        list.insertAt(1, "B");

        assertEquals(java.util.List.of("A", "B", "C"), list.toList());
    }

    @Test
    void shouldDeleteFirstAndUpdateHead() {
        list.insertLast("A");
        list.insertLast("B");

        String deleted = list.deleteAt(0);

        assertEquals("A", deleted);
        list.insertFirst("Z"); // si head no quedó bien enlazado, esto falla
        assertEquals(java.util.List.of("Z", "B"), list.toList());
    }

    @Test
    void shouldDeleteLastAndUpdateTail() {
        list.insertLast("A");
        list.insertLast("B");

        list.deleteAt(1);
        list.insertLast("C"); // si tail no quedó bien enlazado, esto falla

        assertEquals(java.util.List.of("A", "C"), list.toList());
    }

    @Test
    void shouldDeleteMiddleAndKeepBothLinksConsistent() {
        list.insertLast("A");
        list.insertLast("B");
        list.insertLast("C");

        list.deleteAt(1);

        // Verifica consistencia en ambas direcciones, no solo toList() (que solo recorre next)
        assertEquals("A", list.readAt(0));
        assertEquals("C", list.readAt(1));
        assertEquals(2, list.size());
    }

    @Test
    void shouldDeleteUntilEmptyAndAllowReinsertion() {
        list.insertLast("A");
        list.deleteAt(0);

        assertTrue(list.isEmpty());

        list.insertLast("B");
        assertEquals(java.util.List.of("B"), list.toList());
    }

    @Test
    void shouldReadFromTailSideEfficientlyForHighIndex() {
        for (int i = 0; i < 10; i++) {
            list.insertLast("item" + i);
        }

        // No podemos medir "eficiencia" directamente en un test unitario,
        // pero sí confirmamos que el resultado es correcto para un índice
        // cercano al final, que es el camino que ejercita la rama "desde tail".
        assertEquals("item8", list.readAt(8));
    }

    @Test
    void shouldThrowWhenAccessingInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.readAt(0));
        list.insertLast("A");
        assertThrows(IndexOutOfBoundsException.class, () -> list.readAt(5));
    }
}