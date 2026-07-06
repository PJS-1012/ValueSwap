package com.valueswap.trade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank @Size(max = 36) String clientMessageId,
        @NotBlank @Size(max = 1000) String content) {
}
