package serviceUdp;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import db.log.LogParameters;
import db.log.RunningLog;
import lombok.Data;
import lombok.SneakyThrows;
import tool.SignalIntensity;
import tool.Timestamp;

/**
 * @Author : YangFan
 * @Date : 2020年09月24日 17:18
 * @effect :UDP通信
 */
@Data
public class Udp {
    public boolean threadStatus; //线程状态，为了安全终止线程
    private String ip; //外部ip地址
    private Integer port; //外部端口号
    private Context context; //上下文
    private DatagramSocket socket;

    /**
     * 获取IP地址
     */
    public String getLocalIpAddress(Context context){
        try {
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return int2ip(wifiInfo.getIpAddress());
        }catch (Exception e){
            return "获取IP失败，请保证WIFI连接，或者重新打开网络"+e.getMessage();
        }
    }

    /**
     * 将ip的整数形式转换成ip形式
     */
    public String int2ip(int ip){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ip & 0xff).append(".");
        stringBuilder.append((ip >> 8) & 0xff).append(".");
        stringBuilder.append((ip >> 16) & 0xff).append(".");
        stringBuilder.append((ip >> 24) & 0xff);
        return stringBuilder.toString();
    }

    /**
     * 打开UDP通道，定UDP端口
     **/
    //当自定义的端口被占用，会出现异常情况
    public void OpenUdp() throws IOException{

        this.threadStatus = false; //线程状态

        socket = new DatagramSocket();//创建udp端口

        Log.e("UDP配置","完毕~");

        Log.e("本地配置","如下->>");
        Log.e("localIp", getLocalIpAddress(context));
        Log.e("localPort", String.valueOf(socket.getLocalPort()));

        Log.e("推送端口","如下->>");
        Log.e("Ip",ip);
        Log.e("Port", String.valueOf(port));
        Log.e("监听通讯","开启");


        LogParameters.Running(2,1,
                "UDP配置，完毕~"+"\n"+
                        "本地配置,如下->>"+"\n"+
                        "localIp:"+getLocalIpAddress(context)+"\n"+
                        "localPort:"+socket.getLocalPort()+"\n"+
                        "推送端口:如下->>"+"\n"+
                        "IP:"+ip+"\n"+
                        "Port:"+port+"\n"+
                        "监听通讯:开启",
                true
        );

        //线程反复读
        new ReadThread().start(); //开启监控监听传过来的数据
    }

    /**
     * 关闭UDP
     */
    public void closeUdp(){

        this.threadStatus = true; //线程状态

        socket.close();
    }

    //"发送失败，检查目标ip是否在同一局域网或网络无异常"
    public synchronized void sendUdp(String data) throws IOException {

        InetAddress inetAddress = InetAddress.getByName(ip); //判断目标ip与本地ip是否在一个局域网内

        DatagramPacket request = new DatagramPacket(data.getBytes("Utf8"), data.length(), inetAddress, port);

        socket.send(request);
    }

    /**
     * 单开一线程，来读数据
     */
    private class ReadThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {

            int maxLength = 2048;//定义一个包的最大长度

            while (!threadStatus) {

                byte[] bytes = new byte[maxLength];

                //封装成包
                DatagramPacket request = new DatagramPacket(bytes, bytes.length);

                socket.receive(request);//接收数据

                //解析数据
                String s = new String(request.getData(), 0, request.getLength(), "Utf8");

                onUdpDataReceiveListener.onDataReceive(s);  //数据通过接口传过去
            }
        }
    }

    /*
    接口,将数据传到MainActivity
    */
    public OnUdpDataReceiveListener onUdpDataReceiveListener = null;

    public static interface OnUdpDataReceiveListener {
        public void onDataReceive(String data);
    }

    public void setOnUdpDataReceiveListener(OnUdpDataReceiveListener onUdpDataReceiveListener) {
        this.onUdpDataReceiveListener = onUdpDataReceiveListener;
    }

}
