package com.valueswap.matching.graph;

import com.valueswap.matching.score.ItemMatchResult;
import com.valueswap.matching.score.ItemMatchScorer;
import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.PostStatus;
import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.WantItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchGraphBuilder {
    private final ItemMatchScorer scorer;

    public MatchGraphBuilder(ItemMatchScorer scorer) {
        this.scorer = scorer;
    }

    public MatchGraph build(List<ExchangePost> posts) {
        List<ExchangePost> activePosts = posts.stream()
                .filter(post -> post.getStatus() == PostStatus.ACTIVE)
                .toList();
        MatchGraph graph = new MatchGraph();
        activePosts.forEach(post -> graph.addNode(node(post)));

        for (ExchangePost from : activePosts) {
            for (ExchangePost to : activePosts) {
                if (from.getId().equals(to.getId()) || from.getUser().getId().equals(to.getUser().getId())) {
                    continue;
                }
                MatchGraphEdge best = bestEdge(from, to);
                if (best != null) {
                    graph.addEdge(best);
                }
            }
        }
        return graph;
    }

    private MatchGraphEdge bestEdge(ExchangePost from, ExchangePost to) {
        MatchGraphEdge best = null;
        for (WantItem want : from.getWantItems()) {
            for (ProvideItem provide : to.getProvideItems()) {
                ItemMatchResult result = scorer.score(provide, want, to.getRegion(), from.getRegion(),
                        to.getUser().getTrustScore());
                if (result.eligible() && (best == null || result.score() > best.score())) {
                    best = new MatchGraphEdge(node(from), node(to), provide, want, result.score());
                }
            }
        }
        return best;
    }

    private GraphNode node(ExchangePost post) {
        return new GraphNode(post.getId(), post.getUser().getId());
    }
}
