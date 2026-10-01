/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.dao.MechanicDAO;
import com.epsilonworkshop.dao.UserDAO;
import com.epsilonworkshop.dao.WorkHistoryDAO;
import com.epsilonworkshop.dao.WorkOrderDAO;
import com.epsilonworkshop.exception.DuplicateUsernameException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.*;
import com.epsilonworkshop.util.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author sebap
 */
public class MechanicController {

    private final Connection externalConnection;
    private final MechanicDAO mechanicDAO;
    private final UserDAO userDAO;
    private final WorkOrderDAO workOrderDAO;
    private final WorkHistoryDAO workHistoryDAO;

    public MechanicController() {
        this.externalConnection = null;
        this.mechanicDAO = new MechanicDAO();
        this.userDAO = new UserDAO();
        this.workOrderDAO = new WorkOrderDAO();
        this.workHistoryDAO = new WorkHistoryDAO();
    }

    public MechanicController(
            MechanicDAO mechanicDAO,
            UserDAO userDAO,
            WorkOrderDAO workOrderDAO,
            WorkHistoryDAO workHistoryDAO
    ) {
        this.externalConnection = null;
        this.mechanicDAO = mechanicDAO;
        this.userDAO = userDAO;
        this.workOrderDAO = workOrderDAO;
        this.workHistoryDAO = workHistoryDAO;
    }

    public MechanicController(Connection connection) {
        this.externalConnection = connection;
        this.mechanicDAO = new MechanicDAO(connection);
        this.userDAO = new UserDAO(connection);
        this.workOrderDAO = new WorkOrderDAO(connection);
        this.workHistoryDAO = new WorkHistoryDAO(connection);
    }

    public Mechanic create(
            String username, String plainPassword, String name,
            String paternalSurname, String maternalSurname,
            String specialty, User actingUser
    ) throws UnauthorizedActionException, DuplicateUsernameException, SQLException {

        requireAdmin(actingUser);

        boolean ownsConnection = (externalConnection == null);
        Connection conn = ownsConnection ? DBConnection.getConnection() : externalConnection;

        try {
            if (ownsConnection) {
                conn.setAutoCommit(false);
            }

            UserDAO txUserDAO = ownsConnection ? new UserDAO(conn) : userDAO;
            MechanicDAO txMechanicDAO = ownsConnection ? new MechanicDAO(conn) : mechanicDAO;

            if (txUserDAO.existsByUsername(username)) {
                throw new DuplicateUsernameException(
                        "Ya existe un usuario con username=" + username
                );
            }

            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(PasswordHasher.hash(plainPassword));
            user.setRole(Role.MECHANIC);
            user.setName(name);
            user.setPaternalSurname(paternalSurname);
            user.setMaternalSurname(maternalSurname);

            User createdUser = txUserDAO.create(user);

            Mechanic mechanic = new Mechanic();
            mechanic.setUserId(createdUser.getId());
            mechanic.setSpecialty(specialty);

            Mechanic createdMechanic = txMechanicDAO.create(mechanic);

            if (ownsConnection) {
                conn.commit();
            }
            return createdMechanic;

        } catch (SQLException | DuplicateUsernameException | RuntimeException e) {
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

    public Mechanic update(Long mechanicId, String specialty, User actingUser)
            throws UnauthorizedActionException, SQLException {

        requireAdmin(actingUser);

        Mechanic mechanic = mechanicDAO.findById(mechanicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un mecánico con id=" + mechanicId
                ));

        mechanic.setSpecialty(specialty);

        return mechanicDAO.update(mechanic);
    }

    public void softDelete(Long mechanicId, User actingUser)
            throws UnauthorizedActionException, SQLException {

        requireAdmin(actingUser);

        boolean ownsConnection = (externalConnection == null);
        Connection conn = ownsConnection ? DBConnection.getConnection() : externalConnection;

        try {
            if (ownsConnection) {
                conn.setAutoCommit(false);
            }

            WorkOrderDAO txWorkOrderDAO = ownsConnection ? new WorkOrderDAO(conn) : workOrderDAO;
            WorkHistoryDAO txHistoryDAO = ownsConnection ? new WorkHistoryDAO(conn) : workHistoryDAO;
            MechanicDAO txMechanicDAO = ownsConnection ? new MechanicDAO(conn) : mechanicDAO;

            // Confirma que el mecánico existe antes de tocar nada (mismo patrón de siempre)
            txMechanicDAO.findById(mechanicId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe un mecánico con id=" + mechanicId
                    ));

            List<WorkOrder> assignedOrders = txWorkOrderDAO.findByMechanicId(mechanicId);

            for (WorkOrder order : assignedOrders) {
                if (order.getStatus() == OrderStatus.FINISHED
                        || order.getStatus() == OrderStatus.CANCELED) {
                    continue; // no se tocan órdenes ya finalizadas
                }

                String oldValue = order.getMechanicId().toString();
                order.setMechanicId(null);
                txWorkOrderDAO.update(order);

                WorkOrderHistory history = new WorkOrderHistory();
                history.setWorkOrderId(order.getId());
                history.setUserId(actingUser.getId());
                history.setChangeType(WorkOrderChangeType.MECHANIC_REASSIGNMENT);
                history.setOldValue(oldValue);
                history.setNewValue("sin asignar");
                txHistoryDAO.create(history);
            }

            txMechanicDAO.softDelete(mechanicId);

            if (ownsConnection) {
                conn.commit();
            }

        } catch (SQLException | RuntimeException e) {
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

    public Optional<Mechanic> findById(Long id) throws SQLException {
        return mechanicDAO.findById(id);
    }

    public List<Mechanic> findAll() throws SQLException {
        return mechanicDAO.findAll();
    }

    private void requireAdmin(User actingUser) throws UnauthorizedActionException {
        if (actingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(
                    "Solo un administrador puede gestionar mecánicos"
            );
        }
    }

    private void safeRollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
