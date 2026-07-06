package com.valueswap.trade;

import com.valueswap.trade.domain.TradeRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TradeRoomRepository extends JpaRepository<TradeRoom, Long> {
    Optional<TradeRoom> findByMatchCandidateId(Long matchCandidateId);
}
