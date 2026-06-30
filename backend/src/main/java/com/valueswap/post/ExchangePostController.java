package com.valueswap.post;

import com.valueswap.auth.AuthenticatedUser;
import com.valueswap.post.dto.PostCreateRequest;
import com.valueswap.post.dto.PostResponse;
import com.valueswap.post.dto.PostSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class ExchangePostController {
    private final ExchangePostService postService;

    public ExchangePostController(ExchangePostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                               @Valid @RequestBody PostCreateRequest request) {
        return postService.create(user.userId(), request);
    }

    @GetMapping
    public List<PostSummaryResponse> findAll() {
        return postService.findActivePosts();
    }

    @GetMapping("/{postId}")
    public PostResponse findOne(@PathVariable Long postId) {
        return postService.findById(postId);
    }

    @GetMapping("/my")
    public List<PostSummaryResponse> findMine(@AuthenticationPrincipal AuthenticatedUser user) {
        return postService.findMine(user.userId());
    }

    @PutMapping("/{postId}")
    public PostResponse update(@AuthenticationPrincipal AuthenticatedUser user,
                               @PathVariable Long postId,
                               @Valid @RequestBody PostCreateRequest request) {
        return postService.update(user.userId(), postId, request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long postId) {
        postService.cancel(user.userId(), postId);
    }
}
