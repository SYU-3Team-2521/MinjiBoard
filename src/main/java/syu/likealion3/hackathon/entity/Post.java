package syu.likealion3.hackathon.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "posts")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;              // 게시글 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;            // 게시글 본문

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;         // 카테고리

    private String imageUrl;           // 이미지 URL (선택사항)

    @Column(nullable = false, length = 100)
    private String storeName;          // 가게 이름

    @Column(nullable = false, length = 200)
    private String storeAddress;       // 가게 주소

    @Column(nullable = false)
    private int likeCount = 0;         // 좋아요 수

    /**
     * 좋아요 수 증가
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 좋아요 수 감소 (0 이하로 내려가지 않도록)
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}