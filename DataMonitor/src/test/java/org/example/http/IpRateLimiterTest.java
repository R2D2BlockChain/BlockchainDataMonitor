package org.example.http;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpRateLimiterTest {

    @Test
    void allowsAllWhenMaxIsZero() {
        IpRateLimiter limiter = new IpRateLimiter(0, 60);
        for (int i = 0; i < 5; i++) {
            assertFalse(limiter.checkAndRecord("a").isPresent());
        }
    }

    @Test
    void blocksAfterMaxInWindow() {
        IpRateLimiter limiter = new IpRateLimiter(2, 60);
        assertFalse(limiter.checkAndRecord("ip1").isPresent());
        assertFalse(limiter.checkAndRecord("ip1").isPresent());
        OptionalInt retry = limiter.checkAndRecord("ip1");
        assertTrue(retry.isPresent());
        assertTrue(retry.getAsInt() >= 1 && retry.getAsInt() <= 60);
    }

    @Test
    void keysAreIndependent() {
        IpRateLimiter limiter = new IpRateLimiter(1, 60);
        assertFalse(limiter.checkAndRecord("a").isPresent());
        assertTrue(limiter.checkAndRecord("a").isPresent());
        assertFalse(limiter.checkAndRecord("b").isPresent());
    }
}
