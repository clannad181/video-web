package com.cjym.videoweb.utils;

import cn.hutool.crypto.digest.DigestUtil;

import java.security.SecureRandom;
import java.util.Base64;

public class MD5Util {

    /**
     * MD5(Base64盐原文 + 明文)
     */
    public static String md5WithSalt(String password, String saltBase64) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        byte[] passwordBytes = password.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] combined = new byte[salt.length + passwordBytes.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(passwordBytes, 0, combined, salt.length, passwordBytes.length);
        return DigestUtil.md5Hex(combined);
    }

    /**
     * 生成随机盐
     * */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
