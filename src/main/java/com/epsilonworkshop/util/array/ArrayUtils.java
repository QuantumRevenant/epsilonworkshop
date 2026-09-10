package com.epsilonworkshop.util.array;

public final class ArrayUtils {

    private ArrayUtils() {
    }

    // ================= CRUD =================

    /** CREATE: inserta un valor en la posición dada, desplazando el resto. */
    public static int[] insertAt(int[] array, int index, int value) {
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        int[] result = new int[array.length + 1];
        System.arraycopy(array, 0, result, 0, index);
        result[index] = value;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }

    /** READ: lee el valor en una posición. */
    public static int readAt(int[] array, int index) {
        if (index < 0 || index >= array.length) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        return array[index];
    }

    /** UPDATE: reemplaza el valor en una posición (en el mismo arreglo). */
    public static void updateAt(int[] array, int index, int value) {
        if (index < 0 || index >= array.length) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        array[index] = value;
    }

    /** DELETE: elimina el valor en una posición, cerrando el hueco. */
    public static int[] deleteAt(int[] array, int index) {
        if (index < 0 || index >= array.length) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        int[] result = new int[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    // ================= Ya existentes =================

    public static void print(int[] array) {
        for (int value : array) {
            System.out.print(value + " ");
        }
        System.out.println();
    }

    public static void fill(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    public static int[] copy(int[] array) {
        int[] copy = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i];
        }
        return copy;
    }

    public static void reverse(int[] array) {
        int left = 0;
        int right = array.length - 1;
        while (left < right) {
            int temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    public static boolean contains(int[] array, int value) {
        return indexOf(array, value) != -1;
    }

    public static int indexOf(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static int sum(int[] array) {
        int sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }

    public static int max(int[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static int min(int[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }
        int min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }
}