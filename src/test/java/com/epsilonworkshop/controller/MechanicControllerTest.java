/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.dao.MechanicDAO;
import com.epsilonworkshop.dao.UserDAO;
import com.epsilonworkshop.dao.WorkHistoryDAO;
import com.epsilonworkshop.dao.WorkOrderDAO;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Mechanic;
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
public class MechanicControllerTest {

    @Mock
    private MechanicDAO mechanicDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private WorkOrderDAO workOrderDAO;

    @Mock
    private WorkHistoryDAO workHistoryDAO;

    @InjectMocks
    private MechanicController controller;

    @Test
    void shouldUpdateSpecialtyAsAdmin() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Mechanic mechanic = createTestMechanic(1L, "Diagnóstico");

        when(mechanicDAO.findById(1L)).thenReturn(Optional.of(mechanic));
        when(mechanicDAO.update(any(Mechanic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mechanic updated = controller.update(1L, "Frenos", admin);

        assertEquals("Frenos", updated.getSpecialty());
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToUpdateMechanic() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.update(1L, "Frenos", mechanicUser)
        );

        verify(mechanicDAO, never()).update(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenMechanicToUpdateDoesNotExist() throws Exception {
        User admin = createTestUser(Role.ADMIN);

        when(mechanicDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.update(999L, "Frenos", admin)
        );

        verify(mechanicDAO, never()).update(any());
    }

    private User createTestUser(Role role) {
        User user = new User();
        user.setId(1L);
        user.setUsername("test_" + role.name().toLowerCase());
        user.setRole(role);
        return user;
    }

    private Mechanic createTestMechanic(Long id, String specialty) {
        Mechanic mechanic = new Mechanic();
        mechanic.setId(id);
        mechanic.setSpecialty(specialty);
        mechanic.setActive(true);
        return mechanic;
    }
}