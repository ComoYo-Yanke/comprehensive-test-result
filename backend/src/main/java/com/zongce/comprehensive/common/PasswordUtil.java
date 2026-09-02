package com.zongce.comprehensive.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具
 * <p>BCrypt 加密 + 默认密码生成规则。</p>
 */
public final class PasswordUtil {

    /** BCrypt 加密器（线程安全，可复用） */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** 明文密码加密 */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** 校验明文与密文是否匹配 */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成默认密码
     * <p>规则：身份证号后 6 位；若没有身份证信息，则取当前时间戳数字后 6 位。</p>
     *
     * @param idNumber 身份证号，可为 null 或空
     * @return 默认密码明文
     */
    public static String defaultPassword(String idNumber) {
        if (idNumber != null && idNumber.trim().length() >= 6) {
            return idNumber.trim().substring(idNumber.trim().length() - 6);
        }
        String ts = String.valueOf(System.currentTimeMillis());
        return ts.substring(ts.length() - 6);
    }
}
