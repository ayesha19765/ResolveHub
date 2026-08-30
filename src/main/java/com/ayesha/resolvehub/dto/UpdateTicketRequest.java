package com.ayesha.resolvehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to update ticket details")
public class UpdateTicketRequest {

    @Schema(description = "Updated title", example = "Updated issue title")
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Schema(description = "Updated description", example = "Updated description with debug logs")
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Schema(description = "Updated priority: LOW, MEDIUM, HIGH, CRITICAL", example = "MEDIUM")
    @NotBlank(message = "Priority is required")
    private String priority;

    public UpdateTicketRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
