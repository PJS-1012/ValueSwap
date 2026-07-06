package com.valueswap.trade;

import com.valueswap.auth.AuthenticatedUser;
import com.valueswap.trade.dto.TradeRoomSummaryResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.valueswap.trade.dto.ChatMessagePageResponse;
import com.valueswap.trade.dto.TradeRoomDetailResponse;

@RestController
@RequestMapping("/api/trade-rooms")
public class TradeRoomController {
    private final TradeRoomService tradeRoomService;

    public TradeRoomController(TradeRoomService tradeRoomService) {
        this.tradeRoomService = tradeRoomService;
    }

    @GetMapping
    public List<TradeRoomSummaryResponse> findMine(@AuthenticationPrincipal AuthenticatedUser user) {
        return tradeRoomService.findMine(user.userId());
    }

    @GetMapping("/{roomId}")
    public TradeRoomDetailResponse findOne(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable Long roomId) {
        return tradeRoomService.findOne(roomId, user.userId());
    }

    @GetMapping("/{roomId}/messages")
    public ChatMessagePageResponse findMessages(@AuthenticationPrincipal AuthenticatedUser user,
                                                @PathVariable Long roomId,
                                                @RequestParam(required = false) Long beforeId,
                                                @RequestParam(required = false) Long afterId,
                                                @RequestParam(defaultValue = "50") int size) {
        return tradeRoomService.findMessages(roomId, user.userId(), beforeId, afterId, size);
    }

    @PatchMapping("/{roomId}/read")
    public TradeRoomDetailResponse markRead(@AuthenticationPrincipal AuthenticatedUser user,
                                            @PathVariable Long roomId) {
        return tradeRoomService.markRead(roomId, user.userId());
    }

    @PostMapping("/{roomId}/complete")
    public TradeRoomDetailResponse complete(@AuthenticationPrincipal AuthenticatedUser user,
                                            @PathVariable Long roomId) {
        return TradeRoomDetailResponse.from(tradeRoomService.complete(roomId, user.userId()));
    }
}
