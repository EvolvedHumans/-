package serviceUdp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import android_serialport_api.PackageSerialPort.EncapsulationFormat;
import db.agreement.DataTransmissionProtocol;
import db.analyticMethod.InitCpeAnalysis;
import db.log.LogParameters;
import db.log.RunningLog;
import lombok.SneakyThrows;
import tool.SignalIntensity;
import tool.Timestamp;
/**
 * @Author : YangFan
 * @Date : 2020年10月13日 14:31
 * @effect :UDP通信后台服务
 */

/**
 * Android后台，每个Service都需要在AndroidManifest.xml注册才能生效
 * 这是Android四大组件共有的特点
 */
public class UdpService extends Service {

    public String TAG = "UdpService";

    /**
     * UDP通信
     */
    private static Udp udp;

    /**
     * 与MainActivity通信接口
     */
    private IGetUdpMessageCallBack iGetUdpMessageCallBack;

    /**
     * 消息读取器
     */
    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor editor;

    /**
     * 获取网络情况
     */
    SignalIntensity signalIntensity;

    /**
     * 服务端IP
     */
    String ip;
    Integer port;

    /**
     * 网络连接情况，默认是false
     */
    private boolean network = false;


    /**
     * 是否在同一网段,默认是false
     */
    private boolean sameNetwork = false;

    /**
     * 改变标志位，0未初始化，1变化，2未变化
     */
    private Integer state = 0;

    /**
     * 初始化Udp
     */
    private void init() {

        sharedPreferences = getSharedPreferences("data",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        signalIntensity = new SignalIntensity();

        InitThread initThread = new InitThread();
        initThread.start();
    }

    /**
     * 开启服务
     */
    public static void startService(Context mContext) {
        mContext.startService(new Intent(mContext, UdpService.class));
    }

    /**
     * 关闭服务
     */
    public static void stopService(Context context){
        Intent iService=new Intent(context, UdpService.class);
        iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.stopService(iService);
        Log.e("Udp","关闭服务");
    }

    /**
     * 获取网络状态
     */
    public boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
     * UdpService来拿这个接口
     */
    public void setiGetUdpMessageCallBack(IGetUdpMessageCallBack iGetUdpMessageCallBack) {
        this.iGetUdpMessageCallBack = iGetUdpMessageCallBack;
    }

    /**
     *Udp发送；发送数据，目标ip，目标端口号
     */
    public static void send(String data) throws IOException {
        udp.sendUdp(data);
    }

    /**
     * 在每次Service启动时调用,启动时调用init()函数初始化Udp通信
     */
    @SneakyThrows
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //初始化
        init();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        udp.closeUdp();
        Log.e("关闭UDP","完毕");
        stopSelf();
    }

    //初始化配置
    class InitThread extends Thread{
        @Override
        public void run() {
            while (!sharedPreferences.getBoolean("judge",false)){
                Log.e("初始化配置失败","请重新配置！");
                return;
            }
            editor.putBoolean("judge",false);

            //1.配置获取基本信息
            ip = sharedPreferences.getString
                    ("ip","空");
            port = sharedPreferences.getInt
                    ("port",0);

            editor.apply();

            //2.UDP初始化配置
            udp = new Udp();
            udp.setIp(ip);
            udp.setPort(port);
            udp.setContext(UdpService.this);

            //3.开启，恢复重连
            ReconnectionThread reconnectionThread = new ReconnectionThread();
            reconnectionThread.start();

            //4.安全检索
            NetwordThread networdThread = new NetwordThread();
            networdThread.start();
        }
    }

    //开启，恢复重连
    class ReconnectionThread extends Thread{
        @SneakyThrows
        @Override
        public void run() {

            //开启
            if (isConnectIsNomarl() && SignalIntensity.sameNetwork(ip)) {
                network = true;
                sameNetwork = true;
                LogParameters.Running(2,3,
                        "网络正常，在同一网段",
                        true
                );
                udp.OpenUdp(); //开启UDP通信,设置本地端口号
            }
            else if(!isConnectIsNomarl()){
                network = false;
                LogParameters.Running(2,3,
                        "网络检索异常",
                        false
                );
            }
            else if(!SignalIntensity.sameNetwork(ip)){
                sameNetwork = false;
                LogParameters.Running(2,3,
                        "网络检索异常",
                        false
                );
            }

            //重连
            while (true) {
                //网络连接正常
                if (isConnectIsNomarl()) {
                    //如果网络一开始是断开的
                    if (!network) {
                        network = true; //网络连接标志位置true
                        LogParameters.Running(2,3,
                                "网络正常",
                                true
                        );
                        if(SignalIntensity.sameNetwork(ip)){
                            sameNetwork = true;
                            udp.OpenUdp(); //开启UDP通信,设置本地端口号
                        }
                        else{
                            sameNetwork = false;
                            LogParameters.Running(2,3,
                                    "网段不同",
                                    false
                            );
                        }

                    }
                }
                //网络连接异常
                else {
                    //如果网络一开始是连接的
                    if (network) {
                        network = false; //网络连接标志位置false
                        udp.closeUdp(); //关闭UDP通信

                        LogParameters.Running(2,3,
                                "网络异常",
                                true
                        );
                    }
                }

                //监听
                udp.setOnUdpDataReceiveListener(new Udp.OnUdpDataReceiveListener() {
                    @Override
                    public void onDataReceive(String data) {
                        //TODO 1.底层数据封装
                        DataTransmissionProtocol dataTransmissionProtocol = new DataTransmissionProtocol();
                        dataTransmissionProtocol.setCpeTwoUid(SignalIntensity.getSERIAL());
                        dataTransmissionProtocol.setProtocolType(2);
                        dataTransmissionProtocol.setContent(data);

                        //TODO 2.转JSON字符串
                        data = InitCpeAnalysis.toJsonString(dataTransmissionProtocol);

                        //TODO 3.传入接口
                        iGetUdpMessageCallBack.onUdpReceive
                                (EncapsulationFormat.initializationEncapsulation(data,1));
                    }
                });

            }
        }
    }

    //安全检索
    class NetwordThread extends Thread{
        public void run() {
            Log.e("网络检索","~~~");
            while (true){
                if(network&&sameNetwork){
                    if(state == 0 || state == 1){
                        LogParameters.Running(2,3,
                                "安全检索:正确!网段正常信号良好!",
                                true
                        );
                        iGetUdpMessageCallBack.udpServiceSecurity(true);
                        state = 2;
                    }
                }
                else {
                    if(state == 0|| state == 2){
                        LogParameters.Running(2,3,
                                "安全检索:错误!网段异常信号异常",
                                true
                        );
                        iGetUdpMessageCallBack.udpServiceSecurity(false);
                        state = 1;
                    }
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new CustomBinder();
    }

    //让IBinder调用UdpService，从而让其他类能够获取到UdpService服务
    class CustomBinder extends Binder {
        public UdpService getUdpService() {
            return UdpService.this;
        }
    }


}
