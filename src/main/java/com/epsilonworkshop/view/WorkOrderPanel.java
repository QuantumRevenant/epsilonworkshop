/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.epsilonworkshop.view;

import com.epsilonworkshop.config.SessionManager;
import com.epsilonworkshop.controller.MechanicController;
import com.epsilonworkshop.controller.UserController;
import com.epsilonworkshop.controller.VehicleController;
import com.epsilonworkshop.controller.WorkOrderController;
import com.epsilonworkshop.model.Mechanic;
import com.epsilonworkshop.model.OrderStatus;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.model.WorkOrder;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.Vehicle;
import com.epsilonworkshop.exception.InvalidMechanicAssignmentException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.exception.InvalidStatusTransitionException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import java.util.Comparator;

/**
 *
 * @author sebap
 */
public class WorkOrderPanel extends javax.swing.JPanel {

    private final WorkOrderController controller = new WorkOrderController();
    private final UserController userController = new UserController();

    private WorkOrder selected;

    private Long selectedVehicleId;
    private Long selectedMechanicId;

    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VehicleController vehicleController
            = new VehicleController();

    private final MechanicController mechanicController
            = new MechanicController();

    private List<WorkOrder> loadedOrders = new ArrayList<>();

    private Map<Long, Vehicle> vehiclesById = new HashMap<>();
    private Map<Long, Mechanic> mechanicsById = new HashMap<>();
    private Map<Long, User> usersById = new HashMap<>();

    private TableRowSorter<DefaultTableModel> tableSorter;

    public WorkOrderPanel() {
        initComponents();

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        tableSorter = new TableRowSorter<>(model);
        table.setRowSorter(tableSorter);

        tableSorter.setComparator(5, (value1, value2) -> {
            String date1 = value1 == null ? "" : value1.toString();
            String date2 = value2 == null ? "" : value2.toString();

            if (date1.isEmpty() && date2.isEmpty()) {
                return 0;
            }
            if (date1.isEmpty()) {
                return -1;
            }
            if (date2.isEmpty()) {
                return 1;
            }

            return java.time.LocalDateTime.parse(
                    date1,
                    DATE_FORMAT
            ).compareTo(
                    java.time.LocalDateTime.parse(
                            date2,
                            DATE_FORMAT
                    )
            );
        });

        configureFilterControls();

        clearForm();
        configurePermissions();
        reload();
    }

    private void configureFilterControls() {

        statusFilterCombo.setModel(
                new DefaultComboBoxModel<>(new String[]{
            "Todos",
            "Pendiente",
            "En progreso",
            "Terminado",
            "Cancelado"
        })
        );

        assignmentFilterCombo.setModel(
                new DefaultComboBoxModel<>(new String[]{
            "Todas",
            "Sin asignar",
            "Asignadas"
        })
        );

        mechanicFilterCombo.setModel(
                new DefaultComboBoxModel<>(new String[]{
            "Todos"
        })
        );

        statusFilterCombo.addActionListener(e -> applyFilters());
        assignmentFilterCombo.addActionListener(e -> applyFilters());
        mechanicFilterCombo.addActionListener(e -> applyFilters());

        boolean isAdmin = SessionManager.getCurrentUser().getRole() == Role.ADMIN;

        mechanicFilterCombo.setVisible(isAdmin);
        mechanicFilterLabel.setVisible(isAdmin);
    }

    private void populateMechanicFilter() {

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Todos");

        List<String> mechanicNames = new ArrayList<>();

        for (Mechanic mechanic : mechanicsById.values()) {
            mechanicNames.add(getMechanicDisplayName(mechanic.getId()));
        }

        mechanicNames.sort(String.CASE_INSENSITIVE_ORDER);

        for (String name : mechanicNames) {
            if (!mechanicNamesAlreadyAdded(model, name)) {
                model.addElement(name);
            }
        }

        mechanicFilterCombo.setModel(model);
    }

