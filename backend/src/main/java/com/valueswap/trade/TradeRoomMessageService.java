package com.valueswap.trade;

import com.valueswap.common.exception.ApiException;
import com.valueswap.trade.domain.ChatMessage;
import com.valueswap.trade.domain.TradeRoom;
import com.valueswap.trade.domain.TradeRoomStatus;
import com.valueswap.trade.dto.ChatMessageResponse;
import com.valueswap.user.User;
import com.valueswap.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TradeRoomMessageService {
    private final TradeRoomRepository tradeRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public TradeRoomMessageService(TradeRoomRepository tradeRoomRepository,
                                   ChatMessageRepository chatMessageRepository,
                                   UserRepository userRepository) {
        this.tradeRoomRepository = tradeRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatMessageResponse send(Long roomId, Long userId, String clientMessageId, String content) {
        TradeRoom room = requireMember(roomId, userId);
        if (room.getStatus() == TradeRoomStatus.COMPLETED) {
            throw new ApiException(HttpStatus.CONFLICT, "TRADE_ROOM_COMPLETED", "완료된 거래방에는 메시지를 보낼 수 없습니다.");
        }
        var existing = chatMessageRepository.findByTradeRoomIdAndSenderIdAndClientMessageId(
                roomId, userId, clientMessageId);
        if (existing.isPresent()) return ChatMessageResponse.from(existing.get());
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        try {
            return ChatMessageResponse.from(chatMessageRepository.save(
                    ChatMessage.create(room, sender, clientMessageId, content)));
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE", exception.getMessage());
        }
    }

    public boolean isMember(Long roomId, Long userId) {
        return tradeRoomRepository.findById(roomId)
                .map(room -> room.getMembers().stream().anyMatch(member -> member.getUser().getId().equals(userId)))
                .orElse(false);
    }

    private TradeRoom requireMember(Long roomId, Long userId) {
        TradeRoom room = tradeRoomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TRADE_ROOM_NOT_FOUND", "거래방을 찾을 수 없습니다."));
        if (room.getMembers().stream().noneMatch(member -> member.getUser().getId().equals(userId))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "TRADE_ROOM_FORBIDDEN", "거래방 참여자만 메시지를 보낼 수 있습니다.");
        }
        return room;
    }
}
