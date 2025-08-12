package syu.likealion3.hackathon.dto;

import lombok.Getter;
import syu.likealion3.hackathon.entity.Post;

import java.time.LocalDateTime;

@Getter
public class PostResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final String category;
    private final String imageUrl;
    private final String storeName;
    private final String storeAddress;
    private final int likeCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PostResponseDto(Post p) {
        this.id = p.getId();
        this.title = p.getTitle();
        this.content = p.getContent();
        this.category = p.getCategory().getDisplayName();
        this.imageUrl = p.getImageUrl();
        this.storeName = p.getStoreName();
        this.storeAddress = p.getStoreAddress();
        this.likeCount = p.getLikeCount();
        this.createdAt = p.getCreatedAt();
        this.updatedAt = p.getUpdatedAt();
    }
}
