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

import java.time.LocalDateTime;

@Entity
@Table(name = "trade_room_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeRoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_room_id", nullable = false)
    private TradeRoom tradeRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Long lastReadMessageId;

    private LocalDateTime completedAt;

    static TradeRoomMember create(TradeRoom room, User user) {
        TradeRoomMember member = new TradeRoomMember();
        member.tradeRoom = room;
        member.user = user;
        return member;
    }

    void complete() {
        if (completedAt == null) completedAt = LocalDateTime.now();
    }

    public void readThrough(Long messageId) {
        if (messageId != null && (lastReadMessageId == null || messageId > lastReadMessageId)) {
            lastReadMessageId = messageId;
        }
    }
}
