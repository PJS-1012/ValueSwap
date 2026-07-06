package com.valueswap.matching.score;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.ValuePolicy;
import com.valueswap.post.domain.WantItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.valueswap.support.MatchingFixtures.provide;
import static com.valueswap.support.MatchingFixtures.want;
import static org.assertj.core.api.Assertions.assertThat;

class ItemMatchScorerTest {
    private ItemMatchScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new ItemMatchScorer(new TextNormalizer(), 60);
    }

    @Test
    void awardsCategoryAndSubCategoryPoints() {
        var result = scorer.score(
                provide(Category.FOOD, "햄버거", "완전히다른제공", 100_000),
                want(Category.FOOD, "햄버거", "희망", 1, 10),
                "광주", "서울", BigDecimal.ZERO);

        assertThat(result.score()).isEqualTo(36);
    }

    @Test
    void exactNameAwardsThirtyWithoutDoubleCountingContainmentOrTokens() {
        var result = scorer.score(
                provide(Category.FOOD, "A", "햄버거 세트", 100_000),
                want(Category.DESIGN, "B", "햄버거 세트", 1, 10),
                "광주", "서울", BigDecimal.ZERO);

        assertThat(result.score()).isEqualTo(21);
    }

    @Test
    void containedNameAwardsTwenty() {
        var result = scorer.score(
                provide(Category.FOOD, "A", "불고기 햄버거 세트", 100_000),
                want(Category.DESIGN, "B", "햄버거", 1, 10),
                "광주", "서울", BigDecimal.ZERO);

        assertThat(result.score()).isEqualTo(14);
    }

    @Test
    void overlappingNameTokenAwardsTen() {
        var result = scorer.score(
                provide(Category.FOOD, "A", "수제 버거 교환권", 100_000),
                want(Category.DESIGN, "B", "버거 식사", 1, 10),
                "광주", "서울", BigDecimal.ZERO);

        assertThat(result.score()).isEqualTo(7);
    }

    @Test
    void tagPointsAreCappedAtTwenty() {
        var result = scorer.score(
                provide(Category.FOOD, "A", "제공", 100_000, "하나", "둘", "셋", "넷", "다섯"),
                want(Category.DESIGN, "B", "희망", 1, 10, "하나", "둘", "셋", "넷", "다섯"),
                "광주", "서울", BigDecimal.ZERO);

        assertThat(result.score()).isEqualTo(14);
    }

    @Test
    void valueInsideRangeAwardsTwentyAndNearRangeAwardsFive() {
        var inside = scorer.score(
                provide(Category.FOOD, "A", "제공", 50_000),
                want(Category.DESIGN, "B", "희망", 40_000, 60_000),
                "광주", "서울", BigDecimal.ZERO);
        var near = scorer.score(
                provide(Category.FOOD, "A", "제공", 35_000),
                want(Category.DESIGN, "B", "희망", 40_000, 60_000),
                "광주", "서울", BigDecimal.ZERO);
        var far = scorer.score(
                provide(Category.FOOD, "A", "제공", 20_000),
                want(Category.DESIGN, "B", "희망", 40_000, 60_000),
                "광주", "서울", BigDecimal.ZERO);

        assertThat(inside.score()).isEqualTo(14);
        assertThat(near.score()).isEqualTo(4);
        assertThat(far.score()).isZero();
    }

    @Test
    void regionAndTrustScoreContributeUpToTenEach() {
        var result = scorer.score(
                provide(Category.FOOD, "A", "제공", 100_000),
                want(Category.DESIGN, "B", "희망", 1, 10),
                " 광주 ", "광주", new BigDecimal("150"));

        assertThat(result.score()).isEqualTo(14);
    }

    @Test
    void partialBurgerNameIsEligible() {
        var result = scorer.score(
                provide(Category.FOOD, "햄버거", "불고기 햄버거 세트", 30_000, "햄버거", "버거", "세트"),
                want(Category.FOOD, "햄버거", "햄버거", 20_000, 40_000, "햄버거", "버거"),
                "광주", "광주", new BigDecimal("50"));

        assertThat(result.eligible()).isTrue();
        assertThat(result.score()).isGreaterThanOrEqualTo(60);
    }

    @Test
    void categoryOnlyNeverCreatesEdgeEvenWhenScoreReachesThreshold() {
        var result = scorer.score(
                provide(Category.FOOD, "치킨", "후라이드", 20_000),
                want(Category.FOOD, "한식", "비빔밥", 15_000, 25_000),
                "광주", "광주", new BigDecimal("50"));

        assertThat(result.score()).isEqualTo(43);
        assertThat(result.eligible()).isFalse();
    }

    @Test
    void nonNumericValuePoliciesSkipValuePointsAndAllowEmptySubCategory() {
        ProvideItem provide = ProvideItem.create(Category.SERVICE, null, "메뉴판 디자인", null,
                1, null, ValuePolicy.NEGOTIABLE, List.of("디자인"));
        WantItem want = WantItem.create(Category.SERVICE, null, "메뉴판 디자인", null,
                1, null, null, ValuePolicy.OFFER_REQUESTED, List.of("디자인"));

        var result = scorer.score(provide, want, "광주광역시", "광주광역시", BigDecimal.ZERO);

        assertThat(result.score()).isEqualTo(74);
        assertThat(result.eligible()).isTrue();
    }

    @Test
    void perfectMatchIsNormalizedToOneHundred() {
        var result = scorer.score(
                provide(Category.FOOD, "햄버거", "햄버거 세트", 30_000, "하나", "둘", "셋", "넷"),
                want(Category.FOOD, "햄버거", "햄버거 세트", 20_000, 40_000, "하나", "둘", "셋", "넷"),
                "광주", "광주", new BigDecimal("100"));

        assertThat(result.score()).isEqualTo(100);
    }

    @Test
    void unavailableOptionalCriteriaAreExcludedFromMaximumScore() {
        ProvideItem provide = ProvideItem.create(Category.SERVICE, null, "메뉴판 디자인", null,
                1, null, ValuePolicy.NEGOTIABLE, List.of("디자인", "메뉴판", "인쇄", "식당"));
        WantItem want = WantItem.create(Category.SERVICE, null, "메뉴판 디자인", null,
                1, null, null, ValuePolicy.OFFER_REQUESTED, List.of("디자인", "메뉴판", "인쇄", "식당"));

        var result = scorer.score(provide, want, "광주", "광주", new BigDecimal("100"));

        assertThat(result.score()).isEqualTo(100);
    }
}
