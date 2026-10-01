/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.dao.*;
import com.epsilonworkshop.exception.DuplicateUsernameException;
import com.epsilonworkshop.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sebap
 */
public class MechanicControllerIntegrationTest {

    private Connection connection;
    private ClientDAO clientDAO;
    private VehicleDAO vehicleDAO;
    private UserDAO userDAO;
    private MechanicDAO mechanicDAO;
    private WorkOrderDAO workOrderDAO;
    private WorkHistoryDAO workHistoryDAO;
    private MechanicController controller;

    @BeforeEach
    void setUp() throws Exception {
        connection = DBConnection.getConnection();
        connection.setAutoCommit(false);

        clientDAO = new ClientDAO(connection);
        vehicleDAO = new VehicleDAO(connection);
        userDAO = new UserDAO(connection);
        mechanicDAO = new MechanicDAO(connection);
        workOrderDAO = new WorkOrderDAO(connection);
        workHistoryDAO = new WorkHistoryDAO(connection);

        controller = new MechanicController(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            connection.rollback();
        } finally {
            connection.close();
        }
    }

    // ---------- create ----------

    @Test
    void shouldCreateUserAndMechanicTogether() throws Exception {
        User admin = createTestAdmin();
        String username = generateTestUsername();

        Mechanic created = controller.create(
                username, "plainPassword123", "Juan", "Perez", "Lopez",
                "Diagnóstico", admin
        );

        assertNotNull(created.getId());
        assertEquals("Diagnóstico", created.getSpecialty());

        // Confirma que el User asociado realmente se creó en la misma transacción
        User createdUser = userDAO.findById(created.getUserId()).orElseThrow();
        assertEquals(username, createdUser.getUsername());
        assertEquals(Role.MECHANIC, createdUser.getRole());
        assertNotEquals("plainPassword123", createdUser.getPasswordHash()); // debe estar hasheado
    }

    @Test
    void shouldThrowDuplicateUsernameExceptionWhenUsernameAlreadyExists() throws Exception {
        User admin = createTestAdmin();
        String username = generateTestUsername();

        controller.create(
                username, "pass1", "Juan", "Perez", null, "Diagnóstico", admin
        );

        assertThrows(
                DuplicateUsernameException.class,
                () -> controller.create(
                        username, "pass2", "Otro", "Nombre", null, "Frenos", admin
                )
        );
    }

    @Test
    void shouldRollbackMechanicCreationWhenUsernameIsDuplicate() throws Exception {
        User admin = createTestAdmin();
        String username = generateTestUsername();

        controller.create(username, "pass1", "Juan", "Perez", null, "Diagnóstico", admin);

        List<Mechanic> before = mechanicDAO.findAll();
        int countBefore = before.size();

        try {
            controller.create(username, "pass2", "Otro", "Nombre", null, "Frenos", admin);
        } catch (DuplicateUsernameException ignored) {
            // esperado
        }

        List<Mechanic> after = mechanicDAO.findAll();
        assertEquals(countBefore, after.size()); // no se creó un segundo mecánico a medias
    }

    // ---------- softDelete ----------

    @Test
    void shouldSoftDeleteMechanicWithoutAssignedOrders() throws Exception {
        User admin = createTestAdmin();
        Mechanic mechanic = createTestMechanicInDb();

        controller.softDelete(mechanic.getId(), admin);

        Mechanic result = mechanicDAO.findById(mechanic.getId()).orElseThrow();
        assertFalse(result.getActive());
    }

    @Test
    void shouldUnassignNonFinalOrdersWhenDeactivatingMechanic() throws Exception {
        User admin = createTestAdmin();
        Mechanic mechanic = createTestMechanicInDb();
        WorkOrder pendingOrder = createTestWorkOrder(mechanic.getId(), OrderStatus.PENDING);
        WorkOrder inProgressOrder = createTestWorkOrder(mechanic.getId(), OrderStatus.IN_PROGRESS);

        controller.softDelete(mechanic.getId(), admin);

        WorkOrder updatedPending = workOrderDAO.findById(pendingOrder.getId()).orElseThrow();
        WorkOrder updatedInProgress = workOrderDAO.findById(inProgressOrder.getId()).orElseThrow();

        assertNull(updatedPending.getMechanicId());
        assertNull(updatedInProgress.getMechanicId());
    }

