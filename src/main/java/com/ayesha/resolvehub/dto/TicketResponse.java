package com.ayesha.resolvehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Detailed representation of a ticket")
public class TicketResponse {

    @Schema(description = "Unique ticket ID", example = "1")
    private Long id;

    @Schema(description = "Ticket title", example = "Payment gateway timeout on checkout")
    private String title;

    @Schema(description = "Ticket description", example = "Investigate 504 gateway timeouts when processing payments during high load.")
    private String description;

    @Schema(description = "Current ticket status: OPEN, IN_PROGRESS, RESOLVED, CLOSED", example = "OPEN")
    private String status;

    @Schema(description = "Priority level: LOW, MEDIUM, HIGH, CRITICAL", example = "HIGH")
    private String priority;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project name", example = "ResolveHub Core")
    private String projectName;

    @Schema(description = "Reporter user ID", example = "1")
    private Long reporterId;

    @Schema(description = "Reporter user name", example = "Ayesha")
    private String reporterName;

    @Schema(description = "Assignee user ID (null if unassigned)", example = "2")
    private Long assigneeId;

    @Schema(description = "Assignee user name (null if unassigned)", example = "Bob Smith")
    private String assigneeName;

    @Schema(description = "Timestamp when ticket was created", example = "2026-08-30T10:15:30")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when ticket was last updated", example = "2026-08-30T10:15:30")
    private LocalDateTime updatedAt;

    public TicketResponse(
            Long id,
            String title,
            String description,
            String status,
            String priority,
            Long projectId,
            String projectName,
            Long reporterId,
            String reporterName,
            Long assigneeId,
            String assigneeName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.projectId = projectId;
        this.projectName = projectName;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
