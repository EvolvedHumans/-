package serviceHttp;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @Author : YangFan
 * @Date : 2020年12月09日 14:37
 * @effect : OKHTTP 通过form表单形式传递数据
 */
@Data
public class FormPort {


    public static Request request(String url,HashMap<String,String> hashMap){

        FormBody.Builder builder = new FormBody.Builder();

        Log.e("HashMap",String.valueOf(hashMap));
        Log.e("builder",String.valueOf(builder));

        if(hashMap!=null && builder!=null){

            for(Map.Entry<String,String> arg:hashMap.entrySet()){
                builder.add(arg.getKey(),arg.getValue());
            }

            RequestBody requestBody = builder.build();

            Request request = new Request
                    .Builder()

                    .url(url)
                    .post(requestBody)
                    .build();

            return request;
        }

        return null;

    }

}
