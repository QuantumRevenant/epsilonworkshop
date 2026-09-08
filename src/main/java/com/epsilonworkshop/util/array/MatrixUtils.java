/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.util.array;

/**
 *
 * @author sebap
 */
public class MatrixUtils {

    private MatrixUtils() {
    }

    public static void print(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }

            System.out.println();
        }
    }

    public static void fill(int[][] matrix, int value) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = value;
            }
        }
    }

    public static int[][] copy(int[][] matrix) {
        int[][] copy = new int[matrix.length][];

        for (int i = 0; i < matrix.length; i++) {
            copy[i] = new int[matrix[i].length];

            for (int j = 0; j < matrix[i].length; j++) {
                copy[i][j] = matrix[i][j];
            }
        }

        return copy;
    }

    public static boolean contains(int[][] matrix, int value) {
        return find(matrix, value) != null;
    }

    public static int[] find(int[][] matrix, int value) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == value) {
                    return new int[]{i, j};
                }
            }
        }

        return null;
    }

    public static int sum(int[][] matrix) {
        int sum = 0;

        for (int[] row : matrix) {
            for (int value : row) {
                sum += value;
            }
        }

        return sum;
    }

    public static int max(int[][] matrix) {
        validateNotEmpty(matrix);

        int max = matrix[0][0];

        for (int[] row : matrix) {
            for (int value : row) {
                if (value > max) {
                    max = value;
                }
            }
        }

        return max;
    }

    public static int min(int[][] matrix) {
        validateNotEmpty(matrix);

        int min = matrix[0][0];

        for (int[] row : matrix) {
            for (int value : row) {
                if (value < min) {
                    min = value;
                }
            }
        }

        return min;
    }

    public static int[][] transpose(int[][] matrix) {
        validateNotEmpty(matrix);

        int rows = matrix.length;
        int columns = matrix[0].length;

        int[][] transposed = new int[columns][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }

        return transposed;
    }

    private static void validateNotEmpty(int[][] matrix) {
        if (matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException(
                    "Matrix cannot be empty"
            );
        }
    }
}
