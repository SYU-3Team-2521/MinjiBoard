// src/main/java/syu/likealion3/hackathon/controller/PostQueryController.java
package syu.likealion3.hackathon.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import syu.likealion3.hackathon.dto.PageResponse;
import syu.likealion3.hackathon.dto.PostDetailDto;
import syu.likealion3.hackathon.dto.PostListItemDto;
import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.service.PostQueryService;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostQueryController {

    private final PostQueryService postQueryService;

    @GetMapping
    public ResponseEntity<PageResponse<PostListItemDto>> list(
            @RequestParam(name = "category", required = false) Category category,
            @PageableDefault(page = 0, size = 9, sort = "likeCount", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        PageResponse<PostListItemDto> resp = postQueryService.getPosts(category, pageable);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailDto> detail(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Set<Long> likedIds = parseLikedCookie(request);
        PostDetailDto resp = postQueryService.getPostDetail(id, likedIds);
        return ResponseEntity.ok(resp);
    }

    /**
     * likedPosts 쿠키 파싱
     * - 구분자: 쉼표(,) 또는 콜론(:) 모두 허용
     *   예) "1,2,3" 또는 "1:2:3"
     */
    private Set<Long> parseLikedCookie(HttpServletRequest request) {
        Set<Long> set = new HashSet<>();
        if (request.getCookies() == null) return set;
        for (Cookie c : request.getCookies()) {
            if (!"likedPosts".equals(c.getName())) continue;
            String val = c.getValue();
            if (val == null || val.isBlank()) return set;
            for (String token : val.split("[:,]")) {
                try {
                    if (!token.isBlank()) set.add(Long.parseLong(token.trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return set;
    }
}
