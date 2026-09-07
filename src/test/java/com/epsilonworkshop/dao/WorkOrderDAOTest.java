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
import com.epsilonworkshop.model.User;
import com.epsilonworkshop.model.Vehicle;
import com.epsilonworkshop.model.WorkOrder;
import com.epsilonworkshop.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sebap
 */
public class WorkOrderDAOTest {

    private Connection connection;
    private ClientDAO clientDAO;
    private VehicleDAO vehicleDAO;
    private UserDAO userDAO;
    private MechanicDAO mechanicDAO;
    private WorkOrderDAO workOrderDAO;

    @BeforeEach
    void setUp() throws Exception {
        connection = DBConnection.getConnection();
        connection.setAutoCommit(false);

        clientDAO = new ClientDAO(connection);
        vehicleDAO = new VehicleDAO(connection);
        userDAO = new UserDAO(connection);
        mechanicDAO = new MechanicDAO(connection);
        workOrderDAO = new WorkOrderDAO(connection);
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
    void shouldCreateWorkOrder() throws Exception {
        Vehicle vehicle = createTestVehicle();
        Mechanic mechanic = createTestMechanic();

        WorkOrder workOrder = new WorkOrder();
        workOrder.setVehicleId(vehicle.getId());
        workOrder.setMechanicId(mechanic.getId());
        workOrder.setStatus(OrderStatus.PENDING);
        workOrder.setDescription("Cambio de aceite");

        WorkOrder created = workOrderDAO.create(workOrder);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(vehicle.getId(), created.getVehicleId());
        assertEquals(mechanic.getId(), created.getMechanicId());
        assertEquals(OrderStatus.PENDING, created.getStatus());
        assertEquals("Cambio de aceite", created.getDescription());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        assertNull(created.getClosedAt());
    }

    @Test
    void shouldCreateWorkOrderWithoutMechanic() throws Exception {
        Vehicle vehicle = createTestVehicle();

        WorkOrder workOrder = new WorkOrder();
        workOrder.setVehicleId(vehicle.getId());
        workOrder.setMechanicId(null);
        workOrder.setStatus(OrderStatus.PENDING);
        workOrder.setDescription("Revisión general");

        WorkOrder created = workOrderDAO.create(workOrder);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(vehicle.getId(), created.getVehicleId());
        assertNull(created.getMechanicId());
        assertEquals(OrderStatus.PENDING, created.getStatus());
        assertEquals("Revisión general", created.getDescription());
        assertNull(created.getClosedAt());
    }

    @Test
    void shouldFindWorkOrderById() throws Exception {
        WorkOrder created = createTestWorkOrder();

        Optional<WorkOrder> result
                = workOrderDAO.findById(created.getId());

        assertTrue(result.isPresent());

        WorkOrder found = result.get();

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getVehicleId(), found.getVehicleId());
        assertEquals(created.getMechanicId(), found.getMechanicId());
        assertEquals(created.getStatus(), found.getStatus());
        assertEquals(created.getDescription(), found.getDescription());
    }

    @Test
    void shouldFindAllWorkOrders() throws Exception {
        WorkOrder first = createTestWorkOrder();
        WorkOrder second = createTestWorkOrder();

        List<WorkOrder> workOrders = workOrderDAO.findAll();

        assertTrue(
                workOrders.stream()
                        .anyMatch(order -> order.getId().equals(first.getId()))
        );

        assertTrue(
                workOrders.stream()
                        .anyMatch(order -> order.getId().equals(second.getId()))
        );
    }

    @Test
    void shouldFindWorkOrdersByVehicleId() throws Exception {
        Vehicle vehicle = createTestVehicle();

        WorkOrder first = createTestWorkOrder(vehicle);
        WorkOrder second = createTestWorkOrder(vehicle);

        List<WorkOrder> workOrders
                = workOrderDAO.findByVehicleId(vehicle.getId());

        assertTrue(
                workOrders.stream()
                        .anyMatch(order -> order.getId().equals(first.getId()))
        );

        assertTrue(
                workOrders.stream()
                        .anyMatch(order -> order.getId().equals(second.getId()))
        );
    }

    @Test
    void shouldFindWorkOrdersByMechanicId() throws Exception {
        Mechanic mechanic = createTestMechanic();
        Vehicle vehicle = createTestVehicle();

        WorkOrder first = createTestWorkOrder(vehicle, mechanic);
        WorkOrder second = createTestWorkOrder(vehicle, mechanic);

        List<WorkOrder> workOrders
                = workOrderDAO.findByMechanicId(mechanic.getId());

        assertEquals(2, workOrders.size());

        assertTrue(
                workOrders.stream()
                        .anyMatch(order -> order.getId().equals(first.getId()))
        );

        assertTrue(
                workOrders.stream()
                        .anyMatch(order -> order.getId().equals(second.getId()))
        );
    }

    @Test
    void shouldUpdateWorkOrder() throws Exception {
        WorkOrder workOrder = createTestWorkOrder();

        workOrder.setStatus(OrderStatus.IN_PROGRESS);
        workOrder.setDescription("Cambio de aceite y filtros");

        WorkOrder updated = workOrderDAO.update(workOrder);

        assertNotNull(updated);
        assertEquals(workOrder.getId(), updated.getId());
        assertEquals(workOrder.getVehicleId(), updated.getVehicleId());
        assertEquals(workOrder.getMechanicId(), updated.getMechanicId());
        assertEquals(OrderStatus.IN_PROGRESS, updated.getStatus());
        assertEquals(
                "Cambio de aceite y filtros",
                updated.getDescription()
        );
        assertNull(updated.getClosedAt());
    }

    private WorkOrder createTestWorkOrder() throws Exception {
        Vehicle vehicle = createTestVehicle();
        Mechanic mechanic = createTestMechanic();

        return createTestWorkOrder(vehicle, mechanic);
    }

    private WorkOrder createTestWorkOrder(Vehicle vehicle) throws Exception {
        return createTestWorkOrder(
                vehicle,
                null
        );
    }

    private WorkOrder createTestWorkOrder(
            Vehicle vehicle,
            Mechanic mechanic
    ) throws Exception {

        WorkOrder workOrder = new WorkOrder();

        workOrder.setVehicleId(vehicle.getId());

        if (mechanic != null) {
            workOrder.setMechanicId(mechanic.getId());
        } else {
            workOrder.setMechanicId(null);
        }

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
        return "test_workorder_"
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
