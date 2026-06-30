package com.valueswap.matching.domain;

import com.valueswap.matching.graph.MatchGraphEdge;
import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.WantItem;
import com.valueswap.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_edges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchEdge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_candidate_id", nullable = false)
    private MatchCandidate matchCandidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_post_id", nullable = false)
    private ExchangePost fromPost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_post_id", nullable = false)
    private ExchangePost toPost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provide_item_id", nullable = false)
    private ProvideItem provideItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "want_item_id", nullable = false)
    private WantItem wantItem;

    @Column(nullable = false)
    private Integer edgeOrder;

    @Column(nullable = false)
    private Integer score;

    static MatchEdge create(MatchCandidate candidate, ExchangePost fromPost, ExchangePost toPost,
                            MatchGraphEdge graphEdge, int edgeOrder) {
        MatchEdge edge = new MatchEdge();
        edge.matchCandidate = candidate;
        edge.fromUser = fromPost.getUser();
        edge.toUser = toPost.getUser();
        edge.fromPost = fromPost;
        edge.toPost = toPost;
        edge.provideItem = graphEdge.provideItem();
        edge.wantItem = graphEdge.wantItem();
        edge.edgeOrder = edgeOrder;
        edge.score = graphEdge.score();
        return edge;
    }
}
