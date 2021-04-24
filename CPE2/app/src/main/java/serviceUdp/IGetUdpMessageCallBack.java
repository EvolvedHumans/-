package serviceUdp;

/**
 * @Author : YangFan
 * @Date : 2020年10月13日 17:52
 * @effect :
 */
public interface IGetUdpMessageCallBack {
    /*
    UDP消息接收接口
     */
    public void onUdpReceive(String data);

    /*
    UDP服务安全状态
     */
    public void udpServiceSecurity(boolean state);
}
