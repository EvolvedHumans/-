package serviceHttp;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import db.log.LogParameters;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {
    /**
     * POST,以JSON格式推送
     */
    public static String httpPostJSONJava(String url, RequestBody requestBody, String AccessToken) {
        String str;
        Request request;

        //先创建一个实例
        OkHttpClient client = new OkHttpClient();

        //注意！！！
        //密钥的key-value，按照api文档规格来改正
        //如果无密钥
        if (AccessToken == null) {
            //这个创建地方发送的包体请求
            request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
        }

        //否则有密钥
        else {
            //这个创建地方发送的包体请求
            request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .header("AccessToken", AccessToken)
                    .build();
        }

        try {
            //创建Response对象
            //调用OkHttpClient的newCall()方法来创建一个Call对象，并调用它的execute()方法来发送请求并获取服务器返回的数据
            Response response = client.newCall(request).execute();

            //Response对象就是服务器返回的数据了，使用如下写法来得到返回的最终值
            str = response.body().string();

        } catch (IOException e) {
            return e.getMessage();
        }
        return str;
    }

    /**
     * POST,以表单形式推送
     */
    public static String httpFormPost(String url, HashMap<String,String> hashMap,OkHttpClient client){

        FormBody.Builder builder = new FormBody.Builder();


        for(Map.Entry<String,String> arg:hashMap.entrySet()){
            builder.add(arg.getKey(),arg.getValue());
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            //添加请求成功问题
            if(!response.isSuccessful()){
                Log.e("返回不成功","原因"+response);
                return "";
            }
            else {
                String str = response.body().string();
                return str;
            }

        }
        catch (IOException e) {
//            LogParameters.Running(4,
//                    "Http请求失败,错误原因:"+e.getMessage(),
//                    false
//            );
            return "";
        }
    }

    /**
     * GET
     * 网址,密钥
     */
    public static String httpGETJava(String url, String accessToken) {
        String TAG = "MyOKHttp";
        String str = null;
        Request request = null;

        //先创建一个实例
        OkHttpClient client = new OkHttpClient();

        Log.i(TAG, "RequestBody对象构建成功");

        //如果无密钥
        if (accessToken == null) {
            //这个创建地方发送的包体请求
            request = new Request.Builder()
                    .url(url)
                    .build();
        }

        //否则有密钥
        else {
            //这个创建地方发送的包体请求
            request = new Request.Builder()
                    .url(url)
                    .header("AccessToken", accessToken)
                    .build();
        }

        Log.i(TAG, "get请求构建成功");

        try {
            //创建Response对象
            //调用OkHttpClient的newCall()方法来创建一个Call对象，并调用它的execute()方法来发送请求并获取服务器返回的数据
            Response response = client.newCall(request).execute();

            Log.i(TAG, "get请求发送成功");

            //Response对象就是服务器返回的数据了，使用如下写法来得到返回的最终值
            str = response.body().string();

            Log.i(TAG, "成功获得返回参数");

        } catch (IOException e) {
            return "请求失败";
        }
        return str;
    }

}
