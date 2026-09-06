/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.model.Client;
import com.epsilonworkshop.model.DocumentType;
import com.epsilonworkshop.model.Vehicle;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sebap
 */
public class VehicleDAOTest {

    private Connection connection;
    private ClientDAO clientDAO;
    private VehicleDAO vehicleDAO;

    private String generateTestDocument() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000)
        );
    }

    private String generateTestPlate() {
        return String.format("%c%c%c-%03d",
                ThreadLocalRandom.current().nextInt('A', 'Z' + 1),
                ThreadLocalRandom.current().nextInt('A', 'Z' + 1),
                ThreadLocalRandom.current().nextInt('A', 'Z' + 1),
                ThreadLocalRandom.current().nextInt(0, 1000)
        );
    }

    @BeforeEach
    void setUp() throws Exception {
        connection = DBConnection.getConnection();
        connection.setAutoCommit(false);

        clientDAO = new ClientDAO(connection);
        vehicleDAO = new VehicleDAO(connection);
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
    void shouldCreateVehicle() throws Exception {

        Vehicle vehicle = createTestVehicle();

        assertNotNull(vehicle.getId());
        assertNotNull(vehicle.getClientId());
        assertNotNull(vehicle.getPlate());
        assertEquals("Toyota", vehicle.getBrand());
        assertEquals("Yaris", vehicle.getModel());
        assertEquals(2020, vehicle.getYear());
        assertTrue(vehicle.getActive());
        assertNotNull(vehicle.getCreatedAt());
        assertNotNull(vehicle.getUpdatedAt());
    }

    @Test
    void shouldFindVehicleById() throws Exception {

        Vehicle created = createTestVehicle();

        Optional<Vehicle> result
                = vehicleDAO.findById(created.getId());

        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals(created.getPlate(), result.get().getPlate());
    }

    @Test
    void shouldFindVehicleByPlate() throws Exception {

        Vehicle created = createTestVehicle();

        Optional<Vehicle> result
                = vehicleDAO.findByPlate(created.getPlate());

        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals(created.getPlate(), result.get().getPlate());
    }

    @Test
    void shouldFindAllActiveVehicles() throws Exception {

        Vehicle activeVehicle = createTestVehicle();
        Vehicle deletedVehicle = createTestVehicle();

        vehicleDAO.softDelete(deletedVehicle.getId());

        List<Vehicle> vehicles = vehicleDAO.findAll();

        assertTrue(
                vehicles.stream()
                        .anyMatch(vehicle -> vehicle.getId().equals(activeVehicle.getId()))
        );

        assertFalse(
                vehicles.stream()
                        .anyMatch(vehicle -> vehicle.getId().equals(deletedVehicle.getId()))
        );
    }

    @Test
    void shouldUpdateVehicle() throws Exception {

        Vehicle vehicle = createTestVehicle();

        vehicle.setBrand("Honda");
        vehicle.setModel("Civic");
        vehicle.setYear(2022);
        vehicle.setNotes("Updated by test");

        Vehicle updated = vehicleDAO.update(vehicle);

        assertEquals(vehicle.getId(), updated.getId());
        assertEquals(vehicle.getClientId(), updated.getClientId());
        assertEquals(vehicle.getPlate(), updated.getPlate());
        assertEquals("Honda", updated.getBrand());
        assertEquals("Civic", updated.getModel());
        assertEquals(2022, updated.getYear());
        assertEquals("Updated by test", updated.getNotes());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void shouldSoftDeleteVehicle() throws Exception {

        Vehicle vehicle = createTestVehicle();

        vehicleDAO.softDelete(vehicle.getId());

        Optional<Vehicle> result
                = vehicleDAO.findById(vehicle.getId());

        assertTrue(result.isPresent());
        assertFalse(result.get().getActive());
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

    private Vehicle createTestVehicle() throws Exception {

        Client client = createTestClient();

        Vehicle vehicle = new Vehicle();

        vehicle.setClientId(client.getId());
        vehicle.setPlate(generateTestPlate());
        vehicle.setBrand("Toyota");
        vehicle.setModel("Yaris");
        vehicle.setYear(2020);
        vehicle.setNotes("Created by test");

        return vehicleDAO.create(vehicle);
    }
}