package com.cjym.videoweb.encoders;

import cn.hutool.crypto.digest.DigestUtil;
import com.cjym.videoweb.constant.BaseConstant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Base64;

/**
 * 自定义 MD5 加盐密码编码器
 * <p>
 * 盐值独立存储于用户表 salt 字段，密码列仅存 MD5 哈希。
 * 加密规则：16 字节随机盐 + 原始密码 → MD5 哈希
 */
@Component
public class CustomPasswordEncoder implements PasswordEncoder {

    /**
     * 加密密码：MD5(salt + rawPassword)
     *
     * @param rawPassword 明文密码
     * @return MD5 哈希值（不含盐）
     */
    @Override
    public String encode(CharSequence rawPassword) {
        // 无盐时直接 MD5（不推荐，建议调用方使用 generateSalt + matches(明文, 密文, 盐)）
        return DigestUtil.md5Hex(rawPassword.toString());
    }

    /**
     * 验证密码
     *
     * @param rawPassword     salt$用户明文输入（调用方将 DB 盐拼在密码前）
     * @param encodedPassword 数据库中存储的加密完整串
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        String rawStr = rawPassword.toString();
        int rawSepIdx = rawStr.indexOf(BaseConstant.SALT_SPITE_IDX);
        if (rawSepIdx > 0) {
            // 从 rawPassword 中拆出：盐 + 用户输入
            String salt = rawStr.substring(0, rawSepIdx);
            String inputPassword = rawStr.substring(rawSepIdx + 1);
            return this.matches(inputPassword,encodedPassword,salt);
        }
        //直接 MD5 比对
        return encodedPassword.equals(DigestUtil.md5Hex(rawStr));
    }

    /**
     * 验证密码
     *
     * @param rawPassword 用户输入的明文
     * @param hash        数据库中存储的机密串
     * @param salt        数据库中存储的盐值
     */
    public boolean matches(CharSequence rawPassword, String hash, String salt) {
        if (rawPassword == null || hash == null || salt == null) {
            return false;
        }
        String result = md5WithSalt(rawPassword.toString(), salt);
        System.out.println(rawPassword);
        System.out.println(salt);
        System.out.println(result);
        return hash.equals(result);
    }

    /**
     * MD5(Base64盐原文 + 明文)
     */
    private String md5WithSalt(String password, String saltBase64) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        byte[] passwordBytes = password.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] combined = new byte[salt.length + passwordBytes.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(passwordBytes, 0, combined, salt.length, passwordBytes.length);
        return DigestUtil.md5Hex(combined);
    }
}
