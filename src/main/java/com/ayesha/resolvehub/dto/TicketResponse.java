package com.ayesha.resolvehub.dto;

public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;

    private Long projectId;
    private String projectName;

    private Long reporterId;
    private String reporterName;

    public TicketResponse(
            Long id,
            String title,
            String description,
            String status,
            String priority,
            Long projectId,
            String projectName,
            Long reporterId,
            String reporterName
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
}