    private boolean mechanicNamesAlreadyAdded(
            DefaultComboBoxModel<String> model,
            String name
    ) {
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    private void applyFilters() {

        if (tableSorter == null) {
            return;
        }

        String selectedStatus
                = (String) statusFilterCombo.getSelectedItem();

        String selectedAssignment
                = (String) assignmentFilterCombo.getSelectedItem();

        String selectedMechanic
                = (String) mechanicFilterCombo.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Filtro por estado.
        if (selectedStatus != null && !selectedStatus.equals("Todos")) {

            OrderStatus expectedStatus;

            switch (selectedStatus) {
                case "Pendiente":
                    expectedStatus = OrderStatus.PENDING;
                    break;
                case "En progreso":
                    expectedStatus = OrderStatus.IN_PROGRESS;
                    break;
                case "Terminado":
                    expectedStatus = OrderStatus.FINISHED;
                    break;
                case "Cancelado":
                    expectedStatus = OrderStatus.CANCELED;
                    break;
                default:
                    expectedStatus = null;
            }

            if (expectedStatus != null) {
                filters.add(new RowFilter<Object, Object>() {
                    @Override
                    public boolean include(
                            Entry<?, ?> entry
                    ) {
                        return entry.getValue(4) == expectedStatus;
                    }
                });
            }
        }

        // Filtro por asignación.
        if (selectedAssignment != null
                && !selectedAssignment.equals("Todas")) {

            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {

                    boolean unassigned
                            = "Sin asignar".equals(entry.getValue(3));

                    return selectedAssignment.equals("Sin asignar")
                            ? unassigned
                            : !unassigned;
                }
            });
        }

