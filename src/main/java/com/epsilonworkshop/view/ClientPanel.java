/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.config.SessionManager;
import com.epsilonworkshop.controller.ClientController;
import com.epsilonworkshop.exception.DuplicateDocumentException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.DocumentType;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.util.Validations;
import com.epsilonworkshop.util.list.DoublyLinkedList;
import com.epsilonworkshop.util.list.LinkedListADT;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.sql.SQLException;

/**
 *
 * @author sebap
 */
public class ClientPanel extends javax.swing.JPanel {

    private final ClientController controller = new ClientController();
    private final LinkedListADT<Client> clients = new DoublyLinkedList<>();
    private Client selected;

    /**
     * Creates new form ClientPanel
     */
    public ClientPanel() {
        initComponents();

        documentTypeCombo.setModel(
                new DefaultComboBoxModel(DocumentType.values())
        );

        deactivateButton.setEnabled(false);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        newButton.addActionListener(e -> clearForm());
        saveButton.addActionListener(e -> save());
        deactivateButton.addActionListener(e -> deactivate());

        clearForm();
        reload();
    }

    private void clearForm() {
        selected = null;
        table.clearSelection();

        documentTypeCombo.setSelectedIndex(0);
        documentField.setText("");
        nameField.setText("");
        paternalSurnameField.setText("");
        maternalSurnameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        notesArea.setText("");

        saveButton.setText("Crear cliente");
        deactivateButton.setEnabled(false);
    }

    private void onRowSelected() {
        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            return;
        }

        selected = clients.readAt(
                table.convertRowIndexToModel(viewRow)
        );

        documentTypeCombo.setSelectedItem(selected.getDocumentType());
        documentField.setText(selected.getDocument());
        nameField.setText(selected.getName());
        paternalSurnameField.setText(nullToEmpty(selected.getPaternalSurname()));
        maternalSurnameField.setText(nullToEmpty(selected.getMaternalSurname()));
        phoneField.setText(selected.getPhone());
        emailField.setText(nullToEmpty(selected.getEmail()));
        notesArea.setText(nullToEmpty(selected.getNotes()));

