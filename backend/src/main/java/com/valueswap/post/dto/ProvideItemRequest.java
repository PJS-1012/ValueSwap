package com.valueswap.post.dto;

import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.ValuePolicy;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProvideItemRequest(
        @NotNull Category category,
        @Size(max = 100) String subCategory,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull @Min(1) Integer quantity,
        @PositiveOrZero Long estimatedValue,
        ValuePolicy valuePolicy,
        @Size(max = 20) List<@NotBlank @Size(max = 100) String> tags
) {
    @AssertTrue(message = "직접 입력을 선택한 경우 가치를 입력해야 합니다.")
    public boolean isValueValid() {
        return resolvedValuePolicy() != ValuePolicy.DIRECT || estimatedValue != null;
    }

    public ProvideItem toEntity() {
        return ProvideItem.create(category, normalize(subCategory), name.trim(), description,
                quantity, estimatedValue, resolvedValuePolicy(),
                tags == null ? List.of() : tags.stream().map(String::trim).toList());
    }

    private ValuePolicy resolvedValuePolicy() {
        return valuePolicy == null ? ValuePolicy.DIRECT : valuePolicy;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
