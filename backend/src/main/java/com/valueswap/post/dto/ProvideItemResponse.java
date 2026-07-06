package com.valueswap.post.dto;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.ValuePolicy;

import java.util.List;

public record ProvideItemResponse(Long id, Category category, String subCategory, String name,
                                  String description, Integer quantity, Long estimatedValue,
                                  ValuePolicy valuePolicy, List<String> tags) {
    public static ProvideItemResponse from(ProvideItem item) {
        return new ProvideItemResponse(item.getId(), item.getCategory(), item.getSubCategory(),
                item.getName(), item.getDescription(), item.getQuantity(), item.getEstimatedValue(), item.getValuePolicy(),
                List.copyOf(item.getTags()));
    }
}
