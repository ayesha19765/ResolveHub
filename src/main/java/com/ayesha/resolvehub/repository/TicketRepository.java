package com.ayesha.resolvehub.repository;

import com.ayesha.resolvehub.entity.Ticket;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(String status);

    List<Ticket> findByPriority(String priority);

    List<Ticket> findByStatusAndPriority(String status, String priority);

    List<Ticket> findByProjectId(Long projectId);

    List<Ticket> findByReporterId(Long reporterId);

    @Query(
        """
        SELECT t
        FROM Ticket t
        WHERE t.status = :status
          AND t.priority = :priority
          AND t.assignee.id = :assigneeId
        """
    )
    List<Ticket> findTicketsForAssignee(
        @Param("status") String status,
        @Param("priority") String priority,
        @Param("assigneeId") Long assigneeId
    );

    @Query(
        """
        SELECT t
        FROM Ticket t
        JOIN FETCH t.project
        JOIN FETCH t.reporter
        WHERE t.id = :id
        """
    )
    
    Optional<Ticket> findTicketWithProjectAndReporter(@Param("id") Long id);
}
