package com.paymend.commons.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "paymend.masking")
@Data
public class MaskingConfigProperties {

    private List<String> sensitiveHeaders = List.of("authorization", "x-api-key", "x-auth-token");
    
    public Set<String> getSensitiveHeadersNormalized() {
        if (sensitiveHeaders == null) {
            return Set.of();
        }
        return sensitiveHeaders.stream()
                .map(h -> h.trim().toLowerCase())
                .collect(Collectors.toSet());
    }
}