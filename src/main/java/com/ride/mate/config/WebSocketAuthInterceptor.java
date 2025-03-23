package com.ride.mate.config;
import com.ride.mate.service.impl.CustomUserDetailsService;
import com.ride.mate.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
/**
 * WebSocket Authentication Interceptor
 * Validates JWT token from STOMP CONNECT frame headers.
 *
 * The Flutter client sends:
 *   CONNECT
 *   Authorization: Bearer <jwt-token>
 *
 * This interceptor extracts and validates the token, then sets
 * the Spring Security authentication for the WebSocket session.
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    public WebSocketAuthInterceptor(JwtUtil jwtUtil,
                                     CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                try {
                    if (jwtUtil.validateToken(jwt) && jwtUtil.isAccessToken(jwt)) {
                        String email = jwtUtil.extractEmail(jwt);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        if (jwtUtil.validateToken(jwt, email)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(authentication);
                            log.debug("[WS Auth] Authenticated WebSocket user: {}", email);
                        }
                    }
                } catch (Exception e) {
                    log.warn("[WS Auth] JWT validation failed: {}", e.getMessage());
                }
            } else {
                log.debug("[WS Auth] No Authorization header in STOMP CONNECT");
            }
        }
        return message;
    }
}
