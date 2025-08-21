package syu.likealion3.hackathon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import syu.likealion3.hackathon.dto.CommentCreateRequest;
import syu.likealion3.hackathon.dto.CommentResponseDto;
import syu.likealion3.hackathon.dto.CommentUpdateRequest;
import syu.likealion3.hackathon.dto.PageResponse;
import syu.likealion3.hackathon.service.CommentService;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 댓글 목록: 기본 createdAt desc, page=0,size=10 */
    @GetMapping
    public ResponseEntity<PageResponse<CommentResponseDto>> list(
            @PathVariable Long postId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        PageResponse<CommentResponseDto> resp = commentService.list(postId, pageable);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Void> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest req
    ){
        commentService.update(postId, commentId, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId
    ){
        commentService.delete(postId, commentId);
        return ResponseEntity.noContent().build();
    }


    /** 댓글 작성 */
    @PostMapping
    public ResponseEntity<Map<String, Long>> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest req
    ) {
        Long id = commentService.create(postId, req);
        return ResponseEntity.created(URI.create("/api/posts/" + postId + "/comments/" + id))
                .body(Map.of("id", id));
    }
}