package com.ride.mate.core;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * LoggingHandler
 * Aspect for logging and setting request headers for controllers
 *
 * Note: LoginAuthentication is handled by AuthenticationFilter.
 * This class only handles request header logging.
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 02-03-2026    N/A          N/A          Tishan          Removed LoginAuthentication (handled by AuthenticationFilter)
 * 3 18-03-2026    N/A          N/A          Tishan          Fixed userName overwrite - read from LoginAuthentication instead of header
 */
@Slf4j
@Aspect
@Component
public class LoggingHandler {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    private void pointcutController() {}

    @Before("pointcutController()")
    public void logBeforeController(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String userAgent = request.getHeader("user-agent");
        String requestId = request.getHeader("x-request-id");
        String traceId = request.getHeader("x-b3-traceid");
        String spanId = request.getHeader("x-b3-spanid");
        String parentspanId = request.getHeader("x-b3-parentspanid");
        String sampled = request.getHeader("x-b3-sampled");
        String flags = request.getHeader("x-b3-flags");
        String spanContext = request.getHeader("x-ot-span-context");
        String userName = LoginAuthentication.getUserName();

        DefaultRequestHeaders.getInstance().setHeaders(userAgent, requestId, traceId, spanId,
            parentspanId, sampled, flags, spanContext, userName);

        log.debug("Request: {} {} - User: {}", request.getMethod(), request.getRequestURI(), userName);
    }
}
