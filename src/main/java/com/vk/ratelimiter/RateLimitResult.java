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

    public boolean isAllowed() {
        return allowed;
    }

    public int getRemaining() {
        return remaining;
    }

    public long getResetAtEpochMs() {
        return resetAtEpochMs;
    }

    public int getLimit() {
        return limit;
    }
}