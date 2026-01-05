package com.vk.ratelimiter;

public class RateLimitResult {
    boolean allowed;
    int remaining;
    long resetAtEpochMs;
    int limit;

    public RateLimitResult(boolean allowed, int remaining,
                           long resetAtEpochMs,int limit) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.resetAtEpochMs = resetAtEpochMs;
        this.limit = limit;
    }
}