package http;

import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {
    /**
     * POST
     * 包体请求
     * 仅仅支持发送json数据
     * json数据的key-value
     * 网址，key，value,密钥
     */
    public static String httpPostJSONJava(String url, RequestBody requestBody, String AccessToken) throws IOException
    {
        String str =null;
        Request request =null;

        //先创建一个实例
        OkHttpClient client = new OkHttpClient();

        //注意！！！
        //密钥的key-value，按照api文档规格来改正
        //如果无密钥
        if(AccessToken==null) {
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
            return Hp.fail;
        }
        return str;
    }

    /**
     *  GET
     * 网址,密钥
     */
    public static String httpGETJava(String url,String AccessToken) {
        String str =null;
        Request request =null;

        //先创建一个实例
        OkHttpClient client = new OkHttpClient();

        //如果无密钥
        if(AccessToken==null) {
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
            if(e!=null){
                return Hp.fail;
            }
            else {
                return Hp.fail;
            }

        }
        return str;
    }

}
