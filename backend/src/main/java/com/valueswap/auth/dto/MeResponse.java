package com.valueswap.auth.dto;

import com.valueswap.user.User;
import com.valueswap.user.UserRole;

import java.math.BigDecimal;

public record MeResponse(Long id, String email, String name, String nickname,
                         UserRole role, BigDecimal trustScore) {
    public static MeResponse from(User user) {
        return new MeResponse(user.getId(), user.getEmail(), user.getName(), user.getNickname(),
                user.getRole(), user.getTrustScore());
    }
}
