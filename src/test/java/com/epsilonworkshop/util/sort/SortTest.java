/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.sort;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 *
 * @author sebap
 */
public class SortTest {

    private final SortAlgorithm<Integer>[] integerSorts = new SortAlgorithm[]{
        new BubbleSort<>(),
        new InsertionSort<>(),
        new SelectionSort<>(),
        new MergeSort<>(),
        new ShellSort<>(),
        new QuickSort<>()
    };

    @Test
    void shouldSortUnorderedArray() {
        Integer[] expected = {
            1, 2, 3, 4, 5, 6, 7, 8, 9
        };

        for (SortAlgorithm<Integer> sort : integerSorts) {
            Integer[] array = {
                5, 2, 9, 1, 7, 3, 8, 6, 4
            };

            sort.sort(array);

            assertArrayEquals(expected, array);
        }
    }

    @Test
    void shouldSortArrayWithDuplicates() {
        Integer[] expected = {
            1, 2, 2, 3, 3, 4, 5, 5
        };

        for (SortAlgorithm<Integer> sort : integerSorts) {
            Integer[] array = {
                3, 5, 2, 5, 1, 4, 3, 2
            };

            sort.sort(array);

            assertArrayEquals(expected, array);
        }
    }

    @Test
    void shouldKeepSortedArray() {
        Integer[] expected = {
            1, 2, 3, 4, 5
        };

        for (SortAlgorithm<Integer> sort : integerSorts) {
            Integer[] array = {
                1, 2, 3, 4, 5
            };

            sort.sort(array);

            assertArrayEquals(expected, array);
        }
    }

    @Test
    void shouldSortReverseOrderedArray() {
        Integer[] expected = {
            1, 2, 3, 4, 5
        };

        for (SortAlgorithm<Integer> sort : integerSorts) {
            Integer[] array = {
                5, 4, 3, 2, 1
            };

            sort.sort(array);

            assertArrayEquals(expected, array);
        }
    }

    @Test
    void shouldSortSingleElementArray() {
        Integer[] expected = {42};

        for (SortAlgorithm<Integer> sort : integerSorts) {
            Integer[] array = {42};

            sort.sort(array);

            assertArrayEquals(expected, array);
        }
    }

    @Test
    void shouldHandleEmptyArray() {
        Integer[] expected = {};

        for (SortAlgorithm<Integer> sort : integerSorts) {
            Integer[] array = {};

            sort.sort(array);

            assertArrayEquals(expected, array);
        }
    }

    @Test
    void shouldSortStrings() {

        SortAlgorithm<String>[] stringSorts = new SortAlgorithm[]{
            new BubbleSort<>(),
            new InsertionSort<>(),
            new SelectionSort<>(),
            new MergeSort<>(),
            new ShellSort<>(),
            new QuickSort<>()
        };

        String[] expected = {
            "Ana",
            "Carlos",
            "Ernesto",
            "Luis"
        };

        for (SortAlgorithm<String> sort : stringSorts) {
            String[] array = {
                "Luis",
                "Ernesto",
                "Ana",
                "Carlos"
            };

            sort.sort(array);

            assertArrayEquals(expected, array);
        }
    }
}
