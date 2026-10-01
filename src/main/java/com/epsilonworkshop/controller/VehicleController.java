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

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author sebap
 */
public class VehicleController {

    private final VehicleDAO vehicleDAO;
    private final ClientDAO clientDAO;

    public VehicleController() {
        this.vehicleDAO = new VehicleDAO();
        this.clientDAO = new ClientDAO();
    }

    public VehicleController(VehicleDAO vehicleDAO, ClientDAO clientDAO) {
        this.vehicleDAO = vehicleDAO;
        this.clientDAO = clientDAO;
    }

    public Vehicle create(Vehicle vehicle, User actingUser)
            throws UnauthorizedActionException, DuplicatePlateException, SQLException {

        requireAdmin(actingUser);

        clientDAO.findById(vehicle.getClientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente con id=" + vehicle.getClientId()
                ));

        Optional<Vehicle> existingByPlate = vehicleDAO.findByPlate(vehicle.getPlate());

        if (existingByPlate.isPresent()) {
            throw new DuplicatePlateException(
                    "Ya existe un vehículo con placa " + vehicle.getPlate()
            );
        }

        return vehicleDAO.create(vehicle);
    }

    public Vehicle update(Vehicle vehicle, User actingUser)
            throws UnauthorizedActionException, DuplicatePlateException, SQLException {

        requireAdmin(actingUser);

        Vehicle existing = vehicleDAO.findById(vehicle.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un vehículo con id=" + vehicle.getId()
                ));

        Optional<Vehicle> existingByPlate = vehicleDAO.findByPlate(vehicle.getPlate());

        if (existingByPlate.isPresent()
                && !existingByPlate.get().getId().equals(vehicle.getId())) {
            throw new DuplicatePlateException(
                    "Ya existe otro vehículo con placa " + vehicle.getPlate()
            );
        }

        // RN-10: update() nunca cambia el dueño. Se ignora cualquier clientId
        // que venga en el objeto recibido y se fuerza el valor ya existente en BD --
        // el único camino válido para cambiar de dueño es transferOwnership().
        vehicle.setClientId(existing.getClientId());

        return vehicleDAO.update(vehicle);
    }

    public void softDelete(Long id, User actingUser)
            throws UnauthorizedActionException, SQLException {

        requireAdmin(actingUser);
        vehicleDAO.softDelete(id);
    }

    /**
     * RN-10: transferencia de dueño. El historial de work_orders del vehículo
     * no se toca -- eso ya lo garantiza el schema (work_orders.vehicle_id
     * nunca se reasigna).
     */
    public Vehicle transferOwnership(Long vehicleId, Long newClientId, User actingUser)
            throws UnauthorizedActionException, SQLException {

        requireAdmin(actingUser);

        Vehicle vehicle = vehicleDAO.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un vehículo con id=" + vehicleId
                ));

        clientDAO.findById(newClientId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cliente con id=" + newClientId
                ));

        vehicle.setClientId(newClientId);

        return vehicleDAO.update(vehicle);
    }

    public Optional<Vehicle> findById(Long id) throws SQLException {
        return vehicleDAO.findById(id);
    }

    public List<Vehicle> findAll() throws SQLException {
        return vehicleDAO.findAll();
    }

    private void requireAdmin(User actingUser) throws UnauthorizedActionException {
        if (actingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(
                    "Solo un administrador puede gestionar vehículos"
            );
        }
    }
}