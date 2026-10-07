package com.keyboard.mes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Waits for Redis before the application is marked as ready.
 *
 * @author Keyboard MES项目组
 */
@Component
public class RedisStartupWaiter implements ApplicationRunner {

    private static final String REDIS_READY_RESPONSE = "PONG";


    private static final Logger log = LoggerFactory.getLogger(RedisStartupWaiter.class);

    private final RedisConnectionFactory connectionFactory;
    private final boolean enabled;
    private final Duration maxWait;
    private final Duration retryInterval;

    public RedisStartupWaiter(RedisConnectionFactory connectionFactory,
                              @Value("${app.redis.startup-wait.enabled:true}") boolean enabled,
                              @Value("${app.redis.startup-wait.max-wait:120s}") Duration maxWait,
                              @Value("${app.redis.startup-wait.retry-interval:2s}") Duration retryInterval) {
        this.connectionFactory = connectionFactory;
        this.enabled = enabled;
        this.maxWait = maxWait;
        this.retryInterval = retryInterval;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!enabled) {
            log.info("Redis startup wait is disabled");
            return;
        }

        long deadline = System.nanoTime() + Math.max(0L, maxWait.toNanos());
        int attempt = 0;
        RuntimeException lastError = null;
        do {
            attempt++;
            try (RedisConnection connection = connectionFactory.getConnection()) {
                String response = connection.ping();
                if (REDIS_READY_RESPONSE.equalsIgnoreCase(response)) {
                    log.info("Redis is ready after {} attempt(s)", attempt);
                    return;
                }
                lastError = new IllegalStateException("Unexpected Redis PING response: " + response);
            } catch (RuntimeException error) {
                lastError = error;
            }

            if (System.nanoTime() >= deadline) {
                break;
            }
            log.warn("Redis is not ready; retrying in {} ms (attempt {})", retryInterval.toMillis(), attempt);
            Thread.sleep(Math.max(1L, retryInterval.toMillis()));
        } while (System.nanoTime() < deadline);

        throw new IllegalStateException("Redis was not ready within " + maxWait.toSeconds() + " seconds", lastError);
    }
}
