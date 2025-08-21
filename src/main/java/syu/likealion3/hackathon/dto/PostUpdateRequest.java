package syu.likealion3.hackathon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import syu.likealion3.hackathon.entity.Category;

public record PostUpdateRequest(
        @NotNull Category category,
        @NotBlank @Size(max = 20) String name,
        @NotBlank @Size(max = 255) String address,
        @NotBlank @Size(max = 2000) String content,
        String imgUrl // 이미지 교체 없는 경우 null 허용
) {}
