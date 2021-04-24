package com.communication.yang.cpe_1;

import android.content.Context;
import android.content.Intent;

import org.eclipse.paho.android.service.MqttService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import serviceMqtt.IGetMqttClientMessageCallBack;
import serviceMqtt.MqttClientService;
import serviceMqtt.MqttClientServiceConnection;

/**
 * @Author : YangFan
 * @Date : 2020年11月02日 10:53
 * @effect :
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class BindService {

    private Context context;

    private MqttClientServiceConnection mqttClientServiceConnection = null;

    public void serviceOnBind() {

        mqttClientServiceConnection = new MqttClientServiceConnection();

        //在绑定后开启onServiceConnected
        mqttClientServiceConnection.setContext(context);

        mqttClientServiceConnection.setiGetMqttMessageCallBack
                ((IGetMqttClientMessageCallBack)context);

        Intent intent = new Intent(context,MqttClientService.class);

        context.bindService
                (intent,mqttClientServiceConnection,Context.BIND_AUTO_CREATE);
    }

    public void serviceCommand(){
        MqttClientService.startService(context);
    }

}
