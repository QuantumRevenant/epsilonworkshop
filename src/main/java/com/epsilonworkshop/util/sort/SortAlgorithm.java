/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.sort;

/**
 *
 * @author sebap
 */
@FunctionalInterface
public interface SortAlgorithm<T extends Comparable<? super T>> {
    void sort(T[] array);
}
