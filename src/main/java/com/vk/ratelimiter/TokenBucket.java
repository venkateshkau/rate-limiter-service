package com.vk.ratelimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucket {
    public static Map<String, ArrayDeque<Long>> map =
            new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenBucket.class);
    private static final int limit = AppConfig.getInt("default.limit", 10);
    private static final int windowSeconds = AppConfig.getInt("default.windowseconds", 60);
    private static final Object lockObj = new Object();
    public static RateLimitResult check(String key) {
        LOGGER.info("Request for key: " + key);
        synchronized (lockObj) {
            LOGGER.info("Acquired  lock for key: " + key);
            long currentWindowSecond = Instant.now().toEpochMilli() - (windowSeconds * 1000L);
            ArrayDeque<Long> logs =
                    map.computeIfAbsent(key, k -> new ArrayDeque<Long>());
            while (logs.getFirst() < currentWindowSecond) {
                logs.removeFirst();
            }

            if (logs.size() >= limit) {
                LOGGER.info("Rate limit exceeded for key: " + key + ", Retry after : " +
                        Instant.ofEpochMilli(logs.getFirst()).atZone(ZoneId.systemDefault()));
                return new RateLimitResult(false, 0, logs.getFirst(), limit);
            }
            logs.addLast(currentWindowSecond);
            map.put(key, logs);
            LOGGER.info("Accepted request for key: " + key + " at " + currentWindowSecond);
            return new RateLimitResult(true, limit - logs.size(), logs.getFirst(), limit);
        }
    }
}
