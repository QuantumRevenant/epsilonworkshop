/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.dao.MechanicDAO;
import com.epsilonworkshop.dao.VehicleDAO;
import com.epsilonworkshop.dao.WorkHistoryDAO;
import com.epsilonworkshop.dao.WorkOrderDAO;
import com.epsilonworkshop.exception.InvalidMechanicAssignmentException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Mechanic;
import com.epsilonworkshop.model.OrderStatus;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.model.Vehicle;
import com.epsilonworkshop.model.WorkOrder;
import com.epsilonworkshop.model.WorkOrderChangeType;
import com.epsilonworkshop.model.WorkOrderHistory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author sebap
 */
@ExtendWith(MockitoExtension.class)
public class WorkOrderControllerTest {

    @Mock
    private WorkOrderDAO workOrderDAO;

    @Mock
    private WorkHistoryDAO workHistoryDAO;

    @Mock
    private MechanicDAO mechanicDAO;

    @Mock
    private VehicleDAO vehicleDAO;

    @InjectMocks
    private WorkOrderController controller;

    @Test
    void shouldCreateWorkOrderWithoutMechanic() throws Exception {
        User admin = createTestUser(Role.ADMIN);

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(new Vehicle()));
        when(workOrderDAO.create(any(WorkOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder created = controller.createWorkOrder(
                1L, null, "Cambio de aceite", admin, null
        );

        assertNotNull(created);
        assertNull(created.getMechanicId());
        assertEquals(OrderStatus.PENDING, created.getStatus());
        verify(mechanicDAO, never()).findById(any());
    }

    @Test
    void shouldCreateWorkOrderWhenAdminAssignsAnyActiveMechanic() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Mechanic activeMechanic = createTestMechanic(5L, true);

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(new Vehicle()));
        when(mechanicDAO.findById(5L)).thenReturn(Optional.of(activeMechanic));
        when(workOrderDAO.create(any(WorkOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder created = controller.createWorkOrder(
                1L, 5L, "Revisión de frenos", admin, null
        );

        assertEquals(5L, created.getMechanicId());
    }

    @Test
    void shouldAllowMechanicToAssignThemselves() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);
        Mechanic activeMechanic = createTestMechanic(7L, true);

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(new Vehicle()));
        when(mechanicDAO.findById(7L)).thenReturn(Optional.of(activeMechanic));
        when(workOrderDAO.create(any(WorkOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkOrder created = controller.createWorkOrder(
                1L, 7L, "Cambio de llantas", mechanicUser, 7L
        );

        assertEquals(7L, created.getMechanicId());
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicAssignsSomeoneElse() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(new Vehicle()));

        assertThrows(UnauthorizedActionException.class,
                () -> controller.createWorkOrder(
                        1L, 99L, "Cambio de llantas", mechanicUser, 7L
                )
        );

        verify(workOrderDAO, never()).create(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenVehicleDoesNotExist() throws Exception {
        User admin = createTestUser(Role.ADMIN);

        when(vehicleDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createWorkOrder(
                        999L, null, "Cualquiera", admin, null
                )
        );

        verify(workOrderDAO, never()).create(any());
    }

    @Test
    void shouldThrowInvalidMechanicAssignmentExceptionWhenMechanicDoesNotExist() throws Exception {
        User admin = createTestUser(Role.ADMIN);

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(new Vehicle()));
        when(mechanicDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                InvalidMechanicAssignmentException.class,
                () -> controller.createWorkOrder(
                        1L, 999L, "Cualquiera", admin, null
                )
        );

        verify(workOrderDAO, never()).create(any());
    }

    @Test
    void shouldThrowInvalidMechanicAssignmentExceptionWhenMechanicIsInactive() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Mechanic inactiveMechanic = createTestMechanic(3L, false);

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(new Vehicle()));
        when(mechanicDAO.findById(3L)).thenReturn(Optional.of(inactiveMechanic));

        assertThrows(
                InvalidMechanicAssignmentException.class,
                () -> controller.createWorkOrder(
                        1L, 3L, "Cualquiera", admin, null
                )
        );

        verify(workOrderDAO, never()).create(any());
    }

    @Test
    void shouldReturnHistoryForWorkOrder() throws Exception {
        WorkOrderHistory firstChange = new WorkOrderHistory();
        firstChange.setWorkOrderId(1L);
        firstChange.setChangeType(WorkOrderChangeType.STATUS_CHANGE);
        firstChange.setOldValue("pending");
        firstChange.setNewValue("in_progress");

        when(workHistoryDAO.findByWorkOrderId(1L))
                .thenReturn(List.of(firstChange));

        List<WorkOrderHistory> history = controller.getHistory(1L);

        assertEquals(1, history.size());
        assertEquals("in_progress", history.get(0).getNewValue());
    }

    private User createTestUser(Role role) {
        User user = new User();
        user.setId(1L);
        user.setUsername("test_" + role.name().toLowerCase());
        user.setRole(role);
        return user;
    }

    private Mechanic createTestMechanic(Long id, boolean active) {
        Mechanic mechanic = new Mechanic();
        mechanic.setId(id);
        mechanic.setActive(active);
        return mechanic;
    }
}
