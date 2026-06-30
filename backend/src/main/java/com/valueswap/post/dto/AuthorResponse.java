package com.valueswap.post.dto;

import com.valueswap.user.User;

import java.math.BigDecimal;

public record AuthorResponse(Long id, String nickname, BigDecimal trustScore) {
    public static AuthorResponse from(User user) {
        return new AuthorResponse(user.getId(), user.getNickname(), user.getTrustScore());
    }
}
