package com.valueswap.post.dto;

import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.PostStatus;

import java.time.LocalDateTime;

public record PostSummaryResponse(Long id, String title, PostStatus status, String region,
                                  AuthorResponse author, LocalDateTime createdAt) {
    public static PostSummaryResponse from(ExchangePost post) {
        return new PostSummaryResponse(post.getId(), post.getTitle(), post.getStatus(), post.getRegion(),
                AuthorResponse.from(post.getUser()), post.getCreatedAt());
    }
}
