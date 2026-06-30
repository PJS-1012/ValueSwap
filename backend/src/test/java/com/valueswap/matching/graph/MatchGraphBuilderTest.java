package com.valueswap.matching.graph;

import com.valueswap.matching.score.ItemMatchScorer;
import com.valueswap.matching.score.TextNormalizer;
import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ExchangePost;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.valueswap.support.MatchingFixtures.post;
import static com.valueswap.support.MatchingFixtures.provide;
import static com.valueswap.support.MatchingFixtures.want;
import static org.assertj.core.api.Assertions.assertThat;

class MatchGraphBuilderTest {
    private final MatchGraphBuilder builder = new MatchGraphBuilder(new ItemMatchScorer(new TextNormalizer(), 60));

    @Test
    void createsDirectedEdgeForPartialNameMatch() {
        ExchangePost wantingBurger = post(1, 11, "광주", null,
                want(Category.FOOD, "햄버거", "햄버거", 20_000, 40_000, "햄버거", "버거"));
        ExchangePost providingBurger = post(2, 22, "광주",
                provide(Category.FOOD, "햄버거", "불고기 햄버거 세트", 30_000, "햄버거", "버거"), null);

        MatchGraph graph = builder.build(List.of(wantingBurger, providingBurger));

        assertThat(graph.outgoing(1L)).singleElement().satisfies(edge -> {
            assertThat(edge.to().postId()).isEqualTo(2L);
            assertThat(edge.provideItem().getName()).isEqualTo("불고기 햄버거 세트");
            assertThat(edge.score()).isGreaterThanOrEqualTo(60);
        });
    }

    @Test
    void keepsOnlyHighestScoringItemPairForOrderedPostPair() {
        ExchangePost wanting = post(1, 11, "광주", null,
                want(Category.FOOD, "햄버거", "햄버거", 20_000, 40_000, "햄버거"));
        ExchangePost providing = post(2, 22, "광주",
                provide(Category.FOOD, "치킨", "치킨", 30_000, "치킨"), null);
        providing.addProvideItem(provide(Category.FOOD, "햄버거", "햄버거", 30_000, "햄버거"));

        MatchGraph graph = builder.build(List.of(wanting, providing));

        assertThat(graph.outgoing(1L)).singleElement()
                .extracting(edge -> edge.provideItem().getName()).isEqualTo("햄버거");
    }

    @Test
    void excludesPostsOwnedBySameUser() {
        ExchangePost wanting = post(1, 11, "광주", null,
                want(Category.FOOD, "햄버거", "햄버거", 20_000, 40_000, "햄버거"));
        ExchangePost providing = post(2, 11, "광주",
                provide(Category.FOOD, "햄버거", "햄버거", 30_000, "햄버거"), null);

        assertThat(builder.build(List.of(wanting, providing)).outgoing(1L)).isEmpty();
    }
}
