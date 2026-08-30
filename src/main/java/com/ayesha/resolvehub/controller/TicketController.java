package com.ayesha.resolvehub.controller;

import com.ayesha.resolvehub.dto.ApiErrorResponse;
import com.ayesha.resolvehub.dto.AssignTicketRequest;
import com.ayesha.resolvehub.dto.CommentResponse;
import com.ayesha.resolvehub.dto.CreateCommentRequest;
import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.dto.TicketActivityResponse;
import com.ayesha.resolvehub.dto.TicketResponse;
import com.ayesha.resolvehub.dto.UpdateTicketRequest;
import com.ayesha.resolvehub.dto.UpdateTicketStatusRequest;
import com.ayesha.resolvehub.entity.Ticket;
import com.ayesha.resolvehub.repository.projection.TicketSummary;
import com.ayesha.resolvehub.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tickets", description = "Ticket management, dynamic search, assignments, activities, and discussion APIs")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(summary = "Get ticket by ID", description = "Retrieves complete details of a single ticket by its unique identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket found and returned successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public TicketResponse getTicketById(
        @Parameter(description = "Unique ID of the ticket", example = "1") @PathVariable Long id
    ) {
        return ticketService.getTicketById(id);
    }

    @Operation(summary = "Create a new ticket", description = "Creates a new ticket with OPEN status and logs an initial CREATED activity")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failure or malformed payload", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Project or Reporter not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public TicketResponse createTicket(
        @Valid @RequestBody CreateTicketRequest request
    ) {
        return ticketService.createTicket(request);
    }

    @Operation(summary = "Update ticket details", description = "Updates title, description, and priority of an existing ticket")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failure", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public Ticket updateTicket(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ticketService.updateTicket(id, request);
    }

    @Operation(summary = "Assign ticket to user", description = "Assigns the ticket to a new user and logs an ASSIGNED activity event")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket assigned successfully"),
        @ApiResponse(responseCode = "400", description = "Missing or invalid assignee ID", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ticket or Assignee not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{id}/assignee")
    public TicketResponse assignTicket(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody AssignTicketRequest request
    ) {
        return ticketService.assignTicket(id, request.getAssigneeId());
    }

    @Operation(summary = "Update ticket status", description = "Transitions ticket status according to business rules (OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public TicketResponse updateTicketStatus(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody UpdateTicketStatusRequest request
    ) {
        return ticketService.updateTicketStatus(id, request.getStatus());
    }

    @Operation(summary = "Assign ticket and start working", description = "Atomically assigns the ticket to a user and sets status to IN_PROGRESS")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket assigned and started successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or transition", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ticket or Assignee not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{id}/assign-and-start")
    public TicketResponse assignTicketAndStart(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody AssignTicketRequest request
    ) {
        return ticketService.assignTicketAndStart(id, request.getAssigneeId());
    }

    @Operation(summary = "Get ticket activity history", description = "Retrieves the audit activity log for a ticket ordered newest to oldest")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Activities retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}/activities")
    public List<TicketActivityResponse> getTicketActivities(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id
    ) {
        return ticketService.getTicketActivities(id);
    }

    @Operation(summary = "Add comment to ticket", description = "Adds a discussion comment to a ticket")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comment added successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error (blank content or missing author)", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ticket or Author not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{id}/comments")
    public CommentResponse createComment(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody CreateCommentRequest request
    ) {
        return ticketService.createComment(id, request);
    }

    @Operation(summary = "Get ticket comments", description = "Retrieves paginated discussion comments for a ticket ordered newest first")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}/comments")
    public Page<CommentResponse> getComments(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id,
        @Parameter(description = "Page index (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size (1-100)", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ticketService.getComments(id, page, size);
    }

    @Operation(summary = "Delete ticket", description = "Deletes a ticket and cascades removal to associated comments and activities")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public void deleteTicket(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id
    ) {
        ticketService.deleteTicket(id);
    }

    @Operation(summary = "Get ticket using EntityManager", description = "Loads a ticket directly via Hibernate EntityManager (internal demonstration)")
    @GetMapping("/entity-manager/{id}")
    public Ticket getTicketUsingEntityManager(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id
    ) {
        return ticketService.findTicketUsingEntityManager(id);
    }

    @Operation(summary = "Get tickets by status (derived query)", description = "Finds tickets with exact status using derived repository query")
    @GetMapping("/status/{status}")
    public List<Ticket> getTicketsByStatus(
        @Parameter(description = "Ticket status", example = "OPEN") @PathVariable String status
    ) {
        return ticketService.getTicketsByStatus(status);
    }

    @Operation(summary = "Get tickets for assignee (JPQL query)", description = "Finds tickets matching status, priority, and assignee")
    @GetMapping("/assigned")
    public List<Ticket> getTicketsForAssignee(
        @RequestParam String status,
        @RequestParam String priority,
        @RequestParam Long assigneeId
    ) {
        return ticketService.getTicketsForAssignee(status, priority, assigneeId);
    }

    @Operation(summary = "Get all tickets with projects (JOIN FETCH demo)", description = "Retrieves all tickets with projects eagerly fetched")
    @GetMapping("/with-projects")
    public List<Ticket> getTicketsWithProjects() {
        return ticketService.getAllTickets();
    }

    @Operation(summary = "Get ticket with full details", description = "Loads a ticket with eager project and reporter fetching")
    @GetMapping("/{id}/details")
    public Ticket getTicketWithDetails(
        @Parameter(description = "Ticket ID", example = "1") @PathVariable Long id
    ) {
        return ticketService.getTicketWithDetails(id);
    }

    @Operation(summary = "Get ticket summaries (Projection)", description = "Returns lightweight projection summaries selecting only id, title, status, priority, and projectName")
    @GetMapping("/summary")
    public List<TicketSummary> getTicketSummaries() {
        return ticketService.getTicketSummaries();
    }

    @Operation(summary = "Get tickets with pagination", description = "Simple paginated list of tickets")
    @GetMapping("/paged")
    public Page<Ticket> getTickets(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ticketService.getTickets(page, size);
    }

    @Operation(
        summary = "Search tickets with dynamic filters",
        description = "Flexible search endpoint combining optional filters (status, priority, project, assignee, reporter, text search, date range) with safe pagination and whitelist sorting"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matching tickets page returned successfully")
    })
    @GetMapping
    public Page<TicketResponse> searchTickets(
        @Parameter(description = "Filter by status (e.g. OPEN, IN_PROGRESS, RESOLVED, CLOSED)", example = "OPEN")
        @RequestParam(required = false) String status,

        @Parameter(description = "Filter by priority (e.g. LOW, MEDIUM, HIGH, CRITICAL)", example = "HIGH")
        @RequestParam(required = false) String priority,

        @Parameter(description = "Filter by project ID", example = "1")
        @RequestParam(required = false) Long projectId,

        @Parameter(description = "Filter by assignee user ID", example = "2")
        @RequestParam(required = false) Long assigneeId,

        @Parameter(description = "Filter by reporter user ID", example = "1")
        @RequestParam(required = false) Long reporterId,

        @Parameter(description = "Keyword search matching title OR description (case-insensitive)", example = "payment")
        @RequestParam(required = false) String search,

        @Parameter(description = "Filter tickets created on or after ISO timestamp", example = "2026-08-01T00:00:00")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,

        @Parameter(description = "Filter tickets created on or before ISO timestamp", example = "2026-08-31T23:59:59")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,

        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Page size (1-100)", example = "10")
        @RequestParam(defaultValue = "10") int size,

        @Parameter(description = "Sort field (createdAt, updatedAt, priority, status, title, id)", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") String sort,

        @Parameter(description = "Sort direction (asc, desc)", example = "desc")
        @RequestParam(defaultValue = "desc") String direction
    ) {
        return ticketService.searchTickets(
            status,
            priority,
            projectId,
            assigneeId,
            reporterId,
            search,
            createdAfter,
            createdBefore,
            page,
            size,
            sort,
            direction
        );
    }
}
