package com.epsilonworkshop.util.array;

public class MatrixUtils {

    private MatrixUtils() {
    }

    // ================= CRUD =================

    /** CREATE: inserta una fila completa en la posición dada. */
    public static int[][] insertRow(int[][] matrix, int rowIndex, int[] row) {
        if (matrix.length > 0 && row.length != matrix[0].length) {
            throw new IllegalArgumentException(
                    "La fila debe tener " + matrix[0].length + " columnas");
        }
        if (rowIndex < 0 || rowIndex > matrix.length) {
            throw new IndexOutOfBoundsException("Índice de fila fuera de rango: " + rowIndex);
        }
        int[][] result = new int[matrix.length + 1][];
        System.arraycopy(matrix, 0, result, 0, rowIndex);
        result[rowIndex] = row.clone();
        System.arraycopy(matrix, rowIndex, result, rowIndex + 1, matrix.length - rowIndex);
        return result;
    }

    /** CREATE: inserta una columna completa en la posición dada. */
    public static int[][] insertColumn(int[][] matrix, int colIndex, int[] column) {
        validateNotEmpty(matrix);
        if (column.length != matrix.length) {
            throw new IllegalArgumentException(
                    "La columna debe tener " + matrix.length + " filas");
        }
        int[][] result = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            int[] sourceRow = matrix[i];
            if (colIndex < 0 || colIndex > sourceRow.length) {
                throw new IndexOutOfBoundsException(
                        "Índice de columna fuera de rango: " + colIndex);
            }
            int[] newRow = new int[sourceRow.length + 1];
            System.arraycopy(sourceRow, 0, newRow, 0, colIndex);
            newRow[colIndex] = column[i];
            System.arraycopy(sourceRow, colIndex, newRow, colIndex + 1, sourceRow.length - colIndex);
            result[i] = newRow;
        }
        return result;
    }

    /** READ: lee el valor de una celda. */
    public static int readCell(int[][] matrix, int row, int col) {
        validateCell(matrix, row, col);
        return matrix[row][col];
    }

    /** UPDATE: reemplaza el valor de una celda (en la misma matriz). */
    public static void updateCell(int[][] matrix, int row, int col, int value) {
        validateCell(matrix, row, col);
        matrix[row][col] = value;
    }

    /** DELETE: elimina una fila completa. */
    public static int[][] deleteRow(int[][] matrix, int rowIndex) {
        if (rowIndex < 0 || rowIndex >= matrix.length) {
            throw new IndexOutOfBoundsException("Índice de fila fuera de rango: " + rowIndex);
        }
        int[][] result = new int[matrix.length - 1][];
        System.arraycopy(matrix, 0, result, 0, rowIndex);
        System.arraycopy(matrix, rowIndex + 1, result, rowIndex, matrix.length - rowIndex - 1);
        return result;
    }

    /** DELETE: elimina una columna completa. */
    public static int[][] deleteColumn(int[][] matrix, int colIndex) {
        validateNotEmpty(matrix);
        int[][] result = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            int[] sourceRow = matrix[i];
            if (colIndex < 0 || colIndex >= sourceRow.length) {
                throw new IndexOutOfBoundsException(
                        "Índice de columna fuera de rango: " + colIndex);
            }
            int[] newRow = new int[sourceRow.length - 1];
            System.arraycopy(sourceRow, 0, newRow, 0, colIndex);
            System.arraycopy(sourceRow, colIndex + 1, newRow, colIndex, sourceRow.length - colIndex - 1);
            result[i] = newRow;
        }
        return result;
    }

    private static void validateCell(int[][] matrix, int row, int col) {
        if (row < 0 || row >= matrix.length) {
            throw new IndexOutOfBoundsException("Fila fuera de rango: " + row);
        }
        if (col < 0 || col >= matrix[row].length) {
            throw new IndexOutOfBoundsException("Columna fuera de rango: " + col);
        }
    }

    // ================= Ya existentes =================

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
            throw new IllegalArgumentException("Matrix cannot be empty");
        }
    }
}