package syu.likealion3.hackathon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "content is required")
        @Size(max = 2000, message = "content must be <= 2000")
        String content
) {}
