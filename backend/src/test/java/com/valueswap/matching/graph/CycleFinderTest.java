package com.valueswap.matching.graph;

import com.valueswap.matching.domain.MatchType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CycleFinderTest {
    private final CycleFinder finder = new CycleFinder();

    @Test
    void findsOneCanonicalThreePartyCycle() {
        MatchGraph graph = graph(edge(1, 11, 2, 22, 90), edge(2, 22, 3, 33, 80), edge(3, 33, 1, 11, 70));

        List<MatchCycle> cycles = finder.find(graph);

        assertThat(cycles).singleElement().satisfies(cycle -> {
            assertThat(cycle.type()).isEqualTo(MatchType.THREE_PARTY);
            assertThat(cycle.cycleKey()).isEqualTo("THREE_PARTY:1-2-3");
            assertThat(cycle.averageScore()).isEqualTo(80);
            assertThat(cycle.edges()).hasSize(3);
        });
    }

    @Test
    void findsTwoAndFourPartyCycles() {
        MatchGraph graph = graph(
                edge(1, 11, 2, 22, 80), edge(2, 22, 1, 11, 80),
                edge(3, 33, 4, 44, 90), edge(4, 44, 5, 55, 80),
                edge(5, 55, 6, 66, 70), edge(6, 66, 3, 33, 60));

        assertThat(finder.find(graph)).extracting(MatchCycle::type)
                .containsExactlyInAnyOrder(MatchType.ONE_TO_ONE, MatchType.FOUR_PARTY);
    }

    @Test
    void ignoresOpenPathsRepeatedUsersAndFivePartyCycles() {
        MatchGraph graph = graph(
                edge(1, 11, 2, 22, 80), edge(2, 22, 3, 11, 80), edge(3, 11, 1, 11, 80),
                edge(4, 44, 5, 55, 80), edge(5, 55, 6, 66, 80),
                edge(7, 77, 8, 88, 80), edge(8, 88, 9, 99, 80),
                edge(9, 99, 10, 100, 80), edge(10, 100, 11, 110, 80), edge(11, 110, 7, 77, 80));

        assertThat(finder.find(graph)).isEmpty();
    }

    private MatchGraph graph(MatchGraphEdge... edges) {
        MatchGraph graph = new MatchGraph();
        for (MatchGraphEdge edge : edges) {
            graph.addEdge(edge);
        }
        return graph;
    }

    private MatchGraphEdge edge(long fromPost, long fromUser, long toPost, long toUser, int score) {
        return new MatchGraphEdge(new GraphNode(fromPost, fromUser), new GraphNode(toPost, toUser),
                null, null, score);
    }
}
