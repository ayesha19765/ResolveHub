package com.ayesha.resolvehub.repository.projection;

public interface TicketSummary {

    Long getId();

    String getTitle();

    String getStatus();

    String getPriority();

    String getProjectName();
}
