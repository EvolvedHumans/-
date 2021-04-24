package com.communication.yang.cpe_2;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.communication.yang.cpe_2.adapter.HeartLogAdapter;
import com.communication.yang.cpe_2.adapter.RunningLogAdapter;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.litepal.LitePal;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android_serialport_api.PackageSerialPort.EncapsulationFormat;
import db.InitDataBase;
import db.addressAndPort.PostalAddress;
import db.agreement.DataTransmissionProtocol;
import db.agreement.InitCpeDataField;
import db.agreement.InitializationConfiguration;
import db.analyticMethod.InitCpeAnalysis;
import db.log.HeartbeatLog;
import db.log.LogParameters;
import db.log.RunningLog;
import android_serialport_api.SerialPort;
import db.serialPort.ConnectionVerificationReceiveOne;
import db.serialPort.ConnectionVerificationReceiveTwo;
import db.serialPort.ConnectionVerificationResponse;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import queue.HttpQueue;
import queue.IssueQueue;
import queue.QueueSize;
import queue.SerialPortQueue;
import queue.UploadQueue;
import serviceHttp.FormPort;
import serviceHttp.HttpClient;
import serviceMqtt.IGetMqttClientMessageCallBack;
import serviceMqtt.MqttClientService;
import serviceTcp.IGetTcpClientMessageCallBack;
import serviceTcp.TcpClientService;
import serviceUdp.UdpService;
import tool.IntenetUtil;
import tool.SignalIntensity;
import serviceUdp.IGetUdpMessageCallBack;
import android_serialport_api.SerialPortUtils;
import tool.Timestamp;

public class MainActivity extends AppCompatActivity implements IGetMqttClientMessageCallBack, IGetUdpMessageCallBack, IGetTcpClientMessageCallBack {

    /**
     * 1.队列实例，全部串口数据下发队列
     */
    IssueQueue issueQueue = IssueQueue.getInstance();

    /**
     * 2.队列实例，全部接收串口服务数据队列
     */
    SerialPortQueue serialPortQueue = SerialPortQueue.getInstance();

    /**
     * 3.队列实例，外网数据推送队列
     */
    UploadQueue uploadQueue = UploadQueue.getInstance();

    /**
     * 4.HTTP队列实例，向内网请求队列
     */
    HttpQueue httpQueue = HttpQueue.getInstance();

    /**
     * TCP、UDP地址端口号
     */
    PostalAddress postalAddress = new PostalAddress();

    /**
     * HTTP 上海
     */
    private String url;

//    /**
//     * HTTP 天津
//     */
//    private String url1 = "http://192.168.1.152:8781/tianjinCpe_war_exploded/cpe/sync";


    private boolean http = false;

    //防止错误重发
    private Integer resert = 0;

    //服务器出错
    private Integer server = 0;

    /**
     * 日志写入LogAdapter适配器的ListView中
     */
//    List<String> list_runlog = new LinkedList<String>();
//
//    /**
//     * 心跳写入LogAdapter适配器的ListView中
//     */
//    List<HeartbeatLog> list_heartlog = new LinkedList<HeartbeatLog>();
//
//    /**
//     *
//     */
//    RunningLogAdapter runningLogAdapter = null;
//
//    HeartLogAdapter heartLogAdapter = null;

    /**
     * 后台切换
     */
    ServiceOperation serviceOperation;

    /**
     * 信息推送安全
     */
    private boolean informationSafety = false;

    /**
     * 1.串口对象与参数
     * 2.记录串口状态情况,保证它在发生变化时才会生成日志
     */
    private String path = "/dev/ttyHSL1";
    private int baudrate = 115200;
    private int byteRate = 16000000;

    private SerialPort serialPort = null;
    private SerialPortUtils serialPortUtils = new SerialPortUtils();

    //待响应返回数据
    /**
     * 串口验证对方请求次数 + 1
     */
    private Integer ack;

    /**
     * 串口响应次数
     */
    private Integer seq = 0;

    /**
     * 第一次接收数据记录
     */
    private Integer cpe1_seq;

    OkHttpClient client = null;

