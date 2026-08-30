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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketActivityRepository ticketActivityRepository;

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    private User reporter;
    private User assignee;
    private Project project;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        reporter = new User(1L, "Ayesha", "ayesha@example.com", "DEVELOPER");
        assignee = new User(2L, "Bob", "bob@example.com", "DEVELOPER");
        project = new Project(1L, "ResolveHub", "Issue Tracker", reporter);

        ticket = new Ticket();
        ticket.setId(100L);
        ticket.setTitle("Fix Bug");
        ticket.setDescription("Fix connection timeout");
        ticket.setPriority("HIGH");
        ticket.setStatus("OPEN");
        ticket.setProject(project);
        ticket.setReporter(reporter);
    }

    @Nested
    @DisplayName("Create Ticket Tests")
    class CreateTicketTests {

        @Test
        @DisplayName("Should create ticket successfully and record CREATED activity")
        void shouldCreateTicketSuccessfully() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("Fix Bug");
            request.setDescription("Fix connection timeout");
            request.setPriority("HIGH");
            request.setProjectId(1L);
            request.setReporterId(1L);

            given(projectRepository.findById(1L)).willReturn(Optional.of(project));
            given(userRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(ticketRepository.save(any(Ticket.class))).willAnswer(invocation -> {
                Ticket t = invocation.getArgument(0);
                t.setId(100L);
                return t;
            });

            TicketResponse response = ticketService.createTicket(request);

            assertNotNull(response);
            assertEquals("Fix Bug", response.getTitle());
            assertEquals("OPEN", response.getStatus());
            assertEquals(1L, response.getProjectId());
            assertEquals(1L, response.getReporterId());

            ArgumentCaptor<TicketActivity> activityCaptor = ArgumentCaptor.forClass(TicketActivity.class);
            verify(ticketActivityRepository).save(activityCaptor.capture());
            assertEquals("CREATED", activityCaptor.getValue().getAction());
            assertNull(activityCaptor.getValue().getOldValue());
            assertNull(activityCaptor.getValue().getNewValue());
        }

        @Test
        @DisplayName("Should throw ProjectNotFoundException when project does not exist")
        void shouldThrowProjectNotFoundExceptionWhenCreatingTicketWithNonexistentProject() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setProjectId(999L);
            request.setReporterId(1L);

            given(projectRepository.findById(999L)).willReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class, () -> ticketService.createTicket(request));
            verifyNoInteractions(userRepository, ticketRepository, ticketActivityRepository);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when reporter does not exist")
        void shouldThrowUserNotFoundExceptionWhenCreatingTicketWithNonexistentReporter() {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setProjectId(1L);
            request.setReporterId(999L);

            given(projectRepository.findById(1L)).willReturn(Optional.of(project));
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> ticketService.createTicket(request));
            verifyNoInteractions(ticketRepository, ticketActivityRepository);
        }
    }

    @Nested
    @DisplayName("Get Ticket Tests")
    class GetTicketTests {

        @Test
        @DisplayName("Should get ticket by ID successfully")
        void shouldGetTicketByIdSuccessfully() {
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));

            TicketResponse response = ticketService.getTicketById(100L);

            assertNotNull(response);
            assertEquals(100L, response.getId());
            assertEquals("Fix Bug", response.getTitle());
        }

        @Test
        @DisplayName("Should throw TicketNotFoundException when ticket does not exist")
        void shouldThrowTicketNotFoundExceptionWhenTicketDoesNotExist() {
            given(ticketRepository.findById(999L)).willReturn(Optional.empty());

            assertThrows(TicketNotFoundException.class, () -> ticketService.getTicketById(999L));
        }
    }

    @Nested
    @DisplayName("Update Ticket Tests")
    class UpdateTicketTests {

        @Test
        @DisplayName("Should update ticket and record PRIORITY_CHANGED when priority changes")
        void shouldRecordPriorityChangedActivityWhenPriorityChanges() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Updated Title");
            request.setDescription("Updated Desc");
            request.setPriority("LOW");

            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(ticketRepository.save(any(Ticket.class))).willReturn(ticket);

            Ticket result = ticketService.updateTicket(100L, request);

            assertEquals("LOW", result.getPriority());
            ArgumentCaptor<TicketActivity> activityCaptor = ArgumentCaptor.forClass(TicketActivity.class);
            verify(ticketActivityRepository).save(activityCaptor.capture());
            assertEquals("PRIORITY_CHANGED", activityCaptor.getValue().getAction());
            assertEquals("HIGH", activityCaptor.getValue().getOldValue());
            assertEquals("LOW", activityCaptor.getValue().getNewValue());
        }

        @Test
        @DisplayName("Should not record PRIORITY_CHANGED activity when priority is unchanged")
        void shouldNotRecordPriorityChangedActivityWhenPriorityUnchanged() {
            UpdateTicketRequest request = new UpdateTicketRequest();
            request.setTitle("Updated Title");
            request.setDescription("Updated Desc");
            request.setPriority("HIGH");

            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(ticketRepository.save(any(Ticket.class))).willReturn(ticket);

            ticketService.updateTicket(100L, request);

            verifyNoInteractions(ticketActivityRepository);
        }
    }

    @Nested
    @DisplayName("Ticket Assignment Tests")
    class AssignmentTests {

        @Test
        @DisplayName("Should assign ticket successfully and record ASSIGNED activity")
        void shouldAssignTicketSuccessfully() {
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(userRepository.findById(2L)).willReturn(Optional.of(assignee));

            TicketResponse response = ticketService.assignTicket(100L, 2L);

            assertNotNull(response);
            assertEquals(2L, response.getAssigneeId());
            assertEquals("Bob", response.getAssigneeName());

            ArgumentCaptor<TicketActivity> activityCaptor = ArgumentCaptor.forClass(TicketActivity.class);
            verify(ticketActivityRepository).save(activityCaptor.capture());
            assertEquals("ASSIGNED", activityCaptor.getValue().getAction());
            assertNull(activityCaptor.getValue().getOldValue());
            assertEquals("Bob", activityCaptor.getValue().getNewValue());
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when assignee does not exist")
        void shouldThrowUserNotFoundExceptionWhenAssigningToNonexistentUser() {
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> ticketService.assignTicket(100L, 999L));
            verifyNoInteractions(ticketActivityRepository);
        }

        @Test
        @DisplayName("Should not record ASSIGNED activity when assigning to same user")
        void shouldNotRecordActivityWhenAssigningToSameUser() {
            ticket.setAssignee(assignee);
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(userRepository.findById(2L)).willReturn(Optional.of(assignee));

            ticketService.assignTicket(100L, 2L);

            verifyNoInteractions(ticketActivityRepository);
        }
    }

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should transition OPEN -> IN_PROGRESS successfully")
        void shouldUpdateTicketStatusSuccessfully_OpenToInProgress() {
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));

            TicketResponse response = ticketService.updateTicketStatus(100L, "IN_PROGRESS");

            assertEquals("IN_PROGRESS", response.getStatus());
            ArgumentCaptor<TicketActivity> activityCaptor = ArgumentCaptor.forClass(TicketActivity.class);
            verify(ticketActivityRepository).save(activityCaptor.capture());
            assertEquals("STATUS_CHANGED", activityCaptor.getValue().getAction());
            assertEquals("OPEN", activityCaptor.getValue().getOldValue());
            assertEquals("IN_PROGRESS", activityCaptor.getValue().getNewValue());
        }

        @Test
        @DisplayName("Should transition IN_PROGRESS -> RESOLVED successfully")
        void shouldUpdateTicketStatusSuccessfully_InProgressToResolved() {
            ticket.setStatus("IN_PROGRESS");
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));

            TicketResponse response = ticketService.updateTicketStatus(100L, "RESOLVED");

            assertEquals("RESOLVED", response.getStatus());
        }

        @Test
        @DisplayName("Should transition RESOLVED -> CLOSED successfully")
        void shouldUpdateTicketStatusSuccessfully_ResolvedToClosed() {
            ticket.setStatus("RESOLVED");
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));

            TicketResponse response = ticketService.updateTicketStatus(100L, "CLOSED");

            assertEquals("CLOSED", response.getStatus());
        }

        @Test
        @DisplayName("Should throw InvalidTicketStatusTransitionException on CLOSED -> IN_PROGRESS")
        void shouldThrowInvalidTicketStatusTransitionException_OnClosedToInProgress() {
            ticket.setStatus("CLOSED");
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));

            assertThrows(InvalidTicketStatusTransitionException.class,
                () -> ticketService.updateTicketStatus(100L, "IN_PROGRESS"));
            verifyNoInteractions(ticketActivityRepository);
        }

        @Test
        @DisplayName("Should throw InvalidTicketStatusTransitionException on OPEN -> RESOLVED")
        void shouldThrowInvalidTicketStatusTransitionException_OnOpenToResolved() {
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));

            assertThrows(InvalidTicketStatusTransitionException.class,
                () -> ticketService.updateTicketStatus(100L, "RESOLVED"));
            verifyNoInteractions(ticketActivityRepository);
        }

        @Test
        @DisplayName("Should not record STATUS_CHANGED activity when status is unchanged (OPEN -> OPEN)")
        void shouldNotRecordStatusActivityWhenStatusUnchanged() {
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));

            TicketResponse response = ticketService.updateTicketStatus(100L, "OPEN");

            assertEquals("OPEN", response.getStatus());
            verifyNoInteractions(ticketActivityRepository);
        }

        @Test
        @DisplayName("Should assign and move ticket to IN_PROGRESS atomically")
        void shouldAssignTicketAndStartSuccessfully() {
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(userRepository.findById(2L)).willReturn(Optional.of(assignee));

            TicketResponse response = ticketService.assignTicketAndStart(100L, 2L);

            assertEquals("IN_PROGRESS", response.getStatus());
            assertEquals(2L, response.getAssigneeId());
            verify(ticketActivityRepository, times(2)).save(any(TicketActivity.class));
        }
    }

    @Nested
    @DisplayName("Comment Tests")
    class CommentTests {

        @Test
        @DisplayName("Should create comment successfully")
        void shouldCreateCommentSuccessfully() {
            CreateCommentRequest request = new CreateCommentRequest(1L, "Investigating now");
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(userRepository.findById(1L)).willReturn(Optional.of(reporter));
            given(ticketCommentRepository.save(any(TicketComment.class))).willAnswer(inv -> {
                TicketComment c = inv.getArgument(0);
                c.setId(50L);
                return c;
            });

            CommentResponse response = ticketService.createComment(100L, request);

            assertNotNull(response);
            assertEquals("Investigating now", response.getContent());
            assertEquals("Ayesha", response.getAuthorName());
            assertEquals(1L, response.getAuthorId());
        }

        @Test
        @DisplayName("Should throw TicketNotFoundException when creating comment on nonexistent ticket")
        void shouldThrowTicketNotFoundExceptionWhenCreatingCommentOnNonexistentTicket() {
            CreateCommentRequest request = new CreateCommentRequest(1L, "Investigating now");
            given(ticketRepository.findById(999L)).willReturn(Optional.empty());

            assertThrows(TicketNotFoundException.class, () -> ticketService.createComment(999L, request));
            verifyNoInteractions(userRepository, ticketCommentRepository);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when creating comment with nonexistent author")
        void shouldThrowUserNotFoundExceptionWhenCreatingCommentWithNonexistentAuthor() {
            CreateCommentRequest request = new CreateCommentRequest(999L, "Investigating now");
            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> ticketService.createComment(100L, request));
            verifyNoInteractions(ticketCommentRepository);
        }
    }

    @Nested
    @DisplayName("Activity & Search Query Tests")
    class QueryTests {

        @Test
        @DisplayName("Should return ticket activities ordered newest first")
        void shouldGetTicketActivitiesSuccessfully() {
            TicketActivity activity = new TicketActivity(ticket, "CREATED", "Ticket created", null, null);
            activity.setId(1L);
            activity.setCreatedAt(LocalDateTime.now());

            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(ticketActivityRepository.findByTicketIdOrderByCreatedAtDesc(100L)).willReturn(List.of(activity));

            List<TicketActivityResponse> activities = ticketService.getTicketActivities(100L);

            assertEquals(1, activities.size());
            assertEquals("CREATED", activities.get(0).getAction());
        }

        @Test
        @DisplayName("Should return paginated comments")
        void shouldGetCommentsSuccessfully() {
            TicketComment comment = new TicketComment(ticket, reporter, "Comment text");
            comment.setId(1L);
            comment.setCreatedAt(LocalDateTime.now());

            given(ticketRepository.findById(100L)).willReturn(Optional.of(ticket));
            given(ticketCommentRepository.findByTicketId(eq(100L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(comment)));

            Page<CommentResponse> page = ticketService.getComments(100L, 0, 10);

            assertEquals(1, page.getTotalElements());
            assertEquals("Comment text", page.getContent().get(0).getContent());
        }

        @Test
        @DisplayName("Should search tickets with dynamic filters and map to DTOs")
        @SuppressWarnings("unchecked")
        void shouldSearchTicketsWithDynamicFilters() {
            given(ticketRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(ticket)));

            Page<TicketResponse> page = ticketService.searchTickets(
                "OPEN", "HIGH", 1L, null, null, "timeout", null, null, 0, 10, "createdAt", "desc"
            );

            assertEquals(1, page.getTotalElements());
            assertEquals("Fix Bug", page.getContent().get(0).getTitle());
        }
    }
}
