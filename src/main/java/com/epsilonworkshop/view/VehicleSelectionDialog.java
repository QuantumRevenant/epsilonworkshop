/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.controller.VehicleController;
import com.epsilonworkshop.model.Vehicle;
import com.epsilonworkshop.util.Validations;
import com.epsilonworkshop.util.tree.BinarySearchTree;
import com.epsilonworkshop.util.tree.BinaryTreeADT;
import com.epsilonworkshop.controller.ClientController;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.DocumentType;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sebap
 */
public class VehicleSelectionDialog extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(VehicleSelectionDialog.class.getName());

    private final VehicleController controller = new VehicleController();
    private final ClientController clientController = new ClientController();

    private final List<Vehicle> vehicles = new ArrayList<>();

    private Vehicle selectedVehicle;

    private DefaultTableModel tableModel;

    private TableRowSorter<DefaultTableModel> sorter;

    private BinaryTreeADT<Vehicle> plateIndex;

    private final Map<Long, Client> clientsById = new HashMap<>();

    /**
     * Creates new form VehicleSelectionDialog
     */
    public VehicleSelectionDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        configureTable();
        configureEvents();
        loadVehicles();

        pack();
        setLocationRelativeTo(parent);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private void configureEvents() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterVehicles();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterVehicles();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterVehicles();
            }
        });

        vehiclesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectButton.setEnabled(vehiclesTable.getSelectedRow() >= 0);
            }
        });

        vehiclesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2
                        && javax.swing.SwingUtilities.isLeftMouseButton(e)) {

                    int row = vehiclesTable.rowAtPoint(e.getPoint());

                    if (row >= 0) {
                        vehiclesTable.setRowSelectionInterval(row, row);
                        selectVehicle();
                    }
                }
            }
        });

        searchField.addActionListener(e -> selectByExactPlate());
    }

    private void configureTable() {
        tableModel = new DefaultTableModel(
                new Object[]{
                    "ID",
                    "Placa",
                    "Marca",
                    "Modelo",
                    "Año",
                    "Client ID",
                    "Tipo de documento",
                    "Documento",
                    "Cliente"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        vehiclesTable.setModel(tableModel);
        vehiclesTable.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );
        vehiclesTable.setRowHeight(24);

        sorter = new TableRowSorter<>(tableModel);
        vehiclesTable.setRowSorter(sorter);

        // Ocultar los identificadores internos.
        vehiclesTable.removeColumn(
                vehiclesTable.getColumnModel().getColumn(5)
        );
        vehiclesTable.removeColumn(
                vehiclesTable.getColumnModel().getColumn(0)
        );

        // Solo la última columna (Cliente) absorbe el espacio adicional.
        vehiclesTable.setAutoResizeMode(
                javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN
        );

        javax.swing.table.TableColumnModel columns
                = vehiclesTable.getColumnModel();

        columns.getColumn(0).setPreferredWidth(90);  // Placa
        columns.getColumn(1).setPreferredWidth(110); // Marca
        columns.getColumn(2).setPreferredWidth(110); // Modelo
        columns.getColumn(3).setPreferredWidth(55);  // Año
        columns.getColumn(4).setPreferredWidth(125); // Tipo documento
        columns.getColumn(5).setPreferredWidth(115); // Documento
        columns.getColumn(6).setPreferredWidth(220); // Cliente

        columns.getColumn(6).setMinWidth(180);

        vehiclesTable.setFillsViewportHeight(true);
        selectButton.setEnabled(false);
    }

    private void loadVehicles() {
        try {
            vehicles.clear();
            clientsById.clear();

            // Cargar clientes y crear un índice por ID.
            for (Client client : clientController.findAll()) {
                clientsById.put(client.getId(), client);
            }

            // Cargar únicamente vehículos activos.
            for (Vehicle vehicle : controller.findAll()) {
                if (Boolean.TRUE.equals(vehicle.getActive())) {
                    vehicles.add(vehicle);
                }
            }

            buildPlateIndex();
            refreshTable();

        } catch (SQLException e) {
            vehicles.clear();
            clientsById.clear();
            plateIndex = null;
            refreshTable();

            logger.log(
                    java.util.logging.Level.SEVERE,
                    "Error al cargar vehículos y clientes",
                    e
            );

            messageLabel.setText(
                    "No se pudieron cargar los vehículos y clientes."
            );
        }
    }

    private String getClientFullName(Client client) {
        if (client == null) {
            return "Cliente no disponible";
        }

        StringBuilder name = new StringBuilder();

        if (client.getName() != null) {
            name.append(client.getName());
        }

        if (client.getPaternalSurname() != null
                && !client.getPaternalSurname().isBlank()) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(client.getPaternalSurname());
        }

        if (client.getMaternalSurname() != null
                && !client.getMaternalSurname().isBlank()) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(client.getMaternalSurname());
        }

        return name.toString();
    }

    private void buildPlateIndex() {
        plateIndex = new BinarySearchTree<>(
                Comparator.comparing(vehicle -> normalizePlate(vehicle.getPlate()))
        );

        for (Vehicle vehicle : vehicles) {
            plateIndex.insert(vehicle);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Vehicle vehicle : vehicles) {
            Client client = clientsById.get(vehicle.getClientId());

            String documentType = client == null
                    ? ""
                    : client.getDocumentType().name();

            String document = client == null
                    ? ""
                    : client.getDocument();

            String clientName = getClientFullName(client);

            tableModel.addRow(new Object[]{
                vehicle.getId(),
                vehicle.getPlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear() == null ? "" : vehicle.getYear(),
                vehicle.getClientId(),
                documentType,
                document,
                clientName
            });
        }

        filterVehicles();
    }

    private void filterVehicles() {
        applyCurrentFilter();
    }

    private void applyCurrentFilter() {
        String search = normalize(searchField.getText());

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel, ? extends Integer> entry
            ) {
                if (search.isEmpty()) {
                    return true;
                }

                // Datos del vehículo.
                int[] vehicleColumns = {1, 2, 3, 4};

                for (int column : vehicleColumns) {
                    String value = normalize(
                            String.valueOf(entry.getValue(column))
                    );

                    if (value.contains(search)) {
                        return true;
                    }
                }

                // Datos del cliente.
                int[] clientColumns = {6, 7, 8};

                for (int column : clientColumns) {
                    String value = normalize(
                            String.valueOf(entry.getValue(column))
                    );

                    if (value.contains(search)) {
                        return true;
                    }
                }

                return false;
            }
        });

        updateResultsLabel();
    }

    private void updateResultsLabel() {
        int count = sorter == null
                ? tableModel.getRowCount()
                : sorter.getViewRowCount();

        resultsLabel.setText("Resultados: " + count);
    }

    private void selectVehicle() {
        int viewRow = vehiclesTable.getSelectedRow();

        if (viewRow < 0) {
            messageLabel.setText("Selecciona un vehículo.");
            return;
        }

        int modelRow = vehiclesTable.convertRowIndexToModel(viewRow);
        Object id = tableModel.getValueAt(modelRow, 0);

        for (Vehicle vehicle : vehicles) {
            if (Objects.equals(vehicle.getId(), id)) {
                selectedVehicle = vehicle;
                dispose();
                return;
            }
        }

        messageLabel.setText("No se pudo identificar el vehículo.");
    }

    public Vehicle getSelectedVehicle() {
        return selectedVehicle;
    }

    private static String normalizePlate(String raw) {
        if (raw == null) {
            return "";
        }

        String compact = raw.trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "")
                .replace(" ", "");

        return compact.length() == 6
                ? compact.substring(0, 3) + "-" + compact.substring(3)
                : compact;
    }

    private void selectByExactPlate() {
        String plate = normalizePlate(searchField.getText());

        if (Validations.isValidPlate(plate)) {
            if (plateIndex == null) {
                messageLabel.setText(
                        "El índice de vehículos no está disponible."
                );
                return;
            }

            Vehicle probe = new Vehicle();
            probe.setPlate(plate);

            Optional<Vehicle> found = plateIndex.find(probe);

            if (found.isEmpty()) {
                messageLabel.setText(
                        "No existe un vehículo activo con la placa " + plate
                );
                return;
            }

            selectedVehicle = found.get();
            dispose();
            return;
        }

        // Para DNI, CE, RUC, nombre o marca, conservar los resultados filtrados.
        filterVehicles();

        if (sorter.getViewRowCount() == 0) {
            messageLabel.setText("No se encontraron resultados.");
        } else {
            messageLabel.setText("Selecciona el vehículo que necesitas.");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        resultsPanel = new javax.swing.JPanel();
        resultsLabel = new javax.swing.JLabel();
        tablePane = new javax.swing.JScrollPane();
        vehiclesTable = new javax.swing.JTable();
        messageLabel = new javax.swing.JLabel();
        buttonsPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Seleccionar Vehículo");
        setModal(true);

        searchPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 8, 15));
        searchPanel.setLayout(new java.awt.GridLayout(2, 1));

        searchLabel.setText("Buscar vehículo:");
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        getContentPane().add(searchPanel, java.awt.BorderLayout.NORTH);

        resultsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultsPanel.setLayout(new java.awt.BorderLayout());

        resultsLabel.setText("Resultados");
        resultsPanel.add(resultsLabel, java.awt.BorderLayout.NORTH);

        vehiclesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Placa", "Marca", "Modelo", "Año", "Tipo de documento", "Documento", "Cliente"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        vehiclesTable.setFillsViewportHeight(true);
        vehiclesTable.setRowHeight(24);
        vehiclesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablePane.setViewportView(vehiclesTable);

        resultsPanel.add(tablePane, java.awt.BorderLayout.CENTER);

        messageLabel.setText(" ");
        resultsPanel.add(messageLabel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(resultsPanel, java.awt.BorderLayout.CENTER);

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 10));

        cancelButton.setText("Cancelar");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonsPanel.add(cancelButton);

        selectButton.setText("Seleccionar");
        selectButton.addActionListener(this::selectButtonActionPerformed);
        buttonsPanel.add(selectButton);

        getContentPane().add(buttonsPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        selectVehicle();
    }//GEN-LAST:event_selectButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        selectedVehicle = null;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                VehicleSelectionDialog dialog = new VehicleSelectionDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JButton selectButton;
    private javax.swing.JScrollPane tablePane;
    private javax.swing.JTable vehiclesTable;
    // End of variables declaration//GEN-END:variables
}