    /**
     * 第二次接收数据记录
     */
    private Integer cpe1_2_seq;
    private Integer cpe1_2_ack;

    /**
     * 通讯类型 1.MQTT 2.UDP 3.TCP
     */
    private Integer communication;

    /**
     * 初始化响应次数 true 第一次响应, false 第二次响应
     */
    private Boolean initResponse = true;

    /**
     * 串口连接线程
     */
    SerialPortThread serialPortThread = new SerialPortThread();

    /**
     * 连续响应定时器
     */
    Timer timerConnectionVerificationResponse = new Timer();

    /**
     * RunningLog定时器
     */
    // Timer timerRunningLog = new Timer();
    RunningLogTimer runningLogTimer = new RunningLogTimer();
    /**
     * HeartbeatLog定时器,生成心跳数据
     */
    Timer timerHeartbeatLog = new Timer();
    /**
     * 串口队列的分类、处理
     */
    SerialPortQueueThread serialPortQueueThread = new SerialPortQueueThread();
    /**
     * 下发集合数据队列
     */
    IssueQueueThread issueQueueThread = new IssueQueueThread();
    /**
     * 推送外网数据
     */
    UploadQueueThread uploadQueueThread = new UploadQueueThread();


    /**
     * 密码宽
     */
    EditText text;

