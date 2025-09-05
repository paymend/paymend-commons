package com.paymend.commons.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Utility class for common HTTP request operations.
 */
public final class RequestUtils {

    private RequestUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Converts a map of key-value pairs into a query string format.
     * Each entry is concatenated as key=value and separated by '&'.
     * <p>
     * Example: {a=1, b=2} -> "a=1&b=2"
     * </p>
     *
     * @param data the map containing key-value pairs to convert
     * @return a query string representation of the map
     */
    public static String toQueryString(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Generates a Basic Authentication header value from the provided username and password.
     * <p>
     * The returned string is in the format: {@code Basic <base64-encoded-credentials>},
     * where {@code <base64-encoded-credentials>} is the Base64 encoding of {@code username:password}.
     * </p>
     *
     * @param user     the username to use for authentication
     * @param password the password to use for authentication
     * @return a Basic Authentication header value
     */
    public static String generateBasicAuth(String user, String password) {
        if (user == null || password == null) {
            throw new IllegalArgumentException("Username and password cannot be null");
        }

        String credentials = String.format("%s:%s", user, password);
        String encodedCredentials = Base64.getEncoder().encodeToString(
            credentials.getBytes(StandardCharsets.UTF_8));
        return String.format("Basic %s", encodedCredentials);
    }

    /**
     * Masks sensitive data in a string for logging purposes.
     * Replaces characters with '*' while preserving format.
     *
     * @param data the string to mask
     * @param visibleChars number of characters to keep visible at start and end
     * @return masked string
     */
    public static String maskSensitiveData(String data, int visibleChars) {
        if (data == null || data.length() <= visibleChars * 2) {
            return "*".repeat(Math.max(0, data != null ? data.length() : 8));
        }

        String start = data.substring(0, visibleChars);
        String end = data.substring(data.length() - visibleChars);
        String middle = "*".repeat(data.length() - (visibleChars * 2));

        return start + middle + end;
    }
}