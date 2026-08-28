package com.ayesha.resolvehub.service;

import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.dto.UpdateTicketRequest;
import com.ayesha.resolvehub.entity.Ticket;
import com.ayesha.resolvehub.exception.TicketNotFoundException;
import com.ayesha.resolvehub.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    private final EntityManager entityManager;

    public TicketService(
        TicketRepository ticketRepository,
        EntityManager entityManager
    ) {
        this.ticketRepository = ticketRepository;
        this.entityManager = entityManager;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository
            .findById(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
    }

    public Ticket createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket(
            null,
            request.getTitle(),
            request.getDescription(),
            "OPEN",
            request.getPriority()
        );

        return ticketRepository.save(ticket);
    }

    public Ticket updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = getTicketById(id);

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateTicketStatus(Long id, String status) {
        Ticket ticket = getTicketById(id);

        ticket.setStatus(status);

        return ticket;
    }

    public void deleteTicket(Long id) {
        Ticket ticket = getTicketById(id);

        ticketRepository.delete(ticket);
    }

    @Transactional
    public Ticket findTicketUsingEntityManager(Long id) {
        return entityManager.find(Ticket.class, id);
    }
}
