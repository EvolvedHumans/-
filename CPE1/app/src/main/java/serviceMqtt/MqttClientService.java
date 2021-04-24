package serviceMqtt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.communication.yang.cpe_1.R;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.PackageSerialPort.EncapsulationFormat;
import db.InitDataBase;
import db.agreement.InitCpe;
import db.agreement.InitCpeDataField;
import db.analyticMethod.InitCpeAnalysis;
import db.log.LogParameters;
import http.Hp;
import http.HttpClient;
import lombok.Data;
import lombok.SneakyThrows;
import tool.SignalIntensity;
import tool.Timestamp;

/**
 * @Author : YangFan
 * @Date : 2020年11月03日 14:30
 * @effect : MQTT通信后台服务
 */

/**
 * 线程关系:
 * 通过调用Thread类的start()方法来启动一个线程，这时此线程是处于就绪状态，并没有运行。
 * 然后通过此Thread类调用方法run()来完成其运行操作的，
 * 这里方法run()称为线程体，它包含了要执行的这个线程的内容，Run方法运行结束，此线程终止，
 */

@Data
public class MqttClientService extends Service {

    /**
     * 必需参数
     */
    private boolean serviceSecurity = false; //初始值不安全
    private Timer timer = null; //服务安全定时器
    private DtiThread dtiThread = null; //戴天请求参数

    /**
     * 基本信息获取
     */
    private String host;
    private String userName;
    private String passWord;
    private String clientId;

    /**
     * CPE唯一标识
     */
    String uid1;

    String uid2;

    /**
     * 戴天外网请求次数
     */

    Integer i = 0;

    /**
     * 订阅主题获取
     */
    private LinkedList<String> topicList = new LinkedList<String>();

    /**
     * MQTT通讯,唯一一个对象
     */
    private static MqttClient mqttClient = null;

    /**
     * 设备Uid
     */
    EquipmentIdentification equipmentIdentification = null;

    /**
     * 戴天外网请求标志位
     */
    private volatile static boolean dtiStateThread = false;

    /**
     * 传输信息接口
     */
    private IGetMqttClientMessageCallBack iGetMqttMessageCallBack;

    /**
     * 键值对获取
     */
    private SharedPreferences sharedPreferences = null;

    private SharedPreferences.Editor editor = null;

    /**
     * 发送MQTT消息
     */
    public static void send(String topic, String data){
        mqttClient.publish(topic, data);
    }

    /**
     * 初始化MQTT通信
     */
    public void init(){

        sharedPreferences = getSharedPreferences("data",MODE_PRIVATE);

        editor = sharedPreferences.edit();

        if(!sharedPreferences.getBoolean("judge",false)){

            Log.e("init","没有进行过初始化配置,就登录到了后台");

            LogParameters.Running(1,
                    "没有进行过初始化配置,就登录到了后台",
                    true
            );

            return;

        }

        uid1 = sharedPreferences.getString("cpe1uid","");

        uid2 = sharedPreferences.getString("cpe2uid","");

        Log.e("uid1",uid1);
        Log.e("uid2",uid2);

        LogParameters.Running(1,
                "uid1:"+uid1+"\n"+"uid2:"+uid2,
                true
        );


        //获取设备UID
        equipmentIdentification = new EquipmentIdentification(uid1,uid2);

        dtiThread = new DtiThread(); //初始化参数请求
        dtiThread.start(); //开启线程请求
    }

    /**
     * 当调用了startService()方法则会自动开启onStartCommand
     */
    public static void startService(Context mContext) {

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mContext.startForegroundService(new Intent(mContext, MqttClientService.class));
//        } else {
//            mContext.startService(new Intent(mContext, MqttClientService.class));
//        }
        mContext.startService(new Intent(mContext, MqttClientService.class));
    }

