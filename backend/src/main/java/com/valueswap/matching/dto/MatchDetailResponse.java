package com.valueswap.matching.dto;

import com.valueswap.matching.domain.MatchCandidate;

public record MatchDetailResponse(MatchSummaryResponse match) {
    public static MatchDetailResponse from(MatchCandidate candidate) {
        return new MatchDetailResponse(MatchSummaryResponse.from(candidate));
    }
}
