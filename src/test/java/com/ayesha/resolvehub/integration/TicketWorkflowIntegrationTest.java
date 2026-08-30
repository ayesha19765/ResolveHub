package com.ayesha.resolvehub.integration;

import com.ayesha.resolvehub.dto.AssignTicketRequest;
import com.ayesha.resolvehub.dto.CreateCommentRequest;
import com.ayesha.resolvehub.dto.CreateTicketRequest;
import com.ayesha.resolvehub.dto.UpdateTicketStatusRequest;
import com.ayesha.resolvehub.entity.Project;
import com.ayesha.resolvehub.entity.User;
import com.ayesha.resolvehub.repository.ProjectRepository;
import com.ayesha.resolvehub.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User reporter;
    private User assignee;
    private Project project;

    @BeforeEach
    void setUp() {
        reporter = userRepository.save(new User(null, "Ayesha", "ayesha.wf@example.com", "ENGINEER"));
        assignee = userRepository.save(new User(null, "Bob", "bob.wf@example.com", "ENGINEER"));
        project = projectRepository.save(new Project(null, "ResolveHub Core", "Core Project", reporter));
    }

    @Test
    @DisplayName("End-to-End Workflow: Create -> Assign -> Status Update -> Comment -> Query Activities & Search")
    void testCompleteTicketLifecycleWorkflow() throws Exception {
        // 1. Create Ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setTitle("Integration Bug Test");
        createRequest.setDescription("Reproduce lifecycle in tests");
        createRequest.setPriority("HIGH");
        createRequest.setProjectId(project.getId());
        createRequest.setReporterId(reporter.getId());

        MvcResult createResult = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andReturn();

        JsonNode ticketNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long ticketId = ticketNode.get("id").asLong();

        // 2. Assign Ticket
        AssignTicketRequest assignRequest = new AssignTicketRequest(assignee.getId());
        mockMvc.perform(patch("/api/tickets/" + ticketId + "/assignee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assigneeId").value(assignee.getId()))
            .andExpect(jsonPath("$.assigneeName").value("Bob"));

        // 3. Transition Status: OPEN -> IN_PROGRESS
        UpdateTicketStatusRequest statusRequest = new UpdateTicketStatusRequest();
        statusRequest.setStatus("IN_PROGRESS");
        mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 4. Add Comment
        CreateCommentRequest commentRequest = new CreateCommentRequest(reporter.getId(), "Fix has been pushed to staging branch.");
        mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.authorName").value("Ayesha"))
            .andExpect(jsonPath("$.content").value("Fix has been pushed to staging branch."));

        // 5. Verify Activities (Should contain CREATED, ASSIGNED, STATUS_CHANGED in newest-first order)
        mockMvc.perform(get("/api/tickets/" + ticketId + "/activities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].action").value("STATUS_CHANGED"))
            .andExpect(jsonPath("$[1].action").value("ASSIGNED"))
            .andExpect(jsonPath("$[2].action").value("CREATED"));

        // 6. Verify Comments Retrieval
        mockMvc.perform(get("/api/tickets/" + ticketId + "/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].authorName").value("Ayesha"));

        // 7. Verify Dynamic Search
        mockMvc.perform(get("/api/tickets?status=IN_PROGRESS&assigneeId=" + assignee.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].id").value(ticketId));
    }
}
