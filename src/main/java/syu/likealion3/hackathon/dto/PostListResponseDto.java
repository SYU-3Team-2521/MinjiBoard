package syu.likealion3.hackathon.dto;

import lombok.Getter;
import syu.likealion3.hackathon.entity.Post;

/**
 * 게시글 목록 조회 응답 DTO
 */
@Getter
public class PostListResponseDto {

    private final Long id;
    private final String title;
    private final String category;
    private final int likeCount;
    private final String storeName;

    public PostListResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.category = post.getCategory().getDisplayName(); // 한글명 반환
        this.likeCount = post.getLikeCount();
        this.storeName = post.getStoreName();
    }
}
