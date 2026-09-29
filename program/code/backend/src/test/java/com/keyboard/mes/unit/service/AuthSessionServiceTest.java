package com.keyboard.mes.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyboard.mes.entity.SysUser;
import com.keyboard.mes.service.AuthSessionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AuthSessionServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuthSessionService authSessionService;

    @BeforeEach
    void setUp() {
        authSessionService = new AuthSessionService(redisTemplate, objectMapper);
        ReflectionTestUtils.setField(authSessionService, "sessionTimeoutSeconds", 60);
    }

    @Test
    void createSessionShouldWriteRedisAndCookie() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        SysUser user = new SysUser();
        user.setId(7L);
        user.setEmployeeNo("U001");
        user.setPasswordHash("secret");
        user.setName("Planner");
        user.setRoleCode("planner");
        user.setRoleName("Planner");
        user.setStatus(1);

        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, Object> data = authSessionService.createSession(user, response);

        String sessionId = (String) data.get("sessionId");
        assertThat(sessionId).isNotBlank();
        verify(valueOperations).set(eq("keyboard_mes:login:" + sessionId), org.mockito.ArgumentMatchers.anyString(),
                eq(Duration.ofSeconds(60)));

        Cookie cookie = response.getCookie(AuthSessionService.COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo(sessionId);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(60);

        @SuppressWarnings("unchecked")
        Map<String, Object> loginUser = (Map<String, Object>) data.get("user");
        assertThat(loginUser).containsEntry("employeeNo", "U001");
        assertThat(loginUser).doesNotContainKey("passwordHash");
    }

    @Test
    void getCurrentUserShouldReadAuthorizationHeaderAndRefreshTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("keyboard_mes:login:abc123"))
                .thenReturn("{\"id\":7,\"employeeNo\":\"U001\",\"name\":\"Planner\"}");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuthSessionService.HEADER_NAME, "Bearer abc123");

        Map<String, Object> user = authSessionService.getCurrentUser(request);

        assertThat(user).containsEntry("employeeNo", "U001");
        verify(redisTemplate).expire("keyboard_mes:login:abc123", Duration.ofSeconds(60));
    }

    @Test
    void destroySessionShouldDeleteRedisKeyAndClearCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthSessionService.COOKIE_NAME, "abc123"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        authSessionService.destroySession(request, response);

        verify(redisTemplate).delete("keyboard_mes:login:abc123");
        Cookie cookie = response.getCookie(AuthSessionService.COOKIE_NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isZero();
    }
}
