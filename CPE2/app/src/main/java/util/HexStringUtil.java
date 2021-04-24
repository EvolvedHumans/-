package util;


import java.util.HashMap;
import java.util.Map;

/**
 * @Author : XuJian
 * @Date : 2020年08月06日 14:27
 */
public class HexStringUtil {

    private static final char[] HEX_CHAR_TABLE = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    private static final Map<Character, Byte> MAP = new HashMap<>();

    static {
        for (int i = 0; i < HEX_CHAR_TABLE.length; i++) {
            char c = HEX_CHAR_TABLE[i];
            MAP.put(c, (byte) i);
        }
    }

    public static String toHexString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(HEX_CHAR_TABLE[(b & 0xf0) >> 4]);
            sb.append(HEX_CHAR_TABLE[b & 0x0f]);
        }
        return sb.toString();
    }

    public static byte[] toByteArray(String hexString) {
        hexString = hexString.toUpperCase();
        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length() / 2; i++) {
            char hi = hexString.charAt(i * 2);
            char lo = hexString.charAt(i * 2 + 1);
            result[i] = (byte) ((MAP.get(hi) << 4) + MAP.get(lo));
        }
        return result;
    }

    /**
     * 16进制高低位转换字符串转int
     *
     * @param hex
     * @return
     */
//    public static Integer hexHighAndLowChangeToInt(String hex) {
//        if (StringUtil.isNullOrEmpty(hex)) {
//            return null;
//        }
//        if (hex.length() % 2 != 0) {
//            return null;
//        }
//        try {
//            if (hex.length() == 2) {
//                return Integer.parseInt(hex, 16);
//            }
//            String newHex = "";
//            for (int j = hex.length() / 2 - 1; j > -1; j--) {
//                newHex = newHex + hex.substring(j * 2, (j + 1) * 2);
//            }
//
//            return Integer.parseInt(newHex, 16);
//        } catch (Exception e) {
//            logger.error("hexHighAndLowChangeToInt", e);
//            return null;
//        }
//    }
//
//    /**
//     * 16进制高低位转换字符串转int
//     *
//     * @param hex
//     * @return
//     */
//    public static Long hexHighAndLowChangeToLong(String hex) {
//        if (StringUtil.isNullOrEmpty(hex)) {
//            return null;
//        }
//        if (hex.length() % 2 != 0) {
//            return null;
//        }
//        try {
//            if (hex.length() == 2) {
//                return Long.parseLong(hex, 16);
//            }
//            String newHex = "";
//            for (int j = hex.length() / 2 - 1; j > -1; j--) {
//                newHex = newHex + hex.substring(j * 2, (j + 1) * 2);
//            }
//
//            return Long.parseLong(newHex, 16);
//        } catch (Exception e) {
//            logger.error("hexHighAndLowChangeToLong", e);
//            return null;
//        }
//    }

    /**
     * 16进制直接转换成为字符串(无需Unicode解码)
     *
     * @param hexStr
     * @return
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * 16进制字符串转int
     *
     * @param hex
     * @return
     */
    public static Integer hexToInt(String hex) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (Exception e) {
            return null;
        }
    }
}
