package com.valueswap.auth;

import com.valueswap.user.UserRole;

public record AuthenticatedUser(Long userId, UserRole role) {
}
