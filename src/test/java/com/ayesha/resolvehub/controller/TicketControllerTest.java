package com.ayesha.resolvehub.controller;

import com.ayesha.resolvehub.dto.AssignTicketRequest;
import com.ayesha.resolvehub.dto.CommentResponse;
import com.ayesha.resolvehub.dto.CreateCommentRequest;
import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.dto.TicketActivityResponse;
import com.ayesha.resolvehub.dto.TicketResponse;
import com.ayesha.resolvehub.dto.UpdateTicketStatusRequest;
import com.ayesha.resolvehub.exception.GlobalExceptionHandler;
import com.ayesha.resolvehub.exception.InvalidTicketStatusTransitionException;
import com.ayesha.resolvehub.exception.TicketNotFoundException;
import com.ayesha.resolvehub.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {TicketController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    private TicketResponse sampleTicketResponse() {
        return new TicketResponse(
            1L,
            "Fix connection timeout",
            "Details here",
            "OPEN",
            "HIGH",
            1L,
            "ResolveHub Core",
            1L,
            "Ayesha",
            null,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Ticket CRUD and Assignment Endpoints")
    class TicketEndpoints {

        @Test
        @DisplayName("GET /api/tickets/{id} should return 200 and TicketResponse")
        void shouldReturnTicketById() throws Exception {
            given(ticketService.getTicketById(1L)).willReturn(sampleTicketResponse());

            mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Fix connection timeout"))
                .andExpect(jsonPath("$.status").value("OPEN"));
        }

        @Test
        @DisplayName("GET /api/tickets/{id} when missing should return 404 ApiErrorResponse")
        void shouldReturn404WhenTicketNotFound() throws Exception {
            given(ticketService.getTicketById(999L)).willThrow(new TicketNotFoundException(999L));

            mockMvc.perform(get("/api/tickets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ticket with id 999 not found"))
                .andExpect(jsonPath("$.path").value("/api/tickets/999"));
        }

        @Test
        @DisplayName("POST /api/tickets should return 200 and created TicketResponse")
        void shouldCreateTicketSuccessfully() throws Exception {
            CreateTicketRequest request = new CreateTicketRequest();
            request.setTitle("New Ticket");
            request.setDescription("Ticket description");
            request.setPriority("HIGH");
            request.setProjectId(1L);
            request.setReporterId(1L);

            given(ticketService.createTicket(any(CreateTicketRequest.class))).willReturn(sampleTicketResponse());

            mockMvc.perform(post("/api/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("POST /api/tickets with invalid body should return 400 with fieldErrors")
        void shouldReturn400WhenCreateTicketValidationFails() throws Exception {
            CreateTicketRequest invalidRequest = new CreateTicketRequest(); // all fields null

            mockMvc.perform(post("/api/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.description").exists())
                .andExpect(jsonPath("$.fieldErrors.priority").exists())
                .andExpect(jsonPath("$.fieldErrors.projectId").exists())
                .andExpect(jsonPath("$.fieldErrors.reporterId").exists());
        }

        @Test
        @DisplayName("PATCH /api/tickets/{id}/assignee should return 200 and updated response")
        void shouldAssignTicket() throws Exception {
            AssignTicketRequest request = new AssignTicketRequest(2L);
            given(ticketService.assignTicket(eq(1L), eq(2L))).willReturn(sampleTicketResponse());

            mockMvc.perform(patch("/api/tickets/1/assignee")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("PATCH /api/tickets/{id}/status when transition is invalid should return 400")
        void shouldReturn400WhenStatusTransitionIsInvalid() throws Exception {
            UpdateTicketStatusRequest request = new UpdateTicketStatusRequest();
            request.setStatus("IN_PROGRESS");

            given(ticketService.updateTicketStatus(eq(1L), eq("IN_PROGRESS")))
                .willThrow(new InvalidTicketStatusTransitionException("CLOSED", "IN_PROGRESS"));

            mockMvc.perform(patch("/api/tickets/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cannot transition ticket status from CLOSED to IN_PROGRESS"));
        }

        @Test
        @DisplayName("PATCH /api/tickets/{id}/assign-and-start should return 200")
        void shouldAssignAndStartTicket() throws Exception {
            AssignTicketRequest request = new AssignTicketRequest(2L);
            given(ticketService.assignTicketAndStart(eq(1L), eq(2L))).willReturn(sampleTicketResponse());

            mockMvc.perform(patch("/api/tickets/1/assign-and-start")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        }
    }

    @Nested
    @DisplayName("Activities, Comments, and Search Endpoints")
    class ActivityCommentSearchEndpoints {

        @Test
        @DisplayName("GET /api/tickets/{id}/activities should return 200 and activity list")
        void shouldReturnTicketActivities() throws Exception {
            TicketActivityResponse activity = new TicketActivityResponse(
                1L, "CREATED", "Ticket created", null, null, LocalDateTime.now()
            );
            given(ticketService.getTicketActivities(1L)).willReturn(List.of(activity));

            mockMvc.perform(get("/api/tickets/1/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].action").value("CREATED"));
        }

        @Test
        @DisplayName("POST /api/tickets/{id}/comments should return 200 and CommentResponse")
        void shouldCreateComment() throws Exception {
            CreateCommentRequest request = new CreateCommentRequest(1L, "Good progress");
            CommentResponse response = new CommentResponse(
                10L, 1L, "Ayesha", "Good progress", LocalDateTime.now(), LocalDateTime.now()
            );

            given(ticketService.createComment(eq(1L), any(CreateCommentRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/tickets/1/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.authorName").value("Ayesha"))
                .andExpect(jsonPath("$.content").value("Good progress"));
        }

        @Test
        @DisplayName("POST /api/tickets/{id}/comments with blank content should return 400 fieldErrors")
        void shouldReturn400WhenCommentContentIsBlank() throws Exception {
            CreateCommentRequest invalid = new CreateCommentRequest(1L, "");

            mockMvc.perform(post("/api/tickets/1/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.content").exists());
        }

        @Test
        @DisplayName("GET /api/tickets/{id}/comments should return 200 and paginated comments")
        void shouldReturnPaginatedComments() throws Exception {
            CommentResponse response = new CommentResponse(
                10L, 1L, "Ayesha", "Good progress", LocalDateTime.now(), LocalDateTime.now()
            );
            given(ticketService.getComments(eq(1L), anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of(response)));

            mockMvc.perform(get("/api/tickets/1/comments?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L));
        }

        @Test
        @DisplayName("GET /api/tickets (Search) should return 200 and paginated tickets")
        void shouldSearchTickets() throws Exception {
            given(ticketService.searchTickets(
                any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()
            )).willReturn(new PageImpl<>(List.of(sampleTicketResponse())));

            mockMvc.perform(get("/api/tickets?status=OPEN&priority=HIGH&search=timeout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
        }

        @Test
        @DisplayName("Malformed JSON should return 400 Bad Request")
        void shouldReturn400OnMalformedJson() throws Exception {
            String malformedJson = "{ \"title\": \"Broken\" \"description\": \"missing comma\" }";

            mockMvc.perform(post("/api/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"));
        }

        @Test
        @DisplayName("Type mismatch on path variable should return 400 Bad Request")
        void shouldReturn400OnTypeMismatch() throws Exception {
            mockMvc.perform(get("/api/tickets/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter: 'id' should be of type Long"));
        }
    }
}
