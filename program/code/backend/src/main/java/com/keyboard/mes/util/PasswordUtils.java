package com.keyboard.mes.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * 密码处理工具。
 *
 * <p>当前项目按示例 Shiro 配置使用 MD5 单次散列，后续生产环境建议替换为 BCrypt。</p>
 *
 * @author Keyboard MES项目组
 */
public final class PasswordUtils {

    private PasswordUtils() {
    }

    /**
     * 如果传入的是明文密码，则转换为 MD5；如果已经是 32 位 MD5，则直接返回。
     *
     * @param password 原始密码或已加密密码
     * @return 数据库存储用密码摘要
     */
    public static String encodeIfPlainText(String password) {
        if (password == null || password.isBlank()) {
            return password;
        }
        if (isMd5Hash(password)) {
            return password.toLowerCase(Locale.ROOT);
        }
        return md5(password);
    }

    private static boolean isMd5Hash(String value) {
        return value.matches("^[0-9a-fA-F]{32}$");
    }

    private static String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("MD5 algorithm is not available", exception);
        }
    }
}
