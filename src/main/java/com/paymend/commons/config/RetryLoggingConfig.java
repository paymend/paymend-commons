package com.paymend.commons.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RetryLoggingConfig {

    @Bean
    public RetryListener retryLogger() {
        return new RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                log.warn("Retry attempt #{} failed due to: {}",
                         context.getRetryCount(),
                         throwable.getMessage());
            }
            
            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                if (throwable != null) {
                    log.error("Retry operation failed after {} attempts with error: {}", 
                             context.getRetryCount(), throwable.getMessage());
                } else {
                    log.info("Retry operation completed successfully after {} attempts", 
                            context.getRetryCount());
                }
            }
            
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                log.debug("Starting retry operation");
                return true;
            }
        };
    }
}