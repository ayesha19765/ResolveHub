package com.ayesha.resolvehub.dto;

import java.time.LocalDateTime;

public class TicketActivityResponse {

    private Long id;
    private String action;
    private String description;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;

    public TicketActivityResponse() {
    }

    public TicketActivityResponse(
        Long id,
        String action,
        String description,
        String oldValue,
        String newValue,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.action = action;
        this.description = description;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
