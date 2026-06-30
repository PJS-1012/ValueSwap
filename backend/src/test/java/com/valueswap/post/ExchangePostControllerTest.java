package com.valueswap.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valueswap.support.TestPosts;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExchangePostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ownerCreatesPostWithStructuredItems() throws Exception {
        String token = createUserAndLogin("post-owner@test.com", "글작성자");

        mvc.perform(post("/api/posts").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(TestPosts.restaurantJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.region").value("광주"))
                .andExpect(jsonPath("$.provideItems[0].name").value("돈까스 정식 식사권"))
                .andExpect(jsonPath("$.provideItems[0].tags[0]").value("돈까스"))
                .andExpect(jsonPath("$.wantItems[0].name").value("쌀"))
                .andExpect(jsonPath("$.wantItems[0].minValue").value(40000));
    }

    @Test
    void publicCanReadActivePostsAndDetails() throws Exception {
        String token = createUserAndLogin("public-post@test.com", "공개글");
        long postId = createPost(token);

        mvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(postId));

        mvc.perform(get("/api/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author.nickname").value("공개글"))
                .andExpect(jsonPath("$.provideItems[0].estimatedValue").value(50000));
    }

    @Test
    void ownerCanUpdateAndCancelPost() throws Exception {
        String token = createUserAndLogin("edit-owner@test.com", "수정작성자");
        long postId = createPost(token);

        mvc.perform(put("/api/posts/{postId}", postId).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(TestPosts.updatedJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 교환 글"));

        mvc.perform(delete("/api/posts/{postId}", postId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/posts/my").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELED"));
    }

    @Test
    void anotherUserCannotUpdateOrCancelPost() throws Exception {
        String ownerToken = createUserAndLogin("real-owner@test.com", "진짜작성자");
        String otherToken = createUserAndLogin("other-user@test.com", "다른사용자");
        long postId = createPost(ownerToken);

        mvc.perform(put("/api/posts/{postId}", postId).header("Authorization", bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON).content(TestPosts.updatedJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST_FORBIDDEN"));

        mvc.perform(delete("/api/posts/{postId}", postId).header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void inExchangePostCannotBeUpdated() throws Exception {
        String token = createUserAndLogin("locked-post@test.com", "진행중작성자");
        long postId = createPost(token);
        jdbcTemplate.update("UPDATE exchange_posts SET status = 'IN_EXCHANGE' WHERE id = ?", postId);
        entityManager.clear();

        mvc.perform(put("/api/posts/{postId}", postId).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(TestPosts.updatedJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POST_NOT_EDITABLE"));
    }

    @Test
    void invalidWantedValueRangeReturnsBadRequest() throws Exception {
        String token = createUserAndLogin("invalid-range@test.com", "범위검증");

        mvc.perform(post("/api/posts").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(TestPosts.invalidValueRangeJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void myPostsWithoutTokenReturnsUnauthorized() throws Exception {
        mvc.perform(get("/api/posts/my"))
                .andExpect(status().isUnauthorized());
    }

    private long createPost(String token) throws Exception {
        String response = mvc.perform(post("/api/posts").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(TestPosts.restaurantJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String createUserAndLogin(String email, String nickname) throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password1!","name":"사용자","nickname":"%s"}
                                """.formatted(email, nickname)))
                .andExpect(status().isCreated());

        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password1!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
