package com.valueswap.matching.graph;

import com.valueswap.matching.domain.MatchType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CycleFinder {
    public List<MatchCycle> find(MatchGraph graph) {
        Map<String, MatchCycle> unique = new LinkedHashMap<>();
        graph.nodes().stream().sorted(Comparator.comparing(GraphNode::postId)).forEach(start -> {
            List<GraphNode> path = new ArrayList<>();
            path.add(start);
            Set<Long> postIds = new HashSet<>();
            postIds.add(start.postId());
            Set<Long> userIds = new HashSet<>();
            userIds.add(start.userId());
            search(graph, start, start, path, new ArrayList<>(), postIds, userIds, unique);
        });
        return List.copyOf(unique.values());
    }

    private void search(MatchGraph graph, GraphNode start, GraphNode current,
                        List<GraphNode> path, List<MatchGraphEdge> pathEdges,
                        Set<Long> visitedPosts, Set<Long> visitedUsers,
                        Map<String, MatchCycle> unique) {
        for (MatchGraphEdge edge : graph.outgoing(current.postId())) {
            GraphNode next = edge.to();
            if (next.postId().equals(start.postId())) {
                if (path.size() >= 2 && path.size() <= 4) {
                    List<MatchGraphEdge> cycleEdges = new ArrayList<>(pathEdges);
                    cycleEdges.add(edge);
                    MatchCycle cycle = createCycle(path, cycleEdges);
                    unique.putIfAbsent(cycle.cycleKey(), cycle);
                }
                continue;
            }
            if (path.size() >= 4 || visitedPosts.contains(next.postId()) || visitedUsers.contains(next.userId())) {
                continue;
            }
            path.add(next);
            pathEdges.add(edge);
            visitedPosts.add(next.postId());
            visitedUsers.add(next.userId());
            search(graph, start, next, path, pathEdges, visitedPosts, visitedUsers, unique);
            visitedUsers.remove(next.userId());
            visitedPosts.remove(next.postId());
            pathEdges.remove(pathEdges.size() - 1);
            path.remove(path.size() - 1);
        }
    }

    private MatchCycle createCycle(List<GraphNode> path, List<MatchGraphEdge> edges) {
        MatchType type = MatchType.fromParticipantCount(path.size());
        String ids = path.stream().map(GraphNode::postId).sorted().map(String::valueOf)
                .collect(Collectors.joining("-"));
        int average = (int) edges.stream().mapToInt(MatchGraphEdge::score).average().orElseThrow();
        return new MatchCycle(type, List.copyOf(path), List.copyOf(edges), type + ":" + ids, average);
    }
}
