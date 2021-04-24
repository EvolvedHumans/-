package com.communication.yang.cpe_1;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.communication.yang.cpe_1.adapter.HeartLogAdapter;
import com.communication.yang.cpe_1.adapter.RunningLogAdapter;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.litepal.LitePal;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.PackageSerialPort.EncapsulationFormat;
import db.InitDataBase;
import db.agreement.InitializationConfiguration;
import db.analyticMethod.InitCpeAnalysis;
import db.log.LogParameters;
import db.log.RunningLog;
import db.log.HeartbeatLog;
import db.serialPort.ConnectionVerificationPublishOne;
import db.serialPort.ConnectionVerificationPublishTwo;
import db.serialPort.ConnectionVerificationReceive;
import http.HttpClient;
import android_serialport_api.SerialPort;
import lombok.SneakyThrows;
import queue.CloudDataQueue;
import queue.HeartbeatQueue;
import queue.QueueSize;
import serviceMqtt.IGetMqttClientMessageCallBack;
import serviceMqtt.MqttClientService;
import serviceMqtt.MqttClientServiceConnection;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import serviceMqtt.MqttConfig;
import serviceMqtt.SgitTopic;
import tool.IntenetUtil;
import queue.IssueQueue;
import queue.SerialPortQueue;
import tool.SignalIntensity;
import android_serialport_api.SerialPortUtils;

public class MainActivity extends AppCompatActivity implements IGetMqttClientMessageCallBack, View.OnClickListener {

    /**
     * 1.队列实例，接收service服务数据队列
     */
    IssueQueue issueQueue = IssueQueue.getInstance();

    /**
     * 2.队列实例，接收串口服务数据队列
     */
    SerialPortQueue serialPortQueue = SerialPortQueue.getInstance();

    /**
     * 3.MQTT主题发布云数据队列
     */
    CloudDataQueue cloudDataQueue = CloudDataQueue.getInstance();

    /**
     * 日志写入LogAdapter适配器的ListView中
     */
    List<String> list_runlog = new LinkedList<>();

    /**
     * 心跳写入LogAdapter适配器的ListView中
     */
    List<HeartbeatLog> list_heartlog = new LinkedList<>();

    /**
     * 心跳链接、队列,发送心跳信息
     */
    String heartbeatLogUrl = MqttConfig.mHeartHost();
    HeartbeatQueue heartbeatQueue = HeartbeatQueue.getInstance();

    /**
     * 日志链接，发送日志消息
     */
    String runningLogUrl = MqttConfig.mRunningHost();

    /**
     * COM6参数
     */
    private String path = "/dev/ttyHSL1";
    private int baudrate = 115200;

    /**
     * 串口对象，1.开启串口 2.关闭串口  //默认串口状态是关闭的
     */
    private SerialPortUtils serialPortUtils = new SerialPortUtils();
    private SerialPort serialPort = null;

    /**
     * 串口连接响应推送次数
     */
    private Integer seq = 0;

    /**
     * 后台安全标志位
     */
    private boolean allowToSend = false;

    /**
     * 后台切换
     */
    BindService bindService = null;

    /**
     * JSON格式数据
     */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * 日志适配器
     */
    //日志适配器
    RunningLogAdapter runningLogAdapter = null;

    /*
    心跳适配器
     */
    //心跳适配器
    HeartLogAdapter heartLogAdapter = null;

    /**
     * 线程与定时器
     */
    //1.串口连接
    SerialPortThread serialPortThread = new SerialPortThread();
    //2.串口检测定时器
    Timer timerConnection = new Timer();
    //3.串口队列的分类、处理
    SerialPortQueueThread serialPortQueueThread = new SerialPortQueueThread();
    //4.云数据队列，上传
    CloudDataQueueThread cloudDataQueueThread = new CloudDataQueueThread();
    //5.Mqtt消息队列下发
    IssueQueueThread issueQueueThread = new IssueQueueThread();
    //6.上传心跳
    HeartbeatLogThread heartbeatLogThread = new HeartbeatLogThread();
    //7.定时删库
    Timer timerDataBase = new Timer();
    //8.定时上传日志
    Timer timerRunningLog = new Timer();
    //9.产生心跳
    Timer timerHeartbeatLog = new Timer();

    //mqtt接到的总数据量
    Integer mqtt = 0;

    //串口下发总数据量
    Integer serial = 0;


