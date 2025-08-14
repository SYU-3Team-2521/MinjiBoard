package syu.likealion3.hackathon.dto;

import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.entity.Post;

import java.time.format.DateTimeFormatter;

public record PostListItemDto(
        Long id,
        Category category,
        String name,
        String address,
        Integer likeCount,
        String imgUrl,
        String createdAtFormatted
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static PostListItemDto from(Post p) {
        String created = (p.getCreatedAt() == null) ? null : p.getCreatedAt().format(FORMATTER);
        return new PostListItemDto(
                p.getId(),
                p.getCategory(),
                p.getName(),
                p.getAddress(),
                p.getLikeCount(),
                p.getImgUrl(),
                created
        );
    }
}
