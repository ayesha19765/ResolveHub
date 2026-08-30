package com.ayesha.resolvehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to transition ticket status")
public class UpdateTicketStatusRequest {

    @Schema(description = "New target status: OPEN, IN_PROGRESS, RESOLVED, CLOSED", example = "IN_PROGRESS")
    @NotBlank(message = "Status is required")
    private String status;

    public UpdateTicketStatusRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
