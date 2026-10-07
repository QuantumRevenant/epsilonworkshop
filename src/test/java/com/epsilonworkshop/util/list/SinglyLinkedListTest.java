/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.list;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sebap
 */
public class SinglyLinkedListTest {

    private SinglyLinkedList<String> list;

    @BeforeEach
    void setUp() {
        list = new SinglyLinkedList<>();
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void shouldInsertFirstCorrectly() {
        list.insertLast("B");
        list.insertFirst("A");

        assertEquals("A", list.readAt(0));
        assertEquals("B", list.readAt(1));
        assertEquals(2, list.size());
    }

    @Test
    void shouldInsertLastCorrectly() {
        list.insertLast("A");
        list.insertLast("B");
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
    void shouldThrowWhenInsertingAtInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.insertAt(5, "X"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.insertAt(-1, "X"));
    }

    @Test
    void shouldUpdateAtGivenIndex() {
        list.insertLast("A");
        list.insertLast("B");

        list.updateAt(1, "Z");

        assertEquals("Z", list.readAt(1));
    }

    @Test
    void shouldDeleteFirstElement() {
        list.insertLast("A");
        list.insertLast("B");
        list.insertLast("C");

        String deleted = list.deleteAt(0);

        assertEquals("A", deleted);
        assertEquals(java.util.List.of("B", "C"), list.toList());
    }

    @Test
    void shouldDeleteLastElementAndUpdateTail() {
        list.insertLast("A");
        list.insertLast("B");

        list.deleteAt(1);
        list.insertLast("C"); // si tail no se actualizó bien, esto fallaría

        assertEquals(java.util.List.of("A", "C"), list.toList());
    }

    @Test
    void shouldDeleteMiddleElement() {
        list.insertLast("A");
        list.insertLast("B");
        list.insertLast("C");

        String deleted = list.deleteAt(1);

        assertEquals("B", deleted);
        assertEquals(java.util.List.of("A", "C"), list.toList());
    }

    @Test
    void shouldReturnCorrectIndexOf() {
        list.insertLast("A");
        list.insertLast("B");

        assertEquals(1, list.indexOf("B"));
        assertEquals(-1, list.indexOf("Z"));
    }

    @Test
    void shouldClearList() {
        list.insertLast("A");
        list.insertLast("B");

        list.clear();

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    void shouldHandleDeleteUntilEmptyWithoutBreakingTail() {
        list.insertLast("A");
        list.deleteAt(0);

        assertTrue(list.isEmpty());

        // Si head/tail no quedaron null correctamente, esto fallaría o insertaría mal
        list.insertLast("B");
        assertEquals(java.util.List.of("B"), list.toList());
    }

    @Test
    void shouldDescribeNodesWithNullAtTailAndNoAccessToPrevious() {
        list.insertLast("A");
        list.insertLast("B");
        list.insertLast("C");

        List<NodeInfo> nodes = list.describeNodes();

        assertEquals(3, nodes.size());
        assertEquals("—", nodes.get(2).nextRef()); // el último no tiene siguiente
        assertTrue(nodes.get(1).previousRef().contains("No aplica"));
        assertEquals("Nodo[2]", nodes.get(1).nextRef()); // el del medio sí referencia al siguiente real
    }
}
