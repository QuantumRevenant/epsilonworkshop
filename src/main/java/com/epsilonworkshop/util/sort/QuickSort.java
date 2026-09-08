/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.sort;

/**
 *
 * @author sebap
 */
public final class QuickSort {

    private QuickSort() {
    }

    public static <T extends Comparable<? super T>> void sort(T[] array) {
        quickSort(array, 0, array.length - 1);
    }

    private static <T extends Comparable<? super T>> void quickSort(
            T[] array,
            int low,
            int high
    ) {
        if (low >= high) {
            return;
        }

        int pivotIndex = partition(array, low, high);

        quickSort(array, low, pivotIndex - 1);
        quickSort(array, pivotIndex + 1, high);
    }

    private static <T extends Comparable<? super T>> int partition(
            T[] array,
            int low,
            int high
    ) {
        T pivot = array[high];

        int i = low - 1;

        for (int j = low; j < high; j++) {

            if (array[j].compareTo(pivot) <= 0) {
                i++;

                T temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }

        T temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;

        return i + 1;
    }
}
