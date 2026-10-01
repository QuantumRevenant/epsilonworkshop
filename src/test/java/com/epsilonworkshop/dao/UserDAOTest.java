/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.config.DBConnection;
import com.epsilonworkshop.model.Role;
import com.epsilonworkshop.model.User;
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
public class UserDAOTest {

    private Connection connection;
    private UserDAO userDAO;

    private String generateTestUsername() {
        return "test_user_" + ThreadLocalRandom.current().nextLong(1_000_000, 10_000_000);
    }

    @BeforeEach
    void setUp() throws Exception {
        connection = DBConnection.getConnection();
        connection.setAutoCommit(false);

        userDAO = new UserDAO(connection);
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
    void shouldCreateUser() throws Exception {

        User user = createTestUser();

        assertNotNull(user.getId());
        assertNotNull(user.getUsername());
        assertEquals("test-hash", user.getPasswordHash());
        assertEquals(Role.MECHANIC, user.getRole());
        assertEquals("Test", user.getName());
        assertEquals("User", user.getPaternalSurname());
        assertEquals("Example", user.getMaternalSurname());
        assertTrue(user.getActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldFindUserById() throws Exception {

        User created = createTestUser();

        Optional<User> result = userDAO.findById(created.getId());

        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals(created.getUsername(), result.get().getUsername());
    }

    @Test
    void shouldFindUserByUsername() throws Exception {

        User created = createTestUser();

        Optional<User> result
                = userDAO.findByUsername(created.getUsername());

        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals(created.getUsername(), result.get().getUsername());
    }

    @Test
    void shouldFindAllActiveUsers() throws Exception {

        User activeUser = createTestUser();
        User deletedUser = createTestUser();

        userDAO.softDelete(deletedUser.getId());

        List<User> users = userDAO.findAll();

        assertTrue(
                users.stream()
                        .anyMatch(user -> user.getId().equals(activeUser.getId()))
        );

        assertFalse(
                users.stream()
                        .anyMatch(user -> user.getId().equals(deletedUser.getId()))
        );
    }

    @Test
    void shouldNotFindInactiveUserByUsername() throws Exception {

        User user = createTestUser();

        userDAO.softDelete(user.getId());

        Optional<User> result
                = userDAO.findByUsername(user.getUsername());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUpdateUser() throws Exception {

        User user = createTestUser();

        user.setUsername(generateTestUsername());
        user.setPasswordHash("updated-test-hash");
        user.setRole(Role.ADMIN);
        user.setName("Updated");
        user.setPaternalSurname("Admin");
        user.setMaternalSurname("User");

        User updated = userDAO.update(user);

        assertEquals(user.getId(), updated.getId());
        assertEquals(user.getUsername(), updated.getUsername());
        assertEquals("updated-test-hash", updated.getPasswordHash());
        assertEquals(Role.ADMIN, updated.getRole());
        assertEquals("Updated", updated.getName());
        assertEquals("Admin", updated.getPaternalSurname());
        assertEquals("User", updated.getMaternalSurname());
        assertTrue(updated.getActive());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void shouldSoftDeleteUser() throws Exception {

        User user = createTestUser();

        userDAO.softDelete(user.getId());

        Optional<User> result
                = userDAO.findById(user.getId());

        assertTrue(result.isPresent());
        assertFalse(result.get().getActive());
    }

    @Test
    void shouldReturnTrueWhenUsernameExistsAndIsActive() throws Exception {

        User user = createTestUser();

        boolean exists = userDAO.existsByUsername(user.getUsername());

        assertTrue(exists);
    }

    @Test
    void shouldReturnTrueWhenUsernameExistsButIsInactive() throws Exception {

        User user = createTestUser();
        userDAO.softDelete(user.getId());

        boolean exists = userDAO.existsByUsername(user.getUsername());

        // A diferencia de findByUsername, existsByUsername NO filtra por active --
        // debe reflejar lo mismo que el UNIQUE constraint de la BD, que tampoco filtra.
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() throws Exception {

        boolean exists = userDAO.existsByUsername(generateTestUsername());

        assertFalse(exists);
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
}
