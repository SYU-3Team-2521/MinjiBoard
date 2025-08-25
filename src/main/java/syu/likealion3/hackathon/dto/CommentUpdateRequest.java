package syu.likealion3.hackathon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @NotBlank (message = "빈칸은 허용되지 않습니다.")
        @Size(max = 1000, message = "최대 1000자 입니다.") String content
) {}
