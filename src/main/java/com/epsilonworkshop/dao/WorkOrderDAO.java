/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.model.OrderStatus;
import com.epsilonworkshop.model.WorkOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author sebap
 */
public class WorkOrderDAO extends BaseDAO{
    
    public WorkOrderDAO() {
        super();
    }

    public WorkOrderDAO(Connection connection) {
        super(connection);
    }

    private static final String INSERT_SQL = """
            INSERT INTO work_orders (
                vehicle_id,
                mechanic_id,
                status,
                description
            )
            VALUES (?, ?, ?::order_status, ?)
            RETURNING *
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT *
            FROM work_orders
            WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT *
            FROM work_orders
            ORDER BY id
            """;

    private static final String FIND_BY_VEHICLE_ID_SQL = """
            SELECT *
            FROM work_orders
            WHERE vehicle_id = ?
            ORDER BY id
            """;

    private static final String FIND_BY_MECHANIC_ID_SQL = """
            SELECT *
            FROM work_orders
            WHERE mechanic_id = ?
            ORDER BY id
            """;

    private static final String UPDATE_SQL = """
            UPDATE work_orders
            SET vehicle_id = ?,
                mechanic_id = ?,
                status = ?::order_status,
                description = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING *
            """;

    public WorkOrder create(WorkOrder workOrder) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, workOrder.getVehicleId());

            if (workOrder.getMechanicId() != null) {
                statement.setLong(2, workOrder.getMechanicId());
            } else {
                statement.setNull(2, Types.BIGINT);
            }

            statement.setString(
                    3,
                    workOrder.getStatus().name().toLowerCase()
            );

            statement.setString(4, workOrder.getDescription());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not create work order");
    }

    public Optional<WorkOrder> findById(Long id) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement =
                     conn.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSet(resultSet));
                }
            }

        } finally {
            closeConnection(conn);
        }

        return Optional.empty();
    }

    public List<WorkOrder> findAll() throws SQLException {

        Connection conn = getConnection();
        List<WorkOrder> workOrders = new ArrayList<>();

        try (PreparedStatement statement =
                     conn.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                workOrders.add(mapResultSet(resultSet));
            }

        } finally {
            closeConnection(conn);
        }

        return workOrders;
    }

    public List<WorkOrder> findByVehicleId(Long vehicleId)
            throws SQLException {

        Connection conn = getConnection();
        List<WorkOrder> workOrders = new ArrayList<>();

        try (PreparedStatement statement =
                     conn.prepareStatement(FIND_BY_VEHICLE_ID_SQL)) {

            statement.setLong(1, vehicleId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    workOrders.add(mapResultSet(resultSet));
                }
            }

        } finally {
            closeConnection(conn);
        }

        return workOrders;
    }

    public List<WorkOrder> findByMechanicId(Long mechanicId)
            throws SQLException {

        Connection conn = getConnection();
        List<WorkOrder> workOrders = new ArrayList<>();

        try (PreparedStatement statement =
                     conn.prepareStatement(FIND_BY_MECHANIC_ID_SQL)) {

            statement.setLong(1, mechanicId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    workOrders.add(mapResultSet(resultSet));
                }
            }

        } finally {
            closeConnection(conn);
        }

        return workOrders;
    }

    public WorkOrder update(WorkOrder workOrder) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement =
                     conn.prepareStatement(UPDATE_SQL)) {

            statement.setLong(1, workOrder.getVehicleId());

            if (workOrder.getMechanicId() != null) {
                statement.setLong(2, workOrder.getMechanicId());
            } else {
                statement.setNull(2, Types.BIGINT);
            }

            statement.setString(
                    3,
                    workOrder.getStatus().name().toLowerCase()
            );

            statement.setString(4, workOrder.getDescription());
            statement.setLong(5, workOrder.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not update work order");
    }

    private WorkOrder mapResultSet(ResultSet resultSet)
            throws SQLException {

        WorkOrder workOrder = new WorkOrder();

        workOrder.setId(resultSet.getLong("id"));
        workOrder.setVehicleId(resultSet.getLong("vehicle_id"));

        long mechanicId = resultSet.getLong("mechanic_id");

        if (resultSet.wasNull()) {
            workOrder.setMechanicId(null);
        } else {
            workOrder.setMechanicId(mechanicId);
        }

        workOrder.setStatus(
                OrderStatus.valueOf(
                        resultSet.getString("status").toUpperCase()
                )
        );

        workOrder.setDescription(
                resultSet.getString("description")
        );

        Timestamp createdAt = resultSet.getTimestamp("created_at");

        if (createdAt != null) {
            workOrder.setCreatedAt(
                    createdAt.toLocalDateTime()
            );
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        if (updatedAt != null) {
            workOrder.setUpdatedAt(
                    updatedAt.toLocalDateTime()
            );
        }

        Timestamp closedAt = resultSet.getTimestamp("closed_at");

        if (closedAt != null) {
            workOrder.setClosedAt(
                    closedAt.toLocalDateTime()
            );
        }

        return workOrder;
    }
}
