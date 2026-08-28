package com.ayesha.resolvehub.repository;

import com.ayesha.resolvehub.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
