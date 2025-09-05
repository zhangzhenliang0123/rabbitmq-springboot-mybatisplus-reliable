package xyz.zhenliang.rabbitmq.utils;

import java.util.UUID;

public class UUIDUtils {
    /**
     * 生成32位UUID字符串（不含连字符）,高性能实现
     *
     * @return 32位的UUID字符串
     */
    public static String generate32UUID() {
        // return UUID.randomUUID().toString().replace("-", "");
        final UUID uuid = UUID.randomUUID();
        return toHexString(uuid.getMostSignificantBits()) +
                toHexString(uuid.getLeastSignificantBits());
    }

    // 将long值转为16个字符的十六进制表示
    private static String toHexString(long value) {
        return String.format("%016x", value);
    }
}
