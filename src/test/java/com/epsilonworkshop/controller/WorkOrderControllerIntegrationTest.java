/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.controller;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.dao.ClientDAO;
import com.epsilonworkshop.dao.MechanicDAO;
import com.epsilonworkshop.dao.UserDAO;
import com.epsilonworkshop.dao.VehicleDAO;
import com.epsilonworkshop.dao.WorkHistoryDAO;
import com.epsilonworkshop.dao.WorkOrderDAO;
import com.epsilonworkshop.exception.InvalidMechanicAssignmentException;
import com.epsilonworkshop.exception.InvalidStatusTransitionException;
import com.epsilonworkshop.exception.UnauthorizedActionException;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.DocumentType;
import com.epsilonworkshop.model.Mechanic;
import com.epsilonworkshop.model.OrderStatus;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.model.Vehicle;
import com.epsilonworkshop.model.WorkOrder;
import com.epsilonworkshop.model.WorkOrderHistory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sebap
 */
public class WorkOrderControllerIntegrationTest {

    private Connection connection;
    private ClientDAO clientDAO;
    private VehicleDAO vehicleDAO;
    private UserDAO userDAO;
    private MechanicDAO mechanicDAO;
    private WorkOrderDAO workOrderDAO;
    private WorkHistoryDAO workHistoryDAO;
    private WorkOrderController controller;

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

