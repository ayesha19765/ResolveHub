package com.ayesha.resolvehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Representation of an audit activity event on a ticket")
public class TicketActivityResponse {

    @Schema(description = "Unique activity log ID", example = "1")
    private Long id;

    @Schema(description = "Action category: CREATED, STATUS_CHANGED, ASSIGNED, PRIORITY_CHANGED", example = "STATUS_CHANGED")
    private String action;

    @Schema(description = "Human-readable description of what occurred", example = "Status changed from OPEN to IN_PROGRESS")
    private String description;

    @Schema(description = "Previous field value prior to mutation", example = "OPEN")
    private String oldValue;

    @Schema(description = "New field value after mutation", example = "IN_PROGRESS")
    private String newValue;

    @Schema(description = "Timestamp when the event occurred", example = "2026-08-30T11:10:00")
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