    /**
     * 当调用了startService()方法则会自动关闭onStartCommand
     */
    public static void stopService(Context context) throws MqttException {
        Intent iService = new Intent(context, MqttClientService.class);
        iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.stopService(iService);
        mqttClient.disConnected();
        Log.e("MQTT", "关闭服务");
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //创建一个NotificationManager的引用，通知
        NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //因为NotificationManager是Android8.0系统中新增的API，因此还需要就进行Android版本的判断
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel =
                    new NotificationChannel
                            ("TY-CPE1-V1.0","TY-CPE1-V1.0", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        //Notification 实例
        Notification notification =
                new NotificationCompat.Builder(this,"TY-CPE1-V1.0")
                .setContentTitle("TY-CPE1-V1.0")
                .setContentText("MQTT通讯模式启动")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher_background))
                .build();
        //NotificationManager中的notify()方法可以让通知显示出来
        //接收两个参数
        // 1.一个参数是id，要保证为每个通知指定一个id都是不同的；
        // 2.Notification对象，这里直接将我们刚刚创建好的Notification对象传入，创建后台通知视图
        notificationManager.notify(1,notification);
    }

    @SneakyThrows
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //串口通讯拿到信息以键值对形式存储->开启后台服务->后台获取CPE1设备标识和CPE2设备标识->调用init()动态访问

        init();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new CustomBinder();
    }

    /**
     * 让IBinder调用UdpService，从而让其他类能够获取到UdpService服务
     */
    class CustomBinder extends Binder {
        public MqttClientService getMqttClientService() {
            return MqttClientService.this;
        }
    }

    /**
     * 安全位定时器
     */
    class TimerSecurity extends TimerTask {
        @Override
        public void run() {
            switch (mqttClient.getMqtt()) {
                //传给MainActivity
                case 0:iGetMqttMessageCallBack.setAllowToSend(false);break;
                case 1:iGetMqttMessageCallBack.setAllowToSend(true); break;
                case 2:iGetMqttMessageCallBack.setAllowToSend(false);break;
            }
        }
    }


    /**
     * 访问外网，请求配置参数,并验证参数,1分30秒请求一次
     */
    class DtiThread extends Thread{
        @SneakyThrows
        @Override
        public void run() {
            Log.e("开启线程,同步请求","阻塞中");
            LogParameters.Running(1,
                    "开启线程,同步请求~阻塞中",
                    true
            );
            while (!dtiStateThread){
                Log.e("访问外网~请求网址",equipmentIdentification.InitUrl());
                LogParameters.Running(1,
                        "访问外网~请求网址"+equipmentIdentification.InitUrl(),
                        true
                );

                String s = HttpClient.httpGETJava(equipmentIdentification.InitUrl(), null);

                Log.e("戴天外网返回数据",s);
                LogParameters.Running(1,
                        "戴天外网返回数据"+s,
                        true
                );

                Thread.sleep(90000);

                if(s.equals(Hp.fail)){

                    LogParameters.Running(1,
                            "向戴天外网发送初始化请求失败:重发次数"+ ++i,
                            false
                    );

                    continue;
                }

                InitCpe initCpe = InitCpeAnalysis.toJson(s, InitCpe.class);

                initCpe.save(); //不管对错，先记录起来

                Integer rt = initCpe.getRt();
                String msg = initCpe.getMsg();
                String comments = initCpe.getComments();
                //初始化数据
                Object data = initCpe.getData();

                /**
                 * todo 添加判断条件，如果满足则存储起来,并将连接记录存到数据库中
                 * rt  1:rt 正确 ,其他:rt 错误
                 * msg 返回信息状态码
                 * comments 状态描述
                 */
                switch (rt){
                    case 1:{

                        //1.todo rt正确，返回状态描述
                        Log.e("comments返回状态描述",comments);

                        //2.todo 解析data字段，先将Object对象转化为JSON字符串，在将字符串转为对象。
                        //这一步上将data字段解析了，转化成了对象的形式。对象中的某一个字段变化，也需要进行相应的修改
                        InitCpeDataField initCpeDataField = InitCpeAnalysis.toJson
                                (InitCpeAnalysis.toJsonString(data),InitCpeDataField.class);

                        initCpeDataField.setModernClock(Timestamp.timestamp());


                        //3.todo 获取对象信息，开始校验,CPE设备UID校验
                        if(equipmentIdentification.getCpe1Uid().equals(initCpeDataField.getCpe1Uid())
                                && equipmentIdentification.getCpe2Uid().equals(initCpeDataField.getCpe2Uid())){

                            Log.e("CPE设备UID校验","通过");

                            //转成JSON字符串
                            String json = InitCpeAnalysis.toJsonString(initCpeDataField);

                            //往端口传消息
                            iGetMqttMessageCallBack.setMessage("初始化消息",
                                            EncapsulationFormat.initializationEncapsulation
                                                    (json,0));

                            dtiStateThread = true;

                        }
                        break;
                    }
                    default:{
                        LogParameters.Running(1,
                                "请求失败，设备标识异常,异常字段rt",
                                false
                        );
                        break;
                    }

                }
            }
            Log.e("同步请求","结束阻塞");
            LogParameters.Running(1,
                    "同步请求:结束阻塞",
                    true
            );
            //在子线程中阻塞，不能再主线程中阻塞
            new initThread().start();

            new Timer().schedule(new TimerSecurity(),3000,3000); //开启每3秒一次
        }
    }

    /**
     * MQTT配置线程
     */
    class initThread extends Thread{
        @SneakyThrows
        @Override
        public void run() {
            //请求完毕!
            //1.配置获取基本信息,服务器传过来的信息会存储在本地,这里暂时不解析服务器的数据
            host = MqttConfig.mMqttHost(); //ip 47.99.180.14 端口1883
            userName = "admin"; //用户名：admin
            passWord = "Dti2018"; //密码：Dti2018
            clientId = Timestamp.timestamp_string();//客户端标识
            topicList.add(SgitTopic.CPE_S2C+uid2); //加上cpe2的标识

            //2.MqttClient通讯类初始化配置
            mqttClient = new MqttClient(); //实例化通信MqttClient通讯类
            mqttClient.setContext(MqttClientService.this); //上下文
            mqttClient.setHost(host); //地址
            mqttClient.setUserName(userName); //账号
            mqttClient.setPassWord(passWord); //密码
            mqttClient.setClientId(clientId); //客户端ID
            mqttClient.setTopicList(topicList); //订阅主题集合
            mqttClient.isConnectService(); //连接服务端

            /**
             * CPE1中不需要区分主题,接口中不需要待主题
             */
            mqttClient.setOnMqttClientDataReceiveListener(new MqttClient.OnMqttClientDataReceiveListener() {
                @Override
                public void onDataReceive(String topic, String message) {
                    //TODO 在这里进行数据的封装
                    iGetMqttMessageCallBack.setMessage
                            (topic,EncapsulationFormat.initializationEncapsulation(message,1));
                }
            });
        }
    }
}
