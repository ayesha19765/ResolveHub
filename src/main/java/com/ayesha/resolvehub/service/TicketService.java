package com.ayesha.resolvehub.service;

import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.dto.TicketResponse;
import com.ayesha.resolvehub.dto.UpdateTicketRequest;
import com.ayesha.resolvehub.entity.Project;
import com.ayesha.resolvehub.entity.Ticket;
import com.ayesha.resolvehub.entity.User;
import com.ayesha.resolvehub.exception.ProjectNotFoundException;
import com.ayesha.resolvehub.exception.TicketNotFoundException;
import com.ayesha.resolvehub.exception.UserNotFoundException;
import com.ayesha.resolvehub.repository.ProjectRepository;
import com.ayesha.resolvehub.repository.TicketRepository;
import com.ayesha.resolvehub.repository.UserRepository;
import com.ayesha.resolvehub.repository.projection.TicketSummary;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final EntityManager entityManager;

    public TicketService(
        TicketRepository ticketRepository,
        EntityManager entityManager,
        ProjectRepository projectRepository,
        UserRepository userRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.entityManager = entityManager;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Used when the controller needs a response DTO
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = getTicketEntityById(id);
        return toResponse(ticket);
    }

    // Used internally when the service needs the actual entity
    private Ticket getTicketEntityById(Long id) {
        return ticketRepository
            .findById(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
    }

    public TicketResponse createTicket(CreateTicketRequest request) {
        Project project = projectRepository
            .findById(request.getProjectId())
            .orElseThrow(() ->
                new ProjectNotFoundException(request.getProjectId())
            );

        User reporter = userRepository
            .findById(request.getReporterId())
            .orElseThrow(() ->
                new UserNotFoundException(request.getReporterId())
            );

        Ticket ticket = new Ticket();

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setStatus("OPEN");

        ticket.setProject(project);
        ticket.setReporter(reporter);

        Ticket savedTicket = ticketRepository.save(ticket);

        return toResponse(savedTicket);
    }

    public Ticket updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = getTicketEntityById(id);

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateTicketStatus(Long id, String status) {
        Ticket ticket = getTicketEntityById(id);

        ticket.setStatus(status);

        return ticket;
    }

    public void deleteTicket(Long id) {
        Ticket ticket = getTicketEntityById(id);
        ticketRepository.delete(ticket);
    }

    @Transactional
    public Ticket findTicketUsingEntityManager(Long id) {
        return entityManager.find(Ticket.class, id);
    }

    public List<Ticket> getTicketsByStatus(String status) {
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> getTicketsForAssignee(
        String status,
        String priority,
        Long assigneeId
    ) {
        return ticketRepository.findTicketsForAssignee(
            status,
            priority,
            assigneeId
        );
    }

    public Ticket getTicketWithDetails(Long id) {
        return ticketRepository
            .findTicketWithProjectAndReporter(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
    }

    public List<TicketSummary> getTicketSummaries() {
        return ticketRepository.findTicketSummaries();
    }

    public Page<Ticket> getTickets(int page, int size) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("createdAt").descending()
        );

        return ticketRepository.findAll(pageable);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getStatus(),
            ticket.getPriority(),
            ticket.getProject() != null ? ticket.getProject().getId() : null,
            ticket.getProject() != null ? ticket.getProject().getName() : null,
            ticket.getReporter() != null ? ticket.getReporter().getId() : null,
            ticket.getReporter() != null
                ? ticket.getReporter().getName()
                : null,
            ticket.getAssignee() != null ? ticket.getAssignee().getId() : null,
            ticket.getAssignee() != null
                ? ticket.getAssignee().getName()
                : null,
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }
}
