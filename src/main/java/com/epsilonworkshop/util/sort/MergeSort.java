/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.sort;

/**
 *
 * @author sebap
 */
public final class MergeSort {

    private MergeSort() {
    }

    public static <T extends Comparable<? super T>> void sort(T[] array) {
        if (array.length < 2) {
            return;
        }

        T[] temp = array.clone();

        mergeSort(array, temp, 0, array.length - 1);
    }

    private static <T extends Comparable<? super T>> void mergeSort(
            T[] array,
            T[] temp,
            int left,
            int right
    ) {
        if (left >= right) {
            return;
        }

        int middle = left + (right - left) / 2;

        mergeSort(array, temp, left, middle);
        mergeSort(array, temp, middle + 1, right);

        merge(array, temp, left, middle, right);
    }

    private static <T extends Comparable<? super T>> void merge(
            T[] array,
            T[] temp,
            int left,
            int middle,
            int right
    ) {
        for (int i = left; i <= right; i++) {
            temp[i] = array[i];
        }

        int i = left;
        int j = middle + 1;
        int k = left;

        while (i <= middle && j <= right) {

            if (temp[i].compareTo(temp[j]) <= 0) {
                array[k++] = temp[i++];
            } else {
                array[k++] = temp[j++];
            }
        }

        while (i <= middle) {
            array[k++] = temp[i++];
        }

        while (j <= right) {
            array[k++] = temp[j++];
        }
    }
}
