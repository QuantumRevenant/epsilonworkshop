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
 * work_orders.
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

    // --- Tab 2: Arreglo 1D ---
    private JTextArea daysArrayArea;
    private JLabel sumLabel, maxLabel, minLabel;
    private JTextField searchField;
    private JLabel searchResultLabel;
    private int[] lastDaysArray = new int[0];

    // --- Tab 3: Matriz 2D ---
    private DefaultTableModel matrixTableModel;
    private JLabel matrixSumLabel, matrixMaxLabel, matrixMinLabel;
    private int[][] lastMatrix = new int[0][0];
    private List<Long> lastMechanicIds = new ArrayList<>();

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
        tabs.addTab("Arreglo 1D", buildArray1DTab());
        tabs.addTab("Matriz 2D", buildMatrixTab());
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
                "%s → %d comparaciones, %d ms (sobre %d elementos)",
                selected, comparisons, elapsedMicros, arrayToSort.length));
    }

    private void compareAllAlgorithms() {
        if (workOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Carga datos primero.");
            return;
        }

        // Se desordena UNA sola vez y se usa la misma copia base
        // para los 6 algoritmos, así la comparación es en igualdad
        // de condiciones.
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
    // TAB 2: ARREGLO 1D
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
    // TAB 3: MATRIZ 2D
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

        // En la transpuesta, filas = mecánicos, columnas = estados
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
}
