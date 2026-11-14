/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.config.SessionManager;
import com.epsilonworkshop.controller.ClientController;
import com.epsilonworkshop.controller.VehicleController;
import com.epsilonworkshop.exception.DuplicatePlateException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.model.Vehicle;
import com.epsilonworkshop.util.Validations;
import com.epsilonworkshop.util.list.DoublyLinkedList;
import com.epsilonworkshop.util.list.LinkedListADT;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author sebap
 */
public class VehiclePanel extends javax.swing.JPanel {

    private final VehicleController controller = new VehicleController();
    private final ClientController clientController = new ClientController();

    private final LinkedListADT<Vehicle> vehicles = new DoublyLinkedList<>();

    private final Map<Long, Client> clientsById = new HashMap<>();

    private Vehicle selected;
    private Client selectedOwner;
    private Client transferTarget;

    public VehiclePanel() {
        initComponents();

        deactivateButton.setEnabled(false);
        transferButton.setEnabled(false);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        newButton.addActionListener(e -> clearForm());
        saveButton.addActionListener(e -> save());
        deactivateButton.addActionListener(e -> deactivate());
        transferButton.addActionListener(e -> transfer());

        reload();
        clearForm();
    }

    // ========================================================
    // Formulario
    // ========================================================
    private void clearForm() {
        selected = null;
        selectedOwner = null;
        transferTarget = null;

        table.clearSelection();

        ownerCreatePanel.setVisible(true);
        ownerPanel.setVisible(false);

        ownerCreateLabel.setText("Dueño");
        selectOwnerButton.setText("[Seleccionar cliente...]");
        selectOwnerButton.setEnabled(true);

        ownerLabel.setText(" ");

        plateField.setText("");
        brandField.setText("");
        modelField.setText("");
        yearField.setText("");
        notesArea.setText("");

        saveButton.setText("Crear vehículo");
        deactivateButton.setEnabled(false);
        transferButton.setEnabled(false);
    }

    private void onRowSelected() {
        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            return;
        }

        selected = vehicles.readAt(table.convertRowIndexToModel(viewRow));

        selectedOwner = null;
        transferTarget = null;

        ownerCreatePanel.setVisible(true);
        ownerPanel.setVisible(true);

        ownerCreateLabel.setText("Nuevo dueño");
        ownerLabel.setText(ownerText(selected.getClientId()));

        selectOwnerButton.setText("[Seleccionar nuevo dueño...]");
        selectOwnerButton.setEnabled(true);

        transferButton.setEnabled(false);

        plateField.setText(selected.getPlate());
        brandField.setText(selected.getBrand());
        modelField.setText(selected.getModel());

        yearField.setText(
                selected.getYear() == null
                ? ""
                : String.valueOf(selected.getYear())
        );

        notesArea.setText(nullToEmpty(selected.getNotes()));

