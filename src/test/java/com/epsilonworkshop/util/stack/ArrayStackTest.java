/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.stack;

import com.epsilonworkshop.util.array.SlotInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sebap
 */
public class ArrayStackTest {

    private ArrayStack<String> stack;

    @BeforeEach
    void setUp() {
        stack = new ArrayStack<>(3);
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        assertTrue(stack.isEmpty());
        assertFalse(stack.isFull());
        assertEquals(0, stack.size());
        assertEquals(3, stack.capacity());
    }

    @Test
    void shouldPopInReverseOrderOfPush() {
        stack.push("A");
        stack.push("B");
        stack.push("C");

        assertEquals("C", stack.pop());
        assertEquals("B", stack.pop());
        assertEquals("A", stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    void shouldPeekWithoutRemoving() {
        stack.push("A");
        stack.push("B");

        assertEquals("B", stack.peek());
        assertEquals(2, stack.size());
    }

    @Test
    void shouldThrowWhenPushingToFullStack() {
        stack.push("A");
        stack.push("B");
        stack.push("C");

        assertTrue(stack.isFull());
        assertThrows(IllegalStateException.class, () -> stack.push("D"));
        assertEquals(3, stack.size()); // el intento fallido no modifica la pila
    }

    @Test
    void shouldThrowWhenPoppingEmptyStack() {
        assertThrows(NoSuchElementException.class, stack::pop);
    }

    @Test
    void shouldThrowWhenPeekingEmptyStack() {
        assertThrows(NoSuchElementException.class, stack::peek);
    }

    @Test
    void shouldAllowPushAfterPoppingFromFullStack() {
        stack.push("A");
        stack.push("B");
        stack.push("C");

        stack.pop();
        stack.push("X");

        assertEquals("X", stack.peek());
        assertTrue(stack.isFull());
    }

    @Test
    void shouldListFromTopToBase() {
        stack.push("A");
        stack.push("B");
        stack.push("C");

        assertEquals(List.of("C", "B", "A"), stack.toList());
    }

    @Test
    void shouldClearStackAndAllowReuse() {
        stack.push("A");
        stack.push("B");

        stack.clear();

        assertTrue(stack.isEmpty());
        stack.push("Z");
        assertEquals("Z", stack.peek());
    }

    @Test
    void shouldRejectInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new ArrayStack<String>(0));
        assertThrows(IllegalArgumentException.class, () -> new ArrayStack<String>(-1));
    }

    @Test
    void shouldMarkTopInSlots() {
        stack.push("A");
        stack.push("B");

        List<SlotInfo> slots = stack.describeSlots();

        assertEquals(3, slots.size());
        assertEquals("", slots.get(0).marker());
        assertEquals("CIMA", slots.get(1).marker());
        assertEquals("B", slots.get(1).value());
        assertEquals("—", slots.get(2).value()); // casilla libre
    }

    @Test
    void shouldDescribeEmptyStackWithoutTopMarker() {
        for (SlotInfo slot : stack.describeSlots()) {
            assertEquals("", slot.marker());
            assertEquals("—", slot.value());
        }
    }
}
