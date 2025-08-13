package syu.likealion3.hackathon.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import syu.likealion3.hackathon.dto.LikeResponseDto;
import syu.likealion3.hackathon.dto.PostCreateRequest;
import syu.likealion3.hackathon.dto.PostCreateResponse;
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

    @PostMapping
    public ResponseEntity<PostCreateResponse> create(@RequestBody @jakarta.validation.Valid PostCreateRequest req) {
        PostCreateResponse resp = postCommandService.create(req);
        return ResponseEntity.created(URI.create("/api/posts/" + resp.id())).body(resp);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponseDto> like(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        // 1) 쿠키에서 이미 눌렀는지 확인
        Set<Long> liked = readLikedCookie(request);
        boolean already = liked.contains(id);

        // 2) 서비스 호출 (이미면 증가 안 함)
        LikeResponseDto resp = postCommandService.like(id, already);

        // 3) 쿠키 갱신 (이미 눌렀으면 그대로, 아니면 id 추가)
        if (!already) liked.add(id);
        String newVal = String.join(",", liked.stream().map(String::valueOf).toList());

        ResponseCookie cookie = ResponseCookie.from("likedPosts", newVal)
                .httpOnly(true)
                .secure(false)          // localhost http에서 쿠키가 저장 안 되면 false로 바꿔 테스트하세요
                .sameSite("Lax")
                .maxAge(Duration.ofDays(30))
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
                try {
                    if (!token.isBlank()) set.add(Long.parseLong(token.trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return set;
    }
}
