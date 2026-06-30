package com.valueswap.post;

import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangePostRepository extends JpaRepository<ExchangePost, Long> {
    List<ExchangePost> findAllByStatusOrderByCreatedAtDesc(PostStatus status);
    List<ExchangePost> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
