package com.valueswap.support;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.WantItem;
import com.valueswap.user.User;
import org.springframework.test.util.ReflectionTestUtils;

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

    public static ExchangePost post(long postId, long userId, String region,
                                    ProvideItem provide, WantItem want) {
        User user = User.create("user" + userId + "@test.com", "encoded", "사용자" + userId, "닉네임" + userId);
        ReflectionTestUtils.setField(user, "id", userId);
        ExchangePost post = ExchangePost.create(user, "교환 글 " + postId, "설명", region);
        ReflectionTestUtils.setField(post, "id", postId);
        if (provide != null) {
            post.addProvideItem(provide);
        }
        if (want != null) {
            post.addWantItem(want);
        }
        return post;
    }
}
