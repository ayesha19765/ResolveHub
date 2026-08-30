package com.ayesha.resolvehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Representation of a ticket discussion comment")
public class CommentResponse {

    @Schema(description = "Unique comment ID", example = "1")
    private Long id;

    @Schema(description = "Author user ID", example = "1")
    private Long authorId;

    @Schema(description = "Author user name", example = "Ayesha")
    private String authorName;

    @Schema(description = "Comment text content", example = "I investigated the connection pool and found a leak.")
    private String content;

    @Schema(description = "Timestamp when comment was created", example = "2026-08-30T15:36:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when comment was last updated", example = "2026-08-30T15:36:00")
    private LocalDateTime updatedAt;

    public CommentResponse() {
    }

    public CommentResponse(
        Long id,
        Long authorId,
        String authorName,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
