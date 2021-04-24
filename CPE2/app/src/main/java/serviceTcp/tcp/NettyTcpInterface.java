package serviceTcp.tcp;

/**
 * 项目负责人： 杨帆
 * 包名：      com.example.handleuse
 * 描述：      NettyTcpClient 接口
 * 编译环境：  JDK-1_8、SDK-8.0
 * 创建时间：  2021年 01月 24日 18时 02分
 */
public interface NettyTcpInterface  {
    void connect();
    void disconnect();
    void sendMsg(Object msg);
    boolean isConnect();
}
