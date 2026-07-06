package com.valueswap.matching;

import com.valueswap.common.exception.ApiException;
import com.valueswap.matching.domain.MatchCandidate;
import com.valueswap.matching.domain.AcceptStatus;
import com.valueswap.matching.domain.MatchParticipant;
import com.valueswap.matching.domain.MatchStatus;
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
import com.valueswap.trade.TradeRoomService;
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
    private final TradeRoomService tradeRoomService;

    public MatchingService(ExchangePostRepository postRepository, MatchGraphBuilder graphBuilder,
                           CycleFinder cycleFinder, MatchCandidateRepository candidateRepository,
                           NotificationRepository notificationRepository, TradeRoomService tradeRoomService) {
        this.postRepository = postRepository;
        this.graphBuilder = graphBuilder;
        this.cycleFinder = cycleFinder;
        this.candidateRepository = candidateRepository;
        this.notificationRepository = notificationRepository;
        this.tradeRoomService = tradeRoomService;
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
                candidateRepository.findByCycleKey(cycle.cycleKey()).ifPresent(this::createNotifications);
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
        return MatchDetailResponse.from(requireParticipant(matchId, userId).candidate());
    }

    @Transactional
    public MatchDetailResponse accept(Long matchId, Long userId) {
        ParticipantContext context = requireParticipant(matchId, userId);
        ensureOpen(context.candidate());
        context.participant().accept();
        if (context.candidate().getParticipants().stream()
                .allMatch(item -> item.getAcceptStatus() == AcceptStatus.ACCEPTED)) {
            context.candidate().accept();
            context.candidate().getParticipants().forEach(item -> item.getExchangePost().startExchange());
            tradeRoomService.createIfAbsent(context.candidate());
            createStatusNotifications(context.candidate(), NotificationType.MATCH_ACCEPTED,
                    "모든 참여자가 수락했습니다", "교환이 진행 중 상태로 전환되었습니다.");
        }
        return MatchDetailResponse.from(context.candidate());
    }

    @Transactional
    public MatchDetailResponse reject(Long matchId, Long userId) {
        ParticipantContext context = requireParticipant(matchId, userId);
        ensureOpen(context.candidate());
        context.participant().reject();
        context.candidate().reject();
        createStatusNotifications(context.candidate(), NotificationType.MATCH_REJECTED,
                "교환 후보가 거절되었습니다", context.participant().getUser().getNickname() + "님이 참여를 거절했습니다.");
        return MatchDetailResponse.from(context.candidate());
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
                .filter(participant -> !notificationRepository.existsByUserIdAndTypeAndReferenceId(
                        participant.getUser().getId(), NotificationType.MATCH_FOUND, candidate.getId()))
                .map(participant -> Notification.create(participant.getUser(), title, flow,
                        NotificationType.MATCH_FOUND, candidate.getId()))
                .toList();
        notificationRepository.saveAll(notifications);
    }

    private ParticipantContext requireParticipant(Long matchId, Long userId) {
        MatchCandidate candidate = candidateRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MATCH_NOT_FOUND", "매칭 후보를 찾을 수 없습니다."));
        MatchParticipant participant = candidate.getParticipants().stream()
                .filter(item -> item.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "MATCH_FORBIDDEN", "참여 중인 매칭만 조회할 수 있습니다."));
        return new ParticipantContext(candidate, participant);
    }

    private void ensureOpen(MatchCandidate candidate) {
        if (candidate.getStatus() == MatchStatus.ACCEPTED || candidate.getStatus() == MatchStatus.REJECTED
                || candidate.getStatus() == MatchStatus.COMPLETED || candidate.getStatus() == MatchStatus.EXPIRED) {
            throw new ApiException(HttpStatus.CONFLICT, "MATCH_CLOSED", "이미 종료된 매칭입니다.");
        }
        if (candidate.getParticipants().stream()
                .anyMatch(participant -> participant.getExchangePost().getStatus() != PostStatus.ACTIVE)) {
            throw new ApiException(HttpStatus.CONFLICT, "POST_ALREADY_IN_EXCHANGE",
                    "참여 글 중 하나가 다른 거래가 진행 중입니다.");
        }
    }

    private void createStatusNotifications(MatchCandidate candidate, NotificationType type,
                                           String title, String message) {
        notificationRepository.saveAll(candidate.getParticipants().stream()
                .map(item -> Notification.create(item.getUser(), title, message, type, candidate.getId()))
                .toList());
    }

    private record ParticipantContext(MatchCandidate candidate, MatchParticipant participant) {}
}
