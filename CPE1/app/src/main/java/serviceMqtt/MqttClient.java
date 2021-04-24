package serviceMqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import db.log.LogParameters;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import okio.Utf8;

/**
 * @Author : YangFan
 * @Date : 2020年11月02日 16:57
 * @effect :MqttClient通信
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class MqttClient {

    private final static String TAG = "serviceMqtt.MqttClient";

    /*
    调用时，必需赋值参数
     */
    String host = null; //地址

    String clientId = null; //客户端标识

    Context context = null; //上下文

    String userName = null; //账号

    String passWord = null; //密码

    LinkedList<String> topicList = null; //待订阅主题集合

    /******************************************************************/

    /*
    必需，MQTT安全位 0。无配置，1.安全 2.不安全 ->只有初始化完毕才能开始收发消息
     */
    private int mqtt = 0;

    MqttAndroidClient mqttAndroidClient = null; //MQTT客户端连接

    MqttConnectOptions mqttConnectOptions = null;

    //断线重连的一个错误
    private boolean mqttCatch = false;

    /*
    MQTT连接的监听
     */
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @SneakyThrows
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {

            Log.d("IMqttActionListener","MQTT连接成功");
            Log.d("IMqttActionListener","配置如下~");
            Log.d("host",getHost());
            Log.d("userName",getUserName());
            Log.d("passWord",getPassWord());
            Log.d("clientId",getClientId());

            LogParameters.Running(1,
                    "MQTT连接成功"+"\n"+
                            "配置如下~"+"\n"+
                            "host:"+getHost()+"\n"+
                            "userName:"+getUserName()+"\n"+
                            "passWord:"+getPassWord()+"\n"+
                            "clientId"+getClientId(),
                    true
            );

            //连接成功 开始订阅主题
            for(int i=0;i<topicList.size();i++){
                subscribeTotopics(topicList.get(i));
            }

            //全部配置完毕，可以确认MQTT通信能安全进行安全
            mqtt = 1; //安全

            //todo 向戴天网络发初始化完毕消息
            LogParameters.Running(1,
                    "TY-CPE1-V1.0配置完毕",
                    true
            );

        }
       @SneakyThrows
       @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            //连接失败
            LogParameters.Running(1,
                    "错误原因:" + exception,
                    false
            );
            Thread.sleep(10000);
            //如果mqttcatch为false
            if (!mqttCatch) {
                isConnectService();
            }
        }
    };

    /*
    MQTT监听订阅主题消息
     */
    private MqttCallback mqttCallback = new MqttCallbackExtended() {
        //失去连接
        @SneakyThrows
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            //连接成功 开始订阅主题
            if (reconnect) {
                LogParameters.Running(1, "重连" + serverURI, true);
                for (int i = 0; i < topicList.size(); i++) {
                    subscribeTotopics(topicList.get(i));
                }
                mqtt = 1; //安全
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            LogParameters.Running(1,
                    "MQTT失去连接",
                    false
            );
            mqtt = 0; //不安全
        }

        //接受信息
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            switch (mqtt){
                case 0:break;
                case 1:{
                    //通过接口导出信息
                    onMqttClientDataReceiveListener.onDataReceive
                            (topic,new String(message.getPayload(), "Utf8"));
                    break;
                }
                case 2:{
                    LogParameters.Running(1,
                            "TY-CPE1-V1.0监听到了信息，但是不安全，不接受。可能是网络原因。",
                            false
                    );
                    break;
                }
            }

        }

        //无用
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG, String.valueOf(token.isComplete()));
        }
    };

    /**
     * MqttConnectOptions配置
     */
    public MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);//设置断开后自动连接
        mqttConnectOptions.setCleanSession(false);//设置是否清除缓存 ，离线接收
        mqttConnectOptions.setUserName(userName); //设置用户名
        mqttConnectOptions.setPassword(passWord.toCharArray()); //设置密码
        mqttConnectOptions.setConnectionTimeout(10);//设置超时时间,单位:秒
        mqttConnectOptions.setKeepAliveInterval(100);//设置心跳包发送间隔,单位：秒
        return mqttConnectOptions;
    }

    /**
     * 向对应主题推送信息
     */
    public void publish(String topic,String message){
        try {
            if(mqttAndroidClient!=null){
                switch (mqtt){
                    case 0:{
                        LogParameters.Running(1,
                                "TY-CPE1-V1.0监听到了内网信息，但是未初始化MQTT配置，不推送。",
                                false
                        );
                        break;
                    }
                    case 1:{
                        //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
                        mqttAndroidClient.publish(topic,message.getBytes("Utf8"),2,false);
                        break;
                    }

                    case 2:{
                        LogParameters.Running(1,
                                "TY-CPE1-V1.0监听到了内网信息，但是不安全，不推送。",
                                false
                        );
                        break;
                    }
                }
            }
            else {
                LogParameters.Running(1,
                        "MQTT客户端:没有实例化",
                        false
                );
            }
        }catch (Exception e){
            LogParameters.Running(1,
                    "错误信息:"+e,
                    false
            );
        }

    }

    /**
     * 获取网络状态
     */
    public boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        //连接成功，这里可以拿网路名 true
        if (info != null && info.isAvailable()) {

            String name = info.getTypeName(); //拿到网络名

            return true;
        }

        //未连接成功 false
        else {
            return false;
        }
    }

    /**
     * 订阅主题的封装
     */
    public void subscribeTotopics(final String topic) {
        try {
            if(mqttAndroidClient!=null){
                mqttAndroidClient.subscribe(topic, 2, null,
                        new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                //订阅成功
                                LogParameters.Running(1,
                                        topic+"订阅成功",
                                        true
                                );
                            }
                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                //订阅失败
                                LogParameters.Running(1,
                                        topic+"订阅失败",
                                        false
                                );
                            }
                        });
            }
        }catch (Exception e){
            LogParameters.Running(1,
                    "错误信息:"+e,
                    false
            );
        }
    }

    /**
     * 连接,并添加订阅主题的集合，只有在有网的情况下才能连接
     */
    public void isConnectService() {
        Log.d("isConnectService","等待网络索引");
        LogParameters.Running(1,
                "等待网络索引",
                false
        );
        while (!isConnectIsNomarl());
        Log.d("isConnectService","索引完成，网络状态良好");
        LogParameters.Running(1,
                "索引完成，网络状态良好",
                true
        );


        //防止重复创建MQTTClient实例
        if (mqttAndroidClient == null) {
            mqttAndroidClient = new MqttAndroidClient(context, host, clientId);
            mqttAndroidClient.setCallback(mqttCallback);
        }

        //防止重复创建mqttConectOptions实例
        if(mqttConnectOptions == null){
            mqttConnectOptions = getMqttConnectOptions();
        }


        try{
            //判断拦截状态，这里注意一下，如果没有这个判断，很难受
            if (!isAlreadyConnected()) { //未连接
                Log.e("未连接","则连接");
                mqttAndroidClient.connect
                        (mqttConnectOptions, null, iMqttActionListener);
            }

            else { //已连接，则重连
                Log.e("已经连接","则重连");
                mqttAndroidClient.disconnect();
                mqttAndroidClient.connect
                        (mqttConnectOptions, null, iMqttActionListener);
            }
            mqttCatch = true;
        }

        catch (Exception e){
            mqttCatch = false;
        }
    }

    /**
     * mqttAndroidClient.isConnected()报异常，传入的非布尔值，未解决这个问题而引入
     */
    public boolean isAlreadyConnected() {
        if(mqttAndroidClient != null){
            try{
                boolean result = mqttAndroidClient.isConnected();
                if(result){
                    return true;
                }
                else {
                    return false;
                }
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        else {
            return false;
        }
    }

    public void disConnected() {
        try {
            if(isAlreadyConnected()){
                mqttAndroidClient.disconnect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * 接口，将数据传到MqttService
     */
    public OnMqttClientDataReceiveListener onMqttClientDataReceiveListener = null;

    public static interface OnMqttClientDataReceiveListener {
        public void onDataReceive(String topic,String message);
    }

    public void setOnTcpClientDataReceiveListener(OnMqttClientDataReceiveListener onMqttClientDataReceiveListener) {
        this.onMqttClientDataReceiveListener = onMqttClientDataReceiveListener;
    }

}