/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.queue;

import com.epsilonworkshop.util.array.SlotInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sebap
 */
public class ArrayQueueTest {

    private ArrayQueue<String> queue;

    @BeforeEach
    void setUp() {
        queue = new ArrayQueue<>(3);
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        assertTrue(queue.isEmpty());
        assertFalse(queue.isFull());
        assertEquals(0, queue.size());
        assertEquals(3, queue.capacity());
    }

    @Test
    void shouldDequeueInFifoOrder() {
        queue.enqueue("A");
        queue.enqueue("B");
        queue.enqueue("C");

        assertEquals("A", queue.dequeue());
        assertEquals("B", queue.dequeue());
        assertEquals("C", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    void shouldPeekFrontWithoutRemoving() {
        queue.enqueue("A");
        queue.enqueue("B");

        assertEquals("A", queue.peek());
        assertEquals(2, queue.size());
    }

    @Test
    void shouldThrowWhenEnqueueingToFullQueue() {
        queue.enqueue("A");
        queue.enqueue("B");
        queue.enqueue("C");

        assertTrue(queue.isFull());
        assertThrows(IllegalStateException.class, () -> queue.enqueue("D"));
        assertEquals(3, queue.size());
    }

    @Test
    void shouldThrowWhenDequeueingEmptyQueue() {
        assertThrows(NoSuchElementException.class, queue::dequeue);
    }

    @Test
    void shouldThrowWhenPeekingEmptyQueue() {
        assertThrows(NoSuchElementException.class, queue::peek);
    }

    @Test
    void shouldWrapAroundWhenRearReachesEndOfArray() {
        queue.enqueue("A");
        queue.enqueue("B");
        queue.enqueue("C");
        queue.dequeue(); // sale A
        queue.dequeue(); // sale B
        queue.enqueue("D"); // vuelve al índice 0
        queue.enqueue("E"); // índice 1

        // Arreglo interno: [D, E, C], frente en 2, final en 1.
        assertTrue(queue.isFull());
        assertEquals(List.of("C", "D", "E"), queue.toList());

        List<SlotInfo> slots = queue.describeSlots();
        assertEquals("", slots.get(0).marker());
        assertEquals("FINAL", slots.get(1).marker());
        assertEquals("FRENTE", slots.get(2).marker());
    }

    @Test
    void shouldMarkSingleElementAsFrontAndRear() {
        queue.enqueue("A");

        assertEquals("FRENTE/FINAL", queue.describeSlots().get(0).marker());
    }

    @Test
    void shouldKeepWorkingAfterManyCycles() {
        for (int i = 0; i < 10; i++) {
            queue.enqueue("x" + i);
            assertEquals("x" + i, queue.dequeue());
        }

        assertTrue(queue.isEmpty());
    }

    @Test
    void shouldWorkWithCapacityOne() {
        ArrayQueue<String> single = new ArrayQueue<>(1);

        single.enqueue("A");
        assertTrue(single.isFull());
        assertThrows(IllegalStateException.class, () -> single.enqueue("B"));

        assertEquals("A", single.dequeue());
        single.enqueue("B");
        assertEquals("B", single.peek());
    }

    @Test
    void shouldClearQueueAndAllowReuse() {
        queue.enqueue("A");
        queue.enqueue("B");

        queue.clear();

        assertTrue(queue.isEmpty());
        queue.enqueue("Z");
        assertEquals(List.of("Z"), queue.toList());
    }

    @Test
    void shouldRejectInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new ArrayQueue<String>(0));
        assertThrows(IllegalArgumentException.class, () -> new ArrayQueue<String>(-1));
    }
}
