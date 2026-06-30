package com.valueswap.matching.graph;

import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.WantItem;

public record MatchGraphEdge(GraphNode from, GraphNode to, ProvideItem provideItem,
                             WantItem wantItem, int score) {
}
