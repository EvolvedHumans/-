package com.communication.yang.cpe_2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttException;

import lombok.AllArgsConstructor;
import lombok.Data;
import serviceMqtt.IGetMqttClientMessageCallBack;
import serviceMqtt.MqttClientService;
import serviceMqtt.MqttClientServiceConnection;
import serviceTcp.IGetTcpClientMessageCallBack;
import serviceTcp.TcpClientService;
import serviceTcp.TcpClientServiceConnection;
import serviceUdp.IGetUdpMessageCallBack;
import serviceUdp.UdpService;
import serviceUdp.UdpServiceConnection;

/**
 * @Author : YangFan
 * @Date : 2020年10月27日 18:27
 * @effect : 用来切换不同的后台通信
 * service生命周期
 * onStartCommand() 在调用startService()和stopService()时会启动和销毁
 * onBind()方法 在调用bindService()来获取一个Service的持久连接
 * 如何销毁整个Service进程
 * 先调用startService和s再去调用topService，这时Service中的onDestory会自动去执行
 * 当如何同时也调用了bindService方法那么必须调用unbindService自动执行Service的销毁
 * 异常:java.lang.IllegalArgumentException: Service not registered: serviceMqtt.MqttServiceConnection@8125468
 * 在做Service的绑定和解绑小项目测试的时候，绑定成功，解绑也成功，但是如果解除绑定后再点击给解除绑定的指令
 * （项目中就是点击按钮解除绑定），会报这个错误：
 */
@Data
@AllArgsConstructor
public class ServiceOperation {

    /**
     * service  1  开启MQTT
     * service  2  开启UDP
     * service  3  开启TCP
     */
    private int service;

    private Context context;

    private MqttClientServiceConnection mqttClientServiceConnection;
    private UdpServiceConnection udpServiceConnection;
    private TcpClientServiceConnection tcpClientServiceConnection ;

    private boolean serviceStatus = false;

    public ServiceOperation(Context context,int service){
        this.context = context;
        this.service = service;
    }

    /**
     * 开启Service后台通信业务并与Context绑定
     * Context  开启的活动界面
     * Intent Context活动界面跳转到的Service服务
     */
    public void serviceOnCreate() {
        switch (service){
            case 1:{
                mqttClientServiceConnection = new MqttClientServiceConnection();
                //在绑定后开启onServiceConnected
                mqttClientServiceConnection.setContext(context);

                mqttClientServiceConnection.setiGetMqttMessageCallBack
                        ((IGetMqttClientMessageCallBack) context);

                Intent intent = new Intent(context,MqttClientService.class);

                context.bindService
                        (intent,mqttClientServiceConnection,Context.BIND_AUTO_CREATE);

                serviceStatus = true;
                break;
            }
            case 2:{
                udpServiceConnection = new UdpServiceConnection();
                //在绑定后开启onServiceConnected
                udpServiceConnection.setContext(context);

                udpServiceConnection.setiGetUdpMessageCallBack
                        ((IGetUdpMessageCallBack) context);

                Intent intent = new Intent(context, UdpService.class);

                context.bindService
                        (intent, udpServiceConnection, Context.BIND_AUTO_CREATE);

                serviceStatus = true;
                break;
            }
            case 3:{
                tcpClientServiceConnection = new TcpClientServiceConnection();
                //在绑定后开启onServiceConnected
                tcpClientServiceConnection.setContext(context);

                tcpClientServiceConnection.setiGetTcpClientMessageCallBack((IGetTcpClientMessageCallBack) context);

                Intent intent = new Intent(context, TcpClientService.class);

                context.bindService
                        (intent, tcpClientServiceConnection, Context.BIND_AUTO_CREATE);

                serviceStatus = true;
                break;
            }
            default:{
                Log.e("ServiceOperation","1.开启MQTT,2.开启UDP,3.开启TCP");
                break;
            }
        }

    }

    public void unBind() throws MqttException {
        switch (service){
            case 1:{
                MqttClientService.stopService(context);
                context.unbindService(mqttClientServiceConnection);
                break;
            }

            case 2:{
                UdpService.stopService(context);
                context.unbindService(udpServiceConnection);
                break;
            }

            case 3:{
                TcpClientService.stopService(context);
                context.unbindService(tcpClientServiceConnection);
                break;
            }
        }
    }



}
