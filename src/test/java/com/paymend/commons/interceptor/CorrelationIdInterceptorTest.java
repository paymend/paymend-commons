package com.paymend.commons.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import com.paymend.commons.config.RestTemplateConfigProperties;
import com.paymend.commons.context.CorrelationContext;

@ExtendWith(MockitoExtension.class)
class CorrelationIdInterceptorTest {

    @Mock
    private HttpRequest request;
    
    @Mock
    private ClientHttpRequestExecution execution;
    
    @Mock
    private ClientHttpResponse response;
    
    private HttpHeaders headers;
    private RestTemplateConfigProperties.Correlation correlationConfig;
    private CorrelationIdInterceptor interceptor;
    
    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        correlationConfig = new RestTemplateConfigProperties.Correlation();
        interceptor = new CorrelationIdInterceptor(correlationConfig);
        
        // Clear context before each test
        CorrelationContext.clear();
    }

    @Test
    void shouldPassThroughWhenDisabled() throws IOException {
        // Given
        correlationConfig.setEnabled(false);
        when(execution.execute(request, new byte[]{})).thenReturn(response);
        
        // When
        ClientHttpResponse result = interceptor.intercept(request, new byte[]{}, execution);
        
        // Then
        assertThat(result).isEqualTo(response);
        verify(execution).execute(request, new byte[]{});
    }

    @Test
    void shouldUseExistingRequestIdFromHeader() throws IOException {
        // Given
        String existingRequestId = "EXISTING-123";
        headers.set("request-id", existingRequestId);
        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, new byte[]{})).thenReturn(response);
        
        // When
        interceptor.intercept(request, new byte[]{}, execution);
        
        // Then
        assertThat(headers.getFirst("request-id")).isEqualTo(existingRequestId);
        assertThat(CorrelationContext.getRequestId()).isEqualTo(existingRequestId);
    }

    @Test
    void shouldUseRequestIdFromContext() throws IOException {
        // Given
        String contextRequestId = "CONTEXT-456";
        CorrelationContext.setRequestId(contextRequestId);
        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, new byte[]{})).thenReturn(response);
        
        // When
        interceptor.intercept(request, new byte[]{}, execution);
        
        // Then
        assertThat(headers.getFirst("request-id")).isEqualTo(contextRequestId);
        assertThat(CorrelationContext.getRequestId()).isEqualTo(contextRequestId);
    }

    @Test
    void shouldGenerateFallbackRequestId() throws IOException {
        // Given
        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, new byte[]{})).thenReturn(response);
        
        // When
        interceptor.intercept(request, new byte[]{}, execution);
        
        // Then
        String generatedRequestId = headers.getFirst("request-id");
        assertThat(generatedRequestId).isNotNull();
        assertThat(generatedRequestId).startsWith("REQ-");
        assertThat(generatedRequestId).hasSize(16); // REQ- + 12 chars
        assertThat(CorrelationContext.getRequestId()).isEqualTo(generatedRequestId);
    }

    @Test
    void shouldUseCustomHeaderName() throws IOException {
        // Given
        String customHeader = "x-trace-id";
        correlationConfig.setRequestIdHeader(customHeader);
        String existingRequestId = "TRACE-789";
        headers.set(customHeader, existingRequestId);
        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, new byte[]{})).thenReturn(response);
        
        // When
        interceptor.intercept(request, new byte[]{}, execution);
        
        // Then
        assertThat(headers.getFirst(customHeader)).isEqualTo(existingRequestId);
        assertThat(headers.getFirst("request-id")).isNull();
    }

    @Test
    void shouldUseCustomPrefixAndLength() throws IOException {
        // Given
        correlationConfig.setRequestIdPrefix("TXN-");
        correlationConfig.setRequestIdLength(8);
        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, new byte[]{})).thenReturn(response);
        
        // When
        interceptor.intercept(request, new byte[]{}, execution);
        
        // Then
        String generatedRequestId = headers.getFirst("request-id");
        assertThat(generatedRequestId).isNotNull();
        assertThat(generatedRequestId).startsWith("TXN-");
        assertThat(generatedRequestId).hasSize(12); // TXN- + 8 chars
    }

    @Test
    void shouldPreferHeaderOverContext() throws IOException {
        // Given
        String headerRequestId = "HEADER-123";
        String contextRequestId = "CONTEXT-456";
        headers.set("request-id", headerRequestId);
        CorrelationContext.setRequestId(contextRequestId);
        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, new byte[]{})).thenReturn(response);
        
        // When
        interceptor.intercept(request, new byte[]{}, execution);
        
        // Then
        assertThat(headers.getFirst("request-id")).isEqualTo(headerRequestId);
        assertThat(CorrelationContext.getRequestId()).isEqualTo(headerRequestId);
    }
}