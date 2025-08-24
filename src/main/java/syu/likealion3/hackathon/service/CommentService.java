package syu.likealion3.hackathon.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syu.likealion3.hackathon.dto.CommentCreateRequest;
import syu.likealion3.hackathon.dto.CommentResponseDto;
import syu.likealion3.hackathon.dto.CommentUpdateRequest;
import syu.likealion3.hackathon.dto.PageResponse;
import syu.likealion3.hackathon.entity.Comment;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.exception.ForbiddenException;
import syu.likealion3.hackathon.exception.TooManyAttemptsException;
import syu.likealion3.hackathon.repository.CommentRepository;
import syu.likealion3.hackathon.repository.PostRepository;
import syu.likealion3.hackathon.util.SecurityUtil;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 간단 레이트 리밋: (commentId + remoteAddr) 기준 10분 내 5회 실패 시도 차단
    private final ConcurrentHashMap<String, int[]> pinTrials = new ConcurrentHashMap<>();
    private static final long BLOCK_WINDOW_MS = 10 * 60 * 1000L;

    /**
     * 댓글 목록
     */
    public PageResponse<CommentResponseDto> list(Long postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("Post not found");
        }
        Page<Comment> page = commentRepository.findByPostId(postId, pageable);
        Page<CommentResponseDto> mapped = page.map(CommentResponseDto::from);
        return PageResponse.of(mapped);
    }

    /**
     * 댓글 작성
     * - PIN 선택 입력 → bcrypt 해시 저장
     * - 토큰 원본 생성 → SHA-256 해시 저장
     * - tokenPlain은 컨트롤러에서 쿠키로 내려줌
     */
    @Transactional
    public CommentResponseDto create(Long postId, CommentCreateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        Comment c = Comment.builder()
                .post(post)
                .content(req.content())
                .build();

        if (req.pin() != null && !req.pin().isBlank()) {
            c.setPinComment(passwordEncoder.encode(req.pin()));
        }
        else{
            c.setPinComment(passwordEncoder.encode("000000"));
        }

        //
        
        String tokenPlain = SecurityUtil.generateTokenBase64Url();
        c.setTokenHashComment(SecurityUtil.sha256(tokenPlain));

        Comment saved = commentRepository.save(c);

        // ✅ tokenPlain 포함해서 반환 → 컨트롤러에서 쿠키 발급
        return CommentResponseDto.fromWithToken(saved, tokenPlain);
    }

    /**
     * 댓글 수정
     * - 동일 브라우저: 쿠키 토큰 일치 시 바로 성공
     * - 다른 브라우저: PIN 검증 필요
     */
    @Transactional
    public void update(Long postId, Long commentId, CommentUpdateRequest req,
                       HttpServletRequest httpReq, String pinHeader) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        if (!c.getPost().getId().equals(postId)) throw new NoSuchElementException("Comment not in post");
        if (!isOwner(c, httpReq, pinHeader)) throw new ForbiddenException("NOT_OWNER");
        c.update(req.content());
    }

    /**
     * 댓글 삭제
     * - isOwner 동일 절차
     */
    @Transactional
    public void delete(Long postId, Long commentId,
                       HttpServletRequest httpReq, String pinHeader) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        if (!c.getPost().getId().equals(postId)) throw new NoSuchElementException("Comment not in post");
        if (!isOwner(c, httpReq, pinHeader)) throw new ForbiddenException("NOT_OWNER");
        commentRepository.delete(c);
    }

    /* ===========================
       내부 소유자 검증 로직
       =========================== */

    private boolean isOwner(Comment c, HttpServletRequest req, String pinCandidate) {
        // 1) 쿠키 토큰 우선
        String cookieName = "comment_owner_" + c.getId();
        String cookieToken = readCookie(req, cookieName);
        if (cookieToken != null) {
            byte[] given = SecurityUtil.sha256(cookieToken);
            if (SecurityUtil.constantTimeEquals(given, c.getTokenHashComment())) {
                return true;
            }
        }

        // 2) PIN 검증
        String storedHash = c.getPinComment();
        if (storedHash == null || storedHash.isBlank()) {
            return false; // PIN 없는 댓글은 쿠키 없으면 수정 불가
        }

        // 레이트 리밋 적용
        String ip = req.getRemoteAddr() == null ? "" : req.getRemoteAddr();
        String key = "comment:" + c.getId() + ":" + ip;
        int[] slot = pinTrials.computeIfAbsent(key, k -> new int[]{0, (int) (System.currentTimeMillis() / 1000)});
        long now = System.currentTimeMillis();
        long startSec = slot[1];
        if ((now / 1000 - startSec) > (BLOCK_WINDOW_MS / 1000)) {
            slot[0] = 0;
            slot[1] = (int) (now / 1000);
        }
        if (slot[0] >= 5) throw new TooManyAttemptsException("PIN_TRY_LIMIT");

        boolean ok = (pinCandidate != null && passwordEncoder.matches(pinCandidate, storedHash));
        if (!ok) slot[0]++;
        return ok;
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (jakarta.servlet.http.Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
