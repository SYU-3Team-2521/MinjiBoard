package syu.likealion3.hackathon.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import syu.likealion3.hackathon.support.IntegrationTest;
import syu.likealion3.hackathon.dto.LikeResponseDto;
import syu.likealion3.hackathon.dto.PostCreateRequest;
import syu.likealion3.hackathon.dto.PostCreateResponse;
import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.repository.PostRepository;

import static org.assertj.core.api.Assertions.assertThat;

class PostCommandServiceOnDb1Test extends IntegrationTest {

    @Autowired PostCommandService postCommandService;
    @Autowired PostRepository postRepository; // flush용 주입

    @Test
    @DisplayName("게시글 생성 → 좋아요 1회 (rollback)")
    void create_and_like() {
        PostCreateResponse created = postCommandService.create(
                new PostCreateRequest(Category.ETC, "n", "a", "c", "img")
        );

        postRepository.flush(); // 저장 즉시 반영

        LikeResponseDto liked = postCommandService.like(created.id(), false);
        assertThat(liked.likeCount()).isEqualTo(1);
    }
}