        saveButton.setText("Guardar cambios");
        deactivateButton.setEnabled(true);
    }

    private void reload() {
        try {
            clients.clear();

            for (Client client : controller.findAll()) {
                clients.insertLast(client);
            }

            refreshTable();

        } catch (SQLException e) {
            clients.clear();
            refreshTable();
            showMessage("No se pudieron cargar los clientes: " + e.getMessage(), true);
        }
    }

    private void refreshTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Client c : clients.toList()) {
            model.addRow(new Object[]{
                c.getId(),
                c.getDocumentType(),
                c.getDocument(),
                c.getName(),
                nullToEmpty(c.getPaternalSurname()),
                nullToEmpty(c.getMaternalSurname()),
                c.getPhone(),
                nullToEmpty(c.getEmail())
            });
        }
    }

    private void save() {
        Client client = readForm();

        if (client == null) {
            return;
        }

        User actingUser = SessionManager.getCurrentUser();

        try {
            String successMessage;

            if (selected == null) {
                controller.create(client, actingUser);
                successMessage = "Cliente creado";
            } else {
                client.setId(selected.getId());
                controller.update(client, actingUser);
                successMessage = "Cliente actualizado";
            }

            clearForm();
            reload();
            showMessage(successMessage, false);

        } catch (UnauthorizedActionException e) {
            showMessage("No tienes permiso para gestionar clientes", true);

        } catch (DuplicateDocumentException e) {
            showMessage(e.getMessage(), true);

        } catch (SQLException e) {
            showMessage("Error de base de datos: " + e.getMessage(), true);
        }
    }

    private Client readForm() {
        DocumentType type = (DocumentType) documentTypeCombo.getSelectedItem();

        String document = documentField.getText().trim();
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        String paternalSurname = emptyToNull(paternalSurnameField.getText());
        String maternalSurname = emptyToNull(maternalSurnameField.getText());
        String email = emptyToNull(emailField.getText());

        if (!Validations.isNotBlank(name)) {
            showMessage("El nombre es obligatorio", true);
            return null;
        }

        if (!Validations.isValidDocument(type, document)) {
            showMessage("Documento inválido (DNI: 8 dígitos, CE: 9, RUC: 11)", true);
            return null;
        }

        if (!Validations.isValidPhone(phone)) {
            showMessage("El teléfono no es válido", true);
            return null;
        }

        if (email != null && !Validations.isValidEmail(email)) {
            showMessage("El email no es válido", true);
            return null;
        }

        if (!fitsColumn(name) || !fitsColumn(paternalSurname)
                || !fitsColumn(maternalSurname) || !fitsColumn(phone)
                || !fitsColumn(email)) {
            showMessage("Ningún campo puede superar los 255 caracteres", true);
            return null;
        }

        String notes = emptyToNull(notesArea.getText());

        Client client = new Client();
        client.setDocumentType(type);
        client.setDocument(document);
        client.setName(name);
        client.setPaternalSurname(paternalSurname);
        client.setMaternalSurname(maternalSurname);
        client.setPhone(phone);
        client.setEmail(email);
        client.setNotes(notes);

        return client;
    }

    private void deactivate() {
        if (selected == null) {
            showMessage("Selecciona un cliente de la tabla", true);
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "¿Desactivar a " + selected.getName() + "?",
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
            showMessage("Cliente desactivado", false);

        } catch (UnauthorizedActionException e) {
            showMessage("No tienes permiso para gestionar clientes", true);

        } catch (SQLException e) {
            showMessage("Error de base de datos: " + e.getMessage(), true);
        }
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private static String emptyToNull(String text) {
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean fitsColumn(String text) {
        return text == null || Validations.isValidLength(text, 255);
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
        documentTypePanel = new javax.swing.JPanel();
        documentTypeLabel = new javax.swing.JLabel();
        documentTypeCombo = new javax.swing.JComboBox<>();
        documentPanel = new javax.swing.JPanel();
        documentLabel = new javax.swing.JLabel();
        documentField = new javax.swing.JTextField();
        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        paternalSurnamePanel = new javax.swing.JPanel();
        paternalSurnameLabel = new javax.swing.JLabel();
        paternalSurnameField = new javax.swing.JTextField();
        maternalSurnamePanel = new javax.swing.JPanel();
        maternalSurnameLabel = new javax.swing.JLabel();
        maternalSurnameField = new javax.swing.JTextField();
        phonePanel = new javax.swing.JPanel();
        phoneLabel = new javax.swing.JLabel();
        phoneField = new javax.swing.JTextField();
        emailPanel = new javax.swing.JPanel();
        emailLabel = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        notesPanel = new javax.swing.JPanel();
        notesLabel = new javax.swing.JLabel();
        notesAreaPane = new javax.swing.JScrollPane();
        notesArea = new javax.swing.JTextArea();
        tablePane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        messageLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new java.awt.BorderLayout());

        formPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        newButton.setText("Nuevo");
        buttonsPanel.add(newButton);

        saveButton.setText("Crear Cliente");
        buttonsPanel.add(saveButton);

        deactivateButton.setText("Desactivar");
        buttonsPanel.add(deactivateButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(buttonsPanel, gridBagConstraints);

        documentTypePanel.setLayout(new java.awt.BorderLayout());

        documentTypeLabel.setText("Tipo de documento");
        documentTypePanel.add(documentTypeLabel, java.awt.BorderLayout.CENTER);

        documentTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        documentTypePanel.add(documentTypeCombo, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(documentTypePanel, gridBagConstraints);

        documentPanel.setLayout(new java.awt.BorderLayout());

        documentLabel.setText("Documento");
        documentPanel.add(documentLabel, java.awt.BorderLayout.CENTER);
        documentPanel.add(documentField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(documentPanel, gridBagConstraints);

        namePanel.setLayout(new java.awt.BorderLayout());

        nameLabel.setText("Nombre");
        namePanel.add(nameLabel, java.awt.BorderLayout.CENTER);
        namePanel.add(nameField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
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
        gridBagConstraints.gridy = 1;
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(maternalSurnamePanel, gridBagConstraints);

        phonePanel.setLayout(new java.awt.BorderLayout());

        phoneLabel.setText("Teléfono");
        phonePanel.add(phoneLabel, java.awt.BorderLayout.CENTER);
        phonePanel.add(phoneField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(phonePanel, gridBagConstraints);

        emailPanel.setLayout(new java.awt.BorderLayout());

        emailLabel.setText("Email");
        emailPanel.add(emailLabel, java.awt.BorderLayout.CENTER);
        emailPanel.add(emailField, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(emailPanel, gridBagConstraints);

        notesPanel.setLayout(new java.awt.BorderLayout());

        notesLabel.setText("Notas");
        notesPanel.add(notesLabel, java.awt.BorderLayout.CENTER);

        notesArea.setColumns(20);
        notesArea.setRows(3);
        notesAreaPane.setViewportView(notesArea);

        notesPanel.add(notesAreaPane, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(notesPanel, gridBagConstraints);

        add(formPanel, java.awt.BorderLayout.PAGE_START);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Tipo", "Documento", "Nombre", "Apellido paterno", "Apellido materno", "Teléfono", "Email"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton deactivateButton;
    private javax.swing.JTextField documentField;
    private javax.swing.JLabel documentLabel;
    private javax.swing.JPanel documentPanel;
    private javax.swing.JComboBox<String> documentTypeCombo;
    private javax.swing.JLabel documentTypeLabel;
    private javax.swing.JPanel documentTypePanel;
    private javax.swing.JTextField emailField;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JPanel emailPanel;
    private javax.swing.JPanel formPanel;
    private javax.swing.JTextField maternalSurnameField;
    private javax.swing.JLabel maternalSurnameLabel;
    private javax.swing.JPanel maternalSurnamePanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JButton newButton;
    private javax.swing.JTextArea notesArea;
    private javax.swing.JScrollPane notesAreaPane;
    private javax.swing.JLabel notesLabel;
    private javax.swing.JPanel notesPanel;
    private javax.swing.JTextField paternalSurnameField;
    private javax.swing.JLabel paternalSurnameLabel;
    private javax.swing.JPanel paternalSurnamePanel;
    private javax.swing.JTextField phoneField;
    private javax.swing.JLabel phoneLabel;
    private javax.swing.JPanel phonePanel;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tablePane;
    // End of variables declaration//GEN-END:variables
}
