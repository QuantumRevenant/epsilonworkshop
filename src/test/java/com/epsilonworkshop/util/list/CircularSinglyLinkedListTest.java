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
public class CircularSinglyLinkedListTest {

    private CircularSinglyLinkedList<String> list;

    @BeforeEach
    void setUp() {
        list = new CircularSinglyLinkedList<>();
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        assertTrue(list.isEmpty());
    }

    @Test
    void shouldCloseCircleWithSingleNode() {
        list.insertFirst("A");

        // Si el círculo no se cerró, toList() con el límite por size
        // igual daría ["A"], así que confirmamos de otra forma:
        // insertamos un segundo y tercer elemento y verificamos el orden,
        // lo cual solo funciona si el primer insertLast encontró bien el tail.
        list.insertLast("B");
        list.insertLast("C");

        assertEquals(java.util.List.of("A", "B", "C"), list.toList());
    }

    @Test
    void shouldInsertFirstAndLastCorrectly() {
        list.insertLast("B");
        list.insertFirst("A");
        list.insertLast("C");

        assertEquals(java.util.List.of("A", "B", "C"), list.toList());
    }

    @Test
    void shouldDeleteFirstAndKeepCircleClosed() {
        list.insertLast("A");
        list.insertLast("B");
        list.insertLast("C");

        list.deleteAt(0);
        list.insertLast("D"); // ejercita tail -- si el círculo se rompió, falla o cuelga

        assertEquals(java.util.List.of("B", "C", "D"), list.toList());
    }

    @Test
    void shouldDeleteLastAndKeepCircleClosed() {
        list.insertLast("A");
        list.insertLast("B");
        list.insertLast("C");

        list.deleteAt(2); // elimina "C", que era tail
        list.insertLast("D");

        assertEquals(java.util.List.of("A", "B", "D"), list.toList());
    }

    @Test
    void shouldDeleteOnlyNodeAndAllowReinsertion() {
        list.insertFirst("A");
        list.deleteAt(0);

        assertTrue(list.isEmpty());

        list.insertFirst("B");
        assertEquals(java.util.List.of("B"), list.toList());
    }

    @Test
    void shouldNotLoopForeverWhenTraversingFullCircle() {
        for (int i = 0; i < 20; i++) {
            list.insertLast("item" + i);
        }

        // Si toList() usara "while (current != null)" por error, esto nunca
        // terminaría. El test en sí, al completarse, demuestra que el
        // límite por size está funcionando -- no hace falta un timeout extra.
        assertEquals(20, list.toList().size());
    }

    @Test
    void shouldThrowOnInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.readAt(0));
    }
}