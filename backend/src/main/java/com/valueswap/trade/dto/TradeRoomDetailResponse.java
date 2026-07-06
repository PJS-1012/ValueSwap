package com.valueswap.trade.dto;

import com.valueswap.trade.domain.TradeRoom;
import com.valueswap.trade.domain.TradeRoomStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TradeRoomDetailResponse(Long id, Long matchId, TradeRoomStatus status,
                                      List<TradeRoomMemberResponse> members,
                                      LocalDateTime createdAt, LocalDateTime completedAt) {
    public static TradeRoomDetailResponse from(TradeRoom room) {
        return new TradeRoomDetailResponse(room.getId(), room.getMatchCandidate().getId(), room.getStatus(),
                room.getMembers().stream().map(TradeRoomMemberResponse::from).toList(),
                room.getCreatedAt(), room.getCompletedAt());
    }
}
