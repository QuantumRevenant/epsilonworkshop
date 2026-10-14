package com.epsilonworkshop.util.list;

import com.epsilonworkshop.util.sort.SortAlgorithm;
import java.util.Comparator;
import java.util.List;

/**
 * Ordena cualquier LinkedListADT reutilizando los algoritmos de arreglos del
 * Avance 1 (SortAlgorithm), sin exigir que los elementos sean Comparable: cada
 * valor se envuelve junto con el Comparator recibido. Procedimiento: volcar la
 * lista a un arreglo, ordenarlo con el algoritmo elegido y reinsertar los
 * valores. Costo: el del algoritmo + O(n), porque insertLast es O(1) en las
 * cuatro listas.
 */
public final class ListSorter {

    /**
     * Crea el algoritmo para el tipo que necesite ListSorter. Se usa con
     * referencias a constructor: BubbleSort::new, MergeSort::new, etc.
     */
    @FunctionalInterface
    public interface AlgorithmFactory {

        <E extends Comparable<? super E>> SortAlgorithm<E> create();
    }

    /**
     * Envoltorio que hace Comparable a cualquier valor usando un Comparator.
     */
    private static final class Keyed<T> implements Comparable<Keyed<T>> {

        private final T value;
        private final Comparator<? super T> comparator;

        private Keyed(T value, Comparator<? super T> comparator) {
            this.value = value;
            this.comparator = comparator;
        }

        @Override
        public int compareTo(Keyed<T> other) {
            return comparator.compare(value, other.value);
        }
    }

    private ListSorter() {
    }

    public static <T> void sort(
            LinkedListADT<T> list,
            Comparator<? super T> comparator,
            AlgorithmFactory algorithm
    ) {
        List<T> values = list.toList();

        @SuppressWarnings("unchecked")
        Keyed<T>[] array = (Keyed<T>[]) new Keyed[values.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = new Keyed<>(values.get(i), comparator);
        }

        SortAlgorithm<Keyed<T>> sorter = algorithm.create();
        sorter.sort(array);

        list.clear();
        for (Keyed<T> keyed : array) {
            list.insertLast(keyed.value);
        }
    }
}