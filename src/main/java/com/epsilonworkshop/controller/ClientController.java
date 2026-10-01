/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.dao.ClientDAO;
import com.epsilonworkshop.exception.DuplicateDocumentException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author sebap
 */
public class ClientController {

    private final ClientDAO clientDAO;

    public ClientController() {
        this.clientDAO = new ClientDAO();
    }

    public ClientController(ClientDAO clientDAO) {
        this.clientDAO = clientDAO;
    }

    public Client create(Client client, User actingUser)
            throws UnauthorizedActionException, DuplicateDocumentException, SQLException {

        requireAdmin(actingUser);

        Optional<Client> existing = clientDAO.findByDocument(
                client.getDocumentType(), client.getDocument()
        );

        if (existing.isPresent()) {
            throw new DuplicateDocumentException(
                    "Ya existe un cliente con documento "
                    + client.getDocumentType() + " " + client.getDocument()
            );
        }

        return clientDAO.create(client);
    }

    public Client update(Client client, User actingUser)
            throws UnauthorizedActionException, DuplicateDocumentException, SQLException {

        requireAdmin(actingUser);

        Optional<Client> existing = clientDAO.findByDocument(
                client.getDocumentType(), client.getDocument()
        );

        if (existing.isPresent() && !existing.get().getId().equals(client.getId())) {
            throw new DuplicateDocumentException(
                    "Ya existe otro cliente con documento "
                    + client.getDocumentType() + " " + client.getDocument()
            );
        }

        return clientDAO.update(client);
    }

    public void softDelete(Long id, User actingUser)
            throws UnauthorizedActionException, SQLException {

        requireAdmin(actingUser);
        clientDAO.softDelete(id);
    }

    public Optional<Client> findById(Long id) throws SQLException {
        return clientDAO.findById(id);
    }

    public List<Client> findAll() throws SQLException {
        return clientDAO.findAll();
    }

    private void requireAdmin(User actingUser) throws UnauthorizedActionException {
        if (actingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(
                    "Solo un administrador puede gestionar clientes"
            );
        }
    }
}
