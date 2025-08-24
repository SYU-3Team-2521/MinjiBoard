//package syu.likealion3.hackathon.repository;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import syu.likealion3.hackathon.support.IntegrationTest;
//import syu.likealion3.hackathon.entity.Category;
//import syu.likealion3.hackathon.entity.Comment;
//import syu.likealion3.hackathon.entity.Post;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class CommentRepositoryOnDb1Test extends IntegrationTest {
//
//    @Autowired CommentRepository commentRepository;
//    @Autowired PostRepository postRepository;
//
//    private Post post() {
//        return postRepository.save(
//                Post.builder()
//                        .category(Category.FOOD_CAFE_CONVENIENCE) // 실제 enum에 맞춰 조정
//                        .name("n")
//                        .address("a")
//                        .content("c")
//                        .imgUrl("img")
//                        .likeCount(0)
//                        .build()
//        );
//    }
//
//    @Test @DisplayName("댓글 CREATE & READ on db1 (rollback)")
//    void create_read_comment() {
//        Post parent = post();
//
//        Comment c = Comment.builder()
//                .post(parent)
//                .content("nice")
//                .build();
//
//        Comment saved = commentRepository.save(c);
//        Comment found = commentRepository.findById(saved.getId()).orElseThrow();
//
//        assertThat(found.getPost().getId()).isEqualTo(parent.getId());
//        assertThat(found.getContent()).isEqualTo("nice");
//    }
//}
