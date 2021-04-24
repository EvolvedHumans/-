package serviceTcp;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


import android_serialport_api.PackageSerialPort.EncapsulationFormat;
import android_serialport_api.SerialPortUtils;
import db.log.LogParameters;

import lombok.NonNull;
import lombok.SneakyThrows;
import serviceTcp.tcp.NettyTcpClient;


/**
 * @Author : YangFan
 * @Date : 2020年10月22日 11:56
 * @effect : TCP通信后台服务
 */

public class TcpClientService extends Service {

    String ip;
    Integer port;

    /*
    读取数据
     */
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static NettyTcpClient nettyTcpClient;

    /*
与MainActivity通信接口
 */
    private IGetTcpClientMessageCallBack iGetTcpClientMessageCallBack;

    /**
     *在UdpService中拿这个接口实例
     */
    public void setiGetTcpClientMessageCallBack(IGetTcpClientMessageCallBack iGetTcpClientMessageCallBack) {
        this.iGetTcpClientMessageCallBack = iGetTcpClientMessageCallBack;
    }



    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            //TODO 3.传入接口
            iGetTcpClientMessageCallBack.onTcpReceive
                    (String.valueOf(msg.obj));
        }
    };

    /**
     *开启服务
     */
    public static void startService(Context mContext){
        mContext.startService(new Intent(mContext,TcpClientService.class));
    }

    /**
     * 关闭服务
     */
    public static void stopService(Context context){
        Intent iService=new Intent(context, TcpClientService.class);
        iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.stopService(iService);
        LogParameters.Running(3,2,
                "TCP：关闭服务",
                true);
    }


    public static void send(String data) {
        nettyTcpClient.sendMsg(data);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!sharedPreferences.getBoolean("judge", false)) {
                    Log.e("初始化配置失败", "请重新配置！");
                    return;
                }
                editor.putBoolean("judge", false);

                //1.配置获取基本信息
                ip = sharedPreferences.getString
                        ("ip", "空");
                port = sharedPreferences.getInt
                        ("port", 0);

                editor.apply();

                //todo 创建TCP连接
                nettyTcpClient = new NettyTcpClient();
                nettyTcpClient.setIp(ip);
                nettyTcpClient.setPort(port);
                nettyTcpClient.setHandler(handler);
                nettyTcpClient.connect();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }


    @SneakyThrows
    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopself是停掉Service的方法,但线程不会被马上杀死，会运行完这一段
        nettyTcpClient.disconnect();
        stopSelf();
        Log.e("关闭TCP","完毕");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new CustomBinder();
    }

    //让IBinder调用TcpClientService,从而让其他类能够获取到TcpClientService服务
    class CustomBinder extends Binder {
        public TcpClientService getTcpClientService() {
            return TcpClientService.this;
        }
    }

    /**
     * 接口，将数据传到MainActivity
     */
    public OnTcpClientDataReceiveListener onTcpClientDataReceiveListener = null;

    public static interface OnTcpClientDataReceiveListener {
        public void onDataReceive(String data);
    }

    public void setOnTcpClientDataReceiveListener(OnTcpClientDataReceiveListener onTcpClientDataReceiveListener) {
        this.onTcpClientDataReceiveListener = onTcpClientDataReceiveListener;
    }
}
