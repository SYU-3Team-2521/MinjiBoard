package syu.likealion3.hackathon.dto;

import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.entity.Post;

import java.time.format.DateTimeFormatter;

public record PostDetailDto(
        Long id,
        Category category,
        String name,
        String address,
        String content,
        String imgUrl,
        Integer likeCount,
        String createdAtFormatted,
        boolean liked
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static PostDetailDto from(Post p, boolean liked) {
        String created = (p.getCreatedAt() == null) ? null : p.getCreatedAt().format(FORMATTER);
        return new PostDetailDto(
                p.getId(),
                p.getCategory(),
                p.getName(),
                p.getAddress(),
                p.getContent(),
                p.getImgUrl(),
                p.getLikeCount(),
                created,
                liked
        );
    }
}
