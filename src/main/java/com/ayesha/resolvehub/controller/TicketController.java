package com.ayesha.resolvehub.controller;

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
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
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
    public TicketResponse getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @PostMapping
    public TicketResponse createTicket(
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

    @PatchMapping("/{id}/assignee")
    public TicketResponse assignTicket(
        @PathVariable Long id,
        @Valid @RequestBody AssignTicketRequest request
    ) {
        return ticketService.assignTicket(id, request.getAssigneeId());
    }

    @PatchMapping("/{id}/status")
    public TicketResponse updateTicketStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTicketStatusRequest request
    ) {
        return ticketService.updateTicketStatus(id, request.getStatus());
    }

    @PatchMapping("/{id}/assign-and-start")
    public TicketResponse assignTicketAndStart(
        @PathVariable Long id,
        @Valid @RequestBody AssignTicketRequest request
    ) {
        return ticketService.assignTicketAndStart(id, request.getAssigneeId());
    }

    @GetMapping("/{id}/activities")
    public List<TicketActivityResponse> getTicketActivities(@PathVariable Long id) {
        return ticketService.getTicketActivities(id);
    }

    @PostMapping("/{id}/comments")
    public CommentResponse createComment(
        @PathVariable Long id,
        @Valid @RequestBody CreateCommentRequest request
    ) {
        return ticketService.createComment(id, request);
    }

    @GetMapping("/{id}/comments")
    public Page<CommentResponse> getComments(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ticketService.getComments(id, page, size);
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

    // @GetMapping
    // public List<Ticket> getTickets(
    //     @RequestParam(required = false) String status
    // ) {
    //     if (status != null) {
    //         return ticketService.getTicketsByStatus(status);
    //     }
    //
    //     return ticketService.getAllTickets();
    // }

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

    @GetMapping("/summary")
    public List<TicketSummary> getTicketSummaries() {
        return ticketService.getTicketSummaries();
    }

    @GetMapping("/paged")
    public Page<Ticket> getTickets(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ticketService.getTickets(page, size);
    }

    @GetMapping
    public Page<Ticket> searchTickets(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority,
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ticketService.searchTickets(
            status,
            priority,
            projectId,
            search,
            page,
            size
        );
    }
}
