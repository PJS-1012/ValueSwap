package com.valueswap.trade.dto;

import java.util.List;

public record ChatMessagePageResponse(List<ChatMessageResponse> messages, boolean hasMore) {
}
