/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.model;

import java.time.LocalDateTime;

/**
 *
 * @author sebap
 */
public class WorkOrder implements Comparable<WorkOrder> {

    private Long id;
    private Long vehicleId;
    private Long mechanicId;
    private OrderStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    public WorkOrder() {
    }

    public WorkOrder(Long id, Long vehicleId, Long mechanicId, OrderStatus status, String description, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime closedAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.mechanicId = mechanicId;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(Long mechanicId) {
        this.mechanicId = mechanicId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    private static int comparisonCount = 0;

    public static void resetComparisonCount() {
        comparisonCount = 0;
    }

    public static int getComparisonCount() {
        return comparisonCount;
    }

    @Override
    public int compareTo(WorkOrder other) {
        comparisonCount++; // contador para fines de demostración académica

        if (this.createdAt == null && other.createdAt == null) {
            return 0;
        }
        if (this.createdAt == null) {
            return -1;
        }
        if (other.createdAt == null) {
            return 1;
        }
        int cmp = this.createdAt.compareTo(other.createdAt);
        if (cmp != 0) {
            return cmp;
        }
        return Long.compare(this.id, other.id);
    }
}
