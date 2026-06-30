package com.valueswap.matching.graph;

import com.valueswap.matching.domain.MatchType;

import java.util.List;

public record MatchCycle(MatchType type, List<GraphNode> nodes, List<MatchGraphEdge> edges,
                         String cycleKey, int averageScore) {
}
