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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
public class AuthControllerTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private MechanicDAO mechanicDAO;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        SessionManager.logout();
    }

    @AfterEach
    void tearDown() {
        SessionManager.logout();
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenUsernameDoesNotExist() throws Exception {
        when(userDAO.findByUsername("noexiste"))
                .thenReturn(Optional.empty());

        assertThrows(
                AuthenticationException.class,
                () -> authController.login("noexiste", "cualquiera")
        );

        assertFalse(SessionManager.isLoggedIn());
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenPasswordIsIncorrect() throws Exception {
        User admin = createTestUser(Role.ADMIN, "correcta");

        when(userDAO.findByUsername(admin.getUsername()))
                .thenReturn(Optional.of(admin));

        assertThrows(
                AuthenticationException.class,
                () -> authController.login(admin.getUsername(), "incorrecta")
        );

        assertFalse(SessionManager.isLoggedIn());
    }

    @Test
    void shouldLogInAdminWithoutQueryingMechanicDAO() throws Exception {
        User admin = createTestUser(Role.ADMIN, "correcta");

        when(userDAO.findByUsername(admin.getUsername()))
                .thenReturn(Optional.of(admin));

        User result = authController.login(admin.getUsername(), "correcta");

        assertEquals(admin, result);
        assertTrue(SessionManager.isLoggedIn());
        assertEquals(admin, SessionManager.getCurrentUser());
        assertNull(SessionManager.getCurrentMechanicId());

        verify(mechanicDAO, never()).findByUserId(any());
    }

    @Test
    void shouldLogInMechanicAndResolveMechanicId() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC, "correcta");

        Mechanic mechanic = new Mechanic();
        mechanic.setId(42L);
        mechanic.setUserId(mechanicUser.getId());

        when(userDAO.findByUsername(mechanicUser.getUsername()))
                .thenReturn(Optional.of(mechanicUser));
        when(mechanicDAO.findByUserId(mechanicUser.getId()))
                .thenReturn(Optional.of(mechanic));

        User result = authController.login(mechanicUser.getUsername(), "correcta");

        assertEquals(mechanicUser, result);
        assertEquals(42L, SessionManager.getCurrentMechanicId());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenMechanicUserHasNoMechanicRow() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC, "correcta");

        when(userDAO.findByUsername(mechanicUser.getUsername()))
                .thenReturn(Optional.of(mechanicUser));
        when(mechanicDAO.findByUserId(mechanicUser.getId()))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> authController.login(mechanicUser.getUsername(), "correcta")
        );

        assertFalse(SessionManager.isLoggedIn());
    }

    private User createTestUser(Role role, String plainPassword) {
        User user = new User();
        user.setId(1L);
        user.setUsername("test_" + role.name().toLowerCase());
        user.setPasswordHash(PasswordHasher.hash(plainPassword));
        user.setRole(role);
        return user;
    }
}