/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.DocumentType;
import com.epsilonworkshop.model.Mechanic;
import com.epsilonworkshop.model.OrderStatus;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.model.Vehicle;
import com.epsilonworkshop.model.WorkOrder;
import com.epsilonworkshop.model.WorkOrderChangeType;
import com.epsilonworkshop.model.WorkOrderHistory;
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
public class WorkHistoryDAOTest {
    
    private Connection connection;

    private ClientDAO clientDAO;
    private VehicleDAO vehicleDAO;
    private UserDAO userDAO;
    private MechanicDAO mechanicDAO;
    private WorkOrderDAO workOrderDAO;
    private WorkHistoryDAO workHistoryDAO;

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
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            connection.rollback();
        } finally {
            connection.close();
        }
    }

    @Test
    void shouldCreateWorkOrderHistory() throws Exception {
        WorkOrder workOrder = createTestWorkOrder();
        User user = createTestUser();

        WorkOrderHistory history = new WorkOrderHistory();

        history.setWorkOrderId(workOrder.getId());
        history.setUserId(user.getId());
        history.setChangeType(WorkOrderChangeType.STATUS_CHANGE);
        history.setOldValue("pending");
        history.setNewValue("in_progress");

        WorkOrderHistory created = workHistoryDAO.create(history);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(workOrder.getId(), created.getWorkOrderId());
        assertEquals(user.getId(), created.getUserId());
        assertEquals(
                WorkOrderChangeType.STATUS_CHANGE,
                created.getChangeType()
        );
        assertEquals("pending", created.getOldValue());
        assertEquals("in_progress", created.getNewValue());
        assertNotNull(created.getChangedAt());
    }

    @Test
    void shouldCreateMechanicReassignmentHistory() throws Exception {
        WorkOrder workOrder = createTestWorkOrder();
        User user = createTestUser();

        WorkOrderHistory history = new WorkOrderHistory();

        history.setWorkOrderId(workOrder.getId());
        history.setUserId(user.getId());
        history.setChangeType(
                WorkOrderChangeType.MECHANIC_REASSIGNMENT
        );
        history.setOldValue("1");
        history.setNewValue("2");

        WorkOrderHistory created = workHistoryDAO.create(history);

        assertNotNull(created);
        assertEquals(
                WorkOrderChangeType.MECHANIC_REASSIGNMENT,
                created.getChangeType()
        );
        assertEquals("1", created.getOldValue());
        assertEquals("2", created.getNewValue());
    }

    @Test
    void shouldFindHistoryByWorkOrderId() throws Exception {
        WorkOrder workOrder = createTestWorkOrder();
        User user = createTestUser();

        WorkOrderHistory first = createTestHistory(
                workOrder,
                user,
                WorkOrderChangeType.STATUS_CHANGE,
                "pending",
                "in_progress"
        );

        WorkOrderHistory second = createTestHistory(
                workOrder,
                user,
                WorkOrderChangeType.STATUS_CHANGE,
                "in_progress",
                "finished"
        );

        List<WorkOrderHistory> history =
                workHistoryDAO.findByWorkOrderId(workOrder.getId());

        assertEquals(2, history.size());

        assertTrue(
                history.stream()
                        .anyMatch(item -> item.getId().equals(first.getId()))
        );

        assertTrue(
                history.stream()
                        .anyMatch(item -> item.getId().equals(second.getId()))
        );
    }

    @Test
    void shouldReturnEmptyListWhenWorkOrderHasNoHistory() throws Exception {
        WorkOrder workOrder = createTestWorkOrder();

        List<WorkOrderHistory> history =
                workHistoryDAO.findByWorkOrderId(workOrder.getId());

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    private WorkOrderHistory createTestHistory(
            WorkOrder workOrder,
            User user,
            WorkOrderChangeType changeType,
            String oldValue,
            String newValue
    ) throws Exception {

        WorkOrderHistory history = new WorkOrderHistory();

        history.setWorkOrderId(workOrder.getId());
        history.setUserId(user.getId());
        history.setChangeType(changeType);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);

        return workHistoryDAO.create(history);
    }

    private WorkOrder createTestWorkOrder() throws Exception {
        Vehicle vehicle = createTestVehicle();
        Mechanic mechanic = createTestMechanic();

        WorkOrder workOrder = new WorkOrder();

        workOrder.setVehicleId(vehicle.getId());
        workOrder.setMechanicId(mechanic.getId());
        workOrder.setStatus(OrderStatus.PENDING);
        workOrder.setDescription("Reparación de prueba");

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
        vehicle.setNotes("Vehículo de prueba");

        return vehicleDAO.create(vehicle);
    }

    private Client createTestClient() throws Exception {
        Client client = new Client();

        client.setDocumentType(DocumentType.DNI);
        client.setDocument(generateTestDocument());
        client.setName("Test");
        client.setPaternalSurname("User");
        client.setMaternalSurname("Example");
        client.setPhone("999999999");
        client.setEmail("test@example.com");
        client.setNotes("Created by test");

        return clientDAO.create(client);
    }

    private Mechanic createTestMechanic() throws Exception {
        User user = createTestUser();

        Mechanic mechanic = new Mechanic();

        mechanic.setUserId(user.getId());
        mechanic.setSpecialty("Diagnóstico");

        return mechanicDAO.create(mechanic);
    }

    private User createTestUser() throws Exception {
        User user = new User();

        user.setUsername(generateTestUsername());
        user.setPasswordHash("test-hash");
        user.setRole(Role.MECHANIC);
        user.setName("Test");
        user.setPaternalSurname("User");
        user.setMaternalSurname("Example");

        return userDAO.create(user);
    }

    private String generateTestDocument() {
        return String.valueOf(
                ThreadLocalRandom.current()
                        .nextInt(10_000_000, 100_000_000)
        );
    }

    private String generateTestUsername() {
        return "test_history_"
                + ThreadLocalRandom.current()
                        .nextLong(1_000_000, 10_000_000);
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
