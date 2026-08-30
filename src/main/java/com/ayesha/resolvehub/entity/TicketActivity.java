package com.ayesha.resolvehub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_activities", indexes = {
    @Index(name = "idx_ticket_activities_ticket_id", columnList = "ticket_id"),
    @Index(name = "idx_ticket_activities_created_at", columnList = "createdAt")
})
public class TicketActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false)
    private String action;

    private String description;

    private String oldValue;

    private String newValue;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TicketActivity() {
    }

    public TicketActivity(Ticket ticket, String action, String description, String oldValue, String newValue) {
        this.ticket = ticket;
        this.action = action;
        this.description = description;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
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
