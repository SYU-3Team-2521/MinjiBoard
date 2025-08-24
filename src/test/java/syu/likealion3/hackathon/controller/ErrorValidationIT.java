//package syu.likealion3.hackathon.controller;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class ErrorValidationIT {
//
//    @Autowired
//    MockMvc mvc;
//
//    @Test
//    @DisplayName("존재하지 않는 리소스 → 404")
//    void not_found_returns_404() throws Exception {
//        mvc.perform(get("/api/posts/{id}", 999999L))
//            .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("잘못된 본문 → 4xx (구현에 따라 400/422)")
//    void invalid_body_4xx() throws Exception {
//        // 빈 필드 등 제약 위반을 유도
//        String bad = "{\"name\":\"\",\"address\":\"\",\"content\":\"\"}";
//        mvc.perform(put("/api/posts/{id}", 0L)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(bad))
//            .andExpect(status().is4xxClientError());
//    }
//}
