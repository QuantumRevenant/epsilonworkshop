/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.dao.ClientDAO;
import com.epsilonworkshop.exception.DuplicateDocumentException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.DocumentType;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 *
 * @author sebap
 */
@ExtendWith(MockitoExtension.class)
public class ClientControllerTest {

    @Mock
    private ClientDAO clientDAO;

    @InjectMocks
    private ClientController controller;

    // ---------- create ----------

    @Test
    void shouldCreateClientAsAdmin() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Client client = createTestClient(null, DocumentType.DNI, "12345678");

        when(clientDAO.findByDocument(DocumentType.DNI, "12345678"))
                .thenReturn(Optional.empty());
        when(clientDAO.create(client)).thenReturn(client);

        Client created = controller.create(client, admin);

        assertEquals(client, created);
        verify(clientDAO).create(client);
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToCreateClient() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);
        Client client = createTestClient(null, DocumentType.DNI, "12345678");

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.create(client, mechanicUser)
        );

        verify(clientDAO, never()).create(any());
        verify(clientDAO, never()).findByDocument(any(), any());
    }

    @Test
    void shouldThrowDuplicateDocumentExceptionWhenDocumentAlreadyExists() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Client newClient = createTestClient(null, DocumentType.DNI, "12345678");
        Client existingClient = createTestClient(1L, DocumentType.DNI, "12345678");

        when(clientDAO.findByDocument(DocumentType.DNI, "12345678"))
                .thenReturn(Optional.of(existingClient));

        assertThrows(
                DuplicateDocumentException.class,
                () -> controller.create(newClient, admin)
        );

        verify(clientDAO, never()).create(any());
    }

    // ---------- update ----------

    @Test
    void shouldUpdateClientAsAdmin() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Client client = createTestClient(1L, DocumentType.DNI, "12345678");

        when(clientDAO.findByDocument(DocumentType.DNI, "12345678"))
                .thenReturn(Optional.of(client));
        when(clientDAO.update(client)).thenReturn(client);

        Client updated = controller.update(client, admin);

        assertEquals(client, updated);
    }

    @Test
    void shouldAllowUpdateWhenDocumentBelongsToSameClient() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Client client = createTestClient(1L, DocumentType.DNI, "12345678");

        // findByDocument encuentra al mismo cliente que se está editando -- no es duplicado
        when(clientDAO.findByDocument(DocumentType.DNI, "12345678"))
                .thenReturn(Optional.of(client));
        when(clientDAO.update(client)).thenReturn(client);

        assertDoesNotThrow(() -> controller.update(client, admin));
        verify(clientDAO).update(client);
    }

    @Test
    void shouldThrowDuplicateDocumentExceptionWhenAnotherClientHasSameDocument() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Client clientBeingUpdated = createTestClient(1L, DocumentType.DNI, "12345678");
        Client otherClientWithSameDocument = createTestClient(2L, DocumentType.DNI, "12345678");

        when(clientDAO.findByDocument(DocumentType.DNI, "12345678"))
                .thenReturn(Optional.of(otherClientWithSameDocument));

        assertThrows(
                DuplicateDocumentException.class,
                () -> controller.update(clientBeingUpdated, admin)
        );

        verify(clientDAO, never()).update(any());
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToUpdateClient() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);
        Client client = createTestClient(1L, DocumentType.DNI, "12345678");

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.update(client, mechanicUser)
        );

        verify(clientDAO, never()).update(any());
    }

    // ---------- softDelete ----------

    @Test
    void shouldSoftDeleteClientAsAdmin() throws Exception {
        User admin = createTestUser(Role.ADMIN);

        controller.softDelete(1L, admin);

        verify(clientDAO).softDelete(1L);
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToSoftDeleteClient() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.softDelete(1L, mechanicUser)
        );

        verify(clientDAO, never()).softDelete(any());
    }

    // ---------- findById / findAll (sin restricción de rol) ----------

    @Test
    void shouldAllowMechanicToFindClientById() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);
        Client client = createTestClient(1L, DocumentType.DNI, "12345678");

        when(clientDAO.findById(1L)).thenReturn(Optional.of(client));

        Optional<Client> result = controller.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(client, result.get());
    }

    // ---------- helpers ----------

    private User createTestUser(Role role) {
        User user = new User();
        user.setId(1L);
        user.setUsername("test_" + role.name().toLowerCase());
        user.setRole(role);
        return user;
    }

    private Client createTestClient(Long id, DocumentType documentType, String document) {
        Client client = new Client();
        client.setId(id);
        client.setDocumentType(documentType);
        client.setDocument(document);
        client.setName("Test");
        client.setPaternalSurname("User");
        client.setPhone("999999999");
        return client;
    }
}