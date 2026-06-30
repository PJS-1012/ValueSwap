package com.valueswap.matching.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MatchGraph {
    private final Map<Long, GraphNode> nodes = new LinkedHashMap<>();
    private final Map<Long, List<MatchGraphEdge>> outgoing = new LinkedHashMap<>();

    public void addNode(GraphNode node) {
        nodes.putIfAbsent(node.postId(), node);
        outgoing.computeIfAbsent(node.postId(), ignored -> new ArrayList<>());
    }

    public void addEdge(MatchGraphEdge edge) {
        addNode(edge.from());
        addNode(edge.to());
        outgoing.get(edge.from().postId()).add(edge);
    }

    public Collection<GraphNode> nodes() {
        return List.copyOf(nodes.values());
    }

    public List<MatchGraphEdge> outgoing(Long postId) {
        return List.copyOf(outgoing.getOrDefault(postId, List.of()));
    }
}
