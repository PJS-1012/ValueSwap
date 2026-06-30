package com.valueswap.post.dto;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.WantItem;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WantItemRequest(
        @NotNull Category category,
        @NotBlank @Size(max = 100) String subCategory,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull @Min(1) Integer quantity,
        @NotNull @PositiveOrZero Long minValue,
        @NotNull @PositiveOrZero Long maxValue,
        @Size(max = 20) List<@NotBlank @Size(max = 100) String> tags
) {
    @AssertTrue(message = "최소 가치는 최대 가치보다 클 수 없습니다.")
    public boolean isValueRangeValid() {
        return minValue == null || maxValue == null || minValue <= maxValue;
    }

    public WantItem toEntity() {
        return WantItem.create(category, subCategory.trim(), name.trim(), description,
                quantity, minValue, maxValue, tags == null ? List.of() : tags.stream().map(String::trim).toList());
    }
}