    @Test
    void shouldNotUnassignFinalOrdersWhenDeactivatingMechanic() throws Exception {
        User admin = createTestAdmin();
        Mechanic mechanic = createTestMechanicInDb();
        WorkOrder finishedOrder = createTestWorkOrder(mechanic.getId(), OrderStatus.PENDING);

        // Llevar la orden a estado final antes de desactivar al mecánico
        new WorkOrderController(connection).changeStatus(
                finishedOrder.getId(), OrderStatus.IN_PROGRESS, admin, null
        );
        new WorkOrderController(connection).changeStatus(
                finishedOrder.getId(), OrderStatus.FINISHED, admin, null
        );

        controller.softDelete(mechanic.getId(), admin);

        WorkOrder result = workOrderDAO.findById(finishedOrder.getId()).orElseThrow();
        assertEquals(mechanic.getId(), result.getMechanicId()); // sigue asignado, no se toca
    }

    @Test
    void shouldRecordHistoryForEachUnassignedOrder() throws Exception {
        User admin = createTestAdmin();
        Mechanic mechanic = createTestMechanicInDb();
        WorkOrder order = createTestWorkOrder(mechanic.getId(), OrderStatus.PENDING);

        controller.softDelete(mechanic.getId(), admin);

        List<WorkOrderHistory> history = workHistoryDAO.findByWorkOrderId(order.getId());

        assertEquals(1, history.size());
        assertEquals(WorkOrderChangeType.MECHANIC_REASSIGNMENT, history.get(0).getChangeType());
        assertEquals("sin asignar", history.get(0).getNewValue());
    }

    // ---------- helpers ----------

    private User createTestAdmin() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("test_admin");
        admin.setRole(Role.ADMIN);
        return admin;
    }

    private Mechanic createTestMechanicInDb() throws Exception {
        Mechanic created = controller.create(
                generateTestUsername(), "pass123", "Test", "Mechanic", null,
                "Diagnóstico", createTestAdmin()
        );
        return created;
    }

    private WorkOrder createTestWorkOrder(Long mechanicId, OrderStatus status) throws Exception {
        Vehicle vehicle = createTestVehicle();

        WorkOrder workOrder = new WorkOrder();
        workOrder.setVehicleId(vehicle.getId());
        workOrder.setMechanicId(mechanicId);
        workOrder.setStatus(status);
        workOrder.setDescription("Orden de prueba");

        return workOrderDAO.create(workOrder);
    }

    private Vehicle createTestVehicle() throws Exception {
        Client client = createTestClient();

        Vehicle vehicle = new Vehicle();
        vehicle.setClientId(client.getId());
        vehicle.setPlate(generateTestPlate());
        vehicle.setBrand("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setYear(2020);

        return vehicleDAO.create(vehicle);
    }

    private Client createTestClient() throws Exception {
        Client client = new Client();
        client.setDocumentType(DocumentType.DNI);
        client.setDocument(generateTestDocument());
        client.setName("Test");
        client.setPaternalSurname("Client");
        client.setPhone("999999999");

        return clientDAO.create(client);
    }

    private String generateTestUsername() {
        return "test_mech_" + ThreadLocalRandom.current().nextLong(1_000_000, 10_000_000);
    }

    private String generateTestDocument() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000));
    }

    private String generateTestPlate() {
        return String.format(
                "%c%c%c-%03d",
                ThreadLocalRandom.current().nextInt('A', 'Z' + 1),
                ThreadLocalRandom.current().nextInt('A', 'Z' + 1),
                ThreadLocalRandom.current().nextInt('A', 'Z' + 1),
                ThreadLocalRandom.current().nextInt(0, 1000)
        );
    }
}