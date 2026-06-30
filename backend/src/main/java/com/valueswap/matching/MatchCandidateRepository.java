package com.valueswap.matching;

import com.valueswap.matching.domain.MatchCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchCandidateRepository extends JpaRepository<MatchCandidate, Long> {
    boolean existsByCycleKey(String cycleKey);

    @Query("""
            select distinct candidate from MatchCandidate candidate
            join candidate.participants participant
            where participant.user.id = :userId
            order by candidate.score desc, candidate.createdAt desc
            """)
    List<MatchCandidate> findMine(@Param("userId") Long userId);
}
