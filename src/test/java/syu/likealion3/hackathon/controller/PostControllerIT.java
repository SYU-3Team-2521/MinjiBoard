//package syu.likealion3.hackathon.controller;
//
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class PostControllerIT {
//
//    @Autowired
//    MockMvc mvc;
//
//    Long createdId;
//
//    @BeforeEach
//    void setup() throws Exception {
//        // 멀티파트로 게시글 하나 생성하고 Location에서 id 추출
//        MockMultipartFile image = new MockMultipartFile("image", "a.jpg", "image/jpeg", "fake".getBytes());
//        MvcResult res = mvc.perform(multipart("/api/posts")
//                .file(image)
//                .param("category", "MARKET")
//                .param("name", "통합테스트 가게")
//                .param("address", "서울 어딘가 123")
//                .param("content", "본문"))
//            .andExpect(status().isCreated())
//            .andExpect(header().exists("Location"))
//            .andReturn();
//        String location = res.getResponse().getHeader("Location");
//        assertThat(location).isNotBlank();
//        // Location: /api/posts/{id}
//        String[] segs = location.split("/");
//        createdId = Long.parseLong(segs[segs.length - 1]);
//    }
//
//    @Test
//    @DisplayName("GET /api/posts - 페이징/정렬 정상 200")
//    void list_posts_ok() throws Exception {
//        mvc.perform(get("/api/posts")
//                .param("category", "") // 전체
//                .param("page", "0")
//                .param("size", "9")
//                .param("sort", "likeCount,desc"))
//            .andExpect(status().isOk())
//            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    @DisplayName("PUT /api/posts/{id} - JSON 수정 204")
//    void update_post_json_204() throws Exception {
//        String body = "{\"name\":\"수정된 상호명\",\"address\":\"변경된 주소\",\"content\":\"수정됨\",\"imgUrl\":\"https://ex/b.jpg\"}";
//        mvc.perform(put("/api/posts/{id}", createdId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//            .andExpect(status().isNoContent());
//    }
//
//    @Test
//    @DisplayName("DELETE /api/posts/{id} - 댓글 없이 삭제 204, 이후 GET 404")
//    void delete_post_then_404_on_get() throws Exception {
//        // 새로 하나 더 만들어서 댓글 의존성 제거
//        MockMultipartFile image = new MockMultipartFile("image", "b.jpg", "image/jpeg", "fake".getBytes());
//        MvcResult res = mvc.perform(multipart("/api/posts")
//                .file(image)
//                .param("category", "MARKET")
//                .param("name", "삭제용 포스트")
//                .param("address", "어딘가")
//                .param("content", "삭제 테스트"))
//            .andExpect(status().isCreated()).andReturn();
//        String location = res.getResponse().getHeader("Location");
//        Long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
//
//        mvc.perform(delete("/api/posts/{id}", id))
//            .andExpect(status().isNoContent());
//
//        mvc.perform(get("/api/posts/{id}", id))
//            .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("POST /api/posts/{id}/like - 최초 200, 같은 쿠키로 409")
//    void like_then_duplicate_conflict() throws Exception {
//        MvcResult res1 = mvc.perform(post("/api/posts/{id}/like", createdId))
//            .andExpect(status().isOk()).andReturn();
//
//        // 실제 코드의 쿠키 이름은 likedPosts
//        var cookie = res1.getResponse().getCookie("likedPosts");
//        assertThat(cookie).isNotNull();
//
//        mvc.perform(post("/api/posts/{id}/like", createdId).cookie(cookie))
//            .andExpect(status().isConflict());
//    }
//}
