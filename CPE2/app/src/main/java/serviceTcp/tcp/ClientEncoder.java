package serviceTcp.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import util.VariableTypeConversionUtil;

/**
 * 项目名:    Netty-Android
 * 包名       com.azhon.netty.client
 * 文件名:    ClientEncoder
 * 创建时间:  2019-09-07 on 23:28
 * 描述:     TODO 发送出去的数据编码器，字符串类型接收，十六进制类型发送
 * @author 杨帆
 */

public class ClientEncoder extends MessageToByteEncoder<String> {

    private static final String TAG = "ClientEncoder";

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String s, ByteBuf byteBuf) {
        byte[] sendData = VariableTypeConversionUtil.HexToByteArr(s);
        byteBuf.writeBytes(sendData);
    }
}