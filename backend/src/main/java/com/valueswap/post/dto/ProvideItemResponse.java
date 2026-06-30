package com.valueswap.post.dto;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ProvideItem;

import java.util.List;

public record ProvideItemResponse(Long id, Category category, String subCategory, String name,
                                  String description, Integer quantity, Long estimatedValue,
                                  List<String> tags) {
    public static ProvideItemResponse from(ProvideItem item) {
        return new ProvideItemResponse(item.getId(), item.getCategory(), item.getSubCategory(),
                item.getName(), item.getDescription(), item.getQuantity(), item.getEstimatedValue(),
                List.copyOf(item.getTags()));
    }
}
