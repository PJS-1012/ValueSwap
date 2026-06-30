package com.valueswap.matching.dto;

import com.valueswap.matching.domain.AcceptStatus;
import com.valueswap.matching.domain.MatchParticipant;

public record MatchParticipantResponse(Long userId, String nickname, Long postId, String postTitle,
                                       int orderIndex, AcceptStatus acceptStatus) {
    public static MatchParticipantResponse from(MatchParticipant participant) {
        return new MatchParticipantResponse(participant.getUser().getId(), participant.getUser().getNickname(),
                participant.getExchangePost().getId(), participant.getExchangePost().getTitle(),
                participant.getOrderIndex(), participant.getAcceptStatus());
    }
}
