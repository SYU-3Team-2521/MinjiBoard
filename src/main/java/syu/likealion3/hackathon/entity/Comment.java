package syu.likealion3.hackathon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString(exclude = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "comments",
        indexes = {
                @Index(name = "idx_comments_post", columnList = "post_id"),
                @Index(name = "idx_comments_createdAt", columnList = "createdAt")
        })
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대상 게시글 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** 댓글 내용 */
    @Lob
    @Column(nullable = false)
    private String content;

    /** 작성 시각 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Comment(Post post, String content, LocalDateTime createdAt) {
        this.post = post;
        this.content = content;
        this.createdAt = createdAt;
    }

    public void update(String content) { this.content = content; }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
