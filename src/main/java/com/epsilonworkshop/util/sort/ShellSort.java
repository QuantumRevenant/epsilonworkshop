/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.sort;

/**
 *
 * @author sebap
 */
public final class ShellSort <T extends Comparable<? super T>> implements SortAlgorithm<T> {

    @Override
    public void sort(T[] array) {

        for (int gap = array.length / 2; gap > 0; gap /= 2) {

            for (int i = gap; i < array.length; i++) {

                T current = array[i];
                int j = i;

                while (j >= gap
                        && array[j - gap].compareTo(current) > 0) {

                    array[j] = array[j - gap];
                    j -= gap;
                }

                array[j] = current;
            }
        }
    }
}