        controller = new WorkOrderController(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            connection.rollback();
        } finally {
            connection.close();
        }
    }

    // ---------- changeStatus ----------

    @Test
    void shouldChangeStatusFromPendingToInProgressAsAdmin() throws Exception {
        Mechanic mechanic = createTestMechanic(true);
        WorkOrder order = createTestWorkOrder(mechanic.getId());
        User admin = createTestUser(Role.ADMIN);

        WorkOrder updated = controller.changeStatus(
                order.getId(), OrderStatus.IN_PROGRESS, admin, null
        );

        assertEquals(OrderStatus.IN_PROGRESS, updated.getStatus());
        assertNull(updated.getClosedAt());
    }

    @Test
    void shouldRecordHistoryOnStatusChange() throws Exception {
        Mechanic mechanic = createTestMechanic(true);
        WorkOrder order = createTestWorkOrder(mechanic.getId());
        User admin = createTestUser(Role.ADMIN);

        controller.changeStatus(order.getId(), OrderStatus.IN_PROGRESS, admin, null);

        List<WorkOrderHistory> history = workHistoryDAO.findByWorkOrderId(order.getId());

        assertEquals(1, history.size());
        assertEquals("pending", history.get(0).getOldValue());
        assertEquals("in_progress", history.get(0).getNewValue());
    }

    @Test
    void shouldThrowInvalidStatusTransitionExceptionForSkippedTransition() throws Exception {
        Mechanic mechanic = createTestMechanic(true);
        WorkOrder order = createTestWorkOrder(mechanic.getId());
        User admin = createTestUser(Role.ADMIN);

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> controller.changeStatus(
                        order.getId(), OrderStatus.FINISHED, admin, null
                )
        );
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicChangesUnassignedOrder() throws Exception {
        WorkOrder order = createTestWorkOrder(null);
        Mechanic mechanic = createTestMechanic(true);
        User mechanicUser = userDAO.findById(mechanic.getUserId()).orElseThrow();

        assertThrows(UnauthorizedActionException.class,
                () -> controller.changeStatus(
                        order.getId(), OrderStatus.IN_PROGRESS, mechanicUser, mechanic.getId()
                )
        );
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicChangesSomeoneElsesOrder() throws Exception {
        Mechanic owner = createTestMechanic(true);
        Mechanic other = createTestMechanic(true);
        WorkOrder order = createTestWorkOrder(owner.getId());
        User otherUser = userDAO.findById(other.getUserId()).orElseThrow();

        assertThrows(UnauthorizedActionException.class,
                () -> controller.changeStatus(
                        order.getId(), OrderStatus.IN_PROGRESS, otherUser, other.getId()
                )
        );
    }

    @Test
    void shouldAllowMechanicToChangeTheirOwnOrder() throws Exception {
        Mechanic mechanic = createTestMechanic(true);
        WorkOrder order = createTestWorkOrder(mechanic.getId());
        User mechanicUser = userDAO.findById(mechanic.getUserId()).orElseThrow();

        WorkOrder updated = controller.changeStatus(
                order.getId(), OrderStatus.IN_PROGRESS, mechanicUser, mechanic.getId()
        );

        assertEquals(OrderStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenOrderDoesNotExist() {
        User admin = createTestUser(Role.ADMIN);

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.changeStatus(999999L, OrderStatus.IN_PROGRESS, admin, null)
        );
    }

    // ---------- reassignMechanic ----------

    @Test
    void shouldReassignMechanicAsAdmin() throws Exception {
        Mechanic original = createTestMechanic(true);
        Mechanic replacement = createTestMechanic(true);
        WorkOrder order = createTestWorkOrder(original.getId());
        User admin = createTestUser(Role.ADMIN);

        WorkOrder updated = controller.reassignMechanic(
                order.getId(), replacement.getId(), admin, null
        );

        assertEquals(replacement.getId(), updated.getMechanicId());
    }

    @Test
    void shouldAllowMechanicSelfAssignWhenUnassigned() throws Exception {
        WorkOrder order = createTestWorkOrder(null);
        Mechanic mechanic = createTestMechanic(true);
        User mechanicUser = userDAO.findById(mechanic.getUserId()).orElseThrow();

        WorkOrder updated = controller.reassignMechanic(
                order.getId(), mechanic.getId(), mechanicUser, mechanic.getId()
        );

        assertEquals(mechanic.getId(), updated.getMechanicId());
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToAssignAnotherMechanic() throws Exception {
        WorkOrder order = createTestWorkOrder(null);
        Mechanic actingMechanic = createTestMechanic(true);
        Mechanic otherMechanic = createTestMechanic(true);
        User mechanicUser = userDAO.findById(actingMechanic.getUserId()).orElseThrow();

        assertThrows(UnauthorizedActionException.class,
                () -> controller.reassignMechanic(
                        order.getId(), otherMechanic.getId(), mechanicUser, actingMechanic.getId()
                )
        );
    }

    @Test
    void shouldThrowUnauthorizedWhenMechanicTriesToReassignAlreadyAssignedOrder() throws Exception {
        Mechanic owner = createTestMechanic(true);
        Mechanic other = createTestMechanic(true);
        WorkOrder order = createTestWorkOrder(owner.getId());
        User otherUser = userDAO.findById(other.getUserId()).orElseThrow();

        assertThrows(UnauthorizedActionException.class,
                () -> controller.reassignMechanic(
                        order.getId(), other.getId(), otherUser, other.getId()
                )
        );
    }

    @Test
    void shouldThrowInvalidMechanicAssignmentExceptionWhenMechanicIsInactive() throws Exception {
        Mechanic original = createTestMechanic(true);
        Mechanic inactiveMechanic = createTestMechanic(false);
        WorkOrder order = createTestWorkOrder(original.getId());
        User admin = createTestUser(Role.ADMIN);

        assertThrows(
                InvalidMechanicAssignmentException.class,
                () -> controller.reassignMechanic(
                        order.getId(), inactiveMechanic.getId(), admin, null
                )
        );
    }

    @Test
    void shouldThrowUnauthorizedWhenReassigningFinalOrder() throws Exception {
        Mechanic mechanic = createTestMechanic(true);
        Mechanic replacement = createTestMechanic(true);
        WorkOrder order = createTestWorkOrder(mechanic.getId());
        User admin = createTestUser(Role.ADMIN);

        controller.changeStatus(order.getId(), OrderStatus.IN_PROGRESS, admin, null);
        controller.changeStatus(order.getId(), OrderStatus.FINISHED, admin, null);

        assertThrows(UnauthorizedActionException.class,
                () -> controller.reassignMechanic(
                        order.getId(), replacement.getId(), admin, null
                )
        );
    }

    // ---------- helpers ----------

    private WorkOrder createTestWorkOrder(Long mechanicId) throws Exception {
        Vehicle vehicle = createTestVehicle();

        WorkOrder workOrder = new WorkOrder();
        workOrder.setVehicleId(vehicle.getId());
        workOrder.setMechanicId(mechanicId);
        workOrder.setStatus(OrderStatus.PENDING);
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
        client.setPaternalSurname("User");
        client.setPhone("999999999");

        return clientDAO.create(client);
    }

    private Mechanic createTestMechanic(boolean active) throws Exception {
        User user = createTestUserInDb(Role.MECHANIC);

        Mechanic mechanic = new Mechanic();
        mechanic.setUserId(user.getId());
        mechanic.setSpecialty("Diagnóstico");

        Mechanic created = mechanicDAO.create(mechanic);

        if (!active) {
            mechanicDAO.softDelete(created.getId());
            created.setActive(false);
        }

        return created;
    }

    private User createTestUserInDb(Role role) throws Exception {
        User user = new User();
        user.setUsername(generateTestUsername());
        user.setPasswordHash("test-hash");
        user.setRole(role);
        user.setName("Test");
        user.setPaternalSurname("User");

        return userDAO.create(user);
    }

    private User createTestUser(Role role) {
        // Solo para ADMIN en estos tests: no necesita fila real en mechanics,
        // así que no pasa por userDAO -- basta un User en memoria con id fijo.
        User user = new User();
        user.setId(1L);
        user.setUsername("test_admin");
        user.setRole(role);
        return user;
    }

    private String generateTestDocument() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000));
    }

    private String generateTestUsername() {
        return "test_ctrl_" + ThreadLocalRandom.current().nextLong(1_000_000, 10_000_000);
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