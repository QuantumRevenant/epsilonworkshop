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
public class WorkOrderHistory {
    
    private Long id;
    private Long workOrderId;
    private Long userId;
    private WorkOrderChangeType changeType;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;

    public WorkOrderHistory() {
    }

    public WorkOrderHistory(Long id, Long workOrderId, Long userId, WorkOrderChangeType changeType, String oldValue, String newValue, LocalDateTime changedAt) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.userId = userId;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public WorkOrderChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(WorkOrderChangeType changeType) {
        this.changeType = changeType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
    
        
}
