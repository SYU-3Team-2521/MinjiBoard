package syu.likealion3.hackathon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import syu.likealion3.hackathon.dto.PostListResponseDto;
import syu.likealion3.hackathon.entity.Category;
import syu.likealion3.hackathon.entity.Post;
import syu.likealion3.hackathon.service.PostQueryService;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostQueryController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터가 있다면 테스트에서 비활성화
class PostQueryControllerTest {

    @Resource MockMvc mvc;
    @MockitoBean PostQueryService queryService;
    @Resource ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // LocalDateTime 직렬화 문제 예방(자바타임 모듈 자동 등록)
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("GET /posts?category=FOOD&sort=likes")
    void listByCategoryLikes() throws Exception {
        given(queryService.getPostsByCategoryOrderByLikes(Category.FOOD))
                .willReturn(List.of(new PostListResponseDto(
                        new Post(1L, "t", "c", Category.FOOD, null, "가게", "주소", 3))));

        mvc.perform(get("/posts")
                        .param("category", "FOOD")
                        .param("sort", "likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value(Category.FOOD.getDisplayName()));
    }

    @Test
    @DisplayName("GET /posts/{id} 상세")
    void getDetail() throws Exception {
        given(queryService.getPostById(1L))
                .willReturn(Optional.of(new Post(1L, "t", "c", Category.MARKET, null, "가게", "주소", 0)));

        mvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
