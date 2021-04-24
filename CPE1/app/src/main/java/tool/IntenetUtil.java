package tool;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class IntenetUtil {

    /**
     * 没有网路连接
     */
    public static final int NETWORK_NONE = 0;

    /**
     * wifi连接
     */
    public static final int NETWORK_WIFI = 1;

    /**
     * 手机网络数据连接类型
     */
    public static final int NETWORK_2G = 2;
    public static final int NETWORK_3G = 3;
    public static final int NETWORK_4G = 4;
    public static final int NETWORK_MOBILE = 5; //不知名类型
    public static final int NETWORK_ETHERNET = 6;//以太网

    /** 数据连接类型 **/
    public static int getNetworkState(Context context){

        /**
         * 获取系统的网络服务
         */
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        /**
         * 如果当前没有网络
         */
        if(connectivityManager == null)
            return NETWORK_NONE;

        /**
         * 获取当前的网络类型
         * 1.如果为空，返回无网络
         * 2.判断连接的是不是wifi
         * 3.如果不是wift，则判断当前连接的运营商是哪种网络
         */
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetInfo == null || !activeNetInfo.isAvailable()){
            return NETWORK_NONE;
        }

        //判断连接的是不是wifi
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if( wifiInfo != null){
            NetworkInfo.State state =  wifiInfo.getState();
            if(state!=null){
                if(state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING){
                    return NETWORK_WIFI;
                }
            }
        }

        //判断是几G网络
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(networkInfo != null){
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();
            if(state != null){
                if(state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING){
                    switch (activeNetInfo.getSubtype()){
                        //如果是2g类型
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORK_2G;

                        //如果是3g类型
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORK_3G;

                        //如果是4g类型
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORK_4G;
                        default:
                            if (strSubTypeName.equalsIgnoreCase("TD-SCDMA")
                                    || strSubTypeName.equalsIgnoreCase("WCDMA")
                                    || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                return NETWORK_3G;
                            } else {
                                return NETWORK_MOBILE;
                            }
                    }
                }
            }
        }

        //判断是否是以太网
        NetworkInfo EthernetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if (null != EthernetInfo){
            NetworkInfo.State state = EthernetInfo.getState();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORK_ETHERNET;
                }
        }

        //如果都不成立，则为不知名类型，相当于无网络
        return NETWORK_NONE;
    }

}
