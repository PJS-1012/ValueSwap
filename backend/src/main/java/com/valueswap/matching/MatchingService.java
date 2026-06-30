package com.valueswap.matching;

import com.valueswap.common.exception.ApiException;
import com.valueswap.matching.domain.MatchCandidate;
import com.valueswap.matching.dto.MatchDetailResponse;
import com.valueswap.matching.dto.MatchRunResponse;
import com.valueswap.matching.dto.MatchSummaryResponse;
import com.valueswap.matching.graph.CycleFinder;
import com.valueswap.matching.graph.MatchCycle;
import com.valueswap.matching.graph.MatchGraphBuilder;
import com.valueswap.notification.Notification;
import com.valueswap.notification.NotificationRepository;
import com.valueswap.notification.NotificationType;
import com.valueswap.post.ExchangePostRepository;
import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.PostStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MatchingService {
    private final ExchangePostRepository postRepository;
    private final MatchGraphBuilder graphBuilder;
    private final CycleFinder cycleFinder;
    private final MatchCandidateRepository candidateRepository;
    private final NotificationRepository notificationRepository;

    public MatchingService(ExchangePostRepository postRepository, MatchGraphBuilder graphBuilder,
                           CycleFinder cycleFinder, MatchCandidateRepository candidateRepository,
                           NotificationRepository notificationRepository) {
        this.postRepository = postRepository;
        this.graphBuilder = graphBuilder;
        this.cycleFinder = cycleFinder;
        this.candidateRepository = candidateRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public MatchRunResponse runMatching() {
        List<ExchangePost> posts = postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.ACTIVE);
        List<MatchCycle> cycles = cycleFinder.find(graphBuilder.build(posts));
        Map<Long, ExchangePost> postsById = posts.stream()
                .collect(Collectors.toMap(ExchangePost::getId, Function.identity()));
        int created = 0;
        for (MatchCycle cycle : cycles) {
            if (candidateRepository.existsByCycleKey(cycle.cycleKey())) {
                continue;
            }
            MatchCandidate candidate = candidateRepository.save(MatchCandidate.from(cycle, postsById));
            createNotifications(candidate);
            created++;
        }
        return new MatchRunResponse(posts.size(), cycles.size(), created);
    }

    public List<MatchSummaryResponse> findMine(Long userId) {
        return candidateRepository.findMine(userId).stream().map(MatchSummaryResponse::from).toList();
    }

    public MatchDetailResponse findOne(Long userId, Long matchId) {
        MatchCandidate candidate = candidateRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MATCH_NOT_FOUND", "매칭 후보를 찾을 수 없습니다."));
        boolean participant = candidate.getParticipants().stream()
                .anyMatch(item -> item.getUser().getId().equals(userId));
        if (!participant) {
            throw new ApiException(HttpStatus.FORBIDDEN, "MATCH_FORBIDDEN", "참여 중인 매칭만 조회할 수 있습니다.");
        }
        return MatchDetailResponse.from(candidate);
    }

    private void createNotifications(MatchCandidate candidate) {
        String title = candidate.getMatchType() == com.valueswap.matching.domain.MatchType.ONE_TO_ONE
                ? "1:1 교환 가능성이 발견되었습니다."
                : candidate.getParticipants().size() + "자 교환 가능성이 발견되었습니다.";
        String flow = candidate.getEdges().stream()
                .sorted(java.util.Comparator.comparingInt(edge -> edge.getEdgeOrder()))
                .map(edge -> edge.getToUser().getNickname() + " → " + edge.getFromUser().getNickname()
                        + ": " + edge.getProvideItem().getName() + " 제공")
                .collect(Collectors.joining("\n"));
        List<Notification> notifications = candidate.getParticipants().stream()
                .map(participant -> Notification.create(participant.getUser(), title, flow,
                        NotificationType.MATCH_FOUND))
                .toList();
        notificationRepository.saveAll(notifications);
    }
}
