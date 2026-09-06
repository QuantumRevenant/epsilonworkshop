/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.DocumentType;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sebap
 */
public class ClientDAOTest {

    private Connection connection;
    private ClientDAO clientDAO;

    private String generateTestDocument() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000)
        );
    }

    @BeforeEach
    void setUp() throws Exception {
        connection = DBConnection.getConnection();
        connection.setAutoCommit(false);

        clientDAO = new ClientDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            connection.rollback();
        } finally {
            connection.close();
        }
    }

    @Test
    void shouldCreateClient() throws Exception {

        Client client = createTestClient();

        assertNotNull(client.getId());
        assertEquals(DocumentType.DNI, client.getDocumentType());
        assertNotNull(client.getDocument());
        assertEquals("Test", client.getName());
        assertTrue(client.getActive());
        assertNotNull(client.getCreatedAt());
        assertNotNull(client.getUpdatedAt());
    }

    @Test
    void shouldFindClientById() throws Exception {

        Client created = createTestClient();

        Optional<Client> result
                = clientDAO.findById(created.getId());

        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals(created.getDocument(), result.get().getDocument());
    }

    @Test
    void shouldFindClientByDocument() throws Exception {

        Client created = createTestClient();

        Optional<Client> result
                = clientDAO.findByDocument(
                        created.getDocumentType(),
                        created.getDocument()
                );

        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals(created.getDocument(), result.get().getDocument());
    }

    @Test
    void shouldFindOnlyActiveClients() throws Exception {

        Client activeClient = createTestClient();

        Client deletedClient = createTestClient();
        clientDAO.softDelete(deletedClient.getId());

        List<Client> clients = clientDAO.findAll();

        assertTrue(
                clients.stream()
                        .anyMatch(c -> c.getId().equals(activeClient.getId()))
        );

        assertFalse(
                clients.stream()
                        .anyMatch(c -> c.getId().equals(deletedClient.getId()))
        );
    }

    @Test
    void shouldUpdateClient() throws Exception {

        Client client = createTestClient();

        client.setName("Updated Name");
        client.setPhone("988777666");
        client.setEmail("updated@example.com");
        client.setNotes("Updated notes");

        Client updated = clientDAO.update(client);

        assertEquals("Updated Name", updated.getName());
        assertEquals("988777666", updated.getPhone());
        assertEquals("updated@example.com", updated.getEmail());
        assertEquals("Updated notes", updated.getNotes());
    }

    @Test
    void shouldSoftDeleteClient() throws Exception {

        Client client = createTestClient();

        clientDAO.softDelete(client.getId());

        Optional<Client> result
                = clientDAO.findById(client.getId());

        assertTrue(result.isPresent());
        assertFalse(result.get().getActive());
    }

    private Client createTestClient() throws Exception {

        Client client = new Client();

        client.setDocumentType(DocumentType.DNI);
        client.setDocument(generateTestDocument());
        client.setName("Test");
        client.setPaternalSurname("User");
        client.setMaternalSurname("Example");
        client.setPhone("999999999");
        client.setEmail("test@example.com");
        client.setNotes("Created by test");

        return clientDAO.create(client);
    }
}
