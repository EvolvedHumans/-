package android_serialport_api;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import android_serialport_api.PackageSerialPort.EncapsulationFormat;
import db.log.LogParameters;
import db.log.RunningLog;
import lombok.SneakyThrows;
import tool.SignalIntensity;
import tool.Timestamp;

public class SerialPortUtils extends Activity {

    private final String TAG = "SerialPortUtils";
    public boolean serialPortStatus; //是否打开串口标志
    public boolean threadStatus; //线程状态，为了安全终止线程
    public SerialPort serialPort = null;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    public Integer byteRate = 11520;
    /**
     * 打开串口
     *
     * @return serialPort串口对象
     */
    public SerialPort openSerialPort(String path, int baudrate) {
        try {
            serialPort = new SerialPort(new File(path), baudrate, 0);
            //不停开启，等待它实例化
            while (serialPort == null) {
                //如果为空则，等待连接上
                serialPort = new SerialPort(new File(path), baudrate, 0);
                Thread.sleep(5000);//5s
            }
            threadStatus = false; //线程状态
            this.serialPortStatus = true;
            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            new ReadThread().start(); //开始线程监控是否有数据要接收
        } catch (IOException | InterruptedException e) {
            if (serialPortStatus) {
                serialPortStatus = false;
                //开启串口异常，进程可能会崩溃
                LogParameters.Running(3,
                        "打开串口异常，可能是设备未授权",
                        false
                );
            }
            return serialPort;
        }
        return serialPort;
    }


    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        try {
            inputStream.close();
            outputStream.close();

            this.serialPortStatus = false;
            this.threadStatus = true; //线程状态
            serialPort.close();
        } catch (IOException e) {
            LogParameters.Running(3,
                    "closeSerialPort: 关闭串口异常：" + e.toString(),
                    false
            );
        }
    }

    public static String a(double data) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.000000");
        return decimalFormat.format(data);
    }

    /**
     * 发送串口指令（字符串）
     *
     * @param data String数据指令
     */
    public void sendSerialPort(String data) throws InterruptedException {
        try {
            byte[] sendData = data.getBytes(StandardCharsets.UTF_8); //string转byte[]
            byte[] head = {0x53, 0x5A, 0x59};
            byte[] end = {0x50, 0x26};
            if (sendData.length > 0) {
                outputStream.write(head);
                outputStream.write(sendData);
                outputStream.write(end);
                outputStream.flush();
            }
        } catch (IOException e) {
            //关闭读取线程
            threadStatus = true;
            Log.e(TAG, "sendSerialPort: " + e.toString());
            LogParameters.Running(0,
                    "无法通过串行端口发送数据。进程可能会崩溃",
                    false
            );
            //如果发送数据失败，则关闭串口
            closeSerialPort();
        }
        //data+5个字节
        double a = data.length() + 3 + 2;
        Log.e("发送字节", String.valueOf((int) a));
        //发送速度s级别
        double b = a / byteRate;
        Log.e("发送速度", a(b) + "s");
        //发送速度ms级别
        double c = a / byteRate * 1000;
        Log.e("发送速度", a(c) + "ms" + "\n");
        //取整
        int d = (int) c;
        //休眠时间
        ++d;
        Log.e("阻塞时间", d + "ms");
        Log.e("分割", "____________________");
        Thread.sleep(d + 100);
    }

    /**
     * 单开一线程，来读数据
     */
    private class ReadThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            int maxLength = 16777221;//定义一个包的最大长度
            byte[] buffer = new byte[maxLength];
            byte[] byte_buffer = null;
            int bytes = 0;//字符串长度(当前已经收到包的长度)
            int ch; //读取字符的变量
            int headerLength = 3; //帧头 0x53,0x5A,0x59
            int tailLenght = 2; //帧尾 0x50 0x26

            while (!threadStatus) {
                try {
                    bytes = 0;//字符串长度
                    //防止溢出,如果不溢出
                    while (bytes < maxLength) {
                        //Log.i("bytes","次数:"+bytes);
                        //循环获取字节流
                        //如果读到的是0x26，那么相当于读到了终止位
                        if ((ch = inputStream.read()) != 0x26) {
                            if (ch != -1) {
                                buffer[bytes] = (byte) ch; //将读取到的字符写入
                                bytes++;
                                continue; //重新循环
                            }
                        }

                        //java.lang.ArrayIndexOutOfBoundsException——数组越界
                        //错误原因：bytes>buffer最大数组长，
                        //否则，则读取到了结束位,验证帧尾完整性
                        if (!tailLen(buffer, bytes)) {
                            //如果不是，那么则加上0x26
                            buffer[bytes] = 0x26;
                            bytes++;
                            continue;
                        }

                        //去掉帧尾
                        --bytes;

                        byte_buffer = new byte[bytes];

                        for (int i = 0; i < bytes; i++) {
                            byte_buffer[i] = buffer[i];
                        }
                        break; //运行到这，跳出循环
                    }
                } catch (IOException e) {
                    //如果读取失败，发送日志，并关闭读取线程
                    LogParameters.Running(0,
                            "无法读取串行端口。关闭串行端口，等待重新连接",
                            false
                    );

                    closeSerialPort();
                }

                //记录起始位前的无用字节数
                int cursor = 0;

                //如果收到的包的长度大于起始位，那么则解析当前包
                //假设bytes全为有效字节
                while (bytes >= headerLength) {
                    if (byte_buffer == null) {
                        //todo 如何出现了异常，并且时间过多，那么关闭线程,待会处理
                        LogParameters.Running(0,
                                "串口出现了异常，请立马关机。！",
                                false
                        );
                        break;
                    }

                    //清除起始位前的无用字节
                    if (byte_buffer[cursor] != 0x53) {
                        ++cursor; //无用字节加一
                        --bytes; //有效字节减一
                        continue;
                    }

                    //直到读取到0x53为止
                    /*
                    已知起始位前的截断无用字节cursor和包含起始位的有效字节bytes
                    1.开始帧头效验
                    (1.)true 截取帧头 获取实际发送包
                    (2.)false 继续当做无用字节丢弃， continue，重新循环
                     */
                    int contentLenght = parseLen(byte_buffer, cursor, headerLength);

                    //此时并非上我们所需要的帧头，记录到无用字节中
                    if (contentLenght == 0) {
                        ++cursor; //无用字节加一
                        --bytes; //有效字节减一
                        LogParameters.Running(0,
                                "此时并非上我们所需要的帧头，记录到无用字节中,重载",
                                false
                        );
                        continue;
                    }

                    //maxLength-5 为总数据减去终止位和起始位等于最大内容长度，如果内容包大于这个长度，则说面这个包有问题，丢弃
                    if (contentLenght < 0 || contentLenght > maxLength - 3) {
                        bytes = 0; //将数据位置零，丢包
                        LogParameters.Running(0,
                                "内容包大于总数据减去终止位和起始位等于最大内容长度，丢包",
                                false
                        );
                        break;
                    }

                    int factPackLen = contentLenght + 3;

                    //如果当前获取到长度小于整个包的长度，则跳出循环等待接收数据
                    if (bytes < contentLenght + 3) {
                        LogParameters.Running(0,
                                "当前获取到长度小于整个包的长度，丢包",
                                false
                        );
                        break;
                    }

                    onDataReceiveListener.onDataReceive(byte_buffer, contentLenght, cursor, headerLength);

                    //总长度 - 需要发送的包的总长 = 残余包长
                    bytes -= factPackLen;

                    //残留字节移到缓冲区首
                    if (bytes > 0) {
                        System.arraycopy(buffer, cursor, buffer, 0, bytes);
                        LogParameters.Running(0,
                                "残余包:" + new String(buffer),
                                false
                        );
                    }
                }

            }
        }
    }

    /**
     * 判断帧尾的完整性
     * 其中结束符不算进总byte[]中,因此只需要判断bytes-1位符不符合验证符就好了
     *
     * @return 帧尾的校验结果
     * @java.lang.ArrayIndexOutOfBoundsException——数组越界
     */
    public boolean tailLen(byte[] buffer, int index) {

        //当index为0时，第一项就读到了终止位，如果放到了下一步，会出现上述备注出现的问题
        if (index == 0) {
            return false;
        }

        byte a = buffer[index - 1];
        return a == 0x50;
    }

    /**
     * 获取协议内容长度
     * 总字节
     * 无用字节数
     * 校验字节数
     * 总字节 - 无用字节数 = 有效字节数
     * 有效字节数 = 校验字节数 + 数据字节
     *
     * @param
     * @return
     */
    public int parseLen(byte[] buffer, int index, int headerLength) {
        /*
        后两位帧头校验
         */
        byte a = buffer[index + 1];
        byte b = buffer[index + 2];
        int length = 0;

        if (a == 0x5A && b == 0x59) {
            //获取有效数据包
            //去掉帧头
            length = buffer.length - index - headerLength; //有效帧头长度
        }

        return length;
    }

    public OnDataReceiveListener onDataReceiveListener = null;

    public interface OnDataReceiveListener {
        void onDataReceive(byte[] buffer, int contentLenght, int cursor, int headerLength) throws UnsupportedEncodingException;
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

}
