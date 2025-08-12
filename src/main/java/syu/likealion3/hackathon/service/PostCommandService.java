package syu.likealion3.hackathon.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import syu.likealion3.hackathon.dto.PostCreateRequest;
import syu.likealion3.hackathon.dto.PostResponseDto;
import syu.likealion3.hackathon.dto.PostUpdateRequest;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.repository.PostRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandService {

    private final PostRepository postRepository;

    public PostResponseDto create(PostCreateRequest req) {
        Post post = Post.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .category(req.toCategory())
                .imageUrl(req.getImageUrl())
                .storeName(req.getStoreName())
                .storeAddress(req.getStoreAddress())
                .build();
        return new PostResponseDto(postRepository.save(post));
    }

    public PostResponseDto update(Long id, PostUpdateRequest req) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다: " + id));

        // 엔티티 수정
        post = Post.builder()
                .id(post.getId()) // 동일 PK 유지
                .title(req.getTitle())
                .content(req.getContent())
                .category(req.toCategory())
                .imageUrl(req.getImageUrl())
                .storeName(req.getStoreName())
                .storeAddress(req.getStoreAddress())
                .likeCount(post.getLikeCount()) // 기존 좋아요 유지
                .build();

        return new PostResponseDto(postRepository.save(post));
    }

    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다: " + id);
        }
        postRepository.deleteById(id);
    }

    public PostResponseDto like(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다: " + id));
        post.incrementLikeCount();
        return new PostResponseDto(postRepository.save(post));
    }

    public PostResponseDto unlike(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다: " + id));
        post.decrementLikeCount();
        return new PostResponseDto(postRepository.save(post));
    }
}
