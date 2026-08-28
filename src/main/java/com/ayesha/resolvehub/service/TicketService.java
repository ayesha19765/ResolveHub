package com.ayesha.resolvehub.service;

import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.dto.UpdateTicketRequest;
import com.ayesha.resolvehub.entity.Ticket;
import com.ayesha.resolvehub.exception.TicketNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private final List<Ticket> tickets = new ArrayList<>();

    public TicketService() {
        tickets.add(
            new Ticket(
                1L,
                "Login issue",
                "User cannot log into the application",
                "OPEN",
                "HIGH"
            )
        );

        tickets.add(
            new Ticket(
                2L,
                "Profile update bug",
                "Profile changes are not being saved",
                "IN_PROGRESS",
                "MEDIUM"
            )
        );
    }

    public List<Ticket> getAllTickets() {
        return tickets;
    }

    public Ticket getTicketById(Long id) {
        return tickets
            .stream()
            .filter(ticket -> ticket.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new TicketNotFoundException(id));
    }

    public Ticket createTicket(CreateTicketRequest request) {
        Long newId =
            tickets.stream().mapToLong(Ticket::getId).max().orElse(0) + 1;

        Ticket ticket = new Ticket(
            newId,
            request.getTitle(),
            request.getDescription(),
            "OPEN",
            request.getPriority()
        );

        tickets.add(ticket);

        return ticket;
    }

    public Ticket updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = getTicketById(id);

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        return ticket;
    }

    public Ticket updateTicketStatus(Long id, String status) {
        Ticket ticket = getTicketById(id);

        ticket.setStatus(status);

        return ticket;
    }

    public void deleteTicket(Long id) {
        tickets.removeIf(ticket -> ticket.getId().equals(id));
    }
}
