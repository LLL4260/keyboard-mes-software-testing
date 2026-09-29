package com.keyboard.mes.unit.config;

import com.keyboard.mes.config.RedisStartupWaiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
class RedisStartupWaiterTest {

    @Test
    void shouldRetryUntilRedisRespondsToPing() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(factory.getConnection())
                .thenThrow(new RedisConnectionFailureException("not ready"))
                .thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        RedisStartupWaiter waiter = new RedisStartupWaiter(
                factory, true, Duration.ofSeconds(1), Duration.ofMillis(1));

        assertThatCode(() -> waiter.run(new DefaultApplicationArguments(new String[0])))
                .doesNotThrowAnyException();
        verify(connection).ping();
        verify(connection).close();
    }

    @Test
    void shouldSkipRedisCheckWhenDisabled() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisStartupWaiter waiter = new RedisStartupWaiter(
                factory, false, Duration.ofSeconds(1), Duration.ofMillis(1));

        assertThatCode(() -> waiter.run(new DefaultApplicationArguments(new String[0])))
                .doesNotThrowAnyException();
    }
}
