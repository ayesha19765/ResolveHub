package com.ayesha.resolvehub.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateTicketStatusRequest {

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
