package com.valueswap.trade;

import com.valueswap.common.exception.ApiException;
import com.valueswap.notification.NotificationRepository;
import com.valueswap.trade.domain.ChatMessage;
import com.valueswap.trade.domain.TradeRoom;
import com.valueswap.trade.domain.TradeRoomMember;
import com.valueswap.user.User;
import com.valueswap.matching.domain.MatchCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeRoomServiceTest {
    private TradeRoomRepository roomRepository;
    private ChatMessageRepository messageRepository;
    private TradeRoomService service;

    @BeforeEach
    void setUp() {
        roomRepository = mock(TradeRoomRepository.class);
        messageRepository = mock(ChatMessageRepository.class);
        service = new TradeRoomService(roomRepository, mock(NotificationRepository.class), messageRepository);
    }

    @Test
    void readMarksLatestMessageForMember() {
        TradeRoom room = mock(TradeRoom.class);
        TradeRoomMember member = member(7L);
        ChatMessage latest = mock(ChatMessage.class);
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(room.getMembers()).thenReturn(List.of(member));
        MatchCandidate candidate = mock(MatchCandidate.class);
        when(candidate.getId()).thenReturn(5L);
        when(room.getMatchCandidate()).thenReturn(candidate);
        when(messageRepository.findTopByTradeRoomIdOrderByIdDesc(3L)).thenReturn(Optional.of(latest));
        when(latest.getId()).thenReturn(12L);

        service.markRead(3L, 7L);

        verify(member).readThrough(12L);
    }

    @Test
    void nonMemberCannotReadMessages() {
        TradeRoom room = mock(TradeRoom.class);
        TradeRoomMember another = member(8L);
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(room.getMembers()).thenReturn(List.of(another));

        assertThatThrownBy(() -> service.findMessages(3L, 7L, null, null, 50))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("참여자");
    }

    private TradeRoomMember member(Long userId) {
        TradeRoomMember member = mock(TradeRoomMember.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(member.getUser()).thenReturn(user);
        return member;
    }
}