        saveButton.setText("Guardar cambios");
        deactivateButton.setEnabled(true);
    }

    private void chooseOwner() {
        ClientSelectionDialog dialog = new ClientSelectionDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                true
        );

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Client client = dialog.getSelectedClient();

        if (client == null) {
            return;
        }

        if (selected == null) {
            // Creación: seleccionar dueño inicial.
            selectedOwner = client;
            selectOwnerButton.setText(clientDisplayName(client));

        } else {
            // Edición: seleccionar nuevo dueño.
            if (Objects.equals(client.getId(), selected.getClientId())) {
                showMessage("Ese cliente ya es el dueño del vehículo", true);
                return;
            }

            transferTarget = client;
            selectOwnerButton.setText(clientDisplayName(client));
            transferButton.setEnabled(true);
        }
    }

    private String clientDisplayName(Client client) {
        return client.getDocumentType() + " "
                + client.getDocument() + " — "
                + client.getName() + " "
                + nullToEmpty(client.getPaternalSurname()) + " "
                + nullToEmpty(client.getMaternalSurname());
    }

    // ========================================================
    // Datos
    // ========================================================
    private void reload() {
        try {
            clientsById.clear();

            for (Client client : clientController.findAll()) {
                clientsById.put(client.getId(), client);
            }

            vehicles.clear();

            for (Vehicle vehicle : controller.findAll()) {
                vehicles.insertLast(vehicle);
            }

            refreshTable();

        } catch (SQLException e) {
            clientsById.clear();
            vehicles.clear();

            refreshTable();

            showMessage(
                    "No se pudieron cargar los datos: " + e.getMessage(),
                    true
            );
        }
    }

    private void refreshTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Vehicle v : vehicles.toList()) {
            model.addRow(new Object[]{
                v.getId(),
                v.getPlate(),
                v.getBrand(),
                v.getModel(),
                v.getYear() == null ? "" : v.getYear(),
                ownerText(v.getClientId())
            });
        }
    }

    private String ownerText(Long clientId) {
        Client owner = clientsById.get(clientId);

        return owner == null
                ? "Cliente #" + clientId + " (desactivado)"
                : clientDisplayName(owner);
    }

    // ========================================================
    // Acciones
    // ========================================================
    private void save() {
        Vehicle vehicle = readForm();

        if (vehicle == null) {
            return;
        }

        User actingUser = SessionManager.getCurrentUser();

        // Al editar, "Guardar" nunca cambia el dueño (ADR 0009): solo lo hace
        // "Transferir dueño". Si se había elegido uno, se avisa para que no pase inadvertido.
        boolean pendingTransferIgnored = selected != null && transferTarget != null;

        try {
            String successMessage;

            if (selected == null) {
                controller.create(vehicle, actingUser);
                successMessage = "Vehículo creado";
            } else {
                vehicle.setId(selected.getId());
                controller.update(vehicle, actingUser);
                successMessage = "Vehículo actualizado";

                if (pendingTransferIgnored) {
                    successMessage += " (el nuevo dueño elegido no se aplicó: usa «Transferir dueño»)";
                }
            }

            clearForm();
            reload();
            showMessage(successMessage, false);

        } catch (UnauthorizedActionException e) {
            showMessage("No tienes permiso para gestionar vehículos", true);

        } catch (DuplicatePlateException e) {
            showMessage(e.getMessage(), true);

        } catch (IllegalArgumentException e) {
            reload();
            showMessage("Ese dato ya no existe: " + e.getMessage(), true);

        } catch (SQLException e) {
            showMessage("Error de base de datos: " + e.getMessage(), true);
        }
    }

    private void deactivate() {
        if (selected == null) {
            showMessage("Selecciona un vehículo de la tabla", true);
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "¿Desactivar el vehículo " + selected.getPlate() + "?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controller.softDelete(
                    selected.getId(),
                    SessionManager.getCurrentUser()
            );

            clearForm();
            reload();
            showMessage("Vehículo desactivado", false);

        } catch (UnauthorizedActionException e) {
            showMessage("No tienes permiso para gestionar vehículos", true);

        } catch (SQLException e) {
            showMessage("Error de base de datos: " + e.getMessage(), true);
        }
    }

    private void transfer() {
        if (selected == null) {
            showMessage("Selecciona un vehículo de la tabla", true);
            return;
        }

        if (transferTarget == null) {
            showMessage("Selecciona el nuevo dueño", true);
            return;
        }

        if (Objects.equals(transferTarget.getId(), selected.getClientId())) {
            showMessage("Ese cliente ya es el dueño del vehículo", true);
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "¿Transferir " + selected.getPlate()
                + " a " + clientDisplayName(transferTarget) + "?",
                "Confirmar transferencia",
                JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controller.transferOwnership(
                    selected.getId(),
                    transferTarget.getId(),
                    SessionManager.getCurrentUser()
            );

            clearForm();
            reload();
            showMessage("Vehículo transferido", false);

        } catch (UnauthorizedActionException e) {
            showMessage("No tienes permiso para transferir vehículos", true);

        } catch (IllegalArgumentException e) {
            reload();
            showMessage("Ese dato ya no existe: " + e.getMessage(), true);

        } catch (SQLException e) {
            showMessage("Error de base de datos: " + e.getMessage(), true);
        }
    }

    // ========================================================
    // Validación del formulario
    // ========================================================
    private Vehicle readForm() {
        String plate = normalizePlate(plateField.getText());
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String yearText = yearField.getText().trim();
        String notes = emptyToNull(notesArea.getText());

        Long clientId;

        if (selected == null) {
            if (selectedOwner == null) {
                showMessage("Selecciona un cliente para el vehículo", true);
                return null;
            }

            clientId = selectedOwner.getId();
        } else {
            clientId = selected.getClientId();
        }

        if (!Validations.isValidPlate(plate)) {
            showMessage("Placa inválida (formato ABC-123)", true);
            return null;
        }

        if (!Validations.isNotBlank(brand)
                || !Validations.isValidLength(brand, 255)) {
            showMessage("La marca es obligatoria (máximo 255 caracteres)", true);
            return null;
        }

        if (!Validations.isNotBlank(model)
                || !Validations.isValidLength(model, 255)) {
            showMessage("El modelo es obligatorio (máximo 255 caracteres)", true);
            return null;
        }

        Integer year = null;

        if (!yearText.isEmpty()) {
            try {
                year = Integer.valueOf(yearText);

            } catch (NumberFormatException e) {
                showMessage("El año debe ser un número entero", true);
                return null;
            }
        }

        if (!Validations.isValidYear(year)) {
            showMessage("Año inválido (entre 1886 y el año actual)", true);
            return null;
        }

        Vehicle vehicle = new Vehicle();

        vehicle.setClientId(clientId);
        vehicle.setPlate(plate);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setNotes(notes);

        return vehicle;
    }

    // ========================================================
    // Utilidades
    // ========================================================
    private static String normalizePlate(String raw) {
        String compact = raw.trim()
                .toUpperCase()
                .replace("-", "")
                .replace(" ", "");

        return compact.length() == 6
                ? compact.substring(0, 3) + "-" + compact.substring(3)
                : compact;
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private static String emptyToNull(String text) {
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void showMessage(String text, boolean error) {
        messageLabel.setForeground(
                error ? new Color(176, 0, 32) : new Color(0, 110, 0)
        );

        messageLabel.setText(text);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        formPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        deactivateButton = new javax.swing.JButton();
        transferButton = new javax.swing.JButton();
        ownerCreatePanel = new javax.swing.JPanel();
        ownerCreateLabel = new javax.swing.JLabel();
        selectOwnerButton = new javax.swing.JButton();
        platePanel = new javax.swing.JPanel();
        plateLabel = new javax.swing.JLabel();
        plateField = new javax.swing.JTextField();
        brandPanel = new javax.swing.JPanel();
        brandLabel = new javax.swing.JLabel();
        brandField = new javax.swing.JTextField();
        modelPanel = new javax.swing.JPanel();
        modelLabel = new javax.swing.JLabel();
        modelField = new javax.swing.JTextField();
        yearPanel = new javax.swing.JPanel();
        yearLabel = new javax.swing.JLabel();
        yearField = new javax.swing.JTextField();
        notesPanel = new javax.swing.JPanel();
        notesLabel = new javax.swing.JLabel();
        notesAreaPane = new javax.swing.JScrollPane();
        notesArea = new javax.swing.JTextArea();
        ownerPanel = new javax.swing.JPanel();
        ownerLabelLabel = new javax.swing.JLabel();
        ownerLabel = new javax.swing.JLabel();
        tablePane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        messageLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new java.awt.BorderLayout());

        formPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        newButton.setText("Nuevo");
        buttonsPanel.add(newButton);

        saveButton.setText("Crear Vehículo");
        buttonsPanel.add(saveButton);

        deactivateButton.setText("Desactivar");
        buttonsPanel.add(deactivateButton);

        transferButton.setText("Transferir Dueño");
        buttonsPanel.add(transferButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(buttonsPanel, gridBagConstraints);

        ownerCreatePanel.setLayout(new java.awt.BorderLayout());

        ownerCreateLabel.setText("Dueño");
        ownerCreatePanel.add(ownerCreateLabel, java.awt.BorderLayout.CENTER);

        selectOwnerButton.setText("[Seleccionar cliente...]");
        selectOwnerButton.addActionListener(this::selectOwnerButtonActionPerformed);
        ownerCreatePanel.add(selectOwnerButton, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(ownerCreatePanel, gridBagConstraints);

        platePanel.setLayout(new java.awt.BorderLayout());

        plateLabel.setText("Placa");
        platePanel.add(plateLabel, java.awt.BorderLayout.CENTER);
        platePanel.add(plateField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(platePanel, gridBagConstraints);

        brandPanel.setLayout(new java.awt.BorderLayout());

        brandLabel.setText("Marca");
        brandPanel.add(brandLabel, java.awt.BorderLayout.CENTER);
        brandPanel.add(brandField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(brandPanel, gridBagConstraints);

        modelPanel.setLayout(new java.awt.BorderLayout());

        modelLabel.setText("Modelo");
        modelPanel.add(modelLabel, java.awt.BorderLayout.CENTER);
        modelPanel.add(modelField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(modelPanel, gridBagConstraints);

        yearPanel.setLayout(new java.awt.BorderLayout());

        yearLabel.setText("Año (Opcional)");
        yearPanel.add(yearLabel, java.awt.BorderLayout.CENTER);
        yearPanel.add(yearField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(yearPanel, gridBagConstraints);

        notesPanel.setLayout(new java.awt.BorderLayout());

        notesLabel.setText("Notas");
        notesPanel.add(notesLabel, java.awt.BorderLayout.CENTER);

        notesArea.setColumns(20);
        notesArea.setRows(3);
        notesAreaPane.setViewportView(notesArea);

        notesPanel.add(notesAreaPane, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(notesPanel, gridBagConstraints);

        ownerPanel.setLayout(new java.awt.BorderLayout());

        ownerLabelLabel.setText("Dueño Actual");
        ownerPanel.add(ownerLabelLabel, java.awt.BorderLayout.CENTER);

        ownerLabel.setText(" ");
        ownerPanel.add(ownerLabel, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(ownerPanel, gridBagConstraints);

        add(formPanel, java.awt.BorderLayout.PAGE_START);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Placa", "Marca", "Modelo", "Año", "Dueño"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablePane.setViewportView(table);

        add(tablePane, java.awt.BorderLayout.CENTER);

        messageLabel.setText(" ");
        add(messageLabel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void selectOwnerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectOwnerButtonActionPerformed
        chooseOwner();
    }//GEN-LAST:event_selectOwnerButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField brandField;
    private javax.swing.JLabel brandLabel;
    private javax.swing.JPanel brandPanel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton deactivateButton;
    private javax.swing.JPanel formPanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextField modelField;
    private javax.swing.JLabel modelLabel;
    private javax.swing.JPanel modelPanel;
    private javax.swing.JButton newButton;
    private javax.swing.JTextArea notesArea;
    private javax.swing.JScrollPane notesAreaPane;
    private javax.swing.JLabel notesLabel;
    private javax.swing.JPanel notesPanel;
    private javax.swing.JLabel ownerCreateLabel;
    private javax.swing.JPanel ownerCreatePanel;
    private javax.swing.JLabel ownerLabel;
    private javax.swing.JLabel ownerLabelLabel;
    private javax.swing.JPanel ownerPanel;
    private javax.swing.JTextField plateField;
    private javax.swing.JLabel plateLabel;
    private javax.swing.JPanel platePanel;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton selectOwnerButton;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tablePane;
    private javax.swing.JButton transferButton;
    private javax.swing.JTextField yearField;
    private javax.swing.JLabel yearLabel;
    private javax.swing.JPanel yearPanel;
    // End of variables declaration//GEN-END:variables
}
