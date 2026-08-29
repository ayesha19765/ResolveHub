package com.ayesha.resolvehub.repository.specification;

import com.ayesha.resolvehub.entity.Ticket;
import org.springframework.data.jpa.domain.Specification;

public class TicketSpecification {

    public static Specification<Ticket> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null
                        ? null
                        : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Ticket> hasPriority(String priority) {
        return (root, query, criteriaBuilder) ->
                priority == null
                        ? null
                        : criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> hasProjectId(Long projectId) {
        return (root, query, criteriaBuilder) ->
                projectId == null
                        ? null
                        : criteriaBuilder.equal(
                                root.get("project").get("id"),
                                projectId
                        );
    }

    public static Specification<Ticket> titleContains(String search) {
        return (root, query, criteriaBuilder) ->
                search == null
                        ? null
                        : criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + search.toLowerCase() + "%"
                        );
    }
}
