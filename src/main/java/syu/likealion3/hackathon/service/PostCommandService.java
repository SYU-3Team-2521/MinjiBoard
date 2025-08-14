package syu.likealion3.hackathon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syu.likealion3.hackathon.dto.LikeResponseDto;
import syu.likealion3.hackathon.dto.PostCreateRequest;
import syu.likealion3.hackathon.dto.PostCreateResponse;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.repository.PostRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;

    @Transactional
    public PostCreateResponse create(PostCreateRequest req) {
        Post post = Post.builder()
                .category(req.category())
                .name(req.name())
                .address(req.address())
                .content(req.content())
                .imgUrl(req.imgUrl())
                .likeCount(0)
                .createdAt(null)
                .build();

        Long id = postRepository.save(post).getId();
        return new PostCreateResponse(id);
    }

    @Transactional
    public LikeResponseDto like(Long postId, boolean alreadyLiked) {
        // 이미 눌렀으면 증가 없음
        if (alreadyLiked) {
            Post p = postRepository.findById(postId)
                    .orElseThrow(() -> new NoSuchElementException("Post not found"));
            return new LikeResponseDto(true, p.getLikeCount());
        }
        int updated = postRepository.incrementLike(postId);
        if (updated == 0) throw new NoSuchElementException("Post not found");
        // 증가 후 최신 값 조회
        int now = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"))
                .getLikeCount();
        return new LikeResponseDto(true, now);
    }
}
