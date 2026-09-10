package com.epsilonworkshop.view;

import com.epsilonworkshop.dao.WorkOrderDAO;
import com.epsilonworkshop.model.OrderStatus;
import com.epsilonworkshop.model.WorkOrder;
import com.epsilonworkshop.util.array.ArrayUtils;
import com.epsilonworkshop.util.array.MatrixUtils;
import com.epsilonworkshop.util.sort.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;

/**
 * Panel de demostración académica: aplica los algoritmos de ordenamiento,
 * arreglos unidimensionales y arreglos matriciales sobre datos reales de
 * work_orders, además de un módulo CRUD manual sobre arreglos y matrices
 * independiente de la BD.
 *
 * Crear como "New Java Class" normal en NetBeans (NO como "JPanel Form"), ya
 * que este código Swing es plano y no sigue el formato que exige el editor
 * visual Matisse.
 */
public class AlgorithmsPanel extends JPanel {

    private final WorkOrderDAO workOrderDAO = new WorkOrderDAO();
    private List<WorkOrder> workOrders = new ArrayList<>();

    private final Map<String, SortAlgorithm<WorkOrder>> algorithms = new LinkedHashMap<>();

    private static final DateTimeFormatter DATE_FMT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // --- Tab 1: Ordenamiento ---
    private JComboBox<String> algorithmSelector;
    private DefaultTableModel beforeTableModel;
    private DefaultTableModel afterTableModel;
    private JLabel metricsLabel;
    private WorkOrder[] currentArray = new WorkOrder[0];

    // --- Tab 2: Arreglo 1D (estadísticas sobre datos reales) ---
    private JTextArea daysArrayArea;
    private JLabel sumLabel, maxLabel, minLabel;
    private JTextField searchField;
    private JLabel searchResultLabel;
    private int[] lastDaysArray = new int[0];

    // --- Tab 3: CRUD Arreglo 1D (manual, independiente de la BD) ---
    private int[] workingArray = new int[0];
    private JLabel arrayCrudDisplay;
    private JLabel arrayCrudResultLabel;
    private JTextField arrayIndexField;
    private JTextField arrayValueField;
    private JTextField arraySizeField;
    private JTextField arrayFillValueField;

    // --- Tab 4: Matriz 2D (reporte real sobre datos de la BD) ---
    private DefaultTableModel matrixTableModel;
    private JLabel matrixSumLabel, matrixMaxLabel, matrixMinLabel;
    private int[][] lastMatrix = new int[0][0];
    private List<Long> lastMechanicIds = new ArrayList<>();

    // --- Tab 5: CRUD Matriz 2D (manual, independiente de la BD) ---
    private int[][] workingMatrix = new int[0][0];
    private DefaultTableModel matrixCrudTableModel;
    private JLabel matrixCrudResultLabel;
    private JTextField matrixRowIndexField;
    private JTextField matrixColIndexField;
    private JTextField matrixValuesField;
    private JTextField matrixCellValueField;
    private JTextField matrixRowsField;
    private JTextField matrixColsField;
    private JTextField matrixFillValueField;

