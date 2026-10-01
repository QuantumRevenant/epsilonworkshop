/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.dao.UserDAO;
import com.epsilonworkshop.exception.DuplicateUsernameException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.util.PasswordHasher;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author sebap
 */
public class UserController {

    private final UserDAO userDAO;

    public UserController() {
        this.userDAO = new UserDAO();
    }

    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User createAdmin(
            String username, String plainPassword, String name,
            String paternalSurname, String maternalSurname,
            User actingUser
    ) throws UnauthorizedActionException, DuplicateUsernameException, SQLException {

        requireAdmin(actingUser);

        if (userDAO.existsByUsername(username)) {
            throw new DuplicateUsernameException(
                    "Ya existe un usuario con username=" + username
            );
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash(plainPassword));
        user.setRole(Role.ADMIN);
        user.setName(name);
        user.setPaternalSurname(paternalSurname);
        user.setMaternalSurname(maternalSurname);

        return userDAO.create(user);
    }

    public void toggleActive(Long userId, boolean active, User actingUser)
            throws UnauthorizedActionException, SQLException {

        requireAdmin(actingUser);

        if (!active && actingUser.getId().equals(userId)) {
            throw new UnauthorizedActionException(
                    "Un administrador no puede desactivar su propia cuenta"
            );
        }

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un usuario con id=" + userId
                ));

        user.setActive(active);
        userDAO.update(user);
    }

    public Optional<User> findById(Long id, User actingUser)
            throws UnauthorizedActionException, SQLException {

        requireAdmin(actingUser);
        return userDAO.findById(id);
    }

    public List<User> findAll(User actingUser)
            throws UnauthorizedActionException, SQLException {

        requireAdmin(actingUser);
        return userDAO.findAll();
    }

    private void requireAdmin(User actingUser) throws UnauthorizedActionException {
        if (actingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(
                    "Solo un administrador puede gestionar cuentas de usuario"
            );
        }
    }
}