package com.ayesha.resolvehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to create a new ticket")
public class CreateTicketRequest {

    @Schema(description = "Short summary of the issue", example = "Payment gateway timeout on checkout")
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Schema(description = "Detailed description of the issue or task", example = "Investigate 504 gateway timeouts when processing payments during high load.")
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Schema(description = "Priority level: LOW, MEDIUM, HIGH, CRITICAL", example = "HIGH")
    @NotBlank(message = "Priority is required")
    private String priority;

    @Schema(description = "ID of the associated project", example = "1")
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @Schema(description = "User ID of the reporter creating the ticket", example = "1")
    @NotNull(message = "Reporter ID is required")
    private Long reporterId;

    public CreateTicketRequest() {}

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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }
}
