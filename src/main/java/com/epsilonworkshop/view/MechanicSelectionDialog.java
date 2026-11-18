/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.controller.MechanicController;
import com.epsilonworkshop.model.Mechanic;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.epsilonworkshop.controller.UserController;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.config.SessionManager;
import com.epsilonworkshop.util.tree.BinarySearchTree;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Role;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.Optional;

/**
 *
 * @author sebap
 */
public class MechanicSelectionDialog extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(
                    MechanicSelectionDialog.class.getName()
            );

    private final MechanicController controller = new MechanicController();

    private final List<Mechanic> mechanics = new ArrayList<>();

    private Mechanic selectedMechanic;

    private DefaultTableModel tableModel;

    private TableRowSorter<DefaultTableModel> sorter;

    private final UserController userController = new UserController();

    private final Map<Long, User> usersById = new HashMap<>();

    private BinarySearchTree<User> usersByUsername;

    public MechanicSelectionDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configureTable();
        configureEvents();
        loadMechanics();
        pack();
        setLocationRelativeTo(parent);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return Normalizer.normalize(
                text,
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private void configureEvents() {
        searchField.getDocument().addDocumentListener(
                new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterMechanics();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterMechanics();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterMechanics();
            }
        });

        mechanicsTable.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        selectButton.setEnabled(
                                mechanicsTable.getSelectedRow() >= 0
                        );
                    }
                });

        mechanicsTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2
                        && javax.swing.SwingUtilities
                                .isLeftMouseButton(e)) {

                    int row = mechanicsTable.rowAtPoint(e.getPoint());

                    if (row >= 0) {
                        mechanicsTable.setRowSelectionInterval(row, row);
                        selectMechanic();
                    }
                }
            }
        });
    }

    private void configureTable() {
        tableModel = new DefaultTableModel(
                new Object[]{
                    "ID",
                    "Nombre completo",
                    "Usuario",
                    "Especialidad",
                    "Cuenta"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        mechanicsTable.setModel(tableModel);

        mechanicsTable.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );

        mechanicsTable.setRowHeight(24);

        sorter = new TableRowSorter<>(tableModel);
        mechanicsTable.setRowSorter(sorter);

        mechanicsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        mechanicsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        mechanicsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        mechanicsTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        mechanicsTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        mechanicsTable.setFillsViewportHeight(true);

        selectButton.setEnabled(false);
    }

    private void loadMechanics() {
        try {
            mechanics.clear();
            usersById.clear();

            // Cargar todos los usuarios para recuperar sus datos.
            List<User> users = userController.findAllIncludingInactive(
                    SessionManager.getCurrentUser()
            );

            for (User user : users) {
                usersById.put(user.getId(), user);
            }

            // Construir el BST ordenado por username.
            usersByUsername = new BinarySearchTree<>(
                    (u1, u2) -> normalize(u1.getUsername())
                            .compareTo(normalize(u2.getUsername()))
            );

            // Cargar únicamente mecánicos activos.
            for (Mechanic mechanic : controller.findAll()) {
                if (Boolean.TRUE.equals(mechanic.getActive())) {
                    mechanics.add(mechanic);

                    User user = usersById.get(mechanic.getUserId());

                    if (user != null) {
                        usersByUsername.insert(user);
                    }
                }
            }

            refreshTable();

            if (mechanics.isEmpty()) {
                messageLabel.setText(
                        "No hay mecánicos activos disponibles."
                );
            } else {
                messageLabel.setText(" ");
            }

        } catch (SQLException | UnauthorizedActionException e) {
            mechanics.clear();
            usersById.clear();

            logger.log(
                    java.util.logging.Level.SEVERE,
                    "Error al cargar mecánicos",
                    e
            );

            messageLabel.setText(
                    "No se pudieron cargar los mecánicos."
            );
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        for (Mechanic mechanic : mechanics) {
            User user = usersById.get(mechanic.getUserId());

            String fullName = "Usuario no disponible";
            String username = "—";
            String accountStatus = "—";

            if (user != null) {
                fullName = ((user.getName() == null ? "" : user.getName()) + " "
                        + (user.getPaternalSurname() == null
                        ? "" : user.getPaternalSurname()) + " "
                        + (user.getMaternalSurname() == null
                        ? "" : user.getMaternalSurname())).trim();

                username = user.getUsername() == null
                        ? "—"
                        : user.getUsername();

                accountStatus = Boolean.TRUE.equals(user.getActive())
                        ? "Activa"
                        : "Inactiva";
            }

            String specialty = mechanic.getSpecialty() == null
                    ? "Sin especialidad"
                    : mechanic.getSpecialty();

            tableModel.addRow(new Object[]{
                mechanic.getId(),
                fullName,
                username,
                specialty,
                accountStatus
            });
        }

        filterMechanics();
    }

    private void filterMechanics() {
        String rawSearch = searchField.getText().trim();
        String search = normalize(rawSearch);

        mechanicsTable.clearSelection();
        selectButton.setEnabled(false);

        if (search.isEmpty()) {
            sorter.setRowFilter(null);
            updateResultsLabel();
            return;
        }

        // Primero intentamos una búsqueda exacta por username usando el BST.
        User probe = new User();
        probe.setUsername(rawSearch);

        Optional<User> exactUser = usersByUsername == null
                ? Optional.empty()
                : usersByUsername.find(probe);

        if (exactUser.isPresent()) {
            String exactUsername = normalize(
                    exactUser.get().getUsername()
            );

            sorter.setRowFilter(
                    new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(
                        Entry<? extends DefaultTableModel, ? extends Integer> entry
                ) {
                    return normalize(
                            String.valueOf(entry.getValue(2))
                    ).equals(exactUsername);
                }
            });

        } else {
            // Si no hay coincidencia exacta, buscamos parcialmente
            // en todos los campos visibles.
            sorter.setRowFilter(
                    new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(
                        Entry<? extends DefaultTableModel, ? extends Integer> entry
                ) {
                    for (int column = 0; column < 5; column++) {
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
        }

        updateResultsLabel();
    }

    private void updateResultsLabel() {
        resultsLabel.setText(
                "Resultados: " + sorter.getViewRowCount()
        );
    }

    private void selectMechanic() {
        int viewRow = mechanicsTable.getSelectedRow();

        if (viewRow < 0) {
            messageLabel.setText("Selecciona un mecánico.");
            return;
        }

        int modelRow = mechanicsTable.convertRowIndexToModel(viewRow);

        Object id = tableModel.getValueAt(modelRow, 0);

        for (Mechanic mechanic : mechanics) {
            if (mechanic.getId().equals(id)) {
                selectedMechanic = mechanic;
                dispose();
                return;
            }
        }

        messageLabel.setText(
                "No se pudo identificar el mecánico."
        );
    }

    public Mechanic getSelectedMechanic() {
        return selectedMechanic;
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
        mechanicsTable = new javax.swing.JTable();
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

        mechanicsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        mechanicsTable.setFillsViewportHeight(true);
        mechanicsTable.setRowHeight(24);
        mechanicsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablePane.setViewportView(mechanicsTable);

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
        selectMechanic();
    }//GEN-LAST:event_selectButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        selectedMechanic = null;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {

                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException
                | javax.swing.UnsupportedLookAndFeelException ex) {

            logger.log(
                    java.util.logging.Level.SEVERE,
                    null,
                    ex
            );
        }

        java.awt.EventQueue.invokeLater(() -> {

            // Usuario temporal exclusivamente para probar el diálogo.
            User testAdmin = new User();
            testAdmin.setId(-1L);
            testAdmin.setUsername("test_admin");
            testAdmin.setRole(Role.ADMIN);
            testAdmin.setActive(true);

            SessionManager.login(testAdmin, null);

            MechanicSelectionDialog dialog
                    = new MechanicSelectionDialog(
                            new javax.swing.JFrame(),
                            true
                    );

            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            // Limpiar la sesión temporal al cerrar el diálogo.
            SessionManager.logout();
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTable mechanicsTable;
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
