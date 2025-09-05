package com.paymend.commons.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class PaymentMetrics {

    private final MeterRegistry registry;
    private final String serviceName;

    public PaymentMetrics(MeterRegistry registry, PaymentMetricsProperties properties) {
        this.registry = registry;
        this.serviceName = properties.getServiceName();
    }

    public void incrementPaymentCounter() {
        Counter.builder("custom.stackdriver.requests.incoming")
                .description("Number of incoming payment requests")
                .tag("service_name", serviceName)
                .register(registry)
                .increment();
    }

    public void incrementTransactionCounterWithStatus(String statusCode) {
        Counter.builder("custom.stackdriver.requests.processed")
                .description("Total number of payment by status code")
                .tag("service_name", serviceName)
                .tag("statusCode", statusCode)
                .register(registry)
                .increment();
    }

    public void recordEvervaultInspectResponseTime(long duration) {
        DistributionSummary.builder("custom.stackdriver.evervault_inspect_response_time")
                .description("Response time of evervault inspect requests")
                .tag("service_name", serviceName)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(this.registry)
                .record(duration);
    }

    public void recordTotalResponseTime(long duration) {
        DistributionSummary.builder("custom.stackdriver.total_response_time")
                .description("Total response time of payment requests")
                .tag("service_name", serviceName)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(this.registry)
                .record(duration);
    }

    public void recordPaysightResponseTime(long duration) {
        DistributionSummary.builder("custom.stackdriver.paysight_response_time")
                .description("Total response time of payment requests")
                .tag("service_name", serviceName)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(this.registry)
                .record(duration);
    }

    public void recordHttpClientResponseTime(String clientName, long duration) {
        DistributionSummary.builder("custom.stackdriver.http_client_response_time")
                .description("Response time of HTTP client requests")
                .tag("service_name", serviceName)
                .tag("client", clientName)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(this.registry)
                .record(duration);
    }

    public void incrementHttpClientRequestCounter(String clientName) {
        Counter.builder("custom.stackdriver.http_client.requests")
                .description("Number of HTTP client requests")
                .tag("service_name", serviceName)
                .tag("client", clientName)
                .register(registry)
                .increment();
    }

    public void incrementHttpClientErrorCounter(String clientName, String errorType) {
        Counter.builder("custom.stackdriver.http_client.errors")
                .description("Number of HTTP client errors")
                .tag("service_name", serviceName)
                .tag("client", clientName)
                .tag("error_type", errorType)
                .register(registry)
                .increment();
    }
}