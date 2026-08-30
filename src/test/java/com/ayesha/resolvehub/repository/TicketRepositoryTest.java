package com.ayesha.resolvehub.repository;

import com.ayesha.resolvehub.entity.Project;
import com.ayesha.resolvehub.entity.Ticket;
import com.ayesha.resolvehub.entity.TicketActivity;
import com.ayesha.resolvehub.entity.TicketComment;
import com.ayesha.resolvehub.entity.User;
import com.ayesha.resolvehub.repository.projection.TicketSummary;
import com.ayesha.resolvehub.repository.specification.TicketSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketActivityRepository ticketActivityRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    private User user1;
    private User user2;
    private Project project1;
    private Ticket ticket1;
    private Ticket ticket2;

    @BeforeEach
    void setUp() {
        user1 = new User(null, "Ayesha", "ayesha@example.com", "DEVELOPER");
        user2 = new User(null, "Bob", "bob@example.com", "QA");
        entityManager.persist(user1);
        entityManager.persist(user2);

        project1 = new Project(null, "ResolveHub", "Issue Tracker", user1);
        entityManager.persist(project1);

        ticket1 = new Ticket();
        ticket1.setTitle("Fix Payment Timeout");
        ticket1.setDescription("Payment gateway returns 504 gateway timeout");
        ticket1.setStatus("OPEN");
        ticket1.setPriority("HIGH");
        ticket1.setProject(project1);
        ticket1.setReporter(user1);
        ticket1.setAssignee(user2);
        ticket1.setCreatedAt(LocalDateTime.now().minusDays(2));
        ticket1.setUpdatedAt(LocalDateTime.now().minusDays(2));
        entityManager.persist(ticket1);

        ticket2 = new Ticket();
        ticket2.setTitle("Update Documentation");
        ticket2.setDescription("Add deployment guide for developers");
        ticket2.setStatus("IN_PROGRESS");
        ticket2.setPriority("LOW");
        ticket2.setProject(project1);
        ticket2.setReporter(user2);
        ticket2.setAssignee(null);
        ticket2.setCreatedAt(LocalDateTime.now().minusDays(1));
        ticket2.setUpdatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persist(ticket2);

        entityManager.flush();
    }

    @Nested
    @DisplayName("Derived & JPQL Query Tests")
    class DerivedAndJpqlQueryTests {

        @Test
        @DisplayName("findByStatus should return matching tickets")
        void shouldFindByStatus() {
            List<Ticket> openTickets = ticketRepository.findByStatus("OPEN");
            assertEquals(1, openTickets.size());
            assertEquals("Fix Payment Timeout", openTickets.get(0).getTitle());
        }

        @Test
        @DisplayName("findTicketsForAssignee should return tickets matching status, priority, and assignee")
        void shouldFindTicketsForAssignee() {
            List<Ticket> results = ticketRepository.findTicketsForAssignee("OPEN", "HIGH", user2.getId());
            assertEquals(1, results.size());
            assertEquals(ticket1.getId(), results.get(0).getId());
        }

        @Test
        @DisplayName("findTicketSummaries projection should load projected fields")
        void shouldFindTicketSummaries() {
            List<TicketSummary> summaries = ticketRepository.findTicketSummaries();
            assertFalse(summaries.isEmpty());
            TicketSummary first = summaries.get(0);
            assertNotNull(first.getId());
            assertNotNull(first.getTitle());
            assertNotNull(first.getStatus());
            assertNotNull(first.getProjectName());
        }
    }

    @Nested
    @DisplayName("Specification Dynamic Filtering Tests")
    class SpecificationTests {

        @Test
        @DisplayName("Specification with multiple criteria should return exact matches")
        void shouldFilterByStatusPriorityAndProject() {
            Specification<Ticket> spec = Specification.allOf(
                TicketSpecification.hasStatus("OPEN"),
                TicketSpecification.hasPriority("HIGH"),
                TicketSpecification.hasProjectId(project1.getId())
            );

            List<Ticket> results = ticketRepository.findAll(spec);
            assertEquals(1, results.size());
            assertEquals("Fix Payment Timeout", results.get(0).getTitle());
        }

        @Test
        @DisplayName("Specification search should match title or description case-insensitively")
        void shouldSearchByKeywordInTitleOrDescription() {
            Specification<Ticket> titleSpec = TicketSpecification.search("payment");
            List<Ticket> titleResults = ticketRepository.findAll(titleSpec);
            assertEquals(1, titleResults.size());

            Specification<Ticket> descSpec = TicketSpecification.search("deployment");
            List<Ticket> descResults = ticketRepository.findAll(descSpec);
            assertEquals(1, descResults.size());
            assertEquals("Update Documentation", descResults.get(0).getTitle());
        }

        @Test
        @DisplayName("Specification date range filtering should filter by createdAt")
        void shouldFilterByDateRange() {
            Specification<Ticket> futureSpec = TicketSpecification.createdAfter(LocalDateTime.now().plusDays(1));
            List<Ticket> futureResults = ticketRepository.findAll(futureSpec);
            assertTrue(futureResults.isEmpty());

            Specification<Ticket> pastSpec = TicketSpecification.createdBefore(LocalDateTime.now().plusHours(1));
            List<Ticket> pastResults = ticketRepository.findAll(pastSpec);
            assertEquals(2, pastResults.size());
        }

        @Test
        @DisplayName("Specification with no matching rows should return empty list")
        void shouldReturnEmptyListWhenNoMatches() {
            Specification<Ticket> spec = Specification.allOf(
                TicketSpecification.hasStatus("CLOSED"),
                TicketSpecification.search("nonexistent")
            );

            List<Ticket> results = ticketRepository.findAll(spec);
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("Activity and Comment Repository Tests")
    class ActivityAndCommentRepositoryTests {

        @Test
        @DisplayName("TicketActivityRepository should retrieve activities ordered newest first")
        void shouldRetrieveActivitiesOrderedByCreatedAtDesc() {
            TicketActivity act1 = new TicketActivity(ticket1, "CREATED", "Created", null, null);
            act1.setCreatedAt(LocalDateTime.now().minusHours(2));
            entityManager.persist(act1);

            TicketActivity act2 = new TicketActivity(ticket1, "ASSIGNED", "Assigned", null, "Bob");
            act2.setCreatedAt(LocalDateTime.now().minusHours(1));
            entityManager.persist(act2);

            entityManager.flush();

            List<TicketActivity> activities = ticketActivityRepository.findByTicketIdOrderByCreatedAtDesc(ticket1.getId());
            assertEquals(2, activities.size());
            assertEquals("ASSIGNED", activities.get(0).getAction());
            assertEquals("CREATED", activities.get(1).getAction());
        }

        @Test
        @DisplayName("TicketCommentRepository should return paginated comments with eager author mapping")
        void shouldRetrievePaginatedComments() {
            TicketComment comment1 = new TicketComment(ticket1, user1, "First comment");
            comment1.setCreatedAt(LocalDateTime.now().minusMinutes(10));
            entityManager.persist(comment1);

            TicketComment comment2 = new TicketComment(ticket1, user2, "Second comment");
            comment2.setCreatedAt(LocalDateTime.now().minusMinutes(5));
            entityManager.persist(comment2);

            entityManager.flush();
            entityManager.clear(); // Clear persistence context to test query loading

            Page<TicketComment> page = ticketCommentRepository.findByTicketId(
                ticket1.getId(),
                PageRequest.of(0, 10, Sort.by("createdAt").descending())
            );

            assertEquals(2, page.getTotalElements());
            assertEquals("Second comment", page.getContent().get(0).getContent());
            assertNotNull(page.getContent().get(0).getAuthor());
            assertEquals("Bob", page.getContent().get(0).getAuthor().getName());
        }
    }
}
