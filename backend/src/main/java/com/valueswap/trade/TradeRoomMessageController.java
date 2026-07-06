package com.valueswap.trade;

import com.valueswap.auth.AuthenticatedUser;
import com.valueswap.trade.dto.ChatMessageRequest;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class TradeRoomMessageController {
    private final TradeRoomMessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public TradeRoomMessageController(TradeRoomMessageService messageService,
                                      SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/trade-rooms/{roomId}/messages")
    public void send(@DestinationVariable Long roomId, @Valid ChatMessageRequest request, Principal principal) {
        AuthenticatedUser user = (AuthenticatedUser) ((AbstractAuthenticationToken) principal).getPrincipal();
        var response = messageService.send(roomId, user.userId(), request.clientMessageId(), request.content());
        messagingTemplate.convertAndSend("/topic/trade-rooms/" + roomId, response);
    }
}
