package syu.likealion3.hackathon.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import syu.likealion3.hackathon.dto.LikeResponseDto;
import syu.likealion3.hackathon.dto.PostCreateRequest;
import syu.likealion3.hackathon.dto.PostCreateResponse;
import syu.likealion3.hackathon.dto.PostUpdateRequest;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.exception.ForbiddenException;
import syu.likealion3.hackathon.exception.TooManyAttemptsException;
import syu.likealion3.hackathon.repository.PostRepository;
import syu.likealion3.hackathon.util.SecurityUtil;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.LocalDateTime.now;

/**
 * 익명 게시판 권한/인증 명세 반영:
 * - 생성 시: 선택적 PIN(bcrypt) 저장 + 소유 토큰(원본 생성/해시 저장) 발급
 * - 수정/삭제 시: 쿠키 토큰 우선 검증 → 실패/부재 시 PIN 검증(+레이트리밋)
 * - 비교는 상수시간 비교 사용, 로그에 원문 미기록(서비스 레벨에서 원문 반환/로깅 금지)
 *
 * 컨트롤러에서는 생성 응답의 tokenPlain을 HttpOnly+Secure 쿠키로 내려주세요.
 * (예: Set-Cookie: post_owner_{id}={tokenPlain})
 */
@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;

    // BCrypt for PIN
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 간단 레이트 리밋(메모리): (postId + remoteAddr) 기준 10분 내 5회 실패 시도 차단
    private final ConcurrentHashMap<String, int[]> pinTrials = new ConcurrentHashMap<>();
    private static final long BLOCK_WINDOW_MS = 10 * 60 * 1000L;

    /**
     * 글 생성
     * - PIN이 있으면 bcrypt로 해시 저장
     * - 소유 토큰 원본 생성 → DB에는 SHA-256 해시만 저장
     * - 반환 DTO에 tokenPlain 포함(컨트롤러에서 쿠키 발급에 사용)
     */
    @Transactional
    public PostCreateResponse create(PostCreateRequest req) {
        Post post = Post.builder()
                .category(req.category())
                .name(req.name())
                .address(req.address())
                .content(req.content())
                .imgUrl(req.imgUrl())
                .likeCount(0)
                .createdAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime())
                .build();

        // 선택적 PIN
        if (req.pin() != null && !req.pin().isBlank()) {
            post.setPinPost(passwordEncoder.encode(req.pin()));
        }
        else{
            post.setPinPost(passwordEncoder.encode("000000"));
        }

        // 소유 토큰: 원본 생성 → 해시 저장
        String tokenPlain = SecurityUtil.generateTokenBase64Url();
        post.setTokenHashPost(SecurityUtil.sha256(tokenPlain));

        Long id = postRepository.save(post).getId();

        // 컨트롤러에서 tokenPlain을 HttpOnly+Secure 쿠키로 내려주세요.
        return new PostCreateResponse(id, tokenPlain);
    }

    /**
     * 글 수정
     * - 동일 브라우저: 쿠키 토큰 일치 시 바로 통과
     * - 다른 브라우저/쿠키 없음: PIN 헤더(X-PIN)로 검증(레이트 리밋 적용)
     * - imgUrl이 빈 값이면 기존 유지 로직은 엔티티의 p.update(...) 내부에서 처리
     */
    @Transactional
    public void update(Long id, PostUpdateRequest req, HttpServletRequest httpReq, String pinHeader) {
        Post p = postRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Post not found"));
        if (!isOwner(p, httpReq, pinHeader)) throw new ForbiddenException("NOT_OWNER");
        p.update(req.category(), req.name(), req.address(), req.content(), req.imgUrl());
        // JPA dirty checking 자동 flush
    }

    /**
     * 글 삭제
     * - isOwner 동일 절차
     * - DB/FK에 ON DELETE CASCADE 또는 @OnDelete 설정 시 댓글 자동 정리
     */
    @Transactional
    public void delete(Long id, HttpServletRequest httpReq, String pinHeader) {
        Post p = postRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Post not found"));
        if (!isOwner(p, httpReq, pinHeader)) throw new ForbiddenException("NOT_OWNER");
        postRepository.delete(p);
    }

    /**
     * 좋아요 증가
     * - 이미 눌렀으면 증가 없음 (프론트 쿠키/스토리지로 alreadyLiked 전달받는 전제)
     * - 증가 후 최신 값 반환
     */
    @Transactional
    public LikeResponseDto like(Long postId, boolean alreadyLiked) {
        if (alreadyLiked) {
            Post p = postRepository.findById(postId)
                    .orElseThrow(() -> new NoSuchElementException("Post not found"));
            return new LikeResponseDto(true, p.getLikeCount());
        }
        int updated = postRepository.incrementLike(postId);
        if (updated == 0) throw new NoSuchElementException("Post not found");
        int now = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"))
                .getLikeCount();
        return new LikeResponseDto(true, now);
    }

    /**
     * 소유자 검증
     * 1) 쿠키 토큰 우선(상수시간 비교)
     * 2) 실패 시 PIN 검증(레이트 리밋 적용)
     *    - 글이 PIN 미설정이면 쿠키 없이는 권한 없음
     */
    private boolean isOwner(Post post, HttpServletRequest req, String pinCandidate) {
        // 1) 쿠키 토큰 우선
        String cookieName = "post_owner_" + post.getId();
        String cookieToken = readCookie(req, cookieName);
        if (cookieToken != null) {
            byte[] given = SecurityUtil.sha256(cookieToken);
            byte[] stored = post.getTokenHashPost();
            if (SecurityUtil.constantTimeEquals(given, stored)) {
                return true;
            }
        }

        // 2) PIN 검증
        String storedHash = post.getPinPost();
        if (storedHash == null || storedHash.isBlank()) {
            // PIN 미설정 글은 쿠키 없으면 권한 부여 불가
            return false;
        }

        // 레이트 리밋: 10분 5회
        String ip = req.getRemoteAddr() == null ? "" : req.getRemoteAddr();
        String key = "post:" + post.getId() + ":" + ip;
        int[] slot = pinTrials.computeIfAbsent(key, k -> new int[]{0, (int) (System.currentTimeMillis() / 1000)});
        long now = System.currentTimeMillis();
        long startSec = slot[1];
        if ((now / 1000 - startSec) > (BLOCK_WINDOW_MS / 1000)) {
            // 윈도우 갱신
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
