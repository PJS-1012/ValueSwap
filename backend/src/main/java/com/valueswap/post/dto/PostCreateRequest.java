package com.valueswap.post.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 100) String region,
        @NotEmpty List<@Valid ProvideItemRequest> provideItems,
        @NotEmpty List<@Valid WantItemRequest> wantItems
) {
}
