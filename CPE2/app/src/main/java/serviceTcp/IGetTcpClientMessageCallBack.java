package serviceTcp;

/**
 * @Author : YangFan
 * @Date : 2020年10月22日 11:58
 * @effect :
 */
public interface IGetTcpClientMessageCallBack {
    /*
    TCP消息接收接口
     */
    public void onTcpReceive(String data);

    /*
    TCP服务安全状态
     */
    public void tcpServiceSecurity(boolean state);
}