    public void initThread() {
        Log.e("设备标识", SignalIntensity.getSERIAL());

        /**
         * 初始化
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);


        /**
         * 动态获取权限
         */
        XXPermissions.with(this)
                .permission(Permission.READ_PHONE_STATE)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            LogParameters.Running(5, 3, "获取SIM卡权限成功", true);
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            LogParameters.Running(5, 3, "被永久拒绝授权,请手动授予SIM卡权限", false);
                            //如果是被永久拒绝将跳转到应用权限系统设置页面
                        } else {
                            LogParameters.Running(5, 3, "获取SIM卡权限失败", false);
                        }
                    }
                });


        LitePal.initialize(this);
        LitePal.getDatabase();
        InitDataBase.delete();

        serialPortThread.start(); //串口连接
        serialPortQueueThread.start();//串口接收
        issueQueueThread.start();//串口下发

    }

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initThread();
    }

    @SneakyThrows
    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceOperation.unBind();
    }

    /**
     * MQTT消息接口
     */
    @Override
    public void onMqttReceive(String topic, String message) {
        issueQueue.maxPut(message, QueueSize.MaxSize);
    }

    /**
     * UDP消息接口
     */
    @Override
    public void onUdpReceive(String data) {
        issueQueue.maxPut(data, QueueSize.MaxSize);
    }

    /**
     * TCP消息接口
     */
    @Override
    public void onTcpReceive(String data) {
        issueQueue.maxPut(data, QueueSize.MaxSize);
    }

    /**
     * MQTT推送允许
     */
    @Override
    public void mqttServiceSecurity(boolean message) {
        this.informationSafety = message;
    }

    /**
     * UDP推送允许
     */
    @Override
    public void udpServiceSecurity(boolean state) {
        this.informationSafety = state;
    }

    /**
     * TCP推送允许
     */
    @Override
    public void tcpServiceSecurity(boolean state) {
        this.informationSafety = state;
    }


    /**
     * 串口连接
     */
    class SerialPortThread extends Thread {
        @Override
        public void run() {
            serialPort = serialPortUtils.openSerialPort(path, baudrate, 0); //当出现崩溃的情况会跳到这里重连

            LogParameters.Running(0, 3,
                    "成功打开串行端口,开始接收数据",
                    true
            );
            /**
             * 监听存入串口队列
             * @contentLenght 有效数据位
             * @buffer 总字节
             * @cursor 无用字节
             * @int headerLength 字节头
             */
            serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
                @Override
                public void onDataReceive(byte[] buffer, int contentLenght, int cursor, int headerLength) throws UnsupportedEncodingException {

                    byte[] bytes = new byte[contentLenght];

                    System.arraycopy(buffer, cursor + headerLength, bytes, 0, contentLenght);

                    String data = new String(bytes, "utf8");

                    Log.i("串口CRC校验前的数据", data);

                    //CRC校验
                    if (EncapsulationFormat.judgeCRC(data)) {

                        String string = EncapsulationFormat.interceptDataBits(data); //拿到完整包

                        Log.i("串口CRC校验后的数据", string);

                        serialPortQueue.maxPut(string, QueueSize.MaxSize);
                    }
                }
            });
        }
    }

    /**
     * 串口验证响应
     */
    class ConnectionVerificationResponseTimer extends TimerTask {
        @Override
        public void run() {
            Log.d("Y-CPE2-V1.0串口响应", "+" + ++seq);
            ConnectionVerificationResponse connectionVerificationResponse = new ConnectionVerificationResponse();
            connectionVerificationResponse.setSyn(1);
            connectionVerificationResponse.setAck(1);
            connectionVerificationResponse.setSeqq(seq);
            connectionVerificationResponse.setAckk(ack);

            issueQueue.maxPut(EncapsulationFormat.initializationEncapsulation
                    (InitCpeAnalysis.toJsonString(connectionVerificationResponse), 4), QueueSize.MaxSize);
        }
    }

    /**
     * RunningLog定时器
     * 9000ms -> 1分30秒一次
     * 临时库，临时储存->查询->删除
     */
    class RunningLogTimer extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                Thread.sleep(10000);
                //查库
                List<RunningLog> runningLogs = LitePal.limit(10).find(RunningLog.class);
                if (runningLogs != null) {
                    for (int i = 0; i < runningLogs.size(); i++) {

                        String runningLog = InitCpeAnalysis.toJsonString(runningLogs.get(i));
                        //todo 外层封装
                        String data = EncapsulationFormat.initializationEncapsulation(runningLog, 2);
                        //todo 下发集合数据队列
                        issueQueue.maxPut(data, QueueSize.MaxSize);

                        runningLogs.get(i).delete();
                    }
                }
            }
        }
    }


    /**
     * HeartbeatLog定时器,生成心跳数据
     * 3600000ms -> 1小时一次
     * 待修改
     */
    class HeartbeatLogTimer extends TimerTask {
        @Override
        public void run() {

            final HeartbeatLog heartbeatLog = LogParameters.Heart(5, true, MainActivity.this);

            String data = EncapsulationFormat.initializationEncapsulation
                    (InitCpeAnalysis.toJsonString(heartbeatLog), 3);

            //下发队列
            issueQueue.put(data);

            heartbeatLog.save();

        }
    }

    /**
     * 串口队列的分类、处理
     */
    class SerialPortQueueThread extends Thread {
        @SneakyThrows
        @SuppressLint("LongLogTag")
        @Override
        public void run() {
            while (true) {
                try {
                    if (!serialPortQueue.empty()) {

                        String data = serialPortQueue.get();

                        //todo 1.在这里判断它对应的消息类型，并存入相应的队列,日志消息无队列，直接上传服务器
                        //1.数据位截取
                        String dataBuffer = EncapsulationFormat.interceptData(data);
                        Log.e("数据位截取", dataBuffer);

                        //2.消息类型判断
                        Integer type =
                                EncapsulationFormat.messageTypeJudgment
                                        (EncapsulationFormat.interceptMessageType(data));


                        //TODO 2.读写存储
                        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);

                        SharedPreferences.Editor editor = preferences.edit();

                        switch (type) {
                        /*
                        初始化配置
                         */
                            case 0: {

                                //数据转对象
                                InitCpeDataField initCpeDataField =
                                        InitCpeAnalysis.toJson(dataBuffer, InitCpeDataField.class);

//                            Log.e("修改了系统时间之前的时间", String.valueOf(SystemClock.currentThreadTimeMillis()));

                                // Log.e("修改的系统时间是",initCpeDataField.getModernClock());

                                //修改系统时间
//                            SystemClock.setCurrentTimeMillis(initCpeDataField.getModernClock());


                                LogParameters.Running(0, 2,
                                        "修改系统时间，当前系统时间:" + initCpeDataField.getModernClock(),
                                        true
                                );


//                            Log.e("修改了系统时间", String.valueOf(SystemClock.currentThreadTimeMillis()));

                                url = initCpeDataField.getUrl();

                                if (url != null) {
                                    LogParameters.Running(0, 3,
                                            "配置后的HTTP地址" + url,
                                            true
                                    );
                                }

                                editor.putLong("timestamp", initCpeDataField.getTimestamp());
                                editor.putBoolean("judge", true);

                                communication = initCpeDataField.getProtocolType();

                                //Thread.sleep(2000);
                                //强行关闭应用,指定包名
                                //- 此方法是@hide的方法：
                                //解决方案是使用java的反射机制完成调用，代码如下：
//                            ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
//                            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
//                            method.invoke(mActivityManager,"com.sgcc.vpn_client");  //packageName是需要强制停止的应用程序包名
//
//                            Thread.sleep(10000);
//
//                            //重启VPN应用
//                            PackageManager packageManager = getPackageManager();
//                            Intent intent = packageManager.getLaunchIntentForPackage("com.sgcc.vpn_client");
//                            startActivity(intent);


                                //TODO 对应通讯服务类型
                                if (communication != null) {
                                    switch (communication) {
                                        case 1: {
                                            //参数存储
                                            editor.putString("host", initCpeDataField.getHost());
                                            editor.putString("username", initCpeDataField.getUsername());
                                            editor.putString("password", initCpeDataField.getPassword());

                                            if (initCpeDataField.getClientId() != null) {
                                                editor.putString("clientId", initCpeDataField.getClientId());
                                            }

                                            //List集合转JSON字符串，这里修改了字段，原字段topicNameList，修改后topicList
                                            editor.putString("topicNameList",
                                                    InitCpeAnalysis.toJsonString(initCpeDataField.getTopicList())
                                            );

                                            Log.e("list集合转字符串",
                                                    InitCpeAnalysis.toJsonString(initCpeDataField.getTopicList()));

                                            editor.apply();

                                            //TODO 开启MQTT通讯服务
                                            serviceOperation = new ServiceOperation
                                                    (MainActivity.this, 1);
                                            serviceOperation.serviceOnCreate();
                                            break;
                                        }

                                        case 2: {
                                            postalAddress.setIp(initCpeDataField.getIp());
                                            postalAddress.setPort(initCpeDataField.getPort());

                                            editor.putString("ip", initCpeDataField.getIp());
                                            editor.putInt("port", initCpeDataField.getPort());
                                            editor.apply();
                                            //TODO 开启UDP通讯服务
                                            serviceOperation = new ServiceOperation
                                                    (MainActivity.this, 2);
                                            serviceOperation.serviceOnCreate();
                                            break;
                                        }

                                        case 3: {

                                            postalAddress.setIp(initCpeDataField.getIp());
                                            postalAddress.setPort(initCpeDataField.getPort());

                                            editor.putString("ip", initCpeDataField.getIp());
                                            editor.putInt("port", initCpeDataField.getPort());
                                            editor.apply();
                                            //TODO 开启TCP通讯服务
                                            serviceOperation = new ServiceOperation
                                                    (MainActivity.this, 3);
                                            serviceOperation.serviceOnCreate();
                                            break;
                                        }

                                        //HTTP目前暂无后台服务
                                        case 4: {
                                            http = true;
                                            informationSafety = true;
                                            LogParameters.Running(0, 1,
                                                    "开启HTTP通讯:开启完毕",
                                                    true
                                            );
                                            break;
                                        }

                                        default: {
                                            LogParameters.Running(0, 3,
                                                    "protocolType字段:非法",
                                                    false
                                            );
                                            break;
                                        }
                                    }
                                }
                                break;

                            }
                        /*
                        需要上传的数据
                        */
                            case 1: {
                                //存入推送队列
                                uploadQueue.maxPut(dataBuffer, QueueSize.MaxSize);
                                Log.d("1", "----------------------");
                                break;
                            }
                        /*
                        串口连接验证
                         */
                            case 4: {
                                if (initResponse) {
                                    ConnectionVerificationReceiveOne connectionVerificationReceiveOne =
                                            InitCpeAnalysis.toJson(dataBuffer, ConnectionVerificationReceiveOne.class);

                                    Integer SYN = connectionVerificationReceiveOne.getSyn();
                                    Integer seq = connectionVerificationReceiveOne.getSeq();

                                    Log.d("第一次响应", "SYN:" + SYN);
                                    Log.d("第一次响应", "seq:" + seq);

                                    LogParameters.Running(0, 3,
                                            "第一次响应,SYN：" + SYN + "\n" +
                                                    "第一次响应,seq:" + seq,
                                            true
                                    );

                                    initResponse = false;

                                    if (SYN == 1) {
                                        cpe1_seq = seq; //第一次接收数据记录
                                        ack = seq + 1; //即将推送的ack数据
                                        //开启定时器
                                        timerConnectionVerificationResponse.schedule
                                                (new ConnectionVerificationResponseTimer(), 10000, 90000);
                                    }
                                } else {
                                    ConnectionVerificationReceiveTwo connectionVerificationReceiveTwo =
                                            InitCpeAnalysis.toJson(dataBuffer, ConnectionVerificationReceiveTwo.class);

                                    Integer ACK = connectionVerificationReceiveTwo.getAck();
                                    cpe1_2_seq = connectionVerificationReceiveTwo.getSeqq();
                                    cpe1_2_ack = connectionVerificationReceiveTwo.getAckk();

                                    Log.d("第二次响应", "ACK:" + ACK);
                                    Log.d("第二次响应", "seq:" + cpe1_2_seq);
                                    Log.d("第二次响应", "ack:" + cpe1_2_ack);

                                    Log.d("本机推送次数seq", String.valueOf(seq));

                                    LogParameters.Running(0, 3,
                                            "第二次响应,ACK:" + ACK + "\n" +
                                                    "第二次响应,seq:" + cpe1_2_seq + "\n" +
                                                    "第二次响应,ack:" + cpe1_2_ack + "\n" +
                                                    "本机推送次数seq:" + seq,
                                            true
                                    );

                                    initResponse = true;

                                    if (ACK == 1 && cpe1_2_seq == ack && cpe1_2_ack == seq + 1) {
                                        //确定串口无异常，推送设备标识给CPE1，并且开启所有线程，开始正常通信
                                        timerConnectionVerificationResponse.cancel(); //关闭
                                        timerConnectionVerificationResponse = null;


                                        LogParameters.Running(0, 3,
                                                "TY-CPE1-V2.0:确认TY-CPE1-V1.0收发、TY-CPE2-V2.0收发",
                                                true
                                        );

                                        //timerRunningLog.schedule(new RunningLogTimer(), 1000, 1000);

                                        runningLogTimer.start();

                                        timerHeartbeatLog.schedule(new HeartbeatLogTimer(), 1000, 300000);

                                        uploadQueueThread.start();

                                        InitializationConfiguration initializationConfiguration =
                                                new InitializationConfiguration("OK", SignalIntensity.getSERIAL());


                                        String init = EncapsulationFormat.initializationEncapsulation
                                                (InitCpeAnalysis.toJsonString(initializationConfiguration), 0);

                                        issueQueue.maxPut(init, QueueSize.MaxSize);
                                    }
                                }
                                break;
                            }
                        /*
                         数据非法
                         */
                            default: {
                                Log.e("SerialPortQueueThread", "数据非法");
                                LogParameters.Running(0, 3,
                                        "串口消息类型校验位非法",
                                        false
                                );
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 下发集合数据队列
     */
    class IssueQueueThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                try {
                    if (!issueQueue.empty() && serialPort != null) {
                        String str = issueQueue.get();

                        if (str.length() > byteRate) {
                            LogParameters.Running(6, 3,
                                    "超过极限包长,丢包",
                                    false
                            );
                        } else {
                            serialPortUtils.sendSerialPort(str);
                            Log.d("下发数据", str);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 推送内网数据
     */
    class UploadQueueThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                try {
                    if (!uploadQueue.empty()) {
                        String data = uploadQueue.get();
                        if (!data.equals("")) {
                            DataTransmissionProtocol dataTransmissionProtocol =
                                    InitCpeAnalysis.toJson(data, DataTransmissionProtocol.class);

                            Integer protocolType = dataTransmissionProtocol.getProtocolType();

                            data = dataTransmissionProtocol.getContent();
                            String cpeTwoUid = dataTransmissionProtocol.getCpeTwoUid();

                            //设备标识校验
                            if (SignalIntensity.getSERIAL().equals(cpeTwoUid) && protocolType != null && communication != null) {
                                switch (protocolType) {
                                    //MQTT
                                    case 1: {

                                        if (communication == 1) {
                                            MqttClientService.send
                                                    (dataTransmissionProtocol.getTopicName(),
                                                            data, dataTransmissionProtocol.getQos());
                                            Log.e("数据上传", data);
                                        }

                                        break;
                                    }
                                    //UDP
                                    case 2: {
                                        if (communication == 2) {
                                            UdpService.send
                                                    (data);
                                            Log.e("数据上传", data);
                                            break;
                                        }
                                    }
                                    //TCP
                                    case 3: {
                                        if (communication == 3)
                                            TcpClientService.send
                                                    (data);
                                        Log.e("数据上传", data);
                                        break;
                                    }
                                    //HTTP,分队列进行
                                    case 4: {
                                        if (communication == 4) {
                                            if (http) {

                                                Log.e("请求了", "HTTP,分队列进行");
                                                long id = dataTransmissionProtocol.getMid();

                                                if (client == null) {
                                                    client = new OkHttpClient.Builder()
                                                            .connectTimeout(1, TimeUnit.SECONDS)
                                                            .readTimeout(2, TimeUnit.SECONDS)
                                                            .build();
                                                }

                                                HashMap<String, String> hasHMap = new HashMap<>();
                                                hasHMap.put("content", data);

                                                Log.e("数据", data);

                                                Request request;

                                                Response response = null;


//                                  Log.e("请求地址",text.getText().toString());

                                                if (url != null) {
                                                    request = FormPort.request(url, hasHMap);

                                                    if (request != null) {
                                                        //成功
                                                        try {
                                                            response = client.newCall(request).execute();
                                                        } catch (Exception e) {
                                                            //错误
                                                            switch (resert) {
                                                                case 0: {
                                                                    //发送错误
                                                                    LogParameters.Running(4, 3,
                                                                            "发送错误:" + e.getMessage(),
                                                                            false
                                                                    );
                                                                    resert = 1;
                                                                    break;
                                                                }
                                                                case 1: {
                                                                    Log.e("1", "没网");
                                                                    //让他跑空
                                                                    break;
                                                                }
                                                            }
                                                        }

                                                        if (response != null) {
                                                            if (response.isSuccessful()) {
                                                                server = 0;
                                                                resert = 0;
                                                                String string = response.body().string();

                                                                Log.e("上传", string);

                                                                //下发封装
                                                                dataTransmissionProtocol.setMid(id);
                                                                dataTransmissionProtocol.setCpeTwoUid(SignalIntensity.getSERIAL());
                                                                dataTransmissionProtocol.setContent(string);
                                                                dataTransmissionProtocol.setProtocolType(4);

                                                                //下发队列
                                                                string = InitCpeAnalysis.toJsonString(dataTransmissionProtocol);

                                                                issueQueue.maxPut
                                                                        (EncapsulationFormat.initializationEncapsulation(string, 1),
                                                                                QueueSize.MaxSize);

                                                            } else {
                                                                //服务器出错
                                                                switch (server) {
                                                                    case 0: {
                                                                        //服务器错误
                                                                        LogParameters.Running(4, 3,
                                                                                "请检查服务器的情况，响应错误",
                                                                                false
                                                                        );
                                                                        server = 1;
                                                                        break;
                                                                    }
                                                                    case 1: {
                                                                        Log.e("没网络", "!");
                                                                        //让他跑空
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                            }
                                        }

                                    }
                                }
                            } else {
                                LogParameters.Running(4, 3,
                                        "设备标识错误,我的设备" + SignalIntensity.getSERIAL() + "," + "收到的设备" + cpeTwoUid,
                                        false
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
