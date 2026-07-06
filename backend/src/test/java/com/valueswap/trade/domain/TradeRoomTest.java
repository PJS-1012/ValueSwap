package com.valueswap.trade.domain;

import com.valueswap.matching.domain.MatchCandidate;
import com.valueswap.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeRoomTest {

    @Test
    void everyMemberCompletionClosesRoom() {
        MatchCandidate candidate = mock(MatchCandidate.class);
        User first = user(1L);
        User second = user(2L);
        TradeRoom room = TradeRoom.create(candidate, List.of(first, second));

        room.complete(first.getId());
        assertThat(room.getStatus()).isEqualTo(TradeRoomStatus.IN_EXCHANGE);

        room.complete(second.getId());
        assertThat(room.getStatus()).isEqualTo(TradeRoomStatus.COMPLETED);
        assertThat(room.getCompletedAt()).isNotNull();
    }

    @Test
    void completionIsIdempotentForSameMember() {
        User first = user(1L);
        User second = user(2L);
        TradeRoom room = TradeRoom.create(mock(MatchCandidate.class), List.of(first, second));

        room.complete(first.getId());
        room.complete(first.getId());

        assertThat(room.getStatus()).isEqualTo(TradeRoomStatus.IN_EXCHANGE);
        assertThat(room.getMembers()).filteredOn(member -> member.getCompletedAt() != null).hasSize(1);
    }

    @Test
    void messageTrimsContentAndRejectsInvalidLength() {
        TradeRoom room = TradeRoom.create(mock(MatchCandidate.class), List.of(user(1L)));
        User sender = user(1L);

        ChatMessage message = ChatMessage.create(room, sender, "client-1", "  안녕하세요  ");

        assertThat(message.getContent()).isEqualTo("안녕하세요");
        assertThatThrownBy(() -> ChatMessage.create(room, sender, "client-2", "   "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChatMessage.create(room, sender, "client-3", "가".repeat(1001)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private User user(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }
}
