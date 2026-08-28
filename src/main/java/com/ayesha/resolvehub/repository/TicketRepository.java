package com.ayesha.resolvehub.repository;

import com.ayesha.resolvehub.entity.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(String status);
}
