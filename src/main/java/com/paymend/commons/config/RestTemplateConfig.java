package com.paymend.commons.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import com.paymend.commons.config.RestTemplateConfigProperties.Retry;
import com.paymend.commons.exception.RetryableHttpException;
import com.paymend.commons.interceptor.CorrelationIdInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableRetry
public class RestTemplateConfig {

    private final RestTemplateConfigProperties restTemplateConfigProperties;

    @Bean
    public RetryTemplate retryTemplate(RetryListener retryListener) {
        RetryTemplate retryTemplate = new RetryTemplate();

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(IOException.class, true);
        retryableExceptions.put(RetryableHttpException.class, true);

        Retry retry = restTemplateConfigProperties.getRetry();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(retry.getMaxAttempts(), retryableExceptions);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retry.getInitialInterval().toMillis());
        backOffPolicy.setMaxInterval(retry.getMaxInterval().toMillis());
        backOffPolicy.setMultiplier(retry.getMultiplier());

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.registerListener(retryListener);
        
        return retryTemplate;
    }

    @Bean
    public RestTemplate restTemplate() {
        return restTemplateBuilder().build();
    }

    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder()
                .requestFactory(this::createRequestFactory)
                .interceptors(correlationIdInterceptor(), loggingInterceptor());
    }

    @Bean
    public CorrelationIdInterceptor correlationIdInterceptor() {
        return new CorrelationIdInterceptor(restTemplateConfigProperties.getCorrelation());
    }

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) restTemplateConfigProperties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) restTemplateConfigProperties.getReadTimeout().toMillis());
        
        return new BufferingClientHttpRequestFactory(factory);
    }

    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            log.info("[RestTemplate Request] {} {}", request.getMethod(), request.getURI());
            log.info("[RestTemplate Request] Headers: {}", request.getHeaders());
            if (body != null && body.length > 0) {
                log.info("[RestTemplate Request] Body: {}", new String(body, StandardCharsets.UTF_8));
            }

            ClientHttpResponse response = execution.execute(request, body);

            if (isHttpStatusRetryable(response.getStatusCode())) {
                throw new RetryableHttpException("Retryable status code: " + response.getStatusCode().value());
            }
            
            HttpHeaders headers = response.getHeaders();
            StringBuilder sb = new StringBuilder();
            sb.append("[RestTemplate Response] Status code: ")
              .append(response.getStatusCode().value())
              .append("\nHeaders:\n");

            headers.forEach((key, values) ->
                    values.forEach(value -> sb.append(key).append(": ").append(value).append("\n")));

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                String responseBody = reader.lines().collect(Collectors.joining("\n"));
                if (!responseBody.isBlank()) {
                    sb.append("Body:\n").append(responseBody);
                }
                log.info(sb.toString());
                
                return new BufferedClientHttpResponse(response, responseBody);
            } catch (IOException e) {
                log.warn("Could not read response body for logging: {}", e.getMessage());
                return response;
            }
        };
    }

    private boolean isHttpStatusRetryable(HttpStatusCode httpStatusCode) {
        return getRetryableStatusCodes().contains(httpStatusCode);
    }

    private Set<HttpStatusCode> getRetryableStatusCodes() {
        return restTemplateConfigProperties.getRetry().getRetryableStatusCodes().stream()
            .map(HttpStatusCode::valueOf)
            .collect(Collectors.toSet());
    }

    @RequiredArgsConstructor
    private static class BufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse originalResponse;
        private final String responseBody;

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public HttpHeaders getHeaders() {
            return originalResponse.getHeaders();
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return originalResponse.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return originalResponse.getStatusText();
        }

        @Override
        public void close() {
            originalResponse.close();
        }
    }
}