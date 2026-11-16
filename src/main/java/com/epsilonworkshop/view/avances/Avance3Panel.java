/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.view.avances;

import com.epsilonworkshop.controller.ClientController;
import com.epsilonworkshop.dao.WorkOrderDAO;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.OrderStatus;
import com.epsilonworkshop.model.WorkOrder;
import com.epsilonworkshop.util.array.SlotInfo;
import com.epsilonworkshop.util.queue.ArrayQueue;
import com.epsilonworkshop.util.queue.QueueADT;
import com.epsilonworkshop.util.stack.ArrayStack;
import com.epsilonworkshop.util.stack.StackADT;
import com.epsilonworkshop.util.tree.BinarySearchTree;
import com.epsilonworkshop.util.tree.BinaryTreeADT;
import com.epsilonworkshop.util.tree.TreeNodeInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Panel de demostración académica (Avance 3): pila y cola estáticas con
 * arreglo, y árbol binario de búsqueda, con datos manuales, aleatorios y reales
 * de la base.
 *
 * Crear como "New Java Class" normal en NetBeans (NO como "JPanel Form"),
 * dentro del paquete com.epsilonworkshop.view.avances.
 *
 * @author sebap
 */
public class Avance3Panel extends JPanel {

    private static final Random RANDOM = new Random();
    private static final String[] WORDS = {"alfa", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta"};
    private static final String[] FIRST_NAMES = {"Juan", "Maria", "Carlos", "Ana", "Luis", "Sofia"};

    private final ClientController clientController = new ClientController();
    private final WorkOrderDAO workOrderDAO = new WorkOrderDAO();

    // --- Pila ---
    private StackADT<String> stack = new ArrayStack<>(5);
    private JTextField stackCapacityField;
    private JTextField stackValueField;
    private DefaultTableModel stackTableModel;
    private JLabel stackMessage;

    // --- Cola ---
    private QueueADT<String> queue = new ArrayQueue<>(5);
    private JTextField queueCapacityField;
    private JTextField queueValueField;
    private DefaultTableModel queueTableModel;
    private JLabel queueMessage;

    // --- Árbol ---
    // Los clientes se ordenan por documento sin modificar la clase Client.
    private BinaryTreeADT<Client> tree = newClientTree();
    private JTextField treeDocumentField;
    private JTextField treeNameField;
    private JTextField treeCountField;
    private DefaultTableModel nodeTableModel;
    private JTextArea traversalArea;
    private JLabel treeInfoLabel;
    private JLabel treeResultLabel;

    // --- Comparativa ---
    private JTextField compareCountField;
    private DefaultTableModel compareTableModel;
    private JLabel compareMessage;

    public Avance3Panel() {
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Pila", buildStackTab());
        tabs.addTab("Cola", buildQueueTab());
        tabs.addTab("Árbol binario", buildTreeTab());
        tabs.addTab("Comparativa del árbol", buildCompareTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ========================================================
    // TAB 1: PILA
    // ========================================================
    private JPanel buildStackTab() {
        JPanel panel = new JPanel(new BorderLayout());

        stackCapacityField = new JTextField("5", 4);
        stackValueField = new JTextField(10);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(row(new JLabel("Capacidad:"), stackCapacityField,
                button("Crear pila", e -> stackCreate())));
        top.add(row(new JLabel("Valor:"), stackValueField,
                button("Apilar (push)", e -> stackPush()),
                button("Desapilar (pop)", e -> stackPop()),
                button("Ver cima (peek)", e -> stackPeek()),
                button("Rellenar aleatoriamente", e -> stackRandomFill()),
                button("Vaciar", e -> stackClear())));
        panel.add(top, BorderLayout.NORTH);

        stackTableModel = readOnlyModel("Índice", "Valor", "Marca");
        panel.add(new JScrollPane(new JTable(stackTableModel)), BorderLayout.CENTER);

        stackMessage = new JLabel(" ");
        panel.add(stackMessage, BorderLayout.SOUTH);

        refreshStack();
        return panel;
    }

    private String stackStatus() {
        return "Tamaño " + stack.size() + " de " + stack.capacity();
    }

    private void stackCreate() {
        Integer capacity = parsePositive(stackCapacityField.getText());
        if (capacity == null) {
            stackMessage.setText("La capacidad debe ser un entero mayor que 0");
            return;
        }
        stack = new ArrayStack<>(capacity);
        stackMessage.setText("Pila creada con capacidad " + capacity);
        refreshStack();
    }

    private void stackPush() {
        String value = stackValueField.getText().trim();
        if (value.isEmpty()) {
            stackMessage.setText("Ingresa un valor");
            return;
        }
        try {
            stack.push(value);
            stackMessage.setText("Apilado: " + value + "  |  " + stackStatus());
        } catch (IllegalStateException ex) {
            stackMessage.setText(ex.getMessage());
        }
        refreshStack();
    }

    private void stackPop() {
        try {
            String value = stack.pop();
            stackMessage.setText("Desapilado: " + value + "  |  " + stackStatus());
        } catch (NoSuchElementException ex) {
            stackMessage.setText(ex.getMessage());
        }
        refreshStack();
    }

    private void stackPeek() {
        try {
            stackMessage.setText("Cima: " + stack.peek() + "  |  " + stackStatus());
        } catch (NoSuchElementException ex) {
            stackMessage.setText(ex.getMessage());
        }
    }

    private void stackRandomFill() {
        int added = 0;
        while (!stack.isFull()) {
            stack.push(randomWord());
            added++;
        }
        stackMessage.setText(added + " valores apilados  |  " + stackStatus());
        refreshStack();
    }

    private void stackClear() {
        stack.clear();
        stackMessage.setText("Pila vaciada  |  " + stackStatus());
        refreshStack();
    }

    // La cima se muestra arriba: las casillas van del índice mayor al menor.
    private void refreshStack() {
        stackTableModel.setRowCount(0);
        List<SlotInfo> slots = stack.describeSlots();
        for (int i = slots.size() - 1; i >= 0; i--) {
            SlotInfo slot = slots.get(i);
            stackTableModel.addRow(new Object[]{slot.index(), slot.value(), slot.marker()});
        }
    }

    // ========================================================
    // TAB 2: COLA
    // ========================================================
    private JPanel buildQueueTab() {
        JPanel panel = new JPanel(new BorderLayout());

        queueCapacityField = new JTextField("5", 4);
        queueValueField = new JTextField(10);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(row(new JLabel("Capacidad:"), queueCapacityField,
                button("Crear cola", e -> queueCreate()),
                button("Cargar órdenes sin asignar (BD)", e -> queueLoadUnassigned())));
        top.add(row(new JLabel("Valor:"), queueValueField,
                button("Encolar (enqueue)", e -> queueEnqueue()),
                button("Desencolar (dequeue)", e -> queueDequeue()),
                button("Ver frente (peek)", e -> queuePeek()),
                button("Rellenar aleatoriamente", e -> queueRandomFill()),
                button("Vaciar", e -> queueClear())));
        panel.add(top, BorderLayout.NORTH);

        queueTableModel = readOnlyModel("Índice", "Valor", "Marca");
        panel.add(new JScrollPane(new JTable(queueTableModel)), BorderLayout.CENTER);

        queueMessage = new JLabel(" ");
        panel.add(queueMessage, BorderLayout.SOUTH);

        refreshQueue();
        return panel;
    }

    private String queueStatus() {
        return "Tamaño " + queue.size() + " de " + queue.capacity();
    }

    private void queueCreate() {
        Integer capacity = parsePositive(queueCapacityField.getText());
        if (capacity == null) {
            queueMessage.setText("La capacidad debe ser un entero mayor que 0");
            return;
        }
        queue = new ArrayQueue<>(capacity);
        queueMessage.setText("Cola creada con capacidad " + capacity);
        refreshQueue();
    }

    private void queueEnqueue() {
        String value = queueValueField.getText().trim();
        if (value.isEmpty()) {
            queueMessage.setText("Ingresa un valor");
            return;
        }
        try {
            queue.enqueue(value);
            queueMessage.setText("Encolado: " + value + "  |  " + queueStatus());
        } catch (IllegalStateException ex) {
            queueMessage.setText(ex.getMessage());
        }
        refreshQueue();
    }

    private void queueDequeue() {
        try {
            String value = queue.dequeue();
            queueMessage.setText("Desencolado: " + value + "  |  " + queueStatus());
        } catch (NoSuchElementException ex) {
            queueMessage.setText(ex.getMessage());
        }
        refreshQueue();
    }

    private void queuePeek() {
        try {
            queueMessage.setText("Frente: " + queue.peek() + "  |  " + queueStatus());
        } catch (NoSuchElementException ex) {
            queueMessage.setText(ex.getMessage());
        }
    }

    private void queueRandomFill() {
        int added = 0;
        while (!queue.isFull()) {
            queue.enqueue(randomWord());
            added++;
        }
        queueMessage.setText(added + " valores encolados  |  " + queueStatus());
        refreshQueue();
    }

    private void queueClear() {
        queue.clear();
        queueMessage.setText("Cola vaciada  |  " + queueStatus());
        refreshQueue();
    }

    // Uso real: las órdenes pendientes sin mecánico forman una cola FIFO (RN-04b).
    // La cola es estática, así que se dimensiona con lo que hay (mínimo 1).
    private void queueLoadUnassigned() {
        try {
            List<String> entries = new ArrayList<>();
            for (WorkOrder order : workOrderDAO.findAll()) {
                if (order.getStatus() == OrderStatus.PENDING && order.getMechanicId() == null) {
                    entries.add("Orden #" + order.getId() + " — " + order.getDescription());
                }
            }

            int capacity = Math.max(1, entries.size());
            queue = new ArrayQueue<>(capacity);
            for (String entry : entries) {
                queue.enqueue(entry);
            }

            queueCapacityField.setText(String.valueOf(capacity));
            queueMessage.setText(entries.size() + " órdenes pendientes sin asignar cargadas  |  "
                    + queueStatus());
            refreshQueue();

        } catch (SQLException ex) {
            queueMessage.setText("Error cargando datos: " + ex.getMessage());
        }
    }

    private void refreshQueue() {
        queueTableModel.setRowCount(0);
        for (SlotInfo slot : queue.describeSlots()) {
            queueTableModel.addRow(new Object[]{slot.index(), slot.value(), slot.marker()});
        }
    }

    // ========================================================
    // TAB 3: ÁRBOL BINARIO DE BÚSQUEDA
    // ========================================================
    private static BinaryTreeADT<Client> newClientTree() {
        return new BinarySearchTree<Client>(Comparator.comparing(Client::getDocument));
    }

    private JPanel buildTreeTab() {
        JPanel panel = new JPanel(new BorderLayout());

        treeDocumentField = new JTextField(10);
        treeNameField = new JTextField(10);
        treeCountField = new JTextField("15", 4);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(row(new JLabel("Documento:"), treeDocumentField,
                new JLabel("Nombre:"), treeNameField,
                button("Insertar", e -> treeInsert()),
                button("Buscar", e -> treeSearch()),
                button("Eliminar", e -> treeDelete())));
        top.add(row(new JLabel("Cantidad:"), treeCountField,
                button("Rellenar aleatoriamente", e -> treeRandomFill()),
                button("Cargar clientes (BD)", e -> treeLoadFromDatabase()),
                button("Vaciar", e -> treeClear())));
        top.add(row(new JLabel("Recorridos:"),
                button("Preorden", e -> showTraversal("Preorden", tree.preOrder())),
                button("Inorden", e -> showTraversal("Inorden", tree.inOrder())),
                button("Postorden", e -> showTraversal("Postorden", tree.postOrder())),
                button("Por niveles", e -> showTraversal("Por niveles", tree.levelOrder()))));
        panel.add(top, BorderLayout.NORTH);

        nodeTableModel = readOnlyModel("Nivel", "Documento", "Padre", "Izquierdo", "Derecho", "Tipo");
        panel.add(new JScrollPane(new JTable(nodeTableModel)), BorderLayout.CENTER);

        traversalArea = new JTextArea(3, 40);
        traversalArea.setEditable(false);
        traversalArea.setLineWrap(true);
        traversalArea.setWrapStyleWord(true);

        treeInfoLabel = new JLabel(" ");
        treeResultLabel = new JLabel(" ");

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        treeInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        treeResultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane traversalPane = new JScrollPane(traversalArea);
        traversalPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(treeInfoLabel);
        bottom.add(traversalPane);
        bottom.add(treeResultLabel);
        panel.add(bottom, BorderLayout.SOUTH);

        refreshTree();
        return panel;
    }

    // Objeto de prueba: solo importa el campo por el que se ordena el árbol.
    private Client probe(String document) {
        Client client = new Client();
        client.setDocument(document);
        return client;
    }

    private void treeInsert() {
        String document = treeDocumentField.getText().trim();
        String name = treeNameField.getText().trim();
        if (document.isEmpty() || name.isEmpty()) {
            treeResultLabel.setText("Ingresa documento y nombre");
            return;
        }

        Client client = probe(document);
        client.setName(name);

        boolean inserted = tree.insert(client);
        treeResultLabel.setText(inserted
                ? "Insertado: " + document
                : "Ya existe un cliente con documento " + document + " (no se insertó)");
        refreshTree();
    }

    private void treeSearch() {
        String document = treeDocumentField.getText().trim();
        if (document.isEmpty()) {
            treeResultLabel.setText("Ingresa un documento");
            return;
        }

        Optional<Client> found = tree.find(probe(document));
        treeResultLabel.setText(found
                .map(c -> "Encontrado: " + c.getName() + " (documento " + c.getDocument() + ")")
                .orElse("No existe un cliente con documento " + document));
    }

    private void treeDelete() {
        String document = treeDocumentField.getText().trim();
        if (document.isEmpty()) {
            treeResultLabel.setText("Ingresa un documento");
            return;
        }

        boolean deleted = tree.delete(probe(document));
        treeResultLabel.setText(deleted
                ? "Eliminado: " + document
                : "No existe un cliente con documento " + document);
        refreshTree();
    }

    private void treeRandomFill() {
        Integer count = parsePositive(treeCountField.getText());
        if (count == null) {
            treeResultLabel.setText("La cantidad debe ser un entero mayor que 0");
            return;
        }

        int inserted = 0;
        for (int i = 0; i < count; i++) {
            Client client = probe(String.valueOf(10_000_000 + RANDOM.nextInt(90_000_000)));
            client.setName(FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)]);
            if (tree.insert(client)) {
                inserted++;
            }
        }
        treeResultLabel.setText(inserted + " clientes aleatorios insertados"
                + (inserted < count ? " (" + (count - inserted) + " repetidos descartados)" : ""));
        refreshTree();
    }

    private void treeLoadFromDatabase() {
        try {
            tree.clear();
            int inserted = 0;
            for (Client client : clientController.findAll()) {
                if (tree.insert(client)) {
                    inserted++;
                }
            }
            treeResultLabel.setText(inserted + " clientes de la base cargados en el árbol");
            refreshTree();

        } catch (SQLException ex) {
            treeResultLabel.setText("Error cargando datos: " + ex.getMessage());
        }
    }

    private void treeClear() {
        tree.clear();
        traversalArea.setText("");
        treeResultLabel.setText("Árbol vaciado");
        refreshTree();
    }

    private void showTraversal(String title, List<Client> order) {
        if (order.isEmpty()) {
            traversalArea.setText(title + ": el árbol está vacío");
            return;
        }
        traversalArea.setText(title + ": " + order.stream()
                .map(Client::getDocument)
                .collect(Collectors.joining(" → ")));
    }

    private void refreshTree() {
        nodeTableModel.setRowCount(0);
        for (TreeNodeInfo node : tree.describeNodes(Client::getDocument)) {
            nodeTableModel.addRow(new Object[]{
                node.level(), node.value(), node.parent(), node.left(), node.right(), node.kind()
            });
        }

        if (tree.isEmpty()) {
            treeInfoLabel.setText("Árbol vacío");
        } else {
            treeInfoLabel.setText("Nodos: " + tree.size()
                    + "  |  Altura: " + tree.height()
                    + "  |  Mínimo: " + tree.min().getDocument()
                    + "  |  Máximo: " + tree.max().getDocument());
        }
    }

    // ========================================================
    // TAB 4: COMPARATIVA DEL ÁRBOL
    // ========================================================
    private JPanel buildCompareTab() {
        JPanel panel = new JPanel(new BorderLayout());

        compareCountField = new JTextField("5000", 8);
        panel.add(row(new JLabel("Elementos (1 a 30000):"), compareCountField,
                button("Comparar", e -> compareTreeShapes())), BorderLayout.NORTH);

        compareTableModel = readOnlyModel("Datos insertados", "Altura",
                "Inserción (µs)", "1000 búsquedas (µs)");
        panel.add(new JScrollPane(new JTable(compareTableModel)), BorderLayout.CENTER);

        compareMessage = new JLabel(
                "<html>El árbol no se balancea. Con datos ya ordenados degenera en una lista: "
                + "la altura es N y cada búsqueda recorre casi todos los nodos.<br>"
                + "Con datos en orden aleatorio la altura es del orden de log₂ N.</html>");
        panel.add(compareMessage, BorderLayout.SOUTH);

        return panel;
    }

    private void compareTreeShapes() {
        Integer n = parsePositive(compareCountField.getText());
        if (n == null || n > 30_000) {
            compareMessage.setText("Ingresa un entero entre 1 y 30000");
            return;
        }

        List<Integer> sorted = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            sorted.add(i);
        }
        List<Integer> shuffled = new ArrayList<>(sorted);
        Collections.shuffle(shuffled, new Random(42));

        // Las mismas claves de búsqueda para los dos árboles.
        Random keyRandom = new Random(7);
        int[] keys = new int[1000];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = 1 + keyRandom.nextInt(n);
        }

        compareTableModel.setRowCount(0);
        compareTableModel.addRow(measureTree("Ordenados (1, 2, 3...)", sorted, keys));
        compareTableModel.addRow(measureTree("En orden aleatorio", shuffled, keys));
    }

    private Object[] measureTree(String label, List<Integer> values, int[] keys) {
        BinarySearchTree<Integer> measured = BinarySearchTree.naturalOrder();

        long insertStart = System.nanoTime();
        for (int value : values) {
            measured.insert(value);
        }
        long insertMicros = (System.nanoTime() - insertStart) / 1_000;

        int height = measured.height();

        long searchStart = System.nanoTime();
        for (int key : keys) {
            measured.find(key);
        }
        long searchMicros = (System.nanoTime() - searchStart) / 1_000;

        return new Object[]{label, height, insertMicros, searchMicros};
    }

    // ========================================================
    // Utilidades
    // ========================================================
    private static DefaultTableModel readOnlyModel(Object... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private static JButton button(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        return button;
    }

    private static JPanel row(Component... items) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Component item : items) {
            panel.add(item);
        }
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private static Integer parsePositive(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String randomWord() {
        return WORDS[RANDOM.nextInt(WORDS.length)] + RANDOM.nextInt(100);
    }
}
