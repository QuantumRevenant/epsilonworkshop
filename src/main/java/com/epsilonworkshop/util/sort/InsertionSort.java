/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.sort;

/**
 *
 * @author sebap
 */
public final class InsertionSort {

    private InsertionSort() {
    }

    public static <T extends Comparable<? super T>> void sort(T[] array) {

        for (int i = 1; i < array.length; i++) {

            T current = array[i];
            int j = i - 1;

            while (j >= 0 && array[j].compareTo(current) > 0) {
                array[j + 1] = array[j];
                j--;
            }

            array[j + 1] = current;
        }
    }
}
