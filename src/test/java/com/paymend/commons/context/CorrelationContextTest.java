package com.paymend.commons.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CorrelationContextTest {

    @BeforeEach
    void setUp() {
        CorrelationContext.clear();
    }

    @Test
    void shouldStoreAndRetrieveRequestId() {
        // Given
        String requestId = "REQ-123456789ABC";
        
        // When
        CorrelationContext.setRequestId(requestId);
        
        // Then
        assertThat(CorrelationContext.getRequestId()).isEqualTo(requestId);
        assertThat(CorrelationContext.hasRequestId()).isTrue();
    }

    @Test
    void shouldReturnNullWhenNoRequestId() {
        // When & Then
        assertThat(CorrelationContext.getRequestId()).isNull();
        assertThat(CorrelationContext.hasRequestId()).isFalse();
    }

    @Test
    void shouldReturnDefaultWhenNoRequestId() {
        // Given
        String defaultValue = "DEFAULT-123";
        
        // When & Then
        assertThat(CorrelationContext.getRequestIdOrDefault(defaultValue)).isEqualTo(defaultValue);
    }

    @Test
    void shouldReturnActualValueWhenRequestIdExists() {
        // Given
        String requestId = "REQ-123456789ABC";
        String defaultValue = "DEFAULT-123";
        CorrelationContext.setRequestId(requestId);
        
        // When & Then
        assertThat(CorrelationContext.getRequestIdOrDefault(defaultValue)).isEqualTo(requestId);
    }

    @Test
    void shouldClearRequestId() {
        // Given
        CorrelationContext.setRequestId("REQ-123456789ABC");
        assertThat(CorrelationContext.hasRequestId()).isTrue();
        
        // When
        CorrelationContext.clear();
        
        // Then
        assertThat(CorrelationContext.getRequestId()).isNull();
        assertThat(CorrelationContext.hasRequestId()).isFalse();
    }

    @Test
    void shouldIgnoreNullRequestId() {
        // When
        CorrelationContext.setRequestId(null);
        
        // Then
        assertThat(CorrelationContext.getRequestId()).isNull();
        assertThat(CorrelationContext.hasRequestId()).isFalse();
    }

    @Test
    void shouldIgnoreEmptyRequestId() {
        // When
        CorrelationContext.setRequestId("");
        
        // Then
        assertThat(CorrelationContext.getRequestId()).isNull();
        assertThat(CorrelationContext.hasRequestId()).isFalse();
    }

    @Test
    void shouldIgnoreBlankRequestId() {
        // When
        CorrelationContext.setRequestId("   ");
        
        // Then
        assertThat(CorrelationContext.getRequestId()).isNull();
        assertThat(CorrelationContext.hasRequestId()).isFalse();
    }
}