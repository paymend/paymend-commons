# Paymend Commons Library

[![Maven Central](https://img.shields.io/badge/Maven%20Central-1.0.0--SNAPSHOT-blue.svg)](https://central.sonatype.com/)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)

A shared library for Paymend services providing common utilities, metrics integration, and HTTP client functionality.

## Features

- **Correlation Context**: Request tracking across microservices
- **Payment Metrics**: Micrometer integration with Stackdriver and Prometheus
- **REST Template Configuration**: Pre-configured HTTP client with retry logic
- **Request Utilities**: Common request processing utilities
- **Data Masking**: Privacy-focused data masking configuration

## Installation

### Maven

Add the Paymend Maven repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>artifact-registry</id>
        <url>artifactregistry://us-central1-maven.pkg.dev/ujy-nmi-compatible-api/paymend-maven-repo</url>
    </repository>
</repositories>
```

Add the Maven wagon extension for Artifact Registry:

```xml
<build>
    <extensions>
        <extension>
            <groupId>com.google.cloud.artifactregistry</groupId>
            <artifactId>artifactregistry-maven-wagon</artifactId>
            <version>2.2.1</version>
        </extension>
    </extensions>
</build>
```

Include the dependency:

```xml
<dependency>
    <groupId>com.paymend</groupId>
    <artifactId>paymend-commons</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Authentication

Configure your Maven settings (`~/.m2/settings.xml`) and authenticate with Google Cloud:

```bash
gcloud auth application-default login
```

## Usage

### Correlation Context

Track requests across your microservices:

```java
@Autowired
private CorrelationContext correlationContext;

// Get current correlation ID
String correlationId = correlationContext.getCorrelationId();

// Generate a new correlation ID
String newId = correlationContext.generateCorrelationId();
```

### Payment Metrics

Record custom metrics for payment processing:

```java
@Autowired
private PaymentMetrics paymentMetrics;

// Record a payment attempt
paymentMetrics.recordPaymentAttempt("credit_card");

// Record payment success/failure
paymentMetrics.recordPaymentSuccess("credit_card", amount);
paymentMetrics.recordPaymentFailure("credit_card", "insufficient_funds");

// Record API call duration
Timer.Sample sample = paymentMetrics.startTimer("external_api_call");
// ... make API call ...
sample.stop(paymentMetrics.getTimer("external_api_call"));
```

### REST Template Service

Use the pre-configured HTTP client with retry logic:

```java
@Autowired
private RestTemplateService restTemplateService;

// GET request
ResponseEntity<String> response = restTemplateService.get("/api/endpoint", String.class);

// POST request
MyRequest request = new MyRequest();
ResponseEntity<MyResponse> response = restTemplateService.post("/api/endpoint", request, MyResponse.class);

// PUT request
ResponseEntity<MyResponse> response = restTemplateService.put("/api/endpoint", request, MyResponse.class);

// DELETE request
restTemplateService.delete("/api/endpoint");
```

### Request Utilities

Extract common request information:

```java
// Extract IP address from request
String clientIp = RequestUtils.getClientIpAddress(request);

// Extract correlation ID from headers
String correlationId = RequestUtils.getCorrelationId(request);
```

## Configuration

### Application Properties

Configure the commons library in your `application.yml`:

```yaml
paymend:
  commons:
    metrics:
      enabled: true
      prefix: "paymend"
      step-duration: 60s
    rest-template:
      connection-timeout: 5000
      read-timeout: 10000
      max-retry-attempts: 3
      retry-delay: 1000
    masking:
      enabled: true
      fields:
        - "password"
        - "ssn"
        - "credit_card_number"
```

### Metrics Configuration

The library automatically configures Micrometer with:
- **Stackdriver**: For Google Cloud monitoring
- **Prometheus**: For Prometheus scraping
- **Custom metrics**: Payment-specific metrics

### Retry Configuration

HTTP calls automatically retry on:
- Connection timeouts
- 5xx server errors
- Specific retryable exceptions

## Auto-Configuration

The library provides Spring Boot auto-configuration for:

- `RestTemplateConfig` - Pre-configured RestTemplate beans
- `PaymentMetrics` - Metrics collection beans
- `CorrelationFilter` - Automatic correlation ID handling
- `RetryLoggingConfig` - Retry mechanism with logging

## Contributing

### Development

1. Clone the repository:
```bash
git clone https://github.com/paymend/paymend-commons.git
cd paymend-commons
```

2. Build the project:
```bash
mvn clean compile
```

3. Run tests:
```bash
mvn test
```

4. Deploy to Artifact Registry:
```bash
mvn deploy
```

### Release Process

1. Update version in `pom.xml`
2. Create a git tag:
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

3. GitHub Actions will automatically build and deploy

## Requirements

- **Java**: 21+
- **Spring Boot**: 3.5.4+
- **Maven**: 3.8+
- **Google Cloud**: Authenticated access for Artifact Registry

## License

Copyright © 2025 Paymend. All rights reserved.

## Support

For questions or issues:
- Create an issue on [GitHub](https://github.com/paymend/paymend-commons/issues)
- Contact the platform team at Paymend

---

**Latest Version**: 1.0.0  
**Repository**: https://github.com/paymend/paymend-commons  
**Artifact Registry**: `us-central1-maven.pkg.dev/ujy-nmi-compatible-api/paymend-maven-repo`