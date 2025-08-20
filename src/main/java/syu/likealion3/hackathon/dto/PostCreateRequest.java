package syu.likealion3.hackathon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import syu.likealion3.hackathon.entity.Category;

public record PostCreateRequest(
        @NotNull(message = "카테고리 선택은 필수입니다.")
        Category category,

        @NotBlank(message = "게시물 제목은 필수입니다")
        @Size(max = 20, message = "게시물 제목은 20자 이하여야 합니다")
        String name,

        @NotBlank(message = "주소는 필수입니다")
        @Size(max = 255, message = "주소는 255자 이하여야 합니다")
        String address,

        @NotBlank(message = "본문 내용은 필수입니다")
        @Size(max = 2000, message = "본문 내용은 2000자 이하여야 합니다")
        String content,

        @Size(max = 512, message = "이미지 URL은 512자 이하여야 합니다")
        String imgUrl
) {}
