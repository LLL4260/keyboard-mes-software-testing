package com.keyboard.mes.config;

import com.keyboard.mes.repository.SysUserMapper;
import com.keyboard.mes.shiro.MesShiroRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Shiro + Redis 安全配置。
 *
 * <p>Spring Boot 3 使用 Jakarta Servlet，当前配置保留 Shiro 认证和 Redis 授权缓存，
 * 具体 Web 拦截由 Spring MVC 的 AuthInterceptor 完成，避免引入 Shiro javax Filter。</p>
 *
 * @author Keyboard MES项目组
 */
@Configuration
public class ShiroConfiguration {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.timeout:10000ms}")
    private Duration timeout;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${app.shiro.session-timeout-seconds:1800}")
    private int sessionTimeoutSeconds;

    /**
     * 凭证匹配器：按示例文件采用 MD5 单次散列。
     *
     * @return 凭证匹配器
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        hashedCredentialsMatcher.setHashIterations(1);
        return hashedCredentialsMatcher;
    }

    @Bean
    public MesShiroRealm mesShiroRealm(SysUserMapper sysUserMapper, HashedCredentialsMatcher credentialsMatcher) {
        MesShiroRealm realm = new MesShiroRealm(sysUserMapper);
        realm.setCredentialsMatcher(credentialsMatcher);
        return realm;
    }

    @Bean
    public SecurityManager securityManager(MesShiroRealm mesShiroRealm, RedisCacheManager cacheManager) {
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setRealm(mesShiroRealm);
        securityManager.setCacheManager(cacheManager);
        return securityManager;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisManager redisManager) {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager);
        redisCacheManager.setExpire(sessionTimeoutSeconds);
        redisCacheManager.setPrincipalIdFieldName("id");
        return redisCacheManager;
    }

    @Bean
    public RedisManager redisManager() {
        RedisManager redisManager = new RedisManager();
        redisManager.setHost(host + ":" + port);
        redisManager.setTimeout((int) timeout.toMillis());
        if (password != null && !password.isBlank()) {
            redisManager.setPassword(password);
        }
        return redisManager;
    }
}
