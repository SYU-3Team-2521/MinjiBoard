package syu.likealion3.hackathon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import syu.likealion3.hackathon.dto.PostResponseDto;
import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.service.PostCommandService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostCommandController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 비활성화
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
class PostCommandControllerTest {

    @Resource MockMvc mvc;
    @Resource ObjectMapper om;

    @MockitoBean PostCommandService commandService;

    @Test
    @DisplayName("POST /posts 생성 200 OK + JSON 본문 검증")
    void createOk() throws Exception {
        // given
        given(commandService.create(any()))
                .willReturn(Fx.response(1L));

        String body = """
        {
          "title": "맛집",
          "content": "비빔면",
          "category": "FOOD",
          "storeName": "삼거리",
          "storeAddress": "서울 어딘가"
        }
        """;

        // when & then
        mvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /posts/{id} 204 No Content")
    void deleteNoContent() throws Exception {
        mvc.perform(delete("/posts/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    // --- Test Fixtures ---
    static class Fx {
        static PostResponseDto response(Long id) {
            Post post = new Post(
                    id,
                    "t",
                    "c",
                    Category.FOOD,
                    null,
                    "가게",
                    "주소",
                    0
            );
            return new PostResponseDto(post);
        }
    }
}