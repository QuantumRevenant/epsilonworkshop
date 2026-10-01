/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.dao.MechanicDAO;
import com.epsilonworkshop.dao.VehicleDAO;
import com.epsilonworkshop.dao.WorkHistoryDAO;
import com.epsilonworkshop.dao.WorkOrderDAO;
import com.epsilonworkshop.exception.InvalidMechanicAssignmentException;
import com.epsilonworkshop.exception.InvalidStatusTransitionException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Mechanic;
import com.epsilonworkshop.model.OrderStatus;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.model.WorkOrder;
import com.epsilonworkshop.model.WorkOrderChangeType;
import com.epsilonworkshop.model.WorkOrderHistory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sebap
 */
public class WorkOrderController {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.IN_PROGRESS, OrderStatus.CANCELED),
            OrderStatus.IN_PROGRESS, Set.of(OrderStatus.FINISHED, OrderStatus.CANCELED),
            OrderStatus.FINISHED, Set.of(),
            OrderStatus.CANCELED, Set.of()
    );

    private final WorkOrderDAO workOrderDAO;
    private final WorkHistoryDAO workHistoryDAO;
    private final MechanicDAO mechanicDAO;
    private final VehicleDAO vehicleDAO;
    private final Connection externalConnection; // null en producción normal

    public WorkOrderController() {
        this.externalConnection = null;
        this.workOrderDAO = new WorkOrderDAO();
        this.workHistoryDAO = new WorkHistoryDAO();
        this.mechanicDAO = new MechanicDAO();
        this.vehicleDAO = new VehicleDAO();
    }

    public WorkOrderController(Connection connection) {
        this.externalConnection = connection;
        this.workOrderDAO = new WorkOrderDAO(connection);
        this.workHistoryDAO = new WorkHistoryDAO(connection);
        this.mechanicDAO = new MechanicDAO(connection);
        this.vehicleDAO = new VehicleDAO(connection);
    }

    public WorkOrderController(
            WorkOrderDAO workOrderDAO,
            WorkHistoryDAO workHistoryDAO,
            MechanicDAO mechanicDAO,
            VehicleDAO vehicleDAO
    ) {
        this.externalConnection = null;
        this.workOrderDAO = workOrderDAO;
        this.workHistoryDAO = workHistoryDAO;
        this.mechanicDAO = mechanicDAO;
        this.vehicleDAO = vehicleDAO;
    }

    public WorkOrder createWorkOrder(
            Long vehicleId,
            Long mechanicId,
            String description,
            User actingUser,
            Long actingMechanicId
    ) throws InvalidMechanicAssignmentException, UnauthorizedActionException, SQLException {

        vehicleDAO.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un vehículo con id=" + vehicleId
                ));

        if (mechanicId != null) {
            if (actingUser.getRole() == Role.MECHANIC
                    && !mechanicId.equals(actingMechanicId)) {
                throw new UnauthorizedActionException(
                        "Un mecánico solo puede asignarse la orden a sí mismo al crearla"
                );
            }

            Mechanic mechanic = mechanicDAO.findById(mechanicId)
                    .orElseThrow(() -> new InvalidMechanicAssignmentException(
                            "No existe un mecánico con id=" + mechanicId
                    ));

            if (!Boolean.TRUE.equals(mechanic.getActive())) {
                throw new InvalidMechanicAssignmentException(
                        "El mecánico seleccionado está inactivo y no puede ser asignado"
                );
            }
        }

        WorkOrder workOrder = new WorkOrder();
        workOrder.setVehicleId(vehicleId);
        workOrder.setMechanicId(mechanicId);
        workOrder.setStatus(OrderStatus.PENDING);
        workOrder.setDescription(description);

        return workOrderDAO.create(workOrder);
    }

    /**
     * @param actingMechanicId null si actingUser es ADMIN; el mechanicId de
     * sesión si es MECHANIC.
     */
    public WorkOrder changeStatus(
            Long orderId,
            OrderStatus newStatus,
            User actingUser,
            Long actingMechanicId
    ) throws InvalidStatusTransitionException, UnauthorizedActionException, SQLException {

        boolean ownsConnection = (externalConnection == null);
        Connection conn = ownsConnection ? DBConnection.getConnection() : externalConnection;

        try {
            if (ownsConnection) {
                conn.setAutoCommit(false);
            }

            WorkOrderDAO txWorkOrderDAO = ownsConnection ? new WorkOrderDAO(conn) : workOrderDAO;
            WorkHistoryDAO txHistoryDAO = ownsConnection ? new WorkHistoryDAO(conn) : workHistoryDAO;

            WorkOrder order = txWorkOrderDAO.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe una orden de trabajo con id=" + orderId
                    ));

            if (actingUser.getRole() == Role.MECHANIC) {
                if (order.getMechanicId() == null
                        || !order.getMechanicId().equals(actingMechanicId)) {
                    throw new UnauthorizedActionException(
                            "No puedes modificar el estado de una orden que no tienes asignada"
                    );
                }
            }

            if (!isValidTransition(order.getStatus(), newStatus)) {
                throw new InvalidStatusTransitionException(
                        "Transición no permitida: " + order.getStatus() + " -> " + newStatus
                );
            }

            String oldValue = order.getStatus().name().toLowerCase();
            order.setStatus(newStatus);
            WorkOrder updated = txWorkOrderDAO.update(order);

            WorkOrderHistory history = new WorkOrderHistory();
            history.setWorkOrderId(orderId);
            history.setUserId(actingUser.getId());
            history.setChangeType(WorkOrderChangeType.STATUS_CHANGE);
            history.setOldValue(oldValue);
            history.setNewValue(newStatus.name().toLowerCase());
            txHistoryDAO.create(history);

            if (ownsConnection) {
                conn.commit();
            }
            return updated;

        } catch (SQLException e) {
            if (ownsConnection) {
                safeRollback(conn);
            }
            throw e;
        } catch (InvalidStatusTransitionException | UnauthorizedActionException e) {
            if (ownsConnection) {
                safeRollback(conn);
            }
            throw e;
        } catch (RuntimeException e) {
            if (ownsConnection) {
                safeRollback(conn);
            }
            throw e;
        } finally {
            if (ownsConnection) {
                closeQuietly(conn);
            }
        }
    }

    public WorkOrder reassignMechanic(
            Long orderId,
            Long newMechanicId,
            User actingUser,
            Long actingMechanicId
    ) throws UnauthorizedActionException, InvalidMechanicAssignmentException, SQLException {

        boolean ownsConnection = (externalConnection == null);
        Connection conn = ownsConnection ? DBConnection.getConnection() : externalConnection;

        try {
            if (ownsConnection) {
                conn.setAutoCommit(false);
            }

            WorkOrderDAO txWorkOrderDAO = ownsConnection ? new WorkOrderDAO(conn) : workOrderDAO;
            WorkHistoryDAO txHistoryDAO = ownsConnection ? new WorkHistoryDAO(conn) : workHistoryDAO;
            MechanicDAO txMechanicDAO = ownsConnection ? new MechanicDAO(conn) : mechanicDAO;

            WorkOrder order = txWorkOrderDAO.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe una orden de trabajo con id=" + orderId
                    ));

            if (order.getStatus() == OrderStatus.FINISHED
                    || order.getStatus() == OrderStatus.CANCELED) {
                throw new UnauthorizedActionException(
                        "No se puede reasignar el mecánico de una orden en estado final"
                );
            }

            if (actingUser.getRole() == Role.MECHANIC) {
                if (order.getMechanicId() != null) {
                    throw new UnauthorizedActionException(
                            "Esta orden ya tiene un mecánico asignado; solo un administrador puede reasignarla"
                    );
                }
                if (!newMechanicId.equals(actingMechanicId)) {
                    throw new UnauthorizedActionException(
                            "Un mecánico solo puede autoasignarse a sí mismo, no asignar a otro mecánico"
                    );
                }
            }

            Mechanic newMechanic = txMechanicDAO.findById(newMechanicId)
                    .orElseThrow(() -> new InvalidMechanicAssignmentException(
                            "No existe un mecánico con id=" + newMechanicId
                    ));

            if (!Boolean.TRUE.equals(newMechanic.getActive())) {
                throw new InvalidMechanicAssignmentException(
                        "El mecánico seleccionado está inactivo y no puede ser asignado"
                );
            }

            String oldValue = order.getMechanicId() != null
                    ? order.getMechanicId().toString()
                    : "sin asignar";

            order.setMechanicId(newMechanicId);
            WorkOrder updated = txWorkOrderDAO.update(order);

            WorkOrderHistory history = new WorkOrderHistory();
            history.setWorkOrderId(orderId);
            history.setUserId(actingUser.getId());
            history.setChangeType(WorkOrderChangeType.MECHANIC_REASSIGNMENT);
            history.setOldValue(oldValue);
            history.setNewValue(newMechanicId.toString());
            txHistoryDAO.create(history);

            if (ownsConnection) {
                conn.commit();
            }
            return updated;

        } catch (SQLException e) {
            if (ownsConnection) {
                safeRollback(conn);
            }
            throw e;
        } catch (UnauthorizedActionException | InvalidMechanicAssignmentException e) {
            if (ownsConnection) {
                safeRollback(conn);
            }
            throw e;
        } catch (RuntimeException e) {
            if (ownsConnection) {
                safeRollback(conn);
            }
            throw e;
        } finally {
            if (ownsConnection) {
                closeQuietly(conn);
            }
        }
    }

    /**
     * RN-06: pendiente->en_progreso, pendiente->cancelado,
     * en_progreso->terminado, en_progreso->cancelado. RN-07: terminado y
     * cancelado no tienen transiciones válidas de salida.
     */
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return VALID_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    private void safeRollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                // No relanzamos: ocultaría la excepción original que causó el rollback.
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                // Idem: no ocultar la excepción original.
            }
        }
    }

    public List<WorkOrderHistory> getHistory(Long orderId) throws SQLException {
        return workHistoryDAO.findByWorkOrderId(orderId);
    }
}
