package com.ayesha.resolvehub.integration;

import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.dto.UpdateTicketStatusRequest;
import com.ayesha.resolvehub.entity.Project;
import com.ayesha.resolvehub.entity.Role;
import com.ayesha.resolvehub.entity.Ticket;
import com.ayesha.resolvehub.entity.User;
import com.ayesha.resolvehub.repository.ProjectRepository;
import com.ayesha.resolvehub.repository.TicketRepository;
import com.ayesha.resolvehub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User reporterUser;
    private User agentUser;
    private User adminUser;
    private Project project;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        reporterUser = userRepository.save(new User(
            null, "Reporter", "reporter.sec@example.com", Role.REPORTER, passwordEncoder.encode("password123")
        ));
        agentUser = userRepository.save(new User(
            null, "Agent", "agent.sec@example.com", Role.AGENT, passwordEncoder.encode("password123")
        ));
        adminUser = userRepository.save(new User(
            null, "Admin", "admin.sec@example.com", Role.ADMIN, passwordEncoder.encode("password123")
        ));

        project = projectRepository.save(new Project(null, "Security Test Project", "Desc", reporterUser));

        ticket = new Ticket();
        ticket.setTitle("Security Bug");
        ticket.setDescription("Testing RBAC rules");
        ticket.setStatus("OPEN");
        ticket.setPriority("HIGH");
        ticket.setProject(project);
        ticket.setReporter(reporterUser);
        ticket = ticketRepository.save(ticket);
    }

    @Test
    @DisplayName("1. Unauthenticated request to protected endpoint should return 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/tickets"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("2. Valid HTTP Basic authentication for REPORTER should succeed")
    void shouldAuthenticateReporterWithHttpBasic() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .with(httpBasic("reporter.sec@example.com", "password123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("3. Invalid password with HTTP Basic should return 401 Unauthorized")
    void shouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(get("/api/tickets")
                .with(httpBasic("reporter.sec@example.com", "wrongPassword")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("4. Authenticated REPORTER should be able to create a ticket")
    @WithMockUser(username = "reporter.sec@example.com", roles = "REPORTER")
    void shouldAllowReporterToCreateTicket() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Reporter created ticket");
        request.setDescription("Ticket description here");
        request.setPriority("MEDIUM");
        request.setProjectId(project.getId());
        request.setReporterId(reporterUser.getId());

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("5. Authenticated REPORTER attempting AGENT-only status change should return 403 Forbidden")
    @WithMockUser(username = "reporter.sec@example.com", roles = "REPORTER")
    void shouldDenyReporterFromUpdatingStatus() throws Exception {
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest();
        request.setStatus("IN_PROGRESS");

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("6. Authenticated AGENT should be allowed to update ticket status")
    @WithMockUser(username = "agent.sec@example.com", roles = "AGENT")
    void shouldAllowAgentToUpdateStatus() throws Exception {
        UpdateTicketStatusRequest request = new UpdateTicketStatusRequest();
        request.setStatus("IN_PROGRESS");

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("7. Authenticated AGENT attempting ADMIN-only ticket deletion should return 403 Forbidden")
    @WithMockUser(username = "agent.sec@example.com", roles = "AGENT")
    void shouldDenyAgentFromDeletingTicket() throws Exception {
        mockMvc.perform(delete("/api/tickets/" + ticket.getId()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("8. Authenticated ADMIN should be allowed to delete a ticket")
    @WithMockUser(username = "admin.sec@example.com", roles = "ADMIN")
    void shouldAllowAdminToDeleteTicket() throws Exception {
        mockMvc.perform(delete("/api/tickets/" + ticket.getId()))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("9. Public endpoints (Swagger UI & OpenAPI docs) should be accessible unauthenticated")
    void shouldAllowUnauthenticatedAccessToSwaggerDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("10. Ticket responses should never leak user passwords")
    @WithMockUser(username = "admin.sec@example.com", roles = "ADMIN")
    void shouldNotExposePasswordsInResponses() throws Exception {
        mockMvc.perform(get("/api/tickets/" + ticket.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.reporterPassword").doesNotExist());
    }
}
