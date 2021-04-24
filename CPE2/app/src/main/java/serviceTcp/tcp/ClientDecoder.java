package serviceTcp.tcp;

import java.util.List;

import android_serialport_api.PackageSerialPort.EncapsulationFormat;
import db.agreement.DataTransmissionProtocol;
import db.analyticMethod.InitCpeAnalysis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import tool.SignalIntensity;
import util.HexStringUtil;

/**
 * 项目名:    Netty-Android
 * 包名       com.azhon.netty.client
 * 文件名:    ClientDecoder
 * 创建时间:  2019-09-06 on 00:13
 * 描述:     TODO 解码器，对服务端端的数据进行解析
 *
 * @author 阿钟
 */

public class ClientDecoder extends ByteToMessageDecoder {
    private static final String TAG = "ClientDecoder";
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        int length = byteBuf.readableBytes();
        byte[] bytes = byteBuf.readBytes(length).array();
        DataTransmissionProtocol dataTransmissionProtocol = new DataTransmissionProtocol();
        dataTransmissionProtocol.setCpeTwoUid(SignalIntensity.getSERIAL());
        dataTransmissionProtocol.setProtocolType(3);
        dataTransmissionProtocol.setContent(HexStringUtil.toHexString(bytes));

        list.add(EncapsulationFormat.initializationEncapsulation
                (InitCpeAnalysis.toJsonString(dataTransmissionProtocol),1));
    }
}
