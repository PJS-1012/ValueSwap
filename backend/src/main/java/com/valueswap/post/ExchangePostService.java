package com.valueswap.post;

import com.valueswap.common.exception.ApiException;
import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.PostStatus;
import com.valueswap.post.dto.PostCreateRequest;
import com.valueswap.post.dto.PostResponse;
import com.valueswap.post.dto.PostSummaryResponse;
import com.valueswap.user.User;
import com.valueswap.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExchangePostService {
    private final ExchangePostRepository postRepository;
    private final UserRepository userRepository;

    public ExchangePostService(ExchangePostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PostResponse create(Long userId, PostCreateRequest request) {
        User user = findUser(userId);
        ExchangePost post = ExchangePost.create(user, request.title().trim(),
                request.description().trim(), request.region().trim());
        applyItems(post, request);
        return PostResponse.from(postRepository.save(post));
    }

    public List<PostSummaryResponse> findActivePosts() {
        return postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.ACTIVE).stream()
                .map(PostSummaryResponse::from)
                .toList();
    }

    public PostResponse findById(Long postId) {
        ExchangePost post = findPost(postId);
        if (post.getStatus() == PostStatus.HIDDEN) {
            throw notFound();
        }
        return PostResponse.from(post);
    }

    public List<PostSummaryResponse> findMine(Long userId) {
        return postRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PostSummaryResponse::from)
                .toList();
    }

    @Transactional
    public PostResponse update(Long userId, Long postId, PostCreateRequest request) {
        ExchangePost post = findPost(postId);
        verifyOwner(post, userId);
        verifyEditable(post);
        post.replaceDetails(request.title().trim(), request.description().trim(), request.region().trim(),
                request.provideItems().stream().map(item -> item.toEntity()).toList(),
                request.wantItems().stream().map(item -> item.toEntity()).toList());
        return PostResponse.from(post);
    }

    @Transactional
    public void cancel(Long userId, Long postId) {
        ExchangePost post = findPost(postId);
        verifyOwner(post, userId);
        verifyEditable(post);
        post.cancel();
    }

    private void applyItems(ExchangePost post, PostCreateRequest request) {
        request.provideItems().stream().map(item -> item.toEntity()).forEach(post::addProvideItem);
        request.wantItems().stream().map(item -> item.toEntity()).forEach(post::addWantItem);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
    }

    private ExchangePost findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(this::notFound);
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "교환 글을 찾을 수 없습니다.");
    }

    private void verifyOwner(ExchangePost post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "POST_FORBIDDEN", "본인 소유의 교환 글만 변경할 수 있습니다.");
        }
    }

    private void verifyEditable(ExchangePost post) {
        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "POST_NOT_EDITABLE", "진행 중이거나 종료된 교환 글은 변경할 수 없습니다.");
        }
    }
}
