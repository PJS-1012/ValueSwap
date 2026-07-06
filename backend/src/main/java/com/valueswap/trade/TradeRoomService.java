package com.valueswap.trade;

import com.valueswap.common.exception.ApiException;
import com.valueswap.matching.domain.MatchCandidate;
import com.valueswap.notification.Notification;
import com.valueswap.notification.NotificationRepository;
import com.valueswap.notification.NotificationType;
import com.valueswap.trade.domain.TradeRoom;
import com.valueswap.trade.domain.TradeRoomStatus;
import com.valueswap.trade.dto.TradeRoomSummaryResponse;
import com.valueswap.trade.dto.ChatMessagePageResponse;
import com.valueswap.trade.dto.ChatMessageResponse;
import com.valueswap.trade.dto.TradeRoomDetailResponse;
import com.valueswap.trade.domain.TradeRoomMember;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TradeRoomService {
    private final TradeRoomRepository tradeRoomRepository;
    private final NotificationRepository notificationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public TradeRoomService(TradeRoomRepository tradeRoomRepository,
                            NotificationRepository notificationRepository,
                            ChatMessageRepository chatMessageRepository) {
        this.tradeRoomRepository = tradeRoomRepository;
        this.notificationRepository = notificationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public java.util.List<TradeRoomSummaryResponse> findMine(Long userId) {
        return tradeRoomRepository.findMine(userId).stream().map(room -> {
            var recent = chatMessageRepository.findTopByTradeRoomIdOrderByIdDesc(room.getId()).orElse(null);
            var member = room.getMembers().stream().filter(item -> item.getUser().getId().equals(userId))
                    .findFirst().orElseThrow();
            long unread = chatMessageRepository.countByTradeRoomIdAndSenderIdNotAndIdGreaterThan(
                    room.getId(), userId, member.getLastReadMessageId() == null ? 0L : member.getLastReadMessageId());
            return TradeRoomSummaryResponse.from(room, recent, unread);
        }).toList();
    }

    public TradeRoomDetailResponse findOne(Long roomId, Long userId) {
        return TradeRoomDetailResponse.from(requireRoomMember(roomId, userId).room());
    }

    public ChatMessagePageResponse findMessages(Long roomId, Long userId, Long beforeId, Long afterId, int size) {
        requireRoomMember(roomId, userId);
        int limit = Math.max(1, Math.min(size, 100));
        var pageable = PageRequest.of(0, limit + 1);
        java.util.List<com.valueswap.trade.domain.ChatMessage> found;
        boolean reverse = false;
        if (afterId != null) {
            found = chatMessageRepository.findByTradeRoomIdAndIdGreaterThanOrderByIdAsc(roomId, afterId, pageable);
        } else if (beforeId != null) {
            found = chatMessageRepository.findByTradeRoomIdAndIdLessThanOrderByIdDesc(roomId, beforeId, pageable);
            reverse = true;
        } else {
            found = chatMessageRepository.findByTradeRoomIdOrderByIdDesc(roomId, pageable);
            reverse = true;
        }
        boolean hasMore = found.size() > limit;
        var page = new java.util.ArrayList<>(found.subList(0, Math.min(found.size(), limit)));
        if (reverse) java.util.Collections.reverse(page);
        return new ChatMessagePageResponse(page.stream().map(ChatMessageResponse::from).toList(), hasMore);
    }

    @Transactional
    public TradeRoomDetailResponse markRead(Long roomId, Long userId) {
        ParticipantContext context = requireRoomMember(roomId, userId);
        chatMessageRepository.findTopByTradeRoomIdOrderByIdDesc(roomId)
                .ifPresent(message -> context.member().readThrough(message.getId()));
        return TradeRoomDetailResponse.from(context.room());
    }

    @Transactional
    public TradeRoom createIfAbsent(MatchCandidate candidate) {
        return tradeRoomRepository.findByMatchCandidateId(candidate.getId())
                .orElseGet(() -> tradeRoomRepository.save(TradeRoom.create(candidate,
                        candidate.getParticipants().stream().map(item -> item.getUser()).toList())));
    }

    @Transactional
    public TradeRoom complete(Long roomId, Long userId) {
        TradeRoom room = tradeRoomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TRADE_ROOM_NOT_FOUND",
                        "거래방을 찾을 수 없습니다."));
        boolean member = room.getMembers().stream().anyMatch(item -> item.getUser().getId().equals(userId));
        if (!member) {
            throw new ApiException(HttpStatus.FORBIDDEN, "TRADE_ROOM_FORBIDDEN", "거래방 참여자만 완료할 수 있습니다.");
        }
        boolean alreadyCompleted = room.getStatus() == TradeRoomStatus.COMPLETED;
        room.complete(userId);
        if (!alreadyCompleted && room.getStatus() == TradeRoomStatus.COMPLETED) {
            MatchCandidate candidate = room.getMatchCandidate();
            candidate.complete();
            candidate.getParticipants().forEach(item -> item.getExchangePost().complete());
            notificationRepository.saveAll(candidate.getParticipants().stream()
                    .map(item -> Notification.create(item.getUser(), "거래가 완료되었습니다",
                            "모든 참여자가 거래 완료를 확인했습니다.", NotificationType.EXCHANGE_COMPLETED, candidate.getId()))
                    .toList());
        }
        return room;
    }

    private ParticipantContext requireRoomMember(Long roomId, Long userId) {
        TradeRoom room = tradeRoomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TRADE_ROOM_NOT_FOUND",
                        "거래방을 찾을 수 없습니다."));
        TradeRoomMember member = room.getMembers().stream()
                .filter(item -> item.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "TRADE_ROOM_FORBIDDEN",
                        "거래방 참여자만 접근할 수 있습니다."));
        return new ParticipantContext(room, member);
    }

    private record ParticipantContext(TradeRoom room, TradeRoomMember member) {}
}
