package com.valueswap.trade.domain;

import com.valueswap.matching.domain.MatchCandidate;
import com.valueswap.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trade_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_candidate_id", nullable = false, unique = true)
    private MatchCandidate matchCandidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TradeRoomStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "tradeRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<TradeRoomMember> members = new ArrayList<>();

    public static TradeRoom create(MatchCandidate candidate, List<User> users) {
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("거래방에는 참여자가 필요합니다.");
        }
        TradeRoom room = new TradeRoom();
        room.matchCandidate = candidate;
        room.status = TradeRoomStatus.IN_EXCHANGE;
        users.forEach(user -> room.members.add(TradeRoomMember.create(room, user)));
        return room;
    }

    public void complete(Long userId) {
        TradeRoomMember member = members.stream()
                .filter(item -> item.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("거래방 참여자가 아닙니다."));
        member.complete();
        if (members.stream().allMatch(item -> item.getCompletedAt() != null)) {
            status = TradeRoomStatus.COMPLETED;
            if (completedAt == null) completedAt = LocalDateTime.now();
        }
    }
}
