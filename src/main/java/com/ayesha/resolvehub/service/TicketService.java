package com.ayesha.resolvehub.service;

import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.entity.Ticket;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

        return tickets.stream()
                .filter(ticket -> ticket.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    public Ticket createTicket(CreateTicketRequest request) {

    Long newId = tickets.stream()
            .mapToLong(Ticket::getId)
            .max()
            .orElse(0) + 1;

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
}
