//package syu.likealion3.hackathon.repository;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import syu.likealion3.hackathon.support.IntegrationTest;
//import syu.likealion3.hackathon.entity.Category;
//import syu.likealion3.hackathon.entity.Post;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class PostRepositoryOnDb1Test extends IntegrationTest {
//
//    @Autowired PostRepository postRepository;
//
//    private Post newPost() {
//        return Post.builder()
//                .category(Category.ETC)
//                .name("name")
//                .address("addr")
//                .content("content")
//                .imgUrl("img")
//                .likeCount(0)
//                .build();
//    }
//
//    @Test @DisplayName("CREATE & READ on db1 (rollback)")
//    void create_and_read() {
//        Post saved = postRepository.save(newPost());
//        Optional<Post> found = postRepository.findById(saved.getId());
//        assertThat(found).isPresent();
//        assertThat(found.get().getName()).isEqualTo("name");
//    }
//
//    @Test @DisplayName("UPDATE: 좋아요 1 증가 (rollback)")
//    void update_like() {
//        Post saved = postRepository.save(newPost());
//        int rows = postRepository.incrementLike(saved.getId());
//        assertThat(rows).isEqualTo(1);
//        Post after = postRepository.findById(saved.getId()).orElseThrow();
//        assertThat(after.getLikeCount()).isEqualTo(1);
//    }
//
//    @Test @DisplayName("DELETE (rollback)")
//    void delete_post() {
//        Post saved = postRepository.save(newPost());
//        postRepository.deleteById(saved.getId());
//        assertThat(postRepository.findById(saved.getId())).isEmpty();
//    }
//}
