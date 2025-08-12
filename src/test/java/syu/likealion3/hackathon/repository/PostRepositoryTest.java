package syu.likealion3.hackathon.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.entity.Post;

import jakarta.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class AuditingConfig { }

    private final PostRepository postRepository;
    private final EntityManager em;

    PostRepositoryTest(PostRepository postRepository, EntityManager em) {
        this.postRepository = postRepository;
        this.em = em;
    }

    private Post make(String title, Category c, int likes) {
        return Post.builder()
                .title(title)
                .content("content")
                .category(c)
                .imageUrl(null)
                .storeName("가게")
                .storeAddress("주소")
                .likeCount(likes)
                .build();
    }

    @Test
    @DisplayName("카테고리별 조회 및 좋아요순 정렬")
    void findByCategoryOrderByLikeCountDesc() {
        postRepository.save(make("a", Category.FOOD, 3));
        postRepository.save(make("b", Category.FOOD, 10));
        postRepository.save(make("c", Category.MARKET, 7));
        em.flush();
        em.clear(); // 선택(캐시 영향 제거)

        List<Post> foods = postRepository.findByCategoryOrderByLikeCountDesc(Category.FOOD);

        assertThat(foods).hasSize(2);
        assertThat(foods.get(0).getLikeCount()).isGreaterThanOrEqualTo(foods.get(1).getLikeCount());
        assertThat(foods.get(0).getTitle()).isEqualTo("b");
    }

    @Test
    @DisplayName("가게명 부분 검색")
    void findByStoreNameContaining() {
        // toBuilder() 없이 직접 build
        postRepository.save(Post.builder()
                .title("x").content("content").category(Category.ETC)
                .storeName("삼거리분식").storeAddress("주소").likeCount(0).build());
        postRepository.save(Post.builder()
                .title("y").content("content").category(Category.ETC)
                .storeName("사거리치킨").storeAddress("주소").likeCount(0).build());

        List<Post> result = postRepository.findByStoreNameContaining("거리");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("좋아요 최소 수 이상 인기글")
    void findPostsWithMinimumLikes() {
        postRepository.save(make("l1", Category.FOOD, 1));
        postRepository.save(make("l2", Category.FOOD, 5));
        postRepository.save(make("l3", Category.FOOD, 7));

        List<Post> result = postRepository.findPostsWithMinimumLikes(5);

        // extracting → Object 타입 이슈 해결: 캐스팅 명시
        assertThat(result).extracting(Post::getLikeCount)
                .allMatch((Integer c) -> c >= 5);
        assertThat(result.get(0).getLikeCount())
                .isGreaterThanOrEqualTo(result.get(1).getLikeCount());
    }
}
