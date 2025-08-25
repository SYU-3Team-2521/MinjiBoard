package syu.likealion3.hackathon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "빈칸은 허용되지 않습니다.")
        @Size(max = 400, message = "내용은 400자를 넘을 수 없습니다.")
        String content,
        @Size(max = 100, message = "넘 많은 비번") String pin
) {}
