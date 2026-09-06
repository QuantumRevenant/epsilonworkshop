/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.DocumentType;

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
public class ClientDAO extends BaseDAO{

    public ClientDAO() {
        super();
    }

    public ClientDAO(Connection connection) {
        super(connection);
    }

    private static final String INSERT_SQL = """
            INSERT INTO clients (
                document_type,
                document,
                name,
                paternal_surname,
                maternal_surname,
                phone,
                email,
                notes
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING *
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT *
            FROM clients
            WHERE id = ?
            """;

    private static final String FIND_ALL_SQL = """
            SELECT *
            FROM clients
            WHERE active = TRUE
            ORDER BY id
            """;

    private static final String FIND_BY_DOCUMENT_SQL = """
            SELECT *
            FROM clients
            WHERE document_type = ?
              AND document = ?
            """;

    private static final String UPDATE_SQL = """
            UPDATE clients
            SET document_type = ?,
                document = ?,
                name = ?,
                paternal_surname = ?,
                maternal_surname = ?,
                phone = ?,
                email = ?,
                notes = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING *
            """;

    private static final String SOFT_DELETE_SQL = """
            UPDATE clients
            SET active = FALSE,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    public Client create(Client client) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(INSERT_SQL)) {

            statement.setString(1, client.getDocumentType().name());
            statement.setString(2, client.getDocument());
            statement.setString(3, client.getName());
            statement.setString(4, client.getPaternalSurname());
            statement.setString(5, client.getMaternalSurname());
            statement.setString(6, client.getPhone());
            statement.setString(7, client.getEmail());
            statement.setString(8, client.getNotes());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not create client");
    }

    public Optional<Client> findById(Long id) throws SQLException {

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

    public List<Client> findAll() throws SQLException {

        Connection conn = getConnection();
        List<Client> clients = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                clients.add(mapResultSet(resultSet));
            }

        } finally {
            closeConnection(conn);
        }

        return clients;
    }

    public Optional<Client> findByDocument(
            DocumentType documentType,
            String document
    ) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(FIND_BY_DOCUMENT_SQL)) {

            statement.setString(1, documentType.name());
            statement.setString(2, document);

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

    public Client update(Client client) throws SQLException {

        Connection conn = getConnection();

        try (PreparedStatement statement = conn.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, client.getDocumentType().name());
            statement.setString(2, client.getDocument());
            statement.setString(3, client.getName());
            statement.setString(4, client.getPaternalSurname());
            statement.setString(5, client.getMaternalSurname());
            statement.setString(6, client.getPhone());
            statement.setString(7, client.getEmail());
            statement.setString(8, client.getNotes());
            statement.setLong(9, client.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }

        } finally {
            closeConnection(conn);
        }

        throw new SQLException("Could not update client");
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

    private Client mapResultSet(ResultSet resultSet) throws SQLException {

        Client client = new Client();

        client.setId(resultSet.getLong("id"));

        client.setDocumentType(
                DocumentType.valueOf(
                        resultSet.getString("document_type")
                )
        );

        client.setDocument(resultSet.getString("document"));
        client.setName(resultSet.getString("name"));
        client.setPaternalSurname(
                resultSet.getString("paternal_surname")
        );
        client.setMaternalSurname(
                resultSet.getString("maternal_surname")
        );
        client.setPhone(resultSet.getString("phone"));
        client.setEmail(resultSet.getString("email"));
        client.setNotes(resultSet.getString("notes"));
        client.setActive(resultSet.getBoolean("active"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            client.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            client.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return client;
    }
}