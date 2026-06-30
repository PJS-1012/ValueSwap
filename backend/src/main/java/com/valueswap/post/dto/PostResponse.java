package com.valueswap.post.dto;

import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.PostStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(Long id, String title, String description, PostStatus status,
                           String region, AuthorResponse author,
                           List<ProvideItemResponse> provideItems,
                           List<WantItemResponse> wantItems,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static PostResponse from(ExchangePost post) {
        return new PostResponse(post.getId(), post.getTitle(), post.getDescription(), post.getStatus(),
                post.getRegion(), AuthorResponse.from(post.getUser()),
                post.getProvideItems().stream().map(ProvideItemResponse::from).toList(),
                post.getWantItems().stream().map(WantItemResponse::from).toList(),
                post.getCreatedAt(), post.getUpdatedAt());
    }
}
