package serviceHttp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author：yangfan
 * @data：2020.10.26
 * @effect :HTTP通信后台服务
 * 上海内网：
 */

public class HttpClientService extends Service {
    public HttpClientService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
