package com.valueswap.trade.dto;

import com.valueswap.trade.domain.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(Long id, Long roomId, Long senderId, String senderNickname,
                                  String clientMessageId, String content, LocalDateTime createdAt) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getTradeRoom().getId(),
                message.getSender().getId(), message.getSender().getNickname(), message.getClientMessageId(),
                message.getContent(), message.getCreatedAt());
    }
}
