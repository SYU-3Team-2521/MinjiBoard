package syu.likealion3.hackathon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syu.likealion3.hackathon.dto.PostListResponseDto;
import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.repository.PostRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    /**
     * 전체 게시글 조회 (최신순 정렬)
     */
    public List<PostListResponseDto> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostListResponseDto::new)
                .toList();
    }

    /**
     * 카테고리별 게시글 조회 (최신순)
     */
    public List<PostListResponseDto> getPostsByCategory(Category category) {
        return postRepository.findByCategory(category).stream()
                .map(PostListResponseDto::new)
                .toList();
    }

    /**
     * 카테고리별 좋아요순 게시글 조회
     */
    public List<PostListResponseDto> getPostsByCategoryOrderByLikes(Category category) {
        return postRepository.findByCategoryOrderByLikeCountDesc(category).stream()
                .map(PostListResponseDto::new)
                .toList();
    }

    /**
     * 전체 게시글 좋아요순 조회
     */
    public List<PostListResponseDto> getAllPostsOrderByLikes() {
        return postRepository.findAllByOrderByLikeCountDesc().stream()
                .map(PostListResponseDto::new)
                .toList();
    }

    /**
     * 특정 게시글 조회 (상세 보기용)
     */
    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    /**
     * 가게명 검색
     */
    public List<Post> searchByStoreName(String storeName) {
        return postRepository.findByStoreNameContaining(storeName);
    }

    /**
     * 인기 게시글 조회 (좋아요 수 기준)
     */
    public List<Post> getPopularPosts(int minLikes) {
        return postRepository.findPostsWithMinimumLikes(minLikes);
    }
}
