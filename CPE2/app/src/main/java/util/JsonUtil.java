package util;

import org.json.JSONObject;

import java.util.*;

/**
 * FastJson工具类
 *
 * @Author : XuJian
 * @Date : 2018年10月16日 18:17
 */
public class JsonUtil {
    /**
     * 对象转json字符串
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        return "";
    }

    /**
     * 序列化参数条件对象转json字符串
     *
     * @param object
     * @param features
     * @return
     */
//    public static String toJson(Object object, SerializerFeature... features) {
//
//    }

    /**
     * json字符串转对象
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
//    public static <T> T parse(String data, Class<T> clazz) {
//        try {
//            return JSON.parseObject(data, clazz);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * json字符串转对象集合
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
//    public static <T> List<T> parseArray(String data, Class<T> clazz) {
//        try {
//            return JSON.parseArray(data, clazz);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * json字符串转指定类型
     *
     * @param data
     * @param type
     * @param <T>
     * @return
     */
//    public static <T> T parse(String data, TypeReference<T> type) {
//        try {
//            return JSON.parseObject(data, type);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * json字符串转JSONObject
     *
     * @param data
     * @return
     */
//    public static JSONObject parse(String data) {
//        try {
//            return JSON.parseObject(data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * json字符串转JSONArray
     *
     * @param data
     * @return
     */
//    public static JSONArray parseArray(String data) {
//        try {
//            return JSON.parseArray(data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 根据属性名称获取json字符串响应值
     *
     * @param data
     * @param name
     * @return
     */
//    public static Object getPropertyFromJsonByName(String data, String name) {
//        LinkedHashMap<String, Object> jsonMap = parse(data,
//                new TypeReference<LinkedHashMap<String, Object>>() {
//                });
//        LinkedHashMap<String, Object> map;
//        Map.Entry<String, Object> entry;
//        Set<Map.Entry<String, Object>> set = jsonMap.entrySet();
//        if (null == set || set.isEmpty()) {
//            return null;
//        }
//        Iterator<Map.Entry<String, Object>> iterator = set.iterator();
//        while (iterator.hasNext()) {
//            entry = iterator.next();
//            if (name.equalsIgnoreCase(entry.getKey())) {
//                return entry.getValue();
//            } else {
//                map = parse(entry.getValue().toString(),
//                        new TypeReference<LinkedHashMap<String, Object>>() {
//                        });
//                if (!StringUtil.isNullOrBlank(map)) {
//                    jsonMap.putAll(map);
//                }
//            }
//            jsonMap.remove(entry.getKey());
//            iterator = jsonMap.entrySet().iterator();
//        }
//        return null;
//    }

    /**
     * 判断字符串是否是JSON格式
     *
     * @param string
     * @return
     */
    public static boolean isJson(String string) {
//        try {
//            JSONObject jsonStr = JSONObject.parseObject(string);
//            if (null != jsonStr) {
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
        return true;
    }
}
