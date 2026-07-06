package com.valueswap.post.dto;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.WantItem;
import com.valueswap.post.domain.ValuePolicy;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WantItemRequest(
        @NotNull Category category,
        @Size(max = 100) String subCategory,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull @Min(1) Integer quantity,
        @PositiveOrZero Long minValue,
        @PositiveOrZero Long maxValue,
        ValuePolicy valuePolicy,
        @Size(max = 20) List<@NotBlank @Size(max = 100) String> tags
) {
    @AssertTrue(message = "최소 가치는 최대 가치보다 클 수 없습니다.")
    public boolean isValueRangeValid() {
        return resolvedValuePolicy() != ValuePolicy.DIRECT
                || minValue == null || maxValue == null || minValue <= maxValue;
    }

    @AssertTrue(message = "직접 입력을 선택한 경우 최소·최대 가치를 입력해야 합니다.")
    public boolean isValuePresent() {
        return resolvedValuePolicy() != ValuePolicy.DIRECT || minValue != null && maxValue != null;
    }

    public WantItem toEntity() {
        return WantItem.create(category, normalize(subCategory), name.trim(), description,
                quantity, minValue, maxValue, resolvedValuePolicy(),
                tags == null ? List.of() : tags.stream().map(String::trim).toList());
    }

    private ValuePolicy resolvedValuePolicy() {
        return valuePolicy == null ? ValuePolicy.DIRECT : valuePolicy;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
