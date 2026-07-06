package com.valueswap.trade;

import com.valueswap.trade.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findTopByTradeRoomIdOrderByIdDesc(Long roomId);
    long countByTradeRoomIdAndSenderIdNotAndIdGreaterThan(Long roomId, Long senderId, Long afterId);
    List<ChatMessage> findByTradeRoomIdAndIdLessThanOrderByIdDesc(Long roomId, Long beforeId, Pageable pageable);
    List<ChatMessage> findByTradeRoomIdAndIdGreaterThanOrderByIdAsc(Long roomId, Long afterId, Pageable pageable);
    List<ChatMessage> findByTradeRoomIdOrderByIdDesc(Long roomId, Pageable pageable);
    Optional<ChatMessage> findByTradeRoomIdAndSenderIdAndClientMessageId(
            Long roomId, Long senderId, String clientMessageId);
}
