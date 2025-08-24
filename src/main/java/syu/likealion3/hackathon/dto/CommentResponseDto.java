package syu.likealion3.hackathon.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import syu.likealion3.hackathon.entity.Comment;

import java.time.format.DateTimeFormatter;

public record CommentResponseDto(
        Long id,
        String content,
        String createdAtFormatted,
        @JsonIgnore String tokenPlain // 쿠키 발급용, JSON에는 숨김
) {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 목록/상세 조회용: tokenPlain 없음 */
    public static CommentResponseDto from(Comment c) {
        String t = (c.getCreatedAt() == null) ? null : c.getCreatedAt().format(F);
        return new CommentResponseDto(c.getId(), c.getContent(), t, null);
    }

    /** 생성 직후 컨트롤러로 전달할 때 사용: tokenPlain 포함 */
    public static CommentResponseDto fromWithToken(Comment c, String tokenPlain) {
        String t = (c.getCreatedAt() == null) ? null : c.getCreatedAt().format(F);
        return new CommentResponseDto(c.getId(), c.getContent(), t, tokenPlain);
    }
}