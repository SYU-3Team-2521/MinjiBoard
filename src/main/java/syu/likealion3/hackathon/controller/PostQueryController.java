package syu.likealion3.hackathon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import syu.likealion3.hackathon.dto.PostListResponseDto;
import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.service.PostQueryService;

import java.util.List;

/**
 * 게시글 조회 기능을 제공하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostQueryController {

    private final PostQueryService postQueryService;

    /**
     * 게시글 목록 조회 (카테고리 + 정렬 옵션)
     * [GET] /posts?category=FOOD&sort=likes
     */
    @GetMapping
    public ResponseEntity<List<PostListResponseDto>> getPostsByCategory(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "recent") String sort) {

        try {
            if (category != null) {
                Category enumCategory = Category.valueOf(category.toUpperCase());
                return "likes".equals(sort)
                        ? ResponseEntity.ok(postQueryService.getPostsByCategoryOrderByLikes(enumCategory))
                        : ResponseEntity.ok(postQueryService.getPostsByCategory(enumCategory));
            } else {
                return "likes".equals(sort)
                        ? ResponseEntity.ok(postQueryService.getAllPostsOrderByLikes())
                        : ResponseEntity.ok(postQueryService.getAllPosts());
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다: " + category);
        }
    }

    /**
     * 게시글 상세 조회
     * [GET] /posts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postQueryService.getPostById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다: " + id));

        return ResponseEntity.ok(post);
    }

    /**
     * 가게명으로 검색
     * [GET] /posts/search?storeName=피자
     */
    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchByStoreName(@RequestParam String storeName) {
        List<Post> posts = postQueryService.searchByStoreName(storeName);
        return ResponseEntity.ok(posts);
    }

    /**
     * 인기 게시글 조회 (좋아요 수 기준)
     * [GET] /posts/popular?minLikes=5
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Post>> getPopularPosts(@RequestParam(defaultValue = "5") int minLikes) {
        List<Post> posts = postQueryService.getPopularPosts(minLikes);
        return ResponseEntity.ok(posts);
    }
}
