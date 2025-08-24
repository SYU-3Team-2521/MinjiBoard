package syu.likealion3.hackathon.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;

/**
 * 게시글 엔티티
 * - 목록/상세/좋아요/댓글의 기반이 되는 핵심 테이블
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_posts_category", columnList = "category"),
                @Index(name = "idx_posts_likeCount", columnList = "likeCount")
        }
)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카테고리 (문자열로 저장) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Category category;

    /** 가게 이름 */
    @Column(nullable = false, length = 20)
    private String name;

    /** 주소 */
    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "pinPost", length = 100)
    private String pinPost;

    @Column(name = "token_hash_Post", columnDefinition = "BINARY(32)")
    private byte[] tokenHashPost;

    /** 설명/홍보글 (긴 문장 가능) */
    @Lob
    @Column(nullable = false)
    private String content;

    /** 대표 이미지 URL (단일) */
    @Column(length = 512)
    private String imgUrl;

    /** 좋아요 수 */
    @Column(nullable = false)
    private Integer likeCount = 0;

    /** 생성 시각 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Post(Category category,
                 String name,
                 String address,
                 String content,
                 String imgUrl,
                 Integer likeCount,
                 LocalDateTime createdAt) {
        this.category = category;//
        this.name = name;
        this.address = address;
        this.content = content;//
        this.imgUrl = imgUrl;//
        this.likeCount = likeCount;//
        this.createdAt = createdAt;//
    }

    /** 영속화 직전 기본값 처리 */
    @PrePersist
    protected void onCreate() {
        if (this.likeCount == null) this.likeCount = 0;
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public void update(Category category, String name, String address, String content, String imgUrl) {
        this.category = category;
        this.name = name;
        this.address = address;
        this.content = content;
        if (imgUrl != null && !imgUrl.isBlank()) {
            this.imgUrl = imgUrl;
        }
    }

    public void setPinPost(String pinPost) { this.pinPost = pinPost; }
    public void setTokenHashPost(byte[] tokenHashPost) { this.tokenHashPost = tokenHashPost; }
    public String getPinPost() { return pinPost; }
    public byte[] getTokenHashPost() { return tokenHashPost; }

    /** 좋아요 증가 (동시성 제어는 서비스/레포지토리에서 처리) */
    public void incrementLike() {
        this.likeCount = this.likeCount + 1;
    }
}
