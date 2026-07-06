package com.valueswap.matching.score;

import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.WantItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ItemMatchScorer {
    private final TextNormalizer normalizer;
    private final int threshold;

    public ItemMatchScorer(TextNormalizer normalizer,
                           @Value("${valueswap.matching.threshold:60}") int threshold) {
        this.normalizer = normalizer;
        this.threshold = threshold;
    }

    public ItemMatchResult score(ProvideItem provide, WantItem want,
                                 String provideRegion, String wantRegion,
                                 BigDecimal providerTrustScore) {
        boolean sameCategory = provide.getCategory() == want.getCategory();
        boolean sameSubCategory = normalizer.same(provide.getSubCategory(), want.getSubCategory());
        NameMatch nameMatch = normalizer.compareNames(provide.getName(), want.getName());
        int tagOverlap = normalizer.overlap(provide.getTags(), want.getTags());

        boolean subCategoryApplicable = hasText(provide.getSubCategory()) && hasText(want.getSubCategory());
        boolean valueApplicable = provide.getEstimatedValue() != null
                && want.getMinValue() != null && want.getMaxValue() != null;
        int rawScore = (sameCategory ? 25 : 0)
                + (sameSubCategory ? 25 : 0)
                + nameMatch.points()
                + Math.min(tagOverlap * 5, 20)
                + valuePoints(provide.getEstimatedValue(), want.getMinValue(), want.getMaxValue())
                + (normalizer.same(provideRegion, wantRegion) ? 10 : 0)
                + trustPoints(providerTrustScore);
        int maximumScore = 95 + (subCategoryApplicable ? 25 : 0) + (valueApplicable ? 20 : 0);
        int score = Math.min(100, (int) Math.round(rawScore * 100.0 / maximumScore));

        boolean meaningfullyRelated = sameSubCategory || nameMatch.related() || tagOverlap > 0;
        return new ItemMatchResult(score, sameCategory && meaningfullyRelated && score >= threshold);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int valuePoints(Long estimatedValue, Long minValue, Long maxValue) {
        if (estimatedValue == null || minValue == null || maxValue == null) {
            return 0;
        }
        if (estimatedValue >= minValue && estimatedValue <= maxValue) {
            return 20;
        }
        if (estimatedValue < minValue) {
            return minValue - estimatedValue <= Math.max(1, minValue / 5) ? 5 : 0;
        }
        return estimatedValue - maxValue <= Math.max(1, maxValue / 5) ? 5 : 0;
    }

    private int trustPoints(BigDecimal trustScore) {
        if (trustScore == null) {
            return 0;
        }
        return trustScore.max(BigDecimal.ZERO)
                .min(BigDecimal.valueOf(100))
                .divide(BigDecimal.TEN, 0, RoundingMode.DOWN)
                .intValue();
    }
}
