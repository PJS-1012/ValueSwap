package com.valueswap.trade.dto;

import com.valueswap.trade.domain.ChatMessage;
import com.valueswap.trade.domain.TradeRoom;
import com.valueswap.trade.domain.TradeRoomMember;
import com.valueswap.trade.domain.TradeRoomStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TradeRoomSummaryResponse(Long id, Long matchId, TradeRoomStatus status,
                                       List<String> participants, String recentMessage,
                                       LocalDateTime recentMessageAt, long unreadCount,
                                       LocalDateTime createdAt) {
    public static TradeRoomSummaryResponse from(TradeRoom room, ChatMessage recent, long unreadCount) {
        return new TradeRoomSummaryResponse(room.getId(), room.getMatchCandidate().getId(), room.getStatus(),
                room.getMembers().stream().map(TradeRoomMember::getUser).map(user -> user.getNickname()).toList(),
                recent == null ? null : recent.getContent(), recent == null ? null : recent.getCreatedAt(),
                unreadCount, room.getCreatedAt());
    }
}
