/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.config.SessionManager;
import com.epsilonworkshop.controller.MechanicController;
import com.epsilonworkshop.exception.DuplicateUsernameException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Mechanic;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.controller.UserController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author sebap
 */
public class MechanicPanel extends javax.swing.JPanel {

    private final MechanicController controller = new MechanicController();
    private final UserController userController = new UserController();

    private Mechanic selected;

    /**
     * Creates new form ClientPanel
     */
    public MechanicPanel() {
        initComponents();

        deactivateButton.setEnabled(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        clearForm();
        reload();
    }

    private void clearForm() {
        selected = null;

        usernameField.setText("");
        passwordPasswordField.setText("");
        confirmPasswordPasswordField.setText("");
        nameField.setText("");
        paternalSurnameField.setText("");
        maternalSurnameField.setText("");
        specialtyField.setText("");

        usernameField.setEditable(true);
        passwordPasswordField.setEnabled(true);
        confirmPasswordPasswordField.setEnabled(true);
        nameField.setEditable(true);
        paternalSurnameField.setEditable(true);
        maternalSurnameField.setEditable(true);
        specialtyField.setEditable(true);

        statusLabel.setText("—");

        saveButton.setText("Crear mecánico");
        deactivateButton.setEnabled(false);

        table.clearSelection();

        messageLabel.setText("");
    }

    private void onRowSelected() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        try {
            Long mechanicId = Long.valueOf(
                    table.getValueAt(selectedRow, 0).toString()
            );

            selected = controller.findById(mechanicId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró el mecánico seleccionado"
                    ));

            User user = userController.findById(
                    selected.getUserId(),
                    SessionManager.getCurrentUser()
            ).orElseThrow(() -> new IllegalArgumentException(
                    "No se encontró la cuenta del mecánico"
            ));

            usernameField.setText(user.getUsername());
            passwordPasswordField.setText("");
            confirmPasswordPasswordField.setText("");

            nameField.setText(user.getName());
            paternalSurnameField.setText(user.getPaternalSurname());
            maternalSurnameField.setText(user.getMaternalSurname());

            specialtyField.setText(
                    selected.getSpecialty() == null
                    ? ""
                    : selected.getSpecialty()
            );

            usernameField.setEditable(false);
            passwordPasswordField.setEnabled(false);
            confirmPasswordPasswordField.setEnabled(false);

            nameField.setEditable(false);
            paternalSurnameField.setEditable(false);
            maternalSurnameField.setEditable(false);

            specialtyField.setEditable(true);

            statusLabel.setText(
                    Boolean.TRUE.equals(selected.getActive())
                    ? "Activo"
                    : "Inactivo"
            );

            saveButton.setText("Guardar cambios");
            deactivateButton.setEnabled(true);

            messageLabel.setText("");

        } catch (SQLException | UnauthorizedActionException | IllegalArgumentException ex) {
            showMessage("Error al seleccionar mecánico: " + ex.getMessage(), true);
        }
    }

    private void reload() {
        try {
            List<Mechanic> mechanics = controller.findAll();
            refreshTable(mechanics);

        } catch (SQLException | UnauthorizedActionException ex) {
            showMessage("Error al cargar mecánicos: " + ex.getMessage(), true);
        }
    }

    private void refreshTable(List<Mechanic> mechanics) throws SQLException, UnauthorizedActionException {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        User actingUser = SessionManager.getCurrentUser();

        for (Mechanic mechanic : mechanics) {

            User user = userController.findById(
                    mechanic.getUserId(),
                    actingUser
            ).orElseThrow(() -> new IllegalStateException(
                    "No se encontró el usuario del mecánico con ID "
                    + mechanic.getUserId()
            ));

            model.addRow(new Object[]{
                mechanic.getId(),
                user.getUsername(),
                user.getName(),
                user.getPaternalSurname(),
                user.getMaternalSurname(),
                mechanic.getSpecialty(),
                Boolean.TRUE.equals(mechanic.getActive()) ? "Activo" : "Inactivo"
            });
        }
    }

    private void save() {
        User actingUser = SessionManager.getCurrentUser();

        String username = usernameField.getText().trim();
        String password = new String(passwordPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordPasswordField.getPassword());
        String name = nameField.getText().trim();

        String paternalSurname = emptyToNull(paternalSurnameField.getText());
        String maternalSurname = emptyToNull(maternalSurnameField.getText());
        String specialty = emptyToNull(specialtyField.getText());

        if (selected == null) {
            if (username.isEmpty()) {
                showMessage("El nombre de usuario es obligatorio", true);
                return;
            }

            if (password.isBlank()) {
                showMessage("La contraseña es obligatoria", true);
                return;
            }

            if (!password.equals(confirmPassword)) {
                showMessage("Las contraseñas no coinciden", true);
                return;
            }

            if (name.isEmpty()) {
                showMessage("El nombre es obligatorio", true);
                return;
            }
        }

        try {
            if (selected == null) {
                controller.create(
                        username,
                        password,
                        name,
                        paternalSurname,
                        maternalSurname,
                        specialty,
                        actingUser
                );

                clearForm();
                reload();
                showMessage("Mecánico registrado correctamente", false);

            } else {
                controller.update(
                        selected.getId(),
                        specialty,
                        actingUser
                );

                clearForm();
                reload();
                showMessage("Especialidad actualizada correctamente", false);
            }

        } catch (DuplicateUsernameException e) {
            showMessage(e.getMessage(), true);

        } catch (UnauthorizedActionException e) {
            showMessage("No tienes permiso para gestionar mecánicos", true);

        } catch (SQLException e) {
            showMessage("Error de base de datos: " + e.getMessage(), true);

        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage(), true);
        }
    }

    private void deactivate() {
        if (selected == null) {
            showMessage("Selecciona un mecánico de la tabla", true);
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas desactivar al mecánico "
                + usernameField.getText() + "?",
                "Confirmar desactivación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
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

            showMessage(
                    "Mecánico desactivado. Sus órdenes no finalizadas quedaron sin asignar.",
                    false
            );

        } catch (UnauthorizedActionException e) {
            showMessage("No tienes permiso para gestionar mecánicos", true);

        } catch (SQLException e) {
            showMessage("Error de base de datos: " + e.getMessage(), true);
        }
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
        usernamePanel = new javax.swing.JPanel();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        passwordPanel = new javax.swing.JPanel();
        passwordLabel = new javax.swing.JLabel();
        passwordPasswordField = new javax.swing.JPasswordField();
        confirmPasswordPanel = new javax.swing.JPanel();
        confirmPasswordLabel = new javax.swing.JLabel();
        confirmPasswordPasswordField = new javax.swing.JPasswordField();
        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        paternalSurnamePanel = new javax.swing.JPanel();
        paternalSurnameLabel = new javax.swing.JLabel();
        paternalSurnameField = new javax.swing.JTextField();
        maternalSurnamePanel = new javax.swing.JPanel();
        maternalSurnameLabel = new javax.swing.JLabel();
        maternalSurnameField = new javax.swing.JTextField();
        specialtyPanel = new javax.swing.JPanel();
        specialtyLabel = new javax.swing.JLabel();
        specialtyField = new javax.swing.JTextField();
        statusPanel = new javax.swing.JPanel();
        statusLabelLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        tablePane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        messageLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new java.awt.BorderLayout());

        formPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        newButton.setText("Nuevo");
        newButton.addActionListener(this::newButtonActionPerformed);
        buttonsPanel.add(newButton);

        saveButton.setText("Crear Mecánico");
        saveButton.addActionListener(this::saveButtonActionPerformed);
        buttonsPanel.add(saveButton);

        deactivateButton.setText("Desactivar");
        deactivateButton.addActionListener(this::deactivateButtonActionPerformed);
        buttonsPanel.add(deactivateButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(buttonsPanel, gridBagConstraints);

        usernamePanel.setLayout(new java.awt.BorderLayout());

        usernameLabel.setText("Usuario");
        usernamePanel.add(usernameLabel, java.awt.BorderLayout.CENTER);
        usernamePanel.add(usernameField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(usernamePanel, gridBagConstraints);

        passwordPanel.setLayout(new java.awt.BorderLayout());

        passwordLabel.setText("Contraseña");
        passwordPanel.add(passwordLabel, java.awt.BorderLayout.CENTER);
        passwordPanel.add(passwordPasswordField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(passwordPanel, gridBagConstraints);

        confirmPasswordPanel.setLayout(new java.awt.BorderLayout());

        confirmPasswordLabel.setText("Confirmar Contraseña");
        confirmPasswordPanel.add(confirmPasswordLabel, java.awt.BorderLayout.CENTER);
        confirmPasswordPanel.add(confirmPasswordPasswordField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(confirmPasswordPanel, gridBagConstraints);

        namePanel.setLayout(new java.awt.BorderLayout());

        nameLabel.setText("Nombre");
        namePanel.add(nameLabel, java.awt.BorderLayout.CENTER);
        namePanel.add(nameField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(namePanel, gridBagConstraints);

        paternalSurnamePanel.setLayout(new java.awt.BorderLayout());

        paternalSurnameLabel.setText("Apellido Paterno");
        paternalSurnamePanel.add(paternalSurnameLabel, java.awt.BorderLayout.CENTER);
        paternalSurnamePanel.add(paternalSurnameField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(paternalSurnamePanel, gridBagConstraints);

        maternalSurnamePanel.setLayout(new java.awt.BorderLayout());

        maternalSurnameLabel.setText("Apellido Materno");
        maternalSurnamePanel.add(maternalSurnameLabel, java.awt.BorderLayout.CENTER);
        maternalSurnamePanel.add(maternalSurnameField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(maternalSurnamePanel, gridBagConstraints);

        specialtyPanel.setLayout(new java.awt.BorderLayout());

        specialtyLabel.setText("Especialidad");
        specialtyPanel.add(specialtyLabel, java.awt.BorderLayout.CENTER);
        specialtyPanel.add(specialtyField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(specialtyPanel, gridBagConstraints);

        statusPanel.setLayout(new java.awt.BorderLayout());

        statusLabelLabel.setText("Estado");
        statusPanel.add(statusLabelLabel, java.awt.BorderLayout.CENTER);

        statusLabel.setText("-");
        statusPanel.add(statusLabel, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(statusPanel, gridBagConstraints);

        add(formPanel, java.awt.BorderLayout.PAGE_START);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Usuario", "Nombre", "Apellido paterno", "Apellido materno", "Especialidad", "Estado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
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

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        clearForm();
    }//GEN-LAST:event_newButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        save();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void deactivateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deactivateButtonActionPerformed
        deactivate();
    }//GEN-LAST:event_deactivateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JLabel confirmPasswordLabel;
    private javax.swing.JPanel confirmPasswordPanel;
    private javax.swing.JPasswordField confirmPasswordPasswordField;
    private javax.swing.JButton deactivateButton;
    private javax.swing.JPanel formPanel;
    private javax.swing.JTextField maternalSurnameField;
    private javax.swing.JLabel maternalSurnameLabel;
    private javax.swing.JPanel maternalSurnamePanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JButton newButton;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPanel passwordPanel;
    private javax.swing.JPasswordField passwordPasswordField;
    private javax.swing.JTextField paternalSurnameField;
    private javax.swing.JLabel paternalSurnameLabel;
    private javax.swing.JPanel paternalSurnamePanel;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextField specialtyField;
    private javax.swing.JLabel specialtyLabel;
    private javax.swing.JPanel specialtyPanel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusLabelLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tablePane;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JPanel usernamePanel;
    // End of variables declaration//GEN-END:variables
}
