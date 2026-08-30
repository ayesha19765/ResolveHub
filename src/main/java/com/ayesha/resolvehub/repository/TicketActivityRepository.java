package com.ayesha.resolvehub.repository;

import com.ayesha.resolvehub.entity.TicketActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {

    List<TicketActivity> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
