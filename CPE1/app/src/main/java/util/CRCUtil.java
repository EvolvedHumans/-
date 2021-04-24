package util;

import java.util.zip.CRC32;

/**
 * @Author : XuJian
 * @Date : 2020年04月10日 11:12
 */
public class CRCUtil {
    /**
     * 一个字节包含位的数量 8
     */
    private static final int BITS_OF_BYTE = 8;
    /**
     * 多项式
     */
    private static final int POLYNOMIAL = 0xA001;
    /**
     * 初始值
     */
    private static final int INITIAL_VALUE = 0xFFFF;

    /**
     * 获取字符串CRC32检验码
     *
     * @param str 字符串
     * @return
     */
    public static String getCRC32(String str) {
        CRC32 crc32 = new CRC32();
        crc32.update(str.getBytes());
        String crc = Long.toHexString(crc32.getValue()).toUpperCase();
        if (crc.length() < 8) {
            int len = 8 - crc.length();
            for (int i = 0; i < len; i++) {
                crc = "0" + crc;
            }
        }
        return crc;
    }

    public static String CRC16_MODBUS(byte[] buffer) {
        int wCRCin = 0xffff;
        int POLYNOMIAL = 0xa001;
        for (byte b : buffer) {
            wCRCin ^= ((int) b & 0x00ff);
            for (int j = 0; j < 8; j++) {
                if ((wCRCin & 0x0001) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= POLYNOMIAL;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        wCRCin ^= 0x0000;
        return Integer.toHexString(wCRCin);
    }

    /**
     * 获取字符串CRC16检验码
     *
     * @param str 内容
     * @return 编码结果
     */
    public static String getCRC16(String str) {
        int res = INITIAL_VALUE;
        for (int data : str.getBytes()) {
            res = res ^ data;
            for (int i = 0; i < BITS_OF_BYTE; i++) {
                res = (res & 0x0001) == 1 ? (res >> 1) ^ POLYNOMIAL : res >> 1;
            }
        }
        return Integer.toHexString(res);
    }

    /**
     * 翻转16位的高八位和低八位字节
     *
     * @param src 翻转数字
     * @return 翻转结果
     */
    private static int revert(int src) {
        int lowByte = (src & 0xFF00) >> 8;
        int highByte = (src & 0x00FF) << 8;
        return lowByte | highByte;
    }

    /**
     * 从一个byte[]数组中截取一部分
     * @param src
     * @param begin
     * @param count
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count-begin];
        for (int i=begin; i<count; i++){
            bs[i-begin] = src[i];
        }
        return bs;
    }
}
