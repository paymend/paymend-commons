package com.paymend.commons.client;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymend.commons.config.MaskingConfigProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RestTemplateService {

    private final ObjectMapper objectMapper;
    private final MaskingConfigProperties maskingConfigProperties;

    public <T> ResponseEntity<T> postWithBodyLogging(RestTemplate restTemplate, Object requestBody, String uri,
            Map<String, String> headers, Class<T> responseType) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            log.info("POST {}", uri);
            headers.forEach((k, v) -> log.info("Header: {} = {}", k, maskHeaderValue(k, v)));
            log.info("Request Body: {}", jsonBody);

            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::set);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, httpHeaders);
            
            ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, responseType);
            
            log.info("Response Status: {}", response.getStatusCode());
            return response;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize request body", e);
            throw new RuntimeException("Failed to serialize request body", e);
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during API call: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Error during API call", e);
            throw new RuntimeException("Error during API call", e);
        }
    }

    public <T> ResponseEntity<T> getWithHeaderLogging(RestTemplate restTemplate, String uri,
            Map<String, String> headers, Class<T> responseType) {
        
        log.info("GET {}", uri);
        headers.forEach((k, v) -> log.info("Header: {} = {}", k, maskHeaderValue(k, v)));

        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::set);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, responseType);
            log.info("Response Status: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during API call: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Error during API call", e);
            throw new RuntimeException("Error during API call", e);
        }
    }

    public <T> ResponseEntity<T> putWithBodyLogging(RestTemplate restTemplate, Object requestBody, String uri,
            Map<String, String> headers, Class<T> responseType) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            log.info("PUT {}", uri);
            headers.forEach((k, v) -> log.info("Header: {} = {}", k, maskHeaderValue(k, v)));
            log.info("Request Body: {}", jsonBody);

            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::set);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, httpHeaders);
            
            ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, responseType);
            
            log.info("Response Status: {}", response.getStatusCode());
            return response;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize request body", e);
            throw new RuntimeException("Failed to serialize request body", e);
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during API call: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Error during API call", e);
            throw new RuntimeException("Error during API call", e);
        }
    }

    public <T> ResponseEntity<T> deleteWithHeaderLogging(RestTemplate restTemplate, String uri,
            Map<String, String> headers, Class<T> responseType) {
        
        log.info("DELETE {}", uri);
        headers.forEach((k, v) -> log.info("Header: {} = {}", k, maskHeaderValue(k, v)));

        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::set);
        
        HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.DELETE, requestEntity, responseType);
            log.info("Response Status: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during API call: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Error during API call", e);
            throw new RuntimeException("Error during API call", e);
        }
    }

    protected String maskHeaderValue(String headerName, String headerValue) {
        if (headerName == null) {
            return headerValue;
        }

        Set<String> sensitiveHeaders = maskingConfigProperties.getSensitiveHeadersNormalized();

        if (sensitiveHeaders.contains(headerName.trim().toLowerCase())) {
            return "*** MASKED ***";
        }
        return headerValue;
    }
}