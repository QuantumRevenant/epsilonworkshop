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
public class MatrixUtilsTest {

    @Test
    void shouldFillMatrix() {
        int[][] matrix = {
            {1, 2},
            {3, 4}
        };

        MatrixUtils.fill(matrix, 7);

        assertArrayEquals(
                new int[][]{
                    {7, 7},
                    {7, 7}
                },
                matrix
        );
    }

    @Test
    void shouldCopyMatrix() {
        int[][] original = {
            {1, 2},
            {3, 4}
        };

        int[][] copy = MatrixUtils.copy(original);

        assertArrayEquals(original, copy);
        assertNotSame(original, copy);
        assertNotSame(original[0], copy[0]);
        assertNotSame(original[1], copy[1]);
    }

    @Test
    void shouldFindValue() {
        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6}
        };

        assertTrue(MatrixUtils.contains(matrix, 5));
        assertFalse(MatrixUtils.contains(matrix, 99));
    }

    @Test
    void shouldReturnPositionOfValue() {
        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6}
        };

        assertArrayEquals(
                new int[]{1, 1},
                MatrixUtils.find(matrix, 5)
        );

        assertNull(MatrixUtils.find(matrix, 99));
    }

    @Test
    void shouldCalculateSum() {
        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6}
        };

        assertEquals(21, MatrixUtils.sum(matrix));
    }

    @Test
    void shouldFindMaximum() {
        int[][] matrix = {
            {8, 3, 15},
            {2, 10, 7}
        };

        assertEquals(15, MatrixUtils.max(matrix));
    }

    @Test
    void shouldFindMinimum() {
        int[][] matrix = {
            {8, 3, 15},
            {2, 10, 7}
        };

        assertEquals(2, MatrixUtils.min(matrix));
    }

    @Test
    void shouldTransposeMatrix() {
        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6}
        };

        int[][] transposed = MatrixUtils.transpose(matrix);

        assertArrayEquals(
                new int[][]{
                    {1, 4},
                    {2, 5},
                    {3, 6}
                },
                transposed
        );
    }

    @Test
    void shouldHandleEmptyMatrix() {
        int[][] matrix = {};

        assertEquals(0, MatrixUtils.sum(matrix));
        assertFalse(MatrixUtils.contains(matrix, 10));
        assertNull(MatrixUtils.find(matrix, 10));
    }

    @Test
    void shouldRejectMaximumOfEmptyMatrix() {
        int[][] matrix = {};

        assertThrows(
                IllegalArgumentException.class,
                () -> MatrixUtils.max(matrix)
        );
    }

    @Test
    void shouldRejectMinimumOfEmptyMatrix() {
        int[][] matrix = {};

        assertThrows(
                IllegalArgumentException.class,
                () -> MatrixUtils.min(matrix)
        );
    }

    @Test
    void shouldRejectTransposeOfEmptyMatrix() {
        int[][] matrix = {};

        assertThrows(
                IllegalArgumentException.class,
                () -> MatrixUtils.transpose(matrix)
        );
    }
}
