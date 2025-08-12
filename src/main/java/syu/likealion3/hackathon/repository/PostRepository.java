package syu.likealion3.hackathon.repository;

import syu.likealion3.hackathon.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import syu.likealion3.hackathon.entity.Category;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 카테고리별 게시글 조회 (최신순 정렬)
     */
    List<Post> findByCategory(Category category);

    /**
     * 전체 게시글 최신순 조회
     */
    List<Post> findAllByOrderByCreatedAtDesc();

    /**
     * 카테고리별 게시글 좋아요순 조회 (추후 기능)
     */
    List<Post> findByCategoryOrderByLikeCountDesc(Category category);

    /**
     * 전체 게시글 좋아요순 조회 (추후 기능)
     */
    List<Post> findAllByOrderByLikeCountDesc();

    /**
     * 가게명으로 검색 (부분 일치)
     */
    List<Post> findByStoreNameContaining(String storeName);

    /**
     * 좋아요 수가 특정 값 이상인 게시글 조회
     */
    @Query("SELECT p FROM Post p WHERE p.likeCount >= :minLikes ORDER BY p.likeCount DESC")
    List<Post> findPostsWithMinimumLikes(int minLikes);
}