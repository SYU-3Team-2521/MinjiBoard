package syu.likealion3.hackathon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syu.likealion3.hackathon.dto.CommentCreateRequest;
import syu.likealion3.hackathon.dto.CommentResponseDto;
import syu.likealion3.hackathon.dto.PageResponse;
import syu.likealion3.hackathon.entity.Comment;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.repository.CommentRepository;
import syu.likealion3.hackathon.repository.PostRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public PageResponse<CommentResponseDto> list(Long postId, Pageable pageable) {
        // 존재하지 않는 게시글이면 404
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("Post not found");
        }
        Page<Comment> page = commentRepository.findByPostId(postId, pageable);
        Page<CommentResponseDto> mapped = page.map(CommentResponseDto::from);
        return PageResponse.of(mapped);
    }

    @Transactional
    public Long create(Long postId, CommentCreateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        Comment saved = commentRepository.save(
                Comment.builder()
                        .post(post)
                        .content(req.content())
                        .build()
        );
        return saved.getId();
    }
}
