package syu.likealion3.hackathon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import syu.likealion3.hackathon.entity.Category;

public record PostCreateRequest(
        @NotNull(message = "category is required")
        Category category,

        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be <= 100")
        String name,

        @NotBlank(message = "address is required")
        @Size(max = 255, message = "address must be <= 255")
        String address,

        @NotBlank(message = "content is required")
        String content,

        @Size(max = 512, message = "imgUrl must be <= 512")
        String imgUrl
) {}
