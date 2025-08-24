//package syu.likealion3.hackathon.controller;
//
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class CommentControllerIT {
//
//    @Autowired
//    MockMvc mvc;
//
//    Long postId;
//
//    @BeforeEach
//    void setup() throws Exception {
//        // 게시글 생성
//        MvcResult res = mvc.perform(multipart("/api/posts")
//                .param("category", "MARKET")
//                .param("name", "댓글용 포스트")
//                .param("address", "서울")
//                .param("content", "본문"))
//            .andExpect(status().isCreated()).andReturn();
//        String location = res.getResponse().getHeader("Location");
//        postId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
//    }
//
//    @Test
//    @DisplayName("POST /api/posts/{postId}/comments - 생성 201")
//    void create_comment_201() throws Exception {
//        String body = "{\"content\":\"첫 코멘트\"}";
//        mvc.perform(post("/api/posts/{postId}/comments", postId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//            .andExpect(status().isCreated())
//            .andExpect(header().exists("Location"));
//    }
//
//    @Test
//    @DisplayName("GET /api/posts/{postId}/comments - 목록 200")
//    void list_comments_200() throws Exception {
//        mvc.perform(get("/api/posts/{postId}/comments", postId)
//                .param("page","0").param("size","10").param("sort","createdAt,desc"))
//            .andExpect(status().isOk())
//            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
//    }
//}
