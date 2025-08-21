package syu.likealion3.hackathon.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import syu.likealion3.hackathon.dto.*;
import syu.likealion3.hackathon.exception.DuplicateLikeException;
import syu.likealion3.hackathon.service.FileStorageService;
import syu.likealion3.hackathon.service.PostCommandService;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostCommandController {

    private final PostCommandService postCommandService;
    private final FileStorageService fileStorageService;

    @Value("${app.cookies.secure:true}")
    private boolean cookieSecure;
    @Value("${app.cookies.same-site:Lax}")
    private String cookieSameSite;
    @Value("${app.cookies.max-age-days:30}")
    private int cookieMaxAgeDays;

    /** ✅ 멀티파트(파일 첨부)만 허용 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostCreateResponse> create(
            @Valid @ModelAttribute PostCreateForm form,
            @RequestPart(name = "image", required = false) MultipartFile image // 이미지 필수로 바꾸려면 required=true
    ) throws Exception {
        String imgUrl = (image != null && !image.isEmpty())
                ? fileStorageService.saveImage(image)
                : null;

        PostCreateRequest req = new PostCreateRequest(
                form.category(), form.name(), form.address(), form.content(), imgUrl
        );
        PostCreateResponse resp = postCommandService.create(req);
        return ResponseEntity.created(URI.create("/api/posts/" + resp.id())).body(resp);
    }

    /** 좋아요 (쿠키로 중복 방지, RFC6265-safe 직렬화) */
    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponseDto> like(@PathVariable Long id, HttpServletRequest request) {
        Set<Long> liked = readLikedCookie(request);
        if (liked.contains(id)) {
            throw new DuplicateLikeException("이미 좋아요를 누르셨습니다. (게시물 당 좋아요 1개까지 가능합니다)");
        }

        LikeResponseDto resp = postCommandService.like(id, false);

        liked.add(id);
        String newVal = liked.stream().map(String::valueOf).collect(Collectors.joining(":"));

        ResponseCookie cookie = ResponseCookie.from("likedPosts", newVal)
                .httpOnly(true).secure(cookieSecure).sameSite(cookieSameSite)
                .maxAge(Duration.ofDays(cookieMaxAgeDays)).path("/").build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(resp);
    }

    private Set<Long> readLikedCookie(HttpServletRequest request) {
        Set<Long> set = new LinkedHashSet<>();
        if (request.getCookies() == null) return set;
        for (Cookie c : request.getCookies()) {
            if (!"likedPosts".equals(c.getName())) continue;
            String val = c.getValue();
            if (val == null || val.isBlank()) return set;
            for (String token : val.split("[:,]")) {
                try { if (!token.isBlank()) set.add(Long.parseLong(token.trim())); } catch (NumberFormatException ignored) {}
            }
        }
        return set;
    }
}
