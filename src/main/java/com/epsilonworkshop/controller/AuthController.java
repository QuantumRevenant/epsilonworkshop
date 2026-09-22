/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.config.SessionManager;
import com.epsilonworkshop.dao.MechanicDAO;
import com.epsilonworkshop.dao.UserDAO;
import com.epsilonworkshop.exception.AuthenticationException;
import com.epsilonworkshop.model.Mechanic;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

/**
 *
 * @author sebap
 */
public class AuthController {

    private static final String INVALID_CREDENTIALS_MSG
            = "Usuario o contraseña incorrectos";

    private final UserDAO userDAO;
    private final MechanicDAO mechanicDAO;

    public AuthController() {
        this.userDAO = new UserDAO();
        this.mechanicDAO = new MechanicDAO();
    }

    // Constructor para inyectar DAOs falsos en tests (RNF-07)
    public AuthController(UserDAO userDAO, MechanicDAO mechanicDAO) {
        this.userDAO = userDAO;
        this.mechanicDAO = mechanicDAO;
    }

    public User login(String username, String password)
            throws AuthenticationException, SQLException {

        Optional<User> result = userDAO.findByUsername(username);

        if (result.isEmpty()) {
            throw new AuthenticationException(INVALID_CREDENTIALS_MSG);
        }

        User user = result.get();

        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new AuthenticationException(INVALID_CREDENTIALS_MSG);
        }

        Long mechanicId = null;

        if (user.getRole() == Role.MECHANIC) {
            mechanicId = mechanicDAO.findByUserId(user.getId())
                    .map(Mechanic::getId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Usuario '" + user.getUsername() + "' tiene rol MECHANIC "
                            + "pero no existe una fila en mechanics para user_id="
                            + user.getId()
                    ));
        }

        SessionManager.login(user, mechanicId);

        return user;
    }

    public void logout() {
        SessionManager.logout();
    }
}