    public AlgorithmsPanel() {
        algorithms.put("Bubble Sort", new BubbleSort<>());
        algorithms.put("Insertion Sort", new InsertionSort<>());
        algorithms.put("Merge Sort", new MergeSort<>());
        algorithms.put("Quick Sort", new QuickSort<>());
        algorithms.put("Selection Sort", new SelectionSort<>());
        algorithms.put("Shell Sort", new ShellSort<>());

        setLayout(new BorderLayout());

        JButton reloadButton = new JButton("Cargar datos de la BD");
        reloadButton.addActionListener(e -> loadData());
        add(reloadButton, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ordenamiento", buildSortTab());
        tabs.addTab("Arreglo 1D (BD)", buildArray1DTab());
        tabs.addTab("CRUD Arreglo 1D", buildArrayCrudTab());
        tabs.addTab("Matriz 2D (BD)", buildMatrixTab());
        tabs.addTab("CRUD Matriz 2D", buildMatrixCrudTab());
        add(tabs, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        try {
            workOrders = workOrderDAO.findAll();
            JOptionPane.showMessageDialog(this,
                    workOrders.size() + " órdenes de trabajo cargadas.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error cargando datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========================================================
    // TAB 1: ORDENAMIENTO
    // ========================================================
    private JPanel buildSortTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel();
        algorithmSelector = new JComboBox<>(algorithms.keySet().toArray(new String[0]));

        JButton shuffleButton = new JButton("1. Desordenar");
        shuffleButton.addActionListener(e -> shuffleAndShowBefore());

        JButton sortButton = new JButton("2. Ordenar");
        sortButton.addActionListener(e -> runSort());

        JButton compareAllButton = new JButton("Comparar los 6 algoritmos");
        compareAllButton.addActionListener(e -> compareAllAlgorithms());

        top.add(shuffleButton);
        top.add(new JLabel("Algoritmo:"));
        top.add(algorithmSelector);
        top.add(sortButton);
        top.add(compareAllButton);
        panel.add(top, BorderLayout.NORTH);

        beforeTableModel = createSortTableModel();
        afterTableModel = createSortTableModel();

        JPanel beforePanel = new JPanel(new BorderLayout());
        beforePanel.add(new JLabel("ANTES", SwingConstants.CENTER), BorderLayout.NORTH);
        beforePanel.add(new JScrollPane(new JTable(beforeTableModel)), BorderLayout.CENTER);

        JPanel afterPanel = new JPanel(new BorderLayout());
        afterPanel.add(new JLabel("DESPUÉS", SwingConstants.CENTER), BorderLayout.NORTH);
        afterPanel.add(new JScrollPane(new JTable(afterTableModel)), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, beforePanel, afterPanel);
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);

        metricsLabel = new JLabel("Presiona \"Desordenar\" y luego \"Ordenar\" para comparar.");
        panel.add(metricsLabel, BorderLayout.SOUTH);

        return panel;
    }

    private DefaultTableModel createSortTableModel() {
        return new DefaultTableModel(
                new Object[]{"ID", "Estado", "Creado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private void shuffleAndShowBefore() {
        if (workOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Carga datos primero.");
            return;
        }
        List<WorkOrder> shuffled = new ArrayList<>(workOrders);
        Collections.shuffle(shuffled);
        currentArray = shuffled.toArray(new WorkOrder[0]);

        fillSortTable(beforeTableModel, currentArray);
        afterTableModel.setRowCount(0);
        metricsLabel.setText("Datos desordenados. Ahora presiona \"Ordenar\".");
    }

    private void runSort() {
        if (currentArray.length == 0) {
            JOptionPane.showMessageDialog(this, "Presiona \"Desordenar\" primero.");
            return;
        }

        WorkOrder[] arrayToSort = Arrays.copyOf(currentArray, currentArray.length);

        WorkOrder.resetComparisonCount();
        long start = System.nanoTime();

        String selected = (String) algorithmSelector.getSelectedItem();
        algorithms.get(selected).sort(arrayToSort);

        long elapsedNanos = System.nanoTime() - start;
        double elapsedMicros = elapsedNanos / 1_000.0;
        int comparisons = WorkOrder.getComparisonCount();

        fillSortTable(afterTableModel, arrayToSort);
        metricsLabel.setText(String.format(
                "%s → %d comparaciones, %.3f µs (sobre %d elementos)",
                selected, comparisons, elapsedMicros, arrayToSort.length));
    }

    private void compareAllAlgorithms() {
        if (workOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Carga datos primero.");
            return;
        }

        List<WorkOrder> shuffled = new ArrayList<>(workOrders);
        Collections.shuffle(shuffled);
        WorkOrder[] base = shuffled.toArray(new WorkOrder[0]);

        DefaultTableModel resultModel = new DefaultTableModel(
                new Object[]{"Algoritmo", "Comparaciones", "Tiempo (µs)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        for (Map.Entry<String, SortAlgorithm<WorkOrder>> entry : algorithms.entrySet()) {
            WorkOrder[] copy = Arrays.copyOf(base, base.length);

            WorkOrder.resetComparisonCount();
            long start = System.nanoTime();
            entry.getValue().sort(copy);
            long elapsedNanos = System.nanoTime() - start;
            double elapsedMicros = elapsedNanos / 1_000.0;

            resultModel.addRow(new Object[]{
                entry.getKey(),
                WorkOrder.getComparisonCount(),
                String.format("%.3f", elapsedMicros)
            });
        }

        JTable resultTable = new JTable(resultModel);
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Comparación de algoritmos (mismo array desordenado, "
                + base.length + " elementos)");
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(new JScrollPane(resultTable));
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void fillSortTable(DefaultTableModel model, WorkOrder[] array) {
        model.setRowCount(0);
        for (WorkOrder wo : array) {
            model.addRow(new Object[]{
                wo.getId(),
                wo.getStatus(),
                wo.getCreatedAt() == null ? "-" : wo.getCreatedAt().format(DATE_FMT)
            });
        }
    }

    // ========================================================
    // TAB 2: ARREGLO 1D (estadísticas sobre datos reales)
    // ========================================================
    private JPanel buildArray1DTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton computeButton = new JButton("Calcular días abiertos por orden");
        computeButton.addActionListener(e -> computeDaysArray());
        panel.add(computeButton, BorderLayout.NORTH);

        daysArrayArea = new JTextArea(6, 40);
        daysArrayArea.setEditable(false);
        panel.add(new JScrollPane(daysArrayArea), BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(0, 1));
        sumLabel = new JLabel("Suma total de días: -");
        maxLabel = new JLabel("Máximo (orden más lenta): -");
        minLabel = new JLabel("Mínimo (orden más rápida): -");
        stats.add(sumLabel);
        stats.add(maxLabel);
        stats.add(minLabel);

        JPanel searchPanel = new JPanel();
        searchField = new JTextField(5);
        JButton searchButton = new JButton("Buscar valor exacto");
        searchButton.addActionListener(e -> searchInArray());
        searchResultLabel = new JLabel(" ");
        searchPanel.add(new JLabel("Buscar días ="));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(searchResultLabel);
        stats.add(searchPanel);

        panel.add(stats, BorderLayout.SOUTH);
        return panel;
    }

    private void computeDaysArray() {
        if (workOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Carga datos primero.");
            return;
        }

        int[] days = new int[workOrders.size()];
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < workOrders.size(); i++) {
            WorkOrder wo = workOrders.get(i);
            LocalDateTime end = wo.getClosedAt() != null ? wo.getClosedAt() : now;
            LocalDateTime start = wo.getCreatedAt() != null ? wo.getCreatedAt() : end;
            days[i] = (int) ChronoUnit.DAYS.between(start, end);
        }

        lastDaysArray = days;

        StringBuilder sb = new StringBuilder("Días abiertos por orden (en orden de id): ");
        for (int d : days) {
            sb.append(d).append(" ");
        }

        sb.append("\n\nInvertido (ArrayUtils.reverse): ");
        int[] reversed = ArrayUtils.copy(days);
        ArrayUtils.reverse(reversed);
        for (int d : reversed) {
            sb.append(d).append(" ");
        }

        daysArrayArea.setText(sb.toString());

        sumLabel.setText("Suma total de días: " + ArrayUtils.sum(days));
        maxLabel.setText("Máximo (orden más lenta): " + ArrayUtils.max(days) + " días");
        minLabel.setText("Mínimo (orden más rápida): " + ArrayUtils.min(days) + " días");
    }

    private void searchInArray() {
        try {
            int value = Integer.parseInt(searchField.getText().trim());
            boolean found = ArrayUtils.contains(lastDaysArray, value);
            int index = ArrayUtils.indexOf(lastDaysArray, value);
            searchResultLabel.setText(found
                    ? "Encontrado en posición " + index
                    : "No encontrado");
        } catch (NumberFormatException ex) {
            searchResultLabel.setText("Ingresa un número válido");
        }
    }

// ========================================================
// TAB 3: CRUD ARREGLO 1D (manual)
// ========================================================
    private JPanel buildArrayCrudTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 1));

        JPanel populatePanel = new JPanel();
        arraySizeField = new JTextField(4);
        arrayFillValueField = new JTextField(4);
        JButton populateButton = new JButton("Poblar arreglo");
        populateButton.addActionListener(e -> arrayPopulate());
        populatePanel.add(new JLabel("Tamaño:"));
        populatePanel.add(arraySizeField);
        populatePanel.add(new JLabel("Valor inicial (vacío = aleatorio):"));
        populatePanel.add(arrayFillValueField);
        populatePanel.add(populateButton);

        JPanel crudPanel = new JPanel();
        arrayIndexField = new JTextField(4);
        arrayValueField = new JTextField(4);

        JButton createButton = new JButton("Crear (insertar en índice)");
        createButton.addActionListener(e -> arrayCreate());

        JButton readButton = new JButton("Leer");
        readButton.addActionListener(e -> arrayRead());

        JButton updateButton = new JButton("Actualizar");
        updateButton.addActionListener(e -> arrayUpdate());

        JButton deleteButton = new JButton("Eliminar");
        deleteButton.addActionListener(e -> arrayDelete());

        JButton resetButton = new JButton("Reiniciar arreglo");
        resetButton.addActionListener(e -> arrayReset());

        crudPanel.add(new JLabel("Índice:"));
        crudPanel.add(arrayIndexField);
        crudPanel.add(new JLabel("Valor:"));
        crudPanel.add(arrayValueField);
        crudPanel.add(createButton);
        crudPanel.add(readButton);
        crudPanel.add(updateButton);
        crudPanel.add(deleteButton);
        crudPanel.add(resetButton);

        formPanel.add(populatePanel);
        formPanel.add(crudPanel);
        panel.add(formPanel, BorderLayout.NORTH);

        arrayCrudDisplay = new JLabel("Arreglo vacío []");
        arrayCrudDisplay.setFont(arrayCrudDisplay.getFont().deriveFont(16f));
        arrayCrudDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(arrayCrudDisplay, BorderLayout.CENTER);

        arrayCrudResultLabel = new JLabel(" ");
        panel.add(arrayCrudResultLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void arrayPopulate() {
        try {
            int size = Integer.parseInt(arraySizeField.getText().trim());

            if (size <= 0) {
                arrayCrudResultLabel.setText("El tamaño debe ser mayor a 0");
                return;
            }

            workingArray = new int[size];
            String fillText = arrayFillValueField.getText().trim();

            if (fillText.isEmpty()) {
                Random random = new Random();
                for (int i = 0; i < size; i++) {
                    workingArray[i] = random.nextInt(100); // 0-99
                }
                arrayCrudResultLabel.setText(
                        "Arreglo de tamaño " + size + " poblado con valores aleatorios (0-99)");
            } else {
                int value = Integer.parseInt(fillText);
                ArrayUtils.fill(workingArray, value);
                arrayCrudResultLabel.setText(
                        "Arreglo de tamaño " + size + " poblado con valor " + value);
            }

            refreshArrayCrudDisplay();
        } catch (NumberFormatException ex) {
            arrayCrudResultLabel.setText("Tamaño y valor deben ser números enteros");
        }
    }

    private void arrayCreate() {
        try {
            int index = Integer.parseInt(arrayIndexField.getText().trim());
            int value = Integer.parseInt(arrayValueField.getText().trim());
            workingArray = ArrayUtils.insertAt(workingArray, index, value);
            arrayCrudResultLabel.setText("Insertado " + value + " en índice " + index);
            refreshArrayCrudDisplay();
        } catch (NumberFormatException ex) {
            arrayCrudResultLabel.setText("Índice y valor deben ser números enteros");
        } catch (IndexOutOfBoundsException ex) {
            arrayCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void arrayRead() {
        try {
            int index = Integer.parseInt(arrayIndexField.getText().trim());
            int value = ArrayUtils.readAt(workingArray, index);
            arrayCrudResultLabel.setText("Valor en índice " + index + " = " + value);
        } catch (NumberFormatException ex) {
            arrayCrudResultLabel.setText("Índice debe ser un número entero");
        } catch (IndexOutOfBoundsException ex) {
            arrayCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void arrayUpdate() {
        try {
            int index = Integer.parseInt(arrayIndexField.getText().trim());
            int value = Integer.parseInt(arrayValueField.getText().trim());
            ArrayUtils.updateAt(workingArray, index, value);
            arrayCrudResultLabel.setText("Actualizado índice " + index + " a " + value);
            refreshArrayCrudDisplay();
        } catch (NumberFormatException ex) {
            arrayCrudResultLabel.setText("Índice y valor deben ser números enteros");
        } catch (IndexOutOfBoundsException ex) {
            arrayCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void arrayDelete() {
        try {
            int index = Integer.parseInt(arrayIndexField.getText().trim());
            workingArray = ArrayUtils.deleteAt(workingArray, index);
            arrayCrudResultLabel.setText("Eliminado índice " + index);
            refreshArrayCrudDisplay();
        } catch (NumberFormatException ex) {
            arrayCrudResultLabel.setText("Índice debe ser un número entero");
        } catch (IndexOutOfBoundsException ex) {
            arrayCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void arrayReset() {
        workingArray = new int[0];
        arrayCrudResultLabel.setText("Arreglo reiniciado");
        refreshArrayCrudDisplay();
    }

    private void refreshArrayCrudDisplay() {
        if (workingArray.length == 0) {
            arrayCrudDisplay.setText("Arreglo vacío []");
            return;
        }
        StringBuilder sb = new StringBuilder("[ ");
        for (int i = 0; i < workingArray.length; i++) {
            sb.append(workingArray[i]);
            if (i < workingArray.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(" ]");
        arrayCrudDisplay.setText(sb.toString());
    }
    

    // ========================================================
    // TAB 4: MATRIZ 2D (reporte sobre datos reales)
    // ========================================================
    private JPanel buildMatrixTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel();
        JButton buildButton = new JButton("Generar matriz Estado × Mecánico");
        buildButton.addActionListener(e -> buildMatrix());
        JButton transposeButton = new JButton("Transponer");
        transposeButton.addActionListener(e -> transposeMatrix());
        top.add(buildButton);
        top.add(transposeButton);
        panel.add(top, BorderLayout.NORTH);

        matrixTableModel = new DefaultTableModel();
        panel.add(new JScrollPane(new JTable(matrixTableModel)), BorderLayout.CENTER);

        JPanel stats = new JPanel();
        matrixSumLabel = new JLabel("Total de órdenes: -");
        matrixMaxLabel = new JLabel("Máximo en una celda: -");
        matrixMinLabel = new JLabel("Mínimo en una celda: -");
        stats.add(matrixSumLabel);
        stats.add(matrixMaxLabel);
        stats.add(matrixMinLabel);
        panel.add(stats, BorderLayout.SOUTH);

        return panel;
    }

    private void buildMatrix() {
        if (workOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Carga datos primero.");
            return;
        }

        TreeSet<Long> idSet = new TreeSet<>(Comparator.nullsLast(Long::compareTo));
        for (WorkOrder wo : workOrders) {
            idSet.add(wo.getMechanicId());
        }
        lastMechanicIds = new ArrayList<>(idSet);

        OrderStatus[] statuses = OrderStatus.values();
        int[][] matrix = new int[statuses.length][lastMechanicIds.size()];

        for (WorkOrder wo : workOrders) {
            int row = wo.getStatus().ordinal();
            int col = lastMechanicIds.indexOf(wo.getMechanicId());
            matrix[row][col]++;
        }

        lastMatrix = matrix;
        renderMatrix(matrix, statuses, lastMechanicIds);

        matrixSumLabel.setText("Total de órdenes: " + MatrixUtils.sum(matrix));
        matrixMaxLabel.setText("Máximo en una celda: " + MatrixUtils.max(matrix));
        matrixMinLabel.setText("Mínimo en una celda: " + MatrixUtils.min(matrix));
    }

    private void transposeMatrix() {
        if (lastMatrix.length == 0) {
            JOptionPane.showMessageDialog(this, "Genera la matriz primero.");
            return;
        }
        int[][] transposed = MatrixUtils.transpose(lastMatrix);

        matrixTableModel.setRowCount(0);
        matrixTableModel.setColumnCount(0);

        OrderStatus[] statuses = OrderStatus.values();
        Object[] columns = new Object[statuses.length + 1];
        columns[0] = "Mecánico";
        for (int i = 0; i < statuses.length; i++) {
            columns[i + 1] = statuses[i];
        }
        matrixTableModel.setColumnIdentifiers(columns);

        for (int i = 0; i < transposed.length; i++) {
            Object[] row = new Object[transposed[i].length + 1];
            row[0] = lastMechanicIds.get(i) == null
                    ? "Sin asignar" : "Mecánico #" + lastMechanicIds.get(i);
            for (int j = 0; j < transposed[i].length; j++) {
                row[j + 1] = transposed[i][j];
            }
            matrixTableModel.addRow(row);
        }
    }

    private void renderMatrix(int[][] matrix, OrderStatus[] statuses, List<Long> mechanicIds) {
        matrixTableModel.setRowCount(0);
        matrixTableModel.setColumnCount(0);

        Object[] columns = new Object[mechanicIds.size() + 1];
        columns[0] = "Estado";
        for (int i = 0; i < mechanicIds.size(); i++) {
            columns[i + 1] = mechanicIds.get(i) == null
                    ? "Sin asignar" : "Mecánico #" + mechanicIds.get(i);
        }
        matrixTableModel.setColumnIdentifiers(columns);

        for (int i = 0; i < matrix.length; i++) {
            Object[] row = new Object[matrix[i].length + 1];
            row[0] = statuses[i];
            for (int j = 0; j < matrix[i].length; j++) {
                row[j + 1] = matrix[i][j];
            }
            matrixTableModel.addRow(row);
        }
    }

// ========================================================
// TAB 5: CRUD MATRIZ 2D (manual)
// ========================================================
    private JPanel buildMatrixCrudTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 1));

        JPanel populatePanel = new JPanel();
        matrixRowsField = new JTextField(3);
        matrixColsField = new JTextField(3);
        matrixFillValueField = new JTextField(4);
        JButton populateButton = new JButton("Poblar matriz");
        populateButton.addActionListener(e -> matrixPopulate());
        populatePanel.add(new JLabel("Filas:"));
        populatePanel.add(matrixRowsField);
        populatePanel.add(new JLabel("Columnas:"));
        populatePanel.add(matrixColsField);
        populatePanel.add(new JLabel("Valor inicial:"));
        populatePanel.add(matrixFillValueField);
        populatePanel.add(populateButton);

        JPanel rowPanel = new JPanel();
        matrixRowIndexField = new JTextField(3);
        matrixValuesField = new JTextField(15);
        JButton insertRowButton = new JButton("Insertar fila");
        insertRowButton.addActionListener(e -> matrixInsertRow());
        JButton deleteRowButton = new JButton("Eliminar fila");
        deleteRowButton.addActionListener(e -> matrixDeleteRow());
        rowPanel.add(new JLabel("Índice fila:"));
        rowPanel.add(matrixRowIndexField);
        rowPanel.add(new JLabel("Valores (coma):"));
        rowPanel.add(matrixValuesField);
        rowPanel.add(insertRowButton);
        rowPanel.add(deleteRowButton);

        JPanel colPanel = new JPanel();
        matrixColIndexField = new JTextField(3);
        JButton insertColButton = new JButton("Insertar columna");
        insertColButton.addActionListener(e -> matrixInsertColumn());
        JButton deleteColButton = new JButton("Eliminar columna");
        deleteColButton.addActionListener(e -> matrixDeleteColumn());
        colPanel.add(new JLabel("Índice columna:"));
        colPanel.add(matrixColIndexField);
        colPanel.add(new JLabel("(usa \"Valores\" de arriba)"));
        colPanel.add(insertColButton);
        colPanel.add(deleteColButton);

        JPanel cellPanel = new JPanel();
        matrixCellValueField = new JTextField(4);
        JButton readCellButton = new JButton("Leer celda");
        readCellButton.addActionListener(e -> matrixReadCell());
        JButton updateCellButton = new JButton("Actualizar celda");
        updateCellButton.addActionListener(e -> matrixUpdateCell());
        JButton resetButton = new JButton("Reiniciar matriz");
        resetButton.addActionListener(e -> matrixReset());
        cellPanel.add(new JLabel("Valor celda:"));
        cellPanel.add(matrixCellValueField);
        cellPanel.add(readCellButton);
        cellPanel.add(updateCellButton);
        cellPanel.add(resetButton);
        cellPanel.add(new JLabel("(usa fila/columna de arriba)"));

        formPanel.add(populatePanel);
        formPanel.add(rowPanel);
        formPanel.add(colPanel);
        formPanel.add(cellPanel);
        panel.add(formPanel, BorderLayout.NORTH);

        matrixCrudTableModel = new DefaultTableModel();
        panel.add(new JScrollPane(new JTable(matrixCrudTableModel)), BorderLayout.CENTER);

        matrixCrudResultLabel = new JLabel(" ");
        panel.add(matrixCrudResultLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void matrixPopulate() {
        try {
            int rows = Integer.parseInt(matrixRowsField.getText().trim());
            int cols = Integer.parseInt(matrixColsField.getText().trim());

            if (rows <= 0 || cols <= 0) {
                matrixCrudResultLabel.setText("Filas y columnas deben ser mayores a 0");
                return;
            }

            workingMatrix = new int[rows][cols];
            String fillText = matrixFillValueField.getText().trim();

            if (fillText.isEmpty()) {
                Random random = new Random();
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        workingMatrix[i][j] = random.nextInt(100); // 0-99
                    }
                }
                matrixCrudResultLabel.setText(
                        "Matriz " + rows + "x" + cols + " poblada con valores aleatorios (0-99)");
            } else {
                int value = Integer.parseInt(fillText);
                MatrixUtils.fill(workingMatrix, value);
                matrixCrudResultLabel.setText(
                        "Matriz " + rows + "x" + cols + " poblada con valor " + value);
            }

            refreshMatrixCrudDisplay();
        } catch (NumberFormatException ex) {
            matrixCrudResultLabel.setText("Filas y columnas deben ser números enteros");
        }
    }

    private int[] parseValuesField() {
        String[] parts = matrixValuesField.getText().trim().split(",");
        int[] values = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = Integer.parseInt(parts[i].trim());
        }
        return values;
    }

    private void matrixInsertRow() {
        try {
            int rowIndex = Integer.parseInt(matrixRowIndexField.getText().trim());
            int[] values = parseValuesField();
            workingMatrix = MatrixUtils.insertRow(workingMatrix, rowIndex, values);
            matrixCrudResultLabel.setText("Fila insertada en índice " + rowIndex);
            refreshMatrixCrudDisplay();
        } catch (NumberFormatException ex) {
            matrixCrudResultLabel.setText("Índice y valores deben ser números enteros separados por coma");
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            matrixCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void matrixDeleteRow() {
        try {
            int rowIndex = Integer.parseInt(matrixRowIndexField.getText().trim());
            workingMatrix = MatrixUtils.deleteRow(workingMatrix, rowIndex);
            matrixCrudResultLabel.setText("Fila eliminada: índice " + rowIndex);
            refreshMatrixCrudDisplay();
        } catch (NumberFormatException ex) {
            matrixCrudResultLabel.setText("Índice debe ser un número entero");
        } catch (IndexOutOfBoundsException ex) {
            matrixCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void matrixInsertColumn() {
        try {
            int colIndex = Integer.parseInt(matrixColIndexField.getText().trim());
            int[] values = parseValuesField();
            workingMatrix = MatrixUtils.insertColumn(workingMatrix, colIndex, values);
            matrixCrudResultLabel.setText("Columna insertada en índice " + colIndex);
            refreshMatrixCrudDisplay();
        } catch (NumberFormatException ex) {
            matrixCrudResultLabel.setText("Índice y valores deben ser números enteros separados por coma");
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            matrixCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void matrixDeleteColumn() {
        try {
            int colIndex = Integer.parseInt(matrixColIndexField.getText().trim());
            workingMatrix = MatrixUtils.deleteColumn(workingMatrix, colIndex);
            matrixCrudResultLabel.setText("Columna eliminada: índice " + colIndex);
            refreshMatrixCrudDisplay();
        } catch (NumberFormatException ex) {
            matrixCrudResultLabel.setText("Índice debe ser un número entero");
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            matrixCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void matrixReadCell() {
        try {
            int row = Integer.parseInt(matrixRowIndexField.getText().trim());
            int col = Integer.parseInt(matrixColIndexField.getText().trim());
            int value = MatrixUtils.readCell(workingMatrix, row, col);
            matrixCrudResultLabel.setText("Valor en (" + row + "," + col + ") = " + value);
        } catch (NumberFormatException ex) {
            matrixCrudResultLabel.setText("Fila y columna deben ser números enteros");
        } catch (IndexOutOfBoundsException ex) {
            matrixCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void matrixUpdateCell() {
        try {
            int row = Integer.parseInt(matrixRowIndexField.getText().trim());
            int col = Integer.parseInt(matrixColIndexField.getText().trim());
            int value = Integer.parseInt(matrixCellValueField.getText().trim());
            MatrixUtils.updateCell(workingMatrix, row, col, value);
            matrixCrudResultLabel.setText("Celda (" + row + "," + col + ") actualizada a " + value);
            refreshMatrixCrudDisplay();
        } catch (NumberFormatException ex) {
            matrixCrudResultLabel.setText("Fila, columna y valor deben ser números enteros");
        } catch (IndexOutOfBoundsException ex) {
            matrixCrudResultLabel.setText(ex.getMessage());
        }
    }

    private void matrixReset() {
        workingMatrix = new int[0][0];
        matrixCrudResultLabel.setText("Matriz reiniciada");
        refreshMatrixCrudDisplay();
    }

    private void refreshMatrixCrudDisplay() {
        matrixCrudTableModel.setRowCount(0);
        matrixCrudTableModel.setColumnCount(0);

        if (workingMatrix.length == 0) {
            return;
        }

        int columnCount = workingMatrix[0].length;
        Object[] columns = new Object[columnCount];
        for (int j = 0; j < columnCount; j++) {
            columns[j] = "Col " + j;
        }
        matrixCrudTableModel.setColumnIdentifiers(columns);

        for (int[] row : workingMatrix) {
            Object[] rowData = new Object[row.length];
            for (int j = 0; j < row.length; j++) {
                rowData[j] = row[j];
            }
            matrixCrudTableModel.addRow(rowData);
        }
    }
}
