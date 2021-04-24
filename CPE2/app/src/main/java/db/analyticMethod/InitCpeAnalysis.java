package db.analyticMethod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.TypeVariable;
import java.util.List;

import db.agreement.CpeMqttTopicVo;

/**
 * @Author : YangFan
 * @Date : 2020年11月02日 15:48
 * @effect :
 */
public class InitCpeAnalysis {

    /*
    将JSON字符串转化成对象
     */
    public static <T> T toJson(String json,Class<T> tClass){

        return new Gson().fromJson(json,tClass);
    }

    /*
    将对象或List集合转化为JSON字符串
     */
    public static <T> String toJsonString(T object){
        return new Gson().toJson(object);
    }

    /*
    将JSON字符串转化为List对象
     */
    public static <T> T toJsonList(String json,TypeToken<T> token){
        token = new TypeToken<T>(){};
        return new Gson().fromJson(json,token.getType());
    }

    public static List<CpeMqttTopicVo> jsonToList(String jsonStr) {
        return new Gson().fromJson(jsonStr, new TypeToken<List<CpeMqttTopicVo>>(){}.getType());
    }


}
