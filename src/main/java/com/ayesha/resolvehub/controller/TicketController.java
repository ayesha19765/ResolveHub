package com.ayesha.resolvehub.controller;

import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.dto.UpdateTicketRequest;
import com.ayesha.resolvehub.dto.UpdateTicketStatusRequest;
import com.ayesha.resolvehub.entity.Ticket;
import com.ayesha.resolvehub.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // @GetMapping
    // public List<Ticket> getAllTickets() {
    //     return ticketService.getAllTickets();
    // }

    @GetMapping("/{id}")
    public Ticket getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @PostMapping
    public Ticket createTicket(
        @Valid @RequestBody CreateTicketRequest request
    ) {
        return ticketService.createTicket(request);
    }

    @PutMapping("/{id}")
    public Ticket updateTicket(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ticketService.updateTicket(id, request);
    }

    @PatchMapping("/{id}/status")
    public Ticket updateTicketStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTicketStatusRequest request
    ) {
        return ticketService.updateTicketStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    public void deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
    }

    @GetMapping("/entity-manager/{id}")
    public Ticket getTicketUsingEntityManager(@PathVariable Long id) {
        return ticketService.findTicketUsingEntityManager(id);
    }

    @GetMapping("/status/{status}")
    public List<Ticket> getTicketsByStatus(@PathVariable String status) {
        return ticketService.getTicketsByStatus(status);
    }

    @GetMapping
    public List<Ticket> getTickets(
        @RequestParam(required = false) String status
    ) {
        if (status != null) {
            return ticketService.getTicketsByStatus(status);
        }

        return ticketService.getAllTickets();
    }

    @GetMapping("/assigned")
    public List<Ticket> getTicketsForAssignee(
        @RequestParam String status,
        @RequestParam String priority,
        @RequestParam Long assigneeId
    ) {
        return ticketService.getTicketsForAssignee(
            status,
            priority,
            assigneeId
        );
    }

    @GetMapping("/with-projects")
    public List<Ticket> getTicketsWithProjects() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}/details")
    public Ticket getTicketWithDetails(@PathVariable Long id) {
        return ticketService.getTicketWithDetails(id);
    }
}
