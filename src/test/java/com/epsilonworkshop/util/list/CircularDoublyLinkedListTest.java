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
public class CircularDoublyLinkedListTest {

    private CircularDoublyLinkedList<String> list;

    @BeforeEach
    void setUp() {
        list = new CircularDoublyLinkedList<>();
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
    void shouldTreatHeadPrevAsTailCorrectly() {
        list.insertLast("A");
        list.insertLast("B");
        list.insertLast("C");

        // Inserta al final usando la lógica que depende de head.prev == tail.
        // Si esa propiedad se rompiera, el nuevo nodo no quedaría bien ubicado.
        list.insertLast("D");

        assertEquals(java.util.List.of("A", "B", "C", "D"), list.toList());
    }

    @Test
    void shouldDeleteHeadAndReassignCorrectly() {
        list.insertLast("A");
        list.insertLast("B");
        list.insertLast("C");

        String deleted = list.deleteAt(0);

        assertEquals("A", deleted);
        assertEquals(java.util.List.of("B", "C"), list.toList());

        // Confirma que el nuevo head también cerró bien el círculo hacia atrás
        list.insertFirst("Z");
        assertEquals(java.util.List.of("Z", "B", "C"), list.toList());
    }

    @Test
    void shouldDeleteOnlyNodeAndAllowReinsertion() {
        list.insertFirst("A");
        list.deleteAt(0);

        assertTrue(list.isEmpty());

        list.insertLast("B");
        assertEquals(java.util.List.of("B"), list.toList());
    }

    @Test
    void shouldReadFromTailSideForHighIndex() {
        for (int i = 0; i < 10; i++) {
            list.insertLast("item" + i);
        }

        assertEquals("item8", list.readAt(8));
    }

    @Test
    void shouldNotLoopForeverWhenTraversingFullCircle() {
        for (int i = 0; i < 20; i++) {
            list.insertLast("item" + i);
        }

        assertEquals(20, list.toList().size());
    }

    @Test
    void shouldThrowOnInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.readAt(0));
    }
}
