package com.valueswap.trade.domain;

import com.valueswap.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_room_id", nullable = false)
    private TradeRoom tradeRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 36)
    private String clientMessageId;

    @Column(nullable = false, length = 1000)
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ChatMessage create(TradeRoom room, User sender, String clientMessageId, String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty() || normalized.length() > 1000) {
            throw new IllegalArgumentException("메시지는 1자 이상 1000자 이하여야 합니다.");
        }
        if (clientMessageId == null || clientMessageId.isBlank() || clientMessageId.length() > 36) {
            throw new IllegalArgumentException("클라이언트 메시지 ID가 올바르지 않습니다.");
        }
        ChatMessage message = new ChatMessage();
        message.tradeRoom = room;
        message.sender = sender;
        message.clientMessageId = clientMessageId;
        message.content = normalized;
        return message;
    }
}
