package com.epsilonworkshop.view.avances;

import com.epsilonworkshop.dao.WorkOrderDAO;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.WorkOrder;
import com.epsilonworkshop.util.list.*;
import com.epsilonworkshop.util.sort.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Panel de demostración académica (Avance 2): implementa y ejercita las 4
 * variantes de lista enlazada (simple, doble, circular simple, circular doble)
 * vía la interfaz común LinkedListADT<T>, usando Client como entidad de dominio
 * real, más una demo con WorkOrder de la BD.
 *
 * Crear como "New Java Class" normal en NetBeans (NO como "JPanel Form").
 *
 * @author sebap
 */
public class Avance2Panel extends JPanel {

    private final WorkOrderDAO workOrderDAO = new WorkOrderDAO();

    private static final DateTimeFormatter DATE_FMT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Map<String, Supplier<LinkedListADT<Client>>> clientListFactories = new LinkedHashMap<>();
    private final Map<String, Supplier<LinkedListADT<WorkOrder>>> workOrderListFactories = new LinkedHashMap<>();
    private final Map<String, ListSorter.AlgorithmFactory> sortAlgorithms = new LinkedHashMap<>();
    private final Map<String, Comparator<Client>> clientSortKeys = new LinkedHashMap<>();

    // --- Tab 1: CRUD manual con Client ---
    private JComboBox<String> crudTypeSelector;
    private LinkedListADT<Client> crudList;
    private DefaultTableModel crudTableModel;
    private JLabel crudResultLabel;
    private JTextField crudNameField;
    private JTextField crudSurnameField;
    private JTextField crudDocumentField;
    private JTextField crudIndexField;
    private JTextField crudRandomCountField;
    private JComboBox<String> crudSortKeySelector;
    private JComboBox<String> crudSortSelector;

    // --- Tab 2: Comparativa de rendimiento ---
    private JTextField comparisonCountField;
    private DefaultTableModel comparisonTableModel;

    // --- Tab 3: Inspector de nodos (independiente) ---
    private JComboBox<String> inspectorTypeSelector;
    private LinkedListADT<Client> inspectorList;
    private DefaultTableModel nodeInspectorTableModel;
    private JTextField inspectorRandomCountField;

    // --- Tab 4: Demo con WorkOrder reales ---
    private JComboBox<String> demoTypeSelector;
    private LinkedListADT<WorkOrder> demoList;
    private DefaultTableModel demoTableModel;
    private JLabel demoResultLabel;
    private JTextField demoIndexField;

