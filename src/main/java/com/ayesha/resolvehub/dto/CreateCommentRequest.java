package com.ayesha.resolvehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to add a comment to a ticket")
public class CreateCommentRequest {

    @Schema(description = "User ID of the author posting the comment", example = "1")
    @NotNull(message = "Author ID is required")
    private Long authorId;

    @Schema(description = "Text content of the comment", example = "I reviewed the PR and verified the fix in staging.")
    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String content;

    public CreateCommentRequest() {
    }

    public CreateCommentRequest(Long authorId, String content) {
        this.authorId = authorId;
        this.content = content;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
