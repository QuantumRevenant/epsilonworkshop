/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.config;

import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author sebap
 */
public class SessionManagerTest {

    @BeforeEach
    void setUp() {
        SessionManager.logout();
    }

    @AfterEach
    void tearDown() {
        SessionManager.logout();
    }

    @Test
    void shouldNotBeLoggedInByDefault() {
        assertFalse(SessionManager.isLoggedIn());
    }

    @Test
    void shouldStoreCurrentUserOnLogin() {
        User admin = createTestUser(Role.ADMIN);

        SessionManager.login(admin, null);

        assertTrue(SessionManager.isLoggedIn());
        assertEquals(admin, SessionManager.getCurrentUser());
        assertNull(SessionManager.getCurrentMechanicId());
    }

    @Test
    void shouldStoreMechanicIdOnLoginWhenProvided() {
        User mechanicUser = createTestUser(Role.MECHANIC);

        SessionManager.login(mechanicUser, 42L);

        assertEquals(mechanicUser, SessionManager.getCurrentUser());
        assertEquals(42L, SessionManager.getCurrentMechanicId());
    }

    @Test
    void shouldClearSessionOnLogout() {
        User admin = createTestUser(Role.ADMIN);
        SessionManager.login(admin, null);

        SessionManager.logout();

        assertFalse(SessionManager.isLoggedIn());
        assertNull(SessionManager.getCurrentMechanicId());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenNoSession() {
        assertThrows(
                IllegalStateException.class,
                SessionManager::getCurrentUser
        );
    }

    private User createTestUser(Role role) {
        User user = new User();
        user.setId(1L);
        user.setUsername("test_user");
        user.setRole(role);
        return user;
    }
}
