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

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

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
}
