package serviceTcp.tcp;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import db.log.LogParameters;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 项目负责人： 杨帆
 * 包名：      com.example.handleuse
 * 描述：      Netty客户端TCP通信
 * 编译环境：  JDK-1_8、SDK-8.0
 * 创建时间：  2021年 01月 24日 17时 49分
 */
public class NettyTcpClient implements NettyTcpInterface {

    private static final String TAG = "com.example.handleuse.NettyTcpClient";

    //与服务端建立连接的连接通道
    private Channel channel;

    private String ip;
    private int port;

    private Handler handler;

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        return handler;
    }

    /**
     * @return 初始化Boostrap 客户端引导程序,数据包通道，tcp通道类型，通道处理者，开启广播
     */
    private final Bootstrap getBootstrap() {
        EventLoopGroup group = new NioEventLoopGroup();
        return new Bootstrap().group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ClientChannelInitializer(this));
    }

    @SuppressLint("LongLogTag")
    @Override
    public void connect() {
        //不允许重复连接。
        if (channel != null && channel.isActive()) {
            Log.d(TAG, "不允许重复连接");
            disconnect();
        }
        ChannelFuture channelFuture = getBootstrap().connect(new InetSocketAddress(ip, port));
        channelFuture.addListener(new ChannelFutureListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (channelFuture.isSuccess()) {
                    channel = channelFuture.channel();
                    Log.d(TAG, "[TCP] client 连接服务端成功");
                    LogParameters.Running(3, 2,
                            "[TCP] client 连接服务端成功",
                            true);
                }
                if (!channelFuture.isSuccess()) {
                    channelFuture.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "[TCP] client 连接服务端失败，发起重连");
                            LogParameters.Running(3, 2,
                                    "[TCP] client 连接服务端失败，发起重连",
                                    true);
                            connect();
                        }
                    }, 5, TimeUnit.SECONDS);
                }
            }
        });
    }

    @SuppressLint("LongLogTag")
    @Override
    public void disconnect() {
        if (channel == null) {
            Log.e(TAG, "[TCP] client 请先连接TCP服务器");
        } else {
            channel.writeAndFlush("[TCP] client already disconnect");
            channel.close();
            LogParameters.Running(3, 2,
                    "[TCP] client already disconnect",
                    true);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void sendMsg(Object msg) {
        if (channel != null) {
            Log.d(TAG, "[TCP] sendMsg:" + msg.toString());
            channel.writeAndFlush(msg);
        } else {
            LogParameters.Running(3, 2,
                    "[TCP] No sendMsg 异常：连接尚未建立！",
                    false);
            Log.d(TAG, "[TCP] No sendMsg 异常：连接尚未建立！");
        }
    }

    @Override
    public boolean isConnect() {
        if (channel != null) {
            return true;
        } else {
            return false;
        }
    }

}
