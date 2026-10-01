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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author sebap
 */
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserController controller;

    // ---------- createAdmin ----------

    @Test
    void shouldCreateAdminAccount() throws Exception {
        User actingAdmin = createTestUser(1L, Role.ADMIN);

        when(userDAO.existsByUsername("nuevo_admin")).thenReturn(false);
        when(userDAO.create(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User created = controller.createAdmin(
                "nuevo_admin", "plainPassword123", "Carlos", "Diaz", null, actingAdmin
        );

        assertEquals("nuevo_admin", created.getUsername());
        assertEquals(Role.ADMIN, created.getRole());
        assertNotEquals("plainPassword123", created.getPasswordHash()); // debe estar hasheado
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToCreateAdmin() throws Exception {
        User mechanicUser = createTestUser(1L, Role.MECHANIC);

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.createAdmin(
                        "nuevo_admin", "pass123", "Carlos", "Diaz", null, mechanicUser
                )
        );

        verify(userDAO, never()).create(any());
        verify(userDAO, never()).existsByUsername(any());
    }

    @Test
    void shouldThrowDuplicateUsernameExceptionWhenUsernameAlreadyExists() throws Exception {
        User actingAdmin = createTestUser(1L, Role.ADMIN);

        when(userDAO.existsByUsername("admin_existente")).thenReturn(true);

        assertThrows(
                DuplicateUsernameException.class,
                () -> controller.createAdmin(
                        "admin_existente", "pass123", "Carlos", "Diaz", null, actingAdmin
                )
        );

        verify(userDAO, never()).create(any());
    }

    // ---------- toggleActive ----------

    @Test
    void shouldDeactivateAnotherUserAsAdmin() throws Exception {
        User actingAdmin = createTestUser(1L, Role.ADMIN);
        User targetUser = createTestUser(2L, Role.MECHANIC);
        targetUser.setActive(true);

        when(userDAO.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userDAO.update(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        controller.toggleActive(2L, false, actingAdmin);

        verify(userDAO).update(argThat(u -> u.getId().equals(2L) && !u.getActive()));
    }

    @Test
    void shouldReactivateUserAsAdmin() throws Exception {
        User actingAdmin = createTestUser(1L, Role.ADMIN);
        User targetUser = createTestUser(2L, Role.MECHANIC);
        targetUser.setActive(false);

        when(userDAO.findById(2L)).thenReturn(Optional.of(targetUser));
        when(userDAO.update(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        controller.toggleActive(2L, true, actingAdmin);

        verify(userDAO).update(argThat(u -> u.getId().equals(2L) && u.getActive()));
    }

    @Test
    void shouldThrowUnauthorizedWhenAdminTriesToDeactivateThemselves() throws Exception {
        User actingAdmin = createTestUser(1L, Role.ADMIN);

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.toggleActive(1L, false, actingAdmin)
        );

        verify(userDAO, never()).findById(any());
        verify(userDAO, never()).update(any());
    }

    @Test
    void shouldAllowAdminToReactivateThemselves() throws Exception {
        // El bloqueo solo aplica a desactivarse, no a reactivarse --
        // confirma que el caso "active = true" nunca entra al chequeo de autobloqueo.
        User actingAdmin = createTestUser(1L, Role.ADMIN);
        actingAdmin.setActive(false);

        when(userDAO.findById(1L)).thenReturn(Optional.of(actingAdmin));
        when(userDAO.update(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> controller.toggleActive(1L, true, actingAdmin));
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToToggleActive() throws Exception {
        User mechanicUser = createTestUser(1L, Role.MECHANIC);

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.toggleActive(2L, false, mechanicUser)
        );

        verify(userDAO, never()).findById(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTargetUserDoesNotExist() throws Exception {
        User actingAdmin = createTestUser(1L, Role.ADMIN);

        when(userDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.toggleActive(999L, false, actingAdmin)
        );
    }

    // ---------- findById / findAll (restringidos a admin) ----------

    @Test
    void shouldAllowAdminToFindUserById() throws Exception {
        User actingAdmin = createTestUser(1L, Role.ADMIN);
        User targetUser = createTestUser(2L, Role.MECHANIC);

        when(userDAO.findById(2L)).thenReturn(Optional.of(targetUser));

        Optional<User> result = controller.findById(2L, actingAdmin);

        assertTrue(result.isPresent());
        assertEquals(targetUser, result.get());
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToFindUserById() throws Exception {
        User mechanicUser = createTestUser(1L, Role.MECHANIC);

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.findById(2L, mechanicUser)
        );

        verify(userDAO, never()).findById(any());
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToFindAllUsers() throws Exception {
        User mechanicUser = createTestUser(1L, Role.MECHANIC);

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.findAll(mechanicUser)
        );

        verify(userDAO, never()).findAll();
    }

    @Test
    void shouldAllowAdminToFindAllUsers() throws Exception {
        User actingAdmin = createTestUser(1L, Role.ADMIN);

        when(userDAO.findAll()).thenReturn(List.of());

        List<User> result = controller.findAll(actingAdmin);

        assertNotNull(result);
    }

    // ---------- helpers ----------

    private User createTestUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("test_" + role.name().toLowerCase() + "_" + id);
        user.setRole(role);
        return user;
    }
}