package android_serialport_api.PackageSerialPort;

import android.util.Log;

import java.nio.Buffer;
import java.util.zip.CRC32;

import util.CRCUtil;

/**
 * @Author : YangFan
 * @Date : 2020年11月02日 11:40
 * @effect : 串口数据传输封装
 */
public class EncapsulationFormat {

    /**
     * 串口发送数据的封装,消息类型+数据位+校验位
     * 对原始数据进行的封装
     */
    public static String initializationEncapsulation(String data,Integer type){

        String encapsulatingData = null;

        String TAG = "串口数据传输封装";
        String initializationConfiguration ="00"+data;
        String sendData ="11"+data;
        String journal ="22"+data;
        String heartbeat ="33"+data;
        String serialConnectionVerification = "44"+data;

        switch (type){
            /*
            初始化配置消息
             */
            case 0:{
                encapsulatingData = initializationConfiguration + CRCUtil.getCRC32(initializationConfiguration);
                break;
            }

            /*
            数据
             */
            case 1:{
                encapsulatingData = sendData + CRCUtil.getCRC32(sendData);
                break;
            }

            /*
            日志
             */
            case 2:{
                encapsulatingData = journal + CRCUtil.getCRC32(journal);
                break;
            }

            /*
            心跳
             */
            case 3:{
                encapsulatingData = heartbeat + CRCUtil.getCRC32(heartbeat);
                break;
            }

            /*
            串口连接校验
             */
            case 4:{
                encapsulatingData = serialConnectionVerification + CRCUtil.getCRC32(serialConnectionVerification);
                break;
            }

            /*
           todo 非法赋值,留有隐患,经过此处为空值
             */
            default:
                Log.d(TAG,"超出0~4");
                break;
        }

        return encapsulatingData;
    }

    /*
    截取CRC校验
     */
    public static String interceptCRC(String data){
        try {
            return data.substring(data.length()-8);
        } catch (Exception e) {
            Log.e("调用截取CRC校验","超出索引范围");
            return "";
        }
    }

    /*
    将CRC32校验位断开
     */
    public static String interceptDataBits(String data){
        try {
            return data.substring(0,data.length()-8);
        } catch (Exception e) {
            Log.e("截取数据位+消息类型","超出索引范围");
            return "";
        }
    }

    /*
    截取消息类型,截取前两位
     */
    public static String interceptMessageType(String data){
        try {
            String buffer = data.substring(0,2);
            switch (buffer){
                case "00":return buffer;
                case "11":return buffer;
                case "22":return buffer;
                case "33":return buffer;
                case "44":return buffer;
                default:Log.e("截取消息类型","异常");return "";
            }
        } catch (Exception e) {
            Log.e("截取消息类型","超出索引范围");
            return "";
        }
    }

    /*
    消息类型判断
     */
    public static Integer messageTypeJudgment(String messageBit){
        switch (messageBit){
            case "00":return 0;
            case "11":return 1;
            case "22":return 2;
            case "33":return 3;
            case "44":return 4;
            default:{
                Log.e("messageTypeJudgment","找不到协议类型,传入消息类型异常");
                return 5;
            }
        }
    }

    /*
    截取数据位,在去除CRC校验位情况下
     */
    public static String interceptData(String data){

        try {
            return data.substring(2);
        } catch (Exception e) {
            Log.e("截取数据位","超出索引范围");
            return "";
        }

    }

    /*
    CRC校验判断
     */
    public static boolean judgeCRC(String data){
        try {
            return CRCUtil.getCRC32(interceptDataBits(data)).equals(interceptCRC(data));
        } catch (Exception e) {
            Log.e("截取消息类型","超出索引范围");
            return false;
        }
    }


}
