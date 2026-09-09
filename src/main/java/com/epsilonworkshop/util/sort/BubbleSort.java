/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.sort;

/**
 *
 * @author sebap
 */
public final class BubbleSort<T extends Comparable<? super T>> implements SortAlgorithm<T> {

    @Override
    public void sort(T[] array) {

        for (int i = 0; i < array.length - 1; i++) {

            boolean swapped = false;

            for (int j = 0; j < array.length - 1 - i; j++) {

                if (array[j].compareTo(array[j + 1]) > 0) {
                    T temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;

                    swapped = true;
                }
            }

            if (!swapped) {
                break;
            }
        }
    }
}
