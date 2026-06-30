package com.valueswap.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valueswap.user.User;
import com.valueswap.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void ownerCanListAndReadNotification() throws Exception {
        User user = userRepository.save(User.create("notify@test.com", passwordEncoder.encode("Password1!"),
                "알림", "알림사용자"));
        Notification notification = notificationRepository.save(Notification.create(user, "새 매칭",
                "3자 교환 가능성이 발견되었습니다.", NotificationType.MATCH_FOUND));
        String token = login("notify@test.com");

        mvc.perform(get("/api/notifications").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notification.getId()))
                .andExpect(jsonPath("$[0].read").value(false));

        mvc.perform(patch("/api/notifications/{id}/read", notification.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void anotherUserCannotReadNotification() throws Exception {
        User owner = userRepository.save(User.create("notify-owner@test.com", "encoded", "소유자", "소유자"));
        Notification notification = notificationRepository.save(Notification.create(owner, "새 매칭", "내용",
                NotificationType.MATCH_FOUND));
        userRepository.save(User.create("notify-other@test.com", passwordEncoder.encode("Password1!"),
                "다른", "다른사용자"));
        String otherToken = login("notify-other@test.com");

        mvc.perform(patch("/api/notifications/{id}/read", notification.getId())
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOTIFICATION_FORBIDDEN"));
    }

    private String login(String email) throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Password1!"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
