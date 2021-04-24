package db.serialPort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : YangFan
 * @Date : 2020年11月12日 17:42
 * @effect :串口连接验证,第二次推送封装
 */

@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
public class ConnectionVerificationPublishTwo {
    /**
     * 推送
     */
    private Integer seqq;

    /**
     * ACK = 1 对方确认连接
     */
    private Integer ack;

    /**
     * ack 我的推送次数+1
     */
    private Integer ackk;
}
