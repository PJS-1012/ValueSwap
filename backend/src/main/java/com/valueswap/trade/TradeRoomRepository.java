package com.valueswap.trade;

import com.valueswap.trade.domain.TradeRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TradeRoomRepository extends JpaRepository<TradeRoom, Long> {
    Optional<TradeRoom> findByMatchCandidateId(Long matchCandidateId);

    @Query("""
            select distinct room from TradeRoom room
            join room.members member
            where member.user.id = :userId
            order by room.createdAt desc
            """)
    List<TradeRoom> findMine(@Param("userId") Long userId);
}
