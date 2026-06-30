package com.valueswap.support;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.WantItem;

import java.util.Arrays;
import java.util.List;

public final class MatchingFixtures {
    private MatchingFixtures() {
    }

    public static ProvideItem provide(Category category, String subCategory, String name,
                                      long value, String... tags) {
        return ProvideItem.create(category, subCategory, name, null, 1, value, Arrays.asList(tags));
    }

    public static WantItem want(Category category, String subCategory, String name,
                                long minValue, long maxValue, String... tags) {
        return WantItem.create(category, subCategory, name, null, 1, minValue, maxValue,
                Arrays.asList(tags));
    }

    public static List<String> tags(String... tags) {
        return Arrays.asList(tags);
    }
}
