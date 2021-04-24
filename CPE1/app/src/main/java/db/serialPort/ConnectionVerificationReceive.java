package db.serialPort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2020年11月12日 14:16
 * @effect :串口连接验证,接收封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class ConnectionVerificationReceive {
    /**
     * SYN = 1 接收连接
     */
    private Integer syn;

    /**
     * ACK = 1 对方确认连接
     */
    private Integer ack;

    /**
     * seq 对方确认推送次数
     */
    private Integer seqq;

    /**
     * ack 我的推送次数+1
     */
    private Integer ackk;
}
