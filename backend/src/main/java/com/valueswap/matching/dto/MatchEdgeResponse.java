package com.valueswap.matching.dto;

import com.valueswap.matching.domain.MatchEdge;

public record MatchEdgeResponse(Long fromUserId, String fromNickname, Long toUserId, String toNickname,
                                Long fromPostId, Long toPostId, Long provideItemId, String provideItemName,
                                Long wantItemId, String wantItemName, int score, int orderIndex) {
    public static MatchEdgeResponse from(MatchEdge edge) {
        return new MatchEdgeResponse(edge.getFromUser().getId(), edge.getFromUser().getNickname(),
                edge.getToUser().getId(), edge.getToUser().getNickname(), edge.getFromPost().getId(),
                edge.getToPost().getId(), edge.getProvideItem().getId(), edge.getProvideItem().getName(),
                edge.getWantItem().getId(), edge.getWantItem().getName(), edge.getScore(), edge.getEdgeOrder());
    }
}
