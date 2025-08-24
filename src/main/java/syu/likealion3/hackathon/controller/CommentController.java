package syu.likealion3.hackathon.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import syu.likealion3.hackathon.dto.CommentCreateRequest;
import syu.likealion3.hackathon.dto.CommentResponseDto;
import syu.likealion3.hackathon.dto.CommentUpdateRequest;
import syu.likealion3.hackathon.dto.PageResponse;
import syu.likealion3.hackathon.service.CommentService;

import java.net.URI;
import java.time.Duration;
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

    /** 댓글 작성: 생성 시 소유자 쿠키(comment_owner_{id}) 발급 */
    @PostMapping
    public ResponseEntity<Map<String, Long>> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest req
    ) {
        // 서비스가 tokenPlain 포함 DTO를 반환
        CommentResponseDto created = commentService.create(postId, req);

        // 동일 브라우저에서 PIN 없이 수정/삭제 가능하도록 쿠키 발급
        ResponseCookie owner = ResponseCookie.from("comment_owner_" + created.id(), created.tokenPlain())
                .httpOnly(true)
                .secure(true)                 // 로컬 HTTP 테스트면 보안상 주의. 필요 시 설정값으로 제어하세요.
                .sameSite("Strict")           // 운영 권장값. 필요 시 "Lax"로 완화 가능
                .path("/api/posts/" + postId + "/comments") // 컬렉션 경로로 스코프(수정/삭제 요청에 쿠키가 붙음)
                .maxAge(Duration.ofDays(365))
                .build();

        return ResponseEntity.created(URI.create("/api/posts/" + postId + "/comments/" + created.id()))
                .header(HttpHeaders.SET_COOKIE, owner.toString())
                .body(Map.of("id", created.id()));
    }

    /** 댓글 수정: 쿠키 토큰 우선, 실패 시 X-PIN 헤더로 검증 */
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest req,
            @RequestHeader(value = "X-PIN", required = false) String pin,
            HttpServletRequest httpReq
    ){
        commentService.update(postId, commentId, req, httpReq, pin);
        return ResponseEntity.noContent().build();
    }

    /** 댓글 삭제: 쿠키 토큰 우선, 실패 시 X-PIN 헤더로 검증 */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestHeader(value = "X-PIN", required = false) String pin,
            HttpServletRequest httpReq
    ){
        commentService.delete(postId, commentId, httpReq, pin);
        return ResponseEntity.noContent().build();
    }
}
