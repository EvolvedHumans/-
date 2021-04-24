package serviceTcp;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import lombok.Data;

/**
 * @Author : YangFan
 * @Date : 2020年10月22日 11:59
 * @effect :
 */
@Data
public class TcpClientServiceConnection implements ServiceConnection {

    /*
    TCP后台服务
     */
    private Context context;
    private TcpClientService tcpClientService;
    private IGetTcpClientMessageCallBack iGetTcpClientMessageCallBack;

    /*
    Service服务连接
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        tcpClientService = ((TcpClientService.CustomBinder)iBinder).getTcpClientService();
        tcpClientService.setiGetTcpClientMessageCallBack(iGetTcpClientMessageCallBack);
        TcpClientService.startService(context);
    }

    /*
    Service服务断开
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    //给TcpClientService
    public TcpClientService getTcpClientService(){return tcpClientService;}

    //放接口
    public void setiGetTcpClientMessageCallBack(IGetTcpClientMessageCallBack iGetTcpClientMessageCallBack){
        this.iGetTcpClientMessageCallBack = iGetTcpClientMessageCallBack;
    }
}
