/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.model.Vehicle;
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
public class VehicleDAO {

    private final Connection connection;

    public VehicleDAO() {
        this.connection = null;
    }

    public VehicleDAO(Connection connection) {
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
            INSERT INTO vehicles (
                client_id,
                plate,
                brand,
                model,
                year,
                notes
            )
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING *
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT *
            FROM vehicles
            WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT *
            FROM vehicles
            WHERE active = TRUE
            ORDER BY id
            """;

    private static final String FIND_BY_PLATE_SQL = """
            SELECT *
            FROM vehicles
            WHERE plate = ?
            """;

    private static final String UPDATE_SQL = """
            UPDATE vehicles
            SET client_id = ?,
                plate = ?,
                brand = ?,
                model = ?,
                year = ?,
                notes = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING *
            """;

    private static final String SOFT_DELETE_SQL = """
            UPDATE vehicles
            SET active = FALSE,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    public Vehicle create(Vehicle vehicle) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, vehicle.getClientId());
            statement.setString(2, vehicle.getPlate());
            statement.setString(3, vehicle.getBrand());
            statement.setString(4, vehicle.getModel());
            statement.setInt(5, vehicle.getYear());
            statement.setString(6, vehicle.getNotes());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not create vehicle");
    }

    public Optional<Vehicle> findById(Long id) throws SQLException {

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

    public List<Vehicle> findAll() throws SQLException {

        Connection conn = getConnection();
        List<Vehicle> vehicles = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(FIND_ALL_SQL); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                vehicles.add(mapResultSet(resultSet));
            }

        } finally {
            closeConnection(conn);
        }

        return vehicles;
    }

    public Optional<Vehicle> findByPlate(
            String plate
    ) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(FIND_BY_PLATE_SQL)) {

            statement.setString(1, plate);

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

    public Vehicle update(Vehicle vehicle) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(UPDATE_SQL)) {

            statement.setLong(1, vehicle.getClientId());
            statement.setString(2, vehicle.getPlate());
            statement.setString(3, vehicle.getBrand());
            statement.setString(4, vehicle.getModel());
            statement.setInt(5, vehicle.getYear());
            statement.setString(6, vehicle.getNotes());
            statement.setLong(7, vehicle.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not update vehicle");
    }

    public void softDelete(Long id) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(SOFT_DELETE_SQL)) {

            statement.setLong(1, id);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Client not found: " + id);
            }

        } finally {
            closeConnection(conn);
        }
    }

    private Vehicle mapResultSet(ResultSet resultSet) throws SQLException {

        Vehicle vehicle = new Vehicle();

        vehicle.setId(resultSet.getLong("id"));
        vehicle.setClientId(resultSet.getLong("client_id"));
        vehicle.setPlate(resultSet.getString("plate"));
        vehicle.setBrand(resultSet.getString("brand"));
        vehicle.setModel(resultSet.getString("model"));
        vehicle.setYear(resultSet.getInt("year"));
        vehicle.setNotes(resultSet.getString("notes"));
        vehicle.setActive(resultSet.getBoolean("active"));
           
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            vehicle.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            vehicle.setUpdatedAt(updatedAt.toLocalDateTime());
        }
                
        return vehicle;
    }
}
