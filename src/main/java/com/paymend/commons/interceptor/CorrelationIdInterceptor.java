package com.paymend.commons.interceptor;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import com.paymend.commons.context.CorrelationContext;
import com.paymend.commons.config.RestTemplateConfigProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    private final RestTemplateConfigProperties.Correlation correlationConfig;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        if (!correlationConfig.isEnabled()) {
            return execution.execute(request, body);
        }

        // Handle request-id
        String requestId = getOrCreateRequestId(request);
        request.getHeaders().set(correlationConfig.getRequestIdHeader(), requestId);

        // Store in context for downstream usage
        CorrelationContext.setRequestId(requestId);

        log.debug("Outbound request [{}] {} {}", requestId, request.getMethod(), request.getURI());

        try {
            return execution.execute(request, body);
        } finally {
            // Context cleanup is handled by CorrelationContext itself
        }
    }

    private String getOrCreateRequestId(HttpRequest request) {
        String headerName = correlationConfig.getRequestIdHeader();
        
        // First, check if request-id already exists in headers
        String existingRequestId = request.getHeaders().getFirst(headerName);
        
        if (StringUtils.hasText(existingRequestId)) {
            log.debug("Using existing request-id from header: {}", existingRequestId);
            return existingRequestId;
        }

        // Second, check if request-id exists in correlation context
        String contextRequestId = CorrelationContext.getRequestId();
        if (StringUtils.hasText(contextRequestId)) {
            log.debug("Using request-id from context: {}", contextRequestId);
            return contextRequestId;
        }

        // Fallback: generate new request-id
        String fallbackRequestId = generateRequestId();
        log.warn("No request-id found in header or context, generated fallback: {}", fallbackRequestId);
        return fallbackRequestId;
    }

    private String generateRequestId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        int length = Math.min(correlationConfig.getRequestIdLength(), uuid.length());
        return correlationConfig.getRequestIdPrefix() + uuid.substring(0, length).toUpperCase();
    }
}