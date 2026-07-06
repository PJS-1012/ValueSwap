package com.valueswap.trade;

import com.valueswap.auth.AuthenticatedUser;
import com.valueswap.auth.JwtTokenProvider;
import com.valueswap.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeRoomStompInterceptorTest {

    @Test
    void connectAuthenticationIsStoredInOriginalMessage() {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        TradeRoomMessageService messageService = mock(TradeRoomMessageService.class);
        when(tokenProvider.parse("valid-token")).thenReturn(new AuthenticatedUser(1L, UserRole.USER));
        TradeRoomStompInterceptor interceptor = new TradeRoomStompInterceptor(tokenProvider, messageService);
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        headers.setNativeHeader("Authorization", "Bearer valid-token");
        headers.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        StompHeaderAccessor resultHeaders = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertThat(resultHeaders).isNotNull();
        assertThat(resultHeaders.getUser()).isNotNull();
        assertThat(((Authentication) resultHeaders.getUser()).getPrincipal())
                .isEqualTo(new AuthenticatedUser(1L, UserRole.USER));
    }
}
