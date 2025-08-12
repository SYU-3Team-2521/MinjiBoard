package syu.likealion3.hackathon.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import syu.likealion3.hackathon.dto.PostCreateRequest;
import syu.likealion3.hackathon.dto.PostResponseDto;
import syu.likealion3.hackathon.dto.PostUpdateRequest;
import syu.likealion3.hackathon.service.PostCommandService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostCommandController {

    private final PostCommandService postCommandService;

    /** 생성 */
    @PostMapping
    public ResponseEntity<PostResponseDto> create(@Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.ok(postCommandService.create(request));
    }

    /** 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> update(@PathVariable Long id,
                                                  @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(postCommandService.update(id, request));
    }

    /** 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** 좋아요 증가 */
    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponseDto> like(@PathVariable Long id) {
        return ResponseEntity.ok(postCommandService.like(id));
    }

    /** 좋아요 취소 */
    @PostMapping("/{id}/unlike")
    public ResponseEntity<PostResponseDto> unlike(@PathVariable Long id) {
        return ResponseEntity.ok(postCommandService.unlike(id));
    }
}
