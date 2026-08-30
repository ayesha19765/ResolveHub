package com.ayesha.resolvehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload to assign a ticket to a user")
public class AssignTicketRequest {

    @Schema(description = "User ID of the assignee", example = "2")
    @NotNull(message = "Assignee ID is required")
    private Long assigneeId;

    public AssignTicketRequest() {
    }

    public AssignTicketRequest(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
