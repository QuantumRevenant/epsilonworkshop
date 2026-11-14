/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.controller.ClientController;
import com.epsilonworkshop.model.Client;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.text.Normalizer;
import java.util.Locale;

/**
 *
 * @author sebap
 */
public class ClientSelectionDialog extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ClientSelectionDialog.class.getName());

    private final ClientController controller = new ClientController();

    private List<Client> clients = new ArrayList<>();

    private Client selectedClient;

    private DefaultTableModel tableModel;

    private TableRowSorter<DefaultTableModel> sorter;

    /**
     * Creates new form ClientSelectionDialog
     */
    public ClientSelectionDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        configureTable();
        configureEvents();
        loadClients();

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
        // Detectar cambios en el buscador
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterClients();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterClients();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterClients();
            }
        });

        // Habilitar el botón cuando se seleccione una fila
        clientsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectButton.setEnabled(clientsTable.getSelectedRow() >= 0);
            }
        });
        clientsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2
                        && javax.swing.SwingUtilities.isLeftMouseButton(e)) {

                    int row = clientsTable.rowAtPoint(e.getPoint());

                    if (row >= 0) {
                        clientsTable.setRowSelectionInterval(row, row);
                        selectClient();
                    }
                }
            }
        });
    }

    private void configureTable() {
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Documento", "Nombre", "Apellidos", "Teléfono"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        clientsTable.setModel(tableModel);

        clientsTable.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );

        clientsTable.setRowHeight(24);
        clientsTable.setAutoCreateRowSorter(false);

        sorter = new TableRowSorter<>(tableModel);
        clientsTable.setRowSorter(sorter);

        // El ID se conserva en el modelo, pero no se muestra.
        clientsTable.removeColumn(
                clientsTable.getColumnModel().getColumn(0)
        );

        clientsTable.setFillsViewportHeight(true);

        selectButton.setEnabled(false);
    }

    private void loadClients() {
        try {
            clients = controller.findAll();
            refreshTable();

            if (clients.isEmpty()) {
                messageLabel.setText("No hay clientes registrados.");
            } else {
                messageLabel.setText(" ");
            }

        } catch (SQLException e) {
            clients = new ArrayList<>();
            refreshTable();

            messageLabel.setText("Error al cargar clientes.");
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudieron cargar los clientes: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Client client : clients) {
            String surnames = safe(client.getPaternalSurname())
                    + " " + safe(client.getMaternalSurname());

            String document = client.getDocumentType()
                    + " " + client.getDocument();

            tableModel.addRow(new Object[]{
                client.getId(),
                document,
                client.getName(),
                surnames.trim(),
                client.getPhone()
            });
        }

        updateResultsLabel();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void filterClients() {
        String text = normalize(searchField.getText().trim());

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(
                        Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                    for (int column = 1; column <= 4; column++) {
                        String value = normalize(
                                String.valueOf(entry.getValue(column))
                        );

                        if (value.contains(text)) {
                            return true;
                        }
                    }

                    return false;
                }
            });
        }

        updateResultsLabel();
    }

    private void updateResultsLabel() {
        int count = sorter == null
                ? tableModel.getRowCount()
                : sorter.getViewRowCount();

        resultsLabel.setText("Resultados: " + count);
    }

    private void selectClient() {
        int viewRow = clientsTable.getSelectedRow();

        if (viewRow < 0) {
            messageLabel.setText("Selecciona un cliente.");
            return;
        }

        int modelRow = clientsTable.convertRowIndexToModel(viewRow);
        Object id = tableModel.getValueAt(modelRow, 0);

        for (Client client : clients) {
            if (java.util.Objects.equals(client.getId(), id)) {
                selectedClient = client;
                dispose();
                return;
            }
        }

        messageLabel.setText("No se pudo identificar al cliente.");
    }

    public Client getSelectedClient() {
        return selectedClient;
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
        clientsTable = new javax.swing.JTable();
        messageLabel = new javax.swing.JLabel();
        buttonsPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Seleccionar Cliente");
        setModal(true);
        setPreferredSize(new java.awt.Dimension(650, 430));

        searchPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 8, 15));
        searchPanel.setLayout(new java.awt.GridLayout(2, 1));

        searchLabel.setText("Buscar cliente:");
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        getContentPane().add(searchPanel, java.awt.BorderLayout.NORTH);

        resultsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultsPanel.setLayout(new java.awt.BorderLayout());

        resultsLabel.setText("Resultados");
        resultsPanel.add(resultsLabel, java.awt.BorderLayout.NORTH);

        clientsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Documento", "Nombre", "Apellidos", "Teléfono"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        clientsTable.setFillsViewportHeight(true);
        clientsTable.setRowHeight(24);
        clientsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablePane.setViewportView(clientsTable);

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
        selectClient();
    }//GEN-LAST:event_selectButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        selectedClient = null;
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
                ClientSelectionDialog dialog = new ClientSelectionDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JTable clientsTable;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JButton selectButton;
    private javax.swing.JScrollPane tablePane;
    // End of variables declaration//GEN-END:variables
}
