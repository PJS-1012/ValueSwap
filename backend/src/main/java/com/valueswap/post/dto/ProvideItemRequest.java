package com.valueswap.post.dto;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ProvideItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProvideItemRequest(
        @NotNull Category category,
        @NotBlank @Size(max = 100) String subCategory,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull @Min(1) Integer quantity,
        @NotNull @PositiveOrZero Long estimatedValue,
        @Size(max = 20) List<@NotBlank @Size(max = 100) String> tags
) {
    public ProvideItem toEntity() {
        return ProvideItem.create(category, subCategory.trim(), name.trim(), description,
                quantity, estimatedValue, tags == null ? List.of() : tags.stream().map(String::trim).toList());
    }
}
