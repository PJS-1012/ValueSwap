package com.valueswap.trade;

import com.valueswap.auth.AuthenticatedUser;
import com.valueswap.auth.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class TradeRoomStompInterceptor implements ChannelInterceptor {
    private static final Pattern ROOM_DESTINATION = Pattern.compile("/(?:app|topic)/trade-rooms/(\\d+)(?:/messages)?");
    private final JwtTokenProvider tokenProvider;
    private final TradeRoomMessageService messageService;

    public TradeRoomStompInterceptor(JwtTokenProvider tokenProvider, TradeRoomMessageService messageService) {
        this.tokenProvider = tokenProvider;
        this.messageService = messageService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;
        if (accessor.getCommand() == StompCommand.CONNECT) authenticate(accessor);
        if (accessor.getCommand() == StompCommand.SUBSCRIBE || accessor.getCommand() == StompCommand.SEND) {
            authorizeRoom(accessor);
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AccessDeniedException("WebSocket 인증이 필요합니다.");
        }
        AuthenticatedUser user;
        try {
            user = tokenProvider.parse(authorization.substring(7));
        } catch (RuntimeException exception) {
            throw new AccessDeniedException("유효하지 않은 WebSocket 인증입니다.");
        }
        accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    private void authorizeRoom(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication)
                || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AccessDeniedException("WebSocket 인증이 필요합니다.");
        }
        String destination = accessor.getDestination();
        var matcher = ROOM_DESTINATION.matcher(destination == null ? "" : destination);
        if (!matcher.matches() || !messageService.isMember(Long.valueOf(matcher.group(1)), user.userId())) {
            throw new AccessDeniedException("거래방 참여자만 접근할 수 있습니다.");
        }
    }
}
