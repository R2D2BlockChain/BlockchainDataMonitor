package org.example.http;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Sliding-window limiter: at most {@code maxRequests} per {@code windowSeconds} per client key.
 * When {@code maxRequests} is 0, {@link #checkAndRecord(String)} always allows traffic.
 */
public final class IpRateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, ArrayDeque<Long>> buckets = new HashMap<String, ArrayDeque<Long>>();

    public IpRateLimiter(int maxRequests, long windowSeconds) {
        if (maxRequests < 0) {
            throw new IllegalArgumentException("maxRequests must be >= 0");
        }
        if (windowSeconds < 1) {
            throw new IllegalArgumentException("windowSeconds must be >= 1");
        }
        this.maxRequests = maxRequests;
        this.windowMillis = windowSeconds * 1000L;
    }

    public static IpRateLimiter fromEnvironment() {
        int max = parseNonNegativeInt(System.getenv("API_RATE_LIMIT_MAX"), 120);
        int windowSec = parsePositiveInt(System.getenv("API_RATE_LIMIT_WINDOW_SEC"), 60);
        return new IpRateLimiter(max, windowSec);
    }

    /**
     * @return empty if the request is allowed; otherwise seconds until the client should retry
     */
    public OptionalInt checkAndRecord(String clientKey) {
        if (maxRequests == 0) {
            return OptionalInt.empty();
        }
        if (clientKey == null || clientKey.isEmpty()) {
            clientKey = "unknown";
        }
        long now = System.currentTimeMillis();
        synchronized (buckets) {
            ArrayDeque<Long> window = buckets.computeIfAbsent(clientKey, k -> new ArrayDeque<Long>());
            long windowStart = now - windowMillis;
            while (!window.isEmpty() && window.peekFirst().longValue() < windowStart) {
                window.pollFirst();
            }
            if (window.size() >= maxRequests) {
                long oldest = window.peekFirst().longValue();
                long retryMillis = oldest + windowMillis - now;
                int retrySec = (int) Math.max(1L, (retryMillis + 999L) / 1000L);
                return OptionalInt.of(retrySec);
            }
            window.addLast(Long.valueOf(now));
            return OptionalInt.empty();
        }
    }

    private static int parseNonNegativeInt(String raw, int defaultValue) {
        if (raw == null || raw.isEmpty()) {
            return defaultValue;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            return Math.max(0, v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int parsePositiveInt(String raw, int defaultValue) {
        if (raw == null || raw.isEmpty()) {
            return defaultValue;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            return v < 1 ? defaultValue : v;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
