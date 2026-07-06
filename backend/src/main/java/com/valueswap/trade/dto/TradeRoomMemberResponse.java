package com.valueswap.trade.dto;

import com.valueswap.trade.domain.TradeRoomMember;

import java.time.LocalDateTime;

public record TradeRoomMemberResponse(Long userId, String nickname, LocalDateTime completedAt) {
    public static TradeRoomMemberResponse from(TradeRoomMember member) {
        return new TradeRoomMemberResponse(member.getUser().getId(), member.getUser().getNickname(),
                member.getCompletedAt());
    }
}
