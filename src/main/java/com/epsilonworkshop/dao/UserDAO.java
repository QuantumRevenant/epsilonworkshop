/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
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
public class UserDAO {
    
    private final Connection connection;

    public UserDAO() {
        this.connection = null;
    }

    public UserDAO(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        this.connection = connection;
    }

    private Connection getConnection() throws SQLException {
        return connection != null
                ? connection
                : DBConnection.getConnection();
    }

    private boolean hasExternalConnection() {
        return connection != null;
    }

    private void closeConnection(Connection conn) throws SQLException {
        if (!hasExternalConnection()) {
            conn.close();
        }
    }

    private static final String INSERT_SQL = """
            INSERT INTO users (
                username,
                password_hash,
                role,
                name,
                paternal_surname,
                maternal_surname
            )
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING *
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT *
            FROM users
            WHERE id = ?
            """;

    private static final String FIND_BY_USERNAME_SQL = """
            SELECT *
            FROM users
            WHERE username = ?
              AND active = TRUE
            """;

    private static final String FIND_ALL_SQL = """
            SELECT *
            FROM users
            WHERE active = TRUE
            ORDER BY id
            """;

    private static final String UPDATE_SQL = """
            UPDATE users
            SET username = ?,
                password_hash = ?,
                role = ?,
                name = ?,
                paternal_surname = ?,
                maternal_surname = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING *
            """;

    private static final String SOFT_DELETE_SQL = """
            UPDATE users
            SET active = FALSE,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    public User create(User user) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(INSERT_SQL)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name().toLowerCase());
            statement.setString(4, user.getName());
            statement.setString(5, user.getPaternalSurname());
            statement.setString(6, user.getMaternalSurname());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not create user");
    }

    public Optional<User> findById(Long id) throws SQLException {

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

    public Optional<User> findByUsername(String username) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(FIND_BY_USERNAME_SQL)) {

            statement.setString(1, username);

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

    public List<User> findAll() throws SQLException {

        Connection conn = getConnection();
        List<User> users = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapResultSet(resultSet));
            }

        } finally {
            closeConnection(conn);
        }

        return users;
    }

    public User update(User user) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole().name().toLowerCase());
            statement.setString(4, user.getName());
            statement.setString(5, user.getPaternalSurname());
            statement.setString(6, user.getMaternalSurname());
            statement.setLong(7, user.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not update user");
    }

    public void softDelete(Long id) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(SOFT_DELETE_SQL)) {

            statement.setLong(1, id);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("User not found: " + id);
            }

        } finally {
            closeConnection(conn);
        }
    }

    private User mapResultSet(ResultSet resultSet) throws SQLException {

        User user = new User();

        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));

        user.setRole(
                Role.valueOf(
                        resultSet.getString("role").toUpperCase()
                )
        );

        user.setName(resultSet.getString("name"));
        user.setPaternalSurname(resultSet.getString("paternal_surname"));
        user.setMaternalSurname(resultSet.getString("maternal_surname"));
        user.setActive(resultSet.getBoolean("active"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return user;
    }
}