        // Filtro por mecánico.
        if (selectedMechanic != null
                && !selectedMechanic.equals("Todos")) {

            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    return selectedMechanic.equals(
                            String.valueOf(entry.getValue(3))
                    );
                }
            });
        }

        // Aplicar todos los filtros simultáneamente.
        if (filters.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(
                    RowFilter.andFilter(filters)
            );
        }

        // Evitar mantener seleccionada una orden que ya no se muestra.
        clearForm();
    }

    private void configurePermissions() {
        User currentUser = SessionManager.getCurrentUser();

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isMechanic = currentUser.getRole() == Role.MECHANIC;

        selectMechanicButton.setVisible(isAdmin || isMechanic);

        selectMechanicButton.setText(
                isAdmin ? "Seleccionar mecánico" : "Asignarme a mí"
        );

        changeStatusButton.setEnabled(false);
        historyButton.setEnabled(false);
    }

    private void clearForm() {
        selected = null;
        statusComboBox.setModel(
                new DefaultComboBoxModel<>(new String[0])
        );
        selectedVehicleId = null;
        selectedMechanicId = null;

        selectedVehicleLabel.setText("Ningún vehículo seleccionado");
        selectedMechanicLabel.setText("Sin asignar");
        descriptionTextArea.setText("");

        newstatusPanel.setVisible(false);

        saveButton.setText("Crear orden");
        saveButton.setEnabled(true);

        changeStatusButton.setEnabled(false);
        historyButton.setEnabled(false);

        table.clearSelection();
        messageLabel.setText("");

        selectVehicleButton.setEnabled(true);
        selectMechanicButton.setEnabled(true);

        configurePermissions();
    }

    private void onRowSelected() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        selected = loadedOrders.get(modelRow);
        configureStatusOptions();

        selectedVehicleId = selected.getVehicleId();
        selectedMechanicId = selected.getMechanicId();

        // Mostrar vehículo seleccionado.
        Vehicle vehicle = vehiclesById.get(selectedVehicleId);

        if (vehicle != null) {
            selectedVehicleLabel.setText(
                    vehicle.getBrand() + " " + vehicle.getModel()
            );
        } else {
            selectedVehicleLabel.setText("Vehículo #" + selectedVehicleId);
        }

        // Mostrar nombre completo del mecánico asignado.
        if (selectedMechanicId != null) {
            Mechanic mechanic = mechanicsById.get(selectedMechanicId);
            String mechanicName = "Mecánico #" + selectedMechanicId;

            if (mechanic != null) {
                User mechanicUser = usersById.get(mechanic.getUserId());

                if (mechanicUser != null) {
                    StringBuilder fullName = new StringBuilder();

                    if (mechanicUser.getName() != null) {
                        fullName.append(mechanicUser.getName()).append(" ");
                    }

                    if (mechanicUser.getPaternalSurname() != null) {
                        fullName.append(mechanicUser.getPaternalSurname()).append(" ");
                    }

                    if (mechanicUser.getMaternalSurname() != null) {
                        fullName.append(mechanicUser.getMaternalSurname());
                    }

                    String formattedName = fullName.toString().trim();

                    if (!formattedName.isEmpty()) {
                        mechanicName = formattedName;
                    }
                }
            }

            selectedMechanicLabel.setText(mechanicName);

        } else {
            selectedMechanicLabel.setText("Sin asignar");
        }

        descriptionTextArea.setText(selected.getDescription());

        User currentUser = SessionManager.getCurrentUser();

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isMechanic = currentUser.getRole() == Role.MECHANIC;

        Long currentMechanicId = SessionManager.getCurrentMechanicId();

        boolean isAssignedToCurrentMechanic
                = isMechanic
                && selectedMechanicId != null
                && selectedMechanicId.equals(currentMechanicId);

        boolean isFinal
                = selected.getStatus() == OrderStatus.FINISHED
                || selected.getStatus() == OrderStatus.CANCELED;

        saveButton.setEnabled(false);
        selectVehicleButton.setEnabled(false);

        selectMechanicButton.setVisible(isAdmin || isMechanic);

        selectMechanicButton.setEnabled(
                !isFinal
                && (isAdmin
                || (isMechanic && selectedMechanicId == null))
        );

        selectMechanicButton.setText(
                isAdmin ? "Cambiar mecánico" : "Asignarme a mí"
        );

        changeStatusButton.setEnabled(
                !isFinal && (isAdmin || isAssignedToCurrentMechanic)
        );

        historyButton.setEnabled(true);
        newstatusPanel.setVisible(true);

        messageLabel.setText("");
    }

    private void reload() {
        try {
            User actingUser = SessionManager.getCurrentUser();
            Long actingMechanicId = SessionManager.getCurrentMechanicId();

            List<Vehicle> vehicles = vehicleController.findAll();
            List<Mechanic> mechanics = mechanicController.findAll();

            vehiclesById = new HashMap<>();
            for (Vehicle vehicle : vehicles) {
                vehiclesById.put(vehicle.getId(), vehicle);
            }

            mechanicsById = new HashMap<>();
            for (Mechanic mechanic : mechanics) {
                mechanicsById.put(mechanic.getId(), mechanic);
            }

            usersById = new HashMap<>();

            if (actingUser.getRole() == Role.ADMIN) {
                List<User> users = userController.findAllIncludingInactive(
                        actingUser
                );

                for (User user : users) {
                    usersById.put(user.getId(), user);
                }

            } else {
                usersById.put(actingUser.getId(), actingUser);
            }

            List<WorkOrder> orders = controller.findVisibleWorkOrders(
                    actingUser,
                    actingMechanicId
            );

            populateMechanicFilter();
            refreshTable(orders);
            applyFilters();

        } catch (SQLException
                | UnauthorizedActionException
                | IllegalArgumentException ex) {

            showMessage(
                    "Error al cargar órdenes: " + ex.getMessage(),
                    true
            );
        }
    }

    private void refreshTable(List<WorkOrder> orders) {
        loadedOrders = new ArrayList<>(orders);

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (WorkOrder order : loadedOrders) {
            Vehicle vehicle = vehiclesById.get(order.getVehicleId());

            String plate = vehicle == null
                    ? "—"
                    : vehicle.getPlate();

            String vehicleDescription = vehicle == null
                    ? "Vehículo #" + order.getVehicleId()
                    : vehicle.getBrand() + " " + vehicle.getModel();

            String mechanicDescription;

            if (order.getMechanicId() == null) {
                mechanicDescription = "Sin asignar";
            } else {
                Mechanic mechanic = mechanicsById.get(order.getMechanicId());

                if (mechanic == null) {
                    mechanicDescription = "Mecánico #" + order.getMechanicId();
                } else {
                    User mechanicUser = usersById.get(mechanic.getUserId());

                    if (mechanicUser == null) {
                        mechanicDescription = "Mecánico #" + mechanic.getId();
                    } else {
                        StringBuilder fullName = new StringBuilder();

                        if (mechanicUser.getName() != null) {
                            fullName.append(mechanicUser.getName()).append(" ");
                        }

                        if (mechanicUser.getPaternalSurname() != null) {
                            fullName.append(mechanicUser.getPaternalSurname()).append(" ");
                        }

                        if (mechanicUser.getMaternalSurname() != null) {
                            fullName.append(mechanicUser.getMaternalSurname());
                        }

                        mechanicDescription = fullName.toString().trim();

                        if (mechanicDescription.isEmpty()) {
                            mechanicDescription = "Mecánico #" + mechanic.getId();
                        }
                    }
                }
            }

            model.addRow(new Object[]{
                order.getId(),
                plate,
                vehicleDescription,
                mechanicDescription,
                order.getStatus(),
                order.getCreatedAt() == null
                ? ""
                : order.getCreatedAt().format(DATE_FORMAT)
            });
        }
    }

    private void save() {
        if (selected != null) {
            showMessage(
                    "Pulsa Nuevo para crear otra orden de trabajo.",
                    true
            );
            return;
        }

        if (selectedVehicleId == null) {
            showMessage(
                    "Debes seleccionar un vehículo.",
                    true
            );
            return;
        }

        try {
            User actingUser = SessionManager.getCurrentUser();
            Long actingMechanicId = SessionManager.getCurrentMechanicId();

            String description = descriptionTextArea.getText().trim();

            WorkOrder created = controller.createWorkOrder(
                    selectedVehicleId,
                    selectedMechanicId,
                    description,
                    actingUser,
                    actingMechanicId
            );

            clearForm();
            reload();

            showMessage(
                    "Orden de trabajo #" + created.getId()
                    + " creada correctamente.",
                    false
            );

        } catch (InvalidMechanicAssignmentException ex) {
            showMessage(ex.getMessage(), true);

        } catch (UnauthorizedActionException ex) {
            showMessage(ex.getMessage(), true);

        } catch (SQLException ex) {
            showMessage(
                    "Error al guardar la orden: " + ex.getMessage(),
                    true
            );

        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage(), true);
        }
    }

    private void changeStatus() {
        if (selected == null) {
            showMessage(
                    "Debes seleccionar una orden de trabajo.",
                    true
            );
            return;
        }

        String selectedStatus = (String) statusComboBox.getSelectedItem();

        if (selectedStatus == null) {
            showMessage("Debes seleccionar un estado.", true);
            return;
        }

        OrderStatus newStatus;

        switch (selectedStatus) {
            case "En Progreso":
                newStatus = OrderStatus.IN_PROGRESS;
                break;

            case "Terminado":
                newStatus = OrderStatus.FINISHED;
                break;

            case "Cancelado":
                newStatus = OrderStatus.CANCELED;
                break;

            default:
                showMessage("El estado seleccionado no es válido.", true);
                return;
        }

        try {
            User actingUser = SessionManager.getCurrentUser();
            Long actingMechanicId = SessionManager.getCurrentMechanicId();

            controller.changeStatus(
                    selected.getId(),
                    newStatus,
                    actingUser,
                    actingMechanicId
            );

            reload();
            clearForm();

            showMessage(
                    "Estado de la orden actualizado correctamente.",
                    false
            );

        } catch (InvalidStatusTransitionException
                | UnauthorizedActionException ex) {

            showMessage(ex.getMessage(), true);

        } catch (SQLException ex) {
            showMessage(
                    "Error al actualizar el estado: " + ex.getMessage(),
                    true
            );

        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage(), true);
        }
    }

    private void showMessage(String text, boolean error) {
        messageLabel.setForeground(
                error ? new Color(176, 0, 32) : new Color(0, 110, 0)
        );

        messageLabel.setText(text);
    }

    private void chooseVehicle() {
        VehicleSelectionDialog dialog = new VehicleSelectionDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                true
        );

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Vehicle vehicle = dialog.getSelectedVehicle();

        if (vehicle == null) {
            return;
        }

        selectedVehicleId = vehicle.getId();

        selectedVehicleLabel.setText(
                vehicle.getPlate() + " - "
                + vehicle.getBrand() + " "
                + vehicle.getModel()
        );

        messageLabel.setText("");
    }

    private void chooseMechanic() {
        User currentUser = SessionManager.getCurrentUser();

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isMechanic = currentUser.getRole() == Role.MECHANIC;

        if (!isAdmin && !isMechanic) {
            showMessage("No tienes permisos para asignar mecánicos.", true);
            return;
        }

        // CASO 1: Estamos creando una orden.
        if (selected == null) {

            if (isAdmin) {
                MechanicSelectionDialog dialog = new MechanicSelectionDialog(
                        (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                        true
                );

                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);

                Mechanic mechanic = dialog.getSelectedMechanic();

                if (mechanic == null) {
                    return;
                }

                selectedMechanicId = mechanic.getId();

            } else {
                Long currentMechanicId = SessionManager.getCurrentMechanicId();

                if (currentMechanicId == null) {
                    showMessage(
                            "Tu usuario no tiene un mecánico asociado.",
                            true
                    );
                    return;
                }

                selectedMechanicId = currentMechanicId;
            }

            selectedMechanicLabel.setText(
                    getMechanicDisplayName(selectedMechanicId)
            );

            messageLabel.setText("");
            return;
        }

        // CASO 2: Estamos trabajando con una orden existente.
        Long newMechanicId;

        if (isAdmin) {
            MechanicSelectionDialog dialog = new MechanicSelectionDialog(
                    (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                    true
            );

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            Mechanic mechanic = dialog.getSelectedMechanic();

            if (mechanic == null) {
                return;
            }

            newMechanicId = mechanic.getId();

            if (newMechanicId.equals(selected.getMechanicId())) {
                showMessage(
                        "La orden ya está asignada a ese mecánico.",
                        false
                );
                return;
            }

        } else {
            if (selected.getMechanicId() != null) {
                showMessage(
                        "Esta orden ya tiene un mecánico asignado.",
                        true
                );
                return;
            }

            newMechanicId = SessionManager.getCurrentMechanicId();

            if (newMechanicId == null) {
                showMessage(
                        "Tu usuario no tiene un mecánico asociado.",
                        true
                );
                return;
            }
        }

        try {
            controller.reassignMechanic(
                    selected.getId(),
                    newMechanicId,
                    currentUser,
                    SessionManager.getCurrentMechanicId()
            );

            reload();
            clearForm();

            showMessage(
                    "Mecánico asignado correctamente.",
                    false
            );

        } catch (InvalidMechanicAssignmentException
                | UnauthorizedActionException
                | SQLException
                | IllegalArgumentException e) {

            showMessage(e.getMessage(), true);
        }
    }

    private String getMechanicDisplayName(Long mechanicId) {
        if (mechanicId == null) {
            return "Sin asignar";
        }

        Mechanic mechanic = mechanicsById.get(mechanicId);

        if (mechanic == null) {
            return "Mecánico #" + mechanicId;
        }

        User mechanicUser = usersById.get(mechanic.getUserId());

        if (mechanicUser == null) {
            return "Mecánico #" + mechanicId;
        }

        StringBuilder fullName = new StringBuilder();

        if (mechanicUser.getName() != null) {
            fullName.append(mechanicUser.getName()).append(" ");
        }

        if (mechanicUser.getPaternalSurname() != null) {
            fullName.append(mechanicUser.getPaternalSurname()).append(" ");
        }

        if (mechanicUser.getMaternalSurname() != null) {
            fullName.append(mechanicUser.getMaternalSurname());
        }

        String name = fullName.toString().trim();

        return name.isEmpty() ? "Mecánico #" + mechanicId : name;
    }

    private void configureStatusOptions() {
        DefaultComboBoxModel<String> model
                = new DefaultComboBoxModel<>();

        if (selected == null) {
            statusComboBox.setModel(model);
            return;
        }

        switch (selected.getStatus()) {
            case PENDING:
                model.addElement("En Progreso");
                model.addElement("Cancelado");
                break;

            case IN_PROGRESS:
                model.addElement("Terminado");
                model.addElement("Cancelado");
                break;

            case FINISHED:
            case CANCELED:
                break;
        }

        statusComboBox.setModel(model);
    }

    private void showHistory() {

        if (selected == null) {
            showMessage(
                    "Debes seleccionar una orden de trabajo.",
                    true
            );
            return;
        }

        try {
            User actingUser = SessionManager.getCurrentUser();
            Long actingMechanicId = SessionManager.getCurrentMechanicId();

            WorkOrderHistoryDialog dialog = new WorkOrderHistoryDialog(
                    (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                    true,
                    selected.getId(),
                    actingUser,
                    actingMechanicId
            );

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage(), true);
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
        java.awt.GridBagConstraints gridBagConstraints;

        formPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        changeStatusButton = new javax.swing.JButton();
        historyButton = new javax.swing.JButton();
        vehiclePanel = new javax.swing.JPanel();
        vehicleLabel = new javax.swing.JLabel();
        selectVehicleButton = new javax.swing.JButton();
        selectedVehicleLabel = new javax.swing.JLabel();
        mechanicPanel = new javax.swing.JPanel();
        mechanicLabel = new javax.swing.JLabel();
        selectMechanicButton = new javax.swing.JButton();
        selectedMechanicLabel = new javax.swing.JLabel();
        newstatusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox<>();
        descriptionPanel = new javax.swing.JPanel();
        descriptionLabel = new javax.swing.JLabel();
        descriptionPane = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        filterPanel = new javax.swing.JPanel();
        statusFilterLabel = new javax.swing.JLabel();
        assignmentFilterLabel = new javax.swing.JLabel();
        mechanicFilterLabel = new javax.swing.JLabel();
        statusFilterCombo = new javax.swing.JComboBox<>();
        assignmentFilterCombo = new javax.swing.JComboBox<>();
        mechanicFilterCombo = new javax.swing.JComboBox<>();
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

        saveButton.setText("Crear orden");
        saveButton.addActionListener(this::saveButtonActionPerformed);
        buttonsPanel.add(saveButton);

        changeStatusButton.setText("Actualizar estado");
        changeStatusButton.addActionListener(this::changeStatusButtonActionPerformed);
        buttonsPanel.add(changeStatusButton);

        historyButton.setText("Ver historial");
        historyButton.addActionListener(this::historyButtonActionPerformed);
        buttonsPanel.add(historyButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(buttonsPanel, gridBagConstraints);

        vehiclePanel.setLayout(new java.awt.BorderLayout());

        vehicleLabel.setText("Vehículo");
        vehiclePanel.add(vehicleLabel, java.awt.BorderLayout.NORTH);

        selectVehicleButton.setText("Seleccionar vehículo");
        selectVehicleButton.addActionListener(this::selectVehicleButtonActionPerformed);
        vehiclePanel.add(selectVehicleButton, java.awt.BorderLayout.CENTER);

        selectedVehicleLabel.setText("Ningún vehículo seleccionado");
        vehiclePanel.add(selectedVehicleLabel, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(vehiclePanel, gridBagConstraints);

        mechanicPanel.setLayout(new java.awt.BorderLayout());

        mechanicLabel.setText("Mecánico");
        mechanicPanel.add(mechanicLabel, java.awt.BorderLayout.NORTH);

        selectMechanicButton.setText("Seleccionar Mecánico");
        selectMechanicButton.addActionListener(this::selectMechanicButtonActionPerformed);
        mechanicPanel.add(selectMechanicButton, java.awt.BorderLayout.CENTER);

        selectedMechanicLabel.setText("Sin Asignar");
        mechanicPanel.add(selectedMechanicLabel, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(mechanicPanel, gridBagConstraints);

        newstatusPanel.setLayout(new java.awt.BorderLayout());

        statusLabel.setText("Estado");
        newstatusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);

        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "En Progreso", "Terminado", "Cancelado" }));
        newstatusPanel.add(statusComboBox, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(newstatusPanel, gridBagConstraints);

        descriptionPanel.setLayout(new java.awt.BorderLayout());

        descriptionLabel.setText("Descripción");
        descriptionPanel.add(descriptionLabel, java.awt.BorderLayout.NORTH);

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setRows(5);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionPane.setViewportView(descriptionTextArea);

        descriptionPanel.add(descriptionPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(descriptionPanel, gridBagConstraints);

        filterPanel.setLayout(new java.awt.GridBagLayout());

        statusFilterLabel.setText("Estado");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        filterPanel.add(statusFilterLabel, gridBagConstraints);

        assignmentFilterLabel.setText("Asignación");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        filterPanel.add(assignmentFilterLabel, gridBagConstraints);

        mechanicFilterLabel.setText("Mecánico");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        filterPanel.add(mechanicFilterLabel, gridBagConstraints);

        statusFilterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        filterPanel.add(statusFilterCombo, gridBagConstraints);

        assignmentFilterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        filterPanel.add(assignmentFilterCombo, gridBagConstraints);

        mechanicFilterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        filterPanel.add(mechanicFilterCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        formPanel.add(filterPanel, gridBagConstraints);

        add(formPanel, java.awt.BorderLayout.PAGE_START);

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Placa", "Vehículo", "Mecánico", "Estado", "Fecha de Creación"
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

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        clearForm();
    }//GEN-LAST:event_newButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        save();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void selectVehicleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectVehicleButtonActionPerformed
        chooseVehicle();
    }//GEN-LAST:event_selectVehicleButtonActionPerformed

    private void selectMechanicButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectMechanicButtonActionPerformed
        chooseMechanic();
    }//GEN-LAST:event_selectMechanicButtonActionPerformed

    private void changeStatusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeStatusButtonActionPerformed
        changeStatus();
    }//GEN-LAST:event_changeStatusButtonActionPerformed

    private void historyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_historyButtonActionPerformed
        showHistory();
    }//GEN-LAST:event_historyButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> assignmentFilterCombo;
    private javax.swing.JLabel assignmentFilterLabel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton changeStatusButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionPane;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JPanel formPanel;
    private javax.swing.JButton historyButton;
    private javax.swing.JComboBox<String> mechanicFilterCombo;
    private javax.swing.JLabel mechanicFilterLabel;
    private javax.swing.JLabel mechanicLabel;
    private javax.swing.JPanel mechanicPanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton newButton;
    private javax.swing.JPanel newstatusPanel;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton selectMechanicButton;
    private javax.swing.JButton selectVehicleButton;
    private javax.swing.JLabel selectedMechanicLabel;
    private javax.swing.JLabel selectedVehicleLabel;
    private javax.swing.JComboBox<String> statusComboBox;
    private javax.swing.JComboBox<String> statusFilterCombo;
    private javax.swing.JLabel statusFilterLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tablePane;
    private javax.swing.JLabel vehicleLabel;
    private javax.swing.JPanel vehiclePanel;
    // End of variables declaration//GEN-END:variables
}
