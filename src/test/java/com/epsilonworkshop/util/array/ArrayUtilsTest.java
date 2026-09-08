/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.array;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sebap
 */
public class ArrayUtilsTest {

    @Test
    void shouldFillArray() {
        int[] array = {1, 2, 3, 4, 5};

        ArrayUtils.fill(array, 7);

        assertArrayEquals(
                new int[]{7, 7, 7, 7, 7},
                array
        );
    }

    @Test
    void shouldCopyArray() {
        int[] original = {1, 2, 3, 4, 5};

        int[] copy = ArrayUtils.copy(original);

        assertArrayEquals(original, copy);
        assertNotSame(original, copy);
    }

    @Test
    void shouldReverseArray() {
        int[] array = {1, 2, 3, 4, 5};

        ArrayUtils.reverse(array);

        assertArrayEquals(
                new int[]{5, 4, 3, 2, 1},
                array
        );
    }

    @Test
    void shouldFindValue() {
        int[] array = {10, 20, 30, 40};

        assertTrue(ArrayUtils.contains(array, 30));
        assertFalse(ArrayUtils.contains(array, 99));
    }

    @Test
    void shouldReturnIndexOfValue() {
        int[] array = {10, 20, 30, 40};

        assertEquals(2, ArrayUtils.indexOf(array, 30));
        assertEquals(-1, ArrayUtils.indexOf(array, 99));
    }

    @Test
    void shouldCalculateSum() {
        int[] array = {1, 2, 3, 4, 5};

        assertEquals(15, ArrayUtils.sum(array));
    }

    @Test
    void shouldFindMaximum() {
        int[] array = {8, 3, 15, 2, 10};

        assertEquals(15, ArrayUtils.max(array));
    }

    @Test
    void shouldFindMinimum() {
        int[] array = {8, 3, 15, 2, 10};

        assertEquals(2, ArrayUtils.min(array));
    }

    @Test
    void shouldHandleEmptyArray() {
        int[] array = {};

        assertEquals(0, ArrayUtils.sum(array));
        assertFalse(ArrayUtils.contains(array, 10));
        assertEquals(-1, ArrayUtils.indexOf(array, 10));
    }

    @Test
    void shouldRejectMaximumOfEmptyArray() {
        int[] array = {};

        assertThrows(
                IllegalArgumentException.class,
                () -> ArrayUtils.max(array)
        );
    }

    @Test
    void shouldRejectMinimumOfEmptyArray() {
        int[] array = {};

        assertThrows(
                IllegalArgumentException.class,
                () -> ArrayUtils.min(array)
        );
    }

    @Test
    void shouldReverseSingleElementArray() {
        int[] array = {42};

        ArrayUtils.reverse(array);

        assertArrayEquals(
                new int[]{42},
                array
        );
    }
}
