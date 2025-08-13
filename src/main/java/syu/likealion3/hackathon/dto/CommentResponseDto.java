package syu.likealion3.hackathon.dto;

import syu.likealion3.hackathon.entity.Comment;

import java.time.format.DateTimeFormatter;

public record CommentResponseDto(
        Long id,
        String content,
        String createdAtFormatted
) {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static CommentResponseDto from(Comment c) {
        String t = (c.getCreatedAt() == null) ? null : c.getCreatedAt().format(F);
        return new CommentResponseDto(c.getId(), c.getContent(), t);
    }
}
