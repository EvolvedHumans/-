package serviceTcp.tcp;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import db.log.LogParameters;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.SneakyThrows;

/**
 * 项目负责人： 杨帆
 * 包名：      com.example.handleuse
 * 描述：      SimpleChannelInboundHandler<Object>方法改写方法，这里是消息处理器
 * 编译环境：  JDK-1_8、SDK-8.0
 * 创建时间：  2021年 01月 24日 16时 50分
 */
public class ClientChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    private static final String TAG = "com.example.handleuse.ClientChannelInboundHandler";

    private NettyTcpClient nettyTcpClient;

    private int count =0;

    public ClientChannelInboundHandler(NettyTcpClient nettyTcpClient){
        this.nettyTcpClient = nettyTcpClient;
    }

    /**
     * 建立连接成功后回调方法
     *
     * @param ctx
     * @throws Exception
     */
    @SuppressLint("LongLogTag")
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.d(TAG, "[TCP] client channel is ready!");
        LogParameters.Running(3,2,
                "[TCP] client channel is ready!",
                true);
        super.channelActive(ctx);
    }


    @SuppressLint("LongLogTag")
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) {
        Log.d(TAG, "[TCP] client 收到的消息是：" + ((o == null) ? "null" : o.toString()));
        Log.d("打印rt", String.valueOf(++count));
        Log.d("打印msg", ((o == null) ? "null" : o.toString()));

        nettyTcpClient.getHandler().obtainMessage(++count, ((o == null) ? "null" : o.toString())).sendToTarget();
    }

    /**
     * 未能成功建立连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ctx.channel().eventLoop().schedule(new Runnable() {
            @SuppressLint("LongLogTag")
            @SneakyThrows
            @Override
            public void run() {
                if(nettyTcpClient.isConnect()){
                    Log.d(TAG, "[TCP] client 断开连接,.........5s重连一次");
                    LogParameters.Running(3,2,
                            "[TCP] client 断开连接,.........5s重连一次",
                            true);
                    nettyTcpClient.connect();
                }
            }
        }, 5, TimeUnit.SECONDS);
    }

    /**
     * 心跳包发送
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @SuppressLint("LongLogTag")
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                Log.d(TAG, "[TCP] HEART");
                ctx.channel().writeAndFlush("客户端发送心跳成功");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
