package com.valueswap.post.dto;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.WantItem;
import com.valueswap.post.domain.ValuePolicy;

import java.util.List;

public record WantItemResponse(Long id, Category category, String subCategory, String name,
                               String description, Integer quantity, Long minValue, Long maxValue,
                               ValuePolicy valuePolicy, List<String> tags) {
    public static WantItemResponse from(WantItem item) {
        return new WantItemResponse(item.getId(), item.getCategory(), item.getSubCategory(),
                item.getName(), item.getDescription(), item.getQuantity(), item.getMinValue(),
                item.getMaxValue(), item.getValuePolicy(), List.copyOf(item.getTags()));
    }
}