    public Avance2Panel() {
        clientListFactories.put("Lista Simple", SinglyLinkedList::new);
        clientListFactories.put("Lista Doble", DoublyLinkedList::new);
        clientListFactories.put("Circular Simple", CircularSinglyLinkedList::new);
        clientListFactories.put("Circular Doble", CircularDoublyLinkedList::new);

        workOrderListFactories.put("Lista Simple", SinglyLinkedList::new);
        workOrderListFactories.put("Lista Doble", DoublyLinkedList::new);
        workOrderListFactories.put("Circular Simple", CircularSinglyLinkedList::new);
        workOrderListFactories.put("Circular Doble", CircularDoublyLinkedList::new);

        sortAlgorithms.put("Bubble Sort", BubbleSort::new);
        sortAlgorithms.put("Selection Sort", SelectionSort::new);
        sortAlgorithms.put("Insertion Sort", InsertionSort::new);
        sortAlgorithms.put("Shell Sort", ShellSort::new);
        sortAlgorithms.put("Merge Sort", MergeSort::new);
        sortAlgorithms.put("Quick Sort", QuickSort::new);

        // el orden vive en el panel: Client no se modifica
        Comparator<String> text = Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER);
        clientSortKeys.put("Apellido", Comparator.comparing(Client::getPaternalSurname, text)
                .thenComparing(Client::getName, text));
        clientSortKeys.put("Nombre", Comparator.comparing(Client::getName, text)
                .thenComparing(Client::getPaternalSurname, text));
        clientSortKeys.put("Documento", Comparator.comparing(Client::getDocument, text));

        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("CRUD Manual (Clientes)", buildCrudTab());
        tabs.addTab("Comparativa", buildComparisonTab());
        tabs.addTab("Inspector de Nodos", buildNodeInspectorTab());
        tabs.addTab("Demo con Work Orders (BD)", buildDemoTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ========================================================
    // TAB 1: CRUD MANUAL CON CLIENT
    // ========================================================
    private JPanel buildCrudTab() {
        JPanel panel = new JPanel(new BorderLayout());

        crudTypeSelector = new JComboBox<>(clientListFactories.keySet().toArray(new String[0]));
        crudTypeSelector.addActionListener(e -> crudChangeType());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JPanel typePanel = new JPanel();
        typePanel.add(new JLabel("Tipo de lista:"));
        typePanel.add(crudTypeSelector);
        formPanel.add(typePanel);

        crudNameField = new JTextField(8);
        crudSurnameField = new JTextField(8);
        crudDocumentField = new JTextField(8);
        crudIndexField = new JTextField(4);

        JPanel dataPanel = new JPanel();
        dataPanel.add(new JLabel("Nombre:"));
        dataPanel.add(crudNameField);
        dataPanel.add(new JLabel("Apellido:"));
        dataPanel.add(crudSurnameField);
        dataPanel.add(new JLabel("Documento:"));
        dataPanel.add(crudDocumentField);
        formPanel.add(dataPanel);

        JPanel insertPanel = new JPanel();
        JButton insertFirstButton = new JButton("Insertar al inicio");
        insertFirstButton.addActionListener(e -> crudInsertFirst());
        JButton insertLastButton = new JButton("Insertar al final");
        insertLastButton.addActionListener(e -> crudInsertLast());
        JButton insertAtButton = new JButton("Insertar en índice");
        insertAtButton.addActionListener(e -> crudInsertAt());
        insertPanel.add(insertFirstButton);
        insertPanel.add(insertLastButton);
        insertPanel.add(new JLabel("Índice:"));
        insertPanel.add(crudIndexField);
        insertPanel.add(insertAtButton);
        formPanel.add(insertPanel);

        JPanel actionsPanel = new JPanel();
        JButton readButton = new JButton("Leer en índice");
        readButton.addActionListener(e -> crudReadAt());
        JButton updateButton = new JButton("Actualizar en índice");
        updateButton.addActionListener(e -> crudUpdateAt());
        JButton deleteButton = new JButton("Eliminar en índice");
        deleteButton.addActionListener(e -> crudDeleteAt());
        JButton clearButton = new JButton("Limpiar lista");
        clearButton.addActionListener(e -> crudClear());
        actionsPanel.add(readButton);
        actionsPanel.add(updateButton);
        actionsPanel.add(deleteButton);
        actionsPanel.add(clearButton);
        formPanel.add(actionsPanel);

        JPanel randomPanel = new JPanel();
        crudRandomCountField = new JTextField(4);
        JButton randomFillButton = new JButton("Rellenar aleatoriamente");
        randomFillButton.addActionListener(e -> crudRandomFill());
        randomPanel.add(new JLabel("Cantidad:"));
        randomPanel.add(crudRandomCountField);
        randomPanel.add(randomFillButton);
        formPanel.add(randomPanel);

        JPanel sortPanel = new JPanel();
        crudSortKeySelector = new JComboBox<>(clientSortKeys.keySet().toArray(new String[0]));
        crudSortSelector = new JComboBox<>(sortAlgorithms.keySet().toArray(new String[0]));
        JButton sortButton = new JButton("Ordenar lista");
        sortButton.addActionListener(e -> crudSort());
        sortPanel.add(new JLabel("Ordenar por:"));
        sortPanel.add(crudSortKeySelector);
        sortPanel.add(new JLabel("Algoritmo:"));
        sortPanel.add(crudSortSelector);
        sortPanel.add(sortButton);
        formPanel.add(sortPanel);

        panel.add(formPanel, BorderLayout.NORTH);

        crudTableModel = new DefaultTableModel(
                new Object[]{"Posición", "Nombre", "Apellido", "Documento"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        panel.add(new JScrollPane(new JTable(crudTableModel)), BorderLayout.CENTER);

        crudResultLabel = new JLabel(" ");
        panel.add(crudResultLabel, BorderLayout.SOUTH);

        crudChangeType();
        return panel;
    }

    private void crudChangeType() {
        String selected = (String) crudTypeSelector.getSelectedItem();
        crudList = clientListFactories.get(selected).get();
        crudResultLabel.setText("Lista reiniciada como: " + selected);
        refreshCrudTable();
    }

    private Client buildClientFromForm() {
        String name = crudNameField.getText().trim();
        String surname = crudSurnameField.getText().trim();
        String document = crudDocumentField.getText().trim();

        if (name.isEmpty() || surname.isEmpty() || document.isEmpty()) {
            return null;
        }

        Client client = new Client();
        client.setName(name);
        client.setPaternalSurname(surname);
        client.setDocument(document);
        return client;
    }

    private void crudInsertFirst() {
        Client client = buildClientFromForm();
        if (client == null) {
            crudResultLabel.setText("Completa nombre, apellido y documento");
            return;
        }
        crudList.insertFirst(client);
        crudResultLabel.setText("Insertado al inicio: " + client.getName());
        refreshCrudTable();
    }

    private void crudInsertLast() {
        Client client = buildClientFromForm();
        if (client == null) {
            crudResultLabel.setText("Completa nombre, apellido y documento");
            return;
        }
        crudList.insertLast(client);
        crudResultLabel.setText("Insertado al final: " + client.getName());
        refreshCrudTable();
    }

    private void crudInsertAt() {
        try {
            int index = Integer.parseInt(crudIndexField.getText().trim());
            Client client = buildClientFromForm();
            if (client == null) {
                crudResultLabel.setText("Completa nombre, apellido y documento");
                return;
            }
            crudList.insertAt(index, client);
            crudResultLabel.setText("Insertado \"" + client.getName() + "\" en índice " + index);
            refreshCrudTable();
        } catch (NumberFormatException ex) {
            crudResultLabel.setText("El índice debe ser un número entero");
        } catch (IndexOutOfBoundsException ex) {
            crudResultLabel.setText(ex.getMessage());
        }
    }

    private void crudReadAt() {
        try {
            int index = Integer.parseInt(crudIndexField.getText().trim());
            Client client = crudList.readAt(index);
            crudResultLabel.setText("Índice " + index + ": " + client.getName() + " " + client.getPaternalSurname());
        } catch (NumberFormatException ex) {
            crudResultLabel.setText("El índice debe ser un número entero");
        } catch (IndexOutOfBoundsException ex) {
            crudResultLabel.setText(ex.getMessage());
        }
    }

    private void crudUpdateAt() {
        try {
            int index = Integer.parseInt(crudIndexField.getText().trim());
            Client client = buildClientFromForm();
            if (client == null) {
                crudResultLabel.setText("Completa nombre, apellido y documento");
                return;
            }
            crudList.updateAt(index, client);
            crudResultLabel.setText("Índice " + index + " actualizado a: " + client.getName());
            refreshCrudTable();
        } catch (NumberFormatException ex) {
            crudResultLabel.setText("El índice debe ser un número entero");
        } catch (IndexOutOfBoundsException ex) {
            crudResultLabel.setText(ex.getMessage());
        }
    }

    private void crudDeleteAt() {
        try {
            int index = Integer.parseInt(crudIndexField.getText().trim());
            Client deleted = crudList.deleteAt(index);
            crudResultLabel.setText("Eliminado de índice " + index + ": " + deleted.getName());
            refreshCrudTable();
        } catch (NumberFormatException ex) {
            crudResultLabel.setText("El índice debe ser un número entero");
        } catch (IndexOutOfBoundsException ex) {
            crudResultLabel.setText(ex.getMessage());
        }
    }

    private void crudClear() {
        crudList.clear();
        crudResultLabel.setText("Lista vaciada");
        refreshCrudTable();
    }

    private void crudRandomFill() {
        try {
            int count = Integer.parseInt(crudRandomCountField.getText().trim());
            if (count <= 0) {
                crudResultLabel.setText("La cantidad debe ser mayor a 0");
                return;
            }
            Random random = new Random();
            String[] nombres = {"Juan", "Maria", "Carlos", "Ana", "Luis", "Sofia"};
            String[] apellidos = {"Perez", "Gomez", "Diaz", "Rojas", "Vega", "Castro"};

            for (int i = 0; i < count; i++) {
                Client client = new Client();
                client.setName(nombres[random.nextInt(nombres.length)]);
                client.setPaternalSurname(apellidos[random.nextInt(apellidos.length)]);
                client.setDocument(String.valueOf(10_000_000 + random.nextInt(90_000_000)));
                crudList.insertLast(client);
            }
            crudResultLabel.setText(count + " clientes aleatorios insertados");
            refreshCrudTable();
        } catch (NumberFormatException ex) {
            crudResultLabel.setText("La cantidad debe ser un número entero");
        }
    }

    private void crudSort() {
        if (crudList.size() < 2) {
            crudResultLabel.setText("Se necesitan al menos 2 elementos para ordenar");
            return;
        }
        String keyName = (String) crudSortKeySelector.getSelectedItem();
        String algorithmName = (String) crudSortSelector.getSelectedItem();
        long startNanos = System.nanoTime();
        ListSorter.sort(crudList, clientSortKeys.get(keyName), sortAlgorithms.get(algorithmName));
        long elapsedMicros = (System.nanoTime() - startNanos) / 1_000;
        crudResultLabel.setText("Lista ordenada por " + keyName + " con " + algorithmName + " en " + elapsedMicros + " µs");
        refreshCrudTable();
    }

    private void refreshCrudTable() {
        crudTableModel.setRowCount(0);
        List<Client> values = crudList.toList();
        for (int i = 0; i < values.size(); i++) {
            Client c = values.get(i);
            crudTableModel.addRow(new Object[]{i, c.getName(), c.getPaternalSurname(), c.getDocument()});
        }
    }

    // ========================================================
    // TAB 2: COMPARATIVA DE RENDIMIENTO
    // ========================================================
    private JPanel buildComparisonTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel();
        comparisonCountField = new JTextField(8);
        JButton compareButton = new JButton("Comparar las 4 estructuras");
        compareButton.addActionListener(e -> compareAllStructures());
        topPanel.add(new JLabel("Elementos por estructura:"));
        topPanel.add(comparisonCountField);
        topPanel.add(compareButton);
        panel.add(topPanel, BorderLayout.NORTH);

        JLabel explanationLabel = new JLabel(
                "<html>Eliminar al final: se insertan N elementos, luego se eliminan "
                + "uno por uno desde el final hasta vaciar. Las simples deberían tardar "
                + "más -- no tienen acceso directo al último nodo.<br>"
                + "Leer cerca del final: se insertan N elementos, luego se lee 1000 veces "
                + "el índice más cercano al final. Las dobles deberían tardar menos -- "
                + "pueden recorrer desde el final hacia atrás en vez de siempre desde el inicio.</html>");
        panel.add(explanationLabel, BorderLayout.SOUTH);

        comparisonTableModel = new DefaultTableModel(
                new Object[]{
                    "Estructura",
                    "Tiempo Eliminar al Final (µs)",
                    "Tiempo Leer Cerca del Final (µs)"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        panel.add(new JScrollPane(new JTable(comparisonTableModel)), BorderLayout.CENTER);

        return panel;
    }

    private void compareAllStructures() {
        try {
            int count = Integer.parseInt(comparisonCountField.getText().trim());
            if (count <= 0) {
                return;
            }

            comparisonTableModel.setRowCount(0);

            for (Map.Entry<String, Supplier<LinkedListADT<Client>>> entry : clientListFactories.entrySet()) {
                String structureName = entry.getKey();

                long deleteTimeMicros = measureDeleteFromEnd(entry.getValue(), count);
                long readTimeMicros = measureReadNearEnd(entry.getValue(), count);

                comparisonTableModel.addRow(new Object[]{
                    structureName, deleteTimeMicros, readTimeMicros
                });
            }
        } catch (NumberFormatException ex) {
            comparisonTableModel.setRowCount(0);
            comparisonTableModel.addRow(new Object[]{"Error", "cantidad inválida", ""});
        }
    }

    private long measureDeleteFromEnd(Supplier<LinkedListADT<Client>> factory, int count) {
        LinkedListADT<Client> testList = factory.get();
        for (int i = 0; i < count; i++) {
            testList.insertLast(new Client());
        }

        long startNanos = System.nanoTime();
        while (!testList.isEmpty()) {
            testList.deleteAt(testList.size() - 1);
        }
        return (System.nanoTime() - startNanos) / 1_000;
    }

    private long measureReadNearEnd(Supplier<LinkedListADT<Client>> factory, int count) {
        LinkedListADT<Client> testList = factory.get();
        for (int i = 0; i < count; i++) {
            testList.insertLast(new Client());
        }

        int nearEndIndex = Math.max(0, count - 2);

        long startNanos = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            testList.readAt(nearEndIndex);
        }
        return (System.nanoTime() - startNanos) / 1_000;
    }

    // ========================================================
    // TAB 3: INSPECTOR DE NODOS (INDEPENDIENTE)
    // ========================================================
    private JPanel buildNodeInspectorTab() {
        JPanel panel = new JPanel(new BorderLayout());

        inspectorTypeSelector = new JComboBox<>(clientListFactories.keySet().toArray(new String[0]));
        inspectorTypeSelector.addActionListener(e -> inspectorChangeType());

        inspectorRandomCountField = new JTextField(4);
        JButton randomFillButton = new JButton("Rellenar aleatoriamente");
        randomFillButton.addActionListener(e -> inspectorRandomFill());

        JButton clearButton = new JButton("Limpiar");
        clearButton.addActionListener(e -> inspectorClear());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Tipo de lista:"));
        topPanel.add(inspectorTypeSelector);
        topPanel.add(new JLabel("Cantidad:"));
        topPanel.add(inspectorRandomCountField);
        topPanel.add(randomFillButton);
        topPanel.add(clearButton);
        panel.add(topPanel, BorderLayout.NORTH);

        nodeInspectorTableModel = new DefaultTableModel(
                new Object[]{"Posición", "Cliente", "Anterior", "Siguiente"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        panel.add(new JScrollPane(new JTable(nodeInspectorTableModel)), BorderLayout.CENTER);

        inspectorChangeType();
        return panel;
    }

    private void inspectorChangeType() {
        String selected = (String) inspectorTypeSelector.getSelectedItem();
        inspectorList = clientListFactories.get(selected).get();
        refreshNodeInspector();
    }

    private void inspectorRandomFill() {
        try {
            int count = Integer.parseInt(inspectorRandomCountField.getText().trim());
            if (count <= 0) {
                return;
            }

            Random random = new Random();
            String[] nombres = {"Juan", "Maria", "Carlos", "Ana", "Luis"};

            for (int i = 0; i < count; i++) {
                Client client = new Client();
                client.setName(nombres[random.nextInt(nombres.length)]);
                client.setPaternalSurname("Cliente" + i);
                inspectorList.insertLast(client);
            }
            refreshNodeInspector();
        } catch (NumberFormatException ex) {
            // cantidad inválida: no hace nada, el campo queda tal cual
        }
    }

    private void inspectorClear() {
        inspectorList.clear();
        refreshNodeInspector();
    }

    private void refreshNodeInspector() {
        nodeInspectorTableModel.setRowCount(0);

        List<Client> clients = inspectorList.toList();
        List<NodeInfo> nodes = inspectorList.describeNodes();

        for (int i = 0; i < nodes.size(); i++) {
            Client client = clients.get(i);
            NodeInfo node = nodes.get(i);

            String displayValue = client.getName() + " " + client.getPaternalSurname();

            nodeInspectorTableModel.addRow(new Object[]{
                node.position(), displayValue, node.previousRef(), node.nextRef()
            });
        }
    }

    // ========================================================
    // TAB 4: DEMO CON WORK ORDERS REALES
    // ========================================================
    private JPanel buildDemoTab() {
        JPanel panel = new JPanel(new BorderLayout());

        demoTypeSelector = new JComboBox<>(workOrderListFactories.keySet().toArray(new String[0]));
        demoTypeSelector.addActionListener(e -> demoChangeType());

        JButton loadButton = new JButton("Cargar work orders de la BD");
        loadButton.addActionListener(e -> demoLoadFromDatabase());

        demoIndexField = new JTextField(4);

        JButton deleteAtButton = new JButton("Eliminar en índice");
        deleteAtButton.addActionListener(e -> demoDeleteAt());
        JButton deleteFirstButton = new JButton("Eliminar primero");
        deleteFirstButton.addActionListener(e -> demoDeleteFirst());
        JButton deleteLastButton = new JButton("Eliminar último");
        deleteLastButton.addActionListener(e -> demoDeleteLast());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Tipo de lista:"));
        topPanel.add(demoTypeSelector);
        topPanel.add(loadButton);
        topPanel.add(new JLabel("Índice:"));
        topPanel.add(demoIndexField);
        topPanel.add(deleteAtButton);
        topPanel.add(deleteFirstButton);
        topPanel.add(deleteLastButton);
        panel.add(topPanel, BorderLayout.NORTH);

        demoTableModel = new DefaultTableModel(
                new Object[]{"Posición", "ID", "Estado", "Creado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        panel.add(new JScrollPane(new JTable(demoTableModel)), BorderLayout.CENTER);

        demoResultLabel = new JLabel(" ");
        panel.add(demoResultLabel, BorderLayout.SOUTH);

        demoChangeType();
        return panel;
    }

    private void demoChangeType() {
        String selected = (String) demoTypeSelector.getSelectedItem();
        demoList = workOrderListFactories.get(selected).get();
        demoResultLabel.setText("Lista reiniciada como: " + selected + ". Carga datos de la BD.");
        refreshDemoTable();
    }

    private void demoLoadFromDatabase() {
        try {
            List<WorkOrder> workOrders = workOrderDAO.findAll();
            demoList.clear();
            for (WorkOrder order : workOrders) {
                demoList.insertLast(order);
            }
            demoResultLabel.setText(workOrders.size() + " work orders cargadas en la lista.");
            refreshDemoTable();
        } catch (SQLException ex) {
            demoResultLabel.setText("Error cargando datos: " + ex.getMessage());
        }
    }

    private void demoDeleteAt() {
        try {
            int index = Integer.parseInt(demoIndexField.getText().trim());
            WorkOrder deleted = demoList.deleteAt(index);
            demoResultLabel.setText("Eliminada orden #" + deleted.getId() + " de índice " + index);
            refreshDemoTable();
        } catch (NumberFormatException ex) {
            demoResultLabel.setText("El índice debe ser un número entero");
        } catch (IndexOutOfBoundsException ex) {
            demoResultLabel.setText(ex.getMessage());
        }
    }

    private void demoDeleteFirst() {
        if (demoList.isEmpty()) {
            demoResultLabel.setText("La lista está vacía");
            return;
        }
        WorkOrder deleted = demoList.deleteAt(0);
        demoResultLabel.setText("Eliminada orden #" + deleted.getId() + " (era la primera)");
        refreshDemoTable();
    }

    private void demoDeleteLast() {
        if (demoList.isEmpty()) {
            demoResultLabel.setText("La lista está vacía");
            return;
        }
        WorkOrder deleted = demoList.deleteAt(demoList.size() - 1);
        demoResultLabel.setText("Eliminada orden #" + deleted.getId() + " (era la última)");
        refreshDemoTable();
    }

    private void refreshDemoTable() {
        demoTableModel.setRowCount(0);
        List<WorkOrder> values = demoList.toList();
        for (int i = 0; i < values.size(); i++) {
            WorkOrder order = values.get(i);
            demoTableModel.addRow(new Object[]{
                i,
                order.getId(),
                order.getStatus(),
                order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : ""
            });
        }
    }
}