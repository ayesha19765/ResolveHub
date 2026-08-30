package com.ayesha.resolvehub.repository.specification;

import com.ayesha.resolvehub.entity.Ticket;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public class TicketSpecification {

    public static Specification<Ticket> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null || status.isBlank()
                        ? null
                        : criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("status")),
                                status.toUpperCase().trim()
                        );
    }

    public static Specification<Ticket> hasPriority(String priority) {
        return (root, query, criteriaBuilder) ->
                priority == null || priority.isBlank()
                        ? null
                        : criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("priority")),
                                priority.toUpperCase().trim()
                        );
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

    public static Specification<Ticket> hasAssigneeId(Long assigneeId) {
        return (root, query, criteriaBuilder) ->
                assigneeId == null
                        ? null
                        : criteriaBuilder.equal(
                                root.get("assignee").get("id"),
                                assigneeId
                        );
    }

    public static Specification<Ticket> hasReporterId(Long reporterId) {
        return (root, query, criteriaBuilder) ->
                reporterId == null
                        ? null
                        : criteriaBuilder.equal(
                                root.get("reporter").get("id"),
                                reporterId
                        );
    }

    public static Specification<Ticket> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase().trim() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")),
                            pattern
                    )
            );
        };
    }

    public static Specification<Ticket> titleContains(String search) {
        return (root, query, criteriaBuilder) ->
                search == null || search.isBlank()
                        ? null
                        : criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + search.toLowerCase().trim() + "%"
                        );
    }

    public static Specification<Ticket> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) ->
                createdAfter == null
                        ? null
                        : criteriaBuilder.greaterThanOrEqualTo(
                                root.get("createdAt"),
                                createdAfter
                        );
    }

    public static Specification<Ticket> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) ->
                createdBefore == null
                        ? null
                        : criteriaBuilder.lessThanOrEqualTo(
                                root.get("createdAt"),
                                createdBefore
                        );
    }
}
