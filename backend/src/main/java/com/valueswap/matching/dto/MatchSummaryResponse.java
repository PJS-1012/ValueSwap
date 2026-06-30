package com.valueswap.matching.dto;

import com.valueswap.matching.domain.MatchCandidate;
import com.valueswap.matching.domain.MatchStatus;
import com.valueswap.matching.domain.MatchType;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record MatchSummaryResponse(Long id, MatchType matchType, MatchStatus status, int score,
                                   String cycleKey, LocalDateTime createdAt,
                                   List<MatchParticipantResponse> participants,
                                   List<MatchEdgeResponse> edges) {
    public static MatchSummaryResponse from(MatchCandidate candidate) {
        return new MatchSummaryResponse(candidate.getId(), candidate.getMatchType(), candidate.getStatus(),
                candidate.getScore(), candidate.getCycleKey(), candidate.getCreatedAt(),
                candidate.getParticipants().stream().sorted(Comparator.comparingInt(p -> p.getOrderIndex()))
                        .map(MatchParticipantResponse::from).toList(),
                candidate.getEdges().stream().sorted(Comparator.comparingInt(e -> e.getEdgeOrder()))
                        .map(MatchEdgeResponse::from).toList());
    }
}
