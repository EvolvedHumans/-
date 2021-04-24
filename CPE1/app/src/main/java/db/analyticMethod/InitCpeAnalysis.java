package db.analyticMethod;

import com.google.gson.Gson;

/**
 * @Author : YangFan
 * @Date : 2020年11月02日 15:48
 * @effect :
 */
public class InitCpeAnalysis {

    /*
    判断该数据是否为JSON格式数据
     */
//    public static boolean isJson(String json){
//
//    }

    /*
    将JSON字符串转化成对象
     */
    public static <T> T toJson(String json,Class<T> tClass){

        return new Gson().fromJson(json,tClass);
    }

    /*
    将对象转化为JSON字符串
     */
    public static<T> String toJsonString(T object){
        return new Gson().toJson(object);
    }
}
