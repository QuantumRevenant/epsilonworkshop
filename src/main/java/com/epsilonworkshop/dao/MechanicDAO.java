/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.model.Mechanic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author sebap
 */
public class MechanicDAO extends BaseDAO {

    public MechanicDAO() {
        super();
    }

    public MechanicDAO(Connection connection) {
        super(connection);
    }

    private static final String INSERT_SQL = """
            INSERT INTO mechanics (
                user_id,
                specialty
            )
            VALUES (?, ?)
            RETURNING *
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT *
            FROM mechanics
            WHERE id = ?
            """;

    private static final String FIND_BY_USER_ID_SQL = """
            SELECT *
            FROM mechanics
            WHERE user_id = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT *
            FROM mechanics
            WHERE active = TRUE
            ORDER BY id
            """;

    private static final String UPDATE_SQL = """
            UPDATE mechanics
            SET user_id = ?,
                specialty = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING *
            """;

    private static final String SOFT_DELETE_SQL = """
            UPDATE mechanics
            SET active = FALSE,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    public Mechanic create(Mechanic mechanic) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, mechanic.getUserId());
            statement.setString(2, mechanic.getSpecialty());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not create mechanic");
    }

    public Optional<Mechanic> findById(Long id) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(FIND_BY_ID_SQL)) {

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

    public Optional<Mechanic> findByUserId(Long userId) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(FIND_BY_USER_ID_SQL)) {

            statement.setLong(1, userId);

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

    public List<Mechanic> findAll() throws SQLException {

        Connection conn = getConnection();
        List<Mechanic> mechanics = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(FIND_ALL_SQL); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                mechanics.add(mapResultSet(resultSet));
            }

        } finally {
            closeConnection(conn);
        }

        return mechanics;
    }

    public Mechanic update(Mechanic mechanic) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(UPDATE_SQL)) {

            statement.setLong(1, mechanic.getUserId());
            statement.setString(2, mechanic.getSpecialty());
            statement.setLong(3, mechanic.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not update mechanic");
    }

    public void softDelete(Long id) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(SOFT_DELETE_SQL)) {

            statement.setLong(1, id);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Mechanic not found: " + id);
            }

        } finally {
            closeConnection(conn);
        }
    }

    private Mechanic mapResultSet(ResultSet resultSet) throws SQLException {

        Mechanic mechanic = new Mechanic();

        mechanic.setId(resultSet.getLong("id"));
        mechanic.setUserId(resultSet.getLong("user_id"));
        mechanic.setSpecialty(resultSet.getString("specialty"));
        mechanic.setActive(resultSet.getBoolean("active"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            mechanic.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            mechanic.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return mechanic;
    }
}
