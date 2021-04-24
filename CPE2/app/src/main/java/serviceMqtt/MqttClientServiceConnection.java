package serviceMqtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import lombok.Data;
import lombok.SneakyThrows;

/**
 * @Author : YangFan
 * @Date : 2020年11月03日 14:55
 * @effect :MqttService 与 MainActivity通信通道
 */
@Data
public class MqttClientServiceConnection implements ServiceConnection {

    /*
    MQTT后台服务
     */
    private Context context;
    private MqttClientService mqttClientService;
    private IGetMqttClientMessageCallBack iGetMqttClientMessageCallBack;

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        mqttClientService = ((MqttClientService.CustomBinder)iBinder).getMqttClientService();
        mqttClientService.setIGetMqttMessageCallBack(iGetMqttClientMessageCallBack);

        MqttClientService.startService(context);
    }


    @SneakyThrows
    @Override
    public void onServiceDisconnected(ComponentName name) {
        MqttClientService.stopService(context);
    }


    //放接口
    public void setiGetMqttMessageCallBack(IGetMqttClientMessageCallBack iGetMqttClientMessageCallBack) {
        this.iGetMqttClientMessageCallBack = iGetMqttClientMessageCallBack;
    }
}
