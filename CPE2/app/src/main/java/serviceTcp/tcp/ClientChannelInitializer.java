package serviceTcp.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * 项目负责人： 杨帆
 * 包名：      com.example.handleuse
 * 描述：      ChannelInitializer<SocketChannel>指定Handler
 * 编译环境：  JDK-1_8、SDK-8.0
 * 创建时间：  2021年 01月 24日 16时 59分
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    ClientChannelInboundHandler clientChannelInboundHandler;

    public ClientChannelInitializer(NettyTcpClient nettyTcpClient){
        clientChannelInboundHandler = new ClientChannelInboundHandler(nettyTcpClient);
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {

        //添加发送数据编码器
        socketChannel.pipeline().addLast("encoder",new ClientEncoder());
        //添加收到的数据解码器
        socketChannel.pipeline().addLast("decoder",new ClientDecoder());
        //添加事件处理器
        socketChannel.pipeline().addLast("clientChannelInboundHandler", clientChannelInboundHandler);
    }

}
