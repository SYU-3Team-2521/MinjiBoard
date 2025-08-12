package syu.likealion3.hackathon.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import syu.likealion3.hackathon.dto.PostCreateRequest;
import syu.likealion3.hackathon.dto.PostUpdateRequest;
import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.repository.PostRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

class PostCommandServiceTest {

    private final PostRepository postRepository = Mockito.mock(PostRepository.class);
    private final PostCommandService service = new PostCommandService(postRepository);

    private Post post(Long id, int likes) {
        return Post.builder()
                .id(id)
                .title("t")
                .content("c")
                .category(Category.FOOD)
                .storeName("가게")
                .storeAddress("주소")
                .likeCount(likes)
                .build();
    }

    @Test
    @DisplayName("생성 성공")
    void create() {
        PostCreateRequest req = new PostCreateRequest();
        // 리플렉션 없이 세터가 없다면 빌더 형태로 테스트 대역 생성 → 캡처로 검증
        // 간단히 스텁만: 저장 결과 리턴
        given(postRepository.save(any(Post.class))).willAnswer(inv -> inv.getArgument(0));

        var dto = service.create(new PostCreateRequest() {{
            // 테스트 편의용으로 필드 접근이 어렵다면 실제 코드에 생성자 추가해도 좋음
        }});
        // 위와 같이 빈 객체면 검증이 어렵다 → 아래 방식으로 캡처
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isNotNull(); // 실제론 생성자/세터 추가 권장
    }

    @Test
    @DisplayName("좋아요 / 취소")
    void likeUnlike() {
        Post p = post(1L, 3);
        given(postRepository.findById(1L)).willReturn(Optional.of(p));
        given(postRepository.save(any(Post.class))).willAnswer(inv -> inv.getArgument(0));

        var liked = service.like(1L);
        assertThat(liked.getLikeCount()).isEqualTo(4);

        var unliked = service.unlike(1L);
        assertThat(unliked.getLikeCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("수정은 기존 좋아요 유지")
    void updateKeepsLikeCount() {
        Post existing = post(10L, 9);
        given(postRepository.findById(10L)).willReturn(Optional.of(existing));
        given(postRepository.save(any(Post.class))).willAnswer(inv -> inv.getArgument(0));

        PostUpdateRequest req = new PostUpdateRequest();
        var dto = service.update(10L, req);

        assertThat(dto.getLikeCount()).isEqualTo(9);
        verify(postRepository).save(any(Post.class));
    }
}
