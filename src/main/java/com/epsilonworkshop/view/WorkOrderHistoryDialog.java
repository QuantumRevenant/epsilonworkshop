/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.controller.WorkOrderController;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.WorkOrderChangeType;
import com.epsilonworkshop.model.WorkOrderHistory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author sebap
 */
public class WorkOrderHistoryDialog extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(WorkOrderHistoryDialog.class.getName());

    private final WorkOrderController controller = new WorkOrderController();

    private final User actingUser;
    private final Long actingMechanicId;
    private final Long orderId;

    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Creates new form WorkOrderHistoryDialog
     */
    public WorkOrderHistoryDialog(
            java.awt.Frame parent,
            boolean modal,
            Long orderId,
            User actingUser,
            Long actingMechanicId
    ) {
        super(parent, modal);
        initComponents();

        this.orderId = orderId;
        this.actingUser = actingUser;
        this.actingMechanicId = actingMechanicId;

        setLocationRelativeTo(parent);

        loadHistory();
    }

    private void loadHistory() {

        DefaultTableModel model
                = (DefaultTableModel) historyTable.getModel();

        model.setRowCount(0);

        try {
            List<WorkOrderHistory> records = controller.getHistory(
                    orderId,
                    actingUser,
                    actingMechanicId
            );

            for (WorkOrderHistory record : records) {

                String changedAt = record.getChangedAt() != null
                        ? record.getChangedAt().format(DATE_FORMAT)
                        : "";

                String username = record.getUserId() != null
                        ? "Usuario #" + record.getUserId()
                        : "Desconocido";

                String changeType = formatChangeType(
                        record.getChangeType()
                );

                String oldValue = formatHistoryValue(
                        record.getChangeType(),
                        record.getOldValue()
                );

                String newValue = formatHistoryValue(
                        record.getChangeType(),
                        record.getNewValue()
                );

                model.addRow(new Object[]{
                    changedAt,
                    username,
                    changeType,
                    oldValue,
                    newValue
                });
            }

            if (records.isEmpty()) {
                titleLabel.setText(
                        "Historial de la orden de trabajo (sin registros)"
                );
            }

        } catch (UnauthorizedActionException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Acceso denegado",
                    JOptionPane.WARNING_MESSAGE
            );

            dispose();

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cargar el historial: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            dispose();
        }
    }

    private String formatChangeType(WorkOrderChangeType changeType) {

        if (changeType == null) {
            return "Desconocido";
        }

        switch (changeType) {
            case STATUS_CHANGE:
                return "Cambio de estado";

            case MECHANIC_REASSIGNMENT:
                return "Asignación de mecánico";

            default:
                return changeType.name();
        }
    }

    private String formatHistoryValue(
            WorkOrderChangeType changeType,
            String value
    ) {

        if (value == null || value.isBlank()) {
            return "—";
        }

        if (changeType == WorkOrderChangeType.STATUS_CHANGE) {

            switch (value.toLowerCase()) {
                case "pending":
                    return "Pendiente";

                case "in_progress":
                    return "En progreso";

                case "finished":
                    return "Terminado";

                case "canceled":
                    return "Cancelado";

                default:
                    return value;
            }
        }

        if (changeType == WorkOrderChangeType.MECHANIC_REASSIGNMENT) {

            if (value.equalsIgnoreCase("sin asignar")) {
                return "Sin asignar";
            }

            return "Mecánico #" + value;
        }

        return value;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        historyPane = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Historial de orden de trabajo");
        setModal(true);
        setPreferredSize(new java.awt.Dimension(850, 400));

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        jPanel1.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        titleLabel.setText("Historial de la orden de trabajo");
        jPanel1.add(titleLabel, java.awt.BorderLayout.NORTH);

        historyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Fecha y hora", "Usuario", "Tipo de cambio", "Valor anterior", "Valor nuevo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        historyTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        historyTable.setRowSelectionAllowed(false);
        historyPane.setViewportView(historyTable);

        jPanel1.add(historyPane, java.awt.BorderLayout.CENTER);

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 10));

        closeButton.setText("Cerrar");
        closeButton.addActionListener(this::closeButtonActionPerformed);
        buttonPanel.add(closeButton);

        jPanel1.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            WorkOrderHistoryDialog dialog = new WorkOrderHistoryDialog(
                    new javax.swing.JFrame(),
                    true,
                    1L,
                    null,
                    null
            );

            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JScrollPane historyPane;
    private javax.swing.JTable historyTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
