package com.ayesha.resolvehub.service;

import com.ayesha.resolvehub.dto.CommentResponse;
import com.ayesha.resolvehub.dto.CreateCommentRequest;
import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.dto.TicketActivityResponse;
import com.ayesha.resolvehub.dto.TicketResponse;
import com.ayesha.resolvehub.dto.UpdateTicketRequest;
import com.ayesha.resolvehub.entity.Project;
import com.ayesha.resolvehub.entity.Ticket;
import com.ayesha.resolvehub.entity.TicketActivity;
import com.ayesha.resolvehub.entity.TicketComment;
import com.ayesha.resolvehub.entity.User;
import com.ayesha.resolvehub.exception.InvalidTicketStatusTransitionException;
import com.ayesha.resolvehub.exception.ProjectNotFoundException;
import com.ayesha.resolvehub.exception.TicketNotFoundException;
import com.ayesha.resolvehub.exception.UserNotFoundException;
import com.ayesha.resolvehub.repository.ProjectRepository;
import com.ayesha.resolvehub.repository.TicketActivityRepository;
import com.ayesha.resolvehub.repository.TicketCommentRepository;
import com.ayesha.resolvehub.repository.TicketRepository;
import com.ayesha.resolvehub.repository.UserRepository;
import com.ayesha.resolvehub.repository.projection.TicketSummary;
import com.ayesha.resolvehub.repository.specification.TicketSpecification;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class TicketService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "createdAt",
        "updatedAt",
        "priority",
        "status",
        "title",
        "id"
    );

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketActivityRepository ticketActivityRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final EntityManager entityManager;

    public TicketService(
        TicketRepository ticketRepository,
        TicketActivityRepository ticketActivityRepository,
        TicketCommentRepository ticketCommentRepository,
        EntityManager entityManager,
        ProjectRepository projectRepository,
        UserRepository userRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketActivityRepository = ticketActivityRepository;
        this.ticketCommentRepository = ticketCommentRepository;
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

    @Transactional
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

        recordActivity(savedTicket, "CREATED", "Ticket created", null, null);

        return toResponse(savedTicket);
    }

    @Transactional
    public Ticket updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = getTicketEntityById(id);

        String oldPriority = ticket.getPriority();
        String newPriority = request.getPriority();

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(newPriority);

        if (newPriority != null && !newPriority.equalsIgnoreCase(oldPriority)) {
            recordActivity(
                ticket,
                "PRIORITY_CHANGED",
                "Ticket priority changed",
                oldPriority,
                newPriority
            );
        }

        return ticketRepository.save(ticket);
    }

    @Transactional
    public TicketResponse assignTicket(Long ticketId, Long assigneeId) {
        Ticket ticket = getTicketEntityById(ticketId);
        User assignee = userRepository
            .findById(assigneeId)
            .orElseThrow(() -> new UserNotFoundException(assigneeId));

        User oldAssignee = ticket.getAssignee();
        Long oldAssigneeId = oldAssignee != null ? oldAssignee.getId() : null;

        if (oldAssigneeId == null || !oldAssigneeId.equals(assignee.getId())) {
            String oldVal = oldAssignee != null ? oldAssignee.getName() : null;
            String newVal = assignee.getName();

            ticket.setAssignee(assignee);
            recordActivity(ticket, "ASSIGNED", "Ticket assigned", oldVal, newVal);
        }

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long id, String status) {
        Ticket ticket = getTicketEntityById(id);
        String targetStatus = status.toUpperCase().trim();
        validateStatusTransition(ticket.getStatus(), targetStatus);

        String oldStatus = ticket.getStatus();
        if (!targetStatus.equalsIgnoreCase(oldStatus)) {
            ticket.setStatus(targetStatus);
            recordActivity(
                ticket,
                "STATUS_CHANGED",
                "Ticket status changed",
                oldStatus,
                targetStatus
            );
        }

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse assignTicketAndStart(Long ticketId, Long assigneeId) {
        Ticket ticket = getTicketEntityById(ticketId);
        User assignee = userRepository
            .findById(assigneeId)
            .orElseThrow(() -> new UserNotFoundException(assigneeId));

        validateStatusTransition(ticket.getStatus(), "IN_PROGRESS");

        User oldAssignee = ticket.getAssignee();
        Long oldAssigneeId = oldAssignee != null ? oldAssignee.getId() : null;
        if (oldAssigneeId == null || !oldAssigneeId.equals(assignee.getId())) {
            String oldVal = oldAssignee != null ? oldAssignee.getName() : null;
            ticket.setAssignee(assignee);
            recordActivity(ticket, "ASSIGNED", "Ticket assigned", oldVal, assignee.getName());
        }

        String oldStatus = ticket.getStatus();
        if (!"IN_PROGRESS".equalsIgnoreCase(oldStatus)) {
            ticket.setStatus("IN_PROGRESS");
            recordActivity(
                ticket,
                "STATUS_CHANGED",
                "Ticket status changed",
                oldStatus,
                "IN_PROGRESS"
            );
        }

        return toResponse(ticket);
    }

    private void recordActivity(
        Ticket ticket,
        String action,
        String description,
        String oldValue,
        String newValue
    ) {
        TicketActivity activity = new TicketActivity(
            ticket,
            action,
            description,
            oldValue,
            newValue
        );
        ticketActivityRepository.save(activity);
    }

    public List<TicketActivityResponse> getTicketActivities(Long ticketId) {
        getTicketEntityById(ticketId);

        return ticketActivityRepository
            .findByTicketIdOrderByCreatedAtDesc(ticketId)
            .stream()
            .map(this::toActivityResponse)
            .toList();
    }

    private TicketActivityResponse toActivityResponse(TicketActivity activity) {
        return new TicketActivityResponse(
            activity.getId(),
            activity.getAction(),
            activity.getDescription(),
            activity.getOldValue(),
            activity.getNewValue(),
            activity.getCreatedAt()
        );
    }

    private void validateStatusTransition(String currentStatus, String targetStatus) {
        if (targetStatus == null || targetStatus.isBlank()) {
            throw new InvalidTicketStatusTransitionException("Target status cannot be null or empty");
        }

        String current = currentStatus != null ? currentStatus.toUpperCase().trim() : "";
        String target = targetStatus.toUpperCase().trim();

        if (current.equals(target)) {
            return;
        }

        boolean isValid = switch (current) {
            case "OPEN" -> target.equals("IN_PROGRESS") || target.equals("CLOSED");
            case "IN_PROGRESS" -> target.equals("RESOLVED") || target.equals("OPEN");
            case "RESOLVED" -> target.equals("CLOSED") || target.equals("IN_PROGRESS");
            case "CLOSED" -> false;
            default -> false;
        };

        if (!isValid) {
            throw new InvalidTicketStatusTransitionException(current, target);
        }
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

    public Page<TicketResponse> searchTickets(
        String status,
        String priority,
        Long projectId,
        Long assigneeId,
        Long reporterId,
        String search,
        LocalDateTime createdAfter,
        LocalDateTime createdBefore,
        int page,
        int size,
        String sort,
        String direction
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);

        String sortField = (sort != null && ALLOWED_SORT_FIELDS.contains(sort.trim()))
            ? sort.trim()
            : "createdAt";

        Sort.Direction sortDirection = (direction != null && direction.equalsIgnoreCase("asc"))
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
            safePage,
            safeSize,
            Sort.by(sortDirection, sortField)
        );

        Specification<Ticket> specification = Specification.allOf(
            TicketSpecification.hasStatus(status),
            TicketSpecification.hasPriority(priority),
            TicketSpecification.hasProjectId(projectId),
            TicketSpecification.hasAssigneeId(assigneeId),
            TicketSpecification.hasReporterId(reporterId),
            TicketSpecification.search(search),
            TicketSpecification.createdAfter(createdAfter),
            TicketSpecification.createdBefore(createdBefore)
        );

        return ticketRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional
    public CommentResponse createComment(Long ticketId, CreateCommentRequest request) {
        Ticket ticket = getTicketEntityById(ticketId);

        User author = userRepository
            .findById(request.getAuthorId())
            .orElseThrow(() ->
                new UserNotFoundException(request.getAuthorId())
            );

        TicketComment comment = new TicketComment(
            ticket,
            author,
            request.getContent()
        );

        TicketComment savedComment = ticketCommentRepository.save(comment);

        return toCommentResponse(savedComment);
    }

    public Page<CommentResponse> getComments(Long ticketId, int page, int size) {
        getTicketEntityById(ticketId);

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("createdAt").descending()
        );

        return ticketCommentRepository
            .findByTicketId(ticketId, pageable)
            .map(this::toCommentResponse);
    }

    private CommentResponse toCommentResponse(TicketComment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getAuthor() != null ? comment.getAuthor().getId() : null,
            comment.getAuthor() != null ? comment.getAuthor().getName() : null,
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}