    public void init() {
        /**
         * 保持屏幕永不息屏，永久唤醒
         */
        getWindow().setFlags
                (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        /**
         * 动态获取权限
         */
        XXPermissions.with(this)
                .permission(Permission.READ_PHONE_STATE)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            LogParameters.Running(5, "获取SIM卡权限成功", true);
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            LogParameters.Running(5, "被永久拒绝授权,请手动授予SIM卡权限", false);
                            //如果是被永久拒绝将跳转到应用权限系统设置页面
                        } else {
                            LogParameters.Running(5, "获取SIM卡权限失败", false);
                        }
                    }
                });


        LitePal.initialize(this);
        LitePal.getDatabase();
        InitDataBase.delete();

        serialPortThread.start();

        timerConnection.schedule
                (new ConnectionVerificationTimer(), 10000, 60000);

        serialPortQueueThread.start();

        cloudDataQueueThread.start();

        issueQueueThread.start();

        heartbeatLogThread.start();

        timerDataBase.schedule
                (new InitDataBaseTimer(), 9000000, 9000000);

        timerRunningLog.schedule
                (new RunningLogTimer(), 1000, 1000);

        timerHeartbeatLog.schedule
                (new HeartbeatLogTimer(), 60000, 300000);
    }

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    /**
     * MQTT消息接口
     */
    @Override
    public void setMessage(String topic, String message) {

        Log.e("CPE1接到总数据量(接到后台所有数据)", String.valueOf(++mqtt));

        //存储队列issueQueue
        issueQueue.maxPut(message, QueueSize.MaxSize);
    }

    /**
     * 允许MQTT信息发送标志位
     */
    @Override
    public void setAllowToSend(boolean allowToSend) {
        this.allowToSend = allowToSend;
    }

    /**
     * 日志查看框
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonPanel: {
                View view =
                        getLayoutInflater().inflate
                                (R.layout.popwindow, null, false);

                PopupWindow popupWindow = new PopupWindow
                        (view, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);

                //显示在ID为R.id.buttonPanel控件下,x偏移量 0,y偏移量 0
                popupWindow.showAsDropDown(findViewById(R.id.buttonPanel), 0, 0);

                popupWindow.setFocusable(true);

                ListView listView = view.findViewById(R.id.list_item);
                //日志适配器
                if (runningLogAdapter == null) {
                    runningLogAdapter =
                            new RunningLogAdapter(MainActivity.this, R.layout.runninglog_listview, list_runlog);
                }
                listView.setAdapter(runningLogAdapter);


                ListView listView1 = view.findViewById(R.id.list_item_1);
                //心跳适配器
                if (heartLogAdapter == null) {
                    heartLogAdapter =
                            new HeartLogAdapter(MainActivity.this, R.layout.heartlog_listview, list_heartlog);
                }

                listView1.setAdapter(heartLogAdapter);
            }
        }
    }

    class SerialPortThread extends Thread {
        @Override
        public void run() {
            serialPort = serialPortUtils.openSerialPort(path, baudrate);
            serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
                @Override
                public void onDataReceive(byte[] buffer, int contentLenght, int cursor, int headerLength) throws UnsupportedEncodingException {
                    byte[] bytes = new byte[contentLenght];
                    System.arraycopy(buffer, cursor + headerLength, bytes, 0, contentLenght);
                    String data = new String(bytes, "utf8");
                    //CRC校验
                    if (EncapsulationFormat.judgeCRC(data)) {
                        String string = EncapsulationFormat.interceptDataBits(data); //拿到完整包
                        serialPortQueue.maxPut(string, QueueSize.MaxSize);
                    }
                }
            });
        }
    }

    class ConnectionVerificationTimer extends TimerTask {
        @Override
        public void run() {
            LogParameters.Running(0,
                    "TY-CPE1-V1.0连接请求:+" + ++seq,
                    true
            );
            ConnectionVerificationPublishOne verificationPublishOne = new ConnectionVerificationPublishOne();
            verificationPublishOne.setSyn(1);
            verificationPublishOne.setSeq(seq); //下次推送+1
            //封装
            String data = EncapsulationFormat.initializationEncapsulation
                    (InitCpeAnalysis.toJsonString(verificationPublishOne), 4);
            issueQueue.maxPut(data, QueueSize.MaxSize);
        }
    }

    class SerialPortQueueThread extends Thread {
        @SuppressLint("LongLogTag")
        @Override
        public void run() {
            while (true) {
                if (!serialPortQueue.empty()) {
                    String data = serialPortQueue.get();
                    //todo 在这里判断它对应的消息类型，并存入相应的队列,日志消息无队列，直接上传服务器
                    //1.消息类型截取
                    String dataType = EncapsulationFormat.interceptMessageType(data);
                    //2.数据位截取
                    String dataBuffer = EncapsulationFormat.interceptData(data);
                    //3.消息类型判断
                    Integer type = EncapsulationFormat.messageTypeJudgment(dataType);
                    switch (type) {
                        case 0: {
                            //todo 初始化配置,CPE2启动后给CPE1发送设备标识信息
                            Log.d("初始化配置", dataBuffer);
                            InitializationConfiguration initializationConfiguration =
                                    InitCpeAnalysis.toJson(dataBuffer, InitializationConfiguration.class);
                            if (initializationConfiguration.getMsg().equals("OK")) {
                                SharedPreferences pre = getSharedPreferences("data", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pre.edit();
                                LogParameters.Running(0,
                                        "cpe1uid:+" + SignalIntensity.getSERIAL() + "\n" +
                                                "cpe2uid:" + initializationConfiguration.getUid(),
                                        true
                                );
                                editor.putString("cpe1uid", SignalIntensity.getSERIAL());
                                editor.putString("cpe2uid", initializationConfiguration.getUid());
                                editor.putBoolean("judge", true);
                                editor.apply(); //提交存储数据
                                //todo 开启后台服务
                                /**
                                 * 开启后台服务
                                 */
                                bindService = new BindService
                                        (MainActivity.this, new MqttClientServiceConnection());
                                //绑定,然后开启onStartCommand方法
                                bindService.serviceOnBind();
                            } else {
                                LogParameters.Running(0,
                                        "初始化配置:失败,不能开启后台服务",
                                        false
                                );
                            }
                            break;
                        }

                        case 1: {
                            //todo 上传MQTT的数据队列
                            cloudDataQueue.maxPut(dataBuffer, QueueSize.MaxSize);
                            break;
                        }

                        case 2: {
                            //todo 以日志形式存储的日志信息
                            Log.d("日志", dataBuffer);
                            RunningLog runningLog =
                                    InitCpeAnalysis.toJson(dataBuffer, RunningLog.class);
                            RunningLog runningLog1 = new RunningLog();
                            runningLog1.setCpeType(runningLog.getCpeType());
                            runningLog1.setData(runningLog.getData());
                            runningLog1.setSimId(runningLog.getSimId());
                            runningLog1.setStatus(runningLog.getStatus());
                            runningLog1.setTimestamp(runningLog.getTimestamp());
                            runningLog1.setType(runningLog.getType());
                            runningLog1.setUid(runningLog.getUid());
                            runningLog1.save();
                            Log.e("保存日志", String.valueOf(runningLog));
                            break;
                        }
                        case 3: {
                            //todo 上传心跳的数据队列 ， 并且获取CPE2的UID存储起来
                            heartbeatQueue.maxPut(dataBuffer, QueueSize.MaxSize);
                            break;
                        }
                        case 4: {
                            //todo 串口连接验证
                            Log.d("串口连接验证", dataBuffer);
                            ConnectionVerificationReceive connectionVerificationReceive =
                                    InitCpeAnalysis.toJson(dataBuffer, ConnectionVerificationReceive.class);
                            Integer SYN = connectionVerificationReceive.getSyn(); //接收连接
                            Integer ACK = connectionVerificationReceive.getAck(); //对方确认连接
                            Integer cpe2_seq = connectionVerificationReceive.getSeqq(); //对方确认推送次数
                            Integer cpe2_ack = connectionVerificationReceive.getAckk(); //我的推送次数+1
                            Log.d("TY-CPE1-V1.0响应", "目标seq:" + cpe2_seq);
                            Log.d("TY-CPE1-V1.0响应", "目标ack:" + cpe2_ack);
                            Log.d("TY-CPE1-V1.0本机seq", "" + seq);
                            LogParameters.Running(0,
                                    "TY-CPE1-V1.0响应,目标seq:" + cpe2_seq + "\n" +
                                            "TY-CPE1-V1.0响应,目标ack:" + cpe2_ack + "\n" +
                                            "TY-CPE1-V1.0本机seq",
                                    true
                            );
                            if (SYN == 1 && ACK == 1 && cpe2_ack == seq + 1) {
                                Log.d("TY-CPE1-V1.0", "确认TY-CPE1-V1.0收发、TY-CPE2-V1.0收发");
                                LogParameters.Running(3,
                                        "TY-CPE1-V1.0:确认TY-CPE1-V1.0收发、TY-CPE2-V1.0收发",
                                        true
                                );
                                timerConnection.cancel();
                                timerConnection = null;
                                ConnectionVerificationPublishTwo connectionVerificationPublishTwo = new ConnectionVerificationPublishTwo();
                                connectionVerificationPublishTwo.setAck(1);
                                connectionVerificationPublishTwo.setSeqq(cpe2_ack);
                                connectionVerificationPublishTwo.setAckk(cpe2_seq + 1);
                                issueQueue.maxPut(EncapsulationFormat.initializationEncapsulation
                                                (InitCpeAnalysis.toJsonString(connectionVerificationPublishTwo), 4)
                                        , QueueSize.MaxSize);
                            } else {
                                LogParameters.Running(0,
                                        "SYN与ACK，异常",
                                        false
                                );
                            }
                            break;
                        }
                        default: {
                            Log.d("数据类型非法", dataBuffer);
                            LogParameters.Running(0,
                                    "数据类型非法",
                                    false
                            );
                            break;
                        }

                    }
                }
            }
        }
    }

    class CloudDataQueueThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                if (!cloudDataQueue.empty() && allowToSend) {
                    String cloudData = cloudDataQueue.get();
                    MqttClientService.send(SgitTopic.CPE_C2S, cloudData);
                    Log.d("云数据上传", cloudData);
                }
            }
        }
    }

    class IssueQueueThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                //判断串口状态，看是否允许发送数据
                if (!issueQueue.empty() && serialPort != null) {
                    String str = issueQueue.get();

                    if (str.length() > QueueSize.MaxSize) {
                        LogParameters.Running(6,
                                "超过极限包长,丢包",
                                false
                        );
                    } else {
                        serialPortUtils.sendSerialPort(str);
                        Log.d("下发数据", str);
                    }

                }
            }
        }
    }

    class HeartbeatLogThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                switch (IntenetUtil.getNetworkState(MainActivity.this)) {
                    case 0: {
                        //不上传
                        break;
                    }
                    default: {
                        //有网
                        if (!heartbeatQueue.empty()) {
                            String data = heartbeatQueue.get();
                            Log.d("上传心跳", data);

                            //构造RequestBody
                            RequestBody requestBody = RequestBody.create(JSON, data);
                            //上传 心跳
                            HttpClient.httpPostJSONJava(heartbeatLogUrl, requestBody, null);
                        }
                        break;
                    }
                }

            }
        }
    }

    class InitDataBaseTimer extends TimerTask {
        @Override
        public void run() {
            InitDataBase.delete();
        }
    }

    class RunningLogTimer extends TimerTask {
        @SneakyThrows
        @Override
        public void run() {

            switch (IntenetUtil.getNetworkState(MainActivity.this)) {
                case 0: {
                    break;
                }
                default: {
                    //查库
                    List<RunningLog> runningLogs = LitePal.findAll(RunningLog.class);
                    if (runningLogs != null) {
                        for (int i = 0; i < runningLogs.size(); i++) {
                            Log.d("上传日志", InitCpeAnalysis.toJsonString(runningLogs.get(i)));
                            //构造RequestBody类型
                            RequestBody requestBody =
                                    RequestBody.create
                                            (JSON, InitCpeAnalysis.toJsonString(runningLogs.get(i)));
                            HttpClient.httpPostJSONJava(runningLogUrl, requestBody, null);
                            synchronized (this) {
                                list_runlog.add(runningLogs.get(i).getData());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (runningLogAdapter != null)
                                            runningLogAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                            //发送成功，信息从库上删除
                            runningLogs.get(i).delete();
                        }
                    }
                    break;
                }
            }
        }
    }

    class HeartbeatLogTimer extends TimerTask {
        @SneakyThrows
        @Override
        public void run() {

            HeartbeatLog heartbeatLog = LogParameters.Heart
                    (0, true, MainActivity.this);

            synchronized (this) {
                list_heartlog.add(heartbeatLog);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (heartLogAdapter != null)
                        heartLogAdapter.notifyDataSetChanged();
                }
            });

            //放入队列
            heartbeatQueue.maxPut(InitCpeAnalysis.toJsonString(heartbeatLog), QueueSize.MaxSizeLength);
        }
    }

}

