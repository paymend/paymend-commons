package com.paymend.commons.context;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CorrelationContext {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>() {
        @Override
        protected String initialValue() {
            return null;
        }
        
        @Override
        public void remove() {
            super.remove();
            log.trace("ThreadLocal REQUEST_ID removed for thread: {}", Thread.currentThread().getName());
        }
    };

    public static void setRequestId(String requestId) {
        if (StringUtils.hasText(requestId)) {
            REQUEST_ID.set(requestId);
            log.trace("Set request-id in context: {}", requestId);
        }
    }

    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    public static void clear() {
        REQUEST_ID.remove();
        log.trace("Cleared correlation context");
    }

    public static boolean hasRequestId() {
        return StringUtils.hasText(REQUEST_ID.get());
    }

    public static String getRequestIdOrDefault(String defaultValue) {
        String requestId = getRequestId();
        return StringUtils.hasText(requestId) ? requestId : defaultValue;
    }
}