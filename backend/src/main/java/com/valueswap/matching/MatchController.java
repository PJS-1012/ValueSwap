package com.valueswap.matching;

import com.valueswap.auth.AuthenticatedUser;
import com.valueswap.matching.dto.MatchDetailResponse;
import com.valueswap.matching.dto.MatchRunResponse;
import com.valueswap.matching.dto.MatchSummaryResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/run")
    public MatchRunResponse run() {
        return matchingService.runMatching();
    }

    @GetMapping("/my")
    public List<MatchSummaryResponse> findMine(@AuthenticationPrincipal AuthenticatedUser user) {
        return matchingService.findMine(user.userId());
    }

    @GetMapping("/{matchId}")
    public MatchDetailResponse findOne(@AuthenticationPrincipal AuthenticatedUser user,
                                       @PathVariable Long matchId) {
        return matchingService.findOne(user.userId(), matchId);
    }

    @PostMapping("/{matchId}/accept")
    public MatchDetailResponse accept(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long matchId) {
        return matchingService.accept(matchId, user.userId());
    }

    @PostMapping("/{matchId}/reject")
    public MatchDetailResponse reject(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long matchId) {
        return matchingService.reject(matchId, user.userId());
    }
}
