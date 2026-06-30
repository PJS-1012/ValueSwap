package com.valueswap.matching.domain;

import com.valueswap.matching.graph.MatchCycle;
import com.valueswap.matching.graph.MatchGraphEdge;
import com.valueswap.post.domain.ExchangePost;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "match_candidates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus status;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false, unique = true)
    private String cycleKey;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "matchCandidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<MatchParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "matchCandidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<MatchEdge> edges = new ArrayList<>();

    private MatchCandidate(MatchCycle cycle) {
        this.matchType = cycle.type();
        this.status = MatchStatus.FOUND;
        this.score = cycle.averageScore();
        this.cycleKey = cycle.cycleKey();
    }

    public static MatchCandidate from(MatchCycle cycle, Map<Long, ExchangePost> postsById) {
        MatchCandidate candidate = new MatchCandidate(cycle);
        for (int index = 0; index < cycle.nodes().size(); index++) {
            ExchangePost post = postsById.get(cycle.nodes().get(index).postId());
            candidate.participants.add(MatchParticipant.create(candidate, post.getUser(), post, index));
        }
        for (int index = 0; index < cycle.edges().size(); index++) {
            MatchGraphEdge graphEdge = cycle.edges().get(index);
            ExchangePost fromPost = postsById.get(graphEdge.from().postId());
            ExchangePost toPost = postsById.get(graphEdge.to().postId());
            candidate.edges.add(MatchEdge.create(candidate, fromPost, toPost, graphEdge, index));
        }
        return candidate;
    }
}
