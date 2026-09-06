/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.model.Mechanic;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
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
public class MechanicDAOTest {
    
    private Connection connection;
    private UserDAO userDAO;
    private MechanicDAO mechanicDAO;

    @BeforeEach
    void setUp() throws Exception {
        connection = DBConnection.getConnection();
        connection.setAutoCommit(false);

        userDAO = new UserDAO(connection);
        mechanicDAO = new MechanicDAO(connection);
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
    void shouldCreateMechanic() throws Exception {
        User user = createTestUser();

        Mechanic mechanic = new Mechanic();
        mechanic.setUserId(user.getId());
        mechanic.setSpecialty("Motor");

        Mechanic created = mechanicDAO.create(mechanic);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(user.getId(), created.getUserId());
        assertEquals("Motor", created.getSpecialty());
        assertTrue(created.getActive());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }

    @Test
    void shouldFindMechanicById() throws Exception {
        Mechanic created = createTestMechanic();

        Optional<Mechanic> result = mechanicDAO.findById(created.getId());

        assertTrue(result.isPresent());

        Mechanic found = result.get();

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getUserId(), found.getUserId());
        assertEquals(created.getSpecialty(), found.getSpecialty());
        assertTrue(found.getActive());
    }

    @Test
    void shouldFindMechanicByUserId() throws Exception {
        Mechanic created = createTestMechanic();

        Optional<Mechanic> result =
                mechanicDAO.findByUserId(created.getUserId());

        assertTrue(result.isPresent());

        Mechanic found = result.get();

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getUserId(), found.getUserId());
        assertEquals(created.getSpecialty(), found.getSpecialty());
    }

    @Test
    void shouldFindAllActiveMechanics() throws Exception {
        Mechanic activeMechanic = createTestMechanic();
        Mechanic deletedMechanic = createTestMechanic();

        mechanicDAO.softDelete(deletedMechanic.getId());

        List<Mechanic> mechanics = mechanicDAO.findAll();

        assertTrue(
                mechanics.stream()
                        .anyMatch(m -> m.getId().equals(activeMechanic.getId()))
        );

        assertFalse(
                mechanics.stream()
                        .anyMatch(m -> m.getId().equals(deletedMechanic.getId()))
        );
    }

    @Test
    void shouldUpdateMechanic() throws Exception {
        Mechanic mechanic = createTestMechanic();

        mechanic.setSpecialty("Electricidad");

        Mechanic updated = mechanicDAO.update(mechanic);

        assertNotNull(updated);
        assertEquals(mechanic.getId(), updated.getId());
        assertEquals(mechanic.getUserId(), updated.getUserId());
        assertEquals("Electricidad", updated.getSpecialty());
        assertTrue(updated.getActive());
    }

    @Test
    void shouldSoftDeleteMechanic() throws Exception {
        Mechanic mechanic = createTestMechanic();

        mechanicDAO.softDelete(mechanic.getId());

        Optional<Mechanic> result =
                mechanicDAO.findById(mechanic.getId());

        assertTrue(result.isPresent());
        assertFalse(result.get().getActive());

        List<Mechanic> mechanics = mechanicDAO.findAll();

        assertFalse(
                mechanics.stream()
                        .anyMatch(m -> m.getId().equals(mechanic.getId()))
        );
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
        user.setPaternalSurname("Mechanic");
        user.setMaternalSurname("User");

        return userDAO.create(user);
    }

    private String generateTestUsername() {
        return "test_mechanic_"
                + ThreadLocalRandom.current()
                        .nextLong(1_000_000, 10_000_000);
    }
}
