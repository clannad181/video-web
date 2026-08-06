package com.cjym.videoweb.utils;

/**
 * Base62 编码解码工具
 * <p>
 * 编码表：0-9 a-z A-Z，共62个字符。
 * 用于将 Snowflake 长整型ID转换为短字符串形式的 avid。
 */
public class Base62Converter {

    private static final char[] BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int BASE = 62;

    /** avid 前缀 */
    public static final String AVID_PREFIX = "av";

    /**
     * 将长整型ID编码为 avid 字符串（带 "av" 前缀）
     */
    public static String toAvid(long id) {
        return AVID_PREFIX + encode(id);
    }

    /**
     * 将 avid 字符串解码为原始长整型ID（去除 "av" 前缀后解码）
     *
     * @throws IllegalArgumentException avid 格式无效
     */
    public static long fromAvid(String avid) {
        if (avid == null || !avid.startsWith(AVID_PREFIX) || avid.length() <= AVID_PREFIX.length()) {
            throw new IllegalArgumentException("avid 格式无效，需以 'av' 开头后跟 Base62 编码字符串");
        }
        return decode(avid.substring(AVID_PREFIX.length()));
    }

    /**
     * 长整型 → Base62 编码
     */
    public static String encode(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID不能为负数");
        }
        if (id == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        long value = id;
        while (value > 0) {
            int remainder = (int) (value % BASE);
            sb.append(BASE62_CHARS[remainder]);
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * Base62 编码 → 长整型
     */
    public static long decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("编码字符串不能为空");
        }
        long result = 0;
        for (int i = 0; i < encoded.length(); i++) {
            char c = encoded.charAt(i);
            int val = indexOf(c);
            if (val < 0) {
                throw new IllegalArgumentException("无效的 Base62 字符: " + c);
            }
            result = result * BASE + val;
        }
        return result;
    }

    private static int indexOf(char c) {
        for (int i = 0; i < BASE62_CHARS.length; i++) {
            if (BASE62_CHARS[i] == c) {
                return i;
            }
        }
        return -1;
    }
}
