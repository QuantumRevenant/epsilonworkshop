/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.model.WorkOrderChangeType;
import com.epsilonworkshop.model.WorkOrderHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sebap
 */
public class WorkHistoryDAO extends BaseDAO {

    public WorkHistoryDAO() {
        super();
    }

    public WorkHistoryDAO(Connection connection) {
        super(connection);
    }

    private static final String INSERT_SQL = """
            INSERT INTO work_order_history (
                work_order_id,
                user_id,
                change_type,
                old_value,
                new_value
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING *
            """;

    private static final String FIND_BY_WORK_ORDER_ID_SQL = """
            SELECT *
            FROM work_order_history
            WHERE work_order_id = ?
            ORDER BY changed_at
            """;

    public WorkOrderHistory create(WorkOrderHistory history) throws SQLException {
        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, history.getWorkOrderId());
            statement.setLong(2, history.getUserId());
            statement.setString(
                    3,
                    history.getChangeType().name().toLowerCase()
            );
            statement.setString(4, history.getOldValue());
            statement.setString(5, history.getNewValue());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }
        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not create work order history");
    }

    public List<WorkOrderHistory> findByWorkOrderId(
            Long workOrderId
    ) throws SQLException {

        Connection conn = getConnection();
        List<WorkOrderHistory> history = new ArrayList<>();

        try (PreparedStatement statement
                = conn.prepareStatement(FIND_BY_WORK_ORDER_ID_SQL)) {

            statement.setLong(1, workOrderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    history.add(mapResultSet(resultSet));
                }
            }
        } finally {
            closeConnection(conn);
        }

        return history;
    }

    private WorkOrderHistory mapResultSet(
            ResultSet resultSet
    ) throws SQLException {

        WorkOrderHistory history = new WorkOrderHistory();

        history.setId(resultSet.getLong("id"));
        history.setWorkOrderId(resultSet.getLong("work_order_id"));
        history.setUserId(resultSet.getLong("user_id"));

        history.setChangeType(
                WorkOrderChangeType.valueOf(
                        resultSet.getString("change_type").toUpperCase()
                )
        );

        history.setOldValue(resultSet.getString("old_value"));
        history.setNewValue(resultSet.getString("new_value"));

        Timestamp changedAt = resultSet.getTimestamp("changed_at");
        if (changedAt != null) {
            history.setChangedAt(changedAt.toLocalDateTime());
        }

        return history;
    }
}
