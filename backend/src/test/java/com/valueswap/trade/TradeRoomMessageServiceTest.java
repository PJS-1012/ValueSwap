package com.valueswap.trade;

import com.valueswap.common.exception.ApiException;
import com.valueswap.trade.domain.ChatMessage;
import com.valueswap.trade.domain.TradeRoom;
import com.valueswap.trade.domain.TradeRoomMember;
import com.valueswap.trade.domain.TradeRoomStatus;
import com.valueswap.user.User;
import com.valueswap.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeRoomMessageServiceTest {
    private TradeRoomRepository roomRepository;
    private ChatMessageRepository messageRepository;
    private UserRepository userRepository;
    private TradeRoomMessageService service;

    @BeforeEach
    void setUp() {
        roomRepository = mock(TradeRoomRepository.class);
        messageRepository = mock(ChatMessageRepository.class);
        userRepository = mock(UserRepository.class);
        service = new TradeRoomMessageService(roomRepository, messageRepository, userRepository);
    }

    @Test
    void memberMessageIsSavedAndDuplicateClientIdReturnsExistingMessage() {
        User sender = user(7L);
        TradeRoom room = room(3L, sender, TradeRoomStatus.IN_EXCHANGE);
        when(roomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(userRepository.findById(7L)).thenReturn(Optional.of(sender));
        when(messageRepository.findByTradeRoomIdAndSenderIdAndClientMessageId(3L, 7L, "client-1"))
                .thenReturn(Optional.empty());
        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var first = service.send(3L, 7L, "client-1", " 안녕하세요 ");

        assertThat(first.content()).isEqualTo("안녕하세요");
        verify(messageRepository).save(any(ChatMessage.class));
    }

    @Test
    void completedRoomRejectsMessage() {
        User sender = user(7L);
        TradeRoom completedRoom = room(3L, sender, TradeRoomStatus.COMPLETED);
        when(roomRepository.findById(3L)).thenReturn(Optional.of(completedRoom));

        assertThatThrownBy(() -> service.send(3L, 7L, "client-2", "메시지"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("완료");
        verify(messageRepository, never()).save(any());
    }

    private TradeRoom room(Long id, User memberUser, TradeRoomStatus status) {
        TradeRoom room = mock(TradeRoom.class);
        TradeRoomMember member = mock(TradeRoomMember.class);
        when(room.getId()).thenReturn(id);
        when(room.getStatus()).thenReturn(status);
        when(member.getUser()).thenReturn(memberUser);
        when(room.getMembers()).thenReturn(List.of(member));
        return room;
    }

    private User user(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getNickname()).thenReturn("사용자" + id);
        return user;
    }
}
