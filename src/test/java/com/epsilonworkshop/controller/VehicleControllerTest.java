/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.dao.ClientDAO;
import com.epsilonworkshop.dao.VehicleDAO;
import com.epsilonworkshop.exception.DuplicatePlateException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.model.Vehicle;
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
public class VehicleControllerTest {

    @Mock
    private VehicleDAO vehicleDAO;

    @Mock
    private ClientDAO clientDAO;

    @InjectMocks
    private VehicleController controller;

    // ---------- create ----------

    @Test
    void shouldCreateVehicleAsAdmin() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Vehicle vehicle = createTestVehicle(null, 1L, "ABC-123");

        when(clientDAO.findById(1L)).thenReturn(Optional.of(new Client()));
        when(vehicleDAO.findByPlate("ABC-123")).thenReturn(Optional.empty());
        when(vehicleDAO.create(vehicle)).thenReturn(vehicle);

        Vehicle created = controller.create(vehicle, admin);

        assertEquals(vehicle, created);
        verify(vehicleDAO).create(vehicle);
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToCreateVehicle() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);
        Vehicle vehicle = createTestVehicle(null, 1L, "ABC-123");

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.create(vehicle, mechanicUser)
        );

        verify(vehicleDAO, never()).create(any());
        verify(clientDAO, never()).findById(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenClientDoesNotExist() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Vehicle vehicle = createTestVehicle(null, 999L, "ABC-123");

        when(clientDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.create(vehicle, admin)
        );

        verify(vehicleDAO, never()).create(any());
    }

    @Test
    void shouldThrowDuplicatePlateExceptionWhenPlateAlreadyExists() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Vehicle newVehicle = createTestVehicle(null, 1L, "ABC-123");
        Vehicle existingVehicle = createTestVehicle(5L, 2L, "ABC-123");

        when(clientDAO.findById(1L)).thenReturn(Optional.of(new Client()));
        when(vehicleDAO.findByPlate("ABC-123")).thenReturn(Optional.of(existingVehicle));

        assertThrows(
                DuplicatePlateException.class,
                () -> controller.create(newVehicle, admin)
        );

        verify(vehicleDAO, never()).create(any());
    }

    // ---------- update ----------

    @Test
    void shouldUpdateVehicleAsAdmin() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Vehicle vehicle = createTestVehicle(1L, 1L, "ABC-123");

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleDAO.findByPlate("ABC-123")).thenReturn(Optional.of(vehicle));
        when(vehicleDAO.update(vehicle)).thenReturn(vehicle);

        Vehicle updated = controller.update(vehicle, admin);

        assertEquals(vehicle, updated);
    }

    @Test
    void shouldIgnoreClientIdChangeInUpdate() throws Exception {
        User admin = createTestUser(Role.ADMIN);

        Vehicle existing = createTestVehicle(1L, 1L, "ABC-123");
        Vehicle incomingChange = createTestVehicle(1L, 999L, "ABC-123"); // intenta cambiar el dueño

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(existing));
        when(vehicleDAO.findByPlate("ABC-123")).thenReturn(Optional.of(existing));
        when(vehicleDAO.update(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle updated = controller.update(incomingChange, admin);

        // RN-10: el clientId no debió cambiar, aunque venía distinto en el objeto de entrada
        assertEquals(1L, updated.getClientId());
        verify(clientDAO, never()).findById(any());
    }

    @Test
    void shouldThrowDuplicatePlateExceptionWhenAnotherVehicleHasSamePlate() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Vehicle vehicleBeingUpdated = createTestVehicle(1L, 1L, "ABC-123");
        Vehicle otherVehicleWithSamePlate = createTestVehicle(2L, 3L, "ABC-123");

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(vehicleBeingUpdated));
        when(vehicleDAO.findByPlate("ABC-123")).thenReturn(Optional.of(otherVehicleWithSamePlate));

        assertThrows(
                DuplicatePlateException.class,
                () -> controller.update(vehicleBeingUpdated, admin)
        );

        verify(vehicleDAO, never()).update(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenVehicleToUpdateDoesNotExist() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Vehicle vehicle = createTestVehicle(999L, 1L, "ABC-123");

        when(vehicleDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.update(vehicle, admin)
        );

        verify(vehicleDAO, never()).update(any());
    }

    // ---------- softDelete ----------

    @Test
    void shouldSoftDeleteVehicleAsAdmin() throws Exception {
        User admin = createTestUser(Role.ADMIN);

        controller.softDelete(1L, admin);

        verify(vehicleDAO).softDelete(1L);
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToSoftDeleteVehicle() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.softDelete(1L, mechanicUser)
        );

        verify(vehicleDAO, never()).softDelete(any());
    }

    // ---------- transferOwnership ----------

    @Test
    void shouldTransferOwnershipAsAdmin() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Vehicle vehicle = createTestVehicle(1L, 1L, "ABC-123");

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(vehicle));
        when(clientDAO.findById(2L)).thenReturn(Optional.of(new Client()));
        when(vehicleDAO.update(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle updated = controller.transferOwnership(1L, 2L, admin);

        assertEquals(2L, updated.getClientId());
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToTransferOwnership() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);

        assertThrows(
                UnauthorizedActionException.class,
                () -> controller.transferOwnership(1L, 2L, mechanicUser)
        );

        verify(vehicleDAO, never()).update(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTransferringToNonexistentClient() throws Exception {
        User admin = createTestUser(Role.ADMIN);
        Vehicle vehicle = createTestVehicle(1L, 1L, "ABC-123");

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(vehicle));
        when(clientDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.transferOwnership(1L, 999L, admin)
        );

        verify(vehicleDAO, never()).update(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTransferringNonexistentVehicle() throws Exception {
        User admin = createTestUser(Role.ADMIN);

        when(vehicleDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.transferOwnership(999L, 2L, admin)
        );

        verify(clientDAO, never()).findById(any());
        verify(vehicleDAO, never()).update(any());
    }

    // ---------- findById / findAll (sin restricción de rol) ----------

    @Test
    void shouldAllowMechanicToFindVehicleById() throws Exception {
        User mechanicUser = createTestUser(Role.MECHANIC);
        Vehicle vehicle = createTestVehicle(1L, 1L, "ABC-123");

        when(vehicleDAO.findById(1L)).thenReturn(Optional.of(vehicle));

        Optional<Vehicle> result = controller.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(vehicle, result.get());
    }

    // ---------- helpers ----------

    private User createTestUser(Role role) {
        User user = new User();
        user.setId(1L);
        user.setUsername("test_" + role.name().toLowerCase());
        user.setRole(role);
        return user;
    }

    private Vehicle createTestVehicle(Long id, Long clientId, String plate) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setClientId(clientId);
        vehicle.setPlate(plate);
        vehicle.setBrand("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setYear(2020);
        return vehicle;
    }
}
