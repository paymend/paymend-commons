package com.paymend.commons.filter;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.paymend.commons.context.CorrelationContext;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@Slf4j
public class CorrelationFilter implements Filter {

    public static final String REQUEST_ID_HEADER = "request-id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
            if (requestId != null) {
                CorrelationContext.setRequestId(requestId);
                log.debug("Set request-id from header: {}", requestId);
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Always clear ThreadLocal to prevent memory leaks
            CorrelationContext.clear();
        }
    }
}