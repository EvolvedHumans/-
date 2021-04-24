package serviceUdp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.communication.yang.cpe_2.MainActivity;

import db.log.LogParameters;
import lombok.Data;

/**
 * @Author : YangFan
 * @Date : 2020年10月13日 14:32
 * @effect :UdpService与MainActivity通信通道
 */

@Data
public class UdpServiceConnection implements ServiceConnection {

    /*
    UDP后台服务
     */
    private Context context;
    private UdpService udpService;
    private IGetUdpMessageCallBack iGetUdpMessageCallBack;

    /*
    Service服务连接
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        udpService = ((UdpService.CustomBinder)iBinder).getUdpService();
        udpService.setiGetUdpMessageCallBack(iGetUdpMessageCallBack);
        UdpService.startService(context);
    }

    /*
        Service服务断开
         */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        LogParameters.Running(2,2,
                "UDP配置服务:断开",
                true
        );
    }

    //给UdpService
    public UdpService getUdpService() {
        return udpService;
    }

    //放接口
    public void setiGetUdpMessageCallBack(IGetUdpMessageCallBack iGetUdpMessageCallBack) {
        this.iGetUdpMessageCallBack = iGetUdpMessageCallBack;
    }
}
