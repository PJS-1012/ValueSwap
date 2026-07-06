package com.valueswap.trade;

import com.valueswap.common.exception.ApiException;
import com.valueswap.matching.domain.MatchCandidate;
import com.valueswap.notification.Notification;
import com.valueswap.notification.NotificationRepository;
import com.valueswap.notification.NotificationType;
import com.valueswap.trade.domain.TradeRoom;
import com.valueswap.trade.domain.TradeRoomStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TradeRoomService {
    private final TradeRoomRepository tradeRoomRepository;
    private final NotificationRepository notificationRepository;

    public TradeRoomService(TradeRoomRepository tradeRoomRepository,
                            NotificationRepository notificationRepository) {
        this.tradeRoomRepository = tradeRoomRepository;
        this.notificationRepository = notificationRepository;
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
}
