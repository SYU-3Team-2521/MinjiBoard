package syu.likealion3.hackathon.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import syu.likealion3.hackathon.dto.LikeResponseDto;
import syu.likealion3.hackathon.dto.PostCreateRequest;
import syu.likealion3.hackathon.dto.PostCreateResponse;
import syu.likealion3.hackathon.exception.DuplicateLikeException;
import syu.likealion3.hackathon.service.PostCommandService;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostCommandController {

    private final PostCommandService postCommandService;

    // 환경별 쿠키 설정
    @Value("${app.cookies.secure:true}")
    private boolean cookieSecure;
    @Value("${app.cookies.same-site:Lax}")
    private String cookieSameSite;
    @Value("${app.cookies.max-age-days:30}")
    private int cookieMaxAgeDays;

    @PostMapping
    public ResponseEntity<PostCreateResponse> create(@Valid @RequestBody PostCreateRequest req) {
        PostCreateResponse resp = postCommandService.create(req);
        return ResponseEntity.created(URI.create("/api/posts/" + resp.id())).body(resp);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponseDto> like(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        // 1) 쿠키 읽어서 중복 여부 판단
        Set<Long> liked = readLikedCookie(request);
        boolean already = liked.contains(id);
        if (already) {
            // ★ 요구 메시지로 에러 처리
            throw new DuplicateLikeException("이미 좋아요를 누르셨습니다. (게시물 당 좋아요 1개까지 가능합니다)");
        }

        // 2) 서비스 호출(+1)
        LikeResponseDto resp = postCommandService.like(id, false);

        // 3) 쿠키 갱신 (id 추가)
        liked.add(id);
        String newVal = String.join(",", liked.stream().map(String::valueOf).toList());
        ResponseCookie cookie = ResponseCookie.from("likedPosts", newVal)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(Duration.ofDays(cookieMaxAgeDays))
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(resp);
    }

    private Set<Long> readLikedCookie(HttpServletRequest request) {
        Set<Long> set = new LinkedHashSet<>();
        if (request.getCookies() == null) return set;
        for (Cookie c : request.getCookies()) {
            if (!"likedPosts".equals(c.getName())) continue;
            String val = c.getValue();
            if (val == null || val.isBlank()) return set;
            for (String token : val.split(",")) {
                try { if (!token.isBlank()) set.add(Long.parseLong(token.trim())); }
                catch (NumberFormatException ignored) {}
            }
        }
        return set;
    }
}
